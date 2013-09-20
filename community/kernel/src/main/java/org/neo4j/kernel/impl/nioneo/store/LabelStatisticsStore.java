/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.nioneo.store;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.windowpool.WindowPoolFactory;
import org.neo4j.kernel.impl.util.StringLogger;

public class LabelStatisticsStore extends AbstractRecordStore<LabelStatisticsRecord>
{
    public static final String TYPE_DESCRIPTOR = "LabelStatisticsStore";
    public static final int RECORD_SIZE = 8;

    public LabelStatisticsStore( File fileName, Config conf,
                                 WindowPoolFactory windowPoolFactory,
                                 IdType idType,
                                 IdGeneratorFactory idGeneratorFactory,
                                 FileSystemAbstraction fileSystemAbstraction,
                                 StringLogger stringLogger )
    {
        super( fileName, conf, idType, idGeneratorFactory, windowPoolFactory, fileSystemAbstraction, stringLogger );
    }

    @Override
    public LabelStatisticsRecord getRecord( long id )
    {
        PersistenceWindow window = acquireWindow( id, OperationType.READ );
        try
        {
            return getRecord( id, window );
        }
        finally
        {
            releaseWindow( window );
        }
    }

    @Override
    public void updateRecord( LabelStatisticsRecord record )
    {
        if ( 0 == record.getCount() && record.getId() > getHighId() )
        {
            return;
        }

        setHighId( record.getId() );
        PersistenceWindow window = acquireWindow( record.getId(),OperationType.WRITE );
        try
        {
            updateRecord( record, window );
        }
        finally
        {
            releaseWindow( window );
        }
    }

    private void updateRecord( LabelStatisticsRecord record, PersistenceWindow window )
    {
        long id = record.getId();
        registerIdFromUpdateRecord( id );
        Buffer buffer = window.getOffsettedBuffer( id );
        buffer.putLong( record.getCount() );
    }


    @Override
    public LabelStatisticsRecord forceGetRecord( long id )
    {
        PersistenceWindow window;
        try
        {
            window = acquireWindow( id, OperationType.READ );
        }
        catch ( InvalidRecordException e )
        {
            return new LabelStatisticsRecord( id, 0, false );
        }

        try
        {
            return getRecord( id, window );
        }
        finally
        {
            releaseWindow( window );
        }
    }

    private LabelStatisticsRecord getRecord( long id, PersistenceWindow window )
    {
        Buffer buffer = window.getOffsettedBuffer( id );
        return new LabelStatisticsRecord( id, buffer.getLong() );
    }

    @Override
    public LabelStatisticsRecord forceGetRaw( LabelStatisticsRecord record )
    {
        return record;
    }

    @Override
    public LabelStatisticsRecord forceGetRaw( long id )
    {
        return forceGetRecord( id );
    }

    @Override
    public void forceUpdateRecord( LabelStatisticsRecord record )
    {
    }

    @Override
    public <FAILURE extends Exception> void accept( Processor<FAILURE> processor, LabelStatisticsRecord record )
            throws FAILURE
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordSize()
    {
        return RECORD_SIZE;
    }

    @Override
    public int getRecordHeaderSize()
    {
        return 0;
    }

    @Override
    public List<WindowPoolStats> getAllWindowPoolStats()
    {
        List<WindowPoolStats> list = new ArrayList<>();
        list.add( getWindowPoolStats() );
        return list;
    }

    @Override
    public String getTypeDescriptor()
    {
        return TYPE_DESCRIPTOR;
    }
}
