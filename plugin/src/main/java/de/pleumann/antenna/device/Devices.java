package de.pleumann.antenna.device;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * omry Aug 2, 2006
 */
public class Devices
{
	private static Hashtable s_devices;
	private static Hashtable s_groups;
	private static Hashtable s_capablities;
	// optional base directory to load the database from.
	// this base dir is used as the preferred database location, but missing files will be loaded from the JAR.
	private static String s_baseDir;
	static
	{
		try
		{
			initialize();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * A test main used to parse xml and dump a specific device.
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 1 || !args[0].equalsIgnoreCase("-list") && args.length < 2)
		{
			System.err.println("Usage: java " + Devices.class.getName() + "[-list] | -device DeviceName");
			return;
		}
		
		String command = args[0];
		if ("-list".equalsIgnoreCase(command))
		{
			DeviceProps[] allDevices = getAllDevices();
			for (int i = 0; i < allDevices.length; i++)
			{
				System.out.println(allDevices[i].getIdentifier());
			}
		}
		else
		if ("-device".equalsIgnoreCase(command))
		{
			DeviceProps device = Devices.getDevice(args[1]);
			Properties capabilities = device.getCapabilities();
			Enumeration keys= capabilities.keys();
			while (keys.hasMoreElements())
			{
				String key = (String) keys.nextElement();
				System.out.println(key + "=" + capabilities.getProperty(key));
			}
		}
	}

	// static class, no need to construct.
	private Devices()
	{
	}

	public static DeviceProps getDevice(String deviceName)
	{
		if (deviceName == null)
		{
			return null;
		}
		return (DeviceProps) s_devices.get(deviceName);
	}

	public static DeviceProps[] getAllDevices()
	{
		DeviceProps[] devices = new DeviceProps[s_devices.size()];
		Enumeration enumeration = s_devices.elements();
		int i = 0;
		while (enumeration.hasMoreElements())
		{
			devices[i++] = (DeviceProps) enumeration.nextElement();
		}
		return devices;
	}

	private static void initialize() throws IOException, ParserConfigurationException, SAXException
	{
		synchronized (Devices.class)
		{
			if (s_capablities == null)
			{
				parseCapabilitiesFile();
			}
			
			if (s_groups == null)
			{
				parseGroupsFile();
			}

			if (s_devices == null)
			{
				parseDevicesFile();
			}
		}
	}

	private static InputStream openStream(String cpFile) throws FileNotFoundException, IOException
	{
		File dirs[];
		if (s_baseDir != null)
		{
			dirs = new File[]
			{
				new File(s_baseDir),new File("lib/"), new File("./")
			};
		}
		else
		{
			dirs = new File[]
			{
				new File("lib/"), new File("./")
			};
		}

		
		InputStream in = null;

		for (int i = 0; i < dirs.length; i++)
		{
			File file = new File(dirs[i], cpFile);
			if (file.exists())
			{
				System.out.println("Devices: loading " + file);
				in = new BufferedInputStream(new FileInputStream(file));
				break;
			}
			else
			{
				System.out.println("Devices: " + file.getAbsolutePath() + " not found");
			}
		}

		if (in == null)
		{
			in = new BufferedInputStream(Devices.class.getResourceAsStream("/"+cpFile));
			if (in != null)
			{
				System.out.println("Devices: loading " + cpFile + " from classpath");
			}
			else
			{
				String msg = "Devices: can't find " + cpFile
						+ " in classpath or in file default file system locations (" + toString(dirs) + ")";
				System.out.println(msg);
				throw new IOException(msg);
			}
		}
		return in;
	}

	private static String toString(File[] files)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < files.length; i++)
		{
			sb.append(files[i]);
			if (i < files.length)
				sb.append(",");
		}
		return sb.toString();
	}

	private static void parseGroupsFile() throws IOException, ParserConfigurationException, SAXException
	{
		s_groups = new Hashtable();
		InputStream in = openStream("groups.xml");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				if (node instanceof Element)
				{
					Element e = (Element) node;
					if ("groups".equals(e.getTagName()))
					{
						NodeList groups = e.getChildNodes();
						for (int j = 0; j < groups.getLength(); j++)
						{
							Node node2 = groups.item(j);
							if (node2 instanceof Element)
							{
								Element e1 = (Element) node2;
								if ("group".equals(e1.getTagName()))
								{
									Group group = new Group(e1);
									s_groups.put(group.getName().toLowerCase(), group);
								}
							}
						}
					}
				}
			}
		}
		finally
		{
			in.close();
		}
	}

	private static void parseDevicesFile() throws IOException, ParserConfigurationException, SAXException
	{
		s_devices = new Hashtable();
		parseDevices(openStream("devices.xml"));
		
		try
		{
			// parse custom devices data.
			parseDevices(openStream("custom-devices.xml"));
		}
		catch (IOException e)
		{
			// ignore.
		}		
	}

	private static void parseDevices(InputStream in) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				if (node instanceof Element)
				{
					Element e = (Element) node;
					if ("devices".equals(e.getTagName()))
					{
						NodeList devices = e.getChildNodes();
						for (int j = 0; j < devices.getLength(); j++)
						{
							Node node2 = devices.item(j);
							if (node2 instanceof Element)
							{
								Element e1 = (Element) node2;
								if ("device".equals(e1.getTagName()))
								{
									DeviceProps dev = new DeviceProps(e1);
									dev.setGroupsData(s_groups, s_capablities);
									String ids = dev.getIdentifier();
									StringTokenizer toks = new StringTokenizer(ids, ",");
									boolean clone = toks.countTokens() > 1;
									while(toks.hasMoreTokens())
									{
										String id = toks.nextToken().trim();
										if (s_devices.containsKey(id))
										{
											DeviceProps d = (DeviceProps) s_devices.get(id);
											d.parseBase(e1);
										}
										else
										{
											DeviceProps dev2 = dev;
											if (id.length() > 0)
											{
												if (clone)
												{
													try
													{
														dev2 = (DeviceProps) dev.clone();
														dev2.m_identifier = id;
													}
													catch (CloneNotSupportedException e2)
													{
														throw new RuntimeException(e2);
													}
												}
												s_devices.put(id, dev2);
											}
										}
									}
								}
							}
						}
					}
				}

			}
		}
		finally
		{
			in.close();
		}
	}

	private static void parseCapabilitiesFile() throws IOException, ParserConfigurationException, SAXException
	{
		s_capablities = new Hashtable();
		String cpFile = "capabilities.xml";
		InputStream in = openStream(cpFile);

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			NodeList nodes = document.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				if (node instanceof Element)
				{
					Element e = (Element) node;
					if ("capabilities".equals(e.getTagName()))
					{
						NodeList groups = e.getChildNodes();
						for (int j = 0; j < groups.getLength(); j++)
						{
							Node node2 = groups.item(j);
							if (node2 instanceof Element)
							{
								Element e1 = (Element) node2;
								if ("capability".equals(e1.getTagName()))
								{
									Capability cap = new Capability(e1);
									s_capablities.put(cap.getIdentifier().toLowerCase(), cap);
								}
							}
						}
					}
				}

			}
		}
		finally
		{
			in.close();
		}
	}	
	
	/**
	 * Reload devices list.
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void reload() throws IOException, ParserConfigurationException, SAXException
	{
		synchronized (Devices.class)
		{
			s_groups = null;
			s_capablities = null;
			s_devices = null;
		}
		initialize();
	}
	
	public static void setDatabaseDir(String dir) throws IOException, ParserConfigurationException, SAXException
	{
		s_baseDir = dir;
		reload();
	}
}
