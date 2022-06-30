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
//  2009/04/02  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.util.JSON.JSONBeanGetter;
import org.opengts.util.JSON.JSONBeanSetter;

import org.opengts.dbtools.*;

import org.opengts.db.*;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.DeviceGroup;

public class GroupRecord<RT extends DBRecord<RT>>
    extends AccountRecord<RT>
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* common Asset/Group field definition */
    public static final String FLD_groupID = "groupID";

    /* create a new "groupID" key field definition */
    public static DBField newField_groupID(boolean key)
    {
        return GroupRecord.newField_groupID(key, "Device Group ID");
    }

    /* create a new "groupID" key field definition */
    public static DBField newField_groupID(boolean key, String title)
    {
        return new DBField(FLD_groupID, String.class, DBField.TYPE_GROUP_ID(), title, (key?"key=true":"edit=2"));
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static abstract class GroupKey<RT extends DBRecord<RT>>
        extends AccountKey<RT>
    {
        public GroupKey() {
            super();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* Bean instance */
    public GroupRecord()
    {
        super();
    }

    /* database record */
    public GroupRecord(GroupKey<RT> key)
    {
        super(key);
    }
         
    // ------------------------------------------------------------------------

    /* Group ID */
    @JSONBeanGetter()
    public final String getGroupID()
    {
        String v = (String)this.getKeyValue(FLD_groupID); // getFieldValue
        return StringTools.trim(v);
    }

    public /*final*/ void setGroupID(String v)
    {
        this.setKeyValue(FLD_groupID, StringTools.trim(v)); // setFieldValue
    }

    // ------------------------------------------------------------------------
    // The following is an optimization for holding the DeviceGroup record while
    // processing this GroupRecord.  Use with caution.

    private DeviceGroup group = null;

    public final boolean hasDeviceGroup()
    {
        if ((Object)this instanceof DeviceGroup) {
            return true;
        } else {
            return (this.group != null);
        }
    }

    /* get the device for this event */
    public final DeviceGroup getDeviceGroup()
    {

        /* return this DeviceGroup? */
        if ((Object)this instanceof DeviceGroup) {
            return (DeviceGroup)((Object)this);
        }

        /* get/return DeviceGroup */
        if (this.group == null) {
            String accountID = this.getAccountID();
            String groupID   = this.getGroupID();
            Print.logDebug("[Optimize] Retrieving DeviceGroup record: " + accountID + "/" + groupID);
            try {
                this.group = DeviceGroup.getDeviceGroup(this.getAccount(), groupID);
                // -- 'this.device' may still be null if the asset was not found
            } catch (DBException dbe) {
                // -- may be caused by "java.net.ConnectException: Connection refused: connect"
                Print.logError("Group not found: " + accountID + "/" + groupID);
                this.group = null;
            }
        }
        return this.group;

    }

    /* set the DeviceGroup for this instance */
    public final void setDeviceGroup(DeviceGroup grp) 
    {
        if ((Object)this instanceof DeviceGroup) {
            if (this != grp) {
                Print.logError("'this' is already a DeviceGroup: " + this.getGroupID());
            }
        } else
        if (grp == null) {
            this.group = null;
        } else
        if (!this.getAccountID().equals(grp.getAccountID()) || 
            !this.getGroupID().equals(grp.getGroupID()    )   ) {
            String msg = "DeviceGroup IDs do not match: " + this.getAccountID() + " != " + grp.getAccountID();
            this.group = null;
        } else {
            this.setAccount(grp.getAccount());
            this.group = grp;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Return the description for this instance
    *** @return The DeviceGroup description
    **/
    public final String getGroupDescription()
    {
        DeviceGroup grp = this.getDeviceGroup();
        if (grp == null) {
            return this.getGroupID();
        } else
        if (!StringTools.isBlank(grp.getDisplayName())) {
            return grp.getDisplayName();
        } else 
        if (!StringTools.isBlank(grp.getDescription())) {
            return grp.getDescription();
        } else {
            return grp.getGroupID();
        }
    }

    // ------------------------------------------------------------------------

}
