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

import org.apache.tools.ant.BuildException;

import de.pleumann.antenna.misc.Utility;
import de.pleumann.antenna.post.PostProcessor;

public class WtkPreverify extends PostProcessor {

	private File srcDir;

	private File destDir;

	private boolean cldc = true;

	private int flags;

	public void setCldc(boolean on) {
		this.cldc = on;
	}

	public void setNonative(boolean b) {
		if (b) {
			flags = flags | Utility.PREVERIFY_NONATIVE;
		}
		else  {
			flags = flags & ~Utility.PREVERIFY_NONATIVE;
		}
	}
	
	public void setNofloat(boolean b) {
		if (b) {
			flags = flags | Utility.PREVERIFY_NOFLOAT;
		}
		else  {
			flags = flags & ~Utility.PREVERIFY_NOFLOAT;
		}
	}
	
	public void setNofinalize(boolean b) {
		if (b) {
			flags = flags | Utility.PREVERIFY_NOFINALIZE;
		}
		else  {
			flags = flags & ~Utility.PREVERIFY_NOFINALIZE;
		}
	}

	public void setDestdir(File destDir) {
		if (getJarFile() != null) {
			throw new BuildException("Can only preverify JAR or directory, not both.");
		}

		this.destDir = destDir;
	}

	public void setSrcdir(File srcDir) {
		if (getJarFile() != null) {
			throw new BuildException("Please use \"tojarfile\" to specify preverified JAR");
		}

		this.srcDir = srcDir;
	}

	public void setJarfile(File srcFile) {
		if (srcDir != null) {
			throw new BuildException("Can only preverify JAR or directory, not both.");
		}

		super.setJarfile(srcFile);
	}

	public void setTojarfile(File destFile) {
		if (srcDir != null) {
			throw new BuildException("Please use \"destdir\" to specify preverified directory");
		}

		super.setTojarfile(destFile);
	}

	public void execute() throws BuildException {
		if (!isActive())
			return;

		if ((getJarFile() == null) && (srcDir == null)) {
			throw new BuildException("Need a JAR file or a source directory");
		}

		File tmpDir = getUtility().getTempDir();

		try {
			try {
				if (srcDir != null) {
					getUtility().preverify(srcDir, destDir, getFullClasspath(), cldc, flags);
				}
				else {
					getUtility().preverify(getJarFile(), tmpDir, getFullClasspath(), cldc, flags);

					if (getToJarFile() == null) {
						setTojarfile(getJarFile());
					}

					getToJarFile().delete();
					new File(tmpDir, getJarFile().getName()).renameTo(getToJarFile());

					updateJad();
				}
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
