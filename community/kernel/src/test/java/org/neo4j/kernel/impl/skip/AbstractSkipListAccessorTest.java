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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AbstractSkipListAccessorTest
{

    @Test
    public void shouldBeOpenWhenCreated() {
        // GIVEN
        AbstractSkipListCabinet<Long, Long, Long> context = createCabinet();

        // THEN
        assertTrue( context.isOpen() );
    }

    @Test
    public void shouldCloseWhenReleasedAfterCreate() {
        // GIVEN
        AbstractSkipListCabinet<Long, Long, Long> context = createCabinet();

        // WHEN
        context.release();

        // THEN
        assertFalse( context.isOpen() );
    }


    @Test
    public void shouldCloseAfterMultipleAcquires() {
        // GIVEN
        AbstractSkipListCabinet<Long, Long, Long> context = createCabinet();

        // WHEN
        context.acquire();
        context.acquire();
        context.acquire();
        context.close();

        // THEN
        assertFalse( context.isOpen() );
    }

    @Test
    public void shouldCloseAfterMultipleMatchingAcquiresAndReleases() {
        // GIVEN
        AbstractSkipListCabinet<Long, Long, Long> context = createCabinet();

        // WHEN
        context.acquire();
        context.acquire();
        context.release();
        context.release();
        context.release();

        // THEN
        assertFalse( context.isOpen() );
    }


    @Test(expected = /* THEN */ IllegalStateException.class)
    public void shouldThrowWhenClosingTwice() {
        // GIVEN
        AbstractSkipListCabinet<Long, Long, Long> context = createCabinet();
        context.release();
        assertFalse( context.isOpen() );

        // WHEN
        context.close();
    }

    public AbstractSkipListCabinet<Long, Long, Long> createCabinet() {
        return new AbstractSkipListCabinet<Long, Long, Long>( 8 )
        {
            @Override
            public Long nil()
            {
                return null;
            }

            @Override
            public Long getHead()
            {
                return null;
            }

            @Override
            public boolean isNil( Long aLong )
            {
                return false;
            }

            @Override
            public boolean isHead( Long aLong )
            {
                return false;
            }

            @Override
            public Long createRecord( int height, Long key, Long data )
            {
                return null;
            }

            @Override
            public void removeRecord( Long record )
            {
                throw new UnsupportedOperationException(  );
            }

            @Override
            public boolean areSameRecord( Long first, Long second )
            {
                return first == second;
            }

            @Override
            public Long getRecordKey( Long record )
            {
                return null;
            }

            @Override
            public Long getRecordValue( Long record )
            {
                return null;
            }

            @Override
            public Long setRecordValue( Long entry, Long newData )
            {
                return null;
            }

            @Override
            public int getHeight( Long record )
            {
                return 1;
            }

            @Override
            public Long getLowestNext( Long entry )
            {
                return null;
            }

            @Override
            public Long getNext( Long record, int i )
            {
                return null;
            }

            @Override
            public void setNext( Long record, int i, Long newNext )
            {
            }

            @Override
            public Long nextAtLevelLessThan( Long record, int level, Long key, Long value )
            {
                return null;
            }

            @Override
            protected void finish()
            {
            }
        };
    }
}
