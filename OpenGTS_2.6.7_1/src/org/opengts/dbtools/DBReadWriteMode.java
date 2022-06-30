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
package org.opengts.dbtools;

import org.opengts.util.*;

public enum DBReadWriteMode 
{

    // ------------------------------------------------------------------------
    // -- "enum" does not allow static vars to be placed before the enum-values
    // ------------------------------------------------------------------------

 // enum          isRead, isWrite, keySuffix description
    READ_ONLY   ( true  , false  , "%read" , "ReadOnly"  ), // READ_ONLY
    READ_WRITE  ( true  , true   , "%write", "ReadWrite" ), // READ_WRITE
    DELETE      ( false , true   , "%write", "Delete"    ), // READ_WRITE
    ALTER       ( false , true   , "%write", "Alter"     ), // READ_WRITE
    GRANT       ( false , true   , "%write", "Grant"     ); // READ_WRITE

    // ------------------------------------------------------------------------

    public static final String RW_PREFIX_CHAR   = "%";

    // ------------------------------------------------------------------------

    /**
    *** Returns the specified DBReadWriteMode if non-null,
    *** otherwise returns 'DBReadWriteMode.READ_WRITE'
    *** Does not return null.
    **/
    public static DBReadWriteMode getDefaultReadWriteMode(DBReadWriteMode rwMode)
    {
        return (rwMode != null)? rwMode : DBReadWriteMode.READ_WRITE;
    }

    // ------------------------------------------------------------------------

    /** 
    *** Gets the ReadWriteMode for the specified name
    **/
    public static DBReadWriteMode getReadWriteMode(String m)
    {
        DBReadWriteMode rwMode = DBReadWriteMode.getReadWriteMode(m, null);
        return DBReadWriteMode.getDefaultReadWriteMode(rwMode);
    }

    /**
    *** Gets the ReadWriteMode for the specified name
    **/
    public static DBReadWriteMode getReadWriteMode(String m, DBReadWriteMode dftMode)
    {
        if (!StringTools.isBlank(m)) {
            // -- lookup name, return default if not found
            DBReadWriteMode rwMode = EnumTools.getValueOf(DBReadWriteMode.class, m, (DBReadWriteMode)null);
            return (rwMode != null)? rwMode : dftMode;
        } else {
            // -- blank specified, return default
            return dftMode;
        }
    }

    // ------------------------------------------------------------------------

    private boolean rr = false; // isRead
    private boolean ww = false; // isWrite
    private String  ss = "";    // property key suffix
    private String  dd = "";    // description
    DBReadWriteMode(boolean r, boolean w, String s, String d) 
    { 
        rr = r; 
        ww = w;
        ss = s;
        dd = d;
    }

    // ------------------------------------------------------------------------

    public boolean isRead()      { return  rr       ; }
    public boolean isReadOnly()  { return  rr && !ww; }
    public boolean isWrite()     { return         ww; }
    public boolean isWriteOnly() { return !rr &&  ww; }
    public boolean isReadWrite() { return  rr &&  ww; }
    public String  keySuffix()   { return ss; }
    public String  toString()    { return dd; }

    // ------------------------------------------------------------------------

}
