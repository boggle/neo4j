package org.neo4j.cypher.internal.compiler.v2_1.runtime;

import org.neo4j.kernel.api.StatementConstants;

public final class EntityRegister implements Register
{
    private long entity;

    public EntityRegister()
    {
        this( StatementConstants.NO_SUCH_NODE );
    }

    public EntityRegister( long entity )
    {
        this.entity = entity;
    }

    public long getEntity()
    {
        return entity;
    }

    public void setEntity( long entity )
    {
        this.entity = entity;
    }

    @Override
    public Object getValue()
    {
        return (Long) getEntity();
    }

    @Override
    public void setValue( Object value )
    {
        setEntity( (long) value );
    }
}
