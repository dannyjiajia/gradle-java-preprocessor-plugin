/**
 * 
 */
package de.pleumann.antenna.device;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * omry 
 * Aug 2, 2006
 */
public class DeviceProps extends BaseProps
{
	protected String m_identifier;
	protected Set m_groups;
	 
	public DeviceProps(Element dev)
	{
		super(dev);
		m_groups = new TreeSet();
		
		NodeList props = dev.getChildNodes();
		for (int i = 0; i < props.getLength(); i++)
		{
			Node item = props.item(i);
			if (item instanceof Element)
			{				
				Element e = (Element) item;
				String tagName = e.getTagName();
				if (IDENTIFIER.equals(tagName))
				{
					m_identifier = Util.getText(e);
				}
				else
				if (GROUPS.equals(tagName))
				{
					parseGroup(e, m_groups);
				}
			}
		}
		
		calculateIMEIKey();
		addVendor();
	}
	
	private void addVendor()
	{
		String vendor = "unknown";
		String id = getIdentifier();
		if (id != null)
		{
			int i = id.indexOf('/');
			if (i != -1)
			{
				vendor = id.substring(0, i);
			}
		}
		m_capabilities.setProperty("Vendor", vendor);
	}

	public void addToProperties(Properties props)
	{
		// add capabilities.
		Enumeration cap = m_capabilities.keys();
		while (cap.hasMoreElements())
		{
			String c = (String) cap.nextElement();
			String v = (String) m_capabilities.get(c);
			props.setProperty(c, "'"+v+"'");
		}
		
		props.put(GROUPS, convertSetToString(m_groups));
		props.put(FEATURES, convertSetToString(m_features));
		props.put(m_identifier, "");
	}
	
	private String convertSetToString(Set set)
	{
		StringBuffer b = new StringBuffer();
		for (Iterator iter = set.iterator(); iter.hasNext();)
		{
			String s = (String) iter.next();
			b.append(s);
			if (iter.hasNext())
			{
				b.append(",");
			}
		}
		return b.length() == 0 ? "false" : "'" + b.toString() + "'";
	}

	/**
	 * Adds a virtual property for IMEI key
	 */
	private void calculateIMEIKey()
	{
		String name = null;
		if (m_identifier.toLowerCase().indexOf("sony-ericsson") != -1)
		{
			name = "com.sonyericsson.imei";
		}
		else
		if (m_identifier.toLowerCase().indexOf("motorola") != -1)
		{
			name = "IMEI";
		}
		else
		if (m_identifier.toLowerCase().indexOf("siemens") != -1)
		{
			name = "com.siemens.IMEI";
		}
		else
		if (m_identifier.toLowerCase().indexOf("nokia") != -1)
		{
			if (inGroup("series40"))
			{
				name = "com.nokia.mid.imei";
			}
			else
			{
				name = "com.nokia.IMEI";
			}
		}
		
		if (name == null)
		{
			name = "UNKNOWN IMEI KEY";
		}
		
		m_capabilities.setProperty(IMEI_KEY, name);
	}

	public boolean inGroup(String group)
	{
		return m_groups.contains(group != null ? group.toLowerCase() : null);
	}
	
	public boolean hasFeature(String feature)
	{
		return m_features.contains(feature);
	}
	
	public boolean hasBug(String bug)
	{
		return m_bugs.contains(bug);
	}
	

	public boolean supportSound(String soundFormat)
	{
		return m_soundFormats.contains(soundFormat);
	}

	public boolean supportVideo(String videoFormat)
	{
		return m_videoFormats.contains(videoFormat);
	}
	
	public boolean supportsPackage(String packageName)
	{
		return m_javaPackage.contains(packageName);
	}
	
	public boolean hasCapability(String name)
	{
		return m_capabilities.containsKey(name);
	}
	
	public String getCapability(String name)
	{
		return m_capabilities.getProperty(name);
	}	


	public String getIdentifier()
	{
		return m_identifier;
	}

	public Properties getCapabilities()
	{
		return m_capabilities;
	}

	public String getDefinesString()
	{
		String deviceDefines = "";
		Properties props = new Properties();
		addToProperties(props);
		Enumeration keys = props.keys();
		while(keys.hasMoreElements())
		{
			String key = (String) keys.nextElement();
			String value = props.getProperty(key);
			
			deviceDefines += key;
			if (value.length() > 0)
			{
				deviceDefines += "=" + value + "";
			}
			
			if (keys.hasMoreElements())
			{
				deviceDefines += ",";
			}
		}
		
		return deviceDefines;
	}

	public void setGroupsData(Hashtable groups, Hashtable capTable)
	{
		for (Iterator ii = m_groups.iterator(); ii.hasNext();)
		{
			String groupName = (String) ii.next();
			Group group = (Group) groups.get(groupName.toLowerCase());
			if (group != null)
			{
				Properties capablities = group.constructCapabilities(groups, capTable);
				Util.addCapabilities(capablities, m_capabilities, capTable);
				m_capabilities = capablities;
				
				Set features = group.constructFeatureSet(groups);
				features.addAll(m_features);
				m_features = features;
			}
		}
	}

}