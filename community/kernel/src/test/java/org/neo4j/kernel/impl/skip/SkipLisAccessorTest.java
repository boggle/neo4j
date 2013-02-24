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

import org.junit.Before;
import org.junit.Test;

public class SkipLisAccessorTest
{
    @Test
    public void shouldKnowMaximumPositiveHeightWhenCreated()
    {
        // WHEN
        int maxHeight = accessor.getMaxLevelIndex( cabinet );

        // THEN
        assert(maxHeight > 0);
    }

    private SkipListAccessor<InMemSkipListRecord<Long, String>, Long, String> accessor;
    private SkipListCabinet<InMemSkipListRecord<Long, String>, Long, String> cabinet;

    @Before
    public void before()
    {
        accessor = createAccessor( createCabinetProvider() );
        cabinet = accessor.getDefaultCabinet();

    }

    private SkipListAccessor<InMemSkipListRecord<Long, String>, Long, String>
        createAccessor( InMemSkipListCabinetProvider<Long, String> cabinetProvider )
    {
        return new SkipListAccessor<InMemSkipListRecord<Long, String>, Long, String>( cabinetProvider );
    }

    private InMemSkipListCabinetProvider<Long, String> createCabinetProvider()
    {
        return new InMemSkipListCabinetProvider<Long, String>(8);
    }
}
