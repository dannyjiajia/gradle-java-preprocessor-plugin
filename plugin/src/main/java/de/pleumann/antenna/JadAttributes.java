package de.pleumann.antenna;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.StringInputStream;

/**
 * @author omry
 * Created on Nov 22, 2004
 */
public class JadAttributes extends Task
{
	boolean m_failOnError = true;
	String m_attribName;
	String m_key;
	String m_value;
	String m_fileName;
	
	String m_clear;
	
	public JadAttributes()
	{
	}
	
	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName)
	{
		m_fileName = fileName;
	}
	
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key)
	{
		m_key = key;
	}
	
	/**
	 * @param attribName The attribName to set.
	 */
	public void setAttribName(String attribName)
	{
		m_attribName = attribName;
	}
	
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value)
	{
		m_value = value;
	}
	
	
	/**
	 * if Clear is set to "true", the Attribues object with 
	 * the specified name is cleared from memory.
	 */
	public void setClear(String value)
	{
		m_clear = value;
	}
	
	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException
	{
		if (m_attribName == null)
		{
			throw new BuildException("Missing attrib name");
		}

		// if clear, clear the attribures.
		if ("true".equalsIgnoreCase(m_clear))
		{
			log("Clearing attributes for " + m_attribName);
			Object old = s_jad2attribMap.remove(m_attribName);
			if (old == null && m_failOnError)
			{
				throw new BuildException(m_attribName + " not found");
			}
		}
		else // add attributes.
		{
			// get attributes object, or create a new one.
			AttributesSet attribues = (AttributesSet) s_jad2attribMap.get(m_attribName);
			if (attribues == null)
			{
				log("Creating a new attributes set : " + m_attribName);
				s_jad2attribMap.put(m_attribName, attribues = new AttributesSet(m_attribName));
			}
			
			if (m_fileName == null) // file is null, we need key and value
			{
				if (m_key == null)
				{
					throw new BuildException("Missing key name");
				}
				if (m_value == null)
				{
					throw new BuildException("Missing value name");
				}
				
				attribues.addPair(m_key, m_value);
			}
			else // we have a file, load it.
			{
				try
				{
					File file = new File(m_fileName);
					if (file.exists())
					{
						String pairs[][] = getPairs(file, "#", m_encoding);
						log("Loaded properties from " + file);
						for (int i = 0; i < pairs.length; i++)
						{
							String key = pairs[i][0];
							String value = pairs[i][1];
							attribues.addPair(key, value);
						}
					}
					else
					{
						String msg = "Missing propertis file " + file + "( not found at " + file.getAbsolutePath() + ")";
						if (m_failOnError)
						{
							throw new BuildException(msg);
						}
						else
						{
							log(msg);	
						}
					}
				}
				catch (IOException e)
				{
					if (m_failOnError)
					{
						throw new BuildException(e);	
					}
					else
					{
						log("IO Error reading " + m_fileName + ", " + e.getMessage());	
					}
				}
			}
		}
	}
	
	/**
	 * A hashtable contains mapping between jad name to an Attributes object
	 */
	static Hashtable s_jad2attribMap = new Hashtable();
	
	private String m_encoding;
	
	private class AttributesSet
	{
		String m_jadName;
		Vector m_pairs = new Vector();
		
		public AttributesSet(String jadName)
		{
			m_jadName = jadName;
		}
		
		public void addPair(String key, String value)
		{
			key = getProject().replaceProperties(key);			
			String value2 = getProject().replaceProperties(value);
			if (value.equals(value2)) // not resolved
			{
				log(m_attribName + " <- " + key + "=" + value);
			}
			else // resoleved
			{
				log(m_attribName + " <- " + key + "=" + value2 + " (was " + value + ")");
			}
			m_pairs.addElement(new String[]{key, value2});
		}
	}
	
	public static boolean hasAttributesFor(String jadName)
	{
		return s_jad2attribMap.containsKey(jadName);
	}
	
	public static String[][] getAttributesFor(WtkJad caller, String jadName) 
	{
		AttributesSet attribues = (AttributesSet) s_jad2attribMap.get(jadName);
		if (attribues == null)
		{
			caller.log("No JAD Attributes stored in JadAttributes for " + jadName);
			return new String[0][2];
		}
		
		String[][] pairs = new String[attribues.m_pairs.size()][2];
		attribues.m_pairs.copyInto(pairs);

		return pairs;
	}
	
    private static String[][] getPairs(File file, String commentPrefix, String encoding)
                             	throws IOException
    {
		InputStream in = null;

		try
		{
			return getPairs(in = new FileInputStream(file), commentPrefix, encoding);
		}
		finally
		{
			if (in != null)
			{
				in.close();
			}
		}
    }
    
    /**
     * if failOnError is true, build fails when 
     * 1. a properties file to load is missing or could not be read.
     * 2. clear=true, and the attrib set name was not found.
     * defaults to true.
	 */
	public void setFailOnError(boolean failOnError)
	{
		m_failOnError = failOnError;
	}
    
    /**
     * like sun's properties, except don't scramble the order.
     */
	public static String[][] getPairs(InputStream input, String commentPrefix, String encoding) throws IOException
	{
		Vector result = new Vector();
		BufferedReader in;
		if (encoding == null)
		{
			in = new BufferedReader(new InputStreamReader(input));
		}
		else
		{
			in = new BufferedReader(new InputStreamReader(input, encoding));	
		}
		
		
		String line;

		boolean contOnNext = false;
		while ((line = in.readLine()) != null)
		{
			int indexOfComment = line.indexOf(commentPrefix);
			// strip comment
			if (indexOfComment != -1)
			{
				line = line.substring(0, indexOfComment).trim();
			}
			
			if (line.length() > 0)
			{
				int sepIndex = getSepIndex(line);
				
				boolean willContOnNext = (line.endsWith("\\"));
				if (willContOnNext) 
				{
					line = line.substring(0, line.length() - 1);
				}
				
				if (contOnNext)
				{
					String last[] = (String[]) result.get(result.size()-1);
					last[1] = last[1] + line;
					result.set(result.size()-1, last);
				}
				else
				if (sepIndex != -1)					
				{
					String key = loadConvert(line.substring(0, sepIndex));
					String value = loadConvert(line.substring(sepIndex + 1));
					result.addElement(new String[]{key, value});
				}
				contOnNext = willContOnNext;
			}
		}

		String[][] pairs = new String[result.size()][2];
		result.copyInto(pairs);

		return pairs;
	}
	
    private static int getSepIndex(String line)
	{
		int sepIndex1 = line.indexOf('=');
		int sepIndex2 = line.indexOf(':');
		if (sepIndex1 == -1 && sepIndex2 == -1) return -1;
		int sepIndex = 0;
		if (sepIndex1 != -1 && sepIndex2 != -1)
		{
			sepIndex = Math.min(sepIndex1, sepIndex2);
		}
		else
		{
			if (sepIndex1 == -1) sepIndex = sepIndex2;
			else sepIndex = sepIndex1;
		}    	
		return sepIndex;
	}

	private static String loadConvert(String theString)
	{
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);

		for (int x = 0; x < len;)
		{
			aChar = theString.charAt(x++);
			if(aChar == '\\')
			{
				aChar = theString.charAt(x++);
				if(aChar == 'u')
				{
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++)
					{
						aChar = theString.charAt(x++);
						switch (aChar)
						{
							case '0' :
							case '1' :
							case '2' :
							case '3' :
							case '4' :
							case '5' :
							case '6' :
							case '7' :
							case '8' :
							case '9' :
								value = (value << 4) + aChar - '0';
							break;
							case 'a' :
							case 'b' :
							case 'c' :
							case 'd' :
							case 'e' :
							case 'f' :
								value = (value << 4) + 10 + aChar - 'a';
							break;
							case 'A' :
							case 'B' :
							case 'C' :
							case 'D' :
							case 'E' :
							case 'F' :
								value = (value << 4) + 10 + aChar - 'A';
							break;
							default :
								throw new IllegalArgumentException(
										"Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				}
				else
				{
					if(aChar == 't')
						aChar = '\t';
					else if(aChar == 'r')
						aChar = '\r';
					else if(aChar == 'n')
						aChar = '\n';
					else if(aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			}
			else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	
	/**
	 * Sets the input file encoding, optional.
	 * @param encoding
	 */
	public void setEncoding(String encoding)
	{
		m_encoding = encoding;
	}
}