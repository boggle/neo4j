package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public interface SlotWriter<C extends Cursor<C>> extends TypedSlot<C>
{
    void setNull( C cursor );
    void setValue( C cursor, Object value );
    void setIntValue( C cursor, int value);

//    void setLongValue(int row, long value);
//    void setFloatValue(int row, float value);
//    void setStringValue(int row, String value);
//    void setBooleanValue(int row, boolean value);
}
