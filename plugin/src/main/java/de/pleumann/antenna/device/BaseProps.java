package de.pleumann.antenna.device;

import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BaseProps implements Cloneable
{
	protected static final String CAPABILITY = "capability";
	protected static final String GROUPS = "groups";
	protected static final String FEATURES = "features";
	protected static final String IDENTIFIER = "identifier";
	protected static final String IMEI_KEY = "IMEI_KEY";

	protected Set m_features;
	protected Set m_soundFormats;
	protected Set m_videoFormats;
	protected Set m_javaPackage;
	protected Set m_bugs;
	protected Properties m_capabilities;
	
	public BaseProps(Element dev)
	{
		m_features = new TreeSet();
		m_soundFormats = new TreeSet();
		m_javaPackage = new TreeSet();
		m_videoFormats = new TreeSet();
		m_bugs = new TreeSet();
		m_capabilities = new Properties();
		
		parseBase(dev);
	}

	public void parseBase(Element dev)
	{
		NodeList props = dev.getChildNodes();
		for (int i = 0; i < props.getLength(); i++)
		{
			Node item = props.item(i);
			if (item instanceof Element)
			{				
				Element e = (Element) item;
				String tagName = e.getTagName();
				if (FEATURES.equals(tagName))
				{
					parseGroup(e, m_features);
				}
				else
				if (CAPABILITY.equals(tagName))
				{
					String name = e.getAttribute("name");
					String value = e.getAttribute("value");
					m_capabilities.setProperty(name.trim(), value.trim());
				}
			}
		}
	}
	
	protected void parseGroup(Element e, Set outputSet)
	{
		String text = Util.getText(e);
		StringTokenizer tok = new StringTokenizer(text, ", ");
		while(tok.hasMoreElements()) 
		{
			String item = tok.nextToken();
			outputSet.add(item.toLowerCase().trim());
		}
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
