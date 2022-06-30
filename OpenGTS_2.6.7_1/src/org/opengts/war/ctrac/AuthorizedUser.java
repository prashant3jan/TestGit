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
//  2012/08/01  Martin D. Flynn
//     -Initial release. 
// ----------------------------------------------------------------------------
package org.opengts.war.ctrac;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.dbtypes.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;

public class AuthorizedUser 
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // -- Track.java account/user vars
    public  static final String  SESSVAR_ACCOUNT                = "account"; // Constants.PARM_ACCOUNT
    public  static final String  SESSVAR_USER                   = "user";    // Constants.PARM_USER

    // -- local login vars
    private static final String  SESSVAR_SessionAccountID       = "SessionAccountID";
    private static final String  SESSVAR_SessionUserID          = "SessionUserID";
    private static final String  SESSVAR_SessionPassword        = "SessionPassword";

    /**
    *** Load AuthorizedUser from current session.
    *** Does not return null
    **/
    public static AuthorizedUser LoadFromSession(
        HttpServletRequest request, 
        boolean checkTrackWar, String jSessionID)
    {

        /* no HttpServeletRequest? */
        if (request == null) {
            return new AuthorizedUser();
        }

        /* check for "Track.java" environment login */
        if (checkTrackWar) {
            String loginAcctID = (String)AttributeTools.getSessionAttribute(request, SESSVAR_ACCOUNT, "");
            String loginUserID = (String)AttributeTools.getSessionAttribute(request, SESSVAR_USER   , "");
            if (!StringTools.isBlank(loginAcctID)) {
                Account acct = null;
                User    user = null;
                try {
                    acct = AuthorizedUser.LoadAccount(loginAcctID); // not null
                    user = AuthorizedUser.LoadUser(acct, loginUserID); // may be null
                    return new AuthorizedUser(acct, user);
                } catch (NotAuthorizedException nae) {
                    // -- login Account/User is not authorized
                }
            }
        }

        /* get HttpSession, based on "jSessionID" (if specified) */
        HttpSession hs = null;
        if (!StringTools.isBlank(jSessionID)) {
            ServletContext sc = AttributeTools.getServletContext(request);
            if (sc != null) {
                hs = RTConfigContextListener.GetSessionForID(sc, jSessionID); // may still be null
            }
        }
        if (hs == null) { // default to current HttpSession
            hs = AttributeTools.getSession(request);
        }
        String acctID = (String)AttributeTools.getSessionAttribute(hs, SESSVAR_SessionAccountID, "");
        String userID = (String)AttributeTools.getSessionAttribute(hs, SESSVAR_SessionUserID   , "");
        String passw  = (String)AttributeTools.getSessionAttribute(hs, SESSVAR_SessionPassword , "");
        return new AuthorizedUser(acctID, userID, passw);
 
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static class NotAuthorizedException
        extends Exception
    {
        public NotAuthorizedException(String msg) {
            super(msg);
        }
        public String getMessage() {
            return super.getMessage();
        }
        public String getResponseText() {
            return "Not Authorized";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Load Account from AccountID
    **/
    public static Account LoadAccount(String accountID)
        throws NotAuthorizedException
    {
        if (StringTools.isBlank(accountID)) {
            //Print.logError("AccountID is null");
            throw new NotAuthorizedException("Blank/Null AccountID");
        }
        // --
        try {
            Account a = Account.getAccount(accountID);
            if (a == null) {
                Print.logError("AccountID not found: " + accountID);
                throw new NotAuthorizedException("Invalid Account");
            } else
            if (!a.isActive()) {
                Print.logError("Account not active: " + accountID);
                throw new NotAuthorizedException("Account Inactive");
            } else
            if (a.isExpired()) {
                Print.logError("Account expired: " + accountID);
                throw new NotAuthorizedException("Account Expired");
            } else
            if (a.isSuspended()) {
                Print.logError("Account suspended: " + accountID);
                throw new NotAuthorizedException("Account Suspended");
            }
            return a;
        } catch (DBException dbe) {
            Print.logException("Error reading Account: " + accountID, dbe);
            throw new NotAuthorizedException("Internal Error");
        }
    }

    /**
    *** Load User from Account,UserID
    **/
    public static User LoadUser(Account account, String userID)
        throws NotAuthorizedException
    {
        if (StringTools.isBlank(userID)) {
            return null; // not an error
        }
        // --
        if (account == null) {
            Print.logError("Account is null");
            throw new NotAuthorizedException("Null Account");
        }
        // --
        String accountID = account.getAccountID();
        try {
            User u = User.getUser(account, userID);
            if (u == null) {
                if (User.isAdminUser(userID)) {
                    // -- Explicit "admin" user not required
                } else {
                    Print.logError("UserID not found: " + accountID + "/" + userID);
                    throw new NotAuthorizedException("Invalid User");
                }
            } else
            if (!u.isActive()) {
                Print.logError("User not active: " + accountID + "/" + userID);
                throw new NotAuthorizedException("User Inactive");
            }
            return u;
        } catch (DBException dbe) {
            Print.logException("Error reading User: " + accountID + "/" + userID, dbe);
            throw new NotAuthorizedException("Internal Error");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String  authErrMsg      = "Not Initialized";
    
    private String  accountID       = null;
    private String  userID          = null;
    private String  password        = null;

    private Account account         = null;
    private User    user            = null;

    /**
    *** Constructor
    **/
    public AuthorizedUser()
    {
        super();
        this.authErrMsg = "Not Initialized";
    }

    /**
    *** Constructor
    **/
    public AuthorizedUser(Account acct, User user)
    {
        this();

        /* no account? */
        if (acct == null) {
            this.authErrMsg = "Null Account";
            return;
        }
        // -- TODO: check expired, suspended, etc?

        /* invalid user? */
        if ((user != null) && !user.getAccountID().equals(acct.getAccountID())) {
            this.authErrMsg = "Invalid User";
            return;
        }
        // -- TODO: check expired, suspended, etc?

        /* save */
        this.account    = acct;
        this.user       = user;
        this.password   = null;
        this.authErrMsg = null; // success

    }

    /**
    *** Constructor
    **/
    public AuthorizedUser(String accountID, String userID, String password)
    {
        this();

        /* save account/user/password */
        this.accountID = accountID;
        this.userID    = userID;
        this.password  = password; // may be null/blank

        /* load Account/User */
        Account ACC = null;
        User    USR = null;
        try {
            ACC = AuthorizedUser.LoadAccount(accountID); // not null
            USR = AuthorizedUser.LoadUser(ACC, userID); // may be null
        } catch (NotAuthorizedException nae) {
            this.authErrMsg = nae.getMessage();
            return;
        }
        // -- ACC is non-null here, USR may be null

        /* check password */
        String pwd = (password != null)? password : "";
        BasicPrivateLabel privLabel = ACC.getPrivateLabel();
        if (StringTools.isBlank(pwd)) {
            Print.logWarn("No password specified");
            this.authErrMsg = "No password specified";
            return;
        } else
        if (USR != null) {
            // -- check user password
            if (!USR.checkPassword(privLabel,pwd,true/*suspend*/)) {
                Audit.userLoginFailed(accountID, userID, null, null, null);
                Print.logError("User invalid password: " + accountID + "/" + userID);
                this.authErrMsg = "Invalid Login";
                return;
            }
        } else {
            // -- check account password
            if (!ACC.checkPassword(privLabel,pwd,true/*suspend*/)) {
                Audit.userLoginFailed(accountID, userID, null, null, null);
                Print.logError("Account invalid password: " + accountID);
                this.authErrMsg = "Invalid Login";
                return;
            }
        }

        /* save Account/User */
        this.account    = ACC;
        this.user       = USR;
        this.authErrMsg = null; // success

    }

    // ------------------------------------------------------------------------

    /**
    *** Save AuthorizedUser to current session (only if authorized)
    **/
    public void saveToSession(HttpServletRequest request)
    {
        if (request == null) {
            // -- ignore
        } else
        if (this.isAuthorized() && this.hasAccountID()) {
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionAccountID, this.getAccountID());
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionUserID   , this.getUserID());
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionPassword , this.getPassword());
        } else {
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionAccountID, null);
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionUserID   , null);
            AttributeTools.setSessionAttribute(request, SESSVAR_SessionPassword , null);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this account/user is authorized
    **/
    public boolean isAuthorized()
    {
        return (this.account != null)? true : false;
    }
    
    /**
    *** Gets the Authroization error message, null if successfully Authorized
    **/
    public String getErrorMessage()
    {
        return this.authErrMsg;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets authorized Account instance
    **/
    public Account getAccount()
    {
        return this.account; // will be null, if not authorized
    }

    /**
    *** Gets authorized Account ID
    **/
    public String getAccountID()
    {
        if (this.account != null) {
            return this.account.getAccountID();
        } else
        if (!StringTools.isBlank(this.accountID)) {
            return this.accountID;
        } else {
            return "";
        }
    }

    /**
    *** Returns true if this instance defines an account-id
    **/
    public boolean hasAccountID()
    {
        return !StringTools.isBlank(this.getAccountID())? true : false;
    }

    /**
    *** Returns true if the authorized user is "sysadmin"
    **/
    public boolean isSystemAdmin()
    {
        return Account.isSystemAdmin(this.getAccount())? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets authorized User instance.  May return null for "admin" user.
    **/
    public User getUser()
    {
        return this.user; // may be null
    }

    /**
    *** Gets authorized Account ID
    **/
    public String getUserID()
    {
        if (this.user != null) {
            return this.user.getUserID();
        } else
        if (!StringTools.isBlank(this.userID)) {
            return this.userID;
        } else 
        if (this.hasAccountID()) {
            return User.getAdminUserID();
        } else {
            return ""; // if no AccountID, then no UserID
        }
    }
    
    /**
    *** Returns true if the current user is the "admin" user
    **/
    public boolean isAdminUser()
    {
        return User.isAdminUser(this.getUserID());
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Account/User password
    **/
    private String getPassword()
    {
        return this.password;
    }

    /**
    *** Returns true if a password has been specified
    **/
    public boolean hasPassword()
    {
        return !StringTools.isBlank(this.getPassword())? true : false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a list of Authorized (Active) Device IDs
    **/
    public OrderedSet<String> getAuthorizedDeviceIDs()
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        boolean inclInactive = false;
        try {
            return User.getAuthorizedDeviceIDs(rwMode, this.account, this.user, inclInactive);
        } catch (DBException dbe) {
            Print.logException("Unable to get Authorized DeviceIDs", dbe);
            return new OrderedSet<String>();
        }
    }

    /**
    *** Returns true if the specified device exists, and the user is authorized
    *** to view this device.
    **/
    public boolean isAuthorizedDevice(String deviceID)
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* device exists? */
        if (!this.deviceExists(deviceID)) {
            return false;
        }

        /* is authorized */
        if (this.user == null) {
            return true; // assume "admin" user
        } else {
            try {
                return this.user.isAuthorizedDevice(rwMode, deviceID);
            } catch (DBException dbe) {
                Print.logException("Error checking for authorized device", dbe);
                return false;
            }
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified device exists
    **/
    public boolean deviceExists(String deviceID)
    {
        try {
            return Device.exists(this.getAccountID(), deviceID);
        } catch (DBException dbe) {
            Print.logException("Unable to check Device existance", dbe);
            return false;
        }
    }

    /**
    *** Gets the Device record for the specified DeviceID
    **/
    public Device getDevice(String deviceID)
        throws DBException
    {

        /* no deviceID? */
        if (StringTools.isBlank(deviceID)) {
            return null;
        }

        /* not authorized? */
        if (!this.isAuthorizedDevice(deviceID)) { 
            return null;
        }

        /* return device */
        return Device.getDevice(this.getAccount(), deviceID); // null if non-existent

    }

    /**
    *** Gets the Device record for the specified MobileID
    **/
    public Device loadDeviceByPrefixedModemID(String prefix[], String mobileID)
        //throws DBException
    {
        Device device = null;

        /* get Device by UniqueID */
        device = DCServerFactory._loadDeviceByPrefixedModemID(prefix, mobileID);
        if (device == null) {
            Print.logError("This MobileID does not exist: " + mobileID);
            return null;
        }

        /* check for matching AccountID */
        String accountID = this.getAccountID();
        String mobAcctID = device.getAccountID();
        if (!accountID.equals(mobAcctID)) {
            Print.logError("Mobile/Login Account mismatch: login=" + accountID + ", mobile=" + mobAcctID);
            return null;
        }

        /* check for authorized device */
        String mobDevID = device.getDeviceID();
        if (!this.isAuthorizedDevice(mobDevID)) {
            Print.logError("Not authorized for device: " + accountID + "/" + mobDevID);
            return null;
        }

        /* return Device */
        return device;

    }


    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
