package org.neo4j.kernel.impl.query;

public class QuerySourceDescriptor
{
    private final String[] parts;

    public static final QuerySourceDescriptor UNKNOWN = new QuerySourceDescriptor( "<unknown>");

    public QuerySourceDescriptor( String ... parts )
    {
        this.parts = parts;
    }

    public QuerySourceDescriptor append( String newPart )
    {
        String[] newParts = new String[parts.length + 1];
        System.arraycopy( parts, 0, newParts, 0, parts.length );
        newParts[parts.length] = newPart;
        return new QuerySourceDescriptor( newParts );
    }

    @Override
    public String toString()
    {
        return toString( '\t' );
    }

    public String toString( Character sep )
    {
        StringBuilder builder = new StringBuilder(  );

        boolean isFirst = true;
        for ( String part : parts )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                builder.append( sep );
            }
            builder.append( part );
        }

        return builder.toString();
    }
}
