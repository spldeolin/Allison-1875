package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2023-05-05
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommonConfig extends Allison1875Config {

    /**
     * 基础包名
     */
    @NotEmpty
    String basePackage;

    /**
     * 控制层@RequestBody类型所在包的包名
     */
    @NotEmpty
    String reqDTOPackage;

    /**
     * 控制层@ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    String respDTOPackage;

    /**
     * 业务层Service接口所在包的包名
     */
    @NotEmpty
    String servicePackage;

    /**
     * 业务层ServiceImpl类所在包的包名
     */
    @NotEmpty
    String serviceImplPackage;

    /**
     * 持久层mapper接口所在包的包名
     */
    @NotEmpty
    String mapperPackage;

    /**
     * 持久层Entity类所在包的包名
     */
    @NotEmpty
    String entityPackage;

    /**
     * Design类所在包的包名
     */
    @NotEmpty
    String designPackage;

    /**
     * 持久层Mapper方法签名中Param类所在包的包名
     */
    @NotEmpty
    String paramDTOPackage;

    /**
     * 持久层Mapper方法签名中Record类所在包的包名
     */
    @NotEmpty
    String recordDTOPackage;

    /**
     * WholeDTO类所在包的包名
     */
    @NotEmpty
    String wholeDTOPackage;

    /**
     * mapper.xml所在目录（相对于basedir的相对路径 或 绝对路径 皆可）
     */
    @NotEmpty
    List<File> mapperXmlDirs = Lists.newArrayList(new File("src/main/resources/mapper"));

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    String author = "Allison 1875";

    /**
     * 生成的DataModel是否实现java.io.Serializable接口
     */
    @NotNull
    Boolean isDataModelSerializable = false;

    /**
     * 生成的DataModel是否实现java.lang.Cloneable接口
     */
    @NotNull
    Boolean isDataModelCloneable = false;

    /**
     * 是否在该生成的地方生成 Any modifications may be overwritten by future code generations. 声明
     */
    @NotNull
    Boolean enableNoModifyAnnounce = true;

    /**
     * 是否在该生成的地方生成诸如 Allison 1875 Lot No: DA1000S-967D9357 的声明
     */
    @NotNull
    Boolean enableLotNoAnnounce = true;

}