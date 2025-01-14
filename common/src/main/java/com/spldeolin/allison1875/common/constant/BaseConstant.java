package com.spldeolin.allison1875.common.constant;

import com.google.common.base.Strings;

/**
 * @author Deolin 2020-08-08
 */
public interface BaseConstant {

    String SINGLE_INDENT = "    ";

    String DOUBLE_INDENT = Strings.repeat(SINGLE_INDENT, 2);

    String TREBLE_INDENT = Strings.repeat(SINGLE_INDENT, 3);

    String NEW_LINE = System.lineSeparator();

    String JAVA_DOC_NEW_LINE = System.lineSeparator() + "<p>";

    String NEW_LINE_FOR_MATCHING = "[\\r\\n]+";

    String FORMATTER_OFF_MARKER = "<!-- @formatter:off -->";

    String FORMATTER_ON_MARKER = "<!-- @formatter:on -->";

    String LOT_NO_ANNOUNCE_PREFIXION = "Allison 1875 Lot No: ";

    String NO_MODIFY_ANNOUNCE = "Any modifications may be overwritten by future code generations.";

    String[] JAVA_EXTENSIONS = new String[]{"java"};

    String REMEMBER_REFORMAT_CODE_ANNOUNCE = "# REMEMBER REFORMAT CODE #";

}
