package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.javabean.KeyMethodNameDTO;
import com.spldeolin.allison1875.persistencegenerator.javabean.QueryByKeysDTO;
import com.spldeolin.allison1875.persistencegenerator.javabean.ReplaceMapperXmlMethodsArgs;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperXmlServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperXmlServiceImpl.class)
public interface MapperXmlService {

    List<String> generateAllCloumnSql(TableStructureAnalysisDTO persistence);

    List<String> generateBatchInsertEvenNullMethod(TableStructureAnalysisDTO persistence, String methodName);

    List<String> generateBatchInsertMethod(TableStructureAnalysisDTO persistence, String methodName);

    List<String> generateBatchUpdateEvenNullMethod(TableStructureAnalysisDTO persistence, String methodName);

    List<String> generateBatchUpdateMethod(TableStructureAnalysisDTO persistence, String methodName);

    /**
     * 根据外键删除
     * 表中每有几个外键，这个方法就生成几个method，以_id结尾的字段算作外键
     */
    List<String> generateDeleteByKeyMethod(TableStructureAnalysisDTO persistence,
            List<KeyMethodNameDTO> KeyAndMethodNames);

    List<String> generateInsertOrUpdateMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName);

    List<String> generateInsertMethod(TableStructureAnalysisDTO persistence, String entityName, String methodName);

    List<String> generateListAllMethod(TableStructureAnalysisDTO persistence, String methodName);

    List<String> generateQueryByEntityMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName);

    /**
     * 这个Proc生成2种方法：
     * 1. 根据主键列表查询
     * 2. 根据主键列表查询，并把结果集以主键为key，映射到Map中
     */
    List<String> generateQueryByIdsMethod(TableStructureAnalysisDTO persistence, String methodName);

    List<String> generateQueryByIdMethod(TableStructureAnalysisDTO persistence, String methodName);

    /**
     * 根据外键列表查询，表中每有几个外键，这个Proc就生成几个方法
     */
    List<String> generateQueryByKeysMethod(TableStructureAnalysisDTO persistence, List<QueryByKeysDTO> queryByKeysDTOs);

    List<String> generateQueryByKeyMethod(TableStructureAnalysisDTO persistence,
            List<KeyMethodNameDTO> keyAndMethodNames);

    List<String> generateResultMap(TableStructureAnalysisDTO persistence, String entityName);

    List<String> generateUpdateByIdEvenNullMethod(TableStructureAnalysisDTO persistence, String entityName,
            String methodName);

    List<String> generateUpdateByIdMethod(TableStructureAnalysisDTO persistence, String entityName, String methodName);

    FileFlush replaceMapperXmlMethods(ReplaceMapperXmlMethodsArgs args);

}
