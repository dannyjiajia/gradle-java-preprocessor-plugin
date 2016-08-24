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
package de.pleumann.antenna.post;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import de.pleumann.antenna.misc.*;

public abstract class PostProcessor extends Task {

	public class Preserve extends Conditional {
		private String value = "";

        public Preserve(Project project) {
            super(project);
        }
        
		public void setClass(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

    private boolean verbose = true;

    private Conditional condition;
    
	private Vector preserve = new Vector();

	private File jarFile;

	private File toJarFile;

	private File jadFile;

	private JadFile jad;

	private Path bootclasspath;

	private Path classpath;

	private Utility utility;

	public void init() {
		super.init();
		classpath = new Path(getProject(), "");
		utility = Utility.getInstance(getProject(), this);
        condition = new Conditional(getProject());
	}

   public void setVerbose(boolean verbose) {
      this.verbose = verbose;
   }

	public Object createPreserve() {
		Preserve pre = new Preserve(getProject());
		preserve.addElement(pre);
		return pre;
	}

	public void setJarfile(File srcFile) {
		this.jarFile = srcFile;
	}

	public void setTojarfile(File destFile) {
		this.toJarFile = destFile;
	}

	public void setJadfile(File file) {
		jadFile = file;
	}

	public String getFullClasspath() {
		String cp;

		if (bootclasspath == null) {
			cp = utility.getMidpApi();  // was: getEmptyApi()
		}
		else {
			cp = bootclasspath.toString();
		}

		if ((classpath != null) && (classpath.size() > 0)) {
			cp = cp + File.pathSeparatorChar + classpath;
		}
		return cp;
	}

	public void setClasspath(Path classpath) {
		if (this.classpath == null) {
			this.classpath = classpath;
		}
		else {
			this.classpath.append(classpath);
		}
	}

	/** Gets the classpath to be used for this compilation. */
	public Path getClasspath() {
		return classpath;
	}

	/**
	* Adds a path to the classpath.
	*/
	public Path createClasspath() {
		if (classpath == null) {
			classpath = new Path(getProject());
		}
		return classpath.createPath();
	}

	/**
	* Adds a reference to a classpath defined elsewhere.
	*/
	public void setClasspathref(Reference r) {
		createClasspath().setRefid(r);
	}

	public void setBootclasspath(Path classpath) {
		if (this.bootclasspath == null) {
			this.bootclasspath = classpath;
		}
		else {
			this.bootclasspath.append(classpath);
		}
	}

	/** Gets the classpath to be used for this compilation. */
	public Path getBootclasspath() {
		return bootclasspath;
	}

	/**
	* Adds a path to the bootclasspath.
	*/
	public Path createBootclasspath() {
		if (bootclasspath == null) {
			bootclasspath = new Path(getProject());
		}
		return bootclasspath.createPath();
	}

	/**
	* Adds a reference to a bootclasspath defined elsewhere.
	*/
	public void setBootclasspathref(Reference r) {
		createBootclasspath().setRefid(r);
	}

   public boolean getVerbose() {
      return verbose;
   }

	public Vector getPreserve() {
        Vector result = new Vector();
        for (int i = 0; i < preserve.size(); i++) {
            Preserve p = (Preserve)preserve.elementAt(i);
            if (p.isActive()) {
                result.add(p);
            }
        }
        
		return result;
	}

    public void setIf(String s) {
        condition.setIf(s);
    }
    
    public void setUnless(String s) {
        condition.setUnless(s);
    }

    public boolean isActive() {
        return condition.isActive();
    }
        
	public File getJarFile() {
		return jarFile;
	}

	public File getToJarFile() {
		return toJarFile;
	}

	public JadFile getJad() throws IOException {
		if (jad != null) {
			return jad;
		}
		else {
			if (jadFile != null) {
				jad = new JadFile();
				jad.load("" + jadFile, null);
			}

			return jad;
		}
	}

	public Utility getUtility() {
		return utility;
	}

	public void updateJad() throws IOException {
		JadFile jad = getJad();

		if ((jad != null) && (jarFile != null)) {
			if ((toJarFile == null) || (jarFile.equals(toJarFile))) {
				jad.setValue("MIDlet-Jar-Size", "" + jarFile.length());

				log("Updating JAD file " + jadFile);
				try {
					jad.save("" + jadFile, null);
				}
				catch (IOException ex) {
					throw new BuildException("Error processing JAD file", ex);
				}
			}
		}
	}
}
