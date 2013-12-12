package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public interface SlotWriter extends TypedSlot {
    void setNull( int row );
    void setValue( int row, Object value );

//    void setIntValue(int row, int value);
//    void setLongValue(int row, long value);
//    void setFloatValue(int row, float value);
//    void setStringValue(int row, String value);
//    void setBooleanValue(int row, boolean value);
}
