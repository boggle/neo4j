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
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.nioneo.store.LabelScanStore;
import org.neo4j.kernel.impl.nioneo.store.LabelSkipListAccessor;
import org.neo4j.kernel.impl.nioneo.store.LabelStretch;
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

    int batchSize = 500;
    int batches   = NUM_NODES / batchSize;

    @Test
    public void measureWhereAllNodesHaveTheLabel() throws Exception
    {
        measure( new Runnable()
        {
            @Override
            public void run()
            {
                SkipListStoreCabinet<LabelStretch, long[]> cabinet =
                        labelScanStore.openCabinet( newDefaultLevelGenerator( random ) );
                try
                {
                    SkipListStoreRecord<LabelStretch,long[]> record =
                            skipListAccessor.findFirst( cabinet, new LabelStretch( labelId, 0 ) );

                    int j = 0;
                    while ( ! cabinet.isNil( record ) ) {
                        LabelStretch stretch = cabinet.getRecordKey( record );
                        if ( stretch.labelId() != labelId )
                        {
                            return;
                        }

                        long[] data = cabinet.getRecordValue( record );
                        for ( int l = 0; l < data.length; l++ )
                        {
                            long value = data[l];

                            for (int k = 0; k < 64; ++k)
                            {
                                if ( (value & (1L<<k))  != 0 )
                                {
                                    int nodeId = stretch.nodeId( l * 64 + k);
                                    db.getNodeById( nodeId );
                                    j++;

                                }
                            }
                        }
//                            if ( j % 50000 == 0 )
//                                System.out.println( j );

                        record = cabinet.getNext( record, 0 );
                    }

                    System.out.println( "counted nodes: " + j);
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
    private LabelSkipListAccessor<SkipListStoreRecord<LabelStretch, long[]>> skipListAccessor;
    private final Random random = new Random( 10 );

    @Before
    public void before() throws Exception
    {
        db = (GraphDatabaseAPI) new GraphDatabaseFactory().newEmbeddedDatabase(
                forTest( getClass() ).graphDbDir( /*delete = */ true ).getAbsolutePath() );
        labelScanStore = db.getDependencyResolver().resolveDependency( XaDataSourceManager.class )
                .getNeoStoreDataSource().getNeoStore().getLabelScanStore();
        skipListAccessor = new LabelSkipListAccessor<>( labelScanStore );
        
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
        try
        {
            LevelGenerator levelGenerator = newDefaultLevelGenerator();
            for ( int j = 0; j < batches; j++ )
            {
                SkipListStoreCabinet<LabelStretch, long[]> cabinet = labelScanStore.openCabinet( levelGenerator );
                try
                {
                    for ( int k = 0; k < batchSize; k++ )
                    {
                        int i = j * batchSize + k;
                        Node node = db.createNode();
                        long nodeId = node.getId();

                        int stretchId = (int) (nodeId >> LabelScanStore.STRETCH_SHIFT);
                        LabelStretch stretch = new LabelStretch( labelId, stretchId );
                        int nodeIndex = (int)(nodeId & LabelScanStore.STRETCH_MASK);
                        SkipListStoreRecord<LabelStretch, long[]> record = skipListAccessor.findFirst( cabinet, stretch );
                        if ( cabinet.isNil( record ) )
                        {
                            record = skipListAccessor.insertIfMissing( cabinet, stretch, new long[LabelScanStore.STRETCH_LONGS] );
                        }

                        long[] data = cabinet.getRecordValue( record );
                        int longIndex   = nodeIndex >> 6;
                        int longBit     = nodeIndex & 63;
                        long longPos    = 1L << longBit;
                        long oldValue   = data[longIndex];
                        long newValue   = oldValue | longPos;
                        data[longIndex] = newValue;

                        cabinet.markDirty( record );

                        tx.increment();
                    }
                }
                finally
                {
                    cabinet.close();
                }
            }
        }
        finally
        {
            tx.finish();
        }
        System.out.println( "Data set created" );
    }

    @After
    public void after() throws Exception
    {
        db.shutdown();
    }
}
