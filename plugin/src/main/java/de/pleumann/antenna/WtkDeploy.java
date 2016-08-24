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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.tools.ant.*;

import de.pleumann.antenna.misc.*;

/**
 * Deployment task.
 */
public class WtkDeploy extends Task {

	private File jarFile;

	private File jadFile;

	private String target;

	private boolean delete;

    private String login;

    private String password;

    private Conditional condition;

    public void init() throws BuildException {
        super.init();
        condition = new Conditional(getProject());
    }

	public void setJarfile(File file) {
		jarFile = file;
	}

	public void setJadfile(File file) {
		jadFile = file;
	}

	public void setTarget(String s) {
		target = s;
	}

	public void setDelete(boolean b) {
		delete = b;
	}

    public void setLogin(String s) {
        login = s;
    }

    public void setPassword(String s ) {
        password = s;
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
        if (!isActive()) return;

		try {
			if (jarFile == null || !jarFile.exists()) {
				throw new IllegalArgumentException("Need a JAR file.");
			}

			if (jadFile == null || !jadFile.exists()) {
				throw new IllegalArgumentException("Need a JAD file.");
			}

			if (target == null) {
                JadFile jad = new JadFile();
                jad.load(jadFile.getAbsolutePath(), null);
                
                String s = jad.getValue("MIDlet-Jar-URL");
                if (s != null && s.startsWith("http://")) {
                    int p = s.lastIndexOf('/');
                    target = s.substring(0, p);
                }
            }
            
            if (target == null) {
				throw new IllegalArgumentException("Need a deployment target.");
			}

            log("Deploying to " + target + "...");

			upload(jarFile);
			upload(jadFile);
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
	}

	private void upload(File file) throws IOException {
		log((delete ? "Deleting" : "Uploading") + " file " + file.getName());

		String s = target + "/" + file.getName() + "?delete=" + delete;
        if (login != null) {
            s = s + "&login=" + login;
        }
        if (password != null) {
            s = s + "&password=" + password;
        }
        
		URL url = new URL(s);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		connection.connect();

		if (!delete) {
			InputStream input = new FileInputStream(file);
			OutputStream output = connection.getOutputStream();
			Utility.copyStreams(input, output);
			output.flush();
			output.close();
		}

		int i = connection.getResponseCode();
		String message = connection.getResponseMessage() + " (" + i + ")";

		log(message, Project.MSG_VERBOSE);
		if (i >= 300) {
			throw new IOException(message);
		}
	}
}
