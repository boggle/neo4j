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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.helpers.Pair;

public class LabelRuleRepository
{
    private final Map<Long, LabelRule> impliedLabels = new HashMap<Long, LabelRule>();

    public void add( LabelRule rule )
    {
        if( hasLabelId( rule.getLabel() ) )
        {
            impliedLabels.put( rule.getLabel(), rule );
        }
        else
            throw new IllegalStateException( "Duplicate label rule: " + rule.toString() );
    }

    public boolean hasLabelId( long labelId )
    {
        return impliedLabels.containsKey( labelId );
    }


    public void update( LabelRule rule )
    {
        impliedLabels.put( rule.getLabel(), rule );
    }

    public void remove( LabelRule rule )
    {
        if( hasLabelId( rule.getLabel() ) )
        {
            impliedLabels.get( rule.getLabel() );
        }
    }

    public Set<Long> getDirectlyImpliedLabels( Iterator<Long> labelIds )
    {
        Set<Long> result = new HashSet<Long>();
        while(labelIds.hasNext())
        {
            LabelRule rule = impliedLabels.get( labelIds.next() );
            if (rule != null)
                rule.addImpliedToLabelIdSet( result );
        }
        return result;
    }

    public Set<Long> getTransitivelyImpliedLabels( Iterator<Long> labelIds )
    {
        Set<Long> frontier = getDirectlyImpliedLabels( labelIds );
        Set<Long> seen = new HashSet<Long>();
        while (! frontier.isEmpty() )
        {
            Set<Long> nextFrontier = new HashSet<Long>();
            for (long impliedId : frontier)
            {
                LabelRule impliedExtension = impliedLabels.get( impliedId );
                if ( impliedExtension != null )
                    impliedExtension.addImpliedToLabelIdSet( nextFrontier );
            }
            seen.addAll( frontier );
            frontier = nextFrontier;
            frontier.removeAll( seen );
        }

        return seen;
    }
}
