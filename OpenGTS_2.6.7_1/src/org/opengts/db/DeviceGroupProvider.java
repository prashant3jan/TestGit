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
//  2018/08/16  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;

import org.opengts.dbtools.DBException;
import org.opengts.db.tables.DeviceGroup;

/**
*** Represents a DeviceGroup provider
**/

public interface DeviceGroupProvider
    extends JSON.JSONBean
{

    /**
    *** Returns a DeviceGroup instance
    *** @return A DeviceGroup instance
    **/
    public DeviceGroup getDeviceGroup();

    /**
    *** Gets the AccountID
    **/
    public String getAccountID();

    /**
    *** Gets the DeviceGroupID
    **/
    public String getGroupID();

    /**
    *** Gets the description
    **/
    public String getDescription();

}
