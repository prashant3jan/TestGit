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
// Some [reverse]geocoder options/references:
//   http://www.johnsample.com/articles/GeocodeWithSqlServer.aspx   (incl reverse)
//   http://www.extendthereach.com/products/OSGeocoder.srct
//   http://datamining.anu.edu.au/student/honours-proj2005-geocoding.html
//   http://geocoder.us/
//   http://www.nacgeo.com/reversegeocode.asp
//   http://wsfinder.jot.com/WikiHome/Maps+and+Geography
/// ----------------------------------------------------------------------------
// Change History:
//  2007/01/25  Martin D. Flynn
//     -Initial release
//  2010/07/04  Martin D. Flynn
//     -Added "isEnabled" method
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import java.util.Properties;

import org.opengts.util.*;

public interface ReverseGeocodeProvider
{

    // ------------------------------------------------------------------------

    /**
    *** Returns the name of this ReverseGeocodeProvider 
    *** @return The name of this ReverseGeocodeProvider
    **/
    public String getName();

    /**
    *** Returns true if this ReverseGeocodeProvider is enabled
    *** @return True if this ReverseGeocodeProvider is enabled, false otherwise
    **/
    public boolean isEnabled();

    /**
    *** Returns true if this operation will take less than about 20ms to complete
    *** the returned value is used to determine whether the 'getReverseGeocode' operation
    *** should be performed immediately, or lazily (ie. in a separate thread).
    *** @return True if this is a fast (ie. local) operation
    **/
    public boolean isFastOperation();

    // ------------------------------------------------------------------------

    /**
    *** Returns the minimum allowed number of elapsed seconds since the previous
    *** reverse-geocode.
    **/
    public long getMinimumElapsedSec();

    // ------------------------------------------------------------------------

    /**
    *** Returns the previously cached reverse-geocode, null if no previous reverse-geocode.
    *** @return The previously cached reverse-geocode.
    **/
    public ReverseGeocode getCachedReverseGeocode(GeoPoint gp);

    /**
    *** Returns the best address for the specified GeoPoint 
    *** @param gp        The GeoPoint to reverse-geocode
    *** @param isMoving  Must be true if the vehicle is moving, false if stationary.
    ***                  Used to determine if using the 'economy' reverse-geocoder is preferred.
    *** @param localeStr The locale identifier.  Used for localizing the returned address.
    *** @param cache     True to allow caching this address, false to not cache address
    *** @param clientID  The name of the cient (AccountID/GTSID) that requested the reverse-geocode
    *** @param props     The properties to use for the reverse-geocode configuration.
    *** @return The reverse-geocoded address
    **/
    public ReverseGeocode getReverseGeocode(
        GeoPoint gp, boolean isMoving, String localeStr,
        boolean cache, String clientID,
        Properties props);

    // ------------------------------------------------------------------------

    /**
    *** Sets the economy ReverseGeocodeProvider
    *** @param rgp  The economy ReverseGeocodeProvider
    **/
    public void setEconomyReverseGeocodeProvider(ReverseGeocodeProvider rgp);

    /**
    *** Gets the economy ReverseGeocodeProvider
    *** @return The economy ReverseGeocodeProvider
    **/
    public ReverseGeocodeProvider getEconomyReverseGeocodeProvider();

    /**
    *** Has a economy ReverseGeocodeProvider
    *** @return True if this instance has an economy ReverseGeocodeProvider
    **/
    public boolean hasEconomyReverseGeocodeProvider();

    // ------------------------------------------------------------------------

    /**
    *** Sets the failover ReverseGeocodeProvider
    *** @param rgp  The failover ReverseGeocodeProvider
    **/
    public void setFailoverReverseGeocodeProvider(ReverseGeocodeProvider rgp);

    /**
    *** Gets the failover ReverseGeocodeProvider
    *** @return The failover ReverseGeocodeProvider
    **/
    public ReverseGeocodeProvider getFailoverReverseGeocodeProvider();

    /**
    *** Has a failover ReverseGeocodeProvider
    *** @return True if this instance has a failover ReverseGeocodeProvider
    **/
    public boolean hasFailoverReverseGeocodeProvider();

    // ------------------------------------------------------------------------

}
