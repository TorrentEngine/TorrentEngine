package stdlib.security.asn1.pcks;

import stdlib.security.asn1.ASN1EncodableVector;
import stdlib.security.asn1.ASN1Sequence;
import stdlib.security.asn1.ASN1Set;
import stdlib.security.asn1.DEREncodable;
import stdlib.security.asn1.DERObject;
import stdlib.security.asn1.DERObjectIdentifier;
import stdlib.security.asn1.DERSequence;
import stdlib.security.asn1.DERTaggedObject;

public class SafeBag
    implements DEREncodable
{
    DERObjectIdentifier         bagId;
    DERObject                   bagValue;
    ASN1Set                     bagAttributes;

    public SafeBag(
        DERObjectIdentifier     oid,
        DERObject               obj)
    {
        this.bagId = oid;
        this.bagValue = obj;
        this.bagAttributes = null;
    }

    public SafeBag(
        DERObjectIdentifier     oid,
        DERObject               obj,
        ASN1Set                 bagAttributes)
    {
        this.bagId = oid;
        this.bagValue = obj;
        this.bagAttributes = bagAttributes;
    }

	public SafeBag(
		ASN1Sequence	seq)
	{
		this.bagId = (DERObjectIdentifier)seq.getObjectAt(0);
		this.bagValue = ((DERTaggedObject)seq.getObjectAt(1)).getObject();
        if (seq.size() == 3)
        {
            this.bagAttributes = (ASN1Set)seq.getObjectAt(2);
        }
	}

	public DERObjectIdentifier getBagId()
	{
		return bagId;
	}

    public DERObject getBagValue()
    {
		return bagValue;
    }

    public ASN1Set getBagAttributes()
    {
		return bagAttributes;
    }

    public DERObject getDERObject()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(bagId);
        v.add(new DERTaggedObject(0, bagValue));

        if (bagAttributes != null)
        {
            v.add(bagAttributes);
        }

        return new DERSequence(v);
    }
}
