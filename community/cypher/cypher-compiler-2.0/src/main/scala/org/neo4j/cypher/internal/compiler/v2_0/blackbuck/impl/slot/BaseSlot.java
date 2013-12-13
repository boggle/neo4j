package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Slot;

abstract class BaseSlot<C extends Cursor<C>> extends BaseSlotReader<C> implements Slot<C>
{
    @Override
    public void setNull( C cursor)
    {
        throw unsupportedOperationException();
    }

    @Override
    public void setValue( C cursor, Object value)
    {
        throw unsupportedOperationException();
    }

    @Override
    public void setIntValue( C cursor, int value ) {
        throw unsupportedOperationException();
    }
}
