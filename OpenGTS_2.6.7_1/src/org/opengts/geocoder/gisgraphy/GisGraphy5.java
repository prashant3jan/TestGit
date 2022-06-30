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
// Notes:
//  - http://www.gisgraphy.com/
//  - https://www.gisgraphy.com/documentation/user-guide.php
//  - https://www.gisgraphy.com/gisgraphy_v_5_0.php
// ----------------------------------------------------------------------------
// Examples:
//  - https://premium2.gisgraphy.com/reversegeocoding/reversegeocode?lat=40.7151268520&lng=-74.0073394775
//      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//      <results>
//          <numFound>1</numFound>
//          <QTime>120</QTime>
//          <attributions>http://www.gisgraphy.com/attributions.html</attributions>
//          <result>
//              <adm1Name>New York</adm1Name>
//              <adm2Name>New York City</adm2Name>
//              <adm3Name>New York County</adm3Name>
//              <azimuthEnd>294</azimuthEnd>
//              <azimuthStart>294</azimuthStart>
//              <city>New York City</city>
//              <citySubdivision>Manhattan Community Board 1</citySubdivision>
//              <countryCode>US</countryCode>
//              <distance>10.862108338894013</distance>
//              <formatedFull>79 Reade Street, Manhattan Community Board 1, New York City, New York County, New York City, New York (NY),  (10007), United States</formatedFull>
//              <formatedPostal>79 Reade Street, New York City, New York 10007</formatedPostal>
//              <geocodingLevel>HOUSE_NUMBER</geocodingLevel>
//              <houseNumber>79</houseNumber>
//              <id>192684380</id>
//              <lat>40.7152211</lat>
//              <length>386.216462461</length>
//              <lng>-74.00730920000001</lng>
//              <maxSpeed>25 mph</maxSpeed>
//              <oneWay>true</oneWay>
//              <sourceId>5671314</sourceId>
//              <speedMode>OSM</speedMode>
//              <state>New York</state>
//              <streetName>Reade Street</streetName>
//              <zipCode>10007</zipCode>
//          </result>
//      </results>
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  GTS Development Team
//     -Cloned from "GisGraphy.java" V4 to support V5
//     -Update to remove vendor/center names from address (ie. "Walmart Distribution Center", erc) [2.6.7-B43q]
// ----------------------------------------------------------------------------
package org.opengts.geocoder.gisgraphy;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.util.*;
import org.opengts.geocoder.*;
import org.opengts.geocoder.country.*;

public class GisGraphy5 // v5
    extends ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider, SubdivisionProvider
{

    // ------------------------------------------------------------------------

    // -- XML tags
    protected static final String TAG_error                     = "error";            // error indication
    protected static final String TAG_results                   = "results";          // begin of results
    protected static final String TAG_numFound                  = "numFound";         // number of entries found
    protected static final String TAG_QTime                     = "QTime";            // The execution time of the query in ms
    protected static final String TAG_result                    = "result";           // one result

    protected static final String TAG_distance                  = "distance";         // Distance to address (meters)
    protected static final String TAG_geocodingLevel            = "geocodingLevel";   // Geocoding level [NONE|STREET]
    protected static final String TAG_lat                       = "lat";              // Latitude
    protected static final String TAG_lng                       = "lng";              // Longitude

    protected static final String TAG_formatedFull              = "formatedFull";     // fully formatted address (with county)
    protected static final String TAG_formattedFull             = "formattedFull";    // fully formatted address (with county)
    protected static final String TAG_formatedPostal            = "formatedPostal";   // fully formatted address (sans county)
    protected static final String TAG_formattedPostal           = "formattedPostal";  // fully formatted address (sans county)
    protected static final String TAG_streetName                = "streetName";       // Street
    protected static final String TAG_houseNumber               = "houseNumber";      // House number
    protected static final String TAG_city                      = "city";             // City
    protected static final String TAG_citySubdivision           = "citySubdivision";  // City Subdivision (Estate)
    protected static final String TAG_state                     = "state";            // State
    protected static final String TAG_adm1Name                  = "adm1Name";         // may be State
    protected static final String TAG_adm2Name                  = "adm2Name";         // may be City/County
    protected static final String TAG_adm3Name                  = "adm3Name";         // may be County
    protected static final String TAG_countryCode               = "countryCode";      // Country abbrev (eg. "US")
    protected static final String TAG_zipCode                   = "zipCode";          // Zip Code
    protected static final String TAG_name                      = "name";             // Name of address?
    protected static final String TAG_maxSpeed                  = "maxSpeed";         // speed-limit
    protected static final String TAG_oneWay                    = "oneWay";           // one-way
    protected static final String TAG_tollRoad                  = "tollRoad";         // toll-road
    protected static final String TAG_surface                   = "surface";          // road-surface

    // ------------------------------------------------------------------------
    // http://free.gisgraphy.com/reversegeocoding/reversegeocode?format=XML&from=1&to=1&lat=46.17330&lng=21.29370

    protected static final String PROP_gisgraphyApikey          = "gisgraphyApikey";  // String : "123456abcdef"
    protected static final String PROP_reversegeocodeURL        = "reversegeocodeURL";// String : "http://localhost:8081/reversegeocoding/reversegeocode?"
    protected static final String PROP_useSSL                   = "useSSL";           // boolean: true
    protected static final String PROP_host                     = "host";             // String : "localhost:8081"
    protected static final String PROP_failoverHost             = "failoverHost";     // String : "" (not currently used)

    protected static final String PROP_cacheMaximumSize         = "cacheMaximumSize";     // Integer: Cache size
    protected static final String PROP_cacheMaxEntryAgeSec      = "cacheMaxEntryAgeSec";  // Long: Max age of RG entry
    protected static final String PROP_cacheMaxEntryAgeMS       = "cacheMaxEntryAgeMS";   // Long:
    protected static final String PROP_cacheTrimIntervalSec     = "cacheTrimIntervalSec"; // Long: Auto-trim interval
    protected static final String PROP_cacheTrimIntervalMS      = "cacheTrimIntervalMS";  // Long:
    protected static final String PROP_cacheEnableDB            = "cacheEnableDB";        // Boolean: Enable DB caching  

    // ------------------------------------------------------------------------

    /* ReverseGeocodeCache */
    public    static       int     CACHE_MAXIMUM_SIZE           = 0;            //  0 means disabled
    public    static       long    CACHE_MAXIMUM_AGE_MS         = 20L * 60000L; // 20 minutes?
    public    static       long    AUTO_TRIM_INTERVAL_MS        = 10L * 60000L; // 10 minutes?
    public    static       boolean CACHE_ENABLE_DB              = true;         // enable RGCache table (requires "RGCache.enableDBCache=true")

    protected static       String HOST_PRIMARY                  = "localhost";
    protected static       String HOST_FAILOVER                 = "";

    protected static       int    SERVICE_TIMEOUT_MS            = 5000;
    
    protected static       String GISGRAPHY_URL                 = "https://premium2.gisgraphy.com/reversegeocoding/reversegeocode?";
    protected static       String REVERSEGEOCODE_URL            = null;

    protected static final int    RG_PROVIDER                   = ReverseGeocodeCache.RGPROV_GISGRAPHY;

    // ------------------------------------------------------------------------

    // address has to be within this distance to qualify
    protected static final double MAX_ADDRESS_DISTANCE_KM       = 10.0;

    // ------------------------------------------------------------------------

    protected static final String ENCODING_UTF8                 = StringTools.CharEncoding_UTF_8;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private ReverseGeocodeCache   rgCache       = null;

    /**
    *** Constructor
    *** @param name    The name assigned to this ReverseGeocodeProvider
    *** @param key     The optional authorization key
    *** @param rtProps The properties associated with this ReverseGeocodeProvider
    **/
    public GisGraphy5(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);

        /* load runtime properties */
        if (rtProps != null) {
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
        }

        /* start ReverseGeocodeCache */
        // -- may not be supported in this release
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if locally resolved, false otherwise.
    *** (ie. remote address resolution takes more than 20ms to complete)
    *** @return true if locally resolved, false otherwise.
    **/
    public boolean isFastOperation() 
    {
        String host = this.getHostname(true);
        // -- "localhost:9090"
        int p = host.indexOf(":");
        String h = (p >= 0)? host.substring(0,p) : host;
        if (h.equalsIgnoreCase("localhost") || h.equals("127.0.0.1")) {
            // -- resolved locally, assume fast
            return true;
        } else {
            // -- this may be a slow operation
            return super.isFastOperation();
        }
    }

    // ------------------------------------------------------------------------

    /* return cached reverse-geocode */
    public ReverseGeocode getCachedReverseGeocode(GeoPoint gp, boolean isMoving)
    {
        if (this.rgCache != null) {
            // -- attempt to read existing cached ReverseGeocode
            return this.rgCache.getReverseGeocode(RG_PROVIDER, gp, isMoving); // may return null
        } else {
            // -- cache not supported
            return null;
        }
    }

    /**
    *** Returns a ReverseGeocode instance for the specified GeoPoint
    *** @param gp  The GeoPoint
    *** @return The ReverseGeocode instance
    **/
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
                props);
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
        return rg;

    }

    /* return subdivision */
    public String getSubdivision(GeoPoint gp) 
    {
        throw new UnsupportedOperationException("Not supported");
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the hostname
    *** @param primary  True to return the primary host, else failover host
    *** @return The hostname
    **/
    private String getHostname(boolean primary) 
    {
        RTProperties rtp = this.getProperties();
        String host = primary?
            rtp.getString(PROP_host        , HOST_PRIMARY ) :
            rtp.getString(PROP_failoverHost, HOST_FAILOVER);
        return host;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a ReverseGeocode instance containing address information
    *** @param gp  The GeoPoint
    *** @return The ReverseGeocode instance
    **/
    private ReverseGeocode getAddressReverseGeocode(
        GeoPoint gp, boolean isMoving, String localeStr,
        boolean cache, String clientID,
        Properties props) 
    {
        long startMS = DateTime.getCurrentTimeMillis();

        /* get street address URL */
        String street_url = this.getStreetReverseGeocodeURL(gp);
        //Print.logInfo("Street URL: " + street_url);

        /* ReverseGeocode instance */
        ReverseGeocode rg = NewReverseGeocode(this); // new ReverseGeocode()
        rg.setRGUrl(street_url);
        rg.setRGProvider(this.getName());
        rg.setIsMoving(isMoving);

        /* retry loop */
        retryLoop:
        for (int rgAttempt = 0;;rgAttempt++) {

            /* check retry */
            if (rgAttempt > 0) {
                // -- retry OK?
                if (!this.retrySleep(rgAttempt)) {
                    // -- still failing, but done with retry
                    return null;
                }
            }

            /* get XML document */
            Document street_xmlDoc = null;
            try {
                street_xmlDoc = GetXMLDocument(street_url);
                if (street_xmlDoc == null) {
                    // -- some error (but not an "over limit"
                    return null;
                } 
            } catch (HTMLTools.HttpIOException hioe) {
                // -- 429: "over limit"
                continue retryLoop;
            }

            /* read/parse Street address */
            Element results = street_xmlDoc.getDocumentElement(); // <results>
            if (results.getTagName().equalsIgnoreCase(TAG_results)) {
                NodeList ResultList = results.getElementsByTagName(TAG_result);
                resultTags:
                for (int g = 0; (g < ResultList.getLength()); g++) {
                    // -- clear 'rg' on subsequent results?
                    if (g > 0) {
                        rg.setStreetAddress(null);
                        rg.setCity(null);
                        rg.setStateProvince(null);
                        rg.setPostalCode(null);
                        rg.setCountryCode(null);
                        rg.setSpeedLimitKPH(0.0);
                        rg.setIsTollRoad(-1);
                        rg.setIsOneWay(-1);
                        rg.setRoadSurface((String)null);
                    }
                    // -- parse "<result>" tag section
                    Element  response      = (Element)ResultList.item(g);
                    NodeList responseNodes = response.getChildNodes();
                    String   addrName      = null;
                    String   addrPost      = null;
                    String   addrFull      = null;
                    for (int n = 0; n < responseNodes.getLength(); n++) {
                        // -- iterate through "<result>" sub-tags
                        Node responseNode = responseNodes.item(n);
                        if (!(responseNode instanceof Element)) { continue; }
                        Element responseElem = (Element)responseNode;
                        String responseNodeName = responseElem.getNodeName();
                        if (responseNodeName.equalsIgnoreCase(TAG_name)) {
                            addrName = GisGraphy5.GetNodeText(responseElem); // "Walmart Distribution Center"
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_formatedFull) ||
                            responseNodeName.equalsIgnoreCase(TAG_formattedFull)  ) {
                            // -- this includes the county in the address
                            // -  180 New Jersey Turnpike, East Brunswick Township, Middlesex County, New Jersey (NJ),  (08816), United States
                            addrFull = GisGraphy5.GetNodeText(responseElem);
                            //rg.setFullAddress(address); <== set below
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_formatedPostal) ||
                            responseNodeName.equalsIgnoreCase(TAG_formattedPostal)  ) {
                            // -- New Jersey Videography, New Jersey Videography 180 New Jersey Turnpike, East Brunswick Township, New Jersey 08816
                            // -- Customized Distribution Services, Inc., Customized Distribution Services, Inc. 3355 Cedar Street, Ontario, CA 91764
                            // -- Walmart Distribution Center, Walmart Distribution Center 23701 West Southern Avenue, Buckeye, AZ 85326
                            addrPost = GisGraphy5.GetNodeText(responseElem);
                           //rg.setFullAddress(addrPost); <== set below
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_houseNumber)) {
                            String _streetAddr = StringTools.trim(rg.getStreetAddress());       // blank
                            String houseNumber = GisGraphy5.GetNodeText(responseElem);          // "2507"
                            rg.setStreetAddress(StringTools.trim(houseNumber+" "+_streetAddr)); // "2507"
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_streetName)) {
                            String _streetAddr = StringTools.trim(rg.getStreetAddress());       // "2507"
                            String streetName  = GisGraphy5.GetNodeText(responseElem);          // "Vista Road"
                            rg.setStreetAddress(StringTools.trim(_streetAddr+" "+streetName));  // "2507 Vista Road"
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_city)) {
                            String city = GisGraphy5.GetNodeText(responseElem);
                            rg.setCity(city);
                            //Print.logInfo("Found City: " + city);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_state)) {
                            String state = GisGraphy5.GetNodeText(responseElem);
                            String code = USState.getCode(state,null); // ie. "California" ==> "CA"
                            if (!StringTools.isBlank(code)) {
                                rg.setStateProvince(code);
                            } else {
                                rg.setStateProvince(state);
                            }
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_zipCode)) {
                            String zipCode = GisGraphy5.GetNodeText(responseElem);
                            rg.setPostalCode(zipCode);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_countryCode)) {
                            String cc = GisGraphy5.GetNodeText(responseElem);
                            rg.setCountryCode(cc);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_maxSpeed)) {
                            String _speedLim = StringTools.trim(GisGraphy5.GetNodeText(responseElem)).toLowerCase();
                            double speedLim = StringTools.parseDouble(_speedLim,0.0);
                            if (_speedLim.endsWith("mph")) {
                                speedLim *= GeoPoint.KILOMETERS_PER_MILE;
                            }
                            rg.setSpeedLimitKPH(speedLim);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_tollRoad)) {
                            boolean isTollRoad = StringTools.parseBoolean(GisGraphy5.GetNodeText(responseElem),false);
                            rg.setIsTollRoad(isTollRoad);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_oneWay)) {
                            boolean isOneWay = StringTools.parseBoolean(GisGraphy5.GetNodeText(responseElem),false);
                            rg.setIsOneWay(isOneWay);
                        } else
                        if (responseNodeName.equalsIgnoreCase(TAG_surface)) {
                            String surface = GisGraphy5.GetNodeText(responseElem);
                            rg.setRoadSurface(surface);
                        } else {
                            // -- quietly ignore tag
                        }
                    } // iterate through result sub-nodes
                    // -- final full-address assembly
                    if (!StringTools.isBlank(addrPost)) {
                        if (StringTools.isBlank(addrName)) {
                            // -- 'addrPost' likely does not contain a vendor/center name (ie. sans "Walmart Distribution Center")
                            rg.setFullAddress(addrPost);
                        } else
                        if (this.hasAddressComponents(rg,true/*inclStreet*/)) {
                            // -- 'addrPost' likely includes a vender/center name, however all full-address components are available
                            // -  delegate to 'createFullAddress(...)' below
                        } else
                        if (!StringTools.isBlank(addrFull)) {
                            // -- 'rg' does not contain all full-address components, but we do have 'addrFull'
                            // -- remove superfluous vendor/center (<name>) from 'addrPost' (ie. "Walmart Distribution Center")
                            // -- GPS : 33.387998,-112.560919
                            // -- POST: Walmart Distribution Center, Walmart Distribution Center 23701 West Southern Avenue, Buckeye, Arizona 85326
                            // -  FULL: 23701 West Southern Avenue, Buckeye, Maricopa County, Arizona (AZ),  (85326), United States
                            String addr = null;
                            int f = addrFull.indexOf(',');
                            if (f > 0) {
                                String pfx = addrFull.substring(0,f);    // full: "23701 West Southern Avenue"
                                int p = addrPost.indexOf(pfx);           // post: ~57
                                if (p > 0) {
                                    addr = addrPost.substring(p);        // post: "23701 West Southern Avenue, ..."
                                }
                            }
                            // -- if 'addr' is null, 'addrPost' it's the best we've got
                            rg.setFullAddress((addr != null)? addr : addrPost);
                        } else {
                            // -- 'addrPost' likely contains vendor/center name, and 'addrFull' is null
                            // -- and 'rg' does not have the full-address components ...
                            // -- attempt to remove superfluous vendor/center and save 'addrPost' as-is anyway
                            String addr = null;
                            int p = addrPost.indexOf(',');
                            if (p > 0) {
                                String pfx = addrPost.substring(0,p);
                                String suf = addrPost.substring(p+1).trim();
                                if (suf.startsWith(pfx)) {
                                    // -- POST: Walmart Distribution Center, Walmart Distribution Center 23701 West Southern Avenue, Buckeye, Arizona 85326
                                    // -    ==> Walmart Distribution Center 23701 West Southern Avenue, Buckeye, Arizona 85326
                                    addr = suf;
                                }
                            }
                            rg.setFullAddress((addr != null)? addr : addrPost);
                        }
                    } else {
                        // -- 'addrPost' is empty/null.  
                        // -- we may have 'addrFull', but we do not want to use this (ill formatted) anyway.
                        // -- delegate to 'createFullAddress(...)' below with whatever is available
                    }
                    // -- we may have an RG, exit from retry loop
                    if (rg.hasFullAddress() || this.hasAddressComponents(rg,true/*inclStreet*/)) {
                        // -- we have a full-address, exit loop now
                        break retryLoop;
                    }
                } // resultTags: for each <result>
            }  // tag <results>
            // -- should not get here if a result was found

            /* address not found */
            return null;

        } // retryLoop:
        // -- "rg" is non-null here, and contains an address

        /* create/return address */
        if (rg.hasFullAddress()) {
            String addr = rg.getFullAddress();
            // -- remove ", United States"
            {
                String s = ", United States";
                int p = addr.indexOf(s);
                if (p >= 0) {
                    addr = (addr.substring(0,p).trim() + " " + addr.substring(p+s.length()).trim()).trim();
                }
            }
            // -- remove ", US" (country is assumed)
            {
                String s = ", US";
                int p = addr.indexOf(s); // "US" is already assumed
                if (p >= 0) {
                    addr = (addr.substring(0,p).trim() + " " + addr.substring(p+s.length()).trim()).trim();
                }
            }
            // -- convert state name to state abbreviation (ie. "California" ==> "CA")
            {
                int p = addr.lastIndexOf(",");
                if (p >= 0) {
                    // -- look for trailing zip code (ie. ", New Mexico 83709")
                    int addrL = addr.length();
                    int s = addr.lastIndexOf(" ");
                    if ((s > (p + 1)) && ((s + 1) < addrL) && Character.isDigit(addr.charAt(s+1))) {
                        // -- 's' points to space just before zip
                    } else {
                        // -- no trailing zip, move 's' to end of address
                        s = addrL;
                    }
                    String state = addr.substring(p+1,s).trim();
                    String code  = USState.getCode(state,null);
                    if (!StringTools.isBlank(code)) {
                        String tail = (s < addrL)? addr.substring(s) : ""; // do not trim (keep leading space)
                        addr = addr.substring(0,p) + ", " + code + tail;
                    }
                }
            }
            // -- set resulting address
            rg.setFullAddress(addr);
        } else {
            boolean inclUS = false; // <== US is assumed here
            String addr = this.createFullAddress(rg, inclUS);
            rg.setFullAddress(addr);
        }

        /* set elapsed time */
        long deltaMS = DateTime.getCurrentTimeMillis() - startMS;
        rg.setElapsedTimeMS(deltaMS);
        rg.setCachedState(ReverseGeocode.CACHED_PROVIDER);

        /* return address */
        //Print.logInfo("RG City: " + rg.getCity());
        return rg;
    
    }

    private String getStreetReverseGeocodeURL(GeoPoint gp) 
    {
        StringBuffer sb = new StringBuffer();
        RTProperties rtp = this.getProperties();
        String url = rtp.getString(PROP_reversegeocodeURL, REVERSEGEOCODE_URL);
        if (!StringTools.isBlank(url)) {
            sb.append(url);
            if (!url.endsWith("?") && !url.endsWith("&")) {
                sb.append("?");
            }
        } else {
            boolean useSSL = rtp.getBoolean(PROP_useSSL,true);
            if (useSSL) {
                sb.append("https://");
            } else {
                sb.append("http://");
            }
            sb.append(this.getHostname(true));
            sb.append("/reversegeocoding/reversegeocode?");
        }
        // -- standard vars
        //sb.append("from=1&to=1&format=xml&indent=true"); <== gisgraphy4
        sb.append("radius=0&limitnbresult=1&format=xml"); // &country=
        // -- apikey
        String apikey = this.getProperties().getString(PROP_gisgraphyApikey,null);
        if (StringTools.isBlank(apikey)) { apikey = super.getAuthorization(); }
        if (!StringTools.isBlank(apikey)) {
            sb.append("&apikey=").append(URIArg.encodeArg(apikey));
        }
        // -- latitude/longitude
        sb.append("&lat=").append(gp.getLatitudeString(GeoPoint.SFORMAT_DEC_5,null));
        sb.append("&lng=").append(gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null));
        return sb.toString();
    }

    private Document GetXMLDocument(String url) 
        throws HTMLTools.HttpIOException
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream input = HTMLTools.inputStream_GET(url, null, SERVICE_TIMEOUT_MS);
            InputStreamReader reader = new InputStreamReader(input, ENCODING_UTF8);
            InputSource inSrc = new InputSource(reader);
            inSrc.setEncoding(ENCODING_UTF8);
            return db.parse(inSrc);
        } catch (ParserConfigurationException pce) {
            // -- XML error?
            Print.logError("Parse error: " + pce);
            return null;
        } catch (SAXException se) {
            // -- XML error?
            Print.logError("Parse error: " + se);
            return null;
        } catch (HTMLTools.HttpIOException hioe) {
            // -- HTTP error
            // --  401 : Blank/missing API key parameter
            // --  403 : IP address not authorized/allowed
            // --  429 : Usage rate exceeded the authorized subscription (*)
            // --  412 : Required parameter is missing
            // --  404 : page not found
            // --  500 : internal error
            int code = hioe.getResponseCode();
            Print.logError("HTTP IO error: ("+code+") " + hioe);
            if (code == 429) {
                // -- rethrow over limit error - candidate for retry
                throw hioe;
            }
            return null;
        } catch (IOException ioe) {
            Print.logError("IO error: " + ioe);
            return null;
        }
    }

    /* return the value of the XML text node */
    protected static String GetNodeText(Node root)
    {
        StringBuffer sb = new StringBuffer();
        if (root != null) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.CDATA_SECTION_NODE) { // CDATA Section
                    sb.append(n.getNodeValue());
                } else
                if (n.getNodeType() == Node.TEXT_NODE) {
                    sb.append(n.getNodeValue());
                } 
            }
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns trus if the ReverseGeocode instance has the required address components
    *** to create an address
    **/
    private boolean hasAddressComponents(ReverseGeocode rg, boolean inclStreet)
    {
        if (rg == null) {
            return false;
        } else
        if (inclStreet && !rg.hasStreetAddress()) {
            return false;
        } else
        if (!rg.hasCity()) {
            return false;
        } else
        if (!rg.hasStateProvince()) {
            return false;
        } else
        if (!rg.hasPostalCode()) {
            return false;
        } else
        if (!rg.hasCountryCode()) {
            return false;
        } else {
            return true;
        }
    }

    /**
    *** Creates a full address from the address components
    **/
    private String createFullAddress(ReverseGeocode rg, boolean inclUS) 
    {
        // -- must have ReverseGeocode
        if (rg == null) {
            return "";
        }
        // -- "Street Address, City, State ZipCode, Country"
        StringBuffer sb = new StringBuffer();
        // -- street address
        String street = rg.getStreetAddress();
        if (!StringTools.isBlank(street)) {
            if (sb.length() > 0) { sb.append(", "); } // no-op, since sb is blank
            sb.append(street);
        }
        // -- city
        String city = rg.getCity();
        if (!StringTools.isBlank(city)){
            if (sb.length() > 0) { sb.append(", "); }
            sb.append(city);
        }
        // -- state
        String state = rg.getStateProvince();
        if (!StringTools.isBlank(state)) {
            if (sb.length() > 0) { sb.append(", "); }
            String code = USState.getCode(state,null);
            if (!StringTools.isBlank(code)) {
                sb.append(code);
            } else {
                sb.append(state);
            }
        }
        // -- zip code
        String zip = rg.getPostalCode();
        if (!StringTools.isBlank(zip)) {
            if (sb.length() > 0) { sb.append(" "); }
            sb.append(zip);
        }
        // -- country
        String country = rg.getCountryCode();
        if (!StringTools.isBlank(country)) {
            if (inclUS || !country.equalsIgnoreCase("US")) {
                if (sb.length() > 0) { sb.append(", "); }
                sb.append(country);
            }
        }
        // -- return
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Main entery point for debugging/testing
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.setAllOutputToStdout(true);
        Print.setEncoding(ENCODING_UTF8);

        /* host */
        String host = RTConfig.getString("host",null);
        if (!StringTools.isBlank(host)) {
            HOST_PRIMARY = host;
        }

        /* failover */
        String failover = RTConfig.getString("failoverHost",null);
        if (!StringTools.isBlank(failover)) {
            HOST_FAILOVER = failover;
        }

        /* GeoPoint */
        GeoPoint gp = new GeoPoint(RTConfig.getString("gp",null));
        if (!gp.isValid()) {
            Print.logInfo("Invalid GeoPoint specified");
            System.exit(1);
        }
        Print.logInfo("Reverse-Geocoding GeoPoint: " + gp);

        /* Reverse Geocoding */
        GisGraphy5 gn = new GisGraphy5("gisgraphy5", null, RTConfig.getCommandLineProperties());

        Print.sysPrintln("RevGeocode = " + gn.getReverseGeocode(gp,false/*isMoving*/,null/*localeStr*/,false/*cache*/,""/*clientID*/,null/*props*/));
        //Print.sysPrintln("Address    = " + gn.getAddressReverseGeocode(gp));
        //Print.sysPrintln("PostalCode = " + gn.getPostalReverseGeocode(gp));
        //Print.sysPrintln("PlaceName  = " + gn.getPlaceNameReverseGeocode(gp));
        // Note: Even though the values are printed in UTF-8 character encoding, the
        // characters may not appear to be properly displayed if the console display
        // does not support UTF-8.

    }

}
