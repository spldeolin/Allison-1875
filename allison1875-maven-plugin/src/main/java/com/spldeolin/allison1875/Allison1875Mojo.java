package com.spldeolin.allison1875;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import com.google.common.base.MoreObjects;
import com.spldeolin.allison1875.ast.MavenProjectBuiltAstForest;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.guice.Allison1875Module;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-06-13
 */
@Slf4j
public abstract class Allison1875Mojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(alias = "common")
    private CommonConfig commonConfig = new CommonConfig();

    @Override
    public void execute() throws MojoExecutionException {
        Allison1875.hello();
        initParam();

        ClassLoader classLoader;
        Allison1875Module allison1875Module;
        try {
            classLoader = getClassLoader(project);
            allison1875Module = newAllison1875Module(commonConfig, classLoader);
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }

        List<File> sourceRoots = project.getCompileSourceRoots().stream().map(File::new).collect(Collectors.toList());
        log.info("sourceRoots={}", sourceRoots);
        try {
            for (File sourceRoot : sourceRoots) {
                AstForest astForest = new MavenProjectBuiltAstForest(classLoader, sourceRoot);
                Allison1875.letsGo(allison1875Module, astForest);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }

    private void initParam() {
        log.info("project={}", project);
        log.info("basedir={}", project.getBasedir());
        commonConfig = MoreObjects.firstNonNull(commonConfig, new CommonConfig());
        String basePackage = commonConfig.getBasePackage();
        commonConfig.setBasePackage(MoreObjects.firstNonNull(basePackage, project.getGroupId()));
        commonConfig.setReqDTOPackage(
                MoreObjects.firstNonNull(commonConfig.getReqDTOPackage(), basePackage + ".dto.req"));
        commonConfig.setRespDTOPackage(
                MoreObjects.firstNonNull(commonConfig.getRespDTOPackage(), basePackage + ".dto.resp"));
        commonConfig.setServicePackage(
                MoreObjects.firstNonNull(commonConfig.getServicePackage(), basePackage + ".service"));
        commonConfig.setServiceImplPackage(
                MoreObjects.firstNonNull(commonConfig.getServiceImplPackage(), basePackage + ".service.impl"));
        commonConfig.setMapperPackage(
                MoreObjects.firstNonNull(commonConfig.getMapperPackage(), basePackage + ".mapper"));
        commonConfig.setEntityPackage(
                MoreObjects.firstNonNull(commonConfig.getEntityPackage(), basePackage + ".entity"));
        commonConfig.setDesignPackage(
                MoreObjects.firstNonNull(commonConfig.getDesignPackage(), basePackage + ".design"));
        commonConfig.setParamDTOPackage(
                MoreObjects.firstNonNull(commonConfig.getParamDTOPackage(), basePackage + ".dto.param"));
        commonConfig.setRecordDTOPackage(
                MoreObjects.firstNonNull(commonConfig.getRecordDTOPackage(), basePackage + ".dto.record"));
        commonConfig.setWholeDTOPackage(
                MoreObjects.firstNonNull(commonConfig.getWholeDTOPackage(), basePackage + ".dto"));
        commonConfig.setMapperXmlDirs(
                commonConfig.getMapperXmlDirs().stream().map(this::getCanonicalFileRelativeToBasedir)
                        .collect(Collectors.toList()));
        log.info("commonConfig={}", JsonUtils.toJsonPrettily(commonConfig));
    }

    public abstract Allison1875Module newAllison1875Module(CommonConfig commonConfig, ClassLoader classLoader)
            throws Exception;

    private ClassLoader getClassLoader(MavenProject project) throws Exception {
        List<String> classpathElements = project.getCompileClasspathElements();
        log.debug("classpathElements={}", JsonUtils.toJson(classpathElements));
        classpathElements.add(project.getBuild().getOutputDirectory());
        classpathElements.add(project.getBuild().getTestOutputDirectory());
        URL[] urls = new URL[classpathElements.size()];
        for (int i = 0; i < classpathElements.size(); ++i) {
            urls[i] = new File(classpathElements.get(i)).toURL();
        }
        return new URLClassLoader(urls, this.getClass().getClassLoader());
    }

    protected File getCanonicalFileRelativeToBasedir(File file) {
        try {
            return project.getBasedir().toPath().resolve(file.toPath()).toFile().getCanonicalFile();
        } catch (IOException e) {
            log.warn("fail to getCanonicalFile, basedir={} file={}", project.getBasedir(), file, e);
            return file;
        }
    }

}
