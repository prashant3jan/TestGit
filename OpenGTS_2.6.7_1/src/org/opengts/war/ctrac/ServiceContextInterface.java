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
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opengts.util.*;
import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;
import org.opengts.db.tables.Device;

public interface ServiceContextInterface
{

    // ------------------------------------------------------------------------

    /**
    *** Sets the RequestMethod (GET, POST, PUT, DELETE)
    **/
    public void setRequestMethod(HTMLTools.RequestMethod reqMeth);

    /**
    *** Sets the RequestMethod ("GET", "POST", "PUT", "DELETE")
    **/
    public void setRequestMethod(String reqMeth);

    // --------------------------------

    /**
    *** Sets the HttpServletRequest
    **/
    public void setHttpServletRequest(HttpServletRequest req);

    /**
    *** Gets the HttpServletRequest
    **/
    public HttpServletRequest getHttpServletRequest();

    // --------------------------------

    /**
    *** Sets the HttpServletResponse
    **/
    public void setHttpServletResponse(HttpServletResponse res);

    /**
    *** Gets the HttpServletResponse
    **/
    public HttpServletResponse getHttpServletResponse();

    // ------------------------------------------------------------------------

    /**
    *** Sets the response format as a String value
    **/
    public void setResponseFormat(String respFormat);

    ///**
    //*** Sets the response format
    //**/
    //public void setResponseFormat(long respFormat);

    /**
    *** Gets the response format mask
    **/
    public long getResponseFormat();

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the AuthorizedUser is actually authorized
    **/
    public boolean isAuthorized();

    /**
    *** Gets the AuthorizedUser
    **/
    public AuthorizedUser getAuthorizedUser();

    // --------------------------------

    /**
    *** Gets the AccountID from the AuthorizedUser
    **/
    public String getAccountID();

    /**
    *** Gets the Account from the AuthorizedUser
    **/
    public Account getAccount();

    // --------------------------------

    /**
    *** Gets the UserID from the AuthorizedUser
    **/
    public String getUserID();

    /**
    *** Gets the User from the AuthorizedUser
    **/
    public User getUser();

    // ------------------------------------------------------------------------

    /**
    *** Gets the DeviceID
    */
    public String getDeviceID();

    /**
    *** Gets the Device
    **/
    public Device getDevice(boolean authRequired)
        throws ResponseException;

    /**
    *** Gets the Mobile/Modem ID
    */
    public String getMobileID();

    /**
    *** Tracking: Gets the Device-Code
    */
    public String getDeviceCode();

    /**
    *** Tracking: Gets the Unique ID Prefix
    */
    public String[] getUniqueIdPrefix();

    /**
    *** Tracking: Gets the first DCS unique-id prefix array for this instance
    **/
    public String firstUniqueIdPrefix();

    // ------------------------------------------------------------------------

    /**
    *** Gets the internal RTProperties instance
    **/
    public RTProperties getRTProperties();

    // --------------------------------

    /**
    *** Gets a String from the RTProperties 
    **/
    public String getString(String key, String dft);

    /**
    *** Sets a String in the RTProperties 
    **/
    public void setString(String key, String val);

    // --------------------------------

    /**
    *** Gets an Object property from the RTProperties 
    **/
    public Object getProperty(String key, Object dft);

    /**
    *** Sets an Object property in the RTProperties 
    **/
    public void setProperty(String key, Object val);

    // ------------------------------------------------------------------------

    /**
    *** Sets the current BasicPrivateLabel instance
    **/
    public void setPrivateLabel(BasicPrivateLabel bpl);

    /**
    *** Gets the current BasicPrivateLabel instance
    **/
    public BasicPrivateLabel getPrivateLabel();

    // ------------------------------------------------------------------------

    /**
    *** Sets the current RequestProperties instance.
    *** Only used withing the "track.war" environment.
    **/
    public void setRequestProperties(Object reqState);

    /**
    *** Gets the current RequestProperties instance.
    *** Only available withing the "track.war" environment.
    *** Must be cast to "(RequestProperties)".
    **/
    public Object getRequestProperties();

    // ------------------------------------------------------------------------

    /**
    *** Command handling
    **/
    public Response execCommand(String command, String commandArg);

    // ------------------------------------------------------------------------

    /**
    *** Forwards to Tracking support
    **/
    public Response track(RTProperties rtpArg, boolean rtpOnly)
        throws ServletException, IOException;

    /**
    *** Forwards to Tracking support
    **/
    public Response track(String dataStr);

}
