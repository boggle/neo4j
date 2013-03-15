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
package org.neo4j.kernel.impl.api.index;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.kernel.impl.nioneo.store.AbstractSchemaRule;

public class LabelRule extends AbstractSchemaRule
{
    private final Set<Long> impliedLabelIds;

    public LabelRule( long id, long label, Set<Long> impliedLabelIds )
    {
        super( id, label, Kind.LABEL_RULE );
        if (impliedLabelIds == null)
            throw new IllegalArgumentException( "impliedLabelIds must not be null" );

        this.impliedLabelIds = impliedLabelIds;
    }

    public LabelRule( long id, long labelId, ByteBuffer buffer )
    {
        super( id, labelId, Kind.LABEL_RULE );

        int numImpliedLabelIds = buffer.getInt();
        impliedLabelIds = new HashSet<Long>();
        for (int i = 0; i < numImpliedLabelIds; i++)
            impliedLabelIds.add( buffer.getLong() );
    }

    @Override
    public void serialize( ByteBuffer target )
    {
        super.serialize( target );
        target.putInt( impliedLabelIds.size() );
        for (long impliedLabelId : impliedLabelIds)
            target.putLong( impliedLabelId );
    }


    @Override
    public int length()
    {
        return super.length() + 4 + 8 * impliedLabelIds.size();
    }

    public void addImpliedToLabelIdSet( Set<Long> set )
    {
        for (long impliedId : impliedLabelIds)
            set.add( impliedId );
    }

    public void removeImpliedFromLabelIdSet( Set<Long> set )
    {
        for (long impliedId : impliedLabelIds)
            set.remove( impliedId );
    }

    @Override
    public String innerToString()
    {
        return ", implies: " + Arrays.toString( impliedLabelIds.toArray() );
    }
}
