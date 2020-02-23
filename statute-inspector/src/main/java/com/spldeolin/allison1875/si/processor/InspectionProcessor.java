package com.spldeolin.allison1875.si.processor;

import java.util.Arrays;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.si.statute.StatuteEnum;
import com.spldeolin.allison1875.si.vo.LawlessVo;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Log4j2
@Accessors(fluent = true)
public class InspectionProcessor {

    @Getter
    private Collection<LawlessVo> lawlesses = Lists.newLinkedList();

    public InspectionProcessor process() {
        Collection<CompilationUnit> cus = StaticGitAddedFileContainer
                .removeIfNotContain(StaticAstContainer.getCompilationUnits());
        Arrays.stream(StatuteEnum.values()).forEach(statuteEnum -> {
            Collection<LawlessVo> vos = statuteEnum.getStatute().inspect(cus);
            vos.forEach(vo -> vo.setStatuteNo(statuteEnum.getNo()));
            lawlesses.addAll(vos);
        });
        return this;
    }

}