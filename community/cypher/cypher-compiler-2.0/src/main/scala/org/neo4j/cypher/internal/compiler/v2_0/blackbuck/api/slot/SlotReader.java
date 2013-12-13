package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface SlotReader<C extends Cursor<C>> extends TypedSlot<C>
{
    boolean isNull( C cursor );
    Object value( C cursor );
    int intValue( C cursor );

//    long longValue(int row);
//    double floatValue(int row);
//    String stringValue(int row);
//    boolean booleanValue(int row);
}
