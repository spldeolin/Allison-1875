package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.spldeolin.allison1875.common.ancestor.Allison1875Exception;
import com.spldeolin.allison1875.common.ancestor.Allison1875Module;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.interceptor.ValidInterceptor;
import com.spldeolin.allison1875.common.javabean.InvalidDTO;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Allison1875
 *
 * @author Deolin 2020-12-06
 */
@Slf4j
public class Allison1875 {

    public static String SHORT_VERSION;

    public static void hello() {
        try (Reader reader = Resources.asCharSource(Resources.getResource("allison1875-git.properties"),
                StandardCharsets.UTF_8).openStream()) {

            // read allison1875-banner.txt
            String banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);

            // read allison1875-git.properties
            Properties properties = new Properties();
            properties.load(reader);
            banner = banner.replace("${commitId}", properties.getProperty("git.commit.id.abbrev"));

            // replace placeholders
            String version = properties.getProperty("git.build.version");
            banner = banner.replace("${buildVersion}", version);

            // abbreviate version
            String[] components = version.split("-")[0].split("\\.");
            String prefix = String.format("%02d", Integer.parseInt(components[0]));
            String suffix = String.format("%02d", Integer.parseInt(components.length > 1 ? components[1] : "0"));
            SHORT_VERSION = prefix + suffix + (version.endsWith("-SNAPSHOT") ? "S" : "R");

            // print banner
            log.info(banner);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void letsGo(Allison1875Module allison1875Module, AstForest astForest) {
        // valid config
        List<InvalidDTO> invalids = allison1875Module.validConfigs();
        if (CollectionUtils.isNotEmpty(invalids)) {
            throw new Allison1875Exception(
                    "Allison 1875 fail to work cause invalid config\ninvalids=" + JsonUtils.toJsonPrettily(invalids));
        }

        // register interceptor
        List<Module> guiceModules = Lists.newArrayList(allison1875Module, new ValidInterceptor().toGuiceModule());

        // create ioc container
        Injector injector = Guice.createInjector(guiceModules);

        // process main service
        AstForestContext.set(astForest);
        try {
            injector.getInstance(allison1875Module.declareMainService()).process(astForest);
        } catch (Exception e) {
            log.error("main process failed", e);
            throw new Allison1875Exception(e);
        }
    }

}