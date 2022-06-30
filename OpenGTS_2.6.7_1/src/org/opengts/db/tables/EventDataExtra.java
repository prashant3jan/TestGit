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
// Description: [GTS Enterprise only]
//  Stores overflow fields for the EventData table.
//  When the EventData table is very large (over 100 mil records), it becomes
//  very difficult upgrade the EventData table with new required columns while
//  minimizing the service downtime.  Using the EventDataExtra table for any
//  new and seldom used columns makes it easier to upgrade with a long drawn-out
//  upgrade process.
// ----------------------------------------------------------------------------
// Change History:
//  2018/09/10  Martin D. Flynn
//     -Initial release
//      Enabled via property "EventData.enableEventDataExtra=true"
//      See "EventData.IsEventDataExtraEnabled()"
// ----------------------------------------------------------------------------
package org.opengts.db.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONBeanGetter;

import org.opengts.geocoder.*;
import org.opengts.cellid.*;

import org.opengts.dbtools.*;
import org.opengts.dbtools.DBField.DBFieldTemplate;
import org.opengts.dbtypes.*;
import org.opengts.db.*;

import org.opengts.cellid.CellTower;

public class EventDataExtra // EventDataXtra, EventDataOF
    extends DeviceRecord<EventDataExtra>
{

    // ------------------------------------------------------------------------

  //public static final String OPTCOLS_OverflowExtra_1  = "startupInit.EventData.OverflowExtra_1";

    // ------------------------------------------------------------------------

    /* table name */
    public static final String _TABLE_NAME              = "EventDataExtra";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    // ------------------------------------------------------------------------

    /* field definition */
    // -- Key fields
    public static final String FLD_timestamp            = EventData.FLD_timestamp;  // Unix Epoch time
    public static final String FLD_statusCode           = EventData.FLD_statusCode;
    private static final DBField StandardFieldInfo[] = {
        //--  Key fields
        newField_accountID(true,""),
        newField_deviceID(true,""),
        new DBField(FLD_timestamp         , Long.TYPE    , DBField.TYPE_UINT32    , "Timestamp"  , "key=true"),
        new DBField(FLD_statusCode        , Integer.TYPE , DBField.TYPE_UINT32    , "Status Code", "key=true editor=statusCode format=X2"),
    };

    // -- Example Overflow field set #1
    //public static final String FLD_ambientTemp          = EventData.FLD_ambientTemp;            // C
    //public static final String FLD_groundTemp           = EventData.FLD_groundTemp;             // C
  ////public static final String FLD_plowBladeAngle       = EventData.FLD_plowBladeAngle;         // Degrees
    //public static final String FLD_plowBladeHeight      = EventData.FLD_plowBladeHeight;        // Meters
  ////public static final String FLD_plowDownForce        = EventData.FLD_plowDownForce;          // Kg
    //public static final String FLD_granularSpreadRate   = EventData.FLD_granularSpreadRate;     // Kg/minute
    //public static final String FLD_liquidSpreadRate     = EventData.FLD_liquidSpreadRate;       // Litres/minute
    //public static final DBField OverflowExtra_1[] = {
    //    new DBField(FLD_ambientTemp       , Double.TYPE  , DBField.TYPE_DOUBLE    , "Ambient Temperature" , "format=#0.0 units=temp"),
    //    new DBField(FLD_groundTemp        , Double.TYPE  , DBField.TYPE_DOUBLE    , "Ground Temperature"  , "format=#0.0 units=temp"),
    //  //new DBField(FLD_plowBladeAngle    , Double.TYPE  , DBField.TYPE_DOUBLE    , "Plow Blade Angle"    , ""),
    //    new DBField(FLD_plowBladeHeight   , Double.TYPE  , DBField.TYPE_DOUBLE    , "Plow Blade Height"   , ""),
    //  //new DBField(FLD_plowDownForce     , Double.TYPE  , DBField.TYPE_DOUBLE    , "Plow Down Force"     , ""),
    //    new DBField(FLD_granularSpreadRate, Double.TYPE  , DBField.TYPE_DOUBLE    , "Granular Spread Rate", ""),
    //    new DBField(FLD_liquidSpreadRate  , Double.TYPE  , DBField.TYPE_DOUBLE    , "Liquid Spread Rate"  , ""),
    //};

    /* key class */
    public static class Key
        extends DeviceKey<EventDataExtra> // DBRecordKey
    {
        private EventData.Key  evDataKey  = null;
        private boolean        didSetKey  = false;
        public Key() {
            // -- Because EventDataExtra is use as an ancillary table, and it always 
            // -  queued by the EventData table, this default constructor should never
            // -  be used except when deleting Accounts/Devices.
            super();
            this.getFieldValues().setIgnoreInvalidFields(true);
        }
        public Key(EventData.Key evKey) {
            this.evDataKey = evKey;
            // -- at this point the EventData.Key fields have not yet been defined
        }
        private void _setKeys() {
            if (!this.didSetKey && (this.evDataKey != null)) {
                this.setKeyValue(FLD_accountID , this.evDataKey.getKeyValue(FLD_accountID ));
                this.setKeyValue(FLD_deviceID  , this.evDataKey.getKeyValue(FLD_deviceID  ));
                this.setKeyValue(FLD_timestamp , this.evDataKey.getKeyValue(FLD_timestamp ));
                this.setKeyValue(FLD_statusCode, this.evDataKey.getKeyValue(FLD_statusCode));
                this.didSetKey = true;
            }
        }
        public DBFactory<EventDataExtra> getFactory() {
            return EventDataExtra.getFactory();
        }
        public EventDataExtra getDBRecord(boolean reload, String... fldNames) {
            this._setKeys();
            return super.getDBRecord(reload, fldNames);
        }
        public EventDataExtra _getDBRecord(boolean reload, DBReadWriteMode rwMode, String... fldNames) throws DBException {
            this._setKeys();
            return super._getDBRecord(reload, rwMode, fldNames);
        }
    }

    /* factory constructor */
    private static DBFactory<EventDataExtra> factory = null;
    public static DBFactory<EventDataExtra> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                EventDataExtra.TABLE_NAME(),
                EventDataExtra.StandardFieldInfo,
                DBFactory.KeyType.PRIMARY,
                EventDataExtra.class, 
                EventDataExtra.Key.class,
                false/*editable*/,false/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
            factory.addParentTable(Device.TABLE_NAME());
            // -- "COUNT(*)", with "where", not allowed if InnoDB
            boolean countOK = RTConfig.getBoolean(DBConfig.PROP_EventData_allowInnoDBCountWithWhere,EventData.DFT_allowInnoDBCountWithWhere);
            factory.setAllowInnoDBCOUNT(countOK);
        }
        return factory;
    }

    /* Bean instance */
    public EventDataExtra()
    {
        super();
    }

    /* database record */
    public EventDataExtra(EventDataExtra.Key key)
    {
        super(key);
        // init?
    }

    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(EventDataExtra.class, loc);
        return i18n.getString("EventDataExtra.description", 
            "This table contains " +
            "overflow fields which have not yet been added to EventData."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_MAKE_INNODB[]       = new String[] { "makeInnoDB"    };
    private static final String ARG_CONFIRM_INNODB[]    = new String[] { "confirmInnoDB" };

    private static void usage()
    {
        Print.sysPrintln("Usage:");
        Print.sysPrintln("  java ... " + EventDataExtra.class.getName() + " {options}");
        Print.sysPrintln("Common Options:");
        Print.sysPrintln("  -makeInnoDB -confirmInnoDB=<SEC>  Convert EventDataExtra table to InnoDB");
        System.exit(1);
    }

    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);

        /* convert EventDataExtra table to InnoDB */
        if (RTConfig.getBoolean(ARG_MAKE_INNODB,false)) {
            // -- bin/admin.pl EventDataExtra -makeInnoDB [-confirmInnoDB=30]
            DBFactory<EventDataExtra> edeFact = EventDataExtra.getFactory();
            boolean isInnoDB = edeFact.isMySQLInnoDB();
            Print.sysPrintln("");
            Print.sysPrintln("EventDataExtra table conversion from MyISAM to InnoDB ...");
            // -- check for MySQL
            if (DBProvider.getProvider().getID() != DBProvider.DB_MYSQL) {
                Print.sysPrintln("ERROR: DBProvider must be MySQL!");
                Print.sysPrintln("");
                System.exit(99);
            }
            // -- check InnoDB
            if (isInnoDB) {
                // -- already InnoDB
                try {
                    long rcdCnt = edeFact.getRecordCount("",false);
                    Print.sysPrintln("EventDataExtra table is already InnoDB [~"+rcdCnt+" records]");
                    Print.sysPrintln("");
                    System.exit(0);
                } catch (DBException dbe) {
                    Print.logException("Error getting record count", dbe);
                    System.exit(99);
                }
            }
            // -- display MyISAM record count
            long recordCount = 0L;
            try {
                recordCount = edeFact.getRecordCount("",true);
            } catch (DBException dbe) {
                Print.logException("Error getting record count", dbe);
                System.exit(99);
            }
            Print.sysPrintln("Current number of records in EventDataExtra table  : " + recordCount);
            // -- approximate time to convert (note: this is a "very rough" approximation)
            // -    5078637 ==> 1856 sec
            //recordCount = 80L; // testing purposes only
            long estSEC_lo = (long)(((double)recordCount * 0.12) / 1000.0); // 0.044
            long estSEC_hi = (long)(((double)recordCount * 0.37) / 1000.0);
            StringBuffer estSB = new StringBuffer();
            estSB.append("Approximate InnoDB conversion completion time : ");
            if (estSEC_hi > 3600L) {
                // -- minutes
                estSB.append(estSEC_lo / 60L);
                estSB.append(" to ");
                estSB.append((estSEC_hi + 45L) / 60L);
                estSB.append(" minutes");
                // -- hours
                estSB.append(" [");
                estSB.append(StringTools.format((double)estSEC_lo / 3600.0,"0.00"));
                estSB.append(" to ");
                estSB.append(StringTools.format((double)estSEC_hi / 3600.0,"0.00"));
                estSB.append(" hours]");
            } else 
            if (estSEC_hi > 60L) {
                // -- seconds
                estSB.append(estSEC_lo);
                estSB.append(" to ");
                estSB.append(estSEC_hi);
                estSB.append(" seconds");
                // -- minutes
                estSB.append(" [");
                estSB.append(StringTools.format((double)estSEC_lo / 60.0,"0.0"));
                estSB.append(" to ");
                estSB.append(StringTools.format((double)estSEC_hi / 60.0,"0.0"));
                estSB.append(" minutes]");
            } else
            if (estSEC_hi > 1L) {
                // -- seconds
                estSB.append(estSEC_lo);
                estSB.append(" to ");
                estSB.append(estSEC_hi);
                estSB.append(" seconds");
            } else {
                // -- seconds
                estSB.append(" < 1 second");
            }
            estSB.append(", Estimate Only!");
            Print.sysPrintln(estSB.toString());
            // -- confirmation?
            boolean confirm;
            String confirmStr = RTConfig.getString(ARG_CONFIRM_INNODB,"false");
            if (StringTools.isBlank(confirmStr)) {
                // -- only "-confirmInnoDB" specified
                confirm = false;
            } else
            if (Character.isDigit(confirmStr.charAt(0))) {
                // -- "-confirmInnoDB=SECONDS" specified
                long sec = StringTools.parseLong(confirmStr,-1L);
                confirm = ((sec > 0L) && (estSEC_hi <= sec))? true : false;
            } else {
                // -- "-confirmInnoDB=yes" specified?
                //confirm = StringTools.parseBoolean(confirmStr,false);
                confirm = confirmStr.equalsIgnoreCase("yes");
            }
            if (!confirm) {
                String msg = 
                "-----------------------------------------------------------------------------------------------------------\n" +
                "IMPORTANT NOTICE:\n" +
                "The process to convert the EventDataExtra table to InnoDB may take an extended length of time to complete. \n" +
                "To indicate that you are aware of the time it will take to execute this command, please add \n" +
                "'-"+ARG_CONFIRM_INNODB[0]+"=yes' to the command-line parameters used to execute this command.\n" +
                "All DCS modules and Servlets must be stopped prior to converting tables to InnoDB.\n" +
                "-----------------------------------------------------------------------------------------------------------\n" +
                "\n";
                Print.sysPrint(msg);
                System.exit(1);
            }
            // -- convert to InnoDB
            int exitCode;
            DBConnection dbc = null;
            try {
                // -- "ALTER TABLE EventDataExtra engine=InnoDB;"
                String edeTblName = EventDataExtra.TABLE_NAME(); // edeFact.getUntranslatedTableName()
                String makeInnoDB = "ALTER TABLE "+edeTblName+" engine=InnoDB;";
                Print.sysPrintln("Converting "+edeTblName+" to InnoDB ...");
                Print.sysPrintln("Start time: " + (new DateTime()));
                Print.sysPrintln("Executing : " + makeInnoDB);
                Print.sysPrintln("...");
                dbc = DBConnection.getDBConnection(DBReadWriteMode.READ_WRITE);
                long startMS = DateTime.getCurrentTimeMillis();
                dbc.executeUpdate(makeInnoDB);
                long endMS = DateTime.getCurrentTimeMillis();
                double deltaSec = (double)(endMS - startMS) / 1000.0;
                Print.sysPrintln("End time  : " + (new DateTime()) + " ["+StringTools.format(deltaSec,"0.0")+" sec]");
                Print.sysPrintln("DCS modules and Servlet can now be restarted.");
                exitCode = 0;
            } catch (SQLException sqe) {
                Print.logException("Error converting to InnoDB", sqe);
                exitCode = 99;
            } catch (DBException dbe) {
                Print.logException("Error converting to InnoDB", dbe);
                exitCode = 99;
            } finally {
                DBConnection.release(dbc);
            }
            // -- done
            Print.sysPrintln("");
            System.exit(exitCode);
        }

        /* no options specified */
        Print.sysPrintln("No command-line options specified");
        usage();

    }

}

