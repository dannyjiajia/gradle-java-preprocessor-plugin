package antenna.preprocessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import antenna.preprocessor.v2.PPException;

import de.pleumann.antenna.misc.Strings;


/**
 * omry 
 * Aug 3, 2006
 */
public interface IPreprocessor
{
	public static final int MODE_NORMAL = 0; // Normal operation
	public static final int MODE_BACKUP = 1; // Backup source files
	public static final int MODE_CLEANUP = 2; // Cleanup uncommented lines
	public static final int MODE_FILTER = 4; // Remove directives
	public static final int MODE_TEST = 8; // Test only, don't write
	public static final int MODE_VERBOSE = 16; // Verbose operation
	public static final int MODE_QUERY = 32; // Query available defines
	public static final int MODE_TARGET = 64; // Write to different directory
	public static final int MODE_INDENT = 128; // Comment indentation
	
	public void addSymbols(String defines) throws PreprocessorException;
	public void addSymbols(InputStream in) throws PreprocessorException, IOException;
	public void addSymbols(File file) throws PreprocessorException, IOException;
	public void clearSymbols() throws PreprocessorException;
	public void printSymbols() throws PreprocessorException;
	public void outputDefinesToFile(File file, String encoding) throws PreprocessorException, IOException;
	public boolean preprocess(Strings lines, String encoding) throws PreprocessorException, IOException;
	public void setMode(int mode);
	public void setFile(File fileName);
	public void setDebugLevel(String level) throws PPException;
}
