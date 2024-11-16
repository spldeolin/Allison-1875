package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * by()调用链的谓语
 *
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2020-01-11
 */
public interface ByChainPredicate<RT, ARG> {

    /**
     * EQual
     */
    RT eq(ARG value);

    /**
     * Not Equal
     */
    RT ne(ARG value);

    /**
     * IN
     */
    RT in(Collection<ARG> values);

    /**
     * Not IN
     */
    RT nin(Collection<ARG> values);

    /**
     * Greater Than
     */
    RT gt(ARG value);

    /**
     * Greater or Equals
     */
    RT ge(ARG value);

    /**
     * Lesser Than
     */
    RT lt(ARG value);

    /**
     * Lesser or Equal
     */
    RT le(ARG value);

    /**
     * NOT NULL
     */
    RT notnull();

    /**
     * IS NULL
     */
    RT isnull();

    /**
     * LIKE
     */
    RT like(ARG value);

}