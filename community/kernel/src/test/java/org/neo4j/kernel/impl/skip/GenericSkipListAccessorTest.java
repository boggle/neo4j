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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.neo4j.helpers.collection.IteratorUtil.asCollection;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.kernel.impl.skip.base.RandomLevelGenerator;
import org.neo4j.kernel.impl.skip.inmem.InMemSkipListRecord;

public abstract class GenericSkipListAccessorTest<R>
{
    @Test
    public void shouldCreateCabinet()
    {
        // ENSURE
        assertFalse( null == cabinet );
    }

    @Test
    public void shouldAccessHeadRecord()
    {
        // WHEN
        R head = cabinet.getHead();

        // THEN
        assertFalse( null == head );
        assertTrue( cabinet.isHead( head ) );
    }

    @Test
    public void shouldKnowOwnNil()
    {
        // ENSURE
        assertTrue( cabinet.isNil( cabinet.nil() ) );
    }

    @Test
    public void shouldHaveHeadThatDiffersFromNil()
    {
        // WHEN
        R head = cabinet.getHead();

        // THEN
        assertFalse( cabinet.isNil( head ) );
    }

    @Test
    public void shouldHaveNilDifferentFromHead()
    {
        // WHEN
        R head = cabinet.getHead();

        // THEN
        assertFalse( cabinet.isNil( head ) );
    }

    @Test
    public void shouldCreateNewNonNilRecord()
    {
        // WHEN
        R record = cabinet.createRecordWithHeight( 8, 0l, "data" );

        // THEN
        assertFalse( cabinet.isNil( record ) );
        assertEquals( (Long) 0l, cabinet.getRecordKey( record ) );
        assertEquals( "data", cabinet.getRecordValue( record ) );
    }

    @Test
    public void shouldCreateRecordsWithPositiveHeight()
    {
        // GIVEN
        R record = cabinet.createRecordWithHeight( 8, 0l, "data" );

        // WHEN
        int height = cabinet.getHeight( record );

        // THEN
        assertTrue( height > 0 );
        assertTrue( height <= cabinet.getMaxHeight() );
    }

    @Test
    public void shouldCreateRecordsWithNextsPointingToNil()
    {
        // GIVEN
        R record = cabinet.createRecordWithHeight( 8, 0l, "data" );

        // WHEN
        int height = cabinet.getHeight( record );

        // THEN
        for (int i = 0; i < height; i++)
            assertTrue( cabinet.isNil( cabinet.getNext( record, i ) ) );
    }

    @Test
    public void shouldCreateRecordsWithUpdateableNexts()
    {
        // GIVEN
        R record = cabinet.createRecordWithHeight( 8, 0l, "data" );

        // WHEN
        int height = cabinet.getHeight( record );
        for (int i = 0; i < height; i++)
            cabinet.setNext( record, i, record );

        // THEN
        for (int i = 0; i < height; i++)
            assertEquals( record, cabinet.getNext( record, i ) );
    }

    @Test
    public void shouldNeverDeleteHead() {
        // GIVEN
        R head = cabinet.getHead();

        // ENSURE
        expectedException.expect( IllegalArgumentException.class );

        // WHEN
        cabinet.removeRecord( head );
    }

    @Test
    public void shouldNotReturnFoundElementWhenEmpty()
    {
        // WHEN
        R result = accessor.findFirst( cabinet, 1L, "a" );

        // THEN
        assertTrue( cabinet.isNil( result ) );
    }

    @Test
    public void shouldReturnHeadAsPredWhenEmpty()
    {
        // WHEN
        R result = accessor.findPred( cabinet, 1L, "a", null );

        // THEN
        assertTrue( cabinet.isHead( result ) );
    }

    @Test
    public void shouldInsertElementWhenEmpty()
    {
        // GIVEN
        accessor.insertIfMissing( cabinet, 1L, "a" );

        // WHEN
        R result = accessor.findFirst( cabinet, 1L, "a" );

        // THEN
        assertRecordEquals( 1L, "a", result );
    }

    @Test
    public void shouldInsertMultipleElementsWithoutFailures()
    {
        // PASS
        insertSomeRecords();
    }

    private void insertSomeRecords()
    {
        accessor.insertIfMissing( cabinet, 2L, "d" );
        accessor.insertIfMissing( cabinet, 2L, "e" );
        accessor.insertIfMissing( cabinet, 1L, "b" );
        accessor.insertIfMissing( cabinet, 8L, "v" );
        accessor.insertIfMissing( cabinet, 1L, "c" );
        accessor.insertIfMissing( cabinet, 7L, "v" );
    }

    @Test
    public void shouldFindInsertedElements() {
        // WHEN
        insertSomeRecords();

        // THEN
        assertRecordEquals( 1L, "b", accessor.findFirst( cabinet, 1L ) );
        assertTrue( accessor.contains( cabinet, 1L, "c" ) );
        assertTrue( accessor.contains( cabinet, 2L, "d" ) );
        assertTrue( accessor.contains( cabinet, 2L, "e" ) );
        assertRecordEquals( 8L, "v", accessor.findFirst( cabinet, 8L ) );

        // WHEN
        Iterator<String> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );

        // THEN
        assertEquals( strCollection( "b", "c" ), asCollection( allOnes ) );

        // WHEN
        Iterator<String> allTwos = accessor.findAll( cabinet, accessor.returnValues(), 2L );

        // THEN
        assertEquals( strCollection( "d", "e" ), asCollection( allTwos ) );
    }

    @Test
    public void shouldDeleteOneExisting()
    {
        insertSomeRecords();

        // WHEN
        accessor.removeFirst( cabinet, 1L, "b" );

        // THEN
        assertFalse( accessor.contains( cabinet, 1L, "b" ) );
    }

    @Test
    public void shouldDeleteTwoExisting()
    {
        // GIVEN
        insertSomeRecords();
        accessor.removeFirst( cabinet, 1L, "c" );
        accessor.removeFirst( cabinet, 2L, "v" );

        // WHEN
        Iterator<String> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );

        // THEN
        assertEquals( strCollection( "b" ), asCollection( allOnes ) );

        // WHEN
        Iterator<String> allTwos = accessor.findAll( cabinet, accessor.returnValues(), 2L );

        // THEN
        assertEquals( strCollection( "d", "e" ), asCollection( allTwos ) );
    }

    @Test
    public void shouldRemoveFirstWithSameKey()
    {
        // GIVEN
        insertSomeRecords();

        // WHEN
        accessor.removeFirst( cabinet, 1L );

        // THEN
        Iterator<String> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );
        assertEquals( strCollection( "c" ), asCollection( allOnes ) );
    }

    @Test
    public void shouldRemoveAllWithSameKey()
    {
        // GIVEN
        insertSomeRecords();

        // WHEN
        accessor.removeAll( cabinet, 1L );

        // THEN
        Iterator<String> allOnes = accessor.findAll( cabinet,  accessor.returnValues(), 1L );
        assertEquals( Collections.EMPTY_LIST, asCollection( allOnes ) );
    }

    @Test
    public void shouldFindMultipleInsertedElement()
    {
        // GIVEN
        accessor.insertIfMissing( cabinet, 1L, "a" );

        // WHEN
        R result = accessor.findFirst( cabinet, 1L, "a" );

        // THEN
        assertRecordEquals( 1L, "a", result );
    }

    private void assertRecordEquals( Long key, String value, R record )
    {
        assertEquals( 0, accessor.compareKeys( key, cabinet.getRecordKey( record ) ) );
        assertEquals( 0, accessor.compareValues( value, cabinet.getRecordValue( record ) ) );
    }


    private SkipListCabinet<R, Long, String> cabinet;
    private SkipListAccessor<R, Long, String> accessor;
    private LevelGenerator levelGenerator;

    @Before
    public void before() throws Exception
    {
        accessor = createAccessor( );
        Random rand = new Random();
        long time = System.currentTimeMillis();
        // System.out.println( time );
        rand.setSeed( time );
        // rand.setSeed( 1364759296045L );
        RandomLevelGenerator levelGenerator = new RandomLevelGenerator( rand, 12, 1 );
        this.cabinet = accessor.openCabinet( levelGenerator );

    }

    @After
    public void after() throws Exception
    {
        cabinet.release();
        assertFalse( cabinet.isOpen() );
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected abstract SkipListAccessor<R, Long, String> createAccessor( );

    protected Collection<String> strCollection( String... elems ) {
        final ArrayList<String> list = new ArrayList<String>( elems.length );
        for ( String str : elems )
            list.add( str );
        return list;
    }
}
