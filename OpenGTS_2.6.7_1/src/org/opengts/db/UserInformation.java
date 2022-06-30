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
//  Interface which includes fields common between the Account and User tables.
// ----------------------------------------------------------------------------
// Change History:
//  2008/05/14  Martin D. Flynn
//     -Initial release
//  2018/09/10  Martin D. Flynn
//     -Updated with additional fields
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.security.Principal;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;
import org.opengts.db.tables.*;

public interface UserInformation
{

    /* account info */
    public Account   getAccount();
    public String    getAccountID();
    public String    getAccountDescription();

    /* user info */
    public String    getUserID();
    public String    getUserDescription();
    public boolean   isAdminUser();
    
    /* Principal */
    public Principal getPrincipal(boolean saveUserInformation);

    /* password info */
    public String    getEncodedPassword();
    public void      setDecodedPassword(BasicPrivateLabel bpl, String enteredPass, boolean isTemp);
    public boolean   checkPassword(BasicPrivateLabel bpl, String enteredPass, boolean suspend);
    public long      getPasswdQueryTime();
    public void      setPasswdQueryTime(long v);

    /* gender */
    public int       getGender();
    //public void setGender(int gender);

    /* contact info */
    public String    getContactName();
    public void      setContactName(String v);
    public String    getContactPhone();
    public void      setContactPhone(String v);
    public String    getContactEmail();
    public void      setContactEmail(String v);

    /* timezone */
    public String    getTimeZone();
    public void      setTimeZone(String v);

    /* last login */
    public long      getLastLoginTime();
    public void      setLastLoginTime(long v);

    /* welcom time */
    //public long    getWelcomeTime();
    //public void    setWelcomeTime(long v);

    /* preferred units/formats */
    public int       getAltitudeUnits();
    public int       getSpeedUnits();
    public int       getDistanceUnits();
    public int       getTemperatureUnits();
    public int       getLatLonFormat();

    /* authorized device groups */
    public java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode) throws DBException;
    public java.util.List<String> getAllAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode) throws DBException;
    public java.util.List<DeviceGroupProvider> getAllAuthorizedDeviceGroups(DBReadWriteMode rwMode) throws DBException;

    /* authorized devices */
    public java.util.List<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode) throws DBException;
    public java.util.List<DeviceProvider> getAuthorizedDevices(DBReadWriteMode rwMode) throws DBException;

}
