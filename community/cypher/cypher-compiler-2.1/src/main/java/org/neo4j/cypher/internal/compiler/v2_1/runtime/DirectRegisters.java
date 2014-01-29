package org.neo4j.cypher.internal.compiler.v2_1.runtime;

public class DirectRegisters implements Registers
{
    public static final RegisterFactory FACTORY = new Factory();

    private final RegisterSignature signature;

    private final ValueRegister[] valueRegisters;
    private final EntityRegister[] entityRegisters;

    public DirectRegisters( RegisterSignature signature )
    {
        this.signature = signature;
        this.valueRegisters = new ValueRegister[signature.valueRegisters()];
        for ( int i = 0; i < this.valueRegisters.length; i++ )
        {
            this.valueRegisters[i] = new ValueRegister();
        }

        this.entityRegisters = new EntityRegister[signature.entityRegisters()];
        for ( int i = 0; i < this.entityRegisters.length; i++ )
        {
            this.entityRegisters[i] = new EntityRegister();
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

    public DirectRegisters( DirectRegisters original )
    {
        this.signature = original.signature;
        this.valueRegisters = new ValueRegister[signature.valueRegisters()];
        for ( int i = 0; i < this.valueRegisters.length; i++ )
        {
            this.valueRegisters[i] = original.valueRegisters[i].copy();
        }

        this.entityRegisters = new EntityRegister[signature.entityRegisters()];
        for ( int i = 0; i < this.entityRegisters.length; i++ )
        {
            this.entityRegisters[i] = original.entityRegisters[i].copy();
        }
    }

    @Override
    public void updateFrom( Registers registers )
    {
        RegisterSignature fromSignature = registers.signature();

        int fromValues = fromSignature.valueRegisters();
        for ( int i = 0; i < fromValues; i++ )
        {
            valueRegisters[i].updateFrom( registers.valueRegister( i ) );
        }

        int fromEntities = fromSignature.entityRegisters();
        for ( int i = 0; i < fromEntities; i++ )
        {
            entityRegisters[i].updateFrom( registers.entityRegister( i ) );
        }
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

    @Override
    public Registers copy()
    {
        return new DirectRegisters( this );
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
