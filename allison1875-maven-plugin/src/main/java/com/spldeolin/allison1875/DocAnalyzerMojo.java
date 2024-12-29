package com.spldeolin.allison1875;

import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-10
 */
@Mojo(name = "doc-analyzer", requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.COMPILE)
@Slf4j
public class DocAnalyzerMojo extends Allison1875Mojo {

    @Parameter(alias = "docAnalyzer")
    private final DocAnalyzerMojoConfig docAnalyzerConfig = new DocAnalyzerMojoConfig();

    @Override
    public Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader) throws Exception {
        // 对config对象中的文件路径进行相对basedir的处理
        docAnalyzerConfig.setDependencyProjectDirs(
                docAnalyzerConfig.getDependencyProjectDirs().stream().map(super::getCanonicalFileRelativeToBasedir)
                        .collect(Collectors.toList()));
        docAnalyzerConfig.setMarkdownDir(getCanonicalFileRelativeToBasedir(docAnalyzerConfig.getMarkdownDir()));
        log.info("docAnalyzerConfig={}", JsonUtils.toJsonPrettily(docAnalyzerConfig));
        log.info("new module instance for {}", docAnalyzerConfig.getModule());
        return (Allison1875Module) classLoader.loadClass(docAnalyzerConfig.getModule())
                .getConstructor(CommonConfig.class, DocAnalyzerConfig.class)
                .newInstance(commonConfig, docAnalyzerConfig);
    }

}
