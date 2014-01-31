package org.neo4j.cypher.internal.compiler.v2_1.runtime;

public final class DirectRegisters implements Registers
{
    public static final RegisterFactory FACTORY = new Factory();

    public static final ValueRegister[] NO_VALUES = new ValueRegister[0];
    public static final EntityRegister[] NO_ENTITIES = new EntityRegister[0];

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

    public DirectRegisters( RegisterSignature signature,
                            ValueRegister[] valueRegisters,
                            EntityRegister[] entityRegisters )
    {
        this.signature = signature;
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

    public void setEntitiesFrom( DirectRegisters source, int[] indices )
    {
        for ( int i = 0; i < indices.length; i++ )
        {
            int idx = indices[i];
            entityRegisters[idx].setEntity( source.entityRegisters[idx].getEntity() );
        }
    }

    public long[] copyEntities( int[] indices )
    {
        long[] result = new long[indices.length];
        for ( int i = 0; i < indices.length; i++ )
        {
            result[i] = this.entityRegisters[indices[i]].getEntity();
        }
        return result;
    }


    public DirectRegisters copy()
    {
        ValueRegister[] values;
        if ( valueRegisters.length > 0 )
        {
            values = new ValueRegister[valueRegisters.length];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = new ValueRegister( valueRegisters[i].getValue() );
            }
        }
        else
        {
            values = NO_VALUES;
        }

        EntityRegister[] entities;
        if ( entityRegisters.length > 0 )
        {
            entities = new EntityRegister[entityRegisters.length];
            for (int i = 0; i < entities.length; i++)
            {
                entities[i] = new EntityRegister( entityRegisters[i].getEntity() );
            }
        }
        else
        {
            entities = NO_ENTITIES;
        }

        return new DirectRegisters( values, entities );
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
