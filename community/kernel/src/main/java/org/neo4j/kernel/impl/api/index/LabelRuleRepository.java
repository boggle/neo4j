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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LabelRuleRepository
{
    private final Map<Long, Set<Long>> impliedLabels = new HashMap<Long, Set<Long>>();

    public void add( LabelRule rule )
    {
        if(!impliedLabels.containsKey( rule.getLabel() ))
        {
            impliedLabels.put( rule.getLabel(), new HashSet<Long>() );
        }

        rule.addToLabelIdSet( impliedLabels.get( rule.getLabel() ) );
    }

    public void remove( LabelRule rule )
    {
        if(impliedLabels.containsKey( rule.getLabel() ))
        {
            rule.removeFromLabelIdSet( impliedLabels.get( rule.getLabel() ) );
        }
    }
    public Set<Long> getDirectlyImpliedLabels( long[] labelIds )
    {
        Set<Long> result = new HashSet<Long>();
        for (long labelId : labelIds)
        {
            Set<Long> longSet = impliedLabels.get( labelId );
            if (longSet != null)
                result.addAll( longSet );
        }
        return result;
    }

    public Set<Long> getTransitivelyImpliedLabels( long[] labelIds )
    {
        Set<Long> frontier = getDirectlyImpliedLabels( labelIds );
        Set<Long> seen = new HashSet<Long>();
        while (! frontier.isEmpty() )
        {
            Set<Long> nextFrontier = new HashSet<Long>();
            for (long impliedId : frontier)
            {
                Set<Long> addToNextFrontier = impliedLabels.get( impliedId );
                if ( addToNextFrontier != null )
                    nextFrontier.addAll( addToNextFrontier );
            }
            seen.addAll( frontier );
            frontier = nextFrontier;
            frontier.removeAll( seen );
        }

        return seen;
    }
}
