package net.jacksum.zzadopt.org.bouncycastle.crypto;

import net.jacksum.zzadopt.org.bouncycastle.crypto.digests.EncodableDigest;
import net.jacksum.zzadopt.org.bouncycastle.util.Memoable;

/**
 * Extended digest which provides the ability to store state and
 * provide an encoding.
 */
public interface SavableDigest
    extends ExtendedDigest, EncodableDigest, Memoable
{
}
