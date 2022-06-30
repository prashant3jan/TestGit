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
//  2020/02/19  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.util.*;
import java.io.*;

import org.opengts.util.*;
import org.opengts.db.tables.Device;

public class Maintenance
{

    // ------------------------------------------------------------------------
    // -- Maintenance Type

    public enum Type {
        KM (I18N.getString(Maintenance.class,"Maintenance.type.odometer"   ,"Odometer"    )),
        HR (I18N.getString(Maintenance.class,"Maintenance.type.engineHours","Engine-Hours")),
        FT (I18N.getString(Maintenance.class,"Maintenance.type.fixedTime"  ,"Fixed-Time"  ));
        // ---
        private I18N.Text  aa = null;
        Type(I18N.Text a)                   { aa = a; }
        public String  toString()           { return aa.toString(); }
        public String  toString(Locale loc) { return aa.toString(loc); }
        public boolean isKM()               { return this.equals(KM); }
        public boolean isHR()               { return this.equals(HR); }
        public boolean isFT()               { return this.equals(FT); }
    };
    
    private static final Maintenance.Type DefaultType = Maintenance.Type.KM;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the total number of maintenance items for the specied type
    **/
    public static int getCount(Maintenance.Type type)
    {
        if (type != null) {
            switch (type) {
                case KM : return Device.getMaintCountKM();
                case HR : return Device.getMaintCountHR();
                case FT : return Device.getMaintCountFT();
            }
        }
        return 0;
    }

    // --------------------------------

    /**
    *** Returns a array of all Maintenance items.
    *** Does not return null, but may return an empty array.
    **/
    public static Maintenance[] getAllItems(Maintenance.Type type, Device dev)
    {
        if ((type != null) && (dev != null)) {
            int cnt = Maintenance.getCount(type);
            Maintenance m[] = new Maintenance[cnt];
            for (int i = 0; i < cnt; i++) {
                m[i] = new Maintenance(type, i, dev);
            }
            return m;
        }
        return new Maintenance[0];
    }

    // --------------------------------

    /**
    *** Returns a Maintenance instance of the next item to become due (or is past-due).
    *** Returns null if no maintenance item is becoming due.
    **/
    public static Maintenance[] getNextDue(Maintenance.Type type, Device dev, double maxVal)
    {
        if ((type != null) && (dev != null)) {
            int ndxArr[] = null;
            switch (type) {
                case KM : ndxArr = dev.getMaintNextDueKM(      maxVal); break;
                case HR : ndxArr = dev.getMaintNextDueHR(      maxVal); break;
                case FT : ndxArr = dev.getMaintNextDueFT((long)maxVal); break;
            }
            if (!ListTools.isEmpty(ndxArr)) {
                Maintenance m[] = new Maintenance[ndxArr.length];
                for (int i = 0; i < ndxArr.length; i++) {
                    m[i] = new Maintenance(type, ndxArr[i], dev);
                }
                return m;
            }
        }
        return null;
    }

    // --------------------------------

    /**
    *** Returns a list of Maintenance instances which are due.
    *** Returns null if no items are due.
    **/
    public static java.util.List<Maintenance> getAllDue(Maintenance.Type type, Device dev)
    {
        if ((type != null) && (dev != null)) {
            Vector<Maintenance> dueList = null;
            int cnt = Maintenance.getCount(type);
            for (int i = 0; i < cnt; i++) {
                double offset = 0.0;
                boolean isDue = false;
                switch (type) {
                    case KM : isDue = dev.isMaintDueKM(i,      offset); break;
                    case HR : isDue = dev.isMaintDueHR(i,      offset); break;
                    case FT : isDue = dev.isMaintDueFT(i,(long)offset); break;
                }
                if (isDue) {
                    if (dueList == null) { dueList = new Vector<Maintenance>(); }
                    dueList.add(new Maintenance(type,i,dev));
                }
            }
            return dueList;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private Maintenance.Type    type    = Maintenance.DefaultType;
    private int                 index   = -1;
    private Device              device  = null;

    /**
    *** Constructor
    **/
    public Maintenance(Maintenance.Type type, int ndx, Device dev)
    {
        super();
        this.type   = (type != null)? type : Maintenance.DefaultType;
        this.index  = ((ndx >= 0) && (ndx < Maintenance.getCount(this.type)))? ndx : 0;
        this.device = dev;
        if (this.device == null) {
            Print.logFatal("Specified device is null!");
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance type
    **/
    public Maintenance.Type getType()
    {
        return this.type; // guaranteed by the constructor to be non-null
    }

    // --------------------------------

    /**
    *** Returns true if KM/Odometer maintenance type
    **/
    public boolean isKM()
    {
        return this.getType().isKM();
    }

    // --------------------------------

    /**
    *** Returns true if HR/EngineHours maintenance type
    **/
    public boolean isHR()
    {
        return this.getType().isHR();
    }

    // --------------------------------

    /**
    *** Returns true if FT/FixedTime maintenance type
    **/
    public boolean isFT()
    {
        return this.getType().isFT();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance index
    **/
    public int getIndex()
    {
        return this.index; // guaranteed by the constructor to be valid for this type
    }

    /**
    *** Gets the Device instance
    **/
    public Device getDevice()
    {
        return this.device; // could be null
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance label
    **/
    public String getLabel()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.getMaintLabelKM(ndx);
                case HR : return dev.getMaintLabelHR(ndx);
                case FT : return dev.getMaintLabelFT(ndx);
            }
        }
        return "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance interval
    **/
    public double getInterval()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.getMaintIntervalKM(ndx);           // km
                case HR : return dev.getMaintIntervalHR(ndx);           // hours
                case FT : return (double)dev.getMaintIntervalFT(ndx);   // seconds/days/months
            }
        }
        return 0.0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance fixed-time interval units (Seconds, Days, Months)
    *** Returns null if this Maintenance instance does not represent a Fixed-Time interval
    **/
    public Device.MaintUnitsFT getUnitsFT()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return null;                      // km
                case HR : return null;                      // hours
                case FT : return dev.getMaintUnitsFT(ndx);  // seconds/days/months
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance last service value
    **/
    public double getLastService() // getMaint()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.getMaintOdometerKM(ndx);           // km
                case HR : return dev.getMaintEngHoursHR(ndx);           // hours
                case FT : return (double)dev.getMaintFixedTimeFT(ndx);  // epoch time
            }
        }
        return 0.0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Maintenance early notification offset
    **/
    public double getEarlyNotificationOffset() // getPreNotify()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.getMaintPreNotifyKM(ndx);          // km
                case HR : return dev.getMaintPreNotifyHR(ndx);          // hours
                case FT : return (double)dev.getMaintPreNotifyFT(ndx);  // seconds
            }
        }
        return 0.0;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the remaining interval until due
    **/
    public double getRemainingInterval() // getRemaining()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.getMaintRemainingKM(ndx);          // km
                case HR : return dev.getMaintRemainingHR(ndx);          // hours
                case FT : return (double)dev.getMaintRemainingFT(ndx);  // seconds
            }
        }
        return 0.0;
    }

    // --------------------------------

    /**
    *** Returns true if this maintenance interval will be due within the specified early-notify offset
    **/
    public boolean isDue(double offset)
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : return dev.isMaintDueKM(ndx,      offset);
                case HR : return dev.isMaintDueHR(ndx,      offset);
                case FT : return dev.isMaintDueFT(ndx,(long)offset);
            }
        }
        return false;
    }

    /**
    *** Returns true if this maintenance interval is now due
    **/
    public boolean isDue()
    {
        return this.isDue(0.0);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Resets the maintenance item in the Device table.
    *** Note: Device table is not yet saved.
    **/
    public boolean reset()
    {
        int    ndx = this.getIndex();
        Device dev = this.getDevice();
        if ((ndx >= 0) && (dev != null)) {
            switch (this.getType()) {
                case KM : dev.resetMaintOdometerKM(ndx) ; return true;
                case HR : dev.resetMaintEngHoursHR(ndx) ; return true;
                case FT : dev.resetMaintFixedTimeFT(ndx); return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
