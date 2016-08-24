package antenna.preprocessor.v2;

import java.io.File;


/**
 * Represents a single line of source code to be handled by the preprocessor. The class provides methods to find out if it is a preprocessor directive and, if so, which one.
 */
public class PPLine
{
	public static final char HIDDEN_LINE_COMMENT_CHAR = '@';
	
	/**
	 * Represents a normal line of code that is currently visible.
	 */
	public static final String TYPE_VISIBLE = "VISIBLE";

	/**
	 * Represents a normal line of code that is currently hidden, that is, commented out using "//#".
	 */
	public static final String TYPE_HIDDEN = "HIDDEN";

	/**
	 * Represents a line that holds any "//#command" statement.
	 */
	public static final String TYPE_COMMAND = "COMMAND";

	/**
	 * Holds the original line of source.
	 */
	private String m_source;

	/**
	 * Holds the line type, which is represented by one of the above constants.
	 */
	private String m_type;

	/**
	 * If the original line started with one or more spaces or tabs, these are stored here.
	 */
	private String m_spaces;

	/**
	 * Holds the directive contained in the line, or the Java code, in case it is a normal source line.
	 */
	private String m_text;

	private final int m_lineNumber;

	private final File m_fileName;

	private char m_prefixChar;
	
	
	public PPLine(String s)
	{
		this(null, s, -1);
	}
	
	/**
	 * Creates a new preprocessor line, automatically analyzing the given source line.
	 */
	public PPLine(File fileName, String s, int lineNumber) 
	{
		m_fileName = fileName;
		m_lineNumber = lineNumber;
		processLine(s);
	}

	private void processLine(String s) 
	{
		// keep original text
		m_source = s;

		String ws[] = getWhites(s);
		// keep whitespace and strip it from the string
		m_spaces = ws[0];
		s = ws[1];
		
		
		// default to visible line, in case we can't match the type.
		m_type = TYPE_VISIBLE;
		m_text = s;
		m_prefixChar = '?';
		// check the prefix.
		if (s.startsWith("//"))
		{
			s = s.substring(2);
			ws = getWhites(s);
			s = ws[1];
			if (s.length() > 0)
			{
				char c = s.charAt(0);
				s = s.substring(1);
				// maintain backward compatiblity with //$ and //#
				if (c == '#' || c == '$' || c == HIDDEN_LINE_COMMENT_CHAR)
				{
					m_prefixChar = c;
					if (c == HIDDEN_LINE_COMMENT_CHAR || c == '$' || s.length() == 0 || (s.charAt(0) == ' ' || s.charAt(0) == '\t'))
					{
						m_type = TYPE_HIDDEN;
					}
					else
					{
						m_type = TYPE_COMMAND;
					}
					m_text = s;
				}
			}
		}
	}

	public String toString()
	{
		return m_type + "[" + m_source + "]";
	}

	private String[] getWhites(String s)
	{
		int p = 0;
		while (p < s.length())
		{
			char c = s.charAt(p);
			if ((c != ' ') && (c != '\t'))
				break;
			p++;
		}
		String white = s.substring(0, p);
		String text = s.substring(p);
		return new String[]{white, text};
	}

	public String getSource()
	{
		return m_source;
	}

	public String getSpace()
	{
		return m_spaces;
	}

	public String getText()
	{
		return m_text;
	}

	public String getType()
	{
		return m_type;
	}

	public int getLineNumber()
	{
		return m_lineNumber;
	}

	public File getFileName()
	{
		return m_fileName;
	}
	
	public char prefixChar()
	{
		return m_prefixChar;
	}

}
