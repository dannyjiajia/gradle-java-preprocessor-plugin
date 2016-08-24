package antenna.preprocessor.v2.parser;



/**
 * omry 
 * 06/02/2007
 */
public class Define
{
	public final String m_key;
	public final Literal m_value;
	
	public Define(String key)
	{
		m_key = key;
		m_value = null;
	}
	
	public Define(String key, Literal value)
	{
		m_key = key;
		m_value = value;
	}
	
	public String toString()
	{
		if (m_value !=  null)
		{
			return m_key + "=" + m_value;
		}
		else
		{
			return m_key;
		}
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof Define)
		{
			Define d = (Define) obj;
			if (!m_key.equals(d.m_key)) return false;
			Literal l1 = m_value;
			Literal l2 = d.m_value;
			return (l1.equals(l2)); 
		}
		return false;
	}
	
}
