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
//  2007/01/25  Martin D. Flynn
//     -Initial release
//  2007/03/30  Martin D. Flynn
//     -Moved to "org.opengts.db.tables"
//  2007/06/13  Martin D. Flynn
//     -Added BLANK_PASSWORD to explicitly support blank passwords
//  2007/06/14  Martin D. Flynn
//     -Fixed 'isAuthorizedDevice' to return true if no 'deviceGroup' has been specified.
//  2007/07/14  Martin D. Flynn
//     -Added "-nopass" & "-password" options to command-line administration.
//  2007/09/16  Martin D. Flynn
//     -Integrated DBSelect
//  2007/11/28  Martin D. Flynn
//     -Added '-editall' command-line option to display all fields.
//  2008/07/21  Martin D. Flynn
//     -Fixed problem preventing device groups from being set properly.
//  2008/08/15  Martin D. Flynn
//     -Explicitly write DeviceGroup "all" to authorized device group list when 
//      DBConfig.DEFAULT_DEVICE_AUTHORIZATION is 'false'.
//     -Added static methods 'getAdminUserID()' and 'isAdminUser(...)'
//  2008/09/01  Martin D. Flynn
//     -Added 'FLD_firstLoginPageID'
//  2008/10/16  Martin D. Flynn
//     -Changed 'getUsersForContactEmail' to return a list of 'User' objects.
//     -Changed unspecified 'gender' text from "Unknown" to "n/a" (not applicable)
//     -Added fields 'FLD_preferredDeviceID', 'FLD_roleID'
//  2011/03/08  Martin D. Flynn
//     -Added FLD_notifyEmail
//  2013/11/11  Martin D. Flynn
//     -Default timezone is now obtained from the Account record.
//  2016/01/04  Martin D. Flynn
//     -Added FLD_expirationTime
//  2016/04/01  Martin D. Flynn
//     -Added FLD_suspendUntilTime [2.6.2-B50]
// ----------------------------------------------------------------------------
package org.opengts.db.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;
import java.security.Principal;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONBeanGetter;
import org.opengts.util.JSON.JSONBeanSetter;

import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.AclEntry.AccessLevel;
import org.opengts.db.tables.*;

public class User
    extends UserRecord<User>
    implements UserInformation,
               JSON.JSONBean
{

    // ------------------------------------------------------------------------

    private static      int     LastPasswordColumnLength        = -1; // FLD_lastPasswords

    // ------------------------------------------------------------------------

    /* optional columns */
    public static final String  OPTCOLS_AddressFieldInfo            = "startupInit.User.AddressFieldInfo";
    public static final String  OPTCOLS_ExtraFieldInfo              = "startupInit.User.ExtraFieldInfo";
    public static final String  OPTCOLS_PlatinumInfo                = "startupInit.User.PlatinumInfo";

    // ------------------------------------------------------------------------
    // Administrator user name

    public static final String  USER_ADMIN = "admin";

    /**
    *** Gets the defined "admin" user id
    *** @return The defined "admin" user id
    **/
    public static String getAdminUserID()
    {
        return USER_ADMIN;
    }

    /**
    *** Returns true if specified user is an "admin" user
    *** @param userID  The userID to test
    *** @return True if the specified is an "admin" user
    **/
    public static boolean isAdminUser(String userID)
    {
        if (StringTools.isBlank(userID)) {
            return false; // must be explicit
        } else {
            return User.getAdminUserID().equals(userID);
        }
    }

    /**
    *** Returns true if specified user is and "admin" user
    *** @param user  The user to test
    *** @return True if the specified is an "admin" user
    **/
    public static boolean isAdminUser(User user)
    {
        if (user == null) {
            return true; // null user is considered an 'admin'
        } else {
            return User.getAdminUserID().equalsIgnoreCase(user.getUserID());
        }
    }
    
    /**
    *** Gets the account/user name for the specified user. <br>
    *** (typically used for debug/logging purposes)
    *** @param user  The user for which the account/user name is returned
    *** @return The account/user id/name
    **/
    public static String getUserName(User user)
    {
        if (user == null) {
            return "null";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(user.getAccountID());
            sb.append("/");
            sb.append(user.getUserID());
            sb.append("] ");
            sb.append(user.getDescription());
            return sb.toString().trim();
        }
    }

    // ------------------------------------------------------------------------
    // blank password
    
    public static final String  BLANK_PASSWORD      = Account.BLANK_PASSWORD;

    // ------------------------------------------------------------------------
    // max field lengths

    public static       int     UserIDColumnLength  = -1;   // FLD_userID

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // User preferred device authorization

    public enum PreferredDeviceAuth {
        FALSE,
        TRUE,
        ONLY
    };

    /**
    *** Gets the global enumerated value for the property:<br>
    ***     User.authorizedPreferredDeviceID
    **/
    public static PreferredDeviceAuth GetPreferredDeviceAuth()
    {
        // -- User.authorizedPreferredDeviceID=false|true|only
        String prefDevAuth = RTConfig.getString(DBConfig.PROP_User_authorizedPreferredDeviceID,"");
        if (StringTools.isBlank(prefDevAuth)) {
            return PreferredDeviceAuth.FALSE;
        } else
        if (prefDevAuth.equalsIgnoreCase("false")) {
            return PreferredDeviceAuth.FALSE;
        } else
        if (prefDevAuth.equalsIgnoreCase("true")) {
            return PreferredDeviceAuth.TRUE;
        } else
        if (prefDevAuth.equalsIgnoreCase("only")) { // [2.6.3-B30]
            return PreferredDeviceAuth.ONLY;
        } else {
            return PreferredDeviceAuth.FALSE;
        }
    }

    /**
    *** True if the global enumerated "User.authorizedPreferredDeviceID" property value is ONLY
    **/
    public static boolean IsPreferredDeviceAuth_ONLY()
    {
        // -- User.authorizedPreferredDeviceID=only
        return User.GetPreferredDeviceAuth().equals(PreferredDeviceAuth.ONLY);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // User type enum

    public enum UserType implements EnumTools.StringLocale, EnumTools.IntValue {
        TYPE_000    (  0, I18N.getString(User.class,"User.type.type000"  ,"Type000"  )), // default
        TYPE_001    (  1, I18N.getString(User.class,"User.type.type001"  ,"Type001"  )),
        TYPE_002    (  2, I18N.getString(User.class,"User.type.type002"  ,"Type002"  )),
        TYPE_003    (  3, I18N.getString(User.class,"User.type.type003"  ,"Type003"  )),
        TYPE_010    ( 10, I18N.getString(User.class,"User.type.type010"  ,"Type010"  )),
        TYPE_011    ( 11, I18N.getString(User.class,"User.type.type011"  ,"Type011"  )),
        TYPE_020    ( 20, I18N.getString(User.class,"User.type.type020"  ,"Type020"  )),
        TYPE_021    ( 21, I18N.getString(User.class,"User.type.type021"  ,"Type021"  )),
        TYPE_030    ( 30, I18N.getString(User.class,"User.type.type030"  ,"Type030"  )),
        TYPE_031    ( 31, I18N.getString(User.class,"User.type.type031"  ,"Type031"  )),
        TEMPORARY   (900, I18N.getString(User.class,"User.type.temporary","Temporary")),
        SYSTEM      (999, I18N.getString(User.class,"User.type.system"   ,"System"   ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        UserType(int v, I18N.Text a)                { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isDefault()                  { return this.equals(TYPE_000); }
        public boolean isTemporary()                { return this.equals(TEMPORARY); }
        public boolean isSystem()                   { return this.equals(SYSTEM); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Gender enum

    public enum Gender implements EnumTools.StringLocale, EnumTools.IntValue {
        UNKNOWN (0, I18N.getString(User.class,"User.gender.notSpecified","n/a"    )), // "not applicable"
        MALE    (1, I18N.getString(User.class,"User.gender.male"        ,"Male"   )),
        FEMALE  (2, I18N.getString(User.class,"User.gender.female"      ,"Female" ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        Gender(int v, I18N.Text a)                  { vv=v; aa=a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
    };

    /**
    *** Returns the defined Gender for the specified user.
    *** @param u  The user from which the Gender will be obtained.  
    ***           If null, the default Gender will be returned.
    *** @return The Gender
    **/
    public static Gender getGender(User u)
    {
        return (u != null)? EnumTools.getValueOf(Gender.class,u.getGender()) : EnumTools.getDefault(Gender.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** EXPERIMENTAL
    *** DBRecordListener callback API
    **/
    public static class RecordListener
        implements DBRecordListener<User>
    {
        private DBRecordListener<User> delegate = null;
        public RecordListener() {
            this.delegate = null;
            // -- TODO: assign delegate
        }
        public void recordWillInsert(User user) {
            //Print.logDebug("* User will be inserted: " + user.getAccountID() + "/" + user.getUserID());
            if (this.delegate != null) {
                this.delegate.recordWillInsert(user);
            }
        }
        public void recordDidInsert(User user) {
            Print.logDebug("* User inserted: " + user.getAccountID() + "/" + user.getUserID());
            if (this.delegate != null) {
                this.delegate.recordDidInsert(user);
            }
        }
        public void recordWillUpdate(User user) {
            //Print.logDebug("* User will be updated: " + user.getAccountID() + "/" + user.getUserID());
            if (this.delegate != null) {
                this.delegate.recordWillUpdate(user);
            }
        }
        public void recordDidUpdate(User user) {
            Print.logDebug("* User updated: " + user.getAccountID() + "/" + user.getUserID());
            if (this.delegate != null) {
                this.delegate.recordDidUpdate(user);
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Timezone

    /**
    *** Gets the default User timezone
    **/
    public static String GetDefaultTimeZone()
    {
        return Account.GetDefaultTimeZone();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- User JSONBean implementation that places the User JSON object
    // -  within another object with key name "User"

    public JSON.JSONBean getUserBean()
    {
        return new JSON.JSONBean() {
            @JSONBeanGetter(name="User")
            public User getUser() {
                return User.this;
            }
        };
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME                  = "User";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_userType                 = "userType";
    public static final String FLD_roleID                   = RoleAcl.FLD_roleID; 
    public static final String FLD_password                 = Account.FLD_password;
    public static final String FLD_tempPassword             = Account.FLD_tempPassword;
    public static final String FLD_lastPasswords            = Account.FLD_lastPasswords;
    public static final String FLD_gender                   = "gender";
    public static final String FLD_notifyEmail              = Account.FLD_notifyEmail;
    public static final String FLD_contactName              = Account.FLD_contactName;
    public static final String FLD_contactPhone             = Account.FLD_contactPhone;
    public static final String FLD_contactEmail             = Account.FLD_contactEmail;
    public static final String FLD_timeZone                 = Account.FLD_timeZone;
    public static final String FLD_speedUnits               = Account.FLD_speedUnits;
    public static final String FLD_distanceUnits            = Account.FLD_distanceUnits;
    public static final String FLD_firstLoginPageID         = "firstLoginPageID";       // first page viewed at login
    public static final String FLD_preferredDeviceID        = "preferredDeviceID";      // preferred device ID
    public static final String FLD_maxAccessLevel           = "maxAccessLevel";
    public static final String FLD_passwdChangeTime         = "passwdChangeTime";
    public static final String FLD_passwdQueryTime          = "passwdQueryTime";
    public static final String FLD_expirationTime           = "expirationTime";
    public static final String FLD_suspendUntilTime         = "suspendUntilTime";
    public static final String FLD_lastLoginTime            = "lastLoginTime";
    public static final String FLD_welcomeTime              = "welcomeTime";
    private static DBField FieldInfo[] = {
        // -- Key fields
        newField_accountID(true),
        newField_userID(true),
        // -- User fields
        new DBField(FLD_userType            , Integer.TYPE  , DBField.TYPE_UINT16      , "User Type"                 , "edit=2"),
        new DBField(FLD_roleID              , String.class  , DBField.TYPE_ROLE_ID()   , "User Role"                 , "edit=2 altkey=role"),
        new DBField(FLD_password            , String.class  , DBField.TYPE_STRING(32)  , "Password"                  , "edit=2 editor=password"),
        new DBField(FLD_tempPassword        , String.class  , DBField.TYPE_STRING(32)  , "Temporary Password"        , "edit=2 editor=password"),
        new DBField(FLD_lastPasswords       , String.class  , DBField.TYPE_STRING(300) , "Prior Passwords"           , "edit=2"),
        new DBField(FLD_gender              , Integer.TYPE  , DBField.TYPE_UINT8       , "Gender"                    , "edit=2 enum=User$Gender"),
        new DBField(FLD_notifyEmail         , String.class  , DBField.TYPE_EMAIL_LIST(), "Notification EMail Address", "edit=2"),
        new DBField(FLD_contactName         , String.class  , DBField.TYPE_STRING(64)  , "Contact Name"              , "edit=2 utf8=true"),
        new DBField(FLD_contactPhone        , String.class  , DBField.TYPE_STRING(32)  , "Contact Phone"             , "edit=2"),
        new DBField(FLD_contactEmail        , String.class  , DBField.TYPE_STRING(64)  , "Contact EMail Address"     , "edit=2 altkey=email"),
        new DBField(FLD_timeZone            , String.class  , DBField.TYPE_STRING(32)  , "Time Zone"                 , "edit=2 editor=timeZone"),
      //new DBField(FLD_speedUnits          , Integer.TYPE  , DBField.TYPE_UINT8       , "Speed Units"               , "edit=2 enum=Account$SpeedUnits"),
      //new DBField(FLD_distanceUnits       , Integer.TYPE  , DBField.TYPE_UINT8       , "Distance Units"            , "edit=2 enum=Account$DistanceUnits"),
        new DBField(FLD_firstLoginPageID    , String.class  , DBField.TYPE_STRING(24)  , "First Login Page ID"       , "edit=2"),
        new DBField(FLD_preferredDeviceID   , String.class  , DBField.TYPE_DEV_ID()    , "Preferred Device ID"       , "edit=2"),
        new DBField(FLD_maxAccessLevel      , Integer.TYPE  , DBField.TYPE_UINT16      , "Maximum Access Level"      , "edit=2 enum=AclEntry$AccessLevel"),
        new DBField(FLD_passwdChangeTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Change Time" , "format=time"),
        new DBField(FLD_passwdQueryTime     , Long.TYPE     , DBField.TYPE_UINT32      , "Last Password Query Time"  , "format=time"),
        new DBField(FLD_expirationTime      , Long.TYPE     , DBField.TYPE_UINT32      , "Expiration Time"           , "format=time"),
        new DBField(FLD_suspendUntilTime    , Long.TYPE     , DBField.TYPE_UINT32      , "Suspend Until Time"        , "format=time"),
        new DBField(FLD_lastLoginTime       , Long.TYPE     , DBField.TYPE_UINT32      , "Last Login Time"           , "format=time"),
        new DBField(FLD_welcomeTime         , Long.TYPE     , DBField.TYPE_UINT32      , "Welcome Notice Time"       , "format=time"),
        // -- Common fields
        newField_isActive(),
        newField_displayName(),
        newField_description(),
        newField_notes(),
        newField_lastUpdateTime(),
        newField_lastUpdateAccount(true),
        newField_lastUpdateUser(true),
        newField_creationTime(),
    };

    // Address fields
    // startupInit.User.AddressFieldInfo=true
    public static final String FLD_addressLine1             = "addressLine1";           // address line 1
    public static final String FLD_addressLine2             = "addressLine2";           // address line 2
    public static final String FLD_addressLine3             = "addressLine3";           // address line 3
    public static final String FLD_addressCity              = "addressCity";            // address city
    public static final String FLD_addressState             = "addressState";           // address state/province
    public static final String FLD_addressPostalCode        = "addressPostalCode";      // address postal code
    public static final String FLD_addressCountry           = "addressCountry";         // address country
    public static final String FLD_officeLocation           = "officeLocation";         // office location (id, region, etc)
    public static final DBField AddressFieldInfo[] = {
        new DBField(FLD_addressLine1        , String.class  , DBField.TYPE_STRING(70)  , "Address Line 1"            , "edit=2 utf8=true"),
        new DBField(FLD_addressLine2        , String.class  , DBField.TYPE_STRING(70)  , "Address Line 2"            , "edit=2 utf8=true"),
        new DBField(FLD_addressLine3        , String.class  , DBField.TYPE_STRING(70)  , "Address Line 3"            , "edit=2 utf8=true"),
        new DBField(FLD_addressCity         , String.class  , DBField.TYPE_STRING(50)  , "Address City"              , "edit=2 utf8=true"),
        new DBField(FLD_addressState        , String.class  , DBField.TYPE_STRING(50)  , "Address State/Province"    , "edit=2 utf8=true"),
        new DBField(FLD_addressPostalCode   , String.class  , DBField.TYPE_STRING(20)  , "Address Postal Code"       , "edit=2 utf8=true"),
        new DBField(FLD_addressCountry      , String.class  , DBField.TYPE_STRING(20)  , "Address Country"           , "edit=2 utf8=true"),
        new DBField(FLD_officeLocation      , String.class  , DBField.TYPE_STRING(200) , "Office Location"           , "edit=2 utf8=true"),
    };

    // Misc fields
    // startupInit.User.ExtraFieldInfo=true
    public static final String FLD_customAttributes         = "customAttributes";      // custom attributes
    public static final DBField ExtraFieldInfo[] = {
        new DBField(FLD_customAttributes    , String.class , DBField.TYPE_TEXT         , "Custom Fields"             , "edit=2 utf8=true"),
    };

    // -- Platinum Edition fields
    // -  [OPTCOLS_PlatinumInfo] startupInit.User.PlatinumInfo=true
    public static final String FLD_isDispatcher             = "isDispatcher";
    public static final DBField PlatinumInfo[]              = {
        new DBField(FLD_isDispatcher        , Boolean.TYPE , DBField.TYPE_BOOLEAN      , "isDispatcher"              , "edit=2"),
    };

    /* key class */
    public static class Key
        extends UserKey<User>
    {
        public Key() {
            super();
        }
        public Key(String acctId, String userId) {
            super.setKeyValue(FLD_accountID, ((acctId != null)? acctId.toLowerCase() : ""));
            super.setKeyValue(FLD_userID   , ((userId != null)? userId.toLowerCase() : ""));
        }
        public DBFactory<User> getFactory() {
            return User.getFactory();
        }
    }

    /* factory constructor */
    private static DBFactory<User> factory = null;
    public static DBFactory<User> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                User.TABLE_NAME(), 
                User.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                User.class, 
                User.Key.class,
                true/*editable*/, true/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
            // -- FLD_userID max length
            DBField userIdFld = factory.getField(FLD_userID);
            User.UserIDColumnLength = (userIdFld != null)? userIdFld.getStringLength()   : 0;
            // -- FLD_lastPasswords max length
            DBField lastPwFld = factory.getField(FLD_lastPasswords);
            User.LastPasswordColumnLength = (lastPwFld != null)? lastPwFld.getStringLength() : 0;
        }
        return factory;
    }

    /* Bean instance */
    public User()
    {
        super();
    }

    /* database record */
    public User(User.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(User.class, loc);
        return i18n.getString("User.description", 
            "This table defines " +
            "Account specific Users."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    /* return the user type */
    @JSONBeanGetter(enumClass="UserType",ignore="$zero")
    public int getUserType()
    {
        Integer v = (Integer)this.getFieldValue(FLD_userType);
        return (v != null)? v.intValue() : 0;
    }

    /* set the user type */
    public void setUserType(int v)
    {
        this.setFieldValue(FLD_userType, ((v >= 0)? v : 0));
    }

    // ------------------------------------------------------------------------

    /* gets the defined Role, or null if no role was defined */
    private Role userRole = null;
    public Role getRole()
    {
        if ((this.userRole == null) && !StringTools.isBlank(this.getRoleID())) {
            try {
                this.userRole = Role.getRole(this.getAccountID(), this.getRoleID());
                if (this.userRole != null) {
                    if (this.hasAccount() && !this.userRole.isSystemAdminRole()) {
                        // Only set the Role account if not a SystemAdmin Role.
                        this.userRole.setAccount(this.getAccount());
                    }
                } else {
                    Print.logError("User Role not found: %s/%s [user=%s]", this.getAccountID(), this.getRoleID(), this.getUserID());
                    return null;
                }
            } catch (DBException dbe) {
                Print.logException("Error retrieving User Role: " + this.getAccountID() + "/" + this.getRoleID(), dbe);
                return null;
            }
        }
        return this.userRole; // may be null
    }

    /* get the user role id */
    @JSONBeanGetter(ignore="$blank")
    public String getRoleID()
    {
        String v = (String)this.getFieldValue(FLD_roleID);
        return StringTools.trim(v);
    }

    /* set the user role id */
    public void setRoleID(String v)
    {
        this.setFieldValue(FLD_roleID, StringTools.trim(v));
        this.userRole = null;
    }

    /* returns true if this user has a defined RoleID */
    public boolean hasRoleID()
    {
        return !StringTools.isBlank(this.getRoleID())? true : false;
    }

    // ------------------------------------------------------------------------

    private UserPrincipal userPrincipal = null;

    /**
    *** Creates/Returns a Principal instance backed by this user
    *** @param saveUI  Save UserInformation instance (save this Account instance)
    **/
    public Principal getPrincipal(boolean saveUI)
    {
        if (this.userPrincipal == null) {
            this.userPrincipal = new UserPrincipal(this,saveUI);
        }
        return this.userPrincipal;
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
    *** Sets the encoded password for this user 
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
    *** Sets the encoded password for this user 
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
        this.setFieldValue(FLD_lastPasswords, ((p != null)? p : ""));
    }

    /**
    *** Adds a password to the last passwords list
    **/
    public void addLastPassword(String p)
    {
        // -- get number of required unique passwords
        PasswordHandler pwh = Account.getPasswordHandler(this.getAccount()); // non-null
        int reqUniqPass = pwh.getRequiredUniquePasswordCount();
        if (reqUniqPass <= 0) {
            this.setLastPasswords("");
            return;
        }
        // -- 
        java.util.List<String> lpList = new Vector<String>();
        lpList.add(p);
        // --
        java.util.List<String> lpl = Account.decodeLastPasswords(this.getLastPasswords());
        if (!ListTools.isEmpty(lpl)) {
            for (int i = 0; (lpList.size() < reqUniqPass) && (i < lpl.size()); i++) {
                lpList.add(lpl.get(i));
            }
        }
        // --
        String encLastPwds = Account.encodeLastPasswords(lpList,User.LastPasswordColumnLength);
        this.setLastPasswords(encLastPwds);
    }

    // --------

    /**
    *** Gets a list of the last used encoded passwords (including current password)
    **/
    public String[] getLastEncodedPasswords()
    {
        // -- get number of required unique passwords
        PasswordHandler pwh = Account.getPasswordHandler(this.getAccount()); // non-null
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
    *** Gets the decoded password for this user.
    *** Returns null if password cannot be decoded.
    **/
    public String getDecodedPassword(BasicPrivateLabel bpl)
    {
        if (bpl == null) {
            bpl = Account.getPrivateLabel(this.getAccount());
        }
        String pass = Account.decodePassword(bpl, this.getEncodedPassword());
        // -- it is possible that this password cannot be decoded
        return pass; // 'null' if password cannot be decoded
    }

    /**
    *** Encodes and sets the entered password for this user 
    **/
    public void setDecodedPassword(BasicPrivateLabel bpl, String enteredPass, boolean isTemp)
    {
        // -- get BasicPrivateLabel
        if (bpl == null) {
            bpl = Account.getPrivateLabel(this.getAccount());
        }
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
            bpl = Account.getPrivateLabel(this.getAccount());
        }
        // -- check password
        boolean ok = Account._checkPassword(bpl, this, enteredPass);
        if (!ok && suspend) {
            // -- suspend on excessive failed login attempts
            this.suspendOnLoginFailureAttempt(true); // count current login failure
        }
        return ok;
    }
    // -- the released CelltracGTS/Server still references this method
    @Deprecated
    public boolean checkPassword(BasicPrivateLabel bpl, String enteredPass)
    {
        return this.checkPassword(bpl, enteredPass, false);
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
        this.update(User.FLD_password, User.FLD_tempPassword, User.FLD_lastPasswords);
    }

    // ------------------------------------------------------------------------

    /* get the gender of the user */
    @JSONBeanGetter(enumClass="Gender",ignore="$zero")
    public int getGender()
    {
        Integer v = (Integer)this.getFieldValue(FLD_gender);
        return (v != null)? v.intValue() : EnumTools.getDefault(Gender.class).getIntValue();
    }

    /* set the gender */
    public void setGender(int v)
    {
        this.setFieldValue(FLD_gender, EnumTools.getValueOf(Gender.class,v).getIntValue());
    }

    /* set the gender */
    public void setGender(Gender v)
    {
        this.setFieldValue(FLD_gender, EnumTools.getValueOf(Gender.class,v).getIntValue());
    }

    /* set the string representation of the gender */
    public void setGender(String v, Locale locale)
    {
        this.setFieldValue(FLD_gender, EnumTools.getValueOf(Gender.class,v,locale).getIntValue());
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
        this.setFieldValue(FLD_notifyEmail, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get contact name of this user */
    @JSONBeanGetter(ignore="$blank")
    public String getContactName()
    {
        String v = (String)this.getFieldValue(FLD_contactName);
        return StringTools.trim(v);
    }

    public void setContactName(String v)
    {
        this.setFieldValue(FLD_contactName, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get contact phone of this user */
    @JSONBeanGetter(ignore="$blank")
    public String getContactPhone()
    {
        String v = (String)this.getFieldValue(FLD_contactPhone);
        return StringTools.trim(v);
    }

    public void setContactPhone(String v)
    {
        this.setFieldValue(FLD_contactPhone, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /* get contact email of this user */
    @JSONBeanGetter(ignore="$blank")
    public String getContactEmail()
    {
        String v = (String)this.getFieldValue(FLD_contactEmail);
        return StringTools.trim(v);
    }

    public void setContactEmail(String v)
    {
        this.setFieldValue(FLD_contactEmail, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    private TimeZone timeZone = null;

    /**
    *** Gets the TimeZone instance for this user 
    *** @param dft The default timezone if no timezone is defined for usr/account
    **/
    public TimeZone getTimeZone(TimeZone dft)
    {
        if (this.timeZone == null) {
            this.timeZone = DateTime.getTimeZone(this.getTimeZone(), null);
            if (this.timeZone == null) {
                Account acct = this.getAccount(); // should never be null
                if (acct != null) {
                    this.timeZone = acct.getTimeZone(dft);
                } else {
                    this.timeZone = (dft != null)? dft : DateTime.getGMTTimeZone();
                }
            }
        }
        return this.timeZone;
    }

    /**
    *** Gets time zone for this user 
    **/
    @JSONBeanGetter(ignore="$blank")
    public String getTimeZone()
    {
        String v = (String)this.getFieldValue(FLD_timeZone);
        if (StringTools.isBlank(v)) {
            Account acct = this.getAccount(); // should never be null
            return (acct != null)? acct.getTimeZone() : User.GetDefaultTimeZone();
        } else {
            return v.trim();
        }
    }

    /**
    *** Sets the timezone for this user
    **/
    public void setTimeZone(String v)
    {
        String tz = StringTools.trim(v);
        if (!StringTools.isBlank(tz)) {
            // -- validate timezone value?
        }
        this.timeZone = null;
        this.setFieldValue(FLD_timeZone, tz);
    }

    /* return current DateTime (relative the User TimeZone) */
    public DateTime getCurrentDateTime()
    {
        return new DateTime(this.getTimeZone(null));
    }

    // ------------------------------------------------------------------------

    /* get the speed-units for this account */
    @JSONBeanGetter(enumClass="org.opengts.db.tables.Account$SpeedUnits")
    public int getSpeedUnits()
    {
        Integer v = (Integer)this.getOptionalFieldValue(FLD_speedUnits);
        int u = (v != null)? v.intValue() : -1;
        if (u >= 0) {
            return u;
        }
        Account a = this.getAccount();
        if (a != null) {
            return a.getSpeedUnits();
        }
        return EnumTools.getDefault(Account.SpeedUnits.class).getIntValue();
    }

    /* set the speed-units */
    public void setSpeedUnits(int v)
    {
        this.setOptionalFieldValue(FLD_speedUnits, EnumTools.getValueOf(Account.SpeedUnits.class,v).getIntValue());
    }

    /* set the speed-units */
    public void setSpeedUnits(Account.SpeedUnits v)
    {
        this.setOptionalFieldValue(FLD_speedUnits, EnumTools.getValueOf(Account.SpeedUnits.class,v).getIntValue());
    }

    /* set the string representation of the speed-units */
    public void setSpeedUnits(String v, Locale locale)
    {
        this.setOptionalFieldValue(FLD_speedUnits, EnumTools.getValueOf(Account.SpeedUnits.class,v,locale).getIntValue());
    }

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
    public String getSpeedString(double speedKPH, String format, Account.SpeedUnits speedUnitsEnum, boolean inclUnits, Locale locale)
    {
        if (speedUnitsEnum == null) { speedUnitsEnum = Account.getSpeedUnits(this); }
        double speed = speedUnitsEnum.convertFromKPH(speedKPH);
        String speedFmt = StringTools.format(speed, format);
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

    // ------------------------------------------------------------------------

    /* get the distance units for this account */
    @JSONBeanGetter(enumClass="org.opengts.db.tables.Account$DistanceUnits")
    public int getDistanceUnits()
    {
        Integer v = (Integer)this.getOptionalFieldValue(FLD_distanceUnits);
        int u = (v != null)? v.intValue() : -1;
        if (u >= 0) {
            return u;
        }
        Account a = this.getAccount();
        if (a != null) {
            return a.getSpeedUnits();
        }
        return EnumTools.getDefault(Account.DistanceUnits.class).getIntValue();
    }

    /* set the distance units */
    public void setDistanceUnits(int v)
    {
        this.setOptionalFieldValue(FLD_distanceUnits, EnumTools.getValueOf(Account.DistanceUnits.class,v).getIntValue());
    }

    /* set the distance units */
    public void setDistanceUnits(Account.DistanceUnits v)
    {
        this.setOptionalFieldValue(FLD_distanceUnits, EnumTools.getValueOf(Account.DistanceUnits.class,v).getIntValue());
    }

    /* set the string representation of the distance units */
    public void setDistanceUnits(String v, Locale locale)
    {
        this.setOptionalFieldValue(FLD_distanceUnits, EnumTools.getValueOf(Account.DistanceUnits.class,v,locale).getIntValue());
    }

    /* return a formatted distance string */
    public String getDistanceString(double distKM, boolean inclUnits, Locale locale)
    {
        Account.DistanceUnits units = Account.getDistanceUnits(this);
        String distUnitsStr = units.toString(locale);
        double dist         = units.convertFromKM(distKM);
        String distStr      = StringTools.format(dist, "0");
        return inclUnits? (distStr + " " + distUnitsStr) : distStr;
    }

    // ------------------------------------------------------------------------

    /* get default login page ID */
    @JSONBeanGetter(ignore="$blank")
    public String getFirstLoginPageID()
    {
        String v = (String)this.getFieldValue(FLD_firstLoginPageID);
        return StringTools.trim(v);
    }

    public void setFirstLoginPageID(String v)
    {
        this.setFieldValue(FLD_firstLoginPageID, StringTools.trim(v));
    }
    
    public boolean hasFirstLoginPageID()
    {
        return !StringTools.isBlank(this.getFirstLoginPageID());
    }

    // ------------------------------------------------------------------------

    /* get preferred device ID */
    @JSONBeanGetter(ignore="$blank")
    public String getPreferredDeviceID()
    {
        String v = (String)this.getFieldValue(FLD_preferredDeviceID);
        return StringTools.trim(v);
    }

    public void setPreferredDeviceID(String v)
    {
        this.setFieldValue(FLD_preferredDeviceID, StringTools.trim(v));
    }
    
    public boolean hasPreferredDeviceID()
    {
        return !StringTools.isBlank(this.getPreferredDeviceID());
    }

    // ------------------------------------------------------------------------

    /* get maximum access level */
    @JSONBeanGetter()
    public int getMaxAccessLevel()
    {
        if (this.isAdminUser()) {
            // admin user is never restricted
            return AccessLevel.ALL.getIntValue();
        } else {
            Integer v = (Integer)this.getFieldValue(FLD_maxAccessLevel);
            if (v != null) {
                int aclLevel = v.intValue();
                if ((aclLevel < 0) || (aclLevel == AccessLevel.NONE.getIntValue())) {
                    // -- default to ALL, if invalid/undefined
                    return AccessLevel.ALL.getIntValue();
                } else
                if (aclLevel > AccessLevel.ALL.getIntValue()) {
                    // -- cannot me more than ALL
                    return AccessLevel.ALL.getIntValue();
                } else {
                    // -- defined maximum access level
                    return aclLevel;
                }
            } else {
                // -- default to ALL, if undefined
                return AccessLevel.ALL.getIntValue();
            }
        }
    }

    public void setMaxAccessLevel(int v)
    {
        int accessLevel = EnumTools.getValueOf(AccessLevel.class,v).getIntValue();
        this.setFieldValue(FLD_maxAccessLevel, accessLevel);
    }

    public void setMaxAccessLevel(String v)
    {
        int accessLevel = EnumTools.getValueOf(AccessLevel.class,v).getIntValue();
        this.setFieldValue(FLD_maxAccessLevel, accessLevel);
    }

    public void setMaxAccessLevel(AccessLevel v)
    {
        int accessLevel = (v != null)? v.getIntValue() : AccessLevel.ALL.getIntValue();
        this.setFieldValue(FLD_maxAccessLevel, accessLevel);
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
        PasswordHandler pwh = Account.getPasswordHandler(this.getAccount()); // non-null
        return pwh.hasPasswordExpired(this.getPasswdChangeTime());
    }

    // ------------------------------------------------------------------------

    /* return time of last password query */
    public long getPasswdQueryTime()
    {
        Long v = (Long)this.getFieldValue(FLD_passwdQueryTime);
        return (v != null)? v.longValue() : 0L;
    }

    public void setPasswdQueryTime(long v)
    {
        this.setFieldValue(FLD_passwdQueryTime, v);
    }

    // ------------------------------------------------------------------------

    /* return the time this user expires */
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
    public boolean isExpired()
    {

        /* not active? (assume expired if not active) */
        if (!this.isActive()) {
            return true;
        }

        /* expired? */
        long expireTime = this.getExpirationTime();
        if ((expireTime > 0L) && (expireTime < DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not expired */
        return false;

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

    /* return the user suspend time */
    public long getSuspendUntilTime()
    {
        Long v = (Long)this.getFieldValue(FLD_suspendUntilTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set the time of this user suspension */
    public void setSuspendUntilTime(long v)
    {
        this.setFieldValue(FLD_suspendUntilTime, v);
    }

    /* return true if this user is suspended [2.6.2-B50] */
    public boolean isSuspended()
    {

        /* account suspended? */
        // -- TODO:

        /* user suspended? */
        long suspendTime = this.getSuspendUntilTime();
        if ((suspendTime > 0L) && (suspendTime >= DateTime.getCurrentTimeSec())) {
            return true;
        }

        /* not suspended */
        return false;

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
        PasswordHandler pwh = Account.getPasswordHandler(this.getAccount()); // not null
        if (!pwh.getFailedLoginSuspendEnabled()) {
            // -- failed login attempt suspend is not enabled
            return false;
        }

        /* number of failed login attempts */
        String accountID   = this.getAccountID();
        String userID      = this.getUserID();
        long   asOfTime    = DateTime.getCurrentTimeSec(); // now
        long   sinceTime   = asOfTime - pwh.getFailedLoginAttemptInterval();
        long   addCount    = addCurrentFailure? 1L : 0L;
        long   failCount   = Audit.getFailedLoginAttempts(accountID, userID, sinceTime) + addCount;
        long   suspendTime = pwh.getFailedLoginAttemptSuspendTime((int)failCount, asOfTime);
        if (suspendTime > 0L) {
            // -- too many failed login attempts, suspend user
            long oldSuspendTime = this.getSuspendUntilTime(); // may be zero
            if (suspendTime > oldSuspendTime) {
                this.setSuspendUntilTime(suspendTime);
                try {
                    this.update(User.FLD_suspendUntilTime);
                } catch (DBException dbe) {
                    Print.logError("Unable to set suspendUntilTime for user ("+accountID+"/"+userID+"): " + dbe);
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

    /* get last user login time */
    @JSONBeanGetter(ignore="$zero")
    public long getLastLoginTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastLoginTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* get last user login time as a String */
    public String getLastLoginTimeString(TimeZone tmz)
    {
        long llTime = this.getLastLoginTime();
        if (llTime > 0L) {
            TimeZone   tz = (tmz != null)? tmz : this.getTimeZone(DateTime.GMT);
            DateTime llDT = new DateTime(llTime, tz);
            String  dtFmt = Account.GetDateTimeFormat(this.getAccount()); // TimeZone not included
            String  llStr = llDT.format(dtFmt, tz);
            return llStr + ", " + llDT.getTimeZoneShortName();
        } else {
            I18N i18n = I18N.getI18N(User.class, Account.GetLocale(this.getAccount()));
            return i18n.getString("User.loginNever", "never");
        }
    }

    /* get last user login time */
    public String getLastLoginTimeString()
    {
        TimeZone tmz = null; // Account.GetTimeZone(this.getAccount(),this.getTimeZone(DateTime.GMT));
        return this.getLastLoginTimeString(tmz);
    }

    /* set last user login time */
    public void setLastLoginTime(long v)
    {
        this.setFieldValue(FLD_lastLoginTime, v);
    }

    // ------------------------------------------------------------------------

    /* get welcome notice time */
    @JSONBeanGetter(ignore="$zero")
    public long getWelcomeTime()
    {
        Long v = (Long)this.getFieldValue(FLD_welcomeTime);
        return (v != null)? v.longValue() : 0L;
    }

    /* set welcome notice time */
    public void setWelcomeTime(long v)
    {
        this.setFieldValue(FLD_welcomeTime, v);
    }

    /* has welcome notece been sent? */
    public boolean hasWelcomeTime()
    {
        return (this.getWelcomeTime() > 0L)? true : false;
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

    @JSONBeanGetter(ignore="$blank")
    public String getOfficeLocation()
    {
        String v = (String)this.getFieldValue(FLD_officeLocation);
        return StringTools.trim(v);
    }

    public void setOfficeLocation(String v)
    {
        this.setFieldValue(FLD_officeLocation, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------
    // Custom getters and setters

    private String companyName;

    @JSONBeanGetter(name="companyName", tags="getUserInfo")
    public String getCompanyName()
    {
        return this.companyName;
    }
    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    // ------------------------------------------------------------------------

    private String imgSrc;

    @JSONBeanGetter(name="imgSrc", tags="getUserInfo")
    public String getImageSource()
    {
        return this.imgSrc;
    }
    public void setImageSource(String imgSrc)
    {
        this.imgSrc = imgSrc;
    }

    // ------------------------------------------------------------------------

    private RTProperties customAttrRTP = null;
    private Collection<String> customAttrKeys = null;

    /* get the custom attributes as a String */
    public String getCustomAttributes()
    {
        String v = (String)this.getOptionalFieldValue(FLD_customAttributes);
        return StringTools.trim(v);
    }

    /* set the custom attributes as a String */
    public void setCustomAttributes(String v)
    {
        this.setOptionalFieldValue(FLD_customAttributes, StringTools.trim(v));
        this.customAttrRTP  = null;
        this.customAttrKeys = null;
    }

    /* get custom attributes a an RTProperties */
    public RTProperties getCustomAttributesRTP()
    {
        if (this.customAttrRTP == null) {
            this.customAttrRTP = new RTProperties(this.getCustomAttributes());
        }
        return this.customAttrRTP;
    }

    /* get the custom attributes keys */
    public Collection<String> getCustomAttributeKeys()
    {
        if (this.customAttrKeys == null) {
            this.customAttrKeys = this.getCustomAttributesRTP().getPropertyKeys(null);
        }
        return this.customAttrKeys;
    }

    /* get the custom attributes as a String */
    public String getCustomAttribute(String key)
    {
        return this.getCustomAttributesRTP().getString(key,null);
    }

    /* get the custom attributes as a String */
    public String setCustomAttribute(String key, String value)
    {
        return this.getCustomAttributesRTP().getString(key,value);
    }

    // ------------------------------------------------------------------------

    /** (NOT CURRENTLY USED)
    *** Returns true if this user is a dispatcher
    *** @return True if this user is a dispatcher
    **/
    public boolean getIsDispatcher()
    {
        // -- check account
        Account account = this.getAccount();
        if (account == null) {
            // -- unlikely to occur
            return false;
        } else
        if (!account.getIsDispatcher()) {
            // -- Account must be a dispatcher
            return false;
        }
        // -- "admin" is always a dispatcher
        if (this.isAdminUser()) {
            return true;
        }
        // -- check assigned isDispatcher state
        Boolean v = (Boolean)this.getOptionalFieldValue(FLD_isDispatcher);
        return (v != null)? v.booleanValue() : false;
    }

    /** (NOT CURRENTLY USED)
    *** Sets the "Dispatcher" state for this User
    *** @param v The "Dispatcher" state for this User
    **/
    public void setIsDispatcher(boolean v)
    {
        // -- check account
        if (v == true) {
            // -- set "true" to "false" if account is not a dispatcher
            Account account = this.getAccount();
            if (account == null) {
                // -- unlikely to occur
                v = false;
            } else
            if (!account.getIsDispatcher()) {
                // -- Account must be a dispatcher
                v = false;
            }
        } else {
            // -- set "false" to "true" if user is "admin"
            if (this.isAdminUser()) {
                v = true;
            }
        }
        // -- set value
        this.setOptionalFieldValue(FLD_isDispatcher, v);
    }

    /** (NOT CURRENTLY USED)
    *** Returns true if the specified AccountID/UserID is a dispatcher
    **/
    public static boolean IsDispatcher(String accountID, String userID)
    {
        // -- check account
        Account account = null;
        try {
            account = Account.getAccount(accountID);
            if (account == null) {
                return false;
            } else
            if (!account.getIsDispatcher()) {
                return false;
            }
        } catch (DBException dbe) {
            Print.logException("Reading Account", dbe);
            return false;
        }
        // -- "admin" user?
        if (StringTools.isBlank(userID) || User.isAdminUser(userID)) {
            return true;
        }
        // -- check user
        User user = null;
        try {
            user = User.getUser(account, userID);
            if (user == null) {
                return false; // user not found
            } else
            if (user.isAdminUser()) {
                return true;
            } else
            if (user.getIsDispatcher()) {
                return true;
            } else {
                return false;
            }
        } catch (DBException dbe) {
            Print.logException("Reading User", dbe);
            return false;
        }
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {
        Account acct = this.getAccount();
        this.setIsActive(true);
        if (this.isAdminUser()) {
            this.setDescription("Administrator");
            if (acct != null) {
                this.setEncodedPassword(acct.getEncodedPassword());
                this.setTimeZone(acct.getTimeZone());
            }
        } else {
            this.setDescription("New User");
            if (acct != null) {
                this.setTimeZone(acct.getTimeZone());
            }
        }
        super.setRuntimeDefaultValues();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the Account-ID
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter()
    //public String getAccountID()  <-- is "final" in AccountRecord
    //{
    //    return super.getAccountID();
    //}

    /**
    *** Gets the Account Description
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(name="accountName,userDescription")
    public String getAccountDescription()
    {
        return super.getAccountDescription();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the User-ID
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter()
    //public String getUserID()  <-- is "final" in UserRecord
    //{
    //    return super.getUserID();
    //}

    /**
    *** Gets the UserID of the specified User
    **/
    public static String getUserID(User user)
    {
        if (user != null) {
            return user.getUserID();
        } else {
            return User.getAdminUserID();
        }
    }

    /**
    *** Returns true if this user is the admin user 
    *** [UserInformation interface]
    **/
    @JSONBeanGetter()
    public boolean isAdminUser()
    {
        return User.isAdminUser(this.getUserID());
    }

    // --------------------------------

    /**
    *** Gets the User Description
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(name="userName")
    public String getUserDescription()
    {
        return super.getUserDescription();
    }

    /**
    *** Gets the User Description
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter()
    public String getDescription()
    {
        return super.getDescription();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Temperature Units (from the Account record)
    *** [UserInformation interface]
    **/
    @JSONBeanGetter(enumClass="org.opengts.db.tables.Account$TemperatureUnits")
    public int getTemperatureUnits()
    {
        Account a = this.getAccount();
        return (a != null)? a.getTemperatureUnits() : EnumTools.getDefault(Account.TemperatureUnits.class).getIntValue();
    }

    /**
    *** Gets the Altitude units, based on the Distance units
    **/
    @JSONBeanGetter(enumClass="org.opengts.db.tables.Account$AltitudeUnits")
    public int getAltitudeUnits()
    {
        return Account.getAltitudeUnits(this).getIntValue();
    }

    /**
    *** Gets the Lat/Lon format 
    **/
    @JSONBeanGetter(enumClass="org.opengts.db.tables.Account$LatLonFormat")
    public int getLatLonFormat()
    {
        Account a = this.getAccount();
        return (a != null)? a.getLatLonFormat() : EnumTools.getDefault(Account.LatLonFormat.class).getIntValue();
    }

    // ------------------------------------------------------------------------

    /* return default device authorization */
    public boolean getDefaultDeviceAuthorization()
    {
        if (this.isAdminUser()) {
            // -- authorized for "ALL" devices
            return true;
        } else {
            // -- check for "ALL" device authorization
            return DBConfig.GetDefaultDeviceAuthorization(this.getAccountID());
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return the DBSelect statement for the specified account/user */
    protected static DBSelect<? extends DBRecord<?>> _getGroupListSelect(String acctId, String userId, long limit)
    {

        /* empty/null account */
        if (StringTools.isBlank(acctId)) {
            return null;
        }

        /* empty/null user */
        if (StringTools.isBlank(userId)) {
            return null;
        }
        
        /* get select */
        // DBSelect: SELECT * FROM GroupList WHERE ((accountID='acct') and (userID='user')) ORDER BY sequence;
        DBSelect<GroupList> dsel = new DBSelect<GroupList>(GroupList.getFactory());
        dsel.setSelectedFields(GroupList.FLD_groupID);
        DBWhere dwh = dsel.createDBWhere();
        dsel.setWhere(
            dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(GroupList.FLD_accountID,acctId),
                    dwh.EQ(GroupList.FLD_userID   ,userId)
                )
            )
        );
        if (GroupList.getFactory().hasExistingColumn(GroupList.FLD_sequence)) {
            dsel.setOrderByFields(GroupList.FLD_sequence);
        } else {
            dsel.setOrderByFields(GroupList.FLD_groupID);
        }
        dsel.setLimit(limit);
        return dsel;

    }

    /**
    *** Gets a list of all explicitly authorized GroupIDs within the GroupList table
    **/
    public static java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode, String acctID, String userID, long limit)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* valid account/groupId? */
        if (StringTools.isBlank(acctID)) {
            return null;
        } else
        if (StringTools.isBlank(userID)) {
            return null;
        }

        /* get db selector */
        DBSelect<? extends DBRecord<?>> dsel = User._getGroupListSelect(acctID, userID, limit);
        if (dsel == null) {
            return null;
        }

        /* read devices for account */
        OrderedSet<String> grpList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode); // getDBConnection_read
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String grpId = rs.getString(GroupList.FLD_groupID);
                grpList.add(grpId);
            }
        } catch (SQLException sqe) {
            throw new DBException("Getting User DeviceGroup List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return grpList;

    }
    // --
    @Deprecated
    public static java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs(String acctID, String userID, long limit)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getExplicitlyAuthorizedDeviceGroupIDs(rwMode, acctID, userID, limit);
    }

    // --------------------------------

    /* refresh cached device group list */
    private java.util.List<String> deviceGroupList = null;
    public void clearCachedDeviceGroupIDs()
    {
        this.deviceGroupList = null;
    }

    /**
    *** Returns a list of explicitly authorized device group IDs assigned to this user 
    **/
    public java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        if (this.deviceGroupList == null) {
            this.deviceGroupList = User.getExplicitlyAuthorizedDeviceGroupIDs(rwMode, this.getAccountID(), this.getUserID(), -1L);
        }
        return this.deviceGroupList;
    }
    // --
    @Deprecated
    public java.util.List<String> getExplicitlyAuthorizedDeviceGroupIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getExplicitlyAuthorizedDeviceGroupIDs(rwMode);
    }

    // --------------------------------

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    public static java.util.List<String> getAllAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode, String accountID, User user)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        java.util.List<String> groupIDs = null; // DeviceGroup ids

        /* Account must be specified */
        if (StringTools.isBlank(accountID)) {
            if (user != null) {
                // -- set accountID
                accountID = user.getAccountID();
            } else {
                // -- accountID not specified
                return null;
            }
        } else
        if ((user != null) && !user.getAccountID().equals(accountID)) {
            // -- accountID does not match user accountID
            return null;
        }

        /* get DeviceGroups for user */
        if (!User.isAdminUser(user)) { // user can only be null if userID is "admin"
            // -- (user is not null) get User authorized groups
            try {
                java.util.List<String> grpList = user.getExplicitlyAuthorizedDeviceGroupIDs(rwMode); // throws DBException
                if (!ListTools.isEmpty(grpList)) {
                    // -- explicit list of DeviceGroups have been assigned
                    groupIDs = grpList; // new OrderedSet<String>(grpList);
                } else {
                    // -- no explicit DeviceGroups assigned, assume 'ALL'
                    groupIDs = DeviceGroup.getDeviceGroupsForAccount(rwMode,accountID,false/*includeAll*/);
                }
            } catch (DBException dbe) {
                // -- empty on error
                //groupIDs = new OrderedSet<String>();
                throw dbe;
            }
        } else {
            // -- no user (assume "admin") get all groups for current account
            try {
                groupIDs = DeviceGroup.getDeviceGroupsForAccount(rwMode,accountID,false/*includeAll*/);
            } catch (DBException dbe) {
                // -- empty on error
                //groupIDs = new OrderedSet<String>();
                throw dbe;
            }
        }
        // -- 'groups' is non-null here

        /* include ALL */
        java.util.List<String> allGroupIDs = new OrderedSet<String>(groupIDs); // copy
        allGroupIDs.add(0, DeviceGroup.DEVICE_GROUP_ALL);

        /* return groups */
        return allGroupIDs;

    }
    // -- retained here for legacy purposes (used only by 'ctrac')
    @Deprecated
    public static java.util.List<String> getAllAuthorizedDeviceGroupIDs(String accountID, User user)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getAllAuthorizedDeviceGroupIDs(rwMode, accountID, user);
    }

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    //@JSONBeanGetter(name="deviceGroupIDs")
    public java.util.List<String> getAllAuthorizedDeviceGroupIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        return User.getAllAuthorizedDeviceGroupIDs(rwMode, this.getAccountID(), this);
    }
    // --
    @Deprecated
    public java.util.List<String> getAllAuthorizedDeviceGroupIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAllAuthorizedDeviceGroupIDs(rwMode);
    }

    // --------------------------------

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    public static java.util.List<DeviceGroupProvider> getAllAuthorizedDeviceGroups(DBReadWriteMode rwMode, Account account, User user)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* validate Account/User */
        final String accountID = (account != null)? account.getAccountID() : null;
        if (StringTools.isBlank(accountID)) {
            return null;
        } else
        if ((user != null) && !accountID.equals(user.getAccountID())) {
            return null;
        }

        /* get all authorized DeviceGroup IDs */
        Collection<String> groupIDs = User.getAllAuthorizedDeviceGroupIDs(rwMode, accountID, user);
        if (groupIDs == null) {
            return null;
        }

        /* convert to DeviceGroup.DeviceGroupName */
        // -- The returned values are designed to work with the JsonBean feature of the JSON module.
        OrderedSet<DeviceGroupProvider> groups = new OrderedSet<DeviceGroupProvider>();
        for (String grpID : groupIDs) {
            if (grpID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                // -- the 'ALL' DeviceGroup does not actual exists, so create a temporary one
                final String grpDesc = DeviceGroup.GetDeviceGroupAllTitle(account,null/*L10N*/);
                DeviceGroupProvider dgp = new DeviceGroupProvider() {
                    public DeviceGroup getDeviceGroup() { return null; }
                    public String      getAccountID()   { return accountID; }
                    @JSONBeanGetter(name="groupID")
                    public String      getGroupID()     { return DeviceGroup.DEVICE_GROUP_ALL; }
                    @JSONBeanGetter(name="groupName")
                    public String      getDescription() { return grpDesc; }
                };
                groups.add(dgp);
            } else {
                try {
                    final DeviceGroup grp = DeviceGroup.getDeviceGroup(account, grpID, false);
                    if (grp != null) { // unlikely to be null
                        DeviceGroupProvider dgp = new DeviceGroupProvider() {
                            public DeviceGroup getDeviceGroup() { return grp; }
                            public String      getAccountID()   { return grp.getAccountID(); }
                            @JSONBeanGetter(name="groupID")
                            public String      getGroupID()     { return grp.getGroupID(); }
                            @JSONBeanGetter(name="groupName")
                            public String      getDescription() { return grp.getGroupDescription(); }
                        };
                        groups.add(dgp);
                    }
                } catch (DBException dbe) {
                    // -- skip this group
                }
            }
        }
        return groups;

    }

    /**
    *** Returns a list of all authorized DeviceGroups for the specified User
    *** or all DeviceGroups if user is null.
    **/
    @JSONBeanGetter(name="deviceGroups")
    public java.util.List<DeviceGroupProvider> getAllAuthorizedDeviceGroups(DBReadWriteMode rwMode)
        throws DBException
    {
        return User.getAllAuthorizedDeviceGroups(rwMode, this.getAccount(), this);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the authorized device groups for this user
    **/
    public boolean setDeviceGroups(String groupList[])
    {
        // see "BasicPrivateLabel.PROP_UserInfo_authorizedGroupCount"
        return this._setDeviceGroups(ListTools.toIterator(groupList));
    }

    /**
    *** Sets the authorized device groups for this user
    **/
    public boolean setDeviceGroups(java.util.List<String> groupList)
    {
        return this._setDeviceGroups(ListTools.toIterator(groupList));
    }

    /**
    *** Sets the authorized device groups for this user
    **/
    protected boolean _setDeviceGroups(Iterator<String> groupListIter)
    {
        String accountID = this.getAccountID();
        String userID    = this.getUserID();
        this.clearCachedDeviceGroupIDs();

        /* delete all existing DeviceGroup entries from the GroupList table for this User */
        // -- [DELETE FROM GroupList WHERE accountID='account' AND userID='user']
        try {
            DBRecordKey<GroupList> grpListKey = new GroupList.Key();
            grpListKey.setFieldValue(GroupList.FLD_accountID, accountID);
            grpListKey.setFieldValue(GroupList.FLD_userID   , userID);
            DBDelete ddel = new DBDelete(GroupList.getFactory());
            ddel.setWhere(grpListKey.getWhereClause(DBWhere.KEY_PARTIAL_FIRST));
            DBConnection dbc = null;
            try {
                dbc = DBConnection.getDBConnection(DBReadWriteMode.DELETE);
                dbc.executeUpdate(ddel.toString());
            } finally {
                DBConnection.release(dbc);
            }
        } catch (Throwable th) { // DBException, SQLException
            Print.logException("Error deleting existing DeviceGroup entries from the User GroupList table", th);
            return false;
        }

        /* add new entries */
        if (groupListIter != null) {

            /* check groups other than ALL or blank */
            boolean all = false;
            int grpCount = 0;
            OrderedSet<String> addGroups = new OrderedSet<String>();
            for (;groupListIter.hasNext();) {
                String groupID = groupListIter.next();
                if (DeviceGroup.DEVICE_GROUP_ALL.equalsIgnoreCase(groupID)) {
                    all = true;
                    addGroups.clear();
                    break;
                } else
                if (DeviceGroup.DEVICE_GROUP_NONE.equalsIgnoreCase(groupID)) {
                    // -- skip this reserved group id
                } else
                if (StringTools.isBlank(groupID)) {
                    // -- skip blank group ids
                } else {
                    try {
                        if (DeviceGroup.exists(accountID,groupID)) {
                            grpCount++;
                            addGroups.add(groupID);
                        } else {
                            Print.logError("DeviceGroup does not exist: %s/%s", accountID, groupID);
                        }
                    } catch (DBException dbe) {
                        Print.logException("Error creating new DeviceGroup entries in the User GroupList table", dbe);
                        return false;
                    }
                }
            }

            /* add groupIDs in list */
            if (all) {
                if (!this.getDefaultDeviceAuthorization()) {
                    // -- if the default device authorization is false, we do explicitly specify that 
                    // -  this user has authority to view "ALL" devices, otherwise he will not athority 
                    // -  to view any device.
                    try {
                        GroupList groupListItem = GroupList.getGroupList(this, DeviceGroup.DEVICE_GROUP_ALL, true);
                        groupListItem.setSequence(0); // [2.6.2-B71]
                        groupListItem.save(); // insert();
                    } catch (DBException dbe) {
                        Print.logException("Error creating new DeviceGroup entries in the User GroupList table", dbe);
                        return false;
                    }
                }
            } else
            if (!ListTools.isEmpty(addGroups)) {
                try {
                    int grpLen = addGroups.size();
                    for (int g = 0; g < grpLen; g++) {
                        String groupID = addGroups.get(g);
                        GroupList groupListItem = GroupList.getGroupList(this, groupID, true/*create*/); 
                        groupListItem.setSequence(g); // [2.6.2-B71]
                        groupListItem.save(); // insert();
                    }
                } catch (DBException dbe) {
                    Print.logException("Error creating new DeviceGroup entries in the User GroupList table", dbe);
                    return false;
                }
            }

        }
        
        /* success */
        return true;

    }

    /* should not be used (does not set sequence/index)
    public void addDeviceGroup(String groupID)
        throws DBException
    {
        if (!StringTools.isBlank(groupID)) {
            String accountID = this.getAccountID();
            if (groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL) || DeviceGroup.exists(accountID,groupID)) {
                this.clearCachedDeviceGroupIDs();
                if (!GroupList.exists(accountID,this.getUserID(),groupID)) {
                    GroupList groupListItem = GroupList.getGroupList(this, groupID, true);
                  //groupListItem.setSequence(???); // [2.6.2-B71]
                    groupListItem.save();
                } else {
                    // -- already exists (quietly ignore)
                }
            } else {
                Print.logError("DeviceGroup does not exist: %s/%s", accountID, groupID);
            }
        }
    }
    */

    /* should not be used
    public void removeDeviceGroup(String groupID)
        throws DBException
    {
        if (!StringTools.isBlank(groupID)) {
            this.clearCachedDeviceGroupIDs();
            GroupList.Key grpListKey = new GroupList.Key(this.getAccountID(), this.getUserID(), groupID);
            grpListKey.delete(true);
        }
    }
    */

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* add all user authorized devices to the internal device map */
    public static OrderedSet<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode, Account account, User user, boolean inclInactv)
        throws DBException
    {
        String accountID = (account != null)? account.getAccountID() : null;
        return User.getAuthorizedDeviceIDs(rwMode, accountID, user, inclInactv);
    }
    // --
    @Deprecated
    public static OrderedSet<String> getAuthorizedDeviceIDs(Account account, User user, boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getAuthorizedDeviceIDs(rwMode, account, user, inclInactv);
    }

    /* add all user authorized devices to the internal device map */
    public static OrderedSet<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode, String accountID, User user, boolean inclInactv)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        if (user != null) {
            return user.getAuthorizedDeviceIDs(rwMode, inclInactv);
        } else
        if (accountID != null) {
            return Device.getDeviceIDsForAccount(rwMode, accountID, null, inclInactv);
        } else {
            return new OrderedSet<String>();
        }
    }
    // --
    @Deprecated
    public static OrderedSet<String> getAuthorizedDeviceIDs(String accountID, User user, boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getAuthorizedDeviceIDs(rwMode, accountID, user, inclInactv);
    }

    /* get all authorized devices for this user */
    protected OrderedSet<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode, boolean inclInactv)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        this.clearCachedDeviceGroupIDs();
        Collection<String> groupList = this.getExplicitlyAuthorizedDeviceGroupIDs(rwMode);
        if (!ListTools.isEmpty(groupList)) {
            // -- The user is authorized to all Devices in the listed groups (thus "User" can be null)
            OrderedSet<String> devList = new OrderedSet<String>(); // DeviceIDs
            for (String groupID : groupList) {
                OrderedSet<String> d = DeviceGroup.getDeviceIDsForGroup(rwMode, this.getAccountID(), groupID, null/*User*/, inclInactv);
                ListTools.toList((java.util.List<String>)d, devList);
            }
            return devList;
        } else {
            // -- no explicit defined groups, get all authorized devices
            if (this.getDefaultDeviceAuthorization()) {
                // -- all devices are authorized
                return Device.getDeviceIDsForAccount(rwMode, this.getAccountID(), null, inclInactv, -1L);
            } else {
                // -- no devices are authorized
                return new OrderedSet<String>();
            }
        }
    }
    // --
    @Deprecated
    protected OrderedSet<String> getAuthorizedDeviceIDs(boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAuthorizedDeviceIDs(rwMode, inclInactv);
    }

    /**
    *** Gets all active/authorized devices for the "admin" user
    *** [UserInformation interface]
    **/
    //@JSONBeanGetter(name="deviceIDs")
    public java.util.List<String> getAuthorizedDeviceIDs(DBReadWriteMode rwMode)
        throws DBException
    {
        boolean inclInactv = false;
        return this.getAuthorizedDeviceIDs(rwMode, inclInactv);
    }
    // --
    @Deprecated
    public java.util.List<String> getAuthorizedDeviceIDs()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAuthorizedDeviceIDs(rwMode);
    }

    // --------------------------------

    /* return ture if specified device is authorized for this User */
    public boolean isAuthorizedDevice(DBReadWriteMode rwMode, String deviceID)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* always allow "admin" user */
        if (this.isAdminUser()) {
            return true;
        }

        /* deviceID blank? */
        if (StringTools.isBlank(deviceID)) {
            return false; // blank deviceID not authorized 
        }

        /* preferred deviceID is authorized? [2.6.1-B44] */
        // -- User.authorizedPreferredDeviceID=false|true|only
        PreferredDeviceAuth prefDevAuth = User.GetPreferredDeviceAuth();
        if (!prefDevAuth.equals(PreferredDeviceAuth.FALSE)) {
            // -- check preferred device match
            String prefDevID = this.getPreferredDeviceID();
            if (!StringTools.isBlank(prefDevID) && deviceID.equalsIgnoreCase(prefDevID)) {
                return true; // authorized
            }
            // -- return false if only the preferred device can be authorized [2.6.3-B30]
            if (prefDevAuth.equals(PreferredDeviceAuth.ONLY)) {
                return false; // not authorized
            }
        }

        /* check deviceID is in an authorized group */
        Collection<String> groupList = this.getExplicitlyAuthorizedDeviceGroupIDs(rwMode);
        if (ListTools.isEmpty(groupList)) {
            // -- db.defaultDeviceAuthorization.ACCOUNT=true
            // -- db.defaultDeviceAuthorization=true
            return this.getDefaultDeviceAuthorization();
        } else {
            for (String groupID : groupList) {
                // -- authorized if the device exists in the DeviceGroup (DeviceList)
                if (groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                    // -- always authorized for group 'all'
                    return true;
                } else
                if (DeviceGroup.exists(this.getAccountID(), groupID, deviceID)) {
                    return true;
                }
            }
            // -- does not exist in any authorized group
          //Print.logInfo("Not authorized device for user '%s': %s", this.getUserID(), deviceID);
            Print.logInfo("User not authorized to device '%s/%s': %s", this.getAccountID(), this.getUserID(), deviceID);
            return false;
        }

    }
    // --
    @Deprecated
    public boolean isAuthorizedDevice(String deviceID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.isAuthorizedDevice(rwMode, deviceID);
    }

    // ------------------------------------------------------------------------

    /* get the preferred/first authorized device for this user */
    public String getDefaultDeviceID(DBReadWriteMode rwMode, boolean inclInactv)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* first check preferred device */
        if (this.hasPreferredDeviceID()) {
            String devID = this.getPreferredDeviceID();
            try {
                if (Device.exists(this.getAccountID(),devID) && this.isAuthorizedDevice(rwMode,devID)) {
                    return devID;
                }
            } catch (DBException dbe) {
                // -- 'Device.exists' error, ignore
            }
            // -- device does not exist, or not authorized for preferred device
        }

        /* check for first authorized device */
        java.util.List<String> groupList = User.getExplicitlyAuthorizedDeviceGroupIDs(rwMode, this.getAccountID(), this.getUserID(), 1L);
        if (ListTools.isEmpty(groupList)) {
            // -- no defined groups
            if (this.getDefaultDeviceAuthorization()) {
                // -- all devices are authorized, return first device
                OrderedSet<String> d = Device.getDeviceIDsForAccount(rwMode, this.getAccountID(), null, inclInactv, 1);
                return !ListTools.isEmpty(d)? d.get(0) : null;
            } else {
                // -- no devices are authorized
                return null;
            }
        } else {
            String groupID = groupList.get(0);
            OrderedSet<String> d = DeviceGroup.getDeviceIDsForGroup(rwMode, this.getAccountID(), groupID, null, inclInactv, 1L);
            return !ListTools.isEmpty(d)? d.get(0) : null;
        }

    }
    // --
    @Deprecated
    public String getDefaultDeviceID(boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getDefaultDeviceID(rwMode, inclInactv);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a list of authorized devices, with names
    **/
    public static java.util.List<DeviceProvider> getAuthorizedDevices(DBReadWriteMode rwMode, Account account, User user, boolean inclInactv)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        OrderedSet<DeviceProvider> devList = new OrderedSet<DeviceProvider>();

        /* get Account */
        if (user != null) {
            if (account == null) {
                account = user.getAccount();
            } else 
            if (!user.getAccountID().equals(account.getAccountID())) {
                return devList;
            }
        }
        // --
        if (account == null) {
            return devList;
        }
        String accountID = account.getAccountID();

        /* device DeviceIDs */
        java.util.List<String> deviceIDs = User.getAuthorizedDeviceIDs(rwMode, accountID, user, inclInactv);
        if (ListTools.isEmpty(deviceIDs)) {
            return devList;
        }

        /* get DeviceProvider list */
        for (String devID : deviceIDs) {
            final Device device = Device._getDevice(account, devID);
            if (device == null) {
                // -- (unlikely) devID does not exist
                Print.logWarn("DeviceID not found: " + devID);
            } else
            if (!inclInactv && !device.isActive()) {
                // -- device is not active
                Print.logWarn("DeviceID is not active: " + devID);
            } else
            if (!inclInactv && device.getLastGPSTimestamp() <= 0L) {
                // -- never received a GPS event (also consider inactive)
                Print.logWarn("DeviceID has not yet received a valid GPS event: " + devID);
            } else {
                DeviceProvider dgp = new DeviceProvider() { // DeviceGroupProvider
                    public Device getDevice()      { return null; } // return null, for now
                    public String getAccountID()   { return device.getAccountID(); }
                    @JSONBeanGetter(name="deviceID")
                    public String getDeviceID()    { return device.getDeviceID(); }
                    @JSONBeanGetter(name="uniqueID")
                    public String getUniqueID()    { return device.getUniqueID(); }
                    @JSONBeanGetter(name="deviceName")
                    public String getDescription() { return device.getDeviceDescription(); }
                    @JSONBeanGetter(name="shortName")
                    public String getShortName()   { return device.getShortName(); }
                };
                devList.add(dgp);
            }
        }
        return devList;

    }
    // --
    @Deprecated
    public static java.util.List<DeviceProvider> getAuthorizedDevices(Account account, User user, boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getAuthorizedDevices(rwMode, account, user, inclInactv);
    }

    /**
    *** Gets a list of authorized devices, with names
    **/
    public java.util.List<DeviceProvider> getAuthorizedDevices(DBReadWriteMode rwMode, boolean inclInactv)
        throws DBException
    {
        Account account = this.getAccount();
        User    user    = this;
        return User.getAuthorizedDevices(rwMode, account, user, inclInactv);
    }
    // --
    @Deprecated
    public java.util.List<DeviceProvider> getAuthorizedDevices(boolean inclInactv)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAuthorizedDevices(rwMode, inclInactv);
    }

    /**
    *** Gets a list of authorized devices, with names
    **/
    //@JSONBeanGetter(name="devices")
    public java.util.List<DeviceProvider> getAuthorizedDevices(DBReadWriteMode rwMode)
        throws DBException
    {
        Account account    = this.getAccount();
        User    user       = this;
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
    *** Gets a JSON object containing all AclEntry's for this User
    **/
    public static JSON._Object getAclEntriesJSON(UserInformation userInfo, BasicPrivateLabel bpl)
    {
        // -- JSON._Object aclEnt = User.getAclEntriesJSON((user!=null)?user:account, privateLbl);
        Locale  locale      = (bpl != null)? bpl.getLocale() : null;
        String  aclLevels[] = EnumTools.getValueNames(AccessLevel.class, locale);
        User    user        = (userInfo instanceof User)? (User)userInfo : null;
        boolean isAdmin     = User.isAdminUser(user);
        Role    userRole    = (user != null)? user.getRole() : null;
        JSON._Object aclObj = new JSON._Object();

        /* Account/User */
        if (userInfo != null) {
            aclObj.addKeyValue(User.FLD_accountID, userInfo.getAccountID());
            aclObj.addKeyValue(User.FLD_userID   , userInfo.getUserID());
        }

        /* AccessLevel JSON object */
        JSON._Object aclLevel = new JSON._Object();
        Enum<AccessLevel> ale[] = AccessLevel.class.getEnumConstants();
        if (ale != null) {
            for (int i = 0; i < ale.length; i++) {
                String name = ((AccessLevel)ale[i]).name();
                int    ival = ((AccessLevel)ale[i]).getIntValue();
                String desc = ((AccessLevel)ale[i]).toString(locale);
                JSON._Object alObj = new JSON._Object().setFormatIndent(false);
                alObj.addKeyValue("intValue"   , ival);
                alObj.addKeyValue("description", desc);
                aclLevel.addKeyValue(name, alObj);
            }
        }
        aclObj.addKeyValue("accessLevels", aclLevel);

        /* AclEntries for user */
        boolean inclHidden = false;
        JSON._Array aclArry = new JSON._Array();
        AclEntry aclEntries[] = bpl.getAllAclEntries();
        for (int a = 0; a < aclEntries.length; a++) {
            AclEntry acl = aclEntries[a];
            if (!inclHidden && acl.isHidden()) {
                //Print.logInfo("ACL is hidden: " + acl);
                continue;
            }
            // -- add ACL to array
            String      aclName  = acl.getName();
            int         valAcc[] = acl.getAccessLevelIntValues();  // not null
            AccessLevel maxAcc   = acl.getMaximumAccessLevel(); // not null
            AccessLevel dftAcc   = User.isAdminUser(user)? maxAcc : bpl.getAccessLevel(userRole,aclName); // not null
            AccessLevel usrAcc   = !User.isAdminUser(user)? UserAcl.getAccessLevel(user,aclName,dftAcc) : maxAcc;
            JSON._Object acleObj = new JSON._Object().setFormatIndent(false);
            //JSON.toJsonBean(acl, null, aclObj);
            acleObj.addKeyValue("name"    , aclName);
            if (inclHidden /* && acl.isHidden()*/) {
            acleObj.addKeyValue("isHidden", acl.isHidden());
            }
            acleObj.addKeyValue("values"  , (new JSON._Array(valAcc)).setFormatIndent(false));
            acleObj.addKeyValue("maximum" , maxAcc.getIntValue());
            acleObj.addKeyValue("default" , dftAcc.getIntValue());
            acleObj.addKeyValue("selected", usrAcc.getIntValue());
            aclArry.addValue(acleObj);
        }
        aclObj.addKeyValue("userAcls", aclArry);

        return aclObj;

    }

    /**
    *** Gets an array of all currently defined ACL IDs for this User
    *** (does not return null)
    **/
    public Collection<UserAcl> getUserAcls(DBReadWriteMode rwMode)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        String acctID = this.getAccountID();

        /* read ACLs for user */
        java.util.List<UserAcl> aclList = new Vector<UserAcl>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM UserAcl WHERE (accountID='acct') AND (userID='user') ORDER BY aclID
            DBSelect<UserAcl> dsel = new DBSelect<UserAcl>(UserAcl.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(UserAcl.FLD_accountID,this.getAccountID()),
                    dwh.EQ(UserAcl.FLD_userID,this.getUserID())
                )
            ));
            dsel.setOrderByFields(UserAcl.FLD_aclID);
    
            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String aID = rs.getString(UserAcl.FLD_accountID);
                String uID = rs.getString(UserAcl.FLD_userID);
                String acl = rs.getString(UserAcl.FLD_aclID);
                UserAcl userAcl = new UserAcl(new UserAcl.Key(aID,uID,acl));
                userAcl.setAllFieldValues(rs);
                aclList.add(userAcl);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting User ACL List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return aclList;

    }
    // --
    @Deprecated
    public Collection<UserAcl> getUserAcls()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getUserAcls(rwMode);
    }

    /**
    *** Gets an array of all currently defined ACL IDs for this User
    *** (does not return null)
    **/
    public String[] getAclsForUser(DBReadWriteMode rwMode)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        String acctID = this.getAccountID();

        /* read ACLs for user */
        java.util.List<String> aclList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT aclID FROM UserAcl WHERE (accountID='acct') AND (userID='user') ORDER BY aclID
            DBSelect<UserAcl> dsel = new DBSelect<UserAcl>(UserAcl.getFactory());
            dsel.setSelectedFields(UserAcl.FLD_aclID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE_(
                dwh.AND(
                    dwh.EQ(UserAcl.FLD_accountID,this.getAccountID()),
                    dwh.EQ(UserAcl.FLD_userID,this.getUserID())
                )
            ));
            dsel.setOrderByFields(UserAcl.FLD_aclID);
    
            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String aclId = rs.getString(UserAcl.FLD_aclID);
                aclList.add(aclId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting User ACL List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return aclList.toArray(new String[aclList.size()]);

    }
    // --
    @Deprecated
    public String[] getAclsForUser()
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return this.getAclsForUser(rwMode);
    }

    // ------------------------------------------------------------------------

    /* to String value */
    public String toString()
    {
        return this.getAccountID() + "/" + this.getUserID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return true if the specified user exists */
    public static boolean exists(String acctID, String userID)
        throws DBException // if error occurs while testing existance
    {
        if ((acctID != null) && (userID != null)) {
            User.Key userKey = new User.Key(acctID, userID);
            return userKey.exists(DBReadWriteMode.READ_WRITE);
        }
        return false;
    }
    
    // ------------------------------------------------------------------------

    /* Return specified user (may return null) */
    public static User getUser(Account account, String userID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        if (account == null) {
            throw new DBException("Account is null");
        } else
        if (userID == null) {
            throw new DBException("UserID is null");
        } else {
            String acctID = account.getAccountID();
            User.Key userKey = new User.Key(acctID, userID);
            if (userKey.exists(rwMode)) {
                User user = userKey._getDBRecord(true, rwMode);
                user.setAccount(account);
                return user;
            } else {
                return null;
            }
        }
    }

    /* Return specified user, create if specified (does not return null) */
    public static User getUser(Account account, String userId, boolean create)
        throws DBException
    {
        
        /* account-id specified? */
        if (account == null) {
            throw new DBNotFoundException("Account not specified.");
        }
        String acctId = account.getAccountID();

        /* user-id specified? */
        if ((userId == null) || userId.equals("")) {
            throw new DBNotFoundException("User-ID not specified.");
        }

        /* get/create user */
        User user = null;
        User.Key userKey = new User.Key(acctId, userId);
        if (!userKey.exists(DBReadWriteMode.READ_WRITE)) { // may throw DBException
            if (create) {
                user = userKey._getDBRecord();
                user.setAccount(account);
                user.setCreationDefaultValues();
                return user; // not yet saved!
            } else {
                throw new DBNotFoundException("User-ID does not exists '" + userKey + "'");
            }
        } else
        if (create) {
            // we've been asked to create the user, and it already exists
            throw new DBAlreadyExistsException("User-ID already exists '" + userKey + "'");
        } else {
            user = User.getUser(account, userId); // may throw DBException
            if (user == null) {
                throw new DBException("Unable to read existing User-ID '" + userKey + "'");
            }
            return user;
        }

    }

    /* Create specified user.  Return null if user already exists */
    public static User createNewUser(Account account, String userID, String contactEmail, String passwd)
        throws DBException
    {
        if ((account != null) && (userID != null) && !userID.equals("")) {
            // -- create user record (not yet saved)
            User user = User.getUser(account, userID, true); // does not return null
            // -- set contact email address
            if (contactEmail != null) {
                user.setContactEmail(contactEmail);
            }
            // -- set password
            if (passwd != null) {
                user.setDecodedPassword(null, passwd, true);
            }
            // -- save
            user.save();
            return user;
        } else {
            throw new DBNotFoundException("Invalid Account/UserID specified");
        }
    }

    // ------------------------------------------------------------------------

    /* return list of all Users owned by the specified Account (NOT SCALABLE) */
    // does not return null
    public static String[] getUsersForAccount(DBReadWriteMode rwMode, String acctId)
        throws DBException
    {
        return User.getUsersForAccount(rwMode, acctId, -1);
    }
    // --
    @Deprecated
    public static String[] getUsersForAccount(String acctId)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getUsersForAccount(rwMode, acctId);
    }

    /* return list of all Users owned by the specified Account (NOT SCALABLE) */
    // -- does not return null
    public static String[] getUsersForAccount(DBReadWriteMode rwMode, String acctId, int userType)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* invalid account */
        if ((acctId == null) || acctId.equals("")) {
            return new String[0];
        }

        /* select */
        // DBSelect: SELECT userID FROM User WHERE (accountID='acct') ORDER BY userID
        DBSelect<User> dsel = new DBSelect<User>(User.getFactory());
        dsel.setSelectedFields(User.FLD_userID);
        DBWhere dwh = dsel.createDBWhere();
        //dsel.setWhere(dwh.WHERE_(dwh.EQ(User.FLD_accountID,acctId)));
        dwh.append(dwh.EQ(User.FLD_accountID,acctId));
        if (userType >= 0) {
            // AND (userType=0)
            dwh.append(dwh.AND_(dwh.EQ(User.FLD_userType,userType)));
        }
        dsel.setWhere(dwh.WHERE(dwh.toString()));
        dsel.setOrderByFields(User.FLD_userID);

        /* select */
        return User.getUserIDs(rwMode, dsel);
        
    }
    // --
    @Deprecated
    public static String[] getUsersForAccount(String acctId, int userType)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getUsersForAccount(rwMode, acctId, userType);
    }

    /* return list of all Users owned by the specified Account (NOT SCALABLE) */
    // -- does not return null
    public static String[] getUserIDs(DBReadWriteMode rwMode, DBSelect<User> dsel)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* invalid selection */
        if (dsel == null) {
            return new String[0];
        }
        dsel.setSelectedFields(User.FLD_userID);

        /* read users for account */
        java.util.List<String> userList = new Vector<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode); // getDBConnection_read
            stmt = dbc.execute(dsel.toString());
            rs = stmt.getResultSet();
            while (rs.next()) {
                String userId = rs.getString(User.FLD_userID);
                userList.add(userId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account User List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return userList.toArray(new String[userList.size()]);

    }
    // --
    @Deprecated
    public static String[] getUserIDs(DBSelect<User> dsel)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getUserIDs(rwMode, dsel);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the UserInformation interface instance for the specified unique UserID.
    *** Returns null if not found, or if there is more than one matching UserID.
    **/
    public static UserInformation getUserInformation(String userID, boolean activeOnly)
        throws DBException
    {

        /* get list of Users */
        // -- use limit of 2 to check for duplicate matches
        java.util.List<User> userList = User.getAllMatchingUsers(userID, false/*!activeOnly*/, 2);
        if (ListTools.isEmpty(userList)) {
            // -- no matches found
            return null; // TODO: how to indicate not-found to caller?
        } else
        if (ListTools.size(userList) > 1) {
            // -- duplicate entries found
            return null; // TODO: how to indicate duplicate to caller?
        }

        /* single unique entry found */
        User user = userList.get(0);

        /* check active? */
        if (activeOnly) {
            Account.ActiveStatus actvStat = Account.GetActiveStatus(user);
            if (!actvStat.isActive()) {
                // -- not active
                return null; // TODO: how to indicate inactive to caller?
            }
        }

        /* return */
        return user;

    }

    /**
    *** Gets all Users matching the specified UserID (in any Account).
    *** Does not return null.
    *** (READ database assumed)
    **/
    public static java.util.List<User> getAllMatchingUsers(String userID, boolean activeOnly, long limit)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        java.util.List<User> userList = new Vector<User>();

        /* UserID invalid? */
        if (!AccountRecord.isValidID(userID)) {
            // -- UserID invalid (possibly blank)
            return userList;
        }

        /* read users for contact email */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM User WHERE (userID='user')
            DBSelect<User> dsel = new DBSelect<User>(User.getFactory());
            DBWhere dwh = dsel.createDBWhere();
            dwh.append(
                dwh.EQ(User.FLD_userID, userID)
            );
            dsel.setWhere(dwh.WHERE(dwh.toString()));
            dsel.setOrderByFields(User.FLD_accountID);
            if (limit > 0L) {
                dsel.setLimit(limit);
            }
    
            /* get user records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String aid = rs.getString(User.FLD_accountID);
                String uid = rs.getString(User.FLD_userID);
                User user = new User(new User.Key(aid, uid));
                user.setAllFieldValues(rs);
                if (activeOnly) {
                    Account.ActiveStatus actvStat = Account.GetActiveStatus(user);
                    if (actvStat.isActive()) {
                        userList.add(user);
                    }
                } else {
                    userList.add(user);
                }
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting unique User", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list (may be empty) */
        return userList;

    }

    // ------------------------------------------------------------------------

    /* return the first user which specifies this email address as the contact email */
    public static User getUserForContactEmail(String acctId, String emailAddr, boolean unique)
        throws DBException
    {
        java.util.List<User> userList = User.getUsersForContactEmail(acctId, emailAddr);
        if (ListTools.isEmpty(userList)) {
            // --- no matches found
            return null;
        } else
        if (unique && (userList.size() > 1)) {
            // -- more than one found
            return null;
        } else {
            // -- return first match
            return userList.get(0);
        }
    }

    /* return all users which list this email address as the contact email (READ database assumed) */
    public static java.util.List<User> getUsersForContactEmail(String acctId, String emailAddr)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        java.util.List<User> userList = new Vector<User>();

        /* invalid account? */
        boolean acctIdBlank = StringTools.isBlank(acctId);
        //if (acctIdBlank) {
        //    return userList;
        //}

        /* EMailAddress specified? */
        if (StringTools.isBlank(emailAddr)) {
            return userList; // empty list
        }

        /* read users for contact email */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT userID FROM User WHERE [(accountID='account') AND] (contactEmail='email')
            DBSelect<User> dsel = new DBSelect<User>(User.getFactory());
            //dsel.setSelectedFields(User.FLD_accountID,User.FLD_userID);
            DBWhere dwh = dsel.createDBWhere();
            if (acctIdBlank) {
                dwh.append(
                    dwh.EQ(User.FLD_contactEmail,emailAddr)
                );
            } else {
                dwh.append(dwh.AND(
                    dwh.EQ(User.FLD_accountID   ,acctId),
                    dwh.EQ(User.FLD_contactEmail,emailAddr)
                ));
            }
            dsel.setWhere(dwh.WHERE(dwh.toString()));
            dsel.setOrderByFields(User.FLD_userID);
            // -- Note: The index on the column FLD_contactEmail is not unique

            /* get records */
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String aid = rs.getString(User.FLD_accountID);
                String uid = rs.getString(User.FLD_userID);
                User user = new User(new User.Key(aid, uid));
                user.setAllFieldValues(rs);
                userList.add(user);
            }

        } catch (SQLException sqe) {
            throw new DBException("Get User ContactEmail", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        return userList;
    }

    // ------------------------------------------------------------------------

    /* return the DBSelect statement for the specified account/user */
    protected static DBSelect<User> _getUsersForRoleSelect(String acctID, String roleID, long limit)
    {

        /* invalid accountID? */
        if (StringTools.isBlank(acctID)) {
            return null;
        }

        /* invalid roleID? */
        if (StringTools.isBlank(roleID)) {
            return null;
        }

        /* select */
        // DBSelect: SELECT accountID,userID FROM User WHERE (accountID='account') AND (roleID='role')
        DBSelect<User> dsel = new DBSelect<User>(User.getFactory());
        dsel.setSelectedFields(User.FLD_accountID,User.FLD_userID);
        DBWhere dwh = dsel.createDBWhere();
        dwh.append(dwh.AND(
            dwh.EQ(User.FLD_accountID, acctID),
            dwh.EQ(User.FLD_roleID   , roleID)
        ));
        dsel.setWhere(dwh.WHERE(dwh.toString()));
        dsel.setOrderByFields(User.FLD_userID);
        dsel.setLimit(limit);
        return dsel;

    }

    /* return all user IDs for the specified role ID */
    public static long countUserIDsForRole(DBReadWriteMode rwMode, String acctID, String roleID)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);

        /* valid account/roleId? */
        if (StringTools.isBlank(acctID)) {
            return 0L;
        } else
        if (StringTools.isBlank(roleID)) {
            return 0L;
        }

        /* get db selector */
        DBSelect<User> dsel = User._getUsersForRoleSelect(acctID, roleID, -1);
        if (dsel == null) {
            return 0L;
        }

        /* count users */
        long recordCount = 0L;
        try {
            DBProvider.lockTables(new String[] { TABLE_NAME() }, null);
            recordCount = DBRecord.getRecordCount(rwMode, dsel);
        } finally {
            DBProvider.unlockTables();
        }
        return recordCount;

    }
    // --
    @Deprecated
    public static long countUserIDsForRole(String acctID, String roleID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.countUserIDsForRole(rwMode, acctID, roleID);
    }

    /* return all user IDs for the specified role ID */
    public static java.util.List<String> getUserIDsForRole(DBReadWriteMode rwMode, String acctID, String roleID, long limit)
        throws DBException
    {
        rwMode = DBReadWriteMode.getDefaultReadWriteMode(rwMode);
        java.util.List<String> userList = new Vector<String>();

        /* valid account/roleId? */
        if (StringTools.isBlank(acctID)) {
            return null;
        } else
        if (StringTools.isBlank(roleID)) {
            return null;
        }

        /* get db selector */
        DBSelect<User> dsel = User._getUsersForRoleSelect(acctID, roleID, limit);
        if (dsel == null) {
            return null;
        }

        /* read users for roleID */
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {
            dbc  = DBConnection.getDBConnection(rwMode);
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String aid = rs.getString(User.FLD_accountID);
                String uid = rs.getString(User.FLD_userID);
                userList.add(uid);
            }
        } catch (SQLException sqe) {
            throw new DBException("Get Users for Role", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        return userList;
    }
    // --
    @Deprecated
    public static java.util.List<String> getUserIDsForRole(String acctID, String roleID, long limit)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getUserIDsForRole(rwMode, acctID, roleID, limit);
    }

    /* return true if there are any users that reference the specified role */
    public static boolean hasUserIDsForRole(DBReadWriteMode rwMode, String acctID, String roleID)
        throws DBException
    {
        return !ListTools.isEmpty(User.getUserIDsForRole(rwMode, acctID, roleID, 1L));
    }
    // --
    @Deprecated
    public static boolean hasUserIDsForRole(String acctID, String roleID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.hasUserIDsForRole(rwMode, acctID, roleID);
    }

    /* return all user IDs for the specified role ID */
    public static java.util.List<String> getUserIDsForRole(DBReadWriteMode rwMode, String acctID, String roleID)
        throws DBException
    {
        return User.getUserIDsForRole(rwMode, acctID, roleID, -1L);
    }
    // --
    @Deprecated
    public static java.util.List<String> getUserIDsForRole(String acctID, String roleID)
        throws DBException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_WRITE;
        return User.getUserIDsForRole(rwMode, acctID, roleID);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // This section supports a method for obtaining human readable information 
    // from the User record for reporting, or email purposes. (currently 
    // this is used by the 'rules' engine when generating notification emails).

    private static final String KEY_ACCOUNT[]       = EventData.KEY_ACCOUNT;
    private static final String KEY_ACCOUNT_ID[]    = EventData.KEY_ACCOUNT_ID;
    private static final String KEY_USER[]          = { "user"     , "userDesc" };  // 
    private static final String KEY_USER_ID[]       = { "userID"                };  // 
    private static final String KEY_CONTACT_NAME[]  = { "contactName"           };
    private static final String KEY_CONTACT_EMAIL[] = { "contactEmail"          };
    private static final String KEY_CONTACT_PHONE[] = { "contactPhone"          };
    private static final String KEY_TEMP_PASSWORD[] = { "tempPassword"          };

    private static final String KEY_ADDRESS_1[]     = { "addressLine1"          };
    private static final String KEY_ADDRESS_2[]     = { "addressLine2"          };
    private static final String KEY_ADDRESS_3[]     = { "addressLine3"          };
    private static final String KEY_ADDRESS_CITY[]  = { "addressCity"           };
    private static final String KEY_ADDRESS_STATE[] = { "addressState"          };
    private static final String KEY_ADDRESS_ZIP[]   = { "addressZip"            };

    private static final String KEY_DATETIME[]      = EventData.KEY_DATETIME;
    private static final String KEY_DATE_YEAR[]     = EventData.KEY_DATE_YEAR;
    private static final String KEY_DATE_MONTH[]    = EventData.KEY_DATE_MONTH;
    private static final String KEY_DATE_DAY[]      = EventData.KEY_DATE_DAY;
    private static final String KEY_DATE_DOW[]      = EventData.KEY_DATE_DOW;
    private static final String KEY_TIME[]          = EventData.KEY_TIME;
    
    public static String getKeyFieldTitle(String key, String arg, Locale locale)
    {
        return User._getKeyFieldString(
            true/*title*/, key, arg, 
            locale, null/*BasicPrivateLabel*/, null/*User*/);
    }

    // getFieldValueString
    public String getKeyFieldValue(String key, String arg, BasicPrivateLabel bpl)
    {
        Locale locale = (bpl != null)? bpl.getLocale() : null;
        return User._getKeyFieldString(
            false/*value*/, key, arg, 
            locale, bpl, this);
    }

    public static String _getKeyFieldString(
        boolean getTitle, String key, String arg, 
        Locale locale, BasicPrivateLabel bpl, User user)
    {

        /* check for valid field name */
        if (key == null) {
            return null;
        } else
        if ((user == null) && !getTitle) {
            return null; // user required for value (not for title)
        }
        if ((locale == null) && (bpl != null)) { locale = bpl.getLocale(); }
        I18N i18n = I18N.getI18N(Account.class, locale);
        long now = DateTime.getCurrentTimeSec();

        /* Account */
        if (EventData._keyMatch(key,User.KEY_ACCOUNT)) {
            if (getTitle) {
                return i18n.getString("User.key.accountDescription", "Account");
            } else {
                Account account = user.getAccount();
                if (account == null) {
                    return user.getAccountID();
                } else
                if ((arg != null) && arg.equalsIgnoreCase("id")) {
                    return user.getAccountID();
                } else {
                    return account.getDescription();
                }
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ACCOUNT_ID)) {
            if (getTitle) {
                return i18n.getString("User.key.accountID", "Account-ID");
            } else {
                return user.getAccountID();
            }
        }

        /* User */
        if (EventData._keyMatch(key,User.KEY_USER)) {
            if (getTitle) {
                return i18n.getString("User.key.userDescription", "Account");
            } else {
                if ((arg != null) && arg.equalsIgnoreCase("id")) {
                    return user.getUserID();
                } else {
                    String d = user.getDescription();
                    return !StringTools.isBlank(d)? d : user.getContactName();
                }
            }
        } else
        if (EventData._keyMatch(key,User.KEY_USER_ID)) {
            if (getTitle) {
                return i18n.getString("User.key.userID", "User-ID");
            } else {
                return user.getUserID();
            }
        } 

        /* Contact */
        if (EventData._keyMatch(key,User.KEY_CONTACT_NAME)) {
            if (getTitle) {
                return i18n.getString("User.key.contactName", "Contact Name");
            } else {
                String cn = user.getContactName();
                return !StringTools.isBlank(cn)? cn : user.getDescription();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_CONTACT_EMAIL)) {
            if (getTitle) {
                return i18n.getString("User.key.contactEmail", "Contact EMail");
            } else {
                return user.getContactEmail();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_CONTACT_PHONE)) {
            if (getTitle) {
                return i18n.getString("User.key.contactPhone", "Contact Phone");
            } else {
                return user.getContactPhone();
            }
        }

        /* Temporary Password */
        if (EventData._keyMatch(key,User.KEY_TEMP_PASSWORD)) {
            if (getTitle) {
                return i18n.getString("User.key.temporaryPassword", "Temporary Password");
            } else {
                return user.getTempPassword();
            }
        }

        /* Address */
        if (EventData._keyMatch(key,User.KEY_ADDRESS_1)) {
            if (getTitle) {
                return i18n.getString("User.key.addressLine1", "Address-1");
            } else {
                return user.getAddressLine1();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ADDRESS_2)) {
            if (getTitle) {
                return i18n.getString("User.key.addressLine2", "Address-2");
            } else {
                return user.getAddressLine2();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ADDRESS_3)) {
            if (getTitle) {
                return i18n.getString("User.key.addressLine3", "Address-3");
            } else {
                return user.getAddressLine3();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ADDRESS_CITY)) {
            if (getTitle) {
                return i18n.getString("User.key.addressCity", "City");
            } else {
                return user.getAddressCity();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ADDRESS_STATE)) {
            if (getTitle) {
                return i18n.getString("User.key.addressState", "State");
            } else {
                return user.getAddressState();
            }
        } else
        if (EventData._keyMatch(key,User.KEY_ADDRESS_ZIP)) {
            if (getTitle) {
                return i18n.getString("User.key.addressPostalCode", "Zip");
            } else {
                return user.getAddressPostalCode();
            }
        }

        /* Date/Time */
        if (EventData._keyMatch(key,User.KEY_DATETIME)) {
            if (getTitle) {
                return i18n.getString("User.key.dateTime", "Date/Time");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                Account account = user.getAccount();
                return EventData.getTimestampString(now, account, tmz, bpl);
            }
        } else
        if (EventData._keyMatch(key,User.KEY_DATE_YEAR)) {
            if (getTitle) {
                return i18n.getString("User.key.dateYear", "Year");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                return EventData.getTimestampYear(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,User.KEY_DATE_MONTH)) {
            if (getTitle) {
                return i18n.getString("User.key.dateMonth", "Month");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                return EventData.getTimestampMonth(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,User.KEY_DATE_DAY)) {
            if (getTitle) {
                return i18n.getString("User.key.dateDay", "Day");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfMonth(now, tmz);
            }
        } else
        if (EventData._keyMatch(key,User.KEY_DATE_DOW)) {
            if (getTitle) {
                return i18n.getString("User.key.dayOfWeek", "Day Of Week");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                return EventData.getTimestampDayOfWeek(now, false, tmz, locale);
            }
        } else
        if (EventData._keyMatch(key,User.KEY_TIME)) {
            if (getTitle) {
                return i18n.getString("User.key.time", "Time");
            } else {
                TimeZone tmz = user.getTimeZone(null); // non-null
                Account account = user.getAccount();
                return EventData.getTimestampString(now, account, tmz, bpl);
            }
        }

        /* User fields */
        if (getTitle) {
            DBField dbFld = User.getFactory().getField(key);
            if (dbFld != null) {
                return dbFld.getTitle(locale);
            }
            // -- field not found
        } else {
            String fldName = user.getFieldName(key); // this gets the field name with proper case
            DBField dbFld = (fldName != null)? user.getField(fldName) : null;
            if (dbFld != null) {
                Object val = user.getFieldValue(fldName); // straight from table
                if (val == null) { val = dbFld.getDefaultValue(); }
                Account account = user.getAccount();
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
        if (user.hasTemporaryProperties()) {
            RTProperties rtp = user.getTemporaryProperties();
            Object text = (rtp != null)? rtp.getProperty(key,null) : null;
            if (text instanceof I18N.Text) {
                if (getTitle) {
                    // -- all we have is the key name for the title
                    return key;
                } else {
                    // -- return Localized version of value
                    return ((I18N.Text)text).toString(locale);
                }
            }
        }

        // ----------------------------
        // User key not found

        /* not found */
        //Print.logWarn("User key not found: " + key);
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
    public static String insertUserKeyValues(User user, String text)
    {
        if (user != null) {
            return StringTools.insertKeyValues(text, 
                START_DELIM, END_DELIM, DFT_DELIM,
                new User.UserValueMap(user));
        } else {
            return null;
        }
    }

    /**
    *** Insert User replacement values in specified text String
    **/
    public String insertUserKeyValues(String text)
    {
        return User.insertUserKeyValues(this, text);
    }

    public static class UserValueMap
        implements StringTools.KeyValueMap // ReplacementMap
    {
        private User              user      = null;
        private BasicPrivateLabel privLabel = null;
        public UserValueMap(User user) {
            this.user      = user;
            this.privLabel = null;
        }
        public String getKeyValue(String key, String arg, String dft) {
            if (EventData._keyMatch(key,User.KEY_PASSWORD)) {
                if (this.user != null) {
                    String pwd = this.user.getDecodedPassword(this.privLabel);
                    return (pwd != null)? pwd : dft;
                } else {
                    return dft;
                }
            } else
            if (this.user != null) {
                String fldStr = this.user.getKeyFieldValue(key,arg,this.privLabel);
                return (fldStr != null)? fldStr : dft; // "("+key+")";
            } else {
                //Print.logWarn("Key not found: " + key);
                return dft; // "("+key+")";
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final int ID_LEN = DBField.GetDataTypeLength_String(DBField.TYPE_USER_ID()); // 32

    public static class UserLoadValidator
        extends DBLoadValidatorAdapter
    {
        private   Account account          = null;
        private   int     userIDLen        = ID_LEN;
        private   boolean filterIDs        = false;
        private   boolean validateOnly     = false;
        public UserLoadValidator(Account acct, boolean filtIDs, boolean valOnly) throws DBException {
            this.account      = acct;
            this.filterIDs    = filtIDs;
            this.validateOnly = valOnly;
            this.userIDLen    = (User.UserIDColumnLength > 0)? User.UserIDColumnLength : ID_LEN;
        }
        // --
        public Account getAccount() {
            return this.account;
        }
        public String getAccountID() {
            return (this.account != null)? this.account.getAccountID() : "";
        }
        // -- do filter incoming UserID
        public boolean isFilterIDs() { 
            return this.filterIDs;
        }
        // -- validate only, do not send email
        public boolean isValidateOnly() {
            return this.validateOnly;
        }
        // -- set fields that will be inserted/updated
        public boolean setFields(String f[]) throws DBException {
            if (!super.setFields(f)) {
                return false;
            }
            // -- required fields
            if (!this.hasField(FLD_accountID)) {
                Print.logError("Load file is missing column: " + FLD_accountID);
                this.setError();
                return false;
            }
            if (!this.hasField(FLD_userID)) {
                Print.logError("Load file is missing column: " + FLD_userID);
                this.setError();
                return false;
            }
            // -- success
            return true;
        }
        // -- validate field values
        public boolean validateValues(String v[]) throws DBException {
            if (!super.validateValues(v)) {
                return false;
            }
            // -- validate accountID
            String accountID = this.getFieldValue(FLD_accountID,v);
            if (StringTools.isBlank(accountID)) {
                Print.logError("Blank/Null AccountID found: [#" + this.getCount() + "] " + accountID);
                this.setError();
                return false;
            } else
            if (!accountID.equals(this.getAccountID())) {
                Print.logError("Unexpected AccountID: [#" + this.getCount() + "] found '" + accountID + "', expected '"+this.getAccountID()+"'");
                this.setError();
                return false;
            }
            // -- validate userID
            String userID = this.getFieldValue(FLD_userID,v);
            if (this.isFilterIDs()) {
                userID = AccountRecord.getFilteredID(userID,false/*noNull*/,true/*lowerCase*/,true/*strict*/);
                if (userID.length() > this.userIDLen) { userID = userID.substring(0,this.userIDLen); }
                this.setFieldValue(FLD_userID,v,userID);
            }
            if (StringTools.isBlank(userID)) {
                Print.logError("Blank/Null UserID found: [#" + this.getCount() + "] " + userID);
                this.setError();
                return false;
            } else
            if (!AccountRecord.isValidID(userID)) {
                Print.logError("Invalid UserID found: [#" + this.getCount() + "] " + userID);
                this.setError();
                return false;
            } else
            if (userID.length() > this.userIDLen) {
                Print.logError("UserID exceeds maximum ID length: [#" + this.getCount() + "] " + userID);
                this.setError();
                return false;
            }
            // -- validate tempPassword (if present, must not be blank)
            String tempPass = this.getFieldValue(FLD_tempPassword,v);
            if ((tempPass != null) && StringTools.isBlank(tempPass)) {
                Print.logError("Blank Temporary Password found: [#" + this.getCount() + "]");
                this.setError();
                return false;
            }
            // -- force invalid if validate only
            if (this.isValidateOnly()) {
                return false; // force invalid
            }
            // -- ok
            return true;
        }
        // -- validate record for insertion
        public boolean validateInsert(DBRecord<?> dbr) throws DBException {
            return super.validateInsert(dbr);
        }
        public void recordDidInsert(DBRecord<?> dbr) {
            this.recordDidInsertUpdate(dbr, true, null);
        }
        // -- validate record for update
        public boolean validateUpdate(DBRecord<?> dbr, Set<String> updFields) throws DBException {
            return super.validateUpdate(dbr, updFields);
        }
        public void recordDidUpdate(DBRecord<?> dbr, Set<String> updFields) {
            this.recordDidInsertUpdate(dbr, false, updFields);
        }
        // -- validate record for insert/update
        public boolean validateRecord(DBRecord<?> dbr, boolean newRecord, Set<String> updFields) throws DBException {
            this._encodePassword(dbr,newRecord,updFields); // encode temporary password
            return true;
        }
        protected void recordDidInsertUpdate(DBRecord<?> dbr, boolean newRecord) {
            // --
        }
        // --
        protected void _encodePassword(DBRecord<?> dbr, boolean newRecord, Set<String> updFields) {
            if ((dbr != null) && this.hasField(FLD_tempPassword)) {
                User user = (User)dbr;
                String tempPass = user.getTempPassword();
                user.setDecodedPassword(null,tempPass,true/*isTemp*/);
                if ((updFields != null) && !updFields.contains(FLD_password)) {
                    updFields.add(FLD_password); // add "password" field for update
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below

    private static final String ARG_ACCOUNT[]   = { "account" , "acct"      , "a" };
    private static final String ARG_USER[]      = { "user"    , "usr"       , "u" };
    private static final String ARG_EMAIL[]     = { "email"                 };
    private static final String ARG_CREATE[]    = { "create"  , "cr"        };
    private static final String ARG_NOPASS[]    = { "nopass"                };
    private static final String ARG_PASSWORD[]  = { "password", "passwd"    , "pass" };
    private static final String ARG_EDIT[]      = { "edit"    , "ed"        };
    private static final String ARG_EDITALL[]   = { "editall" , "eda"       };
    private static final String ARG_DELETE[]    = { "delete"  , "purge"     };
    private static final String ARG_LIST[]      = { "list"                  };

    private static final String ARG_LOAD[]      = { "load"    , "import"    };
    private static final String ARG_OVERWRITE[] = { "overwrite"             }; // -overwrite=true
    private static final String ARG_VALIDATE[]  = { "validate"              }; // -validate=true
    private static final String ARG_FILTERID[]  = { "filterID", "filter"    }; // -filter=true
    private static final String ARG_VALIDATOR[] = { "validate", "validator" }; // -validator=CLASS_NAME

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + User.class.getName() + " {options}");
        Print.logInfo("Common Options:");
        Print.logInfo("  -account=<id>   Acount ID which owns User");
        Print.logInfo("  -user=<id>      User ID to create/edit");
        Print.logInfo("  -create         Create a new User");
        Print.logInfo("  -edit           Edit an existing (or newly created) User");
        Print.logInfo("  -delete         Delete specified User");
        Print.logInfo("  -list           List Users for Account");
        System.exit(1);
    }

    public static void main(String args[])
    {
        DBConfig.cmdLineInit(args,true);  // main
        String acctID = RTConfig.getString(ARG_ACCOUNT, "");
        String userID = RTConfig.getString(ARG_USER   , "");

        /* option count */
        int opts = 0;

        /* account-id specified? */
        if (StringTools.isBlank(acctID)) {
            Print.logError("Account-ID not specified.");
            usage();
        }

        /* get account */
        Account acct = null;
        try {
            acct = Account.getAccount(acctID); // may return DBException
            if (acct == null) {
                Print.logError("Account-ID does not exist: " + acctID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error loading Account: " + acctID, dbe);
            //dbe.printException(); // CLI
            System.exit(99);
        }

        /* list */
        if (RTConfig.getBoolean(ARG_LIST, false)) {
            opts++;
            try {
                Print.logInfo("Account: " + acctID);
                String userList[] = User.getUsersForAccount(DBReadWriteMode.READ_ONLY, acctID);
                for (int i = 0; i < userList.length; i++) {
                    Print.logInfo("  User: " + userList[i]);
                }
            } catch (DBException dbe) {
                Print.logError("Error listing Users: " + acctID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        }

        /* load */
        if (RTConfig.hasProperty(ARG_LOAD)) {
            opts++;
            SmtpProperties.setGlobalThreadModel_CURRENT();
            // -- get load file
            String  loadFileName = RTConfig.getString(ARG_LOAD,"");
            File    loadFile     = !StringTools.isBlank(loadFileName)? new File(loadFileName) : null;
            if (!FileTools.isFile(loadFile)) {
                Print.sysPrintln("ERROR: Load file does not exist: " + loadFile);
                System.exit(99);
            } else
            if (!FileTools.hasExtension(loadFile,"csv")) {
                Print.sysPrintln("ERROR: Load file does not have '.csv' extension: " + loadFile);
                System.exit(99);
            }
            // -- parameters
            boolean overwrite     = RTConfig.getBoolean(ARG_OVERWRITE,false);// overwrite existing
            boolean validateOnly  = RTConfig.hasProperty(ARG_VALIDATE);      // validate (no insert)
            boolean filterIDs     = RTConfig.getBoolean(ARG_FILTERID,false); // filter/adjust userID
            boolean insertRecord  = !validateOnly;
            boolean noDropWarning = false;
            // -- UserLoadValidator
            UserLoadValidator usrLoadVal;
            String userValCN = RTConfig.getString(ARG_VALIDATOR,null);
            if (!StringTools.isBlank(userValCN)) {
                try {
                    Class<?> userValClass = Class.forName(userValCN);
                    MethodAction ma = new MethodAction(userValClass,Account.class,Boolean.TYPE,Boolean.TYPE);
                    usrLoadVal = (UserLoadValidator)ma.invoke(acct,filterIDs,validateOnly); // non-null
                } catch (Throwable th) { 
                    // -- ClassNotFoundException, NoSuchMethodException, 
                    Print.logException("ERROR: unable to instantiate custom UserLoadValidator: "+userValCN, th);
                    System.exit(99);
                    // -- control does not reach here 
                    // -  (below only needed to keep compiler from complaining about "usrLoadVal")
                    return;
                }
            } else {
                try {
                    usrLoadVal = new UserLoadValidator(acct,filterIDs,validateOnly); // non-null
                } catch (Throwable th) {
                    Print.logException("ERROR: unable to instantiate UserLoadValidator", th);
                    System.exit(99);
                    // -- control does not reach here 
                    // -  (below only needed to keep compiler from complaining about "usrLoadVal")
                    return;
                }
            }
            Print.sysPrintln("UserLoadValidator: " + StringTools.className(usrLoadVal));
            if (usrLoadVal.hasErrors()) {
                Print.logError("UserLoadValidator has errors");
                System.exit(99);
            }
            // -- load User table
            DBFactory<User> fact = User.getFactory();
            String mode = validateOnly? "Validating" : "Loading";
            try {
                Print.sysPrintln(mode + " file: " + loadFile);
                if (!fact.tableExists()) {
                    Print.sysPrintln("ERROR: Table does not exist: " + TABLE_NAME());
                    System.exit(99);
                }
                fact.loadTable(loadFile,usrLoadVal,insertRecord,overwrite,noDropWarning);
                if (usrLoadVal.hasErrors()) {
                    Print.sysPrintln(mode+" error encountered: " + loadFile);
                    System.exit(1);
                } else {
                    Print.sysPrintln(mode+" successful: " + loadFile);
                }
            } catch (DBException dbe) {
                Print.logException(mode+" error encountered", dbe);
                System.exit(1);
            }
            // -- done
            System.exit(0);
        }

        // ---------------------------------------------
        // -- the following require a "-user" specification

        /* user-id specified? */
        if (StringTools.isBlank(userID)) {
            Print.logError("User-ID not specified.");
            usage();
        }

        /* user exists? */
        boolean userExists = false;
        try {
            userExists = User.exists(acctID, userID);
        } catch (DBException dbe) {
            Print.logError("Error determining if User exists: " + acctID + "," + userID);
            System.exit(99);
        }

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE, false) && !acctID.equals("") && !userID.equals("")) {
            opts++;
            if (!userExists) {
                Print.logWarn("User does not exist: " + acctID + "/" + userID);
                Print.logWarn("Continuing with delete process ...");
            }
            try {
                User.Key userKey = new User.Key(acctID, userID);
                userKey.delete(true); // also delete dependencies
                Print.logInfo("User deleted: " + acctID + "/" + userID);
            } catch (DBException dbe) {
                Print.logError("Error deleting User: " + acctID + "/" + userID);
                dbe.printException(); // CLI
                System.exit(99);
            }
            System.exit(0);
        }

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (userExists) {
                Print.logWarn("User already exists: " + acctID + "/" + userID);
            } else {
                String contactEmail = RTConfig.getString(ARG_EMAIL, "");
                try {
                    String passwd = null;
                    if (RTConfig.getBoolean(ARG_NOPASS,false)) {
                        passwd = BLANK_PASSWORD;
                    } else
                    if (RTConfig.hasProperty(ARG_PASSWORD)) {
                        passwd = RTConfig.getString(ARG_PASSWORD,"");
                    }
                    User.createNewUser(acct, userID, contactEmail, passwd);
                    Print.logInfo("Created User-ID: " + acctID + "/" + userID);
                } catch (DBException dbe) {
                    Print.logError("Error creating User: " + acctID + "/" + userID);
                    dbe.printException(); // CLI
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) {
            opts++;
            if (!userExists) {
                Print.logError("User does not exist: " + acctID + "/" + userID);
            } else {
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL,false);
                    User user = User.getUser(acct, userID, false); // may throw DBException
                    DBEdit editor = new DBEdit(user);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing User: " + acctID + "/" + userID);
                    dbe.printException(); // CLI
                    System.exit(99);
                }
            }
            System.exit(0);
        }

        /* no options specified */
        if (opts == 0) {
            Print.logWarn("Missing options ...");
            usage();
        }

    }

    // ------------------------------------------------------------------------

}
