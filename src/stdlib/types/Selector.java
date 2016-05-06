package stdlib.types;

public interface Selector
    extends Cloneable
{
    boolean match(Object obj);

    Object clone();
}
