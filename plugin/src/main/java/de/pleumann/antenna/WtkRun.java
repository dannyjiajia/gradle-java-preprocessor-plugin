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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import de.pleumann.antenna.misc.Conditional;
import de.pleumann.antenna.misc.JadFile;
import de.pleumann.antenna.misc.Utility;

public class WtkRun extends Task {

	private class Redirector implements Runnable {

		private InputStream input;

		public Redirector(InputStream input) {
			this.input = input;
		}

		public void run() {
			int c;
			StringBuffer s = new StringBuffer("");

			try {
				while (((c = input.read()) != -1)) {
					if (c == '\n') {
						log(s.toString());
						s = new StringBuffer("");
					}
					else if (c != '\r') {
						s.append((char) c);
					}
				}
				input.close();

				if (s.length() != 0) {
					log(s.toString());
				}
			}
			catch (IOException ignored) {
			}
		}
	}

	private Utility utility;

    private Conditional condition;
    
	private Path classpath;

	private File jadFile;

	private String device;

	private String heapsize = "1M";

	private boolean wait = true;

    private String debugAddress;

    private String trace;

	private File m_jadDirectory;
	
	private boolean m_eclipseDebugger = false;
	private String m_sourcePath;
        
	public void setClasspath(Path classpath) {
		if (this.classpath == null) {
			this.classpath = classpath;
		}
		else {
			this.classpath.append(classpath);
		}
	}

	public Path getClasspath() {
		return classpath;
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

	public void setDevice(String device) {
		this.device = device;
	}

	public void setHeapsize(String heapsize) {
		this.heapsize = heapsize;
	}

	public void setJadFile(File jadFile) {
		this.jadFile = jadFile;
	}

	public void setWait(boolean wait) {
		this.wait = wait;
	}

    public void setDebugAddress(String address) {
        this.debugAddress = address;
    }

    /**
     * @deprecated user setDebugAddress
     */
    public void setDebug(String address) {
        this.debugAddress = address;
    }
    
    public void setTrace(String trace) {
        this.trace = trace;
    }
    
	public void init() throws BuildException {
		super.init();
		utility = Utility.getInstance(getProject(), this);
        condition = new Conditional(getProject());
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
    
    public void setJadDirectory(File jadDirectory)
    {
		m_jadDirectory = jadDirectory;
    }

    private void executeSiemensEmulator(String device) throws BuildException {
        String emulator;
        
        if (device != null) {
	        emulator = utility.getWtkRelative("emulators/" + device + "/bin/emulator.exe");

	        if (!new File(emulator).exists()) {
	            emulator = utility.getWtkRelative(device + "emulators/bin/mmiu35.exe");
	        }

	        if (!new File(emulator).exists()) {
	            throw new BuildException("Siemens " + device + " emulator not found.");
	        }
        }
        else {
	        emulator = utility.getWtkRelative("/bin/emulator.exe");

	        if (!new File(emulator).exists()) {
	            throw new BuildException("Siemens emulator not found.");
	        }
        }
        
        emulator = utility.getQuotedName(new File(emulator));

        JadFile jad = new JadFile();
        try {
            jad.load(jadFile.getAbsolutePath(), null);
        }
        catch (IOException ex) {
            throw new BuildException("Unable to load JAD file", ex);
        }
        
        //File jarFile = new File(jadFile.getParentFile(), jad.getValue("MIDlet-Jar-URL"));
                
        String arguments =
            (debugAddress != null ? " /dj " : " /sj ") + utility.getQuotedName(jadFile);

        if (classpath != null) {
            arguments = arguments + " -classpath \"" + classpath + "\"";
        }

        if (trace != null) {
            arguments = arguments + " /tracing";
        }
                
        log("Running : " + emulator + " " + arguments);
        
        try {
            Process proc = Runtime.getRuntime().exec(emulator + " " + arguments);

            if (wait) {
                proc.waitFor();
                if (proc.exitValue() != 0) {
                    throw new BuildException("Emulation failed (result=" + proc.exitValue() + ")");
                }
            }
        }
        catch (IOException ex) {
            throw new BuildException("Emulation failed", ex);
        }
        catch (InterruptedException ex) {
            throw new BuildException("Emulation failed", ex);
        }
    }
/*
    private void executeIdenEmulator(String device) throws BuildException {
        String redirect = "";

        String emulator = utility.getWtkRelative("bin/emulator.exe");
//        if (!new File(emulator).exists()) {
//            emulator = utility.getWtkRelative("bin/emulator");
//            
//            if (!wait) {
//                log("Note: Calling the emulator with wait=false under Linux/Unix");
//                log("      might result in problems. If this is the case, please");
//                log("      send an e-mail to <joerg@pleumann.de>.");
//        
//                redirect = " </dev/nul >/dev/nul";
//            }
//        }

        emulator = utility.getQuotedName(new File(emulator));
        
        String arguments =
            "-Xdevice:" + device + " -Xheapsize:" + heapsize + " -Xdescriptor:" + utility.getQuotedName(jadFile) + redirect;

        if (classpath != null) {
            arguments = arguments + " -classpath \"" + classpath + "\"";
        }

        if (debugAddress != null) {
            arguments = arguments + " -Xdebug -Xrunjdwp:transport=dt_socket,address=" + debugAddress + ",server=y";
        }

        if (trace != null) {
            arguments = arguments + " -Xverbose:" + trace;
        }
                
        log("Running : " + emulator + " " + arguments);
        
        try {
            Process proc = Runtime.getRuntime().exec(emulator + " " + arguments);

            if (wait) {
                new Thread(new Redirector(proc.getInputStream())).start();
                new Thread(new Redirector(proc.getErrorStream())).start();
                
                proc.waitFor();
                if (proc.exitValue() != 0) {
                    throw new BuildException("Emulation failed (result=" + proc.exitValue() + ")");
                }
            }
        }
        catch (IOException ex) {
            throw new BuildException("Emulation failed", ex);
        }
        catch (InterruptedException ex) {
            throw new BuildException("Emulation failed", ex);
        }
    }
   */ 

    private void executeMPowerEmulator(String device) throws BuildException {
        String arguments = utility.getQuotedName(jadFile);
                
        log("Arguments : " + arguments, Project.MSG_VERBOSE);

        Java java = new Java();
        java.setProject(getProject());
        java.setTaskName(getTaskName());
        java.setFork(true);
        java.setJar(new File(utility.getWtkRelative("player.jar")));
        java.setArgs(arguments);
        java.executeJava();
    }

    private void executeDefaultEmulator(String device) throws BuildException {
        String redirect = "";
        
        String emulator = utility.getWtkRelative(wait ? "bin/emulator.exe" : "bin/emulatorw.exe");
        if (!new File(emulator).exists()) {
            emulator = utility.getWtkRelative("bin/emulator");
            
            if (!wait) {
//                log("Note: Calling the emulator with wait=false under Linux/Unix");
//                log("      might result in problems. If this is the case, please");
//                log("      send an e-mail to <joerg@pleumann.de>.");
        
                redirect = " </dev/nul >/dev/nul";
            }
        }

        emulator = utility.getQuotedName(new File(emulator));
        
        String arguments =
            "-Xdevice:" + device + " -Xheapsize:" + heapsize + " -Xdescriptor:" + utility.getQuotedName(jadFile) + redirect;

        if (classpath != null) {
            arguments = arguments + " -classpath \"" + classpath + "\"";
        }

        if (debugAddress != null) {
            arguments = arguments + " -Xdebug -Xrunjdwp:transport=dt_socket,address=" + debugAddress + ",server=y";
        }

        if (trace != null) {
            arguments = arguments + " -Xverbose:" + trace;
        }
                
        log("Running : " + emulator + " " + arguments);
        
        if (m_eclipseDebugger)
        {
        	startEclipseRemoteDebugger();
        }
        

        try {
            Process proc = Runtime.getRuntime().exec(emulator + " " + arguments);

            if (wait) {
                new Thread(new Redirector(proc.getInputStream())).start();
                new Thread(new Redirector(proc.getErrorStream())).start();
                
                proc.waitFor();
                if (proc.exitValue() != 0) {
                    throw new BuildException("Emulation failed (result=" + proc.exitValue() + ")");
                }
            }
        }
        catch (IOException ex) {
            throw new BuildException("Emulation failed", ex);
        }
        catch (InterruptedException ex) {
            throw new BuildException("Emulation failed", ex);
        }
    }
    
	private void startEclipseRemoteDebugger()
	{
		try
		{
			InetAddress address = InetAddress.getByName("localhost");
			int port = 60001;
			String srcPath = m_sourcePath == null ? "" : m_sourcePath;
			log("Launching eclispe debugger : (address="+ debugAddress+", source path="+srcPath+") -> eclipse("+ address + ":" + port+")");
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			dout.writeUTF(debugAddress);
			dout.writeUTF(srcPath);
			dout.flush();
			byte buf[] = bout.toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
		} 
		catch (Exception e)
		{
			log(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	public void execute() throws BuildException {
        if (!isActive()) return;
        
        if (m_jadDirectory != null)
        {
        	jadFile = new JadSelector(m_jadDirectory).selectJad();
        	if (jadFile == null)
        	{
        		throw new BuildException("No jad selected");
        	}
        	
        	log("Selected jad : " + jadFile);
        }
        
        if ("Siemens".equals(device) || utility.getToolkitType() == Utility.TOOLKIT_SIEMENS) {
            if ("Siemens".equals(device)) {
                device = null;
            }
            
            log("Running " + jadFile + " on Siemens " + (device == null ? "phone" : device));
            executeSiemensEmulator(device);
        }
        else if (utility.getToolkitType() == Utility.TOOLKIT_IDEN) {
            if (device == null) {
                device = "i85s";
            }

            log("Running " + jadFile + " on iDEN " + device);
            executeDefaultEmulator(device);
        }
        else if (utility.getToolkitType() == Utility.TOOLKIT_MPOWER) {
            log("Running " + jadFile + " on MPowerPlayer");
            executeMPowerEmulator(device);
        }
        else {
            if (device == null) {
                device = "DefaultColorPhone";
            }
            
			log("Running " + jadFile + " on " + device);
            executeDefaultEmulator(device);
        }
    }

	public void setEclipseDebugger(boolean startEclipseRemoteDebugger)
	{
		m_eclipseDebugger = startEclipseRemoteDebugger;
	}

	public void setSourcePath(String sourcePath)
	{
		m_sourcePath = sourcePath;
	}
}
