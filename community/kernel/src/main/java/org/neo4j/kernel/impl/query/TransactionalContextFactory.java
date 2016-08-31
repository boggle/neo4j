package org.neo4j.kernel.impl.query;

import java.util.Map;

import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.api.security.AccessMode;
import org.neo4j.kernel.impl.query.Neo4jTransactionalContext;

public interface TransactionalContextFactory<C extends TransactionalContext>
{
    C newContext(
        KernelTransaction.Type type,
        AccessMode accessMode,
        String queryText,
        Map<String,Object> queryParameters
    );
}
