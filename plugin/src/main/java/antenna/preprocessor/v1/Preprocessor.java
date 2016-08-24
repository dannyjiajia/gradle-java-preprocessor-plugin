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
package antenna.preprocessor.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import antenna.preprocessor.IPreprocessor;
import antenna.preprocessor.PreprocessorException;
import antenna.preprocessor.v2.PPException;
import de.pleumann.antenna.misc.Strings;
import de.pleumann.antenna.misc.Utility;

public class Preprocessor implements IPreprocessor{

	private static final int STATE_NO_CONDITIONAL = 0;
	private static final int STATE_CAN_BECOME_TRUE = 1;
	private static final int STATE_IS_TRUE = 2;
	private static final int STATE_HAS_BEEN_TRUE = 3;

	
	private BooleanEvaluator eval;
	private Stack stack;
	private int state;
	private int mode;
	private File file;
	private String packageName;
	private PreprocessorLine pl;
	private int currentLine;

	private Utility utility;

	private String encoding;
	
	public Preprocessor(Utility utility, String encoding) {
		eval = new BooleanEvaluator("");
		stack = new Stack();
		mode = IPreprocessor.MODE_NORMAL;

		this.utility = utility;
		this.encoding = encoding;
	}

	
//	public String getPositionDescription(){
//		return "line '"+currentLine+" in "+file;
//	}
	
	public void setFile(File f) {
		file = f;
	}

	public boolean isBlind() {
		return ((state == STATE_CAN_BECOME_TRUE) || (state == STATE_HAS_BEEN_TRUE)) && (mode != IPreprocessor.MODE_CLEANUP);
	}

	public String getPackageName() {
		return packageName;
	}

	private void pushState() {
		stack.push(new Integer(state));
	}

	private void popState() {
		state = ((Integer) stack.pop()).intValue();
	}

	/**
	 * Handles a new "IF"-like command. The old
	 * state is pushed on the stack, a new "scope"
	 * is entered.
	 */
	private void handleIf(boolean condition) {
		pushState();
		if (!isBlind()) {
			if (condition) {
				state = STATE_IS_TRUE;
			}
			else {
				state = STATE_CAN_BECOME_TRUE;
			}
		}
		else {
			state = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleElseIf(boolean condition) throws PreprocessorException {
		if (state == STATE_NO_CONDITIONAL) {
			throw new PreprocessorException("Unexpected #elif", file, currentLine);
		}
		else if (state == STATE_CAN_BECOME_TRUE) {
			if (condition)
				state = STATE_IS_TRUE;
		}
		else if (state == STATE_IS_TRUE) {
			state = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleElse() throws PreprocessorException {
		if (state == STATE_NO_CONDITIONAL) {
			throw new PreprocessorException("Unexpected #else",file,  currentLine);
		}
		else if (state == STATE_CAN_BECOME_TRUE) {
			state = STATE_IS_TRUE;
		}
		else if (state == STATE_IS_TRUE) {
			state = STATE_HAS_BEEN_TRUE;
		}
	}

	private void handleEndIf() throws PreprocessorException {
		if (state == STATE_NO_CONDITIONAL) {
			throw new PreprocessorException("Unexpected #endif",file,  currentLine);
		}
		else {
			popState();
		}
	}

	private void handleCommand(PreprocessorLine l) throws PreprocessorException {
		int type = l.getType();

		if (type == PreprocessorLine.TYPE_DEFINE) {
			if (!isBlind()) {
				eval.define(l.getArgs());
			}
		}
		else if (type == PreprocessorLine.TYPE_UNDEF) {
			if (!isBlind()) {
				eval.undefine(l.getArgs());
			}
		}
		else if (type == PreprocessorLine.TYPE_IF) {
			handleIf(eval.evaluate(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_IFDEF) {
			handleIf(eval.isDefined(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_IFNDEF) {
			handleIf(!eval.isDefined(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_ELIF) {
			handleElseIf(eval.evaluate(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_ELIFDEF) {
			handleElseIf(eval.isDefined(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_ELIFNDEF) {
			handleElseIf(!eval.isDefined(l.getArgs()));
		}
		else if (type == PreprocessorLine.TYPE_ELSE) {
			handleElse();
		}
		else if (type == PreprocessorLine.TYPE_ENDIF) {
			handleEndIf();
		}
	}

	public void setMode(int value) {
		mode = value;
	}

	public String filterLine(String s) throws PreprocessorException
	{
	    String t = utility.getProject().replaceProperties(s);

	    // Replacement of Foo.parseFoo() expressions, requested by Steve Oldmeadow.
	    if (!t.equals(s)) {
	        // Replace Integer.parseInt("...") by int constant, if possible.
	        int i = t.indexOf("Integer.parseInt(\"");
	        if (i != -1) {
	            int j = t.indexOf("\")", i + 18);
	            if (j != -1) {
	                try {
	                    int value = Integer.parseInt(t.substring(i + 18, j));
	                    t = t.substring(0, i) + value + t.substring(j + 2);
	                }
	                catch (Exception ignored) {
	                }
	            }
	        }

	        // Replace Boolean.parseBoolean("...") by boolean constant, if possible.
	        i = t.indexOf("\"true\".equals(\"");
	        if (i != -1) {
	            int j = t.indexOf("\")", i + 15);
	            if (j != -1) {
	                try {
	                    boolean value = "true".equals(t.substring(i + 15, j));
	                    t = t.substring(0, i) + value + t.substring(j + 2);
	                }
	                catch (Exception ignored) {
	                }
	            }
	        }
	        
	        // Replace Double.parseDouble("...") by double constant, if possible.
	        i = t.indexOf("Double.parseDouble(\"");
	        if (i != -1) {
	            int j = t.indexOf("\")", i + 20);
	            if (j != -1) {
	                try {
	                    double value = Double.parseDouble(t.substring(i + 20, j));
	                    t = t.substring(0, i) + value + t.substring(j + 2);
	                }
	                catch (Exception ignored) {
	                }
	            }
	        }
	    }
	    return t;
	}
	
	public PreprocessorLine analyzeLine(String s) throws PreprocessorException {

		if (pl == null) {
			pl = new PreprocessorLine(s);
		}
		else {
			pl.processLine(s);
		}
		
		return pl;
	}

	String commentLine(PreprocessorLine l) {
		if ((mode & IPreprocessor.MODE_INDENT) == 0) {
			return "//# " + l.getSpace() + l.getText();
		}
		else {
			return l.getSpace() + "//# " + l.getText();
		}
	}

	String uncommentLine(PreprocessorLine l) {
		return l.getSpace() + l.getText();
	}

	public Strings getIncludeData(PreprocessorLine l) throws PreprocessorException {
		Strings include = new Strings();
		/**
		 * Try to load include file
		 */
		String name = utility.interpret(l.getArgs());
		File file = new File(name);
		if (!file.isAbsolute()) {
			name = this.file.getParent() + File.separatorChar + name;
		}

		try {
			if (encoding != null && encoding.length() != 0) {
				include.loadFromFile(new File(name), encoding);
			}
			else {
				include.loadFromFile(new File(name));
			}
		}
		catch (java.io.UnsupportedEncodingException uee) {
			throw new PreprocessorException("Unknown encoding \"" + encoding + "\" for "+ file);
		}
		catch (java.io.IOException error) {
			throw new PreprocessorException("File \"" + name + "\" not found");
		}

		return include;
	}

	public boolean preprocess(Strings lines, String encoding) throws PreprocessorException, IOException
	{
		stack.clear();
		state = STATE_NO_CONDITIONAL;
		//this.mode = mode;

		Strings oldLines = new Strings();
		oldLines.assign(lines);

		int i = 0;
		while (i < lines.size()) {
			currentLine = i;
			
			// System.out.println("(" + i + ") " + lines.get(i));

			String line = lines.get(i);
			try {
				//System.out.println(lines.get(i));
				PreprocessorLine l = analyzeLine(line);
				

				/**
				 * Include handling comes first
				 */
				if (l.getType() == PreprocessorLine.TYPE_INCLUDE) {

					Strings include = isBlind() ? null : getIncludeData(l);

					/**
					 * Find end of include marker in source code
					 */
					i++;
					while (i < lines.size()) {
						l = analyzeLine(lines.get(i));
						if (l.getType() == PreprocessorLine.TYPE_ENDINCLUDE)
							break;
						lines.delete(i);
					}

					/**
					 * If not found, raise an error
					 */
					if (l.getType() != PreprocessorLine.TYPE_ENDINCLUDE) {
						throw new PreprocessorException("Missing #endinclude", file, currentLine);
					}

					if (include != null && (mode & IPreprocessor.MODE_CLEANUP) == 0) {
						/**
						 * Insert new include lines.
						 */
						Preprocessor subfilter = new Preprocessor(utility, encoding);
						subfilter.eval = this.eval; // Ugly stuff.
						subfilter.setFile(file);
						subfilter.setMode(mode);
						subfilter.preprocess(include, encoding);

						for (int k = 0; k < include.size(); k++) {
							String s = include.get(k);
							PreprocessorLine ln = analyzeLine(s);
							if (ln.getType() == PreprocessorLine.TYPE_VISIBLE) {
								lines.insert(i, s);
								i++;
							}
						}
					}

					/**
					 * Continue with preprocessing.
					 */
					i = i + 1; //i = i; // + 1; // include.size() + 2;
				}
				/**
				 * Normal stuff goes here.
				 */
				else {
					if (l.getType() == PreprocessorLine.TYPE_VISIBLE || l.getType() == PreprocessorLine.TYPE_HIDDEN) {
						if (isBlind()) {
							lines.set(i, commentLine(l));
							if ((mode & IPreprocessor.MODE_VERBOSE) != 0) {
								System.out.println("Comment: " + l);
							}
						}
					    else {
							lines.set(i, uncommentLine(l));
							if ((mode & IPreprocessor.MODE_VERBOSE) != 0) {
								System.out.println("Uncomment: " + l);
							}
						}
					}
					else {
						handleCommand(l);
					}
					i++;
				}
			}
			catch (PreprocessorException error) {
				throw new PreprocessorException(error.getMessage() + " in line " + (i + 1) + " : " + line, file, currentLine);
			}
		}
		if (state != STATE_NO_CONDITIONAL) {
			throw new PreprocessorException("Missing #endif", file, currentLine);
		}

		if ((mode & IPreprocessor.MODE_FILTER) != 0) {
		    // Cleanup all directives and uncommented stuff.
		    
		    for (int j = lines.size() - 1; j >= 0; j--) {
				PreprocessorLine l = analyzeLine(lines.get(j));
				
				if (l.getType() != PreprocessorLine.TYPE_VISIBLE) {
				    lines.delete(j);
				}
		    }
		}
		
		boolean linesModified = !(lines.equals(oldLines));

		/**
		 * Where the heck did the following three lines come from?
		 */
		//if (modified != linesModified) {
		//	throw new PreprocessorException("modified(" + modified + ") != equals");
		//}

		return (linesModified);
	}


	public void setSymbols(String symbols) throws PreprocessorException
	{
		eval = new BooleanEvaluator(symbols);
	}
	

	
	public void addSymbols(String defines) throws PreprocessorException
	{
		setSymbols(defines);
	}


	public void addSymbols(InputStream in) throws PreprocessorException, IOException
	{
		throw new UnsupportedOperationException("addSymbols(InputStream) is not supported by preprocessor v1");		
	}


	public void addSymbols(File file) throws PreprocessorException, IOException
	{
		throw new UnsupportedOperationException("addSymbols(File) is not supported by preprocessor v1");
	}


	public void clearSymbols() throws PreprocessorException
	{
		eval = new BooleanEvaluator("");
	}


	public void outputDefinesToFile(File file, String encoding) throws PreprocessorException, IOException
	{
		throw new UnsupportedOperationException("outputSymbolsToFile is not supported by preprocessor v1");
	}


	public void printSymbols() throws PreprocessorException
	{
		utility.getProject().log("Symbols: " + eval.getDefines());
	}


	public void setDebugLevel(String level) throws PPException
	{
		throw new UnsupportedOperationException("setDebugLevel is not supported by preprocessor v1");
	}
	
	/*
	// this method allow using a different filter to handle the lines. 
	public static void execute(Preprocessor filter,  File sourceDir, File targetDir, String[] files, String defines, int mode, String newext, String encoding, Utility utility) throws PreprocessorException {
		
		Strings lines = new Strings();
		String filename = ""; // For exception handling

		try {
			for (int i = 0; i < files.length; i++) {
				filename = files[i];

				String sourceFile = "" + sourceDir + File.separatorChar + filename;

				try {
					if (encoding != null && encoding.length() > 0)
						lines.loadFromFile(sourceFile, encoding);
					else
						lines.loadFromFile(sourceFile);
				}
				catch (java.io.UnsupportedEncodingException uee) {
					throw new PreprocessorException("Unknown encoding \"" + encoding + "\" for "+filename);
				}
				catch (java.io.IOException e) {
					throw new PreprocessorException("Unable to read file \"" + filename);
				}

				filter.setFile(sourceFile);
				filter.setLines(lines);
				filter.setDefines(defines);

				filter.setMode(mode & (IPreprocessor.MODE_CLEANUP | IPreprocessor.MODE_INDENT | IPreprocessor.MODE_FILTER));

				boolean modified = filter.execute();
				// if preprocessing modifies the file, or
				// we are putting the output in a different directory
				// or we are changing the file extension
				// then we have to write a new file
				if (modified || !sourceDir.equals(targetDir) || (newext != null)) {
					try {
						if ((mode & IPreprocessor.MODE_VERBOSE) != 0) {
							System.out.println(filename + " ... modified");
						}

						if ((mode & IPreprocessor.MODE_TEST) == 0) {
							String targetFile;

							if (newext != null) {
								int dot = filename.indexOf('.');

								if (dot != -1) {
									filename = filename.substring(0, dot) + newext;
								}
								else {
									filename = filename + newext;
								}
							}

							targetFile = "" + targetDir + File.separatorChar + filename;

							File file = new File(targetFile + "~");
							file.delete();
							if (!new File(targetFile).renameTo(file) && (targetDir == null)) {
								throw new java.io.IOException();
							}

							new File(targetFile).getParentFile().mkdirs();

							if (encoding != null && encoding.length() > 0) {
								filter.getLines().saveToFile(targetFile, encoding);
							}
							else {
								filter.getLines().saveToFile(targetFile);
							}

							if ((mode & IPreprocessor.MODE_BACKUP) == 0) {
								file.delete(); // ??????
							}
						}
					}
					catch (java.io.UnsupportedEncodingException uee) {
						throw new PreprocessorException("Unknown encoding \"" + encoding + "\" for "+files[i]);
					}
					catch (java.io.IOException e) {
						throw new PreprocessorException("Unable to write file \"" + files[i] + "\"");
					}
				}
				else {
					if ((mode & IPreprocessor.MODE_VERBOSE) != 0) {
						System.out.println(filename + " ... not modified");
					}
				}
			}
		}
		catch (PreprocessorException error) {
			if ((mode & IPreprocessor.MODE_VERBOSE) == 0) {
				System.out.println(filename + " ... not modified, " + error.getMessage());
			}

			throw error;
		}
	}
	*/
	
	/**
	 * Add a filter.
	 * the filter will be asked to filter each line in each file processed.
	 * @param filter
	 */
	/*
	public void addFilter(LineFilter filter)
	{
		filters.add(filter);
	}
	*/
}
