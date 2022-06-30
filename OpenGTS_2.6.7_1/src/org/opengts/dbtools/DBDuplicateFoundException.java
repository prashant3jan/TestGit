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
// ----------------------------------------------------------------------------
package org.opengts.dbtools;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.opengts.util.*;

/**
*** <code>DBDuplicateFoundException</code> is thrown in cases where a single record was expected,
*** but multiple occurances of the requested record was found.
**/

public class DBDuplicateFoundException
    extends DBException
{

    // ----------------------------------------------------------------------------

    private DBRecord<?>  dbRcds[]   = null;
    private String       keyFldName = null;
    private String       lookupKey  = null;

    /**
    *** Constructor
    *** @param msg  The message associated with this exception
    **/
    public DBDuplicateFoundException(String msg)
    {
        super(msg);
    }

    /**
    *** Constructor
    *** @param msg   The message associated with this exception
    *** @param cause The cause of this exception
    **/
    public DBDuplicateFoundException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
    *** Constructor
    *** @param msg         The message associated with this exception
    *** @param rcds        Array of the records with duplicate keys
    *** @param keyFldName  The key field name (ie. alternate key)
    *** @param lookupKey   The key lookup value
    **/
    public DBDuplicateFoundException(String msg, 
        DBRecord<?> rcds[],
        String keyFldName, String lookupKey)
    {
        super(msg);
        this.setDuplicateRecords(rcds);
        this.keyFldName = keyFldName;
        this.lookupKey  = lookupKey;
    }

    /**
    *** Constructor
    *** @param msg   The message associated with this exception
    *** @param rcds  Array of the records with duplicate keys
    **/
    public DBDuplicateFoundException(String msg, 
        DBRecord<?> rcds[])
    {
        this(msg,rcds,null,null);
    }

    // ----------------------------------------------------------------------------

    /**
    *** Sets the list/array of duplicate matching DBRecords
    **/
    public DBDuplicateFoundException setDuplicateRecords(DBRecord rcds[])
    {
        this.dbRcds = rcds; // may be null
        return this;
    }

    /**
    *** Gets the list/array of duplicate matching DBRecords
    **/
    public DBRecord[] getDuplicateRecords()
    {
        return this.dbRcds; // may be null
    }

    /**
    *** Gets the list/array of duplicate matching DBRecords
    **/
    public DBRecord getFirstRecord()
    {
        return !ListTools.isEmpty(this.dbRcds)? this.dbRcds[0] : null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the key field name.
    *** Will be null if not specified.
    **/
    public String getKeyFieldName()
    {
        return this.keyFldName;
    }

    /**
    *** Gets the lookup key value.
    *** Will be null if not specified.
    **/
    public String getLookupKeyValue()
    {
        return this.lookupKey;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a String representation of the primary keys 
    **/
    public String getRecordKeysString()
    {
        return this.getRecordKeysString(", ", "/");
    }

    /**
    *** Gets a String representation of the primary keys.
    *** @param rcdSep  The String used to separate primary key groups.
    *** @param keySep  The String used to separate primary keys in a group.
    **/
    public String getRecordKeysString(String rcdSep, String keySep)
    {
        // -- have dbRecords?
        if (ListTools.isEmpty(this.dbRcds)) {
            return "";
        }
        // -- get DBFactory
        DBFactory dbFact = DBRecord.getFactory(this.dbRcds[0]);
        if (dbFact == null) {
            // -- should only occur if this.dbRcds[0] is null
            return "";
        }
        // -- primary key fields
        DBField priKeyFlds[] = dbFact.getKeyFields(); // primary keys
        if (ListTools.isEmpty(priKeyFlds)) {
            // -- no primary keys?
            return "";
        }
        // -- assemble list of duplicate key fields
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.dbRcds.length; i++) {
            if (i > 0) { sb.append((rcdSep!=null)?rcdSep:", "); }
            DBRecordKey rcdKey = this.dbRcds[i].getRecordKey();
            sb.append(rcdKey.toString(((keySep!=null)?keySep:"/"),"null"));
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------------------

}
