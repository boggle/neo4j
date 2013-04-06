package org.neo4j.kernel.impl.skip.store;

import java.util.Collection;

import org.neo4j.kernel.impl.nioneo.store.DynamicRecord;

/**
 * Variant of {@link SkipListStoreRecord} that additionally holds pre-existing {@link DynamicRecord}s for re-use
 *
 * @see SkipListStoreRecord
 */
public class SkipListStoreLoadedRecord<K, V> extends SkipListStoreRecord<K, V>
{
    private final Collection<DynamicRecord> dynRecords;

    public SkipListStoreLoadedRecord( long id, K key, V value, long[] nexts, Collection<DynamicRecord> dynRecords )
    {
        super( id, key, value, nexts, SkipListRecordState.LOADED );
        this.dynRecords = dynRecords;
    }

    public SkipListStoreLoadedRecord( long[] nexts, Collection<DynamicRecord> dynRecords )
    {
        super( nexts, SkipListRecordState.LOADED );
        this.dynRecords = dynRecords;
    }

    @Override
    Collection<DynamicRecord> getDynRecords()
    {
        return dynRecords;
    }
}
