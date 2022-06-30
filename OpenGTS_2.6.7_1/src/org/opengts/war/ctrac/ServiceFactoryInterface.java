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

import javax.servlet.http.HttpServletRequest;

import org.opengts.util.*;
import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;

public interface ServiceFactoryInterface
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the ServiceFactory version
    **/
    public String getVersion();

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /** 
    *** Sets the DCServerConfig instance
    **/
    public void setDCServerConfig(DCServerConfig dcs);

    /**
    *** Sets the RTProperties instance
    **/
    public void setRTProperties(RTProperties rtp);

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Creates a new ServiceContext from specified account/user.
    */
    public ServiceContextInterface newServiceContext(
        Account account, User user);

    /**
    *** Creates a new ServiceContext from specified account/user.
    *** If authorized, context wiull be save to the current session.
    */
    public ServiceContextInterface newServiceContext(
        String accountID, String userID, String password, 
        HttpServletRequest request);

    /**
    *** Creates a new ServiceContext from specified account/user.
    */
    public ServiceContextInterface newServiceContext(
        String accountID, String userID, String password);

    /**
    *** Creates a new ServiceContext from specified session-id.
    */
    public ServiceContextInterface newServiceContext(
        HttpServletRequest request, String jSessionID);

    /**
    *** Creates a new ServiceContext from the current session only.
    */
    public ServiceContextInterface newServiceContext(
        HttpServletRequest request);

    // ------------------------------------------------------------------------

    /**
    *** Gets a property value
    **/
    public Object getProperty(String key, Object dft);

    /**
    *** Gets a property value
    **/
    public String getString(String key, String dft);

    /**
    *** Gets a property value
    **/
    public String[] getStringArray(String key, String dft[]);

    /**
    *** Gets a property value
    **/
    public double getDouble(String key, double dft);

    /**
    *** Gets a property value
    **/
    public long getLong(String key, long dft);

    /**
    *** Gets a property value
    **/
    public int getInt(String key, int dft);

    /**
    *** Gets a property value
    **/
    public boolean getBoolean(String key, boolean dft);

    // ------------------------------------------------------------------------

    /**
    *** Print Tracking header log 
    **/
    public void printTrackingHeaderLog(int P, int W);

    /**
    *** Print Tracking header log 
    **/
    public void printCommandsHeaderLog(int P, int W);

    // ------------------------------------------------------------------------

}
