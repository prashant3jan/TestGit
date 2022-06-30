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
// References:
//  - https://developers.google.com/maps/documentation/webservices/
// ----------------------------------------------------------------------------
// Change History:
//  2008/12/01  Martin D. Flynn
//     -Initial release
//  2009/09/23  Martin D. Flynn
//     -Added "&oe=utf8" to reverse-geocode request url
//  2009/11/01  Martin D. Flynn
//     -Added check for error code "620" (ie. limit exceeded)
//  2009/12/16  Martin D. Flynn
//     -Added "reverseGeocodeURL", "geocodeURL" properties.
//     -Added support for client-id (ie. "&client=gme-...")
//     -Added support for Geocoding
//  2012/04/03  Martin D. Flynn
//     -Cloned from "GoogleGeocodeV2.java"
//  2013/05/28  Martin D. Flynn
//     -Added check for returned status "OVER_QUERY_LIMIT" (see STATUS_OVER_QUERY_LIMIT)
//  2013/08/06  Martin D. Flynn
//     -Added additional checks for other status types ("ZERO_RESULTS", etc).
//  2015/05/03  Martin D. Flynn
//     -Parse separate city/state/zip/country
//  2015/09/16  Martin D. Flynn
//     -Added support for including speed-limits.  Note including speed-limits requires
//      an additional call to the Google "Roads" API and requires a special API_KEY.
//     -Added support for ReverseGeocodeCache (EXPERIMENTAL) [2.6.0-B83]
//  2016/01/04  Martin D. Flynn
//     -Added property PROP_useSSL for selecting "https" vs "http"
//     -Added properties to support setting configurable failover timeouts. [2.6.1-B03]
//     -Added lazy starting of auto-trim thread [2.6.1-B35]
//  2018/09/10  GTS Development Team
//     -Added retry support on overlimit failures
// ----------------------------------------------------------------------------
package org.opengts.geocoder.google;

import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;
import org.opengts.CompileTime;
import org.opengts.db.*;
import org.opengts.db.tables.Device;
import org.opengts.geocoder.*;
import org.opengts.google.GoogleRoads;

public class GoogleGeocodeV3
    extends ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider, GeocodeProvider
{

    // ------------------------------------------------------------------------
    // References:
    //   - https://developers.google.com/maps/documentation/webservices/
    //   - http://code.google.com/apis/maps/documentation/services.html#Geocoding_Direct
    //   - http://code.google.com/apis/maps/documentation/geocoding/index.html
    //
    // API URLs:
    //   - http://maps.googleapis.com/maps/api/geocode/output?...
    //   - https://maps.googleapis.com/maps/api/geocode/output?...
    //
    // Nearest Address: V3 API
    //   - http://maps.googleapis.com/maps/api/geocode/json?oe=utf8&latlng=39.12340,-142.12340&client=gme-companyname&channel=gts&language=en&signature=hswERkR3asQC7SkhvjaTlgwy_r4=
    //   - http://maps.googleapis.com/maps/api/geocode/json?latlng=40.479581,-117.773438&language=en
    //     {
    //        "results" : [
    //           {
    //              "address_components" : [
    //                 {
    //                    "long_name" : "Unnamed Rd",
    //                    "short_name" : "Unnamed Rd",
    //                    "types" : [ "route" ]
    //                 },
    //                 {
    //                    "long_name" : "Imlay",
    //                    "short_name" : "Imlay",
    //                    "types" : [ "sublocality", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "Pershing",
    //                    "short_name" : "Pershing",
    //                    "types" : [ "administrative_area_level_2", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "Nevada",
    //                    "short_name" : "NV",
    //                    "types" : [ "administrative_area_level_1", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "United States",
    //                    "short_name" : "US",
    //                    "types" : [ "country", "political" ]
    //                 }
    //              ],
    //              "formatted_address" : "Unnamed Rd, Imlay, NV, USA",
    //              "geometry" : {
    //                 "bounds" : {
    //                    "northeast" : {
    //                       "lat" : 40.50201490,
    //                       "lng" : -117.7399320
    //                    },
    //                    "southwest" : {
    //                       "lat" : 40.49069360,
    //                       "lng" : -117.75710910
    //                    }
    //                 },
    //                 "location" : {
    //                    "lat" : 40.49852230,
    //                    "lng" : -117.74807860
    //                 },
    //                 "location_type" : "APPROXIMATE",
    //                 "viewport" : {
    //                    "northeast" : {
    //                       "lat" : 40.50201490,
    //                       "lng" : -117.7399320
    //                    },
    //                    "southwest" : {
    //                       "lat" : 40.49069360,
    //                       "lng" : -117.75710910
    //                    }
    //                 }
    //              },
    //              "types" : [ "route" ]
    //           },
    //           {
    //              "address_components" : [
    //                 {
    //                    "long_name" : "Pershing",
    //                    "short_name" : "Pershing",
    //                    "types" : [ "administrative_area_level_2", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "Nevada",
    //                    "short_name" : "NV",
    //                    "types" : [ "administrative_area_level_1", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "United States",
    //                    "short_name" : "US",
    //                    "types" : [ "country", "political" ]
    //                 }
    //              ],
    //              "formatted_address" : "Pershing, NV, USA",
    //              "geometry" : {
    //                 "bounds" : {
    //                    "northeast" : {
    //                       "lat" : 40.96115890,
    //                       "lng" : -117.2999130
    //                    },
    //                    "southwest" : {
    //                       "lat" : 39.9982940,
    //                       "lng" : -119.3392960
    //                    }
    //                 },
    //                 "location" : {
    //                    "lat" : 40.56859520,
    //                    "lng" : -118.48639630
    //                 },
    //                 "location_type" : "APPROXIMATE",
    //                 "viewport" : {
    //                    "northeast" : {
    //                       "lat" : 40.96115890,
    //                       "lng" : -117.2999130
    //                    },
    //                    "southwest" : {
    //                       "lat" : 39.9982940,
    //                       "lng" : -119.3392960
    //                    }
    //                 }
    //              },
    //              "types" : [ "administrative_area_level_2", "political" ]
    //           },
    //           {
    //              "address_components" : [
    //                 {
    //                    "long_name" : "Nevada",
    //                    "short_name" : "NV",
    //                    "types" : [ "administrative_area_level_1", "political" ]
    //                 },
    //                 {
    //                    "long_name" : "United States",
    //                    "short_name" : "US",
    //                    "types" : [ "country", "political" ]
    //                 }
    //              ],
    //              "formatted_address" : "Nevada, USA",
    //              "geometry" : {
    //                 "bounds" : {
    //                    "northeast" : {
    //                       "lat" : 42.0022070,
    //                       "lng" : -114.0396480
    //                    },
    //                    "southwest" : {
    //                       "lat" : 35.0018570,
    //                       "lng" : -120.0064730
    //                    }
    //                 },
    //                 "location" : {
    //                    "lat" : 38.80260970,
    //                    "lng" : -116.4193890
    //                 },
    //                 "location_type" : "APPROXIMATE",
    //                 "viewport" : {
    //                    "northeast" : {
    //                       "lat" : 42.0022070,
    //                       "lng" : -114.0396480
    //                    },
    //                    "southwest" : {
    //                       "lat" : 35.0018570,
    //                       "lng" : -120.0064730
    //                    }
    //                 }
    //              },
    //              "types" : [ "administrative_area_level_1", "political" ]
    //           },
    //           {
    //              "address_components" : [
    //                 {
    //                    "long_name" : "United States",
    //                    "short_name" : "US",
    //                    "types" : [ "country", "political" ]
    //                 }
    //              ],
    //              "formatted_address" : "United States",
    //              "geometry" : {
    //                 "bounds" : {
    //                    "northeast" : {
    //                       "lat" : 71.3898880,
    //                       "lng" : -66.94539480000002
    //                    },
    //                    "southwest" : {
    //                       "lat" : 18.91106430,
    //                       "lng" : 172.45469670
    //                    }
    //                 },
    //                 "location" : {
    //                    "lat" : 37.090240,
    //                    "lng" : -95.7128910
    //                 },
    //                 "location_type" : "APPROXIMATE",
    //                 "viewport" : {
    //                    "northeast" : {
    //                       "lat" : 71.3898880,
    //                       "lng" : -66.94539480000002
    //                    },
    //                    "southwest" : {
    //                       "lat" : 18.91106430,
    //                       "lng" : 172.45469670
    //                    }
    //                 }
    //              },
    //              "types" : [ "country", "political" ]
    //           }
    //        ],
    //        "status" : "OK"
    //     }
    //
    // ------------------------------------------------------------------------
    // Speed Limit:
    //  https://developers.google.com/maps/documentation/roads/speed-limits
    //  https://console.developers.google.com/project/164795502565/apiui/credential
    //
    // Speed limit request
    //  - https://roads.googleapis.com/v1/speedLimits?key=API_KEY&path=60.170880,24.942795
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // V3 API
    
    protected static final String  TAG_results                  = "results";    // main tag
    
    protected static final String  TAG_status                   = "status";     
    
    protected static final String  TAG_address_components       = "address_components";
    protected static final String  TAG_long_name                = "long_name";
    protected static final String  TAG_short_name               = "short_name";
    protected static final String  TAG_types                    = "types";
    protected static final String  TAG_formatted_address        = "formatted_address";
    protected static final String  TAG_geometry                 = "geometry";
    protected static final String  TAG_bounds                   = "bounds";
    protected static final String  TAG_northeast                = "northeast";
    protected static final String  TAG_southwest                = "southwest";
    protected static final String  TAG_lat                      = "lat";
    protected static final String  TAG_lng                      = "lng";
    protected static final String  TAG_location                 = "location";
    protected static final String  TAG_location_type            = "location_type";
    protected static final String  TAG_viewport                 = "viewport";

    // -- extra tags (not part of the Google RG)
    protected static final String  TAGx_municipality            = "municipality";
    protected static final String  TAGx_state_province          = "state_province";
    protected static final String  TAGx_country_code            = "country_code";
    protected static final String  TAGx_speed_limit             = "speed_limit";

    // ------------------------------------------------------------------------

    /* http/https */
    protected static final String  URL_http                     = "http";
    protected static final String  URL_https                    = "https";

    /* V3 URLs */
    protected static final String  URL_ReverseGeocode_          = "://maps.googleapis.com/maps/api/geocode/json?";
    protected static final String  URL_Geocode_                 = "://maps.googleapis.com/maps/api/geocode/json?";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static final String  PROP_isProxy                 = "isProxy";              // Boolean:

    protected static final String  PROP_reverseGeocodeURL       = "reverseGeocodeURL";    // String:
    protected static final String  PROP_geocodeURL              = "geocodeURL";           // String:
    protected static final String  PROP_useSSL                  = "useSSL";               // Boolean: 
  //protected static final String  PROP_sensor                  = "sensor";               // Boolean: [OBSOLETE]
    protected static final String  PROP_channel                 = "channel";              // String:
    protected static final String  PROP_countryCodeBias         = "countryCodeBias";      // String: see http://en.wikipedia.org/wiki/CcTLD
    protected static final String  PROP_signatureKey            = "signatureKey";         // String:
    protected static final String  PROP_includeSpeedLimit       = "includeSpeedLimit";    // String: never/always/moving/only
    protected static final String  PROP_ignoreIfMoving          = "ignoreIfMoving";       // Boolean: Reverse-Geocode iff stopped

    protected static final String  PROP_cacheMaximumSize        = "cacheMaximumSize";     // Integer: Cache size
    protected static final String  PROP_cacheMaxEntryAgeSec     = "cacheMaxEntryAgeSec";  // Long: Max age of RG entry
    protected static final String  PROP_cacheMaxEntryAgeMS      = "cacheMaxEntryAgeMS";   // Long:
    protected static final String  PROP_cacheTrimIntervalSec    = "cacheTrimIntervalSec"; // Long: Auto-trim interval
    protected static final String  PROP_cacheTrimIntervalMS     = "cacheTrimIntervalMS";  // Long:
    protected static final String  PROP_cacheEnableDB           = "cacheEnableDB";        // Boolean: Enable DB caching

    protected static final String  PROP_failoverTimeout_        = "failoverTimeout.";
    protected static final String  PROP_failTMO_default         = PROP_failoverTimeout_ + "default";        // failoverTimeout.default=0  
    protected static final String  PROP_failTMO_overQuerLimit   = PROP_failoverTimeout_ + "overQueryLimit"; // failoverTimeout.overQueryLimit=60 
    protected static final String  PROP_failTMO_limitExceeded   = PROP_failoverTimeout_ + "limitExceeded";  // failoverTimeout.limitExceeded=1800
    protected static final String  PROP_failTMO_requestDenied   = PROP_failoverTimeout_ + "requestDenied";  // failoverTimeout.requestDenied=3600
    protected static final String  PROP_failTMO_invalidRequest  = PROP_failoverTimeout_ + "invalidRequest"; // failoverTimeout.invalidRequest=7200
    protected static final String  PROP_failTMO_notAuthorized   = PROP_failoverTimeout_ + "notAuthorized";  // failoverTimeout.notAuthorized=300
    protected static final String  PROP_failTMO_unknown         = PROP_failoverTimeout_ + "unknown";        // failoverTimeout.unknown=300

    // ------------------------------------------------------------------------

    /* ReverseGeocodeCache */
    public    static       int     CACHE_MAXIMUM_SIZE           = 0;            //  0 means disabled
    public    static       long    CACHE_MAXIMUM_AGE_MS         = 20L * 60000L; // 20 minutes?
    public    static       long    AUTO_TRIM_INTERVAL_MS        = 10L * 60000L; // 10 minutes?
    public    static       boolean CACHE_ENABLE_DB              = false;        // enable RGCache table (requires "RGCache.enableDBCache=true")

    // ------------------------------------------------------------------------

    protected static final int     TIMEOUT_ReverseGeocode       = 2500; // milliseconds
    protected static final int     TIMEOUT_Geocode              = 5000; // milliseconds

    protected static final String  DEFAULT_COUNTRY              = "US"; // http://en.wikipedia.org/wiki/CcTLD

    protected static final String  CLIENT_ID_PREFIX             = "gme-";

    protected static final String  EMPTY_ADDRESS                = "?";

    protected static final int     RG_PROVIDER                  = ReverseGeocodeCache.RGPROV_GOOGLE;

    // ------------------------------------------------------------------------

    private enum SpeedLimitType {
        NEVER,
        ALWAYS,
        MOVING,
        ONLY
    };

    private static final SpeedLimitType SpeedLimitType_DEFAULT = SpeedLimitType.NEVER;

    // ------------------------------------------------------------------------

    protected static final String  STATUS_OK                    = "OK";
    protected static final String  STATUS_ZERO_RESULTS          = "ZERO_RESULTS";
    protected static final String  STATUS_OVER_QUERY_LIMIT      = "OVER_QUERY_LIMIT";
    protected static final String  STATUS_LIMIT_EXCEEDED        = "620";
    protected static final String  STATUS_REQUEST_DENIED        = "REQUEST_DENIED";
    protected static final String  STATUS_INVALID_REQUEST       = "INVALID_REQUEST";
    protected static final String  STATUS_FORBIDDEN_403         = "403";
    protected static final String  STATUS_NOT_FOUND_404         = "404";
    protected static final String  STATUS_NO_REVGEO             = "NO_REVGEO";

    protected static final JSON    JSON_ZERO_RESULTS            = JSONStatus(STATUS_ZERO_RESULTS);
    protected static final JSON    JSON_LIMIT_EXCEEDED          = JSONStatus(STATUS_LIMIT_EXCEEDED);
    protected static final JSON    JSON_FORBIDDEN_403           = JSONStatus(STATUS_FORBIDDEN_403);
    protected static final JSON    JSON_NOT_FOUND_404           = JSONStatus(STATUS_NOT_FOUND_404);
    protected static final JSON    JSON_NO_REVGEO               = JSONStatus(STATUS_NO_REVGEO);

    private static JSON JSONStatus(String status)
    {
        // ( "status" : "OK" }
        StringBuffer J = new StringBuffer();
        J.append("{ ");
        J.append("\"").append(TAG_status).append("\" : \"").append(status).append("\"");
        J.append(" }");
        String j = J.toString();
        try {
            return new JSON(j);
        } catch (JSON.JSONParsingException jpe) {
            Print.logError("Invalid JSON: " + J);
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /* MUST BE FALSE IN PRODUCTION!!! */
    protected static final boolean  FAILOVER_DEBUG              = false;

    // ------------------------------------------------------------------------

    // address has to be within this distance to qualify (cannot be greater than 5.0 kilometers)
    protected static final double   MAX_ADDRESS_DISTANCE_KM     = 1.1; 
   // protected static final double MAX_ADDRESS_DISTANCE_KM = 4.5;

    // ------------------------------------------------------------------------

    protected static final String   ENCODING_UTF8               = StringTools.CharEncoding_UTF_8;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean                 isProxy         = false;

    private boolean                 signature_init  = false;
    private GoogleSig               signature       = null;

    private GoogleRoads             googleRoads     = null;

    private SpeedLimitType          speedLimitState = SpeedLimitType_DEFAULT;
    private boolean                 ignoreIfMoving  = false;

    private ReverseGeocodeCache     rgCache         = null;

    public GoogleGeocodeV3(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);

        /* load runtime properties */
        if (rtProps != null) {
            // -- is "gtsproxy"?
            this.isProxy          = rtProps.getBoolean(PROP_isProxy, false); // "proxyrg/GoogleProxy.java"
            // -- do not reverse-geocode if moving
            this.ignoreIfMoving   = rtProps.getBoolean(PROP_ignoreIfMoving, false);
            // -- ReverseGeocodeCache (may not be supported in this release)
            CACHE_MAXIMUM_SIZE    = rtProps.getInt(PROP_cacheMaximumSize, CACHE_MAXIMUM_SIZE);
            long maxEntryAgeMS    = rtProps.getLong(PROP_cacheMaxEntryAgeSec,0L) * 1000L;
            CACHE_MAXIMUM_AGE_MS  = (maxEntryAgeMS > 0L)? maxEntryAgeMS :
                rtProps.getLong(PROP_cacheMaxEntryAgeMS,CACHE_MAXIMUM_AGE_MS);
            CACHE_ENABLE_DB       = rtProps.getBoolean(PROP_cacheEnableDB, CACHE_ENABLE_DB);
            // -- auto-trim interval
            long autoTrimIntervMS = rtProps.getLong(PROP_cacheTrimIntervalSec,0L) * 1000L;
            AUTO_TRIM_INTERVAL_MS = (autoTrimIntervMS > 0L)? autoTrimIntervMS :
                rtProps.getLong(PROP_cacheTrimIntervalMS, AUTO_TRIM_INTERVAL_MS);
            /* * /
            Print.logInfo("RG Cache: " + name);
            Print.logInfo("    Max Size     : " + CACHE_MAXIMUM_SIZE);
            Print.logInfo("    Max Age      : " + (CACHE_MAXIMUM_AGE_MS / 1000L) + " sec");
            Print.logInfo("    Trim Interval: " + (AUTO_TRIM_INTERVAL_MS / 1000L) + " sec");
            / * */
            // -- failover quiet
            /* * /
            Print.logInfo("Failover Quiet ["+name+"]: " + this.getFailoverQuiet());
            / * */
        }

        /* start ReverseGeocodeCache */
        // -- may not be supported in this release
        if (CACHE_MAXIMUM_SIZE > 0L) {
            this.rgCache = new ReverseGeocodeCache(this.getName(), 
                CACHE_MAXIMUM_SIZE, CACHE_MAXIMUM_AGE_MS, AUTO_TRIM_INTERVAL_MS);
            this.rgCache.setDBCacheEnable(CACHE_ENABLE_DB);
            Print.logDebug("ReverseGeocodeCache enabled: " + this.getName());
        } else {
            Print.logDebug("ReverseGeocodeCache disabled: " + this.getName());
        }

        /* init speed-limit selection */
        String inclSpeedLim = this.getProperties().getString(PROP_includeSpeedLimit,null);
        if (StringTools.isBlank(inclSpeedLim)) {
            this.speedLimitState = SpeedLimitType_DEFAULT;
        } else
        if (inclSpeedLim.equalsIgnoreCase("never")  || 
            inclSpeedLim.equalsIgnoreCase("0")      || 
            inclSpeedLim.equalsIgnoreCase("false")    ) {
            // -- never speed-limit
            this.speedLimitState = SpeedLimitType.NEVER;
        } else
        if (inclSpeedLim.equalsIgnoreCase("always") || 
            inclSpeedLim.equalsIgnoreCase("1")      || 
            inclSpeedLim.equalsIgnoreCase("true")     ) {
            // -- always speed-limit
            this.speedLimitState = SpeedLimitType.ALWAYS;
        } else 
        if (inclSpeedLim.equalsIgnoreCase("moving") || 
            inclSpeedLim.equalsIgnoreCase("2")        ) {
            // -- speed-limit only while moving
            this.speedLimitState = SpeedLimitType.MOVING;
        } else 
        if (inclSpeedLim.equalsIgnoreCase("only")   || 
            inclSpeedLim.equalsIgnoreCase("3")        ) {
            // -- only speed-limit, while moving
            this.speedLimitState = SpeedLimitType.ONLY;
        } else {
            this.speedLimitState = SpeedLimitType_DEFAULT;
        }

    }

    // ------------------------------------------------------------------------

    public boolean isFastOperation()
    {
        // -- this is a slow operation
        return super.isFastOperation();
    }

    // ------------------------------------------------------------------------

    public GoogleSig getSignature()
    {
        if (!this.signature_init) {
            this.signature_init = true;
            String key = this.getAuthorization();
            if (!StringTools.isBlank(key) && key.startsWith(CLIENT_ID_PREFIX)) {
                String sigKey = this.getProperties().getString(PROP_signatureKey,"");
                if (!StringTools.isBlank(sigKey)) {
                    //Print.logWarn("Setting SignatureKey: " + sigKey);
                    this.signature = new GoogleSig(sigKey);
                } else {
                    Print.logWarn("No signatureKey ...");
                }
            }
        }
        return this.signature;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the Geocode timeout
    **/
    protected int getGeocodeTimeout()
    {
        return TIMEOUT_Geocode;
    }

    /**
    *** Returns the ReverseGeocode timeout
    **/
    protected int getReverseGeocodeTimeout()
    {
        return TIMEOUT_ReverseGeocode;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns include-speed-limit state (non-null)
    **/
    private SpeedLimitType getSpeedLimitType() // getIncludeSpeedLimit()
    {
        return this.speedLimitState;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the GoogleRoads instance
    **/
    public GoogleRoads getGoogleRoads()
    {
        if (this.googleRoads == null) {
            this.googleRoads = new GoogleRoads(this.getProperties());
        }
        return this.googleRoads;
    }
    
    /**
    *** Gets the speed limit from the Google Roads API
    **/
    public double getSpeedLimitKPH(GeoPoint gp, boolean isMoving, String clientChannel)
    {
        switch (this.getSpeedLimitType()) {
            case NEVER :
                // -- never get speed limit
                return -1.0;
            case ALWAYS :
                // -- should never be 'ALWAYS'!
                return this.getGoogleRoads().getSpeedLimitKPH(gp,clientChannel);
            case MOVING :
            case ONLY :
                // -- iff moving
                return isMoving? this.getGoogleRoads().getSpeedLimitKPH(gp,clientChannel) : -1.0;
        }
        Print.logError("Unexpected speed-limit state value: " + this.getSpeedLimitType());
        return -1.0;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return reverse-geocode */
    //public ReverseGeocode getReverseGeocode(GeoPoint gp, String localeStr)
    //{
    //    return this.getReverseGeocode(gp, localeStr, false, null);
    //}

    /* return cached reverse-geocode */
    public ReverseGeocode getCachedReverseGeocode(GeoPoint gp, boolean isMoving)
    {
        if (this.rgCache != null) {
            return this.rgCache.getReverseGeocode(RG_PROVIDER, gp, isMoving); // may return null
        } else {
            return null;
        }
    }

    /* return reverse-geocode */
    public ReverseGeocode getReverseGeocode(
        GeoPoint gp, boolean isMoving, String localeStr, 
        boolean cache, String clientID,
        Properties props)
    {

        /* get ReverseGeocode */
        ReverseGeocode rg = null;
        long startMS = System.currentTimeMillis();
        for (;;) { // single-pass loop

            /* check ReverseGeocodeCache */
            rg = this.getCachedReverseGeocode(gp, isMoving);
            if (rg != null) {
                // -- found cached RG (already includes ElapsedTime and CachedState)
                break;
            }

            /* get ReverseGeocode from provider */
            rg = this.getAddressReverseGeocode(
                gp, isMoving, localeStr, 
                cache, clientID,
                props); // with failover check
            if (rg != null) {
                // -- found provider RG (already includes ElapsedTime and CachedState)
                if (cache && (this.rgCache != null)) {
                    this.rgCache.addReverseGeocode(RG_PROVIDER, gp, rg, clientID);
                }
                break;
            }

            /* single-pass loop break */
            // -- no Reverse-Geocode at this point
            break;

        }
        long endMS = System.currentTimeMillis();

        /* found, update elapsed time */
        if (rg != null) {
            // -- should already be within a few ms of rg.getElapsedTimeMS(), but update anyway
            long deltaMS = endMS - startMS;
            rg.setElapsedTimeMS(deltaMS); // may overwrite RGCache elapsed time (if isCached==true)
            Print.logDebug("ReverseGeocode elapsed time: " + deltaMS + "ms (" + rg.getCachedStateString() + ")");
        }

        /* return result */
        return rg; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Proxy: append GeoPoint,IsMoving,Device
    **/
    private StringBuffer appendProxyArgs(StringBuffer sb, GeoPoint gp, boolean isMoving, Properties props)
    {
        // -- "gp=39.1234,-142.1234&mv=1&d=ACCOUNT/DEVICE"
        // -- latitude/longitude
        sb.append("gp="); // first arg, no "&"
        if (gp != null) {
            String latS = gp.getLatitudeString(GeoPoint.SFORMAT_DEC_5,null);
            String lonS = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
            sb.append(latS).append(",").append(lonS);
        }
        // -- moving? [2.6.7-B13f]
        sb.append("&mv=").append(isMoving?"1":"0");
        // -- account/device? [2.6.7-B29b][2.6.7-B42h]
        String accID = (props != null)? StringTools.trim(props.getProperty(Device.FLD_accountID,null)) : "";
        String devID = (props != null)? StringTools.trim(props.getProperty(Device.FLD_deviceID ,null)) : "";
        if (!StringTools.isBlank(accID)) {
            String adID = URIArg.encodeArg(accID + "/" + devID); 
            //adID = Checksum.calcCrc64_ECMA_182(adID.getBytes());
            sb.append("&d=").append(adID); // quoted if necessary
        } else
        if (!StringTools.isBlank(devID)) {
            String adID = URIArg.encodeArg(devID); 
            //adID = Checksum.calcCrc64_ECMA_182(adID.getBytes());
            sb.append("&d=").append(adID); // quoted if necessary
        }
        return sb;
    }

    /* nearest address URI */
    protected String getAddressReverseGeocodeURI(boolean ssl)
    {
        return (ssl? URL_https : URL_http) + URL_ReverseGeocode_;
    }

    /* encode GeoPoint into nearest address URI */
    protected String getAddressReverseGeocodeURL(
        GeoPoint gp, boolean isMoving, String localeStr, 
        String clientChannel,
        Properties props)
    {
        StringBuffer sb = new StringBuffer();
        GoogleSig   sig = this.getSignature();

        /* predefined/custom URL */
        String rgURL = this.getProperties().getString(PROP_reverseGeocodeURL,null);
        if (StringTools.isBlank(rgURL) || rgURL.equalsIgnoreCase("DEFAULT")) {
            // -- continue below
        } else
        if (rgURL.equalsIgnoreCase("NONE") || rgURL.equalsIgnoreCase("SKIP")) {
            // -- no reverse-geocoding
            return "";
        } else {
            // -- custom reverse-geocoding URL
            sb.append(rgURL);
            if (!rgURL.endsWith("?") && !rgURL.endsWith("&")) {
                sb.append("&");
            }
            // -- "gtsproxy"
            if (this.isProxy) { 
                // -- "gp=39.1234,-142.1234&mv=1&d=ACCOUNT/DEVICE"
                this.appendProxyArgs(sb, gp, isMoving, props); // first arg, no prefixing "&"
            } else {
                // -- "latlng=39.1234,-142.1234"
                // -- latitude/longitude
                sb.append("latlng="); // first arg, no prefixing "&"
                if (gp != null) {
                    String latS = gp.getLatitudeString(GeoPoint.SFORMAT_DEC_5,null);
                    String lonS = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                    sb.append(latS).append(",").append(lonS);
                }
            }
            String defURL = sb.toString();
            if (sig == null) {
                return defURL;
            } else {
                String urlStr = sig.signURL(defURL);
                return (urlStr != null)? urlStr : defURL;
            }
        }

        /* assemble URL */
        boolean useSSL = this.getProperties().getBoolean(PROP_useSSL,false); // http/https
        sb.append(this.getAddressReverseGeocodeURI(useSSL)); // URL_ReverseGeocode_
        sb.append("oe=utf8");

        /* additional proxy args [gtsproxy] */
        if (this.isProxy) {
            // -- "&gp=39.1234,-142.1234&mv=1&d=ACCOUNT/DEVICE"
            sb.append("&"); // start with '&'
            this.appendProxyArgs(sb, gp, isMoving, props); // first arg, no prefixing "&"
        } else {
            // -- "&latlng=39.1234,-142.1234&sensor=true
            // -- latitude/longitude
            sb.append("&latlng=");
            if (gp != null) {
                String latS = gp.getLatitudeString(GeoPoint.SFORMAT_DEC_5,null);
                String lonS = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                sb.append(latS).append(",").append(lonS);
            }
            // -- Google no longer requires/uses the "sensor" parameter,
            // -  We overload its use here to indicating "moving".
            //String sensor = this.getProperties().getString(PROP_sensor,"false");
            sb.append("&sensor=").append(isMoving?"true":"false"); // [2.6.7-B13f]
        }

        /* channel */
        String channel;
        if (!StringTools.isBlank(clientChannel)) {
            channel = clientChannel.trim();
        } else {
            channel = this.getProperties().getString(PROP_channel, null);
        }

        /* key */
        String auth = this.getAuthorization();
        if (StringTools.isBlank(auth) || auth.startsWith("*")) {
            // -- invalid key
        } else
        if (auth.startsWith(CLIENT_ID_PREFIX)) {
            sb.append("&client=").append(auth);
            if (StringTools.isBlank(channel)) {
                channel = DBConfig.getServiceAccountID(null);
            }
        } else {
            sb.append("&key=").append(auth);
        }

        /* channel */
        if (!StringTools.isBlank(channel)) {
            sb.append("&channel=").append(channel);
        }

        /* localization ("&language=") */
        if (!StringTools.isBlank(localeStr)) {
            sb.append("&language=").append(localeStr);
        }

        /* (redundent) moving? */
        //if (this.isProxy && isMoving) {
        //    sb.append("&moving=true");
        //}

        /* return url */
        String defURL = sb.toString();
        if (sig == null) {
            return defURL;
        } else {
            String urlStr = sig.signURL(defURL);
            return (urlStr != null)? urlStr : defURL;
        }

    }

    /* get failover timeout */
    protected long getFailoverTimeout(String propKey, long dft)
    {
        RTProperties rtp = this.getProperties();
        // -- check for specific property key
        long tmo = rtp.getLong(propKey, 0L);
        // -- get default if specific key is undefined
        if (tmo <= 0L) {
            tmo = rtp.getLong(PROP_failTMO_default, 0L);
        }
        // -- return results
        return (tmo > 0L)? tmo : dft;
    }

    // --------------------------------

    private static String channelKeys[] = { "clientChannel", "serviceID", "clientID", "accountID" };

    /* return reverse-geocode using nearest address */
    protected ReverseGeocode getAddressReverseGeocode(
        GeoPoint gp, boolean isMoving, String localeStr, 
        boolean cache, String clientID,
        Properties props)
    {
        long startMS = DateTime.getCurrentTimeMillis();

        /* client "channel" */
        String clientChannel = CompileTime.SERVICE_ACCOUNT_ID; // may be null/blank
        if (props != null) {
            for (String chKey : channelKeys) {
                String cc = props.getProperty(chKey, null);
                if (!StringTools.isBlank(cc)) {
                    clientChannel = cc; // found a non-blank key
                    break;
                }
            }
        }

        /* moving? */
        if (isMoving) {
            if (this.ignoreIfMoving) {
                // -- do not reverse-geocode if moving, include speed-limit?
                double speedLimitKPH = this.getSpeedLimitKPH(gp, isMoving, clientChannel);
                if (speedLimitKPH >= 0.0) {
                    ReverseGeocode rg = ReverseGeocodeProviderAdapter.NewReverseGeocode(this); // new ReverseGeocode()
                  //rg.setRGUrl(url);
                    rg.setIsMoving(true);
                    rg.setSpeedLimitKPH(speedLimitKPH);
                    rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                    rg.setCachedState(ReverseGeocode.CACHED_PROVIDER);
                    return rg;
                } else {
                    return null;
                }
            }
            // -- try economy RGP
            if (this.hasEconomyReverseGeocodeProvider()) {
                ReverseGeocodeProvider eRGP = this.getEconomyReverseGeocodeProvider();
                ReverseGeocode rg = eRGP.getReverseGeocode(
                    gp, isMoving, localeStr, 
                    cache, clientID,
                    props);
                // -- already contains ElapsedTimeMS, CachedState
                return rg;
            } else {
                // --
                //Print.logInfo("Economy RG not found!");
            }
        }
        // -- "isMoving" may still be true here

        /* speed limit only? */
        if (this.getSpeedLimitType().equals(SpeedLimitType.ONLY)) { // SpeedLimitType.ONLY
            // -- speed-limit only, do not reverse-geocode
            double speedLimitKPH = this.getSpeedLimitKPH(gp, isMoving, clientChannel);
            if (speedLimitKPH >= 0.0) {
                ReverseGeocode rg = ReverseGeocodeProviderAdapter.NewReverseGeocode(this); // new ReverseGeocode()
              //rg.setRGUrl(url);
                rg.setIsMoving(isMoving);
                rg.setSpeedLimitKPH(speedLimitKPH);
                rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                rg.setCachedState(ReverseGeocode.CACHED_PROVIDER);
                return rg;
            } else {
                return null;
            }
        }

        /* URL */
        String url = this.getAddressReverseGeocodeURL(
            gp, isMoving, localeStr, 
            clientChannel,
            props);
        if (StringTools.isBlank(url)) {
            // -- no reverse-geocode URL, try speed-limit, then return
            double speedLimitKPH = this.getSpeedLimitKPH(gp, isMoving, clientChannel);
            if (speedLimitKPH >= 0.0) {
                ReverseGeocode rg = ReverseGeocodeProviderAdapter.NewReverseGeocode(this); // new ReverseGeocode()
                rg.setRGUrl(url);
                rg.setIsMoving(isMoving);
                rg.setSpeedLimitKPH(speedLimitKPH);
                rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                rg.setCachedState(ReverseGeocode.CACHED_PROVIDER);
                return rg;
            } else {
                return null;
            }
        }

        /* check for failover mode */
        if (this.isReverseGeocodeFailoverMode()) {
            // -- this RGP is in failover mode
            ReverseGeocodeProvider rg = this.getFailoverReverseGeocodeProvider(); // non-null here
            return rg.getReverseGeocode(
                gp, isMoving, localeStr,
                cache, clientID,
                props);
        }

        /* retry loop */
        boolean failover           = false;
        long    failoverTimeoutSec = -1L; // indefinite
        int     revGeoTimeoutMS    = this.getReverseGeocodeTimeout();
        Print.logInfo("Google RG URL: " + url);
        retryLoop:
        for (int rgAttempt = 0;;rgAttempt++) {

            /* check retry */
            if (rgAttempt > 0) {
                // -- retry OK?
                if (FAILOVER_DEBUG) {
                    // -- do not retry during FAILOVER_DEBUG
                    break;
                } else
                if (!this.retrySleep(rgAttempt)) {
                    // -- done with retry
                    break;
                }
            }

            /* create JSON document */
            JSON jsonDoc = null;
            JSON._Object jsonObj = null;
            try {
                jsonDoc = GetJSONDocument(url, revGeoTimeoutMS);
                jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
                if (jsonObj == null) {
                    return null;
                }
            } catch (Throwable th) {
                Print.logException("Error", th);
            }
            //Print.logInfo("JSON Result: " + jsonObj);

            /* parse "status" */
            String status = jsonObj.getStringForName(TAG_status, ""); // expect "OK"
            if (!status.equalsIgnoreCase(STATUS_OK)          && 
                !status.equalsIgnoreCase(STATUS_ZERO_RESULTS)  ) {
                // -- ie. status.equals("OVER_QUERY_LIMIT")
                Print.logDebug("Status : " + status); 
            }

            /* parse address */
            String address  = null;
            double speedLim = 0.0;  // not part of the Google V3 RG data
            String houseN   = null;
            String street   = null;
            String strAddr  = null; // houseN + " " + street
            String city     = null;
            String county   = null;
            String state    = null;
            String postal   = null;
            String country  = null;
            JSON._Array results = jsonObj.getArrayForName(TAG_results, null);
            if (!ListTools.isEmpty(results)) {
                JSON._Value val0 = results.getValueAt(0); // first array entry
                JSON._Object addr = (val0 != null)? val0.getObjectValue(null) : null;
                if (addr != null) {
                    // -- (standard) formatted_address
                    address  = addr.getStringForName(TAG_formatted_address, null);
                    // -- (extra) city/state/country/speedLimit (not present in Google V3 data)
                    city     = addr.getStringForName(TAGx_municipality  , "");
                    state    = addr.getStringForName(TAGx_state_province, "");
                    county   = addr.getStringForName(TAGx_country_code  , "");
                    speedLim = addr.getDoubleForName(TAGx_speed_limit   , 0.0);
                    Print.logDebug("Address: " + address + ((speedLim > 0.0)?" ["+speedLim+" km/h]":""));
                    // -- (standard) address_components
                    JSON._Array addrComp = addr.getArrayForName(TAG_address_components, null);
                    if (!ListTools.isEmpty(addrComp)) {
                        for (int a = 0; a < addrComp.size(); a++) {
                            JSON._Value acVal = addrComp.getValueAt(a);
                            JSON._Object acObj = (acVal != null)? acVal.getObjectValue(null) : null;
                            if (acObj != null) {
                                String long_name  = acObj.getStringForName(TAG_long_name, null);
                                String short_name = acObj.getStringForName(TAG_short_name, null);
                                String types[]    = acObj.getStringArrayForName(TAG_types, null);
                                // -- house number
                                if (ListTools.contains(types,"street_number")) {
                                    houseN = short_name;
                                }
                                // -- street
                                if (ListTools.contains(types,"route")) {
                                    street = short_name;
                                }
                                // -- city
                                if (ListTools.contains(types,"locality")) {
                                    city = short_name;
                                }
                                // -- county
                                if (ListTools.contains(types,"administrative_area_level_2")) {
                                    county = short_name;
                                }
                                // -- state
                                if (ListTools.contains(types,"administrative_area_level_1")) {
                                    state = short_name;
                                }
                                // -- zip code
                                if (ListTools.contains(types,"postal_code")) {
                                    postal = short_name;
                                }
                                // -- country
                                if (ListTools.contains(types,"country")) {
                                    country = short_name;
                                }
                            }
                        }
                        // -- combine houseN/street
                        if (street != null) {
                            if (houseN != null) {
                                strAddr = StringTools.trim(houseN + " " + street);
                            } else {
                                strAddr = street;
                            }
                        }
                    }
                }
            } else
            if (status.equalsIgnoreCase(STATUS_ZERO_RESULTS)) {
                Print.logInfo("No address available");
            }

            /* Debug: force failover */
            if (FAILOVER_DEBUG) {
                status  = STATUS_LIMIT_EXCEEDED;
                address = null;
            }

            /* do we have an address? */
            ReverseGeocode revGeo = null;
            if (!StringTools.isBlank(address)) { // status.equalsIgnoreCase(STATUS_OK)
                // -- address found 
                revGeo = ReverseGeocodeProviderAdapter.NewReverseGeocode(this); // new ReverseGeocode()
                revGeo.setRGUrl(url);
                revGeo.setIsMoving(isMoving);
                revGeo.setFullAddress(address);
                if (speedLim >  0.0) { revGeo.setSpeedLimitKPH(speedLim); }
                if (strAddr != null) { revGeo.setStreetAddress(strAddr);  }
                if (city    != null) { revGeo.setCity(city);              }
                if (state   != null) { revGeo.setStateProvince(state);    }
                if (postal  != null) { revGeo.setPostalCode(postal);      }
                if (country != null) { revGeo.setCountryCode(country);    }
            } else
            if (status.equalsIgnoreCase(STATUS_OK)          || 
                status.equalsIgnoreCase(STATUS_ZERO_RESULTS)  ) {
                // -- request was successful, however no address is available
                revGeo = ReverseGeocodeProviderAdapter.NewReverseGeocode(this); // new ReverseGeocode()
                revGeo.setRGUrl(url);
                revGeo.setIsMoving(isMoving);
                revGeo.setFullAddress(EMPTY_ADDRESS);
            }

            /* do we have a valid ReverseGeocode */
            if (revGeo != null) {
                // -- include speed-limit
                double speedLimitKPH = this.getSpeedLimitKPH(gp, isMoving, clientChannel);
                if (speedLimitKPH >= 0.0) {
                    revGeo.setSpeedLimitKPH(speedLimitKPH);
                }
                revGeo.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                revGeo.setCachedState(ReverseGeocode.CACHED_PROVIDER);
                // -- return ReverseGeocode
                return revGeo;
            }

            // -----------------------------------------------------
            // -- continue below iff we do not have a ReverseGeocode

            /* everything below sets the fail-over condition */
            failover = true;

            /* request over-limit, check retry/failover */
            if (status.equals(STATUS_OVER_QUERY_LIMIT)) {
                // -- More than X requests per second, or other request limit
                // -  "failoverTimeout.overQueryLimit"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Over Query Limit! ["+status+"]");
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(2) : DateTime.MinuteSeconds(1);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_overQuerLimit, dftTMO);
                continue retryLoop; // retry
            } else
            if (status.equals(STATUS_LIMIT_EXCEEDED)) {
                // -- Daily limit exceeded (can also be max queries per second)
                // -  "failoverTimeout.limitExceeded"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Limit Exceeded! ["+status+"]");
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(6) : DateTime.MinuteSeconds(30);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_limitExceeded, dftTMO);
                continue retryLoop; // retry
            }

            /* request failed, check failover */
            if (status.equals(STATUS_REQUEST_DENIED)) {
                // -- Invalid request key, etc.
                // -  "failoverTimeout.requestDenied"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Request Denied! ["+status+"]");
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(6) : DateTime.HourSeconds(1);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_requestDenied, dftTMO);
                break retryLoop; // no retry
            } else
            if (status.equals(STATUS_INVALID_REQUEST)) {
                // -- Malformed request
                // -  "failoverTimeout.invalidRequest"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Invalid Request! ["+status+"]");
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(6) : DateTime.HourSeconds(2);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_invalidRequest, dftTMO);
                break retryLoop; // no retry
            } else
            if (status.equals(STATUS_FORBIDDEN_403)) {
                // -- Forbidden 403: not authorized
                // -  "failoverTimeout.notAuthorized"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Forbidden! ["+status+"]");
                    Print.logError("Google Reverse-Geocode Forbidden Response ["+status+"]:\n" + jsonDoc);
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(6) : DateTime.MinuteSeconds(5);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_notAuthorized, dftTMO);
                break retryLoop; // no retry
            } else {
                // -- Unknown error
                // -  "failoverTimeout.unknown"
                if (!this.getFailoverQuiet()) {
                    Print.logError("Google Reverse-Geocode Error! ["+status+"]");
                    Print.logError("Google Reverse-Geocode Error Response ["+status+"]:\n" + jsonDoc);
                }
                long dftTMO = this.getFailoverQuiet()? DateTime.HourSeconds(6) : DateTime.HourSeconds(1);
                failoverTimeoutSec = this.getFailoverTimeout(PROP_failTMO_unknown, dftTMO);
                break retryLoop; // no retry
            }
            // -- control does not reach here
        
        } // retryLoop

        /* failover */
        if (failover && this.hasFailoverReverseGeocodeProvider()) {
            this.startReverseGeocodeFailoverMode(failoverTimeoutSec);
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            Print.logWarn("Failing over to '" + frgp.getName() + "'");
            return frgp.getReverseGeocode(
                gp, isMoving, localeStr,
                cache, clientID,
                props);
        }

        /* no reverse-geocode available */
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* nearest address URI */
    protected String getGeoPointGeocodeURI(boolean ssl)
    {
        return (ssl? URL_https : URL_http) + URL_Geocode_;
    }

    /* encode GeoPoint into nearest address URI */
    protected String getGeoPointGeocodeURL(String address, String country)
    {
        StringBuffer sb = new StringBuffer();
        GoogleSig   sig = this.getSignature();

        /* country */
        if (StringTools.isBlank(country)) { 
            country = this.getProperties().getString(PROP_countryCodeBias, DEFAULT_COUNTRY);
        }

        /* predefined URL */
        String gcURL = this.getProperties().getString(PROP_geocodeURL,null);
        if (!StringTools.isBlank(gcURL)) {
            sb.append(gcURL);
            sb.append("&address=").append(URIArg.encodeArg(address));
            if (!StringTools.isBlank(country)) {
                // country code bias: http://en.wikipedia.org/wiki/CcTLD
                sb.append("&region=").append(country);
            }
            String defURL = sb.toString();
            if (sig == null) {
                return defURL;
            } else {
                String urlStr = sig.signURL(defURL);
                return (urlStr != null)? urlStr : defURL;
            }
        }

        /* assemble URL */
        boolean useSSL = this.getProperties().getBoolean(PROP_useSSL,false); // http/https
        sb.append(this.getGeoPointGeocodeURI(useSSL)); // URL_Geocode_
        sb.append("oe=utf8");

        /* address/country */
        sb.append("&address=").append(URIArg.encodeArg(address));
        if (!StringTools.isBlank(country)) {
            sb.append("&region=").append(country);
        }

        /* sensor [Geocode] */
        // -- Google no longer requires the "sensor" parameter, however we use it here to indicating "moving"
        //String sensor = this.getProperties().getString(PROP_sensor, null);
        //String sensor = isMoving? "true" : "false"; // 
        //if (!StringTools.isBlank(sensor)) {
        //    sb.append("&sensor=").append(sensor);
        //}

        /* channel */
        String channel = this.getProperties().getString(PROP_channel, null);
        if (!StringTools.isBlank(channel)) {
            sb.append("&channel=").append(channel);
        }

        /* key */
        String auth = this.getAuthorization();
        if (StringTools.isBlank(auth) || auth.startsWith("*")) {
            // invalid key
        } else
        if (auth.startsWith(CLIENT_ID_PREFIX)) {
            sb.append("&client=").append(auth);
        } else {
            sb.append("&key=").append(auth);
        }

        /* return url */
        String defURL = sb.toString();
        if (sig == null) {
            return defURL;
        } else {
            String urlStr = sig.signURL(defURL);
            return (urlStr != null)? urlStr : defURL;
        }


    }

    /* return geocode */
    public GeoPoint getGeocode(String address, String country)
    {

        /* URL */
        String url = this.getGeoPointGeocodeURL(address, country);
        Print.logDebug("Google GC URL: " + url);

        /* create JSON document */
        JSON jsonDoc = GetJSONDocument(url, this.getReverseGeocodeTimeout());
        JSON._Object jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
        if (jsonObj == null) {
            return null;
        }

        /* vars */
        String status = jsonObj.getStringForName(TAG_status, null); // expect "OK"
        Print.logInfo("Status : " + status);

        /* parse GeoPoint */
        GeoPoint geoPoint = null;
        JSON._Array results = jsonObj.getArrayForName(TAG_results, null);
        if (!ListTools.isEmpty(results)) {
            JSON._Value  val0 = results.getValueAt(0);
            JSON._Object obj0 = (val0 != null)? val0.getObjectValue(null) : null;
            JSON._Object geometry = (obj0 != null)? obj0.getObjectForName(TAG_geometry, null) : null;
            JSON._Object location = (geometry != null)? geometry.getObjectForName(TAG_location,null) : null;
            if (location != null) {
                double lat = location.getDoubleForName(TAG_lat,0.0);
                double lng = location.getDoubleForName(TAG_lng,0.0);
                if (GeoPoint.isValid(lat,lng)) {
                    geoPoint = new GeoPoint(lat,lng);
                    Print.logInfo("GeoPoint: " + geoPoint);
                }
            }
        } else {
            Print.logInfo("No location found: null");
        }

        /* return location */
        if (geoPoint != null) {
            // -- GeoPoint found 
            return geoPoint;
        }

        /* check for Google reverse-geocode limit exceeded */
        if ((status != null) && status.equals(STATUS_LIMIT_EXCEEDED)) {
            Print.logError("!!!! Google Reverse-Geocode Limit Exceeded !!!!");
        }

        /* no geocode available */
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static JSON GetJSONDocument(String url, int timeoutMS)
    {
        JSON jsonDoc = null;
        HTMLTools.HttpBufferedInputStream input = null;
        try {
            input = HTMLTools.inputStream_GET(url, null, timeoutMS);
            jsonDoc = new JSON(input);
        } catch (JSON.JSONParsingException jpe) {
            Print.logError("JSON parse error: " + jpe);
        } catch (HTMLTools.HttpIOException hioe) {
            // IO error: java.io.IOException: 
            //   Server returned HTTP response code: 403 for URL: http://maps.googleapis.com/...
            int    rc = hioe.getResponseCode();
            String rm = hioe.getResponseMessage();
            Print.logError("HttpIOException ["+rc+"-"+rm+"]: " + hioe.getMessage());
            if (rc == 403) {
                jsonDoc = JSON_FORBIDDEN_403; // STATUS_FORBIDDEN_403: not authorized
            } else
            if (rc == 404) {
                jsonDoc = JSON_NOT_FOUND_404; // STATUS_NOT_FOUND_404: path not found
            }
        } catch (IOException ioe) {
            Print.logError("IOException: " + ioe.getMessage());
        } finally {
            if (input != null) {
                try { input.close(); } catch (Throwable th) {/*ignore*/}
            }
        }
        return jsonDoc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final String ARG_ACCOUNT[]       = new String[] { "account", "a"  };
    private static final String ARG_GEOCODE[]       = new String[] { "geocode", "gc" };
    private static final String ARG_REVGEOCODE[]    = new String[] { "revgeo" , "rg" };
    private static final String ARG_CACHE[]         = new String[] { "cache"  , "ca" };
    private static final String ARG_MOVING[]        = new String[] { "moving" , "mv" };

    private static String FilterID(String id)
    {
        if (id == null) {
            return null;
        } else {
            StringBuffer newID = new StringBuffer();
            int st = 0;
            for (int i = 0; i < id.length(); i++) {
                char ch = Character.toLowerCase(id.charAt(i));
                if (Character.isLetterOrDigit(ch)) {
                    newID.append(ch);
                    st = 1;
                } else
                if (st == 1) {
                    newID.append("_");
                    st = 0;
                } else {
                    // ignore char
                }
            }
            while ((newID.length() > 0) && (newID.charAt(newID.length() - 1) == '_')) {
                newID.setLength(newID.length() - 1);
            }
            return newID.toString();
        }
    }

    /**
    *** Main entery point for debugging/testing
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.setAllOutputToStdout(true);
        Print.setEncoding(ENCODING_UTF8);
        String accountID = RTConfig.getString(ARG_ACCOUNT,"demo");

        /* moving? */
        boolean isMoving = RTConfig.getBoolean(ARG_MOVING,false);

        /* create GoogleGeocodeV3 */
        boolean      cache = RTConfig.getBoolean(ARG_CACHE,false); // !isMoving);
        String    clientID = "";
        CACHE_MAXIMUM_SIZE = cache? 10 : 0;
        GoogleGeocodeV3 gn = new GoogleGeocodeV3("googleV3", null, null);

        /* reverse geocode */
        if (RTConfig.hasProperty(ARG_REVGEOCODE)) {
            GeoPoint gp = new GeoPoint(RTConfig.getString(ARG_REVGEOCODE,null));
            if (!gp.isValid()) {
                Print.logInfo("Invalid GeoPoint specified");
                System.exit(1);
            }
            Print.logInfo("Reverse-Geocoding GeoPoint: " + gp);
            Print.sysPrintln("RevGeocode(1) = " + gn.getReverseGeocode(gp,isMoving,null/*localeStr*/,cache,clientID,null/*props*/));
            Print.sysPrintln("RevGeocode(2) = " + gn.getReverseGeocode(gp,isMoving,null/*localeStr*/,cache,clientID,null/*props*/));
            // -- Note: Even though the values are printed in UTF-8 character encoding, the
            // -  characters may not appear to be properly displayed if the console display
            // -  does not support UTF-8.
            System.exit(0);
        }

        /* no options */
        Print.sysPrintln("No options specified");
        System.exit(1);

    }

}
