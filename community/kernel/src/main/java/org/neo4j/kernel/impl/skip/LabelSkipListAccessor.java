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

import org.neo4j.kernel.impl.nioneo.store.LabelStretch;
import org.neo4j.kernel.impl.skip.base.SkipListAccessorBase;

public class LabelSkipListAccessor<R> extends SkipListAccessorBase<R, LabelStretch, byte[]>
{
    private final int size;

    public LabelSkipListAccessor( SkipListCabinetProvider<R, LabelStretch, byte[]> cabinetProvider, int size )
    {
        super( cabinetProvider );
        this.size = size;
    }

    @Override
    public int compareKeys( LabelStretch a, LabelStretch b )
    {
        return a.compareTo( b );
    }

    @Override
    public int compareValues( byte[] a, byte[] b )
    {
        return 0;
    }
}
