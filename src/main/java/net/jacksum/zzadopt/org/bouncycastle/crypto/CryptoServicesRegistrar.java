// JACKSUM-MOD: stub that replaces Bouncy Castle's CryptoServicesRegistrar.
//   Upstream, the digests call CryptoServicesRegistrar.checkConstraints(...) in their constructors.
//   The real registrar pulls in ~170 further classes (ASN.1, EC math, DH/DSA parameters, ...) that
//   Jacksum does not need. Jacksum does not use BC's service constraints, so checkConstraints() is a
//   no-op here. Only the methods that are referenced by the stripped tree are provided; if a new BC
//   version calls another method, update-bc.sh fails to compile and this stub must be extended.
//   Upstream: replaces the file of every BC version | Remove when: never (by design)
package net.jacksum.zzadopt.org.bouncycastle.crypto;

/**
 * Minimal replacement of Bouncy Castle's CryptoServicesRegistrar for Jacksum.
 */
public final class CryptoServicesRegistrar
{
    private CryptoServicesRegistrar()
    {
    }

    /**
     * Does nothing, Jacksum does not use Bouncy Castle's crypto service constraints.
     *
     * @param cryptoService the service to be checked.
     */
    public static void checkConstraints(CryptoServiceProperties cryptoService)
    {
    }
}
