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
import java.io.IOException;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Java;

import de.pleumann.antenna.misc.*;

public class WtkMakePrc extends Task {

    private Utility utility;
    
    private Conditional condition;
    
    private File prcFile;
    
    private File jadFile;
    
    private File jarFile;
    
    private File icon;
    
    private File smallicon;
    
    private String name;
    
    private String longname;
    
    private String creator;
    
    private String type = "Data";

    private boolean highres = false; // ZHOU: this property is only used by IBM WME

    private String converter;

    public void init() throws BuildException {
        super.init();
        utility = Utility.getInstance(getProject(), this);
        condition = new Conditional(getProject());
        
        if (getProject().getProperty("wtk.wme.home") != null) {
            converter = "wme";
        }
        else {
            converter = "wtk";
        }
    }

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setIcon(File icon) {
		this.icon = icon;
	}

	public void setJadFile(File jadFile) {
		this.jadFile = jadFile;
	}

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

	public void setLongname(String longname) {
		this.longname = longname;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrcFile(File prcFile) {
		this.prcFile = prcFile;
	}

	public void setSmallicon(File smallicon) {
		this.smallicon = smallicon;
	}

	public void setType(String type) {
		this.type = type;
	}

    public void setHighres(boolean highres) {
        this.highres = highres;
    }

    public void setIf(String s) {
        condition.setIf(s);
    }
    
    public void setUnless(String s) {
        condition.setUnless(s);
    }
    
    public void setConverter(String s) {
    	this.converter = s;
    }

    public boolean isActive() {
        return condition.isActive();
    }

	public void executeWtk() throws BuildException {
       
		JadFile jad = new JadFile();
		if (jadFile != null) {
			try {
				jad.load(jadFile.getAbsolutePath(), null);
			}
			catch (IOException ex) {
				throw new BuildException("Error loading JAD file", ex);
			}        
		}

		String prc;
		if (prcFile == null) {
			prc = (jadFile == null ? jarFile : jadFile).getAbsolutePath();
			prc = prc.substring(0, prc.lastIndexOf('.')) + ".prc";
		}
		else {
			prc = prcFile.getAbsolutePath();
		}
        
		String converter = utility.getWtkRelative("wtklib/devices/PalmOS_Device/converter.jar");

		Java java = new Java();
		java.setProject(getProject());
		java.setTaskName(this.getTaskName());
		java.createClasspath().setPath(converter);
		java.setClassname("com.sun.midp.palm.database.MakeMIDPApp");
		java.setFork(true);
		java.setFailonerror(true);
        
		java.createArg().setLine(" -o \"" + prc + "\"");

		if (creator != null) java.createArg().setLine(" -creator \"" + creator + "\"");
		if (icon != null) java.createArg().setLine(" -icon \"" + icon + "\"");
		if (smallicon != null) java.createArg().setLine(" -smallicon \"" + smallicon + "\"");
		if (name != null) java.createArg().setLine(" -name \"" + name + "\"");
		if (longname != null) java.createArg().setLine(" -longname \"" + longname + "\"");
		if (type != null) java.createArg().setLine(" -type " + type);
		if (jadFile != null) java.createArg().setLine(" -jad \"" + jadFile + "\"");
                
		if (jarFile != null) {        
			java.createArg().setFile(jarFile);
		}
		else {
			java.createArg().setFile(new File(jadFile.getParent(), new File(jad.getValue("MIDlet-Jar-URL")).getName()));
		}

		log("Generating PRC file " + new File(prc).getName() + " using WTK");
        
		utility.delete(new File(prc));
		java.execute();
		if (!new File(prc).exists()) {
			log("There was a problem generating the PRC file. Maybe you are using");
			log("the Wireless Toolkit 2.x (and thus MIDP 2.0), which doesn't contain");
			log("the PalmOS converter any longer because SUN's MIDP for PalmOS supports");
			log("MIDP 1.0 only. In this case, please switch back to an older Wireless");
			log("Toolkit. Alternatively, you might want to use the PRC converter from");
			log("IBM's Websphere Micro Environment Toolkit for PalmOS.");
            
			throw new BuildException("Unable to generate PRC file - please check arguments.");
		}
	}

    /**
     * Using IBM Websphere Micro Edition to generant PRC executable file for PalmOS.<p>
     *
     * Note: This method was originally developed by the author of Antenna.
     *       And patched by <b>Xiaodan ZHOU, China (danj@263.net)</b>
     *
     * Modification: Changed jad2prc.exe to the newer jartoprc.exe,
     * @throws BuildException
     */
	public void executeWme() throws BuildException {
		String wme = getProject().getProperty("wtk.wme.home");
		if (wme == null || !(new File(wme + "/bin/jartoprc.exe").exists())) {
			throw new BuildException("IBM PRC generator not found. Please define wtk.wme.home properly.");
		}

		String prc;
		if (prcFile == null) {
			prc = (jadFile == null ? jarFile : jadFile).getAbsolutePath();
			prc = prc.substring(0, prc.lastIndexOf('.')) + ".prc";
		}
		else {
			prc = prcFile.getAbsolutePath();
		}

		if (jarFile == null) {
			JadFile jad = new JadFile();
			if (jadFile != null) {
				try {
					jad.load(jadFile.getAbsolutePath(), null);
					
					jarFile = new File(jad.getValue("MIDlet-Jar-URL"));
					if (!jarFile.isAbsolute()) {
						jarFile = new File(jadFile.getParentFile(), jarFile.getName());
					}
				}
				catch (IOException ex) {
					throw new BuildException("Error loading JAD file", ex);
				}        
			}
		}
        
		String executable = utility.getQuotedName(new File(wme + "/bin/jartoprc.exe"));
		StringBuffer arguments = new StringBuffer();

		//if (prc != null) // ZHOU: PRC file is obsolete in jartoprc.exe. property 'name' is neccessary
        if (name==null) {
            String fileSeparator = System.getProperty("file.separator");
            int begin = prc.lastIndexOf(fileSeparator);
            int end = prc.lastIndexOf('.');
            name = prc.substring(begin, end);
        }
        arguments.append(" -name:").append(name);

		if (creator != null) arguments.append(" -id:").append(creator);
        // if (type != null) // ZHOU: type is obsolete in jartoprc.exe
		if (icon != null) arguments.append(" -lgicon:").append("\"").append(icon).append("\"");
		if (smallicon != null) arguments.append(" -smicon:").append("\"").append(smallicon).append("\"");

		if (jadFile != null) arguments.append(" -jad:").append("\"").append(jadFile).append("\"");
        // ZHOU: jartoprc.requirs that jad exists, then do not specify jar
        else if (jarFile != null) arguments.append(" -jar:").append("\"").append(jarFile).append("\"");
        else throw new BuildException("JAD & JAR missing, PRC cannot be generated");

        if (highres==true) arguments.append(" -highres");

		log("Executable: " + executable, Project.MSG_VERBOSE);
		log("Arguments : " + arguments, Project.MSG_VERBOSE);
        
		log("Generating PRC file " + new File(prc).getName() + " using WME");

		try {
			Process proc = Runtime.getRuntime().exec(executable + " " + arguments, null, new File(wme + "/bin"));
			proc.waitFor();
			utility.printProcessOutput(proc);

			if (proc.exitValue() != 0)
				throw new BuildException("PRC generation failed (result=" + proc.exitValue() + ")");
		}
		catch (IOException ex) {
			throw new BuildException(ex);
		}
		catch (InterruptedException ex) {
			throw new BuildException(ex);
		}
	}
        
    public void execute() throws BuildException {
        if (!isActive()) return;
        
        if ((jadFile == null) && (jarFile == null)) {
            throw new BuildException("Need either a JAR file or a JAD file");
        }

        if ("wme".equalsIgnoreCase(converter)) {
        	executeWme();
        }
        else {
        	executeWtk();
        }
    }
}
