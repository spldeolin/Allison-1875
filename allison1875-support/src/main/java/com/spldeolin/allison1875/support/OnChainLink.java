package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * 链环：On子句中的逻辑比较符
 *
 * @author Deolin 2024-11-18
 */
public interface OnChainLink<RETURN_TYPE, LITERAL_TYPE, ENTITY_KEY_TYPE extends EntityKey<?, LITERAL_TYPE>> {

    /**
     * <strong>=</strong>
     */
    RETURN_TYPE eq(ENTITY_KEY_TYPE value);

    /**
     * <strong>=</strong>
     */
    RETURN_TYPE eq(LITERAL_TYPE value);

    /**
     * <strong>!=</strong>
     */
    RETURN_TYPE ne(ENTITY_KEY_TYPE value);

    /**
     * <strong>!=</strong>
     */
    RETURN_TYPE ne(LITERAL_TYPE value);

    /**
     * <strong>IN</strong>
     */
    RETURN_TYPE in(Collection<ENTITY_KEY_TYPE> values);

    /**
     * <strong>IN</strong>
     */
    RETURN_TYPE in(Iterable<LITERAL_TYPE> values); // It should be enough

    /**
     * <strong>NOT IN</strong>
     */
    RETURN_TYPE nin(Collection<ENTITY_KEY_TYPE> values);

    /**
     * <strong>NOT IN</strong>
     */
    RETURN_TYPE nin(Iterable<LITERAL_TYPE> values);

    /**
     * <strong>></strong>
     */
    RETURN_TYPE gt(ENTITY_KEY_TYPE value);

    /**
     * <strong>></strong>
     */
    RETURN_TYPE gt(LITERAL_TYPE value);

    /**
     * <strong>>=</strong>
     */
    RETURN_TYPE ge(ENTITY_KEY_TYPE value);

    /**
     * <strong>>=</strong>
     */
    RETURN_TYPE ge(LITERAL_TYPE value);

    /**
     * <strong><</strong>
     */
    RETURN_TYPE lt(ENTITY_KEY_TYPE value);

    /**
     * <strong><</strong>
     */
    RETURN_TYPE lt(LITERAL_TYPE value);

    /**
     * <strong><=</strong>
     */
    RETURN_TYPE le(ENTITY_KEY_TYPE value);

    /**
     * <strong><=</strong>
     */
    RETURN_TYPE le(LITERAL_TYPE value);

    /**
     * <strong>IS NOT NULL</strong>
     */
    RETURN_TYPE notnull();

    /**
     * <strong>IS NULL</strong>
     */
    RETURN_TYPE isnull();

    /**
     * <strong>LIKE</strong>
     */
    RETURN_TYPE like(ENTITY_KEY_TYPE value);

    /**
     * <strong>LIKE</strong>
     */
    RETURN_TYPE like(LITERAL_TYPE value);

}