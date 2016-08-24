package antenna.preprocessor.v2.parser;


/**
 * omry 
 * 06/02/2007
 */
public class Literal
{
	public static final int NUMBER = APPLexerTokenTypes.NUMBER;
	public static final int STRING = APPLexerTokenTypes.STRING;
	public static final int SYMBOL = APPLexerTokenTypes.SYMBOL;
	public static final int BOOLEAN = -1;
	public static final int DEBUG_LEVEL = -2;
	
	private int m_type;
	private String m_value;
	
	public Literal(int type, String value)
	{
		m_type = type;
		switch (type)
		{
			case SYMBOL:
				break;
			case NUMBER:
				Double.parseDouble(value);
				break;
			case STRING:
				if (value.toLowerCase().equals("false") || value.toLowerCase().equals("true"))
				{
					m_type = BOOLEAN;
				}
				else
				{
					try
					{
						Double.parseDouble(value);
						m_type = NUMBER;
					}
					catch (NumberFormatException e){}
				}
				break;
			case APPLexerTokenTypes.LITERAL_false:
			case APPLexerTokenTypes.LITERAL_true:
			case BOOLEAN:
				m_type = BOOLEAN;
				if (!value.toLowerCase().equals("false") && !value.toLowerCase().equals("true")) throw new IllegalArgumentException("Invalid boolean value");
				value = value.toLowerCase();
				break;
			case APPLexerTokenTypes.LITERAL_debug:
			case APPLexerTokenTypes.LITERAL_info:
			case APPLexerTokenTypes.LITERAL_warn:
			case APPLexerTokenTypes.LITERAL_error:
			case APPLexerTokenTypes.LITERAL_fatal:
			case DEBUG_LEVEL:
				m_type = DEBUG_LEVEL;
				break;
			default:
				throw new IllegalArgumentException("unsupported type " + type + " for value " + value);
		}
		m_value = value;
	}
	
	public String toString()
	{
		switch (m_type)
		{
			case STRING:
				return "\""+m_value+"\"";
			case NUMBER:
			case BOOLEAN:
			case SYMBOL:
			case DEBUG_LEVEL:
			default:
				return m_value;
			
		}
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof Literal)
		{
			Literal other = (Literal) obj;
			return other.m_type == m_type && other.m_value.equals(m_value);
		}
		return false;
	}

	public boolean isFalse()
	{
		return m_type == BOOLEAN && m_value.equals("false");
	}

	public boolean isTrue()
	{
		return m_type == BOOLEAN && m_value.equals("true");
	}
	
	public String getValue()
	{
		return m_value;
	}
	
	public boolean isDebugLevel()
	{
		return m_type == DEBUG_LEVEL;
	}
	
	public boolean isNumber()
	{
		return m_type == NUMBER;
	}
	
	public boolean isString()
	{
		return m_type == STRING;
	}
	
	public boolean isSymbol()
	{
		return m_type == SYMBOL;
	}
	
	public boolean isBoolean()
	{
		return m_type == BOOLEAN;
	}
	
}
