package com.spldeolin.allison1875.support;


/**
 * @author Deolin 2023-05-01
 */
@SuppressWarnings("all")
public final class StarSchema {

    private final static UnsupportedOperationException e = new UnsupportedOperationException();

    /**
     * 指定事实表
     *
     * @param centralFactTablePrimaryKey 事实表的主键
     * @param primaryKey 主键
     */
    public static <E, K> Cft<K> cft(PropertyName<E, K> centralFactTablePrimaryKey, K primaryKey) {
        throw e;
    }

    public static class Cft<K> {

        /**
         * 指定维度表（事实-维度关联关系是One to One）
         *
         * @param dimensionTableForeignKey 维度表关联事实表的外键
         */
        public <E> Cft<K> oo(PropertyName<E, K> dimensionTableForeignKey) {
            throw e;
        }

        /**
         * 指定维度表（事实-维度关联关系是One to Many）
         *
         * @param dimensionTableForeignKey 维度表关联事实表的外键
         */
        public <E> Om<E, K> om(PropertyName<E, K> dimensionTableForeignKey) {
            throw e;
        }

        public void over() {
            throw e;
        }

    }

    public static class Om<E, K> extends Cft<K> {

        /**
         * 为One to Many关系的维度表指定其他需要关注的key，映射到Map
         *
         * @param dimensionTableKey 维度表key的
         */
        public Om<E, K> key(PropertyName<E, ?> dimensionTableKey) {
            throw e;
        }

        /**
         * 为One to Many关系的维度表指定其他需要关注的key，映射到Multimap
         *
         * @param dtKeyGetter 维度表key
         */
        public Om<E, K> mkey(PropertyName<E, ?> dimensionTableKey) {
            throw e;
        }

    }

}