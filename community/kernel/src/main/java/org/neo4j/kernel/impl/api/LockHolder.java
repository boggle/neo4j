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
package org.neo4j.kernel.impl.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transaction;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.kernel.impl.transaction.LockManager;
import org.neo4j.kernel.impl.transaction.LockType;

public class LockHolder
{
    private final LockManager lockManager;
    private final Transaction tx;
    private final List<Releasable> locks = new ArrayList<Releasable>();

    public LockHolder( LockManager lockManager, Transaction tx )
    {
        this.lockManager = lockManager;
        this.tx = tx;
    }

    public void acquireNodeReadLock( long nodeId )
    {
        NodeLock resource = new NodeLock( nodeId, LockType.READ );
        lockManager.getReadLock( resource );
        locks.add( resource );
    }

    public void acquireNodeWriteLock( long nodeId )
    {
        NodeLock resource = new NodeLock( nodeId, LockType.WRITE );
        lockManager.getWriteLock( resource );
        locks.add( resource );
    }

    public void releaseLocks()
    {
        Collection<Releasable> releaseFailures = null;
        Exception releaseException = null;
        for ( Releasable lockElement : locks )
        {
            try
            {
                lockElement.release();
            }
            catch ( Exception e )
            {
                releaseException = e;
                if ( releaseFailures == null )
                    releaseFailures = new ArrayList<Releasable>();
                releaseFailures.add( lockElement );
            }
        }
        
        if ( releaseException != null )
        {
//            log.warn( "Unable to release locks: " + releaseFailures + ". Example of exception:" + releaseException );
        }
    }
    
    private interface Releasable
    {
        void release();
    }

    // Have them be releasable also since they are internal and will save the
    // amount of garbage produced
    private class NodeLock implements Node, Releasable
    {
        private final long id;
        private final LockType lockType;

        public NodeLock( long id, LockType lockType )
        {
            this.id = id;
            this.lockType = lockType;
        }
        
        @Override
        public void release()
        {
            lockType.release( lockManager, lockType, tx );
        }

        @Override
        public boolean equals( Object o )
        {
            if ( !(o instanceof Node) )
            {
                return false;
            }
            return this.getId() == ((Node) o).getId();
        }

        @Override
        public int hashCode()
        {
            return (int) (( id >>> 32 ) ^ id );
        }

        @Override
        public long getId()
        {
            return id;
        }

        @Override
        public void delete()
        {
        }

        @Override
        public Iterable<Relationship> getRelationships()
        {
            return null;
        }

        @Override
        public boolean hasRelationship()
        {
            return false;
        }

        @Override
        public Iterable<Relationship> getRelationships( RelationshipType... types )
        {
            return null;
        }

        @Override
        public Iterable<Relationship> getRelationships( Direction direction, RelationshipType... types )
        {
            return null;
        }

        @Override
        public boolean hasRelationship( RelationshipType... types )
        {
            return false;
        }

        @Override
        public boolean hasRelationship( Direction direction, RelationshipType... types )
        {
            return false;
        }

        @Override
        public Iterable<Relationship> getRelationships( Direction dir )
        {
            return null;
        }

        @Override
        public boolean hasRelationship( Direction dir )
        {
            return false;
        }

        @Override
        public Iterable<Relationship> getRelationships( RelationshipType type, Direction dir )
        {
            return null;
        }

        @Override
        public boolean hasRelationship( RelationshipType type, Direction dir )
        {
            return false;
        }

        @Override
        public Relationship getSingleRelationship( RelationshipType type, Direction dir )
        {
            return null;
        }

        @Override
        public Relationship createRelationshipTo( Node otherNode, RelationshipType type )
        {
            return null;
        }

        @Override
        public Traverser traverse( Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator
                returnableEvaluator, RelationshipType relationshipType, Direction direction )
        {
            return null;
        }

        @Override
        public Traverser traverse( Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator
                returnableEvaluator, RelationshipType firstRelationshipType, Direction firstDirection,
                                   RelationshipType secondRelationshipType, Direction secondDirection )
        {
            return null;
        }

        @Override
        public Traverser traverse( Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator
                returnableEvaluator, Object... relationshipTypesAndDirections )
        {
            return null;
        }

        @Override
        public GraphDatabaseService getGraphDatabase()
        {
            return null;
        }

        @Override
        public boolean hasProperty( String key )
        {
            return false;
        }

        @Override
        public Object getProperty( String key )
        {
            return null;
        }

        @Override
        public Object getProperty( String key, Object defaultValue )
        {
            return null;
        }

        @Override
        public void setProperty( String key, Object value )
        {
        }

        @Override
        public Object removeProperty( String key )
        {
            return null;
        }

        @Override
        public Iterable<String> getPropertyKeys()
        {
            return null;
        }

        @Override
        public Iterable<Object> getPropertyValues()
        {
            return null;
        }
    }
}
