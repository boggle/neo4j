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
package org.neo4j.kernel.impl.api.operations;

import org.neo4j.helpers.collection.Visitor;
import org.neo4j.kernel.api.procedure.ProcedureException;
import org.neo4j.kernel.api.procedure.ProcedureSignature;
import org.neo4j.kernel.api.procedure.ProcedureSignature.ProcedureName;
import org.neo4j.kernel.impl.api.KernelStatement;

/**
 * TODO
 */
public interface ProcedureExecutionOperations
{
    void verify( KernelStatement statement, ProcedureSignature signature, String language, String code ) throws
            ProcedureException;

    void call( KernelStatement statement, ProcedureName signature, Object[] args, Visitor<Object[], ProcedureException> visitor )
            throws ProcedureException;
}
