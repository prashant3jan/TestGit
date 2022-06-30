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
//  2009/04/02  Martin D. Flynn
//     -Added 'filterID' and 'isValidID' methods
//  2013/04/08  Martin D. Flynn
//     -Added 'GetSimpleLocalString' method
//  2016/04/06  Martin D. Flynn
//     -Added "strict" option to "isValidID" and "getFilteredID" [2.6.2-B15]
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
import org.opengts.db.tables.SystemProps;

public class AccountRecord<RT extends DBRecord<RT>>
    extends DBRecord<RT>
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Attempts to return a localized String based on the specified value.
    *** @param v   The value for which the localized string is returned.
    *** @param loc The Locale
    *** @return The localized value for the specified String, or the value unchanged
    ***   if no match is found for the specified String.
    **/
    public static String GetSimpleLocalString(String v, Locale loc)
    {
        I18N i18n = I18N.getI18N(AccountRecord.class, loc);

        /* blank */
        if (StringTools.isBlank(v)) {
            return v; // leave as-is
        }

        /* Yes/No */
        if (v.equalsIgnoreCase("yes")) {
            return i18n.getString("AccountRecord.yes","Yes");
        } else
        if (v.equalsIgnoreCase("no")) {
            return i18n.getString("AccountRecord.no","No");
        }

        /* True/False */
        if (v.equalsIgnoreCase("true")) {
            return i18n.getString("AccountRecord.true","True");
        } else
        if (v.equalsIgnoreCase("false")) {
            return i18n.getString("AccountRecord.false","False");
        }

        /* On/Off */
        if (v.equalsIgnoreCase("on")) {
            return i18n.getString("AccountRecord.on","On");
        } else
        if (v.equalsIgnoreCase("off")) {
            return i18n.getString("AccountRecord.off","Off");
        }

        /* Enable/Disable */
        if (v.equalsIgnoreCase("enable")) {
            return i18n.getString("AccountRecord.enable","Enable");
        } else
        if (v.equalsIgnoreCase("disable")) {
            return i18n.getString("AccountRecord.disable","Disable");
        }

        /* Enabled/Disabled */
        if (v.equalsIgnoreCase("enabled")) {
            return i18n.getString("AccountRecord.enabled","Enabled");
        } else
        if (v.equalsIgnoreCase("disabled")) {
            return i18n.getString("AccountRecord.disabled","Disabled");
        }

        /* Enter/Exit */
        if (v.equalsIgnoreCase("enter")) {
            return i18n.getString("AccountRecord.enter","Enter");
        } else
        if (v.equalsIgnoreCase("exit")) {
            return i18n.getString("AccountRecord.exit","Exit");
        }

        /* Login/Logout */
        if (v.equalsIgnoreCase("login")) {
            return i18n.getString("AccountRecord.login","Login");
        } else
        if (v.equalsIgnoreCase("logout")) {
            return i18n.getString("AccountRecord.logout","Logout");
        }

        /* default */
        return v; // return as-is

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* common Account field definition */
    public static final String FLD_accountID        = "accountID";
    public static final String FLD_deletedTime      = "deletedTime";
    public static final String FLD_isActive         = "isActive";
    public static final String FLD_displayName      = "displayName";
    public static final String FLD_notes            = "notes";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* Account key */
    public static abstract class AccountKey<RT extends DBRecord<RT>>
        extends DBRecordKey<RT>
    {
        public AccountKey() {
            super();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a filtered ID with all invalid characters removed.  If the resulting
    *** ID contains no valid characters, null will be returned.
    *** @param id  The specified ID to filter
    *** @return The filtered ID, or null if the specified id contains no valid characters.
    **/
    public static String getFilteredID(String id)
    {
        return AccountRecord.getFilteredID(id, false/*nullOnError*/, false/*lowerCase*/, true/*strict*/);
    }

    /**
    *** Returns a filtered ID with all invalid characters removed.  If the resulting
    *** ID contains no valid characters, null will be returned.
    *** @param id           The specified ID to filter
    *** @param nullOnError  If true, return 'null' immediately if the id contains any invalid characters.
    *** @param lowerCase    If true, return the filtered ID in lower case.
    *** @return The filtered ID, or null if the specified id contains no valid characters.
    **/
    public static String getFilteredID(String id, boolean nullOnError, boolean lowerCase, boolean strict)
    {
        if (StringTools.isBlank(id)) {
            return null; // invalid id
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < id.length(); i++) {
                // -- allow(strict): 0..9 a..z _ - . @ 
                char ch = id.charAt(i);
                if (Character.isLetterOrDigit(ch)) {
                    sb.append(ch);
                } else
                if ((ch == '_') || (ch == '-') || (ch == '.') || (ch == '@')) {
                    sb.append(ch);
                } else
                if (!strict && ((ch == ' ') || (ch == '#'))) {
                    sb.append(ch);
                } else
                if (nullOnError) {
                    return null;
                }
            }
            String s = sb.toString();
            if (StringTools.isBlank(s)) {
                return null;
            } else {
                return lowerCase? s.toLowerCase() : s;
            }
        }
    }

    /**
    *** Returns true if the id is valid
    *** @return True if the id is valid
    **/
    public static boolean isValidID(String id)
    {
        return AccountRecord.isValidID(id,true/*strict*/);
    }

    /**
    *** Returns true if the id is valid
    *** @return True if the id is valid
    **/
    public static boolean isValidID(String id, boolean strict)
    {
        return (AccountRecord.getFilteredID(id,true/*nullOnError*/,false/*lowerCase*/,strict) != null);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* Bean instance */
    public AccountRecord()
    {
        super();
    }

    /* database record */
    public AccountRecord(AccountKey<RT> key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------

    /* create a new "accountID" key field definition */
    protected static DBField newField_accountID(boolean priKey)
    {
        return AccountRecord.newField_accountID(priKey, null);
    }

    /* create a new "accountID" key field definition */
    protected static DBField newField_accountID(boolean priKey, String xAttr)
    {
        I18N.Text title = I18N.getString(AccountRecord.class,"AccountRecord.fld.accountID","Account ID"); 
        String attr = (priKey?"key=true":"edit=2") + (StringTools.isBlank(xAttr)?"":(" "+xAttr));
        return new DBField(FLD_accountID, String.class, DBField.TYPE_ACCT_ID(), title, attr);
    }

    // --------------------------------

    /* return the Account ID for this record */
    @JSONBeanGetter()
    public final String getAccountID()
    {
        String v = (String)this.getKeyValue(FLD_accountID); // getFieldValue
        String acctID = (v != null)? v : "";
        if (StringTools.isBlank(acctID)) { 
            Print.logWarn("Account ID is blank: ");
            Print.logWarn(this.getRecordKey().getKeyValues().toString());
            Print.logWarn(this.getRecordKey().getFieldValues().toString());
        }
        return acctID;
    }

    /* set the Account ID for this record */
    private final void setAccountID(String v)
    {
        this.setKeyValue(FLD_accountID, ((v != null)? v : "")); // setFieldValue
    }

    /* return true if this account is a system admin */
    public final boolean isSystemAdmin()
    {
        return AccountRecord.isSystemAdminAccountID(this.getAccountID());
    }

    // --------------------------------

    /**
    *** Returns true if the current instance AccountID matches the specified AccountID
    **/
    public boolean isAccount(String accountID)
    {
        return this.getAccountID().equalsIgnoreCase(accountID)? true : false;
    }

    /**
    *** Returns true if the current instance AccountID matches the AccountID of the specified instance
    **/
    public boolean isAccount(AccountRecord<?> acctRcd)
    {
        if (acctRcd == null) {
            return false;
        } else {
            return this.isAccount(acctRcd.getAccountID());
        }
    }

    /**
    *** Returns true if the specified AccountRecord instances belong to the same Account
    **/
    public static boolean isAccount(AccountRecord<?> ar1, AccountRecord<?> ar2)
    {
        if ((ar1 == null) || (ar2 == null)) {
            return false;
        } else {
            return ar1.isAccount(ar2.getAccountID())? true : false;
        }
    }

    // --------------------------------

    /* gets the system-account ID */
    public static String getSystemAdminAccountID()
    {
        return StringTools.trim(RTConfig.getString(DBConfig.PROP_sysAdmin_account));
    }

    /* returns true if the system-account ID is defined */
    public static boolean hasSystemAdminAccountID()
    {
        return !StringTools.isBlank(AccountRecord.getSystemAdminAccountID());
    }

    /* return true if this account is a system admin */
    public static boolean isSystemAdminAccountID(String acctID)
    {
        if (StringTools.isBlank(acctID)) {
            return false;
        } else {
            String sysAdminAcctID = AccountRecord.getSystemAdminAccountID();
            if (StringTools.isBlank(sysAdminAcctID)) {
                return false;
            } else {
                return acctID.equalsIgnoreCase(sysAdminAcctID);
            }
        }
    }

    /* return true if the specified account is a system admin */
    public static boolean isSystemAdmin(Account account)
    {
        if (account == null) {
            return false;
        } else {
            return account.isSystemAdmin();
        }
    }

    /* return SystemAdmin account */
    public static Account getSystemAdminAccount()
        throws DBException
    {
        String sysAdminAcctID = AccountRecord.getSystemAdminAccountID();
        if (StringTools.isBlank(sysAdminAcctID)) {
            return null;
        } else {
            return Account.getAccount(sysAdminAcctID);
        }
    }

    /* creates SystemAdmin account (throws exception if already exists) */
    public static Account createSystemAdminAccount(String password)
        throws DBException
    {
        String sysAdminAcctID = AccountRecord.getSystemAdminAccountID();
        if (StringTools.isBlank(sysAdminAcctID)) {
            return null;
        } else {
            Account sysAdmin = Account.getAccount(sysAdminAcctID, true);
            if (!StringTools.isBlank(password)) {
                sysAdmin.setDecodedPassword(null/*BasicPrivateLabel*/, password, false);
            }
            sysAdmin.save();
            return sysAdmin;
        }
    }

    // ------------------------------------------------------------------------

    /* return true if the specified account is a system admin */
    public static boolean isAccountManager(Account account)
    {
        if (account == null) {
            return false;
        } else {
            return account.isAccountManager();
        }
    }

    // ------------------------------------------------------------------------

    /* create a new "deletedTime" field definition */
    protected static DBField newField_deletedTime()
    {
        return AccountRecord.newField_deletedTime(null);
    }

    /* create a new "deletedTime" field definition */
    protected static DBField newField_deletedTime(String xAttr)
    {
        I18N.Text title = I18N.getString(AccountRecord.class,"AccountRecord.fld.deletedTime","Deleted Time"); 
        String attr = "edit=2" + (StringTools.isBlank(xAttr)?"":(" " + xAttr));
        return new DBField(FLD_deletedTime, Long.TYPE, DBField.TYPE_UINT32, title, attr);
    }

    // --------------------------------

    /**
    *** Returns true if this table supports the "deletedTime" field
    **/
    public boolean supportsDeletedTime()
    {
        DBFactory<RT> dbFact = this.getRecordKey().getFactory();
        return dbFact.hasField(FLD_deletedTime);
    }

    /**
    *** Gets the deleted-time value.
    *** Returns "0" if this record has not been deleted, or if the "deletedTime" field is not supported.
    **/
    public long getDeletedTime()
    {
        if (this.supportsDeletedTime()) {
            Long v = (Long)this.getOptionalFieldValue(FLD_deletedTime);
            return (v != null)? v.longValue() : 0L;
        } else {
            return 0L;
        }
    }

    /**
    *** Sets the deleted-time value.
    *** Ignored if the "deletedTime" field is not supported.
    **/
    public void setDeletedTime(long v)
    {
        if (this.supportsDeletedTime()) {
            this.setOptionalFieldValue(FLD_deletedTime, ((v <= 0L)? 0L : v));
        }
    }

    // --------------------------------

    /**
    *** Returns true if this record is deleted
    **/
    public final boolean getIsDeleted()
    {
        return (this.getDeletedTime() > 0L)? true : false;
    }

    /**
    *** Returns true if this record is deleted
    **/
    public boolean isDeleted()
    {
        return this.getIsDeleted();
    }

    // --------------------------------

    /**
    *** Marks this record as deleted
    *** Ignored if the "deletedTime" field is not supported, or if already deleted.
    **/
    protected Set<String> _markRecordAs_Deleted()
    {

        /* "deleteTime" supported? */
        if (!this.supportsDeletedTime()) {
            return null;
        }

        /* already deleted? */
        if (this.getIsDeleted()) {
            return null;
        }

        /* newly deleted */
        Set<String> flds = new HashSet<String>();
        this.setDeletedTime(DateTime.getCurrentTimeSec()); // FLD_deletedTime
        flds.add(FLD_deletedTime);

        /* set inactive */
        if (this.supportsIsActive() && this.getIsActive()) {
            // -- newly inactive
            this.setIsActive(false); // FLD_isActive
            flds.add(FLD_isActive);
        }

        /* return modified fields */
        return flds;

    }

    /**
    *** Marks this record as deleted.
    *** @param update   True to update table, false to not update table
    **/
    public void markRecordAsDeleted(boolean update)
        throws DBException
    {
        Set<String> flds = this._markRecordAs_Deleted();
        if (update && !ListTools.isEmpty(flds)) {
            this.update(flds); // throws DBException
        }
    }

    // --------------------------------

    /**
    *** Marks this record as undeleted
    **/
    protected Set<String> _markRecordAs_Undeleted()
    {

        /* "deleteTime" supported? */
        if (!this.supportsDeletedTime()) {
            return null;
        }

        /* already undeleted? */
        if (!this.getIsDeleted()) {
            return null;
        }

        /* newly undeleted */
        Set<String> flds = new HashSet<String>();;
        this.setDeletedTime(0L); // FLD_deletedTime
        flds.add(FLD_deletedTime);

        /* return modified fields */
        return flds;

    }

    /**
    *** Marks this record as undeleted.
    *** @param update   True to update table, false to not update table
    **/
    public void markRecordAsUndeleted(boolean update)
        throws DBException
    {
        Set<String> flds = this._markRecordAs_Deleted();
        if (update && !ListTools.isEmpty(flds)) {
            this.update(flds); // throws DBException
        }
    }

    // ------------------------------------------------------------------------

    /* create a new "isActive" field definition */
    protected static DBField newField_isActive()
    {
        return AccountRecord.newField_isActive(null);
    }

    /* create a new "isActive" field definition */
    protected static DBField newField_isActive(String xAttr)
    {
        I18N.Text title = I18N.getString(AccountRecord.class,"AccountRecord.fld.isActive","Is Active"); 
        String attr = "edit=2" + (StringTools.isBlank(xAttr)?"":(" " + xAttr));
        return new DBField(FLD_isActive, Boolean.TYPE, DBField.TYPE_BOOLEAN, title, attr);
    }

    // --------------------------------

    /**
    *** Returns true if this table supports the "isActive" field
    **/
    public boolean supportsIsActive()
    {
        DBFactory<RT> dbFact = this.getRecordKey().getFactory();
        return dbFact.hasField(FLD_isActive);
    }

    /**
    *** Gets the isActive state of this record
    **/
    public /*final*/ boolean getIsActive() // "final" removed v2.5.6-B15
    {
        if (this.supportsIsActive()) {
            Boolean v = (Boolean)this.getFieldValue(FLD_isActive);
            return (v != null)? v.booleanValue() : true; // if null, default set to true [2.6.6-B54d]
        } else {
            return true;
        }
    }

    /**
    *** Sets the isActive state of this record
    **/
    public /*final*/ void setIsActive(boolean v)
    {
        if (this.supportsIsActive()) {
            this.setFieldValue(FLD_isActive, v);
        }
    }

    // --------------------------------

    /**
    *** Gets the isActive state of this record.
    *** Also, returns false if this record has been deleted.
    **/
    public boolean isActive() // removed "final" (overridden by DeviceReord)
    {
        if (this.isDeleted()) { // this.getIsDeleted() ?
            // -- record is deleted, cannot be active
            return false;
        } else {
            // -- return "isActive" state from record
            return this.getIsActive();
        }
    }

    // ------------------------------------------------------------------------

    /* create a new "displayName" field definition */
    protected static DBField newField_displayName()
    {
        return AccountRecord.newField_displayName(null);
    }

    /* create a new "displayName" field definition */
    protected static DBField newField_displayName(String xAttr)
    {
        I18N.Text title = I18N.getString(AccountRecord.class,"AccountRecord.fld.displayName","Display Name"); 
        String attr = "edit=2 utf8=true" + (StringTools.isBlank(xAttr)?"":(" " + xAttr));
        return new DBField(FLD_displayName, String.class, DBField.TYPE_STRING(40), title, attr);
    }

    // --------------------------------

    /* return the display name */
    public /*final*/ String getDisplayName() // "final" removed [2.6.6-B68g]
    {
        String v = (String)this.getFieldValue(FLD_displayName);
        return (v != null)? v : "";
    }

    /* set the display name */
    public final void setDisplayName(String v)
    {
        this.setFieldValue(FLD_displayName, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /* create a new "notes" field notes */
    protected static DBField newField_notes()
    {
        return AccountRecord.newField_notes(null);
    }

    /* create a new "notes" field notes */
    protected static DBField newField_notes(String xAttr)
    {
        I18N.Text title = I18N.getString(AccountRecord.class,"AccountRecord.fld.notes","Notes"); 
        String attr = "edit=2 editor=textArea utf8=true" + (StringTools.isBlank(xAttr)?"":(" " + xAttr));
        return new DBField(FLD_notes, String.class, DBField.TYPE_TEXT, title, attr);
    }

    // --------------------------------

    public String getNotes()
    {
        String v = (String)this.getFieldValue(FLD_notes);
        return (v != null)? v : "";
    }

    public void setNotes(String v)
    {
        this.setFieldValue(FLD_notes, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // The following is an optimization for holding the Account record while
    // processing this AccountRecord.  Use with caution.

    private Account account = null;

    public final boolean hasAccount()
    {
        if ((Object)this instanceof Account) {
            return true;
        } else {
            return (this.account != null);
        }
    }
    
    public final Account getAccount()
    {

        /* return this Account? */
        if ((Object)this instanceof Account) {
            return (Account)((Object)this);
        }

        /* get/return Account */
        if (this.account == null) {
            String acctID = this.getAccountID();
            if (!StringTools.isBlank(acctID)) {
                Print.logDebug("[Optimize] Retrieving Account record: " + acctID);
                try {
                    this.account = Account.getAccount(acctID);
                    if (this.account == null) {
                        Print.logError("Account not found: " + acctID);
                    }
                } catch (DBException dbe) {
                    // may be caused by "java.net.ConnectException: Connection refused: connect"
                    Print.logError("Account not found: " + acctID);
                    this.account = null;
                }
            } else {
                Print.logError("Account not defined: " + StringTools.className(this));
            }
        }
        return this.account;

    }

    /* set the account for this event */
    private static long _setAccountError = 0L;
    public final void setAccount(Account acct) 
    {
        if ((Object)this instanceof Account) {
            if (this != acct) {
                Print.logError("'this' is already an Account: " + this.getAccountID());
            }
        } else
        if (acct == null) {
            this.account = null;
        } else
        if (!this.getAccountID().equals(acct.getAccountID())) {
            _setAccountError++;
            String msg = "Account IDs do not match: " + this.getAccountID() + " != " + acct.getAccountID();
            if (_setAccountError < 4L) {
                Print.logStackTrace(msg);
            } else {
                Print.logError(msg);
            }
            this.account = null;
        } else {
            this.account = acct;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Return the description for this DBRecord's Account
    *** @return The Account description
    **/
    public /*final*/ String getAccountDescription() // "final" removed [2.6.6.-B68g]
    {
        Account acct = this.getAccount();
        return (acct != null)? acct.getDescription() : this.getAccountID();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the runtime default values.
    *** This method will attempt to first look up the property value from the PrivateLabel file.
    **/
    public void setRuntimeDefaultValues()
    {
        if (!((Object)this instanceof Account) && this.hasAccount()) {
            BasicPrivateLabel privLbl = this.getAccount().getPrivateLabel();
            if (privLbl != null) {
                DBRecordKey<RT> rk = this.getRecordKey();
                String  tableName  = rk.getUntranslatedTableName();
                DBField fld[]      = rk.getFields();
                for (int i = 0; i < fld.length; i++) {
                    String fn   = fld[i].getName();
                    String dk[] = this.getDefaultFieldValueKey(fn); // "TABLE.FIELD", "TABLE.default.FIELD"
                    String val  = null;
                    // first try PrivateLabel properties
                    for (int p = 0; p < dk.length; p++) {
                        if (privLbl.hasProperty(dk[p])) {
                            val = privLbl.getStringProperty(dk[p], null);
                            break;
                        }
                    }
                    // next try Runtime properties
                    if ((val == null) && RTConfig.hasProperty(dk)) {
                        val = RTConfig.getString(dk, null);
                    }
                    // if found, set the default value
                    if (val != null) {
                        if (!fld[i].isPrimaryKey()) { // cannot change primary key
                            this.setFieldValue(fn, fld[i].parseStringValue(val));
                        } else {
                            Print.logError("Refusing to set a default value for a primary key field: " + fn);
                        }
                    }
                }
                return;
            }
        }
        super.setRuntimeDefaultValues(); // DBRecord
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
            boolean isMember = DBFactory.isTableClass(utableName, AccountRecord.class);
            Print.sysPrintln("isAccountRecord("+utableName+") == " + isMember);
            System.exit(0);
        }

        /* list members */
        if (RTConfig.hasProperty(ARG_LIST_MEMBERS)) {
            Print.sysPrintln("AccountRecord tables:");
            DBFactory<? extends DBRecord<?>> facts[] = DBAdmin.getClassTableFactories(AccountRecord.class);
            for (DBFactory<? extends DBRecord<?>> tableFact : facts) {
                Print.sysPrintln("  "+tableFact.getUntranslatedTableName());
            }
            System.exit(0);
        }

    }

}
