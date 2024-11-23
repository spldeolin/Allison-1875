package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * 链环：where子句中的逻辑比较符
 *
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2020-01-11
 */
public interface ByChainPredicate<RT, ARG> {

    /**
     * <strong>=</strong>
     */
    RT eq(ARG value);

    /**
     * <strong>!=</strong>
     */
    RT ne(ARG value);

    /**
     * <strong>IN</strong>
     */
    RT in(Collection<ARG> values);

    /**
     * <strong>NOT IN</strong>
     */
    RT nin(Collection<ARG> values);

    /**
     * <strong>></strong>
     */
    RT gt(ARG value);

    /**
     * <strong>>=</strong>
     */
    RT ge(ARG value);

    /**
     * <strong><</strong>
     */
    RT lt(ARG value);

    /**
     * <strong><=</strong>
     */
    RT le(ARG value);

    /**
     * <strong>IS NOT NULL</strong>
     */
    RT notnull();

    /**
     * <strong>IS NULL</strong>
     */
    RT isnull();

    /**
     * <strong>LIKE</strong>
     */
    RT like(ARG value);

}