package stdlib.math.crypto.generators;

import java.math.BigInteger;

import stdlib.math.crypto.AsymmetricCipherKeyPair;
import stdlib.math.crypto.AsymmetricCipherKeyPairGenerator;
import stdlib.math.crypto.KeyGenerationParameters;
import stdlib.math.crypto.params.ElGamalKeyGenerationParameters;
import stdlib.math.crypto.params.ElGamalParameters;
import stdlib.math.crypto.params.ElGamalPrivateKeyParameters;
import stdlib.math.crypto.params.ElGamalPublicKeyParameters;

/**
 * a ElGamal key pair generator.
 * <p>
 * This generates keys consistent for use with ElGamal as described in
 * page 164 of "Handbook of Applied Cryptography".
 */
public class ElGamalKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private ElGamalKeyGenerationParameters param;

    public void init(
        KeyGenerationParameters param)
    {
        this.param = (ElGamalKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        BigInteger           p, g, x, y;
        int                  qLength = param.getStrength() - 1;
        ElGamalParameters    elParams = param.getParameters();

        p = elParams.getP();
        g = elParams.getG();
    
        //
        // calculate the private key
        //
		x = new BigInteger(qLength, param.getRandom());

        //
        // calculate the public key.
        //
        y = g.modPow(x, p);

        return new AsymmetricCipherKeyPair(
                new ElGamalPublicKeyParameters(y, elParams),
                new ElGamalPrivateKeyParameters(x, elParams));
    }
}
