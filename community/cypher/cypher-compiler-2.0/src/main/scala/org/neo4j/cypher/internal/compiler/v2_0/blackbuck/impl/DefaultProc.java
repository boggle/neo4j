package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.Proc;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.SlotReader;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.SlotWriter;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.TypedSlot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DefaultProc implements Proc {
    private final String name;

    private final Set<SlotReader> readers;
    private final Set<SlotWriter> writers;

    protected DefaultProc( String name, TypedSlot... slots )
    {
        this.name = name;

        Set<SlotReader> readers = new HashSet<>();
        Set<SlotWriter> writers = new HashSet<>();

        for ( TypedSlot slot : slots )
        {
            if ( slot instanceof SlotReader )
            {
                readers.add( (SlotReader) slot );
            }
            if ( slot instanceof SlotWriter )
            {
                writers.add( (SlotWriter) slot );
            }
        }

        this.readers = Collections.unmodifiableSet( readers );
        this.writers = Collections.unmodifiableSet( writers );
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public Set<SlotReader> reads() {
        return readers;
    }

    @Override
    public Set<SlotWriter> writes() {
        return writers;
    }
}
