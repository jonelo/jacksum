/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, <https://jacksum.net>.

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <https://www.gnu.org/licenses/>.


 */
package net.jacksum.algorithms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Determines whether an algorithm is considered broken. The information is read
 * from the field "broken:" of the algorithm's documentation in Jacksum's
 * English help file, which is the single source of truth for that information.
 * <p>
 * A single query does not keep any state: it performs one streaming pass over
 * the algorithm section of the help file and keeps the matching block only. If
 * many algorithms are queried (e.g. if Jacksum is used as a library, or by the
 * option --list in combination with --info), call {@link #preload()} once in
 * order to build a cache that answers all subsequent queries without any
 * further I/O.
 *
 * @since 4.0.0
 */
public final class BrokenStateRegistry {

    private static final String HELP_FILE = "/net/jacksum/help/help_en.txt";
    private static final String SECTION_BEGIN = "#ALGORITHMS-BEGIN";
    private static final String SECTION_END = "#ALGORITHMS-END";
    private static final String FIELD = "broken:";

    // a line that consists of a single backslash is a blank line that belongs to the block
    private static final String BLOCK_BLANK_LINE = "\\";

    private static final int HEADER_INDENT_MAX = 11;
    private static final int FIELD_INDENT = 12;
    private static final int VALUE_INDENT_MIN = 16;

    // priorities of the three matching stages, the lower the better
    private static final int PRIORITY_EXACT = 1;
    private static final int PRIORITY_PATTERN = 2;
    private static final int PRIORITY_COLON_PREFIX = 3;
    private static final int PRIORITY_NONE = Integer.MAX_VALUE;

    private static final Block NOT_FOUND = new Block(Collections.emptyList());

    /**
     * The cache. It is null unless {@link #preload()} has been called
     * explicitly, so that neither a normal hash calculation nor a single query
     * builds it implicitly.
     */
    private static volatile Index cache;

    private BrokenStateRegistry() {
    }

    /**
     * Returns whether the algorithm with the given ID is considered broken.
     *
     * @param algorithmId the ID of the algorithm, e.g. "md5", "haval_128_3",
     * "hmac:sha1"
     * @return the state, never null, {@link BrokenState#NOT_APPLICABLE} if the
     * algorithm is unknown or if it does not claim cryptographic security
     */
    public static BrokenState getBrokenState(String algorithmId) {
        return lookup(algorithmId).state;
    }

    /**
     * Returns the explanation of the state that is returned by
     * {@link #getBrokenState(String)}, as it is documented in Jacksum's help
     * file. The lines are returned as they are wrapped in the help file.
     *
     * @param algorithmId the ID of the algorithm, e.g. "md5"
     * @return the lines of the explanation, an empty list if there is none
     */
    public static List<String> getBrokenDescription(String algorithmId) {
        return Collections.unmodifiableList(lookup(algorithmId).description);
    }

    /**
     * Builds the cache that answers all subsequent queries without any further
     * I/O. Calling this method is only worthwhile if many algorithms are
     * queried, because a single query is answered by one streaming pass over
     * the help file anyway. The method is idempotent.
     */
    public static void preload() {
        if (cache == null) {
            cache = buildIndex();
        }
    }

    /**
     * Releases the cache that has been built by {@link #preload()}.
     */
    public static void unload() {
        cache = null;
    }

    /**
     * Returns whether the cache has been built by {@link #preload()}.
     *
     * @return whether the cache has been built
     */
    public static boolean isPreloaded() {
        return cache != null;
    }

    private static Block lookup(String algorithmId) {
        if (algorithmId == null) {
            return NOT_FOUND;
        }
        String id = algorithmId.toLowerCase(Locale.US);
        Index index = cache;
        return index != null ? index.lookup(id) : singleLookup(id);
    }

    /**
     * Performs one streaming pass over the help file and keeps the best
     * matching block only.
     */
    private static Block singleLookup(String id) {
        Candidate candidate = new Candidate();
        scan(block -> {
            int priority = priorityOf(block, id, candidate.priority);
            if (priority < candidate.priority) {
                candidate.priority = priority;
                candidate.block = block;
            }
            // nothing can beat an exact match, and among equal matches the first one wins
            return candidate.priority > PRIORITY_EXACT;
        });
        return candidate.block != null ? candidate.block : NOT_FOUND;
    }

    /**
     * Determines by which of the three matching stages the given block matches
     * the given algorithm ID, see {@link Index#lookup(String)}. Stages that
     * cannot beat the given priority are skipped.
     */
    private static int priorityOf(Block block, String id, int bestPriority) {
        for (String token : block.tokens) {
            if (token.equals(id)) {
                return PRIORITY_EXACT;
            }
        }
        if (bestPriority > PRIORITY_PATTERN) {
            for (String token : block.tokens) {
                if (isParameterized(token) && toPattern(token).matcher(id).matches()) {
                    return PRIORITY_PATTERN;
                }
            }
        }
        if (bestPriority > PRIORITY_COLON_PREFIX) {
            int colon = id.indexOf(':');
            if (colon > 0) {
                String prefix = id.substring(0, colon);
                for (String token : block.tokens) {
                    int tokenColon = token.indexOf(':');
                    if (tokenColon > 0 && token.substring(0, tokenColon).equals(prefix)) {
                        return PRIORITY_COLON_PREFIX;
                    }
                }
            }
        }
        return PRIORITY_NONE;
    }

    private static Index buildIndex() {
        Index index = new Index();
        scan(block -> {
            index.add(block);
            return true;
        });
        return index;
    }

    /**
     * Reads the algorithm section of the help file and hands over each block
     * that has been read to the given handler, until the handler asks to stop.
     * If the help file cannot be read, no block is handed over at all, because
     * a missing information must not break the program.
     */
    private static void scan(BlockHandler handler) {
        InputStream is = BrokenStateRegistry.class.getResourceAsStream(HELP_FILE);
        if (is == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            boolean inSection = false;
            boolean carryOn = true;
            Block block = null;
            boolean inField = false;
            String line;
            while (carryOn && (line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (line.startsWith(SECTION_BEGIN)) {
                        inSection = true;
                    } else if (line.startsWith(SECTION_END)) {
                        break;
                    }
                    continue;
                }
                if (!inSection) {
                    continue;
                }
                if (line.equals(BLOCK_BLANK_LINE)) {
                    // a blank line that belongs to the block, it terminates a field value
                    inField = false;
                    continue;
                }
                if (line.isBlank()) {
                    // a blank line terminates the block
                    if (block != null) {
                        carryOn = handler.handle(block);
                        block = null;
                    }
                    inField = false;
                    continue;
                }
                int indent = indentOf(line);
                if (indent <= HEADER_INDENT_MAX) {
                    // a header line terminates the previous block and starts a new one
                    if (block != null) {
                        carryOn = handler.handle(block);
                        block = null;
                    }
                    inField = false;
                    if (carryOn) {
                        block = new Block(tokenize(line));
                    }
                } else if (block == null) {
                    // a continuation line without a header, ignore it
                } else if (indent == FIELD_INDENT) {
                    inField = line.trim().equals(FIELD);
                } else if (inField && indent >= VALUE_INDENT_MIN) {
                    block.description.add(line.trim());
                    if (block.description.size() == 1) {
                        block.state = BrokenState.fromHelpToken(firstWordOf(line));
                    }
                }
            }
            if (carryOn && block != null) {
                handler.handle(block);
            }
        } catch (IOException ioe) {
            // a help file that cannot be read must not break the program
        }
    }

    /**
     * Splits a header line into the algorithm IDs and aliases it declares, e.g.
     * "md5, md5sum" into "md5" and "md5sum". Square brackets, which indicate
     * optional parts, are removed, exactly as it is done by the help system.
     */
    private static List<String> tokenize(String headerLine) {
        List<String> tokens = new ArrayList<>();
        for (String token : headerLine.trim().split(",")) {
            String trimmed = token.trim().replace("[", "").replace("]", "");
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed.toLowerCase(Locale.US));
            }
        }
        return tokens;
    }

    private static boolean isParameterized(String token) {
        return token.indexOf('<') >= 0;
    }

    /**
     * Converts a parameterized token to a regular expression, e.g.
     * "haval_&lt;length&gt;_&lt;rounds&gt;" to "\Qhaval_\E[0-9]+\Q_\E[0-9]+",
     * which matches "haval_128_3". This is the same semantics that the help
     * system uses for the option -h.
     */
    private static Pattern toPattern(String token) {
        StringBuilder regex = new StringBuilder();
        int pos = 0;
        while (pos < token.length()) {
            int open = token.indexOf('<', pos);
            if (open < 0) {
                break;
            }
            int close = token.indexOf('>', open);
            if (close < 0) {
                break;
            }
            if (open > pos) {
                regex.append(Pattern.quote(token.substring(pos, open)));
            }
            regex.append("[0-9]+");
            pos = close + 1;
        }
        if (pos < token.length()) {
            regex.append(Pattern.quote(token.substring(pos)));
        }
        return Pattern.compile(regex.toString());
    }

    private static int indentOf(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    private static String firstWordOf(String line) {
        String trimmed = line.trim();
        int blank = trimmed.indexOf(' ');
        return blank < 0 ? trimmed : trimmed.substring(0, blank);
    }

    private interface BlockHandler {

        /**
         * @param block the block that has been read
         * @return whether the scan should carry on
         */
        boolean handle(Block block);
    }

    /**
     * One algorithm entry of the help file.
     */
    private static class Block {

        private final List<String> tokens;
        private final List<String> description = new ArrayList<>();
        private BrokenState state = BrokenState.NOT_APPLICABLE;

        Block(List<String> tokens) {
            this.tokens = tokens;
        }
    }

    /**
     * Mutable holder for the best matching block that has been found so far.
     */
    private static class Candidate {

        private int priority = PRIORITY_NONE;
        private Block block;
    }

    /**
     * The cache: all blocks of the help file, prepared for fast lookups.
     */
    private static class Index {

        private final Map<String, Block> exactMatches = new HashMap<>(256);
        private final List<PatternMatch> patternMatches = new ArrayList<>();
        private final Map<String, Block> colonPrefixMatches = new HashMap<>();

        void add(Block block) {
            for (String token : block.tokens) {
                if (isParameterized(token)) {
                    patternMatches.add(new PatternMatch(toPattern(token), block));
                } else {
                    exactMatches.putIfAbsent(token, block);
                }
                int colon = token.indexOf(':');
                if (colon > 0) {
                    colonPrefixMatches.putIfAbsent(token.substring(0, colon), block);
                }
            }
        }

        Block lookup(String id) {
            Block block = exactMatches.get(id);
            if (block != null) {
                return block;
            }
            for (PatternMatch patternMatch : patternMatches) {
                if (patternMatch.pattern.matcher(id).matches()) {
                    return patternMatch.block;
                }
            }
            int colon = id.indexOf(':');
            if (colon > 0) {
                block = colonPrefixMatches.get(id.substring(0, colon));
                if (block != null) {
                    return block;
                }
            }
            return NOT_FOUND;
        }
    }

    private static class PatternMatch {

        private final Pattern pattern;
        private final Block block;

        PatternMatch(Pattern pattern, Block block) {
            this.pattern = pattern;
            this.block = block;
        }
    }

}
