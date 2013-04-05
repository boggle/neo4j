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
import org.neo4j.kernel.impl.skip.base.SkipListCabinetBase;

public class SkipListCabinetBaseTest
{
    @Test
    public void shouldBeOpenWhenCreated() {
        // GIVEN
        SkipListCabinetBase<Long, Long, Long> context = createCabinet();

        // THEN
        assertTrue( context.isOpen() );
    }

    @Test
    public void shouldCloseWhenReleasedAfterCreate() {
        // GIVEN
        SkipListCabinetBase<Long, Long, Long> context = createCabinet();

        // WHEN
        context.release();

        // THEN
        assertFalse( context.isOpen() );
    }

    @Test
    public void shouldCloseAfterMultipleAcquires() {
        // GIVEN
        SkipListCabinetBase<Long, Long, Long> context = createCabinet();

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
        SkipListCabinetBase<Long, Long, Long> context = createCabinet();

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
        SkipListCabinetBase<Long, Long, Long> context = createCabinet();
        context.release();
        assertFalse( context.isOpen() );

        // WHEN
        context.close();
    }

    public SkipListCabinetBase<Long, Long, Long> createCabinet() {
        return new SkipListCabinetBaseAdapter<Long, Long, Long>( 8 )
        {
            @Override
            public Long[] newVisitationArray()
            {
                return new Long[ 8 ];
            }

            @Override
            public SkipListCabinet<Long, Long, Long> reopen()
            {
                close();
                return createCabinet();
            }
        };
    }
}
