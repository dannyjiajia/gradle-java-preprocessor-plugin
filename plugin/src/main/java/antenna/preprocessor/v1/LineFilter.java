package antenna.preprocessor.v1;

import antenna.preprocessor.PreprocessorException;


/**
 * omry 
 * Aug 2, 2006
 * 
 * A line filter receives each line of a file being pre-processed, and is given a change to modify the line by returning a new line.
 */
public interface LineFilter
{
	public String filterLine(String s) throws PreprocessorException;
}
