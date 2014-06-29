package org.neo4j.approx.primitive.base;

import org.neo4j.approx.primitive.ApproxDistinctCounter;
import org.neo4j.collection.primitive.PrimitiveIntVisitor;
import org.neo4j.collection.primitive.PrimitiveLongVisitor;

public abstract class AbstractHashBasedApproxDistinctCounter implements ApproxDistinctCounter
{
    @Override
    public final PrimitiveIntVisitor addIntVisitor()
    {
        return new PrimitiveIntVisitor()
        {
            @Override
            public void visited( int value )
            {
                addHash( value );
            }
        };
    }

    @Override
    public final PrimitiveLongVisitor addLongVisitor()
    {
        return new PrimitiveLongVisitor()
        {
            @Override
            public void visited( long value )
            {
                addHash( (int)(value ^ (value >>> 32)) );
            }
        };
    }

    @Override
    public final void addAll( Iterable<?> iterable )
    {
        for (Object item : iterable)
        {
            add(item);
        }
    }

    @Override
    public final void add( Object obj )
    {
        addHash( obj.hashCode() );
    }

    protected abstract void addHash(int value);
}
