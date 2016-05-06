package stdlib.security.asn1.x9;

import java.math.BigInteger;

import stdlib.security.asn1.ASN1EncodableVector;
import stdlib.security.asn1.ASN1Sequence;
import stdlib.security.asn1.DEREncodable;
import stdlib.security.asn1.DERInteger;
import stdlib.security.asn1.DERObject;
import stdlib.security.asn1.DERObjectIdentifier;
import stdlib.security.asn1.DERSequence;

/**
 * ASN.1 def for Elliptic-Curve Field ID structure. See
 * X9.62, for further details.
 */
public class X9FieldID
    implements DEREncodable, X9ObjectIdentifiers
{
    private DERObjectIdentifier     id;
    private DERObject               parameters;

	public X9FieldID(
        DERObjectIdentifier id,
		BigInteger          primeP)
	{
        this.id = id;
        this.parameters = new DERInteger(primeP);
	}

    public X9FieldID(
        ASN1Sequence  seq)
    {
        this.id = (DERObjectIdentifier)seq.getObjectAt(0);
        this.parameters = (DERObject)seq.getObjectAt(1);
    }

    public DERObjectIdentifier getIdentifier()
    {
        return id;
    }

    public DERObject getParameters()
    {
        return parameters;
    }

    /**
     * Produce a DER encoding of the following structure.
     * <pre>
     *  FieldID ::= SEQUENCE {
     *      fieldType       FIELD-ID.&amp;id({IOSet}),
     *      parameters      FIELD-ID.&amp;Type({IOSet}{&#64;fieldType})
     *  }
     * </pre>
     */
    public DERObject getDERObject()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(this.id);
        v.add(this.parameters);

        return new DERSequence(v);
    }
}
