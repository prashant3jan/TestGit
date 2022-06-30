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
// References:
//   - http://java.sun.com/products/javamail/index.jsp
//   - https://github.com/javaee/javamail/releases
//   - https://javaee.github.io/javamail/docs/api/
//   - https://hellokoding.com/sending-email-through-gmail-smtp-server-with-java-mail-api-and-oauth-2-authorization/
// ----------------------------------------------------------------------------
// Change History:
//  2009/06/01  Martin D. Flynn
//     -Extracted from SendMail
//  2011/12/06  Martin D. Flynn
//     -Added "UTF-8" character set to email body text.
//  2012/08/01  Martin D. Flynn
//     -Added check for "smtp.ignoredEmail.file" (see IsIgnoredEmailAddress)
//  2013/08/27  Martin D. Flynn
//     -Default to "smtp.user.emailAddress" if explicit "from" address is blank/"default".
//  2018/09/10  GTS Development Team
//     -Moved "IsIgnoredEmailAddress(...)" to SmtpProperties
//  2020/02/19  GTS Development Team
//     -Added support for Google OAuth2 [2.6.7-B46n]
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;
import java.net.ConnectException;

import javax.activation.DataSource;
import javax.activation.DataHandler;

import javax.mail.Session;
import javax.mail.Authenticator;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.AuthenticationFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.AddressException;
import com.sun.mail.smtp.SMTPTransport;

public class SendMailArgs
{

    // ------------------------------------------------------------------------

    public static final boolean USE_AUTHENTICATOR   = true; // javax.mail.Authenticator

    public static final String  SSL_FACTORY         = "javax.net.ssl.SSLSocketFactory";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /** 
    *** Filters and returns the base email address from the specified String.<br>
    *** For example, if the String "Jones&lt;jones@example.com&gt;" is passed to this
    *** method, then the value "jones@example.com" will be returned.
    *** @param addr The email address to filter.
    *** @return  The filtered email address, or null if the specified email address is invalid.
    **/
    public static String parseEMailAddress(String addr)
    {
        if (!StringTools.isBlank(addr)) {
            try {
                InternetAddress ia = new InternetAddress(addr, true);
                return ia.getAddress();
            } catch (Throwable ae) { // AddressException
                Print.logWarn("Invalid EMail address: " + addr);
                return null;
            }
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Internal method to send email
    *** @param args  The email arguments
    *** @throws SendMail.SendMailException if an error occurs
    **/
    public static void send(SendMail.Args args)
        throws SendMail.SendMailException
    {
        String                 from = args.getFrom();
        String                 to[] = args.getTo();
        String                 cc[] = args.getCc();
        String                bcc[] = args.getBcc();
        String              subject = args.getSubject();
        String              msgBody = args.getBody();
        Properties          headers = args.getHeaders();
        SendMail.Attachments attach = args.getAttachments();
      //boolean          queueRetry = args.getQueueRetry();
        SmtpProperties    smtpProps = args.getSmtpProperties(); // never null
        String             smtpName = smtpProps.getName();

        /* SMTP properties */
        // -  mail.transport.protocol (String)
        // -  mail.debug (boolean)
        // -  mail.smtp.host (String)
        // -  mail.smtp.port (int)
        // -  mail.smtp.user (String)
        // -  mail.smtp.auth (boolean)
        // -  mail.smtp.connectiontimeout (int)  [miliseconds]
        // -  mail.smtp.timeout (int)  [miliseconds]
        // -  mail.smtp.socketFactory.class (String)
        // -  mail.smtp.socketFactory.port (int)
        // -  mail.smtp.socketFactory.fallback (boolean)
        // -  mail.smtp.starttls.enable (boolean)
        // -  mail.smtp.sendpartial (boolean)
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp"); // [2.6.7-B46k]

        // -- Debug
        if (smtpProps.getDebug()) {
            //if (!Print.isDebugLoggingLevel()) {
            //    Print.setLogLevel(Print.LOG_DEBUG);
            //    Print.logDebug("Enabled Print debug logging level");
            //}
            props.put("mail.debug", "true");
            Print.logDebug(smtpName+"] SendMail debug mode");
        }

        // -- SMTP host:port
        final String smtpHost = smtpProps.getHost();
        final int    smtpPort = smtpProps.getPort();
        if (StringTools.isBlank(smtpHost) || smtpHost.endsWith("example.com")) {
            Print.logError(smtpName+"] Null/Invalid SMTP host, not sending email");
            throw SendMail.newSendMailException("Null/Invalid SMTP host");
        } else
        if (smtpPort <= 0) {
            Print.logError(smtpName+"] Invalid SMTP port, not sending email");
            throw SendMail.newSendMailException("Invalid SMTP port");
        }
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        /* timeout */
        final int smtpTMO = smtpProps.getTimeoutMS();
        int timeout = (smtpTMO > 0)? smtpTMO : 60000;
        props.put("mail.smtp.connectiontimeout", String.valueOf(timeout)); // 60000
        props.put("mail.smtp.timeout"          , String.valueOf(timeout)); // 60000
      
        // The following can be used as a replacement for the value returned by
        // "InetAddress.getLocalHost().getHostName()".
      //props.put("mail.smtp.localhost"        , "mydomain.example.com");

        /* ok to send to valid email addresses? */
        final String sendPartial = smtpProps.getSendPartial();   // "true" | "false"
        if (sendPartial.equalsIgnoreCase("true")) {
            props.put("mail.smtp.sendpartial", "true");
        } else
        if (sendPartial.equalsIgnoreCase("false")) {
            props.put("mail.smtp.sendpartial", "false");
        }

        /* SSL */
        final String enableSSL = smtpProps.getEnableSSL();     // "only" | "true" | "false"
        if (enableSSL.equalsIgnoreCase("only") || enableSSL.equalsIgnoreCase("true")) {
            props.put("mail.smtp.socketFactory.port"        , String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class"       , SSL_FACTORY);
            props.put("mail.smtp.socketFactory.fallback"    , "false");
          //props.put("mail.smtp.socketFactory.fallback"    , "true");
            if (enableSSL.equalsIgnoreCase("only")) {
                props.put("mail.smtp.ssl.enable"            , "true");
                props.put("mail.smtp.ssl.socketFactory.port", String.valueOf(smtpPort));
            }
        } else {
            //enableSSL = "false";
        }

        /* TLS */
        final String enableTLS = smtpProps.getEnableTLS();     // "only" | "true" | "false"
        if (enableTLS.equalsIgnoreCase("only") || enableTLS.equalsIgnoreCase("true")) {
            props.put("mail.smtp.starttls.required", "true"); // iff "only"?
            props.put("mail.smtp.starttls.enable"  , "true");
        } else {
            //enableTLS == "false";
        }

        /* AuthenticationMethod */
        SmtpProperties.AuthenticationMethod authMeth = smtpProps.getAuthenticationMethod();
        Print.logDebug(smtpName+"] AuthenticationMethod: " + authMeth);

        /* SMTP Authenticator */
        final String smtpUser  = smtpProps.getUser();
        final String smtpUName = smtpProps.getUserFullName();
        final String smtpEmail = smtpProps.getUserEmail();  // "From"
        final String smtpPass  = smtpProps.getPassword();
        String oauth2Token = null;  // OAuth2 access-token [2.6.7-B46n]
        javax.mail.Authenticator auth = null;
        if (authMeth.isOAuth2()) {  // [2.6.7-B46n]
            // -- get OAuth2 access-token (required for Google email on 2021/02/15)
            try {
                oauth2Token = smtpProps.getOAuth2AccessToken(); // may call external service
            } catch (IOException ioe) {
                throw SendMail.newSendMailException("OAuth2 Access-Token Error", ioe);
            }
            props.put("mail.smtp.user"           , smtpUser);
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.auth"           , "true"); // SSL
            auth = new javax.mail.Authenticator() { // this may not be requried here
                public javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(smtpUser, smtpPass);
                }
            };
        } else
        if (USE_AUTHENTICATOR && !StringTools.isBlank(smtpUser)) { // authMeth.isNormalPassword()
            // -- NormalPassword Authenticator
            props.put("mail.smtp.user", smtpUser);
            props.put("mail.smtp.auth", "true");   // SSL
            auth = new javax.mail.Authenticator() {
                public javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(smtpUser, smtpPass);
                }
            };
        } else {
            //props.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN DIGEST-MD5 NTLM");
            //props.put("mail.smtp.auth"           , "true");
        }

        /* SMTP Session */
        //props.list(System.out);
        Session session = Session.getInstance(props, auth);
        //session.setDebug(true);

        /* prepare and send */
        Transport transport = null;
        try {
            MimeMessage msg = new MimeMessage(session);

            /* 'From' address */
            if (StringTools.isBlank(from) || from.equalsIgnoreCase("default")) {
                msg.setFrom(new InternetAddress(smtpEmail));
            } else {
                msg.setFrom(new InternetAddress(from));
            }

            /* add additional destination email addresses */
            // -- only BCC here, should a manager wish to receive a copy of all emails sent
            String addTO  = null; // not used here
            String addCC  = null; // not used here
            String addBCC = smtpProps.getBccEmail(); // may be blank

            /* destination email addresses */
            InternetAddress toAddr[]  = _convertRecipients("TO" , to , addTO , smtpProps);
            InternetAddress ccAddr[]  = _convertRecipients("CC" , cc , addCC , smtpProps);
            InternetAddress bccAddr[] = _convertRecipients("BCC", bcc, addBCC, smtpProps);
            if (ListTools.isEmpty(toAddr)) {
                // -- no 'To' email address
                Print.logError(smtpName+"] No 'To' address specified, not sending email");
                throw SendMail.newSendMailException("No 'To' address specified");
            }

            /* set headers */
            for (Iterator<?> i = headers.keySet().iterator(); i.hasNext();) {
                String k = (String)i.next();
                String v = headers.getProperty(k);
                if (v != null) {
                    msg.setHeader(k, v);
                }
            }

            /* set recipients */
            msg.setRecipients(Message.RecipientType.TO , toAddr);
            msg.setRecipients(Message.RecipientType.CC , ccAddr);
            msg.setRecipients(Message.RecipientType.BCC, bccAddr);

            /* subject */
            msg.setSubject(subject, StringTools.CharEncoding_UTF_8);

            /* date */
            msg.setSentDate(new Date());

            /* message body/content */
            if ((attach != null) && attach.hasAttachments()) {
                // -- "mixed"(multipart/mixed) [default]
                // -    Each attachment has a different content-type
                // -- "digest(multipart/digest)
                // -    Used for sending multiple text messages
                // -- "alternative"(multipart/alternative)
                // -    Each attachments is an "alternative" version of the same content
                // -- "related"(multipart/related) 
                // -    Each attachment is part of an aggregate whole
                // -- "report"(multipart/report)
                // -    Attachments contain data destine for an email reader
                final String smtpMPT = smtpProps.getMultipartType();
                String multipartType = attach.getMultipartType(smtpMPT); // [2.6.2-B52]
                //Print.logInfo("SendMail attach: multipartType=" + multipartType);
                Multipart multipart = !StringTools.isBlank(multipartType)?
                    new MimeMultipart(multipartType) : new MimeMultipart();
                // -- text body (add first)
                if (!StringTools.isBlank(msgBody)) {
                    //Print.logInfo("SendMail attach: text=" + msgBody);
                    MimeBodyPart textBodyPart = new MimeBodyPart();
                    textBodyPart.setText(msgBody, StringTools.CharEncoding_UTF_8);
                    multipart.addBodyPart(textBodyPart);
                }
                // -- add attachments
                for (SendMail.Attachment A : attach.getAttachments()) {
                    if ((A == null) || (A.getSize() <= 0)) { continue; }
                    //Print.logInfo("SendMail attach: name=" + A.getName() + ", type=" + A.getType());
                    BodyPart attachBodyPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(A.getName(), A.getType(), A.getBytes());
                    attachBodyPart.setDataHandler(new DataHandler(source));
                    attachBodyPart.setFileName(source.getName());
                    multipart.addBodyPart(attachBodyPart);
                }
                // -- set content 
                msg.setContent(multipart);
            } else {
                msg.setText(msgBody, StringTools.CharEncoding_UTF_8);
                //msg.setText(msgBody); // setContent(msgBody, CONTENT_TYPE_PLAIN);
            }

            /* save changes to message [may already be implicit with send()] */
            msg.saveChanges(); // redundant: implicit with send()

            /* send email */
            //Transport.send(msg); // java.net.ConnectException
            transport = new SMTPTransport(session, null/*URLName*/);
            transport.connect(smtpHost, smtpUser, (smtpPass!=null?smtpPass:""));
            if (authMeth.isOAuth2()) { // [2.6.7-B46n]/[2.6.7-B46q]
                // -- XOAUTH2 authentication requried
                String authStr = "user="+smtpUser+"\1auth=Bearer "+oauth2Token+"\1\1";
                String authCmd = "AUTH XOAUTH2 " + Base64.encode(authStr.getBytes());
                Print.logDebug(smtpName+"] Sending Command: AUTH XOAUTH2 <"+authStr+"> ...");
                try {
                    int expectResp = 235; // "Accepted" / "Authentication successful"?
                    ((SMTPTransport)transport).issueCommand(authCmd, expectResp);
                } catch (MessagingException me) {
                    // -- check for any returned JSON
                    // -- "334 eyJzdGF0dXMiOiI0MDAiLCJzY2hlbWVzIjoiQmVhcmVyIiwic2NvcGUiOiJodHRwczovL21haWwuZ29vZ2xlLmNvbS8ifQ=="
                    // -- JSON(Google): {"status":"400","schemes":"Bearer","scope":"https://mail.google.com/"}
                    /* -- not currently used, so commented for now --
                    String errMsg = me.getMessage();
                    //Print.logError("XOAUTH2 Error: Message=" + errMsg);
                    if (errMsg.startsWith("334 ")) {
                        try {
                            String errJsonStr = StringTools.toStringValue(Base64.decode(errMsg.substring("334".length())));
                            Print.logDebug(smtpName+"] XOAUTH2 Error: JSON=" + errJsonStr);
                            JSON._Object errJsonObj = JSON.parse_Object(errJsonStr);
                            String status = StringTools.trim(errJsonObj.getStringForName("status",null));
                            if (status.equals("400")) {
                                // -- Bad Request
                            }
                        } catch (Base64.Base64DecodeException bde) {
                            // -- not valid Base64
                        } catch (JSON.JSONParsingException jpe) {
                            // -- not valie JSON
                        }
                    }
                    */
                    boolean retry = true;
                    smtpProps.setOAuth2AccessTokenExpired(); // refresh access-token on next retry attempt
                    throw SendMail.newSendMailException("XOAUTH2 Error",me).setRetry(retry);
                    // -- SendMail Debug output:
                    // -    ...
                    // -    535-5.7.8 Username and Password not accepted. Learn more at
                    // -    535 5.7.8  https://support.google.com/mail/?p=BadCredentials z64sm51623137pfz.23 - gsmtp
                    // -    DEBUG SMTP: QUIT failed with 535
                }
            }
            transport.sendMessage(msg, msg.getAllRecipients());

            /* success (if we get here) */
            Print.logDebug(smtpName+"] Email sent ...");

        } catch (MessagingException me) {
            //Print.logException("--- Send ERROR ---", me);

            /* error */
            String message = null;
            boolean retry = false;
            Print.logStackTrace(smtpName+"] Unable to send email [host="+smtpHost+"; port="+smtpPort+"; SSL="+enableSSL+"; TLS="+enableTLS+"]", me);
            for (Exception ex = me; ex != null;) {
                if (ex instanceof SendFailedException) {
                    // javax.mail.SendFailedException
                    // -- unable to send to some of the listed recipients
                    SendFailedException sfex = (SendFailedException)ex;
                    _printAddresses("Invalid:"     , sfex.getInvalidAddresses());
                    _printAddresses("Valid Unsent:", sfex.getValidUnsentAddresses());
                    _printAddresses("Valid Sent:"  , sfex.getValidSentAddresses());
                    message = "Partial send";
                    retry   = false;
                } else
                if (ex instanceof ConnectException) {
                    // java.net.ConnectException: Connection timed out
                    // -- save/retry?
                    message = "Connection Error";
                    retry   = true;
                } else
                if (ex instanceof AuthenticationFailedException) {
                    // javax.mail.AuthenticationFailedException: failed to connect
                    // -- save/retry?
                    message = "Authentication Failed";
                    retry   = true;
                }
                // -- next exception
                ex = (ex instanceof MessagingException)? ((MessagingException)ex).getNextException() : null;
            }

            /* did not send email */
            if (!StringTools.isBlank(message)) {
                throw SendMail.newSendMailException(message,me).setRetry(retry);
            } else {
                throw SendMail.newSendMailException(me).setRetry(retry);
            }

        } finally {

            /* close transport, if open */
            if (transport != null) {
                try {
                    transport.close(); // throws MessagingException
                } catch (MessagingException me) {
                    Print.logStackTrace("Unable to close Transport", me);
                }
            }

        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Converts the list of String email addresses to instances of 'InternetAddress'
    *** @param type       One of "TO","CC", or "BCC"
    *** @param to         The array of email addresses
    *** @param addTo      An additional 'To' email address
    *** @param smtpProps  The local SmtpProperties instance (must not be null)
    *** @return An array of InternetAddress instances
    *** @throws AddressException if any of the specified email addresses are invalid
    **/
    private static InternetAddress[] _convertRecipients(
        String type, String to[], String addTo,
        SmtpProperties smtpProps)
        throws AddressException
    {
        java.util.List<InternetAddress> inetAddr = new Vector<InternetAddress>();

        /* loop through "to" */
        for (int i = 0; i < to.length; i++) {

            /* trim email address */
            String t = (to[i] != null)? to[i].trim() : "";
            if (t.equals("")) { 
                //Print.logWarn("Ignoring Blank Email Address");
                continue;
            }

            /* check for email address to skip */
            if (smtpProps.isIgnoredEmailAddress(t)) {
                Print.logWarn("Ignoring Email Address ["+type+"]: " + t);
                continue; 
            }

            /* convert to InternetAddress instance */
            // -- this should also validate email addresses
            try {
                inetAddr.add(new InternetAddress(t)); 
            } catch (AddressException ae) {
                Print.logStackTrace("Invalid Address ["+type+"]: " + t + " (ignored)", ae);
            }

        }

        /* additional "To" */
        if (!StringTools.isBlank(addTo)) {
            // -- add additional address
            try {
                inetAddr.add(new InternetAddress(addTo)); 
            } catch (AddressException ae) {
                Print.logStackTrace("Address ["+type+"]: " + addTo + " (skipped)", ae);
            }
        }

        /* return array of InternetAddress */
        return inetAddr.toArray(new InternetAddress[inetAddr.size()]);

    }

    // ------------------------------------------------------------------------

    /**
    *** Prints the list of email addresses (debug purposes only)
    **/
    private static void _printAddresses(String msg, Address addr[])
    {
        if (addr != null) {
            Print.logInfo(msg);
            for (int i = 0; i < addr.length; i++) {
                Print.logInfo("    " + addr[i]);
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** ByteArrayDataSource class
    **/
    private static class ByteArrayDataSource
        implements DataSource
    {
        private String name   = null;
        private String type   = null;
        private Object source = null;
        private ByteArrayDataSource(String name, String type, Object src) {
            this.name   = name;
            this.type   = type;
            this.source = src;
        }
        public ByteArrayDataSource(String name, byte src[]) {
            this(name, null, src);
        }
        public ByteArrayDataSource(String name, String type, byte src[]) {
            this(name, type, (Object)src);
        }
        public ByteArrayDataSource(String name, String src) {
            this(name, null, src);
        }
        public ByteArrayDataSource(String name, String type, String src) {
            this(name, type, (Object)src);
        }
        public String getName() {
            return (this.name != null)? this.name : "";
        }
        public String getContentType() {
            if (this.type != null) {
                return this.type;
            } else 
            if (this.getName().toLowerCase().endsWith(".csv")) {
                return HTMLTools.MIME_CSV();
            } else 
            if (this.getName().toLowerCase().endsWith(".gif")) {
                return HTMLTools.MIME_GIF();
            } else 
            if (this.getName().toLowerCase().endsWith(".png")) {
                return HTMLTools.MIME_PNG();
            } else
            if (this.source instanceof byte[]) {
                return SendMail.DefaultContentType((byte[])this.source,HTMLTools.CONTENT_TYPE_OCTET);
            } else
            if (this.source instanceof ByteArrayOutputStream) {
                return SendMail.DefaultContentType(((ByteArrayOutputStream)this.source).toByteArray(),HTMLTools.CONTENT_TYPE_OCTET);
            } else {
                return HTMLTools.MIME_PLAIN();
            }
        }
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.toByteArray());
        }
        public OutputStream getOutputStream() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte b[] = this.toByteArray();
            if ((b != null) && (b.length > 0)) {
                out.write(b, 0, b.length);
            }
            this.source = out;
            return (ByteArrayOutputStream)this.source;
        }
        private byte[] toByteArray() {
            if (this.source == null) {
                return new byte[0];
            } else
            if (this.source instanceof byte[]) {
                return (byte[])this.source;
            } else
            if (this.source instanceof ByteArrayOutputStream) {
                return ((ByteArrayOutputStream)this.source).toByteArray();
            } else {
                return StringTools.getBytes(this.source.toString());
            }
        }
    }                               

    // ------------------------------------------------------------------------

}
