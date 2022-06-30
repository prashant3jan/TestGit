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
//  2017/04/08  Martin D. Flynn
//     -Initial release
//  2018/09/10  GTS Development Team
//     -Updated
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;

import org.opengts.db.*;
import org.opengts.db.tables.*;

public class CTCustomCommand
    implements CustomCommandInterface
{

    // ------------------------------------------------------------------------

    private static final long    MAX_ACTIVE_AGE_SEC         = DateTime.HourSeconds(6);

    // ------------------------------------------------------------------------

    /**
    *** Default Constructor
    **/
    public CTCustomCommand()
    {
        // -- init
        Print.logInfo("Instantiated CTCustomCommand ...");
    }

    // ------------------------------------------------------------------------

    /**
    *** Callback to handle custom web-service commands.  
    *** @param servCtx  The current ServiceContext
    *** @param cmdID    The command id
    *** @param cmdArg   The command argument string
    *** @return The response which will be sent back to the requestor
    **/
    public Response execCommand(
        ServiceContextInterface servCtx,
        String cmdID, String cmdArgs[])
    {
        Account           account    = servCtx.getAccount();           // may be null (but unlikely)
        User              user       = servCtx.getUser();              // may be null
        long              respFormat = servCtx.getResponseFormat();    // 
        BasicPrivateLabel privLabel  = servCtx.getPrivateLabel();      // may be null (but unlikely)
        Object            reqPropObj = servCtx.getRequestProperties(); // may be null
      //RequestProperties reqProp    = (reqPropObj instanceof RequestProperties)? (RequestProperties)reqPropObj : null;
        Print.logDebug("Command:" + cmdID + " Args:" + StringTools.join(cmdArgs,",")); 
        long format = Response.ParseFormat("jse");
        return Response.OK_JSON(format, "");
    }

}
