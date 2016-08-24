package antenna.preprocessor.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.Project;

import antenna.preprocessor.IPreprocessor;
import antenna.preprocessor.PreprocessorException;
import de.pleumann.antenna.misc.Strings;
import de.pleumann.antenna.misc.Utility;

/**
 * @author omry
 */
public class PreprocessorBridge implements IPreprocessor
{
	private Preprocessor m_preprocessor;
	private final Utility m_utility;
	
	public PreprocessorBridge(Utility utility)
	{
		m_utility = utility;
		Preprocessor.ILineFilter filter = null;
		Preprocessor.ILogger logger = null;
		
		if (utility != null)
		{
			final Project project = utility.getProject();
			filter = new Preprocessor.ILineFilter()
			{
				public String filter(String line)
				{
					return project.replaceProperties(line);
				}
			};
			
			logger = new Preprocessor.ILogger()
			{
				public void log(String message)
				{
					project.log(message);
				}
			};
		}
		
		m_preprocessor = new Preprocessor(logger, filter);
	}

	public void setMode(int mode)
	{
		m_preprocessor.setVerbose((mode & IPreprocessor.MODE_VERBOSE) != 0);
	}
	
	public void setFile(File fileName)
	{
		m_preprocessor.setFile(fileName);
	}
	
//	public boolean preprocess(Defines defines, String inputFile, String outputFile, String encoding)
//			throws IOException, PreprocessorException
//	{
//		FileInputStream fin = new FileInputStream(inputFile);
//		FileOutputStream fout = new FileOutputStream(outputFile);
//		try
//		{
//			return preprocess(defines, fin, fout, encoding);
//		}
//		finally
//		{
//			fout.close();
//		}
//	}

	public boolean preprocess(InputStream in, OutputStream out, String encoding)
			throws IOException, PreprocessorException
	{
		try
		{
			return m_preprocessor.preprocess(in, out, encoding);
		}
		catch (PPException e)
		{
			throw new PreprocessorException(e.getMessage(), e);
		}
	}

	public boolean preprocess(Strings lines, String encoding)
			throws PreprocessorException, IOException
	{
		try
		{
			return m_preprocessor.preprocess(lines.getVector(), encoding);
		}
		catch (PPException e)
		{
			throw new PreprocessorException(e.getMessage(), e);
		}
	}

	public boolean isVerbose()
	{
		return m_preprocessor.isVerbose();
	}

	public void addSymbols(String defines) throws PreprocessorException
	{
		try
		{
			m_preprocessor.addDefines(defines);
		}
		catch (PPException e)
		{
			throw new PreprocessorException(e.getMessage(), e);
		}
	}

	public void addSymbols(InputStream in) throws PreprocessorException, IOException
	{
		try
		{
			m_preprocessor.addDefines(in);
		}
		catch (PPException e)
		{
			throw new PreprocessorException(e.getMessage(), e);
		}		
	}

	public void addSymbols(File file) throws PreprocessorException, IOException
	{
		try
		{
			m_preprocessor.addDefines(file);
		}
		catch (PPException e)
		{
			throw new PreprocessorException(e.getMessage(), e);
		}		
	}

	public void clearSymbols() throws PreprocessorException
	{
		m_preprocessor.clearDefines();
	}

	public void outputDefinesToFile(File file, String encoding) throws PreprocessorException, IOException
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			out.write(m_preprocessor.getDefines().toString().getBytes(encoding != null ? encoding : "UTF-8"));
			out.flush();
		}
		finally
		{
			if (out != null) out.close();
		}
	}

	public void printSymbols() throws PreprocessorException
	{
		m_utility.getProject().log("Symbols: " + m_preprocessor.getDefines().toString());
	}

	public void setDebugLevel(String level) throws PPException
	{
		if ("none".equals(level) || level == null)
		{
			m_preprocessor.getDefines().undefine("DEBUG");	
		}
		else
		{
			if (level.equalsIgnoreCase("debug")|| 
					level.equalsIgnoreCase("info") || 
					level.equalsIgnoreCase("warn") || 
					level.equalsIgnoreCase("error") || 
					level.equalsIgnoreCase("fatal"))
			{
				m_preprocessor.addDefines("DEBUG=" + level);
			}
			else
			{
				throw new PPException("Unsupported debug level "+level+", Supported values are [debug|info|warn|error|fatal|none]");
			}
		}
	}
}
