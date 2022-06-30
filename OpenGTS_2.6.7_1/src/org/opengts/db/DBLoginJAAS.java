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
//  Java Authentication and Authorization Service (JAAS) integration
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.util.*;
import java.io.*;
import java.security.Principal;

import javax.security.auth.*;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;

import org.opengts.Version;
import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.DBConfig;
import org.opengts.db.tables.*;

public class DBLoginJAAS
    implements LoginModule
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- Notes:
    // -  - Minimum Library dependencies:
    // -      gtsdb.jar
    // -      gtsutils.jar
    // -      custom.jar
    // -- Copy:
    // -    (cd $GTS_HOME/build/lib; cp gtsdb.jar gtsutils.jar custom.jar $CATALINA_HOME/webapps/idp/WEB-INF/lib/.)
    // -    (cd $GTS_HOME/build/lib; cp gtsdb.jar gtsutils.jar custom.jar $CATALINA_HOME/webapps/idp/lib/.)
    // -- Shibboleth Ant bin/build.xml:
    // <!-- GTS_HOME -->
    // <property name="GTS_HOME" value="/usr/local/gts"/>
    // ...
    // <!--
    //  CREATE WAR FILE
    // -->
    // <target name="build-war" depends="gettarget">
    //     <!-- no logging -->
    //     <echo>Rebuilding ${idp.target.dir}/war/idp.war ...</echo>
    //     <copy todir="${idp.target.dir}/webapp" overwrite="true" failonerror="false">
    //         <fileset dir="${idp.target.dir}/edit-webapp" />
    //     </copy>
    //     <!-- GTE libraries/config -->
    //     <copy todir="${idp.target.dir}/webapp/WEB-INF/lib" verbose="true" overwrite="true" failonerror="true">
    //         <fileset dir="${GTS_HOME}/build/lib">
    //             <include name="gtsdb.jar"/>
    //             <include name="gtsutils.jar"/>
    //             <include name="custom.jar"/>
    //         </fileset>
    //     </copy>
    //     <!-- build a jar, not war, since it is already fully populated -->
    //     <delete file="${idp.target.dir}/war/idp.war" failonerror="false" />
    //     <jar destfile="${idp.target.dir}/war/idp.war" basedir="${idp.target.dir}/webapp" />
    //     <echo>...done</echo>
    // </target>
    // ...
    // ------------------------------------------------------------------------

    public  static final String VERSION           = "0.2.4"; // [2.6.7-B33g]

    // ------------------------------------------------------------------------

    /* options */
    private static final String OPT_logLevel      = "logLevel";    // "error"|"warn"|"info"|"debug"|"none"
    private static final String OPT_contextName   = "contextName"; // "idp"
    private static final String OPT_contextPath   = "contextPath"; // "/usr/local/gts"
    private static final String OPT_loginType     = "loginType";   // "AccountUser"|"AccountAdmin"|"UniqueUser"|"EmailUser"|"EmailContact"

    /* defaults */
    private static final String DFT_logLevel      = "error";
    private static final String DFT_contextName   = "idp";
    private static final String DFT_contextPath   = "/usr/local/gts"; // symbolic link
    private static final String DFT_loginType     = "AccountUser";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- local implementation of utility functions (for security purposes)

    /**
    *** Local implementation of "StringTools.trim(...)"
    **/
    private static String Trim(Object obj)
    {
        return (obj != null)? obj.toString().trim() : "";
    }

    /**
    *** Local implementation of "StringTools.isBlank(...)"
    **/
    private static boolean IsBlank(String s)
    {
        return Trim(s).equals("")? true : false;
    }

    /**
    *** Local implementation of "StringTools.blankDefault(...)"
    **/
    private static String BlankDefault(String s, String dft)
    {
        return !IsBlank(s)? s : dft;
    }

    /**
    *** Local implementation of "StringTools.parseBoolean(...)"
    **/
    private static boolean ParseBoolean(String s, boolean dft)
    {
        s = Trim(s).toLowerCase();
        if (IsBlank(s)) {
            return dft;
        } else
        if (s.startsWith("true") || s.startsWith("yes")) {
            return true;
        } else
        if (s.startsWith("false") || s.startsWith("no")) {
            return false;
        } else {
            return dft;
        }
    }

    /**
    *** Local implementation of "StringTools.__eraseString(...)"
    *** Erase the contents of a String (kids, do not try this at home!)
    *** Used here for clearing passwords contained in a String.
    *** However, this does violate the String immutability rule.
    **/
    private static boolean __EraseString(String s)
        //throws SecurityException
    {
        if (s != null) {
            try {
                String valueName = "value";
                java.lang.reflect.Field valFld = String.class.getDeclaredField(valueName);
                valFld.setAccessible(true);
                Object valObj = valFld.get(s);
                if (valObj instanceof char[]) {
                    Arrays.fill((char[])valObj, '\0');
                } else {
                    //throw new SecurityException("Field '"+valueName+"' is not a char[]");
                    return false;
                }
            } catch (SecurityException se) {
                //throw se;
                return false;
            } catch (NoSuchFieldException nsfe) {
                //throw new SecurityException(nsfe); // unlikely
                return false;
            } catch (Throwable th) { 
                // -- IllegalArgumentException, IllegalAccessException, ExceptionInInitializerError
                //throw new SecurityException(th);
                return false;
            }
        }
        return true;
    }

    /**
    *** Local implementation similar to "ListTools.containsIgnoreCase(...)"
    **/
    public static boolean IsMatch(String target, String... list) 
    {
        if (list != null) {
            for (String s : list) { 
                if ((s == target) || s.equalsIgnoreCase(target)) { 
                    return true; 
                } 
            }
        }
        return false;
    }

    /**
    *** Local implementation similar to "FileTools.getRealFile(...)"
    **/
    public static File RealFile(File file, boolean rtnFileOnErr)
    {
        if ((file != null) && file.exists()) {
            try {
                // -- NOTE: may not return the actual/real file for Windows "Junction" linked directories.
                return file.getCanonicalFile();
            } catch (IOException ioe) {
                // -- ignore error
                //Print.logException("Unable to obtain RealFile: " + file);
                return rtnFileOnErr? file : null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- log-level
    // -    logLevel="error"
    // -    logLevel="warn"
    // -    logLevel="info"
    // -    logLevel="debug"

    /* logging level for this module */
    public  static final int    LOG_QUIET         = 9; // no logging
    public  static final int    LOG_ERROR         = 4; // error logging only
    public  static final int    LOG_WARN          = 3; // error/warning logging only
    public  static final int    LOG_INFO          = 2; // error/warning/info logging only
    public  static final int    LOG_DEBUG         = 1; // all logging

    /**
    *** "Print" logging
    **/
    private static void _Log(int printLevel, int frame, String msg)
    {
        Print._log(printLevel, frame+1, msg, (Object[])null);
    }

    // ------------------------------------------------------------------------

    /**
    *** Login Type
    **/
    private enum LoginType {
        ACCOUNT_USER            ("AccountUser", "Standard"),
            // -- Login requested fields:
            // -    Account:
            // -    User:
            // -    Password:
            // -- Lookup as entered AccountID and UserID
        ACCOUNT_ADMIN           ("AccountAdmin", "AccountDefault"),
            // -- Login requested fields:
            // -    Account:
            // -    Password:
            // -- Lookup as entered AccountID and 'default' UserID
            // -    Default UserID is "<Account>.getDefaultUser()" or "<PrivateLabel>.getDefaultLoginUser()"
            // -    UserID defaults to "admin" if neither Account nor PrivateLabel define a specific user.
        UNIQUE_USER_ID          ("UniqueUserID", "UniqueUser"),
            // -- Login requested fields:
            // -    User:
            // -    Password:
            // -- Lookup as unique UserID
            // -    fails if non-unique match
        EMAIL_USER_ID           ("EmailUserID", "EmailUser", "UserEmail"),
            // -- Login requested fields:
            // -    Email Address:
            // -    Password:
            // -- If email address, lookup email address as unique UserID match in User table
            // -    fails if not an email address, or non-unique match
        CONTACT_EMAIL           ("ContactEmail"),
            // -- Login requested fields:
            // -    Email Address:
            // -    Password:
            // -- If email address, lookup as unique ContactEmail match in both Account and User tables
            // -    fails if not an email address, or non-unique match
        ACCOUNT_CONTACT_EMAIL   ("AccountContactEmail"),
            // -- Login requested fields:
            // -    Email Address:
            // -    Password:
            // -- If email address, lookup as unique ContactEmail match in Account table (only)
            // -    fails if not an email address, or non-unique match
        USER_CONTACT_EMAIL      ("UserContactEmail");
            // -- Login requested fields:
            // -    Email Address:
            // -    Password:
            // -- If email address, lookup as unique ContactEmail match in User table (only)
            // -    fails if not an email address, or non-unique match
        // ---
        private String ss[] = new String[0];
        LoginType(String... s) { ss = s; }
        public boolean isMatch(String t) { return IsMatch(t,ss); }
        public String toString() { return ss[0]; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // -- logging level
    private int                 logLevel        = LOG_ERROR;

    // -- "initialize" vars
    private Subject             subject         = null;
    private CallbackHandler     callbackHandler = null;
    private Map<String,?>       sharedState     = null;
    private Map<String,?>       options         = null;
    private int                 initCount       = 0;
    private LoginType           loginType       = LoginType.ACCOUNT_USER;

    private Principal           loginPrincipal  = null;
    private boolean             didCommit       = false;

    public DBLoginJAAS()
    {
        // --
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the log-level for the specified String value
    **/
    private int getLogLevel(String LL)
    {
        if (IsMatch(LL,"error")) {
            // -- error logging
            return LOG_ERROR;
        } else
        if (IsMatch(LL,"warning","warn")) {
            // -- error/warn logging
            return LOG_WARN;
        } else
        if (IsMatch(LL,"info")) {
            // -- error/warn/info logging
            return LOG_INFO;
        } else
        if (IsMatch(LL,"debug","any","all")) {
            // -- all logging
            return LOG_DEBUG;
        } else
        if (IsMatch(LL,"none","quiet","off")) {
            // -- no logging
            return LOG_QUIET;
        } else {
            // -- unrecognized logLevel value
            int dft = LOG_WARN;
            LogWARN("[JAAS]Unrecognized log-level: " + LL + " (assuming '" + this.getLogLevelDesc(dft) + "')");
            return dft;
        }
    }

    private String getLogLevelDesc(int LL) 
    {
        switch (LL) {
            case 9 : return "none";
            case 4 : return "error";
            case 3 : return "warning";
            case 2 : return "info";
            case 1 : return "debug";
            default: return "unknown";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the LoginType for the specified String value
    **/
    private LoginType getLoginType(String LT)
    {
        if (LoginType.ACCOUNT_USER.isMatch(LT)) {
            return LoginType.ACCOUNT_USER;
        } else
        if (LoginType.ACCOUNT_ADMIN.isMatch(LT)) {
            return LoginType.ACCOUNT_ADMIN;
        } else
        if (LoginType.UNIQUE_USER_ID.isMatch(LT)) {
            return LoginType.UNIQUE_USER_ID;
        } else
        if (LoginType.EMAIL_USER_ID.isMatch(LT)) {
            return LoginType.EMAIL_USER_ID;
        } else
        if (LoginType.CONTACT_EMAIL.isMatch(LT)) {
            return LoginType.CONTACT_EMAIL;
        } else
        if (LoginType.USER_CONTACT_EMAIL.isMatch(LT)) {
            return LoginType.USER_CONTACT_EMAIL;
        } else
        if (LoginType.ACCOUNT_CONTACT_EMAIL.isMatch(LT)) {
            return LoginType.ACCOUNT_CONTACT_EMAIL;
        } else {
            // -- unrecognized loginType value, assume Account/User
            LoginType dft = LoginType.ACCOUNT_USER;
            LogWARN("[JAAS]Unrecognized LoginType: " + LT + " (assuming '" + dft + "')");
            return dft;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private void LogERROR(String msg)
    {
        this.LogERROR(msg, null);
    }

    private void LogERROR(String msg, Throwable th)
    {
        if (this.logLevel <= LOG_ERROR) {
            if (th != null) {
                Print._logStackTrace(Print.LOG_ERROR, 1, "Exception: " + msg, th);
            } else {
                _Log(Print.LOG_ERROR, 1, msg);
            }
        }
    }

    // --------------------------------

    private void LogWARN(String msg)
    {
        if (this.logLevel <= LOG_WARN) {
            _Log(Print.LOG_WARN, 1, msg);
        }
    }

    // --------------------------------

    private void LogINFO(String msg)
    {
        if (this.logLevel <= LOG_INFO) {
            _Log(Print.LOG_INFO, 1, msg);
        }
    }

    // --------------------------------

    private void LogDEBUG(String msg)
    {
        if (this.logLevel <= LOG_DEBUG) {
            //_Log(Print.LOG_DEBUG, 1, msg);
            _Log(Print.LOG_INFO, 1, msg); // <== Print.LOG_INFO is intentional here
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Initialize 
    **/
    public void initialize(
        Subject subject, CallbackHandler callbackHandler, 
        Map<String,?> sharedState, Map<String,?> options)
    {
        // --------------------------------------
        // -- cache context vars
        this.subject         = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState     = sharedState;
        this.options         = options;
        this.logLevel        = this.getLogLevel(this.getString(OPT_logLevel,DFT_logLevel));
        this.loginType       = this.getLoginType(this.getString(OPT_loginType,DFT_loginType));
        // --------------------------------------
        // -- web-service interface
        this.initCount++;
        if (!RTConfig.hasContextName()) {
            // -- not yet initialized
            LogDEBUG("[JAAS]First initialization ...");
            // --
            String ctxName = this.getString(OPT_contextName, DFT_contextName);
            if (!StringTools.isBlank(ctxName)) {
                RTConfig.setContextName(ctxName);
            } else {
                LogWARN("[JAAS]Blank Context Name");
            }
            // --
            String ctxPath = this.getString(OPT_contextPath, DFT_contextPath);
            if (!StringTools.isBlank(ctxPath)) {
                File ctxPathFile = RealFile(new File(ctxPath), true);
                if (ctxPathFile != null) {
                    RTConfig.setContextPath(ctxPathFile.toString());
                } else {
                    LogWARN("[JAAS]Invalid Context Path: " + ctxPath);
                    RTConfig.setContextPath(ctxPath);
                }
            } else {
                LogWARN("[JAAS]Blank Context Path");
            }
            // --
            if (this.logLevel == LOG_DEBUG) {
                RTConfig.setDebugMode(true);
            }
            // --
            DBConfig.servletInit(new Properties());
        } else {
            // -- already initialized
            LogINFO("[JAAS]Already initialized! ["+this.initCount+"]");
        }
        // -- log header
        LogINFO( "[JAAS]Version     : " + VERSION);
        LogINFO( "[JAAS]GTS         : " + Version.getVersion() + " [GTS_HOME="+BlankDefault(System.getenv(DBConfig.env_GTS_HOME),"n/a")+"]");
        LogINFO( "[JAAS]Login Type  : " + this.loginType);
        LogINFO( "[JAAS]Context Name: " + RTConfig.getContextName());
        LogINFO( "[JAAS]Context Path: " + RTConfig.getContextPath());
        LogDEBUG("[JAAS]Log Level   : " + this.getLogLevelDesc(this.logLevel)); // display iff DEBUG log-level
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets named option from the Options map 
    **/
	private Object _getOption(String name)
	{
	    return (this.options != null)? this.options.get(name) : null;
	}

    /**
    *** Gets named option as a String, from the Options map 
    **/
	private String getString(String name, String dft)
	{
	    String s = Trim(this._getOption(name));
	    return !IsBlank(s)? s : dft;
	}

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** AccountID callback
    **/
    private class AccountCallback
        extends NameCallback
    {
        public AccountCallback(String prompt) {
            super(prompt);
        }
        public String getAccountID() {
            String n = this.getName();
            return (n != null)? n.trim() : "";
        }
    }

    /**
    *** UserID callback
    **/
    private class UserCallback
        extends NameCallback
    {
        public UserCallback(String prompt) {
            super(prompt);
        }
        public String getUserID() {
            String n = this.getName();
            return (n != null)? n.trim() : "";
        }
    }

    /**
    *** Email Address callback
    **/
    private class EmailAddressCallback 
        extends NameCallback
    {
        public EmailAddressCallback(String prompt) {
            super(prompt);
        }
        public String getEmailAddress() {
            String n = this.getName();
            return (n != null)? n.trim() : "";
        }
    }

    // --------------------------------

    /**
    *** Container for the user credentials as entered by user at login
    **/
    private class UserCredentials
    {
        private String accountID  = "";
        private String userID     = "";
        private String emailAddr  = "";
        private char   password[] = new char[0];
        private String passwordS  = null;
        public UserCredentials(String aid, String uid, String email, char pwd[]) {
            this.accountID = (aid   != null)? aid.trim()   : "";
            this.userID    = (uid   != null)? uid.trim()   : "";
            this.emailAddr = (email != null)? email.trim() : "";
            this.password  = (pwd   != null)? pwd          : new char[0];
        }
        public String getAccountID() {
            return this.accountID;
        }
        public String getUserID() {
            return this.userID;
        }
        public String getEmailAddress() {
            return this.emailAddr;
        }
        public char[] getEnteredPassword() {
            return this.password; // non-null
        }
        public String getEnteredPasswordString() {
            char c[] = this.getEnteredPassword(); // non-null, but check anyway
            if (c != null) {
                this.passwordS = new String(c,0,c.length);
                return this.passwordS;
            } else {
                return "";
            }
        }
        public void clear() {
            this.accountID = "";
            this.userID    = "";
            this.emailAddr = "";
            Arrays.fill(this.password,'\0');
            this.password  = new char[0];
            __EraseString(this.passwordS);
            this.passwordS = null;
        }
        public String toString() {
            // -- "A=acme,U=smith,E=smith@acme.com,P=********"
            StringBuffer sb = new StringBuffer();
            if (!IsBlank(this.accountID)) {
                sb.append("A=").append(this.accountID).append(",");
            }
            if (!IsBlank(this.userID)) {
                sb.append("U=").append(this.userID).append(",");
            }
            if (!IsBlank(this.emailAddr)) {
                sb.append("E=").append(this.emailAddr).append(",");
            }
            sb.append("P=xxxxx");
            return sb.toString();
        }
    }

    /**
    *** Gets the AccountID/UserID/EMail/Password from the specified CallbackHandler
    *** Does not return null
    **/
    private UserCredentials getUserCredentials(CallbackHandler callbackHndlr, Locale locale)
        throws LoginException
    {
        I18N i18n = I18N.getI18N(DBLoginJAAS.class, locale);

        /* no callback-handler? */
        if (callbackHndlr == null) {
            LogERROR("[JAAS]'CallbackHandler' is null");
            String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
            throw new LoginException(msg);
        }

        /* callback */
        try {

            /* TODO: localize? */
            // -- localization should be performed by the Callback handler
            String promptAccount   = "Account";
            String promptUser      = "User";
            String promptEmailAddr = "EMailAddress";
            String promptPassword  = "Password";

            /* init callback credentials request */
            Vector<Callback> callbackList = new Vector<Callback>();
            switch (this.loginType) {
                case ACCOUNT_USER :
                    callbackList.add(new AccountCallback(promptAccount));
                    callbackList.add(new UserCallback(   promptUser));
                    break;
                case ACCOUNT_ADMIN :
                    callbackList.add(new AccountCallback(promptAccount));
                    break;
                case UNIQUE_USER_ID :
                    callbackList.add(new UserCallback(promptUser));
                    break;
                case EMAIL_USER_ID :
                    callbackList.add(new EmailAddressCallback(promptEmailAddr));
                    break;
                case CONTACT_EMAIL :
                    callbackList.add(new EmailAddressCallback(promptEmailAddr));
                    break;
                case USER_CONTACT_EMAIL :
                    callbackList.add(new EmailAddressCallback(promptEmailAddr));
                    break;
                case ACCOUNT_CONTACT_EMAIL :
                    callbackList.add(new EmailAddressCallback(promptEmailAddr));
                    break;
            }
            // -- Password
            callbackList.add(new PasswordCallback(promptPassword,false));
            Callback callbacks[] = callbackList.toArray(new Callback[callbackList.size()]);

            /* execute callback */
            LogDEBUG("[JAAS]Executing callback request ...");
            callbackHndlr.handle(callbacks);

            /* extract from callback response */
            LogDEBUG("[JAAS]Parsing callback response ...");
            String acctID   = "";
            String userID   = "";
            String email    = "";
            char   passwd[] = new char[0];
            for (Callback cb : callbacks) {
                if (cb instanceof AccountCallback) {
                    acctID = ((AccountCallback)cb).getAccountID();
                } else
                if (cb instanceof UserCallback) {
                    userID = ((UserCallback)cb).getUserID();
                } else 
                if (cb instanceof EmailAddressCallback) {
                    email = ((EmailAddressCallback)cb).getEmailAddress();
                } else 
                if (cb instanceof PasswordCallback) {
                    passwd = ((PasswordCallback)cb).getPassword(); // returns a "copy" 
                    ((PasswordCallback)cb).clearPassword(); // clears internal 
                }
            }

            /* return UserCredentials */
            return new UserCredentials(acctID, userID, email, passwd);

        } catch (IOException ioe) {
            LogERROR("[JAAS]IO Error", ioe);
            String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
            throw new LoginException(msg);
        } catch (UnsupportedCallbackException uce) {
            LogERROR("[JAAS]Unsupported Callback", uce);
            String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
            throw new LoginException(msg);
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Login
    **/
    public boolean login() 
        throws LoginException
    {
        LogDEBUG("[JAAS]Login ...");
        
        /* Locale */
        String localeStr = RTConfig.getString(RTKey.SESSION_LOCALE, null);
        if (StringTools.isBlank(localeStr)) {
            localeStr = RTConfig.getString(RTKey.LOCALE, null);
        }
        // --
        Locale locale = null; // TODO:
        if (!StringTools.isBlank(localeStr)) {
            //locale = account.getLocale(); 
            locale = I18N.getLocale(localeStr);
        }

        /* login */
        UserCredentials cred = null;
        boolean rtn = false;
        try {
            /// -- user credentials
            cred = this.getUserCredentials(this.callbackHandler, locale); // throws LoginException
            // -- 'cred' is non-null here
            LogDEBUG("[JAAS]UserCredentials: " + cred);
            // -- login
            rtn = this._login(cred, locale);
        } catch (LoginException le) {
            // -- re-throw exception
            throw le;
        } finally {
            if (cred != null) { // but could be null here if "getUserCredentials" fails
                cred.clear();
            }
        }

        /* return */
        return rtn;
        
    }
        
    /**
    *** Login
    **/
    private boolean _login(UserCredentials cred, Locale locale) 
        throws LoginException
    {
        I18N i18n = I18N.getI18N(DBLoginJAAS.class, locale);

        /* lookup UserInformation */
        String acctID = cred.getAccountID();
        String userID = cred.getUserID();
        String email  = cred.getEmailAddress();
        UserInformation userInfo = null;
        try {
            switch (this.loginType) {
                case ACCOUNT_USER :
                    // -- Specified AccountID and specified UserID
                    if (IsBlank(acctID)) {
                        LogWARN("[JAAS]Missing AccountID");
                        String msg = i18n.getString("DBLoginJAAS.accountNotSpecified", "Account not specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation
                        userInfo = Account.getUserInformation(acctID, userID, false/*!activeOnly*/);
                        if (userInfo == null) {
                            LogWARN("[JAAS]Account/User not found: " + acctID + "/" + userID);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        }
                    }
                    break;
                case ACCOUNT_ADMIN :
                    // -- Specified AccountID and "admin" UserID
                    if (IsBlank(acctID)) {
                        LogWARN("[JAAS]Missing AccountID");
                        String msg = i18n.getString("DBLoginJAAS.accountNotSpecified", "Account not specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation
                        userInfo = Account.getUserInformation(acctID, null/*userID*/, false/*!activeOnly*/);
                        if (userInfo == null) {
                            LogWARN("[JAAS]Account/admin not found: " + acctID + "/admin");
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        }
                    }
                    break;
                case UNIQUE_USER_ID :
                    // -- Unique UserID in User table
                    if (IsBlank(userID)) {
                        LogWARN("[JAAS]Missing UserID");
                        String msg = i18n.getString("DBLoginJAAS.userNotSpecified", "User not specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation from UserID
                        boolean activeOnly = false; // active status checked below
                        long limit = 2; // >1 to check for duplicates
                        java.util.List<User> userList = User.getAllMatchingUsers(userID, activeOnly, limit);
                        if (ListTools.isEmpty(userList)) {
                            LogWARN("[JAAS]UserID not found: " + userID);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (ListTools.size(userList) > 1) {
                            LogWARN("[JAAS]UserID not unique: " + userID);
                            String msg = i18n.getString("DBLoginJAAS.invalidLoginUnique", "Invalid Login");
                            throw new LoginException(msg);
                        }  else {
                            // -- exactly 1 entry in 'userList' (active status checked below)
                            userInfo = userList.get(0); // non-null
                        }
                    }
                    break;
                case EMAIL_USER_ID :
                    // -- Unique EmailAddress lookup as as UserID in User table
                    if (IsBlank(email)) {
                        LogWARN("[JAAS]Missing EmailAddress");
                        String msg = i18n.getString("DBLoginJAAS.emailNotSpecified", "Email Address not specified");
                        throw new LoginException(msg);
                    } else
                    if (!SendMail.validateAddress(email)) { // (email.indexOf("@") <= 0)
                        LogWARN("[JAAS]Invalid EmailAddress: " + email);
                        String msg = i18n.getString("DBLoginJAAS.emailInvalid", "Invalid Email Address specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation from UserID
                        boolean activeOnly = false; // active status checked below
                        long limit = 2; // >1 to check for duplicates
                        java.util.List<User> userList = User.getAllMatchingUsers(email,activeOnly, limit);
                        if (ListTools.isEmpty(userList)) {
                            LogWARN("[JAAS]EmailAddress UserID not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (ListTools.size(userList) > 1) {
                            LogWARN("[JAAS]EmailAddress UserID not unique: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLoginUnique", "Invalid Login");
                            throw new LoginException(msg);
                        } else {
                            // -- exactly 1 entry in 'userList' (active status checked below)
                            userInfo = userList.get(0); // non-null
                        }
                    }
                    break;
                case CONTACT_EMAIL :
                    // -- Unique Contact EmailAddress in both Account/User tables
                    if (IsBlank(email)) {
                        LogWARN("[JAAS]Missing EmailAddress");
                        String msg = i18n.getString("DBLoginJAAS.emailNotSpecified", "Email Address not specified");
                        throw new LoginException(msg); // TODO: i18n
                    } else
                    if (!SendMail.validateAddress(email)) { // (email.indexOf("@") <= 0)
                        LogWARN("[JAAS]Invalid EmailAddress: " + email);
                        String msg = i18n.getString("DBLoginJAAS.emailInvalid", "Invalid Email Address specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation from ContactEmail
                        java.util.List<String> acctList = Account.getAccountIDsForContactEmail(email);
                        java.util.List<User>   userList = User.getUsersForContactEmail(null, email);
                        int acctSize = (acctList != null)? acctList.size() : 0; // >= 0
                        int userSize = (userList != null)? userList.size() : 0; // >= 0
                        if ((acctSize + userSize) <= 0) {
                            // -- no matching email address
                            LogWARN("[JAAS]Account/User Contact EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if ((acctSize + userSize) > 1) {
                            // -- not unique
                            LogWARN("[JAAS]Account/User Contact EMailAddress not unique: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLoginUnique", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (acctSize == 1) {
                            // -- exacly 1 account match
                            userInfo = Account.getAccount(acctList.get(0)); // throws DBException
                        } else 
                        if (userSize == 1) {
                            // -- exacly 1 user match
                            userInfo = userList.get(0);
                        } else {
                            // -- will not occur
                            LogWARN("[JAAS]Should not occur: " + email);
                            userInfo = null;
                        }
                        // --
                        if (userInfo == null) {
                            // -- unlikely to occur
                            LogWARN("[JAAS]Account/User EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        }
                    } 
                    break;
                case ACCOUNT_CONTACT_EMAIL :
                    // -- Unique Contact EmailAddress in Account table (only)
                    if (IsBlank(email)) {
                        LogWARN("[JAAS]Missing EmailAddress");
                        String msg = i18n.getString("DBLoginJAAS.emailNotSpecified", "Email Address not specified");
                        throw new LoginException(msg); // TODO: i18n
                    } else
                    if (!SendMail.validateAddress(email)) { // (email.indexOf("@") <= 0)
                        LogWARN("[JAAS]Invalid EmailAddress: " + email);
                        String msg = i18n.getString("DBLoginJAAS.emailInvalid", "Invalid Email Address specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation from ContactEmail
                        java.util.List<String> acctList = Account.getAccountIDsForContactEmail(email);
                        int acctSize = (acctList != null)? acctList.size() : 0; // >= 0
                        if (acctSize <= 0) {
                            // -- no matching email address
                            LogWARN("[JAAS]Account Contact EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (acctSize > 1) {
                            // -- not unique
                            LogWARN("[JAAS]Account Contact EMailAddress not unique: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLoginUnique", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (acctSize == 1) {
                            // -- exacly 1 account match
                            userInfo = Account.getAccount(acctList.get(0)); // throws DBException
                        } else {
                            // -- will not occur
                            LogWARN("[JAAS]Should not occur: " + email);
                            userInfo = null;
                        }
                        // --
                        if (userInfo == null) {
                            // -- unlikely to occur
                            LogWARN("[JAAS]Account EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        }
                    } 
                    break;
                case USER_CONTACT_EMAIL :
                    // -- Unique Contact EmailAddress in User table (only)
                    if (IsBlank(email)) {
                        LogWARN("[JAAS]Missing EmailAddress");
                        String msg = i18n.getString("DBLoginJAAS.emailNotSpecified", "Email Address not specified");
                        throw new LoginException(msg); // TODO: i18n
                    } else
                    if (!SendMail.validateAddress(email)) { // (email.indexOf("@") <= 0)
                        LogWARN("[JAAS]Invalid EmailAddress: " + email);
                        String msg = i18n.getString("DBLoginJAAS.emailInvalid", "Invalid Email Address specified");
                        throw new LoginException(msg);
                    } else {
                        // -- get UserInformation from ContactEmail
                        java.util.List<User> userList = User.getUsersForContactEmail(null, email);
                        int userSize = (userList != null)? userList.size() : 0; // >= 0
                        if (userSize <= 0) {
                            // -- no matching email address
                            LogWARN("[JAAS]User Contact EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        } else
                        if (userSize > 1) {
                            // -- not unique
                            LogWARN("[JAAS]User Contact EMailAddress not unique: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLoginUnique", "Invalid Login");
                            throw new LoginException(msg);
                        } else 
                        if (userSize == 1) {
                            // -- exacly 1 user match
                            userInfo = userList.get(0);
                        } else {
                            // -- will not occur
                            LogWARN("[JAAS]Should not occur: " + email);
                            userInfo = null;
                        }
                        // --
                        if (userInfo == null) {
                            // -- unlikely to occur
                            LogWARN("[JAAS]User EMailAddress not found: " + email);
                            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
                            throw new LoginException(msg);
                        }
                    } 
                    break;
                default :
                    // -- unrecognized LoginType: should never occur
                    LogWARN("[JAAS]Unexpected LoginType: " + this.loginType);
                    String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
                    throw new LoginException(msg); // TODO: i18n
            }
            LogDEBUG("[JAAS]UserInformation: " + userInfo);
        } catch (DBException dbe) {
            LogERROR("[JAAS]Unable to get UserInformation: "+acctID+"/"+userID+"/"+email, dbe);
            String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
            throw new LoginException(msg); // TODO: i18n
        }
        // -- 'userInfo' is non-null here
        String uiAcctID = userInfo.getAccountID();
        String uiUserID = userInfo.getUserID();

        /* must have a parent Account */
        Account account = userInfo.getAccount(); // should NOT be null
        if (account == null) {
            // -- unlikely to occur (possible if User is an orphan)
            LogERROR("[JAAS]Parent Account not found: " + uiAcctID + "/" + uiUserID);
            String msg = i18n.getString("DBLoginJAAS.internalError", "Internal Error");
            throw new LoginException(msg);
        }

        /* check Account inactive/suspended/expired/etc */
        Account.ActiveStatus actvStat = Account.GetActiveStatus(userInfo);
        if (!actvStat.isActive()) {
            // -- Account/User is not currently active
            String actvStatDesc = actvStat.toString(locale);
            LogWARN("[JAAS]Account/User is '"+actvStatDesc+"': " + uiAcctID + "/" + uiUserID);
            String msg = i18n.getString("DBLoginJAAS.loginProhibited", "Login prohibited ({0})", actvStatDesc);
            throw new FailedLoginException(msg);
        }

        /* check password */
        BasicPrivateLabel bpl = null; // account.getPrivateLabel(); <-- would be redundant
        boolean suspend = true; // temporarily suspend account/user on too many login failures
        if (!userInfo.checkPassword(bpl,cred.getEnteredPasswordString(),suspend)) {
            LogWARN("[JAAS]'checkPassword' failed: " + uiAcctID + "/" + uiUserID);
            String msg = i18n.getString("DBLoginJAAS.invalidLogin", "Invalid Login");
            throw new FailedLoginException(msg);
        }

        /* successful login */
        this.loginPrincipal = userInfo.getPrincipal(false/*no-cache*/);
        LogDEBUG("[JAAS]Login successful: " + uiAcctID + "/" + uiUserID);
        return true;

    }

    // ------------------------------------------------------------------------

    /**
    *** Commit
    **/
    public boolean commit() 
        throws LoginException
    {
        LogDEBUG("[JAAS]Commit ...");
        // -- no commit if we haven't logged in?
        if (this.loginPrincipal == null) {
            LogWARN("[JAAS]'commit' called when user hasn't logged in yet!");
            return false;
        }
        // -- add login Principal to Subject
        if (this.subject != null) {
            this.subject.getPrincipals().add(this.loginPrincipal);
        } else {
            LogERROR("[JAAS]'Subject' is null");
        }
        this.didCommit = true;
        return true;
    }

    // ------------------------------------------------------------------------

    /**
    *** Abort
    **/
    public boolean abort() 
        throws LoginException
    {
        LogDEBUG("[JAAS]Abort ...");
        if (this.loginPrincipal == null) {
            LogINFO("[JAAS]'abort' called when user hasn't logged in yet");
            return false;
        } else 
        if (!this.didCommit) {
            this.loginPrincipal = null;
        } else {
            this.logout();
        }
        return true;
    }

    // ------------------------------------------------------------------------

    /**
    *** Logout
    **/
    public boolean logout() 
        throws LoginException
    {
        LogDEBUG("[JAAS]Logout ...");
        if (this.loginPrincipal != null) {
            if (this.subject != null) {
                this.subject.getPrincipals().remove(this.loginPrincipal);
            } else {
                LogERROR("[JAAS]'Subject' is null");
            }
            this.loginPrincipal = null;
        }
        this.didCommit = false;
        return true;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- debug command-line testing

    private static String ARG_VERSION[]     = { "version" , "v" };
    private static String ARG_ACCOUNT[]     = { "account" , "a" };
    private static String ARG_USER[]        = { "user"    , "u" };
    private static String ARG_EMAIL[]       = { "email"   , "e" };
    private static String ARG_PASSWORD[]    = { "password", "p" };

    private static class TestCallback implements CallbackHandler
    {
        public String accountID = null;
        public String userID    = null;
        public String emailAddr = null;
        public char   passwd[]  = null;
        public TestCallback(String accountID, String userID, String email, char passwd[]) {
            this.accountID = accountID;
            this.userID    = userID;
            this.emailAddr = email;
            this.passwd    = passwd;
        }
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                Callback _cb = callbacks[i];
                if (_cb instanceof TextOutputCallback) {
                    TextOutputCallback cb = (TextOutputCallback)_cb;
                    switch (cb.getMessageType()) {
                        case TextOutputCallback.INFORMATION:
                            Print.sysPrintln("[Main]INFO: " + cb.getMessage());
                            break;
                        case TextOutputCallback.ERROR:
                            Print.sysPrintln("[Main]ERROR: " + cb.getMessage());
                            break;
                        case TextOutputCallback.WARNING:
                            Print.sysPrintln("[Main]WARN: " + cb.getMessage());
                            break;
                        default:
                            Print.sysPrintln("[Main]UNKNOWN:"+cb.getMessageType()+": " + cb.getMessage());
                            break;
                    }
                } else 
                if (_cb instanceof AccountCallback) {
                    AccountCallback cb = (AccountCallback)_cb;
                    cb.setName(this.accountID);
                    Print.sysPrintln("[Main]Prompt: " + cb.getPrompt() + " ==> " + cb.getName());
                } else 
                if (_cb instanceof UserCallback) {
                    UserCallback cb = (UserCallback)_cb;
                    cb.setName(this.userID);
                    Print.sysPrintln("[Main]Prompt: " + cb.getPrompt() + " ==> " + cb.getName());
                } else 
                if (_cb instanceof EmailAddressCallback) {
                    EmailAddressCallback cb = (EmailAddressCallback)_cb;
                    cb.setName(this.emailAddr);
                    Print.sysPrintln("[Main]Prompt: " + cb.getPrompt() + " ==> " + cb.getName());
                } else 
                if (_cb instanceof PasswordCallback) {
                    PasswordCallback cb = (PasswordCallback)_cb;
                    cb.setPassword(this.passwd);
                    Print.sysPrintln("[Main]Prompt: " + cb.getPrompt() + " ==> " + "*****");
                } else {
                    throw new UnsupportedCallbackException(_cb, "Unrecognized Callback"); // debug/test
                }
            }
        }
    }

    /**
    *** Command-line entry point 
    **/
    public static void main(String argv[]) 
    {
        // -- bin/exeJava -Djava.security.auth.login.config=$GTS_HOME/jaas.config org.opengts.db.DBLoginJAAS -a=ACCOUNT -u=USER -p=PASS
        // -- bin/exeJava -Djava.security.auth.login.config=$GTS_HOME/jaas.config org.opengts.db.DBLoginJAAS -a=demo -u=smith -p=jones
        // -- Requires the following in a JAAS config file (ie "$GTS_HOME/jaas.config")
        // -    ShibUserPassAuth  {
        // -        org.opengts.db.DBLoginJAAS required 
        // -        contextName="idp"
        // -        contextPath="/usr/local/gts"
        // -        loginType="AccountUser"
        // -        logLevel="debug";
        // -    };
        DBConfig.cmdLineInit(argv,true);  // main
        String acctID   = Trim(RTConfig.getString(ARG_ACCOUNT ,""));
        String userID   = Trim(RTConfig.getString(ARG_USER    ,""));
        String email    = Trim(RTConfig.getString(ARG_EMAIL   ,""));
        char   passwd[] =      RTConfig.getString(ARG_PASSWORD,"").toCharArray();
        Print.sysPrintln(""); // blank line separator
        
        /* version? */
        if (RTConfig.getBoolean(ARG_VERSION,false)) {
            Print.sysPrintln("[JAAS]Version: " + VERSION);
            Print.sysPrintln("[JAAS]GTS    : " + Version.getVersion() + " [GTS_HOME="+BlankDefault(System.getenv(DBConfig.env_GTS_HOME),"n/a")+"]");
            System.exit(0);
        }

        /* create LoginContext */
        LoginContext loginCtx = null;
        try {
            String name = "ShibUserPassAuth"; // matches name in "jaas.config"
            TestCallback tcb = new TestCallback(acctID, userID, email, passwd);
            loginCtx = new LoginContext(name, tcb);
        } catch (Throwable th) { // LoginException, SecurityException
            Print.sysPrintln("[JAAS:Main]ERROR: Unable to create LoginContext: " + th);
            System.exit(1);
        }

        /* authenticate */
        try {
            loginCtx.login();
            Print.sysPrintln("[JAAS:Main]INFO: Login success!");
        } catch (Throwable th) { // FailedLoginException, LoginException
            Print.sysPrintln("[JAAS:Main]ERROR: Login failed: " + th);
            System.exit(1);
        }

        /* logout */
        try {
            loginCtx.logout();
        } catch (Throwable th) { // LoginException
            Print.sysPrintln("[JAAS:Main]ERROR: Logout failed: " + th);
            System.exit(1);
        }

        /* done */
        Print.sysPrintln(""); // blank line separator

    }

}
