package org.neo4j.cypher.internal.compiler.v2_1.runtime;

public final class DirectRegisters implements Registers
{
    public static final RegisterFactory FACTORY = new Factory();

    private static final ValueRegister[] NO_VALUES = new ValueRegister[0];
    private static final EntityRegister[] NO_ENTITIES = new EntityRegister[0];

    private final RegisterSignature signature;

    private final ValueRegister[] valueRegisters;
    private final EntityRegister[] entityRegisters;

    public DirectRegisters( RegisterSignature signature )
    {
        this.signature = signature;
        int numValues = signature.valueRegisters();
        if ( numValues > 0 )
        {
            this.valueRegisters = new ValueRegister[numValues];
            for ( int i = 0; i < numValues; i++ )
            {
                this.valueRegisters[i] = new ValueRegister();
            }
        }
        else
        {
            this.valueRegisters = NO_VALUES;
        }

        int numEntities = signature.entityRegisters();
        if ( numEntities > 0 )
        {
            this.entityRegisters = new EntityRegister[numEntities];
            for ( int i = 0; i < numEntities; i++ )
            {
                this.entityRegisters[i] = new EntityRegister();
            }
        }
        else
        {
            this.entityRegisters = NO_ENTITIES;
        }
    }

    public DirectRegisters( ValueRegister[] valueRegisters, EntityRegister[] entityRegisters )
    {
        this.signature =
            RegisterSignature.empty()
                .withValueRegisters( valueRegisters.length )
                .withEntityRegisters( entityRegisters.length );

        this.valueRegisters = valueRegisters;
        this.entityRegisters = entityRegisters;
    }

    @Override
    public RegisterFactory factory()
    {
        return FACTORY;
    }

    @Override
    public RegisterSignature signature()
    {
        return signature;
    }

    @Override
    public ValueRegister valueRegister( int idx )
    {
        return valueRegisters[idx];
    }

    @Override
    public EntityRegister entityRegister( int idx )
    {
        return entityRegisters[idx];
    }

    private static final class Factory implements RegisterFactory
    {
        @Override
        public Registers createRegisters( RegisterSignature signature )
        {
            return new DirectRegisters( signature );
        }
    }
}
