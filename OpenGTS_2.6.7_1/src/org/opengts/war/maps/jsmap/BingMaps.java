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
// Microsoft Development:
//  - https://docs.microsoft.com/en-us/bingmaps/v8-web-control/
// Misc:
//  - http://www.mapchannels.com/dualmaps.aspx
//  - http://garzilla.net/vemaps
// ----------------------------------------------------------------------------
// Note: 
//   When using the Microsoft Bing mapping service, it is your responsibility 
//   to make sure you comply with all of the Microsoft terms of use for this service:
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.maps.jsmap;

import java.util.*;
import java.io.*;

import org.opengts.util.*;

import org.opengts.db.tables.Geozone;
import org.opengts.war.tools.*;
import org.opengts.war.maps.JSMap;

public class BingMaps
    extends JSMap
{

    // ------------------------------------------------------------------------

    private static final String  _BING_MAPCONTROL_URL       = "www.bing.com/api/maps/mapcontrol?branch=release";

    private static final String  PROP_mapcontrol[]          = { "bing.mapcontrol"   }; // http://www.bing.com/api/maps/mapcontrol?branch=release
    private static final String  PROP_useSSL[]              = { "bing.useSSL"       };

    private static final String  PROP_addTrafficLayer[]     = { "bing.addTrafficLayer"  , "addTrafficLayer" };
    private static final String  PROP_navigationBarMode[]   = { "bing.navigationBarMode", "navigationBarMode" };

    // ------------------------------------------------------------------------

    // -- valid zoom values
    //private final int VALID_ZOOM_VALUES[] = new int[] {
    //    // --> Smaller values == Greater meters-per-pixel -->
    //    19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    //};

    //private static final int DEFAULT_ZOOM = 7; // 1..19

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* BingMaps instance */ 
    public BingMaps(String name, String key) 
    {
        super(name, key); 
        this.addSupportedFeature(FEATURE_LATLON_DISPLAY);
        this.addSupportedFeature(FEATURE_DISTANCE_RULER);
        this.addSupportedFeature(FEATURE_GEOZONES);
        this.addSupportedFeature(FEATURE_DETAIL_REPORT);
        this.addSupportedFeature(FEATURE_DETAIL_INFO_BOX);
        this.addSupportedFeature(FEATURE_REPLAY_POINTS);
        this.addSupportedFeature(FEATURE_CENTER_ON_LAST);
        this.addSupportedFeature(FEATURE_CORRIDORS);
    }

    // ------------------------------------------------------------------------

    /* write mapping support JS to stream */ 
    protected void writeJSVariables(PrintWriter out, RequestProperties reqState) 
        throws IOException
    {
        PrivateLabel privLabel   = reqState.getPrivateLabel();
        I18N         i18n        = privLabel.getI18N(BingMaps.class);
        Locale       locale      = reqState.getLocale();
        RTProperties rtp         = this.getProperties();

        // --
        out.write("// --- Bing specific vars ["+this.getName()+"]\n");
        JavaScriptTools.writeJSVar(out, "BING_TRAFFIC_LAYER", rtp.getBoolean(PROP_addTrafficLayer,false));
        JavaScriptTools.writeJSVar(out, "BING_NAVBAR_MODE"  , rtp.getString(PROP_navigationBarMode,""));

        /* general JS vars */
        super.writeJSVariables(out, reqState);

    }

    // ------------------------------------------------------------------------

    /**
    *** Write css to stream
    **/
    public void writeStyle(PrintWriter out, RequestProperties reqState)
        throws IOException 
    {
        super.writeStyle(out, reqState);
        //WebPageAdaptor.writeCssLink(out, reqState, "BingMaps.css", null/*cssDir*/);
    }

    // ------------------------------------------------------------------------

    /**
    *** Write JavaScript references
    **/
    protected void writeJSIncludes(PrintWriter out, RequestProperties reqState)
        throws IOException 
    {
        MapProvider mp = reqState.getMapProvider();
        RTProperties mrtp = (mp != null)? mp.getProperties() : null;
        String mapControlURL = (mrtp != null)? mrtp.getString(PROP_mapcontrol, null) : null;
        if (StringTools.isBlank(mapControlURL)) { // http://www.bing.com/api/maps/mapcontrol?...
            StringBuffer sb = new StringBuffer();
            // -- "https://" or "http://"
            String useSSLStr = mrtp.getString(PROP_useSSL, null);
            boolean useSSL = false; // mrtp.getBoolean(PROP_useSSL, false);
            if (StringTools.isBlank(useSSLStr)) {
                // -- default: follow parent URL secure protocol
                useSSL = reqState.isSecure()? true : false;
            } else 
            if (useSSLStr.equalsIgnoreCase("auto")) {
                // -- auto: follow parent URL secure protocol
                useSSL = reqState.isSecure()? true : false;
            } else {
                // -- explicit: use specified ssl mode
                useSSL = StringTools.parseBoolean(useSSLStr, false);
            }
            sb.append(useSSL? "https://" : "http://");
            // -- Domain/Path
            sb.append(_BING_MAPCONTROL_URL);
            sb.append("&setLang=en");
            // -- key
            String authKey = this.getAuthorization();
            if (!StringTools.isBlank(authKey) && !authKey.startsWith("*")) {
                // -- an API key has been specified
                sb.append("&key=").append(authKey);
            } else {
                // -- no API key specified
            }
            // -- URL
            mapControlURL = sb.toString();
        }
        // --
        super.writeJSIncludes(out, reqState, new String[] {
            JavaScriptTools.qualifyJSFileRef("maps/jsmap.js"),
            mapControlURL,
            JavaScriptTools.qualifyJSFileRef("maps/BingMaps.js")
        });
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the number of supported points for the specified Geozone type
    *** @param type  The Geozone type
    *** @return The number of supported points for the specified Geozone type
    **/
    public int getGeozoneSupportedPointCount(int type)
    {

        /* Geozone type supported? */
        Geozone.GeozoneType gzType = Geozone.getGeozoneType(type);
        if (!Geozone.IsGeozoneTypeSupported(gzType)) {
            return 0;
        }

        /* return supported point count */
        RTProperties rtp = this.getProperties();
        switch (gzType) {
            case POINT_RADIUS        : return rtp.getBoolean(PROP_zone_map_multipoint,false)? Geozone.GetMaxVerticesCount() : 1;
            case BOUNDED_RECT        : return 0; // not yet supported
            case SWEPT_POINT_RADIUS  : return rtp.getBoolean(PROP_zone_map_corridor  ,false)? Geozone.GetMaxVerticesCount() : 0;
            case POLYGON             : return rtp.getBoolean(PROP_zone_map_polygon   ,false)? Geozone.GetMaxVerticesCount() : 0;
        }
        return 0;

    }

    public String[] getGeozoneInstructions(int type, Locale loc)
    {
        I18N i18n = I18N.getI18N(BingMaps.class, loc);
        if (type == Geozone.GeozoneType.POINT_RADIUS.getIntValue()) {
            return new String[] {
                i18n.getString("BingMaps.geozoneNotes.1", "Click to reset center."),
                i18n.getString("BingMaps.geozoneNotes.2", "Click-drag Geozone to move."),
                i18n.getString("BingMaps.geozoneNotes.3", "Shift-click-drag to resize."),
                i18n.getString("BingMaps.geozoneNotes.4", "Ctrl-click-drag for distance.")
            };
        } else
        if (type == Geozone.GeozoneType.POLYGON.getIntValue()) {
            return new String[] {
                i18n.getString("BingMaps.geozoneNotes.1", "Click to reset center."),
                i18n.getString("BingMaps.geozoneNotes.5", "Click-drag corner to resize."),
                i18n.getString("BingMaps.geozoneNotes.4", "Ctrl-click-drag for distance.")
            };
        } else {
            return new String[0];
        }
    }

    // ------------------------------------------------------------------------

}
