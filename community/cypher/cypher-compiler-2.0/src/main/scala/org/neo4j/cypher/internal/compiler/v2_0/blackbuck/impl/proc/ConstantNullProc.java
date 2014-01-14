package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;

public class ConstantNullProc<P extends Pos<P>> extends DefaultProc<P>
{
    private final SlotWriter<P> dst;

    public ConstantNullProc( SlotWriter<P> dst )
    {
        super( "ConstantNull", dst );
        this.dst = dst;
    }

    @Override
    public void run( P pos )
    {
        dst.setNull( pos );
    }
}
