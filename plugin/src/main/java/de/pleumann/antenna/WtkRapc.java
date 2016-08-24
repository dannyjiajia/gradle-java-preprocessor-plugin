/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 *
 * WtkRapc Antenna Ant task for BlackBerry
 * Copyright (c) 2002-2004 C. Enrique Ortiz <eortiz@j2medeveloper.com>
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import de.pleumann.antenna.post.PostProcessor;

public class WtkRapc extends PostProcessor {

    private final static String BB_BUILD_TOOLS_HOME = "bb.buildjars.home";
    private final static String RAPC_EXE = "/rapc.exe";
//    private static final String task = "[wtkrapc] ";

    private Project project;
    private File jadfile;
    private File source;
    private String codename;
    private String importlibs;
//  private File srcDir;
    private boolean quietMode;
    private boolean midletMode;
    private File destDir;

    public WtkRapc() {
        project = getProject();
    }

    public void setJadfile(File jadfile) {
        this.jadfile = jadfile;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public void setImport(String importlibs) {
        this.importlibs = importlibs;
    }

    public void setQuiet(boolean b) {
        this.quietMode = b;
    }

    public void setMidlet(boolean b) {
        this.midletMode = b;
    }

    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    public void execute() throws BuildException {

        project = getProject();

        // Check Preconditions
        if ((jadfile == null) || (source == null)) {
            throw new BuildException("RAPC needs a JAD and source files");
        }
        String bbjdebuildJars = getProject().getProperty(BB_BUILD_TOOLS_HOME);
        if (bbjdebuildJars == null) {
            throw new BuildException("RAPC needs BlackBerry JAR location property defined");
        }

        // Win32 vs. Linux file system (slash vs. backslash) fix
        String rapc = getUtility().getQuotedName(new File(bbjdebuildJars + RAPC_EXE));
        String jad = getUtility().getQuotedName(jadfile);
        String src = getUtility().getQuotedName(source);

        // Build the exec command and argument list.
        String arguments =
            "import="+getUtility().getOutsideQuotedPath(importlibs) + " codename="+codename + " " + jad + " " + src;
        String execCmd = rapc + ((quietMode)?" -quiet ":" ") + ((midletMode)?" -midlet ":" ") + arguments;

        try {

            StringBuffer t0 = new StringBuffer("RAPC Ant Task v0.1 initialized for project \"" + getProject().getName() + "\"");
            while (t0.length() < 58) t0.append(' ');
            StringBuffer t1 = new StringBuffer("Ant task by C. Enrique Ortiz, eortiz@j2medeveloper.com");
            while (t1.length() < 58) t1.append(' ');
            StringBuffer t2 = new StringBuffer("For JDE 3.7 ("+bbjdebuildJars+")");
            while (t2.length() < 58) t2.append(' ');

            this.log("**************************************************************");
            this.log("* " + t0 + " *");
            this.log("* " + t1 + " *");
            this.log("* " + t2 + " *");
            this.log("**************************************************************");

            this.log("Codename   : " + codename);
            this.log("JAD        : " + jadfile);
            this.log("Source(s)  : " + source);
            this.log("Imports    : " + importlibs);
            this.log("MIDlet mode: " + midletMode);
            this.log("Quiet mode : " + quietMode);
            this.log("DestDir    : " + destDir);
            this.log("Compiling now...");
            this.log("");

            // Get runtime and execute command
            Process proc = Runtime.getRuntime().exec(execCmd, null);
            getUtility().printProcessOutput(proc);
            int rc = proc.exitValue();
            if (rc != 0) {
                throw new BuildException("Failed (result=" + rc + ")");
            }

            // Output a blank line for readablity
            project.log("");

            // If a destination directory was specified,
            //  copy appropriate files to such destination
            if (destDir != null) {
                File srcfile;
                File destfile;

                // Copy the JAD file
                srcfile = jadfile;
                destfile = new File(destDir+"/"+codename+".jad");
                this.log("Copying file " + srcfile + " To " + destfile);
                getUtility().copy(srcfile, destfile);

                // Copy the COD file
                srcfile = new File(project.getBaseDir()+"/"+codename+".cod");
                destfile = new File(destDir+"/"+codename+".cod");
                this.log("Copying file " + srcfile + " To " + destfile);
                getUtility().copy(srcfile, destfile);

                // Copy the ALX file
                srcfile = new File(project.getBaseDir()+"/"+codename+".alx");
                destfile = new File(destDir+"/"+codename+".alx");
                this.log("Copying file " + srcfile + " To " + destfile);
                getUtility().copy(srcfile, destfile);

                // Should we remove the copy source files?
                //  TBD...

            }

        } catch (IOException ex) {
            this.log("IOException: " + ex);
            throw new BuildException(ex);
        } catch (Exception e) {
            this.log("Exception: " + e);
            throw new BuildException(e);
        } finally {
            this.log("Done");
        }
    }

}
