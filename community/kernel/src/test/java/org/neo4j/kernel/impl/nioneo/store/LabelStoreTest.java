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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.neo4j.helpers.collection.IteratorUtil.asCollection;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;
import static org.neo4j.helpers.collection.MapUtil.stringMap;
import static org.neo4j.kernel.impl.util.StringLogger.SYSTEM;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.kernel.DefaultIdGeneratorFactory;
import org.neo4j.kernel.DefaultTxHook;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.test.impl.EphemeralFileSystemAbstraction;

@SuppressWarnings("UnusedDeclaration")
public class LabelStoreTest
{
//    private Config config;
//    private LabelStore store;
//    private EphemeralFileSystemAbstraction fileSystemAbstraction;
//    private StoreFactory storeFactory;
//
//    @Test
//    public void testAutoCreateHead() {
//        assertEquals( 0, store.getMaxLevelIndex() );
//    }
//
//    @Test @Ignore("too slow to run by default, needed for testing and parameter tuning of skip list store")
//    public void testLevelGeneration() {
//        // GIVEN
//        long[] counts = new long[SkipListIndexStore.H_MAX];
//        int i = 0;
//        long rounds = 0;
//        long sum = 0;
//        RandomLevelGenerator heightGenerator = store.getHeightGenerator();
//
//        // WHEN
//        while (i < SkipListIndexStore.H_MAX) {
//            int j = heightGenerator.getRandomHeight();
//            if (counts[j-1] == 0) {
//               System.out.println( j );
//               i++;
//            }
//            counts[j-1]++;
//            rounds++;
//            sum += j;
//        }
//
//        // THEN
//        assertTrue( sum >= counts.length );
//
//        // TELL (1)
//        System.out.println( Arrays.toString( counts ) );
//        System.out.println( "Inserts until all heights reached: " + rounds);
//        System.out.println( "Average height: " + ((double)sum)/((double)rounds) );
//
//        // TELL (2)
//        long goal = (long) (0.85 * rounds);
//        for(int k = 0; k < counts.length; k++) {
//            if (goal > counts[k])
//                goal -= counts[k];
//            else
//            {
//                System.out.println( "85 % have no more links than: " + (k + 1) );
//                break;
//            }
//        }
//    }
//
//    @Test
//    public void testSimpleInsert() {
//        store.insert( 1L, 2L );
//        store.insert( 1L, 3L );
//        store.insert( 2L, 4L );
//        store.insert( 2L, 5L );
//        store.insert( 8L, 10L );
//    }
//
//
//    @Test
//    public void testSimpleLookup() {
//        // GIVEN
//        testSimpleInsert();
//
//        // THEN
//        assertEquals( 2L, (long) store.getFirst( 1L ) );
//        assertTrue( store.contains( 1L, 3L ) );
//        assertTrue( store.contains( 2L, 4L ) );
//        assertTrue( store.contains( 2L, 5L ) );
//        assertEquals( 10L, (long) store.getFirst( 8L ) );
//        assertEquals( asCollection( asIterable( 2L, 3L ) ), asCollection( asIterable( store.getAll( 1L ) ) ) );
//        assertEquals( asCollection( asIterable( 4L, 5L ) ), asCollection( asIterable( store.getAll( 2L ) ) ) );
//    }
//
//    @Test
//    public void testSimpleDelete() {
//        // GIVEN
//        testSimpleInsert();
//
//        // WHEN
//        store.delete( 1L, 3L );
//        store.delete( 2L, 10L );
//        assertEquals( asCollection( asIterable( 2L ) ), asCollection( asIterable( store.getAll( 1L ) ) ) );
//        assertEquals( asCollection( asIterable( 4L, 5L ) ), asCollection( asIterable( store.getAll( 2L ) ) ) );
//    }
//
//    @Before
//    public void before() throws Exception
//    {
//        config = new Config( stringMap() );
//        fileSystemAbstraction = new EphemeralFileSystemAbstraction();
//        DefaultIdGeneratorFactory idGeneratorFactory = new DefaultIdGeneratorFactory();
//        DefaultWindowPoolFactory windowPoolFactory = new DefaultWindowPoolFactory();
//        storeFactory = new StoreFactory( config, idGeneratorFactory, windowPoolFactory, fileSystemAbstraction, SYSTEM,
//                new DefaultTxHook() );
//        File file = new File( "label-store" );
//        storeFactory.createLabelStore( file );
//        store = storeFactory.newLabelStore( file );
//    }
//
//    @After
//    public void after() throws Exception
//    {
//        store.close();
//        fileSystemAbstraction.shutdown();
//    }
}
