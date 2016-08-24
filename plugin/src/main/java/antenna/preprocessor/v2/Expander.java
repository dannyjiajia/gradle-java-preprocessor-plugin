package antenna.preprocessor.v2;

import antenna.preprocessor.v2.parser.Define;
import antenna.preprocessor.v2.parser.Defines;

public class Expander
{
	/**
	 * replace defines wrapped in ${define} with their value. 
	 */
    public static String expandMacros(String text, Defines defines)
	{
		int macroStart = -1;
		macroStart = text.indexOf('%', macroStart + 1);
		while (macroStart != -1)
		{
			int macroEnd = text.indexOf('%', macroStart + 1);
			if (macroEnd == -1)
			{
				return text;
			} else
			{
				String macro = text.substring(macroStart + 1, macroEnd);
				Define define = defines.getDefine(macro);
				if (define != null && define.m_value != null)
				{
					String value = define.m_value.getValue();
					String macro2 = "%" + macro + "%";
					int delta = value.length() - macro2.length();
					text = replaceAll(text, macro2, value);
					macroEnd += delta;
					macroStart += delta;
				}
			}

			macroStart = text.indexOf("%", macroEnd + 1); // next macro
		}

		return text;
	}
    
    private static String replaceAll(String where, String what, String with)
    {
    	return where.replaceAll(what, with);
    }
    
}
