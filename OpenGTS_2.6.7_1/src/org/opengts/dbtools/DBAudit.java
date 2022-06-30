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
//  2018/09/10  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.dbtools;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

/**
*** DBAudit table.
**/

public class DBAudit
    extends DBRecord<DBAudit>
{

    // ------------------------------------------------------------------------

    // -- DBAudit enabled?
    private static int           ENABLE_DBAUDIT     = -1;

    // -- DBAudit during web-app only?
    private static final boolean WEB_AUDIT_ONLY     = true;

    /**
    *** Returns true if DBAudit is globally enabled
    **/
    public static boolean EnableTableAudit()
    {
        // -- initialize 
        if (ENABLE_DBAUDIT < 0) {
            // -- initialize from property
            // -    db.enableTableAudit=true
            // -    Account.auditTable=true
            // -    User.auditTable=true
            // -    Device.auditTable=true
            // -    ...
            //if (WEB_AUDIT_ONLY && !RTConfig.isWebApp()) {
            //    ENABLE_DBAUDIT = 0;
            //} else {
                ENABLE_DBAUDIT = RTConfig.getBoolean(RTKey.DB_ENABLE_TABLE_AUDIT,false)? 1 : 0;
            //}
        }
        // -- DBAudit enabled/disabled?
        return (ENABLE_DBAUDIT == 1)? true : false;
    }

    /**
    *** Returns true if DBAudit is enabled for the specified table
    **/
    public static boolean EnableTableAudit(String utableName)
    {
        if (!DBAudit.EnableTableAudit()) {
            // -- global DBAudit disabled
            return false;
        } else
        if (utableName == null) {
            // -- no table name specified, disabled
            return false;
        } else
        if (utableName.length() <= 0) {
            // -- no table name specified, disabled
            return false;
        } else
        if (utableName.equalsIgnoreCase(DBAudit._TABLE_NAME)) {
            // -- "DBAudit" is never auditable! (this could be a disaster!)
            return false;
        } else {
            // -- enabled for table?
            String auditKey = utableName + RTKey._DB_TABLE_AUDIT;
            return RTConfig.getBoolean(auditKey, false);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Change type

    public enum ChangeType implements EnumTools.StringLocale, EnumTools.IntValue {
        UNKNOWN ( 0, I18N.getString(DBAudit.class,"DBAudit.ChangeType.unknown","Unknown")),
        INSERT  (10, I18N.getString(DBAudit.class,"DBAudit.ChangeType.insert" ,"Insert" )),
        UPDATE  (20, I18N.getString(DBAudit.class,"DBAudit.ChangeType.update" ,"Update" )),
        DELETE  (30, I18N.getString(DBAudit.class,"DBAudit.ChangeType.delete" ,"Delete" ));
        // ---
        private int       vv = 0;
        private I18N.Text aa = null;
        ChangeType(int v, I18N.Text a)      { vv = v; aa = a; }
        public int     getIntValue()        { return vv; }
        public String  toString()           { return aa.toString(); }
        public String  toString(Locale loc) { return aa.toString(loc); }
        public boolean isUnknown()          { return this.equals(ChangeType.UNKNOWN); }
        public boolean isInsert()           { return this.equals(ChangeType.INSERT);  }
        public boolean isUpdate()           { return this.equals(ChangeType.UPDATE);  }
        public boolean isDelete()           { return this.equals(ChangeType.DELETE);  }
    };

    /**
    *** Returns the defined ChangeType for the specified DBAudit
    *** @param dba  The DeviceMessageInterface from which the ChangeType will be obtained.  
    ***           If null, the default MessageType will be returned.
    *** @return The ChangeType
    **/
    public static ChangeType getChangeType(DBAudit dba)
    {
        return (dba != null)? 
            EnumTools.getValueOf(ChangeType.class,dba.getChangeType()) : 
            EnumTools.getDefault(ChangeType.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "DBAudit";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    // -- Keys:
    // -    accountID
    // -    userID
    // -    tableName
    // -    timestamp
    // -- Fields:
    // -    changeType (insert,update,delete)
    // -    columnValues (field=value field=value ...)
    public static final String FLD_accountID            = "accountID";          // AccountID
    public static final String FLD_userID               = "userID";             // UserID
    public static final String FLD_tableName            = "tableName";          // table name
    public static final String FLD_timestampMS          = "timestampMS";        // timestamp ms
    public static final String FLD_changeType           = "changeType";         // insert,update,delete
    public static final String FLD_columnsBefore        = "columnsBefore";      // column=value column=value ...
    public static final String FLD_columnsAfter         = "columnsAfter";       // column=value column=value ...
    private static DBField FieldInfo[] = {
        // DBAudit fields
        new DBField(FLD_accountID    , String.class  , DBField.TYPE_ACCT_ID() , "Account ID"    , "key=true"),
        new DBField(FLD_userID       , String.class  , DBField.TYPE_USER_ID() , "User ID"       , "key=true"),
        new DBField(FLD_tableName    , String.class  , DBField.TYPE_STRING(24), "Table Name"    , "key=true"),
        new DBField(FLD_timestampMS  , Long.TYPE     , DBField.TYPE_INT64     , "Timestamp MS"  , "key=true"),
        new DBField(FLD_changeType   , Integer.TYPE  , DBField.TYPE_INT16     , "Change Type"   , "edit=2"),
        new DBField(FLD_columnsBefore, String.class  , DBField.TYPE_TEXT      , "Columns Before", "edit=2"),
        new DBField(FLD_columnsAfter , String.class  , DBField.TYPE_TEXT      , "Columns After" , "edit=2"),
    };

    /* key class */
    public static class Key
        extends DBRecordKey<DBAudit>
    {
        public Key() {
            super();
        }
        public Key(String acctID, String usrID, String tblName, long ts) {
            super.setKeyValue(FLD_accountID  , ((acctID != null)? acctID.toLowerCase() : ""));
            super.setKeyValue(FLD_userID     , ((usrID  != null)? usrID.toLowerCase()  : ""));
            super.setKeyValue(FLD_tableName  , StringTools.trim(tblName));
            super.setKeyValue(FLD_timestampMS, ts);
        }
        public DBFactory<DBAudit> getFactory() {
            return DBAudit.getFactory();
        }
    }
    
    /* factory constructor */
    private static DBFactory<DBAudit> factory = null;
    public static DBFactory<DBAudit> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                DBAudit.TABLE_NAME(), 
                DBAudit.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                DBAudit.class, 
                DBAudit.Key.class,
                false/*editable*/, true/*viewable*/);
            //factory.addParentTable(Account.TABLE_NAME());
            //factory.addParentTable(User.TABLE_NAME());
        }
        return factory;
    }

    /* Bean instance */
    public DBAudit()
    {
        super();
    }

    /* database record */
    public DBAudit(DBAudit.Key key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(DBAudit.class, loc);
        return i18n.getString("DBAudit.description", 
            "This table contains " + 
            "DBAudit information."
            );
    }

    // ------------------------------------------------------------------------

    /**
    *** Updates the specified fields in this DBRecord.
    *** @param updFldSet  A Set of fields to update.
    *** @throws DBException if a database error occurs.
    **/
    public void update(Set<String> updFldSet)
        throws DBException
    {
        super.update(updFldSet);
    }

    /** 
    *** Insert this DBRecord in the database.<br>
    *** An exception will be throw if the record already exists
    *** @throws DBException if a database error occurs.
    **/
    public void insert()
        throws DBException
    {
        super.insert();
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    /**
    *** Gets the TableName
    **/
    public String getTableName()
    {
        String v = (String)this.getFieldValue(FLD_tableName);
        return StringTools.trim(v);
    }

    /**
    *** Sets the TableName
    **/
    public void setTableName(String v)
    {
        this.setFieldValue(FLD_tableName, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message was initially queued (in milliseconds)
    **/
    public long getTimestampMS()
    {
        Long v = (Long)this.getFieldValue(FLD_timestampMS);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message was initially queued
    **/
    public void setTimestampMS(long v)
    {
        this.setFieldValue(FLD_timestampMS, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the enumerated change type
    **/
    public int getChangeType()
    {
        Integer v = (Integer)this.getFieldValue(FLD_changeType);
        return (v != null)? v.intValue() : EnumTools.getDefault(ChangeType.class).getIntValue();
    }

    /**
    *** Sets the enumerated change type
    **/
    public void setChangeType(int v)
    {
        this.setFieldValue(FLD_changeType, EnumTools.getValueOf(ChangeType.class,v).getIntValue());
    }

    /**
    *** Sets the enumerated change type
    **/
    public void setChangeType(ChangeType v)
    {
        this.setFieldValue(FLD_changeType, EnumTools.getValueOf(ChangeType.class,v).getIntValue());
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the column "before" values as a String
    **/
    public String getColumnsBefore()
    {
        String v = (String)this.getFieldValue(FLD_columnsBefore);
        return StringTools.trim(v);
    }

    /**
    *** Sets the column "before" values as a String
    **/
    public void setColumnsBefore(String v)
    {
        this.setFieldValue(FLD_columnsBefore, StringTools.trim(v));
    }

    /**
    *** Sets the column "before" values as an RTProperties instance
    **/
    public void setColumnsBefore(RTProperties rtp)
    {
        this.setColumnsBefore((rtp != null)? rtp.toString() : "");
    }

    /**
    *** Sets the column "before" values as a DBRecord instance
    **/
    public void setColumnsBefore(DBRecord<?> rcd)
    {
        if (rcd != null) {
            this.setColumnsBefore(rcd.toRTProperties(null));
        } else {
            this.setColumnsBefore("");
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the column "after" values as a String
    **/
    public String getColumnsAfter()
    {
        String v = (String)this.getFieldValue(FLD_columnsAfter);
        return StringTools.trim(v);
    }

    /**
    *** Sets the column "after" values as a String
    **/
    public void setColumnsAfter(String v)
    {
        this.setFieldValue(FLD_columnsAfter, StringTools.trim(v));
    }

    /**
    *** Sets the column "after" values as an RTProperties instance
    **/
    public void setColumnsAfter(RTProperties rtp)
    {
        this.setColumnsAfter((rtp != null)? rtp.toString() : "");
    }

    /**
    *** Sets the column "after" values as a DBRecord instance
    **/
    public void setColumnsAfter(DBRecord<?> rcd)
    {
        if (rcd != null) {
            this.setColumnsAfter(rcd.toRTProperties(null));
        } else {
            this.setColumnsAfter("");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Insert table record change entry into DBAudit 
    **/
    private static boolean _addDBAudit(
        String accountID, String userID, long timestamp, 
        ChangeType chgType, DBRecord<?> rcdBef, DBRecord<?> rcdAft, Set<String> updFldSet)
    {

        /* find table name */
        String utableName;
        if (rcdBef != null) {
            utableName = rcdBef.getRecordKey().getUntranslatedTableName();
        } else
        if (rcdAft != null) {
            utableName = rcdAft.getRecordKey().getUntranslatedTableName();
        } else {
            utableName = null;
        }
        // -- no table name?
        if (StringTools.isBlank(utableName)) {
            Print.logError("TableName not specified");
            return false;
        }

        /* validate table name */
        try {
            DBFactory<? extends DBRecord<?>> tableFact = DBFactory.getFactoryByName(utableName);
            if (tableFact == null) {
                // -- table factory not found
                Print.logError("Table name not found: " + utableName);
                return false;
            } else
            if (!tableFact.isAuditTable()) {
                // -- do not audit this table
                return false; // quietly ignore
            } else
            if (!tableFact.tableExists()) { // throws DBException
                // -- table does not exist
                Print.logError("Table does not exist: " + utableName);
                return false;
            }
        } catch (DBException dbe) {
            Print.logException("Unable to determine table existance: " + utableName, dbe);
            return false;
        }

        /* validate change type with before/after records */
        if (chgType == null) {
            chgType = ChangeType.UNKNOWN;
        }
        switch (chgType) {
            case UNKNOWN :
                Print.logWarn("ChangeType 'UNKNOWN' specified!");
                updFldSet = null;
                break;
            case INSERT :
                if (rcdAft == null) {
                    if (rcdBef != null) {
                        Print.logWarn("ChangeType 'NEW': Moving record 'before' to 'after' ...");
                        rcdAft = rcdBef;
                        rcdBef = null;
                    } else {
                        Print.logWarn("ChangeType 'NEW': Record not specified!");
                    }
                } else
                if (rcdBef != null) {
                    Print.logWarn("ChangeType 'NEW': Record 'before' is specified (expecting only 'after')");
                }
                updFldSet = null;
                break;
            case UPDATE :
                if ((rcdBef == null) && (rcdAft == null)) {
                    Print.logWarn("ChangeType 'UPDATE': Record 'before'/'after' not specified!");
                } else
                if (rcdBef == null) {
                    Print.logWarn("ChangeType 'UPDATE': Record 'before' not specified!");
                } else
                if (rcdAft == null) {
                    Print.logWarn("ChangeType 'UPDATE': Record 'after' not specified!");
                }
                break;
            case DELETE :
                if (rcdBef == null) {
                    if (rcdAft != null) {
                        Print.logWarn("ChangeType 'DELETE': Moving record 'after' to 'before' ...");
                        rcdBef = rcdAft;
                        rcdAft = null;
                    } else {
                        Print.logWarn("ChangeType 'DELETE': Record not specified!");
                    }
                } else
                if (rcdAft != null) {
                    Print.logWarn("ChangeType 'DELETE': Record 'after' is specified (expecting only 'before')");
                }
                updFldSet = null;
                break;
        }

        /* adjust accountID/userID */
        accountID = StringTools.trim(accountID);
        userID    = StringTools.trim(userID);
        if (StringTools.isBlank(accountID)) {
            accountID = DBRecord.GetCurrentAccount();
            userID    = DBRecord.GetCurrentUser();
        }

        /* adjust timestamp */
        if (timestamp <= 0L) {
            timestamp = DateTime.getCurrentTimeSec();
        }

        /* get before/after records as RTPProperties */
        RTProperties rcdBefRTP = (rcdBef != null)? rcdBef.toRTProperties(null)      : null;
        RTProperties rcdAftRTP = (rcdAft != null)? rcdAft.toRTProperties(updFldSet) : null;

        /* save */
        try {
            // -- create key/record
            DBAudit.Key dbaKey = new DBAudit.Key(accountID,userID,utableName,timestamp);
            DBAudit     dba    = dbaKey._getDBRecord();
            dba.setCreationDefaultValues();
            // -- fill record
            dba.setChangeType(chgType);
            dba.setColumnsBefore(rcdBefRTP);
            dba.setColumnsAfter(rcdAftRTP);
            // -- insert
            dba.insert();
            return true;
        } catch (DBException dbe) {
            Print.logException("Unable to insert DBAudit for table: ["+accountID+"/"+userID+"] " + utableName, dbe);
            return false;
        }

    }

    // --------------------------------

    /**
    *** Add table record INSERT entry into DBAudit 
    **/
    public static boolean addDBAudit_insert(
        String accountID, String userID, long timestamp, 
        DBRecord<?> rcdAft)
    {
        // -- web audit only?
        if (WEB_AUDIT_ONLY && !RTConfig.isWebApp()) { // insert
            return false;
        }
        // -- record not specified?
        if (rcdAft == null) {
            //Print.logWarn("Insert DBRecord not specified");
            return false;
        }
        // -- audit enabled for this table?
        DBRecordKey<?> rcdKey = rcdAft.getRecordKey();
        if (rcdKey == null) {
            // -- unlikely
            Print.logError("Invalid insert DBRecordKey");
            return false;
        }
        DBFactory<?> dbFact = rcdKey.getFactory();
        if ((dbFact == null) || !dbFact.isAuditTable()) {
            // -- not enabled for this table
            return false;
        }
        // -- audit
        return DBAudit._addDBAudit(accountID, userID, timestamp, 
            ChangeType.INSERT, null, rcdAft, null);
    }

    // --------------------------------

    /**
    *** Add table record UPDATE entry into DBAudit 
    **/
    public static boolean addDBAudit_update(
        String accountID, String userID, long timestamp, 
        DBRecord<?> rcdAft, Set<String> updFldSet)
    {
        return DBAudit.addDBAudit_update(accountID, userID, timestamp, 
            null, rcdAft, updFldSet);
    }

    /**
    *** Add table record UPDATE entry into DBAudit 
    **/
    public static boolean addDBAudit_update(
        String accountID, String userID, long timestamp, 
        DBRecord<?> rcdBef, DBRecord<?> rcdAft, Set<String> updFldSet)
    {
        // -- web audit only?
        if (WEB_AUDIT_ONLY && !RTConfig.isWebApp()) { // update
            return false;
        }
        // -- 'After' record not specified?
        if (rcdAft == null) {
            //Print.logWarn("After-update DBRecord not specified");
            return false;
        }
        // -- audit enabled for this table?
        DBRecordKey<?> rcdKey = rcdAft.getRecordKey();
        if (rcdKey == null) {
            // -- unlikely
            Print.logError("Invalid after-update DBRecordKey");
            return false;
        }
        DBFactory<?> dbFact = rcdKey.getFactory();
        if ((dbFact == null) || !dbFact.isAuditTable()) {
            // -- not enabled for this table
            return false;
        }
        // -- 'Before' record specified?
        if (rcdBef == null) {
            // -- load the 'Before' record
            DBRecordKey<?> rcdKeyClone = (DBRecordKey<?>)rcdKey.clone();
            rcdBef = rcdKeyClone.getDBRecord();
            rcdBef.reload();
        }
        // -- audit
        return DBAudit._addDBAudit(accountID, userID, timestamp, 
            ChangeType.UPDATE, rcdBef, rcdAft, updFldSet);
    }

    // --------------------------------

    /**
    *** Add table record DELETE entry into DBAudit 
    **/
    public static boolean addDBAudit_delete(
        String accountID, String userID, long timestamp, 
        DBRecordKey<?> rcdKey)
    {
        // -- web audit only?
        if (WEB_AUDIT_ONLY && !RTConfig.isWebApp()) { // delete
            return false;
        }
        // -- recordKey not specified?
        if (rcdKey == null) {
            //Print.logWarn("DBRecordKey not specified");
            return false;
        }
        // -- no DBAudit for this table?
        DBFactory<?> dbFact = rcdKey.getFactory();
        if ((dbFact == null) || !dbFact.isAuditTable()) {
            return false;
        }
        // -- read prior/unchanged record
        DBRecordKey<?> rcdKeyClone = (DBRecordKey<?>)rcdKey.clone();
        DBRecord<?> rcdBef = rcdKeyClone.getDBRecord();
        rcdBef.reload();
        // -- audit
        return DBAudit._addDBAudit(accountID, userID, timestamp, 
            ChangeType.DELETE, rcdBef, null, null);
    }

    // ------------------------------------------------------------------------

}

