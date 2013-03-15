package org.neo4j.helpers;

public interface Converter<T, R> extends Predicate<T>, Function<T, R>
{
}
