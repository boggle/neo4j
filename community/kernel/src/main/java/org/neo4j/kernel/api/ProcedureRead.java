/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.kernel.api;

import org.neo4j.collection.RawIterator;
import org.neo4j.kernel.api.exceptions.ProcedureException;
import org.neo4j.proc.Procedure;
import org.neo4j.proc.ProcedureSignature;

public interface ProcedureRead
{
    /** For read procedures, this key will be available in the invocation context as a means to access the current read statement. */
    Procedure.Key<ReadOperations> readStatement = Procedure.Key.key("statementContext.read", ReadOperations.class );

    /** Fetch a procedure given its signature. */
    ProcedureSignature procedureGet( ProcedureSignature.ProcedureName name ) throws ProcedureException;

    /** Invoke a read-only procedure by name */
    RawIterator<Object[], ProcedureException> procedureCallRead( ProcedureSignature.ProcedureName name, Object[] input ) throws ProcedureException;
}
