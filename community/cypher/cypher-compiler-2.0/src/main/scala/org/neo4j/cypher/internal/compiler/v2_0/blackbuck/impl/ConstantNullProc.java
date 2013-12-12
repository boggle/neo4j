package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.SlotWriter;

public class ConstantNullProc extends DefaultProc {

    private final SlotWriter dst;

    public ConstantNullProc( SlotWriter dst ) {
        super( "ConstantNull", dst );
        this.dst = dst;
    }

    @Override
    public void run( int row ) {
        dst.setNull( row );
    }
}
