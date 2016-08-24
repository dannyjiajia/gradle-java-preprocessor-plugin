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
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import de.pleumann.antenna.misc.Conditional;
import de.pleumann.antenna.misc.Utility;

public class WtkBuild extends Javac {

	private Conditional condition;

	private Utility utility;

	private boolean preverify = true;

	private boolean cldc = true;

	private int flags = 0;
	
	public void setPreverify(boolean preverify) {
		this.preverify = preverify;
	}

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
	
	public void init() throws BuildException {
		super.init();

		utility = Utility.getInstance(getProject(), this);
		condition = new Conditional(getProject());

		setTarget("1.1");
		setSource("1.2");
		setDebug(true);
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

	public void execute() throws BuildException {
		if (!isActive())
			return;

		File tmpDir = utility.getTempDir();

		try {
            if (utility.getToolkitType() == Utility.TOOLKIT_MPOWER) {
                String bcp = utility.getMidpApi();
                if (getClasspath() == null) {
                    setClasspath(new Path(getProject(), bcp));
                }
                else {
                    getClasspath().add(new Path(getProject(), bcp));
                }
            }
            else if (getBootclasspath() == null) {
				String bcp = utility.getMidpApi();
                setBootclasspath(new Path(getProject(), bcp));
			}

			File origDest = getDestdir();
			File tempDest = new File(tmpDir + "/tmpclasses");

			if (preverify) {
				setDestdir(tempDest);
				tempDest.mkdir();
			}

			try {
				super.execute();
			}
			finally {
				setDestdir(origDest);
			}

			if (preverify) {
				String cp = getBootclasspath() + File.pathSeparator + getClasspath() + File.pathSeparator;

				utility.preverify(tempDest, origDest, cp, cldc, flags);
			}
		}
		finally {
			utility.delete(tmpDir);
		}
	}

}
