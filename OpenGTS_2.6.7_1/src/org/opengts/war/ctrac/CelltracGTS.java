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
//     -Initial release.
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.db.DCServerConfig;

public class CelltracGTS
{

    // ------------------------------------------------------------------------
    // Command properties

    public  static final String  CFG_CelltracGTS_               = "CelltracGTS.";

    // -- ResponseFormat
    public  static final String  CFG_defaultResponseFormat      = "defaultResponseFormat";

    // -- web-service properties
    public  static final String  CFG_enableTracking_POST        = "enableTracking.POST";
    public  static final String  CFG_enableTracking_GET         = "enableTracking.GET";
    public  static final String  CFG_enableCommands_POST        = "enableCommands.POST";
    public  static final String  CFG_enableCommands_GET         = "enableCommands.GET";
    public  static final String  CFG_enableAccountLogin         = "enableAccountLogin";

    // -- App properties
    public  static final String  CFG_app_latestVersions         = "app.latestVersions";
    public  static final String  CFG_app_customConfigJSON       = "app.customConfigJSON";

    // -- GTC client properties
    public  static final String  CFG_gtc_latestVersions         = "gtc.latestVersions";
    public  static final String  CFG_gtc_minimumTransmitInterval= "gtc.minimumTransmitInterval";
    public  static final String  CFG_gtc_movingInterval         = "gtc.movingInterval";
    public  static final String  CFG_gtc_dormantInterval        = "gtc.dormantInterval";
    public  static final String  CFG_gtc_impromptuStatusCodes   = "gtc.impromptuStatusCodes";

    // -- Park/Unpark commands
    public  static final String  CFG_parkRadius_default         = "parkRadius.default";
    public  static final String  CFG_parkRadius_wide            = "parkRadius.wide";
    public  static final String  CFG_parkRadius_minimum         = "parkRadius.minimum";
    public  static final String  CFG_parkSpeed_default          = "parkSpeed.default";

    // -- CustomCommand handler
    public  static final String  CFG_customCommandHandler       = "customCommandHandler";
    public  static final String  CFG_customCommandPackage       = "customCommandPackage";

    // -- disabled commands
    public  static final String  CFG_commandEnabled_            = "commandEnabled.";

    // ------------------------------------------------------------------------
    // -- ServiceContext properties

    public  static final String  CTX_appNameVersion             = "appNameVersion";
    public  static final String  CTX_gtcNameVersion             = "gtcNameVersion";
    public  static final String  CTX_gtcID                      = "gtcID";
    public  static final String  CTX_ipAddress                  = "ipAddress";
    public  static final String  CTX_authorizationPIN           = "authorizationPIN";
    public  static final String  CTX_deviceID                   = "deviceID";
    public  static final String  CTX_mobileID                   = "mobileID";
    // --
    public  static final String  CTX_PrivateLabel               = "PrivateLabel";       // optional
    public  static final String  CTX_RequestProperties          = "RequestProperties";  // optional

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static String ServiceFactory_classN = "org.opengts.opt.war.ctrac.ServiceFactory";

    /**
    *** Creates a new instance of ServiceFactory.
    *** Will return null if unable to create a new instance of ServiceFactory
    **/
    private static ServiceFactoryInterface newServiceFactory()
    {
        try {
            Class<?> ServiceFactory_classN_class = Class.forName(ServiceFactory_classN);
            return (ServiceFactoryInterface)ServiceFactory_classN_class.newInstance(); // new ServiceFactory()
        } catch (ClassNotFoundException cnfe) { // ClassCastException, etc
            Print.logError("CelltracGTS ServiceFactory not available in this installation");
            return null;
        } catch (Throwable th) { // ClassCastException, MethodInvocationException, etc
            Print.logException("Unable to create CelltracGTS ServiceFactory", th);
            return null;
        }
    }

    // --------------------------------

    /**
    *** Creates a new ServiceFactory
    **/
    public static ServiceFactoryInterface newServiceFactory(DCServerConfig dcs)
    {
        ServiceFactoryInterface servFact = CelltracGTS.newServiceFactory();
        if (servFact != null) {
            // -- set property provider and return
            servFact.setDCServerConfig(dcs);
            return servFact;
        } else {
            // -- unable to initialize
            return null;
        }
    }

    /**
    *** Creates a new ServiceFactory
    **/
    public static ServiceFactoryInterface newServiceFactory(RTProperties rtp)
    {
        ServiceFactoryInterface servFact = CelltracGTS.newServiceFactory();
        if (servFact != null) {
            // -- set property provider and return
            servFact.setRTProperties(rtp);
            return servFact;
        } else {
            // -- unable to initialize
            return null;
        }
    }

    /**
    *** Creates a new ServiceFactory
    **/
    public static ServiceFactoryInterface newServiceFactory(Properties props)
    {
        ServiceFactoryInterface servFact = CelltracGTS.newServiceFactory();
        if (servFact != null) {
            // -- set property provider and return
            servFact.setRTProperties(new RTProperties(props));
            return servFact;
        } else {
            // -- unable to initialize
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
