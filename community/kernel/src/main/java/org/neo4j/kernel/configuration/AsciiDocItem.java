/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.configuration;

import java.util.Objects;

/**
 * Represents an item to be included into te generated Asciidoc.
 * Contains {@linkplain #id() id}, {@linkplain #name() name} and {@linkplain #description() description}.
 */
public final class AsciiDocItem
{
    private final String name;
    private final String description;
    private final boolean internal;
    private final boolean deprecated;
    private boolean mandatory;

    public AsciiDocItem( String name, String description, boolean internal, boolean deprecated )
    {
        this.internal = internal;
        this.deprecated = deprecated;
        this.name = name;
        this.description = description;
    }

    public String id()
    {
        return "config_" + name;
    }

    public String name()
    {
        return name;
    }

    public String description()
    {
        return description;
    }

    public boolean isInternal()
    {
        return internal;
    }

    public boolean isDeprecated()
    {
        return deprecated;
    }

    public boolean hasDefault()
    {
        //if ( !defaultValue.equals( DEFAULT_MARKER ) )
        return false;
    }

    public String defaultValue()
    {
        return "";
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public String mandatoryDescription()
    {
        // Note MANDATORY
        return null;
    }

    public String deprecationMessage()
    {
        // Note OBSOLETED & DEPRECATED
        return null;
    }

    public String validationMessage()
    {
        // Note VALIDATION_MESSAGE
        return null;
    }

    public boolean hasValidation()
    {
        return false;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        AsciiDocItem that = (AsciiDocItem) o;
        return Objects.equals( name, that.name ) &&
               Objects.equals( description, that.description );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( name, description );
    }

    @Override
    public String toString()
    {
        return "AsciiDocItem{" + "id='" + id() + "\', name='" + name + "\', description='" + description + "\'}";
    }
}
