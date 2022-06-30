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
// Description:
//  Miscellaneous DB tools for manipulating tables, etc.
//  DO NOT USE UNLESS YOU ARE FAMILIAR WITH THIS TOOL!
// ----------------------------------------------------------------------------
// Change History:
//  2018/09/10  GTS Development Team
//     -Initial release
//  2020/02/19  GTS Development Team
//     -Added support for separating MySQL read/write connections (DBReadWriteMode). [2.6.7-B45]
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
*** DBHacks: tools for manipulating tables, etc.
**/

public class DBHacks
{

    private static final String CREATE_TABLE = "CREATE TABLE";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static void SetCommandLineArgs(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        // -- default database properties
        {
            String dbProv = RTConfig.getString(RTKey.DB_PROVIDER, null);
            if (StringTools.isBlank(dbProv) || dbProv.equals("?")) {
                RTConfig.setString(RTKey.DB_PROVIDER, "mysql");
            }
        }
        {
            String dbHost = RTConfig.getString(RTKey.DB_HOST, null);
            if (StringTools.isBlank(dbHost) || dbHost.equals("?")) {
                RTConfig.setString(RTKey.DB_HOST, "localhost");
            }
        }
        {
            String dbPort = RTConfig.getString(RTKey.DB_PORT, null);
            if (StringTools.isBlank(dbPort) || dbPort.equals("?")) {
                RTConfig.setString(RTKey.DB_PORT, "3306");
            }
        }
        {
            String dbUser = RTConfig.getString(RTKey.DB_USER, null);
            if (StringTools.isBlank(dbUser) || dbUser.equals("?")) {
                RTConfig.setString(RTKey.DB_USER, "gts");
            }
        }
        {
            String dbPass = RTConfig.getString(RTKey.DB_PASS, null);
            if (StringTools.isBlank(dbPass) || dbPass.equals("?")) {
                RTConfig.setString(RTKey.DB_PASS, "opengts");
            }
        }
        {
            String dbName = RTConfig.getString(RTKey.DB_NAME, null);
            if (StringTools.isBlank(dbName) || dbName.equals("?")) {
                RTConfig.setString(RTKey.DB_NAME, "gts");
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CREATE TABLE IF NOT EXISTS `user_photos` (
    //   `accountID` varchar(32) NOT NULL,
    //   `userID` varchar(32) NOT NULL,
    //   `description` varchar(255) DEFAULT NULL,
    //   `height` int(10) DEFAULT NULL,
    //   `width` int(10) DEFAULT NULL,
    //   `isActive` tinyint(4) DEFAULT NULL,
    //   `creationTime` int(10) DEFAULT NULL,
    //   PRIMARY KEY (`accountID`,`userID`)
    // ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
    
    private static class TableDef
    {
        String                    tableName    = null;
        OrderedMap<String,String> defColumnMap = null;
        OrderedMap<String,String> actColumnMap = null;
        public TableDef(String tableN, OrderedMap<String,String> colMap) {
            super();
            this.tableName = StringTools.trim(tableN);
            this.defColumnMap = colMap; // must not be null
            this.defColumnMap.setIgnoreCase(true);
            this.actColumnMap = new OrderedMap<String,String>();
            this.actColumnMap.setIgnoreCase(true);
            try {
                DBField dbFlds[] = DBProvider.getActualTableFields(this.tableName);
                for (DBField dbf : dbFlds) {
                    String colName = dbf._getName();
                    String colType = dbf.getSqlType(true);
                    this.actColumnMap.put(colName,colType);
                }
            } catch (DBException dbe) {
                // --
            }
        }
        public String getTableName() {
            return this.tableName;
        }
        // --
        public OrderedMap<String,String> getDefinedColumnMap() {
            return this.defColumnMap;
        }
        public boolean hasDefinedColumns() {
            return !ListTools.isEmpty(this.getDefinedColumnMap())? true : false;
        }
        public boolean isColumnDefined(String colName) {
            return this.getDefinedColumnMap().containsKeyIgnoreCase(colName)? true : false;
        }
        // --
        public OrderedMap<String,String> getActualColumnMap() {
            return this.actColumnMap;
        }
        public boolean hasActualColumns() {
            return !ListTools.isEmpty(this.getActualColumnMap())? true : false;
        }
        public boolean isColumnActual(String colName) {
            return this.getActualColumnMap().containsKeyIgnoreCase(colName)? true : false;
        }
        // --
        public void printLog() {
            Print.logInfo("Table: " + this.getTableName());
            OrderedMap<String,String> colMap = new OrderedMap<String,String>();
            colMap.setIgnoreCase(true);
            // --
            OrderedMap<String,String> actMap = this.getActualColumnMap();
            if (ListTools.isEmpty(actMap)) {
                Print.logInfo("    (does not exist in database)");
                return;
            }
            colMap.putAll(actMap);
            // --
            OrderedMap<String,String> defMap = this.getDefinedColumnMap();
            for (String defKey : defMap.keySet()) {
                String filtKey = (String)actMap.keyCaseFilter(defKey);
                String defType = defMap.get(defKey);
                colMap.put(filtKey, defType);
            }
            for (String colKey : colMap.keySet()) {
                String colType = colMap.get(colKey);
                boolean isDef = this.isColumnDefined(colKey);
                boolean isAct = this.isColumnActual(colKey);
                String def;
                if (isDef && isAct) {
                    // -- in both defined and actual
                    def = "";
                } else
                if (isDef) {
                    // -- in defined, not actual
                    def = "(defined)";
                } else
                if (isAct) {
                    // -- in actaul, not defined
                    def = "(actual)";
                } else {
                    // -- neither defined nor actual (will not occur)
                    def = "(?)";
                }
                Print.logInfo("   " + colKey + " ==> " + colType + "  " + def);
            }
        }
    }

    private static TableDef ParseCreateTable(String lines[], int s, int e)
    {
        if ((e - s) < 2) {
            Print.logError("Invalid 'CREATE TABLE' definition");
            return null;
        } else
        if (!lines[s].startsWith(CREATE_TABLE)) {
            Print.logError("First line is not 'CREATE TABLE ...'");
            return null;
        } else
        if (!lines[e].startsWith(")")) {
            Print.logError("Last line is not ') ... ;' (1)");
            return null;
        }
        // --
        String tableName = null;
        OrderedMap<String,String> columnMap = new OrderedMap<String,String>();
        for (int i = s ; i <= e; i++) {
            String colDef = lines[i];
            if (colDef.endsWith(",")) {
                colDef = colDef.substring(0,colDef.length() - 1);
            }
            // --
            if (colDef.startsWith("CREATE TABLE")) {
                int tp1 = colDef.indexOf('`');
                int tp2 = (tp1 >= 0)? colDef.indexOf('`',tp1+1) : -1;
                if ((tp1 < 0) || (tp2 <= tp1)) {
                    Print.logError("Unable to locate table name");
                    return null;
                }
                tableName = colDef.substring(tp1+1,tp2);
            } else 
            if (colDef.startsWith("`")) {
                int cnp1 = 0;
                int cnp2 = (cnp1 >= 0)? colDef.indexOf("`",cnp1+1) : -1;
                if ((cnp1 < 0) || (cnp2 <= cnp1)) {
                    Print.logError("Invalid column definition (3)");
                    return null;
                }
                String colName = colDef.substring(cnp1+1,cnp2);
                String colType = colDef.substring(cnp2+1).trim();
                columnMap.put(colName, colType);
            } else
            if (colDef.startsWith("PRIMARY KEY")) {
                // -- ignore for now
            } else
            if (colDef.startsWith("KEY")) {
                // -- ignore for now
            } else
            if (colDef.startsWith("UNIQUE KEY")) {
                // -- ignore for now
            } else
            if (colDef.startsWith(")")) {
                // -- we are at the end
                if ((i + 1) < e) {
                    Print.logError("Unexpected trailing lines found");
                    return null;
                }
            } else {
                Print.logError("Unexpected column definition line: ["+i+"] " + colDef);
                return null;
            }
        }
        // --
        if (StringTools.isBlank(tableName)) {
            Print.logError("Invalid table name");
            return null;
        } else 
        if (ListTools.isEmpty(columnMap)) {
            Print.logError("No columns found");
            return null;
        }
        // --
        TableDef table = new TableDef(tableName,columnMap);
        return table;
    }

    private static final Map<String,TableDef> TableDefMap = new OrderedMap<String,TableDef>();

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_PARSE_CREATE[]          = { "parseCreate" };
    private static final String ARG_ADD_MISSING_COLUMNS[]   = { "addMissing"  };
    private static final String ARG_EXEC_SQL[]              = { "execSQL"     };
    private static final String ARG_PRINT_SQL[]             = { "printSQL"    };
    private static final String ARG_TABLES[]                = { "tables"      };

    public static void main(String argv[])
    {
        SetCommandLineArgs(argv);
        boolean execSQL  = RTConfig.getBoolean(ARG_EXEC_SQL,false);
        boolean printSQL = !execSQL || RTConfig.getBoolean(ARG_PRINT_SQL,false);
        String  tables[] = StringTools.split(RTConfig.getString(ARG_TABLES,null),',');

        /* parse CREATE_TABLE */
        String parseCreate = RTConfig.getString(ARG_PARSE_CREATE,null);
        if (!StringTools.isBlank(parseCreate)) {
            File file = new File(parseCreate);
            if (!FileTools.isFile(file)) {
                Print.sysPrintln("ERROR - not a file: " + parseCreate);
                System.exit(1);
            }
            byte fileData[] = FileTools.readFile(file);
            if (ListTools.isEmpty(fileData)) {
                Print.sysPrintln("ERROR - unable to read file: " + parseCreate);
                System.exit(1);
            }
            String fileDataS = StringTools.toStringValue(fileData);
            String fileDataL[] = StringTools.split(fileDataS,'\n');
            // --
            Vector<String> createTableList = new Vector<String>();
            for (int i = 0; i < fileDataL.length; i++) {
                String L = fileDataL[i].trim();
                // -- skip comments && blank lines
                if (L.equals("") || 
                    L.startsWith("--") || 
                    L.startsWith("/*")) {
                    continue;
                }
                // -- "CREATE TABLE"
                if (L.startsWith(CREATE_TABLE)) {
                    int s = i;
                    int e = -1;
                    for (int j = i; j < fileDataL.length; j++) {
                        if (fileDataL[j].startsWith(")")) {
                            e = j;
                            break;
                        }
                    }
                    if (e < s) {
                        Print.sysPrintln("Invalid 'CREATE TABLE' definition: " + s);
                        System.exit(1);
                    }
                    TableDef tableDef = ParseCreateTable(fileDataL,s,e);
                    if (tableDef == null) {
                        Print.sysPrintln("Invalid TableDef");
                        System.exit(1);
                    }
                    TableDefMap.put(tableDef.getTableName(),tableDef);
                    i = e;
                    continue;
                }
                // -- unexpected lines
                Print.sysPrintln("Unexpected line: " + L);
            }
            // --
            for (String tableName : TableDefMap.keySet()) {
                TableDef tableDef = TableDefMap.get(tableName);
                Print.sysPrintln("------------------------");
                tableDef.printLog();
            }
            // --
            //System.exit(0);
        }

        /* add missing columns */
        if (RTConfig.getBoolean(ARG_ADD_MISSING_COLUMNS,false)) {
            if (ListTools.isEmpty(TableDefMap)) {
                Print.sysPrintln("TableDef not initialized");
                System.exit(1);
            }
            StringBuffer alterSB = new StringBuffer(); 
            StringBuffer noTableSB = new StringBuffer(); 
            for (String tableName : TableDefMap.keySet()) {
                // -- specific table?
                if (!ListTools.isEmpty(tables) && !ListTools.contains(tables,tableName)) {
                    // -- skip this table
                    continue;
                }
                // --
                TableDef tableDef = TableDefMap.get(tableName);
                if (tableDef.hasActualColumns()) {
                    // -- table exists
                    Map<String,String> defColMap = tableDef.getDefinedColumnMap();
                    Vector<String> missingCol = new Vector<String>();
                    for (String colName : defColMap.keySet()) {
                        if (!tableDef.isColumnActual(colName)) {
                            missingCol.add(colName);
                        }
                    }
                    StringBuffer alterTableSQL = new StringBuffer();
                    if (!ListTools.isEmpty(missingCol)) {
                        alterTableSQL.append("ALTER TABLE `" + tableName + "` ");
                        if (!execSQL) { alterTableSQL.append("\n"); }
                        for (int c = 0; c < missingCol.size(); c++) {
                            String colName = missingCol.get(c);
                            String colType = defColMap.get(colName);
                            if (!execSQL) { alterTableSQL.append("   "); }
                            alterTableSQL.append("ADD COLUMN `" + colName + "` " + colType);
                            alterTableSQL.append(((c + 1) >= missingCol.size())? ";" : ", ");
                            if (!execSQL) { alterTableSQL.append("\n"); }
                        }
                        if (execSQL) {
                            DBConnection dbc = null;
                            Statement   stmt = null;
                            ResultSet   rs   = null;
                            try {
                                String alterSQL = alterTableSQL.toString();
                                Print.sysPrintln("Execute SQL: " + alterSQL);
                                dbc  = DBConnection.getDBConnection(DBReadWriteMode.ALTER);
                                stmt = dbc.execute(alterSQL);
                                rs   = stmt.getResultSet();
                            } catch (SQLException sqe) {
                                Print.logException("Unable to execute ALTER TABLE", sqe);
                            } catch (Throwable th) {
                                Print.logException("Unexpected exception: ", th);
                            } finally {
                                if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
                                if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
                                DBConnection.release(dbc);
                            }
                        } else {
                            alterSB.append(alterTableSQL);
                            alterSB.append("\n"); // blank line
                        }
                    }
                } else {
                    // -- table does not exist
                    noTableSB.append("/* '"+tableName+"' does not exist */\n");
                }
            }
            if (printSQL) {
                Print.sysPrint("----------------------------------\n");
                Print.sysPrintln("");
                Print.sysPrint(alterSB.toString());
                Print.sysPrintln("");
                Print.sysPrint(noTableSB.toString());
                Print.sysPrintln("");
            }
        }

    }

}

