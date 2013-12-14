package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

import org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.slot.Cursor;

public final class Event<C extends Cursor<C>>
{
    private final Object source;
    private final Object destination;
    private final C cursor;

    private Event( Object source, Object destination, C cursor )
    {
        this.source = source;
        this.destination = destination;
        this.cursor = cursor;
    }

    public boolean isInput()
    {
        return null == source;
    }

    public boolean isOutput()
    {
        return null == destination;
    }

    public boolean isFrom( Object key )
    {
        return key == source;
    }

    public boolean isTo( Object key )
    {
        return key == destination;
    }

    public boolean isLast() {
        return cursor.isLastChunk();
    }

    public Object source()
    {
        return source;
    }

    public Object destination()
    {
        return destination;
    }

    public C cursor()
    {
        return cursor;
    }

    public static <C extends Cursor<C>> Event<C> message( Object source, Object destination, C cursor )
    {
        return new Event<>( source, destination, cursor );
    }

    public static <C extends Cursor<C>> Event<C> inputTo( Object destination, C cursor )
    {
        return message( null, destination, cursor );
    }

    public static <C extends Cursor<C>> Event<C> outputFrom( Object source, C cursor )
    {
        return message( source, null, cursor );
    }
}
