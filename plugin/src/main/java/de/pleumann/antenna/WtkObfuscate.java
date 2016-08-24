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
package de.pleumann.antenna;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import de.pleumann.antenna.misc.Conditional;
import de.pleumann.antenna.post.PostProcessor;

public class WtkObfuscate extends PostProcessor {

	public class Argument extends Conditional {
		String value;

		public Argument(Project project) {
			super(project);
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	private Vector arguments = new Vector();

	private String obfuscator;

	public Argument createArgument() {
		Argument a = new Argument(getProject());
		arguments.addElement(a);
		return a;
	}

	public Vector getArguments() {
		Vector result = new Vector();
		for (int i = 0; i < arguments.size(); i++) {
			Argument a = (Argument) arguments.elementAt(i);
			if (a.isActive()) {
				result.add(a);
			}
		}

		return result;
	}

	public void setObfuscator(String obfuscator) {
		this.obfuscator = obfuscator;
	}

	// Add ability to work on JAR/JAD pair. JAD size is
	// updated automatically.
	//
	// Same for obfuscate, smartlink, preverify
	//
	// Generic class for ME tasks that take JAR/JAR?

	public void execute() throws BuildException {
		if (!isActive())
			return;

		if (getJarFile() == null) {
			throw new BuildException("Need a JAR file");
		}

		File tmpDir = getUtility().getTempDir();

		try {
			try {
				File tmpFile = getToJarFile();
				if (tmpFile == null) {
					tmpFile = new File(tmpDir + "/output.jar");
				}

				Vector preserve = getPreserve();
				getUtility().getPreserveList(getJad(), preserve);
				getUtility().obfuscate(getJarFile(), tmpFile, getFullClasspath(), getVerbose(), preserve, obfuscator, getArguments(), getJad());

				if (getToJarFile() == null) {
					setTojarfile(getJarFile());
				}

				if (!getToJarFile().delete()) {
					log("Unable to delete " + getToJarFile(), Project.MSG_WARN);
				}

				if (!tmpFile.renameTo(getToJarFile())) {
					log("Unable to rename " + tmpFile, Project.MSG_WARN);
				}

				updateJad();
			}
			finally {
				getUtility().delete(tmpDir);
			}
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
	}
}