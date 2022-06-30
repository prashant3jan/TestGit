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
//  2018/09/10  Martin D. Flynn
//     -Initial release. 
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;

public class Response 
{

    // ------------------------------------------------------------------------
    // -- Response args

    public  static final String  RESPV_VER          = "ver";    // ctrac version
    public  static final String  RESPV_ACCOUNT      = "a";      // accountID
    public  static final String  RESPV_DEVICE       = "d";      // deviceID
    public  static final String  RESPV_MOBILEID     = "m";      // mobileID
    public  static final String  RESPV_EC           = "ec";     // eventCount
    public  static final String  RESPV_MP           = "mp";     // messagePending

    public  static final String  RESPV_NEWAPP       = "newapp"; // newest app version
    public  static final String  RESPV_NEWGTC       = "newgtc"; // newest gtc version

    // ------------------------------------------------------------------------

    public static final long RESPFMT_MASK           = 0x0000000FL; //
    public static final long RESPFMT_UNDEFINED      = 0x00000000L; //
    public static final long RESPFMT_JSON           = 0x00000001L; //
    public static final long RESPFMT_TEXT           = 0x00000002L; //
    public static final long RESPFMT_OMITSUCCESS    = 0x00000010L; // omit "success" tag if "OK"
    public static final long RESPFMT_HTTPERROR      = 0x00000020L; // 

    /* response type (only JSON, and non-JSON) */
    public static final  String RESP_FMT_TEXT       = "text"; // 
    public static final  String RESP_FMT_JSON       = "json"; // must match prefix for RESP_ERR_JSON/RESP_OK_JSON
    public static final  String RESP_FMT_JSE        = "jse";  // 
    public static final  String RESP_FMT_JS         = "js";   // 

    /**
    *** Parse Response format String into format mask
    **/
    public static long ParseFormat(String fmt) // "json", "text", "js", "jse", "je", "ts", "tse", "te"
    {
        fmt = StringTools.trim(fmt).toLowerCase();
        long mask = Response.getDefaultResponseFormat();
        if (StringTools.isBlank(fmt)) {
            // -- return as-is below
        } else
        if (fmt.equals(RESP_FMT_JSON)) {
            // -- force JSON
            mask = Response.SetJSON(mask);
        } else
        if (fmt.equals(RESP_FMT_TEXT)) {
            // -- force TEXT
            mask = Response.SetTEXT(mask);
        } else {
            // -- parse format: ie "jse", "tse", etc.
            boolean not = false;
            for (int c = 0; c < fmt.length(); c++) {
                char ch = fmt.charAt(c);
                // -- JSON, TEXT
                if (ch == 'j') {
                    mask = not? Response.SetTEXT(mask)          : Response.SetJSON(mask);
                } else
                if (ch == 't') {
                    mask = not? Response.SetJSON(mask)          : Response.SetTEXT(mask);
                } else
                if (ch == 's') {
                    mask = not? Response.ClearOmitSuccess(mask) : Response.SetOmitSuccess(mask);
                } else
                if (ch == 'e') {
                    mask = not? Response.ClearHttpError(mask)   : Response.SetHttpError(mask);
                } else
                if ((ch == '-') || (ch == '!')) {
                    not = !not;
                } else {
                    Print.logWarn("Unexpected response format character: " + ch + " (ignored)");
                }
            }
        }
        return mask;
    }

    // --------------------------------

    private static      long RESPFMT_DEFAULT = Response.RESPFMT_JSON;

    /**
    *** Sets the default response format
    **/
    public static void setDefaultResponseFormat(String dft)
    {
        if (!StringTools.isBlank(dft)) {
            Response.setDefaultResponseFormat(Response.ParseFormat(dft));
        }
    }

    /**
    *** Sets the default response format
    **/
    public static void setDefaultResponseFormat(long dft)
    {
        // RTConfig.getString("CelltracGTS.DefaultResponseFormat",null);
        if (dft != Response.RESPFMT_UNDEFINED) {
            Response.RESPFMT_DEFAULT = dft;
        }
    }

    /**
    *** Gets the default response format
    **/
    public static long getDefaultResponseFormat()
    {
        // RTConfig.getString("CelltracGTS.DefaultResponseFormat",null);
        return Response.RESPFMT_DEFAULT;
    }

    // --------------------------------

    /**
    *** Returns the specified Response format mask with the format set to JSON
    **/
    public static long SetJSON(long mask)
    {
        return (mask & ~Response.RESPFMT_MASK) | Response.RESPFMT_JSON;
    }

    /**
    *** Returns the specified Response format mask with the format set to TEXT
    **/
    public static long SetTEXT(long mask)
    {
        return (mask & ~Response.RESPFMT_MASK) | Response.RESPFMT_TEXT;
    }

    /**
    *** Returns true if the Response format mask is set to JSON
    **/
    public static boolean IsJSON(long mask) 
    { 
        return ((mask & Response.RESPFMT_MASK) == Response.RESPFMT_JSON)? true : false; 
    }

    /**
    *** Returns true if the Response format mask is set to TEXT
    **/
    public static boolean IsTEXT(long mask) 
    { 
      //return ((mask & Response.RESPFMT_MASK) == Response.RESPFMT_JSON)? true : false; 
        return !Response.IsJSON(mask); // for now, is TEXT is not JSON
    }

    // --------------------------------

    /**
    *** Returns the specified Response format mask with the format set to OmitSuccess
    *** When set, the response "success" and "type" tags will not be included in the
    *** response if successful (ie. no errors).
    **/
    public static long SetOmitSuccess(long mask)
    {
        return mask | Response.RESPFMT_OMITSUCCESS;
    }

    /**
    *** Returns the specified Response format mask with the format OmitSuccess cleared
    **/
    public static long ClearOmitSuccess(long mask)
    {
        return mask & ~Response.RESPFMT_OMITSUCCESS;
    }

    /**
    *** Returns true if the Response format mask is set to TEXT
    **/
    public static boolean IsOmitSuccess(long mask) 
    { 
        return ((mask & Response.RESPFMT_OMITSUCCESS) != 0L)? true : false; 
    }

    // --------------------------------


    /**
    *** Returns the specified Response format mask with the format set to HttpError
    *** When set, the response will also return an Http error code on error.
    **/
    public static long SetHttpError(long mask)
    {
        return mask | Response.RESPFMT_HTTPERROR;
    }

    /**
    *** Returns the specified Response format mask with the format HttpError cleared
    **/
    public static long ClearHttpError(long mask)
    {
        return mask & ~Response.RESPFMT_HTTPERROR;
    }

    /**
    *** Returns true if the Response format mask is set to TEXT
    **/
    public static boolean IsHttpError(long mask) 
    { 
        return ((mask & Response.RESPFMT_HTTPERROR) != 0L)? true : false; 
    }

    // --------------------------------

    /**
    *** Encodes the Response format mask into a String value
    **/
    public static String EncodeFormat(long mask)
    {
        StringBuffer sb = new StringBuffer();
        // --
        if (Response.IsJSON(mask)) {
            sb.append("j");
        } else 
        if (Response.IsTEXT(mask)) {
            sb.append("t");
        } else {
            sb.append(""); // ?
        }
        // --
        if (Response.IsOmitSuccess(mask)) {
            sb.append("s");
        }
        // --
        if (Response.IsHttpError(mask)) {
            sb.append("e");
        }
        // --
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // -- Response errors/warnings

    /* response codes */
    public  static final String OK                  = "OK";
    public  static final String ERROR               = "ERROR";

    // OK:ack:3
    // OK:version:gprmc-1.2.3
    public  static final String RESP_OK_            = OK + ":";                 // OK
    public  static final String RESP_OK_SUCCESS     = RESP_OK_ + ":";           // Success
    public  static final String RESP_OK_VERSION     = RESP_OK_ + "version:";    // Version
    public  static final String RESP_OK_MOBILEID    = RESP_OK_ + "id:";         // MobileID
    public  static final String RESP_OK_LOGIN       = RESP_OK_ + "login:";      // Login
    public  static final String RESP_OK_UNITS       = RESP_OK_ + "units:";      // Preferred units of measurement
    public  static final String RESP_OK_AUTH        = RESP_OK_ + "auth:";       // Authorization/NewAccount/Registered
    public  static final String RESP_OK_NEWVERS     = RESP_OK_ + "newvers:";    // Latest App/GTC Versions
    public  static final String RESP_OK_NOTICE      = RESP_OK_ + "notice:";     // Notice
    public  static final String RESP_OK_EVENTS      = RESP_OK_ + "events:";     // Events
    public  static final String RESP_OK_MESSAGE     = RESP_OK_ + "message:";    // Message
    public  static final String RESP_OK_GEOCODE     = RESP_OK_ + "geocode:";    // Geocode/ReverseGeocode
    public  static final String RESP_OK_JSON        = RESP_OK_ + "json:";       // OK JSON

    // ERROR:subtype:MobileID already exists
    public  static final String RESP_ERR_           = ERROR + ":";              // ERROR
    public  static final String RESP_ERR_MOBILEID   = RESP_ERR_ + "id:";        // Invalid MobileID
    public  static final String RESP_ERR_MESSAGE    = RESP_ERR_ + "message:";   // Message not acknowledged
    public  static final String RESP_ERR_NO_GET     = RESP_ERR_ + "get:";       // GET not allowed
    public  static final String RESP_ERR_NO_POST    = RESP_ERR_ + "post:";      // POST not allowed
    public  static final String RESP_ERR_NO_RTP     = RESP_ERR_ + "rtp:";       // RTP not supported
    public  static final String RESP_ERR_NOT_AUTH   = RESP_ERR_ + "auth:";      // unauthorized
    public  static final String RESP_ERR_EXPIRED    = RESP_ERR_ + "expired:";   // expired
    public  static final String RESP_ERR_INVALID    = RESP_ERR_ + "invalid:";   // invalid (generic)
    public  static final String RESP_ERR_INTERNAL   = RESP_ERR_ + "internal:";  // server error
    public  static final String RESP_ERR_SYNTAX     = RESP_ERR_ + "syntax:";    // syntax/format error
    public  static final String RESP_ERR_COMMAND    = RESP_ERR_ + "command:";   // invalid command
    public  static final String RESP_ERR_LOGIN      = RESP_ERR_ + "login:";     // Invalid login
    public  static final String RESP_ERR_REGISTER   = RESP_ERR_ + "register:";  // Registration error
    public  static final String RESP_ERR_ACCOUNT    = RESP_ERR_ + "account:";   // Account error
    public  static final String RESP_ERR_USER       = RESP_ERR_ + "user:";      // User error
    public  static final String RESP_ERR_DEVICE     = RESP_ERR_ + "device:";    // Device error
    public  static final String RESP_ERR_DRIVER     = RESP_ERR_ + "driver:";    // Driver error
    public  static final String RESP_ERR_DEVICE_CMD = RESP_ERR_ + "devcmd:";    // Device command error
    public  static final String RESP_ERR_PARK       = RESP_ERR_ + "park:";      // Unable to park device
    public  static final String RESP_ERR_JSON       = RESP_ERR_ + "json:";      // ERROR JSON
    public  static final String RESP_ERR_NO_SUPPORT = RESP_ERR_ + "support:";   // not supported
    public  static final String RESP_ERR_GROUP      = RESP_ERR_ + "group:";     // DeviceGroup error
    public  static final String RESP_ERR_CORRIDOR   = RESP_ERR_ + "corridor:";  // GeoCorridor error
    public  static final String RESP_ERR_GEOZONE    = RESP_ERR_ + "geozone:";   // Geozone error
    public  static final String RESP_ERR_GEOCODE    = RESP_ERR_ + "geocode:";   // Geocode/ReverseGeocode error
    public  static final String RESP_ERR_FILE       = RESP_ERR_ + "file:";      // File error (eg. not found)
    public  static final String RESP_ERR_TABLE      = RESP_ERR_ + "table:";     // Table error (eg. not found)

    /* JSON tags */
    public  static final String JSON_success        = "success";
    public  static final String JSON_type           = "type";
    public  static final String JSON_message        = "message";

    // ------------------------------------------------------------------------

    /* success response */
    public  static Response VERSION(long fmt, RTProperties m)       { return CreateResponse(fmt, RESP_OK_VERSION      ,m);     } // ie. "1.2.3"
    public  static Response MOBILEID(long fmt, RTProperties m)      { return CreateResponse(fmt, RESP_OK_MOBILEID     ,m);     } // 
    public  static Response EVENTS(long fmt, RTProperties m)        { return CreateResponse(fmt, RESP_OK_EVENTS       ,m);     } // 
    public  static Response MESSAGE(long fmt, String m)             { return CreateResponse(fmt, RESP_OK_MESSAGE      ,m);     } // 
    public  static Response NOTICE(long fmt, String m)              { return CreateResponse(fmt, RESP_OK_NOTICE       ,m);     } // 
    public  static Response LOGIN(long fmt, String m)               { return CreateResponse(fmt, RESP_OK_LOGIN        ,m);     } // 
    public  static Response UNITS(long fmt, String m)               { return CreateResponse(fmt, RESP_OK_UNITS        ,m);     } // ie. "du=miles"
    public  static Response AUTH(long fmt, String m)                { return CreateResponse(fmt, RESP_OK_AUTH         ,m);     } // 
    public  static Response NEWVERS(long fmt, RTProperties v)       { return CreateResponse(fmt, RESP_OK_NEWVERS      ,v);     }
    public  static Response SUCCESS(long fmt, String m)             { return CreateResponse(fmt, RESP_OK_SUCCESS      ,m);     }
    public  static Response OK_JSON(long fmt, String j)             { return CreateResponse(fmt, RESP_OK_JSON         ,j);     }

    /* error response */
    public  static Response ERR_MOBILEID(long fmt, String m)        { return CreateResponse(fmt, RESP_ERR_MOBILEID    ,m);     }
    public  static Response ERR_NOT_AUTH(long fmt, String m)        { return CreateResponse(fmt, RESP_ERR_NOT_AUTH    ,m);     }
    public  static Response ERR_MESSAGE(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_MESSAGE     ,m);     }
    public  static Response ERR_EXPIRED(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_EXPIRED     ,m);     }
    public  static Response ERR_INVALID(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_INVALID     ,m);     }
    public  static Response ERR_INTERNAL(long fmt, String m)        { return CreateResponse(fmt, RESP_ERR_INTERNAL    ,m);     }
    public  static Response ERR_NO_GET(long fmt, String m)          { return CreateResponse(fmt, RESP_ERR_NO_GET      ,m);     }
    public  static Response ERR_NO_POST(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_NO_POST     ,m);     }
    public  static Response ERR_NO_RTP(long fmt, String m)          { return CreateResponse(fmt, RESP_ERR_NO_RTP      ,m);     }
    public  static Response ERR_SYNTAX(long fmt, String m)          { return CreateResponse(fmt, RESP_ERR_SYNTAX      ,m);     }
    public  static Response ERR_COMMAND(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_COMMAND     ,m);     }
    public  static Response ERR_LOGIN(long fmt, String m)           { return CreateResponse(fmt, RESP_ERR_LOGIN       ,m);     }
    public  static Response ERR_REGISTER(long fmt, String m)        { return CreateResponse(fmt, RESP_ERR_REGISTER    ,m);     }
    public  static Response ERR_ACCOUNT(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_ACCOUNT     ,m);     }
    public  static Response ERR_USER(long fmt, String m)            { return CreateResponse(fmt, RESP_ERR_USER        ,m);     }
    public  static Response ERR_DEVICE(long fmt, String m)          { return CreateResponse(fmt, RESP_ERR_DEVICE      ,m);     }
    public  static Response ERR_DRIVER(long fmt, String m)          { return CreateResponse(fmt, RESP_ERR_DRIVER      ,m);     }
    public  static Response ERR_DEVICE_CMD(long fmt, String m)      { return CreateResponse(fmt, RESP_ERR_DEVICE_CMD  ,m);     }
    public  static Response ERR_JSON(long fmt, String j)            { return CreateResponse(fmt, RESP_ERR_JSON        ,j);     }
    public  static Response ERR_PARK(long fmt, String m)            { return CreateResponse(fmt, RESP_ERR_PARK        ,m);     }
    public  static Response ERR_NO_SUPPORT(long fmt, String m)      { return CreateResponse(fmt, RESP_ERR_NO_SUPPORT  ,m);     }
    public  static Response ERR_GROUP(long fmt, String m)           { return CreateResponse(fmt, RESP_ERR_GROUP       ,m);     }
    public  static Response ERR_CORRIDOR(long fmt, String m)        { return CreateResponse(fmt, RESP_ERR_CORRIDOR    ,m);     }
    public  static Response ERR_GEOZONE(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_GEOZONE     ,m);     }
    public  static Response ERR_GEOCODE(long fmt, String m)         { return CreateResponse(fmt, RESP_ERR_GEOCODE     ,m);     }
    public  static Response ERR_FILE(long fmt, String m)            { return CreateResponse(fmt, RESP_ERR_FILE        ,m);     }
    public  static Response ERR_TABLE(long fmt, String m)           { return CreateResponse(fmt, RESP_ERR_TABLE       ,m);     }

    // ------------------------------------------------------------------------

    /**
    *** Creates/Returns a Response String
    *** @param fmt   The Response Format
    *** @param resp  The Response Type:SubType
    *** @param msg   The Response Message
    *** @return The Response String
    ***/
    public static Response CreateResponse(long fmt, String resp, String msg)
    {
        return new Response(fmt, resp, msg);
    }

    /**
    *** Creates/Returns a Response String
    *** @param fmt   The Response Format
    *** @param resp  The Response Type:SubType
    *** @param rtp   The RTProperties instance
    *** @return The Response String
    ***/
    public static Response CreateResponse(long fmt, String resp, RTProperties rtp)
    {
        String respMsg = (rtp != null)? rtp.toString(';') : "";
        return new Response(fmt, resp, respMsg);
    }

    // ------------------------------------------------------------------------

    /**
    *** Parse and return the Response text into a 3-element array with the following format:<br>
    ***   "Type", "SubType", "Message"
    *** @param code  The Response Code
    *** @return A 3-element String array.
    **/
    /* OBSOLETE?
    public static String[] ParseMessage(String code)
    {
        // "TYPE", "SUBTYPE", "MESSAGE"
        if (StringTools.isBlank(code)) {
            return new String[] { "", "", "" };
        } else {
            String s[] = code.split(":");
            switch (s.length) {
                case 0 : return new String[] {   "",   "",   "" };
                case 1 : return new String[] { s[0],   "",   "" };
                case 2 : return new String[] { s[0], s[1],   "" };
                case 3 : return new String[] { s[0], s[1], s[3] };
                default: return new String[] { s[0], s[1], s[3] };
            }
        }
    }
    */

    /**
    *** Extracts and returns the text message from the Response Code
    *** @param code The Response Code from which the text message is returned
    *** @return The text message, or a blank string if there is no text message.
    **/
    /* OBSOLETE?
    public static String GetMessage(String code)
    {
        if (code == null) {
            return "";
        } else {
            String s[] = code.split(":");
            return (s.length >= 3)? s[2] : "";
        }
    }
    */

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private long            respFmt     = Response.RESPFMT_UNDEFINED; // respJSON
    private String          respType    = null;
    private String          message     = null;

    private JSON._Object    respObj     = null;

    private int             httpErrCode = 0;
    
    private boolean         logJSON     = true;

    /**
    *** Constructor
    **/
    public Response(long respFmt, String respType, String message)
    {
        this.respFmt  = respFmt;
        this.respType = StringTools.trim(respType);
        this.message  = StringTools.trim(message);
    }

    /**
    *** Constructor
    **/
    public Response(JSON._Object respObj)
    {
        this.respFmt = Response.SetJSON(Response.getDefaultResponseFormat());
        this.respObj = respObj;
        if (this.respObj != null) {
            String succ = this.respObj.getStringForName(JSON_success,"");
            String type = this.respObj.getStringForName(JSON_type   ,"");
            String mess = this.respObj.getStringForName(JSON_message,"");
            this.respType = succ + ":" + type;
            this.message  = mess;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the response format
    **/
    public long getResponseFormat()
    {
        return this.respFmt;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the response type
    **/
    public String getResponseType()
    {
        return this.respType;
    }

    /**
    *** Returns true if the specified Response starts with the specified Code
    *** @param resp The Response to check
    *** @param code The Response Code
    *** @return True if the Response Code represents an error, false otherwise.
    **/
    private boolean _isResponse(String code)
    {
        String resp = this.getResponseType();
        return ((resp != null) && (code != null))?
            resp.toUpperCase().startsWith(code.toUpperCase()) :
            false;
    }

    /**
    *** Returns true if this Response represents a success
    **/
    public boolean isSuccess()
    {
        return this._isResponse(RESP_OK_);
    }

    /**
    *** Returns true if this Response represents an error
    **/
    public boolean isError()
    {
        return this._isResponse(RESP_ERR_);
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Set the Http error code
    **/
    public Response setHttpErrorCode(int code)
    {
        // -- <HttpServletResponse>.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        // -    400 Bad Request               HttpServletResponse.SC_BAD_REQUEST
        // -    401 Unauthorized              
        // -    403 Forbidden                 HttpServletResponse.SC_FORBIDDEN
        // -    404 Not Found                 HttpServletResponse.SC_NOT_FOUND
        // -    405 Method Not Allowed        HttpServletResponse.SC_METHOD_NOT_ALLOWED
        // -    406 Not Acceptable            
        // -    408 Request Timeout           
        // -    440 Login Time-out            
        // -    500 Internal Server Error     HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        // -    501 Not Implemented           
        // -    503 Service Unavailable       
        // -    509 Bandwidth Limit Exceeded  
        // -    520 Unknown Error             
        // -    524 A Timeout Occurred        
        this.httpErrCode = code;
        return this;
    }

    /**
    *** Set the Http error code
    **/
    public Response setCode(int code)
    {
        return this.setHttpErrorCode(code);
    }

    /**
    *** Returns the Http error code for this response
    **/
    public boolean hasHttpErrorCode()
    {
        int ec = this.getHttpErrorCode();
        return ((ec >= 200) && (ec <= 299))? false : true;
    }

    /**
    *** Returns the Http error code for this response
    **/
    public int getHttpErrorCode()
    {
        if (this.isSuccess()) {
            return 200; // success
        } else
        if (!Response.IsHttpError(this.getResponseFormat())) {
            return 200; // http error code not returned on error
        } else 
        if (this.httpErrCode > 0) {
            return this.httpErrCode;
        } else {
            return 400;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this response contains a message
    **/
    public boolean hasMessage()
    {
        return !StringTools.isBlank(message)? true : false;
    }

    /**
    *** Gets the response message
    **/
    public String getMessage()
    {
        return this.message;
    }

    // ------------------------------------------------------------------------

    /**
    *** Forces the response to be JSON, and initializes/returns the JSON response object.
    **/
    public JSON._Object getJsonObject()
    {
        // -- force JSON response type
        this.respFmt = Response.SetJSON(this.respFmt);
        // -- init/return JSON response object
        if (this.respObj == null) {
            this.respObj = new JSON._Object();
            // {
            //    "success" : "OK",
            //    "type"    : "type",
            //    "message" : "message"
            // }
            if (this.respType != null) {
                String s[]     = this.respType.split(":",3);
                String success = (s.length > 0)? s[0] : "";
                String type    = (s.length > 1)? s[1] : "";
                if (!Response.IsOmitSuccess(this.respFmt) || 
                    !success.equalsIgnoreCase(Response.OK)  ) { // this.isSuccess()
                    if (!StringTools.isBlank(success)) {
                        this.respObj.addKeyValue(JSON_success, StringTools.trim(success));
                    }
                    if (!StringTools.isBlank(type)) {
                        this.respObj.addKeyValue(JSON_type   , StringTools.trim(type));
                    }
                }
            }
            if (this.hasMessage()) {
                this.respObj.addKeyValue(JSON_message, StringTools.trim(this.getMessage()));
            }
        }
        return this.respObj;
    }

    // --------------------------------

    /**
    *** Adds a JSON object to the response
    **/
    public Response addJsonValue(String tag, JSON._Object jsonObj)
    {
        this.getJsonObject().addKeyValue(tag, jsonObj);
        return this;
    }

    /**
    *** Adds a JSON array to the response
    **/
    public Response addJsonValue(String tag, JSON._Array jsonArray)
    {
        this.getJsonObject().addKeyValue(tag, jsonArray);
        return this;
    }

    /**
    *** Adds a JSON String to the response
    **/
    public Response addJsonValue(String tag, String strValue)
    {
        this.getJsonObject().addKeyValue(tag, strValue);
        return this;
    }

    /**
    *** Adds a JSON KeyValue to the response
    **/
    public Response addJsonValue(JSON._KeyValue jsonKeyVal)
    {
        this.getJsonObject().addKeyValue(jsonKeyVal);
        return this;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets JSON logging for this response
    **/
    public void setLogJSON(boolean logJ) 
    {
        this.logJSON = logJ;
    }

    /**
    *** Returns true if the JSON for this response should written to the log file
    **/
    public boolean getLogJSON() 
    {
        return this.logJSON;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns response as a String representation
    **/
    public String toString()
    {
        if (Response.IsJSON(this.getResponseFormat())) {
            return this.getJsonObject().toString();
        } else {
            // -- "OK:version:MESSAGE"
            String resp = this.getResponseType();
            return this.hasMessage()? (resp + this.getMessage()) : resp;
        }
    }

    // ------------------------------------------------------------------------

}
