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
//      - Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.io.*;
import java.util.*;

/**
*** Temperature range container
**/

public class TemperatureRange
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Parses the temperature range value from the specified String
    **/
    public static TemperatureRange ParseTemperatureRange(String v)
    {
        // -- "LABEL:HI/LOW"

        /* invalid string? */
        if (StringTools.isBlank(v)) { 
            return null;
        }
        int s = 0;

        /* extract label */
        String label = null;
        int pLbl = v.indexOf(":");
        if (pLbl >= 0) {
            label = v.substring(0,pLbl);
            s = pLbl + 1;
        }

        /* extract low/high */
        Temperature tempLO = null;
        Temperature tempHI = null;
        int pRng = v.indexOf(":", s);
        if (pRng < s) { pRng = v.indexOf("/", s); }
        if (pRng >= s) {
            String loStr = (pRng >= 0)? v.substring(s,pRng) : v.substring(s);
            String hiStr = (pRng >= 0)? v.substring(pRng+1) : null;
            double lo    = StringTools.parseDouble(loStr, -99999.9);
            double hi    = StringTools.parseDouble(hiStr,  99999.9);
            tempLO       = Temperature.isValidTemperature(lo)? new Temperature(lo) : null;
            tempHI       = Temperature.isValidTemperature(hi)? new Temperature(hi) : null;
        }

        /* return TemperatureRange */
        if (!StringTools.isBlank(label) || (tempLO != null) || (tempHI != null)) {
            return new TemperatureRange(label, tempLO, tempHI);
        } else {
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Get Label from temperature range
    **/
    public static String getLabel(TemperatureRange tr, String dft)
    {
        String label = (tr != null)? tr.getLabel() : null;
        return !StringTools.isBlank(label)? label : dft;
    }

    /**
    *** Get low temperature value
    **/
    public static double getLowTemperature(TemperatureRange tr)
    {
        if (tr != null) {
            return tr.getLowTemperatureC();
        } else {
            return Temperature.INVALID_TEMPERATURE;
        }
    }

    /**
    *** Get high temperature value
    **/
    public static double getHighTemperature(TemperatureRange tr)
    {
        if (tr != null) {
            return tr.getHighTemperatureC();
        } else {
            return Temperature.INVALID_TEMPERATURE;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String          label  = null;
    private Temperature     tempLO = null;
    private Temperature     tempHI = null;

    /**
    *** Constructor
    **/
    public TemperatureRange(String lbl, Temperature LO, Temperature HI)
    {
        super();
        this.label  = StringTools.trim(lbl);
        this.tempLO = LO;
        this.tempHI = HI;
    }

    /**
    *** Constructor
    **/
    public TemperatureRange(Temperature LO, Temperature HI)
    {
        this(null, LO, HI);
    }

    // --------------------------------

    /**
    *** Constructor
    **/
    public TemperatureRange(String lbl, double lo, double hi)
    {
        super();
        this.label  = StringTools.trim(lbl);
        this.tempLO = Temperature.isValidTemperature(lo)? new Temperature(lo) : null;
        this.tempHI = Temperature.isValidTemperature(hi)? new Temperature(hi) : null;
    }

    /**
    *** Constructor
    **/
    public TemperatureRange(double lo, double hi)
    {
        this(null, lo, hi);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this instance has a defined label
    **/
    public boolean hasLabel()
    {
        return !StringTools.isBlank(this.getLabel())? true : false;
    }

    /**
    *** Gets the label for this temperature range
    **/
    public String getLabel()
    {
        return this.label;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns trus if this temperature range has a valid defied low temperature
    **/
    public boolean isValidLowTemperature()
    {
        return (this.tempLO != null)? this.tempLO.isValidTemperature() : false;
    }

    /**
    *** Gets the low Temperature instance of this temperature range
    **/
    public Temperature getLowTemperature()
    {
        return this.tempLO;
    }

    /**
    *** Gets the low temperature value of this temperature range
    **/
    public double getLowTemperatureC()
    {
        return (this.tempLO != null)? this.tempLO.getTemperatureC() : Temperature.INVALID_TEMPERATURE;
    }

    /**
    *** Gets the low temperature value of this temperature range
    **/
    public double getLowTemperatureF()
    {
        return (this.tempLO != null)? this.tempLO.getTemperatureF() : Temperature.INVALID_TEMPERATURE;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns trus if this temperature range has a valid defied high temperature
    **/
    public boolean isValidHighTemperature()
    {
        return (this.tempHI != null)? this.tempHI.isValidTemperature() : false;
    }

    /**
    *** Gets the high Temperature instance of this temperature range
    **/
    public Temperature getHighTemperature()
    {
        return this.tempHI;
    }

    /**
    *** Gets the high temperature value of this temperature range
    **/
    public double getHighTemperatureC()
    {
        return (this.tempHI != null)? this.tempHI.getTemperatureC() : Temperature.INVALID_TEMPERATURE;
    }

    /**
    *** Gets the high temperature value of this temperature range
    **/
    public double getHighTemperatureF()
    {
        return (this.tempHI != null)? this.tempHI.getTemperatureF() : Temperature.INVALID_TEMPERATURE;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this instance has a valid temperature range defined
    **/
    public boolean hasValidTemperatureRange()
    {
        return (this.isValidLowTemperature() || this.isValidHighTemperature())? true : false;
    }

    // ------------------------------------------------------------------------

    /** 
    *** Returns true if the specified temperature is in range
    **/
    public boolean isTemperatureInRange(double C)
    {
        // -- invalid temperature specified
        if (!Temperature.isValidTemperature(C)) {
            return false;
        }
        // -- below low temperature?
        if (this.isValidLowTemperature() && (C < this.getLowTemperatureC())) {
            return false;
        }
        // -- above high temperature?
        if (this.isValidHighTemperature() && (C > this.getHighTemperatureC())) {
            return false;
        }
        // -- in range
        return true;
    }

    /** 
    *** Returns true if the specified temperature is in range
    **/
    public boolean isTemperatureInRangeF(double F)
    {
        if (!Temperature.isValidTemperatureF(F)) {
            return false;
        } else {
            return this.isTemperatureInRange(Temperature.F2C(F));
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a String representation of this instance
    *** Note: The format returned by this method is used to store references to
    *** temperature ranges used for production temperature range alerts.
    **/
    public String toString()
    {
        // -- "LABEL:LOW/HIGH"
        StringBuffer sb = new StringBuffer();
        // -- label
        if (this.hasLabel()) {
            sb.append(this.getLabel());
            sb.append(":");
        }
        // -- low
        if (this.isValidLowTemperature()) {
            sb.append(StringTools.format(this.getLowTemperatureC(),"0.0"));
        }
        // -- high
        if (this.isValidHighTemperature()) {
            sb.append("/");
            sb.append(StringTools.format(this.getHighTemperatureC(),"0.0"));
        }
        // -- return
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
