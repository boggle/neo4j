package org.neo4j.kernel.configuration.docs;

import org.junit.Test;

import java.io.File;

import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.Description;
import org.neo4j.kernel.configuration.Internal;

import static org.neo4j.kernel.configuration.Settings.NO_DEFAULT;
import static org.neo4j.kernel.configuration.Settings.PATH;
import static org.neo4j.kernel.configuration.Settings.setting;
import static org.neo4j.kernel.configuration.docs.SettingsDescription.describe;

public class SettingsDescriptionTest
{
    @Test
    public void shouldDescribeBasicAttributes() throws Throwable
    {
        // When
        SettingsDescription description = describe( SimpleSettings.class );

        // Then

    }

    interface SimpleSettings
    {
        @Description("Path of the logs directory")
        Setting<File> public_nodefault = setting("public.nodefault", PATH, NO_DEFAULT);

        @Description("Path of the logs directory")
        Setting<File> public_with_default = setting("public.default", PATH, "/tmp");

        @Deprecated
        @Description("Path of the logs directory")
        Setting<File> public_deprecated = setting("public.deprecated", PATH, "/tmp");

        @Internal
        @Description("Path of the logs directory")
        Setting<File> internal_with_default = setting("internal.default", PATH, "/tmp");
    }
}