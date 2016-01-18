/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.ha;

import java.io.File;
import java.util.Map;

import org.neo4j.kernel.ha.cluster.HighAvailabilityMemberState;
import org.neo4j.kernel.ha.cluster.HighAvailabilityMemberStateMachine;
import org.neo4j.kernel.ha.cluster.member.ClusterMembers;
import org.neo4j.kernel.ha.cluster.modeswitch.HighAvailabilityModeSwitcher;
import org.neo4j.kernel.ha.factory.HighlyAvailableFacadeFactory;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;

/**
 * This has all the functionality of an embedded database, with the addition of services
 * for handling clustering.
 */
public class HighlyAvailableGraphDatabase extends GraphDatabaseFacade
{
    private final ClusterMembers clusterMembers;
    private final HighAvailabilityMemberStateMachine memberStateMachine;

    public HighlyAvailableGraphDatabase( File storeDir, Map<String,String> params, GraphDatabaseFacadeFactory.Dependencies dependencies )
    {
        new HighlyAvailableFacadeFactory().newFacade( storeDir, params, dependencies, this );

        // TODO: TL;DR: Kernel internals need GDS, so the crazy call above handles that circular dependency. However, that means we don't have a good
        //       way to ask for dependencies in our constructor. We should resolve this by removing all remanining Kernel -> GDS dependencies, and then
        //       making this a regular class that simply asks for what it needs in its constructor. This would mean the factory above lives outside this
        //       constructor, rather than inside it, which would help improve the overall state of humanity.
        clusterMembers = getDependencyResolver().resolveDependency( ClusterMembers.class );
        memberStateMachine = getDependencyResolver().resolveDependency( HighAvailabilityMemberStateMachine.class );
    }

    public HighAvailabilityMemberState getInstanceState()
    {
        return memberStateMachine.getCurrentState();
    }

    public String role()
    {
        return clusterMembers.getCurrentMemberRole();
    }

    public boolean isMaster()
    {
        return HighAvailabilityModeSwitcher.MASTER.equals( role() );
    }

    public File getStoreDirectory()
    {
        return new File( getStoreDir() );
    }
}
