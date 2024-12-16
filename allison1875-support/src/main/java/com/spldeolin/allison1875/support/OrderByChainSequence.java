package com.spldeolin.allison1875.support;

/**
 * 0RDER BY子句中的排序顺序
 *
 * 适配Allison 1875 query-transformer
 *
 * @author Deolin 2021-05-12
 */
public interface OrderByChainSequence<E> {

    /**
     * <strong>ASC</strong>
     */
    E asc();

    /**
     * <strong>DESC</strong>
     */
    E desc();

}