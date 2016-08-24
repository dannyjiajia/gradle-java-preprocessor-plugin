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
package de.pleumann.antenna.misc;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Provides utility methods that are shared by the various tasks of Antenna.
 */
public class Utility {

    /**
     * Specifies the Antenna version.
     */
    public static final String ANTENNA_VERSION = "1.0.1";

    /**
     * Constant for preverifier -nonative parameter.
     */
    public static final int PREVERIFY_NONATIVE = 1;

    /**
     * Constant for preverifier -nofinalize parameter.
     */
    public static final int PREVERIFY_NOFINALIZE = 2;

    /**
     * Constant for preverifier -nofloat parameter.
     */
    public static final int PREVERIFY_NOFLOAT = 4;

    /**
     * Constant for unknown toolkit type.
     */
    public static final int TOOLKIT_UNKNOWN = 0;

    /**
     * Constant for Sun Wireless Toolkit 1.0.x.
     */
    public static final int TOOLKIT_SUN_10 = 1;

    /**
     * Constant for Sun Wireless Toolkit 2.0.x.
     */
    public static final int TOOLKIT_SUN_20 = 2;

    /**
     * Constant for Sun Wireless Toolkit 2.1.x.
     */
    public static final int TOOLKIT_SUN_21 = 3;

    /**
     * Constant for Sun Wireless Toolkit 2.2.x. added by Fred Grott(ShareMe
     * Technologies LLC)
     */
    public static final int TOOLKIT_SUN_22 = 4;

    /**
     * Constant for Sun Wireless Toolkit 2.3.x added by Fred
     * Grott(fred.grott@gmail.com, aka ShareMe Technologies LLC)
     */
    public static final int TOOLKIT_SUN_23 = 5;

    /**
     * Constant for Sun Wireless Toolkit 2.5.x added by Fred
     * Grott(fred.grott@gmail.com, aka ShareMe Technologies LLC)
     */
    public static final int TOOLKIT_SUN_25 = 6;

    /**
     * Constant for Siemens Mobility Toolkit.
     */
    public static final int TOOLKIT_SIEMENS = 10;

    /**
     * Constant for iDEN Toolkit.
     */
    public static final int TOOLKIT_IDEN = 20;

    /**
     * Constant for MPowerPlayer Toolkit.
     */
    public static final int TOOLKIT_MPOWER = 30;
    
    /**
     * Holds the type of the toolkit Antenna is used with.
     */
    private int toolkitType;

    /**
     * Holds a printable name for the toolkit.
     */
    private String toolkitName;

    /**
     * Reflects the CLDC version used.
     */
    private String cldcVersion = "1.0";

    /**
     * Reflects the MIDP version used.
     */
    private String midpVersion = "1.0";

    /**
     * Holds the project to which this utility instance belongs to.
     */
    private Project project;

    /**
     * Holds a mapping from projects to Utility instances, so we don't have to
     * create an instance for each task.
     */
    private static Hashtable instances = new Hashtable();

    /**
     * Holds the task to which this utility instance belongs to.
     */
    private Task parent;

    /**
     * Holds the temporary directory created for this Utility instance (by means
     * of the getTempDir() method).
     */
    private File tmpDir;

    /**
     * Creates an instance of the Utility class for the given project and parent
     * task.
     */
    public Utility(Project project, Task parent) {
        this.project = project;
        this.parent = parent;

        detectToolkit();
    }

    public static Utility getInstance(Project project, Task parent) {
        Utility utility = (Utility) instances.get(project);

        if (utility == null) {
            utility = new Utility(project, parent);
            instances.put(project, utility);
        }
        else {
            utility.parent = parent;
        }

        return utility;
    }

    /**
     * Tries to detect the Toolkit type Antenna is used with. Done only once,
     * since values are stored in static attributes.
     */
    public void detectToolkit() {
        /**
         * The idea here is to add WTK22 detection without disrupting the
         * current detection logic.
         * 
         * Probably check it cldcapi11.jar exists and jsr082.jar does not exist
         * for wtk21 for the changes to current first if statement.
         * 
         * Add wtk22 detection as an else if statement.
         * 
         * Or a more clean way might be to assume that WTK23 might add a new
         * optional library and thus check for existance of optional libraries
         * of wtk22 in addition to cldcapi11.jar library.
         * 
         * by Fred Grott, ShareMe Technologies LLC
         * 
         * Add WTK 23 by moving down if to else if and add new if with wtk23.
         * using existence of jsr179.jar to indicate wtk23. Added 2006 by Fred
         * Grott
         * 
         */
        if (new File(getWtkRelative("lib/cldcapi11.jar")).exists() && new File(getWtkRelative("lib/jsr226.jar")).exists()) {
            toolkitType = TOOLKIT_SUN_25;
            toolkitName = "Sun Wireless Toolkit 2.5";

            String midp = project.getProperty("wtk.midp.version");
            if (midp != null)
                midpVersion = midp;
            String cldc = project.getProperty("wtk.cldc.version");
            if (cldc != null)
                cldcVersion = cldc;

        }
        else if (new File(getWtkRelative("lib/cldcapi11.jar")).exists() && new File(getWtkRelative("lib/jsr179.jar")).exists()) {
            toolkitType = TOOLKIT_SUN_23;
            toolkitName = "Sun Wireless Toolkit 2.3";

            String midp = project.getProperty("wtk.midp.version");
            if (midp != null)
                midpVersion = midp;
            String cldc = project.getProperty("wtk.cldc.version");
            if (cldc != null)
                cldcVersion = cldc;
        }
        // changed from if to else if
        else if (new File(getWtkRelative("lib/cldcapi11.jar")).exists() && new File(getWtkRelative("lib/jsr082.jar")).exists()) {
            toolkitType = TOOLKIT_SUN_22;
            toolkitName = "Sun Wireless Toolkit 2.2";

            String midp = project.getProperty("wtk.midp.version");
            if (midp != null)
                midpVersion = midp;
            String cldc = project.getProperty("wtk.cldc.version");
            if (cldc != null)
                cldcVersion = cldc;
        }
        // changed from if to else if..no other changes made, Fred Grott
        else if (new File(getWtkRelative("lib/cldcapi11.jar")).exists()
                && new File(getWtkRelative("lib/cldcapi11.jar")).exists()) {
            toolkitType = TOOLKIT_SUN_21;
            toolkitName = "Sun Wireless Toolkit 2.1";

            String midp = project.getProperty("wtk.midp.version");
            if (midp != null)
                midpVersion = midp;
            String cldc = project.getProperty("wtk.cldc.version");
            if (cldc != null)
                cldcVersion = cldc;
        }

        else if (new File(getWtkRelative("lib/midpapi.zip")).exists() && new File(getWtkRelative("lib/wma.zip")).exists()
                && new File(getWtkRelative("lib/mmapi.zip")).exists()) {

            toolkitType = TOOLKIT_SUN_20;
            toolkitName = "Sun Wireless Toolkit 2.0";

            String midp = project.getProperty("wtk.midp.version");
            if (midp != null)
                midpVersion = midp;
        }

        else if (new File(getWtkRelative("lib/midpapi.zip")).exists()) {
            toolkitType = TOOLKIT_SUN_10;
            toolkitName = "Sun Wireless Toolkit 1.x";
        }
        /*
         * Siemens SMTK no longer stores api.jar as api.jar, it is now API.jar
         * and stored in individual lib folders per device as of SMTK3.
         * 
         * Changes to support SMTK3 added by Fred Grott
         * 
         */
        else if (new File(getWtkRelative("lib/api.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit(MIDP1.0)";
        }
        else if (new File(getWtkRelative("emulators/C6C/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit 3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/C65/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/C75/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/CX6C/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/CX7C/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/CX75/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/S75/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/SL65/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/SL75/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        else if (new File(getWtkRelative("emulators/SXG75/lib/API.jar")).exists()) {
            toolkitType = TOOLKIT_SIEMENS;
            toolkitName = "Siemens Mobility Toolkit3(MIDP2.0)";
        }
        /*
         * IDEN SDKs have the same MotoIcon and thus each sdk would show as iDEN
         * sdks with this: else if (new
         * File(getWtkRelative("bin/resources/MotoIcon.ico")).exists()) {
         * toolkitType = TOOLKIT_IDEN; toolkitName = "iDEN SDK 3.0"; }
         * 
         * Thus, Fred Grott's changes are below
         * 
         */
        else if (new File(getWtkRelative("lib/i85s/com/mot/iden/math/Float.class")).exists()) {
            toolkitType = TOOLKIT_IDEN;
            toolkitName = "iDEN SDK 3.0";
        }

        else if (new File(getWtkRelative("lib/i285/com/mot/iden/math/Float.class")).exists()) {
            toolkitType = TOOLKIT_IDEN;
            toolkitName = "iDEN SDK 4.0";
        }

        else if (new File(getWtkRelative("lib/i730/com/mot/iden/math/Float.class")).exists()) {
            toolkitType = TOOLKIT_IDEN;
            toolkitName = "iDEN i730 SDK";
        }

        /**
         * MPowerPlayer.
         */
        else if (new File(getWtkRelative("player.jar")).exists()) {
            toolkitType = TOOLKIT_MPOWER;
            toolkitName = "MPowerPlayer SDK";
        }
        else {
            toolkitType = TOOLKIT_UNKNOWN;
            toolkitName = "Unknown Toolkit";
        }

        StringBuffer s = new StringBuffer("Antenna " + ANTENNA_VERSION + " initialized for project \"" + project.getName()
                + "\"");
        while (s.length() < 58)
            s.append(' ');
        StringBuffer t = new StringBuffer("Using " + toolkitName + " (CLDC-" + cldcVersion + "; MIDP-" + midpVersion + ")");
        while (t.length() < 58)
            t.append(' ');

        parent.log("**************************************************************");
        parent.log("* " + s + " *");
        parent.log("* " + t + " *");
        parent.log("**************************************************************");
    }

    /**
     * Returns the Wireless Toolkit base path. The method fetches the path from
     * the "wtk.home" property, which needs to be specified for Antenna to work
     * properly. It also checks if the given path is correct by looking for the
     * "midpapi.zip" file.
     */
    public String getWtkHomePath() throws BuildException {
        String result = project.getProperty("wtk.home");

        if (result == null) {
            throw new BuildException("Property wtk.home needs to point to Wireless Toolkit");
        }

        if (!new File(result).exists()) {
            throw new BuildException("Property wtk.home points to non-existing directory");
        }

        return result;
    }

    /**
     * Returns the (boot-) classpath for the MIDP APIs. The method fetches the
     * path from the "wtk.midpapi" property, if specified, or defaults to
     * ${wtk.home}/lib/midpapi.zip otherwise. Handling of WTK 2.0 based on patch
     * provided by Darryl Pierce.
     */
    public String getMidpApi() throws BuildException {
        /*
         * Holds the individual files to be included in the return classpath.
         */
        List results = new ArrayList();

        /*
         * If the user has specified wtk.midpapi, then let's break it up just in
         * case it's a reference to separate JAR/ZIP files.
         */
        String midpapi = project.getProperty("wtk.midpapi");
        if (midpapi != null) {
            String[] items = new Path(project, midpapi).list();
            for (int i = 0; i < items.length; i++) {
                results.add(items[i]);
            }
        }

        /*
         * If API list is empty, set default values depending on toolkit.
         */
        if (results.size() == 0) {
            /**
             * Get WMA version in advance, make 1.0 default, strip dots.
             * 
             * (by Joerg)
             */
            String wmaVersion = project.getProperty("wtk.wma.version");
            if (wmaVersion == null) {
                wmaVersion = "10";
            }
            else {
                wmaVersion = wmaVersion.replaceAll("\\.", "");
            }

            /**
             * added if detect SUN wtk22 toolkit and changed resulting 2nd if to
             * else if
             * 
             * added by Fred Grott, ShareMe Technologies LLC
             * 
             * Changed again for WTK23 2006 by Fred Grott
             */
            if (toolkitType == TOOLKIT_SUN_25) {
                String midp = midpVersion.replaceAll("\\.", "");
                String cldc = cldcVersion.replaceAll("\\.", "");

                results.add(getWtkRelative("lib/midpapi" + midp + ".jar"));
                results.add(getWtkRelative("lib/cldcapi" + cldc + ".jar"));

                if ("true".equals(project.getProperty("wtk.mmapi.enabled"))) {
                    results.add(getWtkRelative("lib/mmapi.jar"));
                }

                if ("true".equals(project.getProperty("wtk.wma.enabled"))) {
                    /**
                     * Wrong we have both wma11 and wma20 in wtk23 long term we
                     * need something better like midp version, just hack to
                     * work for now fred.grott@gmail.com 6-25-06
                     */
                    if ("11".equals(wmaVersion)) {
                        results.add(getWtkRelative("lib/wma11.jar"));
                    }
                    else if ("20".equals(wmaVersion)) {
                        results.add(getWtkRelative("lib/wma20.jar"));
                    }
                }

                if ("true".equals(project.getProperty("wtk.j2mews.enabled"))) {
                    results.add(getWtkRelative("lib/j2me-ws.jar"));
                }

                if ("true".equals(project.getProperty("wtk.bluetooth.enabled"))) {
                    results.add(getWtkRelative("lib/jsr082.jar"));
                }

                if ("true".equals(project.getProperty("wtk.java3d.enabled"))) {
                    results.add(getWtkRelative("lib/jsr184.jar"));
                }

                if ("true".equals(project.getProperty("wtk.optionalpda.enabled"))) {
                    results.add(getWtkRelative("lib/jsr75.jar"));
                }

                /*
                 * add wtk.contenthandler.enabled, wtk.sata.enabled be careful
                 * sata is now split into several jars
                 * wtk.locationservices.enabled
                 */

                if ("true".equals(project.getProperty("wtk.contenthandler.enabled"))) {
                    results.add(getWtkRelative("lib/jsr211.jar"));
                }

                if ("true".equals(project.getProperty("wtk.satsa.enabled"))) {
                    /*
                     * satsa was split into several jars, hack to work for now
                     */
                    results.add(getWtkRelative("lib/satsa-apdu.jar"));
                    results.add(getWtkRelative("lib/satsa-crypto.jar"));
                    results.add(getWtkRelative("lib/satsa-jcrmi.jar"));
                    results.add(getWtkRelative("lib/satsa-pki.jar"));
                }

                if ("true".equals(project.getProperty("wtk.locationservices.enabled"))) {
                    results.add(getWtkRelative("lib/jsr179.jar"));
                }

                /*
                 * add stuff specifically new jars for wtk25 jsr 180 sipapi (SIP
                 * api for j2me) jsr 226 s2dvgapi (scalable 2d vector graphics
                 * api for j2me) jsr 229 papi (payment api) jsr 234 ams
                 * (advanced multimedia supplements) jsr 238 miapi (mobile
                 * internatization api)
                 */

                if ("true".equals(project.getProperty("wtk.sipapi.enabled"))) {
                    results.add(getWtkRelative("lib/jsr180.jar"));
                }

                if ("true".equals(project.getProperty("wtk.s2dvgapi.enabled"))) {
                    results.add(getWtkRelative("lib/jsr226.jar"));
                }

                if ("true".equals(project.getProperty("wtk.papi.enabled"))) {
                    results.add(getWtkRelative("lib/jsr229.jar"));
                }

                if ("true".equals(project.getProperty("wtk.ams.enabled"))) {
                    results.add(getWtkRelative("lib/jsr234.jar"));
                }

                if ("true".equals(project.getProperty("wtk.miapi.enabled"))) {
                    results.add(getWtkRelative("lib/jsr238.jar"));
                }
            }
            if (toolkitType == TOOLKIT_SUN_23) {
                String midp = midpVersion.replaceAll("\\.", "");
                String cldc = cldcVersion.replaceAll("\\.", "");

                results.add(getWtkRelative("lib/midpapi" + midp + ".jar"));
                results.add(getWtkRelative("lib/cldcapi" + cldc + ".jar"));

                if ("true".equals(project.getProperty("wtk.mmapi.enabled"))) {
                    results.add(getWtkRelative("lib/mmapi.jar"));
                }

                if ("true".equals(project.getProperty("wtk.wma.enabled"))) {
                    /**
                     * Wrong we have both wma11 and wma20 in wtk23 long term we
                     * need something better like midp version, just hack to
                     * work for now fred.grott@gmail.com 6-25-06
                     */
                    if ("11".equals(wmaVersion)) {
                        results.add(getWtkRelative("lib/wma11.jar"));
                    }
                    else if ("20".equals(wmaVersion)) {
                        results.add(getWtkRelative("lib/wma20.jar"));
                    }
                }

                if ("true".equals(project.getProperty("wtk.j2mews.enabled"))) {
                    results.add(getWtkRelative("lib/j2me-ws.jar"));
                }

                if ("true".equals(project.getProperty("wtk.bluetooth.enabled"))) {
                    results.add(getWtkRelative("lib/jsr082.jar"));
                }

                if ("true".equals(project.getProperty("wtk.java3d.enabled"))) {
                    results.add(getWtkRelative("lib/jsr184.jar"));
                }

                if ("true".equals(project.getProperty("wtk.optionalpda.enabled"))) {
                    results.add(getWtkRelative("lib/jsr75.jar"));
                }

                /*
                 * add wtk.contenthandler.enabled, wtk.sata.enabled
                 * wtk.locationservices.enabled
                 */

                if ("true".equals(project.getProperty("wtk.contenthandler.enabled"))) {
                    results.add(getWtkRelative("lib/jsr211.jar"));
                }

                if ("true".equals(project.getProperty("wtk.satsa.enabled"))) {
                    results.add(getWtkRelative("lib/jsr177.jar"));
                }

                if ("true".equals(project.getProperty("wtk.locationservices.enabled"))) {
                    results.add(getWtkRelative("lib/jsr179.jar"));
                }
            }

            else if (toolkitType == TOOLKIT_SUN_22) {
                String midp = midpVersion.replaceAll("\\.", "");
                String cldc = cldcVersion.replaceAll("\\.", "");

                results.add(getWtkRelative("lib/midpapi" + midp + ".jar"));
                results.add(getWtkRelative("lib/cldcapi" + cldc + ".jar"));

                if ("true".equals(project.getProperty("wtk.mmapi.enabled"))) {
                    results.add(getWtkRelative("lib/mmapi.jar"));
                }

                if ("true".equals(project.getProperty("wtk.wma.enabled"))) {
                    results.add(getWtkRelative("lib/wma.jar"));
                }

                if ("true".equals(project.getProperty("wtk.j2mews.enabled"))) {
                    results.add(getWtkRelative("lib/j2me-ws.jar"));
                }

                if ("true".equals(project.getProperty("wtk.bluetooth.enabled"))) {
                    results.add(getWtkRelative("lib/jsr082.jar"));
                }

                if ("true".equals(project.getProperty("wtk.java3d.enabled"))) {
                    results.add(getWtkRelative("lib/jsr184.jar"));
                }

                if ("true".equals(project.getProperty("wtk.optionalpda.enabled"))) {
                    results.add(getWtkRelative("lib/jsr75.jar"));
                }
            }

            else if (toolkitType == TOOLKIT_SUN_21) {
                String midp = midpVersion.replaceAll("\\.", "");
                String cldc = cldcVersion.replaceAll("\\.", "");

                results.add(getWtkRelative("lib/midpapi" + midp + ".jar"));
                results.add(getWtkRelative("lib/cldcapi" + cldc + ".jar"));

                if ("true".equals(project.getProperty("wtk.mmapi.enabled"))) {
                    results.add(getWtkRelative("lib/mmapi.jar"));
                }

                if ("true".equals(project.getProperty("wtk.wma.enabled"))) {
                    results.add(getWtkRelative("lib/wma.jar"));
                }

                if ("true".equals(project.getProperty("wtk.j2mews.enabled"))) {
                    results.add(getWtkRelative("lib/j2me-ws.jar"));
                }
            }

            else if (toolkitType == TOOLKIT_SUN_20) {
                results.add(getWtkRelative("lib/midpapi.zip"));

                if ("true".equals(project.getProperty("wtk.mmapi.enabled"))) {
                    results.add(getWtkRelative("lib/mmapi.zip"));
                }

                if ("true".equals(project.getProperty("wtk.wma.enabled"))) {
                    results.add(getWtkRelative("lib/wma.zip"));
                }
            }

            else if (toolkitType == TOOLKIT_SUN_10) {
                results.add(getWtkRelative("lib/midpapi.zip"));
            }
            /*
             * SMTK2 and SMTK3 store api.jar as different names and different
             * locations. Hence the changes added by Fred Grott
             * 
             */
            else if (toolkitType == TOOLKIT_SIEMENS && toolkitName == "Siemens Mobility Toolkit(MIDP1.0)") {
                results.add(getWtkRelative("lib/api.jar"));
            }
            else if (toolkitType == TOOLKIT_SIEMENS && toolkitName == "Siemens Mobility Toolkit 3(MIDP2.0)") {
                if (new File(getWtkRelative("emulators/C6C/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/C6C/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/C65/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/C65/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/C75/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/C75/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/CX6C/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/CX6C/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/CX7C/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/CX7C/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/CX75/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/CX75/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/SL65/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/SL65/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/SL75/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/SL75/lib/API.jar"));
                }
                else if (new File(getWtkRelative("emulators/SXG75/lib/API.jar")).exists()) {
                    results.add(getWtkRelative("emulators/SXG75/lib/API.jar"));
                }

            }

            /*
             * Changed to reflect other iDEn changes from:
             * 
             * else if (toolkitType == TOOLKIT_IDEN) {
             * results.add(getWtkRelative("lib/i85s")); }
             * 
             * Fred Grott added
             * 
             */
            else if (toolkitType == TOOLKIT_IDEN) {
                if (toolkitName == "iDEN SDK 3.0") {
                    results.add(getWtkRelative("lib/i85s"));
                }
                else if (toolkitName == "iDEN SDK 4.0") {
                    results.add(getWtkRelative("lib/i285"));
                }
                else if (toolkitName == "iDEN i730 SDK") {
                    results.add(getWtkRelative("lib/i730"));
                }
            }
            else if (toolkitType == TOOLKIT_MPOWER) {
                results.add(getWtkRelative("cldc.jar"));
                results.add(getWtkRelative("midp.jar"));
            }

        }

        /*
         * If API list is still empty, throw an exception.
         */
        if (results.size() == 0) {
            throw new BuildException("Toolkit type unknown. Please set property wtk.midpapi.");
        }

        /**
         * Check existence of all APIs. If something's missing, collect error
         * messages and throw an exception afterwards.
         */
        boolean ok = true;
        String message = null;
        String result = null;

        Iterator iter = results.iterator();
        while (iter.hasNext()) {
            String filename = iter.next().toString();
            File file = new File(filename);

            if (!file.exists()) {
                message = (message == null ? "Missing the following library file(s): " : message + ",")
                        + new File(filename).getName();
                ok = false;
            }
            else {
                result = (result != null ? result + File.pathSeparator : "") + filename;
            }
        }

        if (!ok) {
            throw new BuildException(message);
        }

        return result;
    }

    /**
     * Returns the (boot-) classpath for the emptied-out MIDP APIs. The method
     * fetches the path from the "wtk.emptyapi" property, if specified, or
     * defaults to ${wtk.home}/wtklib/emptyapi.zip otherwise.
     */
    private String getEmptyApi() throws BuildException {
        String result = project.getProperty("wtk.emptyapi");

        if (result == null) {
            result = getWtkRelative("wtklib/emptyapi.zip");
        }

        if (!new File(result).exists()) {
            throw new BuildException("Emptied-out MIDP API not found. Please define wtk.home or wtk.emptyapi properly.");
        }

        return result;
    }

    /**
     * Returns an absolute path for a Wireless Toolkit component (or another
     * file whose path is specified relative to "wtk.home".
     */
    public String getWtkRelative(String path) throws BuildException {
        return new File(getWtkHomePath(), path).getAbsolutePath();
    }

    /**
     * Preverifies the given file or directory.
     */
    public void preverify(File srcFile, File destDir, String classpath, boolean cldc, int flags) throws BuildException {
        parent.log("Preverifying " + srcFile);

        // WIn32 vs. Linux Fix
        String preverify;
        
        if (toolkitType == TOOLKIT_MPOWER) {
            preverify = getQuotedName(new File(getWtkRelative("osx/preverify/preverify")));
        }
        else {
            preverify = getQuotedName(new File(getWtkRelative("bin/preverify")));
        }
        
        String source = getQuotedName(srcFile);
        String target = getQuotedName(destDir);

        String arguments = "-classpath " + getOutsideQuotedPath(classpath) + " -d " + target;

        /**
         * MacOS X / MPowerPlayer special handling.
         */
        if (toolkitType == TOOLKIT_MPOWER) {
            if ("1.0".equals(cldcVersion)) {
                arguments += " -cldc1.0 ";
            }
        }
        else if (System.getProperty("os.name").startsWith("Mac")) {
            if ("1.0".equals(cldcVersion)) {
                arguments += " -cldc1.0 ";
            }
        }
        /**
         * WTK 2.1 preverifier hangs when passing old-style arguments, thus this
         * different handling.
         */
        else if (toolkitType == TOOLKIT_SUN_21 || toolkitType == TOOLKIT_SUN_22 || toolkitType == TOOLKIT_SUN_23
                || toolkitType == TOOLKIT_SUN_25) {
            arguments += " -target CLDC" + cldcVersion + " ";
        }
        /**
         * Default case.
         */
        else {
            arguments += (cldc ? " -cldc " : " ");
        }

        if ((flags & PREVERIFY_NOFINALIZE) != 0)
            arguments += "-nofinalize ";
        if ((flags & PREVERIFY_NOFLOAT) != 0)
            arguments += "-nofloat ";
        if ((flags & PREVERIFY_NONATIVE) != 0)
            arguments += "-nonative ";

        arguments += source;

        String jdk = project.getProperty("java.home");
        if (jdk.endsWith("/jre") || jdk.endsWith("\\jre")) {
            jdk = jdk.substring(0, jdk.length() - 4);
            parent.log("Adjusted Java home to " + jdk, Project.MSG_VERBOSE);
        }

        parent.log("Executable: " + preverify, Project.MSG_VERBOSE);
        parent.log("Arguments : " + arguments, Project.MSG_VERBOSE);

        try {
            String java_bin = jdk + File.separator + "bin";
            if (!new File(java_bin, "jar").exists() && !new File(java_bin, "jar.exe").exists())
            {
            	parent.log("Cannot find jar or jar.exe in " + java_bin + ", preverify will most likely fail.", Project.MSG_WARN);
            }
            
            // prepend java bin path to system PATH calling preverify
			String env[] = new String[]{
			    "PATH=" + java_bin + System.getProperty("path.separator") +	System.getenv("PATH")
			};


        	Process proc = Runtime.getRuntime().exec(preverify + " " + arguments, env);
            proc.waitFor();
            printProcessOutput(proc);

            int rc = proc.exitValue();
            if (rc != 0) {
                throw new BuildException("Preverification failed (result=" + rc + ")");
            }
        } catch (Exception ex) {
        	File log = new File(destDir, "jarlog.txt");
        	if (log.exists())
        	{
        		parent.log("Error preferifying, attempting to print "+log,Project.MSG_ERR);
	       		parent.log("====" +log+ "====",Project.MSG_ERR);
        		try
				{
					FileInputStream fin = new FileInputStream(log);
					copyStreams(fin, System.out);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
        	}
        	else
        	{
        		parent.log("Error preferifying, log file not found:  "+log,Project.MSG_ERR);
        	}
            throw new BuildException(ex);
        }
    }

    /**
     * Prints output of a process.
     */
    public void printProcessOutput(Process proc) throws IOException {

        InputStream out = proc.getInputStream();
        int c = out.read();
        while (c != -1) {
            System.out.print((char) c);
            c = out.read();
        }

        InputStream err = proc.getErrorStream();
        c = err.read();
        while (c != -1) {
            System.out.print((char) c);
            c = err.read();
        }
    }

    /**
     * Helper function to surround a filename with quotes, if necessary. Tries
     * to take into account the current OS.
     */
    public String getQuotedName(File file) {
        String quote = (File.separatorChar == '/' ? "'" : "\"");

        String name = file.getAbsolutePath();
        if ((name.indexOf(' ') != -1) && !name.startsWith(quote)) {
            return quote + name + quote;
        }
        else
            return name;
    }

    /**
     * Returns a classpath whose elements are quoted, if they contain white
     * space. Needed for ProGuard.
     */
    public String getInsideQuotedPath(String path) {
        if (path.indexOf(' ') == -1)
            return path;

        String result = "";

        int q = 0;
        int p = path.indexOf(File.pathSeparatorChar);
        while (p != -1) {
            result = result + getQuotedName(new File(path.substring(q, p))) + File.pathSeparatorChar;
            q = p + 1;
            p = path.indexOf(File.pathSeparatorChar, q);
        }
        result = result + getQuotedName(new File(path.substring(q)));

        return result;
    }

    /**
     * Returns a classpath which is fully quoted. Needed for the preverifier.
     */
    public String getOutsideQuotedPath(String path) {
        String quote = (File.separatorChar == '/' ? "'" : "\"");

        if ((path.indexOf(' ') != -1) && !path.startsWith(quote)) {
            return quote + path + quote;
        }
        else
            return path;
    }

    /**
     * Deletes the given file or directory. Works recursively, if needed.
     */
    public void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }

            if (file.equals(tmpDir)) {
                tmpDir = null;
            }
        }

        file.delete();
    }

    /**
     * Copies the given file.
     */
    public void copy(File srcFile, File destFile) throws BuildException {
        try {
            delete(destFile);
            FileUtils.getFileUtils().copyFile(srcFile, destFile);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Returns a temporary directory. The directory is created when this method
     * is first called. Subsequent calls return the same result.
     */
    public File getTempDir() {
        if (tmpDir == null) {
            Random rnd = new Random();

            do {
                tmpDir = new File(project.getBaseDir() + "/" + Integer.toHexString(rnd.nextInt() % 0x7FFFFFFF) + ".tmp");
            } while (tmpDir.exists());

            tmpDir.mkdir();
            tmpDir.deleteOnExit();
        }

        return tmpDir;
    }

    /**
     * Calculates a new version from a given version string. Returns 1.0.0, if
     * the string is empty or null.
     */
    public String getNewVersion(String version) {
        try {
            if ((version == null) || (version == "")) {
                version = "1.0.0";
            }
            else {
                int dot = version.lastIndexOf('.');
                int num = Integer.parseInt(version.substring(dot + 1)) + 1;

                version = (dot == -1) ? "" + num : version.substring(0, dot) + "." + num;
            }

            parent.log("New version is " + version);
        } catch (Exception ex) {
            parent.log("Unable to increase version number.");
        }

        return version;
    }

    /**
     * Obfuscates the given file. The function automatically determines whether
     * ProGuard or RetroGuard is available and uses the correct one (or
     * ProGuard, if both are available). The preserve Vector specifies a list of
     * classes to exclude from obfuscation. The defaultpackage parameter
     * specifies a package into which all classes should be collapsed. It is
     * supported for ProGuard only.
     */
    public void obfuscate(File srcFile, File destFile, String classpath, boolean verbose, Vector preserve, String obfuscator,
            Vector arguments, JadFile jad) throws BuildException {

        boolean pgFound = new File(getWtkRelative("bin/proguard.jar")).exists()
                || project.getProperty("wtk.proguard.home") != null;
        boolean rgFound = new File(getWtkRelative("bin/retroguard.jar")).exists()
                || project.getProperty("wtk.retroguard.home") != null;

        if (!pgFound) {
            try {
                Class.forName("proguard.ProGuard");
                pgFound = true;
            } catch (ClassNotFoundException e) {
            }
        }

        if (!rgFound) {
            try {
                Class.forName("RetroGuard");
                rgFound = true;
            } catch (ClassNotFoundException e) {
            }
        }

        try {
            if (obfuscator == null) {
                if (pgFound) {
                    proguard(srcFile, destFile, classpath, verbose, preserve, arguments, jad);
                }
                else if (rgFound) {
                    retroguard(srcFile, destFile, classpath, verbose, preserve, arguments, jad);
                }
                else {
                    throw new BuildException("No obfuscator found in WTK bin directory, CLASSPATH, or properties.");
                }
            }
            else {
                if (obfuscator.equals("proguard")) {
                    if (pgFound) {
                        proguard(srcFile, destFile, classpath, verbose, preserve, arguments, jad);
                    }
                    else {
                        throw new BuildException("ProGuard not found in WTK bin directory or CLASSPATH.");
                    }
                }
                else if (obfuscator.equals("retroguard")) {
                    if (rgFound) {
                        retroguard(srcFile, destFile, classpath, verbose, preserve, arguments, jad);
                    }
                    else {
                        throw new BuildException("RetroGuard not found in WTK bin directory or CLASSPATH.");
                    }
                }
                else {
                    throw new BuildException("\"" + obfuscator + "\" obfuscator is not supported.");
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Obfuscates the given file using RetroGuard. The preserve Vector specifies
     * a list of classes to exclude from obfuscation.
     */
    public void retroguard(File srcFile, File destFile, String classpath, boolean verbose, Vector preserve, Vector arguments,
            JadFile jad) throws IOException {
        parent.log("Obfuscating " + srcFile + " with RetroGuard");

        File script = new File(getTempDir(), "retroguard.rgs");

        PrintWriter writer = new PrintWriter(new FileOutputStream(script));
        for (int i = 0; i < preserve.size(); i++) {
            writer.println(".class " + preserve.elementAt(i).toString().replace('.', '/'));
        }

        writer.flush();
        writer.close();

        Java java = new Java();
        java.setProject(project);
        java.setTaskName(parent.getTaskName());

        String home = project.getProperty("wtk.retroguard.home");
        java.createClasspath().setPath(
                home != null ? new File(home, "retroguard.jar").getAbsolutePath() : getWtkRelative("bin/retroguard.jar"));
        /*
         * Preprend emptied-out API, because RetroGuard can't work with the
         * normal MIDP API.
         */
        java.createClasspath().setPath(getEmptyApi());
        java.createClasspath().setPath(classpath);
        java.createClasspath().setPath(System.getProperty("java.class.path"));

        java.setClassname("RetroGuard");
        java.setFailonerror(true);

        java.createArg().setLine("\"" + srcFile + "\"");
        java.createArg().setLine("\"" + destFile + "\"");
        java.createArg().setLine("\"" + script + "\"");

        if (arguments != null) {
            for (int i = 0; i < arguments.size(); i++) {
                java.createArg().setLine(" " + arguments.elementAt(i));
            }
        }

        java.setFork(true);

        int result = java.executeJava();
        if (result != 0) {
            throw new BuildException("Obfuscation failed (result=" + result + ")");
        }

        try {
            /**
             * Replace MANIFEST.MF destroyed my RetroGuard with original one.
             */
            JarFile jar = new JarFile(srcFile);
            Manifest man = jar.getManifest();
            jar.close();

            if (man != null) {
                File tmp = new File(getTempDir(), "manifest.org");
                FileOutputStream output = new FileOutputStream(tmp);
                man.write(output);
                output.flush();
                output.close();

                Jar task = new Jar();
                task.setTaskName(parent.getTaskName());
                task.setProject(project);
                task.setDestFile(destFile);
                task.setUpdate(true);
                task.setManifest(tmp);
                task.execute();
            }
        } catch (Exception ex) {
            throw new BuildException("Obfuscation failed", ex);
        }
    }

    /**
     * Obfuscates the given file using ProGuard. The preserve Vector specifies a
     * list of classes to exclude from obfuscation. The defaultpackage parameter
     * specifies a package into which all classes should be collapsed.
     */
    public void proguard(File srcFile, File destFile, String classpath, boolean verbose, Vector preserve, Vector arguments,
            JadFile jad) throws IOException {
        parent.log("Obfuscating " + srcFile + " with ProGuard");

        File script = new File(getTempDir(), "proguard.pgs");
        PrintWriter writer = new PrintWriter(new FileOutputStream(script));

        writer.println("-injars \"" + srcFile + "\"");
        writer.println("-outjar \"" + destFile + "\"");
        writer.println("-dontusemixedcaseclassnames");

        if ((classpath != null) && (!"".equals(classpath))) {
            writer.println("-libraryjars " + getInsideQuotedPath(classpath));
        }

        for (int i = 0; i < preserve.size(); i++) {
            writer.println("-keep class " + preserve.elementAt(i));
        }

        writer.flush();
        writer.close();

        Java java = new Java();
        java.setProject(project);
        java.setTaskName(parent.getTaskName());

        String home = project.getProperty("wtk.proguard.home");
        java.createClasspath().setPath(
                home != null ? new File(home + "/lib", "proguard.jar").getAbsolutePath() : getWtkRelative("bin/proguard.jar"));
        java.createClasspath().setPath(System.getProperty("java.class.path"));

        java.setClassname("proguard.ProGuard");
        java.setFailonerror(true);
        /**
         * Strange combination of quotes. Double quotes are used to tell Ant
         * that a filename with spaces is to be treated as one argument. Since
         * Ant filters out the double quotes, we also add single quotes that are
         * passed to ProGuard. They make its command parser happy.
         */
        java.createArg().setLine("\"@'" + script + "'\"");
        if (verbose) {
            java.createArg().setLine(" -verbose");
        }

        if (arguments != null) {
            for (int i = 0; i < arguments.size(); i++) {
                java.createArg().setLine(" " + arguments.elementAt(i));
            }
        }

        java.setFork(true);

        int result = java.executeJava();
        if (result != 0) {
            throw new BuildException("Obfuscation failed (result=" + result + ")");
        }
    }

    /**
     * Builds a list of classes to be excluded from obfuscation based on a given
     * JAD file.
     */
    public void getPreserveList(JadFile jad, Vector preserve) {

        if (jad != null) {
            for (int i = 1; i <= jad.getMIDletCount(); i++) {
                preserve.addElement(jad.getMIDlet(i).getClassName());
            }

            for (int i = 0; i < jad.size(); i++) {
                String key = jad.getName(i);
                if (key.startsWith("iDEN-Install-Class")) {
                    preserve.addElement(jad.getValue(key));
                }
            }
        }
    }

    /**
     * Copies the contents of the source stream to the target stream.
     */
    public static void copyStreams(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[128];
        int i = source.read(buffer);
        while (i != -1) {
            target.write(buffer, 0, i);
            i = source.read(buffer);
        }
    }

    /**
     * Fetches a list of Siemens devices from the path specified in "wtk.smtk".
     */
    public Hashtable getSiemensDevices() {
        Hashtable result = new Hashtable();

        String smtk = project.getProperty("wtk.siemensemu");

        Path path = new Path(project, smtk);
        String[] list = path.list();

        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File[] files = new File(list[i], "bin/skin_src").listFiles();
                if (files != null && files.length > 0) {
                    String device = files[0].getName().toUpperCase();
                    int dot = device.indexOf('.');
                    if (dot != -1) {
                        device = device.substring(0, dot);
                    }

                    result.put(device, list[i]);

                    System.out.println("Siemens " + device + " in " + list[i]);
                }
            }
        }

        return result;
    }

    /**
     * Interprets a file name string and replaces possible Ant properties.
     * 
     * @param str
     *            An ant style property expression
     * @return The evaluated expression
     * @throws IllegalStateException
     *             If ant project has not been set
     * @throws IllegalArgumentException
     *             If str == null
     */
    public String interpret(String str) {

        if (project == null) {
            throw new IllegalStateException("Ant project can not be null.");
        }

        if (str == null) {
            throw new IllegalArgumentException("Evaluated string can not be null");
        }

        // System.out.println("Interpreting " + str);//debug
        // Use the ant project helper to parse the file name expression
        String propValue = project.replaceProperties(str);
        // System.out.println("Interpreted as " + propValue);//debug
        // Use the ant FileUtils to turn the expression into a correct filename
        File matchingFile = project.resolveFile(propValue);
        String result = matchingFile.getAbsolutePath();
        // System.out.println("Filename is " + result);//debug
        return result;
    }

    public int getToolkitType() {
        return toolkitType;
    }

    public String getToolkitName() {
        return toolkitName;
    }

    public String getCldcVersion() {
        return cldcVersion;
    }

    public String getMidpVersion() {
        return midpVersion;
    }

    public Project getProject() {
        return project;
    }
}

/*
 * 
 * Toolkit
 * 
 * runPreverifier(classpath) runObfuscator(classpath) runEmulator(wait)
 * 
 * filterManifest(Jad)
 * 
 * getMidpApi() getEmptyApi() getDevices() getHome() getRelative()
 * 
 * getTempDir() getTempFile()
 * 
 * done
 * 
 * Toolkits for - Wtk - Midp RE - Siemens - Nokia - Motorola
 * 
 */
