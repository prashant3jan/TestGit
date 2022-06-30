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
//  2007/06/13  Martin D. Flynn
//     -Initial release
//  2007/11/28  Martin D. Flynn
//     -Added Street#/City/PostalCode getter/setter methods.
//  2008/03/28  Martin D. Flynn
//     -Added CountryCode methods.
//  2008/05/14  Martin D. Flynn
//     -Added StateProvince methods
//  2016/04/06  Martin D. Flynn
//     -Added Timezone methods
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import java.util.Locale;

import org.opengts.util.*;
import org.opengts.geocoder.country.*;

public class ReverseGeocode
{

    // ------------------------------------------------------------------------

    public static final String COUNTRY_US               = USState.COUNTRY_US;                // "US"
    public static final String SUBDIVISION_SEPARATOR    = CountryCode.SUBDIVISION_SEPARATOR; // "/"
    public static final String COUNTRY_US_              = USState.COUNTRY_US_;               // "US/"

    // ------------------------------------------------------------------------

    public  static final String TAG_Provider[]          = { "RG", "Provider"      }; // eq. "googleV3"
    public  static final String TAG_Requestor[]         = { "RQ", "Requestor"     }; // eq. "smith"
    public  static final String TAG_Latitude[]          = { "LA", "Latitude"      };
    public  static final String TAG_Longitude[]         = { "LO", "Longitude"     };

    public  static final String TAG_FullAddress[]       = { "FA", "FullAddress"   };
    public  static final String TAG_StreetAddress[]     = { "SA", "StreetAddress" };
    public  static final String TAG_City[]              = { "CI", "City"          }; // Municipality
    public  static final String TAG_StateProvince[]     = { "SP", "StateProvince" };
    public  static final String TAG_PostalCode[]        = { "PC", "PostalCode"    };
    public  static final String TAG_CountryCode[]       = { "CC", "CountryCode"   };
    public  static final String TAG_Subdivision[]       = { "SD", "Subdivision"   };
    public  static final String TAG_SpeedLimit[]        = { "SL", "SpeedLimit"    };
    public  static final String TAG_TollRoad[]          = { "TR", "TollRoad"      };
    public  static final String TAG_OneWay[]            = { "OW", "OneWay"        }; // not yet implemented
    public  static final String TAG_RoadSurface[]       = { "RS", "RoadSurface"   };
    public  static final String TAG_TimeZone[]          = { "TZ", "TimeZone"      };
    public  static final String TAG_Moving[]            = { "MV", "Moving"        };
    public  static final String TAG_ReferenceID[]       = { "RF", "ReferenceID"   };
    public  static final String TAG_RGProvider[]        = { "RG", "RGProvider"    };
    public  static final String TAG_ElapsedTime[]       = { "ET", "ElapsedTime"   };
    public  static final String TAG_CachedState[]       = { "CS", "CachedState"   };

    // ------------------------------------------------------------------------

    public  static final String REQ_RGLength            = "RGLength:";
    public  static final String REQ_RGSignature         = "RGSignature:";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String  fullAddress     = null; // "FA", "FullAddress"
    private String  streetAddr      = null; // "SA", "StreetAddress"
    private String  city            = null; // "CI", "City"
    private String  stateProvince   = null; // "SP", "StateProvince"
    private String  postalCode      = null; // "PC", "PostalCode"
    private String  countryCode     = null; // "CC", "CountryCode"
    private String  subdivision     = null; // "SD", "Subdivision"
    private double  speedLimitKPH   = 0.0;  // "SL", "SpeedLimit"
    private int     isTollRoad      = -1;   // "TR", "TollRoad"
    private int     isOneWay        = -1;   // "OW", "OneWay"
    private int     roadSurface     = -1;   // "RS", "RoadSurface"
    private String  timezone        = null; // "TZ", "TimeZone"
    private int     isMoving        = -1;   // "MV", "Moving"

    private String  referenceID     = null; // "RF", "ReferenceID"

    private int     rgProviderID    = ReverseGeocodeCache.RGPROV_UNDEFINED; 
    private String  rgProvider      = null; // "RG", "RGProvider" (ie. "googleV3", "nokiahere", etc)
    private String  rgURL           = null;

    private JSON    jsonCache[]     = null; // length is 0 or 2 (see "toJSON(...)"
    
    private long    queryTimeMS     = -1L;  // elapsed time to obtain ReverseGeocode
    private int     cachedState     = -1;   // -1=unknown, 0=false, 1=true

    /**
    *** Default constructor
    **/
    public ReverseGeocode()
    {
        super();
    }

    /**
    *** Default constructor
    **/
    public ReverseGeocode(String rgProv)
    {
        this();
        this.setRGProvider(rgProv);
    }

    /**
    *** Default constructor
    **/
    public ReverseGeocode(int rgProvID)
    {
        this();
        this.setRGProviderID(rgProvID);
    }

    /**
    *** JSON constructor
    **/
    public ReverseGeocode(JSON._Object jsonObj)
    {
        this();
        if (jsonObj != null) {
            this.setFullAddress(  jsonObj.getStringForName(TAG_FullAddress  ,null));
            this.setStreetAddress(jsonObj.getStringForName(TAG_StreetAddress,null));
            this.setCity(         jsonObj.getStringForName(TAG_City         ,null));
            this.setStateProvince(jsonObj.getStringForName(TAG_StateProvince,null));
            this.setPostalCode(   jsonObj.getStringForName(TAG_PostalCode   ,null));
            this.setCountryCode(  jsonObj.getStringForName(TAG_CountryCode  ,null));
            this.setSubdivision(  jsonObj.getStringForName(TAG_Subdivision  ,null));
            this.setSpeedLimitKPH(jsonObj.getDoubleForName(TAG_SpeedLimit   , 0.0));
            this.setIsTollRoad(   jsonObj.getIntForName(   TAG_TollRoad     ,  -1));
            this.setIsOneWay(     jsonObj.getIntForName(   TAG_OneWay       ,  -1));
            this.setRoadSurface(  jsonObj.getIntForName(   TAG_RoadSurface  ,  -1));
            this.setTimeZone(     jsonObj.getStringForName(TAG_TimeZone     ,null));
            this.setIsMoving(     jsonObj.getIntForName(   TAG_Moving       ,  -1)); // parsed, but may not be present
            this.setReferenceID(  jsonObj.getStringForName(TAG_ReferenceID  ,null));
            this.setRGProvider(   jsonObj.getStringForName(TAG_RGProvider   ,null));
            this.setElapsedTimeMS(jsonObj.getLongForName(  TAG_ElapsedTime  , -1L));
            this.setCachedState(  jsonObj.getIntForName(   TAG_CachedState  ,  -1));
        }
    }

    /**
    *** JSON constructor
    **/
    public ReverseGeocode(JSON json)
    {
        this((json != null)? json.getObject() : null);
    }

    // ------------------------------------------------------------------------
    // Full address

    /**
    *** Sets the full address
    **/
    public void setFullAddress(String address)
    {
        this.fullAddress = (address != null)? address.trim() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the full address
    **/
    public String getFullAddress()
    {
        return this.fullAddress;
    }

    /**
    *** Returns true if the full address is defined
    **/
    public boolean hasFullAddress()
    {
        return !StringTools.isBlank(this.fullAddress);
    }

    // ------------------------------------------------------------------------
    // Street address

    /**
    *** Sets the street address
    **/
    public void setStreetAddress(String address)
    {
        this.streetAddr = (address != null)? address.trim() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the street address
    **/
    public String getStreetAddress()
    {
        return this.streetAddr;
    }

    /**
    *** Returns true if the street address is defined
    **/
    public boolean hasStreetAddress()
    {
        return !StringTools.isBlank(this.streetAddr);
    }

    // ------------------------------------------------------------------------
    // -- City

    /**
    *** Sets the city
    **/
    public void setCity(String city)
    {
        this.city = (city != null)? city.trim() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the city
    **/
    public String getCity()
    {
        return this.city;
    }

    /**
    *** Returns true if the city is defined
    **/
    public boolean hasCity()
    {
        return !StringTools.isBlank(this.city);
    }

    // ------------------------------------------------------------------------
    // State/Province

    /**
    *** Sets the state/province
    **/
    public void setStateProvince(String state)
    {
        this.stateProvince = (state != null)? state.trim() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the state/province
    **/
    public String getStateProvince()
    {
        return this.stateProvince;
    }

    /**
    *** Returns true if the state/province is defined
    **/
    public boolean hasStateProvince()
    {
        return !StringTools.isBlank(this.stateProvince);
    }

    // ------------------------------------------------------------------------
    // Postal code

    /**
    *** Sets the postal code
    **/
    public void setPostalCode(String zip)
    {
        this.postalCode = (zip != null)? zip.trim() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the postal code
    **/
    public String getPostalCode()
    {
        return this.postalCode;
    }

    /**
    *** Returns true if the postal code is defined
    **/
    public boolean hasPostalCode()
    {
        return !StringTools.isBlank(this.postalCode);
    }

    // ------------------------------------------------------------------------
    // Country

    /**
    *** Sets the country code
    **/
    public void setCountryCode(String countryCode)
    {
        this.countryCode = (countryCode != null)? countryCode.trim().toUpperCase() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the country code
    **/
    public String getCountryCode()
    {
        return this.hasCountryCode()? this.countryCode : null;
    }

    /**
    *** Returns true if the country is defined
    **/
    public boolean hasCountryCode()
    {
        return !StringTools.isBlank(this.countryCode);
    }

    // ------------------------------------------------------------------------
    // Subdivision

    /**
    *** Sets the default subdivision
    **/
    private String _getSubdivision(String dft)
    {
        // -- already initialized?
        if (!StringTools.isBlank(this.subdivision)) {
            return this.subdivision;
        }
        // -- get country code
        String country = CountryCode.getCountryCode(this.countryCode,null);
        if (StringTools.isBlank(country)) {
            return dft; // invalid/undefined country
        }
        // -- "US"
        if (country.equals(COUNTRY_US)) {
            // -- create default US subdivision
            String state = USState.getCode(this.stateProvince,null);
            if (StringTools.isBlank(state)) {
                return dft; // invalid/undefined state
            }
            return state + SUBDIVISION_SEPARATOR + country;
        }
        // -- TODO: add default subdivision support for non-US countries  
        return dft;
    }

    /**
    *** Sets the country/state subdivision
    **/
    public void setSubdivision(String subdiv)
    {
        this.subdivision = (subdiv != null)? subdiv.trim().toUpperCase() : null;
        this.jsonCache = null;
    }

    /**
    *** Gets the country/state subdivision
    **/
    public String getSubdivision()
    {
        return this._getSubdivision(null);
    }

    /**
    *** Returns true if the country/state subdivision is defined
    **/
    public boolean hasSubdivision()
    {
        return (this._getSubdivision(null) != null)? true : false;
    }

    // ------------------------------------------------------------------------
    // Speed Limit

    /**
    *** Sets the speed limit at the reverse-geocoded location
    **/
    public void setSpeedLimitKPH(double limitKPH)
    {
        //Print.logInfo("Set Speed Limit %f", limitKPH);
        this.speedLimitKPH = limitKPH;
        this.jsonCache = null;
    }

    /**
    *** Gets the speed limit at the reverse-geocoded location
    **/
    public double getSpeedLimitKPH()
    {
        return this.speedLimitKPH;
    }

    /**
    *** Returns true if the speed limit is defined
    **/
    public boolean hasSpeedLimitKPH()
    {
        return (this.speedLimitKPH > 0.0);
    }

    // ------------------------------------------------------------------------
    // Toll-Road

    /**
    *** Sets the toll-road state
    **/
    public void setIsTollRoad(int tollRoadState)
    {
        switch (tollRoadState) {
            case 0 : this.isTollRoad =  0; break;
            case 1 : this.isTollRoad =  1; break;
            default: this.isTollRoad = -1; break;
        }
        this.jsonCache = null;
    }

    /**
    *** Sets the toll-road state
    **/
    public void setIsTollRoad(boolean tollRoad)
    {
        this.isTollRoad = tollRoad? 1 : 0;
        this.jsonCache = null;
    }

    /**
    *** Gets the toll-road state
    **/
    public int getIsTollRoad()
    {
        return this.isTollRoad; 
    }

    /**
    *** Gets the toll-road state (true|false)
    **/
    public boolean getIsTollRoadBool()
    {
        return (this.isTollRoad >= 1); 
    }

    /**
    *** Returns true if the toll-road state is defined
    **/
    public boolean hasIsTollRoad()
    {
        return (this.isTollRoad >= 0);
    }

    // ------------------------------------------------------------------------
    // One-Way

    /**
    *** Sets the one-way state
    **/
    public void setIsOneWay(int oneWayState)
    {
        switch (oneWayState) {
            case 0 : this.isOneWay =  0; break;
            case 1 : this.isOneWay =  1; break;
            default: this.isOneWay = -1; break;
        }
        this.jsonCache = null;
    }

    /**
    *** Sets the one-way state
    **/
    public void setIsOneWay(boolean oneWay)
    {
        this.isOneWay = oneWay? 1 : 0;
        this.jsonCache = null;
    }

    /**
    *** Gets the one-way state
    **/
    public int getIsOneWay()
    {
        return this.isOneWay; 
    }

    /**
    *** Gets the one-way state (true|false)
    **/
    public boolean getIsisOneWayBool()
    {
        return (this.isOneWay >= 1); 
    }

    /**
    *** Returns true if the one-way state is defined
    **/
    public boolean hasIsOneWay()
    {
        return (this.isOneWay >= 0);
    }

    // ------------------------------------------------------------------------
    // Road-Surface type

    public enum RoadSurface implements EnumTools.StringLocale, EnumTools.IntValue, EnumTools.AliasList {
        UNKNOWN   (   0, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.unknown"  ,"Unknown"  )),
        ASPHALT   (  10, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.asphalt"  ,"Asphalt"  ), "paved"),
        CONCRETE  (  20, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.concrete" ,"Concrete" ), "cement","concrete:plates"),
        COMPOSITE (  30, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.composite","Composite")),
        CHIPSEAL  (  40, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.chipseal" ,"ChipSeal" ), "bituminous"),
        OTTASEAL  (  50, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.ottaseal" ,"OttaSeal" ), "otta"),
        MEMBRANE  (  60, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.membrane" ,"Membrane" )), // "thin membrane"
        PAVER     (  70, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.paver"    ,"Paver"    ), "brick","stone","cobblestone"),
        GRAVEL    (  80, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.gravel"   ,"Gravel"   ), "compacted"),
        AGGREGATE (  90, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.aggregate","Aggregate")),
        EARTH     ( 100, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.earth"    ,"Earth"    ), "dirt","unpaved"),
        WOOD      ( 110, I18N.getString(ReverseGeocode.class,"ReverseGeocode.roadSurface.wood"     ,"Wood"     ));
        // ---
        private int         vv = 0;
        private I18N.Text   dd = null;
        private String      aa[] = null; // alias names
        RoadSurface(int v, I18N.Text d, String... a) { vv = v; dd = d; aa = a; }
        public int      getIntValue()                { return vv; }
        public String   toString()                   { return dd.toString(); }
        public String   toString(Locale loc)         { return dd.toString(loc); }
        public String[] getAliasList()               { return aa; }
        public boolean  isUnknown()                  { return this.equals(UNKNOWN); }
        public boolean  isType(int type)             { return this.getIntValue() == type; }
    }

    public static RoadSurface getRoadSurface(int surfInt)
    {
        return (surfInt > RoadSurface.UNKNOWN.getIntValue())? 
            EnumTools.getValueOf(RoadSurface.class, surfInt) : 
            RoadSurface.UNKNOWN;
    }

    public static RoadSurface getRoadSurface(String surfStr, RoadSurface dft)
    {
        return EnumTools.getValueOf(RoadSurface.class, surfStr, dft);
    }

    // --------------------------------

    /**
    *** Sets the road-surface type, as an int value
    **/
    public void setRoadSurface(int surfInt)
    {
        if (surfInt < RoadSurface.UNKNOWN.getIntValue()) {
            this.roadSurface = RoadSurface.UNKNOWN.getIntValue(); // unknown
        } else {
            this.roadSurface = surfInt;
        }
        this.jsonCache = null;
    }

    /**
    *** Sets the road-surface type, as a String value
    **/
    public void setRoadSurface(String surfStr)
    {
        if (StringTools.isBlank(surfStr)) {
            // -- explicit "unknown"
            this.setRoadSurface(RoadSurface.UNKNOWN.getIntValue());
        } else {
            RoadSurface rs = ReverseGeocode.getRoadSurface(surfStr,null);
            if (rs == null) {
                // -- unrecognized road-surface value
                Print.logWarn("Unrecognized RoadSurface: " + surfStr);
                this.setRoadSurface(RoadSurface.UNKNOWN.getIntValue());
            } else {
                this.setRoadSurface(rs.getIntValue());
            }
        }
    }

    /**
    *** Sets the road-surface type, as a RoadSurface enum
    **/
    public void setRoadSurface(RoadSurface surfRS)
    {
        int rs = (surfRS != null)? surfRS.getIntValue() : RoadSurface.UNKNOWN.getIntValue();
        this.setRoadSurface(rs);
    }

    /**
    *** Gets the road-surface type, as an int value
    **/
    public int getRoadSurface()
    {
        return this.roadSurface;
    }

    /**
    *** Returns true if the road-surface type is defined
    **/
    public boolean hasRoadSurface()
    {
        return (this.roadSurface > RoadSurface.UNKNOWN.getIntValue()); // "0" is unknown
    }

    // ------------------------------------------------------------------------
    // TimeZone

    /**
    *** Sets the time-zone
    **/
    public void setTimeZone(String tmz)
    {
        if (!StringTools.isBlank(tmz)) {
            this.timezone = StringTools.trim(tmz);
        } else {
            this.timezone = null;
        }
        this.jsonCache = null;
    }

    /**
    *** Gets the timezone
    **/
    public String getTimeZone()
    {
        return this.hasTimeZone()? this.timezone : null;
    }

    /**
    *** Returns true if the timezone is defined
    **/
    public boolean hasTimeZone()
    {
        return !StringTools.isBlank(this.timezone);
    }

    // ------------------------------------------------------------------------
    // Is Moving

    /**
    *** Sets the in-motion state
    **/
    public void setIsMoving(int inMotion)
    {
        switch (inMotion) {
            case 0 : this.isMoving =  0; break;
            case 1 : this.isMoving =  1; break;
            default: this.isMoving = -1; break;
        }
        this.jsonCache = null;
    }

    /**
    *** Sets the in-motion state
    **/
    public void setIsMoving(boolean inMotion)
    {
        this.isMoving = inMotion? 1 : 0;
        this.jsonCache = null;
    }

    /**
    *** Gets the in-motion state
    **/
    public boolean getIsMoving()
    {
        return (this.isMoving == 1);
    }

    /**
    *** Returns true if the in-motion state is defined
    **/
    public boolean hasIsMoving()
    {
        return (this.isMoving >= 0);
    }

    // ------------------------------------------------------------------------
    // ReferenceID

    /**
    *** Sets the Reference ID
    **/
    public void setReferenceID(String refID)
    {
        if (!StringTools.isBlank(refID)) {
            this.referenceID = StringTools.trim(refID);
        } else {
            this.referenceID = null;
        }
        this.jsonCache = null;
    }

    /**
    *** Gets the Reference ID
    **/
    public String getReferenceID()
    {
        return this.hasReferenceID()? this.referenceID : null;
    }

    /**
    *** Returns true if the Reference ID is defined
    **/
    public boolean hasReferenceID()
    {
        return !StringTools.isBlank(this.referenceID);
    }

    // ------------------------------------------------------------------------
    // RGProvider

    /**
    *** Sets the ReverseGeocodeProvider ID
    **/
    public void setRGProvider(String rgpID)
    {
        this.rgProvider   = !StringTools.isBlank(rgpID)? StringTools.trim(rgpID) : null;
        this.rgProviderID = ReverseGeocodeCache.RGPROV_UNDEFINED;
        this.jsonCache    = null;
    }

    /**
    *** Sets the ReverseGeocodeProvider ID
    **/
    public void setRGProviderID(int rgProvID)
    {
        this.rgProviderID = (rgProvID >= 0)? rgProvID : ReverseGeocodeCache.RGPROV_UNDEFINED;
        this.rgProvider   = null;
        this.jsonCache    = null;
    }

    /**
    *** Returns true if the ReverseGeocodeProvider ID is defined
    **/
    public boolean hasRGProvider()
    {
        return (this.rgProviderID > ReverseGeocodeCache.RGPROV_UNDEFINED) ||
               !StringTools.isBlank(this.rgProvider);
    }

    /**
    *** Gets the ReverseGeocodeProvider name
    **/
    public String getRGProvider()
    {
        if (!StringTools.isBlank(this.rgProvider)) {
            return this.rgProvider;
        } else
        if (this.rgProviderID > ReverseGeocodeCache.RGPROV_UNDEFINED) {
            this.rgProvider = ReverseGeocodeCache.getProviderName(this.rgProviderID);
            return this.rgProvider;
        } else {
            return null;
        }
    }

    /**
    *** Gets the ReverseGeocodeProvider ID
    **/
    public int getRGProviderID()
    {
        if (this.rgProviderID > ReverseGeocodeCache.RGPROV_UNDEFINED) {
            return this.rgProviderID;
        } else
        if (!StringTools.isBlank(this.rgProvider)) {
            this.rgProviderID = ReverseGeocodeCache.getProviderID(this.rgProvider);
            return this.rgProviderID;
        } else {
            return ReverseGeocodeCache.RGPROV_UNDEFINED;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the URL used to obtain this Reverse-Geocode
    **/
    public void setRGUrl(String url)
    {
        this.rgURL = url;
    }

    /**
    *** Returns true if the RG URL has been defined
    **/
    public boolean hasRGUrl()
    {
        return !StringTools.isBlank(this.rgURL)? true : false;
    }

    /**
    *** Gets the URL used to obtain this Reverse-Geocode
    **/
    public String getRGUrl()
    {
        return this.rgURL;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this instance
    **/
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        if (this.hasFullAddress()) {
            sb.append(this.getFullAddress());
        }
        if (this.hasSubdivision()) {
            if (sb.length() > 0) { 
                sb.append(" ["); 
                sb.append(this.getSubdivision());
                sb.append("]"); 
            } else {
                sb.append(this.getSubdivision());
            }
        }
        if (this.hasSpeedLimitKPH()) {
            double limitKPH = this.getSpeedLimitKPH();
            if (limitKPH >= 900.0) {
                sb.append(" (unlimited speed)");
            } else {
                sb.append(" (limit ");
                sb.append(StringTools.format(limitKPH,"0.0"));
                sb.append(" km/h, ");
                sb.append(StringTools.format(limitKPH*GeoPoint.MILES_PER_KILOMETER,"0.0"));
                sb.append(" mph)");
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a JSON String containing this ReverseGeocode information (short format)
    **/
    public JSON toJSON()
    {
        return this.toJSON(false);
    }

    /**
    *** Returns a JSON String containing this ReverseGeocode information
    **/
    public JSON toJSON(boolean longFmt)
    {

        /* initialize JSON cache array */
        int ndx = longFmt? 1 : 0;
        if (this.jsonCache == null) {
            this.jsonCache = new JSON[] { null/*short*/, null/*long*/ };
        }

        /* initialize JSON object for specified format */
        if (this.jsonCache[ndx] == null) {
            JSON._Object jsonObj = new JSON._Object();
            // -- FullAddress
            if (this.hasFullAddress()) {
                jsonObj.addKeyValue(TAG_FullAddress[ndx]  , this.getFullAddress());
            }
            // -- StreetAddress
            if (this.hasStreetAddress()) {
                jsonObj.addKeyValue(TAG_StreetAddress[ndx], this.getStreetAddress());
            }
            // -- City
            if (this.hasCity()) {
                jsonObj.addKeyValue(TAG_City[ndx]         , this.getCity());
            }
            // -- StateProvince
            if (this.hasStateProvince()) {
                jsonObj.addKeyValue(TAG_StateProvince[ndx], this.getStateProvince());
            }
            // -- PostalCode
            if (this.hasPostalCode()) {
                jsonObj.addKeyValue(TAG_PostalCode[ndx]   , this.getPostalCode());
            }
            // -- CountryCode
            if (this.hasCountryCode()) {
                jsonObj.addKeyValue(TAG_CountryCode[ndx]  , this.getCountryCode());
            }
            // -- Subdivision
            if (this.hasSubdivision()) {
                jsonObj.addKeyValue(TAG_Subdivision[ndx]  , this.getSubdivision());
            }
            // -- SpeedLimit
            if (this.hasSpeedLimitKPH()) {
                jsonObj.addKeyValue(TAG_SpeedLimit[ndx]   , this.getSpeedLimitKPH());
            }
            // -- TollRoad
            if (this.hasIsTollRoad()) {
                jsonObj.addKeyValue(TAG_TollRoad[ndx]     , this.getIsTollRoad());
            }
            // -- OneWay
            if (this.hasIsOneWay()) {
                jsonObj.addKeyValue(TAG_OneWay[ndx]       , this.getIsOneWay());
            }
            // -- RoadSurface
            if (this.hasRoadSurface()) {
                jsonObj.addKeyValue(TAG_RoadSurface[ndx]  , this.getRoadSurface());
            }
            // -- TimeZone
            if (this.hasTimeZone()) {
                jsonObj.addKeyValue(TAG_TimeZone[ndx]     , this.getTimeZone());
            }
            // -- Moving (not returned in JSON)
          //if (this.hasIsMoving()) {
          //    jsonObj.addKeyValue(TAG_Moving[ndx]       , this.getIsMoving());
          //}
            // -- ReferenceID
            if (this.hasReferenceID()) {
                jsonObj.addKeyValue(TAG_ReferenceID[ndx]  , this.getReferenceID());
            }
            // -- ReverseGeocodeProviderID
            if (this.hasRGProvider()) {
                jsonObj.addKeyValue(TAG_RGProvider[ndx]   , this.getRGProvider());
            }
            // -- Elapsed QueryTime
            if (this.hasElapsedTimeMS()) {
                jsonObj.addKeyValue(TAG_ElapsedTime[ndx]  , this.getElapsedTimeMS());
            }
            // -- Elapsed QueryTime
            if (this.hasCachedState()) {
                jsonObj.addKeyValue(TAG_CachedState[ndx]  , this.getCachedState());
            }
            // -- return JSON
            this.jsonCache[ndx] = new JSON(jsonObj);
        }

        /* return JSON */
        return this.jsonCache[ndx];

    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the elapsed time required to obtain this ReverseGeocode (in milliseconds).
    **/
    public void setElapsedTimeMS(long deltaMS)
    {
        this.queryTimeMS = (deltaMS >= 0L)? deltaMS : -1L;
    }

    /**
    *** Gets the elapsed time required to obtain this ReverseGeocode (in milliseconds).
    *** Returns -1 if unknown/unspecified.
    **/
    public long getElapsedTimeMS()
    {
        return this.queryTimeMS;
    }

    /**
    *** Returns true if the ElapsedTime has been specified
    **/
    public boolean hasElapsedTimeMS()
    {
        return (this.getElapsedTimeMS() >= 0L)? true : false;
    }

    // ------------------------------------------------------------------------

    public static final int CACHED_UNKNOWN      = -1; // 
    public static final int CACHED_PROVIDER     =  0; // from RGProvider URL
    public static final int CACHED_MEMORY       =  1; // from memory cache
    public static final int CACHED_DB           =  2; // from DB cache

    /** 
    *** Sets the cached state
    **/
    public void setCachedState(int state)
    {
        switch (state) {
            case CACHED_UNKNOWN :
                this.cachedState = CACHED_UNKNOWN;
                break;
            case CACHED_PROVIDER :
                this.cachedState = CACHED_PROVIDER;
                break;
            case CACHED_MEMORY :
                this.cachedState = CACHED_MEMORY;
                break;
            case CACHED_DB :
                this.cachedState = CACHED_DB;
                break;
            default :
                this.cachedState = (state < 0)? CACHED_UNKNOWN : state;
                break;
        }
    }

    /**
    *** Gets the cached state
    **/
    public int getCachedState()
    {
        return this.cachedState;
    }

    /**
    *** Returns true if the CachedState has been specified
    **/
    public boolean hasCachedState()
    {
        return (this.getCachedState() >= CACHED_PROVIDER)? true : false;
    }

    /**
    *** Returns true if cached.  Returns false if not cached, or unknown.
    **/
    public boolean isCached()
    {
        return (this.getCachedState() >= CACHED_MEMORY)? true : false;
    }

    /**
    *** Gets the cached-state valus as a String
    **/
    public String getCachedStateString()
    {
        int state = this.getCachedState();
        switch (state) {
            case CACHED_UNKNOWN :
                return "unknown";
            case CACHED_PROVIDER :
                return "provider";
            case CACHED_MEMORY :
                return "memcache";
            case CACHED_DB :
                return "dbcache";
            default :
                return (state < 0)? "unknown" : "cache";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
