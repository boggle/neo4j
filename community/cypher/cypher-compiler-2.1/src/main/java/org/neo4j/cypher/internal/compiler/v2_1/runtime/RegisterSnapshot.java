package org.neo4j.cypher.internal.compiler.v2_1.runtime;

public final class RegisterSnapshot
{
    private static final Object[] NO_VALUES = new Object[0];
    private static final long[] NO_ENTITIES = new long[0];

    private final Object[] values;
    private final long[] entities;

    public RegisterSnapshot( Registers registers )
    {
        RegisterSignature signature = registers.signature();

        int numValues = signature.valueRegisters();
        if ( numValues > 0 )
        {
            this.values = new Object[numValues];
            for ( int i = 0; i < numValues; i++ )
            {
                this.values[i] = registers.valueRegister( i ).getValue();
            }
        }
        else
        {
            this.values = NO_VALUES;
        }

        int numEntities = signature.entityRegisters();
        if ( numEntities > 0 )
        {
            this.entities = new long[numEntities];
            for ( int i = 0; i < numValues; i++ )
            {
                this.entities[i] = registers.entityRegister( i ).getEntity();
            }
        }
        else
        {
            this.entities = NO_ENTITIES;
        }
    }

    public void restore( Registers registers )
    {
        for ( int i = 0; i < values.length; i++ )
        {
            registers.valueRegister( i ).setValue( values[i] );
        }

        for ( int i = 0; i < entities.length; i++ )
        {
            registers.entityRegister( i ).setEntity( entities[i] );
        }
    }
}
