package net.jacksum.zzadopt.org.bouncycastle.crypto.engines;

import net.jacksum.zzadopt.org.bouncycastle.crypto.CryptoServicePurpose;

class Utils
{
    static CryptoServicePurpose getPurpose(boolean forEncryption)
    {
        return forEncryption ? CryptoServicePurpose.ENCRYPTION : CryptoServicePurpose.DECRYPTION;
    }
}
