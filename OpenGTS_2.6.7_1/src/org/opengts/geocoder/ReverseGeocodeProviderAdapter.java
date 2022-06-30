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
//  2008/05/14  Martin D. Flynn
//     -Initial release
//  2009/05/24  Martin D. Flynn
//     -Added command-line Reverse-Geocoder test.
//  2012/05/27  Martin D. Flynn
//     -Updated failover support
//  2013/08/06  Martin D. Flynn
//     -Added ability for subclass to specify a failover timeout value.
//  2016/01/04  Martin D. Flynn
//     -Added "failoverQuiet" hint [2.6.1-B03]
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import java.util.*;

import org.opengts.util.*;

import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.Device;
import org.opengts.db.tables.EventData;

public abstract class ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider
{

    // ------------------------------------------------------------------------

    public static final String PROP_ReverseGeocodeProvider_ = "ReverseGeocodeProvider.";
    public static final String _PROP_isEnabled              = ".isEnabled";

    public static final String PROP_minElapsedSeconds[]     = { "minElapsedSeconds" }; // Long: 0
    public static final String PROP_alwaysFast[]            = { "alwaysFast", "forceAlwaysFast" }; // Boolean: false
    public static final String PROP_maxFailoverSeconds[]    = { "maxFailoverSeconds" }; // Long: 
    public static final String PROP_failoverQuiet[]         = { "failoverQuiet" }; // Boolean: 

    public static final String PROP_overLimitRetry[]        = { "overLimitRetry" };

    // ------------------------------------------------------------------------

    public static       long   DEFAULT_MIN_ELAPSED_SECONDS  = 0L;
    public static       long   DEFAULT_MAX_FAILOVER_SECONDS = DateTime.HourSeconds(1);
    public static       long   MIN_FAILOVER_SECONDS         = DateTime.MinuteSeconds(10);

    public static final int    MAX_RETRY_COUNT              = 4;
    public static final long   MAX_RETRY_SLEEP_MS           = 5000L;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String                  name                    = null;
    private TriState                isEnabled               = TriState.UNKNOWN;

    private String                  accessKey               = null;
    private RTProperties            properties              = null;

    private int                     failoverQuiet           = -1;  // quiet failover hint

    private ReverseGeocodeProvider  rgEconomyRGP            = null;

    private ReverseGeocodeProvider  rgFailoverRGP           = null;
    private Object                  rgFailoverLock          = new Object();
    private long                    rgFailoverTime          = 0L; // Epoch time of failover
    private long                    rgFailoverTimeoutSec    = 0L; // failover timeout

    private int                     retryCount              = 0;
    private long                    retryMinWaitMS          = 0L;
    private long                    retryMaxWaitMS          = 0L;

    /**
    *** Constructor
    *** @param name  The name of this reverse-geocode provider
    *** @param key     The access key (may be null)
    *** @param rtProps The properties (may be null)
    **/
    public ReverseGeocodeProviderAdapter(String name, String key, RTProperties rtProps)
    {
        super();
        //Print.logInfo("Name="+name+", Key="+key);
        this.setName(name);
        this.setAuthorization(key);
        this.setProperties(rtProps);
        // -- initialize over-limit retry
        long retry[] = this.getProperties().getLongArray(PROP_overLimitRetry,null);
        if ((ListTools.size(retry) >= 2) && (retry[0] > 0L)) {
            long r0 = retry[0], r1 = retry[1], r2 = (retry.length > 2)? retry[2] : r1;
            this.retryCount     = Math.min(Math.max((int)r0,0             ), MAX_RETRY_COUNT   );
            this.retryMinWaitMS = Math.min(Math.max(r1,0L                 ), MAX_RETRY_SLEEP_MS);
            this.retryMaxWaitMS = Math.min(Math.max(r2,this.retryMinWaitMS), MAX_RETRY_SLEEP_MS);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the name of this ReverseGeocodeProvider
    *** @param name  The name of this reverse-geocode provider
    **/
    public void setName(String name)
    {
        this.name = (name != null)? name : "";
    }

    /**
    *** Gets the name of this ReverseGeocodeProvider
    *** @return The name of this reverse-geocode provider
    **/
    public String getName()
    {
        return (this.name != null)? this.name : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the authorization key of this ReverseGeocodeProvider
    *** @param key  The key of this reverse-geocode provider
    **/
    public void setAuthorization(String key)
    {
        this.accessKey = key;
    }

    /**
    *** Gets the authorization key of this ReverseGeocodeProvider
    *** @return The access key of this reverse-geocode provider
    **/
    public String getAuthorization()
    {
        return this.accessKey;
    }

    /**
    *** Returns true if the authorization key has been defined
    *** @return True if the authorization key has been defined, false otherwise.
    **/
    public boolean hasAuthorization()
    {
        return !StringTools.isBlank(this.getAuthorization());
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the economy ReverseGeocodeProvider
    *** @param rgp  The economy ReverseGeocodeProvider
    **/
    public void setEconomyReverseGeocodeProvider(ReverseGeocodeProvider rgp)
    {
        this.rgEconomyRGP = rgp;
    }

    /**
    *** Gets the economy ReverseGeocodeProvider
    *** @return The economy ReverseGeocodeProvider
    **/
    public ReverseGeocodeProvider getEconomyReverseGeocodeProvider()
    {
        return this.rgEconomyRGP;
    }

    /**
    *** Gets the economy ReverseGeocodeProvider name
    *** @return The economy ReverseGeocodeProvider name, or an empty string if
    ***         no economy is defined.
    **/
    public String getEconomyReverseGeocodeProviderName()
    {
        return (this.rgEconomyRGP != null)? this.rgEconomyRGP.getName() : "";
    }

    /**
    *** Has a economy ReverseGeocodeProvider
    *** @return True if this instance has an economy ReverseGeocodeProvider
    **/
    public boolean hasEconomyReverseGeocodeProvider()
    {
        return (this.rgEconomyRGP != null);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the failover ReverseGeocodeProvider
    *** @param rgp  The failover ReverseGeocodeProvider
    **/
    public void setFailoverReverseGeocodeProvider(ReverseGeocodeProvider rgp)
    {
        if (this.rgFailoverRGP != this) {
            this.rgFailoverRGP = rgp;
        } else {
            Print.logError("Recursive Failover ReverseGeocodeProvider specification!");
            this.rgFailoverRGP = null;
        }
    }

    /**
    *** Gets the failover ReverseGeocodeProvider
    *** @return The failover ReverseGeocodeProvider
    **/
    public ReverseGeocodeProvider getFailoverReverseGeocodeProvider()
    {
        return this.rgFailoverRGP;
    }

    /**
    *** Gets the failover ReverseGeocodeProvider name
    *** @return The failover ReverseGeocodeProvider name, or an empty string if
    ***         no failover is defined.
    **/
    public String getFailoverReverseGeocodeProviderName()
    {
        return (this.rgFailoverRGP != null)? this.rgFailoverRGP.getName() : "";
    }

    /**
    *** Has a failover ReverseGeocodeProvider
    *** @return True if this instance has a failover ReverseGeocodeProvider
    **/
    public boolean hasFailoverReverseGeocodeProvider()
    {
        return (this.rgFailoverRGP != null);
    }

    // ------------------------------------------------------------------------

    /**
    *** Start failover mode (with default timeout)
    **/
    protected void startReverseGeocodeFailoverMode()
    {
        this.startReverseGeocodeFailoverMode(-1L);
    }

    /**
    *** Start failover mode (with specified timeout)
    *** @param failoverTimeoutSec The explicit failover timeout, or <= 0 for the default timeout.
    **/
    protected void startReverseGeocodeFailoverMode(long failoverTimeoutSec)
    {
        synchronized (this.rgFailoverLock) {
            this.rgFailoverTime       = DateTime.getCurrentTimeSec();
            this.rgFailoverTimeoutSec = (failoverTimeoutSec > 0L)? 
                failoverTimeoutSec : this.getMaximumFailoverElapsedSec();
        }
    }

    /** 
    *** Returns true if failover mode is active
    **/
    protected boolean isReverseGeocodeFailoverMode()
    {
        if (this.hasFailoverReverseGeocodeProvider()) {
            boolean rtn;
            synchronized (this.rgFailoverLock) {
                if (this.rgFailoverTime <= 0L) {
                    rtn = false;
                } else {
                    long deltaSec = DateTime.getCurrentTimeSec() - this.rgFailoverTime;
                    long maxFailoverSec = (this.rgFailoverTimeoutSec > 0L)? 
                        this.rgFailoverTimeoutSec : this.getMaximumFailoverElapsedSec();
                    rtn = (deltaSec < maxFailoverSec)? true : false;
                    if (!rtn) {
                        // no longer in failover timeout mode
                        this.rgFailoverTime       = 0L;
                        this.rgFailoverTimeoutSec = 0L;
                    }
                }
            }
            return rtn;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the failover "quiet" mode hint.
    **/
    public void setFailoverQuiet(boolean quiet)
    {
        this.failoverQuiet = quiet? 1 : 0;
    }

    /**
    *** Gets the failover "quiet" mode hint.
    **/
    public boolean getFailoverQuiet()
    {
        if (this.failoverQuiet >= 0) {
            return (this.failoverQuiet != 0)? true : false;
        } else {
            RTProperties rtp = this.getProperties();
            return rtp.getBoolean(PROP_failoverQuiet, false);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Parse and return the user name and password
    *** @return The username and password (always a 2 element array)
    **/
    protected String[] _getUserPass()
    {
        String username = null;
        String password = null;
        String key = this.getAuthorization();
        if ((key != null) && !key.equals("")) {
            int p = key.indexOf(":");
            if (p >= 0) {
                username = key.substring(0,p);
                password = key.substring(p+1);
            } else {
                username = key;
                password = "";
            }
        } else {
            username = null;
            password = null;
        }
        return new String[] { username, password };
    }

    /** 
    *** Return authorization username.  This assumes that the username and password are
    *** separated by a ':' character
    *** @return The username
    **/
    protected String getUsername()
    {
        return this._getUserPass()[0];
    }
    
    /** 
    *** Return authorization password.  This assumes that the username and password are
    *** separated by a ':' character
    *** @return The password
    **/
    protected String getPassword()
    {
        return this._getUserPass()[1];
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the properties for this ReverseGeocodeProvider
    *** @param rtProps  The properties for this reverse-geocode provider
    **/
    public void setProperties(RTProperties rtProps)
    {
        this.properties = rtProps;
    }

    /**
    *** Gets the properties for this ReverseGeocodeProvider
    *** @return The properties for this reverse-geocode provider
    **/
    public RTProperties getProperties()
    {
        if (this.properties == null) {
            this.properties = new RTProperties();
        }
        return this.properties;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this instance
    *** @return A String representation of this instance
    **/
    public String toString()
    {
        StringBuffer sb= new StringBuffer();
        sb.append(this.getName());
        String auth = this.getAuthorization();
        if (!StringTools.isBlank(auth)) {
            sb.append(" [");
            sb.append(auth);
            sb.append("]");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if this ReverseGeocodeProvider is enabled
    *** @return True if enabled
    **/
    public boolean isEnabled()
    {
        if (this.isEnabled.isUnknown()) {
            String key = PROP_ReverseGeocodeProvider_ + this.getName() + _PROP_isEnabled;
            if (RTConfig.getBoolean(key,true)) {
                this.isEnabled = TriState.TRUE;
            } else {
                this.isEnabled = TriState.FALSE;
                Print.logWarn("ReverseGeocodeProvider disabled: " + this.getName());
            }
        }
        //Print.logInfo("Checking RGP 'isEnabled': " + this.getName() + " ==> " + this.isEnabled.isTrue());
        return this.isEnabled.isTrue();
    }

    // ------------------------------------------------------------------------

    /* Fast operation? */
    public boolean isFastOperation(boolean dft)
    {
        RTProperties rtp = this.getProperties();
        return rtp.getBoolean(PROP_alwaysFast, dft);
    }

    /* Fast operation? */
    public boolean isFastOperation()
    {
        // -- default to a slow operation
        return this.isFastOperation(false);
    }

    /* Maximum failover elapsed seconds */
    public long getMaximumFailoverElapsedSec()
    {
        RTProperties rtp = this.getProperties();
        long sec = rtp.getLong(PROP_maxFailoverSeconds, DEFAULT_MAX_FAILOVER_SECONDS);
        return (sec > MIN_FAILOVER_SECONDS)? sec : MIN_FAILOVER_SECONDS;
    }

    // ------------------------------------------------------------------------

    /* the minimum allowed number of elapsed seconds since the previous reverse-geocode. */
    public long getMinimumElapsedSec()
    {
        RTProperties rtp = this.getProperties();
        long sec = rtp.getLong(PROP_minElapsedSeconds, DEFAULT_MIN_ELAPSED_SECONDS);
        return sec;
    }

    // ------------------------------------------------------------------------

    /* creates a new empty ReverseGeocode instance */
    public static ReverseGeocode NewReverseGeocode(String rgProvider)
    {
        ReverseGeocode rg = new ReverseGeocode();
        if (rgProvider != null) {
            rg.setRGProvider(rgProvider);
        }
        return rg;
    }

    /* creates a new empty ReverseGeocode instance */
    public static ReverseGeocode NewReverseGeocode(ReverseGeocodeProvider rgp)
    {
        return NewReverseGeocode((rgp != null)? rgp.getName() : null);
    }

    /* creates a new empty ReverseGeocode instance */
    public ReverseGeocode newReverseGeocode()
    {
        return NewReverseGeocode(this.getName());
    }

    /* creates a new empty ReverseGeocode instance */
    public ReverseGeocode newReverseGeocode(JSON json)
    {
        ReverseGeocode rg = new ReverseGeocode(json);
        if (!rg.hasRGProvider()) {
            rg.setRGProvider(this.getName());
        }
        return rg;
    }

    // ------------------------------------------------------------------------

    /* get previously cached reverse-geocode */
    public ReverseGeocode getCachedReverseGeocode(GeoPoint gp)
    {
        // -- Override
        return null;
    }

    /* new: get reverse-geocode */
    public abstract ReverseGeocode getReverseGeocode(GeoPoint gp, boolean isMoving, 
        String localeStr, boolean cache, 
        String clientID, Properties props);

    // ------------------------------------------------------------------------

    /* retry count */
    public int getRetryCount()
    {
        return this.retryCount;
    }

    /* minimum retry delay MS */
    public long getMinRetryDelayMS()
    {
        return this.retryMinWaitMS;
    }

    /* maximum retry delay MS */
    public long getMaxRetryDelayMS()
    {
        return this.retryMaxWaitMS;
    }

    /* retry delay */
    public boolean retrySleep(int retryAttempt)
    {

        /* first attempt? not a retry */
        if (retryAttempt <= 0) {
            return true;
        }

        /* retry enabled for this count? */
        if (retryAttempt > this.getRetryCount()) {
            // -- exceeded retry attempts
            return false;
        }

        /* sleep */
        if (MAX_RETRY_SLEEP_MS > 0L) {
            long minMS = this.getMinRetryDelayMS();
            long maxMS = this.getMaxRetryDelayMS();
            long sleepMS = Math.max(minMS,0L);
            if (maxMS > minMS) {
                Random r = new Random();
                sleepMS += (long)r.nextInt((int)(maxMS - minMS));
            }
            if (sleepMS > 0L) {
                sleepMS = Math.min(sleepMS,MAX_RETRY_SLEEP_MS);
                try { Thread.sleep(sleepMS); } catch (Throwable th) { /*ignore*/ }
            }
        }

        /* retry ok */
        return true;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_ACCOUNT[]   = new String[] { "account"          , "acct"       };
    private static final String ARG_DEVICE[]    = new String[] { "device"           , "dev"        };
    private static final String ARG_PLN[]       = new String[] { "privateLabelName" , "pln" , "pl" };
    private static final String ARG_GEOPOINT[]  = new String[] { "geoPoint"         , "gp"         };
    private static final String ARG_ADDRESS[]   = new String[] { "address"          , "addr", "a"  };
    private static final String ARG_COUNTRY[]   = new String[] { "country"          , "c"          };
    private static final String ARG_CACHE[]     = new String[] { "cache"            , "save"       };
    private static final String ARG_MOVING[]    = new String[] { "moving"           , "mv"         };
    private static final String ARG_ABBREV[]    = new String[] { "abbrev"           , "short"      };

    private static void usage()
    {
        String n = ReverseGeocodeProviderAdapter.class.getName();
        Print.sysPrintln("");
        Print.sysPrintln("Description:");
        Print.sysPrintln("   Reverse-Geocode Testing Tool ...");
        Print.sysPrintln("");
        Print.sysPrintln("Usage:");
        Print.sysPrintln("   java ... " + n + " -geoPoint=<gp> -account=<id>");
        Print.sysPrintln(" or");
        Print.sysPrintln("   java ... " + n + " -geoPoint=<gp> -pln=<name>");
        Print.sysPrintln("");
        Print.sysPrintln("Common Options:");
        Print.sysPrintln("   -account=<id>   Acount ID from which to obtain the ReverseGeocodeProvider");
        Print.sysPrintln("   -pln=<name>     PrivateLabel name/host");
        Print.sysPrintln("   -geoPoint=<gp>  GeoPoint in the form <latitude>/<longitude>");
        Print.sysPrintln("   -addr=<addr>    Address to Geocode");
        Print.sysPrintln("");
        System.exit(1);
    }

    public static void _main()
    {
        boolean abbrevDisp = RTConfig.getBoolean(ARG_ABBREV,false);

        /* get GeoPoint(s) */
        GeoPoint GPA[] = null;
        if (RTConfig.hasProperty(ARG_GEOPOINT)) {
            String gpa[] = StringTools.split(RTConfig.getString(ARG_GEOPOINT,""),',');
            Vector<GeoPoint> gpList = new Vector<GeoPoint>();
            for (String gps : gpa) {
                Print.sysPrintln("Parsing: " + gps);
                GeoPoint gp = new GeoPoint(gps);
                if (gp.isValid()) {
                    gpList.add(gp);
                }
            }
            GPA = gpList.toArray(new GeoPoint[gpList.size()]);
        }
        if (ListTools.isEmpty(GPA)) {
            Print.sysPrintln("ERROR: No GeoPoint specified");
            usage();
        }

        /* get PrivateLabel */
        BasicPrivateLabel privLabel = null;
        String accountID = RTConfig.getString(ARG_ACCOUNT, "");
        if (!StringTools.isBlank(accountID)) {
            Account acct = null;
            try {
                acct = Account.getAccount(accountID); // may throw DBException
                if (acct == null) {
                    Print.sysPrintln("ERROR: Account-ID does not exist: " + accountID);
                    usage();
                }
                privLabel = acct.getPrivateLabel();
            } catch (DBException dbe) {
                Print.logException("Error loading Account: " + accountID, dbe);
                //dbe.printException();
                System.exit(99);
            }
        } else {
            String pln = RTConfig.getString(ARG_PLN,"default");
            if (StringTools.isBlank(pln)) {
                Print.sysPrintln("ERROR: Must specify '-account=<Account>'");
                usage();
            } else {
                privLabel = BasicPrivateLabelLoader.getPrivateLabel(pln);
                if (privLabel == null) {
                    Print.sysPrintln("ERROR: PrivateLabel name not found: %s", pln);
                    usage();
                }
            }
        }

        /* get reverse-geocode provider */
        ReverseGeocodeProvider rgp = privLabel.getReverseGeocodeProvider();
        if (rgp == null) {
            Print.sysPrintln("ERROR: No ReverseGeocodeProvider for PrivateLabel: %s", privLabel.getName());
            System.exit(99);
        } else
        if (!rgp.isEnabled()) {
            Print.sysPrintln("WARNING: ReverseGeocodeProvider disabled: " + rgp.getName());
            System.exit(0);
        }

        /* init properties */
        Properties props = null;
        String deviceUID = RTConfig.getString(ARG_DEVICE, "");
        if (!StringTools.isBlank(deviceUID)) {
            // -- some reverse-geocode providers require this field
            props = new Properties();
            if (deviceUID.indexOf("/") >= 0) {
                // -- device UID already specified as "ACCOUNT/DEVICE"
                props.setProperty(Device.FLD_deviceID, deviceUID);
            } else
            if (!StringTools.isBlank(accountID)) {
                // -- account ID is available
                props.setProperty(Device.FLD_accountID, accountID);
                props.setProperty(Device.FLD_deviceID , deviceUID);
            } else {
                // -- save deviceID as-is
                props.setProperty(Device.FLD_deviceID, deviceUID);
            }
        }

        /* get ReverseGeocode */
        Print.sysPrintln("");
        try {
            // -- make sure the Domain properties are available to RTConfig
            privLabel.pushRTProperties();   // stack properties (may be redundant in servlet environment)
            boolean isMoving  = RTConfig.getBoolean(ARG_MOVING, false);
            String  isMovingS = isMoving? " (moving)" : "";
            boolean cache     = RTConfig.getBoolean(ARG_CACHE, false);
            String  cacheS    = cache? " (cache)" : "";
            String  clientID  = "";
            for (GeoPoint gp : GPA) {
                Print.sysPrintln("------------------------------------------------");
                Print.sysPrintln(rgp.getName() + "] ReverseGeocode: " + gp + cacheS);
                long startTimeMS = DateTime.getCurrentTimeMillis();
                ReverseGeocode rg = rgp.getReverseGeocode(gp, isMoving, 
                    privLabel.getLocaleString(), cache, 
                    clientID, props);
                long deltaMS = DateTime.getCurrentTimeMillis() - startTimeMS;
                if (rg == null) {
                    Print.sysPrintln("Unable to reverse-geocode point ["+deltaMS+" ms]");
                    continue;
                }
                // --
                if (abbrevDisp) {
                    StringBuffer sb = new StringBuffer();
                    Print.errPrintln("Addr: ["+gp+"] "+rg.toString());
                } else {
                    Print.sysPrintln("------------------------------------------------");
                    Print.sysPrintln("RGProv  : " + rg.getRGProvider() + " (from "+rg.getCachedStateString()+")");
                    if (rg.hasRGUrl()) {
                        Print.sysPrintln("URL     : " + StringTools.trim(rg.getRGUrl()));
                    }
                    Print.sysPrintln("Address : " + rg.toString());
                    if (rg.hasCity()) {
                        Print.sysPrintln("City    : " + StringTools.trim(rg.getCity()));
                    }
                    if (rg.hasStateProvince()) {
                        Print.sysPrintln("State   : " + StringTools.trim(rg.getStateProvince()));
                    }
                    if (rg.hasPostalCode()) {
                        Print.sysPrintln("Postal  : " + StringTools.trim(rg.getPostalCode()));
                    }
                    if (rg.hasCountryCode()) {
                        Print.sysPrintln("Country : " + StringTools.trim(rg.getCountryCode()));
                    }
                    if (rg.hasSpeedLimitKPH()) {
                        double kph  = rg.getSpeedLimitKPH();
                        String _kph = StringTools.format(kph,"0.0");
                        String _mph = StringTools.format(kph*GeoPoint.MILES_PER_KILOMETER,"0.0");
                        Print.sysPrintln("SpeedLim: "+_kph+" km/h ["+_mph+" mph]"+isMovingS);
                    } else {
                        //Print.sysPrintln("SpeedLim: n/a "+isMovingS);
                    }
                    Print.sysPrintln("Time    : " + deltaMS + " ms");
                }
            }
            Print.sysPrintln("------------------------------------------------");
        } catch (Throwable th) {
            // -- ignore
        } finally {
            privLabel.popRTProperties();    // remove from stack
        }
        Print.sysPrintln("");

    }

    public static void main(String args[])
    {
        DBConfig.cmdLineInit(args,true);  // main
        if (RTConfig.hasProperty(ARG_ADDRESS)) {
            GeocodeProviderAdapter._main();
        } else {
            ReverseGeocodeProviderAdapter._main();
        }
    }

}
