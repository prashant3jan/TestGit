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
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/04/09  Martin D. Flynn
//     -Integrate DBException
//  2007/01/25  Martin D. Flynn
//     -Moved to "OpenGTS"
//     -Various new fields added
//  2007/02/28  Martin D. Flynn
//     -Default to GeocoderMode=GEOZONE when creating new accounts
//  2007/03/11  Martin D. Flynn
//     -Added 'FLD_distanceUnits' & 'FLD_temperatureUnits'
//  2007/03/25  Martin D. Flynn
//     -Moved to 'org.opengts.db.tables'
//  2007/05/20  Martin D. Flynn
//     -Added 'FLD_privateLabelName'
//  2007/06/13  Martin D. Flynn
//     -Added BLANK_PASSWORD to explicitly support blank passwords
//  2007/07/27  Martin D. Flynn
//     -Added ability to list a specific AccountID
//  2007/08/09  Martin D. Flynn
//     -Set 'accountExists' to true when creating a new account.
//  2007/09/16  Martin D. Flynn
//     -Added PrivateLabel access methods
//     -Integrated DBSelect
//     -Added "getGeocoderModeString" method
//  2007/11/28  Martin D. Flynn
//     -Added '-editall' command-line option to display all fields.
//  2007/12/13  Martin D. Flynn
//     -Added methods to allow customizing "Device", "Device Group", and "Entity"
//      titles (not yet fully implemented).
//  2008/02/04  Martin D. Flynn
//     -Added Volume/Economy conversion methods
//     -Added column 'FLD_volumeUnits'
//     -Custom "Device"/"Device Group"/"Entity" title now supported via AccountString
//  2008/02/11  Martin D. Flynn
//     -Added column 'FLD_autoAddDevices'
//     -Added AccountString support for ID_DEVICE_NEW_DESCRIPTION
//  2008/05/14  Martin D. Flynn
//     -Encorporated 'enum' types for various display units and other enumerated types.
//     -"acct.getGeocoderModeString()" has been replaced with "Account.getGeocoderMode(acct).toString()"
//  2008/05/20  Martin D. Flynn
//     -Displayed text for enumerated types can now be localized.
//  2008/06/20  Martin D. Flynn
//     -Added column 'FLD_economyUnits'
//  2008/09/19  Martin D. Flynn
//     -Added column 'FLD_defaultUser'
//  2009/01/01  Martin D. Flynn
//     -Added command-line option "-desc=" for setting the description when creating Account.
//  2009/01/28  Martin D. Flynn
//     -Unless overridden by PrivateLabel/Runtime configs, the default 'privateLabelName' field
//      value for new Accounts is BasicPrivateLabel.ALL_HOSTS ("*")
//  2009/04/02  Martin D. Flynn
//     -Added 'FLD_retainedEventAge'.
//     -Added check for invalid ID during Account '-list'
//  2010/01/29  Martin D. Flynn
//     -Added "PressureUnits"
//     -Added "hasDeviceLastNotifySince" method
//  2010/04/11  Martin D. Flynn
//     -Added FLD_pressureUnits
//  2010/07/18  Martin D. Flynn
//     -Added "KPG" Economy units
//  2011/01/28  Martin D. Flynn
//     -Added FLD_maximumDevices, FLD_isAccountManager, FLD_managerID
//  2011/03/08  Martin D. Flynn
//     -Added "getFieldValueString"
//  2011/05/13  Martin D. Flynn
//     -Added FLD_totalPingCount, FLD_maxPingCount
//  2011/10/03  Martin D. Flynn
//     -Added "getAddressTitles(...)"
//     -Added FLD_dcsPropertiesID
//  2011/12/06  Martin D. Flynn
//     -Updated "getFieldValueString" to also search for matching table fields.
//  2012/02/03  Martin D. Flynn
//     -Added EconomyUnits "L/100km"
//  2012/04/03  Martin D. Flynn
//     -Added "setCurrentUser"/"getCurrentUser"
//     -Renamed "getFieldValueString" to "getKeyFieldValue", and added title support.
//  2012/05/27  Martin D. Flynn
//     -Fixed NPE in "_getKeyFieldString"
//  2012/06/29  Martin D. Flynn
//     -Check for divide-by-zero in EconomyUnits, when units are "L/100km"
//  2012/08/01  Martin D. Flynn
//     -Added "-findEmail" command-line option
//  2012/03/01  Martin D. Flynn
//     -Changed "-confirm" to "-confirmDelete" for "deleteOldEvents".  
//     -Removed "-confirm" from "countOldEvents".
//     -Added "-account=acct1,acct2,..." support for "deleteOldEvents"/"countOldEvents".
//     -Added expiration date/days account replacement variables. (KEY_EXPIREDATE)
//  2013/05/28  Martin D. Flynn
//     -Changed "-findEMail" option to make email address searches case-insentitive.
//  2013/08/06  Martin D. Flynn
//     -Added FLD_allowNotify
//     -Added "-setPassword=" command-line option.
//  2013/09/23  Martin D. Flynn
//     -Changed sample data "demo" event date from "2010/03/12" to "2013/08/26".
//  2014/03/03  Martin D. Flynn
//     -Added FLD_allowWebService
//  2014/09/16  Martin D. Flynn
//     -Limit upper value for "totalPingCount"/"maxPingCount" to 0xFFFF [2.5.7-B11]
//  2014/12/19  Martin D. Flynn
//     -Added support for global "alwaysAllowWebService" property settings. [2.5.8-B36]
//  2015/05/03  Martin D. Flynn
//     -Fixed "${gtsDiskUsage} to properly display free space.
//  2016/04/06  Martin D. Flynn
//     -Added text replacement key KEY_EVAL to allow evaluating RuleFactory selectors. [2.6.2-B53]
//  2016/12/21  Martin D. Flynn
//     -Added "pressure" to "convertFieldUnits" [2.6.4-B18]
//     -Added "FLD_smtpProperties", removed "FLD_emailProperties" [2.6.4-B23]
//  2018/09/10  GTS Development Team
//     -Added KEY_ACCOUNT_STATUS, with accompanying "GetActiveStatusDescription" method [2.6.6-B60c]
//  2020/02/19  GTS Development Team
//     -Added method for retro-actively updating blank addresses for an account (see "updateBlankEventAddresses")
//     -Added support for listing/deleting Account/DeviceID orphans [2.6.7-B15h]
//     -Field "FLD_smtpProperties", datatype changed from "TYPE_STRING(400)" to "TYPE_TEXT" [2.6.7-B46n]
//     -ELogHOSProvider disabled by default, unless DBConfig.PROP_Account_eLogHOSProviderClass is set [2.6.7-B46q]
// ----------------------------------------------------------------------------
package org.opengts.db.tables;

//import java.util.*; // "Base64" ambiguous
import java.lang.reflect.*;
import java.lang.annotation.*;

import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.math.*;
import java.io.*;
import java.sql.*;
import java.security.Principal;
import java.security.*;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONBeanGetter;
import org.opengts.util.JSON.JSONBeanSetter;
import org.opengts.dbtypes.*;
import org.opengts.dbtools.*;
import org.opengts.dbtools.DBField.DBFieldTemplate;
import org.opengts.geocoder.SlowOperationException;

import org.opengts.db.*;

public class Account
    extends AccountRecord<Account>
    implements UserInformation,
               JSON.JSONBean
{

    // ------------------------------------------------------------------------

    private static      int     NotifyEmailColumnLength         = -1; // FLD_notifyEmail
    private static      int     LastPasswordColumnLength        = -1; // FLD_lastPasswords

    // ------------------------------------------------------------------------

    /* optional columns */
    public static final String  OPTCOLS_AddressFieldInfo        = "startupInit.Account.AddressFieldInfo";
    public static final String  OPTCOLS_MapLegendFieldInfo      = "startupInit.Account.MapLegendFieldInfo";
    public static final String  OPTCOLS_AccountManagerInfo      = "startupInit.Account.AccountManagerInfo";
    public static final String  OPTCOLS_BillingInfo             = "startupInit.Account.BillingInfo";
    public static final String  OPTCOLS_DataPushInfo            = "startupInit.Account.DataPushInfo";
    public static final String  OPTCOLS_ELogHOSInfo             = "startupInit.Account.ELogHOSInfo";
    public static final String  OPTCOLS_MaintFieldInfo          = "startupInit.Account.MaintFieldInfo";
    public static final String  OPTCOLS_PlatinumInfo            = "startupInit.Account.PlatinumInfo";

    // ------------------------------------------------------------------------

    /* common ACL keys (see also 'private.xml') */
    public static final String  ACL_CHANGE_PASSWORD             = "acl.admin.password";
    public static final String  ACL_CHANGE_ACCOUNT              = "acl.admin.account";
    public static final String  ACL_CHANGE_USER                 = "acl.admin.user";

    // ------------------------------------------------------------------------
    // -- "Account" titles

    /**
    *** Gets a String array containing the default singular and plural names for the 
    *** "Account" title (ie. "Account", "Company", etc) 
    **/
    public static String[] GetTitles(Locale loc) 
    {
        I18N i18n = I18N.getI18N(Account.class, loc);
        return new String[] {
            i18n.getString("Account.title.singular", "Account"),
            i18n.getString("Account.title.plural"  , "Accounts"),
        };
    }

    // ------------------------------------------------------------------------
    // -- ActiveStatus

    public enum ActiveStatus implements EnumTools.StringLocale {
        ACTIVE      (I18N.getString(Account.class,"Account.status.active"          ,"Active"      ), false),
        NULL        (I18N.getString(Account.class,"Account.status.null"            ,"Null"        ), false),
        DELETED     (I18N.getString(Account.class,"Account.status.deleted"         ,"Deleted"     ), false),
        INACTIVE    (I18N.getString(Account.class,"Account.status.inactive"        ,"Inactive"    ), false),
        INACTIVE_M  (I18N.getString(Account.class,"Account.status.managerInactive" ,"Inactive(M)" ), true ),
        EXPIRED     (I18N.getString(Account.class,"Account.status.expired"         ,"Expired"     ), false),
        EXPIRED_M   (I18N.getString(Account.class,"Account.status.managerExpired"  ,"Expired(M)"  ), true ),
        SUSPENDED   (I18N.getString(Account.class,"Account.status.suspended"       ,"Suspended"   ), false),
        SUSPENDED_M (I18N.getString(Account.class,"Account.status.managerSuspended","Suspended(M)"), true ),
        ERROR       (I18N.getString(Account.class,"Account.status.error"           ,"Error"       ), false); // unable to determine status
        // ---
        private I18N.Text   aa = null;
        private boolean     mm = false;
        ActiveStatus(I18N.Text a, boolean m)        { aa = a; mm = m; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isManager()                  { return mm; }
        public boolean isActive()                   { return this.equals(ACTIVE); }
        public boolean isNull()                     { return this.equals(NULL); }
        public boolean isDeleted()                  { return this.equals(DELETED); }
        public boolean isInactive()                 { return this.equals(INACTIVE)  || this.equals(INACTIVE_M ); }
        public boolean isExpired()                  { return this.equals(EXPIRED)   || this.equals(EXPIRED_M  ); }
        public boolean isSuspended()                { return this.equals(SUSPENDED) || this.equals(SUSPENDED_M); }
        public boolean isError()                    { return this.equals(ERROR); }
    };

    /**
    *** Gets an enumerated value representing the state of the specificed Account/User.
    *** Does not return null, but may return the enumerated value ActiveStatus.NULL
    *** if the specified Account/User is null.
    **/
    public static ActiveStatus GetActiveStatus(UserInformation userInfo)
    {

        /* check all possible active states */
        try {

            /* userInfo null */
            if (userInfo == null) {
                return ActiveStatus.NULL;
            }
            Account acct = userInfo.getAccount();

            /* account null */
            if (acct == null) {
                return ActiveStatus.NULL;
            }

            /* deleted */
            if (acct.getIsDeleted()) {   // .isDeleted() : n/a
                return ActiveStatus.DELETED;
            }

            /* active */
            if (!acct.getIsActive()) {   // .isActive() : isDeleted
                return ActiveStatus.INACTIVE;
            } else
            if (acct.hasManagerID() && !acct.getIsAccountManagerActive()) { // [2.6.7-B07g]
                return ActiveStatus.INACTIVE_M; // Manager Inactive
            }

            /* expired */
            if (acct.getIsExpired()) {   // .isExpired() : .isDeleted(), !.isActive()
                return ActiveStatus.EXPIRED;
            } else
            if (acct.hasManagerID() && acct.getIsAccountManagerExpired()) { // [2.6.7-B07g]
                return ActiveStatus.EXPIRED_M; // Manager Expired
            }

            /* suspended */
            if (acct.getIsSuspended()) { // .isSuspended() : n/a
                return ActiveStatus.SUSPENDED;
            } else
            if (acct.hasManagerID() && acct.getIsAccountManagerSuspended()) { // [2.6.7-B07g]
                return ActiveStatus.SUSPENDED_M;
            }

            /* check user? */
            if (userInfo instanceof User) {
                User user = (User)userInfo;
                if (user.getIsDeleted()) {   // isDeleted()
                    return ActiveStatus.DELETED;
                } else
                if (user.isExpired()) { // getIsExpired()
                    return ActiveStatus.EXPIRED;
                } else
                if (user.isSuspended()) { // getIsSuspended()
                    return ActiveStatus.SUSPENDED;
                }
            }

            /* finally active */
            return ActiveStatus.ACTIVE;

        } catch (DBException dbe) {

            /* unable to determine status */
            return ActiveStatus.ERROR;

        }

    }

    /**
    *** Gets a String description of the Account Status
    *** ie. "Deleted", "Inactive", "Expired", "Suspended", "Active"
    **/
    public static String GetActiveStatusDescription(Locale loc, UserInformation userInfo)
    {
        return Account.GetActiveStatus(userInfo).toString(loc);
    }

    // --------------------------------

    /**
    *** Gets the enumerated value representing the state of the current Account.
    **/
    public ActiveStatus getActiveStatus()
    {
        return Account.GetActiveStatus(this);
    }

    /**
    *** Gets a String description of the Account Status
    *** ie. "Deleted", "Inactive", "Expired", "Suspended", "Active"
    **/
    public String getActiveStatusDescription(Locale loc)
    {
        return this.getActiveStatus().toString(loc);
    }

    // ------------------------------------------------------------------------

    /* extract account display ID */
    /* OBSOLETE
    public static String SUPER_ACCOUNT_SEPARATOR    = ":";
    public static String getAccountDisplayID(String accountID)
    {
        if (accountID != null) {
            int p = accountID.indexOf(SUPER_ACCOUNT_SEPARATOR);
            if (p >= 0) {
                return accountID.substring(p+1);
            }
        }
        return accountID;
    }
    */

    // ------------------------------------------------------------------------
    // Demo account information
    // This is the section that specifies the date ranges for the sample data found
    // in the "sampleData" directory.
    // Default properties:
    //   DemoAccount.accountName=demo
    //   DemoAccount.deviceNames=demo,demo2
    //   DemoAccount.demo.dateRange=2015/05/27,2015/05/27
    //   DemoAccount.demo2.dateRange=2015/05/27,2015/05/27

    public  static String           PROP_DemoAccount_                   = "DemoAccount.";
    public  static String           PROP_DemoAccount_accountName        = PROP_DemoAccount_ + "accountName";
    public  static String           PROP_DemoAccount_deviceNames        = PROP_DemoAccount_ + "deviceNames";
    public  static String           _PROP_DemoAccount_device_dateRange  = "dateRange";

    private static String           DEFAULT_DEMO_ACCOUNT_ID             = "demo";
    private static String           DEFAULT_DEMO_DEVICE_IDS[]           = new String[] { "demo", "demo2" };
    private static String           DEFAULT_DEMO_DEVICE_DATE_RANGE[]    = new String[] { "2015/05/27", "2015/05/27" };
    // demo : 2015/05/27,15:07:01 to 2015/05/27,16:54:10
    // demo2: 2015/05/27,06:47:01 to 2015/05/27,13:36:42

    public static String GetDemoAccountID()
    { 
        String da = RTConfig.getString(PROP_DemoAccount_accountName, DEFAULT_DEMO_ACCOUNT_ID);
        //Print.logInfo("Demo Account: " + da);
        return da;
    }
    
    public static boolean IsDemoAccount(String accountID)
    {
        if (!StringTools.isBlank(accountID)) {
            return accountID.equals(Account.GetDemoAccountID());
        } else {
            return false;
        }
    }

    public static String[] GetDemoAccountDeviceIDs()
    { 
        String dd[] = RTConfig.getStringArray(PROP_DemoAccount_deviceNames, DEFAULT_DEMO_DEVICE_IDS);
        Print.logInfo("Demo Devices: " + StringTools.join(dd,"|"));
        return dd;
    }
    
    public static boolean IsDemoDevice(String accountID, String deviceID)
    {
        if (Account.IsDemoAccount(accountID) && !StringTools.isBlank(deviceID)) {
            String did[] = Account.GetDemoAccountDeviceIDs();
            if (ListTools.size(did) > 0) {
                for (int i = 0; i < did.length; i++) {
                    if (deviceID.equals(did[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String[] GetDemoDeviceDateRange(String accountID, String deviceID)
    { 
        if (Account.IsDemoDevice(accountID,deviceID)) {
            String key = PROP_DemoAccount_ + deviceID + _PROP_DemoAccount_device_dateRange;
            String dr[] = RTConfig.getStringArray(key, DEFAULT_DEMO_DEVICE_DATE_RANGE);
            Print.logInfo("Demo Device: " + accountID + "/" + deviceID + " Date Range " + StringTools.join(dr,","));
            return dr;
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Default SMS enabled state

    public enum SMSDefaultState {
        FALSE,
        TRUE,
        ACCOUNT
    };

    public static SMSDefaultState GetDefaultSmsEnabledState()
    {
        String smsState = StringTools.trim(RTConfig.getString(DBConfig.PROP_Account_smsEnabled,""));
        if (smsState.equalsIgnoreCase("account")) {
            return SMSDefaultState.ACCOUNT;
        } else
        if (StringTools.isBoolean(smsState,false)) {
            return StringTools.parseBoolean(smsState,false)? 
                SMSDefaultState.TRUE : 
                SMSDefaultState.FALSE;
        } else {
            return SMSDefaultState.ACCOUNT;
        }
    }
    
    public static boolean IsFixedSmsEnabledState()
    {
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE:
            case TRUE:
                return true;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------------
    // -- Temporary account attributes

    public  static final long       DFT_EXPIRATION_SEC      = DateTime.DaySeconds(7);
    public  static final long       MAX_EXPIRATION_SEC      = DateTime.DaySeconds(60);
    public  static final long       MAX_UNCONFIRMED_SEC     = DateTime.HourSeconds(12);
    private static Object           TempAccountLock         = new Object();

    // ------------------------------------------------------------------------
    // -- Account bitwise attributes [FLD_attributeMask]

    public  static final long       ATTR_DeviceReset        = 0x0000000000000001L;  // 0

    /**
    *** Gets the AccountAttribute name for the specified mask bit index
    **/
    public static String GetAttributeName(int ndx)
    {
        switch (ndx) {
            case 0 : return "DeviceReset";
            default: return "?";
        }
    }

    /**
    *** Gets the AccountAttribute bit index for the specified name
    **/
    public static int GetAttributeIndex(String name)
    {
        String n = StringTools.trim(name).toUpperCase();
        switch (name) {
            case "DEVICERESET" : // "DeviceReset"
            case "DEVRESET"    : // "DevReset"
            case "DEVICEREBOOT": // "DeviceReboot"
            case "DEVREBOOT"   : // "DevReboot"
            case "0"           :
                return 0; // ATTR_DeviceReboot
            default: 
                return -1;
        }
    }

    // ------------------------------------------------------------------------
    // -- Password attributes

    public static final int         TEMP_PASSWORD_LENGTH    = 8;
    public static final String      BLANK_PASSWORD          = "*blank*";

    private static PasswordHandler  passwordHandler         = null;

    public static void setDefaultPasswordHandler(PasswordHandler ph)
    {
        Account.passwordHandler = ph;
    }

    public static PasswordHandler getDefaultPasswordHandler()
    {
        if (Account.passwordHandler == null) {
            Account.passwordHandler = new GeneralPasswordHandler();
        }
        return Account.passwordHandler;
    }

    // --------------------------------

    public static PasswordHandler getPasswordHandler(BasicPrivateLabel bpl)
    {
        if (bpl != null) {
            return bpl.getPasswordHandler();
        } else {
            return Account.getDefaultPasswordHandler();
        }
    }

    public static PasswordHandler getPasswordHandler(Account acount)
    {
        return Account.getPasswordHandler(Account.getPrivateLabel(acount));
    }

    // ------------------------------------------------------------------------
    // Timezone

    /**
    *** Gets the default Account timezone
    **/
    public static String GetDefaultTimeZone()
    {
        String tmz = StringTools.trim(RTConfig.getString(DBConfig.PROP_Account_defaultTimeZone,""));
        return !StringTools.isBlank(tmz)? tmz : DateTime.GMT_TIMEZONE;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ELog/HOS provider

    private static ELogHOSProvider elogHosProvider = null;

    /**
    *** Initialize ELogHOSProvider
    **/
    @SuppressWarnings("unchecked")
    private static void initELogHOSProvider()
    {
        if (!Account.hasELogHOSProvider()) {
            // -- requires the following property setting: [2.6.7-B46q]
            // -    Account.eLogHOSProviderClass=org.opengts.opt.atselog.ELogHOSProviderImpl
          //String elogCN = DBConfig.PACKAGE_ATSELOG + ".ELogHOSProviderImpl";
            String elogCN = RTConfig.getString(DBConfig.PROP_Account_eLogHOSProviderClass,null);
            if (!StringTools.isBlank(elogCN)) {
                try {
                    Class<?> elogClass = (Class<ELogHOSProvider>)Class.forName(elogCN);
                    ELogHOSProvider ehp = (ELogHOSProvider)elogClass.newInstance();
                    Account.setELogHOSProvider(ehp);
                    Print.logDebug("Installed ELogHOSProvider: ["+StringTools.className(ehp)+"] v"+ehp.getVersion()+" "+ehp.getDescription());
                } catch (ClassNotFoundException cnfe) {
                    // -- ELogHOSProvider class not found (quietly ignore)
                } catch (ClassCastException cce) {
                    // -- specified class is not a ELogHOSProvider
                    Print.logError("Invalid ELogHOSProvider class: " + elogCN);
                } catch (Throwable th) { // catch all
                    // -- ELogHOSProvider support not present?
                    Print.logError("Unexpected ELogHOSProvider error: " + th);
                }
            } else {
                // -- not configured, quietly ignore
            }
        }
    }

    /**
    *** Sets the ELogHOSProvider
    *** @param elp  The ELogHOSProvider
    **/
    private static void setELogHOSProvider(ELogHOSProvider elp)
    {
        if (elp != null) {
            Account.elogHosProvider = elp;
            Print.logDebug("Account ELogHOSProvider installed: " + StringTools.className(Account.elogHosProvider));
        } else
        if (Account.elogHosProvider != null) {
            Account.elogHosProvider = null;
            Print.logDebug("Account ELogHOSProvider removed.");
        }
    }

    /** 
    *** Returns true if an ELogHOSProvider has been defined
    *** @return True if an ELogHOSProvider has been defined
    **/
    public static boolean hasELogHOSProvider()
    {
        return (Account.elogHosProvider != null);
    }

    /**
    *** Gets the defined ELogHOSProvider
    *** @return The defined ELogHOSProvider
    **/
    public static ELogHOSProvider getELogHOSProvider()
    {
        return Account.elogHosProvider;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // DataPush provider

    private static DataPushProvider dataPushProvider = null;

    /**
    *** Initialize DataPushProvider
    **/
    @SuppressWarnings("unchecked")
    private static void initDataPushProvider()
    {
        if (!Account.hasDataPushProvider()) {
            String pushClassName = RTConfig.getString(DBConfig.PROP_Account_dataPushProviderClass,null);
            if (!StringTools.isBlank(pushClassName)) {
                try {
                    Class<?> pushClass = (Class<DataPushProvider>)Class.forName(pushClassName);
                    DataPushProvider dpp = (DataPushProvider)pushClass.newInstance();
                    Account.setDataPushProvider(dpp);
                    Print.logInfo("Installed DataPushProvider: ["+StringTools.className(dpp)+"] v"+dpp.getVersion()+" "+dpp.getDescription());
                } catch (ClassNotFoundException cnfe) {
                    // -- DataPushProvider class not found 
                    Print.logError("DataPushProvider class not found: " + pushClassName);
                } catch (ClassCastException cce) {
                    // -- specified class is not a DataPushProvider
                    Print.logError("Invalid DataPushProvider class: " + pushClassName);
                } catch (Throwable th) { // catch all
                    // -- DataPushProvider support not present?
                    Print.logError("Unexpected DataPushProvider error: " + th);
                }
            }
        }
    }

    /**
    *** Sets the DataPushProvider
    *** @param dpp  The DataPushProvider
    **/
    private static void setDataPushProvider(DataPushProvider dpp)
    {
        if (dpp != null) {
            Account.dataPushProvider = dpp;
            Print.logDebug("Account DataPushProvider installed: " + StringTools.className(Account.dataPushProvider));
        } else
        if (Account.dataPushProvider != null) {
            Account.dataPushProvider = null;
            Print.logDebug("Account DataPushProvider removed.");
        }
    }

    /** 
    *** Returns true if an DataPushProvider has been defined
    *** @return True if an DataPushProvider has been defined
    **/
    public static boolean hasDataPushProvider()
    {
        return (Account.dataPushProvider != null);
    }

    /**
    *** Gets the defined DataPushProvider
    *** @return The defined DataPushProvider
    **/
    public static DataPushProvider getDataPushProvider()
    {
        return Account.dataPushProvider;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account type [FLD_accountType]

    public enum AccountType implements EnumTools.StringLocale, EnumTools.IntValue {
        TYPE_000    (  0, I18N.getString(Account.class,"Account.type.type000"  ,"Type000"  )), // default
        TYPE_001    (  1, I18N.getString(Account.class,"Account.type.type001"  ,"Type001"  )),
        TYPE_002    (  2, I18N.getString(Account.class,"Account.type.type002"  ,"Type002"  )),
        TYPE_003    (  3, I18N.getString(Account.class,"Account.type.type003"  ,"Type003"  )),
        TYPE_004    (  4, I18N.getString(Account.class,"Account.type.type004"  ,"Type004"  )),
        TYPE_005    (  5, I18N.getString(Account.class,"Account.type.type005"  ,"Type005"  )),
        TYPE_006    (  6, I18N.getString(Account.class,"Account.type.type006"  ,"Type006"  )),
        TYPE_007    (  7, I18N.getString(Account.class,"Account.type.type007"  ,"Type007"  )),
        TYPE_008    (  8, I18N.getString(Account.class,"Account.type.type008"  ,"Type008"  )),
        TYPE_009    (  9, I18N.getString(Account.class,"Account.type.type009"  ,"Type009"  )),
        TYPE_010    ( 10, I18N.getString(Account.class,"Account.type.type010"  ,"Type010"  )),
        TYPE_011    ( 11, I18N.getString(Account.class,"Account.type.type011"  ,"Type011"  )),
        TYPE_020    ( 20, I18N.getString(Account.class,"Account.type.type020"  ,"Type020"  )),
        TYPE_021    ( 21, I18N.getString(Account.class,"Account.type.type021"  ,"Type021"  )),
        TYPE_030    ( 30, I18N.getString(Account.class,"Account.type.type030"  ,"Type030"  )),
        TYPE_031    ( 31, I18N.getString(Account.class,"Account.type.type031"  ,"Type031"  )),
        TEMPORARY   (900, I18N.getString(Account.class,"Account.type.temporary","Temporary")),
        DEMO        (920, I18N.getString(Account.class,"Account.type.demo"     ,"Demo"     )),
        SYSTEM      (999, I18N.getString(Account.class,"Account.type.system"   ,"System"   ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        AccountType(int v, I18N.Text a)             { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isDefault()                  { return this.equals(TYPE_000); }
        public boolean isTemporary()                { return this.equals(TEMPORARY); }
        public boolean isSystem()                   { return this.equals(SYSTEM); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    };

    /**
    *** Returns the defined AccountType for the specified account.
    *** @param a  The account from which the AccountType will be obtained.  
    ***           If null, the default AccountType will be returned.
    *** @return The AccountType
    **/
    public static AccountType getAccountType(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(AccountType.class,a.getAccountType()) : 
            EnumTools.getDefault(AccountType.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Geocode modes: (RG = "Reverse-Geocode) [FLD_geocoderMode]

    public enum GeocoderMode implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE        (0, I18N.getString(Account.class,"Account.geocoder.none"   ,"none"   )),
        GEOZONE     (1, I18N.getString(Account.class,"Account.geocoder.geozone","geozone")),
        PARTIAL     (2, I18N.getString(Account.class,"Account.geocoder.partial","partial")),
        FULL        (3, I18N.getString(Account.class,"Account.geocoder.full"   ,"full"   ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        GeocoderMode(int v, I18N.Text a)       { vv = v; aa = a; }
        public int     getIntValue()           { return vv; }
        public String  toString()              { return aa.toString(); }
        public String  toString(Locale loc)    { return aa.toString(loc); }
        public boolean isNone()                { return this.equals(NONE); } 
        public boolean isGeozone()             { return this.equals(GEOZONE); } 
      //public boolean okGeozone()             { return (this.getIntValue() >= 1); }
        public boolean isPartial()             { return this.equals(PARTIAL); } 
      //public boolean okPartial()             { return (this.getIntValue() >= 2); }
        public boolean isFull()                { return this.equals(FULL); } 
      //public boolean okFull()                { return (this.getIntValue() >= 3); }
        public boolean equals(GeocoderMode gm) { return ((gm != null) && (this.getIntValue() == gm.getIntValue())); }
    };

    /**
    *** Returns the defined GeocoderMode for the specified account.
    *** @param a  The account from which the GeocoderMode will be obtained.  
    ***           If null, the default GeocoderMode will be returned.
    *** @return The GeocoderMode
    **/
    public static GeocoderMode getGeocoderMode(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(GeocoderMode.class,a.getGeocoderMode()) : 
            EnumTools.getDefault(GeocoderMode.class);
    }

    /**
    *** Gets the GeocoderMode enum value for the specified name
    *** @param code The name of the GeocoderMode (one of "none", "geozone", "partial", "full")
    *** @return The GeocoderMode, or GeocoderMode.NONE if the name is invalid
    **/
    public static GeocoderMode getGeocoderMode(String code)
    {
        return Account.getGeocoderMode(code, GeocoderMode.NONE);
    }

    /**
    *** Gets the GeocoderMode enum value for the specified name
    *** @param code The name of the GeocoderMode (one of "none", "geozone", "partial", "full")
    *** @param dft  The default GeocoderMode if the specified name is invalid.
    *** @return The GeocoderMode, or the specified default if the name is invalid
    **/
    public static GeocoderMode getGeocoderMode(String code, GeocoderMode dft)
    {
        return EnumTools.getValueOf(GeocoderMode.class, code, dft);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Speed units & conversion [FLD_speedUnits]

    public enum SpeedUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPH         ( 0, I18N.getString(Account.class,"Account.speed.mph"  ,"mph"  ), GeoPoint.MILES_PER_KILOMETER           ),
        KPH         ( 1, I18N.getString(Account.class,"Account.speed.kph"  ,"km/h" ), 1.0                                    ),
        KNOTS       ( 2, I18N.getString(Account.class,"Account.speed.knots","knots"), GeoPoint.NAUTICAL_MILES_PER_KILOMETER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        SpeedUnits(int v, I18N.Text a, double m)    { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromKPH(double v)     { return v * mm; } // ie. MPH: km/h * mi/km = mi/h
        public double  convertToKPH(double v)       { return v / mm; }
    };

    /**
    *** Returns the defined SpeedUnits for the specified name.
    *** @param name  The name from which the SpeedUnits will be obtained.  
    ***              If not found, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(String name)
    {
        return EnumTools.getValueOf(SpeedUnits.class, name);
    }

    /**
    *** Returns the defined SpeedUnits for the specified account.
    *** @param a  The account from which the SpeedUnits will be obtained.  
    ***           If null, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(SpeedUnits.class,a.getSpeedUnits()) : 
            EnumTools.getDefault(SpeedUnits.class);
    }

    /**
    *** Returns the defined SpeedUnits for the specified user.
    *** @param u  The user from which the SpeedUnits will be obtained.  
    ***           If null, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(User u)
    {
        return (u != null)? 
            EnumTools.getValueOf(SpeedUnits.class,u.getSpeedUnits()) : 
            EnumTools.getDefault(SpeedUnits.class);
    }

    /**
    *** Returns the defined SpeedUnits for the specified user.
    *** @param u  The user from which the SpeedUnits will be obtained.  
    ***           If null, the default SpeedUnits will be returned.
    *** @return The SpeedUnits
    **/
    public static SpeedUnits getSpeedUnits(Account a, User u)
    {
        return (u != null)? 
            Account.getSpeedUnits(u) : 
            Account.getSpeedUnits(a);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Distance units & conversion [FLD_distanceUnits]

    public enum DistanceUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MILES       (0, I18N.getString(Account.class,"Account.distance.miles","Miles"), GeoPoint.MILES_PER_KILOMETER           ),
        KM          (1, I18N.getString(Account.class,"Account.distance.km"   ,"Km"   ), 1.0                                    ),
        NM          (2, I18N.getString(Account.class,"Account.distance.nm"   ,"Nm"   ), GeoPoint.NAUTICAL_MILES_PER_KILOMETER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        DistanceUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int       getIntValue()                { return vv; }
        public I18N.Text getI18NText()                { return aa; }
        public String    toString()                   { return aa.toString(); }
        public String    toString(Locale loc)         { return aa.toString(loc); }
        public double    getMultiplier()              { return mm; } // km to unit
        public boolean   isKM()                       { return this.equals(KM); }
        public boolean   isMiles()                    { return this.equals(MILES); }
        public boolean   isKnots()                    { return this.equals(NM); }
        public double    convertFromKM(double v)      { return v * mm; }   // MILES: km * mi/km = mi
        public double    convertToKM(double v)        { return v / mm; }
    };

    /**
    *** Returns the defined DistanceUnits for the specified name.
    *** @param name  The name from which the DistanceUnits will be obtained.  
    ***              If not found, the default DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(String name)
    {
        return EnumTools.getValueOf(DistanceUnits.class, name);
    }

    /**
    *** Returns the defined DistanceUnits for the specified account.
    *** @param a  The account from which the DistanceUnits will be obtained.  
    ***           If null, the default DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(DistanceUnits.class,a.getDistanceUnits()) : 
            EnumTools.getDefault(DistanceUnits.class);
    }

    /**
    *** Returns the defined DistanceUnits for the specified user.
    *** @param u  The user from which the DistanceUnits will be obtained.  
    ***           If null, the default DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(User u)
    {
        return (u != null)? 
            EnumTools.getValueOf(DistanceUnits.class,u.getDistanceUnits()) : 
            EnumTools.getDefault(DistanceUnits.class);
    }

    /**
    *** Returns the defined DistanceUnits for the specified user.
    *** @param a  The account from which the DistanceUnits will be obtained.  
    ***           If null, the default DistanceUnits will be returned.
    *** @param u  The user from which the DistanceUnits will be obtained.  
    ***           If null, the Account DistanceUnits will be returned.
    *** @return The DistanceUnits
    **/
    public static DistanceUnits getDistanceUnits(Account a, User u)
    {
        return (u != null)? 
            Account.getDistanceUnits(u) : 
            Account.getDistanceUnits(a);
    }
    
    /**
    *** Parses a String containing a distance value into a double value representing kilometers
    **/
    public static double parseDistance(String v, double dft)
    {

        /* trim and check for blank */
        v = StringTools.trim(v);
        if (StringTools.isBlank(v)) {
            return dft;
        }

        /* parse/extract number from String */
        StringTools.FilterNumber fn = new StringTools.FilterNumber(v, Double.class);
        if (!fn.supportsType(Double.class)) {
            // -- should not occur
            return dft;
        }

        /* convert to double */
        double d = StringTools.parseDouble(fn.getValueString(), -1.0);
        if (d < 0.0) {
            return dft;
        }

        /* convert parsed value to kilometers */
        String t = fn.getTrailingString().toLowerCase();
        if (StringTools.isBlank(t)) {
            // -- assume kilometers
            return d;
        } else
        if (t.equals("km") || t.equals("k") || t.equals("km/h") || t.equals("kph")) {
            // -- explicitly kilometers
            return d;
        } else
        if (t.equals("miles") || t.equals("mi") || t.equals("m") || t.equals("mph")) {
            // -- explicitly miles
            return d * GeoPoint.KILOMETERS_PER_MILE;
        } else
        if (t.equals("nm") || t.equals("n") || t.equals("knots")) {
            // -- explicitly nautical miles
            return d * GeoPoint.KILOMETERS_PER_NAUTICAL_MILE;
        } else {
            // -- default to kilometers
            Print.logWarn("Unrecognized distance units: " + t);
            return d;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Altitude units 

    public enum AltitudeUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        FEET        (0, I18N.getString(Account.class,"Account.altitude.feet"  ,"feet"  ), GeoPoint.FEET_PER_METER      ),
        METERS      (1, I18N.getString(Account.class,"Account.altitude.meters","meters"), 1.0                          ),
        MILES       (2, I18N.getString(Account.class,"Account.altitude.miles" ,"miles" ), GeoPoint.MILES_PER_METER     ),
        KM          (3, I18N.getString(Account.class,"Account.altitude.km"    ,"km"    ), GeoPoint.KILOMETERS_PER_METER),
        CUBITS      (4, I18N.getString(Account.class,"Account.altitude.cubits","cubits"), 2.187226597                  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        AltitudeUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public boolean isMeters()                   { return this.equals(METERS); }
        public boolean isFeet()                     { return this.equals(FEET); }
        public double  convertFromMeters(double v)  { return v * mm; }   // FEET: m * m/ft = ft
        public double  convertToMeters(double v)    { return v / mm; }
    };

    /**
    *** Returns the defined AltitudeUnits for the specified account.
    *** @param a  The account from which the AltitudeUnits will be obtained.  
    ***           If null, the default AltitudeUnits will be returned.
    *** @return The AltitudeUnits
    **/
    public static AltitudeUnits getAltitudeUnits(Account a)
    {
        DistanceUnits distUnits = Account.getDistanceUnits(a);
        return (distUnits.isMiles() || distUnits.isKnots())?
            AltitudeUnits.FEET :
            AltitudeUnits.METERS;
    }

    /**
    *** Returns the defined AltitudeUnits for the specified user.
    *** @param u  The user from which the AltitudeUnits will be obtained.  
    ***           If null, the default AltitudeUnits will be returned.
    *** @return The AltitudeUnits
    **/
    public static AltitudeUnits getAltitudeUnits(User u)
    {
        DistanceUnits distUnits = Account.getDistanceUnits(u);
        return (distUnits.isMiles() || distUnits.isKnots())?
            AltitudeUnits.FEET :
            AltitudeUnits.METERS;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Temeprature units & conversion [FLD_temperatureUnits]

    public enum TemperatureUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        F (0, I18N.getString(Account.class,"Account.temperature.f","F")),  // Fahrenheit
        C (1, I18N.getString(Account.class,"Account.temperature.c","C"));  // Celsius (default)
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        TemperatureUnits(int v, I18N.Text a) { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isC()                        { return this.equals(C); }
        public boolean isF()                        { return this.equals(F); }
        public double  convertFromC(double c)       { return this.isF()? ((c * 9.0 / 5.0) + 32.0) : c; }
        public double  convertToC(double c)         { return this.isF()? ((c - 32.0) * 5.0 / 9.0) : c; }
    };

    /**
    *** Returns the defined TemperatureUnits for the specified name.
    *** @param ame  The name from which the TemperatureUnits will be obtained.  
    ***             If not found, the default TemperatureUnits will be returned.
    *** @return The TemperatureUnits
    **/
    public static TemperatureUnits getTemperatureUnits(String name)
    {
        return EnumTools.getValueOf(TemperatureUnits.class, name);
    }

    /**
    *** Returns the defined TemperatureUnits for the specified account.
    *** @param a  The account from which the TemperatureUnits will be obtained.  
    ***           If null, the default TemperatureUnits will be returned.
    *** @return The TemperatureUnits
    **/
    public static TemperatureUnits getTemperatureUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(TemperatureUnits.class,a.getTemperatureUnits()) : 
            EnumTools.getDefault(TemperatureUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Volume units & conversion [FLD_volumeUnits]

    public static final double  LITERS_PER_US_GALLON    = 3.785411784;
    public static final double  US_GALLONS_PER_LITER    = 1.0 / LITERS_PER_US_GALLON; // 0.264172052
    public static final double  LITERS_PER_UK_GALLON    = 4.546090; // Weights and Measures Act of 1985 (Imperial Gallon)
    public static final double  UK_GALLONS_PER_LITER    = 1.0 / LITERS_PER_UK_GALLON; // 0.2199692483
    public static final double  CUBIC_FEET_PER_LITER    = 0.0353146667;

    // ----------

    // Base Unit: Liters
    public enum VolumeUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        US_GALLONS  (0, I18N.getString(Account.class,"Account.volume.usgal"  ,"gal"  ), Account.US_GALLONS_PER_LITER  ),
        LITERS      (1, I18N.getString(Account.class,"Account.volume.liter"  ,"Liter"), 1.0                           ),  // default
        UK_GALLONS  (2, I18N.getString(Account.class,"Account.volume.ukgal"  ,"IG"   ), Account.UK_GALLONS_PER_LITER  ),
        CUBIC_FEET  (3, I18N.getString(Account.class,"Account.volume.cubicFt","ft^3" ), Account.CUBIC_FEET_PER_LITER  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        VolumeUnits(int v, I18N.Text a, double m)   { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromLiters(double v)  { return v * mm; } // ie. US_GALLONS: L * g/L = g
        public double  convertToLiters(double v)    { return v / mm; }
        public boolean isUSGallons()                { return this.equals(US_GALLONS); }
    };

    /**
    *** Returns the defined VolumeUnits for the specified account.
    *** @param a  The account from which the VolumeUnits will be obtained.  
    ***           If null, the default VolumeUnits will be returned.
    *** @return The VolumeUnits
    **/
    public static VolumeUnits getVolumeUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(VolumeUnits.class,a.getVolumeUnits()) : 
            EnumTools.getDefault(VolumeUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Currency [ISO-4217] 

    public enum Currency implements EnumTools.StringLocale { // CurrencyUnits
        USD/*"usd"*/("$"  ,"Dollar","USA"               ),
        AUD/*"aud"*/("$"  ,"Dollar","Australia"         ),
        NZD/*"nzd"*/("$"  ,"Dollar","New Zealand"       ),
        CAD/*"cad"*/("$"  ,"Dollar","Canada"            ),
        GBP/*"gbp"*/("?"  ,"Pound" ,"United Kingdom"    ), // 
        DOP/*"dop"*/("RD$","Peso"  ,"Dominican Republic"),
        EUR/*"eur"*/("?"  ,"Euro"  ,"Europe"            ), // France/Germany/Ireland/Italy/Luxembourg/Spain
        INR/*"inr"*/("?"  ,"Rupee" ,"India"             ),
        MXN/*"mxn"*/("$"  ,"Peso"  ,"Mexico"            ),
        RUB/*"rub"*/("R"  ,"Ruble" ,"Russia"            ),
        SAR/*"sar"*/("SR" ,"Riyal" ,"Saudi Arabia"      ),
        NAD/*"nad"*/("$"  ,"Dollar","Namibia"           );
        // ---
        private String      ss = null;
        private String      dd = null;
        private String      cc = null;
        Currency(String s, String d, String c)  { ss=s; dd=d; cc=c; }
        public String  getCode()                { return this.name().toLowerCase(); }
        public boolean hasSymbol()              { return (!StringTools.isBlank(ss) && !ss.equals("?")); }
        public String  getSymbol()              { return ss; }
        public String  getDescription()         { return dd; }
        public String  getCountry()             { return cc; }
        public String  toString()               { return this.getCode(); }
        public String  toString(Locale loc)     { return this.getCode(); }
    };

    public static Currency GetCurrency(String code)
    {
        return EnumTools.getValueOf(Currency.class, code, Currency.USD);
    }

    // --------------------------------

    /**
    *** Returns the default currency
    *** @return The default currency
    **/
    public static String GetDefaultCurrency()
    {
        String curr = RTConfig.getString(DBConfig.PROP_Account_defaultCurrency, null);
        if (!StringTools.isBlank(curr)) {
            return curr;
        } else {
            return Account.Currency.USD.getCode();
        }
    }

    /**
    *** Returns the currency units for the specified account
    *** @param a  The account for which the currency units is returned
    *** @return The currency units for the specified account
    **/
    public static String getCurrency(Account a) // getCurrencyUnits
    {
        if (a != null) {
            return a.getCurrencyUnits();
        } else {
            return Account.GetDefaultCurrency();
        }
    }

    // --------------------------------

    /**
    *** Returns the default currency symbol
    *** @return The default currency symbol
    **/
    public static String GetDefaultCurrencySymbol()
    {

        /* symbol from property */
        String dftSym = RTConfig.getString(DBConfig.PROP_Account_defaultCurrencySymbol, null);
        if (!StringTools.isBlank(dftSym)) {
            return dftSym;
        }

        /* from default currency */
        String code = Account.GetDefaultCurrency();
        Currency curr = Account.GetCurrency(code);
        String sym = ((curr != null) && curr.hasSymbol())? curr.getSymbol() : null;
        if (!StringTools.isBlank(sym)) {
            return sym;
        } else {
            return code;
        }

    }

    /**
    *** Returns the currency symbol for the specified account
    *** @param a The account for which the currency symbol is returned
    *** @return The currency symbol for the specified account
    **/
    public static String getCurrencySymbol(Account a)
    {
        return Account.GetDefaultCurrencySymbol();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Economy units & conversion [FLD_economyUnits]
    // mpg     = km/L * mi/km * L/g
    // km/L    = (L/100km) * 100((km/L)^2)
    // 100km/L = (km/L) * (1/100)
    // km/L    = 100km/L / (1/100)
    // L/100km = 1 / ((km/L) * (1/100)) = 100 / (km/L)
    // km/L    = (1 / (L/100km)) / (1/100)) = 100 / (L/100km)

    public enum EconomyUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPG     (0, I18N.getString(Account.class,"Account.economy.mpg"    ,"mpg"    ), GeoPoint.MILES_PER_KILOMETER * Account.LITERS_PER_US_GALLON,false),
        KPL     (1, I18N.getString(Account.class,"Account.economy.kpl"    ,"km/L"   ), 1.0                                                        ,false),
        KPG     (2, I18N.getString(Account.class,"Account.economy.kpg"    ,"kpg"    ), Account.LITERS_PER_US_GALLON                               ,false),
        LP100KM (3, I18N.getString(Account.class,"Account.economy.lp100km","L/100km"), 1.0 / 100.0                                                ,true );
        // ---
        private int         vv = 0;     // index
        private I18N.Text   aa = null;  // description
        private double      mm = 1.0;   // conversion
        private boolean     ii = false; // L/100km (inverse)
        EconomyUnits(int v, I18N.Text a, double m, boolean i)  { vv=v; aa=a; mm=m; ii=i; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
      //public double  getMultiplier()              { return mm; }
        public double  convertFromKPL(double v)     { return (v == 0.0)? 0.0 : (ii? (1.0 / (v * mm)) : (v * mm)); }
        public double  convertToKPL(double v)       { return (v == 0.0)? 0.0 : (ii? ((1.0 / v) / mm) : (v / mm)); }
    };

    /**
    *** Returns the defined EconomyUnits for the specified account.
    *** @param a  The account from which the EconomyUnits will be obtained.  
    ***           If null, the default EconomyUnits will be returned.
    *** @return The EconomyUnits
    **/
    public static EconomyUnits getEconomyUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(EconomyUnits.class,a.getEconomyUnits()) : 
            EnumTools.getDefault(EconomyUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Economy units & conversion (natural gas)
    // Km/Kg = Mi/Lb * Km/Mi * Lb/Kg

    public enum EconomyUnitsNG implements EnumTools.StringLocale, EnumTools.IntValue {
        MPLB (0, I18N.getString(Account.class,"Account.economyng.mplb" ,"miles/lb"), GeoPoint.MILES_PER_KILOMETER * Account.KG_PER_LBS),
        KPKG (1, I18N.getString(Account.class,"Account.economyng.kmpkg","km/kg"   ), 1.0);
        // ---
        private int         vv = 0;     // index
        private I18N.Text   aa = null;  // description
        private double      mm = 1.0;   // conversion
        EconomyUnitsNG(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m;; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
      //public double  getMultiplier()              { return mm; }
        public double  convertFromKPKG(double v)    { return (v == 0.0)? 0.0 : (v * mm); }
        public double  convertToKPKG(double v)      { return (v == 0.0)? 0.0 : (v / mm); }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Pressure units & conversion [FLD_pressureUnits]
    // http://www.unitconversion.org/unit_converter/pressure-ex.html

    public static final double  PA_PER_KPA          = 1000.0;                   // Pascals per KiloPascals
    public static final double  PSF_PER_KPA         = 20.885434233;             // Lbs per sq ft
    public static final double  PSI_PER_KPA         = 0.14503773773020923;      // Lbs per sq in
    public static final double  TORR_PER_KPA        = 7.500616827;  // 7.5028   // Torr
    public static final double  MMHG_PER_KPA        = 7.500637554;              // mm mercury
    public static final double  ATM_PER_KPA         = 0.009869233;              // Standard Atmosphere
    public static final double  AT_PER_KPA          = 0.010197162;              // Technical Atmosphere
    public static final double  BAR_PER_PA          = 0.00001;
    public static final double  BAR_PER_KPA         = BAR_PER_PA * PA_PER_KPA;
    public static final double  KPA_PER_BAR         = 1.0 / BAR_PER_KPA;
    public static final double  KPA_PER_PSI         = 1.0 / PSI_PER_KPA;

    public enum PressureUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        KPA         (0, I18N.getString(Account.class,"Account.pressure.kPa" ,"kPa"  ), 1.0                 ),
        PSI         (1, I18N.getString(Account.class,"Account.pressure.psi" ,"psi"  ), Account.PSI_PER_KPA ),
        MMHG        (2, I18N.getString(Account.class,"Account.pressure.mmHg","mmHg" ), Account.MMHG_PER_KPA),
        BAR         (3, I18N.getString(Account.class,"Account.pressure.bar" ,"bar"  ), Account.BAR_PER_KPA );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        PressureUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public double  getMultiplier()              { return mm; }
        public double  convertFromKPa(double v)     { return v * mm; }
        public double  convertToKPa(double v)       { return v / mm; }
    };

    /**
    *** Returns the defined PressureUnits for the specified account.
    *** @param a  The account from which the PressureUnits will be obtained.  
    ***           If null, the default PressureUnits will be returned.
    *** @return The PressureUnits
    **/
    public static PressureUnits getPressureUnits(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(PressureUnits.class,a.getPressureUnits()) : 
            EnumTools.getDefault(PressureUnits.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Mass/Weight/Force units & conversion [FLD_massUnits]

    public static final double  LBS_PER_KG      = 2.20462262185;            // Pounds per Kilogram
    public static final double  KGS_PER_LB      = 1.0 / LBS_PER_KG;         // Kilograms per Pound [0.45359236999975]
    public static final double  KG_PER_LBS      = KGS_PER_LB;               // Kilograms per Pound [0.45359236999975]
    public static final double  LBS_PER_KN      = 224.808943871;            // Pounds per Kilonewton (force)
    public static final double  KN_PER_LBS      = 1.0 / LBS_PER_KN;         // Kilonewtons per Pound (force) [0.0044482216]
    public static final double  KG_PER_KN       = 101.97162129779282;       // Kilograms per Kilonewton (force)
    public static final double  KN_PER_KG       = 1.0 / KG_PER_KN;          // Kilonewton per Kilograms (force) [0.00980665]
    public static final double  KG_PER_MTON     = 1000.0;                   // 1000 kg per metric ton
    public static final double  LBS_PER_ETON    = 2000.0;                   // 2000 lbs per english ton
    public static final double  LBS_PER_MTON    = KG_PER_MTON * LBS_PER_KG; // Pounds per Metric-Ton
    public static final double  KG_PER_ETON     = LBS_PER_ETON * KGS_PER_LB;// Kilograms per English-Ton
    public static final double  ETON_PER_MTON   = KG_PER_MTON / KG_PER_ETON;
    public static final double  MTON_PER_ETON   = 1.0 / ETON_PER_MTON;

    public enum MassUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        KG  (0, I18N.getString(Account.class,"Account.mass.kg", "kg"), 1.0                ),
        LB  (1, I18N.getString(Account.class,"Account.mass.lb", "lb"), Account.LBS_PER_KG ),
        KN  (2, I18N.getString(Account.class,"Account.mass.kn", "kn"), Account.KN_PER_KG  );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        MassUnits(int v, I18N.Text a, double m) { vv=v; aa=a; mm=m; }
        public int     getIntValue()            { return vv; }
        public String  toString()               { return aa.toString(); }
        public String  toString(Locale loc)     { return aa.toString(loc); }
        public double  getMultiplier()          { return mm; }
        public double  convertFromKG(double v)  { return v * mm; }
        public double  convertToKG(double v)    { return v / mm; }
    };

    /**
    *** Returns the defined MassUnits for the specified account.
    *** @param a  The account from which the MassUnits will be obtained.  
    ***           If null, the default MassUnits will be returned.
    *** @return The MassUnits
    **/
    public static MassUnits getMassUnits(Account a)
    {
        //return (a != null)? 
        //    EnumTools.getValueOf(MassUnits.class,a.getMassUnits()) : 
        //    EnumTools.getDefault(MassUnits.class);
        if (a != null) {
            VolumeUnits vu = Account.getVolumeUnits(a);
            MassUnits massUnits = vu.isUSGallons()?  MassUnits.LB : MassUnits.KG;
            return EnumTools.getValueOf(MassUnits.class, massUnits);
        } else {
            return EnumTools.getDefault(MassUnits.class);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Force/Acceleration units & conversion

    public static final double METERS_PER_SEC_SQ_PER_G      = Accelerometer.METERS_PER_SEC_SQ_PER_G;
    public static final double MPSS_PER_G_FORCE             = Accelerometer.METERS_PER_SEC_SQ_PER_G;
    public static final double G_PER_MPSS_FORCE             = Accelerometer.G_PER_MPSS_FORCE;
    public static final double MPH_PER_SEC_PER_MPSS         = Accelerometer.MPH_PER_SEC_PER_MPSS;
    public static final double MPH_PER_SEC_PER_G            = Accelerometer.MPH_PER_SEC_PER_G;

    public enum ForceUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        MPSS     ( 0, I18N.getString(Account.class,"Account.force.metersPerSecSquared","m/ss" ), 1.0                  ),
        CMPSS    ( 1, I18N.getString(Account.class,"Account.force.centimPerSecSquared","cm/ss"), 100.0                ),
        G        ( 5, I18N.getString(Account.class,"Account.force.gForce"             ,"G"    ), G_PER_MPSS_FORCE     ),
        MPHPS    (10, I18N.getString(Account.class,"Account.force.milesPerHourPerSec" ,"mph/s"), MPH_PER_SEC_PER_MPSS );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        ForceUnits(int v, I18N.Text a, double m)         { vv=v; aa=a; mm=m; }
        public int     getIntValue()                     { return vv; }
        public String  toString()                        { return aa.toString(); }
        public String  toString(Locale loc)              { return aa.toString(loc); }
        public double  getMultiplier()                   { return mm; }
        public double  convertFromMetersPerSS(double v)  { return v * mm; }
        public double  convertToMetersPerSS(double v)    { return v / mm; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Area units & conversion

    public static final double SQUARE_METERS_PER_KILOMETER  = 1000000.0;
    public static final double SQUARE_KILOMETERS_PER_METER  = 1.0 / SQUARE_METERS_PER_KILOMETER; // 0.000001
    public static final double SQUARE_METERS_PER_MILE       = 2589988.110336;
    public static final double SQUARE_MILES_PER_METER       = 1.0 / SQUARE_METERS_PER_MILE;      // 0.000000386102159
    public static final double SQUARE_METERS_PER_ACRE       = 4046.8564224;
    public static final double ACRES_PER_SQUARE_METER       = 1.0 / SQUARE_METERS_PER_ACRE;      // 0.000247105381467
    public static final double SQUARE_METERS_PER_FOOT       = 0.09290304;
    public static final double SQUARE_FEET_PER_METER        = 1.0 / SQUARE_METERS_PER_FOOT;      // 10.763910416709722

    public enum AreaUnits implements EnumTools.StringLocale, EnumTools.IntValue {
        SQUARE_METERS ( 0, I18N.getString(Account.class,"Account.area.squareMeters","Sq.m" ), 1.0                    ),
        SQUARE_FEET   ( 1, I18N.getString(Account.class,"Account.area.squareFeet"  ,"Sq.ft"), SQUARE_FEET_PER_METER  ),
        SQUARE_MILES  (10, I18N.getString(Account.class,"Account.area.squareMiles" ,"Sq.mi"), SQUARE_MILES_PER_METER ),
        ACRES         (20, I18N.getString(Account.class,"Account.area.acres"       ,"Acres"), ACRES_PER_SQUARE_METER );
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        private double      mm = 1.0;
        AreaUnits(int v, I18N.Text a, double m)          { vv=v; aa=a; mm=m; }
        public int     getIntValue()                     { return vv; }
        public String  toString()                        { return aa.toString(); }
        public String  toString(Locale loc)              { return aa.toString(loc); }
        public double  getMultiplier()                   { return mm; }
        public double  convertFromSquareMeters(double v) { return v * mm; }
        public double  convertToSquareMeters(double v)   { return v / mm; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Latitude/Longitude display format [FLD_latLonFormat]

    public enum LatLonFormat implements EnumTools.StringLocale, EnumTools.IntValue {
        DEG (0, I18N.getString(Account.class,"Account.latlon.degrees","Degrees"    )),
        DMS (1, I18N.getString(Account.class,"Account.latlon.dms"    ,"Deg:Min:Sec")),
        DM  (2, I18N.getString(Account.class,"Account.latlon.dm"     ,"Deg:Min"    ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        LatLonFormat(int v, I18N.Text a)            { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public boolean isDegrees()                  { return (vv == DEG.getIntValue()); }
        public boolean isDegMinSec()                { return (vv == DMS.getIntValue()); }
        public boolean isDegMin()                   { return (vv == DM.getIntValue());  }
        public String  getFormatType()              { return this.isDegMinSec()?GeoPoint.SFORMAT_DMS:this.isDegMin()?GeoPoint.SFORMAT_DM:GeoPoint.SFORMAT_DEC_5; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        //public String  formatLatitude( double v, Locale loc) { return GeoPoint.formatLatitude( v, this.getFormatType(), loc); }
        //public String  formatLongitude(double v, Locale loc) { return GeoPoint.formatLongitude(v, this.getFormatType(), loc); }
    };

    /**
    *** Returns the defined LatLonFormat for the specified account.
    *** @param a  The account from which the LatLonFormat will be obtained.  
    ***           If null, the default LatLonFormat will be returned.
    *** @return The LatLonFormat
    **/
    public static LatLonFormat getLatLonFormat(Account a)
    {
        return (a != null)? 
            EnumTools.getValueOf(LatLonFormat.class,a.getLatLonFormat()) : 
            EnumTools.getDefault(LatLonFormat.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Global Account specification
    // - Used for Device auto-add

    /**
    *** Gets the global AccountID to which new devices will be automatically added.
    **/
    public static String getGlobalAccountID()
    {
        return RTConfig.getString(DBConfig.PROP_Account_globalAccountID,null);
    }

    /**
    *** Gets the global Account to which new devices will be automatically added.
    **/
    public static Account getGlobalAccount()
    {
        String acctID = Account.getGlobalAccountID();
        if (!StringTools.isBlank(acctID)) {
            try {
                Account acct = Account.getAccount(acctID);
                return acct;
            } catch (DBException dbe) {
                return null;
            }
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Check for reserved AccountID

    /**
    *** Returns true if the specified AccountID is reserved
    **/
    public static boolean IsReservedAccountID(String acctID)
    {

        /* blank is always reserved */
        if (StringTools.isBlank(acctID)) {
            // -- blank always reserved
            return true;
        }

        /* current user is "sysadmin"? */
        String currAcctID = DBRecord.GetCurrentAccount();
        if (AccountRecord.isSystemAdminAccountID(currAcctID)) {
            // -- never reserved for "sysadmin"
            return false;
        }

        /* check for specific AccountIDs per configuration */
        String aid[] = RTConfig.getStringArray(DBConfig.PROP_Account_reservedAccountIDs, null);
        if (ListTools.isEmpty(aid)) {
            // -- no reserved accountID's
            return false;
        }

        /* check for specified accountID in list */
        if (ListTools.containsIgnoreCase(aid,acctID)) {
            return true;
        } 

        /* not reserved */
        return false;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- Account JSONBean implementation that places the Account JSON object
    // -  within another object with key name "Account"

    public JSON.JSONBean getJSONBeanTable() // getAccountBean()
    {
        return new JSON.JSONBean() {
            @JSONBeanGetter(name="Account")
            public Account getAccount() {
                return Account.this;
            }
        };
    }

    // --------------------------------

    private static String[] FieldSortOrder = {
        Account.FLD_accountID,
        Device.FLD_deviceID,
        EventData.FLD_timestamp,
        EventData.FLD_statusCode,
        "statusCodeHex",
        DeviceGroup.FLD_groupID,
        Driver.FLD_driverID,
        "ruleID"/*Rule.FLD_ruleID*/,
        EventData.FLD_latitude,
        EventData.FLD_longitude,
        EventData.FLD_speedKPH,
        EventData.FLD_heading,
        EventData.FLD_altitude,
        EventData.FLD_geozoneID,
        EventData.FLD_address,
        EventData.FLD_odometerKM,
    };

    public static Comparator<JSON._KeyValue> getAccountBeanComparator()
    {
        return new Comparator<JSON._KeyValue>() {
            public int compare(JSON._KeyValue kv1, JSON._KeyValue kv2) {
                String k1 = StringTools.trim(kv1.getKey());
                String k2 = StringTools.trim(kv2.getKey());
                if (k1.equals(k2)) {
                    return 0;
                } else {
                    for (String fld : Account.FieldSortOrder) {
                        if (k1.equalsIgnoreCase(fld)) {
                            return -1;
                        } else
                        if (k2.equalsIgnoreCase(fld)) {
                            return 1;
                        }
                    }
                    return k1.compareTo(k2);
                }
            }
        };
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME                  = "Account";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    // -- Account fields
    public static final String FLD_accountType              = "accountType";
    public static final String FLD_notifyEmail              = "notifyEmail";
    public static final String FLD_allowNotify              = "allowNotify";
    public static final String FLD_speedUnits               = "speedUnits";
    public static final String FLD_distanceUnits            = "distanceUnits";
    public static final String FLD_volumeUnits              = "volumeUnits";
    public static final String FLD_pressureUnits            = "pressureUnits";
    public static final String FLD_economyUnits             = "economyUnits";
    public static final String FLD_temperatureUnits         = "temperatureUnits";
    public static final String FLD_currencyUnits            = "currencyUnits";
    public static final String FLD_fuelCostPerLiter         = "fuelCostPerLiter";
    public static final String FLD_latLonFormat             = "latLonFormat";
    public static final String FLD_geocoderMode             = "geocoderMode";
    public static final String FLD_privateLabelName         = "privateLabelName";
    public static final String FLD_privateLabelJsp          = "privateLabelJsp";
    public static final String FLD_isBorderCrossing         = "isBorderCrossing";      // bcross
    public static final String FLD_retainedEventAge         = "retainedEventAge";
    public static final String FLD_maximumDevices           = "maximumDevices";
    public static final String FLD_totalPingCount           = "totalPingCount";        // total ping count
    public static final String FLD_maxPingCount             = "maxPingCount";          // maximum allowed ping count
    public static final String FLD_autoAddDevices           = "autoAddDevices";        // EXPERIMENTAL (not fully implemented)
    public static final String FLD_dcsPropertiesID          = "dcsPropertiesID";
    public static final String FLD_smsEnabled               = "smsEnabled";
    public static final String FLD_smsProperties            = "smsProperties";
  //public static final String FLD_emailProperties          = "emailProperties";
    public static final String FLD_smtpProperties           = "smtpProperties";
    public static final String FLD_expirationTime           = "expirationTime";
    public static final String FLD_suspendUntilTime         = "suspendUntilTime";
    public static final String FLD_allowWebService          = "allowWebService";
    public static final String FLD_attributeMask            = "attributeMask";
    // -- User fields
    public static final String FLD_defaultUser              = "defaultUser";
    public static final String FLD_password                 = "password";
    public static final String FLD_tempPassword             = "tempPassword";
    public static final String FLD_lastPasswords            = "lastPasswords";
    public static final String FLD_contactName              = "contactName";
    public static final String FLD_contactPhone             = "contactPhone";
    public static final String FLD_contactEmail             = "contactEmail";
    public static final String FLD_timeZone                 = "timeZone";
    public static final String FLD_preferDateFormat         = "preferDateFormat";
    public static final String FLD_preferTimeFormat         = "preferTimeFormat";
    public static final String FLD_passwdChangeTime         = "passwdChangeTime";
    public static final String FLD_passwdQueryTime          = "passwdQueryTime";
    public static final String FLD_lastLoginTime            = "lastLoginTime";
    public static final String FLD_loginMessage             = "loginMessage";
    public static final String FLD_inactiveMessage          = "inactiveMessage";
  //public static final String FLD_newUserRoleID            = "newUserRoleID";
    public static final DBField FieldInfo[] = {
        // -- Account fields
        newField_accountID(true), // key
        new DBField(FLD_accountType         , Integer.TYPE  , DBField.TYPE_UINT16      , "Account Type"              , "edit=2 enum=Account$AccountType"),
        new DBField(FLD_notifyEmail         , String.class  , DBField.TYPE_EMAIL_LIST(), "Notification EMail Address", "edit=2"),
        new DBField(FLD_allowNotify         , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "Allow Notification"        , "edit=2"),
        new DBField(FLD_speedUnits          , Integer.TYPE  , DBField.TYPE_UINT8       , "Speed Units"               , "edit=2 enum=Account$SpeedUnits"),
        new DBField(FLD_distanceUnits       , Integer.TYPE  , DBField.TYPE_UINT8       , "Distance Units"            , "edit=2 enum=Account$DistanceUnits"),
        new DBField(FLD_volumeUnits         , Integer.TYPE  , DBField.TYPE_UINT8       , "Volume Units"              , "edit=2 enum=Account$VolumeUnits"),
        new DBField(FLD_pressureUnits       , Integer.TYPE  , DBField.TYPE_UINT8       , "Pressure Units"            , "edit=2 enum=Account$PressureUnits"),
        new DBField(FLD_economyUnits        , Integer.TYPE  , DBField.TYPE_UINT8       , "Economy Units"             , "edit=2 enum=Account$EconomyUnits"),
        new DBField(FLD_temperatureUnits    , Integer.TYPE  , DBField.TYPE_UINT8       , "Temperature Units"         , "edit=2 enum=Account$TemperatureUnits"),
        new DBField(FLD_currencyUnits       , String.class  , DBField.TYPE_STRING(8)   , "Currency Units"            , "edit=2 enum=Account$Currency"),
        new DBField(FLD_fuelCostPerLiter    , Double.TYPE   , DBField.TYPE_DOUBLE      , "Fuel Cost Per Liter"       , "edit=2"),
        new DBField(FLD_latLonFormat        , Integer.TYPE  , DBField.TYPE_UINT8       , "Latitude/Longitude Format" , "edit=2 enum=Account$LatLonFormat"),
        new DBField(FLD_geocoderMode        , Integer.TYPE  , DBField.TYPE_UINT8       , "Geocoder Mode"             , "edit=2 enum=Account$GeocoderMode"),
        new DBField(FLD_privateLabelName    , String.class  , DBField.TYPE_STRING(32)  , "PrivateLabel Name"         , "edit=2 editor=privateLabel"),
      //new DBField(FLD_privateLabelJsp     , String.class  , DBField.TYPE_STRING(32)  , "PrivateLabel JSP"          , "edit=2"),
        new DBField(FLD_isBorderCrossing    , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "BorderCrossing Enabled"    , "edit=2"),
        new DBField(FLD_retainedEventAge    , Long.TYPE     , DBField.TYPE_UINT32      , "Retained Event Age (sec)"  , "edit=2"),
        new DBField(FLD_maximumDevices      , Long.TYPE     , DBField.TYPE_INT32       , "Maximum number of devices" , "edit=2"),
        new DBField(FLD_totalPingCount      , Integer.TYPE  , DBField.TYPE_UINT16      , "Total 'Ping' Count"        , ""),
        new DBField(FLD_maxPingCount        , Integer.TYPE  , DBField.TYPE_UINT16      , "Maximum 'Ping' Count"      , "edit=2"),
        new DBField(FLD_autoAddDevices      , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "AutoAdd Devices"           , "edit=2"),
        new DBField(FLD_dcsPropertiesID     , String.class  , DBField.TYPE_STRING(32)  , "DCS Properties ID"         , "edit=2"),
        new DBField(FLD_smsEnabled          , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "SMS Enabled"               , "edit=2"),
        new DBField(FLD_smsProperties       , String.class  , DBField.TYPE_STRING(400) , "SMS Properties"            , "edit=2"),
      //new DBField(FLD_emailProperties     , String.class  , DBField.TYPE_STRING(250) , "EMail Properties"          , "edit=2"),
        new DBField(FLD_smtpProperties      , String.class  , DBField.TYPE_TEXT        , "SMTP Properties"           , "edit=2"),
        new DBField(FLD_expirationTime      , Long.TYPE     , DBField.TYPE_UINT32      , "Expiration Time"           , "format=time"),
        new DBField(FLD_suspendUntilTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Suspend Until Time"        , "format=time"),
        new DBField(FLD_allowWebService     , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "Allow Web-Service"         , "edit=2"),
        new DBField(FLD_attributeMask       , Long.TYPE     , DBField.TYPE_INT64       , "Attributes Mask"           , "edit=2"),
        // -- User fields
        new DBField(FLD_defaultUser         , String.class  , DBField.TYPE_USER_ID()   , "Default User ID"           , "edit=2"),
        new DBField(FLD_password            , String.class  , DBField.TYPE_STRING(32)  , "Password"                  , "edit=2 editor=password"),
        new DBField(FLD_tempPassword        , String.class  , DBField.TYPE_STRING(32)  , "Temporary Password"        , "edit=2 editor=password"),
        new DBField(FLD_lastPasswords       , String.class  , DBField.TYPE_STRING(300) , "Prior Passwords"           , "edit=2"),
        new DBField(FLD_contactName         , String.class  , DBField.TYPE_STRING(64)  , "Contact Name"              , "edit=2 utf8=true"),
        new DBField(FLD_contactPhone        , String.class  , DBField.TYPE_STRING(32)  , "Contact Phone"             , "edit=2"),
        new DBField(FLD_contactEmail        , String.class  , DBField.TYPE_STRING(128) , "Contact EMail Address"     , "edit=2 altkey=email"),
        new DBField(FLD_timeZone            , String.class  , DBField.TYPE_STRING(32)  , "Time Zone"                 , "edit=2 editor=timeZone"),
        new DBField(FLD_preferDateFormat    , String.class  , DBField.TYPE_STRING(16)  , "Date Format"               , "edit=2"),
        new DBField(FLD_preferTimeFormat    , String.class  , DBField.TYPE_STRING(16)  , "Time Format"               , "edit=2"),
        new DBField(FLD_passwdChangeTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Change Time" , "format=time"),
        new DBField(FLD_passwdQueryTime     , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Query Time"  , "format=time"),
        new DBField(FLD_lastLoginTime       , Long.TYPE     , DBField.TYPE_UINT32      , "Last Login Time"           , "format=time"),
        new DBField(FLD_loginMessage        , String.class  , DBField.TYPE_TEXT        , "Login Message"             , "edit=2"),
        new DBField(FLD_inactiveMessage     , String.class  , DBField.TYPE_TEXT        , "Inactive Message"          , "edit=2"),
      //new DBField(FLD_newUserRoleID       , String.class  , DBField.TYPE_ROLE_ID()   , "Default New User Role"     , "edit=2"),
        // -- Common fields
        newField_deletedTime(), // getDeletedTime, getIsDeleted
        newField_isActive(), // getIsActive
        newField_displayName(),
        newField_description(),
        newField_notes(),
        newField_lastUpdateTime(),
        newField_lastUpdateAccount(true),
        newField_lastUpdateUser(true),
        newField_creationTime(),
    };
    
    // -- Address fields
    // -  [OPTCOLS_AddressFieldInfo] startupInit.Account.AddressFieldInfo=true
    public static final String FLD_addressLine1             = "addressLine1";           // address line 1
    public static final String FLD_addressLine2             = "addressLine2";           // address line 2
    public static final String FLD_addressLine3             = "addressLine3";           // address line 3
    public static final String FLD_addressCity              = "addressCity";            // address city
    public static final String FLD_addressState             = "addressState";           // address state/province
    public static final String FLD_addressPostalCode        = "addressPostalCode";      // address postal code
    public static final String FLD_addressCountry           = "addressCountry";         // address country
    public static final DBField AddressFieldInfo[] = {
        new DBField(FLD_addressLine1        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 1"              , "edit=2 utf8=true"),
        new DBField(FLD_addressLine2        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 2"              , "edit=2 utf8=true"),
        new DBField(FLD_addressLine3        , String.class      , DBField.TYPE_STRING(70)   , "Address Line 3"              , "edit=2 utf8=true"),
        new DBField(FLD_addressCity         , String.class      , DBField.TYPE_STRING(50)   , "Address City"                , "edit=2 utf8=true"),
        new DBField(FLD_addressState        , String.class      , DBField.TYPE_STRING(50)   , "Address State/Province"      , "edit=2 utf8=true"),
        new DBField(FLD_addressPostalCode   , String.class      , DBField.TYPE_STRING(20)   , "Address Postal Code"         , "edit=2 utf8=true"),
        new DBField(FLD_addressCountry      , String.class      , DBField.TYPE_STRING(20)   , "Address Country"             , "edit=2 utf8=true"),
    };

    // -- Map fields
    // -  [OPTCOLS_MapLegendFieldInfo] startupInit.Account.MapLegendFieldInfo=true
    public static final String FLD_mapLegendDevice          = "mapLegendDevice";        // Device Map Legend
    public static final String FLD_mapLegendGroup           = "mapLegendGroup";         // DeviceGroup Map Legend
    public static final String FLD_mapAutoUpdate            = "mapAutoUpdate";          // Map Auto-Uupdate Properties
    public static final String FLD_routeSegColorSel         = "routeSegColorSel";       // Map RouteSegmentColor selector
    public static final DBField MapLegendFieldInfo[] = {
        new DBField(FLD_mapLegendDevice      , String.class      , DBField.TYPE_TEXT       , "Device Map Legend"           , "edit=2 utf8=true"),
        new DBField(FLD_mapLegendGroup       , String.class      , DBField.TYPE_TEXT       , "DeviceGroup Map Legend"      , "edit=2 utf8=true"),
        new DBField(FLD_mapAutoUpdate        , String.class      , DBField.TYPE_STRING(100), "Map Auto-update Properties"  , "edit=2"),
        new DBField(FLD_routeSegColorSel     , String.class      , DBField.TYPE_STRING(100), "Route Segment Color selector", "edit=2"), // RouteSegmentColor
    };

    // -- Account Manager Fields
    // -  [OPTCOLS_AccountManagerInfo] startupInit.Account.AccountManagerInfo=true
    public static final String FLD_isAccountManager         = "isAccountManager";
    public static final String FLD_managerID                = "managerID";
    public static final DBField AccountManagerInfo[]        = {
        new DBField(FLD_isAccountManager     , Boolean.TYPE      , DBField.TYPE_BOOLEAN    , "Is Account Manager"          , "edit=2"),
        new DBField(FLD_managerID            , String.class      , DBField.TYPE_ID()       , "Manager ID"                  , "edit=2 altkey=manager"),
    };
    
    // -- Billing/Invoice fields
    // -  [OPTCOLS_BillingInfo] startupInit.Account.BillingInfo=true
    public static final String FLD_costPerDeviceActive      = "costPerDeviceActive";   // per FLD_currencyUnits
    public static final String FLD_costPerDeviceInactive    = "costPerDeviceInactive"; // per FLD_currencyUnits
  //public static final String FLD_costServerUsage          = "costServerUsage";       // per FLD_currencyUnits
    public static final DBField BillingInfo[]               = {
        new DBField(FLD_costPerDeviceActive  , Double.TYPE      , DBField.TYPE_DOUBLE      , "Cost per Active Device"      , "edit=2"),
        new DBField(FLD_costPerDeviceInactive, Double.TYPE      , DBField.TYPE_DOUBLE      , "Cost per Inactive Device"    , "edit=2"),
    };

    // -- Data Request/Push Fields
    // -  [OPTCOLS_DataPushInfo] startupInit.Account.DataPushInfo=true
    public static final String FLD_requestPassCode          = "requestPassCode";        // data request passcode
    public static final String FLD_requestIPAddress         = "requestIPAddress";       // valid request IP address block
    public static final String FLD_dataPushURL              = "dataPushURL";            // data push URL
    public static final String FLD_lastDataRequestTime      = "lastDataRequestTime";    // timestamp of last data request
    public static final String FLD_lastDataPushTime         = "lastDataPushTime";       // timestamp of last data push
    public static final DBField DataPushInfo[]              = {
        new DBField(FLD_requestPassCode     , String.class      , DBField.TYPE_STRING(32)  , "Request Passcode"            , "edit=2"),
        new DBField(FLD_requestIPAddress    , DTIPAddrList.class, DBField.TYPE_STRING(128) , "Valid Request IP Addresses"  , "edit=2"),
        new DBField(FLD_dataPushURL         , String.class      , DBField.TYPE_STRING(240) , "Data Push URL (destination)" , "edit=2"),
        new DBField(FLD_lastDataRequestTime , Long.TYPE         , DBField.TYPE_UINT32      , "Last Data Request Time"      , "format=time"),
        new DBField(FLD_lastDataPushTime    , Long.TYPE         , DBField.TYPE_UINT32      , "Last Data Push Time (millis)", "format=time"),
    };

    // -- ELog/HOS fields
    // -  [OPTCOLS_ELogHOSInfo] startupInit.Account.ELogHOSInfo=true
    public static final String FLD_eLogEnabled              = "eLogEnabled";           // ELog/HOS enabled 
    public static final String FLD_eLogAccountID            = "eLogAccountID";         // ELog/HOS account/partner ID
    public static final String FLD_eLogUsername             = "eLogUsername";          // ELog/HOS username
    public static final String FLD_eLogPassword             = "eLogPassword";          // ELog/HOS password
  //public static final String FLD_eLogLastTokenIndex       = "eLogLastTokenIndex";    // ELog/HOS last SSO token index
  //public static final String FLD_eLogMaxTokenIndex        = "eLogMaxTokenIndex";     // ELog/HOS maximum SSO token index
    public static final String FLD_eLogProperties           = "eLogProperties";        // ELog/HOS properties
    public static final DBField ELogHOSInfo[]               = {
        new DBField(FLD_eLogEnabled         , Boolean.TYPE      , DBField.TYPE_BOOLEAN     , "ELog/HOS Enabled"            , "edit=2"),
        new DBField(FLD_eLogAccountID       , String.class      , DBField.TYPE_STRING(32)  , "ELog/HOS Account ID"         , "edit=2"),
        new DBField(FLD_eLogUsername        , String.class      , DBField.TYPE_STRING(32)  , "ELog/HOS Username"           , "edit=2"),
        new DBField(FLD_eLogPassword        , String.class      , DBField.TYPE_STRING(32)  , "ELog/HOS Password"           , "edit=2 editor=password"),
      //new DBField(FLD_eLogLastTokenIndex  , Long.TYPE         , DBField.TYPE_INT64       , "ELog/HOS Last Token Index"   , ""),
      //new DBField(FLD_eLogMaxTokenIndex   , Long.TYPE         , DBField.TYPE_INT64       , "ELog/HOS Max Token Index"    , ""),
        new DBField(FLD_eLogProperties      , String.class      , DBField.TYPE_STRING(200) , "ELog/HOS Properties"         , "edit=2"),
    };

    // -- Maintenance fields [OPTCOLS_MaintFieldInfo]
    // -  startupInit.Account.MaintFieldInfo=true
    public static final String FLD_maintLabelKM_            = "maintLabelKM";          // odometer maint #X label/description
    private static      DBField[] MaintFields_Odom_ = null;
    public static final DBField[] MaintFields_Odom() {  // [2.6.6-B61c]
        // -- function required to delay execution of "Device.getMaintCountKM()" until RTConfig has initialized.
        if (MaintFields_Odom_ == null) {
            MaintFields_Odom_ = DBField.CreateDBFieldArray(
                Device.getMaintCountKM(), // <-- RTConfig init required
                new DBFieldTemplate(FLD_maintLabelKM_, String.class, DBField.TYPE_STRING(40), I18N.getString(Account.class,"Account.fld.maintLabelKM","#{0} Maint Label (Odom)"      ), "edit=2")
                );
        }
        return MaintFields_Odom_;
    }
    public static final String FLD_maintLabelHR_            = "maintLabelHR";          // hours maint #X label/description
    private static      DBField[] MaintFields_Hours_ = null;
    public static final DBField[] MaintFields_Hours() {  // [2.6.6-B61c]
        // -- function required to delay execution of "Device.getMaintCountHR()" until RTConfig has initialized.
        if (MaintFields_Hours_ == null) {
            MaintFields_Hours_ = DBField.CreateDBFieldArray(
                Device.getMaintCountHR(), // <-- RTConfig init required
                new DBFieldTemplate(FLD_maintLabelHR_, String.class, DBField.TYPE_STRING(40), I18N.getString(Account.class,"Account.fld.maintLabelHR","#{0} Maint Label (Hours)"     ), "edit=2")
                );
        }
        return MaintFields_Hours_;
    }
    public static final String FLD_maintLabelFT_            = "maintLabelFT";          // fixed-time maint #X label/description
    private static      DBField[] MaintFields_FixedTime_ = null;
    public static final DBField[] MaintFields_FixedTime() {  // [2.6.7-B03]
        // -- function required to delay execution of "Device.getMaintCountFT()" until RTConfig has initialized.
        if (MaintFields_FixedTime_ == null) {
            MaintFields_FixedTime_ = DBField.CreateDBFieldArray(
                Device.getMaintCountFT(), // <-- RTConfig init required
                new DBFieldTemplate(FLD_maintLabelFT_, String.class, DBField.TYPE_STRING(40), I18N.getString(Account.class,"Account.fld.maintLabelFT","#{0} Maint Label (Fixed-Time)"), "edit=2")
                );
        }
        return MaintFields_FixedTime_; // fixed [2.6.7-B03e]
    }

    // -- Platinum Edition fields
    // -  [OPTCOLS_PlatinumInfo] startupInit.Account.PlatinumInfo=true
    public static final String FLD_isDispatcher             = "isDispatcher";
    public static final DBField PlatinumInfo[]              = {
        new DBField(FLD_isDispatcher        , Boolean.TYPE      , DBField.TYPE_BOOLEAN      , "Is Dispatcher Account"       , "edit=2"),
    };

    /* key class */
    public static class Key
        extends AccountKey<Account>
    {
        public Key() {
            super();
        }
        public Key(String acctId) {
            super.setKeyValue(FLD_accountID, ((acctId != null)? acctId.trim().toLowerCase() : ""));
        }
        public DBFactory<Account> getFactory() {
            return Account.getFactory();
        }
    }

    /* factory constructor */
    protected static DBFactory<Account> factory = null;
    public static DBFactory<Account> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                Account.TABLE_NAME(), 
                Account.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                Account.class, 
                Account.Key.class,
                true/*editable*/, true/*viewable*/);
            // -- init data push handlers
            Account.initELogHOSProvider();
            Account.initDataPushProvider();
            // -- FLD_notifyEmail max length
            DBField emailFld = factory.getField(FLD_notifyEmail);
            Account.NotifyEmailColumnLength = (emailFld != null)? emailFld.getStringLength() : 0;
            // -- FLD_lastPasswords max length
            DBField lastPwFld = factory.getField(FLD_lastPasswords);
            Account.LastPasswordColumnLength = (lastPwFld != null)? lastPwFld.getStringLength() : 0;
        }
        return factory;
    }

    /* Bean instance */
    public Account()
    {
        super();
    }

    /* database record */
    public Account(Account.Key key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(Account.class, loc);
        return i18n.getString("Account.description", 
            "This table defines " +
            "the top level Account specific information."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    // -- JSOBBean testing/debug
    /*
    @JSONBeanGetter(arg="${locale},${%this:contactName}")
    public String getBeanTesterString(String arg)
    {
        return "HelloWorld (String): " + arg;
    }
    @JSONBeanGetter(arg="${locale}")
    public String getBeanTesterLocale(Locale arg)
    {
        return "HelloWorld (Locale): " + arg;
    }
    @JSONBeanGetter(arg="123")
    public String getBeanTesterInt(int arg)
    {
        return "HelloWorld (long): " + arg;
    }
    @JSONBeanGetter(arg="bad")
    public String getBeanTesterIntBad(int arg)
    {
        return "HelloWorld (long): " + arg;
    }
    @JSONBeanGetter(arg="1")
    public String getBeanTesterLong(long arg)
    {
        return "HelloWorld (long): " + arg;
    }
    @JSONBeanGetter(arg="3.14159")
    public String getBeanTesterDouble(double arg)
    {
        return "HelloWorld (double): " + arg;
    }
    @JSONBeanGetter(arg="SUSPENDED")
    public String getBeanTesterEnum(ActiveStatus arg)
    {
        return "HelloWorld (Enum): " + arg;
    }
    @JSONBeanGetter(arg="3")
    public String getBeanTesterEnum2(AccountType arg)
    {
        return "HelloWorld (Enum): " + arg;
    }
    */

    // ------------------------------------------------------------------------

    /**
    *** Gets the default login user-id
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getDefaultUser()
    {
        String v = (String)this.getFieldValue(FLD_defaultUser);
        return (v != null)? v : "";
    }

    /**
    *** Sets the default login user-id
    **/
    public void setDefaultUser(String v)
    {
        this.setFieldValue(FLD_defaultUser, StringTools.trim(v));
    }

    /**
    *** Gets the default login user-id for the specified account
    *** @param acct     The Account
    *** @param rtnAdmin True to return the "admin" user-id if the account default user-id is not defined
    *** @return The default account user-id.
    **/
    public static String getDefaultUser(Account acct, boolean rtnAdmin)
    {
        String userID = (acct != null)? acct.getDefaultUser() : null;
        if (!StringTools.isBlank(userID)) {
            return userID;
        } else
        if (rtnAdmin) {
            return User.getAdminUserID();
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    private UserPrincipal accountPrincipal = null;

    /**
    *** Creates/Returns a Principal instance backed by this account
    *** @param saveUI  Save UserInformation instance (save this Account instance)
    **/
    public Principal getPrincipal(boolean saveUI)
    {
        if (this.accountPrincipal == null) {
            this.accountPrincipal = new UserPrincipal(this,saveUI); 
        }
        return this.accountPrincipal;
    }

    // ------------------------------------------------------------------------

    /** 
    *** Gets the encoded password of this account 
    **/
    public String getPassword()
    {
        String p = (String)this.getFieldValue(FLD_password);
        return (p != null)? p : "";
    }

    /**
    *** Sets the encoded password for this account 
    **/
    public void setPassword(String p)
    {
        this.addLastPassword(this.getPassword());
        this.setFieldValue(FLD_password, ((p != null)? p : ""));
        this.setPasswdChangeTime(DateTime.getCurrentTimeSec());
    }

    /** 
    *** Gets the encoded password of this account 
    **/
    public String getEncodedPassword()
    {
        return this.getPassword();
    }

    /**
    *** Sets the encoded password for this account 
    **/
    public void setEncodedPassword(String p)
    {
        this.setPassword(p);
    }

    // --------

    /** 
    *** Gets the previous encoded passwords
    *** (Comma separated list of Base64 encoded passwords)
    **/
    public String getLastPasswords()
    {
        String p = (String)this.getFieldValue(FLD_lastPasswords);
        return (p != null)? p : "";
    }

    /**
    *** Sets the previous encoded passwords
    *** (Comma separated list of Base64 encoded passwords)
    **/
    public void setLastPasswords(String p)
    {
        //Print.logInfo("Setting last passwords: " + p);
        this.setFieldValue(FLD_lastPasswords, ((p != null)? p : ""));
    }

    /**
    *** Adds a password to the last passwords list
    **/
    public void addLastPassword(String p)
    {
        // -- get number of required unique passwords
        PasswordHandler pwh = Account.getPasswordHandler(this); // non-null
        int reqUniqPass = pwh.getRequiredUniquePasswordCount();
        if (reqUniqPass <= 0) {
            //Print.logInfo("No previous passwords required ...");
            this.setLastPasswords("");
            return;
        }
        // -- 
        java.util.List<String> lpList = new Vector<String>();
        //Print.logInfo("Adding current password: " + p);
        lpList.add(p);
        // --
        java.util.List<String> lpl = Account.decodeLastPasswords(this.getLastPasswords());
        if (!ListTools.isEmpty(lpl)) {
            for (int i = 0; (lpList.size() < reqUniqPass) && (i < lpl.size()); i++) {
                //Print.logInfo("Adding current password: " + lpl.get(i));
                lpList.add(lpl.get(i));
            }
        }
        // --
        String encLastPwds = Account.encodeLastPasswords(lpList,Account.LastPasswordColumnLength);
        this.setLastPasswords(encLastPwds);
    }

    // --------

    /** 
    *** Gets the previous passwords of this account 
    **/
    public static java.util.List<String> decodeLastPasswords(String lp)
    {
        if (StringTools.isBlank(lp)) {
            return null;
        }
        // --
        java.util.List<String> lpList = new Vector<String>();
        String lpArry[] = StringTools.split(lp,',');
        for (int i = 0; i < lpArry.length; i++) {
            String p = lpArry[i];
            if (StringTools.isBlank(p)) { continue; }
            try {
                String s = StringTools.toStringValue(Base64.decode(p));
                lpList.add(s);
            } catch (Base64.Base64DecodeException bde) {
                // -- ignore invalid Base64 encoded password
            }
        }
        return lpList;
    }

    /** 
    *** Sets the previous encoded passwords of this account 
    **/
    public static String encodeLastPasswords(java.util.List<String> lpList, int maxLen)
    {
        // -- "<Base64Pass>,<Base64Pass>,<Base64Pass>,..."
        StringBuffer sb = new StringBuffer();
        if (!ListTools.isEmpty(lpList)) {
            for (String s : lpList) {
                if (!StringTools.isBlank(s)) {
                    String encPass = StringTools.trimTrailing(Base64.encode(s),'=');
                    if ((maxLen <= 0) || ((sb.length() + 1 + encPass.length()) <= maxLen)) {
                        if (sb.length() > 0) { sb.append(","); }
                        sb.append(encPass);
                    } else {
                        // -- password not added, not enough space in column/field
                    }
                }
            }
        }
        return sb.toString();
    }

    // --------

    /**
    *** Gets a list of the last used encoded passwords (including current password)
    **/
    public String[] getLastEncodedPasswords()
    {
        // -- get number of required unique passwords
        PasswordHandler pwh = Account.getPasswordHandler(this); // non-null
        int reqUniqPass = pwh.getRequiredUniquePasswordCount();
        if (reqUniqPass <= 0) {
            return null;
        }
        // -- include current password only
        if (reqUniqPass == 1) {
            return new String[] { this.getEncodedPassword() };
        }
        // -- current/previous passwords
        java.util.List<String> lpList = new Vector<String>();
        lpList.add(this.getEncodedPassword()); // current
        java.util.List<String> lpl = Account.decodeLastPasswords(this.getLastPasswords()); 
        if (!ListTools.isEmpty(lpl)) {
            for (int i = 0; (lpList.size() < reqUniqPass) && (i < lpl.size()); i++) {
                String p = lpl.get(i);
                if (StringTools.isBlank(p)) { continue; }
                lpList.add(p);
            }
        }
        // -- return previous passwords
        return lpList.toArray(new String[lpList.size()]);
    }

    // --------

    /**
    *** Gets the decoded password for this account.
    *** Returns null if password cannot be decoded.
    **/
    public String getDecodedPassword(BasicPrivateLabel contextBPL)
    {
        // -- get BasicPrivateLabel
        BasicPrivateLabel bpl = this.getPrivateLabel(contextBPL);
        // -- decode password
        String pass = Account.decodePassword(bpl, this.getEncodedPassword());
        // -- it is possible that this password cannot be decoded
        return pass; // 'null' if password cannot be decoded
    }

    /**
    *** Encodes and sets the entered password for this account 
    **/
    public void setDecodedPassword(BasicPrivateLabel contextBPL, String enteredPass, boolean isTemp)
    {
        // -- get BasicPrivateLabel
        BasicPrivateLabel bpl = this.getPrivateLabel(contextBPL);
        // -- encode and set password
        String encodedPass = Account.encodePassword(bpl, enteredPass);
        if (!this.getEncodedPassword().equals(encodedPass)) {
            this.setEncodedPassword(encodedPass);
        }
        // -- temporary password?
        if (isTemp) {
            this.setTempPassword(enteredPass);
        } else {
            this.setTempPassword(null); // clear temporary password
        }
    }

    // --------

    /* reset the password */
    // -- does not save the record!
    public String resetPassword(BasicPrivateLabel bpl)
    {
        String enteredPass = Account.createRandomPassword(Account.TEMP_PASSWORD_LENGTH);
        this.setDecodedPassword(bpl, enteredPass, true);
        return enteredPass; // record not yet saved!
    }

    /* check that the specified password is a match for this account */
    public boolean checkPassword(BasicPrivateLabel bpl, String enteredPass, boolean suspend)
    {
        // -- get BasicPrivateLabel
        if (bpl == null) { 
            bpl = this.getPrivateLabel(); 
        }
        // -- check password
        boolean ok = Account._checkPassword(bpl, this, enteredPass);
        if (!ok && suspend) {
            // -- suspend on excessive failed login attempts
            this.suspendOnLoginFailureAttempt(true); // count current login failure
        }
        return ok;
    }

    /* check that the specified password is a match for this account */
    // -- the released CelltracGTS/Server still references this method
    @Deprecated
    public boolean checkPassword(BasicPrivateLabel bpl, String enteredPass)
    {
        return this.checkPassword(bpl, enteredPass, false);
    }

    /* check that the specified password is a match for this account */
    public static boolean _checkPassword(BasicPrivateLabel bpl, UserInformation user, String enteredPass)
    {
        PasswordHandler pwh = Account.getPasswordHandler(bpl); // non-null
        //((GeneralPasswordHandler)pwh).setDebugCheckPassword(true);
        return pwh.checkPassword(user, enteredPass);
    }

    // --------

    /* encode password */
    // -- Convert a clear-text password into a table-encoded password 
    // -  (this may be a one-way encoding).
    public static String encodePassword(BasicPrivateLabel bpl, String enteredPass)
    {
        // -- all passwords are encodable (even if encode(A)==A)
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.encodePassword(enteredPass);
    }

    /* decode password */
    // -- Convert a table-encoded password into a clear-text password
    public static String decodePassword(BasicPrivateLabel bpl, String tablePass)
    {
        // -- this method must return 'null' if table-encoded passwords cannot be decoded
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.decodePassword(tablePass);
    }

    // --------

    /**
    *** Gets the temporary clear-text password of this user.
    **/
    public String getTempPassword()
    {
        String p = (String)this.getFieldValue(FLD_tempPassword);
        return (p != null)? p : "";
    }

    /**
    *** Sets the temporary clear-text password of this user.
    *** This temporary password will be cleared when the regular password is set.
    **/
    public void setTempPassword(String p)
    {
        this.setFieldValue(FLD_tempPassword, ((p != null)? p : ""));
    }

    // --------

    /** 
    *** Update password fields
    **/
    public void updatePasswordFields()
        throws DBException
    {
        this.update(Account.FLD_password, Account.FLD_tempPassword, Account.FLD_lastPasswords);
    }

    // ------------------------------------------------------------------------

    /* return the account type (default AccountType.DEFAULT) */
    @JSONBeanGetter(enumClass="AccountType",ignore="0")
    public int getAccountType()
    {
        Integer v = (Integer)this.getFieldValue(FLD_accountType);
        return (v != null)? v.intValue() : EnumTools.getDefault(AccountType.class).getIntValue();
    }

    /* set the account type */
    public void setAccountType(int v)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v).getIntValue());
    }

    /* set the account type */
    public void setAccountType(AccountType v)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v).getIntValue());
    }

    /* set the account type */
    public void setAccountType(String v, Locale locale)
    {
        this.setFieldValue(FLD_accountType, EnumTools.getValueOf(AccountType.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* return the contact name for this account */
    @JSONBeanGetter(ignore="$blank")
    public String getContactName()
    {
        String v = (String)this.getFieldValue(FLD_contactName);
        return StringTools.trim(v);
    }

    /* set the contact name for this account */
    public void setContactName(String v)
    {
        this.setFieldValue(FLD_contactName, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the contact phone# for this account */
    @JSONBeanGetter(ignore="$blank")
    public String getContactPhone()
    {
        String v = (String)this.getFieldValue(FLD_contactPhone);
        return StringTools.trim(v);
    }

    /* set the contact phone# for this account */
    public void setContactPhone(String v)
    {
        this.setFieldValue(FLD_contactPhone, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the contact email address for this account */
    @JSONBeanGetter(ignore="$blank")
    public String getContactEmail()
    {
        String v = (String)this.getFieldValue(FLD_contactEmail);
        return StringTools.trim(v);
    }

    /* set the contact email address for this account */
    public void setContactEmail(String v)
    {
        this.setFieldValue(FLD_contactEmail, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the notification email address for this account */
    public String getNotifyEmail()
    {
        String v = (String)this.getFieldValue(FLD_notifyEmail);
        return StringTools.trim(v);
    }

    /* set the notification email address for this account */
    public void setNotifyEmail(String v)
    {
        String ne = StringTools.trim(v);
        if ((Account.NotifyEmailColumnLength > 0)           &&
            (ne.length() >= Account.NotifyEmailColumnLength)  ) {
            int newLen = Account.getMaximumNotifyEmailLength();
            ne = ne.substring(0, newLen).trim();
            // Note: MySQL will refuse to insert the record if the data length
            // is greater than the table column length.
        }
        this.setFieldValue(FLD_notifyEmail, ne);
    }

    /**
    *** Gets the maximum Notify Email length
    **/
    public static int getMaximumNotifyEmailLength()
    {
        // -1 so we are not so close to the edge of the cliff
        return Account.NotifyEmailColumnLength - 1;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if notifications are allowed for this Account
    *** @return True if notifications are allowed for this Account
    **/
    public boolean getAllowNotify()
    {
        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_allowNotify);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "Allow Notification" state for this Account
    *** @param v The "Allow Notification" state for this Account
    **/
    public void setAllowNotify(boolean v)
    {
        this.setOptionalFieldValue(FLD_allowNotify, v);
    }

    // ------------------------------------------------------------------------

    private TimeZone timeZone = null;

    /* get TimeZone */
    public static TimeZone getTimeZone(Account account, TimeZone dft)
    {
        return (account != null)? account.getTimeZone(dft) : dft;
    }

    /* get the TimeZone instance for this account */
    // -- return the specified default if no timezone have been specified
    // -  does not return null
    public TimeZone getTimeZone(TimeZone dft)
    {
        if (this.timeZone == null) {
            this.timeZone = DateTime.getTimeZone(this.getTimeZone(), null);
            if (this.timeZone == null) {
                this.timeZone = (dft != null)? dft : DateTime.getGMTTimeZone();
            }
        }
        return this.timeZone;
    }

    /* get the string representation of the TimeZone for this account */
    @JSONBeanGetter()
    public String getTimeZone()
    {
        String v = (String)this.getFieldValue(FLD_timeZone);
        return !StringTools.isBlank(v)? v.trim() : Account.GetDefaultTimeZone();
    }

    /* set the string representation of the timezone for this account */
    public void setTimeZone(String v)
    {
        String tz = StringTools.trim(v);
        if (!StringTools.isBlank(tz)) {
            // validate timezone value?
        }
        this.timeZone = null;
        this.setFieldValue(FLD_timeZone, !StringTools.isBlank(tz)? tz : Account.GetDefaultTimeZone());
    }

    /* return current DateTime (relative the Account TimeZone) */
    public DateTime getCurrentDateTime()
    {
        return new DateTime(this.getTimeZone(null));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Account preferred date format, or blank if undefined
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getPreferDateFormat()
    {
        String v = (String)this.getFieldValue(FLD_preferDateFormat);
        return !StringTools.isBlank(v)? v.trim() : "";
    }

    /**
    *** Sets the Account preferred date format
    **/
    public void setPreferDateFormat(String fmt)
    {
        this.setFieldValue(FLD_preferDateFormat, StringTools.trim(fmt));
    }

    /* get date format */
    @JSONBeanGetter()
    public String getDateFormat()
    {
        String dateFmt = this.getPreferDateFormat();
        if (!StringTools.isBlank(dateFmt)) {
            return dateFmt;
        } else {
            BasicPrivateLabel privLabel = this.getPrivateLabel();
            if (privLabel != null) {
                return privLabel.getDateFormat();
            } else {
                return BasicPrivateLabel.getDefaultDateFormat();
            }
        }
    }

    /* get date format */
    public static String GetDateFormat(Account acct)
    {
        return (acct != null)? acct.getDateFormat() : BasicPrivateLabel.getDefaultDateFormat();
    }

    /* return formated date */
    public String formatDate(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getDateFormat(), tz);
        } else {
            return "";
        }
    }

    // --------------------------------

    /**
    *** Gets the Account preferred time format, or blank if undefined
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getPreferTimeFormat()
    {
        String v = (String)this.getFieldValue(FLD_preferTimeFormat);
        return !StringTools.isBlank(v)? v.trim() : "";
    }

    /**
    *** Sets the Account preferred time format
    **/
    public void setPreferTimeFormat(String fmt)
    {
        this.setFieldValue(FLD_preferTimeFormat, StringTools.trim(fmt));
    }

    /* get time format */
    @JSONBeanGetter()
    public String getTimeFormat()
    {
        String timeFmt = this.getPreferTimeFormat();
        if (!StringTools.isBlank(timeFmt)) {
            return timeFmt;
        } else {
            BasicPrivateLabel privLabel = this.getPrivateLabel();
            if (privLabel != null) {
                return privLabel.getTimeFormat();
            } else {
                return BasicPrivateLabel.getDefaultTimeFormat();
            }
        }
    }

    /* get time format */
    public static String GetTimeFormat(Account acct)
    {
        return (acct != null)? acct.getTimeFormat() : BasicPrivateLabel.getDefaultTimeFormat();
    }

    /* return formated date */
    public String formatTime(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getTimeFormat(), tz);
        } else {
            return "";
        }
    }

    // --------------------------------

    /* get date/time format */
    @JSONBeanGetter()
    public String getDateTimeFormat()
    {
        return this.getDateFormat() + " " + this.getTimeFormat();
    }

    /* get date/time format */
    public static String GetDateTimeFormat(Account acct)
    {
        return Account.GetDateFormat(acct) + " " + Account.GetTimeFormat(acct);
    }

    /* return formated date */
    public String formatDateTime(DateTime dt)
    {
        if (dt != null) {
            TimeZone tz = this.getTimeZone(null);
            return dt.format(this.getDateTimeFormat(), tz);
        } else {
            return "";
        }
    }
    public String formatDateTime(long dt)
    {
        TimeZone tz = this.getTimeZone(null);
        return (new DateTime(dt,tz)).format(this.getDateTimeFormat(),tz);
    }

    // ------------------------------------------------------------------------

    /* return the last time the password was changed for this account */
    public long getPasswdChangeTime()
    {
        Long v = (Long)this.getFieldValue(FLD_passwdChangeTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time the password was changed for this account */
    public void setPasswdChangeTime(long v)
    {
        this.setFieldValue(FLD_passwdChangeTime, v);
    }

    /* password expired? */
    public boolean hasPasswordExpired()
    {
        BasicPrivateLabel bpl = this.getPrivateLabel();
        PasswordHandler pwh = Account.getPasswordHandler(bpl);
        return pwh.hasPasswordExpired(this.getPasswdChangeTime());
    }

    // ------------------------------------------------------------------------

    /* return the last time the password was queried for this account */
    public long getPasswdQueryTime()
    {
        Long v = (Long)this.getFieldValue(FLD_passwdQueryTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time the password was queried for this account */
    public void setPasswdQueryTime(long v)
    {
        this.setFieldValue(FLD_passwdQueryTime, v);
    }

    // ------------------------------------------------------------------------

    /* return true if the retained-event-age has been defined */
    public boolean hasRetainedEventAge()
    {
        return (this.getRetainedEventAge() > 0L)? true : false;
    }

    /* return the maximum age of retained events (in seconds) */
    public long getRetainedEventAge()
    {
        Long v = (Long)this.getFieldValue(FLD_retainedEventAge);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the maximum age of retained events (in seconds) */
    public void setRetainedEventAge(long v)
    {
        this.setFieldValue(FLD_retainedEventAge, v);
    }
    
    /* adjust retained event age date */
    public long adjustRetainedEventTime(long oldTimeSec)
    {
        long retainedAgeSec = this.getRetainedEventAge();
        if (retainedAgeSec > 0L) {
            // -- retainedEventAge is defined
            long retainedTimeSec = DateTime.getCurrentTimeSec() - retainedAgeSec;
            if (retainedTimeSec < oldTimeSec) {
                // -- retained-time is prior to delete-time
                oldTimeSec = retainedTimeSec;
                if (oldTimeSec < 1L) {
                    // -- must not be less than "1"
                    oldTimeSec = 1L;
                }
            }
        }
        return oldTimeSec;
    }

    // ------------------------------------------------------------------------

    /**
    *** Return true if this account is at the maximum number of allowed devices
    **/
    public boolean isAtMaximumDevices(boolean zeroIsUnlimited)
    {

        /* check account maximum devices */
        long maxCnt = this.getMaximumDevices();
        if (maxCnt < 0L) {
            // -- no limit on number of devices
            return false;
        } else
        if ((maxCnt == 0L) && zeroIsUnlimited) {
            // -- no limit on number of devices
            return false;
        } else {
            // -- check device count against maximum limit
            long devCnt = this.getDeviceCount();
            return (devCnt >= maxCnt)? true : false;
        }

    }

    /**
    *** Return true if the specified value exceeds the allow maximum number of devices
    **/
    public boolean exceedsMaximumDevices(long devCnt, boolean zeroIsUnlimited)
    {

        /* check account maximum devices */
        long maxCnt = this.getMaximumDevices();
        if (maxCnt < 0L) {
            return false;
        } else
        if ((maxCnt == 0L) && zeroIsUnlimited) {
            return false;
        } else {
            return (devCnt > maxCnt)? true : false;
        }

    }

    /* return the maximum number of allowed devices */
    public long getMaximumDevices()
    {
        Long v = (Long)this.getFieldValue(FLD_maximumDevices);
        return (v != null)? v.intValue() : -1L;
    }

    /* set the maximum number of allowed devices */
    public void setMaximumDevices(long v)
    {
        this.setFieldValue(FLD_maximumDevices, v);
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

    public boolean incrementPingCount(long pingTime, boolean reload, boolean update)
    {

        /* refresh current totalPingCount */
        if (reload) {
            // in case another Device 'ping' has changed this value already
            this.reload(Account.FLD_totalPingCount);
        }

        /* increment totalPingCount */
        this.setTotalPingCount(this.getTotalPingCount() + 1);
        if (pingTime > 0L) {
            //this.setLastPingTime(pingTime);   TODO: add this method
        }

        /* update record */
        if (update) {
            try {
                this.update( // may throw DBException
                  //Account.FLD_lastPingTime,
                    Account.FLD_totalPingCount
                    );
            } catch (DBException dbe) {
                Print.logException("Unable to update 'ping' count", dbe);
                return false;
            }
        }

        return true;
    }

    public boolean resetTotalPingCount(boolean update)
    {

        /* reset */
        this.setTotalPingCount(0);
        //this.setLastPingTime(0L);   TODO: add this method

        /* update record */
        if (update) {
            try {
                this.update( // may throw DBException
                  //Account.FLD_lastPingTime,
                    Account.FLD_totalPingCount
                    );
            } catch (DBException dbe) {
                Print.logException("Unable to update 'ping' count", dbe);
                return false;
            }
        }

        return true;

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

    /* return the time this account expires */
    public long getExpirationTime()
    {
        Long v = (Long)this.getFieldValue(FLD_expirationTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time this account expires */
    public void setExpirationTime(long v)
    {
        this.setFieldValue(FLD_expirationTime, v);
    }

    /* return true if this account has expired */
    @JSONBeanGetter(ignore="false")
    public boolean getIsExpired()
    {

        /* check expired time */
        long expireTime = this.getExpirationTime();
        if ((expireTime > 0L) && (expireTime < DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not expired */
        return false;

    }

    /* return true if this account has expired */
    public boolean isExpired()
    {

        /* deleted? (assume expired if deleted) */
        if (this.isDeleted()) {
            return true;
        }

        /* not active? (assume expired if not active) */
        if (!this.isActive()) {
            return true;
        }

        /* expired? */
        return this.getIsExpired();

    }
    
    /* return true if this account has an expiry date */
    public boolean doesExpire()
    {
        long expireTime = this.getExpirationTime();
        return (expireTime > 0L);
    }

    /* return true if this account will expire within the specified # of seconds */
    public boolean willExpire(long withinSec)
    {

        /* will account expire? */
        long expireTime = this.getExpirationTime();
        if ((expireTime > 0L) && 
            ((withinSec < 0L) || (expireTime < (DateTime.getCurrentTimeSec() + withinSec)))) {
            return true;
        }

        /* will not expired */
        return false;

    }

    // ------------------------------------------------------------------------

    /* return the account suspend time */
    public long getSuspendUntilTime()
    {
        Long v = (Long)this.getFieldValue(FLD_suspendUntilTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time of this account suspension */
    public void setSuspendUntilTime(long v)
    {
        this.setFieldValue(FLD_suspendUntilTime, v);
    }

    /* return true if this account is suspended */
    @JSONBeanGetter(ignore="false")
    public boolean getIsSuspended()
    {

        /* account suspended? */
        long suspendTime = this.getSuspendUntilTime();
        if ((suspendTime > 0L) && (suspendTime >= DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not suspended */
        return false;

    }

    /* return true if this account is suspended */
    public boolean isSuspended()
    {
        return this.getIsSuspended();
    }

    /**
    *** Called after login failure, check for user temporary suspension 
    *** @param addCurrentFailure  True to add the current login failure to the previously 
    ***     recorded login failures.  Should be true if "Audit.userLoginFailed(...)" has
    ***     not yet been called to record the current login failure.
    **/
    public boolean suspendOnLoginFailureAttempt(boolean addCurrentFailure)
    {

        /* suspend on failed login attempt disabled? */
        PasswordHandler pwh = Account.getPasswordHandler(this); // not null
        if (!pwh.getFailedLoginSuspendEnabled()) {
            // -- failed login attempt suspend is not enabled
            return false;
        }

        /* number of failed login attempts */
        String accountID   = this.getAccountID();
        String userID      = null; // any user?
        long   asOfTime    = DateTime.getCurrentTimeSec(); // now
        long   sinceTime   = asOfTime - pwh.getFailedLoginAttemptInterval();
        long   addCount    = addCurrentFailure? 1L : 0L;
        long   failCount   = Audit.getFailedLoginAttempts(accountID, userID, sinceTime) + addCount;
        long   suspendTime = pwh.getFailedLoginAttemptSuspendTime((int)failCount, asOfTime);
        if (suspendTime > 0L) {
            // -- too many failed login attempts, suspend user
            long oldSuspendTime = this.getSuspendUntilTime();
            if (suspendTime > oldSuspendTime) {
                this.setSuspendUntilTime(suspendTime);
                try {
                    this.update(Account.FLD_suspendUntilTime);
                } catch (DBException dbe) {
                    Print.logError("Unable to set suspendUntilTime for account ("+accountID+"): " + dbe);
                }
            } else {
                // -- already suspended
            }
            return true;
        } else {
            return false;
        }

    }

    // ------------------------------------------------------------------------

    /* default value for "Account.alwaysAllowWebService" */
    private static final boolean DEFAULT_alwaysAllowWebService = true;

    /**
    *** Returns true if web-service access is always allowed for all Accounts
    *** @return True if web-service access is always allowed for all Accounts
    **/
    public static boolean IsAlwaysAllowWebService()
    {
        if (RTConfig.getBoolean(DBConfig.PROP_Account_alwaysAllowWebService,DEFAULT_alwaysAllowWebService)) {
            return true;
        } else {
            return false;
        }
    }

    /**
    *** Returns true if web-service access is allowed for the specified Account
    *** @return True if web-service access is allowed for the specified Account
    **/
    public static boolean GetAllowWebService(Account acct)
    {
        if (acct == null) {
            return false;
        } else
        if (IsAlwaysAllowWebService()) {
            return true;
        } else {
            return acct.getAllowWebService();
        }
    }

    /**
    *** Returns true if web-service access is allowed for this Account
    *** @return True if web-service access is allowed for this Account
    **/
    @JSONBeanGetter()
    public boolean getAllowWebService()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_allowWebService);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "Allow Web-Service" state for this Account
    *** @param v The "Allow Web-Service" state for this Account
    **/
    public void setAllowWebService(boolean v)
    {
        this.setFieldValue(FLD_allowWebService, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the account attribute mask 
    **/
    @JSONBeanGetter(ignore="0")
    public long getAttributeMask() // AccountAttribute
    {
        Long v = (Long)this.getFieldValue(FLD_attributeMask);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the account attribute mask 
    **/
    public void setAttributeMask(long v)
    {
        this.setFieldValue(FLD_attributeMask, v);
    }

    // --------------------------------

    /**
    *** Gets the boolean value of the specified bit from the account attribute mask
    **/
    public boolean getAttributeMaskBit(int ndx)
    {
        long attrMask = this.getAttributeMask();
        long bitMask  = 1L << ndx;
        return ((attrMask & bitMask) != 0)? true : false;
    }

    /**
    *** Sets the boolean value for the specified bit from the account attribute mask
    **/
    public void setAttributeMaskBit(int ndx, boolean val)
    {
        if (val != this.getAttributeMaskBit(ndx)) {
            long attrMask = this.getAttributeMask();
            if (val) {
                attrMask |= (1L << ndx);
            } else {
                attrMask &= ~(1L << ndx);
            }
            this.setAttributeMask(attrMask);
        }
    }

    // ------------------------------------------------------------------------

    @JSONBeanGetter(ignore="$blank")
    public String getAddressLine1()
    {
        String v = (String)this.getFieldValue(FLD_addressLine1);
        return StringTools.trim(v);
    }
    
    @JSONBeanGetter(ignore="$blank")
    public String getAddressLine2()
    {
        String v = (String)this.getFieldValue(FLD_addressLine2);
        return StringTools.trim(v);
    }

    @JSONBeanGetter(ignore="$blank")
    public String getAddressLine3()
    {
        String v = (String)this.getFieldValue(FLD_addressLine3);
        return StringTools.trim(v);
    }

    public String[] getAddressLines()
    {
        return new String[] {
            this.getAddressLine1(),
            this.getAddressLine2(),
            this.getAddressLine3()
        };
    }
    
    @JSONBeanGetter(ignore="$blank")
    public String getAddressCity()
    {
        String v = (String)this.getFieldValue(FLD_addressCity);
        return StringTools.trim(v);
    }
    
    @JSONBeanGetter(ignore="$blank")
    public String getAddressState()
    {
        String v = (String)this.getFieldValue(FLD_addressState);
        return StringTools.trim(v);
    }
   
    @JSONBeanGetter(ignore="$blank")
    public String getAddressPostalCode()
    {
        String v = (String)this.getFieldValue(FLD_addressPostalCode);
        return StringTools.trim(v);
    }
   
    @JSONBeanGetter(ignore="$blank")
    public String getAddressCountry()
    {
        String v = (String)this.getFieldValue(FLD_addressCountry);
        return StringTools.trim(v);
    }

    public void setAddressLine1(String v)
    {
        this.setFieldValue(FLD_addressLine1, StringTools.trim(v));
    }

    public void setAddressLine2(String v)
    {
        this.setFieldValue(FLD_addressLine2, StringTools.trim(v));
    }

    public void setAddressLine3(String v)
    {
        this.setFieldValue(FLD_addressLine3, StringTools.trim(v));
    }
    
    public void setAddressLines(String lines[])
    {
        if ((lines != null) && (lines.length > 0)) {
            int n = 0;
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine1((n < lines.length)? lines[n++].trim() : "");
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine2((n < lines.length)? lines[n++].trim() : "");
            while ((n < lines.length) && ((lines[n] == null) || lines[n].trim().equals(""))) { n++; }
            this.setAddressLine3((n < lines.length)? lines[n++].trim() : "");
        } else {
            this.setAddressLine1("");
            this.setAddressLine2("");
            this.setAddressLine3("");
        }
    }

    public void setAddressCity(String v)
    {
        this.setFieldValue(FLD_addressCity, StringTools.trim(v));
    }

    public void setAddressState(String v)
    {
        this.setFieldValue(FLD_addressState, StringTools.trim(v));
    }

    public void setAddressPostalCode(String v)
    {
        this.setFieldValue(FLD_addressPostalCode, StringTools.trim(v));
    }

    public void setAddressCountry(String v)
    {
        this.setFieldValue(FLD_addressCountry, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get the speed-units for this account */
    @JSONBeanGetter(enumClass="SpeedUnits")
    public int getSpeedUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_speedUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(SpeedUnits.class).getIntValue();
    }

    /* set the speed-units */
    public void setSpeedUnits(int v)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v).getIntValue());
    }

    /* set the speed-units */
    public void setSpeedUnits(SpeedUnits v)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v).getIntValue());
    }

    /* set the string representation of the speed-units */
    public void setSpeedUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_speedUnits, EnumTools.getValueOf(SpeedUnits.class,v,locale).getIntValue());
    }

    // --------------------------------

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, boolean inclUnits, Locale locale)
    {
        return this.getSpeedString(speedKPH, "0", null, inclUnits, locale);
    }

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, String format, boolean inclUnits, Locale locale)
    {
        return this.getSpeedString(speedKPH, format, null, inclUnits, locale);
    }

    /* return a formatted speed string */
    public String getSpeedString(double speedKPH, String format, SpeedUnits speedUnitsEnum, boolean inclUnits, Locale locale)
    {
        return Account.getSpeedString(this, speedKPH, format, speedUnitsEnum, inclUnits, locale);
    }

    // --------------------------------

    /* return a formatted speed string */
    public static String getSpeedString(Account acct, double speedKPH, boolean inclUnits, Locale locale)
    {
        return Account.getSpeedString(acct, speedKPH, "0", null, inclUnits, locale);
    }

    /* return a formatted speed string */
    public static String getSpeedString(Account acct, double speedKPH, String format, SpeedUnits speedUnitsEnum, boolean inclUnits, Locale locale)
    {
        if (speedUnitsEnum == null) { speedUnitsEnum = Account.getSpeedUnits(acct); }
        double speed = speedUnitsEnum.convertFromKPH(speedKPH);
        String speedFmt = StringTools.format(speed, StringTools.blankDefault(format,"0"));
        if (speed <= 0.0) {
            return speedFmt;
        } else {
            if (inclUnits) {
                return speedFmt + " " + speedUnitsEnum.toString(locale);
            } else {
                return speedFmt;
            }
        }
    }

    public static String getFullSpeedString(Account  acct, double speedKPH, String format, SpeedUnits speedUnitsEnum, boolean inclUnits, Locale locale)
    {
        if (speedUnitsEnum == null) { speedUnitsEnum = Account.getSpeedUnits(acct); }
        double speed = speedUnitsEnum.convertFromKPH(speedKPH);
        String speedFmt = StringTools.format(speed, StringTools.blankDefault(format,"0"));
        if (inclUnits) {
            return speedFmt + " " + speedUnitsEnum.toString(locale);
        } else {
            return speedFmt;
        }
    }

    // ------------------------------------------------------------------------

    /* get the distance units for this account */
    @JSONBeanGetter(enumClass="DistanceUnits")
    public int getDistanceUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_distanceUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(DistanceUnits.class).getIntValue();
    }

    /* set the distance units */
    public void setDistanceUnits(int v)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v).getIntValue());
    }

    /* set the distance units */
    public void setDistanceUnits(DistanceUnits v)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v).getIntValue());
    }

    /* set the string representation of the distance units */
    public void setDistanceUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_distanceUnits, EnumTools.getValueOf(DistanceUnits.class,v,locale).getIntValue());
    }

    // --------------------------------

    /* return a formatted distance string */
    public static String getDistanceString(Account acct, double distKM, boolean inclUnits, Locale locale)
    {
        DistanceUnits units = Account.getDistanceUnits(acct);
        String distUnitsStr = units.toString(locale);
        double dist         = units.convertFromKM(distKM);
        String distStr      = StringTools.format(dist, "0");
        return inclUnits? (distStr + " " + distUnitsStr) : distStr;
    }

    /* return a formatted distance string */
    public String getDistanceString(double distKM, boolean inclUnits, Locale locale)
    {
        return Account.getDistanceString(this, distKM, inclUnits, locale);
    }

    // --------------------------------

    /**
    *** Gets the Altitude units, based on the Distance units
    **/
    @JSONBeanGetter(enumClass="AltitudeUnits")
    public int getAltitudeUnits()
    {
        return Account.getAltitudeUnits(this).getIntValue();
    }

    // ------------------------------------------------------------------------

    /* get the volume units for this account */
    @JSONBeanGetter(enumClass="VolumeUnits")
    public int getVolumeUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_volumeUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getDistanceUnits(this)) { // assume volume units based on distanceUnits
                case MILES : return VolumeUnits.US_GALLONS.getIntValue();
                default    : return VolumeUnits.LITERS.getIntValue();
            }
        }
    }

    /* set the volume units */
    public void setVolumeUnits(int v)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v).getIntValue());
    }

    /* set the volume units */
    public void setVolumeUnits(VolumeUnits v)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v).getIntValue());
    }

    /* set the string representation of the volume units */
    public void setVolumeUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_volumeUnits, EnumTools.getValueOf(VolumeUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Fuel cost per Liter
    *** @return The Fuel cost per Liter
    **/
    @JSONBeanGetter(ignore="0.0")
    public double getFuelCostPerLiter()
    {
        Double v = (Double)this.getFieldValue(FLD_fuelCostPerLiter);
        return (v != null)? v.doubleValue() : 0.0;
    }

    /**
    *** Sets the Fuel cost per Liter
    *** @param v The Fuel cost per Liter
    **/
    public void setFuelCostPerLiter(double v)
    {
        this.setFieldValue(FLD_fuelCostPerLiter, (v >= 0.0)? v : 0.0);
    }

    // ------------------------------------------------------------------------

    /* get the volume units for this account */
    @JSONBeanGetter(enumClass="PressureUnits")
    public int getPressureUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_pressureUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getVolumeUnits(this)) {
                case US_GALLONS : return PressureUnits.PSI.getIntValue();
                default         : return PressureUnits.KPA.getIntValue();
            }
        }
    }

    /* set the pressure units */
    public void setPressureUnits(int v)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v).getIntValue());
    }

    /* set the pressure units */
    public void setPressureUnits(PressureUnits v)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v).getIntValue());
    }

    /* set the string representation of the pressure units */
    public void setPressureUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_pressureUnits, EnumTools.getValueOf(PressureUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the economy units for this account */
    @JSONBeanGetter(enumClass="EconomyUnits")
    public int getEconomyUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_economyUnits);
        if (v != null) {
            return v.intValue();
        } else {
            switch (Account.getVolumeUnits(this)) {
                case US_GALLONS : return EconomyUnits.MPG.getIntValue();
                default         : return EconomyUnits.KPL.getIntValue();
            }
        }
    }

    /* set the economy units */
    public void setEconomyUnits(int v)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v).getIntValue());
    }

    /* set the economy units */
    public void setEconomyUnits(EconomyUnits v)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v).getIntValue());
    }

    /* set the economy units */
    public void setEconomyUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_economyUnits, EnumTools.getValueOf(EconomyUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the temperature units of the account */
    @JSONBeanGetter(enumClass="TemperatureUnits")
    public int getTemperatureUnits()
    {
        Integer v = (Integer)this.getFieldValue(FLD_temperatureUnits);
        return (v != null)? v.intValue() : EnumTools.getDefault(TemperatureUnits.class).getIntValue();
    }

    /* set the temperature units */
    public void setTemperatureUnits(int v)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v).getIntValue());
    }

    /* set the temperature units */
    public void setTemperatureUnits(TemperatureUnits v)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v).getIntValue());
    }

    /* set the string representation of the temperature units */
    public void setTemperatureUnits(String v, Locale locale)
    {
        this.setFieldValue(FLD_temperatureUnits, EnumTools.getValueOf(TemperatureUnits.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the currency units for this account */
    @JSONBeanGetter(enumClass="Currency")
    public String getCurrencyUnits()
    {
        String v = (String)this.getFieldValue(FLD_currencyUnits);
        if (!StringTools.isBlank(v)) {
            return v;
        } else {
            return Account.GetDefaultCurrency(); // "usd"
        }
    }

    /* set the currency units */
    public void setCurrencyUnits(String v)
    {
        v = StringTools.trim(v);
        if (StringTools.isBlank(v)) {
            v = Account.GetDefaultCurrency();
        }
        this.setFieldValue(FLD_currencyUnits, v);
    }

    /* set the currency units */
    public void setCurrencyUnits(Currency v)
    {
        this.setFieldValue(FLD_currencyUnits, EnumTools.getValueOf(Currency.class,v).getCode());
    }

    // ------------------------------------------------------------------------

    /* get the Lat/Lon format of the account */
    @JSONBeanGetter(enumClass="LatLonFormat")
    public int getLatLonFormat()
    {
        Integer v = (Integer)this.getFieldValue(FLD_latLonFormat);
        return (v != null)? v.intValue() : EnumTools.getDefault(LatLonFormat.class).getIntValue();
    }

    /* set the Lat/Lon format */
    public void setLatLonFormat(int v)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v).getIntValue());
    }

    /* set the Lat/Lon format */
    public void setLatLonFormat(LatLonFormat v)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v).getIntValue());
    }

    /* set the string representation of the Lat/Lon format */
    public void setLatLonFormat(String v, Locale locale)
    {
        this.setFieldValue(FLD_latLonFormat, EnumTools.getValueOf(LatLonFormat.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* return the geocoder mode for this account */
    @JSONBeanGetter(enumClass="GeocoderMode")
    public int getGeocoderMode()
    {
        Integer v = (Integer)this.getFieldValue(FLD_geocoderMode);
        return (v != null)? v.intValue() : EnumTools.getDefault(GeocoderMode.class).getIntValue();
    }

    /* set the geocoder mode */
    public void setGeocoderMode(int v)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v).getIntValue());
    }

    /* set the geocoder mode */
    public void setGeocoderMode(GeocoderMode v)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v).getIntValue());
    }

    /* set the geocoder mode based on the specified string */
    public void setGeocoderMode(String v, Locale locale)
    {
        this.setFieldValue(FLD_geocoderMode, EnumTools.getValueOf(GeocoderMode.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------

    /* get the PrivateLabel name assigned to this account */
    @JSONBeanGetter(ignore="$blank")
    public String getPrivateLabelName()
    {
        String v = (String)this.getFieldValue(FLD_privateLabelName);
        return StringTools.trim(v);
    }

    /* set the PrivateLabel name assigned to this account */
    public void setPrivateLabelName(String v)
    {
        this.setFieldValue(FLD_privateLabelName, StringTools.trim(v));
    }
    
    /**
    *** Returns true is this Account has a defined PrivateLabel name
    **/
    public boolean hasPrivateLabelName()
    {
        return !StringTools.isBlank(this.getPrivateLabelName())? true : false;
    }

    /**
    *** Gets the current active PrivateLabel name.
    *** If not currently in a web-context, the Account PrivateLabel name will be returned
    **/
    public String getContextPrivateLabelName()
    {
        String bplName = Account.getSessionPrivateLabelName();
        if (StringTools.isBlank(bplName)) {
            bplName = this.getPrivateLabelName(); // BasicPrivateLabel
        }
        return bplName; // BasicPrivateLabel
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the current session PrivateLabel name.
    **/
    public static String getSessionPrivateLabelName()
    {
        RTProperties thRTP = RTConfig.getThreadProperties(RTKey.SESSION_PRIVLABEL);
        return (thRTP != null)? thRTP.getString(RTKey.SESSION_PRIVLABEL,null) : null;
    }

    /**
    *** Gets the current session PrivateLabel name.
    **/
    public static BasicPrivateLabel getSessionPrivateLabel()
    {
        String bplName = Account.getSessionPrivateLabelName();
        return BasicPrivateLabelLoader.getPrivateLabel(bplName);
    }

    // ------------------------------------------------------------------------

    /* get the PrivateLabel jsp assigned to this account */
    @JSONBeanGetter(ignore="$blank")
    public String getPrivateLabelJsp()
    {
        String v = (String)this.getOptionalFieldValue(FLD_privateLabelJsp);
        return StringTools.trim(v);
    }

    /* set the PrivateLabel jsp assigned to this account */
    public void setPrivateLabelJsp(String v)
    {
        this.setOptionalFieldValue(FLD_privateLabelJsp, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return the last account login time */
    @JSONBeanGetter(ignore="0")
    public long getLastLoginTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastLoginTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the last account login time */
    public void setLastLoginTime(long v)
    {
        this.setFieldValue(FLD_lastLoginTime, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this account has a login-message
    **/
    public boolean hasLoginMessage()
    {
        return !StringTools.isBlank(this.getLoginMessage())? true : false;
    }

    /**
    *** Gets the Account login message
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getLoginMessage()
    {
        String v = (String)this.getFieldValue(FLD_loginMessage);
        return !StringTools.isBlank(v)? v.trim() : "";
    }

    /**
    *** Sets the Account login message
    **/
    public void setLoginMessage(String v)
    {
        this.setFieldValue(FLD_loginMessage, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this account has an inactive-message
    **/
    public boolean hasInactiveMessage()
    {
        return !StringTools.isBlank(this.getInactiveMessage())? true : false;
    }

    /**
    *** Gets the Account inactive-message
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getInactiveMessage()
    {
        String v = (String)this.getFieldValue(FLD_inactiveMessage);
        return !StringTools.isBlank(v)? v.trim() : "";
    }

    /**
    *** Sets the Account inactive-message
    **/
    public void setInactiveMessage(String v)
    {
        this.setFieldValue(FLD_inactiveMessage, StringTools.trim(v));
    }

    // --------------------------------

    /**
    *** Returns true if this account, or the AccountManager, has an inactive-message
    **/
    public boolean hasInactiveMessage(boolean chkAcctMgr)
    {
        return !StringTools.isBlank(this.getInactiveMessage(chkAcctMgr))? true : false;
    }

    /**
    *** Gets the inactive-message for this account, or the AccountManager inactive-message
    *** if this inactive-message is blank
    **/
    public String getInactiveMessage(boolean chkAcctMgr)
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* this Inactive Message */
        if (this.hasInactiveMessage()) {
            return this.getInactiveMessage();
        }

        /* check AccountManagers? */
        if (chkAcctMgr) {
            try {
                Account mgrAccts[] = this.getManagerAccounts(rwMode); // may be cached
                if (!ListTools.isEmpty(mgrAccts)) {
                    for (Account mAcct : mgrAccts) {
                        if (mAcct.hasInactiveMessage()) {
                            // -- found an AccountManager with an inactive-message
                            return mAcct.getInactiveMessage();
                        }
                    }
                }
            } catch (DBException dbe) {
                Print.logException("Unable to read list of AccountManagers", dbe);
                return "";
            }
        }

        /* no inactive-message found */
        return "";

    }

    // ------------------------------------------------------------------------

    /* return true if BorderCrossing detection is enabled for this account */
    @JSONBeanGetter(ignore="false")
    public boolean getIsBorderCrossing() // bcross
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isBorderCrossing);
        return (v != null)? v.booleanValue() : false;
    }

    /* set the 'borderCrossing' enabled state for this account */
    public void setIsBorderCrossing(boolean v)
    {
        this.setFieldValue(FLD_isBorderCrossing, v);
    }

    /* return true if BorderCrossing detection is enabled for this account [see "getIsBorderCrossing()"] */
    public boolean isBorderCrossing()
    {
        return this.getIsBorderCrossing();
    }
    
    private static int borderCrossingExists = -1;
    public static boolean SupportsBorderCrossing()
    {
        if (borderCrossingExists < 0) {
            // NOTE: test may fail if table name translation is enabled
            //borderCrossingExists = (DBFactory.getFactoryByName("BorderCrossing") != null)? 1 : 0;
            try {
                Class.forName(DBConfig.PACKAGE_BCROSS_TABLES_ + "BorderCrossing");
                borderCrossingExists = 1;
            } catch (Throwable th) {
                borderCrossingExists = 0;
            }
        }
        return (borderCrossingExists == 1);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this Account can "auto-add" at least one new devices 
    **/
    public boolean okToAutoAddDevice()
    {
        if (!this.getAutoAddDevices()) { // okToAutoAddDevice()
            return false;
        } else {
            return !this.isAtMaximumDevices(true);
        }
    }

    /**
    *** Gets the 'auto-add' devices enabled state for this account 
    **/
    @JSONBeanGetter(ignore="false")
    public boolean getAutoAddDevices()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_autoAddDevices);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the 'auto-add' devices enabled state for this account 
    **/
    public void setAutoAddDevices(boolean v)
    {
        this.setFieldValue(FLD_autoAddDevices, v);
    }

    // ------------------------------------------------------------------------

    /* get the DCS Properties ID assigned to this account */
    @JSONBeanGetter(ignore="$blank")
    public String getDcsPropertiesID()
    {
        String v = (String)this.getFieldValue(FLD_dcsPropertiesID);
        return StringTools.trim(v);
    }

    /* set the DCS Properties ID assigned to this account */
    public void setDcsPropertiesID(String v)
    {
        this.setFieldValue(FLD_dcsPropertiesID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* return true if SMS is enabled for this account */
    @JSONBeanGetter()
    public boolean getSmsEnabled()
    {
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE  : 
                return false;
            case TRUE   : 
                return true;
            case ACCOUNT:
                Boolean v = (Boolean)this.getFieldValue(FLD_smsEnabled);
                return (v != null)? v.booleanValue() : false;
            default:
                return false;
        }
    }

    /* set the 'smsEnabled' state for this account */
    public void setSmsEnabled(boolean v)
    {
        this.setFieldValue(FLD_smsEnabled, v);
    }

    // ------------------------------------------------------------------------

    /* cached SMSProperties */
    private SMSProperties smsProperties = null;

    /**
    *** Returns true if the SmsProperties are defined in this account
    **/
    public boolean hasSmsProperties()
    {
        return !StringTools.isBlank(this.getSmsProperties())? true : false;
    }

    /* gets the SMS Gateway Properties assigned to this account */
    public String getSmsProperties()
    {
        String v = (String)this.getFieldValue(FLD_smsProperties);
        return StringTools.trim(v);
    }

    /* sets the SMS Gateway Properties assigned to this account */
    public void setSmsProperties(String v)
    {
        this.setFieldValue(FLD_smsProperties, StringTools.trim(v));
        this.smsProperties = null;
    }

    /* gets the SMS Gateway Properties assigned to this account */
    /*
    public RTProperties getSmsRTProperties()
    {
        String v = this.getSmsProperties();
        return new RTProperties(v);
    }
    */

    /* sets the SMS Gateway Properties assigned to this account */
    /*
    public void setSmsRTProperties(RTProperties v)
    {
        if (v == null) {
            this.setSmsProperties("");
        } else {
            this.setSmsProperties(v.toString());
        }
    }
    */

    /* sets the SMS Gateway Properties assigned to this account */
    public SMSProperties getSmsProperties(BasicPrivateLabel bpl)
    { 
        // -- Order of SmsProperties hierarchy
        // -    1) This Account (delegate to AccountManager or PrivateLabel)
        // -    2) AccountManager for this Account (delegate to PrivateLabel)
        // -    3) PrivateLabel 
        if (this.smsProperties == null) {

            /* single-pass loop */
            for (;;) {

                /* init with this Account? */
                if (this.hasSmsProperties()) {
                    // -- initialize with this account SMSProperties
                    RTProperties smsRTP = new RTProperties(this.getSmsProperties());
                    this.smsProperties = new SMSProperties(("Account:"+this.getAccountID()), smsRTP);
                } else {
                    // -- this account does not define SMSProperties
                }

                /* managed account? */
                if (this.hasManagerID() && !this.getIsAccountManager()) {
                    // -- this is a managed account, but not the actual account manager
                    try {
                        Account mgrAccount = this.getFirstManagerAccount(); // should not return null here
                        if (mgrAccount == null) {
                            // -- should not occur (can't find the accountID handed to it?)
                        } else
                        if (!mgrAccount.getIsAccountManager()) {
                            // -- should not occur (we've asked for the AccountManager, which now says false?)
                        } else
                        if (!mgrAccount.hasSmsProperties()) {
                            // -- manager does not define SMSProperties
                        } else {
                            // -- this account AccountManager SMSProperties
                            SMSProperties smsMgr = mgrAccount.getSmsProperties(bpl); // non-null
                            if (this.smsProperties != null) {
                                this.smsProperties.setDelegate(smsMgr);
                                break;
                            } else {
                                this.smsProperties = smsMgr;
                                break;
                            }
                        }
                    } catch (DBException dbe) {
                        // -- unable to read AccountManager?  continue below ...
                    }
                } else {
                    // -- this account is not managed (or is the actual account manager)
                }

                /* PrivalLabel properties */
                SMSProperties smsBPL = this.getPrivateLabel(bpl).getSmsProperties();
                if (this.smsProperties != null) {
                    // -- use PrivateLabel SMSProperties as delegate
                    this.smsProperties.setDelegate(smsBPL);
                } else {
                    // -- return PrivateLabel properties
                    this.smsProperties = smsBPL;
                }

                /* exit single-pass loop */
                break;

            } // for (;;)

        }

        /* return */
        return this.smsProperties;

    }

    // ------------------------------------------------------------------------

    private long smsMsgTimeSince = -99L;
    private long smsMsgTimeUntil = -99L;
    private long smsMsgCount     = 0L;
    
    /**
    *** Return number of SMS messages sent during specified time range.
    *** This includes SMS messages sent for both command and notification purposes.
    **/
    public long getSmsMessagesSent(long sinceTime, long untilTime)
    {
        if ((sinceTime != this.smsMsgTimeSince) || (untilTime != this.smsMsgTimeUntil)) {
            this.smsMsgCount = Audit.getSmsMessagesSent(this.getAccountID(), sinceTime, untilTime);
            this.smsMsgTimeSince = sinceTime;
            this.smsMsgTimeUntil = untilTime;
        }
        return this.smsMsgCount;
    }

    // ------------------------------------------------------------------------

    /* gets the Email Properties assigned to this account */
    //public String getEmailProperties()
    //{
    //    String v = (String)this.getFieldValue(FLD_emailProperties);
    //    return StringTools.trim(v);
    //}

    /* sets the Email Properties assigned to this account */
    //public void setEmailProperties(String v)
    //{
    //    this.setFieldValue(FLD_emailProperties, StringTools.trim(v));
    //}

    /* gets the Email Properties assigned to this account */
    //public RTProperties getEmailRTProperties()
    //{
    //    String v = this.getEmailProperties();
    //    return new RTProperties(v);
    //}

    /* sets the Email Properties assigned to this account */
    //public void setEmailRTProperties(RTProperties v)
    //{
    //    if (v == null) {
    //        this.setEmailProperties("");
    //    } else {
    //        this.setEmailProperties(v.toString());
    //    }
    //}

    // ------------------------------------------------------------------------

    /**
    *** Returns true is the SmtpProperties are defined in this account
    **/
    public boolean hasSmtpProperties()
    {
        return !StringTools.isBlank(this.getSmtpProperties())? true : false;
    }

    /**
    *** Gets the SmtpProperties as a String
    **/
    public String getSmtpProperties()
    {
        String v = (String)this.getFieldValue(FLD_smtpProperties);
        return StringTools.trim(v);
    }

    /**
    *** Sets the SmtpProperties as a String
    **/
    public void setSmtpProperties(String smtpStr)
    {
        if (!StringTools.isBlank(smtpStr)) {
            // -- user-filtered copy
            SmtpProperties smtpUser = new SmtpProperties("Account:Set",new RTProperties(smtpStr),true/*user*/);
            this._setSmtpProperties(smtpUser);
        } else {
            // -- clear
            this._setSmtpProperties((SmtpProperties)null);
        }
    }

    /**
    *** Sets the SmtpProperties
    **/
    public void setSmtpProperties(SmtpProperties smtpProps)
    {
        if (smtpProps != null) {
            // -- user-filtered copy
            this._setSmtpProperties(smtpProps.copy(true/*user*/));
        } else {
            // -- clear
            this._setSmtpProperties((SmtpProperties)null);
        }
    }

    /**
    *** Sets the SmtpProperties
    **/
    private void _setSmtpProperties(SmtpProperties smtpUser)
    {
        if (smtpUser != null) {
            String v = smtpUser.toString(true/*abbrev*/);
            this.setFieldValue(FLD_smtpProperties, v);
        } else {
            this.setFieldValue(FLD_smtpProperties, "");
        }
    }

    /**
    *** Gets the SMTP properties for this Account.
    *** Does not return null
    **/
    public SmtpProperties getSmtpProperties(BasicPrivateLabel bpl)
    {
        // -- Order of SmtpProperties hierarchy
        // -    1) This Account (delegate to AccountManager or PrivateLabel)
        // -    2) AccountManager for this Account (delegate to PrivateLabel)
        // -    3) PrivateLabel 
        SmtpProperties smtpProps = null;

        /* this Account? */
        if (this.hasSmtpProperties()) {
            // -- initialize with this account SmtpProperties
            boolean userFilter = true;
            RTProperties smtpRTP = new RTProperties(this.getSmtpProperties());
            smtpProps = new SmtpProperties(("Account:"+this.getAccountID()), smtpRTP, userFilter);
        } else {
            // -- this account does not define SmtpProperties
        }

        /* managed account? */
        if (this.hasManagerID() && !this.getIsAccountManager()) {
            // -- this is a managed account, but not the actual account manager
            try {
                Account mgrAccount = this.getFirstManagerAccount(); // should not return null here
                if (mgrAccount == null) {
                    // -- should not occur (can't find the accountID handed to it?)
                } else
                if (!mgrAccount.getIsAccountManager()) {
                    // -- should not occur (we've asked for the AccountManage, which now says false?)
                } else
                if (!mgrAccount.hasSmtpProperties()) {
                    // -- manager does not define SmtpProperties
                } else {
                    // -- this account AccountManager SmtpProperties
                    SmtpProperties smtpMgr = mgrAccount.getSmtpProperties(bpl); // non-null
                    if (smtpProps != null) {
                        smtpProps.setDelegate(smtpMgr);
                        return smtpProps;
                    } else {
                        return smtpMgr;
                    }
                }
            } catch (DBException dbe) {
                // -- unable to read AccountManager?  continue below ...
            }
        } else {
            // -- this account is not managed (or is the actual account manager)
        }

        /* PrivalLabel properties */
        SmtpProperties smtpBPL = this.getPrivateLabel(bpl).getSmtpProperties(); // not null
        if (smtpProps != null) {
            // -- use PrivateLabel SmtpProperties as delegate
            smtpProps.setDelegate(smtpBPL);
            return smtpProps;
        } else {
            // -- return PrivateLabel properties
            return smtpBPL;
        }

    }

    // ------------------------------------------------------------------------

    public String getMapLegendDevice()
    {
        if (this.hasField(FLD_mapLegendDevice)) {
            String v = (String)this.getFieldValue(FLD_mapLegendDevice);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    public void setMapLegendDevice(String v)
    {
        if (this.hasField(FLD_mapLegendDevice)) {
            this.setFieldValue(FLD_mapLegendDevice, ((v != null)? v : ""));
        }
    }

    public String getMapLegendGroup()
    {
        if (this.hasField(FLD_mapLegendGroup)) {
            String v = (String)this.getFieldValue(FLD_mapLegendGroup);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    public void setMapLegendGroup(String v)
    {
        if (this.hasField(FLD_mapLegendGroup)) {
            this.setFieldValue(FLD_mapLegendGroup, ((v != null)? v : ""));
        }
    }

    public void setMapLegend(boolean isFleet, String legend)
    {
        if (isFleet) {
            this.setMapLegendGroup(legend);
        } else {
            this.setMapLegendDevice(legend);
        }
    }

    public String getMapLegend(boolean isFleet)
    {
        if (isFleet) {
            return this.getMapLegendGroup();
        } else {
            return this.getMapLegendDevice();
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the map Route Segment Color selector
    **/
    public String getRouteSegColorSel() // RouteSegmentColor
    {
        if (this.hasField(FLD_routeSegColorSel)) {
            String v = (String)this.getFieldValue(FLD_routeSegColorSel);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /**
    *** Sets the map Route Segment Color selector
    **/
    public void setRouteSegColorSel(String v) // RouteSegmentColor
    {
        if (this.hasField(FLD_routeSegColorSel)) {
            this.setFieldValue(FLD_routeSegColorSel, ((v != null)? v : ""));
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the route-segment color rule-selector is defined, false otherwise.
    **/
    public boolean hasRouteSegmentColorSelector() // RouteSegmentColor
    {
        return !StringTools.isBlank(this.getRouteSegmentColorSelector())? true : false;
    }

    /**
    *** Gets the route-segment color rule-selector 
    **/
    public String getRouteSegmentColorSelector() // RouteSegmentColor
    {
        return this.getRouteSegColorSel(); // table column/field, may be blank
    }

    // ------------------------------------------------------------------------

    private static final String AUTOUPD_ENABLED     = "enabled";
    private static final String AUTOUPD_ONLOAD      = "onload";
    private static final String AUTOUPD_INTERVAL    = "interval";
    private static final String AUTOUPD_COUNT       = "count";
    private static final String AUTOUPD_SKIPRAD     = "skiprad";
    private static final String AUTOUPD_RECENTER    = "recenter";
    // -- "enabled=true onload=false interval=120 count=999999 skiprad=50 recenter=last"

    private RTProperties mapAutoUpdProps = null;

    public RTProperties getMapAutoUpdateRTP()
    {
        if (this.mapAutoUpdProps == null) {
            this.mapAutoUpdProps = new RTProperties(this.getMapAutoUpdate());
        }
        return this.mapAutoUpdProps;
    }

    public boolean hasMapAutoUpdate()
    {
        if (!this.hasField(FLD_mapAutoUpdate)) {
            return false;
        } else {
            RTProperties rtp = this.getMapAutoUpdateRTP();
            String enabled = rtp.getString(AUTOUPD_ENABLED,null);
            if (StringTools.isBoolean(enabled,true)) {
                // -- ["true"|"false"]
                return true;
            } else {
                // -- ["default"]
                return false;
            }
        }
    }

    public String getMapAutoUpdate()
    {
        if (this.hasField(FLD_mapAutoUpdate)) {
            String v = (String)this.getFieldValue(FLD_mapAutoUpdate);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    public void setMapAutoUpdate(String v)
    {
        // -- "enabled=true onload=false interval=120 count=1000"
       if (this.hasField(FLD_mapAutoUpdate)) {
            this.setFieldValue(FLD_mapAutoUpdate, ((v != null)? v : ""));
        }
        this.mapAutoUpdProps = null;
    }

    public void setMapAutoUpdate(
        boolean enabled,
        boolean onload,
        long    interval, long count)
    {
        // -- "enabled=true onload=false interval=120 count=1000 recenter=2"
        RTProperties rtp = new RTProperties();
        if ((interval > 0L) && (count > 0L)) {
            rtp.setBoolean(AUTOUPD_ENABLED , enabled);
            rtp.setBoolean(AUTOUPD_ONLOAD  , onload);
            rtp.setLong(   AUTOUPD_INTERVAL, interval);
            rtp.setLong(   AUTOUPD_COUNT   , count);
          //rtp.setDouble( AUTOUPD_SKIPEAD , skipRad);
          //rtp.setInt(    AUTOUPD_RECENTER, recenter);
        } else {
            rtp.setString( AUTOUPD_ENABLED , "default");
        }
        this.setMapAutoUpdate(rtp.toString());
        this.mapAutoUpdProps = rtp;
    }

    // --------------------------------

    public boolean getMapAutoUpdateEnabled(boolean dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_ENABLED,null);
        if (StringTools.isBoolean(auv,true)) {
            // -- ["true"|"false"]
            return StringTools.parseBoolean(auv,false);
        } else {
            // -- ["default"]
            return dft;
        }
    }

    public boolean getMapAutoUpdateOnload(boolean dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_ONLOAD,null);
        if (StringTools.isBoolean(auv,true)) {
            // -- ["true"|"false"]
            return StringTools.parseBoolean(auv,false);
        } else {
            // -- ["default"|undefined]
            return dft;
        }
    }

    public long getMapAutoUpdateInterval(long dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_INTERVAL,null);
        if (StringTools.isLong(auv,true)) {
            // -- [Long]
            return StringTools.parseLong(auv,dft);
        } else {
            // -- [undefined]
            return dft;
        }
    }

    public long getMapAutoUpdateCount(long dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_COUNT,null);
        if (StringTools.isLong(auv,true)) {
            // -- [Long]
            return StringTools.parseLong(auv,dft);
        } else {
            // -- [undefined]
            return dft;
        }
    }

    public double getMapAutoUpdateSkipRadius(double dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_SKIPRAD,null);
        if (StringTools.isDouble(auv,true)) {
            // -- [Double]
            return StringTools.parseDouble(auv,dft);
        } else {
            // -- [undefined]
            return dft;
        }
    }

    public int getMapAutoUpdateRecenter(int dft)
    {
        RTProperties rtp = this.getMapAutoUpdateRTP();
        String auv = rtp.getString(AUTOUPD_RECENTER,null);
        if (StringTools.isInt(auv,true)) {
            // -- [Integer]
            return StringTools.parseInt(auv,dft);
        } else {
            // -- [undefined]
            return dft;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Return true if AccountManager is supported
    **/
    public static boolean SupportsAccountManager()
    {
        DBFactory<Account> dbFact = Account.getFactory();
        if (!dbFact.hasField(FLD_isAccountManager)) {
            return false;
        } else
        if (!dbFact.hasField(FLD_managerID)) {
            return false;
        } else {
            return true;
        }
    }

    /**
    *** Sets the 'accountManager' state for this account 
    **/
    public void setIsAccountManager(boolean v)
    {
        if (this.hasField(FLD_isAccountManager)) {
            this.setFieldValue(FLD_isAccountManager, v);
        }
    }

    /**
    *** Return true if this is an account manager 
    **/
    @JSONBeanGetter(ignore="false")
    public boolean getIsAccountManager()
    {
        if (this.hasField(FLD_isAccountManager)) {
            Boolean v = (Boolean)this.getFieldValue(FLD_isAccountManager);
            return (v != null)? v.booleanValue() : false;
        } else {
            return false;
        }
    }

    /**
    *** Returns true if this account is an account manager [see "getIsAccountManager()"] 
    **/
    public boolean isAccountManager()
    {
        return this.getIsAccountManager();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the manager id 
    *** @return The manager ID associated with this account
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getManagerID()
    {
        if (this.hasField(FLD_managerID)) {
            String v = (String)this.getFieldValue(FLD_managerID);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /**
    *** Sets the manager id 
    *** @param v The manager id to set
    **/
    public void setManagerID(String v)
    {
        if (this.hasField(FLD_managerID)) {
            this.setFieldValue(FLD_managerID, StringTools.trim(v));
        }
    }

    /**
    *** Returns true if ManagerID is defined.
    *** Returns false if this Account is not managed by an AccountManager.
    *** @return True if the ManagerID is defined
    **/
    public boolean hasManagerID() 
    {
        return !StringTools.isBlank(this.getManagerID());
    }

    /**
    *** Returns true if the ManagerID is defined, but this account is not an Account Manager
    **/
    public boolean isManaged()
    {
        return (this.hasManagerID() && !this.isAccountManager())? true : false;
    }

    // --------------------------------

    /**
    *** Gets the AccountManager AccountID(s). (does not return null)
    *** The returned array will be empty (non-null) if no AccountManagers were found.
    *** Ideally, this returned array will have at-most one accountID entry, however it is possible 
    *** to have more than one AccountManager for an account.
    *** @return An array of Account IDs.  Will be empty if no explicit AccountManagers were found.
    ***     May have more than one entry if multiple AccountManagers were found.
    **/
    public String[] getManagerAccountID()
        throws DBException
    {
        return Account.getManagerAccountID(this.getManagerID());
    }

    /**
    *** Gets the AccountManager AccountID(s). (does not return null)
    *** The returned array will be empty (non-null) if no AccountManagers were found.
    *** Ideally, this returned array will have at-most one accountID entry, however it is possible 
    *** to have more than one AccountManager for an account.
    *** @return An array of Account IDs.  Will be empty if no explicit AccountManagers were found.
    ***     May have more than one entry if multiple AccountManagers were found.
    **/
    public static String[] getManagerAccountID(DBReadWriteMode rwMode, String managerID)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* get non-blank managerID */
        if (StringTools.isBlank(managerID)) { // this.hasManagerID()
            // -- no manager, return active (blank AccountManager is always active)
            return new String[0]; // empty
        }

        /* AccountManager AccountIDs (should be only one, but multiple are possible) */
        Vector<String> mgrAcctIDList = new Vector<String>();

        /* read AccountManager for managerID */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* assemble DBSelect ... */
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(
                Account.FLD_accountID,
                Account.FLD_isAccountManager,
                Account.FLD_managerID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(Account.FLD_managerID,managerID),
                    dwh.NE(Account.FLD_isAccountManager,0)  // true
                )
            ));

            /* read records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                // -- should have one-and-only-one record, but multiple are possible
                String acctID = rs.getString(Account.FLD_accountID);
                mgrAcctIDList.add(acctID);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account ManagerID list", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return */
        if (ListTools.isEmpty(mgrAcctIDList)) {
            return new String[0];
        } else {
            return mgrAcctIDList.toArray(new String[mgrAcctIDList.size()]);
        }

    }
    // --
    @Deprecated
    public static String[] getManagerAccountID(String managerID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getManagerAccountID(rwMode, managerID);
    }

    // --------------------------------

    private Account _accountManagers[] = null; // cache

    /**
    *** Gets the AccountManager Account(s). (does not return null)
    *** The returned array will be empty (non-null) if no AccountManagers were found.
    *** Ideally, this returned array will have at-most one accountID entry, however it is possible 
    *** to have more than one AccountManager for an account.
    *** @return An array of Accounts.  Will be empty if no explicit AccountManagers were found.
    ***     May have more than one entry if multiple AccountManagers were found.
    **/
    public Account[] getManagerAccounts(DBReadWriteMode rwMode)
        throws DBException
    {
        if (this._accountManagers == null) {
            this._accountManagers = Account.getManagerAccounts(rwMode, this.getManagerID());
        }
        return this._accountManagers;
    }
    // --
    @Deprecated
    public Account[] getManagerAccounts()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getManagerAccounts(rwMode);
    }

    /**
    *** Gets the AccountManager Account(s). (does not return null)
    *** The returned array will be empty (non-null) if no AccountManagers were found.
    *** Ideally, this returned array will have at-most one Account entry, however it is possible 
    *** to have more than one AccountManager for an account.
    *** @return An array of Accounts.  Will be empty if no explicit AccountManagers were found.
    ***     May have more than one entry if multiple AccountManagers were found.
    **/
    public static Account[] getManagerAccounts(DBReadWriteMode rwMode, String managerID) // getFirstManagerAccount()
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* get non-blank managerID */
        if (StringTools.isBlank(managerID)) { // this.hasManagerID()
            // -- no manager, return active (blank AccountManager is always active)
            return new Account[0]; // empty
        }

        /* AccountManager Accounts (should be only one, but multiple are possible) */
        Vector<Account> mgrAcctList = new Vector<Account>();

        /* read AccountManager for managerID */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* assemble DBSelect ... */
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(Account.FLD_managerID,managerID),
                    dwh.NE(Account.FLD_isAccountManager,0)  // true
                )
            ));

            /* read records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                // -- should have one-and-only-one record, but multiple are possible
                String acctID = rs.getString(Account.FLD_accountID);
                Account acct = new Account(new Account.Key(acctID));
                acct.setAllFieldValues(rs);
                mgrAcctList.add(acct);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Manager list", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return */
        if (ListTools.isEmpty(mgrAcctList)) {
            return new Account[0];
        } else {
            return mgrAcctList.toArray(new Account[mgrAcctList.size()]);
        }

    }
    // --
    @Deprecated
    public static Account[] getManagerAccounts(String managerID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getManagerAccounts(rwMode, managerID);
    }

    // --------------------------------

    /**
    *** Gets the first manager Account for this account.
    *** Returns null if no manager accounts defined
    **/
    public Account getFirstManagerAccount()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* is managed? */
        if (!this.hasManagerID()) {
            // -- this account is not managed
            return null;
        }

        /* am I already an account manager? */
        if (this.getIsAccountManager()) {
            // -- this account manages itself
            return this;
        }

        /* get AccountManagers */
        Account mgrAccts[] = this.getManagerAccounts(rwMode);
        if (ListTools.isEmpty(mgrAccts)) {
            // -- no managers found.
            return null;
        }

        /* return first entry */
        return mgrAccts[0];

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the associated AccountManager for this account is active.
    *** If more than one AccountManager is found, returns true if any associated AccountManager is active.
    *** @return True if the associated AccountManager is active.  
    ***     Also returns true if this account does not have an associated AccountManager.
    **/
    public boolean getIsAccountManagerActive() // getIsActive()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* get AccountManagers */
        Account mgrAccts[] = this.getManagerAccounts(rwMode); // may be cached
        if (ListTools.isEmpty(mgrAccts)) {
            // -- no managers found.
            return this.isManaged()? false : true; // not-active if managed
        }

        /* loop through managers */
        for (Account mAcct : mgrAccts) {
            if (!mAcct.getIsActive()) {
                // -- at least one manager was not-active
                return false;
            }
        }

        /* all managers are active */
        return true;

    }

    // --------------------------------

    /**
    *** Returns true if the associated AccountManager for this account is suspended.
    *** If more than one AccountManager is found, returns true if any associated AccountManager is suspended.
    *** @return True if the associated AccountManager is suspended.  
    ***     Returns false (not suspended) if this account does not have an associated AccountManager.
    **/
    public boolean getIsAccountManagerSuspended() // getIsSuspended()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* get AccountManagers */
        Account mgrAccts[] = this.getManagerAccounts(rwMode); // may be cached
        if (ListTools.isEmpty(mgrAccts)) {
            // -- no managers found.
            return this.isManaged()? true : false; // suspended if managed
        }

        /* loop through managers */
        for (Account mAcct : mgrAccts) {
            if (mAcct.getIsSuspended()) {
                // -- at least one manager was suspended
                return true;
            }
        }

        /* all managers are not-suspended */
        return false;

    }

    // --------------------------------

    /**
    *** Returns true if the associated AccountManager for this account is expired.
    *** If more than one AccountManager is found, returns true if any associated AccountManager is expired.
    *** @return True if the associated AccountManager is expired.  
    ***     Returns false (not expired) if this account does not have an associated AccountManager.
    **/
    public boolean getIsAccountManagerExpired() // getIsExpired()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* get AccountManagers */
        Account mgrAccts[] = this.getManagerAccounts(rwMode); // may be cached
        if (ListTools.isEmpty(mgrAccts)) {
            // -- no managers found.
            return this.isManaged()? true : false; // expired if managed
        }

        /* loop through managers */
        for (Account mAcct : mgrAccts) {
            if (mAcct.getIsExpired()) {
                // -- at least one manager was expired
                return true;
            }
        }

        /* all managers are not-expired */
        return false;

    }

    // --------------------------------

    /**
    *** Gets an array of accountIDs managed by the specified managerID.
    *** Returns an empty array if the managerID is not found.
    **/
    public static String[] getManagedAccountIDs(DBReadWriteMode rwMode, String managerID)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* get non-blank managerID */
        if (StringTools.isBlank(managerID)) {
            // -- no manager, return empty array
            return new String[0]; // empty
        }

        /* managed AccountIDs */
        Vector<String> acctIDList = new Vector<String>();

        /* read AccountManager for managerID */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* assemble DBSelect ... */
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(
                Account.FLD_accountID,
                Account.FLD_isAccountManager,
                Account.FLD_managerID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.EQ(Account.FLD_managerID,managerID)
            ));

            /* read records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctID = rs.getString(Account.FLD_accountID);
                int    isMgr  = rs.getInt(Account.FLD_isAccountManager);
                acctIDList.add(acctID);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting managed accountIDs", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return */
        if (ListTools.isEmpty(acctIDList)) {
            return new String[0];
        } else {
            return acctIDList.toArray(new String[acctIDList.size()]);
        }

    }
    // --
    @Deprecated
    public static String[] getManagedAccountIDs(String managerID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getManagedAccountIDs(rwMode, managerID);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if billing fields are supported
    **/
    public static boolean SupportsBilling()
    {
        DBFactory<Account> dbFact = Account.getFactory();
        return dbFact.hasField(FLD_costPerDeviceActive);
    }

    /**
    *** Gets the Billing cost per active Device per Month in this account
    *** @return The Billing cost per active Device per Month in this account
    **/
    public double getCostPerDeviceActive()
    {
        Double v = (Double)this.getFieldValue(FLD_costPerDeviceActive);
        return (v != null)? v.doubleValue() : 0.0;
    }

    /**
    *** Sets the Billing cost per active Device per Month in this account
    *** @param v The Billing cost per active Device per Month in this account
    **/
    public void setCostPerDeviceActive(double v)
    {
        this.setFieldValue(FLD_costPerDeviceActive, (v >= 0.0)? v : 0.0);
    }

    // --------------------------------

    /**
    *** Gets the Billing cost per inactive Device in this account
    *** @return The Billing cost per inactive Device in this account
    **/
    public double getCostPerDeviceInactive()
    {
        Double v = (Double)this.getFieldValue(FLD_costPerDeviceInactive);
        return (v != null)? v.doubleValue() : 0.0;
    }

    /**
    *** Sets the Billing cost per inactive Device in this account
    *** @param v The Billing cost per inactive Device in this account
    **/
    public void setCostPerDeviceInactive(double v)
    {
        this.setFieldValue(FLD_costPerDeviceInactive, (v >= 0.0)? v : 0.0);
    }

    // --------------------------------

    /**
    *** Gets the cost per device
    **/
    public double getCostPerDevice(boolean active)
    {
        if (active) {
            return this.getCostPerDeviceActive();
        } else {
            return this.getCostPerDeviceInactive();
        }
    }

    // --------------------------------

    /**
    *** Gets the cost per Device per Month.
    *** Looks for a cost per device from the following Accounts (and in this order)
    ***   1) The specified Account
    ***   2) The Account Manager
    ***   3) The "sysadmin" Account
    **/
    public static double getAccountCostPerDevice(Account acct, boolean active)
        throws DBException
    {

        /* Check Account */
        if (acct != null) {
            double costPerDev = Account._getAccountCostPerDevice(acct, active);
            if (costPerDev > 0.0) {
                return costPerDev;
            }
        }

        /* Check AccountManager */
        if ((acct != null) && acct.isManaged()) {
            double costPerDev = Account._getManagerCostPerDevice(acct.getManagerID(), active);
            if (costPerDev > 0.0) {
                return costPerDev;
            }
        }

        /* Check "sysadmin" Account */
        double costPerDev = Account._getSysAdminCostPerDevice(active);
        if (costPerDev > 0.0) {
            return costPerDev;
        }

        /* no cost found */
        return 0.0;

    }

    /**
    *** Gets the cost per Device per Month from the Account only
    **/
    protected static double _getAccountCostPerDevice(Account acct, boolean active)
        throws DBException
    {
        if (acct != null) {
            double costPerDev = acct.getCostPerDevice(active);
            if (costPerDev > 0.0) {
                return costPerDev;
            }
        }
        return 0.0;
    }

    // --------------------------------

    /**
    *** Gets the AccountManager cost per Device per Month
    *** Looks for a cost per device from the following Accounts (and in this order)
    ***   1) The Account Manager
    ***   2) The "sysadmin" Account
    **/
    public static double getManagerCostPerDevice(String managerID, boolean active)
        throws DBException
    {

        /* Check AccountManager */
        if (!StringTools.isBlank(managerID)) {
            double costPerDev = Account._getManagerCostPerDevice(managerID, active);
            if (costPerDev > 0.0) {
                return costPerDev;
            }
        }

        /* Check "sysadmin" Account */
        double costPerDev = Account._getSysAdminCostPerDevice(active);
        if (costPerDev > 0.0) {
            return costPerDev;
        }

        /* no cost found */
        // -- not from the manager, and not from the sysatmin
        return 0.0;

    }

    /**
    *** Gets the AccountManager cost per Device per Monthfrom the AccountManager only.
    **/
    protected static double _getManagerCostPerDevice(String managerID, boolean active)
        throws DBException
    {
        if (!StringTools.isBlank(managerID)) {
            String mgrAcctIDs[] = Account.getManagerAccountID(managerID);
            if (!ListTools.isEmpty(mgrAcctIDs)) {
                double costPerDev = 0.0;
                // -- iterator through manager accounts and get the maximum cost per device
                for (String maID : mgrAcctIDs) {
                    Account a = Account.getAccount(maID); // throws DBException
                    if (a != null) {
                        costPerDev = Math.max(costPerDev, a.getCostPerDevice(active));
                    } else {
                        // -- unlikely
                        Print.logError("Unable to find manager Account: " + maID);
                    }
                }
                // -- found a >0 cost?
                if (costPerDev > 0.0) {
                    return costPerDev;
                }
            } else {
                Print.logError("Unable to locate AccountManager for ID: " + managerID);
            }
        }
        return 0.0;
    }

    // --------------------------------

    /**
    *** Gets the SystemAdministator cost per Device per Month.
    **/
    public static double getSysAdminCostPerDevice(boolean active)
        throws DBException
    {
        double costPerDev = Account._getSysAdminCostPerDevice(active);
        if (costPerDev > 0.0) {
            return costPerDev;
        }
        return 0.0;
    }

    /**
    *** Gets the SystemAdministator cost per Device per Month from the SysAdmin only
    **/
    protected static double _getSysAdminCostPerDevice(boolean active)
        throws DBException
    {
        Account sysAdminAcct = AccountRecord.getSystemAdminAccount();
        if (sysAdminAcct != null) {
            double costPerDev = sysAdminAcct.getCostPerDevice(active);
            if (costPerDev > 0.0) {
                return costPerDev;
            }
        } else {
            Print.logError("Unable to locate 'sysadmin' Account: " + AccountRecord.getSystemAdminAccountID());
        }
        return 0.0;
    }

    // ------------------------------------------------------------------------

    /* get the data request passcode */
    public String getRequestPassCode()
    {
        if (this.hasField(FLD_requestPassCode)) {
            String v = (String)this.getFieldValue(FLD_requestPassCode);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /* set the data request passcode */
    public void setRequestPassCode(String v)
    {
        if (this.hasField(FLD_requestPassCode)) {
            this.setFieldValue(FLD_requestPassCode, StringTools.trim(v));
        }
    }

    // ------------------------------------------------------------------------

    /* gets the valid data request IP address */
    public DTIPAddrList getRequestIPAddress()
    {
        if (this.hasField(FLD_requestIPAddress)) {
            DTIPAddrList v = (DTIPAddrList)this.getFieldValue(FLD_requestIPAddress);
            return v; // May return null!!
        } else {
            return null;
        }
    }

    /* sets the valid data request IP address */
    public void setRequestIPAddress(DTIPAddrList v)
    {
        if (this.hasField(FLD_requestIPAddress)) {
            this.setFieldValue(FLD_requestIPAddress, v);
        }
    }

    /* sets the valid data request IP address */
    public void setRequestIPAddress(String v)
    {
        if (this.hasField(FLD_requestIPAddress)) {
            this.setRequestIPAddress((v != null)? new DTIPAddrList(v) : null);
        }
    }

    /* returns true if the specified IP address matches the saved valid IP address */
    public boolean isValidRequestIPAddress(String ipAddr)
    {
        DTIPAddrList ipList = this.getRequestIPAddress();
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

    /* returns true if the dataPushURL field is supported */
    public static boolean SupportsDataPushURL()
    {
        DBFactory<Account> dbFact = Account.getFactory();
        return dbFact.hasField(FLD_dataPushURL);
    }
    
    /* get the data push URL */
    public String getDataPushURL()
    {
        if (this.hasField(FLD_dataPushURL)) {
            String v = (String)this.getFieldValue(FLD_dataPushURL);
            return StringTools.trim(v);
        } else {
            return "";
        }
    }

    /* set the data push URL */
    public void setDataPushURL(String v)
    {
        if (this.hasField(FLD_dataPushURL)) {
            this.setFieldValue(FLD_dataPushURL, StringTools.trim(v));
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if DataPush is enabled for this Account
    **/
    public boolean isDataPushEnabled()
    {
        return !StringTools.isBlank(this.getDataPushURL())? true : false;
    }

    /**
    *** Returns true if DataPush is enabled for this Account
    **/
    public static boolean IsDataPushEnabled(Account account)
    {
        return (account != null)? account.isDataPushEnabled() : false;
    }

    // ------------------------------------------------------------------------

    public long getLastDataRequestTime()
    {
        if (this.hasField(FLD_lastDataRequestTime)) {
            Long v = (Long)this.getFieldValue(FLD_lastDataRequestTime);
            return (v != null)? v.longValue() : 0L;
        } else {
            return 0L;
        }
    }

    public void setLastDataRequestTime(long v)
    {
        if (this.hasField(FLD_lastDataRequestTime)) {
            this.setFieldValue(FLD_lastDataRequestTime, v);
        }
    }

    // ------------------------------------------------------------------------

    public long getLastDataPushTime()
    {
        if (this.hasField(FLD_lastDataPushTime)) {
            Long v = (Long)this.getFieldValue(FLD_lastDataPushTime);
            return (v != null)? v.longValue() : 0L;
        } else {
            return 0L;
        }
    }

    public void setLastDataPushTime(long v)
    {
        if (this.hasField(FLD_lastDataPushTime)) {
            this.setFieldValue(FLD_lastDataPushTime, v);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if ELog/HOS is enabled for the specified Account
    *** @return True if ELog/HOS is enabled for the specified Account
    **/
    public static boolean IsELogEnabled(Account acct)
    {
        return Account.IsELogEnabled(acct, false);
    }

    /**
    *** Returns true if ELog/HOS is enabled for the specified Account
    *** @return True if ELog/HOS is enabled for the specified Account
    **/
    public static boolean IsELogEnabled(Account acct, boolean showReason)
    {

        /* has ELog/HOS package */ 
        if (!DBConfig.hasELogPackage()) {
            if (showReason) { Print.logDebug("ELog/HOS not supported"); }
            return false;
        }

        /* check account */
        if (acct == null) {
            if (showReason) { Print.logDebug("Account is null"); }
            return false;
        } else
        if (acct.isDeleted()) {
            if (showReason) { Print.logDebug("Account is deleted: " + acct.getAccountID()); }
            return false;
        } else
        if (!acct.isActive()) {
            if (showReason) { Print.logDebug("Account is inactive: " + acct.getAccountID()); }
            return false;
        } else
        if (acct.isExpired()) {
            if (showReason) { Print.logDebug("Account is expired: " + acct.getAccountID()); }
            return false;
        } else
        if (!acct.getELogEnabled()) {
            if (showReason) { Print.logDebug("Account ELog is not enabled: " + acct.getAccountID()); }
            return false;
        }

        /* enabled */
        return true;

    }

    /**
    *** Returns true if ELog/HOS is enabled for this Account
    *** @return True if ELog/HOS is enabled for this Account
    **/
    public boolean getELogEnabled()
    {
        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_eLogEnabled);
        return (v != null)? v.booleanValue() : false;
    }

    /**
    *** Sets the "ELog/HOS Enabled" state for this Account
    *** @param v The "ELog/HOS Enabled" state for this Account
    **/
    public void setELogEnabled(boolean v)
    {
        this.setOptionalFieldValue(FLD_eLogEnabled, v);
    }

    // --------------------------------

    /**
    *** Returns true if ELog AccountID/PartnerID is present
    **/
    public boolean hasELogAccountID()
    {
        return !StringTools.isBlank(this.getELogAccountID());
    }

    /**
    *** Gets the ELog/HOS account id (partnerID)
    **/
    public String getELogAccountID()
    {
        String v = (String)this.getOptionalFieldValue(FLD_eLogAccountID);
        return (v != null)? v : "";
    }

    /**
    *** Sets the ELog/HOS account id
    **/
    public void setELogAccountID(String v)
    {
        this.setOptionalFieldValue(FLD_eLogAccountID, StringTools.trim(v));
    }

    // --------------------------------

    /**
    *** Returns true if ELog Login/Username is present
    **/
    public boolean hasELogUsername()
    {
        return !StringTools.isBlank(this.getELogUsername());
    }

    /**
    *** Gets the ELog/HOS Login/Username (loginID)
    **/
    public String getELogUsername()
    {
        String v = (String)this.getOptionalFieldValue(FLD_eLogUsername);
        return (v != null)? v : "";
    }

    /**
    *** Sets the ELog/HOS Login/Username
    **/
    public void setELogUsername(String v)
    {
        this.setOptionalFieldValue(FLD_eLogUsername, StringTools.trim(v));
    }

    // --------------------------------

    /**
    *** Gets the ELog/HOS password 
    **/
    public String getELogPassword()
    {
        String p = (String)this.getOptionalFieldValue(FLD_eLogPassword);
        return (p != null)? p : "";
    }

    /**
    *** Sets the ELog/HOS password 
    **/
    public void setELogPassword(String p)
    {
        this.setOptionalFieldValue(FLD_eLogPassword, ((p != null)? p : ""));
    }

    // --------------------------------

    /**
    *** Gets the ELog/HOS properties 
    **/
    public String getELogProperties()
    {
        String p = (String)this.getOptionalFieldValue(FLD_eLogProperties);
        return (p != null)? p : "";
    }

    /**
    *** Sets the ELog/HOS properties 
    **/
    public void setELogProperties(String p)
    {
        this.setOptionalFieldValue(FLD_eLogProperties, ((p != null)? p : ""));
    }

    /**
    *** Gets the ELog/HOS properties as an RTProperties object
    **/
    public RTProperties getELogRTProperties()
    {
        return new RTProperties(this.getELogProperties());
    }

    /**
    *** Sets the ELog/HOS properties from an RTProperties object
    **/
    public void setELogRTProperties(RTProperties rtp)
    {
        if (rtp != null) {
            this.setELogProperties(rtp.toString());
        } else {
            //this.setELogProperties(""); <-- do not clear properties here
        }
    }

    /**
    *** Gets a specific ELog/HOS property value
    **/
    public String getELogProperty(String key, String dft)
    {
        RTProperties rtp = this.getELogRTProperties();
        return rtp.getString(key,dft);
    }

    /**
    *** Sets a specific ELog/HOS property value
    **/
    public void setELogProperty(String key, String val)
    {
        RTProperties rtp = this.getELogRTProperties();
        rtp.setString(key, val);
        this.setELogRTProperties(rtp);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets an array of Odometer Maintenance Descriptions
    **/
    @JSONBeanGetter(expandArray="true", ignore="$blank")
    public String[] getMaintLabelKM()
    {
        int cnt = Device.getMaintCountKM();
        String maintLabel[] = new String[cnt];
        for (int i = 0; i < maintLabel.length; i++) {
            maintLabel[i] = this.getMaintLabelKM(i);
        }
        return maintLabel;
    }

    /**
    *** Gets the odometer maintenance description for the specified index
    *** @param ndx  The index of the odometer maintenance description to return
    *** @return The odometer maintenance description for the specified index
    **/
    public String getMaintLabelKM(int ndx)
    {
        return this.getMaintLabelKM(ndx, ("OdomMaint#" + ndx));
    }

    /**
    *** Gets the odometer maintenance description for the specified index
    *** @param ndx  The index of the odometer maintenance description to return
    *** @param dft  The default label/description to return if none found
    *** @return The odometer maintenance description for the specified index
    **/
    public String getMaintLabelKM(int ndx, String dft)
    {
        String v;
        // -- 0) invalid index?
        if (ndx < 0) {
            return "";
        }
        // -- 1) try "Account.maintLabelKM#" field
        v = this.getOptionalFieldValue(FLD_maintLabelKM_+ndx, (String)null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 2) try runtime property
        v = RTConfig.getString(DBConfig.PROP_Account_maintenanceLabelKM_+ndx, null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 3) return default
        return dft;
    }

    /**
    *** Sets the odometer maintenance label for the specified index
    *** @param ndx  The index of the odometer maintenance label to set
    *** @param v    The label/description
    **/
    public void setMaintLabelKM(int ndx, String v)
    {
        this.setOptionalFieldValue(FLD_maintLabelKM_+ndx, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets an array of engine-hours Maintenance Descriptions
    **/
    @JSONBeanGetter(expandArray="true", ignore="$blank")
    public String[] getMaintLabelHR()
    {
        int cnt = Device.getMaintCountHR();
        String maintLabel[] = new String[cnt];
        for (int i = 0; i < maintLabel.length; i++) {
            maintLabel[i] = this.getMaintLabelHR(i);
        }
        return maintLabel;
    }

    /**
    *** Gets the engine-hours maintenance description for the specified index
    *** @param ndx  The index of the engine-hours maintenance description to return
    *** @return The engine-hours maintenance description for the specified index
    **/
    public String getMaintLabelHR(int ndx)
    {
        return this.getMaintLabelHR(ndx, ("HoursMaint#" + ndx));
    }

    /**
    *** Gets the engine-hours maintenance description for the specified index
    *** @param ndx  The index of the engine-hours maintenance description to return
    *** @param dft  The default label/description to return if none found
    *** @return The engine-hours maintenance description for the specified index
    **/
    public String getMaintLabelHR(int ndx, String dft)
    {
        String v;
        // -- 0) invalid index?
        if (ndx < 0) {
            return "";
        }
        // -- 1) try "Device.maintLabelHR#" field
        v = this.getOptionalFieldValue(FLD_maintLabelHR_+ndx, (String)null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 2) try runtime property
        v = RTConfig.getString(DBConfig.PROP_Account_maintenanceLabelHR_+ndx, null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 3) return default
        return dft;
    }

    /**
    *** Sets the engine-hours maintenance description for the specified index
    *** @param ndx  The index of the engine-hours maintenance description to set
    *** @param v    The label/description
    **/
    public void setMaintLabelHR(int ndx, String v)
    {
        this.setOptionalFieldValue(FLD_maintLabelHR_+ndx, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets an array of fixed-time Maintenance Descriptions
    **/
    @JSONBeanGetter(expandArray="true", ignore="$blank")
    public String[] getMaintLabelFT()
    {
        int cnt = Device.getMaintCountHR();
        String maintLabel[] = new String[cnt];
        for (int i = 0; i < maintLabel.length; i++) {
            maintLabel[i] = this.getMaintLabelFT(i);
        }
        return maintLabel;
    }

    /**
    *** Gets the fixed-time maintenance description for the specified index
    *** @param ndx  The index of the fixed-time maintenance description to return
    *** @return The fixed-time maintenance description for the specified index
    **/
    public String getMaintLabelFT(int ndx)
    {
        return this.getMaintLabelFT(ndx, ("TimeMaint#" + ndx));
    }

    /**
    *** Gets the fixed-time maintenance descrption for the specified index
    *** @param ndx  The index of the fixed-time maintenance description to return
    *** @param dft  The default label/description to return if none found
    *** @return The fixed-time maintenance description for the specified index
    **/
    public String getMaintLabelFT(int ndx, String dft)
    {
        String v;
        // -- 0) invalid index?
        if (ndx < 0) {
            return "";
        }
        // -- 1) try "Device.maintLabelFT#" field
        v = this.getOptionalFieldValue(FLD_maintLabelFT_+ndx, (String)null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 2) try runtime property
        v = RTConfig.getString(DBConfig.PROP_Account_maintenanceLabelFT_+ndx, null);
        if (!StringTools.isBlank(v)) {
            return v;
        }
        // -- 3) return default
        return dft;
    }

    /**
    *** Sets the fixed-time maintenance description for the specified index
    *** @param ndx  The index of the fixed-time maintenance description to set
    *** @param v    The label/description
    **/
    public void setMaintLabelFT(int ndx, String v)
    {
        this.setOptionalFieldValue(FLD_maintLabelFT_+ndx, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /** (NOT CURRENTLY USED)
    *** Returns true if this user is a dispatcher
    *** @return True if this user is a dispatcher
    **/
    public boolean getIsDispatcher()
    {
	    if (this.isSystemAdmin()) {
	        return true;
	    } else {
	        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_isDispatcher);
	        return (v != null)? v.booleanValue() : false;
	    }
    }

    /** (NOT CURRENTLY USED)
    *** Sets the "Dispatcher" state for this User
    *** @param v The "Dispatcher" state for this User
    **/
    public void setIsDispatcher(boolean v)
    {
        this.setOptionalFieldValue(FLD_isDispatcher, v);
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {

        /* always active */
        this.setIsActive(true);

        /* Description and default geocoder mode */
        if (this.isSystemAdmin()) {
            this.setDescription("System Administrator");
            this.setPrivateLabelName(BasicPrivateLabel.ALL_HOSTS);
            this.setGeocoderMode(GeocoderMode.FULL);
            this.setIsAccountManager(true);
        } else {
            this.setDescription("New Account [" + this.getAccountID() + "]");
            this.setGeocoderMode(GeocoderMode.FULL); // <-- default to FULL for now
            this.setIsBorderCrossing(Account.SupportsBorderCrossing());
            String plk[] = this.getDefaultFieldValueKey(Account.FLD_privateLabelName);
            this.setPrivateLabelName(RTConfig.hasProperty(plk,false)? RTConfig.getString(plk,"") : BasicPrivateLabel.ALL_HOSTS);
            this.setIsAccountManager(false);
        }

        /* Notify enabled */
        //this.setAllowNotify(RTConfig.getBoolean(DBConfig.PROP_Device_checkAccountAllowNotify,false));
        this.setAllowNotify(true);

        /* SMS enabled? */
        switch (Account.GetDefaultSmsEnabledState()) {
            case FALSE:
                this.setSmsEnabled(false);
                break;
            case TRUE:
                this.setSmsEnabled(true);
                break;
        }

        /* clear account manager */
        this.setManagerID("");

        /* password */
        String newPass = Account.createRandomPassword(16);
        this.setDecodedPassword(null, newPass, true); // setPassword

        /* allow overriding values from runtime configuration */
        super.setRuntimeDefaultValues();

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the Account-ID
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter()
    //public String getAccountID() <-- is "final" in AccountRecord
    //{
    //    return super.getAccountID();
    //}

    /**
    *** Gets the Account Description
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(name="accountName")
    public String getAccountDescription()
    {
        return super.getAccountDescription();
    }

    /* return the display name */
    @JSONBeanGetter(ignore="$blank")
    public final String getDisplayName()
    {
        return super.getDisplayName();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the isActive state of this record
    **/
    @JSONBeanGetter()
    public boolean getIsActive()
    {
        return super.getIsActive();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the "admin" User-ID
    *** [UserInformation interface]
    **/
    @JSONBeanGetter()
    public String getUserID()
    {
        return User.getAdminUserID(); // "admin"
    }

    /**
    *** Gets the User Description ("Administrator")
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(name="userName")
    public String getUserDescription()
    {
        Locale loc = this.getLocale();
        I18N i18n = I18N.getI18N(Account.class, loc);
        return i18n.getString("Account.administrator", "Administrator");
    }

    /**
    *** Returns true (Account user is always "admin")
    *** [UserInformation interface]
    **/
    @JSONBeanGetter()
    public boolean isAdminUser()
    {
        return true;
    }

    /**
    *** Returns Gender.UNKNOWN ("0") (Account user is currently unspecified)
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(ignore="$zero")
    public int getGender()
    {
        return User.Gender.UNKNOWN.getIntValue();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets all active/authorized devices for the "admin" user
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter(name="deviceIDs")
    public java.util.List<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        boolean inclInactv = false;
        return Device.getDeviceIDsForAccount(rwMode, this.getAccountID(), null, inclInactv);
    }
    // -- 
    @Deprecated
    public java.util.List<String> getAuthorizedDeviceIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAuthorizedDeviceIDs(rwMode);
    }

    /**
    *** Gets a list of authorized devices, with names
    **/
    //@JSONBeanGetter(name="devices")
    public java.util.List<DeviceProvider> getAuthorizedDevices(DBReadWriteMode rwMode)
        throws DBException
    {
        Account account    = this;
        User    user       = null;
        boolean inclInactv = false;
        return User.getAuthorizedDevices(rwMode, account, user, inclInactv);
    }
    // -- 
    @Deprecated
    public java.util.List<DeviceProvider> getAuthorizedDevices()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAuthorizedDevices(rwMode);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if this Account has an "admin" user 
    *** @return True if this Account has an "admin" user 
    **/
    public boolean hasAdminUser()
    {
        try {
            return User.exists(this.getAccountID(), User.getAdminUserID());
        } catch (DBException dbe) {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    private User currentUser = null;

    /**
    *** Sets the current context user 
    *** @param user  The current context user
    **/
    public void setCurrentUser(User user)
    {
        this.currentUser = user;
    }

    /**
    *** Gets the current context user 
    *** @return  The current context user
    **/
    @JSONBeanGetter(ignore="$null")
    public User getCurrentUser()
    {
        return this.currentUser; // may be null
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the email address to which reports should be emailed
    *** @param account  The account to test for email address, if user not specified
    *** @param user     The overriding user to test for email address first
    *** @return The report email address, or null if no address was found
    **/
    public static String getReportEmailAddress(Account account, User user)
    {
        return (account != null)? account.getReportEmailAddress(user) : null;
    }

    /**
    *** Returns the email address to which reports should be emailed
    *** @param user  The overriding user to test for email address first
    *** @return The report email address, or null if no address was found
    **/
    public String getReportEmailAddress(User user)
    {

        /* try user email address */
        if ((user != null) && user.getAccountID().equals(this.getAccountID())) {
            // Notification email address
            String notifyEmail = user.getNotifyEmail();
            if (!StringTools.isBlank(notifyEmail)) {
                return notifyEmail;
            }
            // Contact email address
            String contactEmail = user.getContactEmail();
            if (!StringTools.isBlank(contactEmail)) {
                return contactEmail;
            }
        }

        /* try account "admin" email address */
        try {
            User adminUser = User.getUser(this, User.getAdminUserID());
            if (adminUser != null) {
                // Notification email address
                String notifyEmail = adminUser.getNotifyEmail();
                if (!StringTools.isBlank(notifyEmail)) {
                    return notifyEmail;
                }
                // Contact email address
                String contactEmail = adminUser.getContactEmail();
                if (!StringTools.isBlank(contactEmail)) {
                    return contactEmail;
                }
            }
        } catch (DBException dbe) {
            Print.logError("Error retrieving Admin user: " + dbe);
        }

        /* Account */
        // Notification email address
        String notifyEmail = this.getNotifyEmail();
        if (!StringTools.isBlank(notifyEmail)) {
            return notifyEmail;
        }
        // Contact email address
        String contactEmail = this.getContactEmail();
        if (!StringTools.isBlank(contactEmail)) {
            return contactEmail;
        }

        /* still not found */
        return null;

    }
    
    // ------------------------------------------------------------------------

    /* get device */
    // Note: may return null if device was not found
    /*
    public Device getDevice(String devID)
        throws DBException
    {
        return Device.getDevice(this, devID);
    }
    */

    /* get number of devices for this account */
    @JSONBeanGetter()
    public long getDeviceCount()
    {
        return this.getDeviceCount(false);
    }

    /* get number of devices for this account */
    public long getDeviceCount(boolean activeOnly)
    {
        return this.getDeviceCount(activeOnly? 1 : 0);
    }

    /* get number of devices for this account */
    public long getDeviceCount(int activeState)
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        try {
            //Print.logInfo("Retrieving count: " + dsel);
            DBWhere dwh = new DBWhere(Device.getFactory());
            String where;
            if (activeState <= 0) {
                // -- all devices in account
                where = dwh.WHERE_(
                    dwh.EQ(Device.FLD_accountID,this.getAccountID())
                );
            } else
            if (activeState == 1) {
                // -- active only
                where = dwh.WHERE_(
                    dwh.AND(
                        dwh.EQ(Device.FLD_accountID,this.getAccountID()),
                        dwh.NE(Device.FLD_isActive,0) // is active
                    )
                );
            } else
            if (activeState == 2) {
                // -- inactive only
                where = dwh.WHERE_(
                    dwh.AND(
                        dwh.EQ(Device.FLD_accountID,this.getAccountID()),
                        dwh.EQ(Device.FLD_isActive,0) // is inactive
                    )
                );
            } else {
                // -- all devices in account
                where = dwh.WHERE_(
                    dwh.EQ(Device.FLD_accountID,this.getAccountID())
                );
            }
            return DBRecord.getRecordCount(rwMode, Device.getFactory(), where);
        } catch (DBException dbe) {
            Print.logException("Unable to retrieve Device count", dbe);
            return -1L;
        }
    }

    /* return true if any device has a recent 'lastNotifyTime' ["Save/Alert" action] */
    public boolean hasDeviceLastNotifySince(long sinceTime, User user)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* simple case */
        if (sinceTime < 0L) {
            return true;
        }

        /* invalid User? */
        if ((user != null) && !user.getAccountID().equals(this.getAccountID())) {
            // -- User account does not match device account (unlikely to occur)
            Print.logError("User Account does not match Device Account");
            return false;
        }

        /* read devices for account with (lastNotifyTime > sinceTime) */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        boolean    found = false;
        try {

            /* select */
            // -- DBSelect: SELECT * FROM Device WHERE (accountID='acct') ORDER BY accountID,deviceID
            DBSelect<Device> dsel = new DBSelect<Device>(Device.getFactory());
            dsel.setSelectedFields(
                Device.FLD_accountID,
                Device.FLD_deviceID,
                Device.FLD_lastNotifyTime,
                Device.FLD_lastNotifyCode);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(Device.FLD_accountID,this.getAccountID()),
                    dwh.GT(Device.FLD_lastNotifyTime,sinceTime),
                    dwh.NE(Device.FLD_isActive,0) // active
                )
            ));
            dsel.setOrderByFields(
                Device.FLD_accountID,
                Device.FLD_deviceID);
            dsel.setLimit(1L);

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode); // getDBConnection_read
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String accountID  = rs.getString(Device.FLD_accountID);
                String deviceID   = rs.getString(Device.FLD_deviceID);
                long   notifyTime = rs.getLong(Device.FLD_lastNotifyTime);
                int    notifyCode = rs.getInt(Device.FLD_lastNotifyCode);
                if ((user != null) && !user.isAuthorizedDevice(rwMode,deviceID)) {
                    // User is not authorized for this device
                    Print.logDebug("User '"+accountID+"/"+user.getUserID()+"' not authorized to device: " + deviceID);
                    continue;
                }
                Print.logDebug("Found Device with recent notification: "+accountID+"/"+deviceID+" ==> "+notifyTime+":"+StatusCodes.GetHex(notifyCode));
                found = true;
                break;
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Device 'lastNotifyTime'", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return results */
        return found;

    }

    /* return true if any device has a recent 'lastNotifyTime' (READ database assumed) */
    // Note: for optimum lookup, the following property should be specified:
    //  Device.keyedLastNotifyTime=true
    public static boolean hasAnyDeviceLastNotifySince(long sinceTime)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* simple case */
        if (sinceTime < 0L) {
            return true;
        }

        /* read devices for account with (lastNotifyTime > sinceTime) */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        boolean    found = false;
        try {

            /* select */
            // DBSelect: SELECT * FROM Device WHERE (accountID='acct') ORDER BY accountID,deviceID
            DBSelect<Device> dsel = new DBSelect<Device>(Device.getFactory());
            dsel.setSelectedFields(
                Device.FLD_accountID,
                Device.FLD_deviceID,
                Device.FLD_lastNotifyTime,
                Device.FLD_lastNotifyCode);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(Device.FLD_lastNotifyTime,sinceTime),
                    dwh.NE(Device.FLD_isActive,0)
                )
            ));
            dsel.setOrderByFields(
                Device.FLD_accountID,
                Device.FLD_deviceID);
            dsel.setLimit(1L);

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                found = true;
                String accountID  = rs.getString(Device.FLD_accountID);
                String deviceID   = rs.getString(Device.FLD_deviceID);
                long   notifyTime = rs.getLong(Device.FLD_lastNotifyTime);
                int    notifyCode = rs.getInt(Device.FLD_lastNotifyCode);
                Print.logInfo("Found Device with recent notification: "+accountID+"/"+deviceID+" ==> "+notifyTime+":"+StatusCodes.GetHex(notifyCode));
                break;
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Device 'lastNotifyTime'", sqe);
        } finally {
            DBConnection.release(dbc, stmt, rs);
        }

        /* return results */
        return found;

    }

    // ------------------------------------------------------------------------
    
    /* convert field value units */
    public Object convertFieldUnits(DBField field, Object value, boolean inclUnits, Locale locale)
    {

        /* unconvertable */
        if ((field == null) || (value == null)) {
            return value;
        }
        String fldName = field.getName();

        /* field unit type */
        String unitAtt = field.getStringAttribute(DBField.ATTR_UNITS, null);
        if (StringTools.isBlank(unitAtt)) {
            return value;
        }

        /* DBFactory */
        //DBFactory dbFact = field.getFactory();
        //if (dbFact == null) {
        //    return value;
        //}
        //String tblName = dbFact.getTableName();

        /* speed */
        if (unitAtt.equalsIgnoreCase("speed")) {
            SpeedUnits units = Account.getSpeedUnits(this);
            double val = units.convertFromKPH(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* distance */
        if (unitAtt.equalsIgnoreCase("distance")) {
            DistanceUnits units = Account.getDistanceUnits(this);
            double val = units.convertFromKM(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* volume */
        if (unitAtt.equalsIgnoreCase("volume")) {
            VolumeUnits units = Account.getVolumeUnits(this);
            double val = units.convertFromLiters(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* temperature */
        if (unitAtt.equalsIgnoreCase("temp")) {
            TemperatureUnits units = Account.getTemperatureUnits(this);
            double val = units.convertFromC(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* economy */
        if (unitAtt.equalsIgnoreCase("econ")) {
            EconomyUnits units = Account.getEconomyUnits(this);
            double val = units.convertFromKPL(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* percent */
        if (unitAtt.equalsIgnoreCase("percent")) {
            double dval = StringTools.parseDouble(value,0.0);
            double val = (dval < 0.0)? 0.0 : (dval > 1.50)? dval : (dval * 100.0);
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0"));
                return valFmt + "%";
            } else {
                return new Double(val);
            }
        }

        /* pressure [2.6.4-B18] */
        if (unitAtt.equalsIgnoreCase("pressure")) {
            Account.PressureUnits units = Account.getPressureUnits(this);
            double val = units.convertFromKPa(StringTools.parseDouble(value,0.0));
            if (inclUnits) {
                String valFmt = StringTools.format(val, field.getFormat("0.0"));
                return valFmt + " " + units.toString(locale);
            } else {
                return new Double(val);
            }
        }

        /* default to specified field value */
        return value;
        
    }

    // ------------------------------------------------------------------------

    /* return the AccountID */
    public String toString()
    {
        return this.getAccountID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private BasicPrivateLabel privateLabel = null;
    private boolean           foundPrivateLabel = false;

    /**
    *** Gets the PrivateLabel instance for the specified account. 
    **/
    public static BasicPrivateLabel getPrivateLabel(Account acct)
    {
        return (acct != null)? acct.getPrivateLabel() : null;
    }

    /**
    *** Gets the PrivateLabel instance for this account 
    *** Returns a default instance if this account does not have a defined PrivateLabel.
    *** Does not return null.
    **/
    public BasicPrivateLabel getPrivateLabel()
    {
        if (this.privateLabel == null) {
            String bplName = this.getPrivateLabelName();
            this.privateLabel = BasicPrivateLabelLoader.getPrivateLabel(bplName); // may return default 
            if (this.privateLabel == null) {
                // -- can occur if no "default" PrivateLabel is defined
                Print.logWarn("PrivateLabel not defined! [" + bplName + "]");
                this.privateLabel = new BasicPrivateLabel("null");
                this.foundPrivateLabel = false;
            } else {
                //Print.logInfo("PrivateLabel found: " + bplName + " ==> " + this.privateLabel.getName());
                this.foundPrivateLabel = true;
            }
        }
        return this.privateLabel;
    }

    /**
    *** Returns the specified context PrivateLabel, if non-null.
    *** Otherwise return account BasicPrivateLabel 
    *** Does not return null.
    **/
    public BasicPrivateLabel getPrivateLabel(BasicPrivateLabel contextBPL)
    {
        return (contextBPL != null)? contextBPL : this.getPrivateLabel();
    }

    /**
    *** Returns true if this Account has a valid/defined PrivateLabel 
    **/
    public boolean hasPrivateLabel()
    {
        this.getPrivateLabel();
        return this.foundPrivateLabel;
    }

    /**
    *** Gets the current active PrivateLabel name.
    *** If not currently in a web-context, the Account PrivateLabel name will be returned
    **/
    public BasicPrivateLabel getContextPrivateLabel()
    {
        String bplName = this.getContextPrivateLabelName();
        BasicPrivateLabel bpl = BasicPrivateLabelLoader.getPrivateLabel(bplName);
        if (bpl == null) {
            Print.logWarn("PrivateLabel not defined! [" + bplName + "]");
            bpl = this.getPrivateLabel();
        }
        return bpl;
    }

    /**
    *** Gets the current active PrivateLabel.
    *** If not currently in a web-context, the Account PrivateLabel will be returned
    **/
    public static BasicPrivateLabel getContextPrivateLabel(Account account)
    {
        return (account != null)? 
            account.getContextPrivateLabel() : 
            Account.getSessionPrivateLabel();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Locale for the current Account.
    **/
    @JSONBeanGetter()
    public String getLocaleString()
    {
        BasicPrivateLabel bpl = this.getPrivateLabel();
        if (bpl != null) {
            return bpl.getLocaleString();
        } else {
            // --- will not occur
            return "";
        }
    }

    /**
    *** Gets the Locale for the current Account.
    **/
    public Locale getLocale()
    {
        BasicPrivateLabel bpl = this.getPrivateLabel();
        if (bpl != null) {
            return bpl.getLocale();
        } else {
            // --- will not occur
            return null;
        }
    }

    /**
    *** Gets the Locale for the specified Account.
    *** Returns null if the Account is null
    **/
    public static Locale GetLocale(Account acct)
    {
        return (acct != null)? acct.getLocale() : null;
    }

    // ------------------------------------------------------------------------

    /* return the "Device" titles */
    public String[] getDeviceTitles(Locale loc)
    {
        return this.getDeviceTitles(loc, Device.GetTitles(loc));
    }

    /* return the "Device" titles */
    public String[] getDeviceTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_DEVICE, dft);
    }
    
    /* set device title string */
    public void setDeviceTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE;
            String desc  = "Device Title"; // no need to localize here
            if (StringTools.isBlank(plural)) { plural = singular; }
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Device: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the default new device description */
    public String getNewDeviceDescription()
    {
        return this.getNewDeviceDescription(null, "");
    }
    
    /* return the default new device description */
    public String getNewDeviceDescription(Locale loc, String dftDesc)
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        //I18N i18n = I18N.getI18N(Account.class, loc);
        try {
            AccountString str = AccountString.getAccountString(rwMode, this, AccountString.ID_DEVICE_NEW_DESCRIPTION);
            return ((str != null) && str.hasSingularTitle())? str.getSingularTitle() : dftDesc;
        } catch (DBException dbe) {
            return dftDesc;
        }
    }

    /* set the default new device description */
    public void setNewDeviceDescription(String singular)
    {
        this.setNewDeviceDescription(singular, null);
    }
    
    /* set the default new device description */
    public void setNewDeviceDescription(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE_NEW_DESCRIPTION;
            String desc  = "New Device Description"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Device: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the "Device Group" titles */
    public String[] getDeviceGroupTitles(Locale loc)
    {
        return this.getDeviceGroupTitles(loc, DeviceGroup.GetTitles(loc));
    }

    /* return the "Device Group" titles */
    public String[] getDeviceGroupTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_DEVICE_GROUP, dft);
    }

    /* set device group title string */
    public void setDeviceGroupTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_DEVICE_GROUP;
            String desc  = "Device Group Title"; // no need to localize here
            if (StringTools.isBlank(plural)) { plural = singular; }
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save DeviceGroup title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a list of explicitly authorized device group IDs assigned to this user 
    **/
    //@JSONBeanGetter()
    public java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        // -- Account users are "admin" users, by default.
        // -  "admin" users shoudl have no explicitly assigned DeviceGroups.
        return new OrderedSet<String>();
    }
    // --
    @Deprecated
    public java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getExplicitlyAuthorizedDeviceGroupIDs(rwMode);
    }

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    //@JSONBeanGetter(name="DeviceGroupIDs")
    public java.util.List<String> getAllAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        return User.getAllAuthorizedDeviceGroupIDs(rwMode, this.getAccountID(), null);
    }
    // --
    @Deprecated
    public java.util.List<String> getAllAuthorizedDeviceGroupIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAllAuthorizedDeviceGroupIDs(rwMode);
    }

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    @JSONBeanGetter(name="DeviceGroups")
    public java.util.List<DeviceGroupProvider> getAllAuthorizedDeviceGroups(DBReadWriteMode rwMode)
        throws DBException
    {
        return User.getAllAuthorizedDeviceGroups(rwMode, this, null);
    }
    // --
    @Deprecated
    public java.util.List<DeviceGroupProvider> getAllAuthorizedDeviceGroups()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAllAuthorizedDeviceGroups(rwMode);
    }

    // ------------------------------------------------------------------------

    /* return the "Entity" title (ie "Trailer") */
    public String[] getEntityTitles(Locale loc)
    {
        String T[] = this.getEntityTitles(loc, null);
        return (T != null)? T : (new String[] { "", "" });
    }

    /* return the "Entity" title (ie "Trailer") */
    public String[] getEntityTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_ENTITY, dft);
    }

    /* set entity title string */
    public void setEntityTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_ENTITY;
            String desc  = "Entity Title"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Entity title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------

    /* return the "Address" titles (ie "Address", "Landmark", etc.) */
    public String[] getAddressTitles(Locale loc)
    {
        String T[] = this.getAddressTitles(loc, null);
        return (T != null)? T : (new String[] { "", "" });
    }

    /* return the "Address" titles (ie "Address", "Landmark", etc.) */
    public String[] getAddressTitles(Locale loc, String dft[])
    {
        return AccountString.getStringsArray(this, AccountString.ID_ADDRESS, dft);
    }

    /* set address title string */
    public void setAddressTitle(String singular, String plural)
    {
        try {
            String strID = AccountString.ID_ADDRESS;
            String desc  = "Address Title"; // no need to localize here
            AccountString.updateAccountString(this,strID,desc,singular,plural);
        } catch (DBException dbe) {
            Print.logError("Unable to save Address title: " + dbe);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* returns an array of all account IDs (or null if an error occurs) */
    public static String[] getAllAccountIDs(DBReadWriteMode rwMode)
    {
        try {
            return ListTools.toArray(Account.getAllAccounts(rwMode), String.class);
        } catch (DBException dbe) {
            return null;
        }
    }
    // --
    @Deprecated
    public static String[] getAllAccountIDs()
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAllAccountIDs(rwMode);
    }

    /* return list of all Account IDs (may not be scalable) */
    public static Collection<String> getAllAccounts(DBReadWriteMode rwMode)
        throws DBException
    {
        return Account.getAllAccounts(rwMode, (DBSelect<Account>)null);
    }
    // --
    @Deprecated
    public static Collection<String> getAllAccounts()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAllAccounts(rwMode);
    }

    /* return list of all active Account IDs */
    public static Collection<String> getAllActiveAccounts(DBReadWriteMode rwMode)
        throws DBException
    {
        DBFactory<Account> acctFact = Account.getFactory();
        DBSelect<Account> dsel = new DBSelect<Account>(acctFact);
        DBWhere dwh = new DBWhere(acctFact);
        dwh.append(dwh.NE(Account.FLD_isActive, 0));
        dsel.setWhere(dwh);
        dsel.setSelectedFields(Account.FLD_accountID);
        dsel.setOrderByFields(Account.FLD_accountID);
        return Account.getAllAccounts(rwMode, dsel);
    }
    // --
    @Deprecated
    public static Collection<String> getAllActiveAccounts()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAllActiveAccounts(rwMode);
    }

    /* return list of all Account IDs which have a non-blank dataPushURL (may not be scalable) */
    public static Collection<String> getDataPushAccountIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        DBFactory<Account> acctFact = Account.getFactory();
        DBSelect<Account> dsel = new DBSelect<Account>(acctFact);
        DBWhere dwh = new DBWhere(acctFact);
        dwh.append(dwh.NE(Account.FLD_dataPushURL, ""));
        dsel.setWhere(dwh);
        dsel.setSelectedFields(Account.FLD_accountID);
        dsel.setOrderByFields(Account.FLD_accountID);
        return Account.getAllAccounts(rwMode, dsel);
    }
    // --
    @Deprecated
    public static Collection<String> getDataPushAccountIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getDataPushAccountIDs(rwMode);
    }

    /* return list of all Account IDs (may not be scalable) */
    public static Collection<String> getAllAccounts(DBReadWriteMode rwMode, String selWhere)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        if (StringTools.isBlank(selWhere)) {
            return Account.getAllAccounts(rwMode, (DBSelect<Account>)null);
        } else {
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE(selWhere));
            dsel.setSelectedFields(Account.FLD_accountID);
            dsel.setOrderByFields(Account.FLD_accountID);
            return Account.getAllAccounts(rwMode, dsel);
        }
    }
    // --
    @Deprecated
    public static Collection<String> getAllAccounts(String selWhere)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAllAccounts(rwMode, selWhere);
    }

    /* return list of all Account IDs */
    public static Collection<String> getAllAccounts(DBReadWriteMode rwMode, DBSelect<Account> dsel)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* default selection? */
        if (dsel == null) {
            // -- DBSelect: SELECT accountID FROM Account 
            dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            dsel.setOrderByFields(Account.FLD_accountID);
        }

        /* read accounts */
        Collection<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList;

    }
    // --
    @Deprecated
    public static Collection<String> getAllAccounts(DBSelect<Account> dsel)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAllAccounts(rwMode, dsel);
    }


    /* return list of all Account IDs */
    public static Collection<String> getAuthorizedAccounts(DBReadWriteMode rwMode, Account account)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* invalid account */
        if (account == null) {
            return new Vector<String>(); // TODO: should be immutable
        }

        /* SysAdmin Account? */
        if (account.isSystemAdmin()) {
            // -- all accounts
            return Account.getAllAccounts(rwMode);
        }

        /* inactive account? */
        if (!account.getIsActive()) {
            // -- not active, not even authorized to self
            return new Vector<String>(); // TOD: should be immutable
        }

        /* manager account? */
        if (account.isAccountManager()) {
            String managerID = account.getManagerID();
            if (!StringTools.isBlank(managerID)) {
                // -- guaranteed to have at least 'account' in the list
                DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
                dsel.setSelectedFields(Account.FLD_accountID, Account.FLD_managerID);
                dsel.setOrderByFields(Account.FLD_accountID);
                DBWhere dwh = dsel.createDBWhere();
                dsel.setWhere(dwh.WHERE(dwh.EQ(Account.FLD_managerID, managerID)));
                return Account.getAllAccounts(rwMode, dsel);
            }
        }

        /* only authorized to self */
        Collection<String> acctList = new Vector<String>();
        acctList.add(account.getAccountID());
        return acctList;

    }
    // --
    @Deprecated
    public static Collection<String> getAuthorizedAccounts(Account account)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAuthorizedAccounts(rwMode, account);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Account-ID exists
    *** @param acctID  The Account-ID to test for existance
    *** @return True if the specified account exists, false otherwise.
    **/
    public static boolean exists(String acctID)
        throws DBException // if error occurs while testing existence
    {
        if (acctID != null) {
            Account.Key actKey = new Account.Key(acctID);
            return actKey.exists(DBReadWriteMode.READ_WRITE);
        }
        return false;
    }

    /**
    *** Gets an Account with the specified Account-ID.  Returns null if the 
    *** Account ID does not exist
    *** @param acctID  The Account-ID to retrieve
    *** @return The retrieved Account, or null if the Account does not exist.
    **/
    public static Account getAccount(DBReadWriteMode rwMode, String acctID)
        throws DBException // if error occurs while getting record
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        if (StringTools.isBlank(acctID)) {
            // -- invalid AccountID specified
            //Print.logError("Account-ID is null/blank");
            return null;
        } else {
            Account.Key key = new Account.Key(acctID);
            if (key.exists(rwMode)) {
                return key._getDBRecord(true, rwMode);
            } else {
                // -- Account does not exist
                return null;
            }
        }
    }
    // --
    //@Deprecated
    public static Account getAccount(String acctID)
        throws DBException // if error occurs while getting record
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAccount(rwMode, acctID);
    }

    /** 
    *** Gets or creates an Account with the specified Account-ID
    *** @param acctID  The Account ID to get or create.
    *** @param create  True to create a nee account, false to get an existing account
    *** @return The created/retrieved Account (does not return null, not yet saved)
    *** @throws DBException if the account already exists and 'create' was specified,
    ***         or if the account does not exist and 'create' was not specified.
    **/
    public static Account getAccount(DBReadWriteMode rwMode, String acctID, boolean create)
        throws DBException
    {

        /* account-id specified? */
        if (StringTools.isBlank(acctID)) {
            // always throw an exception
            throw new DBNotFoundException("Account-ID not specified.");
        }

        /* adjust DBReadWriteMode */
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        if (create && rwMode.equals(DBReadWriteMode.READ_WRITE)) {
            rwMode = DBReadWriteMode.READ_WRITE;
        }

        /* get/create account */
        Account acct = null;
        Account.Key acctKey = new Account.Key(acctID);
        if (!acctKey.exists(rwMode)) { // may throw DBException
            if (!create) {
                // -- accountID does not already exist, and we are not creating it
                throw new DBNotFoundException("Account-ID does not exists '" + acctKey + "'");
            } else
            if (Account.IsReservedAccountID(acctID)) {
                // -- accountID is reserved, return that it already exists
                throw new DBAlreadyExistsException("Account-ID is reserved '" + acctKey + "'");
            } else {
                // -- created, but not yet saved
                acct = acctKey._getDBRecord();
                acct.setCreationDefaultValues();
                return acct; // not yet saved!
            }
        } else
        if (create) {
            // -- we've been asked to create the account, and it already exists
            throw new DBAlreadyExistsException("Account-ID already exists '" + acctKey + "'");
        } else {
            acct = Account.getAccount(rwMode, acctID); // may throw DBException
            if (acct == null) {
                throw new DBException("Unable to read existing Account-ID '" + acctKey + "'");
            }
            return acct;
        }

    }
    // --
    //@Deprecated
    public static Account getAccount(String acctID, boolean create)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAccount(rwMode, acctID, create);
    }

    /**
    *** Creates a new Account with the specified ID and password
    *** @param acctMgr The creating account
    *** @param acctID  The account ID to create
    *** @param passwd  The account password
    *** @return The created account
    *** @throws DBException if an error occurs, or if account already exists
    **/
    public static Account createNewAccount(Account acctMgr, String acctID, String passwd)
        throws DBException
    {

        /* validate account id */
        if (StringTools.isBlank(acctID)) {
            throw new DBException("Invalid AccountID specified");
        }

        /* create account */
        Account acct = Account.getAccount(DBReadWriteMode.READ_WRITE, acctID, true); // not yet saved

        /* set password */
        if (passwd != null) { // empty string allowed
            acct.setDecodedPassword(null, passwd, true);
        }

        /* account manager */
        if (acctMgr != null) {
            if (!Account.isSystemAdmin(acctMgr) && !Account.isAccountManager(acctMgr)) {
                throw new DBNotAuthorizedException("Not Authorized to create accounts");
            }
            acct.setPrivateLabelName(acctMgr.getPrivateLabelName());
            acct.setManagerID(acctMgr.getManagerID());
            acct.setGeocoderMode(acctMgr.getGeocoderMode());
            acct.setIsBorderCrossing(acctMgr.getIsBorderCrossing());
            acct.setDcsPropertiesID(acctMgr.getDcsPropertiesID());
            acct.setSmsEnabled(acctMgr.getSmsEnabled());
            //acct.setSmsProperties(acctMgr.getSmsProperties()); // comment/remove this line
        }

        /* save and return */
        acct.save();
        return acct;

    }
    
    /**
    *** Creates a temporary account
    *** @param accountID         The AccountID used for the temporary account. If null/blank a random accountid will be assigned.
    *** @param expireDays        The number of days the account will be available.
    *** @param contactName       The account contact name
    *** @param contactEmail      The account contact email address
    *** @param privateLabelName  The assigned PrivateLabel name
    *** @return The created/saved account
    **/
    public static Account createTemporaryAccount(
        String accountID, int expireDays, String encPass,
        String contactName, String contactEmail, 
        String privateLabelName)
        throws DBException
    {
        Account account = null;
        long nowTime    = DateTime.getCurrentTimeSec();
        long expireSec  = (expireDays > 0L)? DateTime.DaySeconds(expireDays) : Account.DFT_EXPIRATION_SEC;
        if (expireSec > Account.MAX_EXPIRATION_SEC) { expireSec = Account.MAX_EXPIRATION_SEC; }
        long expireTime = (new DateTime((nowTime + expireSec),DateTime.getGMTTimeZone())).getDayEnd();

        // -- make sure we're creating only one account at a time
        synchronized (TempAccountLock) {
            if (!StringTools.isBlank(accountID)) {
                // -- create specific accountID
                if (Account.IsReservedAccountID(accountID)) {
                    // -- accountID is reserved
                    Print.logError("Account-ID is reserved: " + accountID);
                    account = null;
                } else {
                    Account.Key acctKey = new Account.Key(accountID);
                    if (acctKey.exists(DBReadWriteMode.READ_WRITE)) { // may throw DBException
                        // -- acountID already exists
                        Print.logError("AccountID already exists: " + accountID);
                        account = null;
                    } else {
                        account = acctKey._getDBRecord();
                    }
                }
            } else {
                // -- This temporary account number has granularity of 3 seconds and 
                // -  only repeats every 34 days.
                long tval = (nowTime % DateTime.DaySeconds(34)) / 3; // 0..979200
                // -- we make 3 attempts to create a unique account name
                account = null;
                for (int i = 0; i < 3; i++) {
                    String acctID = "T" + StringTools.format(tval+i,"000000"); // T106052
                    Account.Key acctKey = new Account.Key(acctID);
                    if (!acctKey.exists(DBReadWriteMode.READ_WRITE)) { // may throw DBException
                        account = acctKey._getDBRecord();
                        break;
                    }
                    // -- try again
                }
            }
        }

        /* set/save account */
        if (account != null) {
            account.setCreationDefaultValues();
            account.setDescription(contactName);
            account.setContactEmail(contactEmail);
            account.setContactName(contactName);
            account.setMaximumDevices(1L);
            account.setPasswdQueryTime(nowTime);
            account.setEncodedPassword(encPass);
            account.setExpirationTime(expireTime);
            account.setPrivateLabelName(privateLabelName);
            account.save();
        }

        return account;
    }

    /**
    *** A list of characters from which the random password is generated
    *** (vowels omitted to avoid spelling words :-)
    **/
    private static final String RANDOM_PASSWORD_ALPHABET = "0123456789bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ.-_@";

    /**
    *** Creates a random password with the specified number of characters
    *** @param length  The length of the created password
    *** @return The created password.
    **/
    public static String createRandomPassword(int length)
    {
        return StringTools.createRandomString(length, RANDOM_PASSWORD_ALPHABET);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the UserInformation interface instance for the specified AccountID/UserID
    **/
    public static UserInformation getUserInformation(String acctID, String userID, boolean activeOnly)
        throws DBException
    {

        /* AccountID invalid? */
        if (!AccountRecord.isValidID(acctID)) {
            // -- AccountID invalid (possibly blank)
            return null;
        }

        /* get Account */
        Account account = Account.getAccount(acctID);
        if (account == null) {
            // -- AccountID not found, no UserInformation
            return null;
        }

        /* default userID? */
        if (StringTools.isBlank(userID)) {
            // -- get default login userID
            userID = account.getDefaultUser();
            if (StringTools.isBlank(userID)) {
                userID = account.getPrivateLabel().getDefaultLoginUser(); // non-blank
            }
        }

        /* UserID invalid? */
        if (!AccountRecord.isValidID(userID)) {
            // -- non-blank UserID is invalid
            return null;
        }

        /* get user */
        User user = User.getUser(account, userID);
        if ((user == null) && !User.isAdminUser(userID)) {
            // -- a null user is only allowed for an "admin" user
            return null; // non-admin userID was not found
        }

        /* UserInformation */
        UserInformation userInfo = (user != null)? user : account;

        /* return active only? */
        if (activeOnly && !Account.GetActiveStatus(userInfo).isActive()) {
            // -- Account/User is not active
            return null;
        }

        /* return UserInformation instance */
        return userInfo;

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a list of Account-IDs managed by the specified contact email address
    *** @param emailAddr  The contact email address
    *** @return A list of Accounts managed by the specifgied contact email address
    *** @throws DBException if a database error occurs
    **/
    public static java.util.List<String> getAccountIDsForContactEmail(String emailAddr)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* EMailAddress specified? */
        if (StringTools.isBlank(emailAddr)) {
            throw new DBException("Contact EMail address not specified");
        }

        /* read accounts for contact email */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM <TableName> WHERE (contactEmail='email')
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE(dwh.EQ(Account.FLD_contactEmail,emailAddr)));
            // Note: The index on the column FLD_contactEmail is not unique
            // (since null/empty values are allowed and needed)
    
            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(FLD_accountID);
                if (acctList == null) {
                    acctList = new Vector<String>();
                }
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Account ContactEmail", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        return acctList;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets all Account IDs that have devices matching to the specified DeviceID.
    *** @param devID  The Device-ID of the devices for which to search
    *** @return A list of AccountIDs which define the specified DeviceID
    *** @throws DBException if a database error occurs
    **/
    public static java.util.List<String> getAccountIDsForDeviceID(DBReadWriteMode rwMode, String devID)
        throws DBException
    {

        /* invalid id? */
        if (StringTools.isBlank(devID)) {
            return null; // just say it doesn't exist
        }

        /* read device for subscriber-id */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // -- DBSelect: SELECT accountID FROM Device WHERE (deviceID='device')
            DBSelect<Device> dsel = new DBSelect<Device>(Device.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(dwh.EQ(Device.FLD_deviceID,devID)));

            /* get record */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(FLD_accountID);
                if (acctList == null) {
                    acctList = new Vector<String>();
                }
                acctList.add(acctId);
            }
            // -- it's possible at this point that we haven't even read 1 device

        } catch (SQLException sqe) {
            throw new DBException("Getting Device Account-IDs: " + devID, sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list of accountIDs */
        // -- Note: may be null if no devices were found
        return acctList;

    }
    // --
    @Deprecated
    public static java.util.List<String> getAccountIDsForDeviceID(String devID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return Account.getAccountIDsForDeviceID(rwMode, devID);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns an array of Account-IDs that have been created more than 11 hours ago,
    *** and have not yet logged in to the system.
    *** @return An array of Account-IDs which have been created, but no-one has yet logged into them.
    **/
    public static String[] getUnconfirmedAccounts()
        throws DBException
    {
        return getUnconfirmedAccounts(Account.MAX_UNCONFIRMED_SEC);
    }

    /**
    *** Returns an array of Account-IDs that have been created more than 'ageSec' seconds ago,
    *** and have not yet logged in to the system.
    *** @param ageSec  The specified 'age' of an existing account, in seconds
    *** @return An array of Account-IDs which have been created, but no-one has yet logged into them.
    **/
    public static String[] getUnconfirmedAccounts(long ageSec)
        throws DBException
    {
        // -- Return temporary accounts that no one has logged into, and the creation time is greater than 'ageSec' seconds ago.
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* read unconfirmed accounts */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            long unconfirmedTime = DateTime.getCurrentTimeSec() - ageSec;

            /* select */
            // DBSelect: SELECT accountID FROM Account WHERE ((expirationTime > 0) AND (lastLoginTime = 0) AND (creationTime < time))
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(FLD_expirationTime,0L),              // temporary account (has expiration)
                    dwh.EQ(FLD_lastLoginTime,0L),               // never logged-in
                    dwh.LT(FLD_creationTime,unconfirmedTime)    // > 'ageSec' old
                )
            ));

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Unconfirmed Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList.toArray(new String[acctList.size()]);

    }
    
    // ------------------------------------------------------------------------

    /**
    *** Returns an array of currently expired Account-IDs
    *** @return An array of currently expired Account-IDs
    **/
    public static String[] getExpiredAccounts()
        throws DBException
    {
        // return active, currently expired, accounts (for purposes of deactivation)
        return getExpiredAccounts(0L, true);
        // the returned accounts can be passed to 'deactivateAccounts(...)' for deactivation
    }

    /**
    *** Returns an array of account-ids which are expired.
    *** @param deltaSec  The number of seconds specifying a range which represents the
    ***                 Accounts which are due to expire within the next 'deltaSec' seconds.
    *** @return An array of Account IDs matching the expiration criteria
    **/
    public static String[] getExpiredAccounts(long deltaSec)
        throws DBException
    {
        // return inactive, past expired (more than deltaSec seconds ago), accounts (for purposes of deletion)
        return getExpiredAccounts(deltaSec, false);
        // the returned accounts can be passed to 'deleteAccounts(...)' for deletion
    }
    
    /**
    *** Returns an array of account-ids which are expired, or active, depending on the
    *** value specified for 'activeState'.
    *** @param deltaSec  The number of seconds specifying a range which represents the
    ***                 Accounts which are due to expire within the next 'deltaSec' seconds.
    *** @param activeState  Accounts matching the specified 'active' state will be returned.
    *** @return An array of Account IDs matching the expiration criteria
    **/
    public static String[] getExpiredAccounts(long deltaSec, boolean activeState)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* read unconfirmed accounts */
        java.util.List<String> acctList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            long expTime = DateTime.getCurrentTimeSec() - deltaSec;

            /* select */
            // DBSelect: SELECT accountID FROM Account WHERE ((expirationTime > 0) AND (expirationTime < time))
            DBSelect<Account> dsel = new DBSelect<Account>(Account.getFactory());
            dsel.setSelectedFields(Account.FLD_accountID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.GT(FLD_expirationTime,0L),          // temporary account (has expiration)
                    dwh.LT(FLD_expirationTime,expTime),     // never logged-in
                    dwh.EQ(FLD_isActive,activeState)        // active/inactive
                )
            ));

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String acctId = rs.getString(Account.FLD_accountID);
                acctList.add(acctId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get Expired Account List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return acctList.toArray(new String[acctList.size()]);

    }

    // ------------------------------------------------------------------------

    /**
    *** Deactivates the specified list of Accounts.  A deactivated account is not
    *** able to log-in to the system.
    *** @param acctID  An array of account-ids to deactivate
    **/
    public static void deactivateAccounts(String acctID[])
        throws DBException
    {
        if (acctID != null) {
            for (int i = 0; i < acctID.length; i++) {
                Account account = Account.getAccount(acctID[i]); // may return null
                if (account != null) {
                    Print.logInfo("Deactivating account: " + acctID[i]);
                    if (account.getIsActive()) {
                        account.setIsActive(false);
                        account.save();
                    } else {
                        // already inactive
                    }
                } else {
                    Print.logWarn("[Deactivate] Account not found: " + acctID[i]);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    
    /**
    *** Deletes the specified Accounts, including all owned Devices, Users, Events, etc.
    *** @param acctID  An array of Account-IDs to delete
    **/
    public static void deleteAccounts(String acctID[])
        throws DBException
    {
        if (acctID != null) {
            for (int i = 0; i < acctID.length; i++) {
                Account.Key acctKey = new Account.Key(acctID[i]);
                Print.logWarn("Deleting Account: " + acctID[i]);
                acctKey.delete(true); // will also delete dependencies
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Backup the specified managerID Accounts to disk.
    *** (READ database assumed)
    **/
    public static long backupAllAccountIDs(DBReadWriteMode rwMode, File dumpDir, int maxEventDataDays)
        throws DBException
    {
        // -- Account backup table files will be placed into directories as follows:
        // -    DUMP_DIR/ACCOUNT_ID/...
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* dumpDir must be an existing directory */
        if (!FileTools.isDirectory(dumpDir)) {
            // -- dump directory must exist
            throw new DBException("Backup directory does not exist: " + dumpDir);
        }

        /* get list of all account IDs */
        Collection<String> aidList = Account.getAllAccounts(rwMode);
        if (ListTools.isEmpty(aidList)) {
            throw new DBException("No accounts found");
        }

        /* all directory */
        File allDir = dumpDir;

        /* loop through accounts */
        long sizeB = 0L;
        for (String aid : aidList) {
            // -- backup one account at a time
            sizeB += Account.backupAccountID(rwMode, aid, allDir, maxEventDataDays);
        }

        /* return success */
        return sizeB;

    }

    /**
    *** Backup the specified managerID Accounts to disk
    **/
    public static long backupManagedAccountIDs(DBReadWriteMode rwMode, String managerID, File dumpDir, int maxEventDataDays)
        throws DBException
    {
        // -- Account backup table files will be placed into directories as follows:
        // -    DUMP_DIR/MANAGER_ID/ACCOUNT_ID/...
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* dumpDir must be an existing directory */
        if (!FileTools.isDirectory(dumpDir)) {
            // -- dump directory must exist
            throw new DBException("Backup directory does not exist: " + dumpDir);
        }

        /* get list of all managed accounts */
        String aidList[] = Account.getManagedAccountIDs(rwMode, managerID);
        if (ListTools.isEmpty(aidList)) {
            throw new DBException("No accounts with managerID: " + managerID);
        }

        /* manager directory */
        File managerDir = new File(dumpDir, managerID);
        if (managerDir.exists()) {
            // -- manager dump directory ok to exist
        } else
        if (!managerDir.mkdirs()) {
            // -- manager dump directory was not created
            throw new DBException("Unable to create manager backup directory: " + managerDir);
        }

        /* loop through accounts */
        long sizeB = 0L;
        for (String aid : aidList) {
            // -- backup one account at a time
            sizeB += Account.backupAccountID(rwMode, aid, managerDir, maxEventDataDays);
        }

        /* return success */
        return sizeB;

    }

    /**
    *** Backup the specified account to disk
    *** [EXPERIMENTAL - may fail on very large tables due to insufficient memory]
    *** @return The size of the backup files, in bytes
    **/
    public static long backupAccountID(DBReadWriteMode rwMode, String accountID, File dumpDir, int maxEventDataDays)
        throws DBException
    {
        // -- Account backup table files will be placed into directories as follows:
        // -    DUMP_DIR/ACCOUNT_ID/...
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* table backup file extension */
        String tableFileExt = ".txt"; // ".csv";

        /* dumpDir must be an existing directory */
        if (!FileTools.isDirectory(dumpDir)) {
            // -- dump directory must exist
            throw new DBException("Backup directory does not exist: " + dumpDir);
        }

        /* accountID must exist */
        Account account = Account.getAccount(accountID);
        if (account == null) {
            throw new DBException("AccountID does not exist: " + accountID);
        }

        /* create account dump directory (must not already exist) */
        File acctDir = new File(dumpDir, accountID);
        if (acctDir.exists()) {
            // -- account dump directory must not exist
            throw new DBException("Account backup directory already exists: " + acctDir);
        } else
        if (!acctDir.mkdirs()) {
            // -- account dump directory was not created
            throw new DBException("Unable to create account backup directory: " + acctDir);
        }

        /* header */
        // --------------------------------------------------
        // Account   : demo
        // Directory : /gts/backup/demo
        // Table     : Device [20 records] ...
        // Table     : Properties [no records] ...
        // Table     : SystemAudit [skipping] ...
        // Table     : EventData [12 Devices, last 90 days] ...
        //             ==> 1) demo1 ...
        //             ==> 2) demo2 ...
        // ... Done  : 52.3Kb [00:32:47]
        Print.logInfo("--------------------------------------------------");
        Print.logInfo("Account   : " + accountID);
        Print.logInfo("Directory : " + acctDir);

        /* file size accumulator */
        long sizeB   = 0L;
        long startMS = DateTime.getCurrentTimeMillis();

        /* create WHERE (accountID = "ACCOUNT") */
        DBFactory<Account> acctFact = Account.getFactory();
        DBWhere _acctWhere = new DBWhere(acctFact);
        String   acctWhere = _acctWhere.WHERE_(_acctWhere.EQ(Account.FLD_accountID,accountID));

        /* dump account table */
        File acctFile = new File(acctDir, acctFact.getUntranslatedTableName()+tableFileExt);
        Print.sysPrintln("Table     : " + Account.TABLE_NAME() + " ...");
        acctFact.dumpTable(acctFile, false, acctWhere);
        sizeB += acctFile.length();

        /* dependents */
        DBFactory<? extends DBRecord<?>> dependents[] = DBAdmin.getChildTableFactories(acctFact); // copy

        /* dump dependent tables */
        for (DBFactory<? extends DBRecord<?>> fact : dependents) {
            if (fact == null) {
                continue; // unlikely to be null
            }
            // -- table name
            String tableName = fact.getUntranslatedTableName();
            if (tableName.equals(EventData.TABLE_NAME())) {
                //Print.sysPrintln("   ["+tableName+"] delay until end ...");
                continue;
            } else
            if (tableName.equals("SystemAudit")) {
                // -- not backed up
                Print.sysPrintln("Table     : " + tableName + " [skipping] ...");
                continue;
            } else
            if (tableName.equals("NotifyQueue")) {
                // -- not backed up
                Print.sysPrintln("Table     : " + tableName + " [skipping] ...");
                continue;
            }
            // -- number of records
            long rcdCount = DBRecord.getRecordCount(fact,acctWhere);
            if (rcdCount <= 0) {
                Print.logInfo("Table     : " + tableName + " [no records] ...");
                continue;
            }
            // -- backup 
            Print.logInfo("Table     : " + tableName + " [" + rcdCount + " records] ...");
            File tableFile = new File(acctDir, tableName+tableFileExt);
            fact.dumpTable(tableFile, false, acctWhere);
            sizeB += tableFile.length();
        } // dependency tables

        /* list of Devices */
        OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(rwMode, accountID, null/*User*/, true/*inclInactv*/);
        int devCnt = ListTools.size(devIDList);

        /* dump EventData records */
        Print.logInfo("Table     : " + EventData.TABLE_NAME() + " [" + devCnt + " Devices, last " + maxEventDataDays + " days] ...");
        DBFactory<EventData> eventFact = EventData.getFactory();
        File tableFile = new File(acctDir, EventData.TABLE_NAME() + tableFileExt);
        long nowSec = DateTime.getCurrentTimeSec();
        boolean append = false; // false for first dumpTable
        int devNum = 0;
        for (String devID : devIDList) {
            // -- read Device
            Device device = Device.getDevice(account, devID); // account.getDevice(devID);
            if (device == null) { // unlikely to be null
                continue;
            }
            // -- last event
            EventData lastEV = device.getLastEvent(false);
            if (lastEV == null) {
                continue; // no events
            }
            // -- start time
            long startTime = nowSec - DateTime.DaySeconds(maxEventDataDays);
            if (lastEV.getTimestamp() < startTime) {
                startTime = lastEV.getTimestamp() - 1L;
            }
            // -- dump events for the last XX days
            DBWhere eventWhere = new DBWhere(eventFact); // dsel.createDBWhere();
            String eventWhereS = eventWhere.WHERE_(
                eventWhere.AND(
                    eventWhere.EQ(EventData.FLD_accountID,accountID),
                    eventWhere.EQ(EventData.FLD_deviceID ,devID    ),
                    eventWhere.GE(EventData.FLD_timestamp,startTime)
                ));
            Print.logInfo("            ==> " + (++devNum) + ") " + devID + " ...");
            eventFact.dumpTable(tableFile, append, eventWhereS); // 'append' false on first call
            append = true;
        }
        sizeB += tableFile.length();
        double sizeK   = (double)sizeB / 1024.0;
        long   elapSec = (DateTime.getCurrentTimeMillis() - startMS) / 1000L;
        String sizeStr = StringTools.format(sizeK,"0.0");
        String elapStr = StringTools.formatElapsedSeconds(elapSec,StringTools.ELAPSED_FORMAT_HHMMSS);
        Print.logInfo("... Done  : " + sizeStr + "Kb [" + elapStr + "]");

        /* success */
        return sizeB;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Retroactively goes back to the specified date range and reverse-geocodes
    *** any blank address it finds in the EventData table.
    **/
    public static void updateBlankEventAddresses(Collection<String> acctList, 
        long startTime, long endTime, 
        int maxPoolSize)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* start/end time */
        if (startTime <= 0L) {
            return;
        }
        if (endTime <= 0L) {
            endTime = DateTime.getCurrentTimeSec();
        }
        if (endTime <= startTime) {
            return;
        }

        /* all Accounts? */
        if ((acctList == null) || ListTools.containsIgnoreCase(acctList,"all")) {
            acctList = Account.getAllAccounts(rwMode);
        }

        /* interval */
        final long intervalSec = DateTime.HourSeconds(12);

        /* thread pool */
        int _maxPoolSize  = (maxPoolSize > 0)? maxPoolSize : 1;
        int _maxQueueSize = _maxPoolSize * 5;
        ThreadPool updateAddressPool = new ThreadPool("UpdateBlankAddress");
        updateAddressPool.setMaxPoolSize( _maxPoolSize);
        updateAddressPool.setMaxQueueSize(_maxQueueSize);

        /* DBWhere (address == "") */
        final DBWhere dwh = new DBWhere(EventData.getFactory());
        dwh.append(dwh.EQ(EventData.FLD_address,""));
        final String selectStr = dwh.toString();

        /* loop through accounts */
        accountUpdateAddr:
        for (String acctID : acctList) {
            // -- get Account
            final Account acct = Account.getAccount(acctID);
            if (acct == null) {
                // -- unlikely
                Print.logError("AccountID does not exist: " + acctID);
                continue accountUpdateAddr;
            } else
            if (Account.getGeocoderMode(acct).isNone()) {
                // -- no reverse-geocoding is performed for this Account
                continue accountUpdateAddr;
            }
            // -- get deviceIDs
            Collection<String> devList = Device.getDeviceIDsForAccount(rwMode,acctID,null/*User*/,false/*inclInactv*/);
            if (ListTools.isEmpty(devList)) {
                // -- no devices
                continue accountUpdateAddr;
            }
            // -- loop through devices 
            for (final String devID : devList) {
                // -- create address-update job
                final long _startTime = startTime;
                final long _endTime   = endTime;
                Runnable devUpdAddr = new Runnable() {
                    public void run() {
                        try {
                            Account._updateBlankEventAddresses_Device(
                                acct, devID, 
                                _startTime, _endTime, 
                                intervalSec,
                                selectStr);
                        } catch (DBException dbe) {
                            Print.logError("Error while updating blank addresses: " + dbe);
                        }
                    }
                };
                // -- add job to queue
                while (!updateAddressPool.run(devUpdAddr)) {
                    // -- job not added, wait and try again
                    if (updateAddressPool.isStoppingNow()) {
                        // -- stop-threads-now in process, return now
                        Print.logWarn("ThreadPool is stopping ...");
                        return;
                    }
                    // -- sleep
                    Print.logDebug("ThreadPool queue is full, waiting for Jobs to complete ...");
                    OSTools.sleepSec(5);
                }
                Print.logInfo("Queued Account/Device : " + acctID + "/" + devID);
            } // loop through devices
        } // loop through accounts

        /* done with adding jobs to queue, now wait for queue to finish */
        updateAddressPool.stopThreads(false);
        while (updateAddressPool.hasActiveJobs()) {
            OSTools.sleepMS(1000L);
        }

    }

    /**
    *** Retroactively goes back to the specified date range and reverse-geocodes
    *** any blank address it finds in the EventData table.
    **/
    private static boolean _updateBlankEventAddresses_Device(
        Account acct, String devID,
        long startTime, long endTime,
        long intervalSec,
        String selectStr)
        throws DBException
    {

        /* validate account/device */
        if (acct == null) {
            return false;
        } else
        if (StringTools.isBlank(devID)) {
            return false;
        }
        final String acctID = acct.getAccountID();
        final String acctDevID = acctID + "/" + devID;

        /* get Device */
        final Device device;
        try {
            device = Device.getDevice(acct, devID);
            if (device == null) {
                Print.logError("Device does not exist: "+acctDevID);
                return false;
            }
        } catch (DBException dbe) {
            Print.logError("Unable to read Device: "+acctDevID+" ["+dbe+"]");
            return false;
        }

        /* GeocoderMode */
        final Account.GeocoderMode geocodeMode = Account.getGeocoderMode(acct);
        if (geocodeMode.isNone()) {
            // -- no reverse-geocoding is performed for this Account
            return false;
        }

        /* DBRecordHandler */
        DBRecordHandler<EventData> updAddrHandler = new DBRecordHandler<EventData>() {
            public int handleDBRecord(EventData ev) throws DBException {
                if (!ev.hasAddress()) {
                    ev.setDevice(device); // optimization
                    try {
                        Set<String> updFlds = ev.updateAddress(false/*fastOnly*/,false/*force*/,geocodeMode);
                        if (!ev.hasAddress()) {
                            Print.logWarn("Unable to update Address for location ["+acctDevID+"]: " + ev.getGeoPoint());
                        } else
                        if (!ListTools.isEmpty(updFlds)) {
                            try {
                                ev.update(updFlds);
                                Print.logDebug("Updated Address ["+acctDevID+"]: " + ev.getGeoPoint() + " ==> " + ev.getAddress());
                            } catch (DBException dbe) {
                                // -- error while updating
                                Print.logError("Unable to update EventData address ["+acctDevID+"]: " + dbe);
                                // -- continue below
                            }
                        }
                    } catch (SlowOperationException soe) {
                        // -- will not occur (fastOnly is false)
                    }
                }
                return DBRecordHandler.DBRH_SKIP;
            }
        };

        /* loop through date range */
        Print.logInfo("Account/Device : " + acctDevID);
        eventTimeRangeSegment:
        for (long _sTime = startTime; _sTime < endTime; _sTime += intervalSec) {
            long _eTime = _sTime + intervalSec;
            if (_eTime > endTime) {
                _eTime = endTime;
            }
            // -- process events in range
            EventData.getRangeEvents(
                acctID, devID,
                _sTime, _eTime,
                null/*statusCodes*/,
                true/*validGPS*/, // can't reverse-geocode without a valid GPS
                EventData.LimitType.FIRST, -1L/*limit?*/, true/*ascending*/,
                selectStr/*additionalSelect*/,
                updAddrHandler);
        } // split full interval into smaller segments

        /* success */
        return true;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Count old events for all devices within this account.<br>
    *** Note: Will return -1 if EventData table is InnoDB.
    *** @param oldTimeSec  The Epoch time before which all events will be counted.
    *** @param log True to print the progress to the logging output.
    **/
    public long countOldEvents(long oldTimeSec, boolean log)
        throws DBException
    {
        String acctID  = this.getAccountID();
        String groupID = DeviceGroup.DEVICE_GROUP_ALL;
        if (log) Print.sysPrintln("Counting old events for account "+acctID+" prior to "+(new DateTime(oldTimeSec)));
        return DeviceGroup.countOldEvents(this, groupID, oldTimeSec, log); // -1 for InnoDB
    }

    // ------------------------------------------------------------------------

    /**
    *** Delete old events for all devices within this account.<br>
    *** Note: Will return -1 if EventData table is InnoDB.  
    ***       Old events will still be deleted, however it will still go through the
    ***       motions of attempting to delete events, event if the range is empty.
    *** @param oldTimeSec  The EPoch time before which all events will be deleted.
    ***          If there are no events for a device after this specified time, then 
    ***          the most recent event prior to the specified time will be retained.
    *** @param log True to print the progress to the logging output.
    **/
    public long deleteOldEvents(long oldTimeSec, boolean log)
        throws DBException
    {
        String acctID  = this.getAccountID();
        String groupID = DeviceGroup.DEVICE_GROUP_ALL;
        if (log) Print.sysPrintln("Deleting old events for account "+acctID+" prior to "+(new DateTime(oldTimeSec)));
        return DeviceGroup.deleteOldEvents(this, groupID, oldTimeSec, log); // -1 for InnoDB?
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets a list of orphaned AccountIDs present in the specified table name
    *** @param tableFact  The DBFactory for which orphans will be checked
    *** @param acctIdSet  An optimization to allow using a previously preloaded list of AccountIDs.
    ***                   If null, this method will read/load a temporary copy of all Accounts.
    ***                   This must contain either null, or a full list of all accounts, 
    ***                   otherwise an incorrect list of orphaned accountIDs may be returned.
    **/
    private static Collection<String> _getOrphanedAccountIDs(
        DBFactory<? extends DBRecord<?>> tableFact, 
        Set<String> acctIdSet)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* no DBFactory? */
        if (tableFact == null) {
            // -- can't continue without the DBFactory
            Print.logError("DBFactory not specified");
            return null;
        }
        String tableName = tableFact.getUntranslatedTableName();

        /* Set of all current defined AccountIDs (non-orphans) */
        if (acctIdSet == null) {
            // -- all accounts
            acctIdSet = new HashSet<String>(Account.getAllAccounts(rwMode));
        }
        // --
        if (ListTools.isEmpty(acctIdSet)) {
            // -- no accounts?
            Print.logError("No Accounts specified: ["+tableName+"] ");
            return null;
        }

        /* get all primary keys from "accountID" to last key */
        String priKeys[] = tableFact.getKeyNames();
        int accIDKeyNdx = -1;
        for (int i = 0; i < priKeys.length; i++) {
            if (priKeys[i].equals(AccountRecord.FLD_accountID)) {
                accIDKeyNdx = i;
                break;
            }
        }
        // -- check "accountID" not found
        if (accIDKeyNdx < 0) {
            // -- "accountID" not included in primary keys (thus no orphans)
            //Print.logInfo("AccountID is not part of the primary-key fields: ["+tableName+"] ");
            return null;
        }
        // -- adjust "priKeys" array to start with "accountID"
        if (accIDKeyNdx > 0) {
            // -- trim leading keys (unlikely to occur, since "accoutID" is likely the first key)
            String newPriKeys[] = new String[priKeys.length - accIDKeyNdx];
            System.arraycopy(priKeys, accIDKeyNdx, newPriKeys, 0, newPriKeys.length);
            priKeys = newPriKeys;
        }
        // -- "accountID" will always be at priKeys[0]

        /* create DBSelect */
        // -- SELECT accountID from TableName GROUP BY accountID;
        @SuppressWarnings("unchecked")
        DBSelect<Account> dsel = new DBSelect<Account>((DBFactory<Account>)tableFact); // warning: [unchecked] unchecked cast
        dsel.setSelectedFields(AccountRecord.FLD_accountID); // accountID,...
        dsel.setGroupByFields( AccountRecord.FLD_accountID); // only first "accountID" is included
        dsel.setOrderByFields( AccountRecord.FLD_accountID);

        /* orphan list */
        Collection<String> orphanAcctIDs = null;

        /* read orphaned AccountIDs */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String A = rs.getString(AccountRecord.FLD_accountID);
                if (acctIdSet.contains(A)) {
                    // -- AccountID was found in non-orphan list
                } else
                if (Account.exists(A)) {
                    // -- AccountID exists, but is missing from non-orphan list
                    // -  this prevents an AccountID from being tagged as an orphan when it isn't
                    Print.logWarn("AccountID exists, but is missing from list: "+A);
                } else {
                    // -- "accountID" is an orphan
                    if (orphanAcctIDs == null) { orphanAcctIDs = new Vector<String>(); }
                    orphanAcctIDs.add(A); 
                }
            }
        } catch (SQLException sqe) {
            throw new DBException("Getting Orphaned AccountIDs", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return orphanAcctIDs; // may be null

    }
    
    // --------------------------------

    /**
    *** Gets a list of orphaned Account/DeviceIDs present in the specified table name
    **/
    private static Collection<String> _getOrphanedDeviceIDs(
        DBFactory<? extends DBRecord<?>> tableFact, 
        String accountID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* no DBFactory? */
        if (tableFact == null) {
            // -- can't continue without the DBFactory
            Print.logError("DBFactory not specified");
            return null;
        }
        String tableName = tableFact.getUntranslatedTableName();

        /* no accountID? */
        if (StringTools.isBlank(accountID)) {
            // -- no accountID specified
            Print.logError("AccountID not specified: ["+tableName+"] ");
            return null;
        }

        /* Set of all current defined DeviceIDs for Account (non-orphans) */
        Set<String> devIdSet = Device.getDeviceIDsForAccount(rwMode,accountID,null,true); // non-null OrderedSet
        if (ListTools.isEmpty(devIdSet)) {
            // -- no devices?
            //Print.logInfo("No Devices specified for Account: ["+tableName+"] " + accountID);
            return null;
        }

        /* get all primary keys from "accountID" to last key */
        String priKeys[] = tableFact.getKeyNames();
        int accIDKeyNdx = -1;
        int devIDKeyNdx = -1;
        for (int i = 0; i < priKeys.length; i++) {
            if (priKeys[i].equals(DeviceRecord.FLD_accountID) && (accIDKeyNdx < 0)) {
                accIDKeyNdx = i;
            } else
            if (priKeys[i].equals(DeviceRecord.FLD_deviceID)) {
                devIDKeyNdx = i;
                if (accIDKeyNdx >= 0) {
                    // -- we now have both the accountID and deviceID
                    break;
                }
            }
        }
        // -- check "accountID" not found
        if (accIDKeyNdx < 0) {
            // -- "accountID" not included in primary keys (thus no orphans)
            //Print.logInfo("AccountID is not part of the primary-key fields: ["+tableName+"] ");
            return null;
        }
        // -- check "deviceID" not found
        if (devIDKeyNdx < 0) {
            // -- "deviceID" not included in primary keys (thus no orphans)
            //Print.logInfo("DeviceID is not part of the primary-key fields: ["+tableName+"] ");
            return null;
        }
        // -- check "deviceID" before "accountID"
        if (devIDKeyNdx < accIDKeyNdx) {
            // -- ERROR: incorrect key order? (unlikely to occur)
            Print.logError("DeviceID is before AccountID in pri-key: ["+tableName+"] ");
            return null;
        }
        // -- check that "deviceID" immediately follows "accountID" (not true in DeviceList)
        //if (devIDKeyNdx != (accIDKeyNdx + 1)) {
        //    // -- ERROR: "deviceID" does not immediately follow "accountID" (unlikely to occur)
        //    Print.logError("DeviceID does not immediately follow AccountID in pri-key: ["+tableName+"] ");
        //    return null;
        //}
        // -- adjust "priKeys" array to start with "accountID"
        if (accIDKeyNdx > 0) {
            // -- trim leading keys (unlikely to occur, since "accoutID" is likely the first key)
            String newPriKeys[] = new String[priKeys.length - accIDKeyNdx];
            System.arraycopy(priKeys, accIDKeyNdx, newPriKeys, 0, newPriKeys.length);
            priKeys = newPriKeys;
        }
        // -- "accountID" will always be at priKeys[0], and "deviceID" will be at priKeys[1]

        /* create DBSelect */
        // -- SELECT accountID,deviceID from TableName WHERE accountID="AccountID" GROUP BY accountID,deviceID;
        @SuppressWarnings("unchecked")
        DBSelect<Device> dsel = new DBSelect<Device>((DBFactory<Device>)tableFact); // warning: [unchecked] unchecked cast
        DBWhere dwh = dsel.createDBWhere();
        dsel.setWhere(dwh.WHERE_(dwh.EQ(Account.FLD_accountID,accountID)));
        dsel.setSelectedFields(DeviceRecord.FLD_accountID,DeviceRecord.FLD_deviceID); // accountID,deviceID,...
        dsel.setGroupByFields( DeviceRecord.FLD_accountID,DeviceRecord.FLD_deviceID); // only first "accountID,deviceID" is included
        dsel.setOrderByFields( DeviceRecord.FLD_accountID,DeviceRecord.FLD_deviceID);

        /* orphan list */
        Collection<String> orphanDevIDs = null;

        /* read orphaned DeviceIDs */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String D = rs.getString(DeviceRecord.FLD_deviceID);
                if (devIdSet.contains(D)) {
                    // -- DeviceID was found in non-orphan list
                } else
                if (Device.exists(accountID,D)) {
                    // -- DeviceID exist, but is missing from non-orphan list
                    // -  this prevents a DeviceID from being tagged as an orphan when it isn't
                    Print.logWarn("DeviceID exists for Account, but is missing from list: "+accountID+"/"+D);
                } else {
                    // -- "deviceID" is an orphan
                    if (orphanDevIDs == null) { orphanDevIDs = new Vector<String>(); }
                    orphanDevIDs.add(D); 
                }
            }
        } catch (SQLException sqe) {
            throw new DBException("Getting Orphaned ["+accountID+"] DeviceIDs", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return orphanDevIDs; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Accumulate total Events-Per-Second from values stored in each individual Device
    **/
    public static double getEventsPerSecond()
    {
        try {
            return Account._getEventsPerSecond();
        } catch (DBException dbe) {
            return -1.0; // unable to calculate
        }
    }

    /**
    *** Accumulate total Events-Per-Second from values stored in each individual Device
    **/
    public static double _getEventsPerSecond()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        double accumEPS = 0.0;
        Collection<String> acctList = Account.getAllAccounts(rwMode);
        long nowTimeMS = System.currentTimeMillis();
        for (String acctID : acctList) {

            /* get account */
            Account account = Account.getAccount(acctID); // may return null
            if (account == null) { // unlikely to be null
                // this should never occur if we started with a valid list
                continue;
            }

            /* devices for account */
            OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(rwMode, acctID, null/*User*/, false/*inclInactv*/);
            for (String devID : devIDList) {
                Device device = Device.getDevice(account, devID); // account.getDevice(devID);
                if (device != null) { // unlikely to be null
                    accumEPS += device.getAgedEventsPerSecond(nowTimeMS);
                }
            }

        }
        return accumEPS;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets a Collection of all custom StatusCode entries for this Account,
    *** and specified DeviceID.
    **/
    public Collection<StatusCode> getStatusCodeList(DBReadWriteMode rwMode, String deviceID)
        throws DBException
    {
        return StatusCode.getStatusCodeList(rwMode, this.getAccountID(), deviceID);
    }
    // --
    @Deprecated
    public Collection<StatusCode> getStatusCodeList(String deviceID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getStatusCodeList(rwMode, deviceID);
    }

    /**
    *** Gets a Collection of all custom StatusCode entries for this Account
    **/
    @JSONBeanGetter(expandArray="true", ignore="$blank")
    public Collection<StatusCode> getStatusCodeList()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getStatusCodeList(rwMode, null/*deviceID*/);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // This section supports a method for obtaining human readable information 
    // from the Account record for reporting, or email purposes. (currently 
    // this is used by the 'rules' engine when generating notification emails).
    
    public  static      boolean ALLOW_TEMP_STRINGS    = false;

    private static final String KEY_ACCOUNT[]         = EventData.KEY_ACCOUNT;
    private static final String KEY_ACCOUNT_ID[]      = EventData.KEY_ACCOUNT_ID;
    private static final String KEY_CONTACT_EMAIL[]   = { "contactEmail"                          };
    private static final String KEY_CONTACT_PHONE[]   = { "contactPhone"                          };
    private static final String KEY_TEMP_PASSWORD[]   = { "tempPassword"                          };

    public  static final String KEY_ACCOUNT_STATUS[]  = { "accountStatus"      , "acctStatus"     };  // "Suspended"

    public  static final String KEY_DEVICE_COUNT[]    = { "deviceCount"        , "devCount"       };  // "123"

    private static final String KEY_DATETIME[]        = EventData.KEY_DATETIME;
    private static final String KEY_DATE_YEAR[]       = EventData.KEY_DATE_YEAR;
    private static final String KEY_DATE_MONTH[]      = EventData.KEY_DATE_MONTH;
    private static final String KEY_DATE_DAY[]        = EventData.KEY_DATE_DAY;
    private static final String KEY_DATE_DOW[]        = EventData.KEY_DATE_DOW;
    private static final String KEY_TIME[]            = EventData.KEY_TIME;

    private static final String KEY_LASTLOGIN_DATE[]  = { "lastLoginDate"                         };
    private static final String KEY_LASTLOGIN_HOURS[] = { "lastLoginHours"     , "sinceLastLogin" };

    private static final String KEY_EXPIREDATE[]      = { "accountExpireDate"  , "acctExpireDate" };  // 
    private static final String KEY_EXPIREDAYS[]      = { "accountExpireDays"  , "acctExpireDays" };  // 

    private static final String KEY_ELOG_ENABLED[]    = { "elogEnabled"        , "elog", "elogs"  };

    private static final String KEY_DISKUSAGE[]       = { "gtsHomeDiskUsage"   , "gtsDiskUsage"   };
    private static final String KEY_DISKFOLDER[]      = { "gtsHomeDiskFolder"  , "gtsDiskFolder"  };
    private static final String KEY_DISKTOTAL[]       = { "gtsHomeDiskTotal"   , "gtsDiskTotal"   };
    private static final String KEY_DISKFREE[]        = { "gtsHomeDiskFree"    , "gtsDiskFree"    };
    private static final String KEY_DISKUSED[]        = { "gtsHomeDiskUsed"    , "gtsDiskUsed"    };
    private static final String KEY_DISKFREE_PCT[]    = { "gtsHomeDiskFreePct" , "gtsDiskFreePct" , "gtsDiskFreePercent" };
    private static final String KEY_DISKUSED_PCT[]    = { "gtsHomeDiskUsedPct" , "gtsDiskUsedPct" , "gtsDiskUsedPercent" };

    private static final String KEY_EVAL[]            = { "evaluate"           , "eval"           }; //

    /**
    *** Gets the title of the specific key field name
    *** @param key The name of the key
    *** @param arg The parameter/argument applied to the key title
    *** @param locale  The Locale
    *** @return The key title
    **/
    public static String getKeyFieldTitle(String key, String arg, Locale locale)
    {
        return Account._getKeyFieldString(
            true/*title*/, key, arg, 
            locale, null/*BasicPrivateLabel*/, null/*Account*/);
    }

    /**
    *** Gets the value of the specific key field name
    *** @param key The name of the key
    *** @param arg The parameter/argument applied to the key value
    *** @param bpl The PrivateLabel instance
    *** @return A String representation of the value
    **/
    public String getKeyFieldValue(String key, String arg, BasicPrivateLabel bpl)
    {
        Locale locale = (bpl != null)? bpl.getLocale() : null;
        return Account._getKeyFieldString(
            false/*value*/, key, arg, 
            locale, bpl, this);
    }

    /**
    *** Gets the title/value of the specific key field name
    *** @param getTitle  True to return the title, false to return the value
    *** @param key       The name of the key
    *** @param arg       The parameter/argument applied to the key title/value
    *** @param bpl       The PrivateLabel instance
    *** @param acct      The owner account
    *** @return A String representation of the title/value
    **/
    public static String _getKeyFieldString(
        boolean getTitle, String key, String arg, 
        Locale locale, BasicPrivateLabel bpl, Account acct)
    {

        /* check for valid field name */
        if (key == null) {
            return null;
        } else
        if ((acct == null) && !getTitle) {
            return null; // account require for value (not for title)
        }
        if ((locale == null) && (bpl != null)) { locale = bpl.getLocale(); }
        I18N i18n = I18N.getI18N(Account.class, locale);
        long now = DateTime.getCurrentTimeSec();

        /* make sure arg is not null */
        arg = StringTools.trim(arg);
        int argP = key.indexOf(":");
        if (argP >= 0) {
            if (arg.equals("")) {
                arg = key.substring(argP+1);
            }
            key = key.substring(0,argP);
        }

        /* Account */
        if (EventData._keyMatch(key,Account.KEY_ACCOUNT)) {
            if (getTitle) {
                return i18n.getString("Account.key.accountDescription", "Account");
            } else {
                Account account = acct;
                if (arg.equalsIgnoreCase("id")) {
                    return account.getAccountID();
                } else {
                    return account.getDescription();
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_ACCOUNT_ID)) {
            if (getTitle) {
                return i18n.getString("Account.key.accountID", "Account-ID");
            } else {
                return acct.getAccountID();
            }
        }

        /* Account Status */
        if (EventData._keyMatch(key,Account.KEY_ACCOUNT_STATUS)) {
            if (getTitle) {
                return i18n.getString("Account.key.accountStatus", "Account Status");
            } else {
                return acct.getActiveStatusDescription(locale);
            }
        }

        /* Contact */
        if (EventData._keyMatch(key,Account.KEY_CONTACT_EMAIL)) {
            if (getTitle) {
                return i18n.getString("Account.key.contactEmail", "Contact EMail");
            } else {
                return acct.getContactEmail();
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_CONTACT_PHONE)) {
            if (getTitle) {
                return i18n.getString("Account.key.contactPhone", "Contact Phone");
            } else {
                String ph = acct.getContactPhone();
                if (StringTools.isBlank(ph)) {
                    return "";
                } else
                if (StringTools.isBlank(arg)     || 
                    arg.equalsIgnoreCase("plain")  ) {
                    return ph;
                } else
                if (arg.equalsIgnoreCase("a")   ||  // "anchor"
                    arg.equalsIgnoreCase("html")||
                    arg.equalsIgnoreCase("link")  ) {
                    return EventUtil.MAP_ESCAPE_HTML+"<a href='tel:"+ph+"' target='_blank'>"+ph+"</a>";
                } else {
                    return ph;
                }
            }
        }

        /* Temporary Password */
        if (EventData._keyMatch(key,Account.KEY_TEMP_PASSWORD)) {
            if (getTitle) {
                return i18n.getString("Account.key.temporaryPassword", "Temporary Password");
            } else {
                return acct.getTempPassword();
            }
        }

        /* Device count */
        if (EventData._keyMatch(key,Account.KEY_DEVICE_COUNT)) {
            if (getTitle) {
                return i18n.getString("Account.key.deviceCount", "Device Count");
            } else {
                return String.valueOf(acct.getDeviceCount());
            }
        }

        /* Date/Time */
        if (EventData._keyMatch(key,Account.KEY_DATETIME)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateTime", "Date/Time");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampString(now, acct, tmz, bpl);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_YEAR)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateYear", "Year");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampYear(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_MONTH)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateMonth", "Month");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampMonth(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_DAY)) {
            if (getTitle) {
                return i18n.getString("Account.key.dateDay", "Day");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfMonth(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DATE_DOW)) {
            if (getTitle) {
                return i18n.getString("Account.key.dayOfWeek", "Day Of Week");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfWeek(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_TIME)) {
            if (getTitle) {
                return i18n.getString("Account.key.time", "Time");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                return EventData.getTimestampString(now, acct, tmz, bpl);
            }
        }

        /* last login */
        if (EventData._keyMatch(key,Account.KEY_LASTLOGIN_DATE)) {
            if (getTitle) {
                return i18n.getString("Account.key.lastLoginDate", "Last Login Date");
            } else {
                TimeZone tmz = acct.getTimeZone(null); // non-null
                long lastLoginTime = acct.getLastLoginTime();
                String dateFmt     = acct.getDateFormat();
                String timeFmt     = acct.getTimeFormat();
                DateTime dt        = new DateTime(lastLoginTime, tmz);
                return dt.format(dateFmt + " " + timeFmt + " z");
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_LASTLOGIN_HOURS)) {
            if (getTitle) {
                return i18n.getString("Account.key.hoursSinceLastLogin", "Hours Since\nLast Login");
            } else {
                long lastLoginTime = acct.getLastLoginTime();
                if ((lastLoginTime > 0L) && (now >= lastLoginTime)) {
                    double hoursSinceLogin = (double)(now - lastLoginTime) / 3600.0;
                    return StringTools.format(hoursSinceLogin, "0.0");
                } else {
                    return i18n.getString("Account.key.hoursSinceLastLogin.never", "Never");
                }
            }
        }

        /* expiration */
        if (EventData._keyMatch(key,Account.KEY_EXPIREDATE)) {
            if (getTitle) {
                return i18n.getString("Account.key.expireDate", "Expiration Date");
            } else {
                long expTime = acct.getExpirationTime();
                if (expTime > 0L) {
                    if (expTime <= now) {
                        return i18n.getString("Account.key.expireDate.expired", "Expired");
                    } else {
                        TimeZone tmz = acct.getTimeZone(null); // non-null
                        return EventData.getTimestampString(expTime, acct, tmz, bpl);
                    }
                } else {
                    return i18n.getString("Account.key.expireDate.noExpire", "n/a");
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_EXPIREDAYS)) {
            if (getTitle) {
                return i18n.getString("Account.key.expireDays", "Expiration Days");
            } else {
                long expTime = acct.getExpirationTime();
                if (expTime > 0L) {
                    if (expTime <= now) {
                        return i18n.getString("Account.key.expireDays.expired", "Expired");
                    } else {
                        long expSec = expTime - now;
                        String D = String.valueOf(expSec / DateTime.DaySeconds(1));
                        String H = String.valueOf((expSec % DateTime.DaySeconds(1)) / DateTime.HourSeconds(1));
                        return i18n.getString("Account.key.expireDays.daysHours", "{0} Days {1} Hours", new String[]{D,H});
                    }
                } else {
                    return i18n.getString("Account.key.expireDays.noExpire", "n/a");
                }
            }
        }

        /* ELog/HOS */
        if (EventData._keyMatch(key,Account.KEY_ELOG_ENABLED)) {
            if (getTitle) {
                return i18n.getString("Account.key.elogEnabled", "ELog/HOS");
            } else {
                boolean elogEnabled = Account.IsELogEnabled(acct);
                if (elogEnabled) {
                    return i18n.getString("Account.key.elogEnabled.yes", "Yes");
                } else {
                    return i18n.getString("Account.key.elogEnabled.no" , "No" );
                }
            }
        }

        /* System Info */
        if (EventData._keyMatch(key,Account.KEY_DISKUSAGE)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsage", "Disk Usage");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // -- 
                        String folder  = diskDir.toString();
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        double freePct = (totalMb > 0.0)? (freeMb / totalMb) : 0.0;
                        // -- Dir=/usr/local/GTS_1.2.3  Total=20480mb  Free=13280mb  [65% free]
                        StringBuffer sb = new StringBuffer();
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.directory","Dir"));
                        sb.append("=");
                        sb.append(folder);
                        sb.append("  ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.total","Total"));
                        sb.append("=");
                        sb.append(StringTools.format(totalMb,"0"));
                        sb.append("mb  ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.free","Free"));
                        sb.append("=");
                        sb.append(StringTools.format(freeMb,"0"));
                        sb.append("mb  [");
                        sb.append(StringTools.format(freePct*100.0,"0"));
                        sb.append("% ");
                        sb.append(i18n.getString("Account.key.gtsHomeDiskUsage.free","Free").toLowerCase());
                        sb.append("]");
                        return sb.toString();
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFOLDER)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFolder", "Disk Folder");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        return diskDir.toString();
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKTOTAL)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskTotal", "Disk Total");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        return StringTools.format(totalMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFREE)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFree", "Disk Free");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        return StringTools.format(freeMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKUSED)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsed", "Disk Used");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        return StringTools.format(usedMb,"0") + "mb";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKFREE_PCT)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskFreePct", "Disk Free %");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double freePct = (totalMb > 0.0)? (freeMb / totalMb) : 0.0;
                        return StringTools.format(freePct*100.0,"0") + "%";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        } else
        if (EventData._keyMatch(key,Account.KEY_DISKUSED_PCT)) {
            if (getTitle) {
                return i18n.getString("Account.key.gtsHomeDiskUsedPct", "Disk Used %");
            } else {
                if (!acct.isSystemAdmin()) {
                    return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                } else {
                    File gtsHome = RTConfig.getLoadedConfigDir();
                    if ((gtsHome != null) && gtsHome.isDirectory()) {
                        // -- "$GTS_HOME/logs"
                        File diskDir = gtsHome;
                        File logsDir = new File(diskDir,"logs");
                        if (logsDir.isDirectory()) {
                            diskDir = logsDir;
                        }
                        // --
                        double totalMb = (double)diskDir.getTotalSpace()  / (1024.0 * 1024.0);
                        double freeMb  = (double)diskDir.getUsableSpace() / (1024.0 * 1024.0);
                        double usedMb  = totalMb - freeMb;
                        double usedPct = (totalMb > 0.0)? (usedMb / totalMb) : 0.0;
                        return StringTools.format(usedPct*100.0,"0") + "%";
                    } else {
                        return i18n.getString("Account.key.gtsHomeDiskUsage.notAuthorized", "n/a");
                    }
                }
            }
        }

        /* RuleFactory */
        if (EventData._keyMatch(key,Account.KEY_EVAL)) {
            if (getTitle) {
                return i18n.getString("Account.key.value", "Value");
            } else {
                // -- rule selector
                String ruleSel = arg; // rule selector to evaluate
                if (StringTools.isBlank(ruleSel)) {
                    return "";
                }
                // -- RuleFactory
                RuleFactory rf = Device.getRuleFactory();
                if ((rf == null) || !rf.checkRuntime()) {
                    Print.logError("Invalid RuleFactory");
                    return "";
                }
                // -- evaluate and return result
                try {
                    Object result = rf.evaluateSelector(ruleSel,acct);
                    if (result == null) {
                        return "";
                    } else {
                        return result.toString();
                    }
                } catch (RuleParseException rpe) {
                    Print.logError("Unable to parse Rule selector: " + arg);
                    return "";
                }
            }
        }

        /* Account fields */
        if (getTitle) {
            DBField dbFld = Account.getFactory().getField(key);
            if (dbFld != null) {
                return dbFld.getTitle(locale);
            }
            // -- field not found
        } else {
            String fldName = acct.getFieldName(key); // this gets the field name with proper case
            DBField dbFld = (fldName != null)? acct.getField(fldName) : null;
            if (dbFld != null) {
                Object val = acct.getFieldValue(fldName); // straight from table
                if (val == null) { val = dbFld.getDefaultValue(); }
                Account account = acct;
                if (account != null) {
                    val = account.convertFieldUnits(dbFld, val, true/*inclUnits*/, locale);
                    return StringTools.trim(val);
                } else {
                    return dbFld.formatValue(val);
                }
            }
            // -- field not found
        }

        /* try temporary properties */
        if ((acct != null) && acct.hasTemporaryProperties()) {
            RTProperties rtp = acct.getTemporaryProperties();
            Object text = (rtp != null)? rtp.getProperty(key,null) : null;
            if (text instanceof I18N.Text) {
                if (getTitle) {
                    // -- all we have is the key name for the title
                    return key;
                } else {
                    // -- return Localized version of value
                    return ((I18N.Text)text).toString(locale);
                }
            } else
            if (Account.ALLOW_TEMP_STRINGS && (text instanceof String)) {
                if (getTitle) {
                    // -- all we have is the key name for the title
                    return key;
                } else {
                    // -- return String as-is
                    return (String)text;
                }
            }
        }

        // ----------------------------
        // Account key not found

        /* not found */
        return null;

    }

    // ------------------------------------------------------------------------

    // -- special case replacement vars
    private static final String KEY_PASSWORD[]      = new String[] { "password"              };  // 

    private static final String START_DELIM         = "${";
    private static final String END_DELIM           = "}";
    private static final String DFT_DELIM           = "=";

    /**
    *** Insert User replacement values in specified text String
    **/
    public static String insertUserKeyValues(Account acct, String text)
    {
        if (acct != null) {
            return StringTools.insertKeyValues(text, 
                START_DELIM, END_DELIM, DFT_DELIM,
                new Account.AccountValueMap(acct));
        } else {
            return text;
        }
    }

    /**
    *** Insert User replacement values in specified text String
    **/
    public String insertUserKeyValues(String text)
    {
        return Account.insertUserKeyValues(this, text);
    }

    public static class AccountValueMap
        implements StringTools.KeyValueMap // ReplacementMap
    {
        private Account           account   = null;
        private BasicPrivateLabel privLabel = null;
        public AccountValueMap(Account acct) {
            this.account   = acct;
            this.privLabel = null;
        }
        public String getKeyValue(String key, String arg, String dft) {
            if (EventData._keyMatch(key,Account.KEY_PASSWORD)) {
                if (this.account != null) {
                    String pwd = this.account.getDecodedPassword(this.privLabel);
                    return (pwd != null)? pwd : dft;
                } else {
                    return dft;
                }
            } else
            if (this.account != null) {
                String fldStr = this.account.getKeyFieldValue(key,arg,this.privLabel);
                return (fldStr != null)? fldStr : dft; // "("+key+")";
            } else {
                //Print.logWarn("Key not found: " + key);
                return dft; // "("+key+")";
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Billing Report (may not be presetn in this release)

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below
    
    private static final String ARG_MANAGER[]           = { "manager"   , "am"    , "m" };
    private static final String ARG_ACCOUNT[]           = { "account"   , "acct"  , "a", "list", "report", "export" };
    private static final String ARG_DEVICE[]            = { "device"    , "dev"   , "d" };
    private static final String ARG_DELETE[]            = { "delete"                    };
    private static final String ARG_CREATE[]            = { "create"                    };
    private static final String ARG_NOPASS[]            = { "nopass"                    };
    private static final String ARG_PASSWORD[]          = { "password"  , "passwd" , "pass" };
    private static final String ARG_SET_PASSWD[]        = { "setPasswd" , "setPass", "setPassword" };
    private static final String ARG_DESC[]              = { "desc"      , "description" };
    private static final String ARG_EDIT[]              = { "edit"                      };
    private static final String ARG_EDITALL[]           = { "editall"                   };
    private static final String ARG_LIST[]              = { "list"      , "short"       };
    private static final String ARG_SHORTLIST[]         = { "short"                     };
    private static final String ARG_REPORT[]            = { "report"                    };
    private static final String ARG_PRUNE[]             = { "prune"                     };
    private static final String ARG_XML[]               = { "xml"                       };
    private static final String ARG_PRIVLABEL[]         = { "privLabel" , "pl"          };
    private static final String ARG_LISTTYPE[]          = { "listType"  , "type"        };
    private static final String ARG_DCS_NAME[]          = { "dcsName", "dcs"            };
    private static final String ARG_SINCE_DATE[]        = { "sinceDate", "since"        };
    private static final String ARG_EVENTCOUNT[]        = { "eventCount", "ec"          };
    private static final String ARG_CNT_OLD_EV[]        = { "countOldEvents"            };
    private static final String ARG_DEL_OLD_EV[]        = { "deleteOldEvents"           };
    private static final String ARG_CONFIRM_DEL[]       = { "confirmDelete"             };
    private static final String ARG_FIND_EMAIL[]        = { "findEMail"                 };
    private static final String ARG_EXPORT[]            = { "export"                    };
    private static final String ARG_TO_DIR[]            = { "toDir"                     };
    private static final String ARG_BACKUP[]            = { "backup"    , "dump"        };
    private static final String ARG_SEND_MAIL[]         = { "sendMail"                  };
    private static final String ARG_UPDATE_ADDRESS[]    = { "updateAddresses", "updAddr"}; // updateBlankEventAddresses

    private static final String ARG_LIST_ORPHANS[]      = { "listOrphans", "orphans"    };
    private static final String ARG_INCL_DEV_ORPHANS[]  = { "inclDevOrphans"            }; // "list..." only
    private static final String ARG_DELETE_ORPHANS[]    = { "deleteOrphans"             };

    private static void usage()
    {
        Print.sysPrintln("Usage:");
        Print.sysPrintln("  java ... " + Account.class.getName() + " {options}");
        Print.sysPrintln("Options:");
        Print.sysPrintln("  -account=<AccountID>          Specific AccountID to list/report/delete/create/edit");
        Print.sysPrintln("");
        Print.sysPrintln("  -delete                       Delete specified Account and owned Devices (USE WITH CAUTION!)");
        Print.sysPrintln("  -create                       To create a new Account");
        Print.sysPrintln("  -pass=<pass>                  Set password on Account creation");
        Print.sysPrintln("  -nopass                       Set blank password on Account creation");
        Print.sysPrintln("  -edit                         To edit an existing (or newly created) Account");
        Print.sysPrintln("");
        Print.sysPrintln("  -list[=<AccountID>]           List all Accounts and owned Devices");
        Print.sysPrintln("     -type=[all|active|count]     List type");
        Print.sysPrintln("     -manager=<ManagerID>         Manager name");
        Print.sysPrintln("     -pl=<PrivateLabelName>       PrivateLabel name");
        Print.sysPrintln("     -dcs=<DCSname>               DCS module name");
        Print.sysPrintln("     -since=<DateTime>            Include only devices connected since <DateTime>");
        Print.sysPrintln("");
        Print.sysPrintln("  -report[=<AccountID>]         Report on Accounts total/active Devices");
        Print.sysPrintln("     -manager=<ManagerID>         Manager name");
        Print.sysPrintln("     -pl=<PrivateLabelName>       PrivateLabel name");
        Print.sysPrintln("     -forMonth=<MonthNumber>      Month number (1..12)");
        Print.sysPrintln("     -showHasEvents=[true|false]  Show has events");
        Print.sysPrintln("     -showLastEvent=[true|false]  Show last event time");
        Print.sysPrintln("     -showActive=[true|false]     Show active only");
        Print.sysPrintln("");
        Print.sysPrintln("  -findEMail=<EmailAddr>        Find all references to specified email address");
        Print.sysPrintln("");
        Print.sysPrintln("  -listOrphans[=TABLE]          List Account orphans [in specified table]");
        Print.sysPrintln("     -inclDevOrphans              Include device orphans");
        Print.sysPrintln("");
        Print.sysPrintln("  -deleteOrphans=TABLE          Delete orphans");
        Print.sysPrintln("     -inclDevOrphans              Include device orphans");
        Print.sysPrintln("     -confirmDelete               Confirms deleteOrphans");
        Print.sysPrintln("");
        Print.sysPrintln("  -countOldEvents=<EpochTime>   Count events (for all Devices) before specified Epoch time");
        Print.sysPrintln("");
        Print.sysPrintln("  -deleteOldEvents=<EpochTime>  Delete events (for all Devices) before specified Epoch time (requires '-confirm')");
        Print.sysPrintln("     -confirmDelete               Confirms deleteOldEvents");
        Print.sysPrintln("");
        Print.sysPrintln("  -prune                        Deactivate/Delete expired accounts");
        System.exit(1);
    }

    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main

        /* "-device" specified? */
        if (RTConfig.hasProperty(ARG_DEVICE)) {
            // -- Checks the case where "admin Account ..." was specified when "admin Device ..." was intended.
            Print.logError("Cannot specify '-device' on Account admin command");
            System.exit(99);
        }

        /* account id */
        String acctID = StringTools.trim(RTConfig.getString(ARG_ACCOUNT,""));
        boolean hasAccountID;
        if (StringTools.isBlank(acctID)) {
            // -- not specified
            hasAccountID = false;
        } else
        if (acctID.equalsIgnoreCase("all")) {
            // -- "ALL" accounts
            hasAccountID = false;
        } else
        if ((acctID.indexOf("*") >= 0) || (acctID.indexOf("%") >= 0)) {
            // -- wildcard accounts
            hasAccountID = false;
        } else
        if (acctID.indexOf(",") >= 0) {
            // -- list of accounts
            hasAccountID = false;
        } else {
            // -- specific accountID specified
            hasAccountID = true;
        }

        /* account exists? */
        boolean accountExists = false;
        if (hasAccountID) {
            try {
                accountExists = Account.exists(acctID);
            } catch (DBException dbe) {
                Print.logError("Error determining if Account exists: " + acctID);
                System.exit(99);
            }
        } else {
            //Print.logWarn("Account-ID not specified");
            accountExists = false;
        }

        /* option count */
        int opts = 0;

        /* delete */
        // -- bin/admin.pl Account -account=ACCOUNT -delete  (warning, does not ask for confirmation)
        if (RTConfig.getBoolean(ARG_DELETE, false)) {
            opts++;
            // -- account exists?
            if (!hasAccountID) {
                Print.logError("Account-ID not specified");
                System.exit(1);
            } else
            if (!accountExists) {
                Print.logWarn("Account-ID already does not exist: " + acctID);
                //Print.logWarn("Continuing with delete process ...");
                System.exit(0);
            } 
            // -- delete
            try {
                Account.deleteAccounts(new String[] { acctID });
                Print.logInfo("Account-ID deleted: " + acctID);
                accountExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting Account-ID: " + acctID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        } // ARG_DELETE

        /* create default account */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            // -- account ID specified
            if (!hasAccountID) {
                Print.logError("Account-ID not specified");
                System.exit(1);
            }
            // -- create if not already exists
            if (accountExists) {
                Print.logWarn("Account-ID already exists: " + acctID);
                // -- continue below (ie. "-create -setPassword=xyzzy")
            } else {
                try {
                    String passwd = null;
                    if (RTConfig.getBoolean(ARG_NOPASS,false)) {
                        passwd = Account.BLANK_PASSWORD;
                    } else
                    if (RTConfig.hasProperty(ARG_PASSWORD)) {
                        passwd = RTConfig.getString(ARG_PASSWORD,"");
                    }
                    Account acct = Account.createNewAccount(null, acctID, passwd);
                    if (RTConfig.hasProperty(ARG_DESC)) {
                        try {
                            acct.setDescription(RTConfig.getString(ARG_DESC,""));
                            acct.save();
                        } catch (DBException dbe) {
                            // ignore errors generated when updating the description
                        }
                    }
                    Print.logInfo("Created Account-ID: " + acctID);
                    accountExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating Account-ID: " + acctID);
                    dbe.printException(); // CLI
                    System.exit(99);
                }
            }
        } // ARG_CREATE

        /* xml */
        if (RTConfig.getBoolean(ARG_XML,false)) {
            opts++;
            // -- account exists?
            if (!hasAccountID) {
                Print.logError("Account-ID not specified");
                System.exit(1);
            } else
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
                System.exit(1);
            }
            // -- output XML
            try {
                Account account = Account.getAccount(acctID,false);
                DBRecord.printXML(new PrintWriter(System.out), account);
            } catch (DBException dbe) {
                Print.logError("Error displaying Account-ID: " + acctID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        } // ARG_XML

        /* edit */
        if ((RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false))) {
            opts++;
            // -- account exists?
            if (!hasAccountID) {
                Print.logError("Account-ID not specified");
                System.exit(1);
            } else
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
                System.exit(1);
            }
            // -- "admin" user?
            boolean adminExists = false;
            try {
                adminExists = User.exists(acctID, User.getAdminUserID());
                if (adminExists) {
                    Print.sysPrintln("(Note: This account has an '"+User.getAdminUserID()+"' user)");
                }
            } catch (DBException dbe) {
                Print.logError("Error determining if User exists: "+acctID+","+User.getAdminUserID());
            }
            // -- edit
            try {
                boolean allFlds = RTConfig.getBoolean(ARG_EDITALL,false);
                Account account = Account.getAccount(acctID,false); // does NOT return null
                DBEdit  editor  = new DBEdit(account);
                editor.edit(allFlds); // may throw IOException
            } catch (IOException ioe) {
                if (ioe instanceof EOFException) {
                    Print.logError("End of input");
                } else {
                    Print.logError("IO Error");
                }
            } catch (DBException dbe) {
                Print.logError("Error editing Account-ID: " + acctID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        } // ARG_EDIT/ARG_EDITALL

        /* set password */
        if (RTConfig.hasProperty(ARG_SET_PASSWD)) {
            opts++;
            // -- account exists
            if (!hasAccountID) {
                Print.logError("Account-ID not specified");
                System.exit(1);
            } else
            if (!accountExists) {
                Print.logError("Account-ID does not exist: " + acctID);
                System.exit(99);
            }
            // -- get password
            String passwd = RTConfig.getString(ARG_SET_PASSWD,null);
            if (StringTools.isBlank(passwd)) {
                Print.logError("Password not specified");
                System.exit(99);
            }
            // -- set password
            try {
                Account account = Account.getAccount(acctID,false); // does NOT return null
                account.setDecodedPassword(null, passwd, true); // temporary password
                account.updatePasswordFields();
                Print.logInfo("Set Account password: " + acctID);
            } catch (DBException dbe) {
                Print.logError("Error setting Account-ID password: " + acctID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        } // ARG_SET_PASSWD

        /* report */
        if (RTConfig.hasProperty(ARG_REPORT)) { // GetBillingReport
            opts++;
            String  plName           = RTConfig.getString( ARG_PRIVLABEL     , null);
            String  managerID        = RTConfig.getString( ARG_MANAGER       , null);
            boolean showHasEvents    = RTConfig.getBoolean("showHasEvents"   , false); // show has events
            boolean showLastEvent    = RTConfig.getBoolean("showLastEvent"   , false);
            int     forMonth         = RTConfig.getInt(    "forMonth"        , -1);
            boolean showActive       = RTConfig.getBoolean("showActive"      , true);
            boolean showCost         = RTConfig.getBoolean("showCost"        , false);
            boolean ncNoEvents       = RTConfig.getBoolean("ncNoEvents"      , false);
            double  actvFeePerMonth  = RTConfig.getDouble( "feePerMonth"     , -1.0);
            double  inactivePerMonth = RTConfig.getDouble( "inactivePerMonth", -1.0);
            // -- get/print billing report 
            // -    [TODO: may not be implemented in this release]
            System.exit(0);
        } // ARG_REPORT

        /* list */
        if (RTConfig.hasProperty(ARG_LIST)) {
            String   type     = RTConfig.getString(  ARG_LISTTYPE  , "all");
            String   plName   = RTConfig.getString(  ARG_PRIVLABEL , null);
            String   manager  = RTConfig.getString(  ARG_MANAGER   , null);
            String   dcsName  = RTConfig.getString(  ARG_DCS_NAME  , null);
            DateTime sinceDT  = RTConfig.getDateTime(ARG_SINCE_DATE, null);
            boolean  eventCnt = RTConfig.getBoolean( ARG_EVENTCOUNT, false); // MyISAM only
            long     nowMS    = System.currentTimeMillis();
            TimeZone tmz      = DateTime.GMT;
            opts++;
            try {
                int    acctCount        = 0;
                int    tdevCountTotal   = 0;
                int    tdevCountActiv   = 0;
                double tdevEventsPerSec = 0.0;
                DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
                Collection<String> acctList = Account.getAllAccounts(rwMode);
                for (String acctListID : acctList) {

                    /* specific Account? */
                    if (hasAccountID && !acctID.equals(acctListID)) {
                        // specific account requested, and accountID doesn't match
                        continue;
                    }

                    /* get account */
                    Account account = Account.getAccount(acctListID); // may return null
                    if (account == null) {
                        // -- this should never occur if we started with a valid list
                        continue;
                    }

                    /* specific PrivateLabel name? */
                    String privLabelName = account.getPrivateLabelName();
                    if (!StringTools.isBlank(plName) && !plName.equalsIgnoreCase(privLabelName)) {
                        // -- specified PrivateLabel name does not match
                        continue;
                    }

                    /* specific account manager */
                    String managerID = account.getManagerID();
                    if (!StringTools.isBlank(manager) && !managerID.equals(manager)) {
                        // -- specific account manager does not match
                        continue;
                    }

                    /* count accounts */
                    acctCount++;

                    /* display account info */
                    if (StringTools.isBlank(type)       || 
                        type.equalsIgnoreCase("all")    ||
                        type.equalsIgnoreCase("active") ||
                        type.equalsIgnoreCase("count")    ) {
                        boolean fullList = !type.equalsIgnoreCase("active");
                        Print.sysPrintln("");

                        /* Account */
                        StringBuffer asb = new StringBuffer();
                        asb.append("Account: " + acctListID + " - " + account.getDescription());
                        asb.append(" [active=").append(account.getIsActive());
                        if (fullList) {
                            // -- has "admin" user
                            asb.append(", hasAdminUser=" + account.hasAdminUser());
                            if (account.isSystemAdmin()) { 
                                asb.append(", isSysAdmin=true"); 
                            }
                        }
                        asb.append("]");
                        // -- invalid ID
                        if (!AccountRecord.isValidID(acctListID)) {
                            asb.append(" (ID may contain invalid characters)");
                        }
                        Print.sysPrintln(asb.toString());

                        /* private label */
                        Print.sysPrintln("  > PrivateLabel: " + privLabelName);

                        /* Account manager */
                        if (!StringTools.isBlank(managerID)) {
                            Print.sysPrintln("  > Account Manager: " + managerID);
                        }

                        /* last login */
                        long lastLoginTime = account.getLastLoginTime();
                        String lastLoginDt = (lastLoginTime > 0L)? new DateTime(lastLoginTime,tmz).toString() : "never";
                        Print.sysPrintln("  > Last Login: " + lastLoginDt);

                        /* expiration */
                        long expireTime = account.getExpirationTime();
                        if (expireTime > 0L) {
                            String expireDt = new DateTime(expireTime,tmz).toString();
                            boolean expired = (expireTime < DateTime.getCurrentTimeSec());
                            Print.sysPrintln("  > Expires: " + expireDt + (expired?" [expired]":""));
                        }

                        /* rules */
                        //try {
                        //    String ruleList[] = Rule.getRuleIDs(acctListID, false/*activeOnly*/, true/*cronRules*/, false/*sysRules*/);
                        //    for (int r = 0; r < ruleList.length; r++) {
                        //        Rule rule = Rule.getRule(account, ruleList[r]);
                        //        if (rule != null) {
                        //            Print.sysPrintln("  Rule: " + ruleList[r] + " - " + rule.getDescription() + (rule.isActive()?"":" [inactive]"));
                        //        }
                        //    }
                        //} catch (Throwable th) {
                        //    // ignore
                        //}

                        /* default device authorization */
                        boolean groupALL = DBConfig.GetDefaultDeviceAuthorization(acctListID);
                        if (!groupALL) {
                            Print.sysPrintln("  > Group 'ALL' Allowed: " + groupALL);
                        }

                        /* device count */
                        OrderedSet<Device> devList = new OrderedSet<Device>();
                        OrderedSet<String> devIDList = Device.getDeviceIDsForAccount(DBReadWriteMode.READ_ONLY, acctListID, null/*User*/, true/*inclInactv*/);
                        int devCntTotal = 0;
                        int devCntActiv = 0;
                        for (String devID : devIDList) {
                            Device device = Device.getDevice(account, devID); // account.getDevice(devID);
                            if (device == null) {
                                // -- will not occur
                                continue;
                            }
                            // -- DCS name check?
                            if (!StringTools.isBlank(dcsName)) {
                                String dc = device.getDeviceCode();
                                if (dcsName.equals("BLANK") || dcsName.equalsIgnoreCase("/blank")) {
                                    if (!StringTools.isBlank(dc)) {
                                        // -- dcs name is not blank
                                        continue;
                                    }
                                } else
                                if (!dcsName.equalsIgnoreCase(dc)) {
                                    // -- dcs name does not match specified name
                                    continue;
                                }
                            }
                            // -- "since" time?
                            if (sinceDT != null) {
                                //Print.logInfo("Since time: " + sinceDT);
                                long lastTS = device.getLastConnectTime();
                                EventData lastEv[] = device.getLatestEvents(1L,false);
                                if (!ListTools.isEmpty(lastEv) && (lastEv[0].getTimestamp() > lastTS)) {
                                    lastTS = lastEv[0].getTimestamp();
                                    //Print.logInfo("Device latest event TS: " + (new DateTime(lastTS,sinceDT.getTimeZone())));
                                }
                                if (sinceDT.isAfter(lastTS,false)) {
                                    // -- 'sinceDT' is after last device connection time
                                    continue;
                                }
                            }
                            // --
                            devList.add(device);
                            devCntTotal++;
                            tdevCountTotal++;
                            if (account.getIsActive() && device.getIsActive()) {
                                devCntActiv++;
                                tdevCountActiv++;
                            }
                            tdevEventsPerSec += device.getAgedEventsPerSecond(nowMS);
                        }
                        Print.sysPrintln("  > Active Device Count: " + devCntActiv + 
                            ((devCntTotal > devCntActiv)? " [total=" + devCntTotal + "]" : ""));

                        /* devices */
                        if (!type.equalsIgnoreCase("count") && !RTConfig.hasProperty(ARG_SHORTLIST)) {
                            for (Device device : devList) {
                                String  uniq = device.getDataTransport().getUniqueID(); // default UniqueID
                                boolean actv = account.getIsActive() && device.getIsActive();
                                StringBuffer dsb = new StringBuffer();
                                dsb.append("  ");
                                if (fullList) { dsb.append("Device: "); }
                                dsb.append("[ID=").append(device.getDeviceID());
                                dsb.append(", uniq=").append(uniq);
                                dsb.append(", dcs=").append(device.getDeviceCode());
                                dsb.append(", active=").append(actv);
                                dsb.append("] ");
                                dsb.append(device.getDescription());
                                Print.sysPrintln(dsb.toString());
                                long lastConnectTime = device.getLastConnectTime(); // getLastTotalConnectTime()
                                String lastConnectDt = (lastConnectTime > 0L)? new DateTime(lastConnectTime,tmz).toString() : "never";
                                if (fullList) { 
                                    Print.sysPrintln("    > Last Connect: " + lastConnectDt);
                                    long    epst = device.getLastEventsPerSecondMS();
                                    double  eps  = device.getAgedEventsPerSecond(nowMS);
                                    double  eph  = eps * 60.0 * 60.0; // events per hour
                                    String  ephs = (eph >= 0.01)? StringTools.format(eph,"0.00") : "n/a";
                                    if (eventCnt) {
                                        long evCnt = device.getEventCount(); // -1 for InnoDB?
                                        if (evCnt > 0L) {
                                            EventData lastEv[] = device.getLatestEvents(1L,false);
                                            DateTime lastEventDt = new DateTime(lastEv[0].getTimestamp(),tmz);
                                            Print.sysPrintln("    > Events: " + evCnt + "  [" + lastEventDt + "] ev/h=" + ephs);
                                        } else 
                                        if (evCnt < 0L) {
                                            Print.sysPrintln("    > Events: unknown (InnoDB?)");
                                        }
                                    } else {
                                        EventData lastEv[] = device.getLatestEvents(1L,false);
                                        if (!ListTools.isEmpty(lastEv)) {
                                            long lastEventTime = lastEv[0].getTimestamp();
                                            String lastEventDt = new DateTime(lastEventTime,tmz).toString();
                                            Print.sysPrintln("    > Last Event: [" + lastEventDt + "] ev/h=" + ephs);
                                        } else {
                                            Print.sysPrintln("    > Last Event: n/a");
                                        }
                                    }
                                }
                                /*
                                Collection<String> ruleNames = null;
                                try {
                                    ruleNames = RuleList.getRulesForDevice(acctListID,devId,-1,true);
                                } catch (Throwable th) {
                                    // ignore
                                }
                                if (!ListTools.isEmpty(ruleNames)) {
                                    StringBuffer rsb = new StringBuffer();
                                    for (String ruleListID : ruleNames) {
                                        if (rsb.length() > 0) { rsb.append(", "); }
                                        rsb.append(ruleListID);
                                    }
                                    Print.sysPrintln("    > Rules: " + rsb);
                                }
                                */
                            }
                        }

                    } else
                    if (type.equalsIgnoreCase("comma")) {

                        // -- comma separated
                        if (acctCount > 1) { Print.sysPrint(","); }
                        Print.sysPrint(acctListID);

                    }

                } // for (String acctID : acctList)
                Print.sysPrintln("");
                Print.sysPrintln("Total Active Device Count: " + tdevCountActiv + 
                    ((tdevCountTotal > tdevCountActiv)? " [total=" + tdevCountTotal + "]" : ""));
                if (tdevEventsPerSec > 0.0) {
                    String eps = StringTools.format(tdevEventsPerSec, "0.000");
                    String eph = StringTools.format(tdevEventsPerSec * 3600.0, "0");
                    Print.sysPrintln("Average Rate of Events   : "+eps+"/second ["+eph+"/hour]");
                }
                Print.sysPrintln("");
            } catch (DBException dbe) {
                Print.logException("Error listing Accounts", dbe);
                System.exit(99);
            }
            System.exit(0);
        } // ARG_LIST

        /* prune */
        if (RTConfig.getBoolean(ARG_PRUNE, false)) {
            opts++;
            try {
                // -- delete temporary accounts that no-one has ever logged into
                Account.deleteAccounts(Account.getUnconfirmedAccounts());
                // -- delete temporary accounts that had expired 3 days ago (and were previously marked inactive)
                Account.deleteAccounts(Account.getExpiredAccounts(DateTime.DaySeconds(3L),false));
                // -- deactivate temporary accounts that have expired
                Account.deactivateAccounts(Account.getExpiredAccounts(0L,true));
            } catch (DBException dbe) {
                Print.logException("Error pruning Accounts", dbe);
                System.exit(99);
            }
            System.exit(0);
        } // ARG_PRUNE

        /* count/delete old events */
        if (RTConfig.hasProperty(ARG_CNT_OLD_EV) || 
            RTConfig.hasProperty(ARG_DEL_OLD_EV)   ) {
            opts++;
            boolean deleteEvents = RTConfig.hasProperty(ARG_DEL_OLD_EV);
            String actionText = deleteEvents? "Deleting" : "Counting";
            TimeZone acctTMZ = DateTime.GMT;
            String   argTime = deleteEvents?
                RTConfig.getString(ARG_DEL_OLD_EV,"") :
                RTConfig.getString(ARG_CNT_OLD_EV,"");
            DBReadWriteMode rwMode = deleteEvents?
                DBReadWriteMode.READ_WRITE :
                DBReadWriteMode.READ_ONLY;
            // -- arg time
            DateTime oldTime = null;
            if (StringTools.isBlank(argTime)) {
                Print.logError("Invalid time specification: " + argTime);
                System.exit(98);
            } else
            if (argTime.equalsIgnoreCase("current")) {
                oldTime = new DateTime(acctTMZ);
            } else {
                try {
                    oldTime = DateTime.parseArgumentDate(argTime,acctTMZ,true); // end of day time
                } catch (DateTime.DateParseException dpe) {
                    oldTime = null;
                }
                if (oldTime == null) {
                    Print.sysPrintln("Invalid Time specification: " + argTime);
                    System.exit(98);
                } else
                if (oldTime.getTimeSec() > DateTime.getCurrentTimeSec()) {
                    Print.sysPrintln(actionText + " future events not allowed");
                    System.exit(98);
                }
            }
            // -- Accounts
            Collection<String> acctList = null;
            if (accountExists) {
                // -- single account, ie. "-account=jones"
                acctList = new Vector<String>();
                acctList.add(acctID);
            } else
            if (acctID.equalsIgnoreCase("ALL")) {
                // -- all accounts, ie. "-account=ALL"
                try {
                    acctList = Account.getAllAccounts(rwMode);
                } catch (DBException dbe) {
                    acctList = null;
                }
            } else
            if (acctID.endsWith("*") || acctID.endsWith("%")) {
                // -- wildcard accounts, ie. "-account=falcon*"
                String partAcctID = acctID.substring(0,acctID.length()-1); // remove trailing '*'/'%'
                if (!StringTools.isBlank(partAcctID) && (partAcctID.indexOf(",") < 0)) {
                    acctList = new Vector<String>();
                    try {
                        Collection<String> alist = Account.getAllAccounts(rwMode);
                        for (String aid : alist) {
                            if (aid.startsWith(partAcctID)) {
                                acctList.add(aid);
                            }
                        }
                    } catch (DBException dbe) {
                        acctList = null;
                    }
                }
            } else 
            if (acctID.indexOf(",") >= 0) {
                // -- list of accounts, ie. "-account=smith,jones,max"
                acctList = ListTools.toList(StringTools.split(acctID,','));
            }
            // -- stop if no Account in list
            if (ListTools.isEmpty(acctList)) {
                Print.sysPrintln("No Accounts specified");
                System.exit(1);
            }
            // -- count/delete events
            long    oldTimeSec = oldTime.getTimeSec();
            boolean confirmDel = RTConfig.getBoolean(ARG_CONFIRM_DEL,false);
            try {
                if (deleteEvents) {
                    Print.sysPrintln("Deleting events prior to: " + (new DateTime(oldTimeSec)));
                    if (!confirmDel) {
                        Print.sysPrintln("ERROR: Missing '-"+ARG_CONFIRM_DEL[0]+"', aborting delete ...");
                        System.exit(1);
                    }
                    for (String A : acctList) {
                        if (!StringTools.isBlank(A)) {
                            Account account = Account.getAccount(A);
                            if (account != null) {
                                account.deleteOldEvents(oldTimeSec, true); // InnoDB
                            } else {
                                Print.sysPrintln("WARN: Skipping non-existent Account: " + A);
                            }
                        }
                    }
                } else {
                    Print.sysPrintln("Counting events prior to: " + (new DateTime(oldTimeSec)));
                    for (String A : acctList) {
                        if (!StringTools.isBlank(A)) {
                            Account account = Account.getAccount(A);
                            if (account != null) {
                                account.countOldEvents(oldTimeSec, true); // InnoDB?
                            } else {
                                Print.sysPrintln("WARN: Skipping non-existent Account: " + A);
                            }
                        }
                    }
                }
                System.exit(0);
            } catch (DBException dbe) {
                Print.logError("Error " + actionText + " old events: " + dbe);
                System.exit(99);
            }
        }

        /* find email address */
        if (RTConfig.hasProperty(ARG_FIND_EMAIL)) {
            opts++;
            String emailAddr = RTConfig.getString(ARG_FIND_EMAIL,"");
            Print.sysPrintln("Searching for email address: " + emailAddr);
            // -- Iterate through accounts
            DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
            int findCount = 0;
            try {
                Collection<String> AIDList = Account.getAllAccounts(rwMode);
                for (String AID : AIDList) {
                    Account A = Account.getAccount(rwMode, AID); // may return null
                    if (A == null) { continue; /* should not occur */ }
                    String AHdr = " Account " + AID;
                    if (A.hasManagerID()) { AHdr += "[" + A.getManagerID() + "]"; }
                    //Print.sysPrintln("  Searching " + AHdr + " ...");
                    // -- Account Contact Email Address
                    String ace = A.getContactEmail();
                    if (StringTools.indexOfIgnoreCase(ace,emailAddr) >= 0) {
                        Print.sysPrintln(AHdr + ": Contact Email [Account."+Account.FLD_contactEmail+"] " + ace);
                        findCount++;
                    }
                    // -- Account Notify Email Address
                    String ane = A.getNotifyEmail();
                    if (StringTools.indexOfIgnoreCase(ane,emailAddr) >= 0) {
                        Print.sysPrintln(AHdr + ": Notify Email [Account."+Account.FLD_notifyEmail+"] " + ane);
                        findCount++;
                    }
                    // -- Users
                    String UIDList[] = User.getUsersForAccount(DBReadWriteMode.READ_ONLY, AID);
                    for (String UID : UIDList) {
                        User U = User.getUser(A, UID);
                        if (U == null) { continue; /* should not occur */ }
                        String UHdr = " User " + AID + "/" + UID;
                        //Print.sysPrintln("    Searching " + UHdr + " ...");
                        // -- Contact Email Address
                        String uce = U.getContactEmail();
                        if (StringTools.indexOfIgnoreCase(uce,emailAddr) >= 0) {
                            Print.sysPrintln(UHdr + ": Contact Email [User."+User.FLD_contactEmail+"] " + uce);
                            findCount++;
                        }
                    }
                    // -- Devices
                    OrderedSet<String> DIDList = Device.getDeviceIDsForAccount(DBReadWriteMode.READ_ONLY, AID, null/*User*/, true/*inclInactv*/);
                    for (String DID : DIDList) {
                        Device D = Device._getDevice(A, DID);
                        if (D == null) { continue; /* should not occur */ }
                        String DHdr = " Device " + AID + "/" + DID;
                        //Print.sysPrintln("    Searching " + DHdr + " ...");
                        // -- Notify Email Address
                        String dne = D.getNotifyEmail(false/*inclAcct*/,false/*inclUser*/,false/*inclGroup*/);
                        if (StringTools.indexOfIgnoreCase(dne,emailAddr) >= 0) {
                            Print.sysPrintln(DHdr + ": Notify Email [Device."+Account.FLD_notifyEmail+"] " + dne);
                            findCount++;
                        }
                        // -- SMS Email Address
                        String sme = D.getSmsEmail();
                        if (StringTools.indexOfIgnoreCase(sme,emailAddr) >= 0) {
                            Print.sysPrintln(DHdr + ": SMS Email [Device."+Account.FLD_notifyEmail+"] " + sme);
                            findCount++;
                        }
                    }
                    // -- Rules (optional - may not be present)
                    RuleFactory ruleFactory = Device.getRuleFactory();
                    if (ruleFactory != null) {
                        String RIDList[] = ruleFactory.getRuleIDs(A); // may be null
                        if (RIDList == null) { RIDList = new String[0]; }
                        for (String RID : RIDList) {
                            String RHdr = " Rule " + AID + "/" + RID;
                            //Print.sysPrintln("    Searching " + RHdr + " ...");
                            // Notify Email Address
                            String rne = StringTools.trim(ruleFactory.getRuleNotifyEmail(A,RID));
                            if (StringTools.indexOfIgnoreCase(rne,emailAddr) >= 0) {
                                Print.sysPrintln(RHdr + ": Notify Email [Rule."+FLD_notifyEmail+"] " + rne);
                                findCount++;
                            }
                        }
                    }
                    // -- ReportJob (optional - may not be present)
                    String rjClassName = DBConfig.PACKAGE_EXTRA_TABLES_ + "ReportJob";
                    try {
                        Class<?> rjClass = Class.forName(rjClassName);
                        MethodAction getRJs = new MethodAction(rjClass, "getReportJobsForAccount", String.class);
                        Object rjList[] = (Object[])getRJs.invoke(AID);
                        if (!ListTools.isEmpty(rjList)) {
                            for (Object rj : rjList) {
                                // -- get "getReportJobID"
                                MethodAction maGetID = new MethodAction(rj,"getReportJobID");
                                String RJID = (String)maGetID.invoke();
                                String RJHdr = " ReportJob " + AID + "/" + RJID;
                                // -- get recipients
                                MethodAction maGetRecip = new MethodAction(rj,"getRecipients");
                                String rjRecip = StringTools.trim(maGetRecip.invoke());
                                if (StringTools.indexOfIgnoreCase(rjRecip,emailAddr) >= 0) {
                                    Print.sysPrintln(RJHdr + ": ReportJob Email [ReportJob.recipients] " + rjRecip);
                                    findCount++;
                                }
                            }
                        }
                    } catch (Throwable th) {
                        // skip
                    }
                    // ---
                }
                if (findCount <= 0) {
                    Print.sysPrintln("Email address not found");
                } else {
                    Print.sysPrintln("Found " + findCount + " matches");
                }
            } catch (DBException dbe) {
                Print.logException("Account error", dbe);
                System.exit(99);
            }
            System.exit(0);
        }

        /* update blank addresses */
        String updAddrArgs = RTConfig.getString(ARG_UPDATE_ADDRESS,null);
        if (!StringTools.isBlank(updAddrArgs)) {
            // -- bin/admin.pl Account -account=ALL -updAddr=-48h,2
            // -- bin/admin.pl Account -account=demo,demo2 -updAddr=1542700800,1542787199,2
            String a[] = StringTools.split(updAddrArgs,',');
            if (ListTools.isEmpty(a)) {
                Print.sysPrintln("Start/End times not specified");
                System.exit(1);
            }
            TimeZone tmz   = DateTime.getDefaultTimeZone(); // Account.getTimeZone(acct)
            long nowSec    = DateTime.getCurrentTimeSec();
            long startTime = -1L;
            long endTime   = -1L;
            int  poolSize  = -1;
            if (a[0].startsWith("-")) {
                // -- "-1d,2", "-24h,3"
                if (a[0].endsWith("d") || a[0].endsWith("D")) {
                    // -- days
                    long v = StringTools.parseLong(a[0].substring(1),-1L);
                    if (v > 0L) {
                        long ts   = nowSec - DateTime.DaySeconds(v);
                        DateTime dt = new DateTime(ts, tmz);
                        startTime = dt.getDayStart(tmz);
                        endTime   = -1L;
                    }
                } else
                if (a[0].endsWith("h") || a[0].endsWith("H")) {
                    // -- hours
                    long v = StringTools.parseLong(a[0].substring(1),-1L);
                    if (v > 0L) {
                        long ts   = nowSec - DateTime.HourSeconds(v);
                        startTime = ts;
                        endTime   = -1L;
                    }
                } else
                if (a[0].endsWith("m") || a[0].endsWith("M")) {
                    // -- minutes
                    long v = StringTools.parseLong(a[0].substring(1),-1L);
                    if (v > 0L) {
                        long ts   = nowSec - DateTime.MinuteSeconds(v);
                        startTime = ts;
                        endTime   = -1L;
                    }
                } else {
                    // -- else assume seconds
                    long v = StringTools.parseLong(a[0].substring(1),-1L);
                    if (v > 0L) {
                        long ts   = nowSec - v;
                        startTime = ts;
                        endTime   = -1L;
                    }
                }
                poolSize  = (a.length > 1)? StringTools.parseInt( a[1],-1 ) : -1 ;
            } else {
                // -- "StartEpoch[,EndEpoch[,PoolSize]]"
                startTime = (a.length > 0)? StringTools.parseLong(a[0],-1L) : -1L;
                endTime   = (a.length > 1)? StringTools.parseLong(a[1],-1L) : -1L;
                poolSize  = (a.length > 2)? StringTools.parseInt( a[2],-1 ) : -1 ;
            }
            // -- validate start/end time
            if (startTime <= 0L) {
                Print.sysPrintln("Invalid StartTime: " + startTime);
                System.exit(1);
            } else
            if (startTime <= (nowSec - DateTime.DaySeconds(30))) {
                Print.sysPrintln("Invalid StartTime: " + startTime + " (too old)");
                System.exit(1);
            } else
            if ((endTime > 0L) && (endTime <= startTime)) {
                Print.sysPrintln("Invalid StartTime: " + endTime);
                System.exit(1);
            }
            // -- header
            Print.logInfo("Starting: " + new DateTime(startTime,tmz));
            Print.logInfo("Ending  : " + ((endTime>0L)?new DateTime(endTime,tmz):"end"));
            Print.logInfo("PoolSize: " + poolSize);
            // -- update addresses
            try {
                Collection<String> acctList = ListTools.toList(StringTools.split(acctID,','));
                Account.updateBlankEventAddresses(acctList, startTime, endTime, poolSize);
            } catch (DBException dbe) {
                Print.logException("Error updating addresses", dbe);
                System.exit(99);
            }
            // -- done
            System.exit(0);
        }

        /* no options specified */
        if (opts == 0) {
            Print.logWarn("Missing options ...");
            usage();
        }

    }

}
