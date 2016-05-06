package stdlib.security.main;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import stdlib.math.crypto.params.AsymmetricKeyParameter;
import stdlib.math.crypto.params.ElGamalParameters;
import stdlib.math.crypto.params.ElGamalPrivateKeyParameters;
import stdlib.math.crypto.params.ElGamalPublicKeyParameters;
import stdlib.security.main.ElGamalPrivateKey;
import stdlib.security.main.ElGamalPublicKey;

/**
 * utility class for converting jce/jca ElGamal objects
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class ElGamalUtil
{
    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ElGamalPublicKey)
        {
            ElGamalPublicKey    k = (ElGamalPublicKey)key;

            return new ElGamalPublicKeyParameters(k.getY(),
                new ElGamalParameters(k.getParams().getP(), k.getParams().getG()));
        }

        throw new InvalidKeyException("can't identify ElGamal public key.");
    }

    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ElGamalPrivateKey)
        {
            ElGamalPrivateKey    k = (ElGamalPrivateKey)key;

            return new ElGamalPrivateKeyParameters(k.getX(),
                new ElGamalParameters(k.getParams().getP(), k.getParams().getG()));
        }
                        
        throw new InvalidKeyException("can't identify ElGamal private key.");
    }
}
