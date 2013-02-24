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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InMemSkipListTest
{
    private SkipListCabinet<InMemSkipListRecord<Long, String>, Long, String> cabinet;

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
        InMemSkipListRecord<Long, String> head = cabinet.getHead();

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
        InMemSkipListRecord<Long, String> head = cabinet.getHead();
        
        // THEN
        assertFalse( cabinet.isNil( head ) );
    }

    @Test
    public void shouldHaveNilDifferentFromHead()
    {
        // WHEN
        InMemSkipListRecord<Long, String> head = cabinet.getHead();

        // THEN
        assertFalse( cabinet.isNil( head ) );
    }

    @Test
    public void shouldCreateNewNonNilRecord()
    {
        // WHEN
        InMemSkipListRecord<Long, String> record = cabinet.createRecord( 8, 0l, "data" );

        // THEN
        assertFalse( cabinet.isNil( record ) );
        assertEquals( (Long) 0l, cabinet.getRecordKey( record ) );
        assertEquals( "data", cabinet.getRecordValue( record ) );
    }

    @Test
    public void shouldUpdateRecordValue()
    {
        // GIVEN
        InMemSkipListRecord<Long, String> record = cabinet.createRecord( 8, 0l, "data" );

        // WHEN
        cabinet.setRecordValue( record, "henry" );

        // THEN
        assertFalse( cabinet.isNil( record ) );
        assertEquals( (Long) 0l, cabinet.getRecordKey( record ) );
        assertEquals( "henry", cabinet.getRecordValue( record ) );
    }

    @Test
    public void shouldCreateRecordsWithPositiveHeight()
    {
        // GIVEN
        InMemSkipListRecord<Long, String> record = cabinet.createRecord( 8, 0l, "data" );

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
        InMemSkipListRecord<Long, String> record = cabinet.createRecord( 8, 0l, "data" );

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
        InMemSkipListRecord<Long, String> record = cabinet.createRecord( 8, 0l, "data" );

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
        InMemSkipListRecord<Long, String> head = cabinet.getHead();

        // ENSURE
        expectedException.expect( IllegalArgumentException.class );

        // WHEN
        cabinet.removeRecord( head );
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before()
    {
        InMemSkipListCabinetProvider<Long, String> cabinetProvider = createCabinetProvider();
        this.cabinet = cabinetProvider.getDefaultCabinet();
    }
    
    private InMemSkipListCabinetProvider<Long, String> createCabinetProvider()
    {
        return new InMemSkipListCabinetProvider<Long, String>( 8 );
    }
}
