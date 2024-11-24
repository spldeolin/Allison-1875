package com.spldeolin.allison1875.extension.satisficing.persistencegenerator;

import static com.github.javaparser.StaticJavaParser.parseAnnotation;
import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.TableStructureAnalysisDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.JdbcTypeServiceImpl;
import com.spldeolin.satisficing.api.EnumAncestor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-05-24
 */
@Slf4j
@Singleton
public class JdbcTypeServiceImpl2 extends JdbcTypeServiceImpl {

    private final String enumPackage;

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private ImportExprService importExprService;

    public JdbcTypeServiceImpl2(String enumPackage) {
        this.enumPackage = enumPackage;
    }

    @Override
    public JavaTypeNamingDto jdbcType2javaType(InformationSchemaDto columnMeta,
            TableStructureAnalysisDto tableStructureAnalysis) {
        JavaTypeNamingDto javaTypeNamingDto = super.jdbcType2javaType(columnMeta, tableStructureAnalysis);

        String dataType = columnMeta.getDataType();
        if ("date".equals(dataType)) {
            return new JavaTypeNamingDto().setClass(LocalDate.class);
        }
        if ("time".equals(dataType)) {
            return new JavaTypeNamingDto().setClass(LocalTime.class);
        }
        if ("datetime".equals(dataType)) {
            return new JavaTypeNamingDto().setClass(LocalDateTime.class);
        }
        if ("timestamp".equals(dataType)) {
            return new JavaTypeNamingDto().setClass(LocalDateTime.class);
        }

        String description = columnMeta.getColumnComment();

        Pattern enumPattern = Pattern.compile("E\\((.+?)\\)");
        Matcher enumMatcher = enumPattern.matcher(description);
        if (enumMatcher.find() && javaTypeNamingDto.getQualifier().equals(String.class.getName())) {
            String enumName = MoreStringUtils.toUpperCamel(columnMeta.getTableName()) + MoreStringUtils.toUpperCamel(
                    columnMeta.getColumnName()) + "Enum";

            CompilationUnit cu = new CompilationUnit();
            cu.setPackageDeclaration(enumPackage);
            EnumDeclaration ed = new EnumDeclaration();
            JavadocUtils.setJavadoc(ed, concatEntityDescription(tableStructureAnalysis),
                    commonConfig.getAuthor() + " " + LocalDate.now());
            ed.addAnnotation(parseAnnotation("@lombok.Getter"));
            ed.addAnnotation(parseAnnotation("@lombok.AllArgsConstructor"));
            ed.setPublic(true);
            ed.setName(enumName);

            ed.addImplementedType(EnumAncestor.class.getName() + "<String>");
            for (String part : enumMatcher.group(1).split(" ")) {
                String[] split = part.split("=");
                String enumConstantName = split[0].replaceAll("[^a-zA-Z0-9]", "");
                if (StringUtils.isNumeric(enumConstantName.substring(0, 1))) {
                    enumConstantName = "e" + enumConstantName;
                }
                EnumConstantDeclaration ecd = new EnumConstantDeclaration().setName(enumConstantName)
                        .addArgument(new StringLiteralExpr(split[0])).addArgument(new StringLiteralExpr(split[1]));
                ed.addEntry(ecd);
            }
            ed.addMember(StaticJavaParser.parseBodyDeclaration(
                    "@com.fasterxml.jackson.annotation.JsonValue private final String code;"));
            ed.addMember(StaticJavaParser.parseBodyDeclaration("private final String title;"));
            ed.addMember(StaticJavaParser.parseBodyDeclaration(
                            "public static boolean valid(String code) { return Arrays.stream(values())" + ".anyMatch"
                                    + "(anEnum -> anEnum.getCode().equals(code)); }").asMethodDeclaration()
                    .setJavadocComment("判断参数code是否是一个有效的枚举"));
            ed.addMember(StaticJavaParser.parseBodyDeclaration(
                            f("@com.fasterxml.jackson.annotation.JsonCreator public static %s of(String code) { "
                                    + "return Arrays" + ".stream(values()).filter(anEnum -> anEnum.getCode().equals"
                                    + "(code))"
                                    + ".findFirst().orElse(null); }", enumName)).asMethodDeclaration()
                    .setJavadocComment("获取code对应的枚举"));
            ed.addMember(StaticJavaParser.parseBodyDeclaration(
                    "@Override public String toString() { return asJavabean().toString(); }").asMethodDeclaration());
            cu.addType(ed);
            Path enumPath = CodeGenerationUtils.fileInPackageAbsolutePath(AstForestContext.get().getSourceRoot(),
                    enumPackage, enumName + ".java");
            cu.setStorage(enumPath);
            importExprService.extractQualifiedTypeToImport(cu);
            cu.addImport("java.util.Arrays");
            tableStructureAnalysis.getFlushes().add(FileFlush.build(cu));

            return new JavaTypeNamingDto().setSimpleName(ed.getNameAsString())
                    .setQualifier(enumPackage + "." + ed.getNameAsString());
        }

        Pattern typePattern = Pattern.compile("T\\((.+?)\\)");
        Matcher typeMatcher = typePattern.matcher(description);
        if (typeMatcher.find()) {
            String qualifier = typeMatcher.group(1);
            return new JavaTypeNamingDto().setSimpleName(qualifier.substring(qualifier.lastIndexOf('.') + 1))
                    .setQualifier(qualifier);
        }

        return javaTypeNamingDto;
    }

    private String concatEntityDescription(TableStructureAnalysisDto tableStructureAnalysis) {
        String result = "";
        if (commonConfig.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (commonConfig.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION
                    + tableStructureAnalysis.getLotNo();
        }
        return result;
    }

}