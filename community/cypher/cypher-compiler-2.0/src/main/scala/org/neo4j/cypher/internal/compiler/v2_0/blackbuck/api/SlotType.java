package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api;

public enum SlotType {
//    INT,
//    LONG,
//    DOUBLE,
//    STRING,
//    BOOL,
    ANY {
        @Override
        public SlotType join( SlotType other ) {
            return null;
        }
    };

    public boolean isNumber()
    {
        return false;
    }

    public static SlotType of( Object value )
    {
        return ANY;
    }

    public abstract SlotType join( SlotType other );
}
