import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.AsconDigest;
import org.bouncycastle.crypto.digests.AsconXof;
import org.bouncycastle.crypto.digests.PhotonBeetleDigest;
import org.bouncycastle.crypto.digests.SparkleDigest;
import org.bouncycastle.crypto.digests.XoodyakDigest;

/**
 * Checks the stripped Bouncy Castle tree against the official LWC hash known answer tests
 * (LWC_HASH_KAT_*.txt from https://github.com/bcgit/bc-test-data).
 * Usage: java -cp <classes>:<this> LwcKat <kat-dir>
 * Exits with 1 if any vector fails.
 */
public class LwcKat {

    // file name in the KAT directory -> digest under test
    static Digest create(String file) {
        switch (file) {
            case "ascon_asconhash_LWC_HASH_KAT_256.txt":  return new AsconDigest(AsconDigest.AsconParameters.AsconHash);
            case "ascon_asconhasha_LWC_HASH_KAT_256.txt": return new AsconDigest(AsconDigest.AsconParameters.AsconHashA);
            case "ascon_asconxof_LWC_HASH_KAT_256.txt":   return new AsconXof(AsconXof.AsconParameters.AsconXof);
            case "ascon_asconxofa_LWC_HASH_KAT_256.txt":  return new AsconXof(AsconXof.AsconParameters.AsconXofA);
            case "photonbeetle_LWC_HASH_KAT_256.txt":     return new PhotonBeetleDigest();
            case "sparkle_LWC_HASH_KAT_256.txt":          return new SparkleDigest(SparkleDigest.SparkleParameters.ESCH256);
            case "sparkle_LWC_HASH_KAT_384.txt":          return new SparkleDigest(SparkleDigest.SparkleParameters.ESCH384);
            case "xoodyak_LWC_HASH_KAT_256.txt":          return new XoodyakDigest();
            default: return null;
        }
    }

    static byte[] fromHex(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return b;
    }

    static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02X", x));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        boolean allOk = true;
        List<Path> files = new ArrayList<>();
        try (var stream = Files.list(Path.of(args[0]))) {
            stream.filter(p -> p.toString().endsWith(".txt")).sorted().forEach(files::add);
        }
        for (Path file : files) {
            String name = file.getFileName().toString();
            if (create(name) == null) {
                System.out.println("SKIP " + name + " (no digest mapped)");
                continue;
            }
            int ok = 0;
            List<String> failed = new ArrayList<>();
            String count = null, msg = null;
            for (String line : Files.readAllLines(file)) {
                line = line.trim();
                if (line.startsWith("Count")) {
                    count = line.substring(line.indexOf('=') + 1).trim();
                } else if (line.startsWith("Msg")) {
                    msg = line.substring(line.indexOf('=') + 1).trim();
                } else if (line.startsWith("MD")) {
                    String expected = line.substring(line.indexOf('=') + 1).trim();
                    byte[] m = fromHex(msg);
                    // 1st: a fresh instance, 2nd: the same instance after reset() and byte-wise update
                    Digest d = create(name);
                    d.update(m, 0, m.length);
                    byte[] out = new byte[d.getDigestSize()];
                    d.doFinal(out, 0);
                    d.reset();
                    for (byte x : m) {
                        d.update(x);
                    }
                    byte[] out2 = new byte[d.getDigestSize()];
                    d.doFinal(out2, 0);
                    if (toHex(out).equals(expected) && toHex(out2).equals(expected)) {
                        ok++;
                    } else {
                        failed.add(count);
                    }
                }
            }
            System.out.println((failed.isEmpty() ? "OK   " : "FAIL ") + name + ": " + ok + " passed"
                    + (failed.isEmpty() ? "" : ", failed Count = " + failed));
            allOk &= failed.isEmpty() && ok > 0;
        }
        System.exit(allOk ? 0 : 1);
    }
}
