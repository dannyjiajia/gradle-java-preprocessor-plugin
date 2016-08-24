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
package de.pleumann.antenna.http;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import de.pleumann.antenna.misc.JadFile;
import de.pleumann.antenna.misc.Strings;

/**
 * MIDlet OTA server.
 */
public class OTAServer extends HttpServlet {

    private File files;

    private String login;

    private String password;

    private Strings htmlpage;

    private Strings wmlpage;

    private Strings counters;

    public void init() throws ServletException {
        super.init();

        String s = getInitParameter("files");
        if (s == null) {
            files = new File(getServletContext().getRealPath("/WEB-INF/files"));
        }
        else {
            files = new File(s);
        }

        files.mkdirs();

        login = getInitParameter("login");
        password = getInitParameter("password");

        htmlpage = new Strings();
        try {
            htmlpage.loadFromFile(getServletContext().getRealPath("/WEB-INF/") + "/index.html");
        }
        catch (IOException ex) {
            try {
                htmlpage.loadFromStream(getClass().getResourceAsStream("/index.html"));
            }
            catch (IOException ignored) {
                htmlpage.clear();
            }
        }

        wmlpage = new Strings();
        try {
            wmlpage.loadFromFile(getServletContext().getRealPath("/WEB-INF/") + "/index.wml");
        }
        catch (IOException ex) {
            try {
                wmlpage.loadFromStream(getClass().getResourceAsStream("/index.wml"));
            }
            catch (IOException ignored) {
            }
        }

        counters = new Strings();
        try {
            counters.loadFromFile(getServletContext().getRealPath("/WEB-INF/") + "/counter.txt");
        }
        catch (IOException ex) {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("" + new Date() + " GET " + request.getPathInfo());

        showInfo(request);

        String name = request.getPathInfo();
        if (name == null || "/".equals(name)) {
            String accept = request.getHeader("accept");
            if (accept == null) accept = "*/*";

            boolean wml = accept.indexOf("text/vnd.wap.wml") != -1;
            boolean html = accept.indexOf("text/html") != -1 || accept.indexOf("*/*") != -1;

            if (wml && !html) {
                name = "/index.wml";
            }
            else {
                response.setStatus(HttpServletResponse.SC_SEE_OTHER);
                response.setHeader("Location", getBaseURL(request) + "/index.html");
                return;
            }
        }

        if ("/index.html".equals(name)) {
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            listFiles(request, htmlpage, writer);
        }
        else if ("/index.wml".equals(name) || "/wap".equals(name)) {
            response.setContentType("text/vnd.wap.wml");
            PrintWriter writer = response.getWriter();
            listFiles(request, wmlpage, writer);
        }
        else {
            File file = new File(files + name);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (file.getName().endsWith(".jad")) {
                response.setContentType("text/vnd.sun.j2me.app-descriptor");
                JadFile jad = new JadFile();
                jad.load(file.getAbsolutePath(), null);
                String s = new File(jad.getValue("MIDlet-Jar-URL")).getName();
                jad.setValue("MIDlet-Jar-URL", getBaseURL(request) + "/" + s);
                jad.save(response.getOutputStream());
            }
            else if (file.getName().endsWith(".jar")) {
                response.setContentType("application/java-archive");
                response.setContentLength(new Long(file.length()).intValue());
                InputStream input = new FileInputStream(file);
                OutputStream output = response.getOutputStream();
                copyStreams(input, output);
                input.close();
                output.flush();

                increaseCounter(file.getName());
            }
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        System.out.println("Ok.");
    }

    private void showInfo(HttpServletRequest request) {
        Enumeration headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String s = (String) headers.nextElement();
            System.out.println(s + "=" + request.getHeader(s));
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("" + new Date() + " PUT " + request.getPathInfo());

        if (login != null && password != null) {
            if (!login.equals(request.getParameter("login")) || !password.equals(request.getParameter("password"))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        if ("true".equals(request.getParameter("delete"))) {
            String name = request.getPathInfo();
            new File(files + name).delete();
        }
        else {
            String name = request.getPathInfo();
            if (name == null || "/".equals(name)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            File file = new File(files + name);
            OutputStream output = new FileOutputStream(file);
            InputStream input = request.getInputStream();
            copyStreams(input, output);
            output.flush();
            output.close();
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private synchronized void increaseCounter(String name) {
        try {
            String num = counters.getValue(name);
            if (num == null) num = "0";
            int i = Integer.parseInt(num) + 1;
            counters.setValue(name, "" + i);
            counters.saveToFile(getServletContext().getRealPath("/WEB-INF/") + "/counter.txt");
        }
        catch (Exception ignored) {
        }
    }

    private String getBaseURL(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    private void listFiles(HttpServletRequest request, Strings template, PrintWriter writer) throws IOException {
        int begin = template.indexOf("<!-- BEGIN -->");
        int end = template.indexOf("<!-- END -->");

        for (int i = 0; i < begin - 1; i++) {
            writer.println(template.get(i));
        }

        File[] list = files.listFiles();
        if (list != null) {
            Arrays.sort(list);

            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().endsWith(".jad")) {

                    JadFile jad = new JadFile();
                    jad.load(list[i].getAbsolutePath(), null);

                    for (int j = begin + 1; j < end; j++) {
                        StringBuffer s = new StringBuffer(template.get(j));

                        int p = s.indexOf("${");
                        while (p != -1) {
                            int q = s.indexOf("}", p + 2);
                            if (q == -1) q = p + 2;

                            String key = s.substring(p + 2, q);
                            String value = jad.getValue(key);
                            if (value == null) {
                                value = "---";
                            }

                            if ("File".equals(key)) {
                                value = getBaseURL(request) + "/" + list[i].getName();
                            }
                            else if ("Date".equals(key)) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(list[i].lastModified());
                                String monthNames = "JanFebMarAprMayJunJulAugSepOctNovDec";
                                int month = calendar.get(Calendar.MONTH);
                                String year = "" + calendar.get(Calendar.YEAR);
                                year = year.substring(2);
                                value = calendar.get(Calendar.DAY_OF_MONTH) + "-"
                                        + monthNames.substring(3 * month, 3 * month + 3) + "-" + year;
                            }
                            else if ("Counter".equals(key)) {
                                value = counters.getValue(new File(jad.getValue("MIDlet-Jar-URL")).getName());
                                if (value == null) value = "0";
                            }
                            else if ("MIDlet-Jar-URL".equals(key)) {
                                value = getBaseURL(request) + "/" + new File(value).getName();
                            }

                            value = encode(value);

                            s.replace(p, q + 1, value);
                            p = s.indexOf("${");
                        }

                        writer.println(s);
                    }
                }
            }
        }

        for (int i = end + 1; i < template.size(); i++) {
            writer.println(template.get(i));
        }
    }

    private String encode(String s) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;

                case '>':
                    result.append("&gt;");
                    break;

                case '&':
                    result.append("&amp;");
                    break;

                case '"':
                    result.append("&quot;");
                    break;

                default:
                    result.append(c);
                    break;
            }
        }

        return result.toString();
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
    
}