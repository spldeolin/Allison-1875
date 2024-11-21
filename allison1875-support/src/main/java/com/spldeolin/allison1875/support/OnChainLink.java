package com.spldeolin.allison1875.support;

import java.util.Collection;

/**
 * 链环：On子句中的逻辑比较符
 *
 * @author Deolin 2024-11-18
 */
public interface OnChainLink<RETURN_TYPE, LITERAL_TYPE, ENTITY_KEY_TYPE extends EntityKey<?, LITERAL_TYPE>> {

    /**
     * EQual
     */
    RETURN_TYPE eq(ENTITY_KEY_TYPE value);

    RETURN_TYPE eq(LITERAL_TYPE value);

    /**
     * Not Equal
     */
    RETURN_TYPE ne(ENTITY_KEY_TYPE value);

    RETURN_TYPE ne(LITERAL_TYPE value);

    /**
     * IN
     */
    RETURN_TYPE in(Collection<ENTITY_KEY_TYPE> values);

    RETURN_TYPE in(Iterable<LITERAL_TYPE> values); // It should be enough

    /**
     * Not IN
     */
    RETURN_TYPE nin(Collection<ENTITY_KEY_TYPE> values);

    RETURN_TYPE nin(Iterable<LITERAL_TYPE> values);

    /**
     * Greater Than
     */
    RETURN_TYPE gt(ENTITY_KEY_TYPE value);

    RETURN_TYPE gt(LITERAL_TYPE value);

    /**
     * Greater or Equals
     */
    RETURN_TYPE ge(ENTITY_KEY_TYPE value);

    RETURN_TYPE ge(LITERAL_TYPE value);

    /**
     * Lesser Than
     */
    RETURN_TYPE lt(ENTITY_KEY_TYPE value);

    RETURN_TYPE lt(LITERAL_TYPE value);

    /**
     * Lesser or Equal
     */
    RETURN_TYPE le(ENTITY_KEY_TYPE value);

    RETURN_TYPE le(LITERAL_TYPE value);

    /**
     * NOT NULL
     */
    RETURN_TYPE notnull();

    /**
     * IS NULL
     */
    RETURN_TYPE isnull();

    /**
     * LIKE
     */
    RETURN_TYPE like(ENTITY_KEY_TYPE value);

    RETURN_TYPE like(LITERAL_TYPE value);

}