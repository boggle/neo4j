package org.neo4j.kernel.configuration.docs;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.Description;
import org.neo4j.kernel.configuration.Internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.kernel.configuration.Settings.NO_DEFAULT;
import static org.neo4j.kernel.configuration.Settings.PATH;
import static org.neo4j.kernel.configuration.Settings.setting;

public class SettingsDocumenterTest
{
    @Test
    public void shouldDocumentBasicSettingsClass() throws Throwable
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream( baos );

        // when
        new SettingsDocumenter( out ).document( SimpleSettings.class );

        // then
        // Note, I got the text below from invoking the existing un-tested
        // config documenter implementation, and running this on it:
        //
        // for ( String line : result.split( "\\n" ) )
        // {
        //     System.out.println("\"" + line +"%n\" +");
        // }
        //
        // My intent here is to add tests and refactor the code, it could be
        // that there are errors in the original implementation that I've missed,
        // in which case you should trust your best judgement, and change the assertion
        // below accordingly.
        out.flush();
        String result = baos.toString( "UTF-8" );
        assertThat( result, equalTo( String.format(
            "[[config-org.neo4j.kernel.configuration.docs.SettingsDocumenterTest$SimpleSettings]]%n" +
            ".List of configuration settings%n" +
            "ifndef::nonhtmloutput[]%n" +
            "%n" +
            "[options=\"header\"]%n" +
            "|===%n" +
            "|Name|Description%n" +
            "|<<config_public.default,public.default>>|Public with default.%n" +
            "|<<config_public.nodefault,public.nodefault>>|Public nodefault.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifdef::nonhtmloutput[]%n" +
            "%n" +
            "* <<config_public.default,public.default>>: Public with default.%n" +
            "* <<config_public.nodefault,public.nodefault>>: Public nodefault.%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "%n" +
            "[[config-org.neo4j.kernel.configuration.docs.SettingsDocumenterTest$SimpleSettings-deprecated]]%n" +
            ".Deprecated settings%n" +
            "ifndef::nonhtmloutput[]%n" +
            "%n" +
            "[options=\"header\"]%n" +
            "|===%n" +
            "|Name|Description%n" +
            "|<<config_public.deprecated,public.deprecated>>|Public deprecated.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifdef::nonhtmloutput[]%n" +
            "%n" +
            "* <<config_public.deprecated,public.deprecated>>: Public deprecated.%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "%n" +
            "ifndef::nonhtmloutput[]%n" +
            "[[config_public.default]]%n" +
            ".public.default%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public with default.%n" +
            "|Valid values a|`public.default` is a path.%n" +
            "|Default value m|/tmp%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifdef::nonhtmloutput[]%n" +
            "[[config_public.default]]%n" +
            ".public.default%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public with default.%n" +
            "|Valid values a|`public.default` is a path.%n" +
            "|Default value m|/tmp%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifndef::nonhtmloutput[]%n" +
            "[[config_public.deprecated]]%n" +
            ".public.deprecated%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public deprecated.%n" +
            "|Valid values a|`public.deprecated` is a path.%n" +
            "|Default value m|/tmp%n" +
            "|Deprecated a|The `public.deprecated` configuration setting has been deprecated.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifdef::nonhtmloutput[]%n" +
            "[[config_public.deprecated]]%n" +
            ".public.deprecated%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public deprecated.%n" +
            "|Valid values a|`public.deprecated` is a path.%n" +
            "|Default value m|/tmp%n" +
            "|Deprecated a|The `public.deprecated` configuration setting has been deprecated.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifndef::nonhtmloutput[]%n" +
            "[[config_public.nodefault]]%n" +
            ".public.nodefault%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public nodefault.%n" +
            "|Valid values a|`public.nodefault` is a path.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n" +
            "%n" +
            "ifdef::nonhtmloutput[]%n" +
            "[[config_public.nodefault]]%n" +
            ".public.nodefault%n" +
            "[cols=\"<1h,<4\"]%n" +
            "|===%n" +
            "|Description a|Public nodefault.%n" +
            "|Valid values a|`public.nodefault` is a path.%n" +
            "|===%n" +
            "endif::nonhtmloutput[]%n%n" ) ));
    }

    public interface SimpleSettings
    {
        @Description("Public nodefault")
        Setting<File> public_nodefault = setting("public.nodefault", PATH, NO_DEFAULT);

        @Description("Public with default")
        Setting<File> public_with_default = setting("public.default", PATH, "/tmp");

        @Deprecated
        @Description("Public deprecated")
        Setting<File> public_deprecated = setting("public.deprecated", PATH, "/tmp");

        @Internal
        @Description("Internal with default")
        Setting<File> internal_with_default = setting("internal.default", PATH, "/tmp");
    }
}