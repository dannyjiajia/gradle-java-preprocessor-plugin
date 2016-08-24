package de.pleumann.antenna.device;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * omry 
 * Aug 2, 2006
 */
public class Util
{
    public static String expandMacros(String text, Properties macros)
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
				String value = macros.getProperty(macro);
				if (value != null)
				{
					String macro2 = "%" + macro + "%";
					int delta = value.length() - macro2.length();
					text = replaceAll(text, macro2, value);
					macroEnd += delta;
					macroStart += delta;
				}
			}

			macroStart = text.indexOf('%', macroEnd + 1); // next macro
		}

		return text;
	}
    
    public static String replaceAll(String where, String what, String with)
    {
    	return where.replaceAll(what, with);
    }

	public static String getText(Element e)
	{
		NodeList childNodes = e.getChildNodes();
		if (childNodes.getLength() > 0)
		{
			return childNodes.item(0).getNodeValue().trim();
		}
		else
		{
			return "";
		}
	}

	static void addCapabilities(Properties base, Properties addition, Hashtable capTable)
	{
		Enumeration keys = addition.keys();
		while (keys.hasMoreElements())
		{
			String name = (String) keys.nextElement();
			String value = addition.getProperty(name);
			Capability cap = (Capability) capTable.get(name.toLowerCase());
			if (cap != null && cap.extendByAppend())
			{
				String current = base.getProperty(name);
				if (current == null) current = "";
				if (value == null) value = "";
				
				if (current.trim().length() > 0)
				{
					value = current + "," + value;
				}
			}
			base.setProperty(name, value);
		}
	}
    
}
