package com.spldeolin.allison1875.base.util.ast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.spldeolin.allison1875.base.exception.StorageAbsentException;
import com.spldeolin.allison1875.base.util.FileBackupUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * 将CompilationUnit保存到硬盘
 *
 * @author Deolin 2020-01-26
 */
@Log4j2
public class Saves {

    private static final ThreadLocal<Set<CompilationUnit>> apiSaveBuffer = ThreadLocal.withInitial(Sets::newHashSet);

    private static final ThreadLocal<Map<CompilationUnit, String>> rawReplaceBuffer = ThreadLocal.withInitial(
            Maps::newHashMap);

    private Saves() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 使用这种方式加入的CU对象在SaveAll时，会使用Javaparser的 Code Format
     */
    public static void add(CompilationUnit cu) {
        apiSaveBuffer.get().add(cu);
    }

    @Data
    @AllArgsConstructor
    public static class Replace {

        private String target;

        private String replacement;

    }

    public static void add(CompilationUnit cu, List<Replace> replaces) {
        if (replaces.size() == 0) {
            return;
        }
        String newCodeText;
        if (rawReplaceBuffer.get().containsKey(cu)) {
            newCodeText = rawReplaceBuffer.get().get(cu);
        } else {
            newCodeText = TokenRanges.getRawCode(cu);
        }
        for (Replace replace : replaces) {
            if (replace.getTarget() == null) {
                newCodeText = replace.getReplacement() + newCodeText;
            } else {
                newCodeText = newCodeText.replace(replace.getTarget(), replace.getReplacement());
            }
        }

        rawReplaceBuffer.get().put(cu, newCodeText);
    }

    public static void saveAll() {
        for (CompilationUnit cu : apiSaveBuffer.get()) {
            Storage storage = cu.getStorage().orElseThrow(StorageAbsentException::new);
            File file = storage.getDirectory().resolve(storage.getFileName()).toFile();
            if (file.exists()) {
                FileBackupUtils.backup(file);
            }
            storage.save();
        }
        apiSaveBuffer.get().clear();
        rawReplaceBuffer.get().forEach((cu, newCodeText) -> {
            try {
                Files.write(newCodeText, Locations.getAbsolutePath(cu).toFile(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("FileUtils#writeStringToFile", e);
            }
        });
        rawReplaceBuffer.get().clear();
    }

}
