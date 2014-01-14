package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;

abstract class BaseSlotReader<P extends Pos<P>> implements SlotReader<P>
{
    @Override
    public boolean isNull( P pos )
    {
        throw unsupportedOperationException();
    }

    @Override
    public Object value( P pos )
    {
        throw unsupportedOperationException();
    }

    @Override
    public int intValue( P pos )
    {
        throw unsupportedOperationException();
    }

    protected UnsupportedOperationException unsupportedOperationException()
    {
        return new UnsupportedOperationException( "This slot does not support this operation." );
    }

    protected void assertNotNull( P pos )
    {
        if ( isNull( pos ) )
        {
            throw new IllegalStateException("Cannot retrieve value from null slot");
        }
    }

}
