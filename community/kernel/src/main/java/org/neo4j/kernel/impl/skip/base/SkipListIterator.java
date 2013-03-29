package org.neo4j.kernel.impl.skip.base;

import java.util.NoSuchElementException;

import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.Function;
import org.neo4j.helpers.Function2;
import org.neo4j.helpers.Predicate;
import org.neo4j.kernel.impl.skip.SkipListCabinet;

/**
 * Iterates starting from a given skip list record and continues as long as the given predicate evaluates to true
 *
 * The predicate is expected to guard against nil
 */
public class SkipListIterator<R, K, V, I> implements ResourceIterator<I>
{
    private final SkipListCabinet<R, K, V> cabinet;
    private final Predicate<R> pred;
    private final Function2<SkipListCabinet<R, K, V>, R, I> resultFun;

    private R next;
    private boolean hasNext;

    public static <R, K, V> Function2<SkipListCabinet<R, K, V>, R, R> returnRecords() {
        return new Function2<SkipListCabinet<R, K, V>, R, R>() {
            @Override
            public R apply( SkipListCabinet<R, K, V> cabinet, R entry )
            {
                return entry;
            }
        };
    }

    public static <R, K, V> Function2<SkipListCabinet<R, K, V>, R, V> returnValues() {
        return new Function2<SkipListCabinet<R, K, V>, R, V>() {
            @Override
            public V apply( SkipListCabinet<R, K, V> cabinet, R entry )
            {
                return cabinet.getRecordValue( entry );
            }
        };
    }

    public SkipListIterator( SkipListCabinet<R, K, V> cabinet, R next,
                             Predicate<R> pred,
                             Function2<SkipListCabinet<R, K, V>, R, I> resultFun
    )
    {
        this.cabinet   = cabinet;
        this.pred      = pred;
        this.resultFun = resultFun;

        this.next    = next;
        computeIfHasNext();
    }

    @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    @Override
    public I next()
    {
        if (! hasNext )
            throw new NoSuchElementException(  );

        I result = resultFun.apply( cabinet, next );
        next     = cabinet.getNext( next, 0 );
        computeIfHasNext();
        return result;
    }

    private void computeIfHasNext()
    {
        this.hasNext = pred.accept( next );
        if ( ! hasNext )
        {
            cabinet.release();
        }
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
