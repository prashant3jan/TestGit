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
//  2011/04/01  Martin D. Flynn
//     -Initial release
//  2017/09/28  Martin D. Flynn
//     -Added "FuelType" Enum [2.6.5-B59]
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.tables.*;

public abstract class FuelManager
{

    // ------------------------------------------------------------------------

    public static final double BTU_PER_THERM            = 100000.0;
    public static final double THERM_PER_BTU            = 1.0 / BTU_PER_THERM;
    public static final double THERM_PER_JOULE          = 9.4804342797335E-9;
    public static final double JOULE_PER_THERM          = 1.0 / THERM_PER_JOULE;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Fuel Type

    // https://www.fuelfreedom.org/our-work/fuels-101/fuel-types/
    // https://itstillruns.com/six-fuels-used-todays-vehicles-7347672.html
    // http://www.eia.gov/tools/faqs/faq.cfm?id=307&t=11
    // http://ecoscore.be/en/info/ecoscore/co2
    // https://www.engineeringtoolbox.com/liquids-densities-d_743.html
    public enum FuelType implements EnumTools.StringLocale, EnumTools.IntValue {                       //  Dens   AFR   CO2
        UNKNOWN  (    0, I18N.getString(FuelManager.class,"FuelManager.FuelType.unknown"    ,"Unknown"  ), 745.0, 15.0, 0.00),
        GASOLINE ( 1000, I18N.getString(FuelManager.class,"FuelManager.FuelType.gasoline"   ,"Gasoline" ), 737.0, 14.7, 2.35), // 2.31
        UNLEADED ( 1100, I18N.getString(FuelManager.class,"FuelManager.FuelType.unleaded"   ,"Unleaded" ), 737.0, 14.7, 2.35), // 2.31
        LEADED   ( 1900, I18N.getString(FuelManager.class,"FuelManager.FuelType.unleaded"   ,"Unleaded" ), 737.0, 14.7, 2.35), // 2.31
        ETHENOL  ( 2000, I18N.getString(FuelManager.class,"FuelManager.FuelType.ethenol"    ,"Ethenol"  ), 789.0, 14.6, 2.27),
        METHENOL ( 3000, I18N.getString(FuelManager.class,"FuelManager.FuelType.methenol"   ,"Methenol" ), 791.0, 14.6, 2.27),
        DIESEL   ( 4000, I18N.getString(FuelManager.class,"FuelManager.FuelType.diesel"     ,"Diesel"   ), 885.0, 14.6, 2.68),
        BIODIESEL( 5000, I18N.getString(FuelManager.class,"FuelManager.FuelType.biodiesel"  ,"Biodiesel"), 885.0, 14.6, 2.64),
        KEROSENE ( 6000, I18N.getString(FuelManager.class,"FuelManager.FuelType.kerosene"   ,"Kerosene" ), 820.1, 14.6, 2.58),
        AVIATION ( 7000, I18N.getString(FuelManager.class,"FuelManager.FuelType.aviation"   ,"Aviation" ), 800.0, 14.7, 2.58),
        JETA     ( 7100, I18N.getString(FuelManager.class,"FuelManager.FuelType.jeta"       ,"JetA"     ), 800.0, 14.7, 2.58),
        JETA1    ( 7110, I18N.getString(FuelManager.class,"FuelManager.FuelType.jeta1"      ,"JetA1"    ), 800.0, 14.7, 2.58),
        JETB     ( 7200, I18N.getString(FuelManager.class,"FuelManager.FuelType.jetb"       ,"JetB"     ), 800.0, 14.7, 2.58),
        LPG      ( 8000, I18N.getString(FuelManager.class,"FuelManager.FuelType.lpg"        ,"LPG"      ), 493.5, 15.6, 1.51),
        PROPANE  ( 8100, I18N.getString(FuelManager.class,"FuelManager.FuelType.propane"    ,"Propane"  ), 493.5, 15.6, 1.51),
        BUTANE   ( 8200, I18N.getString(FuelManager.class,"FuelManager.FuelType.butane"     ,"Butane"   ), 493.5, 15.4, 1.51),
        CNG      ( 9000, I18N.getString(FuelManager.class,"FuelManager.FuelType.cng"        ,"CNG"      ),  80.0, 17.2, 1.51),
        METHANE  ( 9100, I18N.getString(FuelManager.class,"FuelManager.FuelType.methane"    ,"Methane"  ),  80.0, 17.2, 1.51),
        HYDROGEN (10000, I18N.getString(FuelManager.class,"FuelManager.FuelType.hydrogen"   ,"Hydrogen" ),  70.8, 15.0, 0.00), // unknown
        OTHER    (99999, I18N.getString(FuelManager.class,"FuelManager.FuelType.other"      ,"Other"    ), 745.0, 15.0, 2.50);
        // --- Notes:
        // --   - There are 2 types of hydogen fueled vehicles: cumbustion and fuel-cell
        // --   - There are more than the above listed jet fuel types
        private int         vv  = 0;    // int value
        private I18N.Text   tt  = null; // text description
        private double      afr = 0.0;  // Air-Fuel Ratio
        private double      den = 0.0;  // density g/L (or kg/m^3)
        private double      c02 = 0.0;  // amount CO2 kg/Litre
        FuelType(int v, I18N.Text t, double d, double a, double CO2) { vv = v; tt = t; den = d; afr = a; c02 = CO2; };
        public int     getIntValue()             { return vv; }
        public String  toString()                { return tt.toString(); }
        public String  toString(Locale loc)      { return tt.toString(loc); }
        public boolean isUnknown()               { return this.equals(UNKNOWN); }
        public boolean isOther()                 { return this.equals(OTHER); }
        public double  density()                 { return den; }
        public double  airFuelRatio()            { return afr; }
        public double  c02KgPerLitre()           { return c02; }
    }

    /**
    *** Gets the FuelType enumerated value for the specified name
    **/
    public static FuelType GetFuelType(String type, FuelType dft)
    {
        return EnumTools.getValueOf(FuelType.class, type, dft);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Fuel Level Change 

    public enum LevelChangeType implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE        (  0, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.none"    ,"None"    )), // default
        INCREASE    (  1, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.increase","Increase")),
        DECREASE    (  2, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.decrease","Decrease")),
        UNKNOWN     ( 99, I18N.getString(FuelManager.class,"FuelManager.LevelChangeType.unknown" ,"Unknown" ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        LevelChangeType(int v, I18N.Text a)         { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public abstract LevelChangeType insertFuelLevelChange(EventData event);

    // ------------------------------------------------------------------------

}
