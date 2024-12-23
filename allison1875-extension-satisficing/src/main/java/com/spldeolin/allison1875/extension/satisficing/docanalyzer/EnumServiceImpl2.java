package com.spldeolin.allison1875.extension.satisficing.docanalyzer;

import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.EnumServiceImpl;
import com.spldeolin.satisficing.api.EnumAncestor;

/**
 * @author Deolin 2024-02-26
 */
public class EnumServiceImpl2 extends EnumServiceImpl {

    @Override
    protected AnalyzeEnumConstantsRetval analyzeEnumConstant(Object enumConstant) {
        if (enumConstant instanceof EnumAncestor) {
            EnumAncestor<?> enumAncestor = (EnumAncestor<?>) enumConstant;
            AnalyzeEnumConstantsRetval result = new AnalyzeEnumConstantsRetval();
            result.setCode(enumAncestor.getCode().toString());
            result.setTitle(enumAncestor.getTitle());
            return result;
        }
        return super.analyzeEnumConstant(enumConstant);
    }

}