package org.neo4j.kernel.impl.skip.store;

enum SkipListRecordState
{
    CREATED( false, true ),
    LOADED( false, false ),
    OUTDATED( true, true ),
    UPDATED( false, true ),
    REMOVED( false, true );

    public final boolean isOutdated;
    public final boolean shouldWrite;

    SkipListRecordState( boolean serialize, boolean write )
    {
        this.isOutdated = serialize;
        this.shouldWrite = write;
    }
}
