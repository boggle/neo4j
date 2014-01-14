package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.proc.Proc;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Pos;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.TypedSlot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DefaultProc<P extends Pos<P>> implements Proc<P> {
    private final String name;

    private final Set<SlotReader<P>> readers;
    private final Set<SlotWriter<P>> writers;

    @SuppressWarnings("unchecked")
    protected DefaultProc( String name, TypedSlot... slots )
    {
        this.name = name;

        Set<SlotReader<P>> readers = new HashSet<>();
        Set<SlotWriter<P>> writers = new HashSet<>();

        for ( TypedSlot slot : slots )
        {
            if ( slot instanceof SlotReader )
            {
                readers.add( (SlotReader<P>) slot );
            }
            if ( slot instanceof SlotWriter )
            {
                writers.add( (SlotWriter<P>) slot );
            }
        }

        this.readers = Collections.unmodifiableSet( readers );
        this.writers = Collections.unmodifiableSet( writers );
    }


    @Override
    public String name()
    {
        return name;
    }

    @Override
    public Set<SlotReader<P>> reads()
    {
        return readers;
    }

    @Override
    public Set<SlotWriter<P>> writes()
    {
        return writers;
    }
}
