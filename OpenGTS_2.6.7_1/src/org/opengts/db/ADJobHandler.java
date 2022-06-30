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
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.util.*;
import java.io.*;

import org.opengts.util.*;
import org.opengts.dbtypes.*;
import org.opengts.dbtools.*;

import org.opengts.db.*;
import org.opengts.db.tables.*;

public abstract class ADJobHandler
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- This implementation of ADJobHandle must not maintain any state.  If you
    // -  you need an implementaion of ADJobHandle that does maintain state, then
    // -  a subclass of ADJobHandler will be needed that maintains the state that
    // -  you need.

    /**
    *** Constructor
    **/
    public ADJobHandler()
    {
        super();
    }

    // ------------------------------------------------------------------------

    /**
    *** Run job
    **/
    public abstract void runJob(Map<String,Object> props);

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the parent ADJobQueue instance from the specified properties
    **/
    public ADJobQueue getADJobQueue(Map<String,Object> props)
    {
        if (props != null) {
            Object adJobQ = props.get(ADJobQueue.FLD_ADJobQueue); // may be null
            if (adJobQ instanceof ADJobQueue) {
                return (ADJobQueue)adJobQ;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the AccountID from the specified properties
    **/
    public String getAccountID(Map<String,Object> props)
    {
        return this.getAccountID(props, null);
    }

    /**
    *** Gets the AccountID from the specified properties
    **/
    public String getAccountID(Map<String,Object> props, String dft)
    {
        if (props != null) {
            // -- FLD_AccountID
            Object acctID = props.get(ADJobQueue.FLD_AccountID); // may be null
            if ((acctID instanceof String) && !StringTools.isBlank(acctID)) {
                return (String)acctID;
            }
            // -- FLD_Account
            Object acct = props.get(ADJobQueue.FLD_Account); // may be null
            if (acct instanceof Account) {
                return ((Account)acct).getAccountID();
            }
        }
        return dft;
    }

    // --------------------------------

    /**
    *** Gets the Account from the specified properties
    **/
    public Account getAccount(Map<String,Object> props)
    {
        if (props != null) {
            // -- FLD_Account
            Object acct = props.get(ADJobQueue.FLD_Account); // may be null
            if (acct instanceof Account) {
                return (Account)acct;
            }
            // -- FLD_AccountID
            ADJobQueue adJobQ = this.getADJobQueue(props); // may be null
            if (adJobQ != null) {
                Object acctID = props.get(ADJobQueue.FLD_AccountID); // may be null
                if ((acctID instanceof String) && !StringTools.isBlank(acctID)) {
                    try {
                        Account account = adJobQ.getAccount((String)acctID);
                        if (account != null) {
                            props.put(ADJobQueue.FLD_Account, account);
                        }
                        return account; // may be null
                    } catch (ADJobQueue.ADJobException aje) {
                        Print.logError("Error reading Account: " + aje);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the DeviceID from the specified properties
    **/
    public String getDeviceID(Map<String,Object> props)
    {
        return this.getDeviceID(props, null);
    }

    /**
    *** Gets the DeviceID from the specified properties
    **/
    public String getDeviceID(Map<String,Object> props, String dft)
    {
        if (props != null) {
            // -- FLD_DeviceID
            Object devID = props.get(ADJobQueue.FLD_DeviceID); // may be null
            if ((devID instanceof String) && !StringTools.isBlank(devID)) {
                return (String)devID;
            }
            // -- FLD_Device
            Object dev = props.get(ADJobQueue.FLD_Device); // may be null
            if (dev instanceof Device) {
                return ((Device)dev).getDeviceID();
            }
        }
        return dft;
    }

    // --------------------------------

    /**
    *** Gets the Device from the specified properties
    **/
    public Device getDevice(Map<String,Object> props)
    {
        if (props != null) {
            // -- FLD_Device
            Object dev = props.get(ADJobQueue.FLD_Device); // may be null
            if (dev instanceof Device) {
                return (Device)dev;
            }
            // -- FLD_DeviceID
            ADJobQueue adJobQ = this.getADJobQueue(props); // may be null
            if (adJobQ != null) {
                Object devID = props.get(ADJobQueue.FLD_DeviceID); // may be null
                if ((devID instanceof String) && !StringTools.isBlank(devID)) {
                    try {
                        Account account = this.getAccount(props);
                        Device  device  = adJobQ.getDevice(account, (String)devID);
                        if (device != null) {
                            props.put(ADJobQueue.FLD_Device, device);
                        }
                        return device; // may be null
                    } catch (ADJobQueue.ADJobException aje) {
                        Print.logError("Error reading Device: " + aje);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Job Data from the specified properties
    **/
    public Object getJobData(Map<String,Object> props)
    {
        return this.getJobData(props, null);
    }

    /**
    *** Gets the Job Data from the specified properties
    **/
    public Object getJobData(Map<String,Object> props, Object dft)
    {
        if (props != null) {
            Object jdata = props.get(ADJobQueue.FLD_JobData); // may be null
            if (jdata != null) {
                return jdata;
            }
        }
        return dft;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
