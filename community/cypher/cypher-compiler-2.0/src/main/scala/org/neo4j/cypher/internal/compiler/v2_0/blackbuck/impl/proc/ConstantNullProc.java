package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;

public class ConstantNullProc<C extends Cursor<C>> extends DefaultProc<C>
{
    private final SlotWriter<C> dst;

    public ConstantNullProc( SlotWriter<C> dst )
    {
        super( "ConstantNull", dst );
        this.dst = dst;
    }

    @Override
    public void run( C cursor )
    {
        dst.setNull( cursor );
    }
}
