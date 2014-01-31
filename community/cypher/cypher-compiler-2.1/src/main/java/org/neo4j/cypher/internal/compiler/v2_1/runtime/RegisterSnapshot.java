package org.neo4j.cypher.internal.compiler.v2_1.runtime;

import java.lang.reflect.Field;
import java.util.Arrays;

import sun.misc.Unsafe;

public final class RegisterSnapshot
{
    public static final Object[] NO_VALUES = new Object[0];
    public static final long[] NO_ENTITIES = new long[0];

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

    public RegisterSnapshot( Object[] newValues, long[] newEntities )
    {
        values = newValues;
        entities = newEntities;
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

    public long getEntityValue( int idx )
    {
        return entities[idx];
    }

    public void setEntityValue( int idx, long value )
    {
        entities[idx] = value;
    }

    public long getUnsafeEntityValue( int idx )
    {
        return unsafe.getLong( entities, base + idx * scale );
    }

    public void setUnsafeEntityValue( int idx, long value )
    {
        unsafe.putLong( entities, base + idx * scale, value );
    }

    public RegisterSnapshot copy()
    {
        Object[] newValues;
        if ( values.length > 0 )
        {
            newValues = Arrays.copyOf(values, values.length);
        }
        else
        {
            newValues = NO_VALUES;
        }

        long[] newEntities;
        if ( entities.length > 0 )
        {
            newEntities = Arrays.copyOf(entities, entities.length);
        }
        else
        {
            newEntities = NO_ENTITIES;
        }

        return new RegisterSnapshot( newValues, newEntities );
    }

    public RegisterSnapshot copy2()
    {
        Object[] newValues;
        if ( values.length > 0 )
        {
            newValues = new Object[values.length];
            for ( int i = 0; i < values.length; i++)
            {
                newValues[i] = values[i];
            }
        }
        else
        {
            newValues = NO_VALUES;
        }

        long[] newEntities;
        if ( entities.length > 0 )
        {
            newEntities = new long[entities.length];
            for ( int i = 0; i < entities.length; i++ )
            {
                newEntities[i] = entities[i];
            }
        }
        else
        {
            newEntities = NO_ENTITIES;
        }

        return new RegisterSnapshot( newValues, newEntities );
    }

    private static Unsafe unsafe;
    private static int base;
    private static int scale;


    static {
        Field f = null;
        try
        {
            f = Unsafe.class.getDeclaredField( "theUnsafe" );
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            base =  unsafe.arrayBaseOffset( long[].class );
            scale = unsafe.arrayIndexScale( long[].class );

        }
        catch ( NoSuchFieldException | IllegalAccessException e )
        {
            System.exit(1);
        }
    }
}
