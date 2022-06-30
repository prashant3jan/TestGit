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
//  JavaMail support.
//  This module requires the JavaMail api library.
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/04/03  Martin D. Flynn
//     -Renamed source file to "SendMail.java.save" to temporarily remove 
//      the requirement of having the JavaMail api installed in order to compile
//      the server.
//  2006/06/30  Martin D. Flynn
//     -Repackaged.
//  2006/07/13  Martin D. Flynn
//     -Added support for specifying user/password.
//     -Added support SSL connections.
//  2006/09/16  Martin D. Flynn
//     -Added 'main' to allow testing this module
//  2008/02/27  Martin D. Flynn
//     -Added "image/png" mime type
//  2009/01/01  Martin D. Flynn
//     -Added thread-model THREAD_NONE for debug purposes.  
//      Similar to THREAD_DEBUG but skips sending email quietly.
//  2012/02/03  Martin D. Flynn
//     -Fixed displayed error when "SendMailArgs" class cannot be found/returned.
//  2012/10/16  Martin D. Flynn
//     -Added "sendSysadmin(...)" for sending sysadmin notification emails.
//  2013/09/26  Martin D. Flynn
//     -Added support for overriding ThreadPool parameters.
//  2016/04/06  Martin D. Flynn
//     -Added "EMailContent" class.  
//     -Added "Attachments" class to hold MimeMultipart type and Attachment list.
//  2018/09/10  GTS Development Team
//     -Moved ThreadModel settings to SmtpProperties
//     -Updated "validateAddress" to be more restrictive on valid email addresses
//     -Moved "EMailContent" to its own .java module
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.lang.reflect.*;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class SendMail
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public enum SendResult {
        SUCCESS ("Success"), // email was sent
        QUEUED  ("Queued" ), // email was queued for sending
        IGNORED ("Ignored"), // email was ignored
        FAILED  ("Failed" ); // email failed to send
        String ss = null;
        SendResult(String s) { ss = s; }
        public boolean isSuccess()  { return this.equals(SUCCESS); }
        public boolean isQueued()   { return this.equals(QUEUED); }
        public boolean isOK()       { return this.isSuccess() || this.isQueued(); } // email was sent, or is trying to send
        public boolean isFailed()   { return this.equals(FAILED); }
        public String  toString()   { return ss; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static long SleepMSBetweenEMails = 0L;

    /**
    *** Sets the amount of time to sleep after sending an email.<br>
    *** Note: this must be used with caution, since it could cause a significant
    *** backlog if many emails are sent to the threadpool.
    **/
    public static void SetSleepAfterEMailMS(long sleepMS) 
    {
        if (sleepMS <= 0L) {
            SendMail.SleepMSBetweenEMails = 0L;
        } else
        if (sleepMS > 20000L) {
            SendMail.SleepMSBetweenEMails = 20000L;
        } else {
            SendMail.SleepMSBetweenEMails = sleepMS;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public  static final String SendMailArgs_className  = "org.opengts.util.SendMailArgs";

    private static Class<?> _SendMailArgs_class         = null;

    /**
    *** Gets the "SendMailArgs" class
    *** @return The "SendMailArgs" class, if "SendMail" enabled, otherwise null.
    **/
    private static Class<?> GetSendMailArgs_class() 
    {
        if (_SendMailArgs_class == null) {
            try {
                _SendMailArgs_class = Class.forName(SendMailArgs_className);
            } catch (NoClassDefFoundError ncdfe) {
                // -- this class retrieval will fail if JavaMail "mail.jar" is not installed.
                Print.logError("Class '"+SendMailArgs_className+"': " + ncdfe);
                return null;
            } catch (ClassNotFoundException cnfe) {
                // -- this class retrieval will fail if JavaMail "mail.jar" is not installed.
                Print.logError("Class '"+SendMailArgs_className+"': " + cnfe);
                return null;
            } catch (Throwable th) {
                Print.logError("Class '"+SendMailArgs_className+"': " + th);
                return null;
            }
        }
        return _SendMailArgs_class;
    }

    /**
    *** Returns true if "SendMail" is enabled
    *** @return True if "SendMail" is enabled, otherwise false
    **/
    public  static boolean IsSendMailEnabled()
    {
        return (GetSendMailArgs_class() != null);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public  static final String EXAMPLE_DOT_COM     = "example.com";
    
    public static boolean IsBlankEmailAddress(String email)
    {
        email = StringTools.trim(email); // trim
        if (email.equals("")) {
            return true;
        } else 
        if (StringTools.endsWithIgnoreCase(email,EXAMPLE_DOT_COM)) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private  static final Random Randomizer = new Random();

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Save specified SendMail Args to outbox
    **/
    public static boolean SaveToOutbox(SendMail.Args args)
    {
        // -- extract Args
        String                    from = args.getFrom();           // never null
        String                    to[] = args.getTo();             // never null
        String                    cc[] = args.getCc();             // never null
        String                   bcc[] = args.getBcc();            // never null
        String                 subject = args.getSubject();        // never null
        String                 msgBody = args.getBody();           // never null
        Properties             headers = args.getHeaders();        // never null
        SendMail.Attachments    attach = args.getAttachments();    // may be null
        SmtpProperties       smtpProps = args.getSmtpProperties(); // never null
        if (StringTools.isBlank(from)) {
            // -- no 'From' address
            return false;
        } else
        if ((to == null) || (to.length <= 0)) {
            // -- no 'To' address
            return false;
        }
        // -- convert to String
        String toStr        = StringTools.join(to,",");
        String ccStr        = StringTools.join(cc,",");
        String bccStr       = StringTools.join(bcc,",");
        String attachStr    = (attach    != null)? attach.toString() : "";
        String smtpPropsStr = (smtpProps != null)? smtpProps.toString() : "";
        String headersStr   = (headers   != null)? (new RTProperties(headers)).toString() : "";
        // -- save to outbox
        // -  TODO:
        return false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** SendMailException
    **/
    public static class SendMailException
        extends Exception
    {
        private boolean retrySend = false;
        public SendMailException(String msg) {
            super(msg);
        }
        public SendMailException(Throwable cause) {
            super(cause);
        }
        public SendMailException(String msg, Throwable cause) {
            super(msg, cause);
        }
        public SendMailException setRetry(boolean retry) {
            this.retrySend = retry;
            return this;
        }
        public boolean getRetry() {
            return this.retrySend;
        }
        // ----------------------------
        private StringBuffer _getMessage(Throwable th, StringBuffer sb) {
            if (th != null) {
                String msg = StringTools.trim(th.getMessage());
                sb.append("[");
                sb.append(msg);
                this._getMessage(th.getCause(), sb);
                sb.append("]");
            }
            return sb;
        }
        public String _getMessage() {
            StringBuffer sb = new StringBuffer();
            sb.append(StringTools.trim(this.getMessage()));
            this._getMessage(this.getCause(),sb);
            return sb.toString();
        }
    }

    /**
    *** Create a SendMailException exception
    **/
    public static SendMailException newSendMailException(String msg, Throwable cause)
    {
        StringBuffer smMsgSB = new StringBuffer();
        // -- add this message
        if (!StringTools.isBlank(msg)) {
            smMsgSB.append(msg);
        }
        // -- append cause message
        String causeMsg = (cause != null)? cause.getMessage() : null;
        if (!StringTools.isBlank(causeMsg)) {
            if (smMsgSB.length() > 0) {
                smMsgSB.append("[").append(causeMsg).append("]");
            } else {
                smMsgSB.append(causeMsg);
            }
        }
        // -- create new SendMailException
        if (cause instanceof SendMailException) {
            // -- cause is already SendMailException (unlikely)
            Print.logWarn("Recursive SendMailException: " + msg);
          //SendMailException sme = new SendMailException(smMsgSB.toString(),cause.getCasue());
          //sme.setStackTrace(cause.getStackTrace());
          //throw sme;
          //throw (SendMailException)cause;
            return new SendMailException(smMsgSB.toString(), cause);
        } else {
            return new SendMailException(smMsgSB.toString(), cause);
        }
    }

    /**
    *** Create a SendMailException exception
    **/
    public static SendMailException newSendMailException(Throwable cause)
    {
        return SendMail.newSendMailException(null, cause);
    }

    /**
    *** Create a SendMailException exception
    **/
    public static SendMailException newSendMailException(String msg)
    {
        return SendMail.newSendMailException(msg, null);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Custom "X" headers

    public  static final String X_OwnerId               = "X-OwnerId";
    public  static final String X_AssetId               = "X-AssetId";
    public  static final String X_PageType              = "X-PageType";
    public  static final String X_Requestor             = "X-Requestor";
    public  static final String X_OriginatingIP         = "X-OriginatingIP";
    public  static final String X_EventTime             = "X-EventTime";
    public  static final String X_StatusCode            = "X-StatusCode";
    public  static final String X_AlarmRule             = "X-AlarmRule";
    public  static final String X_GPSLocation           = "X-GPSLocation";

    // ------------------------------------------------------------------------

    public  static final byte   MAGIC_GIF_87a[]         = HTMLTools.MAGIC_GIF_87a;
    public  static final byte   MAGIC_GIF_89a[]         = HTMLTools.MAGIC_GIF_89a;
    public  static final byte   MAGIC_JPEG[]            = HTMLTools.MAGIC_JPEG;
    public  static final byte   MAGIC_PNG[]             = HTMLTools.MAGIC_PNG; 

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* SendMail ThreadPool */
    // SendMail.ThreadPool.maximumPoolSize=20
    // SendMail.ThreadPool.maximumIdleSeconds=0
    // SendMail.ThreadPool.maximumQueueSize=0
    private static final RTKey PROP_ThreadPool_SendMail_    = RTKey.valueOf(RTKey.ThreadPool_SendMail_);
    private static final int   ThreadPool_SendMail_Size     = 20;   // max threads
    private static final int   ThreadPool_SendMail_IdleSec  =  0;   // trim idle threads
    private static final int   ThreadPool_SendMail_QueSize  =  0;   // max queue size
    private static ThreadPool  ThreadPool_SendMail          = new ThreadPool(
        "SendMail",
        PROP_ThreadPool_SendMail_, // property allowing default override
        ThreadPool_SendMail_Size, 
        ThreadPool_SendMail_IdleSec, 
        ThreadPool_SendMail_QueSize);
        
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Convenience method for sending notification regarding internal errors
    
    //public static void sendError(String subject, String msgBody)
    //{
    //    Properties headers = null;
    //    String emailFrom   = RTConfig.getString(RTKey.ERROR_EMAIL_FROM);
    //    String emailTo     = RTConfig.getString(RTKey.ERROR_EMAIL_TO);
    //    if ((emailFrom != null) && (emailTo != null)) {
    //        SendMail.Attachment attach[] = null;
    //        SendMail.send(headers,emailFrom,emailTo,subject,msgBody,attach,null/*SmtpProps*/);
    //    }
    //}

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns the specified override "From" email address, if non-blank, otherwise
    *** the default configured "From" email address will be returned.
    *** @param mainEmail  The overriding email address to return if non-blank
    *** @return The SMTP user "From" email address.
    **/
    public static String getDefaultUserEmail(String mainEmail)
    {
        if (!SendMail.IsBlankEmailAddress(mainEmail)) {
            return StringTools.trim(mainEmail);
        } else {
            String email = RTConfig.getString(RTKey.SMTP_SERVER_USER_EMAIL, null);
            return !SendMail.IsBlankEmailAddress(email)? email : null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Sends a system notification email to the type-id email address(es)
    *** @param toTypeID The "To" email address typeID
    *** @param subject  The email subject
    *** @param msgBody  The email message body
    *** @return True if email is sent
    **/
    public static boolean sendSystemEmail(String toTypeID, String subject, String msgBody)
    {
        toTypeID = StringTools.trim(toTypeID);
        if (StringTools.isBlank(toTypeID)) { toTypeID = "sysadmin"; }
        SmtpProperties smtpProps = new SmtpProperties("SendMail:"+toTypeID);
        boolean retrySend = false;

        /* "To:" */
        String to = smtpProps.getToEmailType(toTypeID);
        if (StringTools.isBlank(to)) {
            Print.logWarn("'To' address type-id not found: " + toTypeID);
            return false;
        }

        /* "From:" */
        String from = smtpProps.getFromEmailType("sysadmin");
        if (StringTools.isBlank(from)) {
            Print.logWarn("'From' \""+RTKey.SMTP_SERVER_USER_EMAIL+"\" not specified");
            return false;
        }

        /* send */
        return SendMail.send(from, to, subject, msgBody, smtpProps, retrySend).isOK();

    }

    // --------------------------------

    /**
    *** Sends a system notification email to the defined system admin email address
    *** @param subject  The email subject
    *** @param msgBody  The email message body
    *** @return True if email is sent
    **/
    public static boolean sendSysadminEMail(String subject, String msgBody)
    {
        return SendMail.sendSystemEmail("sysadmin", subject, msgBody);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Sends an email
    *** @param from      The sender of the email
    *** @param to        A comma-separated list of email recipients.
    *** @param subject   The email subject.
    *** @param msgBody   The email message body.
    *** @param smtpProps The custom SMTP properties
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, 
        String subject, String msgBody,
        SmtpProperties smtpProps)
    {
        String ato[]  = (to != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = null;
        String abcc[] = null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,null/*attach*/,smtpProps,false);
    }

    /**
    *** Sends an email
    *** @param from      The sender of the email
    *** @param to        A comma-separated list of email recipients.
    *** @param subject   The email subject.
    *** @param msgBody   The email message body.
    *** @param smtpProps The custom SMTP properties
    *** @param queRetry  True to queue to outbox on connection/auth failure. (may not be supported)
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, 
        String subject, String msgBody,
        SmtpProperties smtpProps,
        boolean queRetry)
    {
        String ato[]  = (to != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = null;
        String abcc[] = null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,null/*attach*/,smtpProps,queRetry);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sends an email
    *** @param from     The sender of the email
    *** @param to       A comma-separated list of email recipients.
    *** @param subject  The email subject.
    *** @param msgBody  The email message body.
    *** @param attach   An email attachment
    *** @param smtpProps The custom SMTP properties
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, 
        String subject, String msgBody, 
        SendMail.Attachments attach,
        SmtpProperties smtpProps)
    {
        String ato[]  = (to  != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = null;
        String abcc[] = null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,attach,smtpProps,false);
    }

    /**
    *** Sends an email
    *** @param from     The sender of the email
    *** @param to       A comma-separated list of email recipients.
    *** @param subject  The email subject.
    *** @param msgBody  The email message body.
    *** @param attach   An email attachment
    *** @param smtpProps The custom SMTP properties
    *** @param queRetry  True to queue to outbox on connection/auth failure. (may not be supported)
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, 
        String subject, String msgBody, 
        SendMail.Attachments attach,
        SmtpProperties smtpProps,
        boolean queRetry)
    {
        String ato[]  = (to  != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = null;
        String abcc[] = null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,attach,smtpProps,queRetry);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sends an email
    *** @param from     The sender of the email
    *** @param to       A comma-separated list of 'To:' email recipients.
    *** @param cc       A comma-separated list of 'Cc:' email recipients.
    *** @param bcc      A comma-separated list of 'Bcc:' email recipients.
    *** @param subject  The email subject.
    *** @param msgBody  The email message body.
    *** @param attach   An email attachment
    *** @param smtpProps The custom SMTP properties
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, String cc, String bcc,
        String subject, String msgBody, 
        SendMail.Attachments attach,
        SmtpProperties smtpProps)
    {
        String ato[]  = (to  != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = (cc  != null)? StringTools.parseStringArray(cc ,',') : null;
        String abcc[] = (bcc != null)? StringTools.parseStringArray(bcc,',') : null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,attach,smtpProps,false);
    }

    /**
    *** Sends an email
    *** @param from     The sender of the email
    *** @param to       A comma-separated list of 'To:' email recipients.
    *** @param cc       A comma-separated list of 'Cc:' email recipients.
    *** @param bcc      A comma-separated list of 'Bcc:' email recipients.
    *** @param subject  The email subject.
    *** @param msgBody  The email message body.
    *** @param attach   An email attachment
    *** @param smtpProps The custom SMTP properties
    *** @param queRetry True to queue to outbox on connection/auth failure. (may not be supported)
    *** @return True if email is queued/sent
    **/
    public static SendResult send(
        String from, String to, String cc, String bcc,
        String subject, String msgBody, 
        SendMail.Attachments attach,
        SmtpProperties smtpProps,
        boolean queRetry)
    {
        String ato[]  = (to  != null)? StringTools.parseStringArray(to ,',') : null;
        String acc[]  = (cc  != null)? StringTools.parseStringArray(cc ,',') : null;
        String abcc[] = (bcc != null)? StringTools.parseStringArray(bcc,',') : null;
        return SendMail.send(null,from,ato,acc,abcc,subject,msgBody,attach,smtpProps,queRetry);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sends an email
    *** @param from      The sender of the email
    *** @param to        An array of 'To:' email recipients.
    *** @param cc        An array of 'Cc:' email recipients.
    *** @param bcc       An array of 'Bcc:' email recipients.
    *** @param subject   The email subject.
    *** @param msgBody   The email message body.
    *** @param attach    An email attachment
    *** @param smtpProps The custom SMTP properties
    *** @param queRetry  True to queue to outbox on connection/auth failure. (may not be supported)
    *** @return True
    **/
    public static SendResult send(
        Properties headers, 
        String from, String to[], String cc[], String bcc[], 
        String subject, String msgBody, 
        SendMail.Attachments attach,
        SmtpProperties smtpProps,
        boolean queRetry)
    {
        Args args = new SendMail.Args(headers,from,to,cc,bcc,subject,msgBody,attach,smtpProps,queRetry);
        return SendMail.send(args);
    }

    /**
    *** Sends an email
    *** @param args      The SendMail arguments/parameters
    *** @return SendResult
    **/
    public static SendResult send(
        Args args 
        )
    {
        if (args == null) {
            return SendResult.IGNORED;
        }
        SendMailRunnable smr = new SendMailRunnable(args);
        boolean showThreadModel = args.getSmtpProperties().getShowThreadModel(); // SendMail.GetShowThreadModel();
        switch (args.getSmtpProperties().getThreadModel()) { // (SendMail.GetThreadModel()) 
            case NONE     :
                //if (showThreadModel) {
                    Print.logDebug("Skipping SendMail (disabled by '"+RTKey.SMTP_THREAD_MODEL+"') ...");
                //}
                args.setSendResult(SendResult.IGNORED);
                return SendResult.IGNORED; // false
            case CURRENT  :
                if (showThreadModel) {
                    Print.logDebug("Running SendMail in current thread");
                }
                smr.run();
                return smr.emailSent()? SendResult.SUCCESS : SendResult.FAILED;
            case NEW   :
                if (showThreadModel) {
                    Print.logDebug("Starting new SendMail thread");
                }
                (new Thread(smr)).start();
                return SendResult.QUEUED; // true
            case DEBUG :
                Print.logDebug("Debug SendMail (email not sent)");
                Print.logDebug(smr.getArgs().toString());
                args.setSendResult(SendResult.IGNORED);
                return SendResult.IGNORED; // false
            case POOL  :
            default : // UNDEFINED?
                if (showThreadModel) {
                    Print.logDebug("Running SendMail in thread pool");
                }
                ThreadPool_SendMail.run(smr);
                return SendResult.QUEUED; // true
        }
    }

    /**
    *** SendMailRunnable class.
    **/
    private static class SendMailRunnable
        implements Runnable
    {
        private Args      args      = null;
        private Throwable sendError = null;
        private boolean   emailSent = false;
        private boolean   retrySend = false;
        public SendMailRunnable(Args args) {
            this.args = args;
        }
        public Args getArgs() {
            return this.args;
        }
        public void run() {
            // -- no args?
            if (this.args == null) {
                // -- exit now
                Print.logWarn("No SMTP parameters, ignoring email ...");
                return;
            }
            // -- send email
            long startMS    = System.currentTimeMillis();
            int maxTryCount = 1 + this.args.getRetryCount(); // max = try + retry
            for (int i = 0; i < maxTryCount; i++) {
                /**/Print.logInfo("---------------------------\nSend Email Attempt #"+(i+1)+"/"+maxTryCount);
                try {
                    this.retrySend = false;
                    //this.emailSent = SendMailArgs.send(this.args);
                    Class<?> sendMailArgs = GetSendMailArgs_class();
                    if (sendMailArgs != null) {
                        MethodAction ma = new MethodAction(sendMailArgs, "send", Args.class);
                        ma.invoke(this.args); 
                        this.emailSent = true; // successful if we are here
                        break; // success
                    } else {
                        throw SendMail.newSendMailException("SendMailArgs: 'javax.mail.jar' may not be properly installed");
                    }
                } catch (SendMail.SendMailException sme) {
                    // -- failed to send email
                    this.sendError = sme;
                    this.emailSent = false;
                    this.retrySend = sme.getRetry();
                    if (this.retrySend && ((i + 1) < maxTryCount)) {
                        // -- sleep for a random amount of time, then try again
                        Print.logInfo("Email 'send' failed: " + sme + " (retrying...)");
                        long sleepMS = (long)Randomizer.nextInt(500);
                        try { Thread.sleep(sleepMS); } catch (Throwable th) {/*ignore*/}
                        continue; // retry ok
                    }
                    Print.logWarn("Email 'send' failed: " + sme);
                } catch (Throwable th) {
                    // -- catch-all, should not occur
                    Print.logWarn("Email 'send' failed: " + th);
                    this.sendError = th;
                    this.emailSent = false;
                    this.retrySend = false;
                }
                // -- no retry
                break;
            } // maxTryCount
            // -- still need to retry?  add to outbox
            if (!this.emailSent && this.retrySend && this.args.getQueueRetry()) {
                // -- save to outbox
                SendMail.SaveToOutbox(this.args);
            }
            // -- set SendResult (allow for callback notification)
            this.args.setSendResult(this.emailSent? SendResult.SUCCESS : SendResult.FAILED);
            // -- sleep after sending email?
            long endMS = System.currentTimeMillis();
            if (SendMail.SleepMSBetweenEMails > 0L) {
                // -- hack to slow down emails being sent for SMTP servers that cannot handle the volume
                long deltaMS = SendMail.SleepMSBetweenEMails - (endMS - startMS);
                if (deltaMS > 0L) {
                    long sleepMS = Math.min(deltaMS, 30000L);
                    try { Thread.sleep(sleepMS); } catch (Throwable th) {/*ignore*/}
                }
            }
        }
        public boolean emailSent() {
            return this.emailSent;
        }
        public boolean retrySend() {
            return this.retrySend;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns the MIME contents type for the specified file contents
    *** @param data  The file contents
    *** @return The MIME content type
    **/
    public static String DefaultContentType(byte data[], String dft)
    {
        String code = null;

        /* GIF */
        if (StringTools.compareEquals(data,MAGIC_GIF_87a,-1)) {
            return HTMLTools.MIME_GIF();
        } else
        if (StringTools.compareEquals(data,MAGIC_GIF_89a,-1)) {
            return HTMLTools.MIME_GIF();
        }

        /* JPEG */
        if (StringTools.compareEquals(data,MAGIC_JPEG,-1)) {
            return HTMLTools.MIME_JPEG();
        }

        /* PNG */
        if (StringTools.compareEquals(data,MAGIC_PNG,-1)) {
            return HTMLTools.MIME_PNG();
        }

        /* HTML */
        if (HTMLTools.isHtmlText(data)) {
            return HTMLTools.MIME_HTML();
        }

        /* default */
        return dft;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String DFT_ATTACHMENT_NAME   = "attachment.att";
    private static final String DFT_ATTACHMENT_TYPE   = HTMLTools.CONTENT_TYPE_OCTET;

    public  static final String MULTIPART_MIXED       = "mixed";       // multipart/mixed
    public  static final String MULTIPART_ALTERNATIVE = "alternative"; // multipart/alternative
    public  static final String MULTIPART_RELATED     = "related";     // multipart/related
    public  static final String MULTIPART_REPORT      = "report";      // multipart/report

    /**
    *** A container for an email attachment
    **/
    public static class Attachment
    {
        private byte       data[] = null;
        private String     name   = DFT_ATTACHMENT_NAME;
        private String     type   = DFT_ATTACHMENT_TYPE;
        public Attachment(byte data[]) {
            // -- attachment data only, default name/type
            this(data, null, null);
        }
        public Attachment(byte data[], String name, String type) {
            // -- explicit attachment components
            this.data = data;
            this.name = !StringTools.isBlank(name)? name : DFT_ATTACHMENT_NAME;
            this.type = !StringTools.isBlank(type)? type : DFT_ATTACHMENT_TYPE;
        }
        public Attachment(String csvData) {
            // -- reconstruct attachment from String
            // "name,mime,0x1234567890"
            String d[] = StringTools.split(csvData,',');
            this.name = (d.length > 0)? d[0] : null;
            this.type = (d.length > 1)? d[1] : null;
            this.data = (d.length > 2)? StringTools.parseHex(d[2],null) : null;
            if (StringTools.isBlank(this.name)) {
                this.name = DFT_ATTACHMENT_NAME;
            }
            if (StringTools.isBlank(this.type)) {
                this.type = DFT_ATTACHMENT_TYPE;
            }
        }
        public byte[] getBytes() {
            return this.data;
        }
        public int getSize() {
            return (this.data != null)? this.data.length : 0;
        }
        public String getName() {
            return this.name;
        }
        public String getType() {
            return this.type;
        }
        public boolean isTextPlain() {
            String t = this.getType();
            if (t.equalsIgnoreCase(HTMLTools.CONTENT_TYPE_PLAIN)) {
                return true;
            }
            return false;
        }
        public boolean isHTML() {
            String t = this.getType();
            if (t.equalsIgnoreCase(HTMLTools.CONTENT_TYPE_HTML)) {
                return true;
            }
            return false;
        }
        public boolean isImage() {
            String t = this.getType();
            if (StringTools.startsWithIgnoreCase(t,HTMLTools.CONTENT_TYPE_IMAGE_)) {
                // -- image MIME types
                // -    HTMLTools.CONTENT_TYPE_GIF
                // -    HTMLTools.CONTENT_TYPE_JPEG
                // -    HTMLTools.CONTENT_TYPE_PNG
                // -    HTMLTools.CONTENT_TYPE_TIFF
                // -    HTMLTools.CONTENT_TYPE_BMP
                return true;
            } else {
                return false;
            }
        }
        public String toString() {
            // "name,mime,0x1234567890"
            StringBuffer sb = new StringBuffer();
            sb.append(this.getName()).append(",");
            sb.append(this.getType()).append(",");
            if (this.getSize() > 0) {
                sb.append("0x").append(StringTools.toHexString(this.getBytes()));
            }
            return sb.toString();
        }
    }

    /**
    *** A container for list of individual <code>Attachment</code> objects.
    *** (does not yet support containing recursive lists of <code>Attachments</code>)
    **/
    public static class Attachments
    {
        private String                      multipartType = "";
        private Vector<SendMail.Attachment> attach        = null;
        // --
        public Attachments() {
            super();
        }
        public Attachments(String mpt, SendMail.Attachment... att) {
            this();
            this.setMultipartType(mpt);
            this.setAttachments(att);
        }
        public Attachments(SendMail.Attachment... att) {
            this(null,att);
        }
        // --
        public void setMultipartType(String mpt) {
            this.multipartType = StringTools.trim(mpt);
        }
        public String getMultipartType() {
            return StringTools.trim(this.multipartType);
        }
        public String getMultipartType(String dftType) {
            String mpt = this.getMultipartType();
            if (!StringTools.isBlank(mpt)) {
                // -- explicit Multipart type for this group of Attachments
                return mpt;
            } else
            if (!StringTools.isBlank(dftType)) {
                // -- system default Multipart type
                return StringTools.trim(dftType);
            } else 
            if (!this.hasAttachments()) {
                // -- no Attachments, return "mixed"
                return ""; // mixed
            } else {
                // -- guess at MimeMultipart type based on Attachment types
                boolean hasPlain  = false;
                boolean hasHTML   = false;
                boolean hasImages = false;
                for (int i = 0; i < this.attach.size(); i++) {
                    SendMail.Attachment att = this.attach.get(i);
                    if (att.isTextPlain()) {
                        // -- alternative or mixed
                        hasPlain = true;
                        if (hasImages) {
                            return ""; // mixed (images and text)
                        }
                    } else
                    if (att.isHTML()) {
                        // -- alternative
                        hasHTML = true;
                    } else 
                    if (att.isImage()) {
                        // -- related or mixed
                        hasImages = true;
                        if (hasPlain) {
                            return ""; // mixed (images and text)
                        }
                    } else {
                        // -- mixed (something other than images/text)
                        return ""; // mixed 
                    }
                }
                // --
                if (!hasHTML) {
                    // -- no HTML, assume "mixed"
                    return ""; // mixed 
                } else
                if (hasImages && !hasPlain) {
                    // -- HTML and Images (no text)
                    return MULTIPART_RELATED;
                } else
                if (!hasImages && hasPlain) {
                    // -- HTML and PlainText (no images)
                    return MULTIPART_ALTERNATIVE;
                } else {
                    // -- HTML and Images and PlainText
                    return ""; // mixed
                }
            }
        }
        // --
        public void setAttachments(java.util.List<SendMail.Attachment> attList) {
            this.attach = null;
            if (attList != null) {
                for (SendMail.Attachment A : attList) {
                    this.addAttachment(A);
                }
            }
        }
        public void setAttachments(SendMail.Attachment... attList) {
            this.attach = null;
            if (attList != null) {
                for (SendMail.Attachment A : attList) {
                    this.addAttachment(A);
                }
            }
        }
        public void addAttachment(SendMail.Attachment att) {
            if ((att != null) && (att.getSize() > 0)) {
                if (this.attach == null) {
                    this.attach = new Vector<SendMail.Attachment>();
                }
                this.attach.add(att);
            }
        }
        public void insertAttachment(SendMail.Attachment att) {
            if ((att != null) && (att.getSize() > 0)) {
                if (this.attach == null) {
                    this.attach = new Vector<SendMail.Attachment>();
                }
                this.attach.add(0,att); // add as first Attachment
            }
        }
        public java.util.List<SendMail.Attachment> getAttachments() {
            return this.attach;
        }
        public boolean hasAttachments() {
            return !ListTools.isEmpty(this.attach);
        }
        // -- 
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getMultipartType());
            if (this.hasAttachments()) {
                for (SendMail.Attachment A : this.getAttachments()) {
                    sb.append("|");
                    sb.append(A.toString());
                }
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** A container for the arguments of an email
    **/
    public static class Args
    {
        private Properties           headers     = null;
        private String               from        = null;
        private String               to[]        = null;
        private String               cc[]        = null;
        private String               bcc[]       = null;
        private String               subject     = null;
        private String               msgBody     = null;
        private SendMail.Attachments attachments = null;
        private SmtpProperties       smtpProps   = null;
        private boolean              queRetry    = false;
        private SendResult           sendResult  = null;
        //public Args(Properties headers, 
        //    String from, String to[], String cc[], String bcc[], 
        //    String subject, String msgBody, 
        //    SendMail.Attachment attach[],
        //    SmtpProperties smtpProps) {
        //    this(headers,from,to,cc,bcc,subject,msgBody,attach,smtpProps,false/*retry?*/);
        //}
        public Args(Properties headers, 
            String from, String to[], String cc[], String bcc[], 
            String subject, String msgBody, 
            SendMail.Attachments attach,
            SmtpProperties smtpProps,
            boolean queRetry) {
            this.headers     = (headers != null)? headers : new Properties();
            this.from        = from;
            this.to          = to;
            this.cc          = cc;
            this.bcc         = bcc;
            this.subject     = subject;
            this.msgBody     = msgBody;
            this.attachments = attach;
            this.smtpProps   = (smtpProps != null)? smtpProps : new SmtpProperties("SendMail:args");
            this.queRetry    = queRetry;
        }
        public Args(Properties headers, 
            String from, String to, String cc, String bcc, 
            String subject, String msgBody, 
            SendMail.Attachments attach,
            SmtpProperties smtpProps,
            boolean queRetry) {
            this.headers     = (headers != null)? headers : new Properties();
            this.from        = from;
            this.to          = (to  != null)? StringTools.parseStringArray(to ,',') : null;
            this.cc          = (cc  != null)? StringTools.parseStringArray(cc ,',') : null;
            this.bcc         = (bcc != null)? StringTools.parseStringArray(bcc,',') : null;
            this.subject     = subject;
            this.msgBody     = msgBody;
            this.attachments = attach;
            this.smtpProps   = (smtpProps != null)? smtpProps : new SmtpProperties("SendMail:args");
            this.queRetry    = queRetry;
        }
        public Properties getHeaders() {
            return this.headers;
        }
        public String getFrom() {
            return (this.from != null)? this.from : "";
        }
        public String[] getTo() {
            return (this.to != null)? this.to : new String[0];
        }
        public String[] getCc() {
            return (this.cc != null)? this.cc : new String[0];
        }
        public String[] getBcc() {
            return (this.bcc != null)? this.bcc : new String[0];
        }
        public String getSubject() {
            return (this.subject != null)? this.subject : "";
        }
        public String getBody() {
            return (this.msgBody != null)? this.msgBody : "";
        }
        public SendMail.Attachments getAttachments() {
            return this.attachments;
        }
        public SmtpProperties getSmtpProperties() {
            return this.smtpProps; // never null
        }
        public boolean getQueueRetry() {
            return this.queRetry;
        }
        public int getRetryCount() {
            if (this.getQueueRetry()) {
                // -- do not retry if email is saved in outbox
                return 0; // fixed [2.6.7-B46q]
            } else {
                return this.getSmtpProperties().getRetryCount(); // fixed [2.6.7-B46q]
            }
        }
        public String toString() {
            StringBuffer sb = new StringBuffer().append("\n");
            Properties headers = this.getHeaders();
            if ((headers != null) && !headers.isEmpty()) {
                for (Iterator<?> i = headers.keySet().iterator(); i.hasNext();) {
                    String k = (String)i.next();
                    String v = headers.getProperty(k);
                    if (v != null) {
                        sb.append(k).append(": ");
                        sb.append(v).append("\n");
                    }
                }
            }
            sb.append("From: ").append(this.getFrom()).append("\n");
            sb.append("To: ").append(StringTools.encodeArray(this.getTo())).append("\n");
            sb.append("Subject: ").append(this.getSubject()).append("\n");
            sb.append(this.getBody()).append("\n");
            SendMail.Attachments attach = this.getAttachments();
            if ((attach != null) && attach.hasAttachments()) {
                for (SendMail.Attachment A : attach.getAttachments()) {
                    sb.append("---- attachment ----\n");
                    sb.append(StringTools.toHexString(A.getBytes())).append("\n");
                }
            }
            sb.append("\n");
            return sb.toString();
        }
        public void setSendResult(SendResult result) {
            this.sendResult = result;
        }
        public SendResult getSendResult() {
            return this.sendResult;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Validate the specified list of comma-separated email addresses.
    *** @param addr  A comma-separated list of email addresses
    *** @return True if all addresses in the list are valid, false otherwise
    **/
    public static boolean validateAddresses(String addr)
    {
        if (StringTools.isBlank(addr)) {
            return false;
        }
        String addrArry[] = StringTools.parseStringArray(addr, ',');
        if (addrArry.length == 0) { return false; }
        for (int i = 0; i < addrArry.length; i++) {
            String em = addrArry[i].trim();
            if (em.equals("")) { return false; }
            if (!SendMail.validateAddress(em)) { return false; }
        }
        return true;
    }
    
    /**
    *** Validate the specified email address.
    *** @param addr  The email address to validate.
    *** @return True if the specified email address is valid, false otherwise
    **/
    public static boolean validateAddress(String addr) // RFC6532?
    {
        String xAddr = SendMail.getEMailAddress(addr); // new InternetAddress(addr).getAddress()
        boolean validateDebug = false;

        /* blank is invalid */
        if (StringTools.isBlank(xAddr)) {
            if (validateDebug) { Print.logError("Is blank"); }
            return false;
        }

        /* double-dots not allowed (ie. "a..b@a.b") */
        if (xAddr.indexOf("..") >= 0) {
            if (validateDebug) { Print.logError("Contains '..'"); }
            return false;
        }

        /* extract host */
        int hp = xAddr.indexOf("@");
        if (hp <= 0) {
            // -- unlikely, since already checked by 'getEMailAddress' above
            if (validateDebug) { Print.logError("Missing '@'"); }
            return false;
        }
        String host = xAddr.substring(hp+1);

        /* host must have at least one "." (local host names not allowed) */
        if (host.indexOf(".") <= 0) {
            if (validateDebug) { Print.logError("Host missing '.'"); }
            return false;
        }

        /* host name may not have embedded space */
        if (host.indexOf(" ") >= 0) {
            // -- unlikely, since already checked by 'getEMailAddress' above
            if (validateDebug) { Print.logError("Embedded space"); }
            return false;
        }

        /* host name regex */
        // -- https://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_hostnames
        // -  Internationalized Domain Names not allowed
      //String hostRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
        String hostRegex = "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
        try {
            Pattern regex = Pattern.compile(hostRegex);
            if (!regex.matcher(host).matches()) {
                if (validateDebug) { Print.logError("Failed regex"); }
                return false;
            }
        } catch (PatternSyntaxException pse) {
            Print.logError("Invalid fileName regular expression: " + hostRegex);
        }

        /* otherwise assume ok */
        return true;

    }

    /** 
    *** Filters and returns the base email address from the specified String.<br>
    *** For example, if the String "Jones&lt;jones@example.com&gt;" is passed to this
    *** method, then the value "jones@example.com" will be returned.
    *** @param addr The email address to filter.
    *** @return  The filtered email address, or null if the specified email address is invalid.
    **/
    public static String getEMailAddress(String addr)
    {
        //return SendMailArgs.parseEMailAddress(addr);
        try {
            Class<?> sendMailArgs = GetSendMailArgs_class();
            if (sendMailArgs != null) {
                MethodAction ma = new MethodAction(sendMailArgs, "parseEMailAddress", String.class);
                return (String)ma.invoke(addr);
            } else {
                Print.logWarn("Unable to get SendMailArgs class (javax.mail.jar installed?)");
                return null;
            }
        } catch (Throwable th) {
            Print.logWarn("Unable to invoke 'parseEMailAddress': " + th);
            return null;
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static String ARG_ADDR[]    = new String[] { "addr"     , "a"    };

    private static String ARG_FROM[]    = new String[] { "from"     , "f"    };
    private static String ARG_TO[]      = new String[] { "to"       , "t"    };
    private static String ARG_SUBJECT[] = new String[] { "subject"  , "subj", "s" };
    private static String ARG_BODY[]    = new String[] { "body"     , "b"    };
    private static String ARG_ATTACH[]  = new String[] { "attach"   , "att"  };
    private static String ARG_LOAD[]    = new String[] { "load"     , "file" };
    private static String ARG_RETRY[]   = new String[] { "retry"             };

    /**
    *** Command-line debug/testing entry point
    *** @param argv  The command-line arguments
    **/
    public static void main(String argv[])
    {
        // -- bin/exeJava org.opengts.util.SendMail -to=mflynn@opengts.org -subj=HelloWorld -body=Test
        // -- bin/exe org.opengts.util.SendMail -to=mflynn@opengts.org -subj=HelloWorld -body=Test -debug
        RTConfig.setCommandLineArgs(argv);
        boolean queRetry = false; // may not be supported

        /* debug mode? */
        if (RTConfig.isDebugMode()) {
            Print.logDebug("Setting SMTP Debug mode to 'true' ...");
            RTConfig.getRuntimeConstantProperties().setBoolean(RTKey.SMTP_DEBUG,true);
        }

        /* SmtpProperties */
        SmtpProperties smtpProps = new SmtpProperties("SendMail.main"); // default properties
        smtpProps.setThreadModel_CURRENT();
        smtpProps.setRetryCount(RTConfig.getInt(ARG_RETRY,0));

        /* validate email address */
        if (RTConfig.hasProperty(ARG_ADDR)) {
            String addr = RTConfig.getString(ARG_ADDR, "");
            Print.sysPrintln("Checking: " + addr);
            Print.sysPrintln("Address : " + SendMail.getEMailAddress(addr));
            Print.sysPrintln("Valid   ? " + SendMail.validateAddress(addr));
            Print.sysPrintln("");
            System.exit(0);
        }

        /* Load EMailContent */
        EMailContent emailContent = new EMailContent();
        // -- "-load"
        if (RTConfig.hasProperty(ARG_LOAD)) {
            File file = RTConfig.getFile(ARG_LOAD,null);
            if (!emailContent.loadFromFile(file)) {
                Print.sysPrintln("ERROR: Unable to load EMailContent file: " + file);
                System.exit(1);
            }
            emailContent.printDebugLog();
        }

        /* "From" */
        String fromAddr = RTConfig.getString(ARG_FROM,null);
        fromAddr = RTConfig.insertKeyValues(fromAddr);
        if (StringTools.isBlank(fromAddr) || fromAddr.endsWith(EXAMPLE_DOT_COM)) {
            fromAddr = smtpProps.getFromEmailType("cmdline");
            if (StringTools.isBlank(fromAddr)) {
                Print.sysPrintln("ERROR: Missing 'From' address.");
                Print.sysPrintln("(define \""+RTKey.SMTP_SERVER_USER_EMAIL+"\" in 'default.conf')");
                Print.sysPrintln("");
                System.exit(1);
            }
        } else
        if (fromAddr.equalsIgnoreCase("blank") || fromAddr.equalsIgnoreCase("default")) {
            fromAddr = null;
        }

        /* "To" */
        String toAddr = RTConfig.getString(ARG_TO,null);
        toAddr = RTConfig.insertKeyValues(toAddr);
        if (StringTools.isBlank(toAddr) || toAddr.endsWith(EXAMPLE_DOT_COM)) {
            Print.sysPrintln("Missing 'To' address");
            Print.sysPrintln("(specify '-to=<emailAddress>' option on command-line)");
            Print.sysPrintln("");
            System.exit(1);
        }

        /* Subject/Body/Attachment */
        // -- "-subject=" 
        if (RTConfig.hasProperty(ARG_SUBJECT)) {
            emailContent.setSubject(RTConfig.getString(ARG_SUBJECT,""));
        }
        // -- "-body="
        if (RTConfig.hasProperty(ARG_BODY)) {
            emailContent.setBody(RTConfig.getString(ARG_BODY,""));
        }
        // -- "-attach"
        if (RTConfig.hasProperty(ARG_ATTACH)) {
            File attachFile = RTConfig.getFile(ARG_ATTACH,null);
            if ((attachFile != null) && attachFile.isFile()) {
                InputStream fis = null;
                try {
                    fis = new FileInputStream(attachFile);
                    byte b[] = FileTools.readStream(fis); // IOException
                    String name = attachFile.getName();
                    String ext  = FileTools.getExtension(attachFile);
                    String type = HTMLTools.getMimeTypeFromExtension(ext,null); // CONTENT_TYPE_OCTET;
                    if (type == null) {
                        type = DefaultContentType(b,HTMLTools.CONTENT_TYPE_OCTET);
                    }
                    SendMail.Attachments attach = new SendMail.Attachments();
                    attach.addAttachment(new SendMail.Attachment(b,name,type));
                    emailContent.setAttachments(attach);
                } catch (Throwable th) {
                    Print.sysPrintln("ERROR: Unable to load attachment - " + th);
                } finally {
                    try { fis.close(); } catch (Throwable th) {/*ignore*/}
                }
            } else {
                Print.sysPrintln("ERROR: Specified Attachment does not exist - " + attachFile);
                System.exit(99);
            }
        }

        /* subject/body/attachment */
        String               subject = emailContent.getSubject((Map<String,String>)null);
        String               body    = emailContent.getBody((Map<String,String>)null);
        SendMail.Attachments attach  = emailContent.getAttachments();

        /* send email */
        Print.sysPrintln("Sending EMail: (retry="+smtpProps.getRetryCount()+")");
        Print.sysPrintln("   From   : " + fromAddr);
        Print.sysPrintln("   To     : " + toAddr);
        Print.sysPrintln("   Subject: " + subject);
        if ((attach != null) && attach.hasAttachments()) {
            for (SendMail.Attachment A : attach.getAttachments()) {
                if ((A == null) || (A.getSize() <= 0)) { continue; }
                Print.sysPrintln("   Attach : " + A.getName() + " ["+A.getSize()+" bytes]");
            }
        }
        Print.sysPrintln("   Body   : \n" + body);
        if (StringTools.indexOfIgnoreCase(toAddr,"localhost") >= 0) {
            Print.sysPrintln("... skipping email send");
        } else
        if (SendMail.send(fromAddr,toAddr,subject,body,attach,smtpProps,queRetry).isOK()) {
            Print.sysPrintln("... sent");
        } else {
            Print.sysPrintln("... Unable to send EMail");
        }
        Print.sysPrintln("");
        System.exit(0);

    }

}
