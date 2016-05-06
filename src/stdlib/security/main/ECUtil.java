package stdlib.security.main;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import stdlib.math.crypto.params.AsymmetricKeyParameter;
import stdlib.math.crypto.params.ECDomainParameters;
import stdlib.math.crypto.params.ECPrivateKeyParameters;
import stdlib.math.crypto.params.ECPublicKeyParameters;
import stdlib.security.main.ECPrivateKey;
import stdlib.security.main.ECPublicKey;
import stdlib.security.main.ECParameterSpec;

/**
 * utility class for converting jce/jca ECDSA, ECDH, and ECDHC
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class ECUtil
{
    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPublicKey)
        {
            ECPublicKey    k = (ECPublicKey)key;
            ECParameterSpec s = k.getParams();

            return new ECPublicKeyParameters(
                            k.getQ(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN()));
        }

        throw new InvalidKeyException("can't identify EC public key.");
    }

    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPrivateKey)
        {
            ECPrivateKey    k = (ECPrivateKey)key;
            ECParameterSpec s = k.getParams();

            return new ECPrivateKeyParameters(
                            k.getD(),
                            new ECDomainParameters(s.getCurve(), s.getG(), s.getN()));
        }
                        
        throw new InvalidKeyException("can't identify EC private key.");
    }
}
