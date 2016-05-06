package stdlib.security.main;

import java.util.Enumeration;

import stdlib.security.asn1.DEREncodable;
import stdlib.security.asn1.DERObjectIdentifier;

/**
 * allow us to set attributes on objects that can go into a PKCS12 store.
 */
public interface PKCS12BagAttributeCarrier
{
    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute);

    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid);

    public Enumeration getBagAttributeKeys();
}
