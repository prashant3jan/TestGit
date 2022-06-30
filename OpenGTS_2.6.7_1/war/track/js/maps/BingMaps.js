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
// Required funtions defined by this module:
//   new JSMap(String mapID)
//   JSClearLayers()
//   JSSetCenter(JSMapPoint center [, int zoom])
//   JSDrawPushpins(JSMapPushpin pushPin[], int recenterMode, int replay)
//   JSDrawRoute(JSMapPoint points[], String color)
//   JSDrawShape(String type, double radius, JSMapPoint points[], String color, boolean zoomTo, String desc, int ppNdx)
//   JSDrawGeozone(int type, double radius, JSMapPoint points[], String color, int primaryIndex)
//   JSShowPushpin(JSMapPushpin pushPin, boolean center)
//   JSUnload()
// References:
//   https://docs.microsoft.com/en-us/bingmaps/v8-web-control/modules/traffic-module/trafficmanager-class
//   https://blogs.bing.com/maps/2017-03/160-code-samples-for-bing-maps-v8-released-on-github
// ----------------------------------------------------------------------------
// Change History:
//  2019/02/19  Martin D. Flynn
//     -Initial Release
// ----------------------------------------------------------------------------

var DRAG_NONE           = 0x00;
var DRAG_RULER          = 0x01;
var DRAG_GEOZONE        = 0x10;
var DRAG_GEOZONE_CENTER = 0x11;
var DRAG_GEOZONE_RADIUS = 0x12;

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
*** Create Microsoft.Maps.Pushpin()
**/
function jsNewPushpin(  // jsnewImageMarker
    lat/*double*/, lon/*double*/, 
    imageURL/*String*/, iconSize/*[]*/, iconAnchor/*[]*/, 
    shadowURL/*String,null*/, shadowSize/*[],null*/, 
    draggable/*boolean*/,
    zIndex/*0..*/
    ) 
{
    var LL = new Microsoft.Maps.Location(lat,lon);
    var pp = new Microsoft.Maps.Pushpin(LL); // VELatLong
    pp.setOptions({
        anchor             : new Microsoft.Maps.Point(iconAnchor[0], iconAnchor[1]),
        icon               : imageURL,
        draggable          : draggable,
        roundClickableArea : true,
        title              : " ",
        subTitle           : ""
        });
    return pp;
};

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
*** JSMap constructor
**/
function JSMap(element)
{

    /* map */
    try {
        var navBarMode   = Microsoft.Maps.NavigationBarMode.compact; // "compact" looks much better
        var navBarOrient = Microsoft.Maps.NavigationBarOrientation.vertical; // vertical appears better
        if (typeof BING_NAVBAR_MODE === 'undefined') {
            // -- leave as specified above
            console.log("NavigationBarMode not specified, using 'compact' ...");
        } else
        if (BING_NAVBAR_MODE == "default") {
            navBarMode   = Microsoft.Maps.NavigationBarMode.default; 
            navBarOrient = Microsoft.Maps.NavigationBarOrientation.vertical;
        } else
        if (BING_NAVBAR_MODE == "compact") {
            navBarMode   = Microsoft.Maps.NavigationBarMode.compact;
            navBarOrient = Microsoft.Maps.NavigationBarOrientation.vertical;
        } else
        if (BING_NAVBAR_MODE == "minified") {
            navBarMode   = Microsoft.Maps.NavigationBarMode.minified; // supports traffic toggle button
            navBarOrient = Microsoft.Maps.NavigationBarOrientation.vertical;
        } else {
            // -- leave as specified above
            console.log("Unrecognized NavigationBarMode: " + BING_NAVBAR_MODE);
        }
        // --
        this.bingMap = new Microsoft.Maps.Map(element, {
            mapTypeId                       : Microsoft.Maps.MapTypeId.road,
            navigationBarMode               : navBarMode,
            navigationBarOrientation        : navBarOrient,
            disableMapTypeSelectorMouseOver : true,  // MapTypeId pulldown must be clicked to open
            disableBirdseye                 : true,  // get rid of useless "Birdseye" view
            showTrafficButton               : true,  // only works on "NavigationBarMode.minified"
            showZoomButtons                 : true,  // include zoom buttons
            disableScrollWheelZoom          : false, // scroll zoom enabled
            showLocateMeButton              : false, // not useful on tracking map
            enableClickableLogo             : false  // "Bing" logo should not be clickable
            });
    } catch (e) {
        alert("Error loading Bing map:\n" + e);
        if (this.bingMap == null) { return; }
    }

    /* traffic layer */
    this.trafficManager = null;
    if (!(typeof BING_TRAFFIC_LAYER === 'undefined') && BING_TRAFFIC_LAYER) {
        try {
            var self = this;
            Microsoft.Maps.loadModule('Microsoft.Maps.Traffic', function () {
                var map = self.bingMap;
                var trafMgr = new Microsoft.Maps.Traffic.TrafficManager(map);
                self.trafficManager = trafMgr;
                self._showTraffic();
                // -- mouse event below does not work on the trafficManager object
                //Microsoft.Maps.Events.addHandler(self.trafficManager, "click", function () {
                //    self._hideTraffic();
                //    });
                /*
                // <a id="TrafficToggleButton" class="NavBar_Button NavBar_trafficToggle selected" title="Turn off traffic" href="#" role="button"></a>
                try {
                    var mapDiv = document.getElementById("jsmap");
                    console.log("Adding Traffic NavBar button ...");
                    var onclick = function() {
                        trafMgr._hideTraffic();
                    };
                    $(mapDiv).find('.NavBar_Container').append(
                        $('<a>').attr('id', 'TrafficToggleButton').
                        addClass('NavBar_Button NavBar_trafficToggle').attr('title', 'Turn off traffic').
                        attr('href', '#').attr('role','button').click(onclick));
                    console.log("... Traffic NavBar button created");
                } catch (e) {
                    alert("Error adding Traffic button:\n" + e);
                }
                */
                });
        } catch (e) {
            alert("Error loading Traffic layer:\n" + e);
        }
    }

    /* map style */
    this._setDefaultMapStyle();

    /* map attributes */
    element.style.cursor = "crosshair";

    /* last mousedown X/Y */
    this.lastX = 0;
    this.lastY = 0;

    /* draw layers */
    this.bingMainLayer    = new Microsoft.Maps.Layer();
    this.bingGeozoneLayer = new Microsoft.Maps.Layer();
    this.bingShapeLayer   = new Microsoft.Maps.Layer();
    this.bingRulerLayer   = new Microsoft.Maps.Layer();
    this.bingMap.layers.insert(this.bingMainLayer);
    this.bingMap.layers.insert(this.bingGeozoneLayer);
    this.bingMap.layers.insert(this.bingShapeLayer);
    this.bingMap.layers.insert(this.bingRulerLayer);
    
    /* popup info box */
    this.visiblePopupInfoBox = null;
    this.pushpinInfobox = new Microsoft.Maps.Infobox(new Microsoft.Maps.Location(0,0), {
        maxWidth        : 550,
        maxHeight       : 450,
        showCloseButton : true,
        showPointer     : true,
        visible         : false
        }); // used in _event_OnPushpinClick
    this.pushpinInfobox.setMap(this.bingMap);

    /* drawn shapes */
    this.drawShapes = [];

    /* replay vars */
    this.replayTimer = null;
    this.replayIndex = 0;
    this.replayInterval = (REPLAY_INTERVAL < 100)? 100 : REPLAY_INTERVAL;
    this.replayInProgress = false;
    this.replayPushpins = [];

    /* drag vars */
    this.dragType = DRAG_NONE;
    this.dragRulerLatLon = null;    // VELatLong
    this.dragMarker = null;
    this.dragZoneOffsetLat = 0.0;
    this.dragZoneOffsetLon = 0.0;
    this.geozoneCenter = null;
    this.geozoneShape  = null;
    this.geozonePoints = null;      // JSMapPoint[]
    this.primaryIndex  = -1;
    this.primaryCenter = null;      // JSMapPoint

    /* Lat/Lon display */
    this.latLonDisplay = jsmGetLatLonDisplayElement();
    jsmSetLatLonDisplay(0,0);

    /* mouse event handlers */
    try {
        var self = this;
        Microsoft.Maps.Events.addHandler(this.bingMap, "mousedown" , function (e) { return self._event_OnMouseDown(e); });
        Microsoft.Maps.Events.addHandler(this.bingMap, "mousemove" , function (e) { return self._event_OnMouseMove(e); });
        Microsoft.Maps.Events.addHandler(this.bingMap, "mouseup"   , function (e) { return self._event_OnMouseUp(e);   });
        Microsoft.Maps.Events.addHandler(this.bingMap, "mouseover" , function (e) { return self._event_OnMouseOver(e); });
        Microsoft.Maps.Events.addHandler(this.bingMap, "mouseout"  , function (e) { return self._event_OnMouseOut(e);  });
        Microsoft.Maps.Events.addHandler(this.bingMap, "click"     , function (e) { return self._event_OnMapClick(e);  });
    } catch (e) {
        alert("Error setting mouse events:\n" + e);
    }

};

/**
*** Set map style
**/
JSMap.prototype._setDefaultMapStyle = function()
{
    if ((DEFAULT_VIEW == "aerial") || (DEFAULT_VIEW == "satellite")) {
        this.bingMap.setView({ mapTypeId : Microsoft.Maps.MapTypeId.aerial });
    } else
    if ((DEFAULT_VIEW == "hybrid") || (DEFAULT_VIEW == "auto")) {
        this.bingMap.setView({ mapTypeId : Microsoft.Maps.MapTypeId.auto });
    } else
    if (DEFAULT_VIEW == "birdseye") {
        this.bingMap.setView({ mapTypeId : Microsoft.Maps.MapTypeId.birdseye });
    } else {
        this.bingMap.setView({ mapTypeId : Microsoft.Maps.MapTypeId.road });
    }
};

// ----------------------------------------------------------------------------

/**
*** Unload/release resources
**/
JSMap.prototype.JSUnload = function()
{
    //
};

// ----------------------------------------------------------------------------

/**
*** Clear all pushpins and drawn lines
**/
JSMap.prototype.JSClearLayers = function()
{
    try { this.bingMainLayer.clear(); } catch (e) {} // DeleteAllShapes, DeleteAllPolylines
    this._removeShapes();
    this._clearReplay();
    this.centerBoundsPoints = [];
};

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
*** Show Traffic manager/layer
**/
JSMap.prototype._showTraffic = function()
{
    if (this.trafficManager != null) {
      //alert("Showing traffic : " + this.trafficManager);
        //console.log(this.trafficManager);
        this.trafficManager.show();
        //this.trafficManager.hideIncidents();
        //this.trafficManager.hideLegend();
    } else {
        //alert("TrafficManager is null");
    }
}

/**
*** Hide Traffic manager/layer
**/
JSMap.prototype._hideTraffic = function()
{
    /*mdf*/alert("Hide traffic");
    if (this.trafficManager != null) {
        this.trafficManager.hide(); 
      //this.trafficManager.setOptions({visible: false});
    }
}

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
*** Pause/Resume replay
**/
JSMap.prototype.JSPauseReplay = function(replay)
{
    /* stop replay? */
    if (!replay || (replay <= 0) || !this.replayInProgress) {
        // stopping replay
        this._clearReplay();
        return REPLAY_STOPPED;
    } else {
        // replay currently in progress
        if (this.replayTimer == null) {
            // replay is "PAUSED" ... resuming replay
            this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox();
            jsmHighlightDetailRow(-1, false);
            this._startReplayTimer(replay, 100);
            return REPLAY_RUNNING;
        } else {
            // replaying "RUNNING" ... pausing replay
            this._stopReplayTimer();
            return REPLAY_PAUSED;
        }
    }
};

/**
*** Start the replay timer
**/
JSMap.prototype._startReplayTimer = function(replay, interval)
{
    if (this.replayInProgress) {
        this.replayTimer = setTimeout("jsmap._replayPushpins("+replay+")", interval);
    }
    jsmSetReplayState(REPLAY_RUNNING);
};

/**
*** Stop the current replay timer
**/
JSMap.prototype._stopReplayTimer = function()
{
    if (this.replayTimer != null) { 
        clearTimeout(this.replayTimer); 
        this.replayTimer = null;
    }
    jsmSetReplayState(this.replayInProgress? REPLAY_PAUSED : REPLAY_STOPPED);
};

/**
*** Clear any current replay in process
**/
JSMap.prototype._clearReplay = function()
{
    this.replayPushpins = [];
    this.replayInProgress = false;
    this._stopReplayTimer();
    this.replayIndex = 0;
    jsmHighlightDetailRow(-1, false);
};

/**
*** Gets the current replay state
**/
JSMap.prototype._getReplayState = function()
{
    if (this.replayInProgress) {
        if (this.replayTimer == null) {
            return REPLAY_PAUSED;
        } else {
            return REPLAY_RUNNING;
        }
    } else {
        return REPLAY_STOPPED;
    }
};

// ----------------------------------------------------------------------------

/**
*** Sets the center of the map
**/
JSMap.prototype.JSSetCenter = function(center, zoom)
{
    var vpt = (center != null)? new Microsoft.Maps.Location(center.lat, center.lon) : null; // VELatLong
    // prefer that this "recentering" NOT be animated!
    if (!vpt) {
        // -- no-op
    } else
    if (!zoom || (zoom == 0)) {
        this._setCenter(vpt, 0);
    } else
    if (zoom > 0) {
        this._setCenter(vpt, zoom);
    } else {
        var vb = new JSBounds();
        vb.extend(center);
        this._setCenter(vpt, this._calcBestZoom(vb));
    }
};

/**
*** Draw the specified pushpins on the map
*** @param pushPins  An array of JSMapPushpin objects
*** @param recenter  True to cause the map to re-center on the drawn pushpins
*** @param replay    Replay mode
**/
JSMap.prototype.JSDrawPushpins = function(pushPins, recenterMode, replay)
{

    /* clear replay (may be redundant, but repeated just to make sure) */
    this._clearReplay();
    this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox(this.dragMarker);
    var mapStyle = this.bingMap.getImageryId();

    /* drawn pushpins */
    var drawPushpins = [];

    /* recenter map on points */
    var points = [];
    var pointCount = 0;
    if ((pushPins != null) && (pushPins.length > 0)) {
        // extend bounding box around displayed pushpins
        for (var i = 0; i < pushPins.length; i++) {
            var pp = pushPins[i]; // JSMapPushpin
            //alert("JSDroawPushpin(#"+(i+1)+"/"+pushPins.length+"): show="+pp.show+", "+pp.lat+"/"+pp.lon);
            if (pp.show && ((pp.lat != 0.0) || (pp.lon != 0.0))) {
                pointCount++;
                var veLatLon = new Microsoft.Maps.Location(pp.lat,pp.lon); // VELatLong
                this.centerBoundsPoints.push(veLatLon);
                points.push(veLatLon);
                drawPushpins.push(pp);
            }
        }
    }
    if (recenterMode > 0) {
        // Recenter modes:
        //  0 = none (leave map positioned as-is)
        //  1 = center on last point only (no zoom)
        //  2 = center and zoom
        try {
            if (pointCount <= 0) {
                var centerPt   = new Microsoft.Maps.Location(DEFAULT_CENTER.lat, DEFAULT_CENTER.lon); // VELatLong
                var zoomFactor = DEFAULT_ZOOM;
                this._setCenter(centerPt, zoomFactor);
            } else
            if (recenterMode == RECENTER_LAST) { // center on last point
                var centerPt   = points[points.length - 1];
                var zoomFactor = -1; // default VirtualEarth zoom
                this._setCenter(centerPt, zoomFactor);
            } else
            if (recenterMode == RECENTER_PAN) { // pan to last point
                var centerPt   = points[points.length - 1];
                var zoomFactor = -1; // default VirtualEarth zoom
                this._setCenter(centerPt, zoomFactor);
            } else {
                if (mapStyle == Microsoft.Maps.MapTypeId.birdseye) { // VEMapStyle
                    this._setDefaultMapStyle();
                }
                this.bingMap.setView({
                    bounds  : Microsoft.Maps.LocationRect.fromLocations(this.centerBoundsPoints),
                    padding : 100
                    });
            }
        } catch (e) {
            /*mdf*/alert("Error: [JSDrawPushpins] " + e);
            return;
        }
    }
    if (pointCount <= 0) {
        return;
    }

    /* replay pushpins? */
    if (replay && (replay >= 1)) {
        this.replayIndex = 0;
        this.replayInProgress = true;
        this.replayPushpins = drawPushpins;
        this._startReplayTimer(replay, 100);
        return;
    }

    /* draw pushpins now */
    var pushpinErr = null;
    for (var i = 0; i < drawPushpins.length; i++) {
        var pp = drawPushpins[i];
        try {
            this._addPushpin(pp);
        } catch (e) {
            if (pushpinErr == null) { pushpinErr = e; }
            /*mdf*/alert("Error: [JSDrawPushpins] " + e);
        }
    }

    /* Birdseye pushpin accuracy */
    //this.bingMap.SetShapesAccuracy(VEShapeAccuracy.Pushpin);

    /* Birdseye scene */
    //var lastPP = drawPushpins[drawPushpins.length - 1];
    //this.bingMap.SetBirdseyeScene(new Microsoft.Maps.Location(lastPP.lat, lastPP.lon)); // VELatLong

    /* any errors? */
    if (pushpinErr != null) {
        alert("Error: adding pushpins:\n" + pushpinErr);
    }

};

/**
*** Draw the specified PointsOfInterest pushpins on the map
*** @param pushPins  An array of JSMapPushpin objects
**/
JSMap.prototype.JSDrawPOI = function(pushPins)
{
    
    /* hide infobox */
    this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox();

    /* draw pushpins now */
    if ((pushPins != null) && (pushPins.length > 0)) {
        var pushpinErr = null;
        for (var i = 0; i < pushPins.length; i++) {
            var pp = pushPins[i];
            try {
                this._addPushpin(pp);
            } catch (e) {
                if (pushpinErr == null) { pushpinErr = e; }
            }
        }
    }

    /* any errors? */
    if (pushpinErr != null) {
        alert("Error: adding pushpins:\n" + pushpinErr);
    }

};

/**
*** Sets the center/zoom of the map
**/
JSMap.prototype._setCenter = function(center, zoom)
{
    if (zoom && (zoom > 0)) {
        this.bingMap.setView({
            center : center,
            zoom   : zoom
            });
    } else {
        this.bingMap.setView({
            center : center,
            });
    }
};

/**
*** Adds a single pushpin to the map
*** @param pp  The JSMapPushpin object to add to the map
**/
JSMap.prototype._addPushpin = function(pp) // JSMapPushpin
{

    pp.map = this.bingMap;

    /* background marker */
    if (pp.bgUrl) {
        var bgMarker = jsNewPushpin(
            pp.lat, pp.lon,
            pp.bgUrl, pp.bgSize, pp.bgOffset,
            null, null,
            false,
            -1);
        bgMarker.pp = pp;
        pp.bgMarker = bgMarker;
        this.bingMainLayer.add(bgMarker);
    }

    /* pushpin marker */
    var ppMarker = jsNewPushpin(
        pp.lat, pp.lon,
        pp.iconUrl, pp.iconSize, pp.iconHotspot,
        pp.shadowUrl, pp.shadowSize,
        false,
        -1);
    ppMarker.pp = pp;
    pp.marker = ppMarker;
    var self = this;
    pp.ibHandler = Microsoft.Maps.Events.addHandler(ppMarker, "click", function (e) { return self._event_OnPushpinClick(e); });
    this.bingMainLayer.add(ppMarker);

};

/**
*** Replays the list of pushpins on the map
*** @param pp  The JSMapPushpin object to add to the map
**/
JSMap.prototype._replayPushpins = function(replay)
{

    /* advance to next valid point */
    while (true) {
        if (this.replayIndex >= this.replayPushpins.length) {
            this._clearReplay();
            jsmHighlightDetailRow(-1, false);
            return; // stop
        }
        var pp = this.replayPushpins[this.replayIndex]; // JSMapPushpin
        if ((pp.lat != 0.0) || (pp.lon != 0.0)) {
            break; // valid point
        }
        this.replayIndex++;
    }

    /* add pushpin */
    try {
        var lastNdx = this.replayIndex - 1;
        var pp = this.replayPushpins[this.replayIndex++]; // JSMapPushpin
        pp.hoverPopup = true;
        if (REPLAY_SINGLE && (lastNdx >= 0)) {
            var lastPP = this.replayPushpins[lastNdx]; // JSMapPushpin
            if (lastPP.marker) {
                this.bingMainLayer.remove(lastPP.marker); // DeleteShape
            }
            if (lastPP.bgMarker) {
                this.bingMainLayer.remove(lastPP.bgMarker); // DeleteShape
            }
        }
        this._addPushpin(pp);
        if (replay && (replay >= 2)) {
            this._showPushpinPopup(pp);
        } else {
            jsmHighlightDetailRow(pp.rcdNdx, true);
        }
        this._startReplayTimer(replay, this.replayInterval);
    } catch (e) {
        // ignore
    }

};

// ----------------------------------------------------------------------------

/**
*** This method should cause the info-bubble popup for the specified pushpin to display
*** @param pushPin   The JSMapPushpin object which popup its info-bubble
**/
JSMap.prototype.JSShowPushpin = function(pp, center)
{
    if (pp) {
        if (pp.popupShown) {
            this._hidePushpinPopup(pp);
        } else {
            if (center || !this._isPointOnMap(pp.lat,pp.lon,5,5,5,5)) {
                this.JSSetCenter(new JSMapPoint(pp.lat, pp.lon));
            }
            this._showPushpinPopup(pp);
        }
    }
};

JSMap.prototype._isPointOnMap = function(lat, lon, margTop, margLeft, margBott, margRght)
{
    if ((MAP_HEIGHT > 0) && (MAP_WIDTH > 0)) {
        var top   = 0                 + margTop; // this.bingMap.GetTop();
        var left  = 0                 + margLeft; // this.bingMap.GetLeft();
        var bott  = top  + MAP_HEIGHT - margBott;
        var rght  = left + MAP_WIDTH  - margRght;
        var TL    = this.bingMap.tryPixelToLocation(new Microsoft.Maps.Point(left, top )); // VELatLong=PixelToLatLong(VEPixel)
        var BR    = this.bingMap.tryPixelToLocation(new Microsoft.Maps.Point(rght, bott)); // VELatLong=PixelToLatLong(VEPixel)
        //alert("TopLeft="+TL.latitude+"/"+TL.longitude+", BottomRight="+BR.latitude+"/"+BR.longitude);
        if ((lat > TL.latitude) || (lat < BR.latitude)) {
            return false;
        } else
        if ((lon < TL.longitude) || (lon > BR.longitude)) {
            return false;
        } else {
            return true;
        }
    } else {
        return true;
    }
};

JSMap.prototype._showPushpinPopup = function(pp)
{
    this._hidePushpinPopup(this.visiblePopupInfoBox);
    if (pp && !pp.popupShown && pp.map && this.pushpinInfobox) {
        this.pushpinInfobox.setOptions({
            location    : new Microsoft.Maps.Location(pp.lat,pp.lon), // e.target.getLocation(),
            title       : pp.title,
            description : pp.getHTML(),
            visible     : true
        });
        pp.popupShown = true;
        this.visiblePopupInfoBox = pp;
        jsmHighlightDetailRow(pp.rcdNdx, true);
    } else {
        this.visiblePopupInfoBox = null;
    }
};

JSMap.prototype._hidePushpinPopup = function(pp)
{
    if (pp && pp.popupShown) {
        this.pushpinInfobox.setOptions({ visible : false }); // pp.map.HideInfoBox(pp.marker)
        pp.popupShown = false;
        jsmHighlightDetailRow(pp.rcdNdx, false);
    }
};

// ----------------------------------------------------------------------------

/**
*** Draws a line between the specified points on the map.
*** @param points   An array of JSMapPoint objects
**/
var routLineNdx = 0;
JSMap.prototype.JSDrawRoute = function(points, color)
{
    if (points && (points.length >= 2)) {
        var latlon = [];
        for (var i = 0; i < points.length; i++) {
            latlon.push(new Microsoft.Maps.Location(points[i].lat, points[i].lon)); // VELatLong
        }
        try {
            var name = "routeLine" + (routLineNdx++);
            var rgb  = rgbVal(color); // ie. Convert "#FF2222" to { R:255, G:34, B:34 }
            var veColor = new Microsoft.Maps.Color(1.00,rgb.R,rgb.G,rgb.B); // VEColor
            var line = new Microsoft.Maps.Polyline(latlon); // VEPolyline
            line.setOptions({
                    strokeThickness : 2,
                    strokeColor     : veColor, // VEColor: #FF6422
                    fillColor       : veColor, // VEColor: #FF2222
                });
            this.bingMainLayer.add(line); //.AddPolyline(new VEPolyline(name, latlon, veColor, 2));
        } catch (e) {
            alert("Error creating route:\n" + e);
        }
    }
};

// ----------------------------------------------------------------------------

/**
*** Remove previously drawn shapes 
**/
JSMap.prototype._removeShapes = function()
{
    this.bingShapeLayer.clear(); // DeleteAllShapes
    this.drawShapes = [];
};

/**
*** Draws a Shape on the map at the specified location
*** @param type     The Geozone shape type
*** @param radiusM  The circle radius, in meters
*** @param points   An array of points (JSMapPoint[])
*** @param color    Shape color
*** @param zoomTo   True to zoom to drawn shape
*** @param desc     The shape description
*** @param ppNdx    The pushpin index
*** @return True if shape was drawn, false otherwise
**/
JSMap.prototype.JSDrawShape = function(type, radiusM, verticePts, color, zoomTo, desc, ppNdx)
{
    //alert("Draw shape: " + type);

    /* no type? */
    if (!type || (type == "") || (type == "!")) {
        this._removeShapes();
        return false;
    }

    /* clear existing shapes? */
    if (type.startsWith("!")) { 
        this._removeShapes();
        type = type.substr(1); 
    }

    /* no geopoints? */
    if (!verticePts || (verticePts.length == 0)) {
        return false;
    }
    
    /* color */
    if (!color || (color == "")) {
        color = "#0000FF";
    }
    var rgb = rgbVal(color);  // ie. Convert "#FF2222" to { R:255, G:34, B:34 }

    /* zoom bounds */
    var mapBounds = zoomTo? new JSBounds() : null;

    /* draw shape */
    var didDrawShape = false;
    if (type == "circle") { // ZONE_POINT_RADIUS

        for (var i = 0; i < verticePts.length; i++) {
            var vp = verticePts[i]; // JSMapPoint
            if ((vp.lat == 0.0) && (vp.lon == 0.0)) { continue; }
            var center = new Microsoft.Maps.Location(vp.lat,vp.lon); // VELatLong
            var circlePoints = this._getCirclePoints(center, radiusM);
            var shape = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
            shape.setLocations(circlePoints);
            shape.setOptions({
                    strokeThickness : 1,
                    strokeColor     : new Microsoft.Maps.Color(1.00,rgb.R,rgb.G,rgb.B), // VEColor
                    fillColor       : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B)  // VEColor
                });
            if (mapBounds) { 
                mapBounds.extend(vp);
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM,   0.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM,  90.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM, 180.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM, 270.0));
            }
            this.drawShapes.push(shape);
            this.bingShapeLayer.add(shape);
            didDrawShape = true;
        }

    } else
    if (type == "rectangle") {

        if (verticePts.length >= 2) {

            /* create rectangle */
            var vp0   = verticePts[0];
            var vp1   = verticePts[1];
            var TL    = new Microsoft.Maps.Location(((vp0.lat>vp1.lat)?vp0.lat:vp1.lat),((vp0.lon<vp1.lon)?vp0.lon:vp1.lon)); // VELatLong
            var TR    = new Microsoft.Maps.Location(((vp0.lat>vp1.lat)?vp0.lat:vp1.lat),((vp0.lon>vp1.lon)?vp0.lon:vp1.lon)); // VELatLong
            var BL    = new Microsoft.Maps.Location(((vp0.lat<vp1.lat)?vp0.lat:vp1.lat),((vp0.lon<vp1.lon)?vp0.lon:vp1.lon)); // VELatLong
            var BR    = new Microsoft.Maps.Location(((vp0.lat<vp1.lat)?vp0.lat:vp1.lat),((vp0.lon>vp1.lon)?vp0.lon:vp1.lon)); // VELatLong
            var vePts = [ TL, TR, BR, BL, TL ];
            var shape = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
            shape.setLocations(vePts);
            shape.setOptions({
                    strokeThickness : 1,
                    strokeColor     : new Microsoft.Maps.Color(1.00,rgb.R,rgb.G,rgb.B), // VEColor
                    fillColor       : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B)  // VEColor
                });
            if (mapBounds) {
                mapBounds.extend(vp0); 
                mapBounds.extend(vp1); 
            }
            this.drawShapes.push(shape);
            this.bingShapeLayer.add(shape);
            didDrawShape = true;

        }
            
    } else
    if (type == "polygon") {
       
        if (verticePts.length >= 3) {

            var vePts = [];
            for (var p = 0; p < verticePts.length; p++) {
                var vePt = new Microsoft.Maps.Location(verticePts[p].lat, verticePts[p].lon); // VELatLong
                vePts.push(vePt);
                if (mapBounds) { 
                    mapBounds.extend(verticePts[p]); 
                }
            }
            var poly = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
            poly.setLocations(vePts);
            poly.setOptions({
                    strokeThickness : 1,
                    strokeColor     : new Microsoft.Maps.Color(1.00,rgb.R,rgb.G,rgb.B), // VEColor
                    fillColor       : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B)  // VEColor
                });
            this.drawShapes.push(poly);
            this.bingShapeLayer.add(poly);
            didDrawShape = true;

        }

    } else
    if (type == "corridor") { // ZONE_SWEPT_POINT_RADIUS

        var lastPT = null;
        for (var i = 0; i < verticePts.length; i++) {
            var vp = verticePts[i]; // JSMapPoint
            if ((vp.lat == 0.0) && (vp.lon == 0.0)) { continue; }
            var center = new Microsoft.Maps.Location(vp.lat,vp.lon); // VELatLong
            var circlePoints = this._getCirclePoints(center, radiusM);
            var circleShape = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
            circleShape.setLocations(circlePoints);
            circleShape.setOptions({
                    strokeThickness : 1,
                    strokeColor     : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B), // VEColor
                    fillColor       : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B)  // VEColor
                });
            if (mapBounds) { 
                mapBounds.extend(vp);
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM,   0.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM,  90.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM, 180.0));
                mapBounds.extend(this._calcRadiusPoint(vp, radiusM, 270.0));
            }
            this.drawShapes.push(circleShape);
            this.bingShapeLayer.add(circleShape);
            
            if (lastPT != null) {
                var ptA = lastPT;   // JSMapPoint
                var ptB = vp;       // JSMapPoint
                var hAB = geoHeading(ptA.lat, ptA.lon, ptB.lat, ptB.lon) - 90.0; // perpendicular
                var rp1 = this._calcRadiusPoint(ptA, radiusM, hAB        ); // JSMapPoint
                var rp2 = this._calcRadiusPoint(ptB, radiusM, hAB        ); // JSMapPoint
                var rp3 = this._calcRadiusPoint(ptB, radiusM, hAB + 180.0); // JSMapPoint
                var rp4 = this._calcRadiusPoint(ptA, radiusM, hAB + 180.0); // JSMapPoint
                var rectPts = [];
                rectPts.push(new Microsoft.Maps.Location(rp1.lat, rp1.lon)); // VELatLong
                rectPts.push(new Microsoft.Maps.Location(rp2.lat, rp2.lon)); // VELatLong
                rectPts.push(new Microsoft.Maps.Location(rp3.lat, rp3.lon)); // VELatLong
                rectPts.push(new Microsoft.Maps.Location(rp4.lat, rp4.lon)); // VELatLong
                var rectShape = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
                rectShape.setLocations(rectPts);
                rectShape.setOptions({
                        strokeThickness : 1,
                        strokeColor     : new Microsoft.Maps.Color(1.00,rgb.R,rgb.G,rgb.B), // VEColor
                        fillColor       : new Microsoft.Maps.Color(0.10,rgb.R,rgb.G,rgb.B)  // VEColor
                    });
                this.drawShapes.push(rectShape);
                this.bingShapeLayer.add(rectShape);
            }
            lastPT = vp;

            didDrawShape = true;
        }

    } else
    if (type == "center") {

        if (mapBounds) {
            for (var p = 0; p < verticePts.length; p++) {
                var jsPt = new JSMapPoint(verticePts[p].lat, verticePts[p].lon);
                mapBounds.extend(jsPt);
            }
            didDrawShape = true;
        }

    }

    /* center on shape */
    if (didDrawShape && zoomTo && mapBounds) {
        var centerPt   = mapBounds.getCenter(); // JSMapPoint
        var zoomFactor = this._calcBestZoom(mapBounds);
        this._setCenter(new Microsoft.Maps.Location(centerPt.lat, centerPt.lon), zoomFactor); // VELatLong
    }

    /* shape not supported */
    return didDrawShape;

};

// ----------------------------------------------------------------------------

/**
*** Draws a Geozone on the map at the specified location
*** @param type     The Geozone type
*** @param radiusM  The circle radius, in meters
*** @param points   An array of points
*** @param color    The color of the geozone
*** @param primNdx  Index of primary point
*** @return An object representing the Circle.
**/
JSMap.prototype.JSDrawGeozone = function(type, radiusM, points, color, primNdx)
{
    //alert("Draw Geozone: " + type);

    /* Geozone mode */
    jsvGeozoneMode = true;

    /* remove old geozone */
    //for (var i = 0; i < this.geozonePoints.length; i++) { this.geozonePoints[i].remove(); }
    //this.geozonePoints = [];
    this.geozoneShape = null;
    this.geozoneCenter = null;  // VELatLong
    this.bingGeozoneLayer.clear();

    /* save geozone points */
    this.geozonePoints = points;
    this.primaryIndex  = primNdx;

    /* no points? */
    if ((points == null) || (points.length <= 0)) {
        //alert("No Zone center!");
        return null;
    }
    
    /* zone shape color */
    this.shapeColor = (color && (color != ""))? color : "#00B400";
    this.lineWidth  = 2;
  //this.opacity    = 1.0;

    /* point-radius */
    if (type == ZONE_POINT_RADIUS) {
        //alert("Draw Geozone Point-Radius: points=" + points.length);

        /* adjust radius */
        if (isNaN(radiusM))              { radiusM = 5000; }
        if (radiusM > MAX_ZONE_RADIUS_M) { radiusM = MAX_ZONE_RADIUS_M; }
        if (radiusM < MIN_ZONE_RADIUS_M) { radiusM = MIN_ZONE_RADIUS_M; }

        /* draw points */
        var count = 0;
        var mapBounds = new JSBounds();
        for (var i = 0; i < points.length; i++) {
            var c = points[i]; // JSMapPoint
            if ((c.lat != 0.0) || (c.lon != 0.0)) {
                var isPrimary = (i == primNdx);
                if (jsvZoneEditable || jsvShowVertices) {
                    // alert("Geozone #"+i+" Center="+ c.lat+"/"+c.lon+", Radius="+radiusM);
                    var gc = new Microsoft.Maps.Location(c.lat,c.lon);
                    var shape = this._addGeozoneCircleShape(gc, radiusM, this.shapeColor, this.lineWidth, isPrimary); // VELatLong
                    var gzPP = jsNewPushpin(
                        c.lat, c.lon,                                               // lat,lon
                        "http://labs.google.com/ridefinder/images/mm_20_blue.png",  // imageURL
                        [ 12, 20 ],                                                 // imageSize
                        [  6, 20 ],                                                 // imageAnchor
                        "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
                        [ 22, 20 ],                                                 // shadowSize
                        true,                                                       // draggable
                        -1);
                    var self = this;
                  //gzPP.dragHandler = Microsoft.Maps.Events.addHandler(gzPP, "drag"   , function (e) { return self._event_OnGeozoneCenterDragEnd(e); });
                    gzPP.dragHandler = Microsoft.Maps.Events.addHandler(gzPP, "dragend", function (e) { return self._event_OnGeozoneCenterDragEnd(e); });
                    this.bingGeozoneLayer.add(gzPP);
                    // -- radius pushpin
                    var radC = this._calcRadiusPoint(c, radiusM, 45.0);
                    var radPP = jsNewPushpin(
                        radC.lat, radC.lon,                                         // lat,lon
                        "http://labs.google.com/ridefinder/images/mm_20_red.png",   // imageURL
                        [ 12, 20 ],                                                 // imageSize
                        [  6, 20 ],                                                 // imageAnchor
                        "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
                        [ 22, 20 ],                                                 // shadowSize
                        true,                                                       // draggable
                        -1);
                    radPP.verticeIndex = 0;
                    var self = this;
                  //radPP.verticeHandler = Microsoft.Maps.Events.addHandler(radPP, "drag"   , function (e) { return self._event_OnGeozoneVerticeDragEnd(e); });
                    radPP.verticeHandler = Microsoft.Maps.Events.addHandler(radPP, "dragend", function (e) { return self._event_OnGeozoneVerticeDragEnd(e); });
                    this.bingGeozoneLayer.add(radPP);
                } else {
                    // alert("Geozone #"+i+" Not Editable! "+ c.lat+"/"+c.lon+", Radius="+radiusM);
                }
                mapBounds.extend(c);
                mapBounds.extend(this._calcRadiusPoint(c, radiusM,   0.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM,  90.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM, 180.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM, 270.0));
                count++;
            } else {
                // alert("Skipping null/empty points: " + i);
            }
        }

        /* center on geozone */
        var centerPt = DEFAULT_CENTER; // JSMapPoint
        var zoom     = DEFAULT_ZOOM;
        if (count > 0) {
            centerPt = mapBounds.getCenter(); // JSMapPoint
            zoom     = this._calcBestZoom(mapBounds);
        }
        //alert("Geozone Center="+centerPt.lat+"/"+centerPt.lon+", Zoom="+zoom);
        this._setCenter(new Microsoft.Maps.Location(centerPt.lat, centerPt.lon), zoom); // VELatLong

    } else
    if (type == ZONE_POLYGON) {

        /* adjust radius */
        if (isNaN(radiusM))              { radiusM = 5000; }
        if (radiusM > MAX_ZONE_RADIUS_M) { radiusM = MAX_ZONE_RADIUS_M; }
        if (radiusM < MIN_ZONE_RADIUS_M) { radiusM = MIN_ZONE_RADIUS_M; }

        var isDragging = false;

        /* draw vertices */
        var count = 0;
        var mapBounds = new JSBounds();
        var polyPts = []; // JSMapPoint[]
        for (var i = 0; i < points.length; i++) {
            var c = points[i]; // JSMapPoint
            if ((c.lat != 0.0) || (c.lon != 0.0)) {
                var isPrimary = (i == primNdx);
                var center    = (isPrimary && isDragging)? this.primaryCenter : c; // JSMapPoint
                if (isPrimary) {
                    this.primaryCenter = center; // JSMapPoint
                }
                polyPts.push(center); // JSMapPoint
                mapBounds.extend(center);
                count++;
            }
        }
        if (polyPts.length >= 3) {
            // -- draw/add polygon
            this._addPolygonShape(polyPts, this.shapeColor, this.lineWidth);
            // -- draw vertice pushpins
            if (jsvZoneEditable || jsvShowVertices) {
                for (var i = 0; i < polyPts.length; i++) {
                    var c = polyPts[i]; // JSMapPoint
                    var vtPP = jsNewPushpin( // jsnewImageMarker
                        c.lat, c.lon,                                               // lat,lon
                        "http://labs.google.com/ridefinder/images/mm_20_red.png",   // imageURL
                        [ 12, 20 ],                                                 // imageSize
                        [  6, 20 ],                                                 // imageAnchor
                        "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
                        [ 22, 20 ],                                                 // shadowSize
                        true,                                                       // draggable
                        -1);
                    vtPP.verticeIndex = i;
                    var self = this;
                    vtPP.verticeHandler = Microsoft.Maps.Events.addHandler(vtPP, "dragend", function (e) { return self._event_OnGeozoneVerticeDragEnd(e); });
                    this.bingGeozoneLayer.add(vtPP);
                }
            }
        }

        /* center on geozone */
        var centerPt = DEFAULT_CENTER; // JSMapPoint
        var zoom     = DEFAULT_ZOOM;
        if (count > 0) {
            centerPt = mapBounds.getCenter(); // JSMapPoint
            zoom     = this._calcBestZoom(mapBounds);
        }
        this._setCenter(new Microsoft.Maps.Location(centerPt.lat, centerPt.lon), zoom); // VELatLong

        /* add polygon-center pushpin */
        var gzPP = jsNewPushpin( // jsnewImageMarker
            centerPt.lat, centerPt.lon,                                 // lat,lon
            "http://labs.google.com/ridefinder/images/mm_20_blue.png",  // imageURL
            [ 12, 20 ],                                                 // imageSize
            [  6, 20 ],                                                 // imageAnchor
            "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
            [ 22, 20 ],                                                 // shadowSize
            true,                                                       // draggable
            -1);
        var self = this;
        gzPP.dragHandler = Microsoft.Maps.Events.addHandler(gzPP, "dragend", function (e) { return self._event_OnGeozoneCenterDragEnd(e); });
        this.bingGeozoneLayer.add(gzPP);
        this.geozoneCenter = new Microsoft.Maps.Location(centerPt.lat,centerPt.lon); // VELatLong;

        /* current MPP (meters-per-pixel) */
        var mpp;
        if ((MAP_WIDTH > 0) && (MAP_HEIGHT > 0)) {
            mpp  = mapBounds.calculateMetersPerPixel(MAP_WIDTH, MAP_HEIGHT);
        } else {
            mpp  = mapBounds.calculateMetersPerPixel(680, 470); // TODO: read these values from the map
        }
        radiusM = 20.0 * mpp;
        jsvZoneRadiusMeters = radiusM;

    } else
    if (type == ZONE_SWEPT_POINT_RADIUS) {

        /* adjust radius */
        if (isNaN(radiusM))              { radiusM = 1000; }
        if (radiusM > MAX_ZONE_RADIUS_M) { radiusM = MAX_ZONE_RADIUS_M; }
        if (radiusM < MIN_ZONE_RADIUS_M) { radiusM = MIN_ZONE_RADIUS_M; }

        /* draw vertices */
        var count = 0;
        var mapBounds = new JSBounds();
        var polyPts = []; // JSMapPoint
        for (var i = 0; i < points.length; i++) {
            var c = points[i]; // JSMapPoint
            if ((c.lat != 0.0) || (c.lon != 0.0)) {
                var isPrimary = (i == primNdx);
                var gc = new Microsoft.Maps.Location(c.lat,c.lon);
                this._addGeozoneCircleShape(gc, radiusM, this.shapeColor, this.lineWidth, isPrimary); // VELatLong
                if (jsvZoneEditable || jsvShowVertices) {
                    var vtPP = jsNewPushpin( // jsnewImageMarker
                        c.lat, c.lon,                                               // lat,lon
                        "http://labs.google.com/ridefinder/images/mm_20_red.png",   // imageURL
                        [ 12, 20 ],                                                 // imageSize
                        [  6, 20 ],                                                 // imageAnchor
                        "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
                        [ 22, 20 ],                                                 // shadowSize
                        true,                                                       // draggable
                        -1);
                    vtPP.verticeIndex = i;
                    var self = this;
                    vtPP.verticeHandler = Microsoft.Maps.Events.addHandler(vtPP, "dragend", function (e) { return self._event_OnGeozoneVerticeDragEnd(e); });
                    this.bingGeozoneLayer.add(vtPP);
                }
                polyPts.push(c); // JSMapPoint
                mapBounds.extend(c);
                mapBounds.extend(this._calcRadiusPoint(c, radiusM,   0.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM,  90.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM, 180.0));
                mapBounds.extend(this._calcRadiusPoint(c, radiusM, 270.0));
                count++;
            }
        }

        /* draw corridors */
        if (polyPts.length >= 2) {
            // routeline "_createRouteFeature"
            for (var i = 0; i < (polyPts.length - 1); i++) {
                var ptA = polyPts[i  ]; // JSMapPoint
                var ptB = polyPts[i+1]; // JSMapPoint
                var hAB = geoHeading(ptA.lat, ptA.lon, ptB.lat, ptB.lon) - 90.0; // perpendicular
                var rp1 = this._calcRadiusPoint(ptA, radiusM, hAB        ); // JSMapPoint
                var rp2 = this._calcRadiusPoint(ptB, radiusM, hAB        ); // JSMapPoint
                var rp3 = this._calcRadiusPoint(ptB, radiusM, hAB + 180.0); // JSMapPoint
                var rp4 = this._calcRadiusPoint(ptA, radiusM, hAB + 180.0); // JSMapPoint
                var rectPts = [ rp1, rp2, rp3, rp4 ];
                this._addPolygonShape(rectPts, this.shapeColor, this.lineWidth);
            }
        }

        /* center on geozone */
        var centerPt = DEFAULT_CENTER; // JSMapPoint
        var zoom     = DEFAULT_ZOOM;
        if (count > 0) {
            centerPt = mapBounds.getCenter(); // JSMapPoint
            zoom     = this._calcBestZoom(mapBounds);
        }
        this.geozoneCenter = new Microsoft.Maps.Location(centerPt.lat,centerPt.lon); // VELatLong
        this._setCenter(this.geozoneCenter, zoom); // VELatLong

        /* add corridor-center pushpin */
        /*
        //var gzPP = jsNewPushpin( // jsnewImageMarker
        //    centerPt.lat, centerPt.lon,                                 // lat,lon
        //    "http://labs.google.com/ridefinder/images/mm_20_blue.png",  // imageURL
        //    [ 12, 20 ],                                                 // imageSize
        //    [  6, 20 ],                                                 // imageAnchor
        //    "http://labs.google.com/ridefinder/images/mm_20_shadow.png",// shadowURL
        //    [ 22, 20 ],                                                 // shadowSize
        //    true,                                                       // draggable
        //    -1);
        //var self = this;
        //gzPP.dragHandler = Microsoft.Maps.Events.addHandler(gzPP, "dragend", function (e) { return self._event_OnGeozoneCenterDragEnd(e); });
        //this.bingGeozoneLayer.add(gzPP);
        */

    } else {

        alert("Geozone type not supported: " + type);

    }

    return null;
};

/**
*** Calculate the best zoom
*** @param bounds JSBounds
**/
JSMap.prototype._calcBestZoom = function(bounds)
{
    // NOTE: may not be applicable to the latest Bing maps version-8.
    // Derived from the zoom values specified at the following link:
    //  - http://blogs.msdn.com/virtualearth/archive/2006/02/25/map-control-zoom-levels-gt-resolution.aspx
    var mpp;
    if ((MAP_WIDTH > 0) && (MAP_HEIGHT > 0)) {
        mpp  = bounds.calculateMetersPerPixel(MAP_WIDTH, MAP_HEIGHT);
    } else {
        mpp  = bounds.calculateMetersPerPixel(680, 470); // TODO: read these values from the map
    }
    // Based on the ideal meters-per-pixel calculated by the JSBounds instance, the following converts
    // this value to the VirtualEarth Zoom#.
    var C = 0.2985821533203125000; // derived from MSVE zoom meters-per-pixel values
    // MPP  = C * 2^(19-ZOOM);   [where ZOOM is between 1 and 19, inclusive]
    // ZOOM = 19 - LOG2(MPP/C);  [where LOG2(X) == (LOGe(X)/LOGe(2))]
    var zoom = (19 - Math.round(Math.log(mpp / C) / Math.log(2.0))) - 1; // '-1' just to make sure everything fits
    if (zoom < 1) {
        return 1;
    } else
    if (zoom > 19) {
        return 19;
    } else {
        return zoom;
    }
};

/**
*** Returns an array of points (Location) representing a circle polygon
*** @param center   The center point (Location) of the circle
*** @param radiusM  The radius of the circle in meters
*** @return An array of points (Location) representing a circle polygon
**/
JSMap.prototype._getCirclePoints = function(center, radiusM)
{
    var rLat = geoRadians(center.latitude);   // radians
    var rLon = geoRadians(center.longitude);  // radians
    var d    = radiusM / EARTH_RADIUS_METERS;
    var circlePoints = new Array();
    for (x = 0; x <= 360; x += 6) {       // 6 degrees (saves memory, and it still looks like a circle)
        var xrad = geoRadians(x);         // radians
        var tLat = Math.asin(Math.sin(rLat) * Math.cos(d) + Math.cos(rLat) * Math.sin(d) * Math.cos(xrad));
        var tLon = rLon + Math.atan2(Math.sin(xrad) * Math.sin(d) * Math.cos(rLat), Math.cos(d) - Math.sin(rLat) * Math.sin(tLat));
        var LL   = new Microsoft.Maps.Location(geoDegrees(tLat), geoDegrees(tLon)); // VELatLong
        circlePoints.push(LL); // VELatLong
    }
    return circlePoints;
};

/**
*** Returns a geozone circle shape (VEShape)
*** @param center   The center point (Location) of the circle
*** @param radiusM  The radius of the circle in meters
*** @return The circle VEShape object
**/
JSMap.prototype._addGeozoneCircleShape = function(center, radiusM, color, width, isPrimary)
{
    //alert("Circle shape: " + center.latitude + "/" + center.longitude + ", radius="+radiusM);

    /* Circle points */
    var circlePoints = this._getCirclePoints(center, radiusM); // Location

    /* Circle shape */
    var circle = null;
    try {
        circle = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
    } catch (e) {
        alert("Error Creating Circle Polygon:\n" + e);
        return null;
    }
    circle.setLocations(circlePoints);

    /* save primary shape */
    var strokeColor;
    var fillColor;
    var rgb = (color && (color != ""))? rgbVal(color) : null;
    if (isPrimary) {
        if (rgb) {
            strokeColor = new Microsoft.Maps.Color(1.00,  220,    0,    0); // VEColor
            fillColor   = new Microsoft.Maps.Color(0.20,rgb.R,rgb.G,rgb.B); // VEColor
        } else {
            strokeColor = new Microsoft.Maps.Color(1.00, 200,    0,    0); // VEColor
            fillColor   = new Microsoft.Maps.Color(0.20,   0,  100,  150); // VEColor
        }
        this.geozoneShape  = circle;
        this.geozoneCenter = center; // VELatLong
    } else {
        if (rgb) {
            strokeColor = new Microsoft.Maps.Color(0.35,rgb.R,rgb.G,rgb.B); // VEColor
            fillColor   = new Microsoft.Maps.Color(0.20,rgb.R,rgb.G,rgb.B); // VEColor
        } else {
            strokeColor = new Microsoft.Maps.Color(0.35,    0,   90,    0); // VEColor: #005A00
            fillColor   = new Microsoft.Maps.Color(0.20,    0,  180,    0); // VEColor: #00B400
        }
    }

    /* set shape options */
    circle.setOptions({
            strokeThickness : width,
            strokeColor     : strokeColor,
            fillColor       : fillColor
        });

    /* add shape to geozone layer */
    this.bingGeozoneLayer.add(circle); // AddShape

    return circle;

};

JSMap.prototype._calcRadiusPoint = function(center/*JSMapPoint*/, radiusM, heading)
{
    var pt = geoRadiusPoint(center.lat, center.lon, radiusM, heading); // { lat: <>, lon: <> }
    return new JSMapPoint(pt.lat, pt.lon);
};

/**
*** Returns a circle shape (Polygon)
*** @param center   The center point (Location) of the circle
*** @param radiusM  The radius of the circle in meters
*** @return The circle Polygon object
**/
JSMap.prototype._addPolygonShape = function(points, color, width)
{

    /* polygon points */
    var polyPoints = [];
    for (var i = 0; i < points.length; i++) {
        polyPoints.push(new Microsoft.Maps.Location(points[i].lat, points[i].lon)); // VELatLong
    }

    /* shape */
    var shape = new Microsoft.Maps.Polygon([]); // VEShape(VEShapeType.Polygon)
    shape.setLocations(polyPoints);

    /* save primary shape */
    var strokeColor;
    var fillColor;
    var rgb = (color && (color != ""))? rgbVal(color) : null;
    if (rgb) {
        strokeColor = new Microsoft.Maps.Color(0.75,rgb.R,rgb.G,rgb.B); // VEColor
        fillColor   = new Microsoft.Maps.Color(0.20,rgb.R,rgb.G,rgb.B); // VEColor
    } else {
        strokeColor = new Microsoft.Maps.Color(0.75,    0,   90,    0); // VEColor: #005A00
        fillColor   = new Microsoft.Maps.Color(0.20,    0,  180,    0); // VEColor: #00B400
    }

    /* set shape options */
    shape.setOptions({
            strokeThickness : width,
            strokeColor     : strokeColor,
            fillColor       : fillColor
        });

    this.bingGeozoneLayer.add(shape);
    this.geozoneShape = shape;

};

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

/**
*** Mouse event handler to draw circles/lines on the map 
*** @param e  The mouse event
**/
JSMap.prototype._getGeozoneAtPoint = function(lat,lon)
{
    if ((this.geozoneCenter == null) || (this.geozoneShape == null)) {
        return null
    } else {
        var CC = this.geozoneCenter; // VELatLong
        var radiusM = zoneMapGetRadius(false);
        if (geoDistanceMeters(CC.latitude,CC.longitude,lat,lon) <= radiusM) {
            return this.geozoneShape;
        } else {
            return null;
        }
    }
};

/**
*** Mouse event handler to draw circles/lines on the map 
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMouseDown = function(e)
{
    
    /* last mousedown X/Y */
    this.lastX = e.getX();
    this.lastY = e.getY();
    
    /* quick exits */
    if (!e.leftMouseButton || e.altKey || (e.ctrlKey && e.shiftKey)) {
        this.dragType = DRAG_NONE;
        return false;
    }

    /* mouse down point */
    var x = e.getX(), y = e.getY();
    var LL = this.bingMap.tryPixelToLocation(new Microsoft.Maps.Point(x,y)); // PixelToLatLong(VEPixel)
    jsmapElem.style.cursor = 'crosshair';

    /* distance ruler */
    if (e.ctrlKey) {
        this.dragType = DRAG_RULER;
        this.dragRulerLatLon = LL; // VELatLong
        this.bingRulerLayer.clear(); // DeleteAllShapes
        jsmSetDistanceDisplay(0);
        return true; // e.preventDefault()
    }

    /* geozone mode */
    if (jsvGeozoneMode && jsvZoneEditable) {
        // Note: We cannot believe the value of 'e.elementID on Safari, so we do not use it here!
        var geozone = this._getGeozoneAtPoint(LL.latitude,LL.longitude); // Polygon shape
        if (geozone != null) {
            this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox()
            this.dragMarker = geozone; // Polygon shape
            if (e.shiftKey) {
                // resize
                this.dragType = DRAG_GEOZONE_RADIUS;
                this.bingRulerLayer.clear(); // DeleteAllShapes
            } else {
                // move 
                this.dragType = DRAG_GEOZONE_CENTER;
                var CC = this.geozoneCenter;
                this.dragZoneOffsetLat = LL.latitude  - CC.latitude;
                this.dragZoneOffsetLon = LL.longitude - CC.longitude;
              //this.bingMap.vemapcontrol.EnableGeoCommunity(true);
            }
            return true;
        }
    }
    
    this.dragType = DRAG_NONE;
    return false;
};

/**
*** Mouse event handler to draw circles on the map 
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMouseUp = function(e)
{

    /* geozone mode */
    if (jsvGeozoneMode && (this.dragMarker != null)) {
        var center = this.geozoneCenter;
        this.dragMarker = null; // Polygon shape
        jsmSetPointZoneValue(center.latitude, center.longitude, jsvZoneRadiusMeters);
      //this.bingMap.vemapcontrol.EnableGeoCommunity(false);
        this.dragType = DRAG_NONE;
        mapProviderParseZones(jsvZoneList);
        return true;
    }
        
    /* normal mode */
    this.dragType = DRAG_NONE;
    this.dragRulerLatLon = null; // VELatLong
    return false;

};

/**
*** Mouse event handler to detect lat/lon changes and draw circles/lines on the map 
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMouseMove = function(e)
{
    
    /* lat/lon location */
    var X = e.getX();
    var Y = e.getY();
    var LL = this.bingMap.tryPixelToLocation(new Microsoft.Maps.Point(X, Y)); // PixelToLatLong(VEPixel)
    if (LL == null) {
        // -- could not get a lat/lon from the point
        return false;
    }

    /* update Latitude/Longitude display */
    if (this.latLonDisplay != null) {
        jsmSetLatLonDisplay(LL.latitude, LL.longitude);
        jsmapElem.style.cursor = 'crosshair';
    }

    /* disance ruler */
    if (this.dragType == DRAG_RULER) {
        this.bingRulerLayer.clear(); // DeleteAllShapes
        var CC    = this.dragRulerLatLon; // VELatLong
        var distM = geoDistanceMeters(CC.latitude, CC.longitude, LL.latitude, LL.longitude);
        var line  = new Microsoft.Maps.Polyline([CC,LL]); // VEShape(VEShapeType.Polyline)
        line.setOptions({
                strokeThickness : 2,
                strokeColor     : new Microsoft.Maps.Color(1.00,255,100,34), // VEColor: #FF6422
                fillColor       : new Microsoft.Maps.Color(1.00,255, 34,34), // VEColor: #FF2222
            });
        this.bingRulerLayer.add(line); // AddShape
        jsmSetDistanceDisplay(distM);
        return true;
    }

    /* EditGeozone: dragging the zone radius? */
    if (this.dragType == DRAG_GEOZONE_RADIUS) {
        this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox(this.dragMarker);
        this.bingGeozoneLayer.clear(); // DeleteAllShapes
        var CC = this.geozoneCenter; // VELatLong
        jsvZoneRadiusMeters = Math.round(geoDistanceMeters(CC.latitude, CC.longitude, LL.latitude, LL.longitude));
        if (jsvZoneRadiusMeters > MAX_ZONE_RADIUS_M) { jsvZoneRadiusMeters = MAX_ZONE_RADIUS_M; }
        if (jsvZoneRadiusMeters < MIN_ZONE_RADIUS_M) { jsvZoneRadiusMeters = MIN_ZONE_RADIUS_M; }
        this._addGeozoneCircleShape(CC, jsvZoneRadiusMeters, jsvZoneColor, 2, true);
        jsmSetDistanceDisplay(jsvZoneRadiusMeters);
        return true;
    }

    /* EditGeozone: dragging the zone center? */
    if (this.dragType == DRAG_GEOZONE_CENTER) {
        this.pushpinInfobox.setOptions({ visible : false }); // this.bingMap.HideInfoBox(this.dragMarker);
        var circlePoints = this.dragMarker.getLocations(); // GetPoints
        this.geozoneCenter = new Microsoft.Maps.Location(LL.latitude - this.dragZoneOffsetLat, LL.longitude - this.dragZoneOffsetLon); // VELatLong
        this.dragMarker.setLocations(this._getCirclePoints(this.geozoneCenter, jsvZoneRadiusMeters));
        return true;
    }
    
    /* no-op */
    return false;

};

/**
*** Mouse event handler to recenter geozone
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMapClick = function(e)
{
    var LL = e.location;

    /* quick exits */
    //if (!e.isPrimary) { // || e.altKey || e.ctrlKey || e.shiftKey) 
    //    // ignore 'shifts'
    //    return false;
    //} else
    //if ((e.getX() != this.lastX) || (e.getY() != this.lastY)) {
    //    // some 'dragging' has occurred
    //    return false;
    //}

    /* Geozone edit mode? */
    if (jsvGeozoneMode && jsvZoneEditable) {
        if (jsvZoneType == ZONE_POINT_RADIUS) {
            //alert("Mouse Click PointRadius ... " + LL.latitude + "/" + LL.longitude);
            var radiusM = zoneMapGetRadius(false);
            var foundZoneNdx = -1;
            for (var x = 0; x < jsvZoneList.length; x++) {
                var pt = jsvZoneList[x]; // JSMapPoint
                if (geoIsValid(pt.lat,pt.lon)) {
                    if (geoDistanceMeters(pt.lat, pt.lon, LL.latitude, LL.longitude) <= radiusM) {
                        foundZoneNdx = x;
                        break;
                    }
                }
            }
            if (foundZoneNdx >= 0) { // inside an existing zone
                // skip
            } else {
                jsmSetPointZoneValue(LL.latitude, LL.longitude, radiusM);
                mapProviderParseZones(jsvZoneList);
            }
        } else
        if (jsvZoneType == ZONE_POLYGON) {
            var count = 0; // count number of valid points
            for (var x = 0; x < jsvZoneList.length; x++) {
                var pt = jsvZoneList[x];
                if (geoIsValid(pt.lat,pt.lon)) { count++; }
            }
            if (count == 0) {
                var radiusM = 450;  // no valid points - create default polygon
                var crLat   = geoRadians(LL.latitude);  // radians
                var crLon   = geoRadians(LL.longitude);  // radians
                var ptCnt   = (jsvZoneList.length <= 6)? jsvZoneList.length : 6;
                for (x = 0; x < ptCnt; x++) {
                    var deg   = x * (360.0 / ptCnt);
                    var radM  = radiusM / EARTH_RADIUS_METERS;
                    if ((deg == 0.0) || ((deg > 170.0) && (deg < 190.0))) { radM *= 0.85; }
                    var xrad  = geoRadians(deg); // radians
                    var rrLat = Math.asin(Math.sin(crLat) * Math.cos(radM) + Math.cos(crLat) * Math.sin(radM) * Math.cos(xrad));
                    var rrLon = crLon + Math.atan2(Math.sin(xrad) * Math.sin(radM) * Math.cos(crLat), Math.cos(radM)-Math.sin(crLat) * Math.sin(rrLat));
                    _jsmSetPointZoneValue(x, geoDegrees(rrLat), geoDegrees(rrLon), 0);
                }
            } else {
                // just move the selected location
                jsmSetPointZoneValue(LL.latitude, LL.longitude, 0);
            }
            // parse points
            mapProviderParseZones(jsvZoneList);
        } else
        if (jsvZoneType == ZONE_SWEPT_POINT_RADIUS) {
            var radiusM = zoneMapGetRadius(false);
            jsmSetPointZoneValue(LL.latitude, LL.longitude, radiusM);
            mapProviderParseZones(jsvZoneList);
        } else {
            //alert("Mouse Click UNKNOWN ... " + LL.latitude + "/" + LL.longitude);
        }
    }

    /* no-op */
    return false;

}; // _event_OnMapClick

/**
*** Mouse event handler to recenter geozone
*** @param e  The mouse event
**/
JSMap.prototype._event_OnPushpinClick = function(e)
{
    // e.target == Microsoft.Maps.Pushpin
    if (e.target.pp && this.pushpinInfobox) { // ShowInfoBox/_showPushpinPopup
        this._showPushpinPopup(e.target.pp);
    }
}; // _event_OnPushpinClick

/**
*** Mouse event handler to recenter geozone
*** @param e  The mouse event
**/
JSMap.prototype._event_OnGeozoneCenterDragEnd = function(e)
{
    //alert("Pushpin 'dragend' ... " + e.getX() + "/" + e.getY() + ", pri=" + e.isPrimary +", Loc="+e.location);

    /* quick exits */
    // -- 'isPrimary' appears to be "undefined"
    //if (!e.isPrimary) { 
    //    return false;
    //}

    /* geozone editing only */
    if (!jsvGeozoneMode || !jsvZoneEditable) {
        return false;
    }

    /* count number of valid points */
    var count = 0;
    for (var z = 0; z < jsvZoneList.length; z++) {
        if ((jsvZoneList[z].lat != 0.0) || (jsvZoneList[z].lon != 0.0)) {
            count++;
        }
    }

    /* recenter */
    var radiusM = zoneMapGetRadius(false);
    var LL = e.location;
    var CC = this.geozoneCenter? this.geozoneCenter : new Microsoft.Maps.Location(0,0); // VELatLong
    var CCIsValid = ((CC.latitude != 0.0) || (CC.longitude != 0.0));
    var CCLLDistKM = geoDistanceMeters(CC.latitude, CC.longitude, LL.latitude, LL.longitude);
    if (jsvZoneType == ZONE_POINT_RADIUS) {
        jsmSetPointZoneValue(LL.latitude, LL.longitude, jsvZoneRadiusMeters);
        mapProviderParseZones(jsvZoneList);
        return true;
    } else
    if (jsvZoneType == ZONE_POLYGON) {
        radiusM = jsvZoneRadiusMeters;
        if (count == 0) {
            // no valid points - create default polygon
            var radMeter = 450; // arbitrary default
            var crLat    = geoRadians(LL.latitude);   // radians
            var crLon    = geoRadians(LL.longitude);  // radians
            for (x = 0; x < jsvZoneList.length; x++) {
                var deg   = x * (360.0 / jsvZoneList.length);
                var radM  = radMeter / EARTH_RADIUS_METERS;
                if ((deg == 0.0) || ((deg > 170.0) && (deg<  190.0))) { radM *= 0.8; }
                var xrad  = geoRadians(deg); // radians
                var rrLat = Math.asin(Math.sin(crLat) * Math.cos(radM) + Math.cos(crLat) * Math.sin(radM) * Math.cos(xrad));
                var rrLon = crLon + Math.atan2(Math.sin(xrad) * Math.sin(radM) * Math.cos(crLat), Math.cos(radM)-Math.sin(crLat) * Math.sin(rrLat));
                _jsmSetPointZoneValue(x, geoDegrees(rrLat), geoDegrees(rrLon), 0);
            }
        } else {
            // move valid points to new GPS lat/lon
            var deltaLat = LL.latitude  - CC.latitude;
            var deltaLon = LL.longitude - CC.longitude;
            for (var x = 0; x < jsvZoneList.length; x++) {
                var pt = jsvZoneList[x];
                if ((pt.lat != 0.0) || (pt.lon != 0.0)) {
                    _jsmSetPointZoneValue(x, (pt.lat + deltaLat), (pt.lon + deltaLon), 0);
                }
            }
        }
        // reparse zone
        mapProviderParseZones(jsvZoneList);
        return true;
    } else
    if (jsvZoneType == ZONE_SWEPT_POINT_RADIUS) {
        radiusM = jsvZoneRadiusMeters;
        if (count == 0) {
            // no valid points - create default geocorridor?
        } else {
            // move valid points to new GPS lat/lon
            var deltaLat = LL.latitude  - CC.latitude;
            var deltaLon = LL.longitude - CC.longitude;
            for (var x = 0; x < jsvZoneList.length; x++) {
                var pt = jsvZoneList[x];
                if ((pt.lat != 0.0) || (pt.lon != 0.0)) {
                    _jsmSetPointZoneValue(x, (pt.lat + deltaLat), (pt.lon + deltaLon), 0);
                }
            }
        }
        // reparse zone
        mapProviderParseZones(jsvZoneList);
        return true;
    }

    /* no-op */
    return false;

}; // _event_OnGeozoneCenterDragEnd


/**
*** Mouse event handler to recenter geozone
*** @param e  The mouse event
**/
JSMap.prototype._event_OnGeozoneVerticeDragEnd = function(e)
{

    /* geozone editing only */
    if (!jsvGeozoneMode || !jsvZoneEditable) {
        return false;
    }
    
    /* Pushpin vertice */
    var ppv = e.target;
    if (typeof ppv.verticeIndex === "undefined") {
        alert("Unable to determine the polygon vertice: " + ppv.verticeIndex);
        return false;
    }

    /* move vertice */
    var LL = e.location;
    if (jsvZoneType == ZONE_POINT_RADIUS) {
        // -- resize circle radius
        var CC = this.geozoneCenter; // VELatLong
        var distM = geoDistanceMeters(CC.latitude,CC.longitude,LL.latitude,LL.longitude);
        _jsmSetPointZoneValue(ppv.verticeIndex, CC.latitude, CC.longitude, distM);
        _zoneReset();
    } else
    if (jsvZoneType == ZONE_POLYGON) {
        // -- move a polygon vertice
        _jsmSetPointZoneValue(ppv.verticeIndex, LL.latitude, LL.longitude, 0);
    } else
    if (jsvZoneType == ZONE_SWEPT_POINT_RADIUS) {
        // -- move a corridor vertice
        _jsmSetPointZoneValue(ppv.verticeIndex, LL.latitude, LL.longitude, 0);
    }
    // -- reparse zone
    mapProviderParseZones(jsvZoneList);
    return true;

}; // _event_OnGeozoneVerticeDragEnd


// ----------------------------------------------------------------------------

/**
*** MouseOver event handler
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMouseOver = function(e)
{

    /* no-op */
    return false;

};

/**
*** MouseOut event handler
*** @param e  The mouse event
**/
JSMap.prototype._event_OnMouseOut = function(e)
{

    /* no-op */
    return false;

};

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
