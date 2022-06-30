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
// Change History:
//  2018/09/10  GTS Development Team
//     -Extracted from SendMail.java
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;
import java.io.*;
import java.net.*;

/**
*** A container for the content of an email (Subject/Body/Attachment)
**/
public class EMailContent
    implements Cloneable
{

    // ------------------------------------------------------------------------

    private static final String START_DELIM      = RTProperties.KEY_START_DELIMITER;  // "${";
    private static final String END_DELIM        = RTProperties.KEY_END_DELIMITER;    // "}";
    private static final String DFT_DELIM        = RTProperties.KEY_DFT_DELIMITER;    // "=";
    
    private static final String EMAILSUBJECT_    = ".EmailSubject.";    // required
    private static final String EMAILATTACHMENT_ = ".EmailAttachment."; // optional
    private static final String EMAILBODY_       = ".EmailBody.";       // required
    private static final String EMAILEND_        = ".EmailEnd.";        // optional
    
    private static final int    URL_LOAD_TIMEOUT_MS = 2000;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private File                 loadFile       = null;
    private long                 loadFileMod    = 0L;
    // --
    private URL                  loadURL        = null;
    private int                  urlTimeoutMS   = URL_LOAD_TIMEOUT_MS;
    // --
    private long                 loadTimeSec    = 0L;

    private String               subject        = "";
    private String               msgBody        = "";
    private SendMail.Attachments attachments    = null;

    /**
    *** Constructor
    **/
    public EMailContent() 
    {
        super();
    }

    /**
    *** Constructor
    **/
    public EMailContent(String subj, String body, SendMail.Attachments attach)
    {
        this.setSubject(subj);
        this.setBody(body);
        this.setAttachments(attach);
    }

    /**
    *** Constructor
    **/
    public EMailContent(String subj, String body)
    {
        this(subj, body, null);
    }

    /**
    *** Constructor
    **/
    public EMailContent(String emailContent)
    {
        this();
        this.loadFromString(emailContent);
    }

    /**
    *** Constructor
    **/
    public EMailContent(File file)
    {
        this();
        this.loadFromFile(file);
    }

    /**
    *** Constructor
    **/
    public EMailContent(URL url, int timeoutMS)
    {
        this();
        this.loadFromURL(url, timeoutMS);
    }

    /**
    *** Constructor
    **/
    public EMailContent(EMailContent other)
    {
        super();
        this.setSubject(other.getSubject());
        this.setBody(other.getBody());
        this.setAttachments(other.getAttachments());
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this EMailContent instance is valid, false otherwise.
    **/
    public boolean isValid()
    {
        if (!this.hasSubject()) {
            return false;
        } else
        if (!this.hasBody()) {
            return false;
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Clears the Subject, Body, and Attachments of this EMailContent instance.
    **/
    public void clear()
    {
        this.setSubject(null);
        this.setBody(null);
        this.setAttachments(null);
        // --
        this.loadFileMod = 0L;
        this.loadTimeSec = 0L;
    }

    // ------------------------------------------------------------------------

    /**
    *** Loads this EMailContent instance from the specified File.
    *** Subsequent calls to "reload()" will reload from the specified File.
    **/
    public boolean loadFromFile(File file)
    {
        // -- clear
        this.clear();
        // -- load
        boolean didLoad = this._loadFromFile(file);
        // -- save URL
        this.loadURL      = null;
        this.urlTimeoutMS = 0;
        this.loadFile     = file; // may be null
        this.loadFileMod  = (file != null)? file.lastModified() : 0L;
        this.loadTimeSec  = DateTime.getCurrentTimeSec();
        // -- return load success
        return didLoad;
    }

    /**
    *** Loads this EMailContent instance from the specified file
    **/
    private boolean _loadFromFile(File file)
    {
        boolean didLoad = false;
        // -- validate file
        if (!FileTools.isFile(file)) {
            // -- not a file
        } else
        if (!FileTools.canRead(file)) {
            // -- file not readable
        } else {
            // -- read/load file text
            String text = StringTools.toStringValue(FileTools.readFile(file));
            if (!StringTools.isBlank(text)) {
                didLoad = this._loadFromString(text);
            }
        }
        // -- return load success
        return didLoad;
    }

    // --------------------------------

    /**
    *** Loads this EMailContent instance from the specified URL.
    *** Subsequent calls to "reload()" will reload from the specified URL.
    **/
    public boolean loadFromURL(URL url, int timeoutMS)
    {
        // -- clear
        this.clear();
        // -- load
        boolean didLoad = this._loadFromURL(url, timeoutMS);
        // -- save URL
        this.loadURL      = url; // may be null
        this.urlTimeoutMS = timeoutMS;
        this.loadFile     = null;
        this.loadFileMod  = 0L;
        this.loadTimeSec  = DateTime.getCurrentTimeSec();
        // -- return load success
        return didLoad;
    }

    /**
    *** Loads this EMailContent instance from the specified URL.
    **/
    private boolean _loadFromURL(URL url, int timeoutMS)
    {
        boolean didLoad = false;
        // -- validate URL
        if (url == null) {
            // -- URL not provided
        } else {
            // -- read/load URL text
            try {
                byte b[] = HTMLTools.readPage_GET(url,timeoutMS,100000); // throws IOException
                String text = StringTools.toStringValue(b); // null, if 'b' is null
                if (!StringTools.isBlank(text)) {
                    didLoad = this._loadFromString(text);
                }
            } catch (IOException ioe) {
                Print.logWarn("Unable to load EMailContent from URL: " + url);
            }
        }
        // -- return load success
        return didLoad;
    }

    // --------------------------------

    /**
    *** Returns the minimum position between the specified start/end positions
    **/
    private int _posEnd(int s, int e, int... pp)
    {
        int end = e; // > 0
        if (pp != null) {
            // -- find smallest position between s and e
            for (int p : pp) {
                if ((p >= s) && (p < end)) {
                    end = p;
                }
            }
        }
        return end;
    }

    /**
    *** Loads this EMailContent instance from the specified String.
    *** Subsequent calls to "reload()" will leave this instance as-is.
    **/
    public boolean loadFromString(String text)
    {
        // -- clear previous values
        this.clear();
        // -- load
        boolean didLoad = this._loadFromString(text);
        // -- clear File/URL
        this.loadURL      = null;
        this.urlTimeoutMS = 0;
        this.loadFile     = null;
        this.loadFileMod  = 0L;
        this.loadTimeSec  = DateTime.getCurrentTimeSec();
        // -- return load success
        return didLoad;
    }

    /**
    *** Loads this EMailContent instance from the specified String
    **/
    private boolean _loadFromString(String text)
    {
        // Format:
        //      .EMailSubject.
        //          This is the subject
        //      .EMailAttachments. 
        //          /path/to/attached/file.pdf
        //          /another/path/to/an/attached/file.xls
        //      .EMailBody.
        //          This is the body of the email
        boolean didLoad = false;

        // -- validate text
        if (StringTools.isBlank(text)) {
            // -- null/empty
            return didLoad; // false
        }
        text = text.trim();

        // -- find location of email components
        int emailSubjectP = StringTools.indexOfIgnoreCase(text,EMAILSUBJECT_);    // required ".EmailSubject." 
        int emailAttachP  = StringTools.indexOfIgnoreCase(text,EMAILATTACHMENT_); // optional ".EmailAttachment." 
        int emailBodyP    = StringTools.indexOfIgnoreCase(text,EMAILBODY_);       // required ".EmailBody."
        int emailEndP     = StringTools.indexOfIgnoreCase(text,EMAILEND_);        // optional ".EmailEnd."

        // -- extract subject
        if (emailSubjectP >= 0) {
            // -- "Subject:" is present
            int s = emailSubjectP + EMAILSUBJECT_.length();
            int e = this._posEnd(s,text.length(),emailAttachP,emailBodyP,emailEndP);
            //int e = text.length();
            //if ((emailAttachP >= s) && (emailAttachP < e)) { e = emailAttachP ; } // start of ".EmailAttachment.", if _after_ ".EmailSubject."
            //if ((emailBodyP   >= s) && (emailBodyP   < e)) { e = emailBodyP   ; } // start of ".EmailBody.", if ".EmailAttachment." not _after_ ".EmailSubject."
            // -- 'e' now points to end of ".EmailSubject." section
            String subject = text.substring(s,e).trim(); // extract "Subject"
            this.setSubject(subject);
            didLoad = true;
        } else {
            Print.logError("Missing '" + EMAILSUBJECT_ + "'");
            return false;
        }

        // -- extract attachments
        if (emailAttachP >= 0) {
            // -- "Attachment:" is present
            int s = emailAttachP + EMAILATTACHMENT_.length();
            int e = this._posEnd(s,text.length(),emailSubjectP,emailBodyP,emailEndP);
            //int e = text.length();
            //if ((emailSubjectP >= s) && (emailSubjectP < e)) { e = emailSubjectP; } // start of ".EmailSubject.", if _after_ ".EmailAttachment."
            //if ((emailBodyP    >= s) && (emailBodyP    < e)) { e = emailBodyP   ; } // start of ".EmailBody.", if ".EmailSubject." not _after_ ".EmailAttachment."
            // -- 'e' now points to end of ".EmailAttachment." section
            String attachList[] = StringTools.split(text.substring(s,e),'\n');
            for (String attachS : attachList) {
                if (StringTools.isBlank(attachS)) { continue; }
                // -- resolve file
                File attachF = new File(attachS); // TODO: handle relative file path?
                // -- load attachment
                byte attachB[] = FileTools.readFile(attachF);
                if (!ListTools.isEmpty(attachB)) {
                    String fileName = attachF.getName();
                    String fileExtn = FileTools.getExtension(attachF);
                    String mimeType = HTMLTools.getMimeTypeFromExtension(fileExtn,null);
                    if (mimeType == null) {
                        mimeType = SendMail.DefaultContentType(attachB,HTMLTools.CONTENT_TYPE_OCTET);
                    }
                    this.addAttachment(new SendMail.Attachment(attachB,fileName,mimeType));
                    didLoad = true;
                } else {
                    // -- unable to read attachment file?
                    Print.logError("Unable to load attachment file: " + attachS);
                }
            }
        }

        // -- extract body
        if (emailBodyP >= 0) {
            // -- "Body:" is present
            int s = emailBodyP + EMAILBODY_.length();
            int e = this._posEnd(s,text.length(),emailSubjectP,emailAttachP,emailEndP);
            //int e = text.length();
            //if ((emailSubjectP >= s) && (emailSubjectP < e)) { e = emailSubjectP; } // start of ".EmailSubject.", if _after_ ".EmailBody."
            //if ((emailAttachP  >= s) && (emailAttachP  < e)) { e = emailAttachP ; } // start of ".EmailAttachment.", if ".EmailSubject." not _after_ ".EmailBody."
            // -- 'e' now points to end of ".EmailBody." section
            String body = text.substring(s,e).trim();
            this.setBody(body);
            didLoad = true;
        } else {
            Print.logError("Missing '" + EMAILBODY_ + "'");
            return false;
        }

        // -- did load?
        return didLoad;

    }

    // ------------------------------------------------------------------------

    /**
    *** Reload from originally specified file (if file has been updated)
    **/
    public boolean reload()
    {
        boolean didReload = false;
        if (this.loadFile != null) {
            // -- reload from file 
            // -    reload iff file modified time has changed
            synchronized (this) {
                long lastMod = this.loadFile.lastModified();
                if ((lastMod > 0L) && (lastMod != this.loadFileMod)) {
                    didReload = this._loadFromFile(this.loadFile);
                    this.loadFileMod = lastMod;
                    this.loadTimeSec = DateTime.getCurrentTimeSec();
                }
            }
        } else
        if (this.loadURL != null) {
            // -- reload from URL
            // -    We can't tell if the URL contents has changed, so we limit the allowed
            // -    reload/update frequency.
            long minElapsedSec = 60L; // at least 60-seconds since last update
            synchronized (this) {
                long nowSec = DateTime.getCurrentTimeSec();
                if ((this.loadTimeSec <= 0L) || ((nowSec - this.loadTimeSec) >= minElapsedSec)) {
                    didReload = this._loadFromURL(this.loadURL, this.urlTimeoutMS);
                    this.loadTimeSec = nowSec;
                }
            }
        }
        return didReload;
    }

    // ------------------------------------------------------------------------

    public boolean hasSubject()
    {
        return !StringTools.isBlank(this.subject)? true : false;
    }

    public void setSubject(String subj)
    {
        this.subject = StringTools.trim(subj);
    }

    public String getSubject()
    {
        return this.subject;
    }

    public String getSubject(Map<String,String> kvMap)
    {
        String subj = (kvMap != null)?
            StringTools.insertKeyValues(this.subject,START_DELIM,END_DELIM,DFT_DELIM,kvMap) :
            RTConfig.insertKeyValues(   this.subject,START_DELIM,END_DELIM,DFT_DELIM);
        return subj;
    }

    public String getSubject(StringTools.KeyValueMap kvMap)
    {
        String subj = (kvMap != null)?
            StringTools.insertKeyValues(this.subject,START_DELIM,END_DELIM,DFT_DELIM,kvMap) :
            RTConfig.insertKeyValues(   this.subject,START_DELIM,END_DELIM,DFT_DELIM);
        return subj;
    }

    // ------------------------------------------------------------------------

    public boolean hasBody()
    {
        return !StringTools.isBlank(this.msgBody)? true : false;
    }

    public boolean isBodyHtml()
    {
        return HTMLTools.isHtmlText(this.msgBody);
    }

    public void setBody(String body)
    {
        this.msgBody = StringTools.trim(body);
    }

    public String getBody()
    {
        return this.msgBody;
    }

    public String getBody(Map<String,String> kvMap)
    {
        String body = (kvMap != null)?
            StringTools.insertKeyValues(this.msgBody,START_DELIM,END_DELIM,DFT_DELIM,kvMap) :
            RTConfig.insertKeyValues(   this.msgBody,START_DELIM,END_DELIM,DFT_DELIM);
        return StringTools.replace(body,"\\n","\n");
    }

    public String getBody(StringTools.KeyValueMap kvMap)
    {
        String body = (kvMap != null)?
            StringTools.insertKeyValues(this.msgBody,START_DELIM,END_DELIM,DFT_DELIM,kvMap) :
            RTConfig.insertKeyValues(   this.msgBody,START_DELIM,END_DELIM,DFT_DELIM);
        return StringTools.replace(body,"\\n","\n");
    }

    // ------------------------------------------------------------------------

    public SendMail.Attachment createHtmlAttachment(String html) {
        if (!StringTools.isBlank(html)) {
            byte b[] = html.getBytes();
            return new SendMail.Attachment(b, "email.html", HTMLTools.MIME_HTML());
        } else {
            return null;
        }
    }

    public boolean hasAttachments()
    {
        return (this.attachments != null)? this.attachments.hasAttachments() : false;
    }

    public void setAttachments(SendMail.Attachments attach)
    {
        this.attachments = attach; // may be null
    }

    public void addAttachment(SendMail.Attachment att)
    {
        if ((att != null) && (att.getSize() > 0)) {
            if (this.attachments == null) {
                this.attachments = new SendMail.Attachments();
            }
            this.attachments.addAttachment(att);
        }
    }

    public SendMail.Attachments getAttachments()
    {
        return this.attachments; // may be null
    }

    // ------------------------------------------------------------------------

    public EMailContent insertKeyValues(StringTools.KeyValueMap kvMap)
    {
        this.subject = this.getSubject(kvMap);
        this.msgBody = this.getBody(kvMap);
        return this;
    }

    public EMailContent insertKeyValues()
    {
        return this.insertKeyValues(null);
    }

    // ------------------------------------------------------------------------

    public EMailContent clone()
    {
        return new EMailContent(this);
    }

    public EMailContent clone(StringTools.KeyValueMap kvMap)
    {
        return this.clone().insertKeyValues(kvMap);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Subject of this EMailContent
    **/
    public String toString()
    {
        return this.getSubject();
    }

    /**
    *** Gets the debug content of this EMailContent
    **/
    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("Subject: \n");
            sb.append("   " + this.getSubject() + "\n");
        sb.append("-----------------------------------------\n");
        sb.append("Body: \n");
            sb.append(this.getBody() + "\n");
        if (this.hasAttachments()) {
            sb.append("-----------------------------------------\n");
            sb.append("Attachments: \n");
            SendMail.Attachments a = this.getAttachments(); // class Attachments
            for (SendMail.Attachment A : a.getAttachments()) {
                sb.append("   ");
                sb.append("name=" + A.getName() + " ");
                sb.append("type=" + A.getType() + " ");
                sb.append("size=" + A.getSize() + "\n");
            }
        }
        sb.append("-----------------------------------------\n");
        return sb.toString();
    }

    /**
    *** Print the contents of this instance as a debug message
    **/
    public void printDebugLog()
    {
        Print.logDebug(this.toDebugString());
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static String ARG_LOAD_FILE[]   = new String[] { "file", "load" };
    private static String ARG_LOAD_URL[]    = new String[] { "url" };

    /**
    *** Command-line debug/testing entry point
    *** @param argv  The command-line arguments
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        /* Load EMailContent from File */
        // -- "-file="
        if (RTConfig.hasProperty(ARG_LOAD_FILE)) {
            EMailContent emailContent = new EMailContent();
            File file = RTConfig.getFile(ARG_LOAD_FILE,null);
            if (!emailContent.loadFromFile(file)) {
                Print.sysPrintln("ERROR: Unable to load EMailContent file: " + file);
                System.exit(1);
            }
            Print.sysPrintln(emailContent.toDebugString());
            System.exit(0);
        }

        /* Load EMailContent from URL */
        // -- "-url="
        if (RTConfig.hasProperty(ARG_LOAD_URL)) {
            String urlStr = RTConfig.getString(ARG_LOAD_URL,null);
            try {
                URL url = new URL(urlStr);
                EMailContent emailContent = new EMailContent();
                if (!emailContent.loadFromURL(url,2000)) {
                    Print.sysPrintln("ERROR: Unable to load EMailContent file: " + urlStr);
                    System.exit(1);
                }
                Print.sysPrintln(emailContent.toDebugString());
            } catch (MalformedURLException mue) {
                Print.sysPrintln("ERROR: Invalid URL - " + urlStr);
                System.exit(1);
            }
            System.exit(0);
        }

        /* no valid command? */
        Print.sysPrintln("No valid command found");
        System.exit(1);

    }

}
