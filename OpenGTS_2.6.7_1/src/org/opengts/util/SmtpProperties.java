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
//  SMTP properties container
// ----------------------------------------------------------------------------
// Change History:
//  2017/03/14  Martin D. Flynn
//     -Initial release (extracted from SendMail.java)
//  2017/10/09  Martin D. Flynn
//     -Added support for KEY_SEND_PARTIAL
//  2018/09/10  GTS Development Team
//     -Added "isIgnoredEmailAddress(...)", moved here from SendMailArgs.java
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

/**
*** SMTP properties container
**/

public class SmtpProperties
{

    // ------------------------------------------------------------------------

    /* SMTP property key name prefix */
    private static final String SMTP_   = "smtp.";

    private static String Abbr(String smtpKey) 
    {
        if (smtpKey.startsWith(SMTP_)) {
            return smtpKey.substring(SMTP_.length());
        } else {
            return smtpKey;
        }
    }

    /* SMTP property keys */
    // -- Element size must be at least 2:
    // -   0  : Contains the full property key name
    // -   1  : Contains an abbreviated property key name, without the prefixing "smtp."
    // -   2+ : [optional] contains additional abbreviated property key names
    // -- Property key abbreviations are only used when reading SMTP properties defined by an account/user
    public static final String KEY_DEBUG[]               = { RTKey.SMTP_DEBUG              , Abbr(RTKey.SMTP_DEBUG              )                  };
    public static final String KEY_SERVER_HOST[]         = { RTKey.SMTP_SERVER_HOST        , Abbr(RTKey.SMTP_SERVER_HOST        )                  };
    public static final String KEY_SERVER_PORT[]         = { RTKey.SMTP_SERVER_PORT        , Abbr(RTKey.SMTP_SERVER_PORT        )                  };
    public static final String KEY_SERVER_USER[]         = { RTKey.SMTP_SERVER_USER        , Abbr(RTKey.SMTP_SERVER_USER        )                  };
    public static final String KEY_SERVER_USER_EMAIL[]   = { RTKey.SMTP_SERVER_USER_EMAIL  , Abbr(RTKey.SMTP_SERVER_USER_EMAIL  ), "user.email"    };
    public static final String KEY_SERVER_USER_NAME[]    = { RTKey.SMTP_SERVER_USER_NAME   , Abbr(RTKey.SMTP_SERVER_USER_NAME   ), "user.name"     };
    public static final String KEY_SERVER_PASSWORD[]     = { RTKey.SMTP_SERVER_PASSWORD    , Abbr(RTKey.SMTP_SERVER_PASSWORD    ), "pass"          };
    public static final String KEY_AUTH_METHOD[]         = { RTKey.SMTP_AUTH_METHOD        , Abbr(RTKey.SMTP_AUTH_METHOD        ), "authMeth"      }; // [2.6.7-B46r]
    public static final String KEY_OAUTH2_TOKEN_CLASS[]  = { RTKey.SMTP_OAUTH2_TOKEN_CLASS  , Abbr(RTKey.SMTP_OAUTH2_TOKEN_CLASS  ), "oauth.class"   }; // [2.6.7-B46n]
    public static final String KEY_OAUTH2_PROVIDER_URL[] = { RTKey.SMTP_OAUTH2_PROVIDER_URL , Abbr(RTKey.SMTP_OAUTH2_PROVIDER_URL ), "oauth.client"  }; // [2.6.7-B46n]
    public static final String KEY_OAUTH2_CLIENT_ID[]    = { RTKey.SMTP_OAUTH2_CLIENT_ID    , Abbr(RTKey.SMTP_OAUTH2_CLIENT_ID    ), "oauth.client"  }; // [2.6.7-B46n]
    public static final String KEY_OAUTH2_SECRET[]       = { RTKey.SMTP_OAUTH2_SECRET       , Abbr(RTKey.SMTP_OAUTH2_SECRET       ), "oauth.secret"  }; // [2.6.7-B46n]
    public static final String KEY_OAUTH2_REFRESH_TOKEN[]= { RTKey.SMTP_OAUTH2_REFRESH_TOKEN, Abbr(RTKey.SMTP_OAUTH2_REFRESH_TOKEN), "oauth.refresh" }; // [2.6.7-B46n]
    public static final String KEY_OAUTH2_ACCESS_TOKEN[] = { RTKey.SMTP_OAUTH2_ACCESS_TOKEN , Abbr(RTKey.SMTP_OAUTH2_ACCESS_TOKEN ), "oauth.access"  }; // [2.6.7-B46n]
    public static final String KEY_ENABLE_SSL[]          = { RTKey.SMTP_ENABLE_SSL         , Abbr(RTKey.SMTP_ENABLE_SSL         ), "SSL"           };
    public static final String KEY_ENABLE_TLS[]          = { RTKey.SMTP_ENABLE_TLS         , Abbr(RTKey.SMTP_ENABLE_TLS         ), "TLS"           };
    public static final String KEY_SERVER_TIMEOUT_MS[]   = { RTKey.SMTP_SERVER_TIMEOUT_MS  , Abbr(RTKey.SMTP_SERVER_TIMEOUT_MS  )                  };
    public static final String KEY_SERVER_RETRY_COUNT[]  = { RTKey.SMTP_SERVER_RETRY_COUNT , Abbr(RTKey.SMTP_SERVER_RETRY_COUNT ), "retry"         };
    public static final String KEY_SEND_PARTIAL[]        = { RTKey.SMTP_SEND_PARTIAL       , Abbr(RTKey.SMTP_SEND_PARTIAL       )                  };
    public static final String KEY_MULTIPART_TYPE[]      = { RTKey.SMTP_MULTIPART_TYPE     , Abbr(RTKey.SMTP_MULTIPART_TYPE     ), "multipart"     };
    public static final String KEY_BCC_EMAIL[]           = { RTKey.SMTP_BCC_EMAIL          , Abbr(RTKey.SMTP_BCC_EMAIL          ), "bcc.email"     };
    public static final String KEY_SYSADMIN_EMAIL[]      = { RTKey.SMTP_SYSADMIN_EMAIL     , Abbr(RTKey.SMTP_SYSADMIN_EMAIL     ), "sysadmin.email"};
    public static final String KEY_THREAD_MODEL[]        = { RTKey.SMTP_THREAD_MODEL       , Abbr(RTKey.SMTP_THREAD_MODEL       )                  };
    public static final String KEY_THREAD_MODEL_SHOW[]   = { RTKey.SMTP_THREAD_MODEL_SHOW  , Abbr(RTKey.SMTP_THREAD_MODEL_SHOW  )                  };
    public static final String KEY_IGNORED_EMAIL_FILE[]  = { RTKey.SMTP_IGNORED_EMAIL_FILE , Abbr(RTKey.SMTP_IGNORED_EMAIL_FILE )                  };
    public static final String KEY_FROM_EMAILADDRESS_[]  = { RTKey.SMTP_FROM_EMAILADDRESS_ , Abbr(RTKey.SMTP_FROM_EMAILADDRESS_ )                  };
    public static final String KEY_TO_EMAILADDRESS_[]    = { RTKey.SMTP_TO_EMAILADDRESS_   , Abbr(RTKey.SMTP_TO_EMAILADDRESS_   )                  };

    /**
    *** Contains all SMTP properties
    **/
    public static final String KEY_PROPERTIES[][] = {
        KEY_DEBUG                ,
        KEY_SERVER_HOST          ,
        KEY_SERVER_PORT          ,
        KEY_SERVER_USER          ,
        KEY_SERVER_USER_EMAIL    ,
        KEY_SERVER_USER_NAME     ,
        KEY_SERVER_PASSWORD      ,
        KEY_AUTH_METHOD          , // [2.6.7-B46r]
        KEY_OAUTH2_TOKEN_CLASS    , // [2.6.7-B46n]
        KEY_OAUTH2_PROVIDER_URL   , // [2.6.7-B46n]
        KEY_OAUTH2_CLIENT_ID      , // [2.6.7-B46n]
        KEY_OAUTH2_SECRET         , // [2.6.7-B46n]
        KEY_OAUTH2_REFRESH_TOKEN  , // [2.6.7-B46n]
        KEY_OAUTH2_ACCESS_TOKEN   , // [2.6.7-B46n]
        KEY_ENABLE_SSL           ,
        KEY_ENABLE_TLS           ,
        KEY_SERVER_TIMEOUT_MS    ,
        KEY_SERVER_RETRY_COUNT   ,
        KEY_SEND_PARTIAL         ,
        KEY_MULTIPART_TYPE       ,
        KEY_BCC_EMAIL            , // may not be supported
        KEY_SYSADMIN_EMAIL       ,
        KEY_THREAD_MODEL         ,
        KEY_THREAD_MODEL_SHOW    ,
        KEY_IGNORED_EMAIL_FILE   ,
    };

    /**
    *** Contains only User configurable SMTP properties
    **/
    public static final String KEY_USER_PROPERTIES[][] = {
        KEY_DEBUG                ,
        KEY_SERVER_HOST          ,
        KEY_SERVER_PORT          ,
        KEY_SERVER_USER          ,
        KEY_SERVER_USER_EMAIL    ,
        KEY_SERVER_USER_NAME     ,
        KEY_SERVER_PASSWORD      ,
        KEY_ENABLE_SSL           ,
        KEY_ENABLE_TLS           ,
        KEY_SERVER_TIMEOUT_MS    ,
        KEY_SERVER_RETRY_COUNT   ,
        KEY_SEND_PARTIAL         ,
        KEY_MULTIPART_TYPE       ,
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public  static final String THREAD_UNDEFINED        = "undefined";
    public  static final int    _THREAD_UNDEFINED       = -2;
    public  static final String THREAD_NONE             = "none";
    public  static final int    _THREAD_NONE            = -1;
    public  static final String THREAD_CURRENT          = "current";
    public  static final int    _THREAD_CURRENT         = 0;
    public  static final String THREAD_POOL             = "pool"; // preferred
    public  static final int    _THREAD_POOL            = 1;
    public  static final String THREAD_NEW              = "new";
    public  static final int    _THREAD_NEW             = 2;
    public  static final String THREAD_DEBUG            = "debug";
    public  static final int    _THREAD_DEBUG           = 3;

    public enum ThreadModel implements EnumTools.IntValue {
        UNDEFINED (-2,false,"undefined"), // THREAD_UNDEFINED
        NONE      (-1,false,"none"     ), // THREAD_NONE
        CURRENT   ( 0,false,"current"  ), // THREAD_CURRENT
        POOL      ( 1,true ,"pool"     ), // THREAD_POOL
        NEW       ( 2,true ,"new"      ), // THREAD_NEW
        DEBUG     ( 3,false,"debug"    ); // THREAD_DEBUG
        private int     vv = 0;
        private boolean tt = false;
        private String  ss = null;
        ThreadModel(int v, boolean t, String s) { vv = v; tt = t; ss = s; }
        public int     getIntValue()            { return vv; }
        public boolean isSeparateThread()       { return tt; }
        public String  toString()               { return ss; }
    };

    /**
    *** Gets the ThreadModel enum value for the specified name
    *** @param code The name of the ThreadModel
    *** @param dft  The default ThreadModel if the specified name is invalid.
    *** @return The ThreadModel, or the specified default if the name is invalid
    **/
    public static ThreadModel getThreadModel(String code, ThreadModel dft)
    {
        return EnumTools.getValueOf(ThreadModel.class, code, dft);
    }

    /**
    *** Gets the ThreadModel enum value for the specified name
    *** @param code The name of the ThreadModel
    *** @return The ThreadModel, or the default POOL if 
    **/
    public static ThreadModel getThreadModel(String code)
    {
        return SmtpProperties.getThreadModel(code, ThreadModel.POOL);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the global thread-model
    **/
    public static void setGlobalThreadModel(String model)
    {
        if (!StringTools.isBlank(model)) {
            RTConfig.setString(RTKey.SMTP_THREAD_MODEL, model);
        }
    }

    /**
    *** Sets the global thread-model
    **/
    public static void setGlobalThreadModel(String model, boolean show)
    {
        SmtpProperties.setGlobalThreadModel(model);
        RTConfig.setBoolean(RTKey.SMTP_THREAD_MODEL_SHOW, show);
    }

    // --------------------------------

    /**
    *** Sets the global thread-model
    **/
    public static void setGlobalThreadModel(ThreadModel model)
    {
        if (model != null) {
            RTConfig.setString(RTKey.SMTP_THREAD_MODEL, model.toString());
        }
    }

    /**
    *** Sets the global thread-model
    **/
    public static void setGlobalThreadModel(ThreadModel model, boolean show)
    {
        SmtpProperties.setGlobalThreadModel(model);
        RTConfig.setBoolean(RTKey.SMTP_THREAD_MODEL_SHOW, show);
    }

    // --------------------------------

    /**
    *** Sets the global thread-model to NONE
    **/
    public static void setGlobalThreadModel_NONE()
    {
        SmtpProperties.setGlobalThreadModel(ThreadModel.NONE);
    }

    /**
    *** Sets the global thread-model to DEBUG
    **/
    public static void setGlobalThreadModel_DEBUG()
    {
        SmtpProperties.setGlobalThreadModel(ThreadModel.DEBUG);
    }

    /**
    *** Sets the global thread-model to CURRENT
    **/
    public static void setGlobalThreadModel_CURRENT()
    {
        SmtpProperties.setGlobalThreadModel(ThreadModel.CURRENT);
    }

    /**
    *** Sets the global thread-model to POOL
    **/
    public static void setGlobalThreadModel_POOL()
    {
        SmtpProperties.setGlobalThreadModel(ThreadModel.POOL);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SMTP Properties:
    //  smtp.debug=[true|false]                 SMTP_DEBUG
    //  smtp.host=[HOSTNAME]                    SMTP_SERVER_HOST
    //  smtp.port=[1..65535]                    SMTP_SERVER_PORT
    //  smtp.user=[USERNAME]                    SMTP_SERVER_USER
    //  smtp.user.emailAddress=[USER@DOMAIN]    SMTP_SERVER_USER_EMAIL
    //  smtp.password=[PASSWORD]                SMTP_SERVER_PASSWORD
    //  smtp.enableSSL=[true|false|only]        SMTP_ENABLE_SSL
    //  smtp.enableTLS=[true|false|only]        SMTP_ENABLE_TLS
    //  smtp.timeout=[milliseconds]             SMTP_SERVER_TIMEOUT_MS
    //  smtp.retryCount=[retryCount]            SMTP_SERVER_RETRY_COUNT
    //  smtp.multipartType=[multipartType]      SMTP_MULTIPART_TYPE
    // ------------------------------------------------------------------------

    private String            name              = "";
    private RTProperties      smtpProps         = null;
    private SmtpProperties    smtpDelegate      = null;

    private boolean           ignFileInit       = false;
    private File              ignFilePath       = null;
    private long              ignFileModTime    = 0L;
    private Set<String>       ignFileSet        = null;
    private Set<String>       ignLocalSet       = null;

    private boolean           ignEmailDebug     = false;
    
    private OAuth2AccessToken oauth2AccessToken = null;

    /**
    *** Constructor (default values)
    *** @param name  The name of this instance (for debugging purposes)
    **/
    public SmtpProperties(String name) 
    {
        super();
        this.name      = StringTools.trim(name);
        this.smtpProps = new RTProperties();
    }

    /**
    *** Constructor
    *** @param name     The name of this instance (for debugging purposes)
    *** @param smtpRTP  RTProperties instance from which the SMTP properties are copied
    *** @param userFilter  True to copy only user-level SMTP properties
    **/
    public SmtpProperties(String name, RTProperties smtpRTP, boolean userFilter) 
    {
        this(name);
        if (smtpRTP != null) {
            String smtpKeys[][] = userFilter? KEY_USER_PROPERTIES : KEY_PROPERTIES;
            for (String K[] : smtpKeys) {
                String V = smtpRTP.getString(K, null);
                if (V != null) {
                    this.smtpProps.setString(K[0], V); // "smtp."
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a deep copy of this instance
    *** @param userFilter  True to copy only user-level SMTP properties
    *** @return A copy of this instance
    **/
    public SmtpProperties copy(boolean userFilter)
    {
        String name = this.getName()+"(Copy)";
        return new SmtpProperties(name, this.smtpProps, userFilter);
    }

    // ------------------------------------------------------------------------

    /**
    *** The delegate SmtpProperties instance to check if this instance does not
    *** contain the requested property value
    *** @param smtp  The delegate SmtpProperties instance
    **/
    public SmtpProperties setDelegate(SmtpProperties smtp) 
    {
        this.smtpDelegate = smtp;
        return this; // for chaining
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the name of this SMtpProperties instance (for debugging purposes)
    **/
    public SmtpProperties setName(String name)
    {
        this.name = StringTools.trim(name);
        return this; // for chaining
    }

    /**
    *** Gets the name of this SMtpProperties instance (for debugging purposes)
    **/
    public String getName()
    {
        return this.name;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the specified property from this instance
    *** @param keys      The key of the property value to return 
    ***                  (first element is the full property name, the second element is the abbreviation)
    *** @param delegate  True to allow checking property value sources (ie. "smtpDelegate" and "RTConfig")
    *** @return The property value
    **/
    private String _getString(String keys[], boolean delegate) 
    {

        /* no key, no value */
        if ((keys == null) || (keys.length <= 0)) {
            // -- no keys?
            return null;
        }

        /* check local properties */
        if (this.smtpProps.hasProperty(keys)) {
            // -- "host" is defined and has local property
            return this.smtpProps.getString(keys,null);
        }

        /* ok to delegate? */
        if (!delegate) {
            // -- "host" is defined, but local property not defined, and not ok to delegate
            return null;
        }

        /* delegate */
        if (this.smtpDelegate != null) {
            // -- delegate property
            return this.smtpDelegate._getString(keys, delegate);
        } else 
        if (keys[0].startsWith(SMTP_)) {
            // -- system property
            return RTConfig.getString(keys[0]); // "smtp." only
        } else {
            // -- first key is not "smtp." property
            return null;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the debug property 
    **/
    public void setDebug(boolean V) 
    {
        this.smtpProps.removeProperties(KEY_DEBUG);
        if (V) {
            this.smtpProps.setBoolean(KEY_DEBUG[0], V);
        }
    }

    /**
    *** Gets the debug property 
    **/
    public boolean getDebug() 
    {
        boolean delegate = true; // delegate always ok
        return StringTools.parseBoolean(this._getString(KEY_DEBUG,delegate),false);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the thread-model 'show' state (during debug mode)
    **/
    public void setShowThreadModel(boolean V) 
    {
        this.smtpProps.removeProperties(KEY_THREAD_MODEL_SHOW);
        if (V) {
            this.smtpProps.setBoolean(KEY_THREAD_MODEL_SHOW[0], V);
        }
    }

    /**
    *** Sets the thread-model 'show' state (during debug mode)
    **/
    public boolean getShowThreadModel() 
    {
        boolean delegate = true; // delegate always ok
        return StringTools.parseBoolean(this._getString(KEY_THREAD_MODEL_SHOW,delegate),false);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the thread-model
    **/
    public void setThreadModel(ThreadModel V)
    {
        this.smtpProps.removeProperties(KEY_THREAD_MODEL);
        if (V != null) {
            this.smtpProps.setString(KEY_THREAD_MODEL[0], V.toString());
        }
    }

    /**
    *** Sets the thread-model
    **/
    public void setThreadModel(String V)
    {
        this.setThreadModel(SmtpProperties.getThreadModel(V,null));
    }

    /**
    *** Gets the thread-model
    **/
    public ThreadModel getThreadModel()
    {
        boolean delegate = true; // delegate always ok
        return SmtpProperties.getThreadModel(this._getString(KEY_THREAD_MODEL,delegate));
    }

    /**
    *** Returns true if the ThreadModel indicates that the SendMail process is to 
    *** queued in a separate thread.
    **/
    public boolean isSeparateThread()
    {
        ThreadModel tm = this.getThreadModel();
        return ((tm != null) && tm.isSeparateThread())? true : false;
    }

    // --------------------------------

    /**
    *** Sets the thread-model to NONE
    **/
    public void setThreadModel_NONE()
    {
        this.setThreadModel(SmtpProperties.ThreadModel.NONE);
    }

    /**
    *** Sets the thread-model to CURRENT
    **/
    public void setThreadModel_CURRENT()
    {
        this.setThreadModel(SmtpProperties.ThreadModel.CURRENT);
    }

    /**
    *** Sets the thread-model to POOL
    **/
    public void setThreadModel_POOL()
    {
        this.setThreadModel(SmtpProperties.ThreadModel.POOL);
    }

    /**
    *** Sets the thread-model to DEBUG
    **/
    public void setThreadModel_DEBUG()
    {
        this.setThreadModel(SmtpProperties.ThreadModel.DEBUG);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this SmtpProperties instance defines a "host".
    *** (Some properties should not allow checking the delegate SmtpProperties
    *** instance if this instance has a defined host)
    **/
    public boolean hasHost()
    {
        String host = this.smtpProps.getString(KEY_SERVER_HOST,null);
        return !StringTools.isBlank(host)? true : false;
    }

    /**
    *** Sets the SMTP host
    **/
    public void setHost(String V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_HOST);
        if (!StringTools.isBlank(V)) {
            this.smtpProps.setString(KEY_SERVER_HOST[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP host
    **/
    public String getHost()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = StringTools.trim(this._getString(KEY_SERVER_HOST,delegate));
        return !StringTools.endsWithIgnoreCase(V,SendMail.EXAMPLE_DOT_COM)? V : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP port
    **/
    public void setPort(int V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_PORT);
        if ((V > 0) && (V <= 65535)) {
            this.smtpProps.setInt(KEY_SERVER_PORT[0], V);
        }
    }

    /**
    *** Gets the SMTP host
    **/
    public int getPort()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        return StringTools.parseInt(this._getString(KEY_SERVER_PORT,delegate),25);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP user
    **/
    public void setUser(String V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_USER);
        if (!StringTools.isBlank(V)) {
            this.smtpProps.setString(KEY_SERVER_USER[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP host
    **/
    public String getUser()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        return StringTools.trim(this._getString(KEY_SERVER_USER,delegate));
    }

    // ------------------------------------------------------------------------

    private static final String SENDPARTIAL_OPTIONS[] = { "true", "false" };

    /**
    *** Sets the default SMTP 'sendpartial' flag
    **/
    public void setSendPartial(boolean V)
    {
        this.setSendPartial(V?"true":"false");
    }

    /**
    *** Sets the default SMTP 'sendpartial' flag
    *** Valid values include "false" | "true" | ""
    **/
    public void setSendPartial(String V)
    { // [2.6.5-B16]
        this.smtpProps.removeProperties(KEY_SEND_PARTIAL);
        V = StringTools.trim(V).toLowerCase();
        if (ListTools.contains(SENDPARTIAL_OPTIONS,V)) {
            this.smtpProps.setString(KEY_SEND_PARTIAL[0], V);
        }
    }

    /**
    *** Sets the default SMTP 'sendpartial' flag
    **/
    public String getSendPartial()
    { // [2.6.5-B16]
        boolean delegate = true; // delegate always ok
        return StringTools.trim(this._getString(KEY_SEND_PARTIAL,delegate));
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the default SMTP multipart MIME type
    **/
    public void setMultipartType(String V)
    { // [2.6.2-B52]
        this.smtpProps.removeProperties(KEY_MULTIPART_TYPE);
        if (!StringTools.isBlank(V)) {
            this.smtpProps.setString(KEY_MULTIPART_TYPE[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the default SMTP multipart MIME type
    **/
    public String getMultipartType()
    { // [2.6.2-B52]
        // -- Multipart MIME Types:
        // -    SendMail.MULTIPART_MIXED       = "mixed";       // multipart/mixed
        // -    SendMail.MULTIPART_ALTERNATIVE = "alternative"; // multipart/alternative
        // -    SendMail.MULTIPART_RELATED     = "related";     // multipart/related
        // -    SendMail.MULTIPART_REPORT      = "report";      // multipart/report
        boolean delegate = true; // delegate always ok
        return StringTools.trim(this._getString(KEY_MULTIPART_TYPE,delegate));
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP user email address
    **/
    public void setUserEmail(String V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_USER_EMAIL);
        if (!StringTools.isBlank(V) && (V.indexOf("@") > 0)) {
            this.smtpProps.setString(KEY_SERVER_USER_EMAIL[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP user email address
    **/
    public String getUserEmail()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = StringTools.trim(this._getString(KEY_SERVER_USER_EMAIL,delegate)); // trim
        return !SendMail.IsBlankEmailAddress(V)? V : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP user full name
    **/
    public void setUserFullName(String V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_USER_NAME);
        if (!StringTools.isBlank(V)) {
            this.smtpProps.setString(KEY_SERVER_USER_NAME[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP user full name
    **/
    public String getUserFullName()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = StringTools.trim(this._getString(KEY_SERVER_USER_NAME,delegate)); // trim
        return V;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the SMTP password
    **/
    public void setPassword(String V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_PASSWORD);
        if (V != null) {
            this.smtpProps.setString(KEY_SERVER_PASSWORD[0], V); // do not trim password
        }
    }

    /**
    *** Sets the SMTP password
    **/
    public String getPassword()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = this._getString(KEY_SERVER_PASSWORD, delegate);
        return (V != null)? V : ""; // do not trim password
    }

    // ------------------------------------------------------------------------

    public static final String AUTH_NORMAL_PASS  = "NormalPassword";
    public static final String AUTH_OAUTH2       = "OAuth2";
  //public static final String AUTH_ENCRYPT_PASS = "EncryptedPassword";
  //public static final String AUTH_KERBEROS     = "Kerberos";
  //public static final String AUTH_TLS_CERT     = "TLSCertificate";

    public enum AuthenticationMethod {
        NormalPassword    ("NormalPassword"),
        OAuth2            ("OAuth2"),
        EncryptedPassword ("EncryptedPassword"),
        Kerberos          ("Kerberos"),
        TLSCertificate    ("TLSCertificate");
        private String  dd = null;
        AuthenticationMethod(String d)       { dd = d; }
        public boolean isOAuth2()            { return this.equals(OAuth2); }
        public boolean isNormalPassword()    { return this.equals(NormalPassword); }
        public boolean isEncryptedPassword() { return this.equals(EncryptedPassword); }
        public String  toString()            { return dd; }
    }

    /**
    *** Gets the AuthenticationMethod enum value for the specified name
    *** @param auth The name of the AuthenticationMethod
    *** @param dft  The default AuthenticationMethod if the specified name is invalid.
    *** @return The AuthenticationMethod, or the specified default if the name is invalid
    **/
    public static AuthenticationMethod getAuthenticationMethod(String auth, AuthenticationMethod dft)
    {
        return EnumTools.getValueOf(AuthenticationMethod.class, auth, dft);
    }

    /**
    *** Gets the getAuthenticationMethod enum value for the specified name
    *** @param auth The name of the getAuthenticationMethod
    *** @return The getAuthenticationMethod, or the default NormalPassword if invalid
    **/
    public static AuthenticationMethod getAuthenticationMethod(String auth)
    {
        return SmtpProperties.getAuthenticationMethod(auth, AuthenticationMethod.NormalPassword);
    }

    // --------------------------------

    /**
    *** Gets the SMTP authentication method
    **/
    public void setAuthenticationMethod(String V)
    {
        this.smtpProps.removeProperties(KEY_AUTH_METHOD);
        if (V != null) {
            this.smtpProps.setString(KEY_AUTH_METHOD[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP authentication method
    **/
    public void setAuthenticationMethod(AuthenticationMethod a)
    {
        this.smtpProps.removeProperties(KEY_AUTH_METHOD);
        if (a != null) {
            this.smtpProps.setString(KEY_AUTH_METHOD[0], a.toString());
        }
    }

    /**
    *** Sets the SMTP authentication method.
    *** Currently only "NormalPassword" and "OAuth2" are supported.
    *** Returns "NormalPassword" if authentication method is not defined (does not return null)
    **/
    public AuthenticationMethod getAuthenticationMethod()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = StringTools.trim(this._getString(KEY_AUTH_METHOD,delegate)); // trim
        AuthenticationMethod authMeth = SmtpProperties.getAuthenticationMethod(V, null);
        if (authMeth == null) {
            Print.logWarn("Invalid Authentication method specified: " + V + " (assuming NormalPassword)");
            authMeth = AuthenticationMethod.NormalPassword;
        }
        return authMeth;
    }

    // --------------------------------

    /**
    *** Returns true if SMTP OAuth2 is enabled
    **/
    public boolean isOAuth2Enabled() // [2.6.7-B46n]
    {
        AuthenticationMethod authMeth = this.getAuthenticationMethod();
        return authMeth.isOAuth2();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP OAuth2 refresh-token
    **/
    public void setOAuth2RefreshToken(String providerURL, String clientID, String secret, String refTok) // [2.6.7-B46n]
    {
        this.smtpProps.removeProperties(KEY_OAUTH2_PROVIDER_URL);
        this.smtpProps.removeProperties(KEY_OAUTH2_CLIENT_ID);
        this.smtpProps.removeProperties(KEY_OAUTH2_SECRET);
        this.smtpProps.removeProperties(KEY_OAUTH2_REFRESH_TOKEN);
        this.smtpProps.removeProperties(KEY_OAUTH2_ACCESS_TOKEN); // clear fixed access-token
        if (!StringTools.isBlank(refTok)) {
            this.smtpProps.setString(KEY_OAUTH2_PROVIDER_URL[0] , StringTools.trim(providerURL));
            this.smtpProps.setString(KEY_OAUTH2_CLIENT_ID[0]    , StringTools.trim(clientID));
            this.smtpProps.setString(KEY_OAUTH2_SECRET[0]       , StringTools.trim(secret));
            this.smtpProps.setString(KEY_OAUTH2_REFRESH_TOKEN[0], StringTools.trim(refTok));
        }
    }

    /**
    *** Sets the SMTP OAuth2 access-token
    **/
    public void setOAuth2AccessToken(String providerURL, String accTok) // [2.6.7-B46n]
    {
        this.smtpProps.removeProperties(KEY_OAUTH2_PROVIDER_URL);
        this.smtpProps.removeProperties(KEY_OAUTH2_CLIENT_ID);
        this.smtpProps.removeProperties(KEY_OAUTH2_SECRET);
        this.smtpProps.removeProperties(KEY_OAUTH2_REFRESH_TOKEN);
        this.smtpProps.removeProperties(KEY_OAUTH2_ACCESS_TOKEN); // clear fixed access-token
        if (!StringTools.isBlank(accTok)) {
            this.smtpProps.setString(KEY_OAUTH2_PROVIDER_URL[0], StringTools.trim(providerURL));
            this.smtpProps.setString(KEY_OAUTH2_ACCESS_TOKEN[0], StringTools.trim(accTok));
        }
    }


    // --------------------------------

    /**
    *** Initializes/Gets the OAuthTokenInstance used to get OAuth2 access-tokens
    *** Does not return null.
    *** @throws IOException if unable to obtain a non-null OAuth2AccessToken instance.
    **/
    private OAuth2AccessToken _getOAuth2AccessTokenImpl()
        throws IOException
    {
        // -- OAuth2 enabled?
        if (!this.isOAuth2Enabled()) {
            throw new IOException("OAuth2 is not enabled");
        }
        // -- get instance of OAuth2AccessToken class
        synchronized (this) {
            if (this.oauth2AccessToken == null) {
                // -- get OAuth2AccessToken class name
                boolean delegate = !this.hasHost(); // delegate iff host not defined
                String accTokCN = this._getString(KEY_OAUTH2_TOKEN_CLASS, delegate);
                if (StringTools.isBlank(accTokCN)) {
                    // -- default
                    String refTokCN = "org.opengts.util.OAuth2RefreshToken"; // may not be present
                    try {
                        Class.forName(refTokCN); // ClassNotFoundException
                        accTokCN = refTokCN + "$OAuth2AccessTokenImpl";
                    } catch (Throwable th) { // ClassNotFoundException, ...
                        // -- default class lookup failed, just indicate class name not specified
                        throw new IOException("OAuth2AccessToken class not specified");
                    }
                }
                // -- create OAuth2AccessToken instance
                try {
                    Class<?> accTokC = Class.forName(accTokCN);
                    this.oauth2AccessToken = (OAuth2AccessToken)accTokC.newInstance();
                } catch (ClassNotFoundException cnfe) {
                    throw new IOException("OAuth2AccessToken class not found", cnfe);
                } catch (ClassCastException cce) {
                    throw new IOException("Class is not OAuth2AccessToken", cce);
                } catch (Throwable th) { // InvocationException, ...
                    throw new IOException("Error creating OAuth2AccessToken instance", th);
                }
                // -- 'this.oauth2AccessToken' non-null at this point
            } else {
                // -- already non-null
            }
        }
        // -- non-null instance of OAuth2AccessToken
        return this.oauth2AccessToken;
    }

    /**
    *** Gets the SMTP OAUTH2 access-token
    **/
    public String getOAuth2AccessToken() // [2.6.7-B46n]
        throws IOException
    {
        boolean delegate = true; // delegate always ok

        /* OAuth2 enabled? */
        if (!this.isOAuth2Enabled()) {
            // -- OAuth2 is not enabled
            throw new IOException("OAuth2 authentication not enabled");
        }

        /* check for non-expiring acccess-token */
        String accToken = StringTools.trim(this._getString(KEY_OAUTH2_ACCESS_TOKEN,delegate));
        if (!StringTools.isBlank(accToken)) {
            // -- static, non-expiring, access-token
            return accToken;
        } 

        /* check for refresh-token */
        String refToken = StringTools.trim(this._getString(KEY_OAUTH2_REFRESH_TOKEN,delegate));
        if (!StringTools.isBlank(refToken)) { // this.isOAuthEnabled()
            // -- RefreshToken found, AccessToken is required
            OAuth2AccessToken oAuthAT = this._getOAuth2AccessTokenImpl(); // throws IOException
            String provURL  = StringTools.trim(this._getString(KEY_OAUTH2_PROVIDER_URL,delegate));
            String clientID = StringTools.trim(this._getString(KEY_OAUTH2_CLIENT_ID   ,delegate));
            String secret   = StringTools.trim(this._getString(KEY_OAUTH2_SECRET      ,delegate));
            return oAuthAT.getAccessToken(provURL, clientID, secret, refToken); // throws IOException
        }

        /* OAuth2 enabled, access-token is requried */
        throw new IOException("OAuth2 access/refresh token not defined");

    }

    /**
    *** Sets the SMTP OAUTH2 access-token as expired
    **/
    public boolean setOAuth2AccessTokenExpired() // [2.6.7-B46n]
    {
        boolean delegate = true; // delegate always ok

        /* OAuth2 enabled? */
        if (!this.isOAuth2Enabled()) {
            // -- OAuth2 is not enabled
            return false;
        }

        /* check for non-expiring acccess-token */
        String accToken = StringTools.trim(this._getString(KEY_OAUTH2_ACCESS_TOKEN,delegate));
        if (!StringTools.isBlank(accToken)) {
            // -- access-token expration not allowed with static access-token configuration
            return false;
        } 

        /* check for refresh-token */
        String refToken = StringTools.trim(this._getString(KEY_OAUTH2_REFRESH_TOKEN,delegate));
        if (!StringTools.isBlank(refToken)) {
            // -- RefreshToken found, mark AccessCode as expired
            try {
                OAuth2AccessToken oAuthAT = this._getOAuth2AccessTokenImpl(); // throws IOException
                String providerURL = StringTools.trim(this._getString(KEY_OAUTH2_PROVIDER_URL,delegate));
                String clientID    = StringTools.trim(this._getString(KEY_OAUTH2_CLIENT_ID   ,delegate));
                oAuthAT.setAccessTokenExpired(providerURL, clientID); // throws IOException
                return true;
            } catch (IOException ioe) {
                // -- ignore error display
                return false;
            }
        }

        /* no access/refresh token defined (error condition) */
        return false;

    }

    // ------------------------------------------------------------------------

    private static final String SSL_OPTIONS[] = { "true", "only" }; // omit "false"

    /**
    *** Sets the SMTP SSL enabled state.
    **/
    public void setEnableSSL(boolean V)
    {
        this.setEnableSSL(V?"true":"false");
    }

    /**
    *** Sets the SMTP SSL enabled state.
    *** Valid values include "false" | "true" | "only"
    **/
    public void setEnableSSL(String V)
    {
        this.smtpProps.removeProperties(KEY_ENABLE_SSL);
        V = StringTools.trim(V).toLowerCase();
        if (ListTools.contains(SSL_OPTIONS,V)) {
            this.smtpProps.setString(KEY_ENABLE_SSL[0], V);
        }
    }

    /**
    *** Gets the SMTP SSL enabled state.
    *** false | true | only
    **/
    public String getEnableSSL()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        return StringTools.trim(this._getString(KEY_ENABLE_SSL,delegate));
    }

    // ------------------------------------------------------------------------

    private static final String TLS_OPTIONS[] = { "true", "only" }; // omit "false"

    /**
    *** Sets the SMTP SSL enabled state.
    **/
    public void setEnableTLS(boolean V)
    {
        this.setEnableTLS(V?"true":"false");
    }

    /**
    *** Sets the SMTP TLS enabled state.
    *** Valid values include "false" | "true" | "only"
    **/
    public void setEnableTLS(String V)
    {
        this.smtpProps.removeProperties(KEY_ENABLE_TLS);
        V = StringTools.trim(V).toLowerCase();
        if (ListTools.contains(TLS_OPTIONS,V)) {
            this.smtpProps.setString(KEY_ENABLE_TLS[0], V);
        }
    }

    /**
    *** Gets the SMTP TLS enabled state.
    *** false | true | only
    **/
    public String getEnableTLS()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        return StringTools.trim(this._getString(KEY_ENABLE_TLS,delegate));
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP timeout in milliseconds
    **/
    public void setTimeoutMS(int V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_TIMEOUT_MS);
        if (V > 0) {
            this.smtpProps.setInt(KEY_SERVER_TIMEOUT_MS[0], V);
        }
    }

    /**
    *** Gets the SMTP timeout in milliseconds
    **/
    public int getTimeoutMS()
    {
        boolean delegate = true; // delegate always ok
        return StringTools.parseInt(this._getString(KEY_SERVER_TIMEOUT_MS,delegate),30000);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP retry count
    **/
    public void setRetryCount(int V)
    {
        this.smtpProps.removeProperties(KEY_SERVER_RETRY_COUNT);
        if ((V > 0) && (V < 10)) {
            this.smtpProps.setInt(KEY_SERVER_RETRY_COUNT[0], V);
        }
    }

    /**
    *** Gets the SMTP retry count
    **/
    public int getRetryCount()
    {
        boolean delegate = true; // delegate always ok
        return Math.max(StringTools.parseInt(this._getString(KEY_SERVER_RETRY_COUNT,delegate),0),0);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the SMTP BCC property (a single email address)
    **/
    public void setBccEmail(String V)
    {
        this.smtpProps.removeProperties(KEY_BCC_EMAIL);
        if (!StringTools.isBlank(V) && (V.indexOf("@") > 0)) {
            this.smtpProps.setString(KEY_BCC_EMAIL[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the SMTP BCC property
    **/
    public String getBccEmail()
    {
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String V = StringTools.trim(this._getString(KEY_BCC_EMAIL,delegate));
        return !SendMail.IsBlankEmailAddress(V)? V : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Sysadmin email address
    **/
    public void setSysadminEmail(String V)
    {
        this.smtpProps.removeProperties(KEY_SYSADMIN_EMAIL);
        if (!StringTools.isBlank(V) && (V.indexOf("@") > 0)) {
            this.smtpProps.setString(KEY_SYSADMIN_EMAIL[0], StringTools.trim(V));
        }
    }

    /**
    *** Gets the Sysadmin email address
    **/
    public String getSysadminEmail()
    {
        boolean delegate = true; // delegate always ok
        String V = StringTools.trim(this._getString(KEY_SYSADMIN_EMAIL,delegate));
        return !SendMail.IsBlankEmailAddress(V)? V : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the SMTP "From" email address
    *** @param type The name/id of the email address to return
    *** @return The SMTP "From" email address
    **/
    public String getFromEmailType(String type)
    {
        // -- eg: "smtp.emailAddress.name=name@example.com"
        type = StringTools.trim(type);
        if (StringTools.isBlank(type)) { type = "user"; }
        String K[] = new String[KEY_FROM_EMAILADDRESS_.length];
        for (int i = 0; i < KEY_FROM_EMAILADDRESS_.length; i++) {
            K[i] = KEY_FROM_EMAILADDRESS_[i] + type;
        }
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String emailAddr = this._getString(K, delegate);
        if (!SendMail.IsBlankEmailAddress(emailAddr)) {
            return emailAddr; // not blank
        } else
        if (type.equalsIgnoreCase("user")) {
            return this.getUserEmail(); // may be blank (error to be handled by caller)
        } else
        if (type.equalsIgnoreCase("sysadmin")) {
            return this.getSysadminEmail(); // may be blank (error to be handled by caller)
        } else {
            return this.getUserEmail(); // may be blank (error to be handled by caller)
        }
    }

    // --------------------------------

    /**
    *** Gets the SMTP "To" email address
    *** @param type The name/id of the email address to return
    *** @return The SMTP "To" email address
    **/
    public String getToEmailType(String type)
    {
        // -- eg: "smtp.emailAddress.name=name@example.com"
        type = StringTools.trim(type);
        String K[] = new String[KEY_TO_EMAILADDRESS_.length];
        for (int i = 0; i < KEY_TO_EMAILADDRESS_.length; i++) {
            K[i] = KEY_TO_EMAILADDRESS_[i] + type;
        }
        boolean delegate = !this.hasHost(); // delegate iff host not defined
        String emailAddrs = this._getString(K, delegate);
        if (SendMail.validateAddresses(emailAddrs)) { // (!SendMail.IsBlankEmailAddress(emailAddr))
            return emailAddrs; // not blank
        } else
        if (type.equalsIgnoreCase("user")) {
            return this.getUserEmail(); // may be blank (error to be handled by caller)
        } else
        if (type.equalsIgnoreCase("sysadmin")) {
            return this.getSysadminEmail(); // may be blank (error to be handled by caller)
        } else {
            return "";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Initialize/Reload ignored email file
    *** MUST BE CALLED WITHIN A SYNCHRONIZATION BLOCK!
    **/
    private void _reloadIgnoredEmailFile()
    {

        // -- initialize
        if (!this.ignFileInit) {
            String ignFileS = this._getString(KEY_IGNORED_EMAIL_FILE, true/*delegate*/);
            File ignFile = !StringTools.isBlank(ignFileS)? new File(ignFileS) : null; // RTConfig.getFile(RTKey.SMTP_IGNORED_EMAIL_FILE, null);
            if ((ignFile != null) && !ignFile.isAbsolute()) {
                File dir = RTConfig.getLoadedConfigDir();
                ignFile = (dir != null)? new File(dir,ignFile.toString()) : null;
            }
            this.ignFilePath    = FileTools.isFile(ignFile)? ignFile : null;
            this.ignFileModTime = 0L;
            this.ignFileSet     = null;
            this.ignFileInit    = true;
            if (this.ignFilePath != null) {
                Print.logInfo("Init IgnoredEmail file: " + this.ignFilePath);
            }
        }

        // -- reload
        if ((this.ignFilePath == null) || !this.ignFilePath.isFile()) {
            if (this.ignFileSet != null) {
                Print.logWarn("IgnoredEmail file no longer exists");
                this.ignFileModTime = 0L;
                this.ignFileSet     = null;
            }
        } else {
            long lastMod = this.ignFilePath.lastModified();
            if (lastMod == 0L) { 
                Print.logWarn("No IgnoredEmail file last modified time: " + this.ignFilePath);
                this.ignFileModTime = 0L;
                this.ignFileSet     = null;
            } else
            if (lastMod > this.ignFileModTime) {
                Print.logInfo("(Re)Loading IgnoredEmail file: " + this.ignFilePath);
                Set<String> ignSet = new HashSet<String>();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(this.ignFilePath);
                    for (int r = 0;; r++) {
                        // -- read line
                        String line = null;
                        try {
                            line = FileTools.readLine(fis);
                            if (line == null) { break; } // end of file
                            line = line.trim();
                        } catch (EOFException eof) {
                            break; // end of file
                        }
                        // -- simple validation
                        if (line.startsWith("#")) {
                            continue; // comment
                        } else
                        if (line.indexOf("@") <= 0) {
                            continue; // not an email address
                        }
                        // -- add to set
                        if (this.ignEmailDebug) { Print.logInfo("Adding IgnoredEmail address [file]: " + line); }
                        ignSet.add(line);
                    }
                } catch (IOException ioe) {
                    Print.logException("IgnoredEmail file IO Error", ioe);
                } finally {
                    if (fis != null) { try { fis.close(); } catch (Throwable th) {} }
                }
                // -- save
                this.ignFileSet     = !ListTools.isEmpty(ignSet)? ignSet : null;
                this.ignFileModTime = lastMod;
            }
        }

    }

    /**
    *** Add specified email address to the locally maintained ignored email addresses
    **/
    public int addIgnoredEmailAddress(String... ee)
    {

        /* empty list? */
        if (ListTools.isEmpty(ee)) {
            return 0; // nothing to add
        }

        /* init/add */
        int addCnt = 0;
        synchronized (this) {
            // -- initialize set
            if (this.ignLocalSet == null) {
                this.ignLocalSet = new HashSet<String>();
            }
            // -- add to local set
            for (String e : ee) {
                if (StringTools.isBlank(e)) {
                    continue;
                } else
                if (e.indexOf("@") <= 0) {
                    continue;
                }
                // -- TODO: validate email address?
                if (this.ignEmailDebug) { Print.logInfo("Adding IgnoredEmail address [local]: " + e); }
                this.ignLocalSet.add(e);
                addCnt++;
            }
        }
        return addCnt;

    }

    /**
    *** Returns true if the specific email address is to be ignored
    *** @param e  The email address to test
    *** @return True if the specific email address is to be ignored, false otherwise
    **/
    public boolean isIgnoredEmailAddress(String e)
    {

        /* blank/invalid email address? */
        if (StringTools.isBlank(e)) {
            return true;
        } else
        if (e.indexOf("@") <= 0) {
            return true;
        }
        // -- TODO: validate email address?

        /* lock */
        synchronized (this) {

            /* email address contained in local ignored email set? */
            if (!ListTools.isEmpty(this.ignLocalSet) && this.ignLocalSet.contains(e)) {
                return true;
            }
    
            /* email address contained in ignored email file? */
            this._reloadIgnoredEmailFile();
            if (!ListTools.isEmpty(this.ignFileSet) && this.ignFileSet.contains(e)) {
                return true;
            }

        }

        /* not ignored */
        return false;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified value is equal to this instance
    **/
    public boolean equals(Object other)
    {
        if (!(other instanceof SmtpProperties)) {
            return false;
        }
        return this.smtpProps.equals(((SmtpProperties)other).smtpProps);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this instance
    **/
    public String toString()
    {
        return this.smtpProps.toString();
    }

    /**
    *** Returns a String representation of this instance
    *** @param abbrev  True to return a String containing the abbriated property names
    **/
    public String toString(boolean abbrev)
    {
        if (abbrev) {
            RTProperties rtp = new RTProperties();
            for (String K[] : KEY_PROPERTIES) {
                String V = this.smtpProps.getString(K, null);
                if (V != null) {
                    rtp.setString(K[K.length - 1], V);
                }
            }
            return rtp.toString();
        } else {
            return this.smtpProps.toString();
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** (Debug Purposes) Print the contents of this instance to stdout
    **/
    public void printProperties(String msg)
    {
        this.smtpProps.printProperties(msg);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
