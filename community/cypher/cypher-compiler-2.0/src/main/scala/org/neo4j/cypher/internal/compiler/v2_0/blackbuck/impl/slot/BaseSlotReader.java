package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;

abstract class BaseSlotReader<C extends Cursor<C>> implements SlotReader<C>
{
    @Override
    public boolean isNull( C cursor )
    {
        throw unsupportedOperationException();
    }

    @Override
    public Object value( C cursor )
    {
        throw unsupportedOperationException();
    }

    @Override
    public int intValue( C cursor )
    {
        throw unsupportedOperationException();
    }

    protected UnsupportedOperationException unsupportedOperationException()
    {
        return new UnsupportedOperationException( "This slot does not support this operation." );
    }

    protected void assertNotNull( C cursor )
    {
        if ( isNull( cursor ) )
        {
            throw new IllegalStateException("Cannot retrieve value from null slot");
        }
    }

}
