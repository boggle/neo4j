package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot;

public enum SlotType
{
    INT {
        @Override
        public SlotType join(SlotType other) {
            switch (other) {
                case INT: return INT;
                default: return ANY;
            }
        }
    },
//    LONG,
//    DOUBLE,
//    STRING,
//    BOOL,
    ANY {
        @Override
        public SlotType join( SlotType other ) {
            return ANY;
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
