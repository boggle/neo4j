/*
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.kernel.impl.procedures.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.ThisShouldNotHappenError;
import org.neo4j.helpers.collection.Visitor;

import static org.mozilla.javascript.Context.getCurrentContext;

/**
 * TODO
 */
public class Neo4jRhinoStdLib implements Visitor<Scriptable,RuntimeException>
{
    private final Map<String,Object> bindings = new HashMap<>();

    public Object require( String key )
    {
        Object o = bindings.get( key );
        if ( o == null )
        {
            throw new IllegalArgumentException( String.format( "'%s' cannot be found by require().", key ) );
        }

        return o;
    }

    /** Make an object available to 'require' in javascript-land. */
    public Neo4jRhinoStdLib bind( String name, Object instance )
    {
        bindings.put( name, instance );
        return this;
    }

    @Override
    public boolean visit( Scriptable scope )
    {
        try
        {
            scope.put( "require", scope,
                    new FunctionObject( "require", getClass().getMethod( "require", String.class ), scope )
                    {
                        @Override
                        public Object call( Context cx, Scriptable scope, Scriptable thisObj, Object[] args )
                        {
                            return require( (String) args[0] );
                        }
                    } );
            scope.put( "label", scope,
                    new FunctionObject( "label", DynamicLabel.class.getMethod( "label", String.class ), scope ) );
            scope.put( "type", scope,
                    new FunctionObject( "type", DynamicRelationshipType.class.getMethod( "withName", String.class ),
                            scope ) );

            bind( "neo4j.findNodes", new FunctionObject( "findNodes",
                    NativeJavaIterator.class.getConstructor(), scope )
            {
                @Override
                public Object call( Context cx, Scriptable scope, Scriptable thisObj, Object[] args )
                {
                    Scriptable topScope = scope.getParentScope().getPrototype();

                    GraphDatabaseService gds = (GraphDatabaseService) Context.jsToJava( ((Scriptable) topScope.get(
                            "neo4j", scope )).get( "db", scope ), GraphDatabaseService.class );

                    Iterator result = gds.findNodes( DynamicLabel.label( (String) args[0] ), (String) args[1],
                            args[2] );
                    return cx.newObject( scope, "NativeJavaIterator", new Object[]{result} );
                }
            } );

            bind( "neo4j.OUTGOING", Direction.OUTGOING );
            bind( "neo4j.INCOMING", Direction.INCOMING );
            bind( "neo4j.BOTH", Direction.BOTH );

            for ( Map.Entry<String,Object> binding : bindings.entrySet() )
            {
                bind( binding.getKey(), scope, Context.javaToJS( binding.getValue(), scope ) );
            }
        }
        catch ( NoSuchMethodException e )
        {
            throw new ThisShouldNotHappenError( "jake", "This function must exist at this point." );
        }
        return false;
    }

    private void bind( String name, Scriptable scope, Object value )
    {
        String[] names = name.split( "\\." );
        Scriptable currentScope = scope;
        for ( int i = 0; i < names.length - 1; i++ )
        {
            String str = names[i];
            Scriptable nextScope;
            if ( currentScope.has( str, currentScope ) )
            {
                nextScope = (Scriptable) currentScope.get( str, currentScope );
            }
            else
            {

                nextScope = getCurrentContext().newObject( currentScope );
                currentScope.put( str, currentScope, nextScope );
            }
            currentScope = nextScope;
        }

        currentScope.put( names[names.length - 1], currentScope, value );
    }
}