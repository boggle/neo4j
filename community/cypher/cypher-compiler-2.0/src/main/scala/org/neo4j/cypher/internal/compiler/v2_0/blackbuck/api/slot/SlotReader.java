package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;

public interface SlotReader<P extends Pos<P>> extends TypedSlot<P>
{
    boolean isNull( P pos );
    Object value( P pos );
    int intValue( P pos );

//    long longValue(int row);
//    double floatValue(int row);
//    String stringValue(int row);
//    boolean booleanValue(int row);
}
