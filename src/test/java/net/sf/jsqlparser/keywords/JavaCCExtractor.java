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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import org.javacc.jjtree.JJTree;
import org.javacc.parser.BNFProduction;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParser;
import org.javacc.parser.ParseException;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.Semanticize;
import org.javacc.parser.Token;

/**
 * Extracts keywords from our JSqlParser grammar.
 *
 * @author tw
 */
public final class JavaCCExtractor {

    private static boolean initialized = false;

    private JavaCCExtractor() {
    }
    private static final Logger LOG = Logger.getLogger(JavaCCExtractor.class.getName());

    /**
     * Extract keywords and whitelisted from JSqlParser grammar. Whitelisted are those that can be used as object names
     * as well. There are multiple levels of whitelisting since some keywords are only allowed at specific positions.
     *
     * @throws IOException
     * @throws ParseException
     */
    public static void initialize() throws IOException, ParseException {
        if (initialized) {
            return;
        }

        LOG.info("reading keywords from JSqlParser grammar");
        Path jjtGrammar = Paths.get("src/main/jjtree/net/sf/jsqlparser/parser/JSqlParserCC.jjt");
        Path jjGrammarDir = Paths.get("target/jjgrammar");
        Files.createDirectories(jjGrammarDir);

        new JJTree().main(new String[]{
            "-JDK_VERSION=1.8",
            "-OUTPUT_DIRECTORY=" + jjGrammarDir.toAbsolutePath().toString(),
            jjtGrammar.toAbsolutePath().toString()
        });
        Path jjGrammarFile = jjGrammarDir.resolve("JSqlParserCC.jj");

        JavaCCParser parser = new JavaCCParser(new java.io.FileInputStream(jjGrammarFile.toFile()));
        parser.javacc_input();

        Semanticize.start();

        LOG.info(">>> extracting keywords");
        JavaCCGlobals.named_tokens_table.forEach((id, item) -> {
            if (item instanceof RStringLiteral) {
                RStringLiteral literal = (RStringLiteral) item;
                if (Character.isAlphabetic(literal.image.charAt(0))) {
                    KEYWORDS.put(literal.label, new Keyword(literal));
                }
            } else {
                LOG.log(Level.FINE, "unkown type {0}", item.getClass().getName());
            }
        });

        LOG.info(">>> extracting whitelisted keywords");
        Map<String, Object> prodTable = JavaCCGlobals.production_table;
        for (Map.Entry<String, Object> item : prodTable.entrySet()) {
            if (item.getKey().startsWith("RelObjectName") && !item.getKey().endsWith("List")) {
                LOG.log(Level.FINE, ">>>> whitelisted in {0} type={1}",
                        new Object[]{item.getKey(), item.getValue().getClass().getName()});
                List<String> whitelistedKw = new ArrayList<>();
                WHITELISTED_KEYWORDS.put(item.getKey(), whitelistedKw);

                if (item.getValue() instanceof BNFProduction) {
                    BNFProduction bnf = (BNFProduction) item.getValue();
                    Token token = bnf.getFirstToken();

                    while (token != null) {
                        if (token.image.startsWith("K_")) {
                            LOG.log(Level.FINE, "    >> found {0}", token.image);
                            whitelistedKw.add(token.image);
                        }

                        token = token.next;
                        if (token == bnf.getLastToken()) {
                            break;
                        }
                    }
                }
            }
        }

        initialized = true;
    }

    public static Collection<Keyword> keywords() {
        return Collections.unmodifiableCollection(KEYWORDS.values());
    }

    public static String imageFromKeyword(String keyword) {
        Keyword kw = KEYWORDS.get(keyword);
        if (kw == null) {
            LOG.log(Level.FINE, "no keyword image found for {0}", keyword);
            return null;
        }
        return kw.image;
    }

    public static Map<String, List<String>> whitelistedKeywords() {
        return Collections.unmodifiableMap(WHITELISTED_KEYWORDS);
    }

    public static List<String> whitelistedKeywords(String... keys) {
        Set<String> keySet = new HashSet<>(Arrays.asList(keys));
        return WHITELISTED_KEYWORDS.entrySet().stream()
                .filter(entry -> keySet.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(toList());
    }

    public static List<String> restrictedKeywords() {
        List<String> list = KEYWORDS.values().stream().map(keyword -> keyword.label).collect(toList());
        for (List<String> whitelisted : WHITELISTED_KEYWORDS.values()) {
            list.removeAll(whitelisted);
        }
        return list;
    }

    private static final Map<String, Keyword> KEYWORDS = new HashMap<>();
    private static final Map<String, List<String>> WHITELISTED_KEYWORDS = new HashMap<>();

    public static class Keyword {

        public final String label;
        public final String image;

        public Keyword(RStringLiteral literal) {
            this.label = literal.label;
            this.image = literal.image;
        }
    }
}
