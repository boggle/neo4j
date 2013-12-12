package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.RowPool;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.Slot;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.SlotWriter;

abstract class BaseSlot extends BaseSlotReader implements Slot
{
    @Override
    public void setNull(int row)
    {
        throw unsupportedOperationException();
    }

    @Override
    public void setValue(int row, Object value)
    {
        throw unsupportedOperationException();
    }
}
