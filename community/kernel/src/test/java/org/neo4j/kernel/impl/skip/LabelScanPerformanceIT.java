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

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.nioneo.store.LabelScanStore;
import org.neo4j.kernel.impl.skip.base.SkipListIterator;
import org.neo4j.kernel.impl.skip.store.SkipListStoreCabinet;
import org.neo4j.kernel.impl.skip.store.SkipListStoreRecord;
import org.neo4j.kernel.impl.transaction.XaDataSourceManager;
import org.neo4j.test.BatchTransaction;

import static org.neo4j.kernel.impl.skip.store.SkipListStore.newDefaultLevelGenerator;
import static org.neo4j.test.BatchTransaction.beginBatchTx;
import static org.neo4j.test.PerformanceMeasurement.measure;
import static org.neo4j.test.TargetDirectory.forTest;

public class LabelScanPerformanceIT
{
    private static final int NUM_NODES = 5000000; // 10M
    
    @Test
    public void measureWhereAllNodesHaveTheLabel() throws Exception
    {
        measure( new Runnable()
        {
            @Override
            public void run()
            {
                SkipListStoreCabinet<Long, Long> cabinet =
                        labelScanStore.openCabinet( newDefaultLevelGenerator( random ) );
                try
                {
                    ResourceIterator<Long> iterator = skipListAccessor.findAll( cabinet,
                            SkipListIterator.<SkipListStoreRecord<Long, Long>, Long, Long> returnValues(), labelId );
                    for ( int i = 0; iterator.hasNext(); i++ )
                    {
                        db.getNodeById( iterator.next() );
                        if ( i % 100000 == 0 )
                            System.out.println( i );
                    }
                }
                finally
                {
                    cabinet.close();
                }
            }
        } );
    }
    
    private GraphDatabaseAPI db;
    private LabelScanStore labelScanStore;
    private final long labelId = 1;
    private SkipListAccessor<SkipListStoreRecord<Long, Long>, Long, Long> skipListAccessor;
    private final Random random = new Random( 10 );

    @Before
    public void before() throws Exception
    {
        db = (GraphDatabaseAPI) new GraphDatabaseFactory().newEmbeddedDatabase(
                forTest( getClass() ).graphDbDir( /*delete = */ false ).getAbsolutePath() );
        labelScanStore = db.getDependencyResolver().resolveDependency( XaDataSourceManager.class )
                .getNeoStoreDataSource().getNeoStore().getLabelScanStore();
        skipListAccessor = new SkipListAccessor<SkipListStoreRecord<Long, Long>, Long, Long>( labelScanStore );
        
        try
        {
            db.getNodeById( 1000 );
        }
        catch ( NotFoundException e )
        {
            createTheDataSet();
        }
    }

    private void createTheDataSet()
    {
        System.out.println( "Creating data set" );
        BatchTransaction tx = beginBatchTx( db ).batchSize( 50000 ).printProgress( true );
        LevelGenerator levelGenerator = newDefaultLevelGenerator();
        SkipListStoreCabinet<Long, Long> cabinet = labelScanStore.openCabinet( levelGenerator );
        try
        {
            for ( int i = 0; i < NUM_NODES; i++ )
            {
                Node node = db.createNode();
                if ( cabinet == null )
                {
                    cabinet = labelScanStore.openCabinet( levelGenerator );
                }
                skipListAccessor.insertIfMissing( cabinet, labelId, node.getId() );
                if ( tx.increment() )
                {
                    cabinet.close();
                    cabinet = null;
                }
            }
        }
        finally
        {
            tx.finish();
            if ( cabinet != null )
            {
                cabinet.close();
            }
        }
        System.out.println( "Data set created" );
    }

    @After
    public void after() throws Exception
    {
        db.shutdown();
    }
}
