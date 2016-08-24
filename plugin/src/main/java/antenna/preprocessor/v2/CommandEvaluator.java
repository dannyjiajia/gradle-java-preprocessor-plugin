package antenna.preprocessor.v2;

import java.util.StringTokenizer;
import java.util.Vector;

import antenna.preprocessor.v2.parser.APPLexerTokenTypes;
import antenna.preprocessor.v2.parser.Define;
import antenna.preprocessor.v2.parser.Defines;
import antenna.preprocessor.v2.parser.Literal;
import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

/**
 * omry 13/02/2007
 */
public class CommandEvaluator
{
	private static final String DEBUG_KEY = "DEBUG";
	
	public static final int UNKNOWN_LINE = -1;
	
	private final Defines m_defines;

	public CommandEvaluator(Defines defines)
	{
		m_defines = defines;
	}

	public boolean evaluate(PPLine ppl, AST ast, IPreprocessorListener listener) throws ANTLRException, PPException
	{
		Eval eval = new Eval(ppl, ast, listener);
		return evaluate(eval);
	}
	
	private boolean evaluate(Eval ast) throws ANTLRException, PPException
	{
		int type = ast.getType();
		switch (type)
		{
			case APPLexerTokenTypes.LITERAL_ifdef:
				return evalDefined(ast.getNextSibling());
			case APPLexerTokenTypes.LITERAL_elifdef:
				return !evalDefined(ast.getNextSibling());
			case APPLexerTokenTypes.LITERAL_if:
			case APPLexerTokenTypes.LITERAL_elif:
			case APPLexerTokenTypes.LITERAL_condition:
				return evaluate(ast.getNextSibling());
			case APPLexerTokenTypes.LITERAL_debug:
			case APPLexerTokenTypes.LITERAL_mdebug:
				return evaluateDebug(ast);
			case APPLexerTokenTypes.LITERAL_ifndef:
			case APPLexerTokenTypes.LITERAL_elifndef:
				return !evaluate(ast.getNextSibling());
			case APPLexerTokenTypes.SYMBOL:
			{
				Define define = m_defines.getDefine(ast.getText());
				if (define == null) return false; // if not defined, assume false.
				// if is a boolean, return it's value.
				if (define.m_value.isBoolean()) return define.m_value.isTrue();
				// else (since it's defined) return true.
				return true;
			}
			case APPLexerTokenTypes.EQ:
			{
				return EQ(ast);
			}
			case APPLexerTokenTypes.NEQ:
			{
				return NEQ(ast);
			}
			case APPLexerTokenTypes.GT:
			{
				return GT(ast);
			}
			case APPLexerTokenTypes.LT:
			{
				return LT(ast);
			}
			case APPLexerTokenTypes.GTE:
			{
				return GTE(ast);
			}
			case APPLexerTokenTypes.LTE:
			{
				return LTE(ast);
			}
			case APPLexerTokenTypes.AT:
			{
				return AT(ast);
			}
			case APPLexerTokenTypes.AND:
			{
				Eval left = ast.getFirstChild();
				Eval right = left.getNextSibling();
				return evaluate(left) && evaluate(right);
			}
			case APPLexerTokenTypes.OR:
			{
				Eval left = ast.getFirstChild();
				Eval right = left.getNextSibling();
				return evaluate(left) || evaluate(right);
			}
			case APPLexerTokenTypes.XOR:
			{
				Eval left = ast.getFirstChild();
				Eval right = left.getNextSibling();
				return evaluate(left) ^ evaluate(right);
			}
			case APPLexerTokenTypes.NOT:
			{
				return !evaluate(ast.getFirstChild());
			}
			case APPLexerTokenTypes.LITERAL_define:
				m_defines.define(ast.getNextSibling().ast);
				return true;
			case APPLexerTokenTypes.LITERAL_undefine:
			{
				String def = ast.getNextSibling().getText();
				boolean removed = m_defines.undefine(def);
				if (!removed)
				{
					System.err.println("Warning: attempting to undefine \"" + def + "\" which is not defined");
				}
				return true;
			}
			default:
				break;
		}
		throw new UnsupportedOperationException("Error evaluating expression " + ast.ppl.getSource());
	}


	private boolean evalDefined(Eval eval)
	{
		return m_defines.isDefined(eval.getText());
	}

	private boolean evaluateDebug(Eval eval) throws PPException
	{
		boolean debugDefined = m_defines.isDefined(DEBUG_KEY);
		if (!debugDefined) return false;
		
		AST nextSibling = eval.ast.getNextSibling();
		// looks like //#debug
		if (nextSibling == null)
		{
			return debugDefined; // always true here
		}
		else
		{
			Define define = m_defines.getDefine(DEBUG_KEY);
			String currentValue = define.m_value.getValue();
			int currentLevel = getDebugLevelNumber(currentValue);
			if (currentLevel == -1)
				throw new PPException("Unknown debug value " + currentValue);
			// line level:
			String level = nextSibling.getText();
			int lineLevel = getDebugLevelNumber(level);
			if (lineLevel == -1)
				throw new PPException("Unknown debug value " + level);
			return lineLevel >= currentLevel;
		}
	}

	private int getDebugLevelNumber(String value)
	{
		int level = -1;
		if (value.equalsIgnoreCase("debug") || value.equalsIgnoreCase("true")) level = 0;
		if (value.equalsIgnoreCase("info")) level = 1;
		if (value.equalsIgnoreCase("warn")) level = 2;
		if (value.equalsIgnoreCase("error")) level = 3;
		if (value.equalsIgnoreCase("fatal")) level = 4;
		return level;
	}

	private boolean AT(Eval ast) throws ANTLRException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		Literal llist[] = values(left);
		Literal rlist[] = values(right);
		for (int i = 0; i < llist.length; i++)
		{
			Literal literal = llist[i];
			for (int j = 0; j < rlist.length; j++)
			{
				if (literal.getValue().equals(rlist[j].getValue()))
					return true;
			}
		}
		return false;
	}

	private boolean GTE(Eval ast) throws ANTLRException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		Literal lval = singleValue(left);
		Literal rval = singleValue(right);
		return !ltImpl(lval, rval);
	}

	private boolean LTE(Eval ast) throws ANTLRException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		Literal lval = singleValue(left);
		Literal rval = singleValue(right);
		return !gtImpl(lval, rval);
	}

	private boolean LT(Eval ast) throws RecognitionException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		Literal lval = singleValue(left);
		Literal rval = singleValue(right);
		return ltImpl(lval, rval);
	}

	private boolean GT(Eval ast) throws RecognitionException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		Literal lval = singleValue(left);
		Literal rval = singleValue(right);
		return gtImpl(lval, rval);
	}

	private boolean EQ(Eval ast) throws RecognitionException
	{
		return eqImpl(ast);
	}

	private boolean NEQ(Eval ast) throws RecognitionException
	{
		return !eqImpl(ast);
	}

	private Literal[] values(Eval ast) throws ANTLRException
	{
		return values(ast, true);
	}
		
	private Literal[] values(Eval ast, boolean warnIfNotDefined) throws ANTLRException
	{
		int type = ast.getType();
		String text = ast.getText();
		switch (type)
		{
			case APPLexerTokenTypes.SYMBOL:
				Define v = m_defines.getDefine(text);
				if (v != null)
				{
					Literal lit = v.m_value;
					return getValues(lit.getValue());
				}
				else
				{
					ast.warning(emptySymbolWarning(text));
					return literals(new Literal(Literal.STRING, ""));
				}
			case APPLexerTokenTypes.STRING:
			{
				String str = text;
				return getValues(str);
			}
			case APPLexerTokenTypes.NUMBER:
				return literals(new Literal(type, text));
		}
		throw new RecognitionException("Unsupported type : " + type);
	}

	private Literal[] getValues(String str) throws RecognitionException, TokenStreamException
	{
		StringTokenizer tok = new StringTokenizer(str, ", ");
		Vector vec = new Vector();
		while(tok.hasMoreElements())
		{
			String t = tok.nextToken();
			Literal literal = new Literal(Literal.STRING, t);
			vec.addElement(literal);
		}
		Literal ls[] = new Literal[vec.size()];
		vec.copyInto(ls);
		return ls;
	}

	private Literal singleValue(Eval ast) throws RecognitionException
	{
		int type = ast.getType();
		String text = ast.getText();
		switch (type)
		{
			case APPLexerTokenTypes.SYMBOL:
				Define v = m_defines.getDefine(text);
				if (v != null)
					return v.m_value;
				else
				{
					ast.warning(emptySymbolWarning(text));
					return new Literal(Literal.STRING, "");
				}
			case APPLexerTokenTypes.STRING:
			case APPLexerTokenTypes.NUMBER:
				return new Literal(type, text);
		}
		throw new RecognitionException("Unsupported type : " + type);
	}


	private String emptySymbolWarning(String text)
	{
		return "Symbol " + text + " is not defined, using empty string";
	}

	private Literal[] literals(Literal value)
	{
		return new Literal[]
		{
			value
		};
	}

	private boolean eqImpl(Eval ast) throws RecognitionException
	{
		Eval left = ast.getFirstChild();
		Eval right = left.getNextSibling();
		
		Literal lval = singleValue(left);
		Literal rval = singleValue(right);
		
		if ((lval.isNumber() ^ rval.isNumber()))
		{
			String number = lval.isNumber() ? left.getText() : right.getText();
			ast.warning("Number " + number + " is compared lexicographically");
		}
		
		if (lval.isNumber() && rval.isNumber())
		{
			double d1 = Double.parseDouble(rval.getValue());
			double d2 = Double.parseDouble(lval.getValue());
			return d1 == d2;
		}
		else
		{
			return lval.getValue().equals(rval.getValue());
		}
	}

	private boolean gtImpl(Literal lval, Literal rval)
	{
		if (lval.isNumber() && rval.isNumber())
		{
			double d1 = Double.parseDouble(rval.getValue());
			double d2 = Double.parseDouble(lval.getValue());
			return d1 < d2;
		}
		else
		{
			return lval.getValue().compareTo(rval.getValue()) > 0;
		}
	}

	private boolean ltImpl(Literal lval, Literal rval)
	{
		if (lval.isNumber() && rval.isNumber())
		{
			double d1 = Double.parseDouble(rval.getValue());
			double d2 = Double.parseDouble(lval.getValue());
			return d1 > d2;
		}
		else
		{
			return lval.getValue().compareTo(rval.getValue()) < 0;
		}
	}
	
	private static class Eval
	{
		public PPLine ppl;
		public AST ast;
		public IPreprocessorListener listener;
		
		public Eval(PPLine ppl, AST ast, IPreprocessorListener listener)
		{
			this.ppl = ppl;
			this.ast = ast;
			this.listener = listener;
		}


		public void warning(String message)
		{
			System.out.println(message);
			if (listener != null)
			{
				int ln = ppl.getLineNumber() + 1; // use 1 based line number system for the external world
				listener.warning(message, ln, ast.getColumn(), getText().length());
			}
		}

		public String getText()
		{
			return ast.getText();
		}

		public int getType()
		{
			return ast.getType();
		}

		public Eval getFirstChild()
		{
			return new Eval(ppl, ast.getFirstChild(), listener);
		}

		public Eval getNextSibling()
		{
			return new Eval(ppl, ast.getNextSibling(), listener);
		}
		
		public String toString()
		{
			return ast.toString();
		}

		public int getColumn()
		{
			return ast.getColumn();
		}
	}
}
