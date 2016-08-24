package de.pleumann.antenna.device;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Group extends BaseProps
{
	protected String m_name;
	private String m_parent;
	
	public Group(Element dev)
	{
		super(dev);
		NodeList props = dev.getChildNodes();
		for (int i = 0; i < props.getLength(); i++)
		{
			Node item = props.item(i);
			if (item instanceof Element)
			{				
				Element e = (Element) item;
				String tagName = e.getTagName();
				if ("name".equals(tagName))
				{
					m_name = Util.getText(e);
				}
				else
				if ("parent".equals(tagName))
				{
					m_parent = Util.getText(e);
				}
			}
		}		
	}

	public String getName()
	{
		return m_name;
	}
	
	public String getParent()
	{
		return m_parent;
	}

	public Properties constructCapabilities(Hashtable groups, Hashtable capTable)
	{
		Properties base = new Properties();
		if (m_parent != null)
		{
			Group parentGroup = (Group) groups.get(m_parent.toLowerCase());
			if (parentGroup != null)
			{
				base = parentGroup.constructCapabilities(groups, capTable);
			}
		}
		
		Util.addCapabilities(base, m_capabilities, capTable);
		return base;
	}

	public Set constructFeatureSet(Hashtable groups)
	{
		Set base = new TreeSet();
		if (m_parent != null)
		{
			Group parentGroup = (Group) groups.get(m_parent);
			if (parentGroup != null)
			{
				base = parentGroup.constructFeatureSet(groups);
			}
		}

		base.addAll(m_features);
		return base;
	}
		
}
