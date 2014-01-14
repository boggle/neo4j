package org.neo4j.cypher.internal.compiler.v2_0.blackbuck.api.sched;

public final class Event<D>
{
    private final Object source;
    private final Object destination;
    private final D payload;
    private final boolean last;

    private Event( Object source, Object destination, D payload, boolean last )
    {
        this.source = source;
        this.destination = destination;
        this.payload = payload;
        this.last = last;
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

    public boolean isLast()
    {
        return last;
    }

    public Object source()
    {
        return source;
    }

    public Object destination()
    {
        return destination;
    }

    public D payload()
    {
        return payload;
    }

    public static <D> Event<D> message( Object source, Object destination, D payload, boolean last )
    {
        return new Event<D>( source, destination, payload, last );
    }

    public static <D> Event<D> inputTo( Object destination, D payload, boolean last )
    {
        return message( null, destination, payload, last );
    }

    public static <D> Event<D> outputFrom( Object source, D payload, boolean last )
    {
        return message( source, null, payload, last );
    }
}
