package stdlib.math.crypto.params;

import stdlib.math.crypto.CipherParameters;

public class AsymmetricKeyParameter
	implements CipherParameters
{
    boolean privateKey;

    public AsymmetricKeyParameter(
        boolean privateKey)
    {
        this.privateKey = privateKey;
    }

    public boolean isPrivate()
    {
        return privateKey;
    }
}
