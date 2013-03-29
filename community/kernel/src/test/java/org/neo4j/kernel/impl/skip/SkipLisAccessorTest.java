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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.IteratorUtil.asCollection;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;
import static org.neo4j.helpers.collection.IteratorUtil.asIterator;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.impl.skip.base.RandomLevelGenerator;
import org.neo4j.kernel.impl.skip.inmem.InMemSkipListCabinetProvider;
import org.neo4j.kernel.impl.skip.inmem.InMemSkipListRecord;

public class SkipLisAccessorTest
{
    @Test
    public void shouldNotReturnFoundElementWhenEmpty()
    {
        // WHEN
        InMemSkipListRecord<Long, Long> result = accessor.findFirst( cabinet, 1L, 1L );

        // THEN
        assertTrue( cabinet.isNil( result ) );
    }

    @Test
    public void shouldReturnHeadAsPredWhenEmpty()
    {
        // WHEN
        InMemSkipListRecord<Long, Long> result = accessor.findPred( cabinet, 1L, 1L, null );

        // THEN
        assertTrue( cabinet.isHead( result ) );
    }

    @Test
    public void shouldInsertElementWhenEmpty()
    {
        // GIVEN
        accessor.insertIfMissing( cabinet, 1L, 1L );

        // WHEN
        InMemSkipListRecord<Long, Long> result = accessor.findFirst( cabinet, 1L, 1L );

        // THEN
        assertRecordEquals( 1L, 1L, result );
    }

    @Test
    public void shouldInsertMultipleElementsWithoutFailures()
    {
        // PASS
        insertSomeRecords();
    }

    private void insertSomeRecords()
    {
        accessor.insertIfMissing( cabinet, 2L, 4L );
        accessor.insertIfMissing( cabinet, 2L, 5L );
        accessor.insertIfMissing( cabinet, 1L, 2L );
        accessor.insertIfMissing( cabinet, 8L, 10L );
        accessor.insertIfMissing( cabinet, 1L, 3L );
        accessor.insertIfMissing( cabinet, 7L, 10L );
    }

    @Test
    public void shouldFindInsertedElements() {
        // WHEN
        insertSomeRecords();

        // THEN
        assertRecordEquals( 1L, 2L, accessor.findFirst( cabinet, 1L ) );
        assertTrue( accessor.contains( cabinet, 1L, 3L ) );
        assertTrue( accessor.contains( cabinet, 2L, 4L ) );
        assertTrue( accessor.contains( cabinet, 2L, 5L ) );
        assertRecordEquals( 8L, 10L, accessor.findFirst( cabinet, 8L ) );

        // WHEN
        Iterator<Long> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );

        // THEN
        assertEquals( asCollection( asIterable( 2L, 3L ) ), asCollection( allOnes ) );

        // WHEN
        Iterator<Long> allTwos = accessor.findAll( cabinet, accessor.returnValues(), 2L );

        // THEN
        assertEquals( asCollection( asIterable( 4L, 5L ) ), asCollection( allTwos ) );
    }

    @Test
    public void shouldDeleteOneExisting()
    {
        insertSomeRecords();

        // WHEN
        accessor.removeFirst( cabinet, 1L, 2L );

        // THEN
        assertFalse( accessor.contains( cabinet, 1L, 2L ) );
    }

    @Test
    public void shouldDeleteTwoExisting()
    {
        // GIVEN
        insertSomeRecords();
        accessor.removeFirst( cabinet, 1L, 3L );
        accessor.removeFirst( cabinet, 2L, 10L );

        // WHEN
        Iterator<Long> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );

        // THEN
        assertEquals( asCollection( asIterable( 2L ) ), asCollection( allOnes ) );

        // WHEN
        Iterator<Long> allTwos = accessor.findAll( cabinet, accessor.returnValues(), 2L );

        // THEN
        assertEquals( asCollection( asIterable( 4L, 5L ) ), asCollection( allTwos ) );
    }

    @Test
    public void shouldRemoveFirstWithSameKey()
    {
        // GIVEN
        insertSomeRecords();

        // WHEN
        accessor.removeFirst( cabinet, 1L );

        // THEN
        Iterator<Long> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );
        assertEquals( asCollection( asIterable( 3L ) ), asCollection( allOnes ) );
    }

    @Test
    public void shouldRemoveAllWithSameKey()
    {
        // GIVEN
        insertSomeRecords();

        // WHEN
        accessor.removeAll( cabinet, 1L );

        // THEN
        Iterator<Long> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );
        assertEquals( Collections.EMPTY_LIST, asCollection( allOnes ) );
    }

    @Test
    public void shouldFindMultipleInsertedElement()
    {
        // GIVEN
        accessor.insertIfMissing( cabinet, 1L, 1L );

        // WHEN
        InMemSkipListRecord<Long, Long> result = accessor.findFirst( cabinet, 1L, 1L );

        // THEN
        assertRecordEquals( 1L, 1L, result );
    }

    private void assertRecordEquals( Long key, Long value, InMemSkipListRecord<Long, Long> record )
    {
        assertEquals( 0, accessor.compareKeys( key, cabinet.getRecordKey( record ) ) );
        assertEquals( 0, accessor.compareValues( value, cabinet.getRecordValue( record ) ) );
    }

    private SkipListAccessor<InMemSkipListRecord<Long, Long>, Long, Long> accessor;
    private SkipListCabinet<InMemSkipListRecord<Long, Long>, Long, Long> cabinet;

    @Before
    public void before()
    {
        accessor = createAccessor( createCabinetProvider() );
        Random rand = new Random();
        long time = System.currentTimeMillis();
        // System.out.println( time );
        rand.setSeed( time );
        // rand.setSeed( 1364759296045L );
        RandomLevelGenerator levelGenerator = new RandomLevelGenerator( rand, 12, 1 );
        cabinet = accessor.openCabinet( levelGenerator );

    }

    @After
    public void after()
    {
        cabinet.release();
        assertFalse( cabinet.isOpen() );
    }

    private SkipListAccessor<InMemSkipListRecord<Long, Long>, Long, Long>
        createAccessor( InMemSkipListCabinetProvider<Long, Long> cabinetProvider )
    {
        return new SkipListAccessor<InMemSkipListRecord<Long, Long>, Long, Long>( cabinetProvider );
    }

    private InMemSkipListCabinetProvider<Long, Long> createCabinetProvider()
    {
        return new InMemSkipListCabinetProvider<Long, Long>();
    }
}
