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
package org.neo4j.kernel.builtinprocs;

import org.neo4j.helpers.Service;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.factory.ProceduresProvider;
import org.neo4j.kernel.impl.proc.Procedures;

@Service.Implementation( ProceduresProvider.class )
public class BuiltInProceduresProvider extends Service implements ProceduresProvider
{
    public BuiltInProceduresProvider()
    {
        super( "built-in-procedures-provider" );
    }

    @Override
    public void registerProcedures( Procedures procedures )
    {
        actuallyRegisterProcedures( procedures, BuiltInProcedures.class );
    }

    // Extracted into a static method this way so it can be re-used from other classes
    public static void actuallyRegisterProcedures( Procedures procedures, Class<?> proceduresClass )
    {
        try
        {
            procedures.register( proceduresClass );
        }
        catch ( KernelException e )
        {
            throw new RuntimeException( e );
        }
    }
}
