package stdlib.math.crypto.generators;

import stdlib.math.crypto.CipherKeyGenerator;
import stdlib.math.crypto.params.DESParameters;

public class DESKeyGenerator
    extends CipherKeyGenerator
{
    public byte[] generateKey()
    {
        byte[]  newKey = new byte[DESParameters.DES_KEY_LENGTH];

        do
        {
            random.nextBytes(newKey);

            DESParameters.setOddParity(newKey);
        }
        while (DESParameters.isWeakKey(newKey, 0));

        return newKey;
    }
}
