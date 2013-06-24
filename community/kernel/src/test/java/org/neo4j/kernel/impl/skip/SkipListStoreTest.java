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
package org.neo4j.kernel.impl.skip;

import static org.neo4j.helpers.collection.MapUtil.stringMap;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.neo4j.kernel.DefaultIdGeneratorFactory;
import org.neo4j.kernel.DefaultTxHook;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.DefaultWindowPoolFactory;
import org.neo4j.kernel.impl.nioneo.store.RecordFieldSerializer;
import org.neo4j.kernel.impl.nioneo.store.StoreFactory;
import org.neo4j.kernel.impl.skip.store.SkipListStore;
import org.neo4j.kernel.impl.skip.store.SkipListStoreRecord;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.test.impl.EphemeralFileSystemAbstraction;

public class SkipListStoreTest extends GenericSkipListAccessorTest<SkipListStoreRecord<Long, String>>
{
    private Config config;
    private EphemeralFileSystemAbstraction fileSystemAbstraction;
    private SkipListStore<Long, String> cabinetProvider;
    private File skipListStoreFile = new File( "skip-list-store.skip.db" );

    @Override
    protected SkipListAccessor<SkipListStoreRecord<Long, String>, Long, String> createAccessor( )
    {
        DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
        DefaultWindowPoolFactory windowPoolFactory = new DefaultWindowPoolFactory();
        StringLogger stringLogger = StringLogger.SYSTEM;
        StoreFactory factory = new StoreFactory( config, idGeneratorFactory, windowPoolFactory,
                                                 fileSystemAbstraction, stringLogger, new DefaultTxHook() );

        IdType idType = IdType.NODE_LABELS;
        factory.createEmptyDynamicStore(
                skipListStoreFile,
                SkipListStore.BLOCK_SIZE,
                SkipListStore.VERSION,
                idType );

        cabinetProvider  = new SkipListStore<Long, String>(
                skipListStoreFile,
                config,
                idType,
                idGeneratorFactory,
                windowPoolFactory,
                fileSystemAbstraction,
                stringLogger,
                RecordFieldSerializer.LONG,
                RecordFieldSerializer.STRING );

        return new SkipListAccessor<SkipListStoreRecord<Long, String>, Long, String>( cabinetProvider );
    }

    @Override
    protected long getStorageUsed()
    {
        return fileSystemAbstraction.getFileSize( skipListStoreFile );
    }

    @Before
    public void before() throws Exception
    {
        config = new Config( stringMap() );
        fileSystemAbstraction = new EphemeralFileSystemAbstraction();
        super.before();
    }

    @After
    public void after() throws Exception
    {
        super.after();
        cabinetProvider.close();
        fileSystemAbstraction.shutdown();
    }
}
