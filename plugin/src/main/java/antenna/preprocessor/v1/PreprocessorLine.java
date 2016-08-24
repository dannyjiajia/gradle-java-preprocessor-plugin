/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java 
 *
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * -----------------------------------------------------------------------------
 */
package antenna.preprocessor.v1;

import antenna.preprocessor.PreprocessorException;

/**
 * Represents a single line of source code to be handled by the preprocessor.
 * The class provides methods to find out if it is a preprocessor directive and,
 * if so, which one.
 */
class PreprocessorLine {

    public static final String[] types = { "VISIBLE(0)", "HIDDEN(1)", "DEFINE(2)", "UNDEF(3)", "IFDEF(4)", "IFNDEF(5)",
            "ELSE(6)", "ENDIF(7)", "ELIFDEF(8)", "ELIFNDEF(9)", "IF(10)", "ELIF(11)", "INCLUDE(12)", "ENDINCLUDE(13)"};

    /**
     * Represents a normal line of code that is currently visible.
     */
    public static final int TYPE_VISIBLE = 0;

    /**
     * Represents a normal line of code that is currently hidden, that is,
     * commented out using "//#".
     */
    public static final int TYPE_HIDDEN = 1;

    /**
     * Represents a line that holds a "//#define" statement.
     */
    public static final int TYPE_DEFINE = 2;

    /**
     * Represents a line that holds a "//#undef" statement.
     */
    public static final int TYPE_UNDEF = 3;

    /**
     * Represents a line that holds a "//#ifdef" statement.
     */
    public static final int TYPE_IFDEF = 4;

    /**
     * Represents a line that holds a "//#ifndef" statement.
     */
    public static final int TYPE_IFNDEF = 5;

    /**
     * Represents a line that holds a "//#else" statement.
     */
    public static final int TYPE_ELSE = 6;

    /**
     * Represents a line that holds a "//#endif" statement.
     */
    public static final int TYPE_ENDIF = 7;

    /**
     * Represents a line that holds a "//#elifdef" statement.
     */
    public static final int TYPE_ELIFDEF = 8;

    /**
     * Represents a line that holds a "//#elifndef" statement.
     */
    public static final int TYPE_ELIFNDEF = 9;

    /**
     * Represents a line that holds a "//#if" statement.
     */
    public static final int TYPE_IF = 10;

    /**
     * Represents a line that holds a "//#elif" statement.
     */
    public static final int TYPE_ELIF = 11;

    /**
     * Represents a line that holds a "//#include" statement.
     */
    public static final int TYPE_INCLUDE = 12;

    /**
     * Represents a line that holds a "//#endinclude" statement.
     */
    public static final int TYPE_ENDINCLUDE = 13;

    /**
     * Holds the original line of source.
     */
    private String source;

    /**
     * Holds the line type, which is represented by one of the above constants.
     */
    private int type;

    /**
     * If the original line started with one or more spaces or tabs, these are
     * stored here.
     */
    private String space;

    /**
     * Holds the directive contained in the line, or the Java code, in case it
     * is a normal source line.
     */
    private String text;

    /**
     * Holds any arguments that follow the directive, for instance the symbol
     * name referenced by an #ifdef directive.
     */
    private String args;

    /**
     * Creates a new preprocessor line, automatically analyzing the given source
     * line.
     * 
     * @param s
     *            The line to process
     * @throws PreprocessorException
     *             if an unknown directive is used
     */
    public PreprocessorLine(String s) throws PreprocessorException {
        processLine(s);
    }

    public void processLine(String s) throws PreprocessorException {
        this.source = s;

        int p = 0;
        while (p < s.length()) {
            char c = s.charAt(p);
            if ((c != ' ') && (c != '\t')) break;
            p++;
        }

        space = s.substring(0, p);
        s = s.substring(p);

        if (s.startsWith("//#")) {
            type = parseCommand(s);
        }
        else {
            type = TYPE_VISIBLE;
            text = s;
        }
    }

    public String toString() {
        return types[type] + "[" + source + "]";
    }

    private int parseCommand(String s) throws PreprocessorException {
        if (s.startsWith("//# ") || s.startsWith("//#\t")) {
            text = s.substring(4);
            return TYPE_HIDDEN;
        }
        else if (s.equals("//#")) {
            text = "";
            return TYPE_HIDDEN;
        }
        else {
            /**
             * Prevent problem when lines end with a TAB character.
             */
            int p = s.indexOf(' ');
            int q = s.indexOf('\t');

            if (((q != -1) && (q < p)) || (p == -1)) p = q;

            if (p != -1) {
                text = s.substring(0, p);
                args = s.substring(p + 1).trim();
            }
            else {
                text = s;
            }

            if ("//#define".equals(text)) {
                return TYPE_DEFINE;
            }
            else if ("//#undef".equals(text)) {
                return TYPE_UNDEF;
            }
            else if ("//#ifdef".equals(text)) {
                return TYPE_IFDEF;
            }
            else if ("//#ifndef".equals(text)) {
                return TYPE_IFNDEF;
            }
            else if ("//#elifdef".equals(text)) {
                return TYPE_ELIFDEF;
            }
            else if ("//#elifndef".equals(text)) {
                return TYPE_ELIFNDEF;
            }
            else if ("//#else".equals(text)) {
                return TYPE_ELSE;
            }
            else if ("//#endif".equals(text)) {
                return TYPE_ENDIF;
            }
            else if ("//#if".equals(text)) {
                return TYPE_IF;
            }
            else if ("//#elif".equals(text)) {
                return TYPE_ELIF;
            }
            else if ("//#include".equals(text)) {
                return TYPE_INCLUDE;
            }
            else if ("//#endinclude".equals(text)) {
                return TYPE_ENDINCLUDE;
            }
            else {
                throw new PreprocessorException("Unknown directive \"" + text + "\"");
            }
        }
    }

    public String getArgs() {
        if (args == null) {
            throw new RuntimeException(text + " needs an argument");
        }

        return args;
    }

    public String getSource() {
        return source;
    }

    public String getSpace() {
        return space;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

}