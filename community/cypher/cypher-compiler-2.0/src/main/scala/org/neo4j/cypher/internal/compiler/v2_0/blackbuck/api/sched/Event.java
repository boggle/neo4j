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

    public Event<C> message( Object nextDestination, C cursor )
    {
        return message( this.destination, nextDestination, cursor );
    }

    public Event<C> output( C cursor )
    {
        return message( this.destination, null, cursor );
    }

    public boolean isInput()
    {
        return null == source;
    }

    public boolean isOutput()
    {
        return null == destination;
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

    public static <C extends Cursor<C>> Event<C> input( Object destination, C cursor )
    {
        return new Event<>( null, destination, cursor );
    }

    public static <C extends Cursor<C>> Event<C> output( Object source, C cursor )
    {
        return new Event<>( source, null, cursor );
    }
}
