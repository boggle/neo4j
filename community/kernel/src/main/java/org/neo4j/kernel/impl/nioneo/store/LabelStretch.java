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

public final class LabelStretch implements Comparable<LabelStretch>
{
    private final long labelId;
    private final int stretchId;

    public LabelStretch( long labelId, int stretchId )
    {
        this.labelId = labelId;
        this.stretchId = stretchId;
    }

    public long labelId()
    {
        return labelId;
    }

    public int stretchId()
    {
        return stretchId;
    }

    @Override
    public int compareTo( LabelStretch o )
    {
        int result = Long.compare( labelId, o.labelId );
        if ( 0l == result )
        {
            result = Integer.compare( stretchId, o.stretchId );
        }
        return result;
    }

    public int nodeId( int k )
    {
        return (stretchId << LabelScanStore.STRETCH_SHIFT) + k;
    }
}

