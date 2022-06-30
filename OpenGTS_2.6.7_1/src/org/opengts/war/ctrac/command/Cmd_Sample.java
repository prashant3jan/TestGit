// ----------------------------------------------------------------------------
// Copyright 2007-2018, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
// Sample command definition:
// Notes:
//  -- CelltracGTS commands may be added to any of the following packages:
//  -   org.opengts.war.ctrac.command  (source directory already provided)
//  -   org.opengts.war.ctrac.webcmd
//  -   org.opengts.war.ctrac.custom
//  -   org.opengts.war.track.command  (source directory already provided)
//  -   org.opengts.war.track.webcmd
//  -   org.opengts.war.track.custom
//  -- The command class name (eg "Cmd_Sample") must be added as a separate 
//  -   uncommented line in a file called ".command" within the chosen package 
//  -   directory. (commented commands will not be loaded)
// ----------------------------------------------------------------------------
//  2018/08/13  GTS Development Team
//     -Initial release. 
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac.command;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.servlet.http.HttpServletRequest;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.dbtypes.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.ctrac.*;

public class Cmd_Sample
    extends CommandInterfaceAdapter
    implements CommandInterface // redundant, but specified here for clarity
{

    // ------------------------------------------------------------------------

    public  static final String  TAG_Device     = "Device";

    // ------------------------------------------------------------------------

    /* get defined commands */
    // -- Each command array below represents a single command with their corresponding 
    // -  command name aliases.
    // -- As a group, these commands typically represent the handling of a common set
    // -  of functionality, such as Geozones (add, delete, query, etc).
    public  static final String  COMMAND_SampleCmd_1[] = { "Sample1"                 };
    public  static final String  COMMAND_SampleCmd_2[] = { "Sample2", "sampleAlias2" };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Constructor
    **/
    public Cmd_Sample()
    {
        super();
        // -- must specify the command-names that this module will handle
        this.setCommandNames(
            COMMAND_SampleCmd_1,
            COMMAND_SampleCmd_2
            );
    }

    /**
    *** Returns true if account/user authorization is required, 
    *** false if authorization is not required.
    *** @param cmd  The name of the specific command
    **/
    public boolean isAuthorizationRequired(String cmd)
    {
        return true;
    }

    /**
    *** Command handling
    *** @param request  The current HttpServletRequest instance.  Required for some commands
    ***     that need to read information from the current request input stream.   If null,
    ***     then thos commands that require a non-null request value will be ignored.
    *** @param respType Must be "json" to return he response in JSON format, otherwise the 
    ***     response will be returned in a parsable text format.
    *** @param servCtx  The current ServiceContextInterface
    *** @param hc       The HandleCommand instance
    *** @param cmd      The command to be executed
    *** @param cmdArgs  The command arguments/parameters
    **/
    public Response execCommand(
        ServiceContextInterface servCtx,
        String cmd, String cmdArgs[])
    {

        /* ServiceContext */
        if (servCtx == null) {
            // -- unlikely, but check anyway
            long respFmt = Response.getDefaultResponseFormat();
            return Response.ERR_NOT_AUTH(respFmt,"Invalid ServiceContext").setCode(503);
        }
        long respFmt = servCtx.getResponseFormat();

        /* AuthorizedUser */
        AuthorizedUser authUser = servCtx.getAuthorizedUser();
        if (authUser == null) {
            // -- will be non-null if "isAuthorizationRequired" returns true
            return Response.ERR_NOT_AUTH(respFmt,"Not Authorized").setCode(401);
        }

        // ----------------------------------------------------
        // -- Sample Command #1
        if (this.commandMatch(COMMAND_SampleCmd_1,cmd)) { // checks for 'cmd' in the list of command alias names
            // --"Arg(args,n): 
            // -    This convenience function returns the n'th element in 'args'.
            // -    Returns null if not available.
            // -- "ParsID(id,dft): 
            // -    This convenience function returns 'id' if valie, 'dft' if invalid.  
            String devID  = ParseID(Arg(cmdArgs,0),servCtx.getDeviceID());  // Arg0: DeviceID
            String someID = ParseID(Arg(cmdArgs,1),"");                     // Arg1: SomeID
            // -- User authorized to device?
            if (!authUser.isAuthorizedDevice(devID)) {
                // -- check to see if the user is authorized to view the specified device.
                Print.logError("Not authorized for device: " + servCtx.getAccountID() + "/" + devID);
                return Response.ERR_NOT_AUTH(respFmt, "Not authorized for device: " + devID).setCode(401);
            }
            // -- Load device
            // -    example showing how to load a Device instance
            Device device = null;
            try {
              //device = Transport.loadDeviceByTransportID(servCtx.getAccount(), devID);
                device = Device.loadDeviceByName(servCtx.getAccount(), devID, true);
                if (device == null) {
                    Print.logError("Device not found AccountID/DeviceID: " + servCtx.getAccountID() + "/" + devID);
                    return Response.ERR_DEVICE(respFmt, "Device not found: " + devID);
                }
            } catch (DBException dbe) {
                Print.logException("Error Loading Device: " + servCtx.getAccountID() + "/" + devID, dbe);
                return Response.ERR_DEVICE(respFmt, "Device internal error: " + devID);
            }
            // -- return response
            if (Response.IsJSON(respFmt)) {
                // -- Response returned as JSON
                // -    This example returns the Device instance as a JsonBean model
                JSON._Object jsonDev = new JSON._Object(device,null/*tag*/);
                return Response.SUCCESS(respFmt,"").addJsonValue(TAG_Device,jsonDev);
            } else {
                // -- Response "JSON only"
                return Response.SUCCESS(respFmt,"Sample1: JSON only");
            }
        }

        // ----------------------------------------------------
        // -- Sample Command #2
        if (this.commandMatch(COMMAND_SampleCmd_2,cmd)) {
            // -- return response
            if (Response.IsJSON(respFmt)) {
                // -- Response returned as JSON
                return Response.SUCCESS(respFmt,"Sample2: success");
            } else {
                // -- Response "JSON only"
                return Response.SUCCESS(respFmt,"Sample2: JSON only");
            }
        }

        // ----------------------------------------------------
        // -- Invalid command
        Print.logError("Invalid/unrecognized Command specified: " + cmd);
        return Response.ERR_COMMAND(respFmt, "Command Invalid");

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
