package de.pleumann.antenna.device;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * omry Jul 25, 2006
 */
public class Device extends Task
{

	private String m_name;

	private String m_result;
	
	/**
	 * one of:
	 * has_capability
	 * get_capability
	 * in_group
	 * has_capability
	 * support_sound
	 * support_video
	 * support_package
	 * has_bug
	 * translate
	 */
	private String m_op;
	
	private String m_key;
	
	
	public Device()
	{
	}
	
	
	public void execute() throws BuildException
	{
		if (m_result == null)
		{
			throw new BuildException("result property is not set");
		}

		if (m_name == null)
		{
			throw new BuildException("name property is not set");
		}

		if (m_op == null)
		{
			throw new BuildException("op property is not set");
		}
		
		if (m_key == null)
		{
			throw new BuildException("key property not set");
		}
		
		DeviceProps deviceProps = Devices.getDevice(m_name);
		if (deviceProps == null) 
		{
			throw new BuildException("Unsupported device \"" + m_name + "\"");
		}
		
		if (m_op.equalsIgnoreCase("has_capability"))
		{
			getProject().setProperty(m_result, "" + deviceProps.hasCapability(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("get_capability"))
		{
			String capability = deviceProps.getCapability(m_key);
			getProject().setProperty(m_result, capability == null ? "" : capability);
		}
		else
		if (m_op.equalsIgnoreCase("in_group"))
		{
			getProject().setProperty(m_result, "" + deviceProps.inGroup(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("has_capability"))
		{
			getProject().setProperty(m_result, "" + deviceProps.hasCapability(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("support_sound"))
		{
			getProject().setProperty(m_result, "" + deviceProps.supportSound(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("support_video"))
		{
			getProject().setProperty(m_result, "" + deviceProps.supportVideo(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("support_package"))
		{
			getProject().setProperty(m_result, "" + deviceProps.supportsPackage(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("has_bug"))
		{
			getProject().setProperty(m_result, "" + deviceProps.hasBug(m_key));
		}
		else
		if (m_op.equalsIgnoreCase("translate"))
		{
			getProject().setProperty(m_result, Util.expandMacros(m_key, deviceProps.getCapabilities()));
		}
		else
			throw new BuildException("Unsupported operation : " + m_op);
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public void setResult(String result)
	{
		m_result = result;
	}

	public void setOp(String op)
	{
		String cap[] = new String[]
		{
			"has_capability", "get_capability", "in_group",
			"has_capability", "support_sound", "support_video",
			"support_package", "has_bug","translate"
		};
		for (int i = 0; i < cap.length; i++)
		{
			if (op.equals(cap[i]))
			{
				m_op = op;
				return;
			}
		}
		
		throw new BuildException("Unsupported operation \""+ op+"\" , should be one of \n" + toString(cap));
	}

	private String toString(String[] arr)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; i++)
		{
			buf.append(arr[i]);
			if (i < arr.length - 1)
			{
				buf.append("|");
			}
		}
		return buf.toString();
	}

	public void setKey(String key)
	{
		m_key = key;
	}
}
