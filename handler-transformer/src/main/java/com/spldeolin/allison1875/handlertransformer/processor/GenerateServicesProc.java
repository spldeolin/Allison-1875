package com.spldeolin.allison1875.handlertransformer.processor;

import java.time.LocalDate;
import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.javadoc.Javadoc;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;

/**
 * @author Deolin 2020-08-26
 */
class GenerateServicesProc {

    private final MetaInfo metaInfo;

    private CompilationUnit serviceCu;

    private CompilationUnit serviceImplCu;

    private String serviceQualifier;

    GenerateServicesProc(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    GenerateServicesProc process() {
        HandlerTransformerConfig config = HandlerTransformer.CONFIG.get();
        String serviceName = StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "Service";
        MethodDeclaration absMethod = new MethodDeclaration();
        if (metaInfo.isRespAbsent()) {
            absMethod.setType("void");
        } else {
            absMethod.setType(metaInfo.getRespBody().getTypeName());
        }
        absMethod.setName(metaInfo.getHandlerName());
        if (!metaInfo.isReqAbsent()) {
            absMethod.addParameter(StaticJavaParser.parseType(metaInfo.getReqBody().getTypeName()), "req");
        }
        MethodDeclaration method = absMethod.clone();

        List<String> imports = Lists.newArrayList();
        if (!metaInfo.isReqAbsent()) {
            imports.add(metaInfo.getReqBody().getTypeQualifier());
        }
        if (!metaInfo.isRespAbsent()) {
            imports.add(metaInfo.getRespBody().getTypeQualifier());
        }
        CuCreator serviceCreator = new CuCreator(metaInfo.getSourceRoot(), config.getServicePackage(), imports, () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            Javadoc javadoc = new JavadocComment("").parse()
                    .addBlockTag("author", config.getAuthor() + " " + LocalDate.now());
            coid.setJavadocComment(javadoc);
            coid.setPublic(true).setInterface(true).setName(serviceName);
            MethodDeclaration decl = absMethod.setBody(null);
            coid.addMember(decl);
            return coid;
        });
        serviceCu = serviceCreator.create(false);
        serviceQualifier = serviceCreator.getPrimaryTypeQualifier();

        List<String> imports4Impl = Lists.newArrayList(imports);
        imports4Impl.add(serviceCreator.getPrimaryTypeQualifier());
        imports4Impl.add("org.springframework.stereotype.Service");
        imports4Impl.add("lombok.extern.slf4j.Slf4j");
        CuCreator serviceImplCreator = new CuCreator(metaInfo.getSourceRoot(), config.getServiceImplPackage(),
                imports4Impl, () -> {
            ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
            Javadoc javadoc = new JavadocComment("").parse()
                    .addBlockTag("author", config.getAuthor() + " " + LocalDate.now());
            coid.setJavadocComment(javadoc);
            coid.addAnnotation(StaticJavaParser.parseAnnotation("@Service"));
            coid.addAnnotation(StaticJavaParser.parseAnnotation("@Slf4j"));
            coid.setPublic(true).setInterface(false).setName(serviceName + "Impl");
            coid.addImplementedType(serviceName);

            method.addAnnotation(StaticJavaParser.parseAnnotation("@Override"));
            method.setPublic(true);

            NodeList<Statement> stmts = new NodeList<>();
            if (!metaInfo.isRespAbsent()) {
                String newWhat = method.getTypeAsString();
                if (method.getType().isClassOrInterfaceType() && method.getType().asClassOrInterfaceType()
                        .getTypeArguments().isPresent()) {
                    newWhat += "<>";
                }
                stmts.add(StaticJavaParser.parseStatement("return new " + newWhat + "();"));
            }
            method.setBody(new BlockStmt(stmts));
            coid.addMember(method);
            return coid;
        });
        serviceImplCu = serviceImplCreator.create(false);

        return this;
    }

    public CompilationUnit getServiceCu() {
        return this.serviceCu;
    }

    public CompilationUnit getServiceImplCu() {
        return this.serviceImplCu;
    }

    public String getServiceQualifier() {
        return this.serviceQualifier;
    }

}