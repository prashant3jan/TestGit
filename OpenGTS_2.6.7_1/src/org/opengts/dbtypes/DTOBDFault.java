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
//  2007/02/21  Martin D. Flynn
//     -Initial release
//  2010/06/17  Martin D. Flynn
//     -Added support for J1939, OBDII
//  2010/09/09  Martin D. Flynn
//     -Modified method used for obtaining J1587 MID/PID/SID/FMI descriptions
//  2012/04/03  Martin D. Flynn
//     -Fixed "GetPropertyString_OBDII"
//  2013/04/08  Martin D. Flynn
//     -Changed OBDII/DTC encoded bitmask to support hex fault codes, ie "P11AF" [B04]
//  2017/03/14  Martin D. Flynn
//     -Changed J1939/DTC String format to support multiple fault codes [2.6.4-B75]
//  2020/02/19  GTS Development Team
//     -Added support for external fault code description lookup [2.6.7-B45q]
//     -Removed "GetPropertyString_XXXX", replaced with "GetRTProperties_XXXX" [2.6.7-B45q]
// ----------------------------------------------------------------------------
package org.opengts.dbtypes;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

public class DTOBDFault
    extends DBFieldType
{

    // ------------------------------------------------------------------------

    private static final String PACKAGE_OPENGTS_        = "org.opengts.";
    public  static final String PACKAGE_EXTRA_          = PACKAGE_OPENGTS_ + "extra.";     // "org.opengts.extra."
    public  static final String PACKAGE_EXTRA_DBTOOLS_  = PACKAGE_EXTRA_   + "dbtools.";   // "org.opengts.extra.dbtools."

    // ------------------------------------------------------------------------

    public  static final String FMT_Prefix_             = "$";
    public  static final String FMT_Fault               = FMT_Prefix_ + "F";
    public  static final String FMT_Desc                = FMT_Prefix_ + "D";
    public  static final String FMT_Regex               = ".*\\"+FMT_Prefix_+"(F|D).*";

    public  static final String FAULT_DESC_SEPARATOR    = "; ";
    public  static final String FAULT_DESC_FORMAT_      = "["+FMT_Fault+"]"+FMT_Desc; // "[$F]$D"

    /**
    *** Returns true if the specified format String contains '$F' or '$D'
    **/
    public  static boolean IsDescFormat(String fmt) 
    {
        if (fmt == null) {
            return false;
        } else
        if (fmt.length() <= 1) {
            return false;
        } else
        if (!fmt.matches(FMT_Regex)) {
            return false;
        } else {
            return true;
        }
    }

    /**
    *** Returns a formatted fault description
    **/
    public static String FormatFaultDescription(String fmt, String faultID, String faultDesc)
    {
        String f = !StringTools.isBlank(fmt)? fmt : (FAULT_DESC_FORMAT_ + FAULT_DESC_SEPARATOR);
        f = StringTools.replace(f, FMT_Fault, faultID);
        f = StringTools.replace(f, FMT_Desc , faultDesc);
        return f;
    }

    // ------------------------------------------------------------------------

    public  static final String PROP_MIL[]          = new String[] { "mil"   , "MIL"    };
    public  static final String PROP_TYPE[]         = new String[] { "type"  , "TYPE"   };
    public  static final String PROP_MID[]          = new String[] { "mid"   , "MID"    };
    public  static final String PROP_SID[]          = new String[] { "sid"   , "SID"    };
    public  static final String PROP_PID[]          = new String[] { "pid"   , "PID"    };
    public  static final String PROP_FMI[]          = new String[] { "fmi"   , "FMI"    }; // Failure Mode Identifier
    public  static final String PROP_SPN[]          = new String[] { "spn"   , "SPN"    }; // Suspect Parameter Number
    public  static final String PROP_DTC[]          = new String[] { "dtc"   , "DTC"    }; // Diagnostic Trouble Code
    public  static final String PROP_MAKE[]         = new String[] { "make"  , "MAKE"   }; // Vehicle manufacturer/make (ie. "Toyota", etc)
    public  static final String PROP_COUNT[]        = new String[] { "count" , "COUNT"  }; // Occurrence Count [aka: OC]
    public  static final String PROP_ACTIVE[]       = new String[] { "active", "ACTIVE" };

    public  static final String NAME_J1708          = "J1708";
    public  static final String NAME_J1939          = "J1939";
    public  static final String NAME_OBDII          = "OBDII";

    public  static final String NAME_TYPE           = "TYPE";
    public  static final String NAME_MAKE           = "MAKE";
    public  static final String NAME_MID            = "MID";
    public  static final String NAME_MID_DESC       = NAME_MID + ".desc";
    public  static final String NAME_PID            = "PID";
    public  static final String NAME_PID_DESC       = NAME_PID + ".desc";
    public  static final String NAME_SID            = "SID";
    public  static final String NAME_SID_DESC       = NAME_SID + ".desc";
    public  static final String NAME_SPN            = "SPN";
    public  static final String NAME_SPN_DESC       = NAME_SPN + ".desc";
    public  static final String NAME_FMI            = "FMI";
    public  static final String NAME_FMI_DESC       = NAME_FMI + ".desc";
    public  static final String NAME_OC             = "OC";
    public  static final String NAME_OC_DESC        = NAME_FMI + ".desc";
    public  static final String NAME_DTC            = "DTC";
    public  static final String NAME_DTC_DESC       = NAME_DTC + ".desc";

    public  static final long   TYPE_MASK           = 0x7000000000000000L;
    public  static final int    TYPE_SHIFT          = 60;
    public  static final long   TYPE_J1708          = 0x0000000000000000L;
    public  static final long   TYPE_J1939          = 0x1000000000000000L;
    public  static final long   TYPE_OBDII          = 0x2000000000000000L;

    public  static final long   ACTIVE_MASK         = 0x0100000000000000L;
    public  static final int    ACTIVE_SHIFT        = 56;

    public  static final long   MID_MASK            = 0x00FFFFFF00000000L;
    public  static final int    MID_SHIFT           = 32;
    
    public  static final long   SPID_MASK           = 0x00000000FFFF0000L;
    public  static final int    SPID_SHIFT          = 16;
    public  static final long   SID_MASK            = 0x0000000080000000L;

    public  static final long   FMI_MASK            = 0x000000000000FF00L;
    public  static final int    FMI_SHIFT           =  8;

    public  static final long   COUNT_MASK          = 0x00000000000000FFL;
    public  static final int    COUNT_SHIFT         =  0;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static String PROP_DTOBDFault_                           = "DTOBDFault.";
    private static String PROP_DescProvClass                         = PROP_DTOBDFault_+"FaultDescriptionProviderClass";
    private static String PROP_FaultDescriptionProviderClass         = PROP_DescProvClass;
    public  static String PROP_FaultDescriptionProviderClass_J1708[] = { PROP_DescProvClass+".J1708", PROP_DescProvClass };
    public  static String PROP_FaultDescriptionProviderClass_J1939[] = { PROP_DescProvClass+".J1939", PROP_DescProvClass };
    public  static String PROP_FaultDescriptionProviderClass_OBDII[] = { PROP_DescProvClass+".OBDII", PROP_DescProvClass };

    private static String DFT_FaultDescClass_J1708                   = PACKAGE_EXTRA_DBTOOLS_ + "J1587";
    private static String DFT_FaultDescClass_J1939                   = null; // PACKAGE_EXTRA_DBTOOLS_ + "J1939";
    private static String DFT_FaultDescClass_OBDII                   = null; // PACKAGE_EXTRA_DBTOOLS_ + "OBDII";

    private static boolean                  faultDescDidInit         = false;
    private static FaultDescriptionProvider GetDescription_J1708     = null;
    private static FaultDescriptionProvider GetDescription_J1939     = null;
    private static FaultDescriptionProvider GetDescription_OBDII     = null;

    /**
    *** Fault-Code Description Provider interface
    **/
    public interface FaultDescriptionProvider
    {
        public String getFaultDescriptions(Properties fault);
    }

    /**
    *** Implementation of the FaultDescriptionProvider that will query all instantiated
    *** FaultDescriptionProvider instances
    **/
    public static class FaultDescriptionProviderImpl
        implements FaultDescriptionProvider
    {
        public FaultDescriptionProviderImpl() {
            super();
        }
        public String getFaultDescriptions(Properties faultP) {
            String type = (faultP != null)? faultP.getProperty(NAME_TYPE, null) : null;
            if (StringTools.isBlank(type)) {
                return "";
            }
            try {
                if (type.equalsIgnoreCase(NAME_J1708)) {
                    if (GetDescription_J1708 != null) {
                        return GetDescription_J1708.getFaultDescriptions(faultP);
                    }
                } else 
                if (type.equalsIgnoreCase(NAME_J1939)) {
                    if (GetDescription_J1939 != null) {
                        return GetDescription_J1939.getFaultDescriptions(faultP);
                    }
                } else
                if (type.equalsIgnoreCase(NAME_OBDII)) {
                    if (GetDescription_OBDII != null) {
                        return GetDescription_OBDII.getFaultDescriptions(faultP);
                    }
                }
            } catch (Throwable th) {
                Print.logException("External OBD description service return an exception", th);
            }
            return "";
        }
    }

    /**
    *** Instantiate the class name at the specified property key
    **/
    private static FaultDescriptionProvider _createFDP(String name, String propKey[], String dftClass)
    {
        String fdpClass = RTConfig.getString(propKey, dftClass);
        if (!StringTools.isBlank(fdpClass)) {
            try {
                Class<?> clazz = Class.forName(fdpClass);
                FaultDescriptionProvider fdp = (FaultDescriptionProvider)clazz.newInstance();
                Print.logInfo(name + " loaded fault code description provider: " + StringTools.className(fdp));
                return fdp;
            } catch (ClassNotFoundException cnfe) {
                // -- not found
                Print.logInfo(name + " fault code description provider not found: " + fdpClass);
              //Print.logException(name + " fault code description provider not found: " + fdpClass, cnfe);
            } catch (Throwable th) {
                // -- failed
                Print.logWarn(name + " fault code description provider not initialized: " + th);
            }
        }
        return null;
    }

    /**
    *** Initializes the external FaultDescriptionProvider instances.
    *** This method should be called after the RTConfig has been initialized
    **/
    public static void InitFaultDescriptionProvider()
    {
        if (!DTOBDFault.faultDescDidInit) {
            DTOBDFault.faultDescDidInit = true;
            GetDescription_J1708 = _createFDP(NAME_J1708, PROP_FaultDescriptionProviderClass_J1708, DFT_FaultDescClass_J1708);
            GetDescription_J1939 = _createFDP(NAME_J1939, PROP_FaultDescriptionProviderClass_J1939, DFT_FaultDescClass_J1939);
            GetDescription_OBDII = _createFDP(NAME_OBDII, PROP_FaultDescriptionProviderClass_OBDII, DFT_FaultDescClass_OBDII);
        }
    }

    /**
    *** Returns true if a FaultDescriptionProvider is provided for the specified fault code
    **/
    public static boolean HasFaultDescriptionProvider(RTProperties faultRTP)
    {
        if (faultRTP == null) {
            return false;
        } else
        if (DTOBDFault.IsJ1708(faultRTP)) {
            return (GetDescription_J1708 != null);
        } else
        if (DTOBDFault.IsJ1939(faultRTP)) {
            return (GetDescription_J1939 != null);
        } else
        if (DTOBDFault.IsOBDII(faultRTP)) {
            return (GetDescription_OBDII != null);
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // OBDII DTC code examples
    //  Example: P0171
    // 1st character identifies the system related to the trouble code.
    //  P = Powertrain
    //  B = Body
    //  C = Chassis
    //  U = Network/Undefined
    // 2nd digit identifies whether the code is a generic code (same on all 
    // OBD-II equpped vehicles), or a manufacturer specific code.
    //  0 = SAE/Generic
    //  1 = Manufacturer specific
    //  2 = SAE/Generic
    //  3 = SAE/Generic(P3400-P3499) or Manufacturer(P3000-P3399)
    // 3rd digit denotes the type of subsystem that pertains to the code
    //  0 = Fuel and Air Metering and Auxilliary Emission Controls
    //  1 = Emission Management (Fuel or Air)
    //  2 = Injector Circuit (Fuel or Air)
    //  3 = Ignition or Misfire
    //  4 = Auxilliary Emission Control
    //  5 = Vehicle Speed & Idle Control
    //  6 = Computer & Output Circuit
    //  7 = Transmission
    //  8 = Transmission
    //  9 = SAE Reserved / Transmission
    //  0 = SAE Reserved
    //  A = Hybrid Propulsion
    //  B - SAE Reserved
    //  C - SAE Reserved
    //  D - SAE Reserved
    //  E - SAE Reserved
    //  F - SAE Reserved
    // 4th/5th digits, along with the others, are variable, and relate to a 
    // particular problem. 
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static String GetGenericDescription_OBDII(String dtc)
    {
        String SEP = ","; // ", ";

        /* blank dtc? */
        dtc = StringTools.trim(dtc).toUpperCase();
        if (StringTools.isBlank(dtc)) {
            return "";
        }

        /* get characters */
        StringBuffer sb = new StringBuffer();
        char ch[] = StringTools.getChars(dtc); // at least one character

        /* first character */
        switch (ch[0]) {
            case 'P': 
                sb.append("Powertrain");
                break;
            case 'B': 
                sb.append("Body");
                break;
            case 'C': 
                sb.append("Chassis");
                break;
            case 'U': 
                sb.append("Network");
                break;
            default : 
                sb.append("?["+ch[0]+"]"); 
                break;
        }

        /* ["U"] Network (special case) */
        if (ch[0] == 'U') {
            if (!dtc.startsWith("U0")) {
                sb.append(SEP);
                sb.append("Manufacturer Specific");
            } else
            if (dtc.startsWith("U00")) { // U00XX
                sb.append(SEP);
                sb.append("Electrical");
            } else
            if (dtc.startsWith("U01")) { // U01XX
                sb.append(SEP);
                sb.append("Communication");
            } else
            if (dtc.startsWith("U02")) { // U02XX
                sb.append(SEP);
                sb.append("Communication");
            } else
            if (dtc.startsWith("U03")) { // U04XX
                sb.append(SEP);
                sb.append("Software");
            } else
            if (dtc.startsWith("U04")) { // U04XX
                sb.append(SEP);
                sb.append("Data");
            }
            return sb.toString();
        }

        /* second character */
        if (ch.length > 1) {
            sb.append(SEP);
            if (ch[1] == '0') {
                sb.append("SAE");
                // continue
            } else
            if (ch[1] == '1') {
                sb.append("Manufacturer Specific");
                return sb.toString(); // exit now
            } else
            if (ch[1] == '2') {
                sb.append("SAE");
                // continue
            } else
            if (ch[1] == '3') {
                if (ch.length > 2) {
                    if ((ch[2] >= '0') && (ch[2] <= '3')) {
                        sb.append("Manufacturer Specific");
                        return sb.toString(); // exit now
                    } else {
                        sb.append("SAE");
                        // continue
                    }
                } else {
                    sb.append("SAE");
                    // continue
                }
            } else {
                sb.append("?["+ch[1]+"]");
                return sb.toString(); // exit now
            }
        }

        /* third character ('P' only) */
        if ((ch[0] == 'P') && (ch.length > 2)) {
            sb.append(SEP);
            switch (ch[2]) {
                case '0': sb.append("Fuel/Air Metering and Aux Emissions"); break;
                case '1': sb.append("Fuel/Air Metering");                   break;
                case '2': sb.append("Fuel/Air Metering");                   break;
                case '3': sb.append("Ignition/Misfire");                    break;
                case '4': sb.append("Aux Emissions");                       break;
                case '5': sb.append("Speed/Idle/Inputs");                   break;
                case '6': sb.append("Computer/Output");                     break;
                case '7': sb.append("Transmission");                        break;
                case '8': sb.append("Transmission");                        break;
                case '9': sb.append("Transmission");                        break;
                case 'A': sb.append("Hybrid Propulsion");                   break;
                case 'B': sb.append("Reserved");                            break;
                case 'C': sb.append("Reserved");                            break;
                case 'D': sb.append("Reserved");                            break;
                case 'E': sb.append("Reserved");                            break;
                case 'F': sb.append("Reserved");                            break;
            }
        }

        /* return description */
        return sb.toString();

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static long EncodeActive(boolean active)
    {
        return active? ACTIVE_MASK : 0L;
    }
    
    public static boolean DecodeActive(long fault)
    {
        if (DTOBDFault.IsJ1708(fault)) {
            return ((fault & ACTIVE_MASK) != 0L);
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------

    public static long EncodeSystem(char sys)
    {
        // -- OBDII: 'P', 'B', 'C', 'U' Powertrain
        if (Character.isLetterOrDigit(sys)) {
            return ((long)sys << MID_SHIFT) & MID_MASK;
        } else {
            return 0L;
        }
    }

    public static long EncodeSystem(int sys)
    {
        // -- OBDII: 'P', 'B', 'C', 'U' Powertrain
        // -- J1708: MID
        // -- J1939: SPN
        if (sys > 0) {
            return ((long)sys << MID_SHIFT) & MID_MASK;
        } else {
            return 0L;
        }
    }

    public static int DecodeSystem(long fault)
    {
        // -- OBDII: 'P', 'B', 'C', 'U' Powertrain
        // -- J1708: MID
        // -- J1939: SPN
        return (int)((fault & MID_MASK) >> MID_SHIFT);
    }
    
    // ------------------------------------------------------------------------

    public static long EncodeSPID(int sub)
    {
        if (sub > 0) {
            return ((long)sub << SPID_SHIFT) & SPID_MASK;
        } else {
            return 0L;
        }
    }
    
    public static int DecodeSPID(long fault)
    {
        return (int)((fault & SPID_MASK) >> SPID_SHIFT);
    }

    public static int DecodePidSid(long fault)
    {
        return DecodeSPID(fault) & 0x0FFF;
    }

    // ------------------------------------------------------------------------

    public static long EncodeFMI(int fmi)
    {
        if (fmi > 0) {
            return ((long)fmi << FMI_SHIFT) & FMI_MASK;
        } else {
            return 0L;
        }
    }
    
    public static int DecodeFMI(long fault)
    {
        return (int)((fault & FMI_MASK) >> FMI_SHIFT);
    }

    // ------------------------------------------------------------------------

    public static long EncodeCount(int count)
    {
        if (count > 0) {
            return ((long)count << COUNT_SHIFT) & COUNT_MASK;
        } else {
            return 0L;
        }
    }
    
    public static int DecodeCount(long fault)
    {
        return (int)((fault & COUNT_MASK) >> COUNT_SHIFT);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //  (OLD)J1708: type=j1708 mil=1 mid=123 pid=123 fmi=1 count=1 active=true
    //  J1708: type=j1708 mil=1 dtc=MID/sPID/FMI/OC,MID/sPID/FMI/OC
    //  J1939: type=j1939 mil=1 dtc=SPN/FMI/OC,SPN/FMI/OC
    //  OBDII: type=obdii mil=1 dtc=P0071 make=TOYOTA

    public static RTProperties GetRTProperties_OBDII(String dtc[])
    {
        return GetRTProperties_OBDII(dtc, null);
    }
    public static RTProperties GetRTProperties_OBDII(String dtc[], String make)
    {
        // -- OBDII: type=obdii mil=1 dtc=P0071,P0420 make=TOYOTA
        String dtcStr = !ListTools.isEmpty(dtc)? StringTools.join(dtc,",") : "";
        RTProperties rtp = new RTProperties();
        rtp.setString(PROP_TYPE[0], NAME_OBDII);
        if (!StringTools.isBlank(dtcStr)) {
            rtp.setString(PROP_MIL[0], "1"); // assumed
            rtp.setString(PROP_DTC[0], dtcStr.trim());
            if (!StringTools.isBlank(make)) {
                rtp.setString(PROP_MAKE[0], make.trim());
            }
        } else {
            rtp.setString(PROP_MIL[0], "0");
        }
        return rtp;
    }

    /**
    *** Returns the full OBDII fault code property String
    **/
    public static RTProperties GetRTProperties_OBDII(long fault)
    {
        if ((fault & TYPE_MASK) == TYPE_OBDII) {
            String dtc = DTOBDFault._GetFaultString(fault);
            return DTOBDFault.GetRTProperties_OBDII(new String[] { dtc });
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the J1708 sPID/FMI/OC fault code element String
    **/
    public static String J1708FaultString(boolean active, int MID, int sPID, boolean isSID, int FMI, int OC)
    {
        return DTOBDFault.J1708FaultString(active, MID, sPID, isSID, FMI, OC, new StringBuffer()).toString();
    }

    /**
    *** Returns the J1708 sPID/FMI/OC fault code element String
    **/
    public static StringBuffer J1708FaultString(boolean active, int mid, int pidSid, boolean isSid, int fmi, int oc, StringBuffer sb)
    {
        // -- "MID/sPID/FMI/OC"
        StringBuffer _sb = (sb != null)? sb : new StringBuffer();
        if (!active) {
            _sb.append("[");
        }
        _sb.append(mid);
        _sb.append("/");
        if (isSid) { _sb.append("s"); }
        _sb.append(pidSid);
        _sb.append("/");
        _sb.append(fmi);
        if (oc > 1) {
            _sb.append("/");
            _sb.append(oc);
        }
        if (!active) {
            _sb.append("]");
        }
        return _sb;
    }

    // --------------------------------

    /**
    *** Returns the full J1708 fault code property String
    **/
    public static RTProperties GetRTProperties_J1708(String... dtc)
    {
        // -- J1708: type=j1708 mil=1 dtc=MID/sPID/FMI/OC,MID/sPID/FMI/OC
        String dtcStr = !ListTools.isEmpty(dtc)? StringTools.join(dtc,",") : "";
        RTProperties rtp = new RTProperties();
        rtp.setString(PROP_TYPE[0],NAME_J1708);
        if (!StringTools.isBlank(dtcStr)) {
            rtp.setString(PROP_MIL[0],"1");
            rtp.setString(PROP_DTC[0],dtcStr);
        } else {
            rtp.setString(PROP_MIL[0],"0");
        }
        return rtp;
    }

    /**
    *** Returns the full J1708 fault code property String
    **/
    public static RTProperties GetRTProperties_J1708(long fault)
    {
        if ((fault & TYPE_MASK) == TYPE_J1708) {
            boolean active = DTOBDFault.DecodeActive(fault);
            int     mid    = DTOBDFault.DecodeSystem(fault); // J1708: MID
            int     pidSid = DTOBDFault.DecodePidSid(fault);
            boolean isSid  = DTOBDFault.IsJ1708_SID(fault);
            int     fmi    = DTOBDFault.DecodeFMI(fault);
            int     oc     = DTOBDFault.DecodeCount(fault);
            String  dtc    = DTOBDFault.J1708FaultString(active, mid, pidSid, isSid, fmi, oc);
            return DTOBDFault.GetRTProperties_J1708(dtc);
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the J1939 SPN/FMI/OC fault code element String
    **/
    public static String J1939FaultString(boolean active, int spn, int fmi, int oc)
    {
        return DTOBDFault.J1939FaultString(active, spn, fmi, oc, new StringBuffer()).toString();
    }

    /**
    *** Returns the J1939 SPN/FMI/OC fault code element String
    **/
    public static StringBuffer J1939FaultString(boolean active, int spn, int fmi, int oc, StringBuffer sb)
    {
        // -- "SPN/FMI", "SPN/FMI/OC"
        StringBuffer _sb = (sb != null)? sb : new StringBuffer();
        if (!active) {
            _sb.append("[");
        }
        _sb.append(spn);
        _sb.append("/");
        _sb.append(fmi);
        if (oc > 1) {
            _sb.append("/");
            _sb.append(oc);
        }
        if (!active) {
            _sb.append("]");
        }
        return _sb;
    }

    // --------------------------------

    /**
    *** Returns the full J1939 fault code property String
    **/
    public static RTProperties GetRTProperties_J1939(String... dtc)
    {
        // -- J1939: type=j1939 mil=1 dtc=SPN/FMI/OC,SPN/FMI/OC
        String dtcStr = !ListTools.isEmpty(dtc)? StringTools.join(dtc,",") : "";
        RTProperties rtp = new RTProperties();
        rtp.setString(PROP_TYPE[0], NAME_J1939);
        if (!StringTools.isBlank(dtcStr)) {
            rtp.setString(PROP_MIL[0], "1");
            rtp.setString(PROP_DTC[0], dtcStr);
        } else {
            rtp.setString(PROP_MIL[0], "0");
        }
        return rtp;
    }

    /**
    *** Returns the full J1939 fault code property String
    **/
    public static RTProperties GetRTProperties_J1939(int spn, int fmi, int oc)
    {
        // -- J1939: type=j1939 mil=1 dtc=SPN/FMI/OC,SPN/FMI/OC
        // -    old: type=j1939 mil=1 spn=1234 fmi=12 count=1
        String dtc = J1939FaultString(true, spn, fmi, oc);
        return DTOBDFault.GetRTProperties_J1939(dtc);
    }

    /**
    *** Returns the full J1939 fault code property String
    **/
    public static RTProperties GetRTProperties_J1939(long fault)
    {
        if ((fault & TYPE_MASK) == TYPE_J1939) {
            boolean active = DTOBDFault.DecodeActive(fault);
            int     spn    = DTOBDFault.DecodeSystem(fault); // J!939: SPN
            int     fmi    = DTOBDFault.DecodeFMI(fault);
            int     oc     = DTOBDFault.DecodeCount(fault);
            String  dtc    = DTOBDFault.J1939FaultString(active, spn, fmi, oc);
            return DTOBDFault.GetRTProperties_J1939(dtc);
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified fault codes are the same type
    **/
    public static boolean IsTypeEqual(long fault1, long fault2)
    {
        return ((fault1 & TYPE_MASK) == (fault2 & TYPE_MASK));
    }

    /** 
    *** Returns true if the specified fault properties are the same type
    **/
    public static boolean IsTypeEqual(RTProperties rtpFault1, RTProperties rtpFault2)
    {
        if (rtpFault1 == rtpFault2) {
            return true; // same object or both are null
        } else
        if ((rtpFault1 == null) || (rtpFault2 == null)) {
            return false; // one of these is null
        } else {
            String type1 = rtpFault1.getString(PROP_TYPE,"");
            String type2 = rtpFault2.getString(PROP_TYPE,"");
            return type1.equalsIgnoreCase(type2);
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the fault code property String from the specified fault value
    **/
    public static RTProperties GetRTProperties(long fault) // DecodeFault(long fault)
    {
        if (fault == 0L) {
            return null;
        } else
        if ((fault & TYPE_MASK) == TYPE_J1708) {
            // -- J1708: type=j1708 mil=1 dtc=MID/sPID/FMI/OC,MID/sPID/FMI/OC
            // --   old: type=j1708 mil=1 mid=123 pid=123 fmi=1 count=1 active=false
            return DTOBDFault.GetRTProperties_J1708(fault);
        } else
        if ((fault & TYPE_MASK) == TYPE_J1939) {
            int spn = DecodeSystem(fault); // J1939: SPN
            int fmi = DecodeFMI(fault);
            int oc  = DecodeCount(fault);
            return DTOBDFault.GetRTProperties_J1939(spn, fmi, oc);
        } else
        if ((fault & TYPE_MASK) == TYPE_OBDII) {
            String dtc = DTOBDFault._GetFaultString(fault);
            return DTOBDFault.GetRTProperties_OBDII(new String[] { dtc });
        } else {
            // -- unrecognized/empty
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /* return string representation of fault code */
    private static String _GetFaultString(long fault)
    {
        RTProperties faultRTP = DTOBDFault.GetRTProperties(fault);
        return DTOBDFault.GetFaultString(faultRTP, true/*firstOnly*/);
    }

    /* return string representation of fault code */
    public static String GetFaultString(RTProperties faultRTP, boolean firstOnly)
    {

        /* empty fault properties */
        if ((faultRTP == null) || faultRTP.isEmpty()) {
            return "";
        }

        /* assemble returned String */
        StringBuffer sb = new StringBuffer();
        boolean active = faultRTP.getBoolean(PROP_ACTIVE, true);
        if (!active) {
            // -- wrap non-active faults in [...] brackets
            sb.append("[");
        }

        /* parse by type */
        String type = faultRTP.getString(PROP_TYPE,"");
        if (type.equalsIgnoreCase(NAME_J1708)) {
            // -- RTP Input:
            // -    "type=j1708 mil=1 dtc=MID/sPID/FMI/OC,MID/sPID/FMI/OC"
            // -- String Output:
            // -    SID: "128/s123/1"
            // -    PID: "128/123/1"
            if (faultRTP.hasProperty(PROP_DTC)) {
                String dtcStr = faultRTP.getString(PROP_DTC,""); // ie. "SPN/FMI/OC,..."
                int p = firstOnly? dtcStr.indexOf(",") : -1;
                sb.append((p >= 0)? dtcStr.substring(0,p) : dtcStr);
            } else {
                // -- old format
                int     mid    = faultRTP.getInt(PROP_MID, 0);
                int     pidSid;
                boolean isSid;
                int     fmi    = faultRTP.getInt(PROP_FMI, 0);
                int     oc     = 1;
                if (faultRTP.hasProperty(PROP_SID)) {
                    pidSid = faultRTP.getInt(PROP_SID, 0);
                    isSid  = true;
                } else {
                    pidSid = faultRTP.getInt(PROP_PID, 0);
                    isSid  = false;
                }
                DTOBDFault.J1708FaultString(active, mid, pidSid, isSid, fmi, 1, sb);
            }
        } else
        if (type.equalsIgnoreCase(NAME_J1939)) {
            // -- RTP Input:
            // -    "type=i1939 mil=1 dtc=SPN/FMI[/OC]"
            // -- String Output:
            // -    "SPN/FMI[/OC]" (ie. "128/1")
            if (faultRTP.hasProperty(PROP_DTC)) {
                String dtcStr = faultRTP.getString(PROP_DTC,""); // ie. "SPN/FMI/OC,..."
                int p = firstOnly? dtcStr.indexOf(",") : -1;
                sb.append((p >= 0)? dtcStr.substring(0,p) : dtcStr);
            } else {
                // -- old format
                int spn = faultRTP.getInt(PROP_SPN  , -1);
                int fmi = faultRTP.getInt(PROP_FMI  , -1);
                int oc  = faultRTP.getInt(PROP_COUNT,  1);
                if (spn >= 0) {
                    J1939FaultString(active,spn,fmi,oc,sb); // "SPN/FMI/OC"
                }
            }
        } else
        if (type.equalsIgnoreCase(NAME_OBDII)) {
            // -- RTP Input:
            // -    "type=obdii mil=1 dtc=P0071,P0321 make=TOYOTA"
            // -- String Output:
            // -    "P0071,P0321" [was "024C"]
            if (faultRTP.hasProperty(PROP_DTC)) {
                String dtcStr = faultRTP.getString(PROP_DTC,""); // ie "P0123,P0321,P1234"
                int p = firstOnly? dtcStr.indexOf(",") : -1;
                sb.append((p > 0)? dtcStr.substring(0,p) : dtcStr);
            }
        } else {
            // -- unrecognized
            sb.append("?").append(type).append("?");
        }

        /* return (may be blank) */
        if (!active) {
            sb.append("]");
        }
        return sb.toString();

    }

    // --------------------------------

    /* return fault header */
    public static String GetFaultHeader(RTProperties faultRTP)
    {
        if ((faultRTP != null) && !faultRTP.isEmpty()) {
            String type = faultRTP.getString(PROP_TYPE,"");
            if (type.equalsIgnoreCase(NAME_J1708)) {
                if (faultRTP.hasProperty(PROP_SID)) {
                    return NAME_MID + "/" + NAME_SID + "/" + NAME_FMI;
                } else {
                    return NAME_MID + "/" + NAME_PID + "/" + NAME_FMI;
                }
            } else
            if (type.equalsIgnoreCase(NAME_J1939)) {
                return NAME_SPN + "/" + NAME_FMI + "/" + NAME_OC;
            } else
            if (type.equalsIgnoreCase(NAME_OBDII)) {
                return NAME_DTC;
            } else {
                return "";
            }
        }
        return "";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* encode "type=<type> ..." into long value */
    public static long EncodeFault(RTProperties rtp)
    {
        // --
        if ((rtp == null) || rtp.isEmpty()) {
            return 0L;
        }
        // --
        String type = rtp.getString(PROP_TYPE,"");
        if (type.equalsIgnoreCase(NAME_J1708)) {
            int     mid    = rtp.getInt(PROP_MID,0);
            int     sid    = rtp.getInt(PROP_SID,-1);
            int     pid    = rtp.getInt(PROP_PID,-1);
            int     pidSid = (sid >= 0)? sid : pid;
            int     fmi    = rtp.getInt(PROP_FMI,0);
            int     count  = rtp.getInt(PROP_COUNT,0);
            boolean active = rtp.getBoolean(PROP_ACTIVE,true);
            return EncodeFault_J1708(mid, (sid >= 0), pidSid, fmi, count, active);
        } else
        if (type.equalsIgnoreCase(NAME_J1939)) {
            String  dtcStr = rtp.getString(PROP_DTC,""); // "SPN/FMI/OC"
            if (!StringTools.isBlank(dtcStr)) {
                // -- DTC=SPN/FMI/OC
                return EncodeFault_J1939(dtcStr);
            } else {
                // -- SPN=SPN
                // -- FMI=FMI
                // -- COUNT=COUNT
                int spn = rtp.getInt(PROP_SPN,-1);
                int fmi = rtp.getInt(PROP_FMI,-1);
                int oc  = rtp.getInt(PROP_COUNT,0);
                return EncodeFault_J1939(spn, fmi, oc);
            }
        } else
        if (type.equalsIgnoreCase(NAME_OBDII)) {
            String dtcStr = rtp.getString(PROP_DTC,""); // "P0071,P0420"
            return EncodeFault_OBDII(dtcStr);
        } else {
            return 0L;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** J1708: Encodes MID,PID/SID,FMI into a fault code
    **/
    public static long EncodeFault_J1708(int mid, boolean isSID, int pidSid, int fmi, int count, boolean active)
    {
        long faultCode = TYPE_J1708;

        /* check SPN/FMI/OC */
        if ((mid < 0) || (fmi < 0)) {
            return faultCode;
        }

        /* encode */
        int spid = isSID? (pidSid | 0x8000) : pidSid;
        faultCode |= EncodeActive(active);      // [ACTIVE_MASK]    0x0100000000000000
        faultCode |= EncodeSystem(mid);         // [MID_MASK]       0x00FFFFFF00000000
        faultCode |= EncodeSPID(spid);          // [SPID_MASK]      0x00000000FFFF0000
        faultCode |= EncodeFMI(fmi);            // [FMI_MASK]       0x000000000000FF00
        faultCode |= EncodeCount(count);        // [COUNT_MASK]     0x00000000000000FF
        return faultCode;

    }

    /** 
    *** Returns true if the specified fault code is a J1708
    **/
    public static boolean IsJ1708(long fault)
    {
        return ((fault & TYPE_MASK) == TYPE_J1708);
    }

    /** 
    *** Returns true if the specified fault property is a J1708
    **/
    public static boolean IsJ1708(RTProperties rtpFault)
    {
        return ((rtpFault != null) && rtpFault.getString(PROP_TYPE,"").equalsIgnoreCase(NAME_J1708));
    }

    /** 
    *** Returns true if the specified fault code is a J1708 SID
    **/
    public static boolean IsJ1708_SID(long fault)
    {
        return DTOBDFault.IsJ1708(fault) && ((fault & SID_MASK) != 0L);
    }

    /** 
    *** Returns true if the specified fault property is a J1708 SID
    **/
    public static boolean IsJ1708_SID(RTProperties rtpFault)
    {
        return DTOBDFault.IsJ1708(rtpFault) && rtpFault.hasProperty(PROP_SID);
    }

    /** 
    *** Returns true if the specified fault code is a J1708 PID
    **/
    public static boolean IsJ1708_PID(long fault)
    {
        return DTOBDFault.IsJ1708(fault) && ((fault & SID_MASK) == 0L);
    }

    /** 
    *** Returns true if the specified fault property is a J1708 PID
    **/
    public static boolean IsJ1708_PID(RTProperties rtpFault)
    {
        return DTOBDFault.IsJ1708(rtpFault) && rtpFault.hasProperty(PROP_PID);
    }

    // ------------------------------------------------------------------------

    /**
    *** J1939: Encodes "SPN/FMI/OC" into a fault code
    **/
    public static long EncodeFault_J1939(String dtcStr)
    {
        long faultCode = TYPE_J1939;

        /* trim */
        dtcStr = StringTools.trim(dtcStr);
        if (dtcStr.indexOf(",") >= 0) {
            dtcStr = dtcStr.substring(0,dtcStr.indexOf(",")).trim();
        }
        if (dtcStr.equals("")) {
            return faultCode;
        }

        /* separate/check SPN/FMI/OC */
        String s[] = StringTools.split(dtcStr,'/');
        int spn = (s.length > 0)? StringTools.parseInt(s[0],-1) : -1;
        int fmi = (s.length > 1)? StringTools.parseInt(s[1],-1) : -1;
        int oc  = (s.length > 2)? StringTools.parseInt(s[2], 0) :  0;
        if ((spn < 0) || (fmi < 0)) {
            return faultCode;
        }

        /* encode */
        faultCode |= EncodeActive(true);        // [ACTIVE_MASK]    0x0100000000000000
        faultCode |= EncodeSystem(spn);         // [MID_MASK]       0x00FFFFFF00000000
        faultCode |= EncodeFMI(fmi);            // [FMI_MASK]       0x000000000000FF00
        faultCode |= EncodeCount(oc);           // [COUNT_MASK]     0x00000000000000FF
        return faultCode;

    }

    /**
    *** J1939: Encodes SPN/FMI/OC into a fault code
    **/
    public static long EncodeFault_J1939(int spn, int fmi, int oc)
    {
        long faultCode = TYPE_J1939;

        /* check SPN/FMI/OC */
        if ((spn < 0) || (fmi < 0)) {
            return faultCode;
        }

        /* encode */
        faultCode |= EncodeActive(true);        // [ACTIVE_MASK]    0x0100000000000000
        faultCode |= EncodeSystem(spn);         // [MID_MASK]       0x00FFFFFF00000000
        faultCode |= EncodeFMI(fmi);            // [FMI_MASK]       0x000000000000FF00
        faultCode |= EncodeCount(oc);           // [COUNT_MASK]     0x00000000000000FF
        return faultCode;

    }

    /** 
    *** Returns true if the specified fault code is J1939
    **/
    public static boolean IsJ1939(long fault)
    {
        return ((fault & TYPE_MASK) == TYPE_J1939);
    }

    /** 
    *** Returns true if the specified fault property is J1939
    **/
    public static boolean IsJ1939(RTProperties rtpFault)
    {
        return ((rtpFault != null) && rtpFault.getString(PROP_TYPE,"").equalsIgnoreCase(NAME_J1939));
    }

    // ------------------------------------------------------------------------

    /**
    *** OBDII: Encodes DTC into a fault code
    **/
    public static long EncodeFault_OBDII(String dtcStr)
    {
        long faultCode = TYPE_OBDII;

        /* trim */
        dtcStr = StringTools.trim(dtcStr);
        if (dtcStr.indexOf(",") >= 0) { // get first DTC only
            dtcStr = dtcStr.substring(0,dtcStr.indexOf(",")).trim();
        }
        if (dtcStr.equals("")) {
            return faultCode;
        }

        /* check length */
        if (dtcStr.length() == 4) {
            dtcStr = "U" + dtcStr; // network error code?
        } else
        if (dtcStr.length() != 5) {
            return faultCode;
        }

        /* active */
        faultCode |= EncodeActive(true);               // [ACTIVE_MASK]    0x0100000000000000

        /* encode system character (ie. "Powertrain") */
        faultCode |= EncodeSystem(dtcStr.charAt(0));   // [MID_MASK]       0x00FFFFFF00000000

        /* encode manufacturer specific and subsystem */
        //int mfgCode = StringTools.parseInt(dtcStr.substring(1,2),0); // .X...
        //int spid    = (mfgCode != 0)? 0x8000 : 0;
        //int subSys  = StringTools.parseInt(dtcStr.substring(2,5),0); // ..XXX   
        //spid |= (subSys & 0xFFF); // BCD encoded
        int spid = StringTools.parseHex(dtcStr.substring(1,5),0); //   [2.4.9-B04] encode to HEX
        faultCode |= EncodeSPID(spid);                 // [SPID_MASK]      0x00000000FFFF0000

        /* return fault code */
        return faultCode;

    }

    /** 
    *** Returns true if the specified fault code is OBDII
    **/
    public static boolean IsOBDII(long fault)
    {
        return ((fault & TYPE_MASK) == TYPE_OBDII);
    }

    /** 
    *** Returns true if the specified fault property is OBDII
    **/
    public static boolean IsOBDII(RTProperties rtpFault)
    {
        return ((rtpFault != null) && rtpFault.getString(PROP_TYPE,"").equalsIgnoreCase(NAME_OBDII));
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a multi-line description of the specified fault properties
    **/
    public static String GetFaultDescription(RTProperties faultRTP, String make, Locale locale, boolean firstOnly, String fmt)
    {
        // -- default format
        if (StringTools.isBlank(fmt)) {
            fmt = FAULT_DESC_FORMAT_ + FAULT_DESC_SEPARATOR;
        }
        // -- retrieve fault description and format
        try {
            String type = faultRTP.getString(PROP_TYPE,"");
            if (type.equalsIgnoreCase(NAME_J1708)) {
                int     mid    = faultRTP.getInt(PROP_MID  ,  0);
                int     sid    = faultRTP.getInt(PROP_SID  , -1);
                int     pid    = faultRTP.getInt(PROP_PID  , -1);
                boolean isSid  = (sid >= 0)? true : false;
                int     pidSid = isSid? sid : pid;
                int     fmi    = faultRTP.getInt(PROP_FMI  ,  0);
                int     count  = faultRTP.getInt(PROP_COUNT,  0);
                boolean active = faultRTP.getBoolean(PROP_ACTIVE,true);
                if (GetDescription_J1708 != null) {
                    Properties p = new Properties();
                    p.setProperty(NAME_TYPE, NAME_J1708);
                    p.setProperty(NAME_MAKE, make);
                    p.setProperty(NAME_MID , String.valueOf(mid));
                    p.setProperty((isSid?NAME_SID:NAME_PID), String.valueOf(pidSid));
                    p.setProperty(NAME_FMI , String.valueOf(fmi));
                    return GetDescription_J1708.getFaultDescriptions(p);
                } else {
                    StringBuffer sb = new StringBuffer();
                    sb.append(NAME_MID + "=" + StringTools.format(mid,"000") + "/");
                    if (isSid) {
                        sb.append(NAME_SID + "=" + StringTools.format(pidSid,"000") + "/");
                    } else {
                        sb.append(NAME_PID + "=" + StringTools.format(pidSid,"000") + "/");
                    }
                    sb.append(NAME_FMI + "=" + StringTools.format(fmi,"000"));
                    return sb.toString();
                }
            } else
            if (type.equalsIgnoreCase(NAME_J1939)) {
                String dtcStr = faultRTP.getString(PROP_DTC,""); // "SPN/FMI/OC"
                String dtc[] = null;
                if (!StringTools.isBlank(dtcStr)) {
                    dtc = StringTools.split(dtcStr,','); // non-null
                } else {
                    int spn = faultRTP.getInt(PROP_SPN  , -1);
                    int fmi = faultRTP.getInt(PROP_FMI  , -1);
                    int oc  = faultRTP.getInt(PROP_COUNT,  0);
                    if (spn >= 0) {
                        dtc = new String[] { spn+"/"+fmi+"/"+oc };
                    } else {
                        return "";
                    }
                }
                StringBuffer descSB = new StringBuffer();
                int dtcLen = ListTools.size(dtc.length);
                for (int d = 0; d < dtcLen; d++) {
                    String s[] = StringTools.split(dtc[d], '/');
                    int  spn = (s.length > 0)? StringTools.parseInt(s[0],-1) : -1;
                    int  fmi = (s.length > 1)? StringTools.parseInt(s[1],-1) : -1;
                    int  oc  = (s.length > 2)? StringTools.parseInt(s[2], 0) :  0;
                    String faultID   = dtc[d];
                    String faultDesc = null;
                    String descSrc   = ""; // debugging purposes
                    // -- try FaultDescriptionProvider
                    if (GetDescription_J1939 != null) {
                        Properties p = new Properties();
                        p.setProperty(NAME_TYPE, NAME_J1939);
                        p.setProperty(NAME_MAKE, make);
                        p.setProperty(NAME_SPN , String.valueOf(spn));
                        p.setProperty(NAME_FMI , String.valueOf(fmi));
                        p.setProperty(NAME_OC  , String.valueOf(oc));
                        String fd = GetDescription_J1939.getFaultDescriptions(p);
                        if (!StringTools.isBlank(fd)) {
                            faultDesc = fd;
                            descSrc   = "Provider";
                        }
                    }
                    // -- default to generic description
                    if (StringTools.isBlank(faultDesc)) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(NAME_SPN + "=" + StringTools.format(spn,"000") + "/");
                        sb.append(NAME_FMI + "=" + StringTools.format(fmi,"000"));
                        faultDesc = sb.toString();
                        descSrc   = "Default";
                    }
                    // -- format and append to description list
                    String fmtDesc = DTOBDFault.FormatFaultDescription(fmt,faultID,faultDesc);
                    //Print.logInfo(descSrc+": " + fmtDesc);
                    descSB.append(fmtDesc);
                    // -- exit loop if we are only interested in the first fault
                    if (firstOnly) {
                        break;
                    }
                }
                return descSB.toString(); // remove trailing '\n', if present?
            } else
            if (type.equalsIgnoreCase(NAME_OBDII)) {
                String dtcStr = faultRTP.getString(PROP_DTC,""); // "P0071,P0420"
                String dtc[] = null;
                if (!StringTools.isBlank(dtcStr)) {
                    dtc = StringTools.split(dtcStr,','); // non-null
                    //Print.logInfo("FaultDescription DTC[x"+ListTools.size(dtc)+"]="+StringTools.join(dtc,'|'));
                } else {
                    Print.logWarn("No OBDII DTC fault codes specified");
                    return "";
                }
                StringBuffer descSB = new StringBuffer();
                int dtcLen = ListTools.size(dtc);
                for (int d = 0; d < dtcLen; d++) {
                    String faultID   = dtc[d];
                    String faultDesc = null;
                    String descSrc   = ""; // debugging purposes
                    // -- try FaultDescriptionProvider
                    if (GetDescription_OBDII != null) {
                        Properties p = new Properties();
                        p.setProperty(NAME_TYPE, NAME_OBDII);
                        p.setProperty(NAME_MAKE, make);
                        p.setProperty(NAME_DTC , dtc[d]);
                        String fd = GetDescription_OBDII.getFaultDescriptions(p);
                        if (!StringTools.isBlank(fd)) {
                            faultDesc = fd;
                            descSrc   = "Provider";
                        }
                    }
                    // -- default to generic description
                    if (StringTools.isBlank(faultDesc)) {
                        String fd = DTOBDFault.GetGenericDescription_OBDII(dtc[d]);
                        if (!StringTools.isBlank(fd)) {
                            faultDesc = fd;
                            descSrc   = "Default";
                        } else {
                            faultDesc = "???";
                            descSrc   = "None";
                        }
                    }
                    // -- format and append to description list
                    String fmtDesc = DTOBDFault.FormatFaultDescription(fmt,faultID,faultDesc);
                    //Print.logInfo(descSrc+": " + fmtDesc);
                    descSB.append(fmtDesc);
                    // -- exit loop if we are only interested in the first fault
                    if (firstOnly) {
                        break;
                    }
                }
                return descSB.toString(); // remove trailing '\n', if present?
            } else {
                return "";
            }
        } catch (Throwable th) {
            // -- may occur when invoking external falt code description provider
            Print.logError("External fault code description provider error: " + th);
            return "";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private long         faultCode = 0L;
    private RTProperties faultRTP  = null;

    /**
    *** J1708 Constructor
    **/
    public DTOBDFault(int mid, boolean isSid, int pidSid, int fmi, int count, boolean active)
    {
        this.faultCode = DTOBDFault.EncodeFault_J1708(mid, isSid, pidSid, fmi, count, active);
        this.faultRTP  = DTOBDFault.GetRTProperties(this.faultCode);
    }

    /**
    *** J1939 Constructor
    **/
    public DTOBDFault(int spn, int fmi, int oc)
    {
        this.faultCode = DTOBDFault.EncodeFault_J1939(spn, fmi, oc);
        this.faultRTP  = DTOBDFault.GetRTProperties(this.faultCode);
    }

    /**
    *** OBDII Constructor
    **/
    public DTOBDFault(String dtc)
    {
        this.faultCode = DTOBDFault.EncodeFault_OBDII(dtc);
        this.faultRTP  = DTOBDFault.GetRTProperties(this.faultCode);
    }

    /**
    *** Constructor
    **/
    public DTOBDFault(long faultCode)
    {
        this.faultCode = faultCode;
        this.faultRTP  = DTOBDFault.GetRTProperties(this.faultCode);
    }

    /**
    *** Constructor
    **/
    public DTOBDFault(ResultSet rs, String fldName)
        throws SQLException
    {
        super(rs, fldName);
        if (rs != null) {
            this.faultCode = rs.getLong(fldName);
            this.faultRTP  = DTOBDFault.GetRTProperties(this.faultCode);
        } else {
            this.faultCode = 0L;
            this.faultRTP  = null;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the fault code for this instance
    **/
    public long getFaultCode()
    {
        return this.faultCode;
    }

    /**
    *** Returns a description of this instance
    **/
    public String getDescription()
    {
        String  make      = null;
        Locale  locale    = null;
        boolean firstOnly = true;
        String  fmt       = null;
        return DTOBDFault.GetFaultDescription(this.faultRTP, make, locale, firstOnly, fmt);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this instance represents a J1708 fault code
    **/
    public boolean isJ1708()
    {
        return DTOBDFault.IsJ1708(this.faultRTP);
    }

    /**
    *** Returns true if this instance represents a J1939 fault code
    **/
    public boolean isJ1939()
    {
        return DTOBDFault.IsJ1939(this.faultRTP);
    }

    /**
    *** Returns true if this instance represents an OBDII fault code
    **/
    public boolean isOBDII()
    {
        return DTOBDFault.IsOBDII(this.faultRTP);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the datatype MySQL column Object
    **/
    public Object getObject()
    {
        return new Long(this.getFaultCode());
    }

    /**
    *** Gets a String representation of this instance 
    **/
    public String toString()
    {
        return "0x" + StringTools.toHexString(this.getFaultCode());
    }

    /**
    *** Returns true if this instance is equivalent to the specified instance
    **/
    public boolean equals(Object other)
    {
        if (other instanceof DTOBDFault) {
            DTOBDFault jf = (DTOBDFault)other;
            return (this.getFaultCode() == jf.getFaultCode());
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Debug: command-line entry point
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        DTOBDFault.InitFaultDescriptionProvider();
        RTProperties cmdLineProps = RTConfig.getCommandLineProperties();
        long         fault     = EncodeFault(cmdLineProps);
        RTProperties faultRTP  = DTOBDFault.GetRTProperties(fault);
        String       make      = null;
        Locale       locale    = null;
        boolean      firstOnly = true;
        String       fmt       = null;
        Print.sysPrintln("Fault : " + fault + " [0x" + StringTools.toHexString(fault) + "]");
        Print.sysPrintln("String: " + faultRTP); 
        Print.sysPrintln("Desc  : " + DTOBDFault.GetFaultDescription(faultRTP,make,locale,firstOnly,fmt));
    }
    
}
