package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Slot;

abstract class BaseSlot<P extends Pos<P>> extends BaseSlotReader<P> implements Slot<P>
{
    @Override
    public void setNull( P pos)
    {
        throw unsupportedOperationException();
    }

    @Override
    public void setValue( P pos, Object value)
    {
        throw unsupportedOperationException();
    }

    @Override
    public void setIntValue( P pos, int value ) {
        throw unsupportedOperationException();
    }
}
