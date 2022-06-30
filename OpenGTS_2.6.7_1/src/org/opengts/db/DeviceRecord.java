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
//  2008/05/14  Martin D. Flynn
//     -Initial release
//  2020/02/19  GTS Development Team
//     -Renamed "getDeviceVIN()" to "getDeviceVID()"
//     -Added additional options for return values to "getDeviceVID()"
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONBeanGetter;

import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.Device;

public class DeviceRecord<RT extends DBRecord<RT>>
    extends AccountRecord<RT>
    implements JSON.JSONBean
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* common Asset/Device field definition */
    public static final String FLD_deviceID = "deviceID";

    /* create a new "deviceID" key field definition */
    public static DBField newField_deviceID(boolean priKey)
    {
        return DeviceRecord.newField_deviceID(priKey, null, (I18N.Text)null);
    }

    /* create a new "deviceID" key field definition */
    public static DBField newField_deviceID(boolean priKey, String xAttr)
    {
        return DeviceRecord.newField_deviceID(priKey, xAttr, (I18N.Text)null);
    }

    /* create a new "deviceID" key field definition */
    public static DBField newField_deviceID(boolean priKey, String xAttr, I18N.Text title)
    {
        if (title == null) { 
            title = I18N.getString(DeviceRecord.class,"DeviceRecord.fld.deviceID","Device/Asset ID"); 
        }
        String attr = (priKey?"key=true":"edit=2") + (StringTools.isBlank(xAttr)?"":(" "+xAttr));
        return new DBField(FLD_deviceID, String.class, DBField.TYPE_DEV_ID(), title, attr);
    }

    /* create a new "deviceID" key field definition */
    public static DBField newField_deviceID(boolean priKey, String xAttr, String title)
    {
        if (StringTools.isBlank(title)) {
            return DeviceRecord.newField_deviceID(priKey, xAttr, (I18N.Text)null);
        } else {
            String attr = (priKey?"key=true":"edit=2") + (StringTools.isBlank(xAttr)?"":(" "+xAttr));
            return new DBField(FLD_deviceID, String.class, DBField.TYPE_DEV_ID(), title, attr);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static abstract class DeviceKey<RT extends DBRecord<RT>>
        extends AccountKey<RT>
    {
        public DeviceKey() {
            super();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* Bean instance */
    public DeviceRecord()
    {
        super();
    }

    /* database record */
    public DeviceRecord(DeviceKey<RT> key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the isDeleted state of this record.
    **/
    @Override
    public boolean isDeleted()
    {

        /* this AccountRecord inactive? */
        if (super.isDeleted()) {
            // -- deleted
            return true;
        }

        /* owner Account inactive? */
        Account account = this.getAccount();
        if (account == null) {
            // -- no account? assumed deleted
            return true;
        } else {
            // -- owning account deleted?
            return account.isDeleted();
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the isActive state of this record.
    *** Also, returns false if this record has been deleted.
    **/
    @Override
    public boolean isActive()
    {

        /* this AccountRecord inactive? */
        if (!super.isActive()) {
            // -- deleted or !active
            return false;
        }

        /* owner Account inactive? */
        Account account = this.getAccount();
        if (account == null) {
            // -- no account? not active
            return false;
        } else {
            // -- owning account active?
            return account.isActive();
        }

    }

    // ------------------------------------------------------------------------

    /* Device ID */
    @JSONBeanGetter()
    public final String getDeviceID()
    {
        String v = (String)this.getKeyValue(FLD_deviceID); // getFieldValue
        return (v != null)? v : "";
    }
    
    public /*final*/ void setDeviceID(String v)
    {
        this.setKeyValue(FLD_deviceID, ((v != null)? v : "")); // setFieldValue
    }

    // ------------------------------------------------------------------------
    // The following is an optimization for holding the Account record while
    // processing this DeviceRecord.  Use with caution.

    private Device device = null;

    public final boolean hasDevice()
    {
        return (this.device != null);
    }

    /* get the device for this event */
    private static int DebugPrintOptimize = 0;
    public final Device getDevice()
    {
        if (this.device == null) {
            String deviceID = this.getDeviceID();
            Print.logInfo("[Optimize] Retrieving Device record: " + this.getAccountID() + "/" + deviceID);
            //Print.logStackTrace("[Optimize] Reading Device");
            //if((DebugPrintOptimize++%50)==0){Print.logStackTrace("[Optimize] Reading Device");}
            try {
                this.device = Device.getDevice(this.getAccount(), deviceID); // null if non-existent
                if (this.device == null) {
                    // 'this.device' may still be null if the asset was not found
                    Print.logError("Device not found: " + this.getAccountID() + "/" + deviceID);
                }
            } catch (DBException dbe) {
                // may be caused by "java.net.ConnectException: Connection refused: connect"
                Print.logError("Device not found: " + this.getAccountID() + "/" + deviceID);
                this.device = null;
            }
        }
        return this.device;
    }

    /* set the device for this record */
    public final void setDevice(Device dev) 
    {
        if ((Object)this instanceof Device) {
            if (this != dev) {
                Print.logError("'this' is already a Device: " + this.getAccountID() + "/" + this.getDeviceID());
            }
        } else
        if (dev == null) {
            //Print.logStackTrace("*** Setting device to null ...");
            this.device = null;
        } else
        if (!this.getAccountID().equals(dev.getAccountID()) ||
            !this.getDeviceID().equals(dev.getDeviceID()  )   ) {
            Print.logError("Account/Device IDs do not match: " + this.getAccountID() + "/" + this.getDeviceID());
            this.device = null;
        } else {
            this.setAccount(dev.getAccount());
            this.device = dev;
        }
    }

    // ------------------------------------------------------------------------

    private String  deviceDesc = null;

    /**
    *** Return the description for this DBRecord's Device
    *** @return The Device description
    **/
    public /*final*/ String getDeviceDescription() // "final" removed [2.6.6-B68g]
    {
        if (this.deviceDesc == null) {
            Device dev = this.getDevice();
            if (dev != null) {
                this.deviceDesc = dev.getDescription();
                if (StringTools.isBlank(this.deviceDesc)) {
                    // -- description is blank, set device-id
                    this.deviceDesc = dev.getDeviceID();
                }
            } else {
                // -- should not occur
                this.deviceDesc = this.getDeviceID();
            }
        } 
        return this.deviceDesc;
    }

    // ------------------------------------------------------------------------

    private String  deviceVIN  = null;
    
    public static       boolean DEVICE_VID_DEBUG            = false; //true;
    public static final String  DEVICE_VID_DeviceID[]       = { "DeviceID"    , "device"     , "id"     };
    public static final String  DEVICE_VID_VehicleID[]      = { "VehicleID"   , "vehicle"    , "vin"    };
    public static final String  DEVICE_VID_ShortName[]      = { "ShortName"   , "short"      , "name"   };
    public static final String  DEVICE_VID_LicensePlate[]   = { "LicensePlate", "NumberPlate", "plate"  };

    /**
    *** Gets the type of short-name used for label pushpins
    **/
    public String getLabelPushpinNameType()
    {
        // --- PROP_DeviceRecord_vehicleIDNameType?
        return RTConfig.getString(DBConfig.PROP_DeviceRecord_labelPushpinNameType, DEVICE_VID_ShortName[0]);
    }

    /**
    *** Return the short Vehicle-ID for this DBRecord's Device.
    *** This value is usually displayed on the maps as the Vehicle-ID
    *** @return The Device Vehicle-ID
    **/
    public String getLabelPushpinName()
    {
        String vidType = this.getLabelPushpinNameType();
        return this.getDeviceVID(vidType);
    }

    // --------------------------------

    /**
    *** Return the short Vehicle-ID for this DBRecord's Device.
    *** This value is usually displayed on the maps as the Vehicle-ID
    *** @return The Device Vehicle-ID
    **/
    public /*final*/ String getDeviceVID() // added return type options [2.6.7-B12f]
    {
        String vidType = this.getLabelPushpinNameType();
        return this.getDeviceVID(vidType);
    }

    /**
    *** Return the short Vehicle-ID for this DBRecord's Device.
    *** This value is usually displayed on the maps as the Vehicle-ID
    *** @return The Device Vehicle-ID
    **/
    public String getDeviceVID(String vidType) // added return type options [2.6.7-B12f]
    {
        // -- Returned value options:
        // -    "DeviceID"      : dev.getDeviceID()
        // -    "VehicleID"     : dev.getVehicleID()     /* VIN */
        // -    "ShortName"     : dev.getShortName()     /* same as getDisplayName() */
        // -    "LicensePlate"  : dev.getLicensePlate()  
        if (this.deviceVIN == null) {
            Device dev = this.getDevice();
            if (dev != null) {
                if (ListTools.containsIgnoreCase(DEVICE_VID_DeviceID, vidType)) {
                    // -- DeviceID
                    this.deviceVIN = dev.getDeviceID();
                    if (DEVICE_VID_DEBUG) {
                        Print.logInfo("[ID] Device="+dev.getDeviceID()+" VID(ID)="+this.deviceVIN);
                    }
                } else
                if (ListTools.containsIgnoreCase(DEVICE_VID_VehicleID, vidType)) {
                    // -- VIN
                    this.deviceVIN = dev.getVehicleID();
                    if (DEVICE_VID_DEBUG && !StringTools.isBlank(this.deviceVIN)) {
                        Print.logInfo("[VIN] Device="+dev.getDeviceID()+" VID(ID)="+this.deviceVIN);
                    }
                } else
                if (ListTools.containsIgnoreCase(DEVICE_VID_ShortName, vidType)) {
                    // -- ShortName
                    this.deviceVIN = dev.getShortName();
                    if (DEVICE_VID_DEBUG && !StringTools.isBlank(this.deviceVIN)) {
                        Print.logInfo("[Short] Device="+dev.getDeviceID()+" VID(ID)="+this.deviceVIN);
                    }
                } else
                if (ListTools.containsIgnoreCase(DEVICE_VID_LicensePlate, vidType)) {
                    // -- LicensePlate
                    this.deviceVIN = dev.getLicensePlate();
                    if (DEVICE_VID_DEBUG && !StringTools.isBlank(this.deviceVIN)) {
                        Print.logInfo("[License] Device="+dev.getDeviceID()+" VID(License)="+this.deviceVIN);
                    }
                }
                // -- default to deviceID
                if (StringTools.isBlank(this.deviceVIN)) {
                    this.deviceVIN = dev.getDeviceID();
                    if (DEVICE_VID_DEBUG && !StringTools.isBlank(this.deviceVIN)) {
                        Print.logInfo("[Default] Device="+dev.getDeviceID()+" VID(Default)="+this.deviceVIN);
                    }
                }
            } else {
                // -- should not occur
                this.deviceVIN = this.getDeviceID();
                if (DEVICE_VID_DEBUG) {
                    Print.logInfo("[Default] Device="+dev.getDeviceID()+" VID(ThisDefault)="+this.deviceVIN);
                }
            }
        } 
        return this.deviceVIN;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final String ARG_IS_MEMBER[]     = new String[] { "isMember"     };
    private static final String ARG_LIST_MEMBERS[]  = new String[] { "listMembers"  };

    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main

        /* is an AccountRecord table? */
        if (RTConfig.hasProperty(ARG_IS_MEMBER)) {
            String utableName = RTConfig.getString(ARG_IS_MEMBER,null);
            boolean isMember = DBFactory.isTableClass(utableName, DeviceRecord.class);
            Print.sysPrintln("isDeviceRecord("+utableName+") == " + isMember);
            System.exit(0);
        }

        /* list members */
        if (RTConfig.hasProperty(ARG_LIST_MEMBERS)) {
            Print.sysPrintln("DeviceRecord tables:");
            DBFactory<? extends DBRecord<?>> facts[] = DBAdmin.getClassTableFactories(DeviceRecord.class);
            for (DBFactory<? extends DBRecord<?>> tableFact : facts) {
                Print.sysPrintln("  "+tableFact.getUntranslatedTableName());
            }
            System.exit(0);
        }

    }

}
