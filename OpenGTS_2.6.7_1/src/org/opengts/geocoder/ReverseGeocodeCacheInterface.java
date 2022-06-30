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
/// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import java.util.Properties;

import org.opengts.util.*;

public interface ReverseGeocodeCacheInterface
{

    // ------------------------------------------------------------------------

    /**
    *** Returns the previously cached reverse-geocode, null if no previous reverse-geocode.
    *** @return The previously cached reverse-geocode.
    **/
    public ReverseGeocode getReverseGeocode(int provider, GeoPoint gp, boolean isMoving);

    /**
    *** Adds the specified ReverseGeocode to the cache for the specified GeoPoint.
    **/
    public boolean addReverseGeocode(int provider, GeoPoint gp, ReverseGeocode rg, String clientID);

    // ------------------------------------------------------------------------

}
