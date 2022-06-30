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
//  2015/09/24  Martin D. Flynn
//     -Initial release [EXPERIMENTAL]
//  2015/12/07  Martin D. Flynn
//     -Auto atart auto-trim thread. [2.6.1-B35]
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import java.util.*;

import org.opengts.util.*;

//import org.opengts.extra.util.MemCache;
//import org.opengts.extra.util.MemCacheAPI;

public class ReverseGeocodeCache
    implements ReverseGeocodeCacheInterface
{

    // ------------------------------------------------------------------------

    private static final long   DEFAULT_MAX_AGE_MS          = DateTime.DaySeconds(7) * 1000L;
    private static final int    DEFAULT_MAX_SIZE            = 3000;
    private static final long   MIN_TRIM_INTERVAL_MS        = DateTime.HourSeconds(6) * 1000L;
    private static final long   DEFAULT_TRIM_INTERVAL_MS    = 0L;

    // ------------------------------------------------------------------------
    // -- ReverseGeocode provider ids.
    // -    - These values must not change because they are stored in the RGCache table
    // -    - This list includes only those RG providers that are candidates for caching
    // -    - These RG providers are roughly ordered by accuracy of returned addresses
    // -    - Those using publicly available data begin at 2000
    
    public  static final int    RGPROV_UNDEFINED            =    0;
    public  static final int    RGPROV_GOOGLE               =  200; //  100
    public  static final int    RGPROV_BING                 =  400; //  200
    public  static final int    RGPROV_APPLE                =  600;
    public  static final int    RGPROV_HERE                 =  800; //  400
    public  static final int    RGPROV_NACGEO               = 1000; 
    public  static final int    RGPROV_MAPBOX               = 2200;
    public  static final int    RGPROV_NOMINATIM            = 2400; //  600
    public  static final int    RGPROV_GISGRAPHY            = 2600; //  800
    public  static final int    RGPROV_OPENCAGEDATA         = 2800; // 1000
    public  static final int    RGPROV_GEONAMES             = 3000; // 1200
    public  static final int    RGPROV_UNKNOWN              = 9999;

    public static int getProviderID(String name) 
    {
        switch (StringTools.toLowerCase(name)) {
            // -- UNDEFINED
            case "undefined"    : return RGPROV_UNDEFINED;
            // -- Google
            case "google"       : return RGPROV_GOOGLE;
            case "googlev2"     : return RGPROV_GOOGLE;
            case "googlev3"     : return RGPROV_GOOGLE;
            // -- Microsoft Bing
            case "bing"         : return RGPROV_BING;
            case "virtualearth" : return RGPROV_BING;
            // -- Apple (not yet supported)
            case "apple"        : return RGPROV_APPLE;
            case "applemaps"    : return RGPROV_APPLE;
            // -- HERE
            case "here"         : return RGPROV_HERE;
            case "nokiahere"    : return RGPROV_HERE;
            // -- NacGeo
            case "nacgeo"       : return RGPROV_NACGEO;
            // -- MapBox (not yet supported)
            case "mapbox"       : return RGPROV_MAPBOX;
            // -- Nominatim
            case "nominatim"    : return RGPROV_NOMINATIM;
            // -- GISGraphy
            case "gisgraphy"    : return RGPROV_GISGRAPHY;
            case "gisgraphy4"   : return RGPROV_GISGRAPHY;
            case "gisgraphy5"   : return RGPROV_GISGRAPHY;
            case "gisgraphy6"   : return RGPROV_GISGRAPHY;
            // -- OpenCaseData
            case "opencage"     : return RGPROV_OPENCAGEDATA;
            case "opencagedata" : return RGPROV_OPENCAGEDATA;
            // -- Geonames
            case "geonames"     : return RGPROV_GEONAMES;
            // -- Unknown
            default             : return RGPROV_UNKNOWN;
        }
    }

    public static String getProviderName(int id) 
    {
        switch (id) {
            // -- Undefined
            case RGPROV_UNDEFINED    : return "UNDEFINED";
            // -- Google
            case RGPROV_GOOGLE       : return "google";     // "googleV3";
            // -- Microsoft Bing
            case RGPROV_BING         : return "bing";       // "virtualEarth";
            // -- Apple
            case RGPROV_APPLE        : return "apple";      // not yet supported
            // -- HERE
            case RGPROV_HERE         : return "here";       // "nokiahere";
            // -- NacGeo
            case RGPROV_NACGEO       : return "nacgeo";
            // -- MapBox
            case RGPROV_MAPBOX       : return "mapbox";     // not yet supported
            // -- Nominatim
            case RGPROV_NOMINATIM    : return "nominatim";
            // -- GISGraphy
            case RGPROV_GISGRAPHY    : return "gisgraphy";  // "gisgraphy5";
            // -- OpenCaseData
            case RGPROV_OPENCAGEDATA : return "opencage";
            // -- Geonames
            case RGPROV_GEONAMES     : return "geonames";
            // -- Unknown
            default                  : return "UNKNOWN";
        }
    }

    // ------------------------------------------------------------------------

    private enum StoreAs {
        FULL_ADDRESS,
        JSON_STRING,
        REV_GEOCODE
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static ReverseGeocodeCacheInterface revGeoDBCache = null;

    /**
    *** Sets the db-based ReverseGeocodeCache instance.
    *** This is called by RGCache when "getFactory()" is first called (initialized). 
    **/
    public static void SetDBReverseGeocodeCache(ReverseGeocodeCacheInterface dbCache)
    {
        // -- see DBConfig.PROP_RGCache_enableDBCache
        if (dbCache != null) {
            Print.logInfo("Setting DB ReverseGeocodeCache ...");
            revGeoDBCache = dbCache;
        } else {
            revGeoDBCache = null;
        }
    }

    /**
    *** Gets the db-based ReverseGeocodeCache instance
    **/
    private static ReverseGeocodeCacheInterface GetDBReverseGeocodeCache()
    {
        return revGeoDBCache;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Encodes the specified GeoPoint latitude/longitude into a Long value
    *** @param gp     The GeoPoint to encode
    *** @param hiRes  True for 5 decimal places (1.5 meters), false for 4 decimal places (15 meters)
    *** @return The encoded Long GeoPoint
    **/
    private static Long EncodeGeoPoint(GeoPoint gp, boolean hiRes)
    {
        if (GeoPoint.isValid(gp)) {
            double gpLat = gp.getLatitude();
            double gpLon = gp.getLongitude();
            if (hiRes) { // >=5 decimal places
                // -- 5 decimal places (high resolution, 1.5 meters)
                long   gpLAT = Math.round(gpLat * 100000.0); //  -9000000[F76ABC0] to  9000000[0895440]
                long   gpLON = Math.round(gpLon * 100000.0); // -18000000[EED5780] to 18000000[112A880]
                long   LL    = ((gpLAT & 0xFFFFFFFL) << 28) | (gpLON & 0xFFFFFFFL);
                LL |= 0x4000000000000000L;
                return new Long(LL);
            } else {
                // -- 4 decimal places (low resolution, 15 meters)
                long   gpLAT = Math.round(gpLat *  10000.0); //  -900000[F24460]   to  900000[0DBBA0]
                long   gpLON = Math.round(gpLon *  10000.0); // -1800000[E488C0]   to 1800000[1B7740]
                long   LL    = ((gpLAT &  0xFFFFFFL) << 24) | (gpLON &  0xFFFFFFL);
                return new Long(LL);
            }
        } else {
            return new Long(0L);
        }
    }

    // --------------------------------

    private static GeoPoint decodeGeoPoint(long gpLL)
    {
        //Print.logInfo("Convertion to GP: " + StringTools.toHexString(gpLL));
        if ((gpLL & 0x4000000000000000L) != 0L) {
            // -- 5 decimal places
            //Print.logInfo("5-decimal locations ...");
            long LL  = gpLL;
            long MSK = 0xFFFFFFFL; // 28-bits
            long LAT = (LL >> 28) & MSK;
            long LON = (LL >>  0) & MSK;
            if ((LAT & 0x8000000L) != 0L) { LAT = ~MSK | LAT; }
            if ((LON & 0x8000000L) != 0L) { LON = ~MSK | LON; }
            double lat = (double)LAT / 100000.0;
            double lon = (double)LON / 100000.0;
            //Print.logInfo("5-dec Lat/Lon: " + lat + "/" + lon);
            return GeoPoint.isValid(lat,lon)? new GeoPoint(lat,lon) : null;
        } else {
            // -- 4 decimal places
            //Print.logInfo("4-decimal locations ...");
            long LL  = gpLL;
            long MSK = 0xFFFFFFL; // 24-bits
            long LAT = (LL >> 24) & MSK;
            long LON = (LL >>  0) & MSK;
            if ((LAT & 0x800000L) != 0L) { LAT = ~MSK | LAT; }
            if ((LON & 0x800000L) != 0L) { LON = ~MSK | LON; }
            double lat = (double)LAT / 10000.0;
            double lon = (double)LON / 10000.0;
            //Print.logInfo("4-dec Lat/Lon: " + lat + "/" + lon);
            return GeoPoint.isValid(lat,lon)? new GeoPoint(lat,lon) : null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Class for periodically trimming the ReverseGeocodeCache
    **/
    private static class AutoTrimThread
        implements Runnable
    {
        private          ReverseGeocodeCache rgCache        = null;
        private          Thread              trimThread     = null;
        private volatile boolean             isStopped      = false;
        private          long                intervalMS     = 5L * 60000L;
        private          Object              intervalLock   = new Object();
        public AutoTrimThread(ReverseGeocodeCache rgc, long intervalMS) {
            super();
            this.rgCache    = rgc; // MUST not be null
            this.intervalMS = intervalMS;
            this.isStopped  = false;
            this.trimThread = null;
        }
        public String getName() {
            return "RGAutoTrim_" + this.rgCache.getName();
        }
        public boolean isRunning() {
            return (this.trimThread != null) && !this.isStopped;
        }
        public boolean start() {
            if (this.trimThread != null) {
                // -- already started
                return false;
            } else
            if (this.isStopped) {
                // -- already stopped
                return false;
            } else {
                // -- create/start thread
                this.trimThread = new Thread(this, this.getName());
                this.trimThread.start();
                return true;
            }
        }
        public void stop() {
            if (this.trimThread != null) {
                this.isStopped = true;
                this.trimThread.interrupt();
            }
        }
        public void run() {
            Print.logInfo("AutoTrimThread started: " + this.getName());
            // -- turn off trim-on-add
            this.rgCache.setTrimOnAdd(false);
            // -- loop and trim
            for (;!this.isStopped;) {
                // -- trim
                this.rgCache.trimCache();
                // -- wait for next interval
                synchronized (this.intervalLock) {
                    long nowMS = System.currentTimeMillis();
                    long futMS = nowMS + this.intervalMS;
                    while (!this.isStopped && (futMS > nowMS)) {
                        try {
                            this.intervalLock.wait(futMS - nowMS);
                        } catch (InterruptedException ie) {
                            // -- thread interrupted, possible stop request
                        } catch (Throwable th) {
                            // -- unepected error
                            this.isStopped = true;
                            break;
                        }
                        nowMS = System.currentTimeMillis();
                    }
                }
            }
            // -- stopped: turn on trim-on-add
            this.rgCache.setTrimOnAdd(true);
            Print.logInfo("AutoTrimThread stopped: " + this.getName());
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** MemCache Subclass
    **/
    private static class RGCacheMap
        extends MemCache<Long,Object>
    {
        private boolean                      highRes       = false; // false=4dec, true=5dec
        private StoreAs                      rgStoreType   = StoreAs.JSON_STRING;
        private ReverseGeocodeCacheInterface dbCache       = null;
        private boolean                      dbCacheEnable = false;
        public RGCacheMap(StoreAs storeAs) {
            super();
            this.rgStoreType = (storeAs != null)? storeAs : StoreAs.JSON_STRING;
            this.dbCache = ReverseGeocodeCache.GetDBReverseGeocodeCache();
        }
        // --
        public void setHighResolution(boolean hiRes) {
            this.highRes = hiRes;
        }
        public boolean getHighResolution() {
            return this.highRes;
        }
        // --
        public void setDBCacheEnable(boolean enable) {
            this.dbCacheEnable = enable;
        }
        public boolean getDBCacheEnable() {
            return (this.dbCacheEnable && (this.dbCache != null))? true : false;
        }
        // --
        public void addDBReverseGeocode(int provider, GeoPoint gp, ReverseGeocode rg, String clientID) {
            if (this.dbCacheEnable && (this.dbCache != null)) {
                this.dbCache.addReverseGeocode(provider, gp, rg, clientID);
            }
        }
        public ReverseGeocode getDBReverseGeocode(int provider, GeoPoint gp, boolean isMoving) {
            if (this.dbCacheEnable && (this.dbCache != null)) {
                return this.dbCache.getReverseGeocode(provider, gp, isMoving);
            }
            return null;
        }
        // --
        public void addCacheReverseGeocode(int provider, GeoPoint gp, ReverseGeocode rg, String clientID) {
            // -- validate arguments
            if ((gp == null) || (rg == null)) {
                return;
            }
            // -- get cached values
            Object rgVal;
            switch (this.rgStoreType) {
                case FULL_ADDRESS:
                    rgVal = rg.getFullAddress();
                    while (((String)rgVal).startsWith("{")) { rgVal = ((String)rgVal).substring(1); }
                    break;
                case REV_GEOCODE:
                    rgVal = rg;
                    break;
                case JSON_STRING:
                default:
                    rgVal = rg.toJSON().toString(false);
                    break;
            }
            // -- add to memory cache
            this.addValue(ReverseGeocodeCache.EncodeGeoPoint(gp,this.highRes), rgVal);
            // -- add to db cache
            this.addDBReverseGeocode(provider, gp, rg, clientID);
        }
        public ReverseGeocode getCacheReverseGeocode(int provider, GeoPoint gp, boolean isMoving) {
            if (!GeoPoint.isValid(gp)) {
                return null;
            }
            // -- read entry
            long startMS = DateTime.getCurrentTimeMillis();
            Object rgVal = this.getValue(ReverseGeocodeCache.EncodeGeoPoint(gp,this.highRes));
            if (rgVal == null) {
                // -- memory key does not exist, read from db cache
                return this.getDBReverseGeocode(provider, gp, isMoving); // may return null
            }
            // -- ReverseGeocode object
            if (rgVal instanceof ReverseGeocode) {
                ReverseGeocode rg = (ReverseGeocode)rgVal;
                rg.setCachedState(ReverseGeocode.CACHED_MEMORY);
                rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                return rg;
            }
            // -- parse String
            if (rgVal instanceof String) {
                String rgValS = rgVal.toString();
                if (rgValS.startsWith("{")) {
                    // -- contains JSON
                    try {
                        ReverseGeocode rg = new ReverseGeocode(new JSON(rgValS));
                        rg.setIsMoving(isMoving);
                        if (!rg.hasRGProvider()) {
                            rg.setRGProviderID(provider);
                        }
                        rg.setCachedState(ReverseGeocode.CACHED_MEMORY);
                        rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                        return rg;
                    } catch (JSON.JSONParsingException jpe) {
                        // -- unable to parse JSON
                        Print.logWarn("Invalid JSON found in ReverseGeocode Cachs: " + rgValS);
                        return null;
                    }
                } else {
                    // -- assume full address
                    ReverseGeocode rg = new ReverseGeocode();
                  //rg.setIsMoving(false); // MemCache caches only stationary locations
                    rg.setFullAddress(rgValS);
                    if (!rg.hasRGProvider()) {
                        rg.setRGProviderID(provider);
                    }
                    rg.setCachedState(ReverseGeocode.CACHED_MEMORY);
                    rg.setElapsedTimeMS(DateTime.getCurrentTimeMillis() - startMS);
                    return rg;
                }
            }
            // -- not a String? (should not occur)
            Print.logWarn("Invalid object type in ReverseGeocode Cache: " + StringTools.className(rgVal));
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String                          rgCacheName         = null;
    private RGCacheMap                      rgCacheMap          = null;

    private long                            autoTrimIntervalMS  = 0L;
    private AutoTrimThread                  autoTrimThread      = null;
    private volatile boolean                autoTrimChecked     = false;
    
    /**
    *** Constructor
    **/
    public ReverseGeocodeCache()
    {
        this("general",DEFAULT_MAX_SIZE,DEFAULT_MAX_AGE_MS,DEFAULT_TRIM_INTERVAL_MS);
    }

    /**
    *** Constructor
    **/
    public ReverseGeocodeCache(String name, int maxSize, long maxAgeMS, long autoTrimMS)
    {
        super();
        this.rgCacheName = StringTools.trim(name);
        this.rgCacheMap  = new RGCacheMap(StoreAs.JSON_STRING);
        this.rgCacheMap.setMaximumCacheSize(maxSize);
        this.rgCacheMap.setMaximumEntryAgeMS(maxAgeMS);
        this.setAutoTrimInterval(autoTrimMS);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the name of this ReverseGeocodeCache instance
    **/
    public String getName()
    {
        return this.rgCacheName;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the current size of the memory cache
    **/
    public int getSize()
    {
        return this.rgCacheMap.getSize();
    }

    /**
    *** Gets the cache cutback size when out-of-memory is detected
    **/
    public int getSizeCutbackCount()
    {
        return this.rgCacheMap.getMaximumCacheSizeCutbackCount();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the maximum cache size 
    **/
    public void setMaximumSize(int maxSize)
    {
        int ms = (maxSize > 100)? maxSize : 100;
        this.rgCacheMap.setMaximumCacheSize(ms);
    }

    /**
    *** Gets the maximum cache size 
    **/
    public int getMaximumSize()
    {
        return this.rgCacheMap.getMaximumCacheSize();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the maximum entry age, in milliseconds 
    **/
    public void setMaximumAgeMS(long maxAgeMS)
    {
        long maMS = (maxAgeMS >= 0L)? maxAgeMS : DEFAULT_MAX_AGE_MS;
        this.rgCacheMap.setMaximumEntryAgeMS(maMS);
    }

    /**
    *** Gets the maximum entry age, in milliseconds 
    **/
    public long getMaximumAgeMS()
    {
        return this.rgCacheMap.getMaximumEntryAgeMS();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the trim-on-add attribute
    **/
    public void setTrimOnAdd(boolean trim)
    {
        this.rgCacheMap.setTrimOnAdd(trim);
    }

    /**
    *** Trims/removes aged/excessive entries from cache
    **/
    protected void trimCache()
    {
        this.rgCacheMap.trimCache("trimCache");
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Set the auto-trim thread interval.
    **/
    public void setAutoTrimInterval(long intervalMS)
    {
        synchronized (this) { // lock while setting interval
            this.autoTrimIntervalMS = (intervalMS > MIN_TRIM_INTERVAL_MS)? 
                intervalMS : MIN_TRIM_INTERVAL_MS;
        }
    }

    /**
    *** Set the auto-trim thread interval.
    **/
    public long getAutoTrimInterval()
    {
        long intervalMS;
        synchronized (this) { // lock while getting interval
            intervalMS = this.autoTrimIntervalMS;
        }
        return intervalMS;
    }

    // --------------------------------

    /**
    *** Starts the "auto-trim" thread.<br>
    *** Calling this method before adding an address is not required.
    *** This method will automatically be called when the first address is added.
    **/
    public void _startAutoTrimThread()
    {
        if (!this.autoTrimChecked) {
            synchronized (this) { // lock while starting thread
                if (!this.autoTrimChecked) { // retest boolean
                    if (this.autoTrimIntervalMS <= 0L) {
                        // -- no auto-trim interval requested
                        Print.logInfo("AutoTrimThread not used");
                    } else
                    if (this.autoTrimThread == null) { // this.autoTrimThread always null here
                        Print.logInfo("AutoTrimThread starting with interval "+this.autoTrimIntervalMS+" ms");
                        this.autoTrimThread = new AutoTrimThread(this, this.autoTrimIntervalMS);
                        this.autoTrimThread.start();
                    }
                    this.autoTrimChecked = true;
                }
            }
        }
    }

    /**
    *** Stop the "trim" thread.
    *** Once stopped, it cannot be restarted.
    **/
    public void stopAutoTrimThread()
    {
        synchronized (this) { // lock while stopping thread
            if (this.autoTrimThread != null) {
                this.autoTrimThread.stop();
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Sets high/low GeoPoint caching resolution.
    *** 'True' uses 5-decimal points of resolution in the Latitude/Longitude (about 1.5 meters).
    *** 'False' uses 4-decimal points of resolution (about 15 meters).
    *** @param hiRes The high/low GeoPoint caching resolution.
    **/
    public void setHighResolution(boolean hiRes)
    {
        this.rgCacheMap.setHighResolution(hiRes);
    }

    /**
    *** Gets high/low GeoPoint caching resolution.
    *** 'True' uses 5-decimal points of resolution in the Latitude/Longitude (about 1.5 meters).
    *** 'False' uses 4-decimal points of resolution (about 15 meters).
    *** @return The high/low GeoPoint caching resolution.
    **/
    public boolean getHighResolution()
    {
        return this.rgCacheMap.getHighResolution();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Enables the DB RGCache
    *** @param enable True to enable, false to disable
    **/
    public void setDBCacheEnable(boolean enable)
    {
        this.rgCacheMap.setDBCacheEnable(enable);
    }

    /**
    *** Gets the DB RGCache enabled state 
    *** @return The DB RGCache enabled state 
    **/
    public boolean getDBCacheEnable()
    {
        return this.rgCacheMap.getDBCacheEnable();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the cached ReverseGeocode for the specified GeoPoint, or null if
    *** no ReverseGeocode exists for the specified GeoPoint.
    **/
    public ReverseGeocode getReverseGeocode(int provider, GeoPoint gp, boolean isMoving)
    {
        return this.rgCacheMap.getCacheReverseGeocode(provider, gp, isMoving);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Adds the specified ReverseGeocode to the cache for the specified GeoPoint.
    **/
    public boolean addReverseGeocode(int provider, GeoPoint gp, ReverseGeocode rg, String clientID)
    {

        /* invalid GeoPoint or no ReverseGeocode entry? */
        if (!GeoPoint.isValid(gp) || (rg == null)) {
            return false;
        }

        /* add to cache */
        this.rgCacheMap.addCacheReverseGeocode(provider, gp, rg, clientID);

        /* start auto-trim thread? */
        if (!this.autoTrimChecked) {
            this._startAutoTrimThread();
        }

        /* success */
        return true;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
