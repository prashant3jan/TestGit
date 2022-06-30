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
//  2006/04/11  Martin D. Flynn
//     -'toString(<FieldName>)' now returns a default value consistent with the
//      field type if the field has not yet been assigned an actual value.
//  2006/04/23  Martin D. Flynn
//     -Integrated logging changes made to Print
//  2007/01/25  Martin D. Flynn
//     -Integrated with "OpenGTS"
//  2007/09/16  Martin D. Flynn
//     -Fixed case where default Boolean valus were not properly converted to a 
//      String value in "toString(fldName)".
//  2008/02/04  Martin D. Flynn
//     -Fixed 'setChanged' method to properly pass the old field value.
//  2009/05/01  Martin D. Flynn
//     -Added DateTime datatype
//  2009/05/24  Martin D. Flynn
//     -Made "_setFieldValue(DBField fld, Object newVal)" public to allow direct
//      access to other modules.
//  2011/05/13  Martin D. Flynn
//     -Modified "setAllFieldValues" to accept a list of specific fields to set.
//  2012/04/11  Martin D. Flynn
//     -Added check for invalid Double/Float values to "toStringValue(...)"
//  2014/03/03  Martin D. Flynn
//     -Case insensitive check for "column" on missing columns.
//  2018/09/10  GTS Development Team
//     -Added "clone()" method.
//     -Added 'missingDelegate' support
//  2020/02/19  GTS Development Team
//     -Added String trucation to "_setFieldValue(...)" (see TRUNCATE) [2.6.7-B43p]
//     -Added support for separating MySQL read/write connections (DBReadWriteMode). [2.6.7-B45]
// ----------------------------------------------------------------------------
package org.opengts.dbtools;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.sql.*;

import org.opengts.util.*;

/**
*** <code>DBFieldValues</code> is a container class for field/column values for
*** a DBRecord.
**/

public class DBFieldValues
{

    // ------------------------------------------------------------------------

    private static boolean TRUNCATE_STRING_FIELDS = true;

    // ------------------------------------------------------------------------

    /* validate field values */
    private static boolean VALIDATE_FIELD_VALUES = false;
    
    /**
    *** Sets the global state for validating field values
    *** @param validate True to validate, false otherwise
    **/
    public static void setValidateFieldValues(boolean validate)
    {
        VALIDATE_FIELD_VALUES = validate;
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private volatile boolean                            didInit             = false;

    private volatile DBRecordKey<? extends DBRecord<?>> recordKey           = null;

    private String                                      name                = "";

    private OrderedMap<String,DBField>                  fieldMap            = new OrderedMap<String,DBField>();
    private OrderedMap<String,Object>                   valueMap            = new OrderedMap<String,Object>();
    private Map<String,String>                          caseMap             = new HashMap<String,String>();
    private boolean                                     isDBLoaded          = false;

    private DBFieldValues                               dataDelegate        = null;
    private DBFieldValues                               missingDelegate     = null;

    private boolean                                     mustExist           = true;

    /**
    *** Constructor
    **/
    public DBFieldValues()
    {
        // -- uninitialized
    }

    /**
    *** Constructor
    *** @param rcdKey  The DBRecordKey associated with this field value container
    **/
    public DBFieldValues(DBRecordKey<? extends DBRecord<?>> rcdKey)
        throws DBException
    {
        this.setRecordKey(rcdKey, null, null);
    }

    /**
    *** Constructor
    *** @param rcdKey  The DBRecordKey associated with this field value container
    **/
    public DBFieldValues(DBRecordKey<? extends DBRecord<?>> rcdKey, 
        DBFieldValues _dataDel, DBFieldValues _missingDel)
        throws DBException
    {
        this.setRecordKey(rcdKey, _dataDel, _missingDel);
    }

    // ------------------------------------------------------------------------
    // -- clone

    /** [2.6.6-B36]
    *** Creates a clone of this DBFieldValues instance.
    *** Note:
    ***     - The delegate feature is not cloned.  Fields will be statically defined.
    ***     - The internal record key will re unassigned
    ***     - 'recordKey' is not cloned
    ***     - 'dataDelegate' is not cloned
    ***     - 'missingDelegate' is not cloned
    **/
    public DBFieldValues clone() // [2.6.6-B36]
    {
        DBFieldValues dfv = new DBFieldValues(); // clone
        dfv.name      = this.name;
        dfv.mustExist = this.mustExist;
        for (String fldN : this.fieldMap.keySet()) {
            DBField fld = this.fieldMap.get(fldN); // PrimaryKey
            dfv.fieldMap.put(fldN, this.fieldMap.get(fldN));
            dfv.caseMap.put(fldN.toLowerCase(), fldN);
            if (this.hasFieldValue(fldN)) {
                dfv.valueMap.put(fldN, this.getFieldValue(fldN));
            }
        }
        return dfv;
    }

    /** [2.6.6-B36]
    *** Creates a clone of this DBFieldValues instance.
    *** Note:
    ***     - The field-delegate feature is not cloned.  Fields will be statically defined.
    ***     - The internal record key will re unassigned
    ***     - 'fieldDelegate' is not cloned
    **/
    public DBFieldValues clone(DBRecordKey<? extends DBRecord<?>> rcdKey) // [2.6.6-B36]
    {
        DBFieldValues fvClone = this.clone();
        fvClone.recordKey = rcdKey;
        return fvClone;
    }

    // ------------------------------------------------------------------------
    
    /**
    *** Initialize DBFieldValues (subclasses only)
    **/
    protected void init()
    {
        // -- override
    }
    
    /**
    *** Returns true if this instance already has a defined DBRecordKey
    **/
    public boolean hasRecordKey()
    {
        return (this.recordKey != null)? true : false;
    }

    /**
    *** Gets the DBRecordKey
    **/
    public DBRecordKey<? extends DBRecord<?>> getRecordKey()
    {
        if (!this.didInit) {
            synchronized (this) {
                if (!this.didInit) {
                    this.init();
                    this.didInit = true;
                }
            }
        }
        return this.recordKey;
    }

    protected void setRecordKey(DBRecordKey<? extends DBRecord<?>> rcdKey, 
        DBFieldValues _dataDel, DBFieldValues _missingDel)
        throws DBException
    {
        //mdf*/Print.logInfo("Setting DBRecordKey, table: " + rcdKey.getUntranslatedTableName());

        synchronized (this) {

            /* already initialized? */
            if (this.recordKey != null) {
                Print.logError("Already initialized");
                throw new DBException("Already initialized");
            }
    
            /* save record key */
            this.recordKey = rcdKey;
            if (rcdKey == null) {
                Print.logError("ERROR: Specified DBRecordKey is null!");
                throw new DBException("Specified DBRecordKey is null!");
            }
    
            /* '_dataDel' and '_missingDel' are mutually exclusive */
            if ((_dataDel != null) && (_missingDel != null)) {
                Print.logStackTrace("ERROR: '_dataDelegate' and '_missingDelegate' are mutually exclusive!");
                // -- '_dataDel' will be used below
            }
    
            /* get fields (key fields only, if delegate is specified) */
            // -- "Data-Field-Delegate":
            // -    This instance is a special case for allowing key fields to be updated
            // -    see also "DBField.AllowUpdateKeyFields()" and "DBRecordKey#getKeyValues()"
            // -- "Missing-Field-Delegate":
            // -    This instance is used as a normal store for both keys and data
            // -    values.  If a field is not found, the missing-delegate will be used
            // -    to store the ancillary field values
            //DBField fld[] = (_dataDel == null)? 
            //    rcdKey.getFields()    : // no field delegate, all fields
            //    rcdKey.getKeyFields();  // field delegate, keys only
            DBField fld[];
            if (_dataDel != null) {
                // -- Data-Field-Delegate, keys only
                fld = rcdKey.getKeyFields();
                this.dataDelegate    = _dataDel;
                this.missingDelegate = null;
            } else
            if (_missingDel != null) {
                // -- Missing-Field-Delegate, missing fields
                if (rcdKey.hasMissingFields()) {
                    //Print.logInfo("MissingDelegate specified, has missing fields: " + rcdKey.getUntranslatedTableName());
                    // -- has missing fields, use missing-delegate
                    fld = rcdKey.getExistingFields(); // defined/existing/remaining fields
                    this.dataDelegate    = null;
                    this.missingDelegate = _missingDel;
                } else {
                    //Print.logInfo("MissingDelegate specified, but no missing fields: " + rcdKey.getUntranslatedTableName());
                    // -- no missing fields, missing-delegate not necessary, all fields
                    fld = rcdKey.getFields(); // all fields
                    this.dataDelegate    = null;
                    this.missingDelegate = null;
                }
            } else {
                // -- no field-delegate, all fields
                fld = rcdKey.getFields();
                this.dataDelegate    = null;
                this.missingDelegate = null;
            }
    
            /* add fields to this instance */
            for (int i = 0; i < fld.length; i++) {
                String fldName = DBProvider.translateColumnName(fld[i].getName());
                this.fieldMap.put(fldName, fld[i]);
                this.caseMap.put(fldName.toLowerCase(), fldName);
            }
            
        }

        /* initialized */

    }

    // ------------------------------------------------------------------------

    /**
    *** Loads this table, if not already loaded
    **/
    private void _loadRecord(DBReadWriteMode rwMode)
        throws DBException
    {
        if (!this.isDBLoaded()) {
            DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey();
            if (rcdKey != null) {
                rcdKey._getDBRecord(true, rwMode);
            }
        } else {
            // -- already loaded
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the name of this instance
    *** @return The name of this instance
    **/
    public String getName()
    {
        return this.name;
    }

    /**
    *** Sets the name of this instance
    *** @param name  The name of this instance
    **/
    public void setName(String name)
    {
        this.name = StringTools.trim(name);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the state for ignoring invalid field names.  True to ignore errors when
    *** setting/getting a field name that does not exist, False to emit any invalid
    *** field errors.
    *** @param state  True to ignore invalid field names, false to emit errors.
    **/
    public void setIgnoreInvalidFields(boolean state)
    {
        this.mustExist = !state;
    }
    
    /**
    *** Gets the state of reporting invalid field names.
    *** @return False to report invalid field names, true to suppress/ignore errors.
    **/
    public boolean getIgnoreInvalidFields()
    {
        return !this.mustExist;
    }
        
    // ------------------------------------------------------------------------

    /**
    *** Returns true if this instance has defined a missing-delegate
    **/
    public boolean hasMissingDelegate()
    {
        return (this.missingDelegate != null)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the table name for this DBFieldValue instance
    *** @return The table name
    **/
    public String getUntranslatedTableName()
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // n/a
        if (rcdKey != null) {
            return rcdKey.getUntranslatedTableName();
        } else {
            return "";
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Clears all field values
    **/
    public void clearFieldValues()
    {
        this.clearFieldValues(null);
    }

    /**
    *** Clears field values
    **/
    public void clearFieldValues(DBField fldList[])
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // write
        if (rcdKey != null) {
            DBField fld[] = (fldList != null)? fldList : rcdKey.getFields();
            for (int i = 0; i < fld.length; i++) {
                if (!fld[i].isPrimaryKey()) {
                    this._setFieldValue(fld[i], (Object)null);
                }
            }
            this.isDBLoaded = false;
        } else {
            Print.logStackTrace("DBRecordKey has not been set!");
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if either 'setAllFieldValues' or 'setFieldValues' has been called
    *** and at least one field has been updated
    **/
    public boolean isDBLoaded()
    {
        return this.isDBLoaded;
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the DBField for the specified field name
    *** @param fldName  The field name of the DBField to return
    *** @return  The returned DBField
    **/
    public DBField getField(String fldName)
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // read
        if (rcdKey != null) {
            return rcdKey.getField(fldName);
        } else {
            Print.logStackTrace("DBRecordKey has not been set!");
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the table name for this DBFieldValue instance
    *** @return The table name
    **/
    public boolean isMissingField(String fldName)
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // n/a
        if (rcdKey != null) {
            return rcdKey.isMissingField(fldName);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the value for the specified field name
    *** @param fldName   The field name to set
    *** @param requiredField  True to indicate that this field is required (warnings displayed if field is not found)
    *** @param newVal    The 'Object' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    protected boolean _setFieldValue(String fldName, boolean requiredField, Object newVal) 
    {

        /* get/validate field */
        DBField fld = this.getField(fldName);
        if (fld == null) {
            // -- field does not exist, defer to delegate
            if (this.dataDelegate != null) {
                return this.dataDelegate._setFieldValue(fldName, requiredField, newVal);
            } else
            if ((this.missingDelegate != null) && this.isMissingField(fldName)) {
                try {
                    this.missingDelegate._loadRecord(DBReadWriteMode.READ_WRITE); // write
                    return this.missingDelegate._setFieldValue(fldName, requiredField, newVal); // ==> "_setFieldValue(fld, newVal)"
                } catch (DBException dbe) {
                    Print.logError("Error loading/setting missing field to delegate: " + dbe);
                    // -- continue below ...
                }
            }
            // -- not found, and no delegate
            if (requiredField && !this.getIgnoreInvalidFields()) {
                String tn = this.getUntranslatedTableName();
                Print.logError("Field does not exist: " + tn + "." + fldName);
            }
            return false;
        }

        /* set field value */
        return this._setFieldValue(fld, newVal);

    }

    /**
    *** Sets the value for the specified field name
    *** @param fld       The DBField to set
    *** @param newVal    The 'Object' value to set for the field
    *** @return True if 'newVal' is proper field type, false otherwise
    **/
    public boolean _setFieldValue(DBField fld, Object newVal) 
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // write

        /* validate Java type */
        if (newVal != null) {
            //if (this.getName().startsWith("EventDataExtra")) Print.logInfo("Setting field ["+this.getName()+"]: "+fld.getName()+" ==> " + newVal);
            /* check Java types */
            Class<?> fldTypeClass = fld.getTypeClass();
            if (newVal.getClass() == fldTypeClass) {
                // -- ok
            } else
            if ((newVal instanceof Boolean) && fld.isTypeBoolean()) {
                // -- ok
            } else
            if ((newVal instanceof Integer) && fld.isTypeInteger()) {
                // -- ok
            } else
            if ((newVal instanceof Long) && fld.isTypeLong()) {
                // -- ok
            } else
            if ((newVal instanceof Float) && fld.isTypeFloat()) {
                // -- ok
            } else
            if ((newVal instanceof Double) && fld.isTypeDouble()) {
                // -- ok
            } else
            if ((newVal instanceof byte[]) && fld.isTypeBLOB()) {
                // -- ok
            } else
            if ((newVal instanceof DateTime) && fld.isTypeDateTime()) {
                // -- ok
            } else {
                Print.logStackTrace("Invalid type["+fld.getName()+"]:" + 
                    " found '" + StringTools.className(newVal) + "'" +
                    ", expected '"+StringTools.className(fldTypeClass)+"'");
                if (String.class.isAssignableFrom(fldTypeClass)) {
                    // -- attempt to convert data type to String
                    newVal = newVal.toString();
                    Print.logWarn("Converted to data type: " + StringTools.className(newVal));
                } else
                if (Number.class.isAssignableFrom(fldTypeClass) && (newVal instanceof Number)) {
                    // -- attempt to convert data type to Number type
                    if (Byte.class.equals(fldTypeClass)) {
                        newVal = new Byte(((Number)newVal).byteValue());
                    } else
                    if (Short.class.equals(fldTypeClass)) {
                        newVal = new Short(((Number)newVal).shortValue());
                    } else
                    if (Integer.class.equals(fldTypeClass)) {
                        newVal = new Integer(((Number)newVal).intValue());
                    } else
                    if (Long.class.equals(fldTypeClass)) {
                        newVal = new Long(((Number)newVal).longValue());
                    } else
                    if (Float.class.equals(fldTypeClass)) {
                        newVal = new Float(((Number)newVal).floatValue());
                    } else
                    if (Double.class.equals(fldTypeClass)) {
                        newVal = new Double(((Number)newVal).doubleValue());
                    } else {
                        // -- conversion not supported
                        return false;
                    }
                    Print.logWarn("Converted to data type: " + StringTools.className(newVal));
                } else {
                    // -- unable to convert
                    return false;
                }
            }
        } else {
            // -- clearFieldValues: 'newVal' is null
        }

        /* truncate String type? */
        if (TRUNCATE_STRING_FIELDS && (newVal instanceof String) && fld.isTypeString()) {
            int maxLen = fld.getStringLength();
            if ((maxLen > 0) && ((String)newVal).length() > maxLen) {
                String utblN = this.getUntranslatedTableName();
                String   _nv = (String)newVal;
                String  fldN = fld.getName();
                newVal       = _nv.substring(0,maxLen); // TRUNCATE
                String trunc = _nv.substring(maxLen);
              //Print.logWarn("Truncated["+utblN+"."+fldN+":"+maxLen+"]: "+newVal+"{"+trunc+"}");
              //Print.logStackTrace("Truncated["+utblN+"."+fldN+":"+maxLen+"]: "+newVal+"{"+trunc+"}");
            }
        }

        /* store value */
        String fldName = fld.getName();
        Object oldVal  = null;
        if (this.dataDelegate != null) {
            //this.valueMap.put(fldName, newVal); // 'newVal' may be null
            return this.dataDelegate._setFieldValue(fld, newVal); // 'newVal' may be null
        } else
        if ((this.missingDelegate != null) && this.isMissingField(fldName)) {
            try {
                if (newVal != null) {
                    // -- only load if not setting value to null (ie. "clearFieldValues")
                    this.missingDelegate._loadRecord(DBReadWriteMode.READ_WRITE); // write
                }
                return this.missingDelegate._setFieldValue(fld, newVal);
            } catch (DBException dbe) {
                Print.logError("Error loading/setting missing field to delegate: " + dbe);
                return false;
            }
        } else {
            oldVal = this._getFieldValue(fldName, false/*required?*/);
            this.valueMap.put(fldName, newVal); // 'newVal' may be null
        }

        /* update DBRecord changed flag */
        DBRecord<?> rcd = (rcdKey != null)? rcdKey._getDBRecordVar() : null;
        if (rcd != null) {
            rcd.setChanged(fldName, oldVal, newVal); // 'newVal' may be null
        } else
        if ((newVal != null) && !fld.isKeyField()) { // [2.6.6-B36] added check for null 'newVal'
            // -- should not be setting a non-key field if there is no associated DBRecord
            Print.logStackTrace("DBRecordKey does not point to a DBRecord! ...");
        }
        return true;

    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName   The field name to set
    *** @param newVal    The 'Object' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, Object newVal) 
    {
        return this._setFieldValue(fldName, false, newVal);
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName   The field name to set
    *** @param newVal    The 'Object' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, Object newVal) 
    {
        return this._setFieldValue(fldName, true, newVal);
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'String' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, String val) 
    {
        return this._setFieldValue(fldName, false, (Object)StringTools.trim(val));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'String' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, String val) 
    {
        return this._setFieldValue(fldName, true, (Object)StringTools.trim(val));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'int' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, int val) 
    {
        return this._setFieldValue(fldName, false, (Object)(new Integer(val)));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'int' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, int val) 
    {
        return this._setFieldValue(fldName, true, (Object)(new Integer(val)));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'long' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, long val) 
    {
        return this._setFieldValue(fldName, false, (Object)(new Long(val)));
    }
          /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'long' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, long val) 
    {
        return this._setFieldValue(fldName, true, (Object)(new Long(val)));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'float' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, float val) 
    {
        return this._setFieldValue(fldName, false, (Object)(new Float(val)));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'float' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, float val) 
    {
        return this._setFieldValue(fldName, true, (Object)(new Float(val)));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'double' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, double val) 
    {
        return this._setFieldValue(fldName, false, (Object)(new Double(val)));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'double' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, double val) 
    {
        return this._setFieldValue(fldName, true, (Object)(new Double(val)));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'boolean' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, boolean val) 
    {
        return this._setFieldValue(fldName, false, (Object)(new Boolean(val)));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'boolean' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, boolean val) 
    {
        return this._setFieldValue(fldName, true, (Object)(new Boolean(val)));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'byte[]' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, byte val[])
    {
        return this._setFieldValue(fldName, false, (Object)((val != null)? val : new byte[0]));
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'byte[]' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, byte val[])
    {
        return this._setFieldValue(fldName, true, (Object)((val != null)? val : new byte[0]));
    }

    /**
    *** Sets the value for the specified optional field name
    *** @param fldName  The field name to set
    *** @param val      The 'DateTime' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setOptionalFieldValue(String fldName, DateTime val) 
    {
        return this._setFieldValue(fldName, false, (Object)val);
    }

    /**
    *** Sets the value for the specified field name
    *** @param fldName  The field name to set
    *** @param val      The 'boolean' value to set for the field
    *** @return True if the field exists, false otherwise
    **/
    public boolean setFieldValue(String fldName, DateTime val) 
    {
        return this._setFieldValue(fldName, true, (Object)val);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets all field values from the specified ResultSet
    *** @param rs  The ResultSet from which field values are retrieved
    *** @throws SQLException If field does not exist
    **/
    public void setAllFieldValues(ResultSet rs) 
        throws SQLException
    {
        this.setAllFieldValues(rs, true, null);
    }
    
    /**
    *** Sets all field values from the specified ResultSet
    *** @param rs  The ResultSet from which field values are retrieved
    *** @param setPrimaryKey  True to set primay key fields
    *** @throws SQLException If field does not exist
    **/
    public void setAllFieldValues(ResultSet rs, boolean setPrimaryKey) 
        throws SQLException
    {
        this.setAllFieldValues(rs, setPrimaryKey, null);
    }

    /**
    *** Sets all field values from the specified ResultSet
    *** @param rs  The ResultSet from which field values are retrieved
    *** @param setPrimaryKey  True to set primay key fields
    *** @throws SQLException If field does not exist
    **/
    public void setAllFieldValues(ResultSet rs, boolean setPrimaryKey, DBField fldList[]) 
        throws SQLException
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // write
        if (rs == null) {
            // -- quietly ignore
        } else
        if (rcdKey != null) {
            String utableName = this.getUntranslatedTableName();
            DBField fld[] = (fldList != null)? fldList : rcdKey.getFields();
            boolean logMissingCols = rcdKey.getFactory().logMissingColumnWarning();
            for (int i = 0; i < fld.length; i++) {
                if (setPrimaryKey || !fld[i].isPrimaryKey()) {
                    try {
                        Object val = fld[i].getResultSetValue(rs); // may throw exception if field does not exist
                        this._setFieldValue(fld[i], val);
                        this.isDBLoaded = true;
                    } catch (SQLException sqe) {
                        // -- we want to ignore "Column 'xxxx' not found" errors [found: SQLState:S0022;ErrorCode:0]
                        int errCode = sqe.getErrorCode(); // in the test we performed, this was '0' (thus useless)
                        String errMsg = sqe.getMessage();
                        String tblColName = utableName + "." + fld[i].getName();
                        if (errCode == DBFactory.SQLERR_UNKNOWN_COLUMN) {
                            // -- this is the errorCode that is supposed to be returned
                            long errCnt = fld[i].incrementErrorCount();
                            if ((errCnt % 120L) == 1L) {
                                Print.logException("Unknown Column: '" + tblColName + "'", sqe);
                            }
                        } else
                        if (errMsg.toLowerCase().indexOf("column") >= 0) {
                            // -- if it says anything about the "Column"
                            // -  IE: "Column 'batteryVolts' not found."
                            // -  IE: "The column name batteryVolts is not valid"
                            if (DBField.IgnoreColumnError(utableName, fld[i].getName())) {
                                // -- ignore errors
                                // -  db.ignoreColumnError.Device.driverStatus=true
                            } else
                            if (RTConfig.isDebugMode() && logMissingCols) {
                                Print.logError("Column '" +  tblColName + "'? ["+errCode+"] " + sqe);
                            } else {
                                long errCnt = fld[i].incrementErrorCount();
                                if (((errCnt % 120L) == 1L) && logMissingCols) {
                                    Print.logError("Column '" +  tblColName + "'? ["+errCode+"] " + sqe);
                                }
                            }
                        } else {
                            throw sqe;
                        }
                    }
                }
            }
        } else {
            Print.logStackTrace("DBRecordKey has not been set!");
        }
    }

    /**
    *** Sets all field values from the specified value map (all fields required)
    *** @param valMap  The Field==>Value map
    *** @throws DBException If field does not exist
    **/
    public void setAllFieldValues(Map<String,String> valMap) 
        throws DBException
    {
        this.setAllFieldValues(valMap, true/*setPrimaryKey*/);
    }

    /**
    *** Sets all field values from the specified value map (all fields required)
    *** @param valMap  The Field==>Value map
    *** @param setPrimaryKey True if key fields should also be set
    *** @throws DBException If field does not exist
    **/
    public void setAllFieldValues(Map<String,String> valMap, boolean setPrimaryKey) 
        throws DBException
    {
        this.setFieldValues(valMap, setPrimaryKey, true/*requireAllFields*/);
    }

    /**
    *** Sets the specified field values from the specified ResultSet.
    *** (NOTE: field names specified in the value map, which do not exist in the 
    *** actual field list, are quietly ignored).
    *** @param valMap  The Field==>Value map
    *** @param setPrimaryKey True if key fields should also be set
    *** @throws DBException If field does not exist
    **/
    public void setFieldValues(Map<String,String> valMap, boolean setPrimaryKey, boolean requireAllFields) 
        throws DBException
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // write
        if (rcdKey != null) {
            String utableName = this.getUntranslatedTableName();
            DBField fld[] = rcdKey.getFields();
            for (int i = 0; i < fld.length; i++) {
                String  name  = fld[i].getName();
                String  val   = valMap.get(name); // may be defined, but null
                if (fld[i].isPrimaryKey()) {
                    if (setPrimaryKey) {
                        if (val != null) {
                            //Print.logInfo("Setting Key Field: " + name + " ==> " + val);
                            Object v = fld[i].parseStringValue(val);
                            this._setFieldValue(fld[i], v);
                            this.isDBLoaded = true;
                        } else {
                            throw new DBException("Setting Key Field Values: value not defined for field - " + name);
                        }
                    }
                } else {
                    if (val != null) {
                        //Print.logInfo("Setting Field: " + name + " ==> " + val);
                        Object v = fld[i].parseStringValue(val);
                        this._setFieldValue(fld[i], v);
                        this.isDBLoaded = true;
                    } else
                    if (requireAllFields) {
                        //throw new DBException("Setting Field Values: value not defined for field - " + name);
                        Print.logError("Column '" + utableName + "." + name + "' value not specified.");
                    }
                }
            }
        } else {
            Print.logStackTrace("DBRecordKey has not been set!");
        }
    }

    /**
    *** Sets the specified field values from the specified ResultSet.
    *** (NOTE: field names specified in the value map, which do not exist in the 
    *** actual field list, are quietly ignored).
    *** @param fldVals  The Field==>Value map
    *** @param setPrimaryKey True if primary key fields should also be set
    *** @throws DBException If field does not exist
    **/
    public void setFieldValues(DBFieldValues fldVals, boolean setPrimaryKey, boolean requireAllFields) 
        throws DBException
    {
        DBRecordKey<? extends DBRecord<?>> rcdKey = this.getRecordKey(); // write
        if ((rcdKey != null) && (fldVals != null)) {
            String utableName = this.getUntranslatedTableName();
            DBField fld[] = rcdKey.getFields();
            for (int i = 0; i < fld.length; i++) {
                String  name  = fld[i].getName();
                Object  val   = fldVals.getOptionalFieldValue(name);
                if (fld[i].isPrimaryKey()) {
                    if (setPrimaryKey) {
                        if (val != null) {
                            //Print.logInfo("Setting Key Field: " + name + " ==> " + val);
                            this._setFieldValue(fld[i], val);
                            this.isDBLoaded = true;
                        } else {
                            throw new DBException("Setting Key Field Values: value not defined for field - " + name);
                        }
                    }
                } else {
                    if (val != null) {
                        //Print.logInfo("Setting Field: " + name + " ==> " + val);
                        this._setFieldValue(fld[i], val);
                        this.isDBLoaded = true;
                    } else
                    if (requireAllFields) {
                        //throw new DBException("Setting Field Values: value not defined for field - " + name);
                        Print.logError("Column '" + utableName + "." + name + "' value not specified.");
                    }
                }
            }
        } else {
            Print.logStackTrace("DBRecordKey has not been set!");
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Converts the field name to the proper case
    *** @param fldName  The case-insensitive field name
    *** @return  The field name in proper case.
    **/
    public String getFieldName(String fldName)
    {
        if (fldName != null) {
            return this.caseMap.get(fldName.toLowerCase());
        } else {
            return null;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified field name exists in this DBFieldValues instance
    *** @param fldName  The field name to test
    *** @return True if the specified field name exists in this DBFieldValues instance
    **/
    public boolean hasField(String fldName)
    {
        if (fldName == null) {
            return false;
        } else {
            String fn = DBProvider.translateColumnName(fldName);
            return this.fieldMap.containsKey(fn);
        }
    }

    /**
    *** Returns true if a value has been set for the specified field name
    *** @param fldName  The field name to test for a set value
    *** @return True if ta value has been set for the specified field name
    **/
    public boolean hasFieldValue(String fldName)
    {
        // -- if true, the field, and its value, are defined
        if (fldName == null) {
            // -- no field name, no field value
            return false;
        } else
        if (this.valueMap.containsKey(fldName)) {
            // -- found in this value map
            return true;
        } else
        if (this.dataDelegate != null) {
            // -- defer to delegate
            return this.dataDelegate.hasFieldValue(fldName);
        } else
        if (this.missingDelegate != null) {
            // -- defer to delegate
            return this.missingDelegate.hasFieldValue(fldName);
        } else {
            // -- not found, no delegate
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the value for the specified field name
    *** @param fldName  The field name for the value retrieved
    *** @param requiredField  True to indicate that this field is required (warnings displayed if field is not found)
    *** @return The field value
    **/
    protected Object _getFieldValue(String fldName, boolean requiredField) 
    {

        /* no field name, no value */
        if (fldName == null) {
            return null;
        }

        /* get value, return if found */
        Object val = this.valueMap.get(fldName); // this.valueMap.containsKey(fldName)?
        if (val != null) {
            // -- field value found, and non-null
            //Print.logInfo("Found field ["+this.getRecordKey().getUntranslatedTableName()+"]: " + fldName + " ==> " + val);
            return val;
        }
        boolean isFieldDefined = this.hasField(fldName);

        /* no value found (or it is null), defer to delegate */
        if (this.dataDelegate != null) {
            return this.dataDelegate._getFieldValue(fldName, requiredField);
        } else 
        if ((this.missingDelegate != null) && this.isMissingField(fldName)) {
            //Print.logInfo("Field is missing: " + fldName);
            try {
                this.missingDelegate._loadRecord(DBReadWriteMode.READ_WRITE); // read
                return this.missingDelegate._getFieldValue(fldName, requiredField);
            } catch (DBException dbe) {
                Print.logError("Error loading/getting missing field from delegate: " + dbe);
                // -- continue below ...
            }
        }

        /* not found, no delegate */
        if (isFieldDefined) {
            // -- field name found, but value is null (which may be the case if the value was undefined/not-set)
            return null;
        } else
        if (requiredField && !this.getIgnoreInvalidFields()) {
            String utableName = this.getUntranslatedTableName();
            Print.logStackTrace("("+this.getName() + ") Field not found: " + utableName + "." + fldName);
            return null;
        } else {
            return null;
        }

    }

    /**
    *** Gets the value for the specified field name
    *** @param fldName        The field name for the value retrieved
    *** @param requiredField  True to indicate that this field is required (warnings displayed if field is not found)
    *** @param rtnDft         True to return a default value if a value has not been set
    *** @return The field value
    **/
    protected Object _getFieldValue(String fldName, boolean requiredField, boolean rtnDft) 
    {

        /* get field value */
        Object obj = this._getFieldValue(fldName, requiredField);

        /* create default? */
        if ((obj == null) && rtnDft) {
            // return a default value consistent with the field type
            DBField fld = this.getField(fldName);
            if (fld != null) {
                obj = fld.getDefaultValue();
                if (obj == null) {
                    // Implementation error, this should never occur
                    Print.logStackTrace("Field doesn't support a default value: " + fldName);
                    return null;
                }
            } else {
                // Implementation error, this should never occur
                // If we're here, the field doesn't exist.
                return null;
            }
        }

        /* return object */
        return obj;
        
    }

    /**
    *** Gets the value for the specified optional field name
    *** @param fldName  The field name for the value retrieved
    *** @return The optional field value, or null if the field has not been set, or does not exist
    **/
    public Object getOptionalFieldValue(String fldName) 
    {
        return this._getFieldValue(fldName, false, false);
    }

    /**
    *** Gets the value for the specified optional field name
    *** @param fldName  The field name for the value retrieved
    *** @return The optional field value, or null if the field has not been set, or does not exist
    **/
    public Object getOptionalFieldValue(String fldName, boolean rtnDft) 
    {
        return this._getFieldValue(fldName, false, rtnDft);
    }

    /**
    *** Gets the value for the specified field name
    *** @param fldName  The field name for the value retrieved
    *** @return The field value
    **/
    public Object getFieldValue(String fldName) 
    {
        return this._getFieldValue(fldName, true, false);
    }

    /**
    *** Gets the value for the specified field name
    *** @param fldName  The field name for the value retrieved
    *** @param rtnDft   True to return a default value if a value has not been set
    *** @return The field value
    **/
    public Object getFieldValue(String fldName, boolean rtnDft) 
    {
        return this._getFieldValue(fldName, true, rtnDft);
    }

    /**
    *** Gets the String representation of the field value
    *** @param fldName  The field name for the value retrieved
    *** @return The String representation of the field value
    **/
    public String getFieldValueAsString(String fldName) 
    {
        Object val = this.getFieldValue(fldName, true);
        if (val instanceof Number) {
            DBField fld = this.getField(fldName);
            if (fld != null) {
                String fmt = fld.getFormat();
                if ((fmt != null) && fmt.startsWith("X")) { // hex
                    // format as hex (Byte/Integer/Long/Short only)
                    return fld.formatValue(val);
                }
            }
        }
        return DBFieldValues.toStringValue(val);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a string representation of this instance
    **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String utableName = this.getUntranslatedTableName();
        sb.append(this.getName()).append(" ");
        sb.append("[").append(utableName).append("]");
        for (String fld : this.valueMap.keySet()) {
            Object val = this.valueMap.get(fld);
            sb.append(" ");
            sb.append(fld).append("=").append(StringTools.trim(val));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Converts the specified object to a String representation
    *** @param obj The Object to converts to a String representation
    *** @return The String representation of the specified object
    **/
    public static String toStringValue(Object obj) 
    {

        /* null? */
        if (obj == null) {
            return "";
        }

        /* DBFieldType? */
        if (obj instanceof DBFieldType) {
            obj = ((DBFieldType)obj).getObject();
            if (obj == null) {
                return "";
            }
        }

        /* convert to String */
        if (obj instanceof String) {
            return ((String)obj).trim();
        } else
        if (obj instanceof Double) {
            Double N = (Double)obj;
            if (N.isNaN()) {
                // should not occur
                Print.logWarn("Invalid Double value: " + N);
                return "0.0";
            } else
            if (N.isInfinite()) {
                // should not occur
                Print.logWarn("Invalid Double value: " + N);
                return (new Double((N>=0.0)?Double.MAX_VALUE:-Double.MAX_VALUE)).toString();
            } else {
                return N.toString();
            }
        } else
        if (obj instanceof Float) {
            Float N = (Float)obj;
            if (N.isNaN()) {
                // should not occur
                Print.logWarn("Invalid Float value: " + N);
                return "0.0";
            } else
            if (N.isInfinite()) {
                // should not occur
                Print.logWarn("Invalid Float value: " + N);
                return (new Float((N>=0.0)?Float.MAX_VALUE:-Float.MAX_VALUE)).toString();
            } else {
                return N.toString();
            }
        } else
        if (obj instanceof Number) { // non-float (Byte/Short/Integer/Long)
            return obj.toString();
        } else
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue()? "1" : "0";
        } else
        if (obj instanceof byte[]) {
            String hex = StringTools.toHexString((byte[])obj);
            return "0x" + hex;
        } else
        if (obj instanceof DateTime) {
            DateTime dt = (DateTime)obj;
            return dt.format("yyyy-MM-dd HH:mm:ss", DateTime.getGMTTimeZone());
        } else {
            Print.logWarn("Converting object to string: " + StringTools.className(obj));
            return obj.toString();
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
