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
//  License Plate Recognition information container
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;
import java.util.regex.*;
import java.math.*;

/**
*** License Plate ID container. (aka "Number Plate")
**/

public class LicensePlate
{

    // ------------------------------------------------------------------------

    public static final char    PLATE_SEP_CHAR      = ',';
    public static final char    CONFIDENCE_SEP_CHAR = ':';

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Decode/Parse array of license plate candidates
    **/
    public static LicensePlate[] DecodeLicensePlates(String plateConf)
    {
        String pc = StringTools.trim(plateConf);
        if (StringTools.isBlank(pc)) {
            return new LicensePlate[0];
        }
        // -- split on ','
        java.util.List<LicensePlate> lpList = new Vector<LicensePlate>();
        String lpSA[] = StringTools.split(pc, PLATE_SEP_CHAR);
        for (String lpS : lpSA) {
            LicensePlate lp = new LicensePlate(lpS); // "PlateID:Confidence"
            if (lp.hasPlateID()) {
                lpList.add(lp);
            }
        }
        // -- do we have any license plates left to sort?
        if (lpList.size() == 0) {
            return new LicensePlate[0];
        }
        // -- sort and return array
        ListTools.sort(lpList, new ConfidenceComparator());
        return (LicensePlate[])lpList.toArray(new LicensePlate[lpList.size()]);
    }

    /**
    *** Encode array of license plate candidates.
    *** @param lpa  An array of LicensePlate instances (null/invalid entries will be ignored)
    *** @param minConf The minimum allowed confidence level to include in encoded String
    *** @param maxLen  The maximum length of the returned String (-1 to disregard maximum length)
    *** @return The encoded license plate String.
    **/
    public static String EncodeLicensePlates(LicensePlate[] lpa, double minConf, int maxLen)
    {
        // -- already beyond max length before we started?
        if (maxLen == 0) {
            return "";
        }
        // -- filter/sort
        lpa = SortLicensePlatesByConfidence(lpa, minConf); // non-null and has plateID
        if (ListTools.isEmpty(lpa)) {
            return "";
        }
        // -- encode to String
        StringBuffer sb = new StringBuffer();
        for (LicensePlate lp : lpa) { // all license-plates in this list are valid
            String plateID = lp.getPlateID();
            int    confI   = lp.hasConfidence()? (int)Math.round(lp.getConfidence() * 100.0) : -1;
            String plateS  = plateID + ((confI > 0)? (""+CONFIDENCE_SEP_CHAR+confI) : "");
            // -- check maximum length
            if (maxLen >= 0) {
                if ((sb.length() == 0) && (plateS.length()) > maxLen) {
                    // -- unlikely, since 'maxLen' should be > the max possible plate length
                    break;
                } else
                if ((sb.length() + 1 + plateS.length()) > maxLen) {
                    // -- maximum String length has been reached
                    break;
                }
            }
            // -- append to String list
            if (sb.length() > 0) { 
                sb.append(PLATE_SEP_CHAR); 
            }
            sb.append(plateS);
        }
        return sb.toString(); // may be blank
    }

    // --------------------------------

    /**
    *** Comparator.
    *** Sorts LicensePlates descending by Confidence.
    *** Nulls will be sorted to the end of the list.
    **/
    public static class ConfidenceComparator
        implements Comparator<LicensePlate>
    {
        public ConfidenceComparator() {
            super();
        }
        public int compare(LicensePlate lp1, LicensePlate lp2) {
            if (lp1 == lp2) {
                // -- handles both null, or same object
                return 0;
            } else
            if (lp1 == null) {
                return 1; // 1>2: sort null to end
            } else
            if (lp2 == null) {
                return -1; // 1<2: sort null to end
            } else {
                double c1 = lp1.hasConfidence()? lp1.getConfidence() : 1.0;
                double c2 = lp2.hasConfidence()? lp2.getConfidence() : 1.0;
                if (c1 == c2) {
                    return 0;
                } else {
                    return (c2 < c1)? -1 : 1; // descending
                }
            }
        }
        public boolean equals(Object other) {
            return (other instanceof ConfidenceComparator)? true : false;
        }
        public int hashCode() {
            return super.hashCode();
        }
    }

    /**
    *** Sort license plate candidate array descending by confidence (highest confidence entries first)
    *** If the specified array is null, and empty array will be returned.
    *** Null and invalid LicensePlate entries will be removed from the returned array.
    *** The original array is left as-is.
    **/
    public static LicensePlate[] SortLicensePlatesByConfidence(LicensePlate[] lpa, double minConf)
    {
        // -- null/empty?
        if (lpa == null) {
            return new LicensePlate[0];
        } else
        if (lpa.length == 0) {
            return lpa;
        }
        // -- copy
        java.util.List<LicensePlate> lpList = new Vector<LicensePlate>();
        int badLP = 0;
        for (LicensePlate lp : lpa) {
            if ((lp == null) || !lp.hasPlateID()) {
                // -- invalid entry
                badLP++;
            } else
            if ((minConf > 0.0) && !lp.isConfidence(minConf)) {
                // -- does not meet confidence level
                badLP++;
            } else {
                // -- valid, add to list
                lpList.add(lp);
            }
        }
        // -- do we have any license plates left to sort?
        if (lpList.size() == 0) {
            return new LicensePlate[0];
        }
        // -- sort/return array
        ListTools.sort(lpList, new ConfidenceComparator());
        return (LicensePlate[])lpList.toArray(new LicensePlate[lpList.size()]);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String plateID      = "";
    private double confidence   = 1.0;

    /**
    *** Constructor
    **/
    public LicensePlate(String plateID, double conf)
    {
        super();
        this.setPlateID(plateID);
        this.setConfidence(conf);
    }

    /**
    *** Constructor
    **/
    public LicensePlate(String plateID)
    {
        super();
        this.setPlateID(plateID, true/*inclConf*/);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Plate ID for this instance.
    *** Confidence is ignored if present in the plateID
    **/
    public void setPlateID(String plateID)
    {
        this.setPlateID(plateID, false/*!inclConf*/);
    }

    /**
    *** Sets the Plate ID for this instance.
    *** @param plateID   The plate ID to set 
    *** @param inclConf  True to also set the confidence, if provided in the plateID
    **/
    public void setPlateID(String plateID, boolean inclConf)
    {
        double conf = -1.0;
        String pid  = StringTools.trim(plateID);
        int p = pid.indexOf(CONFIDENCE_SEP_CHAR);
        if (p >= 0) {
            String confS = pid.substring(p+1).trim();
            if (StringTools.isDouble(confS,false)) {
                // -- 'confS' is a valid/parsable value
                conf = StringTools.parseDouble(confS,100.0) / 100.0;
            }
            pid = pid.substring(0,p).trim();
        }
        this._setPlateID(pid);
        if ((conf >= 0.0) && inclConf) {
            this.setConfidence(conf);
        }
    }

    /**
    *** Sets the Plate ID for this instance
    **/
    public void _setPlateID(String plateID)
    {
        this.plateID = StringTools.trim(plateID);
    }

    /**
    *** Gets the Plate ID for this instance
    **/
    public String getPlateID()
    {
        return this.plateID;
    }

    /**
    *** Returns true if this instance defines a license-plate value
    **/
    public boolean hasPlateID()
    {
        return !StringTools.isBlank(this.getPlateID())? true : false;
    }

    /**
    *** Returns true if this PlateID matches the specified regular expression
    **/
    public boolean isMatchPlateID(String regexS)
    {
        return StringTools.regexMatches(this.getPlateID(),regexS)? true : false;
    }

    /**
    *** Returns true if this PlateID matches the specified regular expression pattern
    **/
    public boolean isMatchPlateID(Pattern regexP)
    {
        return StringTools.regexMatches(this.getPlateID(),regexP)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the confidence level
    **/
    public void setConfidence(double conf)
    {
        if (conf < 0.0) {
            // -- confidence cannot be negative
            this.confidence = 0.0; // -- '0' confidence may be treated as 100% confidence
        } else
        if (conf > 1.0) {
            // -- specified as <confidence> * 100 (ie. 1 < c <= 100)
            double c = conf / 100.0;
            this.confidence = (c > 1.0)? 1.0 : c;
        } else {
            // -- normal: 0 <= c <= 1.0
            this.confidence = conf;
        }
    }

    /**
    *** Gets the confidence value of this license plate id
    **/
    public double getConfidence()
    {
        return this.confidence; // (0.0 <= C <= 1.0)
    }

    /**
    *** Returns true if this instance defined a confidence value that is between 0 and 1 (exclusive)
    **/
    public boolean hasConfidence()
    {
        double c = this.getConfidence();
        return ((c > 0.0) && (c < 1.0))? true : false;
    }

    /**
    *** Returns true if this instance meets, or exceeds, the specified confidence level.
    *** Returns true if the specified minimum confidence level is <= 0.
    *** Returns true if this instance does not define a confidence level.
    **/
    public boolean isConfidence(double minConf) // isConfident(...)
    {
        if (minConf <= 0.0) {
            // -- safely assume the plate-id has a zero or better confidence
            return true;
        } else
        if (!this.hasConfidence()) {
            // -- plate-id has no confidence setting (assume 100%)
            return true;
        } else
        if (this.getConfidence() >= minConf) {
            // -- plate-id has better than the specified minimum confidence
            return true;
        } else {
            // -- plate-id does not meet minimum confidence
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a formatted String representation of this instance suitable for storing in a database.
    *** The String provided by this instance is intended to be parsable by the ParseLicensePlate method.
    **/
    public String format()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getPlateID());
        if (this.hasConfidence()) {
            sb.append(CONFIDENCE_SEP_CHAR);
            double conf100 = this.getConfidence() * 100.0;
            sb.append(StringTools.format(conf100,"0")); // round to nearest integer percent
        }
        return sb.toString();
    }

    // --------------------------------

    /**
    *** Returns a String representation of this instance
    **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getPlateID());
        if (this.hasConfidence()) {
            sb.append(CONFIDENCE_SEP_CHAR);
            double conf100 = this.getConfidence() * 100.0;
            sb.append(StringTools.format(conf100,"0"));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Object is equal to this instance 
    *** Both the plate-id and confidence must match.
    **/
    public boolean equals(Object other)
    {
        if (!(other instanceof LicensePlate)) {
            return false;
        }
        LicensePlate otherLP = (LicensePlate)other;
        if (!this.getPlateID().equalsIgnoreCase(otherLP.getPlateID())) {
            return false;
        } else
        if (this.hasConfidence() != otherLP.hasConfidence()) {
            return false;
        } else
        if (this.getConfidence() != otherLP.getConfidence()) {
            return false;
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
