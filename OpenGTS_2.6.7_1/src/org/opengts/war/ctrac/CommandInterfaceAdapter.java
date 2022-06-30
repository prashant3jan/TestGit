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

import org.opengts.war.ctrac.*; // ServiceFactoryInterface, ...

public abstract class CommandInterfaceAdapter
    implements CommandInterface
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static String StaticMethod_CanHandleCLI  = "CanHandleCLI"; // CanHandleCLI()
    public static String StaticMethod_HandleCLI     = "HandleCLI";    // HandleCLI(URIArg, String)

    /**
    *** Returns true if specified class name supports a command-line-interface
    **/
    public static boolean SupportsCLI(String cliClassName)
    {
        return SupportsCLI(StringTools.classForName(cliClassName));
    }

    /**
    *** Returns true if specified class supports a command-line-interface
    **/
    public static boolean SupportsCLI(Class<?> cliClass)
    {
        if (!MethodAction.hasPublicStaticMethod(cliClass,StaticMethod_CanHandleCLI/*,(Class<?>[])null*/)) {
            return false;
        } else
        if (!MethodAction.hasPublicStaticMethod(cliClass,StaticMethod_HandleCLI,URIArg.class,String.class)) {
            return false;
        } else {
            return true;
        }
    }

    // --------------------------------

    /**
    *** Returns true if the specified class static method "CanHandleCLI" returns true
    **/
    public static boolean CanHandleCLI(String cliClassName)
    {
        return CanHandleCLI(StringTools.classForName(cliClassName));
    }

    /**
    *** Returns true if the specified class static method "CanHandleCLI" returns true
    **/
    public static boolean CanHandleCLI(Class<?> cliClass)
    {
        if (cliClass != null) {
            try {
                MethodAction canM = new MethodAction(cliClass,StaticMethod_CanHandleCLI);
                return StringTools.parseBoolean(canM.invoke(),false);
            } catch (Throwable th) {
                return false;
            }
        } else {
            return false;
        }
    }

    // --------------------------------

    /**
    *** Returns the result of the specified class static method "HandleCLI"
    **/
    public static int HandleCLI(String cliClassName, URIArg authURL, String sessID)
        throws Throwable // NoSuchMethodException, ClassNotFoundException, Throwable
    {
        return HandleCLI(StringTools.classForName(cliClassName), authURL, sessID);
    }

    /**
    *** Returns the result of the specified class static method "HandleCLI"
    **/
    public static int HandleCLI(Class<?> cliClass, URIArg authURL, String sessID)
        throws Throwable // NoSuchMethodException, ClassNotFoundException, Throwable
    {
        MethodAction hanM = new MethodAction(cliClass,StaticMethod_HandleCLI,URIArg.class,String.class);
        return StringTools.parseInt(hanM.invoke(authURL,sessID),0);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Parses/returns the AccountID/DeviceID/etc.
    **/
    public static String ParseID(String ID, String dft)
    {
        ID = StringTools.trim(ID);
        if (StringTools.isBlank(ID)) {
            return ID;
        } else 
        if (ID.equals("THIS")) {
            return dft;
        } else {
            return ID;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* command argument separator */
    public static final char ARG_SEPARATOR_CHAR = CustomCommand.ARG_SEPARATOR_CHAR; // '|';

    public static int ArgSize(String args[])
    {
        return ListTools.size(args);
    }

    /* return indexed arg from argument list */
    public static String Arg(String args[], int ndx, String dft)
    {
        if (args == null) {
            return dft;
        } else
        if ((ndx < 0) || (ndx >= args.length)) {
            return dft;
        } else {
            return StringTools.trim(args[ndx]);
        }
    }

    /* return indexed arg from argument list */
    public static String Arg(String args[], int ndx)
    {
        return Arg(args,ndx,null);
    }

    /* parse/return GeoPoint from argument list */
    public static GeoPoint ArgGeoPoint(String args[], int ndx)
    {
        String gp = Arg(args, ndx);
        int p = !StringTools.isBlank(gp)? gp.indexOf(',') : -1;
        if (p >= 0) {
            double lat = GeoPoint.parseLatitude( gp.substring(0,p),-999.0);
            double lon = GeoPoint.parseLongitude(gp.substring(p+1),-999.0);
            if (GeoPoint.isValid(lat,lon)) {
                return new GeoPoint(lat,lon);
            }
        }
        return GeoPoint.INVALID_GEOPOINT;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private ServiceFactoryInterface     servFactory         = null;
    private String                      cmdGroupAliases[][] = null;

    /**
    *** Constructor
    **/
    public CommandInterfaceAdapter()
    {
        super();
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the global initialization properties
    **/
    public void setServiceFactory(ServiceFactoryInterface sfi)
    {
        this.servFactory = sfi;
    }

    /**
    *** Gets the global initialization properties
    **/
    public ServiceFactoryInterface getServiceFactory()
    {
        return this.servFactory;
    }

    /**
    *** Gets the global initialization properties
    **/
    //public boolean hasServiceFactory()
    //{
    //    return (this.getServiceFactory() != null)? true : false;
    //}

    // ------------------------------------------------------------------------

    /**
    *** Returns true if account/user authorization is required, false otherwise.
    *** This implementation defaults to "true".
    **/
    public boolean isAuthorizationRequired(String cmd)
    {
        // -- default to "true"
        return true;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a list of command names that this CommandInterface handles
    **/
    public String[][] getCommandNames()
    {
        return this.cmdGroupAliases;
    }

    /**
    *** Gets a list of command names that this CommandInterface handles
    **/
    public void setCommandNames(String[]... cmdGrpAliases)
    {
        this.cmdGroupAliases = cmdGrpAliases;
    }

    /**
    *** Gets the class names of this instance, sans the package name
    **/
    //public String getClassKeyName()
    //{
    //    String cn = StringTools.className(this);
    //    int p = cn.lastIndexOf(".");
    //    return (p >= 0)? cn.substring(p+1) : cn;
    //}

    /**
    *** Returns true if the specific command is in the list of command names
    *** and the command is enabled.
    **/
    public boolean commandMatch(String keys[], String cmd)
    {

        /* command not in list? */
        if (ListTools.isEmpty(keys)) {
            return false;
        } else
        if (StringTools.isBlank(cmd)) {
            return false;
        } else 
        if (!ListTools.containsIgnoreCase(keys,cmd)) {
            return false;
        }

        /* match */
        return true;

    }

    // ------------------------------------------------------------------------

    /**
    *** Command handling
    *** @param request  The current HttpServletRequest instance.  Required for some commands
    ***     that need to read information from the current request input stream.   If null,
    ***     then thos commands that require a non-null request value will be ignored.
    *** @param respType Must be "json" to return he response in JSON format, otherwise the 
    ***     response will be returned in a parsable text format.
    *** @param servCtx  The current ServiceContextInterface
    *** @param cmd      The command to be executed
    *** @param cmdArgs  The command arguments/parameters
    **/
    public abstract Response execCommand(
        ServiceContextInterface servCtx, 
        String cmd, String cmdArgs[]);

    // ------------------------------------------------------------------------

    public static String LogHdr(int P,String H,int W){return StringTools.replicateString(" ",P)+StringTools.padRight(H,' ',W-P)+": ";}

    /**
    *** Prints the configuration header information 
    **/
    public void printHeaderLog(int P, int W)
    {
        // -- no-op
    }

    // ------------------------------------------------------------------------

}
