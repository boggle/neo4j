package org.neo4j.kernel.configuration.docs;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.kernel.configuration.AsciiDocItem;
import org.neo4j.kernel.configuration.AsciiDocListGenerator;

import static java.util.stream.Collectors.toList;
import static org.neo4j.kernel.configuration.docs.SettingsDescription.describe;

public class SettingsDocumenter
{
    private static final String DEFAULT_MARKER = "__DEFAULT__";
    private static final Pattern CONFIG_SETTING_PATTERN = Pattern.compile( "[a-z0-9]+((\\.|_)[a-z0-9]+)+" );
    private static final Pattern NUMBER_OR_IP = Pattern.compile( "[0-9\\.]+" );
    private static final List<String> CONFIG_NAMES_BLACKLIST = Arrays.asList( "round_robin", "keep_all", "keep_last",
            "keep_none", "metrics.neo4j", "i.e", "e.g" );
    static final String IFDEF_HTMLOUTPUT = "ifndef::nonhtmloutput[]\n";
    static final String IFDEF_NONHTMLOUTPUT = "ifdef::nonhtmloutput[]\n";
    static final String ENDIF = "endif::nonhtmloutput[]\n\n";

    public void document( Class<?> settings, PrintStream out )
    {
        String settingsResourceId = "config-" + settings.getName();
        String bundleDescription = "List of configuration settings";

        List<AsciiDocItem> regularSettings =
                describe( settings ).settings()
                .filter( ( setting ) -> !setting.isInternal() && !setting.isDeprecated() )
                .collect( toList() );
        out.println(new AsciiDocListGenerator( settingsResourceId, bundleDescription, true )
                .generateListAndTableCombo( regularSettings ));

        List<AsciiDocItem> deprecated =
                describe( settings ).settings()
                .filter( ( setting ) -> !setting.isInternal() && setting.isDeprecated() )
                .collect( toList() );
        if( deprecated.size() > 0 )
        {
            out.println( new AsciiDocListGenerator( settingsResourceId + "-deprecated", "Deprecated settings", true )
                    .generateListAndTableCombo( deprecated ) );
        }

        describe( settings ).settings().forEach( (setting) -> {
            out.print( addDocsForOneSetting( setting, false ) );
            out.print( addDocsForOneSetting( setting, true ) );
        });
    }

    private String addDocsForOneSetting( AsciiDocItem item, boolean pdfOutput )
    {
        StringBuilder table = new StringBuilder( 1024 );
        if ( pdfOutput )
        {
            table.append( IFDEF_NONHTMLOUTPUT );
        }
        else
        {
            table.append( IFDEF_HTMLOUTPUT );
        }
        String monospacedName = "`" + item.name() + "`";
        table.append( "[[" )
                .append( item.id() )
                .append( "]]\n" )
                .append( '.' )
                .append( item.name() )
                .append( '\n' )
                .append( "[cols=\"<1h,<4\"]\n" )
                .append( "|===\n" );

        table.append( "|Description a|" );
        addWithDotAtEndAsNeeded( table, item.description() );

        if ( item.hasValidation() )
        {
            String validation = item.validationMessage();
            table.append( "|Valid values a|" );
            addWithDotAtEndAsNeeded( table, replaceKeysWithLinks( validation, item.name(), pdfOutput ) );
        }

        if ( item.hasDefault() )
        {
            table.append( "|Default value m|" )
                    .append( item.defaultValue() )
                    .append( '\n' );
        }

        if ( item.isMandatory() )
        {
            table.append( "|Mandatory a|" );
            addWithDotAtEndAsNeeded( table, item.mandatoryDescription().replace( item.name(), monospacedName ) );
        }

        if ( item.isDeprecated() )
        {
            table.append( "|Deprecated a|" );
            addWithDotAtEndAsNeeded( table,
                    replaceKeysWithLinks( item.deprecationMessage(), item.name(), pdfOutput ) );
        }

        table.append( "|===\n" )
                .append( ENDIF );
        return table.toString();
    }

    private String replaceKeysWithLinks( String text, String nameToNotLink, boolean pdfOutput )
    {
        Matcher matcher = CONFIG_SETTING_PATTERN.matcher( text );
        StringBuffer result = new StringBuffer( 256 );
        while ( matcher.find() )
        {
            String match = matcher.group();
            if ( match.endsWith( ".log" ) )
            {
                // a filenamne
                match = "_" + match + "_";
            }
            else if ( match.equals( nameToNotLink ) )
            {
                // don't link to the settings we're describing
                match = "`" + match + "`";
            }
            else if ( CONFIG_NAMES_BLACKLIST.contains( match ) )
            {
                // an option value; do nothing
            }
            else if ( NUMBER_OR_IP.matcher( match ).matches() )
            {
                // number or ip; do nothing
            }
            else
            {
                // replace setting name with link to setting, if not pdf output
                if ( pdfOutput )
                {
                    match = "`" + match + "`";
                }
                else
                {
                    match = makeConfigXref( match );
                }
            }
            matcher.appendReplacement( result, match );
        }
        matcher.appendTail( result );
        return result.toString();
    }

    private void addWithDotAtEndAsNeeded( StringBuilder sb, String message )
    {
        sb.append( message );
        if ( !message.endsWith( "." ) && !message.endsWith( ". " ) )
        {
            sb.append( '.' );
        }
        sb.append( '\n' );
    }

    private String makeConfigXref( String settingName )
    {
        return "+<<config_" + settingName + "," + settingName + ">>+";
    }
}
