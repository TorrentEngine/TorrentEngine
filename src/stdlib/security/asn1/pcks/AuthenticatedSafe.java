package stdlib.security.asn1.pcks;

import stdlib.security.asn1.ASN1EncodableVector;
import stdlib.security.asn1.ASN1Sequence;
import stdlib.security.asn1.BERSequence;
import stdlib.security.asn1.DEREncodable;
import stdlib.security.asn1.DERObject;

public class AuthenticatedSafe
    implements DEREncodable
{
    ContentInfo[]    info;

    public AuthenticatedSafe(
        ASN1Sequence  seq)
    {
        info = new ContentInfo[seq.size()];

        for (int i = 0; i != info.length; i++)
        {
            info[i] = ContentInfo.getInstance(seq.getObjectAt(i));
        }
    }

    public AuthenticatedSafe(
        ContentInfo[]       info)
    {
        this.info = info;
    }

    public ContentInfo[] getContentInfo()
    {
        return info;
    }

    public DERObject getDERObject()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        for (int i = 0; i != info.length; i++)
        {
            v.add(info[i]);
        }

        return new BERSequence(v);
    }
}
