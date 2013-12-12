package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public interface SlotReader extends TypedSlot {
    boolean isNull( int row );
    Object value( int row );

//    int intValue(int row);
//    long longValue(int row);
//    double floatValue(int row);
//    String stringValue(int row);
//    boolean booleanValue(int row);
}
