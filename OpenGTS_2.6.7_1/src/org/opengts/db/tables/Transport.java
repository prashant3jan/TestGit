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
// Usa cases:
// 1) Multiple physical devices per Device record entry.
//    Each Transport entry lookup will point to an assigned Account/Device entry
//    - Transports are owned by the assigned Device
//    - TransportID is unique within the assigned device
//    - Deleting a Device deletes the associated transport
// 2) Physical devices are rented to Accounts
//    Each Transport entry lookup will point to an assigned Account/Device entry
//    - Transports are owned by the SysAdmin/Manager Account
//    - TransportID should be unique within the System
//    - Deleting an Account/Device does not delete the target transport
// ----------------------------------------------------------------------------
// Change History:
//  2008/05/14  Martin D. Flynn
//     -Initial release
//  2008/10/16  Martin D. Flynn
//     -Added FLD_lastPingTime, FLD_totalPingCount
//  2009/09/23  Martin D. Flynn
//     -Added FLD_maxPingCount
//  2009/11/01  Martin D. Flynn
//     -Added FLD_expectAck, FLD_lastAckCommand, FLD_lastAckTime
//  2014/09/16  Martin D. Flynn
//     -Limit upper value for "totalPingCount"/"maxPingCount" to 0xFFFF [v2.5.7-B11]
//     -Fixed "setTotalPingCount" to set "this.assocDevice._setTotalPingCount"
//  2020/02/19  GTS Development Team
//     -Changed usage to support multiple physical devices per Device entry.
// ----------------------------------------------------------------------------
package org.opengts.db.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;
import org.opengts.db.*;

/**
*** This class represents a single data transport for a tracking/telematic hardware device.
*** In the case where a single hardware tracking/telematic device supports multiple data 
*** transports (such as GPRS and Satellite), a hardware device would have more than one
*** 'Transport' instance.
**/

public class Transport
    //extends AccountRecord<Transport>
    extends DeviceRecord<Transport>
    implements DataTransport
{

    // ------------------------------------------------------------------------

    /* parent table dependencies */
    // -- If "true", corresponding Transport records will (should) be deleted 
    // -  whenever the associated account/device entry is deleted.
    // -  If "false", Transport records are not deleted when Account/Device 
    // -  entries are deleted.
    public static final boolean PARENT_TABLES_DEPENDENCIES  = false;

    // ------------------------------------------------------------------------

    private static int TransportQueryEnabled = -1; // -1 to use default

    /**
    *** Returns true if Transport Query is enabled, false otherwise.
    **/
    public static boolean isTransportQueryEnabled()
    {
        if (TransportQueryEnabled > 0) {
            // -- 'TransportQueryEnabled' was explicitly set true
            return true;
        } else
        if (TransportQueryEnabled == 0) {
            // -- 'TransportQueryEnabled' was explicitly set false
            return false;
        } else {
            // -- return default property setting
            return RTConfig.getBoolean(DBConfig.PROP_Transport_queryEnabled);
        }
    }

    /**
    *** Sets Transport Query enabled/disabled
    **/
    public static void setTransportQueryEnabled(boolean enable)
    {
        TransportQueryEnabled = enable? 1 : 0; // explicit value
    }

    // --------------------------------

    // set to true when it is desireable to create a default Transport from the Device record
    public static boolean allowCreateDefaultTransport()
    {
        return false;
    }

    // ------------------------------------------------------------------------
    // OpenDMTP Protocol Definition v0.1.0 Conformance:
    // These encoding value constants are defined by the OpenDMTP protocol specification
    // and must remain as specified here.  For more information, see the following source
    // file in the OpenDMTP-Server project: "src/org/opendmtp/codes/Encoding.java"

    public  static final int    SUPPORTED_ENCODING_BINARY               = 0x01;
    public  static final int    SUPPORTED_ENCODING_BASE64               = 0x02;
    public  static final int    SUPPORTED_ENCODING_HEX                  = 0x04;
    public  static final int    SUPPORTED_ENCODING_CSV                  = 0x08;

    public  static final String DEFAULT_XPORT_NAME                      = "New Data Transport";
    public  static final int    DEFAULT_ENCODING = 
        Transport.SUPPORTED_ENCODING_BINARY | 
        Transport.SUPPORTED_ENCODING_BASE64 | 
        Transport.SUPPORTED_ENCODING_HEX;
    public  static final int    DEFAULT_UNIT_LIMIT_INTERVAL_MIN         =  0; // 30] (minutes)
    public  static final int    DEFAULT_MAX_ALLOWED_EVENTS              =  0; // 30] '0' == unlimited
    public  static final int    DEFAULT_TOTAL_MAX_CONNECTIONS           =  0; // 30] '0' == unlimited
    public  static final int    DEFAULT_TOTAL_MAX_CONNECTIONS_PER_MIN   =  0; //  2] '0' == unlimited
    public  static final int    DEFAULT_DUPLEX_MAX_CONNECTIONS          =  0; // 30] '0' == unlimited
    public  static final int    DEFAULT_DUPLEX_MAX_CONNECTIONS_PER_MIN  =  0; //  2]s '0' == unlimited

    // ------------------------------------------------------------------------
    // -- OpenDMTP Protocol Definition v0.1.1 Conformance:
    // -  These property value constants are defined by the OpenDMTP protocol specification
    // -  and must remain as specified here.  For more information, see the following source
    // -  file in the OpenDMTP-J2ME-Client project: "org/opendmtp/j2me/codes/DMTPProps.java"
    // ]OBSOLETE]
    //public  static final int    PROP_COMM_MAX_CONNECTIONS               = 0xF311;           // PropertyKey.PROP_COMM_MAX_CONNECTIONS
    //public  static final String PROP_COMM_MAX_CONNECTIONS_STR           = "com.maxconn";    // PropertyKey.PROP_COMM_MAX_CONNECTIONS
    //
    //public  static final int    PROP_COMM_MIN_XMIT_DELAY                = 0xF312;           // PropertyKey.PROP_COMM_MIN_XMIT_DELAY
    //public  static final String PROP_COMM_MIN_XMIT_DELAY_STR            = "com.mindelay";   // PropertyKey.PROP_COMM_MIN_XMIT_DELAY
    //
    //public  static final int    PROP_COMM_MIN_XMIT_RATE                 = 0xF313;           // PropertyKey.PROP_COMM_MIN_XMIT_RATE
    //public  static final String PROP_COMM_MIN_XMIT_RATE_STR             = "com.minrate";    // PropertyKey.PROP_COMM_MIN_XMIT_RATE
    //
    //public  static final int    PROP_COMM_MAX_XMIT_RATE                 = 0xF315;           // PropertyKey.PROP_COMM_MAX_XMIT_RATE
    //public  static final String PROP_COMM_MAX_XMIT_RATE_STR             = "com.maxrate";    // PropertyKey.PROP_COMM_MAX_XMIT_RATE
    //
    //public  static final int    PROP_COMM_MAX_DUP_EVENTS                = 0xF317;           // PropertyKey.PROP_COMM_MAX_DUP_EVENTS
    //public  static final String PROP_COMM_MAX_DUP_EVENTS_STR            = "com.maxduplex";  // PropertyKey.PROP_COMM_MAX_DUP_EVENTS
    //
    //public  static final int    PROP_COMM_MAX_SIM_EVENTS                = 0xF318;           // PropertyKey.PROP_COMM_MAX_SIM_EVENTS
    //public  static final String PROP_COMM_MAX_SIM_EVENTS_STR            = "com.maxsimplex"; // PropertyKey.PROP_COMM_MAX_SIM_EVENTS

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // DMTP Encodings
    
    public enum Encodings implements EnumTools.BitMask, EnumTools.StringLocale {
        NONE        (0L,I18N.getString(Transport.class,"Transport.encoding.none"  ,"None")),
        BINARY      (1L,I18N.getString(Transport.class,"Transport.encoding.binary","Binary")),
        BASE64      (2L,I18N.getString(Transport.class,"Transport.encoding.base64","Base64")),
        HEX         (4L,I18N.getString(Transport.class,"Transport.encoding.hex"   ,"Hex"   ));
        // ---
        private long        vv = 0;
        private I18N.Text   aa = null;
        Encodings(long v, I18N.Text a)              { vv=v; aa=a; }
        public long    getLongValue()               { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME               = "Transport";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_transportID           = "transportID";           // 
    // -- Target AccountID/DeviceID (assocAccountID/assocDeviceID)
    public static final String FLD_targetAccountID       = "targetAccountID";       // target AccountID (may be blank)
    public static final String FLD_targetDeviceID        = "targetDeviceID";        // target DeviceID (may be blank)
    // -- DataTransport fields
    public static final String FLD_uniqueID              = "uniqueID";              // unique ID
    public static final String FLD_deviceCode            = "deviceCode";            // manufacturer, etc (config)
    public static final String FLD_deviceType            = "deviceType";            // reserved
    public static final String FLD_serialNumber          = "serialNumber";          // device hardware serial#.
    public static final String FLD_simPhoneNumber        = "simPhoneNumber";        // SIM phone number
    public static final String FLD_simID                 = "simID";                 // SIM ID (ICCID)
    public static final String FLD_smsEmail              = "smsEmail";              // SMS email address
  //public static final String FLD_smsGatewayProps       = "smsGatewayProps";       // SMS gateway properties
    public static final String FLD_imeiNumber            = "imeiNumber";            // IMEI number
    public static final String FLD_lastInputState        = "lastInputState";        // last known digital input state
    public static final String FLD_lastOutputState       = "lastOutputState";       // last known digital output state
    public static final String FLD_ignitionIndex         = "ignitionIndex";         // hardware ignition I/O index
    public static final String FLD_codeVersion           = "codeVersion";           // code version installed on device
    public static final String FLD_featureSet            = "featureSet";            // device features
    public static final String FLD_ipAddressValid        = "ipAddressValid";        // valid IP address block
    // -- Ping/Command
    public static final String FLD_pendingPingCommand    = "pendingPingCommand";    // pending ping command
    public static final String FLD_lastPingTime          = "lastPingTime";          // last ping time
    public static final String FLD_totalPingCount        = "totalPingCount";        // total ping count
    public static final String FLD_maxPingCount          = "maxPingCount";          // maximum ping count
    public static final String FLD_expectAck             = "expectAck";             // expecting a returned ACK
    public static final String FLD_expectAckCode         = "expectAckCode";         // expected ACK status code
    public static final String FLD_lastAckCommand        = "lastAckCommand";        // last command expecting an ACK
    public static final String FLD_lastAckTime           = "lastAckTime";           // last received ACK time
    // -- Last Event
    public static final String FLD_ipAddressCurrent      = "ipAddressCurrent";      // current(last) IP address
    public static final String FLD_remotePortCurrent     = "remotePortCurrent";     // current(last) remote port
    public static final String FLD_listenPortCurrent     = "listenPortCurrent";     // current(last) listen port
    public static final String FLD_lastTotalConnectTime  = "lastTotalConnectTime";  // last connect time
    public static final String FLD_lastDuplexConnectTime = "lastDuplexConnectTime"; // last TCP connect time
    // -- TODO: Device Communication Server Configuration
    public static final String FLD_dcsPropertiesID       = "dcsPropertiesID";       // DCS property group name
    public static final String FLD_dcsConfigMask         = "dcsConfigMask";         // DCS Config Mask
    public static final String FLD_dcsConfigString       = "dcsConfigString";       // DCS Config String
    public static final String FLD_dcsCommandHost        = "dcsCommandHost";        // DCS Command host name
    public static final String FLD_dcsCommandState       = "dcsCommandState";       // DCS Command State
    // --
    private static DBField FieldInfo[] = {
        // -- Transport fields
        newField_accountID(true),   // "key=true"
        newField_deviceID(true),    // "key=true"
        new DBField(FLD_transportID          , String.class        , DBField.TYPE_XPORT_ID()  , "Transport ID"                  , "key=true"), // altkey=transport
        // -- Target AccountID/DeviceID (assocAccountID/assocDeviceID)
        new DBField(FLD_targetAccountID      , String.class        , DBField.TYPE_ACCT_ID()   , "Target AccountID"              , "edit=2"),
        new DBField(FLD_targetDeviceID       , String.class        , DBField.TYPE_DEV_ID()    , "Target DeviceID"               , "edit=2"),
        // -- DataTransport fields
        new DBField(FLD_uniqueID             , String.class        , DBField.TYPE_UNIQ_ID()   , "Unique ID"                     , "edit=2 altkey=uniqueID"),
        new DBField(FLD_deviceCode           , String.class        , DBField.TYPE_STRING(24)  , "Device Code"                   , "edit=2"),
        new DBField(FLD_deviceType           , String.class        , DBField.TYPE_STRING(24)  , "Device Type"                   , "edit=2"),
        new DBField(FLD_serialNumber         , String.class        , DBField.TYPE_STRING(24)  , "Serial Number"                 , "edit=2"),
        new DBField(FLD_simPhoneNumber       , String.class        , DBField.TYPE_STRING(24)  , "SIM Phone Number"              , "edit=2"),
        new DBField(FLD_simID                , String.class        , DBField.TYPE_STRING(24)  , "SIM ID"                        , "edit=2"),
        new DBField(FLD_smsEmail             , String.class        , DBField.TYPE_STRING(64)  , "SMS EMail Address"             , "edit=2"),
        new DBField(FLD_imeiNumber           , String.class        , DBField.TYPE_STRING(24)  , "IMEI Number"                   , "edit=2"),
        new DBField(FLD_lastInputState       , Long.TYPE           , DBField.TYPE_UINT32      , "Last Input State"              , ""),
        new DBField(FLD_lastOutputState      , Long.TYPE           , DBField.TYPE_UINT32      , "Last Output State"             , ""),
        new DBField(FLD_ignitionIndex        , Integer.TYPE        , DBField.TYPE_UINT16      , "Ignition I/O Index"            , "edit=2"),
        new DBField(FLD_codeVersion          , String.class        , DBField.TYPE_STRING(32)  , "Code Version"                  , ""),
        new DBField(FLD_featureSet           , String.class        , DBField.TYPE_STRING(64)  , "Feature Set"                   , ""),
        new DBField(FLD_ipAddressValid       , DTIPAddrList.class  , DBField.TYPE_STRING(128) , "Valid IP Addresses"            , "edit=2"),
        // -- Last Event
        new DBField(FLD_ipAddressCurrent     , DTIPAddress.class   , DBField.TYPE_STRING(32)  , "Current IP Address"            , ""),
        new DBField(FLD_remotePortCurrent    , Integer.TYPE        , DBField.TYPE_UINT16      , "Current Remote Port"           , ""),
        new DBField(FLD_listenPortCurrent    , Integer.TYPE        , DBField.TYPE_UINT16      , "Current Listen Port"           , ""),
        // -- Ping/Command
        new DBField(FLD_pendingPingCommand   , String.class        , DBField.TYPE_TEXT        , "Pending Ping Command"          , "edit=2"),
        new DBField(FLD_lastPingTime         , Long.TYPE           , DBField.TYPE_UINT32      , "Last 'Ping' Time"              , "format=time"),
        new DBField(FLD_totalPingCount       , Integer.TYPE        , DBField.TYPE_UINT16      , "Total 'Ping' Count"            , ""),
        new DBField(FLD_maxPingCount         , Integer.TYPE        , DBField.TYPE_UINT16      , "Maximum 'Ping' Count"          , "edit=2"),
        new DBField(FLD_expectAck            , Boolean.TYPE        , DBField.TYPE_BOOLEAN     , "Expecting an ACK"              , "edit=2"),
        new DBField(FLD_expectAckCode        , Integer.TYPE        , DBField.TYPE_UINT32      , "Expected ACK Status Code"      , "edit=2"),
        new DBField(FLD_lastAckCommand       , String.class        , DBField.TYPE_TEXT        , "Last Command Expecting an ACK" , ""),
        new DBField(FLD_lastAckTime          , Long.TYPE           , DBField.TYPE_UINT32      , "Last Received 'ACK' Time"      , "format=time"),
        // -- Last Event
        new DBField(FLD_lastTotalConnectTime , Long.TYPE           , DBField.TYPE_UINT32      , "Last Total Connect Time"       , "format=time"),
        new DBField(FLD_lastDuplexConnectTime, Long.TYPE           , DBField.TYPE_UINT32      , "Last Duplex Connect Time"      , "format=time"),
        // -- Device Communication Server Configuration
        new DBField(FLD_dcsPropertiesID      , String.class        , DBField.TYPE_STRING(32)  , "DCS Properties ID"             , "edit=2"),
        new DBField(FLD_dcsConfigMask        , Long.TYPE           , DBField.TYPE_UINT32      , "DCS Configuration Mask"        , "edit=2"),
        new DBField(FLD_dcsConfigString      , String.class        , DBField.TYPE_STRING(80)  , "DCS Configuration String"      , "edit=2"),
        new DBField(FLD_dcsCommandHost       , String.class        , DBField.TYPE_STRING(32)  , "DCS Command Host"              , "edit=2"),
        new DBField(FLD_dcsCommandState      , String.class        , DBField.TYPE_STRING(64)  , "Command State"                 , "edit=2"),
        // -- Missing fields
        //Device.FLD_lastTcpSessionID,
        //Device.FLD_lastBatteryLevel,
        //Device.FLD_lastBatteryVolts,
        //Device.FLD_lastVBatteryVolts,
        //Device.FLD_lastFuelLevel,           
        //Device.FLD_lastFuelLevel2,           
        //Device.FLD_lastFuelTotal,
        //Device.FLD_lastOilLevel,           
        //Device.FLD_lastValidLatitude,
        //Device.FLD_lastValidLongitude,
        //Device.FLD_lastValidSpeedKPH,
        //Device.FLD_lastValidHeading,
        //Device.FLD_lastGPSTimestamp,
        //Device.FLD_lastEventTimestamp,
        //Device.FLD_lastEventStatusCode,
        //Device.FLD_lastMalfunctionLamp,
        //Device.FLD_lastFaultCode,
        //Device.FLD_lastOdometerKM,
        //Device.FLD_lastDistanceKM,
        //Device.FLD_lastEngineOnHours,
        //Device.FLD_lastEngineOnTime,
        //Device.FLD_lastEngineOffTime,
        //Device.FLD_lastEngineHours,
        //Device.FLD_lastPtoOnHours,
        //Device.FLD_lastPtoOnTime,
        //Device.FLD_lastPtoOffTime,
        //Device.FLD_lastPtoHours,
        //Device.FLD_lastIgnitionOnHours,
        //Device.FLD_lastIgnitionOnTime,
        //Device.FLD_lastIgnitionOffTime,
        //Device.FLD_lastIgnitionHours,
        //Device.FLD_lastStopTime,
        //Device.FLD_lastStartTime,
        // -- Common fields
        newField_displayName(),
        newField_description(),
        newField_lastUpdateTime(),
        newField_lastUpdateAccount(true),
        newField_lastUpdateUser(true),
        newField_creationTime(),
    };

    // -- OpenDMTP support
    // -- DMTP
    public static final String FLD_supportsDMTP          = "supportsDMTP";          // DMTP
    public static final String FLD_supportedEncodings    = "supportedEncodings";    // DMTP
    public static final String FLD_unitLimitInterval     = "unitLimitInterval";     // DMTP
    public static final String FLD_maxAllowedEvents      = "maxAllowedEvents";      // DMTP
    public static final String FLD_totalProfileMask      = "totalProfileMask";      // DMTP
    public static final String FLD_totalMaxConn          = "totalMaxConn";          // DMTP
    public static final String FLD_totalMaxConnPerMin    = "totalMaxConnPerMin";    // DMTP
    public static final String FLD_duplexProfileMask     = "duplexProfileMask";     // DMTP
    public static final String FLD_duplexMaxConn         = "duplexMaxConn";         // DMTP
    public static final String FLD_duplexMaxConnPerMin   = "duplexMaxConnPerMin";   // DMTP
    public static final DBField OpenDMTPFieldInfo[] = {
        // -- DMTP
        new DBField(FLD_supportsDMTP         , Boolean.TYPE        , DBField.TYPE_BOOLEAN     , "Supports DMTP"                 , "edit=2"),
        new DBField(FLD_supportedEncodings   , Integer.TYPE        , DBField.TYPE_UINT8       , "Supported Encodings"           , "edit=2 format=X1 editor=encodings mask=Transport$Encodings"),
        new DBField(FLD_unitLimitInterval    , Integer.TYPE        , DBField.TYPE_UINT16      , "Accounting Time Interval Min"  , "edit=2"),
        new DBField(FLD_maxAllowedEvents     , Integer.TYPE        , DBField.TYPE_UINT16      , "Max Events per Interval"       , "edit=2"),
        new DBField(FLD_totalProfileMask     , DTProfileMask.class , DBField.TYPE_BLOB        , "Total Profile Mask"            , ""),
        new DBField(FLD_totalMaxConn         , Integer.TYPE        , DBField.TYPE_UINT16      , "Max Total Conn per Interval"   , "edit=2"),
        new DBField(FLD_totalMaxConnPerMin   , Integer.TYPE        , DBField.TYPE_UINT16      , "Max Total Conn per Minute"     , "edit=2"),
        new DBField(FLD_duplexProfileMask    , DTProfileMask.class , DBField.TYPE_BLOB        , "Duplex Profile Mask"           , ""),
        new DBField(FLD_duplexMaxConn        , Integer.TYPE        , DBField.TYPE_UINT16      , "Max Duplex Conn per Interval"  , "edit=2"),
        new DBField(FLD_duplexMaxConnPerMin  , Integer.TYPE        , DBField.TYPE_UINT16      , "Max Duplex Conn per Minute"    , "edit=2"),
    };

    /**
    *** Key class 
    **/
    public static class Key
      //extends AccountKey<Transport>
        extends DeviceKey<Transport>
    {
        public Key() {
            super();
        }
        public Key(String acctId, String devId, String xportId) {
            super.setKeyValue(FLD_accountID  , ((acctId  != null)? acctId.toLowerCase()  : ""));
            super.setKeyValue(FLD_deviceID   , ((devId   != null)? devId.toLowerCase()   : ""));
            super.setKeyValue(FLD_transportID, ((xportId != null)? xportId.toLowerCase() : ""));
        }
        public DBFactory<Transport> getFactory() {
            return Transport.getFactory();
        }
    }

    /**
    *** Factory constructor 
    **/
    private static DBFactory<Transport> factory = null;
    public static DBFactory<Transport> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                Transport.TABLE_NAME(),
                Transport.FieldInfo, 
                DBFactory.KeyType.PRIMARY, 
                Transport.class, 
                Transport.Key.class,
                true/*editable*/, true/*viewable*/);
            if (PARENT_TABLES_DEPENDENCIES) {
                // -- should delete this record when Account/Device is deleted.
                factory.addParentTable(Account.TABLE_NAME()); // FLD_accountID
                factory.addParentTable(Device.TABLE_NAME());  // FLD_deviceID
            } else {
                // -- does not delete this record when Account/Device is deleted
                // -  If not deleted, then a special mechanism will be necessary to 
                // -  locate these orphaned records.
            }
        }
        return factory;
    }

    /**
    *** Bean instance 
    **/
    public Transport()
    {
        super();
    }

    /**
    *** Table record 
    **/
    public Transport(Transport.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Table description 
    **/
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(Transport.class, loc);
        return i18n.getString("Transport.description", 
            "This table defines " +
            "the data transport specific information for an Asset/Device.  A 'Transport' " +
            "represents the datapath used to send data to a server.  In some cases a single 'Device' " +
            "can have more than one such datapath to the server, such as a device that incorporates " +
            "both GPRS and satellite communications."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below
    // ------------------------------------------------------------------------

    /**
    *** Gets the Transport ID 
    **/
    public String getTransportID()
    {
        String v = (String)this.getFieldValue(FLD_transportID);
        return StringTools.trim(v);
    }

    /**
    *** Sets the Transport ID 
    **/
    private void setTransportID(String v)
    {
        this.setFieldValue(FLD_transportID, StringTools.trim(v));
    }
                
    // ------------------------------------------------------------------------

    private Account targetAccount = null;
    private Device  targetDevice  = null;

    /**
    *** Gets the Target AccountID.
    *** Will return blank, if the target should be the primary-key DeviceID
    **/
    public String getTargetAccountID()
    {
        String v = (String)this.getFieldValue(FLD_targetAccountID);
        return StringTools.trim(v);
    }

    /**
    *** Gets the Target AccountID.
    *** Returns '<code>getTargetAccountID()</code>' if non-blank, otherwise '<code>getAccountID()</code>'
    **/
    public String _getTargetAccountID()
    {
        String v = this.getTargetAccountID();
        if (!StringTools.isBlank(v)) {
            // -- alternate/target AccountID was set (by "sysadmin")
            return v;
        } else {
            // -- alternate/target AccountID is same as this AccountID
            return this.getAccountID();
        }
    }

    /**
    *** Sets the Target AccountID
    **/
    public void setTargetAccountID(String v)
    {
        // -- only "sysadmin" should be allowed to set this value
        this.setFieldValue(FLD_targetAccountID, StringTools.trim(v));
        this.targetAccount = null;
        this.targetDevice  = null;
    }

    /**
    *** Gets the Target Account instance
    *** Will return this primary-key Account, if 'targetAccountID' is blank
    **/
    public Account getTargetAccount()
    {
        if (this.targetAccount == null) {
            String targAcctID = this.getTargetAccountID();
            if (StringTools.isBlank(targAcctID)) {
                // -- no target account? return this account
                this.targetAccount = this.getAccount();
            } else {
                try {
                    this.targetAccount = Account.getAccount(targAcctID);
                    if (this.targetAccount == null) {
                        Print.logError("TargetAccount not found: " + targAcctID);
                    }
                } catch (DBException dbe) {
                    Print.logError("TargetAccount not found: " + targAcctID);
                    this.targetAccount = null;
                }
            }
        }
        return this.targetAccount;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Target DeviceID.
    *** Will return blank, if the actual target should be the primary-key DeviceID
    **/
    public String getTargetDeviceID()
    {
        if (StringTools.isBlank(this.getTargetAccountID())) {
            // -- if no targetAccountID, then targetDeviceID shold also be blank
            return "";
        } else {
            String v = (String)this.getFieldValue(FLD_targetDeviceID);
            return StringTools.trim(v);
        }
    }

    /**
    *** Gets the Target DeviceID.
    *** Returns '<code>getTargetDeviceID()</code>' if non-blank, otherwise '<code>getDeviceID()</code>'
    **/
    public String _getTargetDeviceID()
    {
        String v = this.getTargetDeviceID();
        if (!StringTools.isBlank(v)) {
            // -- alternate/target DeviceID was set (by "sysadmin")
            return v;
        } else {
            // -- alternate/target DeviceID is same as this DeviceID
            return this.getDeviceID();
        }
    }

    /**
    *** Sets the Target DeviceID
    **/
    public void setTargetDeviceID(String v)
    {
        // -- only "sysadmin" should be allowed to set this value
        this.setFieldValue(FLD_targetDeviceID, StringTools.trim(v));
        this.targetDevice = null;
    }

    /**
    *** Gets the Target Device instance
    *** Will return this primary-key Device, if 'targetDeviceID' is blank
    **/
    public Device getTargetDevice()
    {
        if (this.targetDevice == null) {
            String targAcctID = this.getTargetAccountID(); // may be blank
            String targDevID  = this.getTargetDeviceID();  // may be blank
            if (StringTools.isBlank(targDevID)) {
                // -- no target deviceID? return this device
                Device targDev = this.getDevice(); // this device
                if (targDev != null) {
                    this.targetDevice = targDev;
                    this.targetDevice.setTransport(this);
                } else {
                    Print.logError("Unexpected error, Transport device is null: " + this.getAccountID()+"/"+this.getDeviceID());
                    this.targetDevice = null;
                }
            } else {
                // -- read TargetDevice
                try {
                    Account targAcct = this.getTargetAccount(); // may be null
                    Device  targDev  = Device.getDevice(targAcct, targDevID);
                    if (targDev != null) {
                        this.targetDevice = targDev;
                        this.targetDevice.setTransport(this);
                    } else {
                        Print.logError("TargetDevice not found: " + targAcctID+"/"+targDevID);
                        this.targetDevice = null;
                    }
                } catch (DBException dbe) {
                    Print.logError("TargetDevice not found: " + targAcctID+"/"+targDevID);
                    this.targetDevice = null;
                }
            }
        }
        return this.targetDevice;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the assigned UniqueID 
    **/
    public String getUniqueID()
    {
        String v = (String)this.getFieldValue(FLD_uniqueID);
        return StringTools.trim(v);
    }

    /**
    *** Sets the assigned UniqueID 
    **/
    public void setUniqueID(String v)
    {
        this.setFieldValue(FLD_uniqueID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the code/name representing the DCS type
    **/
    public String getDeviceCode()
    {
        String v = (String)this.getFieldValue(FLD_deviceCode);
        return StringTools.trim(v);
    }

    /**
    *** Sets the code/name representing the DCS type
    **/
    public void setDeviceCode(String v)
    {
        this.setFieldValue(FLD_deviceCode, StringTools.trim(v));
    }
    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device serial number (optional)
    **/
    public String getSerialNumber()
    {
        String v = (String)this.getFieldValue(FLD_serialNumber);
        return StringTools.trim(v);
    }

    /**
    *** Sets the physical device serial number
    **/
    public void setSerialNumber(String v)
    {
        this.setFieldValue(FLD_serialNumber, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device SIM phone number (optional)
    **/
    public String getSimPhoneNumber()
    {
        String v = (String)this.getFieldValue(FLD_simPhoneNumber);
        return StringTools.trim(v);
    }

    /**
    *** Sets the physical device SIM phone number (optional)
    **/
    public void setSimPhoneNumber(String v)
    {
        this.setFieldValue(FLD_simPhoneNumber, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the SIM-ID (ICCID)
    *** @return  The SIM-ID (ICCID)
    **/
    public String getSimID()
    {
        String v = (String)this.getFieldValue(FLD_simID);
        return StringTools.trim(v);
    }

    /**
    *** Gets the SIM-ID (ICCID)
    *** @param v  The SIM-ID (ICCID)
    **/
    public void setSimID(String v)
    {
        this.setFieldValue(FLD_simID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device email-based destination for SMS messaging
    **/
    public String getSmsEmail()
    {
        String v = (String)this.getFieldValue(FLD_smsEmail);
        return StringTools.trim(v);
    }

    /**
    *** Sets the physical device email-based destination for SMS messaging
    **/
    public void setSmsEmail(String v)
    {
        this.setFieldValue(FLD_smsEmail, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device IMEI# (optional)
    **/
    public String getImeiNumber()
    {
        String v = (String)this.getFieldValue(FLD_imeiNumber);
        return StringTools.trim(v);
    }

    /**
    *** Sets the physical device IMEI# (optional)
    **/
    public void setImeiNumber(String v)
    {
        this.setFieldValue(FLD_imeiNumber, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device type (optional)
    **/
    public String getDeviceType()
    {
        String v = (String)this.getFieldValue(FLD_deviceType);
        return StringTools.trim(v);
    }
    
    /**
    *** Sets the physical device type (optional)
    **/
    public void setDeviceType(String v)
    {
        this.setFieldValue(FLD_deviceType, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device last digital input state mask
    **/
    public long getLastInputState()
    {
        Long v = (Long)this.getFieldValue(FLD_lastInputState);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the physical device last digital input state mask
    **/
    public void setLastInputState(long v)
    {
        this.setFieldValue(FLD_lastInputState, v & 0xFFFFFFFFL);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device last digital output state mask
    **/
    public long getLastOutputState()
    {
        Long v = (Long)this.getFieldValue(FLD_lastOutputState);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the physical device last digital output state mask
    **/
    public void setLastOutputState(long v)
    {
        this.setFieldValue(FLD_lastOutputState, v & 0xFFFFFFFFL);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device ignition index
    **/
    public int getIgnitionIndex()
    {
        Integer v = (Integer)this.getFieldValue(FLD_ignitionIndex);
        return (v != null)? v.intValue() : -1;
    }

    /**
    *** Sets the physical device ignition index
    **/
    public void setIgnitionIndex(int v)
    {
        this.setFieldValue(FLD_ignitionIndex, v);
    }

    /**
    *** Gets the physical device ignition status codes, based on the ignition index
    **/
    public int[] getIgnitionStatusCodes()
    {
        int ndx = this.getIgnitionIndex();
        if (ndx >= 0) {
            int scOFF = StatusCodes.GetDigitalInputStatusCode(ndx, false);
            int scON  = StatusCodes.GetDigitalInputStatusCode(ndx, true );
            if (scOFF != StatusCodes.STATUS_NONE) {
                return new int[] { scOFF, scON };
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device code/firmware version (optional)
    **/
    public String getCodeVersion()
    {
        String v = (String)this.getFieldValue(FLD_codeVersion);
        return StringTools.trim(v);
    }

    /**
    *** Gets the physical device code/firmware version (optional)
    **/
    public void setCodeVersion(String v)
    {
        this.setFieldValue(FLD_codeVersion, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device feature set (optional)
    **/
    public String getFeatureSet()
    {
        String v = (String)this.getFieldValue(FLD_featureSet);
        return StringTools.trim(v);
    }

    /**
    *** Sets the physical device feature set (optional)
    **/
    public void setFeatureSet(String v)
    {
        this.setFieldValue(FLD_featureSet, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the physical device valid IP addresses mask
    **/
    public DTIPAddrList getIpAddressValid()
    {
        DTIPAddrList v = (DTIPAddrList)this.getFieldValue(FLD_ipAddressValid);
        return v; // May return null!!
    }

    /**
    *** Sets the physical device valid IP addresses mask
    **/
    public void setIpAddressValid(DTIPAddrList v)
    {
        this.setFieldValue(FLD_ipAddressValid, v);
    }
    
    /**
    *** Sets the physical device valid IP addresses mask
    **/
    public void setIpAddressValid(String v)
    {
        this.setIpAddressValid((v != null)? new DTIPAddrList(v) : null);
    }

    /**
    *** Checks the specified IP addresses against the valid IP address mask
    **/
    public boolean isValidIPAddress(String ipAddr)
    {
        DTIPAddrList ipList = this.getIpAddressValid();
        if ((ipList == null) || ipList.isEmpty()) {
            return true;
        } else
        if (!ipList.isMatch(ipAddr)) {
            return false;
        } else {
            return true;
        }
    }
    
    // ------------------------------------------------------------------------

    public DTIPAddress getIpAddressCurrent()
    {
        DTIPAddress v = (DTIPAddress)this.getFieldValue(FLD_ipAddressCurrent);
        return v; // May return null!!
    }

    public void setIpAddressCurrent(DTIPAddress v)
    {
        this.setFieldValue(FLD_ipAddressCurrent, v);
    }

    public void setIpAddressCurrent(String v)
    {
        this.setIpAddressCurrent((v != null)? new DTIPAddress(v) : null);
    }

    // ------------------------------------------------------------------------

    public int getRemotePortCurrent()
    {
        Integer v = (Integer)this.getFieldValue(FLD_remotePortCurrent);
        return (v != null)? v.intValue() : 0;
    }

    public void setRemotePortCurrent(int v)
    {
        this.setFieldValue(FLD_remotePortCurrent, ((v > 0)? v : 0));
    }

    // ------------------------------------------------------------------------

    public int getListenPortCurrent()
    {
        Integer v = (Integer)this.getFieldValue(FLD_listenPortCurrent);
        return (v != null)? v.intValue() : 0;
    }

    public void setListenPortCurrent(int v)
    {
        this.setFieldValue(FLD_listenPortCurrent, ((v > 0)? v : 0));
    }

    // ------------------------------------------------------------------------

    public String getPendingPingCommand()
    {
        String v = (String)this.getFieldValue(FLD_pendingPingCommand);
        return StringTools.trim(v);
    }

    public void setPendingPingCommand(String v)
    {
        this.setFieldValue(FLD_pendingPingCommand, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    public long getLastPingTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastPingTime);
        return (v != null)? v.longValue() : 0L;
    }

    public void setLastPingTime(long v)
    {
        this.setFieldValue(FLD_lastPingTime, v);
    }

    // ------------------------------------------------------------------------

    public int getTotalPingCount()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalPingCount);
        return (v != null)? v.intValue() : 0;
    }

    public void setTotalPingCount(int v)
    {
        v = (v > 0xFFFF)? 0xFFFF : (v > 0)? v : 0; // limit to 16-bit
        this.setFieldValue(FLD_totalPingCount, v);
    }

    // ------------------------------------------------------------------------

    public int getMaxPingCount()
    {
        Integer v = (Integer)this.getFieldValue(FLD_maxPingCount);
        return (v != null)? v.intValue() : 0;
    }

    public void setMaxPingCount(int v)
    {
        v = (v > 0xFFFF)? 0xFFFF : (v > 0)? v : 0; // limit to 16-bit
        this.setFieldValue(FLD_maxPingCount, v);
    }

    // ------------------------------------------------------------------------

    public boolean getExpectAck()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_expectAck);
        return (v != null)? v.booleanValue() : true;
    }

    public void setExpectAck(boolean v)
    {
        this.setFieldValue(FLD_expectAck, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the expected ACK status code, or '0' if any code should match
    **/
    public int getExpectAckCode()
    {
        Integer v = (Integer)this.getFieldValue(FLD_expectAckCode);
        return (v != null)? v.intValue() : StatusCodes.STATUS_NONE;
    }

    /**
    *** Sets the expected ACK status code, or '0' if any code should match
    **/
    public void setExpectAckCode(int v)
    {
        this.setFieldValue(FLD_expectAckCode, ((v >= 0)? v : StatusCodes.STATUS_NONE));
    }

    // ------------------------------------------------------------------------

    public String getLastAckCommand()
    {
        String v = (String)this.getFieldValue(FLD_lastAckCommand);
        return StringTools.trim(v);
    }

    public void setLastAckCommand(String v)
    {
        this.setFieldValue(FLD_lastAckCommand, StringTools.trim(v));
    }

    public boolean getExpectingCommandAck()
    {
        return this.getExpectAck() && (this.getLastAckTime() <= 0L);
    }

    // ------------------------------------------------------------------------

    public long getLastAckTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastAckTime);
        return (v != null)? v.longValue() : 0L;
    }

    public void _setLastAckTime(long v)
    {
        this.setFieldValue(FLD_lastAckTime, v);
    }

    public void setLastAckTime(long v)
    {
        this._setLastAckTime(v);
        /*
        if (this.assocDevice != null) {
            this.assocDevice._setLastAckTime(v);
        }
        */
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Returns true if this device supports the OpenDMTP protocol
    *** @return True if this device supports the OpenDMTP protocol
    **/
    public boolean getSupportsDMTP()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_supportsDMTP);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** OpenDMTP: Returns true if this device supports the OpenDMTP protocol
    *** @return True if this device supports the OpenDMTP protocol
    **/
    public boolean supportsDMTP()
    {
        return this.getSupportsDMTP();
    }

    /**
    *** OpenDMTP: Sets the OpenDMTP protocol support state
    *** @param v The OpenDMTP protocol support state
    **/
    public void setSupportsDMTP(boolean v)
    {
        this.setFieldValue(FLD_supportsDMTP, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the supported OpenDMTP encodings
    *** @return The supported OpenDMTP encodings
    **/
    public int getSupportedEncodings()
    {
        Integer v = (Integer)this.getFieldValue(FLD_supportedEncodings);
        return (v != null)? v.intValue() : (int)Encodings.BINARY.getLongValue();
    }

    /**
    *** OpenDMTP: Sets the supported OpenDMTP encodings
    *** @param v The supported OpenDMTP encodings
    **/
    public void setSupportedEncodings(int v)
    {
        v &= (int)EnumTools.getValueMask(Encodings.class);
        if (v == 0) { v = (int)Encodings.BINARY.getLongValue(); }
        this.setFieldValue(FLD_supportedEncodings, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the OpenDMTP unit limit interval
    *** @return The OpenDMTP unit limit interval
    **/
    public int getUnitLimitInterval() // Minutes
    {
        Integer v = (Integer)this.getFieldValue(FLD_unitLimitInterval);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** OpenDMTP: Sets the OpenDMTP unit limit interval
    *** @param v The OpenDMTP unit limit interval
    **/
    public void setUnitLimitInterval(int v) // Minutes
    {
        this.setFieldValue(FLD_unitLimitInterval, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the maximum allowed OpenDMTP events
    *** @return The maximum allowed OpenDMTP events
    **/
    public int getMaxAllowedEvents()
    {
        Integer v = (Integer)this.getFieldValue(FLD_maxAllowedEvents);
        return (v != null)? v.intValue() : 1;
    }

    /**
    *** OpenDMTP: Sets the maximum allowed OpenDMTP events
    *** @param v The maximum allowed OpenDMTP events
    **/
    public void setMaxAllowedEvents(int max)
    {
        this.setFieldValue(FLD_maxAllowedEvents, max);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the total (UDP/TCP) connection profile mask
    *** @return The total (UDP/TCP) connection profile mask
    **/
    public DTProfileMask getTotalProfileMask()
    {
        DTProfileMask v = (DTProfileMask)this.getFieldValue(FLD_totalProfileMask);
        return v;
    }

    /**
    *** OpenDMTP: Sets the total (UDP/TCP) connection profile mask
    *** @param v The total (UDP/TCP) connection profile mask
    **/
    public void setTotalProfileMask(DTProfileMask v)
    {
        this.setFieldValue(FLD_totalProfileMask, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the maximum total connections allowed per interval<br>
    *** Note: The effective maximum value for this field is defined by the following:<br>
    *** (org.opendmtp.server.base.ValidateConnections.BITS_PER_MINUTE_MASK * this.getUnitLimitIntervalMinutes())
    *** @return The maximum total connections allowed per interval
    **/
    public int getTotalMaxConn()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalMaxConn);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** OpenDMTP: Sets the maximum total connections allowed per interval
    *** @param v The maximum total connections allowed per interval
    **/
    public void setTotalMaxConn(int v)
    {
        this.setFieldValue(FLD_totalMaxConn, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the maximum total connections allowed per minute<br>
    *** Note: The effective maximum value for this field is defined by the constant:<br>
    *** "org.opendmtp.server.base.ValidateConnections.BITS_PER_MINUTE_MASK"
    *** @return The maximum total connections allowed per minute
    **/
    public int getTotalMaxConnPerMin()
    {
        Integer v = (Integer)this.getFieldValue(FLD_totalMaxConnPerMin);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** OpenDMTP: Sets the maximum total connections allowed per minute<br>
    *** @param v The maximum total connections allowed per minute
    **/
    public void setTotalMaxConnPerMin(int v)
    {
        this.setFieldValue(FLD_totalMaxConnPerMin, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the Duplex/TCP connection profile mask
    *** @return The Duplex/TCP connection profile mask
    **/
    public DTProfileMask getDuplexProfileMask()
    {
        DTProfileMask v = (DTProfileMask)this.getFieldValue(FLD_duplexProfileMask);
        return v;
    }

    /**
    *** OpenDMTP: Sets the Duplex/TCP connection profile mask
    *** @param v The Duplex/TCP connection profile mask
    **/
    public void setDuplexProfileMask(DTProfileMask v)
    {
        this.setFieldValue(FLD_duplexProfileMask, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the maximum Duplex/TCP connections per Interval
    *** Note: The effective maximum value for this field is defined by the following:
    *** (org.opendmtp.server.base.ValidateConnections.BITS_PER_MINUTE_MASK * this.getUnitLimitIntervalMinutes())
    *** @return The maximum Duplex/TCP connections per Interval
    **/
    public int getDuplexMaxConn()
    {
        Integer v = (Integer)this.getFieldValue(FLD_duplexMaxConn);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** OpenDMTP: Sets the maximum Duplex/TCP connections per Interval
    *** @param v The maximum Duplex/TCP connections per Interval
    **/
    public void setDuplexMaxConn(int max)
    {
        this.setFieldValue(FLD_duplexMaxConn, max);
    }

    // ------------------------------------------------------------------------

    /**
    *** OpenDMTP: Gets the maximum Duplex/TCP connections per Minute
    *** Note: The effective maximum value for this field is defined by the constant:
    *** "org.opendmtp.server.base.ValidateConnections.BITS_PER_MINUTE_MASK"
    *** @return The maximum Duplex/TCP connections per Minute
    **/
    public int getDuplexMaxConnPerMin()
    {
        Integer v = (Integer)this.getFieldValue(FLD_duplexMaxConnPerMin);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** OpenDMTP: Sets the maximum Duplex/TCP connections per Minute
    *** @param v The maximum Duplex/TCP connections per Minute
    **/
    public void setDuplexMaxConnPerMin(int max)
    {
        this.setFieldValue(FLD_duplexMaxConnPerMin, max);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the last UDP/TCP connection time
    *** @return The last UDP/TCP connection time
    **/
    public long getLastTotalConnectTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastTotalConnectTime);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the last UDP/TCP connection time
    *** @param v The last UDP/TCP connection time
    **/
    public void setLastTotalConnectTime(long v)
    {
        this.setFieldValue(FLD_lastTotalConnectTime, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the last Duplex/TCP connection time
    *** @return The last Duplex/TCP connection time
    **/
    public long getLastDuplexConnectTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastDuplexConnectTime);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the last Duplex/TCP connection time
    *** @param v The last Duplex/TCP connection time
    **/
    public void setLastDuplexConnectTime(long v)
    {
        this.setFieldValue(FLD_lastDuplexConnectTime, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the DCS Properties ID assigned to this device (DCS Property ID)<br>
    *** Used by some DCS modules to select specific device configurations
    *** @return The DCS Property ID
    **/
    public String getDcsPropertiesID()
    {
        String v = (String)this.getFieldValue(FLD_dcsPropertiesID);
        return StringTools.trim(v);
    }

    /**
    *** Sets the DCS Properties ID assigned to this device (DCS Property ID)<br>
    *** Used by some DCS modules to select specific device configurations
    *** @param v The DCS Property ID
    **/
    public void setDcsPropertiesID(String v)
    {
        this.setFieldValue(FLD_dcsPropertiesID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the DCS configuration mask.
    *** Usage defined by specific DCS. For example, the Enfora DCS uses this to
    *** set a default event field data-mask.
    *** @return The DCS configuration mask
    **/
    public long getDcsConfigMask()
    {
        Long v = (Long)this.getOptionalFieldValue(FLD_dcsConfigMask);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the DCS configuration mask (usage defined by specific DCS)
    *** @param v The DCS configuration mask
    **/
    public void setDcsConfigMask(long v)
    {
        this.setOptionalFieldValue(FLD_dcsConfigMask, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the DCS configuration String.
    *** Usage defined by specific DCS. For example, the Xirgo DCS uses this to get 
    *** the default firmware version.
    *** @return The DCS configuration String
    **/
    public String getDcsConfigString()
    {
        String v = (String)this.getOptionalFieldValue(FLD_dcsConfigString);
        return StringTools.trim(v);
    }

    /**
    *** Sets the DCS configuration String (usage defined by specific DCS)
    *** @param v The DCS configuration String
    **/
    public void setDcsConfigString(String v)
    {
        this.setOptionalFieldValue(FLD_dcsConfigString, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the DCS Command Host assigned to this device (ie. the host name
    *** where the DCS for this device is running)<br>
    *** May return blank to indicate that the default DCS command host should
    *** be used.
    *** @return The DCS Command Hostname
    **/
    public String getDcsCommandHost()
    {
        String v = (String)this.getFieldValue(FLD_dcsCommandHost);
        return StringTools.trim(v);
    }

    /**
    *** Sets the DCS Command Host assigned to this device (ie. the host name
    *** where the DCS for this device is running)<br>
    *** May be blank to indicate that the default DCS command host should be
    *** used.
    *** @param v The DCS Command Hostname
    **/
    public void setDcsCommandHost(String v)
    {
        this.setFieldValue(FLD_dcsCommandHost, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the current DCS Command State<br>
    *** Used by some DCS modules when sending compound commands to a device
    *** @return The DCS Command State
    **/
    public String getDcsCommandState()
    {
        // command=COMMAND timestamp=EPOCH state=300
        String v = (String)this.getFieldValue(FLD_dcsCommandState);
        return StringTools.trim(v);
    }

    /**
    *** Sets the current DCS Command State<br>
    *** Used by some DCS modules to select specific device configurations
    *** @param v The DCS Command State
    **/
    public void setDcsCommandState(String v)
    {
        this.setFieldValue(FLD_dcsCommandState, StringTools.trim(v));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {
        this.setDescription(DEFAULT_XPORT_NAME + " [" + this.getDeviceID() + "/" + this.getTransportID() + "]");
        // -- OpenDMTP
        this.setSupportedEncodings(DEFAULT_ENCODING);
        this.setTotalMaxConn(DEFAULT_TOTAL_MAX_CONNECTIONS);
        this.setDuplexMaxConn(DEFAULT_DUPLEX_MAX_CONNECTIONS);
        this.setUnitLimitInterval(DEFAULT_UNIT_LIMIT_INTERVAL_MIN); // Minutes
        this.setTotalMaxConnPerMin(DEFAULT_TOTAL_MAX_CONNECTIONS_PER_MIN);
        this.setDuplexMaxConnPerMin(DEFAULT_DUPLEX_MAX_CONNECTIONS_PER_MIN);
        this.setMaxAllowedEvents(DEFAULT_MAX_ALLOWED_EVENTS);
        // --
        super.setRuntimeDefaultValues();
    }

    // ------------------------------------------------------------------------

    public String toString()
    {
        return this.getAccountID() + "/" + this.getDeviceID() + "/" + this.getTransportID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static boolean exists(String acctID, String devID, String xportID)
        throws DBException // if error occurs while testing existance
    {
        if ((acctID != null) && (xportID != null)) {
            Transport.Key xportKey = new Transport.Key(acctID, devID, xportID);
            return xportKey.exists(DBReadWriteMode.READ_WRITE);
        }
        return false;
    }

    public static boolean exists(String uniqID)
        throws DBException // if error occurs while testing existance
    {
        if (!StringTools.isBlank(uniqID)) {
            Transport xport = Transport.getTransportByUniqueID(uniqID);
            return (xport != null);
        }
        return false;
    }

    public static boolean exists(String prefix[], String mobileID)
        throws DBException // if error occurs while testing existance
    {
        if (StringTools.isBlank(mobileID)) {
            return false;
        } else
        if (ListTools.isEmpty(prefix)) {
            return Transport.exists(mobileID);
        } else {
            for (int i = 0; i < prefix.length; i++) {
                String uniqueID = prefix[i] + mobileID;
                if (Transport.exists(uniqueID)) {
                    return true;
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /* get Transport by UniqueID (READ database assumed) */
    // -- may return null
    public static Transport getTransportByUniqueID(String uniqId)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* invalid id? */
        if (StringTools.isBlank(uniqId)) {
            return null; // just say it doesn't exist
        }
        
        /* read Transport for unique-id */
        Transport  xport = null;
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
        
            /* select */
            // DBSelect: SELECT * FROM Transport WHERE (uniqueID='unique')
            DBSelect<Transport> dsel = new DBSelect<Transport>(Transport.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(dwh.EQ(Transport.FLD_uniqueID,uniqId)));
            dsel.setLimit(2);
            // Note: The index on the column FLD_uniqueID does not enforce uniqueness
            // (since null/empty values are allowed and needed)

            /* get record */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId  = rs.getString(FLD_accountID);
                String devId   = rs.getString(FLD_deviceID);
                String xportId = rs.getString(FLD_transportID);
                xport = new Transport(new Transport.Key(acctId,devId,xportId));
                xport.setAllFieldValues(rs);
                if (rs.next()) {
                    Print.logError("Found multiple occurances of this unique-id: " + uniqId);
                }
                break; // only one record
            }
            // it's possible at this point that we haven't even read 1 device

        } catch (SQLException sqe) {
            throw new DBException("Getting Transport for UniqueID", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return Transport */
        // Note: 'xport' may be null if it wasn't found
        return xport;

    }

    // ------------------------------------------------------------------------

    /* get Transport for Account/Transport ID */
    // may return null
    public static Transport getTransport(Account account, String devID, String xportID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        if ((account != null) && (devID != null) && (xportID != null)) {
            String acctID = account.getAccountID();
            Transport.Key key = new Transport.Key(acctID, devID, xportID);
            if (key.exists(rwMode)) {
                Transport xport = key._getDBRecord(true, rwMode);
                xport.setAccount(account);
                return xport;
            } else {
                // Transport does not exist
                return null;
            }
        } else {
            return null; // just say it doesn't exist
        }
    }

    /* get Transport */
    // Note: does NOT return null
    public static Transport getTransport(Account account, String devID, String xportID, boolean create)
        throws DBException
    {
        
        /* account-id specified? */
        if (account == null) {
            throw new DBNotFoundException("Account not specified.");
        }
        String acctID = account.getAccountID();

        /* device-id specified? */
        if (StringTools.isBlank(devID)) {
            throw new DBNotFoundException("Device-ID not specified for account: " + acctID);
        }

        /* transport-id specified? */
        if (StringTools.isBlank(xportID)) {
            throw new DBNotFoundException("Transport-ID not specified for account: " + acctID);
        }

        /* get/create */
        Transport xport = null;
        Transport.Key xportKey = new Transport.Key(acctID, devID, xportID);
        if (!xportKey.exists(DBReadWriteMode.READ_WRITE)) {
            if (create) {
                xport = xportKey._getDBRecord();
                xport.setAccount(account);
                xport.setCreationDefaultValues();
                return xport; // not yet saved!
            } else {
                throw new DBNotFoundException("Transport-ID does not exists: " + xportKey);
            }
        } else
        if (create) {
            // we've been asked to create the Transport, and it already exists
            throw new DBAlreadyExistsException("Transport-ID already exists '" + xportKey + "'");
        } else {
            xport = Transport.getTransport(account, devID, xportID);
            if (xport == null) {
                throw new DBException("Unable to read existing Transport: " + xportKey);
            }
            return xport;
        }
        
    }
    
    // ------------------------------------------------------------------------

    /* create Transport */
    public static Transport createNewTransport(Account account, String devID, String xportID, String uniqueID)
        throws DBException
    {
        if ((account != null) && !StringTools.isBlank(devID) && !StringTools.isBlank(xportID)) {
            Transport xport = Transport.getTransport(account, devID, xportID, true); // does not return null
            if ((uniqueID != null) && !uniqueID.equals("")) {
                xport.setUniqueID(uniqueID);
            }
            xport.save();
            return xport;
        } else {
            throw new DBException("Invalid Account/TransportID specified");
        }
    }
    
    /* create Transport from Device */
    public static Transport createNewTransport(Device device, String xportID)
        throws DBException
    {
        
        /* invalid device */
        if (device == null) {
            throw new DBNotFoundException("Invalid Device specified");
        }
        
        /* invalid TransportID */
        if (StringTools.isBlank(xportID)) {
            throw new DBNotFoundException("Invalid TransportID specified");
        }

        /* Transport already defined for Device? */
        DataTransport dt = device.getDataTransport();
        if (dt instanceof Transport) {
            throw new DBAlreadyExistsException("Device already has a defined Transport");
        }

        /* create Transport from Device default DataTransport */
        Account account  = device.getAccount();
        String  devID    = device.getDeviceID();
        Transport xport  = Transport.getTransport(account, devID, xportID, true);   // does not return null
        xport.setUniqueID(              dt.getUniqueID()          );
        xport.setDescription(           dt.getDescription()       );
        xport.setDeviceCode(            dt.getDeviceCode()        );
        xport.setDeviceType(            dt.getDeviceType()        );
        xport.setSerialNumber(          dt.getSerialNumber()      );
        xport.setSimPhoneNumber(        dt.getSimPhoneNumber()    );
        xport.setSimID(                 dt.getSimID()             );
        xport.setSmsEmail(              dt.getSmsEmail()          );
        xport.setImeiNumber(            dt.getImeiNumber()        );
        xport.setLastInputState(        dt.getLastInputState()    );
        xport.setLastOutputState(       dt.getLastOutputState()   );
        xport.setIgnitionIndex(         dt.getIgnitionIndex()     );
        xport.setCodeVersion(           dt.getCodeVersion()       );
        xport.setFeatureSet(            dt.getFeatureSet()        );
        xport.setIpAddressValid(        dt.getIpAddressValid()    );
        xport.setIpAddressCurrent(      dt.getIpAddressCurrent()  );
        xport.setLastTotalConnectTime(  dt.getLastTotalConnectTime());
        xport.setLastDuplexConnectTime( dt.getLastDuplexConnectTime());
        // -- OpenDMTP
        xport.setSupportsDMTP(          dt.getSupportsDMTP()      );
        xport.setSupportedEncodings(    dt.getSupportedEncodings());
        xport.setUnitLimitInterval(     dt.getUnitLimitInterval() );
        xport.setMaxAllowedEvents(      dt.getMaxAllowedEvents()  );
        xport.setTotalProfileMask(      dt.getTotalProfileMask()  );
        xport.setTotalMaxConn(          dt.getTotalMaxConn()      );
        xport.setTotalMaxConnPerMin(    dt.getTotalMaxConnPerMin());
        xport.setDuplexProfileMask(     dt.getDuplexProfileMask() );
        xport.setDuplexMaxConn(         dt.getDuplexMaxConn()     );
        xport.setDuplexMaxConnPerMin(   dt.getDuplexMaxConnPerMin());
        // -- 
        xport.save();
        device.setTransport(xport);
        return xport;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return list of all Transport IDs owned by the specified Account */
    // -- does not return null
    public static String[] getTransportIDsForAccount(DBReadWriteMode rwMode, String acctID)
        throws DBException
    {
        return Transport._getTransportIDs(rwMode, acctID, null);
    }
    // --
    @Deprecated
    public static String[] getTransportIDsForAccount(String acctID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Transport.getTransportIDsForAccount(rwMode, acctID);
    }

    /* return list of all Transport IDs owned by the specified Account */
    // -- does not return null
    public static String[] getTransportIDsForDevice(DBReadWriteMode rwMode, String acctID, String devID)
        throws DBException
    {
        return Transport._getTransportIDs(rwMode, acctID, devID);
    }
    // --
    @Deprecated
    public static String[] getTransportIDsForDevice(String acctID, String devID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Transport.getTransportIDsForDevice(rwMode, acctID, devID);
    }

    /**
    *** Returns a list of all Transport IDs owned by the specified Account
    *** @param acctID   The account ID
    *** @param devID    The device ID.  May be blank/null to return all deviceIDs for specified accountID.
    *** @return An array of TransportID's matching specified criteria.  Does not return null (may return an empty array)
    **/
    private static String[] _getTransportIDs(DBReadWriteMode rwMode, String acctID, String devID)
        throws DBException
    {

        /* select */
        // -- DBSelect: SELECT * FROM Transport WHERE (accountID='acct') and (deviceID='dev') ORDER BY transportID
        DBSelect<Transport> dsel = new DBSelect<Transport>(Transport.getFactory());
        dsel.setSelectedFields(Transport.FLD_transportID);
        DBWhere dwh = dsel.createDBWhere();
        if (StringTools.isBlank(devID)) {
            dsel.setWhere(dwh.WHERE(
                dwh.EQ(Transport.FLD_accountID, acctID)
                ));
        } else {
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(Transport.FLD_accountID, StringTools.trim(acctID)),
                    dwh.EQ(Transport.FLD_deviceID , StringTools.trim(devID ))
                )
            ));
        }
        dsel.setSelectedFields(Transport.FLD_transportID);
        dsel.setOrderByFields(Transport.FLD_transportID);

        /* return list */
        return Transport._getTransportIDs(rwMode, dsel);

    }

    // --------------------------------

    /* return list of Transport IDs based on specified DBSelect */
    // -- does not return null
    private static String[] _getTransportIDs(DBReadWriteMode rwMode, DBSelect<Transport> dsel)
        throws DBException
    {

        /* invalid DBSelect */
        if (dsel == null) {
            return new String[0];
        }

        /* read Transports for account */
        java.util.List<String> xportList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String xportId = rs.getString(Transport.FLD_transportID);
                xportList.add(xportId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Transport List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return xportList.toArray(new String[xportList.size()]);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets a list of UniqueIDs from Transport entries that have been assigned to an Account
    *** that no longer exists.
    *** May return null if no orphans found.
    *** (READ database assumed)
    **/
    public static Collection<String> getOrphanAccountUniqueIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* account orphan ID list */
        Vector<String> uidOrphanList = null;

        /* DBFactory */
        DBFactory<Transport> tableFact = Transport.getFactory();

        /* Set of all currently defined AccountIDs (non-orphans) */
        Set<String> acctIdSet = new HashSet<String>(Account.getAllAccounts(rwMode));

        /* create DBSelect */
        // -- SELECT accountID,deviceID,transportID,uniqueID from Transport;
        DBSelect<Transport> dsel = new DBSelect<Transport>(tableFact);
        dsel.setSelectedFields(
            Transport.FLD_accountID,
            Transport.FLD_uniqueID );
        dsel.setOrderByFields(Transport.FLD_accountID);

        /* last account */
        String  lastAccountID     = null;
        boolean lastAccountExists = false;

        /* read from table */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String accountID = rs.getString(Transport.FLD_accountID);
                String uniqueID  = rs.getString(Transport.FLD_uniqueID);
                // -- last account
                if ((lastAccountID != null) && lastAccountID.equals(accountID)) {
                    if (!lastAccountExists) {
                        // -- previous matching account did not exist
                        if (uidOrphanList == null) { uidOrphanList = new Vector<String>(); }
                        uidOrphanList.add(uniqueID); 
                    } else {
                        // -- previous matching account did exist
                    }
                    continue;
                }
                // -- look for orphaned account
                if ((acctIdSet != null)? !acctIdSet.contains(accountID) : !Account.exists(accountID)) {
                    // -- this "accountID" does not exist
                    if (uidOrphanList == null) { uidOrphanList = new Vector<String>(); }
                    uidOrphanList.add(uniqueID); 
                    // -- set previous account not found
                    lastAccountID     = accountID;
                    lastAccountExists = false;
                    continue;
                } else {
                    // -- set previous account found
                    lastAccountID     = accountID;
                    lastAccountExists = true;
                    continue;
                }
            }
        } catch (SQLException sqe) {
            throw new DBException("Getting Orphaned UniqueIDs", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return uidOrphanList;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a list of UniqueIDs from Transport entries that have been assigned to a Device
    *** that no longer exists.
    *** May return null if no orphans found.
    *** (READ database assumed)
    **/
    public static Collection<String> getOrphanDeviceUniqueIDs(String accountID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* account orphan ID list */
        Vector<String> uidOrphanList = null;

        /* DBFactory */
        DBFactory<Transport> tableFact = Transport.getFactory();
        
        /* accountID exists? */
        if (!Account.exists(accountID)) {
            return null;
        }

        /* create DBSelect */
        // -- SELECT accountID,deviceID,transportID,uniqueID from Transport;
        DBSelect<Transport> dsel = new DBSelect<Transport>(tableFact); 
        DBWhere dwh = dsel.createDBWhere();
        dsel.setWhere(dwh.WHERE_(
            dwh.EQ(Transport.FLD_accountID, accountID)
        ));
        dsel.setSelectedFields(
            Transport.FLD_accountID  ,
            Transport.FLD_deviceID   ,
            Transport.FLD_transportID,
            Transport.FLD_uniqueID   );
        dsel.setOrderByFields(Transport.FLD_uniqueID);

        /* read from table */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
              //String accountID   = rs.getString(Transport.FLD_accountID);
                String deviceID    = rs.getString(Transport.FLD_deviceID);
                String transportID = rs.getString(Transport.FLD_transportID);
                String uniqueID    = rs.getString(Transport.FLD_uniqueID);
                // -- check for orphaned device within found account
                if (!Device.exists(accountID,deviceID)) {
                    // -- 'accountID' exists, 'deviceID' does not exist
                    if (uidOrphanList == null) { uidOrphanList = new Vector<String>(); }
                    uidOrphanList.add(uniqueID); 
                    continue;
                }
                // -- account/device exists
            }
        } catch (SQLException sqe) {
            throw new DBException("Getting Orphaned UniqueIDs", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return uidOrphanList;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** This method is used by Device Communication Servers to load a Device record based
    *** on a Unique-ID.  Returns null if the Device is found, but the Account or Device are
    *** inactive.
    *** @param prefix   An array of Unique-ID prefixes.
    *** @param modemID  The Unique-ID of the device (ie. IMEI, ESN, Serial#, etc)
    *** @return The loaded Device instance, or null if the Device was not found
    *** @throws DBException if a database error occurs
    **/
    public static Device loadDeviceUniqueID(String prefix[], String modemID)
    {
        Device device = null;

        /* find Device */
        String uniqueID = "";
        try {

            /* load device record */
            if (ListTools.isEmpty(prefix)) {
                uniqueID = modemID;
                device = Transport.loadDeviceByUniqueID(uniqueID);
            } else {
                uniqueID = prefix[0] + modemID;
                for (int u = 0; u < prefix.length; u++) {
                    String pfxid = prefix[u] + modemID;
                    device = Transport.loadDeviceByUniqueID(pfxid);
                    if (device != null) {
                        uniqueID = pfxid;
                        break;
                    }
                }
            }

            /* not found? */
            if (device == null) {
                Print.logWarn("!!!UniqueID not found!: " + uniqueID);
                return null;
            }

            /* inactive? */
            if (!device.getAccount().isActive() || !device.isActive()) {
                String a = device.getAccountID();
                String d = device.getDeviceID();
                Print.logWarn("Account/Device is inactive: " + a + "/" + d + " [" + uniqueID + "]");
                return null;
            }

            /* return device */
            device.setModemID(modemID);
            return device;

        } catch (Throwable dbe) { // DBException
            Print.logError("Exception getting Device: " + uniqueID + " [" + dbe + "]");
            return null;
        }

    }

    /**
    *** This method is used by Device Communication Servers to load a Device record based
    *** on a Unique-ID.  The caller must confirm that the Device and Account are active.
    *** @param uniqId  The Unique-ID of the device (ie. IMEI, ESN, Serial#, etc)
    *** @return The loaded Device instance, or null if the Device was not found
    *** @throws DBException if a database error occurs
    **/
    public static Device loadDeviceByUniqueID(String uniqId)
        throws DBException
    {

        /* invalid id? */
        if (StringTools.isBlank(uniqId)) {
            // -- not likely to occur
            Print.logError("Unique-ID is null!");
            return null; // just say it doesn't exist
        }

        /* lookup UniqueXID entry? */
        if (UniqueXID.isUniqueQueryEnabled()) {
            UniqueXID uniqXp = null;
            try {
                uniqXp = UniqueXID.getUniqueXID(uniqId);
            } catch (DBException dbe) {
                // -- ignore this error
            }
            if (uniqXp != null) {
                String a = uniqXp.getAccountID();
                String t = uniqXp.getTransportID();
                Print.logDebug("Located Transport '"+a+"/"+t+"' via UniqueXID '"+uniqXp+"'");
              //return Transport.loadDeviceByTransportID(a, t);
                return Device.loadDeviceByName(Account.getAccount(a), t, true);
            } else {
                // -- continue below
            }
        }

        /* lookup Transport entry */
        if (Transport.isTransportQueryEnabled()) {
            try {
                Transport xport = Transport.getTransportByUniqueID(uniqId);
                if (xport != null) {
                    // -- a Transport entry was found
                    Print.logDebug("Located Transport '"+xport+"' via UniqueID '"+uniqId+"'");

                    /* update the Transport connect time */
                    try {
                        xport.setLastTotalConnectTime(DateTime.getCurrentTimeSec());
                        xport.update(Transport.FLD_lastTotalConnectTime);
                    } catch (DBException dbe) {
                        Print.logError("Error updating connect time: " + dbe);
                        // -- otherwise ignore this error
                    }

                    /* get the associated/target Device record */
                    Device dev = xport.getTargetDevice(); // getAssocDevice();
                    if (dev != null) {
                        // -- this Transport Device was also found
                        Print.logDebug("Located Device '"+dev+"' (via Transport '"+xport+"')");
                        return dev;
                    } else {
                        // -- this Transport record does not reference a device (error already displayed)
                        // -  we probably should return null here, but instead we will try again using the Device below
                        //return null;
                    }

                }
            } catch (DBException dbe) {
                // -- ignore this error
            }
        }

        /* return device */
        Device device = Device.loadDeviceByUniqueID(uniqId);
        if (device != null) {
            // -- if we are here, then the Device exists and no Transport record references this device
            Print.logDebug("Located Device '"+device+"' (via Device record)");
            return device;
        }

        /* transport/device not found */
        //Print.logWarn("Device not found for UniqueID '"+uniqId+"'");
        return null;

    }

    /**
    *** This method is used by Device Communication Servers to load a Device record based
    *** on a Unique-ID.
    *** @param uniqID  The Unique-ID of the device (ie. IMEI, ESN, Serial#, etc)
    *** @return The loaded Device instance, or null if the Device was not found
    *** @throws DBException if a database error occurs
    **/
    public static Device loadDeviceByUniqueID(byte uniqID[])
        throws DBException
    {
        if (uniqID != null) {
            // first try ASCII
            if (StringTools.isPrintableASCII(uniqID,false)) {
                Device dev = Transport.loadDeviceByUniqueID(StringTools.toStringValue(uniqID));
                if (dev != null) {
                    return dev;
                }
            }
            // then try HEX
            String hexId = StringTools.toHexString(uniqID);
            return Transport.loadDeviceByUniqueID(hexId);
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** This method is used by Device Communication Servers to load a Device record based
    *** on the Account and Transport/Device IDs.
    *** @param accountID  The Account ID of the owning account.
    *** @param xportID    The Transport-ID (or Device-ID in some cases).
    *** @return The loaded Device instance, or null if the Device was not found
    *** @throws DBException if a database error occurs
    **/
    /*
    public static Device loadDeviceByTransportID(String accountID, String xportID)
        throws DBException
    {
        // -- no account/transport specified?
        if (accountID == null) {
            Print.logError("Account-ID is null!");
            return null; // just say it doesn't exist
        } else
        if (xportID == null) {
            // -- not likely to occur
            Print.logError("Device/Transport-ID is null!");
            return null; // just say it doesn't exist
        }
        // -- get account
        Account account = Account.getAccount(accountID); // may throw DBException
        if (account == null) {
            Print.logError("Account-ID does not exist: " + accountID);
            return null;
        }
        // -- load Transport from device
        return Transport.loadDeviceByTransportID(account, xportID);
    }
    */

    /**
    *** This method is used by Device Communication Servers to load a Device record based
    *** on the Account and Transport/Device IDs.
    *** @param account  The Account instance representing the owning account.
    *** @param xportID  The Transport-ID (or Device-ID in some cases).
    *** @return The loaded Device instance, or null if the Device was not found
    *** @throws DBException if a database error occurs
    **/
    /*
    public static Device loadDeviceByTransportID(Account account, String xportID)
        throws DBException
    {
        // -- no account/transport specified?
        if (account == null) {
            Print.logError("Account is null (not found/defined?)!");
            return null; // just say it doesn't exist
        } else 
        if (xportID == null) {
            Print.logError("Device/Transport-ID is null!");
            return null; // just say it doesn't exist
        }
        // -- lookup Transport entry
        if (Transport.isTransportQueryEnabled()) {
            try {
                Transport xport = Transport.getTransport(account, xportID);
                if (xport != null) {
                    // a Transport entry was found
                    Device dev = xport.getTargetDevice(); // getAssocDevice();
                    if (dev != null) {
                        // this Transport Device was also found
                        Print.logInfo("Located Device '"+dev+"' via Transport '"+xport+"'");
                        return dev;
                    } else {
                        // this Transport record does not reference a device (error already displayed)
                        // we probably should return null here, but instead we will try again using the Device below
                        //return null;
                    }
                }
            } catch (DBException dbe) {
                // ignore this error
            }
        }
        // -- return device
        Device dev = Device.loadDeviceByName(account, xportID, true);
        if (dev != null) {
            // -- if we are here, then the Device exists and no Transport record references this device
            Print.logInfo("Located Device '"+dev+"' (using default Device transport)");
            return dev;
        }
        // -- transport/device not found
        Print.logWarn("Device not found: " + account.getAccountID() + "/" + xportID);
        return null;
    }
    */

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // -- return suggested OpenDMTP communication property configuration value
    //public static String getSuggestedDMTPConnectionAttribute(int prop, DataTransport dt)
    //{
    //    switch (prop) {
    //        case PROP_COMM_MAX_CONNECTIONS: {
    //            // com.maxconn=20,12,60
    //            int totmax = dt.getTotalMaxConn();
    //            int dupmax = dt.getDuplexMaxConn();
    //            int interv = dt.getUnitLimitInterval(); // Minutes
    //            return PROP_COMM_MAX_CONNECTIONS_STR+"="+totmax+","+dupmax+","+interv;
    //        }
    //        case PROP_COMM_MIN_XMIT_DELAY: { // standard minimum (seconds)
    //            // com.mindelay=300
    //            int interv = (int)DateTime.MinuteSeconds(dt.getUnitLimitInterval());
    //            int dupmax = dt.getDuplexMaxConn();
    //            int delmin = (int)Math.round((double)interv / (double)dupmax);
    //            if (delmin < 60) { delmin = 60; }
    //            return PROP_COMM_MIN_XMIT_DELAY_STR+"="+delmin;
    //        }
    //        case PROP_COMM_MIN_XMIT_RATE: { // absolute minimum (seconds)
    //            // com.minrate=60
    //            int maxTotalConnPerMin  = dt.getTotalMaxConnPerMin();
    //            int maxDuplexConnPerMin = dt.getDuplexMaxConnPerMin();
    //            int totminrate = (int)Math.round(60.0 / (double)maxTotalConnPerMin);
    //            int dupminrate = (int)Math.round(60.0 / (double)maxDuplexConnPerMin);
    //            int minrate    = (dupminrate > totminrate)? dupminrate : totminrate;
    //            return PROP_COMM_MIN_XMIT_RATE_STR+"="+minrate;
    //        }
    //        case PROP_COMM_MAX_XMIT_RATE: {
    //            // com.maxrate=3600
    //            return PROP_COMM_MAX_XMIT_RATE_STR+"=3600";
    //        }
    //        case PROP_COMM_MAX_DUP_EVENTS: {
    //            // com.maxduplex=10
    //            return PROP_COMM_MAX_DUP_EVENTS_STR+"=10";
    //        }
    //        case PROP_COMM_MAX_SIM_EVENTS: {
    //            // com.maxsimplex=2
    //            return PROP_COMM_MAX_SIM_EVENTS_STR+"=2";
    //        }
    //        default: {
    //            return "";
    //        }
    //    }
    //}

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below

    private static final String ARG_ACCOUNT[]   = new String[] { "account"  , "acct"    };
    private static final String ARG_TRANSPORT[] = new String[] { "transport", "xport"   };
    private static final String ARG_UNIQID[]    = new String[] { "uniqueid" , "unique", "uid" };
    private static final String ARG_DEVICE[]    = new String[] { "device"   , "dev"     };
    private static final String ARG_CREATE[]    = new String[] { "create"               };
    private static final String ARG_EDIT[]      = new String[] { "edit"     , "ed"      };
    private static final String ARG_EDITALL[]   = new String[] { "editall"  , "eda"     };
    private static final String ARG_DELETE[]    = new String[] { "delete"               };
    private static final String ARG_ORPHANS[]   = new String[] { "orphan"   , "orphans" };

    private static String _fmtXPortID(String acctID, String devID, String xportID)
    {
        return acctID + "/" + devID + "/" + xportID;
    }

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + Transport.class.getName() + " {options}");
        Print.logInfo("Common Options:");
        Print.logInfo("  -account=<id>   Acount ID which owns the Transport");
        Print.logInfo("  -transport=<id> Transport ID to create/edit");
        Print.logInfo("  -uniqueid=<id>  Unique ID to create/edit");
        Print.logInfo("  -device=<id>    Device ID from which the Transport is created");
        Print.logInfo("  -create         Create a new Transport");
        Print.logInfo("  -edit[all]      Edit an existing (or newly created) Transport");
        Print.logInfo("  -delete         Delete specified Transport");
        System.exit(1);
    }

    public static void main(String args[])
    {
        DBConfig.cmdLineInit(args,true);  // main
        String acctID  = RTConfig.getString(ARG_ACCOUNT  , "");
        String devID   = RTConfig.getString(ARG_DEVICE   , "");
        String xportID = RTConfig.getString(ARG_TRANSPORT, "");
        String uniqID  = RTConfig.getString(ARG_UNIQID   , "");

        /* account-id specified? */
        if (StringTools.isBlank(acctID)) {
            Print.logError("Account-ID not specified.");
            usage();
        }

        /* get account */
        Account acct = null;
        try {
            acct = Account.getAccount(acctID); // may throw DBException
            if (acct == null) {
                Print.logError("Account-ID does not exist: " + acctID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error loading Account: " + acctID, dbe);
            //dbe.printException();
            System.exit(99);
        }

        /* transport-id specified? */
        //if (StringTools.isBlank(xportID)) {
        //    Print.logError("Transport-ID not specified.");
        //    usage();
        //}

        /* device transport exists? */
        boolean xportExists = false;
        try {
            xportExists = StringTools.isBlank(xportID)? false : Transport.exists(acctID, devID, xportID);
        } catch (DBException dbe) {
            Print.logError("Error determining if Transport exists: " + _fmtXPortID(acctID,devID,xportID));
            System.exit(99);
        }

        /* option count */
        int opts = 0;

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE, false) && !StringTools.isBlank(acctID) && !StringTools.isBlank(xportID)) {
            opts++;
            if (!xportExists) {
                Print.logWarn("Transport does not exist: " + _fmtXPortID(acctID,devID,xportID));
                Print.logWarn("Continuing with delete process ...");
            }
            try {
                Transport.Key xportKey = new Transport.Key(acctID, devID, xportID);
                xportKey.delete(true); // also delete dependencies
                Print.logInfo("Transport deleted: " + _fmtXPortID(acctID,devID,xportID));
                xportExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting Transport: " + _fmtXPortID(acctID,devID,xportID));
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (xportExists) {
                Print.logWarn("Transport already exists: " + _fmtXPortID(acctID,devID,xportID));
            } else
            if (!StringTools.isBlank(devID)) {
                if (!StringTools.isBlank(xportID)) {
                    Print.logError("Transport-ID must not be specified when Device-ID is specified.");
                } else {
                    try {
                        Device dev = Device.getDevice(acct, devID); // null if non-existent
                        if (dev != null) {
                            Transport.createNewTransport(dev, xportID);
                            Print.logInfo("Created Transport (from device): " + _fmtXPortID(acctID,devID,xportID));
                            xportExists = true;
                        } else {
                            Print.logError("Device-ID does not exist: " + devID);
                            System.exit(99);
                        }
                    } catch (DBException dbe) {
                        Print.logError("Error creating Transport: " + _fmtXPortID(acctID,devID,xportID));
                        dbe.printException();
                        System.exit(99);
                    }
                }
            } else
            if (StringTools.isBlank(xportID)) {
                Print.logError("Transport-ID not specified.");
            } else {
                try {
                    Transport.createNewTransport(acct, devID, xportID, uniqID);
                    Print.logInfo("Created Transport: " + _fmtXPortID(acctID,devID,xportID));
                    xportExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating Transport: " + _fmtXPortID(acctID,devID,xportID));
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) {
            opts++;
            if (!xportExists) {
                Print.logError("Transport does not exist: " + _fmtXPortID(acctID,devID,xportID));
            } else {
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL,false);
                    Transport transport = Transport.getTransport(acct, devID, xportID, false); // may throw DBException
                    DBEdit editor = new DBEdit(transport);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing Transport: " + _fmtXPortID(acctID,devID,xportID));
                    dbe.printException();
                }
            }
            System.exit(0);
        }

        /* list orphans (Transport records exists, but Device record does not) */
        if (RTConfig.getBoolean(ARG_ORPHANS,false)) {
            opts++;
            DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
            // -- selection
            DBSelect<Transport> dsel = new DBSelect<Transport>(Transport.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(dwh.EQ(Transport.FLD_accountID,acctID)));
            // -- iterate through records
            DBConnection dbc = null;
            Statement   stmt = null;
            ResultSet     rs = null;
            try {
                long rcdCount = 0L, orphanCount = 0L;
                Print.sysPrintln("Listing orphaned Transport records:");
                dbc  = DBConnection.getDBConnection(rwMode);
                stmt = dbc.execute(dsel.toString());
                rs   = stmt.getResultSet();
                while (rs.next()) {
                    rcdCount++;
                    String rsDevID     = rs.getString(Transport.FLD_deviceID);
                    String transportID = rs.getString(Transport.FLD_transportID);
                    String uniqueID    = rs.getString(Transport.FLD_uniqueID);
                    String accountID   = /*!StringTools.isBlank(assocAcctID)? assocAcctID :*/ acctID;
                    String deviceID    = /*!StringTools.isBlank(assocDevID)?  assocDevID  : transportID */ rsDevID;
                    if (!Device.exists(accountID,deviceID)) {
                        Print.sysPrintln("   Device does not exist ["+transportID+"]: " + accountID + "/" + deviceID);
                        orphanCount++;
                    }
                }
                Print.sysPrintln("   Found %d orphaned records [out of %d]", orphanCount, rcdCount);
            } catch (SQLException sqe) {
                Print.logException("Error checking orphaned TransportIDs", sqe);
            } catch (DBException dbe) {
                Print.logException("Error checking orphaned TransportIDs", dbe);
            } finally {
                if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
                if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
                DBConnection.release(dbc);
            }
            System.exit(0);
        }

        /* no options specified */
        if (opts == 0) {
            Print.logWarn("Missing options ...");
            usage();
        }

    }
    
}
