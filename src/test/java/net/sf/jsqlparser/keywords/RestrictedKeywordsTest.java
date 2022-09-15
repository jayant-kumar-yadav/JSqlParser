/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.keywords;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import org.javacc.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RestrictedKeywordsTest {

    /**
     * Marked restricted Keywords.
     */
    private static final List<String> RESTRICTED_KEYWORDS = Arrays.asList("K_PLACING",
            "K_DECLARE",
            "K_RESET",
            "K_NOORDER",
            "K_XOR",
            "K_GUARD",
            "K_TRAILING",
            "K_ASC",
            "K_EMIT",
            "K_SQL_NO_CACHE",
            "K_ESCAPE",
            "K_INCREMENT",
            "K_TUMBLING",
            "K_NOMINVALUE",
            "K_FETCH",
            "K_CONNECT_BY_ROOT",
            "K_KEYS",
            "K_UNLOGGED",
            "K_XMLSERIALIZE",
            "K_AGAINST",
            "K_UNBOUNDED",
            "K_ALTER",
            "K_CROSS",
            "K_GROUP_CONCAT",
            "K_WITH_TIES",
            "K_ROLLBACK",
            "K_LIKE",
            "K_FORCE",
            "K_INTERSECT",
            "K_NOMAXVALUE",
            "K_NATURAL",
            "K_ADVISE",
            "K_SQL_CACHE",
            "K_OR",
            "K_JSON_ARRAYAGG",
            "K_STRAIGHT",
            "K_JSON_OBJECT",
            "K_ONLY",
            "K_DELETE",
            "K_RETURNING",
            "K_AS",
            "K_CALL",
            "K_INTO",
            "K_FUNCTION",
            "K_EXCEPT",
            "K_RESUMABLE",
            "K_CHECK",
            "K_MATCHED",
            "K_SETS",
            "K_WITHOUT",
            "K_DELAYED",
            "K_DML",
            "K_LOW_PRIORITY",
            "K_MINUS",
            "K_BIT",
            "K_DISTINCT",
            "K_ABSENT",
            "K_ADD",
            "K_NOKEEP",
            "K_UPDATE",
            "K_BEGIN",
            "K_MERGE",
            "K_CONNECT",
            "K_RECURSIVE",
            "K_XMLTEXT",
            "K_THEN",
            "K_DATABASE",
            "K_AUTHORIZATION",
            "K_ANALYZE",
            "K_NOCYCLE",
            "K_BUFFERS",
            "K_BY",
            "K_MINVALUE",
            "K_NOT",
            "K_QUICK",
            "K_FOR",
            "K_INNER",
            "K_LATERAL",
            "K_DEFERRABLE",
            "K_XMLAGG",
            "K_NOCACHE",
            "K_APPLY",
            "K_USING",
            "K_OFF",
            "K_MAXVALUE",
            "K_NOVALIDATE",
            "K_NOWAIT",
            "K_RESTART",
            "K_CONSTRAINT",
            "K_SHOW",
            "K_CACHE",
            "K_DROP",
            "K_PIVOT",
            "K_VERBOSE",
            "K_NEXT",
            "K_REGEXP",
            "K_USE",
            "K_IGNORE",
            "K_MOVEMENT",
            "K_BINARY",
            "K_YAML",
            "K_EXEC",
            "K_FULL",
            "K_JSON_ARRAY",
            "K_MATCH",
            "K_SAVEPOINT",
            "K_SQL_CALC_FOUND_ROWS",
            "K_WINDOW",
            "K_WITH",
            "K_MODIFY",
            "K_HIGH_PRIORITY",
            "K_OUTER",
            "K_OUTPUT",
            "K_UNPIVOT",
            "K_CLOSE",
            "K_PURGE",
            "K_BETWEEN",
            "K_AND",
            "K_DDL",
            "K_GRANT",
            "K_HAVING",
            "K_ADVANCE",
            "K_REFERENCES",
            "K_HOPPING",
            "K_BOTH",
            "K_EXECUTE",
            "K_NULL",
            "K_UPSERT",
            "K_OVERLAPS",
            "K_UNIQUE",
            "K_INCLUDE",
            "K_WHERE",
            "K_EXTENDED",
            "K_RESTRICT",
            "K_WAIT",
            "K_CONFLICT",
            "K_EXPLAIN",
            "K_PRECEDING",
            "K_IS",
            "K_SEMI",
            "K_FOREIGN",
            "K_RLIKE",
            "K_JOIN",
            "K_JSON_OBJECTAGG",
            "K_VARYING",
            "K_ILIKE",
            "K_WITHIN",
            "K_UNION",
            "K_WORK",
            "K_CHANGES",
            "K_SIMILAR",
            "K_NOTHING",
            "K_COLLATE",
            "K_WHEN",
            "K_EXISTS",
            "K_GLOBAL",
            "K_RENAME",
            "K_ELSE");

    /**
     * Here all keywords that are restricted should be detected. If someone adds a keyword without adding it to the
     * restricted keyword list or putting it into our RelObject productions this method will fail.
     */
    @Test
    void checkRestrictedKeywords() {
        assertThat(JavaCCExtractor.restrictedKeywords())
                .withFailMessage("restricted tokens/keywords must be in RestrictedKeywordsTest.RESTRICTED_KEYWORDS and whitelisted must be used in RelObjectNames productions")
                .containsExactlyInAnyOrderElementsOf(RESTRICTED_KEYWORDS);
    }

    @Test
    void checkStructureOfGrammar() {
        assertThat(JavaCCExtractor.whitelistedKeywords().keySet())
                .containsExactlyInAnyOrder(
                        "RelObjectNameWithoutStart",
                        "RelObjectName",
                        "RelObjectNameWithoutValue",
                        "RelObjectNameExt",
                        "RelObjectNameExt2");
    }

    public static Stream<String> allPositionKeywords() {
        return JavaCCExtractor.whitelistedKeywords("RelObjectNameWithoutValue").stream();
    }

    @ParameterizedTest(name = "all Position Keyword {0}")
    @MethodSource("allPositionKeywords")
    void checkWhitelistedAllowedAtAllPositions(String keyword) throws JSQLParserException {
        final String imageFromKeyword = JavaCCExtractor.imageFromKeyword(keyword);
        if (imageFromKeyword != null) {
            assertSqlCanBeParsedAndDeparsed(String.format("SELECT %1$s.%1$s AS %1$s FROM %1$s.%1$s AS %1$s", imageFromKeyword));
        } else {
            LOG.log(Level.INFO, "No image found for {0}. Only literal strings are tested!", keyword);
        }
    }

    public static Stream<String> noAliasKeywords() {
        return JavaCCExtractor.whitelistedKeywords("RelObjectName", "RelObjectNameExt", "RelObjectNameExt2").stream();
    }

    @ParameterizedTest(name = "no Alias Keyword {0}")
    @MethodSource("noAliasKeywords")
    void checkWhitelistedNoAlias(String keyword) throws JSQLParserException {
        final String imageFromKeyword = JavaCCExtractor.imageFromKeyword(keyword);
        if (imageFromKeyword != null) {
            assertSqlCanBeParsedAndDeparsed(String.format("SELECT %1$s.%1$s.%1$s AS \"%1$s\" FROM %1$s ORDER BY %1$s", imageFromKeyword));
        } else {
            LOG.info("No image found for " + keyword + ". Only literal strings are tested!");
        }
    }

    private static final Logger LOG = Logger.getLogger(RestrictedKeywordsTest.class.getName());

    @BeforeAll
    static void extractKeywordsFromGrammarFile() throws IOException, ParseException {
        JavaCCExtractor.initialize();
    }
}
