package com.spldeolin.allison1875.startransformer.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.startransformer.dto.PhraseDTO;
import com.spldeolin.allison1875.startransformer.service.WholeDTOService;

/**
 * @author Deolin 2023-05-22
 */
@Singleton
public class WholeDTOServiceImpl implements WholeDTOService {

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private StarTransformerConfig config;

    @Inject
    private DataModelService dataModelGeneratorService;

    @Override
    public DataModelGeneration generateWholeDTO(ChainAnalysisDTO analysis) {
        DataModelArg dataModelArg = new DataModelArg();
        dataModelArg.setAstForest(AstForestContext.get());
        dataModelArg.setPackageName(commonConfig.getWholeDTOPackage());
        dataModelArg.setClassName(analysis.getWholeDTOName());
        if (commonConfig.getEnableLotNoAnnounce()) {
            dataModelArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + analysis.getLotNo());
        }
        dataModelArg.setAuthor(commonConfig.getAuthor());
        dataModelArg.setIsDataModelSerializable(commonConfig.getIsDataModelSerializable());
        dataModelArg.setIsDataModelCloneable(commonConfig.getIsDataModelCloneable());
        FieldArg cftFieldArg = new FieldArg();
        cftFieldArg.setTypeQualifier(analysis.getCftEntityQualifier());
        cftFieldArg.setFieldName(this.entityNameToVarName(analysis.getCftEntityName()));
        dataModelArg.getFieldArgs().add(cftFieldArg);
        for (PhraseDTO phrase : analysis.getPhrases()) {
            FieldArg dtFieldArg = new FieldArg();
            if (phrase.getIsOneToOne()) {
                dtFieldArg.setTypeQualifier(phrase.getDtEntityQualifier());
                dtFieldArg.setFieldName(this.entityNameToVarName(phrase.getDtEntityName()));
            } else {
                dtFieldArg.setTypeQualifier("java.util.List<" + phrase.getDtEntityQualifier() + ">");
                dtFieldArg.setFieldName(English.plural(this.entityNameToVarName(phrase.getDtEntityName())));
            }
            dataModelArg.getFieldArgs().add(dtFieldArg);
            if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(phrase.getMkeys())) {
                for (String key : phrase.getKeys()) {
                    FieldArg keyFieldArg = new FieldArg();
                    keyFieldArg.setTypeQualifier(
                            "java.util.Map<" + phrase.getEntityFieldTypesEachFieldName().get(key) + ","
                            + phrase.getDtEntityName() + ">");
                    keyFieldArg.setFieldName(English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                            + StringUtils.capitalize(key));
                    dataModelArg.getFieldArgs().add(keyFieldArg);
                }
                for (String mkey : phrase.getMkeys()) {
                    FieldArg mkeyFieldArg = new FieldArg();
                    mkeyFieldArg.setTypeQualifier(
                            "com.google.common.collect.Multimap<" + phrase.getEntityFieldTypesEachFieldName().get(mkey)
                                    + "," + phrase.getDtEntityName() + ">");
                    mkeyFieldArg.setFieldName(
                            English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                    + StringUtils.capitalize(mkey));
                    dataModelArg.getFieldArgs().add(mkeyFieldArg);
                }
            }
        }
        dataModelArg.setDataModelExistenceResolution(FileExistenceResolutionEnum.RENAME);
        return dataModelGeneratorService.generateDataModel(dataModelArg);
    }

    private String entityNameToVarName(String entityName) {
        return MoreStringUtils.toLowerCamel(StringUtils.removeEnd(entityName, "Entity"));
    }

}