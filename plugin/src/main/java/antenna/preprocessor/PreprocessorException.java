/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java 
 *
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package antenna.preprocessor;

import java.io.File;



/**
 * Provides a specialized exception type for use inside the preprocessor.
 */
public class PreprocessorException extends Exception {


	public static final int UNKNOWN_LINE = -1;
	
	private final int m_lineNumber;
	private File m_file;
	
	public PreprocessorException(String message)
	{
		this(message, null, null);
	}

	public PreprocessorException(String message, Throwable cause)
	{
		this(message, null, cause);
	}
	
	public PreprocessorException(String message, File file)
	{
		this(message, file, null, UNKNOWN_LINE);
	}

	public PreprocessorException(String message, File file, int lineNumber)
	{
		this(message,file,  null, lineNumber);
	}
	
	public PreprocessorException(String message, File file, Throwable cause)
	{
		this(message, file, cause, UNKNOWN_LINE);
	}
	public PreprocessorException(String message, File file, Throwable cause, int lineNumber)
	{
		super((lineNumber != UNKNOWN_LINE ? "Line " + lineNumber + " : " : "") + message, cause);
		m_lineNumber = lineNumber;
		m_file = file;
	}

	public int getLineNumber()
	{
		return m_lineNumber;
	}
	
	public File getFile()
	{
		return m_file;
	}
	
	public String getMessage()
	{
		if (m_file != null)
		{
			String ln = m_lineNumber != UNKNOWN_LINE ? ":" + m_lineNumber : "";
			return m_file + ln + " : " + super.getMessage();	
		}
		else
		{
			if (m_lineNumber != UNKNOWN_LINE)
			{
				return m_lineNumber + " : " + super.getMessage();
			}
			else
			{
				return super.getMessage();
			}
		}
		
	}
}
