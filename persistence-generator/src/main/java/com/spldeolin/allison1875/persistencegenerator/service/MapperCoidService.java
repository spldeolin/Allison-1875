package com.spldeolin.allison1875.persistencegenerator.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.persistencegenerator.dto.DetectOrGenerateMapperRetval;
import com.spldeolin.allison1875.persistencegenerator.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.persistencegenerator.dto.QueryByKeysDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.TableStructureAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.MapperCoidServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(MapperCoidServiceImpl.class)
public interface MapperCoidService {

    DetectOrGenerateMapperRetval detectOrGenerateMapper(TableStructureAnalysisDTO persistence,
            DataModelGeneration dataModelGeneration);

    String generateInsertMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchInsertMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchInsertEvenNullMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchUpdateMethodToMapper(GenerateMethodToMapperArgs args);

    String generateBatchUpdateEvenNullMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateUpdateByIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateUpdateByIdEvenNullMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdsMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByIdsEachIdMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByKeyMethodToMapper(GenerateMethodToMapperArgs generateMethodToMapper);

    String generateDeleteByKeyMethodToMapper(GenerateMethodToMapperArgs args);

    QueryByKeysDTO generateQueryByKeysMethodToMapper(GenerateMethodToMapperArgs args);

    String generateQueryByEntityMethodToMapper(GenerateMethodToMapperArgs args);

    String generateListAllMethodToMapper(GenerateMethodToMapperArgs args);

    String generateInsertOrUpdateMethodToMapper(GenerateMethodToMapperArgs args);

}