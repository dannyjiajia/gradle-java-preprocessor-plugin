package antenna.preprocessor.v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.Vector;

import antenna.preprocessor.v2.parser.APPLexer;
import antenna.preprocessor.v2.parser.APPLexerTokenTypes;
import antenna.preprocessor.v2.parser.APPParser;
import antenna.preprocessor.v2.parser.Defines;
import antenna.preprocessor.v2.parser.PPLineAST;
import antlr.ANTLRException;
import antlr.CommonAST;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;


/**
 * omry 18/02/2007
 */
public class Preprocessor
{
	public static final int STATE_NO_CONDITIONAL = 0;

	public static final int STATE_CAN_BECOME_TRUE = 1;

	public static final int STATE_IS_TRUE = 2;

	public static final int STATE_HAS_BEEN_TRUE = 3;
	
	private Stack m_statsStack;

	private int m_currentState;

	private boolean m_verbose = false;
	
	private Defines m_defines;

	private File m_file;

	public ILogger m_logger;

	public ILineFilter m_lineFilter;

	private IPreprocessorListener m_listener;
	
	/**
	 * true if the current file is disabled by the //#condition directive at the first line.
	 */
	private boolean m_disabledByCondition = false;

	/**
	 * true if the next line should be hidden due to a //#debug directive.
	 */
	private boolean m_debugHideNextLine = false;

	private int m_currentMdebugBlockStart = -1;
	private boolean m_insideHiddenMdebugBlock = false;

	/**
	 * true if we modified the lines vector
	 */
	private boolean m_modified;
	
	public Preprocessor(ILogger logger, ILineFilter lineFilter)
	{
		m_logger = logger;
		m_lineFilter = lineFilter;
		m_defines = new Defines(lineFilter);
	}
	
	public void setListener(IPreprocessorListener listener)
	{
		m_listener = listener;
	}

	public void setFile(File fileName)
	{
		m_file = fileName;
	}

	public void addDefines(String defines) throws PPException
	{
		try
		{
			m_defines.addDefines(defines);
		}
		catch (ANTLRException e)
		{
			throw new PPException("Error evaluating symbols \"" + defines + "\"", e);
		}
	}
	
	public void addDefines(InputStream in) throws IOException, PPException
	{
		try
		{
			m_defines.loadDefines(in);
		}
		catch (ANTLRException e)
		{
			throw new PPException("Error evaluating symbols from input stream" , e);
		}	
	}	
	
	public void addDefines(File file) throws IOException, PPException
	{
		try
		{
			m_defines.loadDefines(file);
		}
		catch (ANTLRException e)
		{
			throw new PPException("Error evaluating symbols from file "  + file, e);
		}
	}

	

	private void log(String msg)
	{
		if (m_verbose)
		{
			if (m_logger != null)
			{
				m_logger.log(msg);
			}
			else
			{
				System.err.println(msg);
			}
		}
	}

	public boolean preprocess(InputStream in, OutputStream out, String encoding) throws IOException, PPException
	{
		Vector lines = new Vector();
		loadStrings(lines, in, encoding);
		boolean changed = preprocess(lines, encoding);
		saveStrings(lines, out, encoding);
		return changed;
	}

	public static void saveStrings(Vector lines, OutputStream out, String encoding) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
		try
		{
			int size = lines.size();
			for (int i = 0; i < size; i++)
			{
				writer.write((String)lines.get(i));
				writer.newLine();
			}
		}
		finally
		{
			writer.close();
		}
	}

	public static void loadStrings(Vector lines, InputStream in, String encoding) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
		String s;
		while ((s = reader.readLine()) != null)
		{
			lines.add(s);
		}
		reader.close();
	}

	public boolean preprocess(Vector lines, String encoding) throws IOException, PPException
	{
		try
		{
			return preprocessImpl(lines, encoding);
		}
		catch (IOException e)
		{
			if (m_listener != null)
			{
				m_listener.error(e, -1,-1,-1);
			}
			throw e;
		}
		catch (PPException e)
		{
			e.printStackTrace();
			if (m_listener != null)
			{
				int lineNumber = e.getLineNumber();
				int ln = lineNumber != PPException.UNKNOWN_LINE ?  (lineNumber + 1) : lineNumber;
				m_listener.error(e, ln, -1, -1);
				return false;
			}
			else
			{
				// only throw if we didn't report to a listener.
				throw e;
			}
		}
	}
	
	private boolean preprocessImpl(Vector lines, String encoding) throws IOException, PPException
	{
		m_modified = false;
		m_statsStack = new Stack();
		m_currentState = STATE_NO_CONDITIONAL;
		m_disabledByCondition = false;

		CommandEvaluator eval = new CommandEvaluator(m_defines);

		int i = 0;
		while (i < lines.size())
		{
			// System.out.println("(" + i + ") " + lines.get(i));
			String line = (String) lines.get(i);
			if (m_lineFilter != null)
			{
				line = m_lineFilter.filter(line);
			}

			PPLine lp = new PPLine(m_file, line, i);
			try
			{
				if (lp.getType() == PPLine.TYPE_VISIBLE || lp.getType() == PPLine.TYPE_HIDDEN)
				{
					if (isBlind())
					{
						String l = commentLine(lp);
						if (!l.equals(line))
							m_modified = true;

						lines.set(i, l);
						if (isVerbose())
						{
							log("(+)" + l);
						}
					}
					else
					{
						// true to replace the current line, false to insert it.
						boolean replace = true;
						String str = uncommentLine(lp);
						
						if (!str.equals(line))
							m_modified = true;

						if (replace)
						{
							lines.set(i, str);
						}
						else
						{
							lines.insertElementAt(str, i);
						}
						
						if (isVerbose())
						{
							log("(-)" + str);
						}
					}
					// reset debug hide for next line.
					m_debugHideNextLine = false;
				}
				else
				{
					CommonAST ast = getAST(lp);
					int includeLine = i; // for error handling
					// if type is include,
					// look for the endinclude, while removing the lines between the include and the endinclude.
					if (ast.getType() == APPLexerTokenTypes.LITERAL_include)
					{
						boolean foundEndInclude = false;
						i++;
						int currentLine = i;
						while (i < lines.size()) 
						{
							PPLine lp2 = new PPLine(m_file, (String) lines.get(i), currentLine);
							if (lp2.getType() == PPLine.TYPE_COMMAND)
							{
								CommonAST ast2 = getAST(lp2);
								if (ast2.getType() == APPLexerTokenTypes.LITERAL_endinclude)
								{
									foundEndInclude = true;									
									break;
								}
							}
							
							lines.remove(i);
							currentLine++;
						}

						if (!foundEndInclude) 
						{
							throw new PPException("Missing #endinclude", m_file, includeLine);
						}
						
						String file = getIncludeName(lp);
						Vector includeLines = loadIncludedFile(includeLine, file, encoding);
						if (includeLines != null)
						{
							Preprocessor includePreprocessor = new Preprocessor(m_logger, m_lineFilter);
							includePreprocessor.setFile(m_file);
							includePreprocessor.setListener(m_listener);
							// make a copy to be sure changes (defines, undefine does not effect including file).
							includePreprocessor.m_defines = m_defines.copy();
							includePreprocessor.preprocess(includeLines, encoding);
							
							for (int k = 0; k < includeLines.size(); k++)
							{
								String s = (String) includeLines.get(k);
								PPLine lp2 = new PPLine(m_file, s, k);
								if (isBlind())
								{
									s = commentLine(lp2);
								}
								lines.insertElementAt(s, i);
								i++;
								m_modified = true;
							}
						}
					}
					else
					{
						handleCommand(lines, lp, ast, eval, encoding,i+1 == lines.size());
					}
				}
			}
			catch (IllegalStateException e)
			{
				throw new PPException(e.getMessage(), m_file, e, i);
			}
			catch (ANTLRException e)
			{
				throw new PPException("Error parsing line : " + line, m_file, e, i);
			}
			i++;

		}
		
		if (m_currentState != STATE_NO_CONDITIONAL)
		{
			throw new PPException("Missing #endif", m_file, -1);
		}
		
		if (m_insideHiddenMdebugBlock)
		{
			throw new PPException("Missing #enddebug", m_file, m_currentMdebugBlockStart);
		}

		return m_modified;
	}

	
    /**
     * Returns a literal pattern <code>String</code> for the specified
     * <code>String</code>.
     *
     * <p>This method produces a <code>String</code> that can be used to
     * create a <code>Pattern</code> that would match the string
     * <code>s</code> as if it were a literal pattern.</p> Metacharacters
     * or escape sequences in the input sequence will be given no special
     * meaning.
     *
     * @param  s The string to be literalized
     * @return  A literal string replacement
     * taken from jdk 1.5
     */
    public static String regExpQuote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }
    
    
	private String toTemplate(String line)
	{
		// replace %VARIABLE% macors with regexp to match them (.*)
		return "\\s*"+regExpQuote(line.trim()).replaceAll("%.*%", "\\\\E\\.\\*\\\\Q") + "\\s*";
	}

	/**
	 * an ugly function
	 * @throws TokenStreamException 
	 * @throws RecognitionException 
	 */
	private String getIncludeName(PPLine line) throws RecognitionException, TokenStreamException
	{
		String text = line.getText();
		text = text.substring("include".length()).trim();
		return text;
	}

	private String getExpandLine(PPLine line)
	{
		String prevLine = line.getText();
		String tok = APPParser._tokenNames[APPParser.LITERAL_expand];
		tok = tok.substring(1, tok.length() - 1); // strip quotes
		prevLine = prevLine.substring(tok.length());
		if (prevLine.charAt(0) == ' ' || prevLine.charAt(0) == '\t')
		{
			prevLine = prevLine.substring(1);
		}
		return line.getSpace() + prevLine;
	}
	
	private CommonAST getAST(PPLine lp) throws PPException
	{
		String s = lp.getText();
		// special hack for expand and for include
		if (s.startsWith("expand"))
		{
			CommonAST ast = new CommonAST(new CommonToken(APPLexer.LITERAL_expand, s));
			return ast;
		}
		if (s.startsWith("include"))
		{
			CommonAST ast = new CommonAST(new CommonToken(APPLexer.LITERAL_include, s));
			return ast;
		}
		else
		{
			APPLexer lexer = new APPLexer(new java.io.StringReader(lp.getSource()));
			APPParser parser = new APPParser(lexer);
			parser.setASTNodeClass(PPLineAST.class.getName());
			try
			{
				parser.line();
			}
			catch (ANTLRException e)
			{
				throw new PPException("Error parsing " + lp.getSource(), lp.getFileName(), e, lp.getLineNumber());
			}
			CommonAST ast = (CommonAST) parser.getAST();
			// skip comment prefix.
			ast = (CommonAST) ast.getNextSibling();
			return ast;
		}
	}

	private Vector loadIncludedFile(int lineNum, String file, String encoding) throws PPException 
	{
		File f;
		if (new File(file).isAbsolute())
		{
			f = new File(file);
		}
		else
		{
			File parent = m_file.getParentFile();
			f = new File(parent, file);
		}
		if (!f.exists()) throw new PPException("File not found : " + f,m_file, lineNum);
		Vector v = new Vector();
		try
		{
			loadStrings(v, new FileInputStream(f), encoding);
			return v;
		}
		catch (IOException e)
		{
			throw new PPException("Error loading include file " + file, m_file, e, lineNum);
		}
		
	}

	private void pushState()
	{
		m_statsStack.push(new Integer(m_currentState));
	}

	private void popState()
	{
		m_currentState = ((Integer) m_statsStack.pop()).intValue();
	}

	public boolean isBlind()
	{
		return m_currentState == STATE_CAN_BECOME_TRUE || m_currentState == STATE_HAS_BEEN_TRUE
				|| m_disabledByCondition || m_debugHideNextLine || m_insideHiddenMdebugBlock;
	}

	public boolean isVerbose()
	{
		return m_verbose;
	}

	public void setVerbose(boolean verbose)
	{
		m_verbose = verbose;
	}

	String commentLine(PPLine lp)
	{
		if (lp.getType() == PPLine.TYPE_VISIBLE || (lp.prefixChar() != PPLine.HIDDEN_LINE_COMMENT_CHAR && lp.getType() != PPLine.TYPE_COMMAND))
		{
			return "//" + PPLine.HIDDEN_LINE_COMMENT_CHAR + lp.getSpace() + lp.getText();
		}
		else
		{
			return lp.getSource();
		}
	}

	String uncommentLine(PPLine lp)
	{
		return lp.getSpace() + lp.getText();
	}

	/**
	 * Handles a new "IF"-like command. The old state is pushed on the stack, a new "scope" is entered.
	 */
	private void handleIf(boolean condition)
	{
		pushState();
		if (!isBlind())
		{
			if (condition)
			{
				m_currentState = STATE_IS_TRUE;
			}
			else
			{
				m_currentState = STATE_CAN_BECOME_TRUE;
			}
		}
		else
		{
			m_currentState = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleElseIf(boolean condition)
	{
		if (m_currentState == STATE_NO_CONDITIONAL)
		{
			throw new IllegalStateException("Unexpected #elif");
		}
		else if (m_currentState == STATE_CAN_BECOME_TRUE)
		{
			if (condition)
				m_currentState = STATE_IS_TRUE;
		}
		else if (m_currentState == STATE_IS_TRUE)
		{
			m_currentState = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleElse()
	{
		if (m_currentState == STATE_NO_CONDITIONAL)
		{
			throw new IllegalStateException("Unexpected #else");
		}
		else if (m_currentState == STATE_CAN_BECOME_TRUE)
		{
			m_currentState = STATE_IS_TRUE;
		}
		else if (m_currentState == STATE_IS_TRUE)
		{
			m_currentState = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleEndIf()
	{
		if (m_currentState == STATE_NO_CONDITIONAL)
		{
			throw new IllegalStateException("Unexpected #endif");
		}
		else
		{
			popState();
		}
	}

	private void handleCommand(Vector lines, PPLine ppl, AST ast, CommandEvaluator evaluator, String encoding, boolean lastLine) throws ANTLRException,
			PPException, UnsupportedEncodingException
	{
		if (isVerbose())
		{
			log("(?)" + ppl.getSource());
		}

		int type = ast.getType();

		switch (type)
		{
			case APPParser.LITERAL_define:
			case APPParser.LITERAL_undefine:
			{
				if (!isBlind())
				{
					evaluator.evaluate(ppl, ast, m_listener);
				}
			}
				break;
			case APPParser.LITERAL_if:
			case APPParser.LITERAL_ifdef:
			case APPParser.LITERAL_ifndef:
			{
				boolean r = evaluator.evaluate(ppl, ast, m_listener);
				handleIf(r);
			}
				break;
			case APPParser.LITERAL_condition:
			{
				if (ppl.getLineNumber() != 0) throw new PPException("//#condition is only allowed in the first line of the file", m_file, ppl.getLineNumber());
				boolean r = evaluator.evaluate(ppl, ast, m_listener);
				handleCondition(r);
			}
				break;
			case APPParser.LITERAL_elif:
			case APPParser.LITERAL_elifdef:
			case APPParser.LITERAL_elifndef:
			{
				boolean r = evaluator.evaluate(ppl, ast, m_listener);
				handleElseIf(r);
			}
				break;
			case APPParser.LITERAL_else:
				handleElse();
				break;
			case APPParser.LITERAL_endif:
				handleEndIf();
				break;
			case APPParser.LITERAL_debug:
			{
				boolean show = evaluator.evaluate(ppl, ast, m_listener);
				m_debugHideNextLine = !show;
			}
				break;
			case APPParser.LITERAL_mdebug:
				boolean show = evaluator.evaluate(ppl, ast, m_listener);
				handleMdebug(show, ppl.getLineNumber());
			break;
			case APPParser.LITERAL_enddebug:
				handleEnddebug();
				break;
			case APPParser.LITERAL_expand:
				handleExpand(ppl, lines);
				break;
			default:
				throw new PPException("Unexpected token " + APPParser._tokenNames[type] + " at \"" + ppl.getSource() + "\"", m_file,
						ppl.getLineNumber());

		}
		
		if (type != APPParser.LITERAL_debug)
		{
			// reset the debug hide
			m_debugHideNextLine = false;
		}
	}

	private void handleExpand(PPLine ppl, Vector lines)
	{
		String expLine = getExpandLine(ppl);
		String template = toTemplate(expLine);
		int nextIndex = ppl.getLineNumber() + 1;
		String str = lines.size() > nextIndex ? (String)lines.get(nextIndex) : "";
		PPLine nextPPline = new PPLine(m_file, str, nextIndex);
		
		String nextLine = uncommentLine(nextPPline);
		boolean replace = nextLine.matches(template);
		
		String expanded = Expander.expandMacros(expLine, m_defines);
		if (!nextLine.equals(expanded))
		{
			m_modified = true;
		}
		
		if (replace)
		{
			lines.set(nextIndex, expanded);
		}
		else
		{
			lines.insertElementAt(expanded, nextIndex);
		}		
	}

	private void handleEnddebug()
	{
		m_currentMdebugBlockStart = -1;
		m_insideHiddenMdebugBlock = false;
	}

	private void handleMdebug(boolean show, int lineNumber)
	{
		m_currentMdebugBlockStart = lineNumber;
		m_insideHiddenMdebugBlock = !show;
	}


	private void handleCondition(boolean conditionTrue)
	{
		m_disabledByCondition = !conditionTrue;
	}

	/**
	 * A small abstraction away from the actual loggin method.
	 */
	public static interface ILogger
	{
		public void log(String message);
	}

	/**
	 * A small abstraction away from the line filter.
	 * line filters may modify a line before its being preprocessed. this can be useful to 
	 * expand some macroes like ${VERSION} for example.
	 */
	public static interface ILineFilter
	{
		public String filter(String line);
	}

	public Defines getDefines()
	{
		return m_defines;
	}
	

	public void clearDefines()
	{
		m_defines.clear();
	}

}
