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
package org.neo4j.kernel.impl.nioneo.store;

import java.io.File;

import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.nioneo.store.windowpool.WindowPoolFactory;
import org.neo4j.kernel.impl.skip.store.SkipListStore;
import org.neo4j.kernel.impl.util.StringLogger;

public class LabelScanStore extends SkipListStore<LabelStretch, long[]>
{
    public static final String TYPE_DESCRIPTOR = "LabelScanStore";

    // STRETCH_SHIFT last bits of node id fall into same stretch
    public static final int STRETCH_SHIFT = 8;

    // STRETCH_SIZE nodes are in a single stretch
    public static final int STRETCH_SIZE = 1 << STRETCH_SHIFT;
    public static final int STRETCH_LONGS = STRETCH_SIZE >> 6;

    public static final int STRETCH_MASK = STRETCH_SIZE - 1;

    // store version, each store ends with this string (byte encoded)
    public static final String VERSION = buildTypeDescriptorAndVersion( TYPE_DESCRIPTOR );

    public LabelScanStore( File fileName, Config conf, IdGeneratorFactory idGeneratorFactory,
                           WindowPoolFactory windowPoolFactory, FileSystemAbstraction fileSystemAbstraction,
                           StringLogger stringLogger )
    {
        super( fileName, conf, IdType.LABEL_SCAN, idGeneratorFactory,
                windowPoolFactory, fileSystemAbstraction, stringLogger,
                new RecordFieldSerializer.LabelStretchSerializer(),
                new RecordFieldSerializer.LongArray( STRETCH_LONGS ) );
    }

    @Override
    public String getTypeDescriptor()
    {
        return TYPE_DESCRIPTOR;
    }
}