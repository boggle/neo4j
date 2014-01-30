package org.neo4j.cypher.internal.compiler.v2_1.runtime;

public final class ValueRegister implements Register
{
    private Object value;

    public ValueRegister()
    {
        this( null );
    }

    public ValueRegister( Object value )
    {
        this.value = value;
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public void setValue( Object value )
    {
        this.value = value;
    }

    @Override
    public boolean isNull()
    {
        return null == value;
    }

    @Override
    public void setNull()
    {
        value = null;
    }
}
