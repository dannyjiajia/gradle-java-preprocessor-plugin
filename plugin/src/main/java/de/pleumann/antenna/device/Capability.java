package de.pleumann.antenna.device;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Capability
{
	String m_identifier;
	String m_extention_mode;
	String m_group;
	boolean m_required = false;
	String m_type;
	String m_implicitGroup;
	
	public Capability(Element dev)
	{
		NodeList props = dev.getChildNodes();
		for (int i = 0; i < props.getLength(); i++)
		{
			Node item = props.item(i);
			if (item instanceof Element)
			{				
				Element e = (Element) item;
				String tagName = e.getTagName();
				String text = Util.getText(e);
				if ("identifier".equals(tagName))
				{
					m_identifier = text;
				}
				else
				if ("extension-mode".equals(tagName))
				{
					m_extention_mode = text;
				}
				else
				if ("group".equals(tagName))
				{
					m_group = text;
				}
				else
				if ("required".equals(tagName))
				{
					m_required = "yes".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text);
				}
				else
				if ("type".equals(tagName))
				{
					m_type = text;
				}
				else
				if ("implicit-group".equals(tagName))
				{
					m_implicitGroup = text;
				}
			}
		}		
	}

	public String getIdentifier()
	{
		return m_identifier;
	}
	
	public boolean extendByOverwrite()
	{
		return "overwrite".equalsIgnoreCase(m_extention_mode);
	}
	
	public boolean extendByAppend()
	{
		return "append".equalsIgnoreCase(m_extention_mode);
	}
	
}
