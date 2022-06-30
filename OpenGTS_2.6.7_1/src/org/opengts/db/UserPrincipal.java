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
//  Class representing a UserInformation 'Principal' object 
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.security.Principal;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;
import org.opengts.db.tables.*;

public class UserPrincipal
    implements Principal
{

    // ------------------------------------------------------------------------

    private String          accountID = null;
    private String          userID    = null;

    private UserInformation userInfo  = null;

    /**
    *** Constructor
    **/
    public UserPrincipal(UserInformation ui, boolean saveUI) 
    {
        super();
        if (ui != null) {
            this.accountID = ui.getAccountID();
            this.userID    = ui.getUserID();
            if (saveUI) {
                this.userInfo = ui;
            }
        }
    }

    /**
    *** Constructor
    **/
    public UserPrincipal(String acctID, String userID) 
    {
        super();
        this.accountID = StringTools.trim(acctID);
        this.userID    = StringTools.trim(userID);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the UserInformation instance represented by this Principal
    *** (may be null)
    **/
    public UserInformation getUserInformation()
    {
        return this.userInfo; // may be null
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the AccountID for this Principal
    **/
    public String getAccountID()
    {
        if (!StringTools.isBlank(this.accountID)) {
            return this.accountID;
        } else 
        if (this.userInfo != null) {
            return this.userInfo.getAccountID();
        } else {
            return "";
        }
    }

    /**
    *** Gets the UserID for this Principal
    **/
    public String getUserID()
    {
        if (!StringTools.isBlank(this.userID)) {
            return this.userID;
        } else 
        if (this.userInfo != null) {
            return this.userInfo.getUserID();
        } else {
            return "";
        }
    }

    // ------------------------------------------------------------------------

    public String toString() 
    {
        return this.getAccountID() + "/" + this.getUserID();
    }

    public String getName() 
    {
        return this.toString();
    }

    // ------------------------------------------------------------------------

    public boolean equals(Object up)
    {
        if (!(up instanceof UserPrincipal)) {
            return false;
        } else
        if (!this.getAccountID().equals(((UserPrincipal)up).getAccountID())) {
            return false;
        } else
        if (!this.getUserID().equals(((UserPrincipal)up).getUserID())) {
            return false;
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------

    public int hashCode() 
    {
        return super.hashCode();
    }

}
