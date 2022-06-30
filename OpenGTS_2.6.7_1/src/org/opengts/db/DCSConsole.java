// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Device Communication Server console
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.CompileTime;

import org.opengts.db.tables.*;

public class DCSConsole
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static final String cmdPS    =  "ps";
    public static final String cmdGREP  =  "grep";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static class JavaProcess
    {
        private RTProperties javaRTP = new RTProperties();
        public JavaProcess() {
            super();
        }
        // -----------
        public String getUser() {
            return this.javaRTP.getString("$User",null);
        }
        public void setUser(String user) {
            this.javaRTP.setString("$User",user);
        }
        // -----------
        public String getPID() {
            return this.javaRTP.getString("$PID",null);
        }
        public void setPID(String pid) {
            this.javaRTP.setString("$PID",pid);
        }
        // -----------
        public String getParentPID() {
            return this.javaRTP.getString("$ParentPID",null);
        }
        public void setParentPID(String pid) {
            this.javaRTP.setString("$ParentPID",pid);
        }
        // -----------
        public String getCommand() {
            return this.javaRTP.getString("$Command",null);
        }
        public void setCommand(String pid) {
            this.javaRTP.setString("$Command",pid);
        }
        // -----------
        public long getMemory() {
            return this.javaRTP.getLong("$Memory",0L);
        }
        public void setMemory(long mem) {
            this.javaRTP.setLong("$Memory",mem);
        }
        public String getMemoryM() {
            long mb = (long)Math.ceil((double)this.getMemory() / (1024.0 * 1024.0));
            if (mb >= 300L) {
                return mb + "m";
            } else {
                return null;
            }
        }
        // -----------
        public String getClasspath() {
            return this.javaRTP.getString("$Classpath",null);
        }
        public void setClasspath(String cp) {
            this.javaRTP.setString("$Classpath",cp);
        }
        // -----------
        public String getJavaJar() {
            return this.javaRTP.getString("$JavaJar",null);
        }
        public void setJavaJar(String jar) {
            File   jarFile = FileTools.getRealFile(new File(jar), false);
            if (jarFile == null) {
                Print.logWarn("Unable to find real file: " + jar);
            }
            String jarStr  = (jarFile != null)? jarFile.toString() : jar;
            this.javaRTP.setString("$JavaJar",jarStr);
        }
        public boolean hasJavaJar() {
            return !StringTools.isBlank(this.getJavaJar());
        }
        public boolean javaJarExists() {
            return FileTools.isFile(this.getJavaJar());
        }
        // -----------
        public String getDcsName() {
            String javaJar = this.getJavaJar();
            if (!StringTools.isBlank(javaJar) && javaJar.endsWith(".jar")) {
                int p = javaJar.lastIndexOf("/");
                if (p >= 0) {
                    return javaJar.substring(p+1,(javaJar.length() - ".jar".length()));
                }
            }
            return null;
        }
        public String getGtsHome() {
            String javaJar = this.getJavaJar();
            if (!StringTools.isBlank(javaJar)) {
                int p = javaJar.indexOf("/build/lib");
                if (p > 0) {
                    String gtsHome = javaJar.substring(0,p);
                    if (FileTools.isDirectory(gtsHome)) {
                        return gtsHome;
                    }
                }
            }
            return null;
        }
        public boolean isDCS() {
            if (StringTools.isBlank(this.getDcsName())) {
                return false;
            } else
            if (StringTools.isBlank(this.getGtsHome())) {
                return false;
            } else {
                return true;
            }
        }
        // -----------
        public String getJavaClass() {
            return this.javaRTP.getString("$JavaClass",null);
        }
        public void setJavaClass(String clz) {
            this.javaRTP.setString("$JavaClass",clz);
        }
        // -----------
        public Vector<String> getJavaArgs() {
            return (Vector<String>)this.javaRTP.getProperty("$JavaArgs",null);
        }
        public void setJavaArgs(Vector<String> args) {
            this.javaRTP.setProperty("$JavaArgs",args);
        }
        // -----------
        public String getParameter(String k) {
            return this.javaRTP.getString(k,null);
        }
        public void setParameter(String k, String v) {
            this.javaRTP.setString(k,v);
        }
        // -----------
        public Map<String,String> getRunServerEnv() {
            String gtsHome = this.getGtsHome();
            if (StringTools.isBlank(gtsHome)) {
                return null;
            }
            Map<String,String> map = new HashMap<String,String>();
            map.put("GTS_HOME", gtsHome);
            return map;
        }
        public String getRunServerCmd_start() {
            String gtsHome = this.getGtsHome();
            String dcsName = this.getDcsName();
            if (StringTools.isBlank(gtsHome) || StringTools.isBlank(dcsName)) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(gtsHome + "/bin/runserver.pl");
            String mem = this.getMemoryM();
            if (!StringTools.isBlank(mem)) {
                sb.append(" -mem ").append(mem);
            }
            sb.append(" -s ").append(dcsName);
            return sb.toString();
        }
        public String getRunServerCmd_stop() {
            String gtsHome = this.getGtsHome();
            String dcsName = this.getDcsName();
            if (StringTools.isBlank(gtsHome) || StringTools.isBlank(dcsName)) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(gtsHome + "/bin/runserver.pl");
            sb.append(" -s ").append(dcsName);
            sb.append(" -kill");
            return sb.toString();
        }
        public String getRunServerCmd_restart() {
            String gtsHome = this.getGtsHome();
            String dcsName = this.getDcsName();
            if (StringTools.isBlank(gtsHome) || StringTools.isBlank(dcsName)) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(gtsHome + "/bin/runserver.pl");
            String mem = this.getMemoryM();
            if (!StringTools.isBlank(mem)) {
                sb.append(" -mem ").append(mem);
            }
            sb.append(" -s ").append(dcsName);
            sb.append(" -restart");
            return sb.toString();
        }
        // -----------
        public String toString() {
            return this.javaRTP.toString();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static Collection<JavaProcess> getJavaProcesses()
    {

        /* Collection of Java processes */
        Collection<JavaProcess> javaProcs = new Vector<JavaProcess>();

        /* get Java process list */
        String psOpt = "";
        Map<String,String> psEnv = new HashMap<String,String>();
        psEnv.put("COLUMNS","5000");
        if (OSTools.isMacOS()) {
            // ps -eo 'user,ppid,pid,command' | grep java
            psOpt = "-eo user,ppid,pid,command";
        } else
        if (OSTools.isUnixHPUX()) {
            psOpt = "-eHxo user,ppid,pid,comm";
            psEnv.put("UNIX_STD","2003");
        } else {
            psOpt = "-eHo user,ppid,pid,cmd";
        }
        String psCmd[] = StringTools.split(cmdPS + " " + psOpt,' ');
        StringBuffer stdout = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        //Print.logDebug("Command: " + StringTools.join(psCmd,' '));
        OSTools.exec(psEnv, psCmd, stdout, stderr);
        //Print.logInfo("Stdout :\n" + stdout);
        //Print.logInfo("Stderr :\n" + stderr);
        String rcds[] = StringTools.split(stdout.toString(), '\n');
        for (String rr : rcds) {
            if (rr.indexOf("java") < 0) {
                // -- Java processes only
                continue;
            } else
            if (rr.indexOf("DCSConsole") >= 0) {
                // -- but skip this process
                //continue;
            }
            // --
            JavaProcess jp = new JavaProcess();
            boolean pendingClassPath = false;
            boolean pendingJar       = false;
            boolean pendingJavaArgs  = false;
            Vector<String> javaArgs  = new Vector<String>();
            // -- tokenizer
            char rrch[] = rr.toCharArray();
            int rrp = 0;
            for (int tokenNdx = 0; ; tokenNdx++) {
                while ((rrp < rrch.length) && Character.isWhitespace(rrch[rrp])) { rrp++; } // skip WS
                // -- no more tokens?
                if (rrp >= rrch.length) {
                    break;
                }
                // -- next token
                int rrs = rrp;
                while ((rrp < rrch.length) && !Character.isWhitespace(rrch[rrp])) { rrp++; } // find WS
                String token = new String(rrch, rrs, rrp - rrs);
                // -- save token
                if (tokenNdx == 0) {
                    jp.setUser(token);
                    continue;
                } else
                if (tokenNdx == 1) {
                    jp.setPID(token);
                    continue;
                } else
                if (tokenNdx == 2) {
                    jp.setParentPID(token);
                    continue;
                } else
                if (tokenNdx == 3) {
                    jp.setCommand(token);
                    continue;
                } else
                if (token.equalsIgnoreCase("-cp") || token.equalsIgnoreCase("-classpath")) {
                    pendingClassPath = true;
                    continue;
                } else
                if (pendingClassPath) {
                    jp.setClasspath(token);
                    pendingClassPath = false;
                    continue;
                } else
                if (token.equalsIgnoreCase("-jar")) {
                    pendingJar = true;
                    continue;
                } else
                if (pendingJar) {
                    jp.setJavaJar(token);
                    pendingJar = false;
                    pendingJavaArgs = true;
                    continue;
                } else
                if (pendingJavaArgs) {
                    javaArgs.add(token);
                } else
                if (token.startsWith("-")) {
                    if (token.startsWith("-Xmx")) {
                        long mx = StringTools.parseLong(token.substring("-Xmx".length()),0L);
                        if (token.endsWith("k") || token.endsWith("K")) {
                            mx *= 1024L;
                        } else
                        if (token.endsWith("m") || token.endsWith("M")) {
                            mx *= 1024L * 1024L;
                        } else
                        if (token.endsWith("g") || token.endsWith("G")) {
                            mx *= 1024L * 1024L * 1024L;
                        }
                        jp.setMemory(mx);
                    }
                    int p = token.indexOf("=");
                    String k = (p >= 0)? token.substring(0,p) : token.substring(0);
                    String v = (p >= 0)? token.substring(p+1) : "";
                    jp.setParameter(k, v);
                } else {
                    jp.setJavaClass(token);
                    pendingJavaArgs = true;
                }
            }
            if (!ListTools.isEmpty(javaArgs)) {
                jp.setJavaArgs(javaArgs);
            }
            Print.sysPrintln("==============================");
            Print.sysPrintln(rr);
            Print.sysPrintln("---");
            Print.sysPrintln(jp.toString());
            Print.sysPrintln("DCSName  : " + jp.getDcsName());
            Print.sysPrintln("GTS_HOME : " + jp.getGtsHome());
            Print.sysPrintln("Runserver: " + jp.getRunServerCmd_start());
            javaProcs.add(jp);
        }
        return javaProcs;

    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final String ARG_LIST[]              = new String[] { "list"                        };

    /**
    *** Command-Line usage
    **/
    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + DCSConsole.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -start=<serverID>       Start DCS id");
        Print.logInfo("  -stop=<serverID>        Stop DCS id");
        Print.logInfo("  -restart=<serverID>     Restart DCS id");
        Print.logInfo("  -list                   Show running DCS modules");
        System.exit(1);
    }

    /**
    *** Command-line main entry point
    **/
    public static void main(String args[])
    {
        DBConfig.cmdLineInit(args, true);

        /* list */
        if (RTConfig.hasProperty(ARG_LIST)) {
            DCSConsole.getJavaProcesses();
            System.exit(0);
        }

    }

}
