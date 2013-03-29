package org.neo4j.kernel.impl.skip;

import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;

public abstract class SkipListCabinetBaseAdapter<R, K, V> extends SkipListCabinetBase<R, K, V>
{

    SkipListCabinetBaseAdapter( int maxHeight )
    {
        super( maxHeight );
    }

    @Override
    public R nil()
    {
        return null;
    }

    @Override
    public R getHead()
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean isNil( R record )
    {
        return null == record;
    }

    @Override
    public boolean isHead( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public int newRandomLevel()
    {
        throw unsupportedOperation();
    }

    @Override
    public R createRecord( int height, K key, V value )
    {
        throw unsupportedOperation();
    }

    @Override
    public void removeRecord( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public boolean areSameRecord( R first, R second )
    {
        return first == second;
    }

    @Override
    public K getRecordKey( R record )
    {
        throw unsupportedOperation();
    }

    private UnsupportedOperationException unsupportedOperation()
    {
        return new UnsupportedOperationException(  );
    }

    @Override
    public V getRecordValue( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public V setRecordValue( R record, V newValue )
    {
        throw unsupportedOperation();
    }

    @Override
    public int getHeight( R record )
    {
        throw unsupportedOperation();
    }

    @Override
    public R getNext( R record, int i )
    {
        return nil();
    }

    @Override
    public void setNext( R record, int i, R newNext )
    {
    }

    @Override
    protected void onClose()
    {
    }

    @Override
    public void delete()
    {
    }
}
