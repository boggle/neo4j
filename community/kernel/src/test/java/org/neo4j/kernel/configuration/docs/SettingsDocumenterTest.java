package org.neo4j.kernel.configuration.docs;

import org.junit.Test;

import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.Description;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.configuration.ConfigAsciiDocGenerator;

import static org.neo4j.kernel.configuration.Settings.BOOLEAN;
import static org.neo4j.kernel.configuration.Settings.HOSTNAME_PORT;
import static org.neo4j.kernel.configuration.Settings.setting;

public class SettingsDocumenterTest
{
    @Test
    public void shouldDocumentGroup() throws Throwable
    {
        // given


        // when

        // then
    }

    @Test
    public void asd() throws Throwable
    {
        // given
        System.out.println(new ConfigAsciiDocGenerator().generateDocsFor( Settings.class.getName() ));

        // when

        // then
    }

    public interface Settings
    {
        @Description( "Enable connector for blah blah" )
        Setting<Boolean> enabled = setting( "dbms.connector.{key}.enabled", BOOLEAN, "false" );

        @Description( "Set the address for connector blah blah" )
        Setting<HostnamePort> port = setting( "dbms.connector.{key}.address", HOSTNAME_PORT, "localhost:7687" );
    }
}