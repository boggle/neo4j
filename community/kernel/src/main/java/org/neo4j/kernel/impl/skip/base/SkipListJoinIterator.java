package org.neo4j.kernel.impl.skip.base;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.kernel.impl.skip.SkipListCabinet;

public class SkipListJoinIterator<R, K, V> implements ResourceIterator<V>
{
    private final Collection<ResourceIterator<V>> iterators;
    private final SkipListCabinet<R, K, V> cabinet;

    private boolean hasNext = false;
    private V next = null;

    public SkipListJoinIterator( SkipListCabinet<R, K, V> cabinet, Collection<ResourceIterator<V>> iterators )
    {
        this.cabinet = cabinet;
        this.iterators = iterators;
        computeIfHasNext();
    }  @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public V next()
    {
        if ( hasNext )
        {
            V result = next;
            computeIfHasNext();
            return result;
        }
        else
            throw new NoSuchElementException(  );
    }

    private void computeIfHasNext()
    {
        throw new UnsupportedOperationException( );
    }

    public void close()
    {
        if ( hasNext )
        {
            hasNext = false;
            cabinet.release();
        }
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException(  );
    }
}
