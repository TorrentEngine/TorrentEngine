package stdlib.security.main;

import java.security.SecureRandom;

/**
 * all BC provider keystores implement this interface.
 */
public interface BCKeyStore
{
    /**
     * set the random source for the key store
     */
    public void setRandom(SecureRandom random);
}
