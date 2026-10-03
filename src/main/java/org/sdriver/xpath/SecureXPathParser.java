/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the location-specific identifier of an XPath query: its structure with every
 * literal value taken out, combined with the call stack that issued it.
 *
 * <p>Two calls get the same identifier when they issue a query of the same shape from the
 * same chain of methods. The values in the query (string literals and numbers) do not take
 * part, so a trained query keeps matching whatever data it is called with.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 * @author Dimitrios Mitropoulos (dimitro@aueb.gr)
 */
public final class SecureXPathParser {
    /** Stands in for a string literal or a number in the normalised query. */
    static final String VALUE = "?";

    /** The library's own frames, which sit between the caller and the stack walk. */
    private static final Set<String> INTERNAL_FRAMES = Set.of(
            SecureXPathParser.class.getName(),
            SecureXPath.class.getName(),
            "javax.xml.xpath.XPath");

    private static final StackWalker WALKER = StackWalker.getInstance();

    private final String query;

    public SecureXPathParser(String query) {
        this.query = query;
    }

    /**
     * Returns the query as a sequence of XPath 1.0 tokens separated by single spaces, with
     * every string literal (single- or double-quoted) and every number replaced by
     * {@value #VALUE}. Names, axes, node tests, functions and operators are kept as written.
     */
    public String strip() {
        return String.join(" ", tokens(query));
    }

    /**
     * Returns the identifier of the query as issued from the current call stack: a SHA-256
     * over the normalised query and the class and method of every frame below the library.
     */
    public String getUniqueIdentifier() {
        List<String> callers = WALKER.walk(frames -> frames
                .dropWhile(f -> INTERNAL_FRAMES.contains(f.getClassName()))
                .map(f -> f.getClassName() + "." + f.getMethodName())
                .collect(Collectors.toList()));

        StringBuilder key = new StringBuilder(strip());
        for (String caller : callers) {
            key.append('\n').append(caller);
        }
        return sha256(key.toString());
    }

    static List<String> tokens(String s) {
        List<String> out = new java.util.ArrayList<>();
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '\'' || c == '"') {
                int end = s.indexOf(c, i + 1);
                if (end < 0) {
                    // An unterminated literal is not valid XPath; keep it verbatim so the
                    // query can never match a trained one.
                    out.add(s.substring(i));
                    break;
                }
                out.add(VALUE);
                i = end + 1;
            } else if (isDigit(c) || (c == '.' && i + 1 < n && isDigit(s.charAt(i + 1)))) {
                while (i < n && isDigit(s.charAt(i))) {
                    i++;
                }
                if (i < n && s.charAt(i) == '.') {
                    i++;
                    while (i < n && isDigit(s.charAt(i))) {
                        i++;
                    }
                }
                out.add(VALUE);
            } else if (isNameStart(c)) {
                int start = i;
                while (i < n && isNameChar(s.charAt(i))
                        && !(s.charAt(i) == ':' && i + 1 < n && s.charAt(i + 1) == ':')) {
                    i++;
                }
                out.add(s.substring(start, i));
            } else if (c == '-' && isUnaryPosition(out) && startsNumber(s, skipSpace(s, i + 1))) {
                // A sign belongs to the value: "-5" is as much a value as "5". A '-' after an
                // operand is subtraction and stays.
                i++;
            } else {
                String two = i + 1 < n ? s.substring(i, i + 2) : "";
                if (two.equals("//") || two.equals("..") || two.equals("::")
                        || two.equals("!=") || two.equals("<=") || two.equals(">=")) {
                    out.add(two);
                    i += 2;
                } else {
                    out.add(String.valueOf(c));
                    i++;
                }
            }
        }
        return out;
    }

    /** The tokens after which a '-' can only be a sign: an operator, or an opening bracket. */
    private static final Set<String> BEFORE_UNARY = Set.of(
            "(", "[", ",", "=", "!=", "<", "<=", ">", ">=", "+", "-", "|",
            "and", "or", "div", "mod");

    private static boolean isUnaryPosition(List<String> out) {
        return out.isEmpty() || BEFORE_UNARY.contains(out.get(out.size() - 1));
    }

    private static int skipSpace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean startsNumber(String s, int i) {
        return i < s.length() && (isDigit(s.charAt(i))
                || (s.charAt(i) == '.' && i + 1 < s.length() && isDigit(s.charAt(i + 1))));
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isNameStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isNameChar(char c) {
        // XPath names may contain '-' and '.' (last-name, xs.date) and a prefix ':'.
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == ':';
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256.
            throw new IllegalStateException(e);
        }
    }
}
