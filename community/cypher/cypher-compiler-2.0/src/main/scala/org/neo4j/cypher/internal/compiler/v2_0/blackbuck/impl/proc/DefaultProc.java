package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.impl.proc;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.proc.Proc;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotReader;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.SlotWriter;
import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.TypedSlot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class DefaultProc<C extends Cursor<C>> implements Proc<C> {
    private final String name;

    private final Set<SlotReader<C>> readers;
    private final Set<SlotWriter<C>> writers;

    @SuppressWarnings("unchecked")
    protected DefaultProc( String name, TypedSlot... slots )
    {
        this.name = name;

        Set<SlotReader<C>> readers = new HashSet<>();
        Set<SlotWriter<C>> writers = new HashSet<>();

        for ( TypedSlot slot : slots )
        {
            if ( slot instanceof SlotReader )
            {
                readers.add( (SlotReader<C>) slot );
            }
            if ( slot instanceof SlotWriter )
            {
                writers.add( (SlotWriter<C>) slot );
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
    public Set<SlotReader<C>> reads()
    {
        return readers;
    }

    @Override
    public Set<SlotWriter<C>> writes()
    {
        return writers;
    }
}
