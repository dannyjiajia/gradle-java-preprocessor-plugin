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
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package antenna.preprocessor.v1;

import java.util.Hashtable;

import antenna.preprocessor.PreprocessorException;

/**
 * Implements a simple boolean evaluator. The evaluator can define
 * (store) named symbols and undefine (delete) them. It also provides
 * a method to check for the existence of a given symbols. The latter
 * method is usually called to process "#ifdef"-like statements.
 * <p>
 * For more complex "if"-like statements, the evaluator is also able
 * to process whole boolean expressions consisting of symbol names,
 * parentheses and the usual Java operators for boolean "and", "or",
 * "exclusive or", and "not" ("&&", "||", "^", and "!", respectively).
 */
class BooleanEvaluator {

	/**
	 * Holds the currently defined symbols.
	 */
	private Hashtable symbols;
	private final String defines;

	/**
	 * Creates a new evaluator with an optional list
     * of defined symbols.
	 */
	public BooleanEvaluator(String defines) {
		this.defines = defines;
		symbols = new Hashtable();

        if (defines != null) {
    		defines = defines + ",";
    
    		int p = 0;
    		while (p < defines.length()) {
    			int q = defines.indexOf(',', p);
    			String t = defines.substring(p, q).trim();
    			if (t.length() != 0)
    				define(t);
    			p = q + 1;
    		}
        }
	}

	/**
	 * Defines a symbol, thus making its value true
	 * when the symbol is used in further expressions.
	 */
	public void define(String symbol) {
		symbols.put(symbol, symbol);
	}

	/**
	 * Defines a symbol, thus making its value false
	 * when the symbol is used in further expressions.
	 */
	public void undefine(String symbol) {
		symbols.remove(symbol);
	}

	/**
	 * Checks whether a symbol is currently defined, that
	 * is, whether it is true (kind of a "closed world
	 * assumption").
	 */
	public boolean isDefined(String symbol) {
		return symbols.containsKey(symbol);
	}

	/**
	 * Parses a boolean factor, that is, a part of an expression
	 * that only consists of identifiers, possibly preceded by
	 * a "not" sign, or whole subexpressions enclosed in
	 * parentheses.
	 */
	private boolean parseFactor(BooleanTokenizer t) throws PreprocessorException {
		boolean result;

		if (t.getTokenType() == BooleanTokenizer.TYPE_LPAR) {
			t.nextToken();
			result = parseExpression(t);
			if (t.getTokenType() != BooleanTokenizer.TYPE_RPAR) {
				throw new PreprocessorException("\")\" expected");
			}
			t.nextToken();
		}
		else if (t.getTokenType() == BooleanTokenizer.TYPE_NOT) {
			t.nextToken();
			result = !parseFactor(t);
		}
		else if (t.getTokenType() == BooleanTokenizer.TYPE_ID) {
			result = isDefined(t.getTokenText());
			t.nextToken();
		}
		else
			throw new PreprocessorException("Identifier, \"!\" or \"(\" expected");

		return result;
	}

	/**
	 * Parses a boolean term, that is, a part of an expression
	 * that only consists of operator with a higher precedence
	 * that "or" and "xor".
	 */
	private boolean parseTerm(BooleanTokenizer t) throws PreprocessorException {
		boolean result = parseFactor(t);

		while (t.getTokenType() == BooleanTokenizer.TYPE_AND) {
			t.nextToken();
			result = result & parseFactor(t);
		}

		return result;
	}

	/**
	 * Parses a boolean expression.
	 */
	private boolean parseExpression(BooleanTokenizer t) throws PreprocessorException {
		boolean result = parseTerm(t);

		while (true) {
			if (t.getTokenType() == BooleanTokenizer.TYPE_OR) {
				t.nextToken();
				result = result | parseTerm(t);
			}
			else if (t.getTokenType() == BooleanTokenizer.TYPE_XOR) {
				t.nextToken();
				result = result ^ parseTerm(t);
			}
			else {
				return result;
			}
		}
	}

	/**
	 * Evaluates a boolean expression.
	 */
	public boolean evaluate(String expression) throws PreprocessorException {
        //System.out.println("EVAL: " + expression);
        
		BooleanTokenizer t = new BooleanTokenizer(expression);

		t.nextToken();
		boolean result = parseExpression(t);

		if (t.getTokenType() != BooleanTokenizer.TYPE_STOP) {
			throw new PreprocessorException("Syntax error");
		}

		return result;
	}

	public String getDefines()
	{
		return defines;
	}
}
