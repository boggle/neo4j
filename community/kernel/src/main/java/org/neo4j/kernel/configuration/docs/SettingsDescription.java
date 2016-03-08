package org.neo4j.kernel.configuration.docs;

import java.util.stream.Stream;

import org.neo4j.kernel.configuration.AsciiDocItem;

public class SettingsDescription
{
    public static SettingsDescription describe( Class<?> settings )
    {
        return new SettingsDescription();
    }

    public Stream<AsciiDocItem> settings()
    {
        // .sorted( (a,b) -> a.name().compareTo( b.name() ) )

        return null;
    }
}
