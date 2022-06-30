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
//  2018/09/10  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import org.opengts.util.*;
import org.opengts.geocoder.country.*;

public class Geocode
    implements GeoPointProvider
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private GeoPoint    geoPoint        = GeoPoint.INVALID_GEOPOINT;
    private String      fullAddress     = null;

    /**
    *** Default constructor
    **/
    public Geocode()
    {
        super();
    }

    /**
    *** Constructor
    **/
    public Geocode(GeoPoint gp)
    {
        this();
        this.setGeoPoint(gp);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a GeoPoint instance. <br>
    *** (GeoPointProvider interface)
    *** @return A GeoPoint instance (non-null)
    **/
    public GeoPoint getGeoPoint()
    {
        return (this.geoPoint != null)? this.geoPoint : GeoPoint.INVALID_GEOPOINT;
    }

    /**
    *** Sets the GeoPoint instance
    **/
    public void setGeoPoint(GeoPoint gp)
    {
        this.geoPoint = GeoPoint.isValid(gp)? gp : GeoPoint.INVALID_GEOPOINT;
    }

    // ------------------------------------------------------------------------
    // Full address

    /**
    *** Sets the full address
    **/
    public void setFullAddress(String address)
    {
        this.fullAddress = (address != null)? address.trim() : null;
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

}
