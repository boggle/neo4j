package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.rows.Cursor;

public interface SlotWriter<P extends Pos<P>> extends TypedSlot<P>
{
    void setNull( P pos );
    void setValue( P pos, Object value );
    void setIntValue( P pos, int value );

//    void setLongValue(int row, long value);
//    void setFloatValue(int row, float value);
//    void setStringValue(int row, String value);
//    void setBooleanValue(int row, boolean value);
}
