/**
 * Copyright (c) 2002-2014 "Neo Technology,"
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
package org.neo4j.cypher.internal.compiler.v2_1.runtime;

import static java.lang.String.format;

public class RegisterSignature
{
    private static final RegisterSignature EMPTY = new RegisterSignature();

    private final int valueRegisters;
    private final int entityRegisters;

    private RegisterSignature()
    {
        this( 0, 0 );
    }

    public RegisterSignature( int valueRegisters, int entityRegisters )
    {
        this.valueRegisters = ensurePositiveOrNull( "Number of value registers", valueRegisters );
        this.entityRegisters = ensurePositiveOrNull( "Number of entity registers", entityRegisters );
    }

    public int valueRegisters()
    {
        return valueRegisters;
    }

    public int entityRegisters()
    {
        return entityRegisters;
    }

    public RegisterSignature withValueRegisters( int newValueRegisters )
    {
        return new RegisterSignature( newValueRegisters, entityRegisters );
    }

    public RegisterSignature withEntityRegisters( int newEntityRegisters )
    {
        return new RegisterSignature( valueRegisters, newEntityRegisters );
    }

    private int ensurePositiveOrNull( String what, int number )
    {
        if ( number < 0 )
        {
            throw new IllegalArgumentException( format( "%s expected to be >= 0, but is: %d", what, number ) );
        }
        return number;
    }

    public static RegisterSignature empty()
    {
        return EMPTY;
    }

    public static RegisterSignature newWithValueRegisters( int newValueRegisters )
    {
        return EMPTY.withValueRegisters( newValueRegisters );
    }

    public static RegisterSignature newWithEntityRegisters( int newEntityRegisters )
    {
        return EMPTY.withEntityRegisters( newEntityRegisters );
    }
}
