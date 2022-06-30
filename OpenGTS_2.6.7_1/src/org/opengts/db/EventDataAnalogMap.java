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
// Description:
//  Container for EventDataAnalog instances
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.db.tables.*;

/**
*** EventDataAnalogMap class<br>
**/
public class EventDataAnalogMap
{

    // ------------------------------------------------------------------------
    
    public static String DEFAULT    = "default";

    // ------------------------------------------------------------------------

    private Map<String,EventDataAnalog[]> analogMap = new HashMap<String,EventDataAnalog[]>();
    private Class<EventDataAnalog>  classEventDataAnalog = null;

    public EventDataAnalogMap()
    {
        super();
    }

    public EventDataAnalogMap(Class<EventDataAnalog> edaClass)
    {
        this();
        this.classEventDataAnalog = edaClass;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Adds an EventDataAnalog instance for the specified context.
    *** The context may be blank/null for the default instance.
    *** @param context  The context 
    **/
    public void add(String context, EventDataAnalog eda[])
    {
        if (!ListTools.isEmpty(eda)) {
            String ctx = StringTools.trim(context).toLowerCase();
            if (StringTools.isBlank(ctx) || ctx.equals(DEFAULT)) {
                // -- explicit default
                this.analogMap.put(DEFAULT,eda);
            } else
            if (this.analogMap.isEmpty()) {
                // -- first entry: add to both the default and context
                this.analogMap.put(DEFAULT,eda);
                this.analogMap.put(ctx,eda);
            } else {
                // -- subsequent entry: add to context
                this.analogMap.put(ctx,eda);
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Runtime properties String getter for DCServerConfig instances
    **/
    private class AnalogRTP
    {
        private DCServerConfig  dcsc    = null;
        private String          context = null;
        public AnalogRTP(DCServerConfig dcs, String ctx) {
            this.dcsc    = dcs;
            this.context = ctx;
        }
        public String getString(String alogKeyPfx, int alogNdx, String dft) {
            if (this.dcsc == null) {
                return dft;
            }
            String key[] = { 
                alogKeyPfx + alogNdx,              // analog.1  - First analog
                alogKeyPfx + alogNdx        + "1", // analog.11 - First analog, starting at index "1"
                alogKeyPfx + (alogNdx - 1)  + "0", // analog.00 - First analog, starting at index "0"
            };
            if (StringTools.isBlank(this.context)) {
                return this.dcsc.getStringProperty(key, dft);
            } else {
                RTProperties rtp = this.dcsc.getProperties(this.context);
                if (rtp != null) {
                    return rtp.getString(key, dft);
                } else {
                    return dft;
                }
            }
        }
    }

    /**
    *** Load Analog properties from DCServerConfig
    **/
    private EventDataAnalog[] _load(DCServerConfig dcsc, String context, String alogKeyPfx, int maxCount)
    {
        if (dcsc == null) {
            // -- nothing from which to obtain config properties
            return null;
        } else
        if (StringTools.isBlank(alogKeyPfx)) {
            // -- no property prefix
            return null;
        } else
        if (maxCount <= 0) {
            // -- wants zero analog entries
            return null;
        }
        // --
        AnalogRTP rtp = new AnalogRTP(dcsc, context);
        EventDataAnalog eda[] = new EventDataAnalog[maxCount];
        for (int aNdx = 1; aNdx <= maxCount; aNdx++) {
            EventDataAnalog edai = null;
            String valStr = rtp.getString(alogKeyPfx, aNdx, null);
            if (StringTools.isBlank(valStr)) {
                edai = null;
            } else
            if (this.classEventDataAnalog != null) {
                try {
                    MethodAction maIns = new MethodAction(this.classEventDataAnalog,Integer.TYPE,String.class);
                    edai = (EventDataAnalog)maIns.invoke(new Integer(aNdx), valStr);
                } catch (Throwable th) { // InvocationException, ClassCastException
                    // -- error
                    Print.logException("Unable to instantiate EventDataAnalog subclass", th);
                    edai = null;
                }
            } else {
                edai = new EventDataAnalog(aNdx, valStr);
            }
            eda[aNdx - 1] = EventDataAnalog.isValid(edai)? edai : null; // 'edai' may be null
        }
        return eda;
    }

    /**
    *** Load Analog properties from DCServerConfig
    **/
    public void load(DCServerConfig dcsc, String alogKeyPfx, int maxCount)
    {
        if (dcsc == null) {
            // -- nothing from which to obtain config properties
            return;
        } else
        if (StringTools.isBlank(alogKeyPfx)) {
            // -- no property prefix
            return;
        } else
        if (maxCount <= 0) {
            // -- wants zero analog entries
            return;
        }
        // -- add default
        this.add(DEFAULT, this._load(dcsc,null,alogKeyPfx,maxCount));
        // -- add context properties
        for (String context : dcsc.getPropertyGroupNames()) {
            this.add(context, this._load(dcsc,context,alogKeyPfx,maxCount));
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the EventDataAnalog entry for the specified context and index
    **/
    public EventDataAnalog get(String context, int ndx, EventDataAnalog dft)
    {

        /* quick index validation */
        if (ndx < 0) {
            // -- index is invalid in any context
            return dft;
        }

        /* get context EventDataAnalog array */
        String ctx = StringTools.blankDefault(StringTools.trim(context).toLowerCase(),DEFAULT);
        EventDataAnalog eda[] = this.analogMap.get(ctx);
        if (eda == null) {
            eda = this.analogMap.get(DEFAULT); // could still be null
            if (eda == null) {
                // -- default not found
                return dft;
            }
        }

        /* validate index against array size */
        if (ndx >= ListTools.size(eda)) {
            // -- index is outside array
            return dft;
        }

        /* return */
        return (eda[ndx] != null)? eda[ndx] : dft;

    }

    // ------------------------------------------------------------------------

}
