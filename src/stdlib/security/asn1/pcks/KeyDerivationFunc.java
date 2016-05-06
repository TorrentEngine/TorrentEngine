package stdlib.security.asn1.pcks;

import stdlib.security.asn1.ASN1Sequence;
import stdlib.security.asn1.x509.AlgorithmIdentifier;

public class KeyDerivationFunc
    extends AlgorithmIdentifier
{
    KeyDerivationFunc(
        ASN1Sequence  seq)
    {
        super(seq);
    }
}
