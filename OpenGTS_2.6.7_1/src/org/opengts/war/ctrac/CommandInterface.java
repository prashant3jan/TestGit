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
//  2018/09/10  GTS Development Team
//     -Initial release. 
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac;
//package org.opengts.opt.war.ctrac;

import java.lang.*;
import java.util.*;
import java.io.*;

import javax.servlet.http.HttpServletRequest;

import org.opengts.util.*;
import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;

import org.opengts.war.ctrac.*;

public interface CommandInterface
{

    // ------------------------------------------------------------------------

    /**
    *** Sets the global initialization properties
    **/
    public void setServiceFactory(ServiceFactoryInterface sfi);

    /**
    *** Returns true if account/user authorization is required, false otherwise.
    **/
    public boolean isAuthorizationRequired(String cmd);

    /**
    *** Gets a list of command names that this CommandInterface handles
    **/
    public String[][] getCommandNames();

    /**
    *** Command handling
    *** @param servCtx  The current ServiceContext
    *** @param cmd      The command to be executed
    *** @param cmdArgs  The command arguments/parameters
    **/
    public Response execCommand(
        ServiceContextInterface servCtx, 
        String cmd, String cmdArgs[]);

    /**
    *** Prints the configuration header information 
    **/
    public void printHeaderLog(int P, int W);

}
