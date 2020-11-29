package com.spldeolin.allison1875.persistencegenerator;

import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-07-11
 */
@Accessors(chain = true)
@Data
public class PersistenceGeneratorConfig {

    /**
     * 数据库连接
     */
    @NotEmpty
    protected String jdbcUrl;

    /**
     * 数据库用户名
     */
    @NotEmpty
    protected String userName;

    /**
     * 数据库密码
     */
    @NotEmpty
    protected String password;

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    protected String author;

    /**
     * 指定schema
     */
    @NotEmpty
    protected String schema;

    /**
     * 指定table，非必填，未填写时代表schema下所有的table
     */
    @NotNull
    protected Collection<String> tables = Lists.newArrayList();

    /**
     * mapper.xml所在目录的相对路径（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String mapperXmlDirectoryPath;

    /**
     * mapper接口的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String mapperPackage;

    /**
     * Entity类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String entityPackage;

    /**
     * 是否为[query-transformer]生成Query类
     */
    @NotNull
    protected Boolean enableGenerateQueryDesign;

    /**
     * QueryDesign类的包名（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String queryDesignPackage;

    /**
     * QueryPredicate类的全限定名（根据目标工程的情况填写）
     */
    @NotEmpty
    protected String queryPredicateQualifier;

    /**
     * mapper.xml的标签中，是否使用别名来引用Entity类
     */
    @NotNull
    protected Boolean isEntityUsingAlias;

    /**
     * # 生成出的Entity类是否以Entity作为类名的结尾
     */
    @NotNull
    protected Boolean isEntityEndWithEntity;

    /**
     * 如果有逻辑删除，怎么样算作“数据被删”，非必填，只支持等式SQL
     */
    protected String deletedSql;

    /**
     * 如果有逻辑删除，怎么样算作“数据未被删”，非必填，只支持等式SQL
     */
    protected String notDeletedSql;

    /**
     * 对项目隐藏，仅在数据库中可见的表字段
     */
    @NotNull
    protected Collection<String> hiddenColumns = Lists.newArrayList();

    /**
     * 即便符合persistence-generator对外键的定义，也不会被当作外键的表字段（一般用于忽略为创建人ID和更新人ID生成query方法）
     */
    @NotNull
    protected Collection<String> notKeyColumns = Lists.newArrayList();

    /**
     * Entity父类的全限定名
     */
    protected String superEntityQualifier;

    /**
     * 已在Entit父类中声明，无需在具体Entity中再次声明的表字段
     */
    @NotNull
    protected Collection<String> alreadyInSuperEntity = Lists.newArrayList();

    @NotNull
    protected Boolean disableInsert = false;

    @NotNull
    protected Boolean disableBatchInsertEvenNull = false;

    @NotNull
    protected Boolean disableQueryById = false;

    @NotNull
    protected Boolean disableUpdateById = false;

    @NotNull
    protected Boolean disableUpdateByIdEvenNull = false;

    @NotNull
    protected Boolean disableQueryByIds = false;

    @NotNull
    protected Boolean disableQueryByIdsEachId = false;

    @NotNull
    protected Boolean disableQueryByKey = false;

    @NotNull
    protected Boolean disableDeleteByKey = false;

    @NotNull
    protected Boolean disableQueryByKeys = false;

    @NotNull
    protected Boolean disableQueryByEntity = false;

}