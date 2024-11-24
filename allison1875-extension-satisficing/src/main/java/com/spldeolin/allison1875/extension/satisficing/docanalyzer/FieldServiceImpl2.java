package com.spldeolin.allison1875.extension.satisficing.docanalyzer;

import java.util.List;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageSerializable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.javabean.AnalyzeFieldVarsRetval;
import com.spldeolin.allison1875.docanalyzer.service.impl.FieldServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-25
 */
@Slf4j
@Singleton
public class FieldServiceImpl2 extends FieldServiceImpl {

    @Override
    protected Table<String, String, AnalyzeFieldVarsRetval> getAnalyzeFieldVarsRetvalFromThirdParty() {
        Table<String, String, AnalyzeFieldVarsRetval> result = super.getAnalyzeFieldVarsRetvalFromThirdParty();
        result.put(PageSerializable.class.getName(), "total", new AnalyzeFieldVarsRetval("总记录数"));
        result.put(PageSerializable.class.getName(), "list", new AnalyzeFieldVarsRetval("结果集"));
        result.put(PageInfo.class.getName(), "pageNum", new AnalyzeFieldVarsRetval("当前页"));
        result.put(PageInfo.class.getName(), "pageSize", new AnalyzeFieldVarsRetval("每页的数量"));
        result.put(PageInfo.class.getName(), "size", new AnalyzeFieldVarsRetval("当前页的数量"));
        result.put(PageInfo.class.getName(), "startRow",
                new AnalyzeFieldVarsRetval("当前页面第一个元素在数据库中的行号"));
        result.put(PageInfo.class.getName(), "endRow",
                new AnalyzeFieldVarsRetval("当前页面最后一个元素在数据库中的行号"));
        result.put(PageInfo.class.getName(), "pages", new AnalyzeFieldVarsRetval("总页数"));
        result.put(PageInfo.class.getName(), "prePage", new AnalyzeFieldVarsRetval("前一页"));
        result.put(PageInfo.class.getName(), "nextPage", new AnalyzeFieldVarsRetval("下一页"));
        result.put(PageInfo.class.getName(), "isFirstPage", new AnalyzeFieldVarsRetval("是否为第一页"));
        result.put(PageInfo.class.getName(), "isLastPage", new AnalyzeFieldVarsRetval("是否为最后一页"));
        result.put(PageInfo.class.getName(), "hasPreviousPage", new AnalyzeFieldVarsRetval("是否有前一页"));
        result.put(PageInfo.class.getName(), "hasNextPage", new AnalyzeFieldVarsRetval("是否有下一页"));
        result.put(PageInfo.class.getName(), "navigatePages", new AnalyzeFieldVarsRetval("导航页码数"));
        result.put(PageInfo.class.getName(), "navigatepageNums", new AnalyzeFieldVarsRetval("所有导航页号"));
        result.put(PageInfo.class.getName(), "navigateFirstPage", new AnalyzeFieldVarsRetval("导航条上的第一页"));
        result.put(PageInfo.class.getName(), "navigateLastPage", new AnalyzeFieldVarsRetval("导航条上的最后一页"));
        return result;
    }

    @Override
    protected List<String> analyzeMoreAndGenerateDoc(FieldDeclaration field, VariableDeclarator fieldVar) {
        return Lists.newArrayList();
    }

}