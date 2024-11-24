package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * 链环：On子句中的逻辑比较符
 *
 * @author Deolin 2024-11-18
 */
public interface OnChainLink<RETURN, LITERAL, ENTITY_KEY extends EntityKey<?, LITERAL>> {

    /**
     * <strong>=</strong>
     */
    RETURN eq(ENTITY_KEY value);

    /**
     * <strong>!=</strong>
     */
    RETURN ne(ENTITY_KEY value);

    /**
     * <strong>></strong>
     */
    RETURN gt(ENTITY_KEY value);

    /**
     * <strong>>=</strong>
     */
    RETURN ge(ENTITY_KEY value);

    /**
     * <strong><</strong>
     */
    RETURN lt(ENTITY_KEY value);

    /**
     * <strong><=</strong>
     */
    RETURN le(ENTITY_KEY value);

    /**
     * <strong>=</strong>
     */
    RETURN eq(LITERAL value);

    /**
     * <strong>!=</strong>
     */
    RETURN ne(LITERAL value);

    /**
     * <strong>IN</strong>
     */
    RETURN in(Collection<LITERAL> values);

    /**
     * <strong>NOT IN</strong>
     */
    RETURN nin(Collection<LITERAL> values);

    /**
     * <strong>></strong>
     */
    RETURN gt(LITERAL value);

    /**
     * <strong>>=</strong>
     */
    RETURN ge(LITERAL value);

    /**
     * <strong><</strong>
     */
    RETURN lt(LITERAL value);

    /**
     * <strong><=</strong>
     */
    RETURN le(LITERAL value);

    /**
     * <strong>IS NOT NULL</strong>
     */
    RETURN notnull();

    /**
     * <strong>IS NULL</strong>
     */
    RETURN isnull();

    /**
     * <strong>LIKE</strong>
     */
    RETURN like(LITERAL value);

}