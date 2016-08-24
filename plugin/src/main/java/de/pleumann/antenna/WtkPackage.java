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
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * -----------------------------------------------------------------------------
 */
package de.pleumann.antenna;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;

import de.pleumann.antenna.misc.Conditional;
import de.pleumann.antenna.misc.JadFile;
import de.pleumann.antenna.misc.Utility;

public class WtkPackage extends Jar {

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

    private boolean verbose;

    private Vector preserve = new Vector();

    private Utility utility;

    private Conditional condition;

    private File jarFile;

    private File jadFile;

    private boolean autoversion;

    private boolean obfuscate;

    private boolean preverify;

    private boolean keepManifestOrder;

    private String config;

    private String profile;

    private Path bootclasspath;

    private Path classpath;

    private Path libclasspath;

    private File manifest;
    
    private Vector m_excludeFromManifest;

//    private boolean smartlink;

    private boolean cldc = true;

    private int flags;

    private String classDirs = "";

    public void init() throws BuildException {
        super.init();
        utility = Utility.getInstance(getProject(), this);
        
        config = "CLDC-" + utility.getCldcVersion();
        profile = "MIDP-" + utility.getMidpVersion();
        
        condition = new Conditional(getProject());
        classpath = new Path(getProject());
        setUpdate(false);
    }

    public Preserve createPreserve() {
        Preserve pre = new Preserve(getProject());
        preserve.addElement(pre);
        return pre;
    }

    public void setVerbose(boolean aVerbose) {
        this.verbose = aVerbose;
    }

    public void setJarfile(File jar) {
        super.setDestFile(jar);
        this.jarFile = jar;
    }

    public void setJadfile(File jad) {
        this.jadFile = jad;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setAutoversion(boolean version) {
        this.autoversion = version;
    }

    public void setCldc(boolean on) {
        this.cldc = on;
    }

    public void setNonative(boolean b) {
        if (b) {
            flags = flags | Utility.PREVERIFY_NONATIVE;
        }
        else {
            flags = flags & ~Utility.PREVERIFY_NONATIVE;
        }
    }

    public void setNofloat(boolean b) {
        if (b) {
            flags = flags | Utility.PREVERIFY_NOFLOAT;
        }
        else {
            flags = flags & ~Utility.PREVERIFY_NOFLOAT;
        }
    }

    public void setNofinalize(boolean b) {
        if (b) {
            flags = flags | Utility.PREVERIFY_NOFINALIZE;
        }
        else {
            flags = flags & ~Utility.PREVERIFY_NOFINALIZE;
        }
    }

    public void setSmartlink(boolean smartlink) {
    	log("SmartLink is not supported in WtkPackage, use WtkSmartLink", Project.MSG_WARN);
    }

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    public void setPreverify(boolean preverify) {
        this.preverify = preverify;
    }

    public void setBootclasspath(Path bootclasspath) {
        this.bootclasspath = bootclasspath;
    }

    public Path createBootclasspath() {
        if (bootclasspath == null) {
            bootclasspath = new Path(getProject());
        }
        return bootclasspath.createPath();
    }

    public void setBootclasspathref(Reference r) {
        createBootclasspath().setRefid(r);
    }

    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    public void setClasspathref(Reference r) {
        createClasspath().setRefid(r);
    }

    public void setLibclasspath(Path libclasspath) {
        this.libclasspath = libclasspath;
    }

    public Path createLibclasspath() {
        if (libclasspath == null) {
            libclasspath = new Path(getProject());
        }
        return libclasspath.createPath();
    }

    public void setLibclasspathref(Reference r) {
        createLibclasspath().setRefid(r);
    }

    public void setManifest(File manifest) {
        this.manifest = manifest;
        super.setManifest(manifest);
    }

    public void addFileset(FileSet files) {
        super.addFileset(files);
        classDirs = classDirs + files.getDir(getProject()) + File.pathSeparator;

    }

    public Vector getPreserve() {
        Vector result = new Vector();
        for (int i = 0; i < preserve.size(); i++) {
            Preserve p = (Preserve) preserve.elementAt(i);
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

    public void setKeepManifestOrder(boolean keep) {
        this.keepManifestOrder = keep;
    }

    public boolean isActive() {
        return condition.isActive();
    }

    private void addManifest(File tmpDir, File manFile) {
        try {
            FileInputStream manF = new FileInputStream(manFile);
            Manifest man = new Manifest(manF);

            File tmp = new File(tmpDir, jarFile.getName() + ".tmp");
            FileInputStream fis = new FileInputStream(jarFile);
            JarInputStream jis = new JarInputStream(fis);

            FileOutputStream fos = new FileOutputStream(tmp);
            JarOutputStream jos = new JarOutputStream(fos, man);

            int n;
            byte[] buf = new byte[32768];

            JarEntry je = null;
            do {
                je = (JarEntry) jis.getNextEntry();
                if (je != null) {

                    if (!je.getName().equals(JarFile.MANIFEST_NAME)) {
                        jos.putNextEntry(new ZipEntry(je.getName()));
                        n = 0;

                        while ((n = jis.read(buf)) != -1) {
                            jos.write(buf, 0, n);
                        }

                    }

                    jos.closeEntry();
                }

            }
            while (je != null);

            /*
             * log("Adding manifest " + manFile.getAbsolutePath());
             * 
             * FileInputStream manF = new FileInputStream(manFile);
             * jos.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
             * 
             * while ( (n = manF.read(buf)) != -1) { jos.write(buf, 0, n); }
             * 
             * manF.close();
             */

            jos.flush();
            jos.close();
            utility.delete(jarFile);
            utility.copy(tmp, jarFile);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new BuildException("Error re-ordering manifest " + ex.getMessage(), ex);
        }

    }

    public void execute() throws BuildException {
        if (!isActive()) return;

        File tmpDir = utility.getTempDir();

        try {
            if (bootclasspath == null) {
                String bcp = utility.getMidpApi(); // was: getEmptyApi()
                setBootclasspath(new Path(getProject(), bcp));
            }

            if (jadFile == null) {
                throw new BuildException("Required parameter jadfile missing");
            }

            JadFile jad = new JadFile();
            try {
                jad.load(jadFile.getAbsolutePath(), null);
            }
            catch (IOException ex) {
                throw new BuildException("Error loading JAD file", ex);
            }

            if (jarFile == null) {
                String jar = jad.getValue("MIDlet-JAR-URL");
                if (jar != null) {
                    setJarfile(new File(jadFile.getParent() + "/" + new File(jar).getName()));
                }
            }

            JadFile man = new JadFile();
            if (manifest == null) {
            	Vector excludes = new Vector();
            	for (int i = 0; m_excludeFromManifest != null && i < m_excludeFromManifest.size(); i++)
				{
            		Exclude_From_Manifest ex = (Exclude_From_Manifest)m_excludeFromManifest.get(i);
            		if (ex.name != null)
            		{
            			excludes.add(ex.name);
            		}
            		else
           			if (ex.list != null)
            		{
            			StringTokenizer tok = new StringTokenizer(ex.list, ",");
            			while(tok.hasMoreElements()) excludes.add(tok.nextToken().trim());
            		}
           			else
           				throw new BuildException("At least one of list or name must not be null in Exclude_From_Manifest");
				}
            	String eStr[] = new String[excludes.size()];
            	excludes.copyInto(eStr);
            	man.setExcludeFromManifest(eStr);
                man.assign(jad, true);
                man.setValue("MIDlet-Jar-URL", null);
                man.setValue("MIDlet-Jar-Size", null);
                man.setValue("MicroEdition-Configuration", config);
                man.setValue("MicroEdition-Profile", profile);
            }
            else {
                try {
                    man.load(manifest.getAbsolutePath(), null);
                }
                catch (IOException ex) {
                    throw new BuildException("Error opening MANIFEST.MF file ", ex);
                }
            }

            String newVersion = null;
            if (autoversion) {
                newVersion = utility.getNewVersion(jad.getValue("MIDlet-Version"));
                jad.setValue("MIDlet-Version", newVersion);
                man.setValue("MIDlet-Version", newVersion);
            }

            File manFile = (manifest == null ? new File(tmpDir + "/MANIFEST.MF") : manifest);
            try {
                man.save("" + manFile, null);
            }
            catch (IOException ex) {
                throw new BuildException("Error writing MANIFEST.MF file", ex);
            }

            super.setManifest(manFile);

            if (libclasspath != null) {
                String[] libs = libclasspath.list();
                if (libs != null) {
                    for (int i = 0; i < libs.length; i++) {
                        File lib = new File(libs[i]);

                        if (!lib.isDirectory()) {
                            ZipFileSet zip = new ZipFileSet();
                            zip.setProject(getProject());
                            zip.setSrc(lib);
                            zip.createExclude().setName("META-INF/**");
                            addZipfileset(zip);
                        }
                        else {
                            FileSet dir = new FileSet();
                            dir.setDir(lib);
                            dir.createExclude().setName("META-INF/**");
                            addFileset(dir);
                        }
                    }
                }
            }

            String cp = "" + bootclasspath;
            if (classpath.size() != 0) cp = cp + File.pathSeparator + classpath;

            super.execute();

            if (obfuscate) {
                File obfFile = new File(tmpDir + "/obfuscated.jar");
                Vector preserve = new Vector();
                utility.getPreserveList(jad, preserve);
                preserve.addAll(getPreserve());
                utility.obfuscate(jarFile, obfFile, cp, verbose, preserve, null, null, jad);

                Jar jar = new Jar();
                jar.setProject(getProject());
                jar.setTaskName(getTaskName());
                jar.setDestFile(obfFile);
                jar.setManifest(manFile);
                jar.setUpdate(true);
                jar.execute();

                utility.copy(obfFile, jarFile);
            }

            if (preverify) {
                utility.preverify(jarFile, tmpDir, cp, cldc, flags);
                utility.copy(new File(tmpDir + File.separator + jarFile.getName()), jarFile);
            }

            //re-create do the file with the non ordered manifest.
            if (keepManifestOrder) {
                addManifest(tmpDir, manFile);
            }

            if (jad.getValue("MIDlet-Jar-URL") == null) {
                jad.setValue("MIDlet-Jar-URL", "" + jarFile.getName());
            }

            jad.setValue("MIDlet-Jar-Size", "" + jarFile.length());

            log("Updating JAD file " + jadFile);
            try {
                jad.save("" + jadFile, null);
            }
            catch (IOException ex) {
                throw new BuildException("Error processing JAD file", ex);
            }
        }
        finally {
            utility.delete(tmpDir);
        }
    }
    
	public Exclude_From_Manifest createExclude_From_Manifest() {
		if (m_excludeFromManifest == null) m_excludeFromManifest = new Vector();
		Exclude_From_Manifest a = new Exclude_From_Manifest();
		m_excludeFromManifest.addElement(a);
		return a;
	}
    
    
    public static class Exclude_From_Manifest
    {
    	private String name;
    	private String list;

		public void setName(String name)
    	{
			this.name = name;
    	}
		
		public void setList(String list)
		{
			this.list = list;
		}
    }
}