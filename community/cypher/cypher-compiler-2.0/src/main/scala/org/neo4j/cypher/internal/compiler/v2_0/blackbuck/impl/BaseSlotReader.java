package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.SlotReader;

abstract class BaseSlotReader implements SlotReader {
    @Override
    public boolean isNull( int row )
    {
        throw unsupportedOperationException();
    }

    @Override
    public Object value( int row )
    {
        throw unsupportedOperationException();
    }

    protected UnsupportedOperationException unsupportedOperationException()
    {
        return new UnsupportedOperationException( "This slot does not support this operation." );
    }

    protected void assertNotNull( int row )
    {
        if ( isNull( row ) )
        {
            throw new IllegalStateException("Cannot retrieve value from null slot");
        }
    }

}
