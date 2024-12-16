package com.spldeolin.allison1875.common.test.memberadder;

import java.io.File;
import java.io.FileNotFoundException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.javabean.AddInjectFieldRetval;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import com.spldeolin.allison1875.common.service.impl.MemberAdderServiceImpl;

/**
 * @author Deolin 2024-02-14
 */
public class MemberAdderServiceImplTest {

    private static final MemberAdderService memberAdderService = new MemberAdderServiceImpl();

    public static void main(String[] args) throws FileNotFoundException {
        StaticJavaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(new ClassLoaderTypeSolver(Allison1875.class.getClassLoader())));

        ClassOrInterfaceDeclaration coid = StaticJavaParser.parse(new File(
                        "/Users/deolin/Documents/project-repo/spldeolin/allison1875/common/src/test/java/com/spldeolin"
                                + "/allison1875/common/test/memberadder/Coid.java")).getPrimaryType().get()
                .asClassOrInterfaceDeclaration();


        System.out.println("不同名");
        AddInjectFieldRetval retval = memberAdderService.addInjectField(
                "com.spldeolin.allison1875.common.javabean.FieldArg", "d", coid);
        System.out.println(retval);
        System.out.println(coid);

        System.out.println("同名同类型");
        retval = memberAdderService.addInjectField("com.spldeolin.allison1875.common.javabean.InvalidDTO", "b", coid);
        System.out.println(retval);
        System.out.println(coid);

        System.out.println("同名不同类型");
        retval = memberAdderService.addInjectField("com.spldeolin.allison1875.common.javabean.InvalidDTO", "c", coid);
        System.out.println(retval);
        System.out.println(coid);

        System.out.println("resolve and describe失败");
        retval = memberAdderService.addInjectField("com.X", "x", coid);
        retval = memberAdderService.addInjectField("com.Y", "x", coid);

    }

}