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
//  LDAP Authentication
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.Properties;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.naming.*;
import javax.naming.directory.*;

import org.opengts.util.*;

/**
*** LDAP Authentication
**/

public class LdapAuth
{

    // ------------------------------------------------------------------------

    public static final String INITIAL_CONTEXT_FACTORY      = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String SECURITY_AUTHENTICATION      = "simple";

    public static final String UID_SEPARATOR                = "|";

    // ------------------------------------------------------------------------
    // -- Properties

    public static final String PROP_LdapAuth_ProviderURL    = "LdapAuth.ProviderURL";               // "ldap://127.0.0.1:389";
    public static final String PROP_LdapAuth_UsersContext   = "LdapAuth.UsersContext";              // "ou=users,dc=example,dc=com";
    public static final String PROP_LdapAuth_RootDN         = "LdapAuth.RootDistinguishedName";     // "cn=admin,dc=example,dc=com";
    public static final String PROP_LdapAuth_RootPassword   = "LdapAuth.RootPassword";              // "ldap";
    public static final String PROP_LdapAuth_SecurityAuth   = "LdapAuth.SecurityAuthentication";    // "simple";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Escape leading/trailing space, and special characters in DN names
    **/
    public static String EscapeUID(String uid)
    {
        // -- null UID?
        if (uid == null) {
            return "";
        }
        // -- check for special characters to escape
        StringBuffer sb = new StringBuffer();
        int uidLen = uid.length();
        uidCharLoop:
        for (int i = 0; i < uidLen;) {
            char ch = uid.charAt(i);
            // -- check for space (special-case because embedded space is not escaped)
            if (Character.isWhitespace(ch)) {
                // -- escape leading/trailing space 
                if (sb.length() == 0) {
                    // -- found a leading leading space
                    sb.append("\\").append(ch); // first space character
                    i++;
                    // -- escape remaining leading space
                    while (i < uidLen) {
                        char sch = uid.charAt(i);
                        if (Character.isWhitespace(sch)) {
                            sb.append("\\").append(sch);
                            i++; // consume this char
                        } else {
                            // -- non-space at 'i'
                            break; // continue uidCharLoop;
                        }
                    }
                } else {
                    // -- check for embedded/trailing space
                    int e = i + 1; // start at char following known-space
                    while ((e < uidLen) && Character.isWhitespace(uid.charAt(e))) { e++; }
                    if (e >= uidLen) {
                        // -- trailing space
                        for (;i < uidLen;) {
                            sb.append("\\").append(uid.charAt(i));
                            i++;
                        }
                    } else {
                        // -- embedded space (no escape)
                        for (;i < e;) {
                            sb.append(uid.charAt(i));
                            i++;
                        }
                    }
                }
                // continue uidCharLoop;
            } else {
                // -- not a space, escape special characters
                switch (ch) {
                    case ','  :
                    case '\\' :
                    case '#'  :
                    case '+'  :
                    case '<'  :
                    case '>'  :
                    case ';'  :
                    case '\"' :
                    case '='  :
                    case '/'  :
                        sb.append("\\").append(ch);
                        break;
                    default   :
                        sb.append(ch);
                        break;
                }
                i++; // consume character
            }
        } // uidCharLoop
        return sb.toString();
    }

    /**
    *** Returns a UID based on the specified accountID and userID
    **/
    private static String CreateUID(UserInformation user) 
    {
        if (user != null) {
            String acctID = user.getAccountID();
            String userID = user.getUserID();
            String uid    = acctID + UID_SEPARATOR + userID;
            return LdapAuth.EscapeUID(uid);
        } else {
            return "";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final int SALT_LENGTH = 4;

    /**
    *** Generates an SSHA encoded password String compatible with OpenLDAP
    **/
    public static String EncodePassword(String passwd)
        throws NamingException
    {
        // - https://stackoverflow.com/questions/35065529/java-method-for-password-encrypt-in-ssha-for-ldap

        /* encode password */
        try {

            /* password bytes */
            byte p[] = (passwd != null)? passwd.getBytes() : new byte[0];

            /* salt */
            byte s[] = new byte[SALT_LENGTH];
            (new SecureRandom()).nextBytes(s);
    
            /* encode password */
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            sha1Digest.reset();
            sha1Digest.update(p);
            sha1Digest.update(s);
            byte[] d = sha1Digest.digest();

            /* convert to Base64 String */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(d,0,d.length);
            baos.write(s,0,s.length);
            byte b[] = baos.toByteArray();
            return "{SSHA}" + Base64.encode(b);

        } catch (NoSuchAlgorithmException nsae) {

            /* error */
            throw new NamingException("Unable to create SHA1 password hash (missing algorithm)");
            //Print.logError("Unable to create SHA-1 encoded password (missing algorithm)");
            //return null;
 
        }

    }
    
    /**
    *** Checks a password against the provided SSHA value
    **/
    public static boolean CheckPassword(String passwd, String sshaVal)
    {

        /* decode Base64 */
        if (StringTools.isBlank(sshaVal)) {
            return false;
        }

        /* remove "{SSHA}" prefix */
        if (sshaVal.startsWith("{SSHA}")) {
            sshaVal = sshaVal.substring("{SSHA}".length());
        }

        /* decode/check password */
        try {

            /* password bytes */
            byte p[] = (passwd != null)? passwd.getBytes() : new byte[0];
    
            /* decode Base64 */
            byte b[] = Base64.decode(sshaVal);
            if (b.length < SALT_LENGTH) {
                return false;
            }
            int pLen = b.length - SALT_LENGTH;
    
            /* extract salt */
            byte s[] = new byte[SALT_LENGTH];
            for (int i = 0; i < SALT_LENGTH; i++) {
                s[i] = b[pLen + i];
            }

            /* re-encode password */
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            sha1Digest.reset();
            sha1Digest.update(p);
            sha1Digest.update(s);
            byte[] d = sha1Digest.digest();

            /* compare d to b[0,pLen) */
            if (d.length != pLen) {
                // -- SHA-1 hash is different size
                return false;
            } else
            if (ListTools.diff(d,b,pLen) >= 0) {
                // -- not equals
                return false; 
            } else {
                // -- equals
                return true;
            }

        } catch (Base64.Base64DecodeException bde) {

            /* error */
            Print.logError("Unable to decode Base64 SHA-1 password");
            return false;
 

        } catch (NoSuchAlgorithmException nsae) {

            /* error */
            Print.logError("Unable to create SHA-1 encoded password (missing algorithm)");
            return false;
 
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Closes the specified directory context.
    *** @return True if closed (or already closed)
    **/
    private static boolean _CloseDirContext(DirContext dc)
    {
        if (dc != null) { 
            try { 
                dc.close();
                return true;
            } catch (Throwable th) {
                // -- unable to close?
                return false;
            } 
        } else {
            // -- already closed
            return true;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** [DEBUG] Print the ID/Value for the specified Attribute
    **/
    private static void PrintAttribute(Attribute attr)
        throws NamingException
    {
        if (attr != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(attr.getID() + " = ");
            for (int i = 0; i < attr.size(); i++) {
                if (i > 0) { sb.append(", "); }
                sb.append(attr.get(i));
            }
            Print.logInfo("  " + sb.toString());
        }
    }

    /**
    *** [DEBUG] Print all Attribute instances in specified Attributes container
    **/
    private static void PrintAttributes(String objName, Attributes attrs)
        throws NamingException
    {
        Print.logInfo("Object: " + objName);
        if (attrs != null) {
            NamingEnumeration e = attrs.getAll();
            while (e.hasMoreElements()) {
                Attribute attr = (Attribute)e.nextElement();
                LdapAuth.PrintAttribute(attr);
            }
        }
    }

    /**
    *** [DEBUG] Print Attributes in NamingEnumeration
    **/
    private static void PrintNamingEnumeration_Attributes(NamingEnumeration nenum)
        throws NamingException
    {
        if (nenum != null) {
            try {
                while (nenum.hasMoreElements()) {
                    SearchResult match = (SearchResult)nenum.nextElement();
                    Print.logInfo("Found " + match.getName() + ": ");
                    Attributes attrs = match.getAttributes();
                    LdapAuth.PrintAttributes(match.getName(), attrs);
                    Print.logInfo("---------------------------------------");
                }
            } catch (NamingException ne) {
                Print.logError("Error", ne);
            }
        }
    }

    /**
    *** [DEBUG] Print Object tree
    **/
    private static void PrintObjectTree(DirContext dirCtx, String name)
        throws NamingException
    {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration nenum = dirCtx.search(
            name,
            "(objectClass=*)", searchCtls);
        while (nenum.hasMoreElements()) {
            SearchResult match = (SearchResult)nenum.nextElement();
            Print.logInfo("Found " + match.getName() + " ... ");
        }
    }

    /**
    *** Print all properties found in the specified Properties container
    **/
    private static void PrintProperties(Properties p)
    {
        for (Iterator<?> i = p.keySet().iterator(); i.hasNext();) {
            Object k = i.next();
            Object v = p.get(k);
            Print.logInfo("Property: " + k + " ==> " + v);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* NOT USED */
    public static class __LdapDirContext
    {
        private DirContext dirContext = null;
        // --
        public __LdapDirContext(DirContext dirCtx) {
            this.dirContext = dirCtx;
        }
        // --
        public boolean isOpen() {
            return (this.dirContext != null)? true : false;
        }
        public boolean isClosed() {
            return !this.isOpen();
        }
        public boolean close() {
            if (this.isClosed()) {
                // -- already closed
                return true;
            } else {
                try { 
                    this.dirContext.close();
                    this.dirContext = null;
                    return true;
                } catch (Throwable th) {
                    // -- unable to close?
                    return false;
                } 
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private String              ldapProviderURL         = null; // "ldap://localhost:389"
    
    private String              ldapUsersOrgUnit        = null; // "ou=users,dc=example,dc=com"
    
    private String              ldapRootAuthType        = SECURITY_AUTHENTICATION; // "simple"
    private String              ldapRootDN              = null; // "cn=admin,dc=example,dc=com""
    private String              ldapRootPwd             = null; // "password"

    private InitialDirContext   rootDirContext          = null;

    /**
    *** Constructor<br>
    *** Properties loaded from runtime configuration.
    **/
    public LdapAuth()
    {
        super();
        String ldapURL = RTConfig.getString(PROP_LdapAuth_ProviderURL ,null);
        String usersOU = RTConfig.getString(PROP_LdapAuth_UsersContext,null);
        String secAuth = RTConfig.getString(PROP_LdapAuth_SecurityAuth,null);
        String rootDN  = RTConfig.getString(PROP_LdapAuth_RootDN      ,null);
        String rootPWD = RTConfig.getString(PROP_LdapAuth_RootPassword,null);
        this.setProviderURL(ldapURL);                   // "ldap://localhost:389"
        this.setUsersContext(usersOU);                  // "ou=Users,dc=example,dc=com"
        this.setSecurityAuthentication(secAuth);        // "simple"
        this.setRootDistinguishedName(rootDN, rootPWD); // "cn=admin,dc=example,dc=com"
    }

    /**
    *** Constructor
    **/
    public LdapAuth(
        String ldapURL, 
        String usersOU,
        String secAuth,
        String rootDN, String rootPWD)
    {
        super();
        this.setProviderURL(ldapURL);                   // "ldap://localhost:389"
        this.setUsersContext(usersOU);                  // "ou=Users,dc=example,dc=com"
        this.setSecurityAuthentication(secAuth);        // "simple"
        this.setRootDistinguishedName(rootDN, rootPWD); // "cn=admin,dc=example,dc=com"
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the LDAP provider URL
    **/
    private void setProviderURL(String url)
    {
        this.ldapProviderURL = StringTools.trim(url);
    }

    /**
    *** Gets the LDAP provider URL
    **/
    private String getProviderURL()
    {
        return this.ldapProviderURL;
    }

    /**
    *** Gets the LDAP provider URL
    **/
    public boolean hasProviderURL()
    {
        return !StringTools.isBlank(this.getProviderURL())? true : false;
    }

    // --------------------------------

    /**
    *** Sets the Users context name 
    **/
    private void setUsersContext(String usersOrgUnit)
    {
        this.ldapUsersOrgUnit = StringTools.trim(usersOrgUnit);
    }

    /**
    *** Gets the Users context name 
    **/
    private String getUsersContext()
    {
        return this.ldapUsersOrgUnit;
    }

    // --------------------------------

    /**
    *** Sets the SecurityAuthentication
    **/
    private void setSecurityAuthentication(String secAuth)
    {
        this.ldapRootAuthType = StringTools.blankDefault(secAuth, SECURITY_AUTHENTICATION);
    }

    /**
    *** Gets the SecurityAuthentication
    **/
    private String getSecurityAuthentication()
    {
        if (StringTools.isBlank(this.ldapRootAuthType)) {
            this.setSecurityAuthentication(null); // set default
        }
        return this.ldapRootAuthType;
    }

    // --------------------------------

    /**
    *** Sets the Root DN
    **/
    private void setRootDistinguishedName(String rootUserDN, String rootPass)
    {
        this.ldapRootDN  = StringTools.trim(rootUserDN);        // "cn=admin,dc=example,dc=com"
        this.ldapRootPwd = (rootPass != null)? rootPass : null; // "ldap"
    }

    /**
    *** Gets the Root DN
    **/
    private String getRootDistinguishedName()
    {
        return this.ldapRootDN;
    }

    /**
    *** Gets the Root password
    **/
    private String getRootPassword()
    {
        return this.ldapRootPwd;
    }

    // ------------------------------------------------------------------------

    /**
    *** Create InitialDirContext
    **/
    private InitialDirContext _createInitialDirContext(String DN, String pwd)
        throws NamingException
    {
        try {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            p.put(Context.PROVIDER_URL           , this.getProviderURL());
            if (this.getProviderURL().toUpperCase().startsWith("LDAPS://")) { // SSL
                p.put(Context.SECURITY_PROTOCOL, "ssl");
                //p.put("java.naming.ldap.factory.socket", "??");
            }
            p.put(Context.SECURITY_AUTHENTICATION, this.getSecurityAuthentication()); // "simple"
            p.put(Context.SECURITY_PRINCIPAL     , DN);
            p.put(Context.SECURITY_CREDENTIALS   , ((pwd != null)? pwd : ""));
            // -- DEBUG
            //LdapAuth.PrintProperties(p);
            // --
            return new InitialDirContext(p);
        } catch (AuthenticationNotSupportedException anse) {
            // -- this authentication mechanism is not supported
            Print.logException("Not supported", anse);
            throw anse;
        } catch (AuthenticationException ae) {
            // -- Invalid Credentials
            Print.logError("Not authorized: " + ae);
            throw ae;
        } catch (NamingException ne) {
            // -- other error
            throw ne;
        }
    }

    /**
    *** Open/Get main root directory context
    *** Does not return null
    **/
    private InitialDirContext _getRootDirContext()
        throws NamingException
    {
        if (this.rootDirContext == null) {
            String rootDN = this.getRootDistinguishedName();
            String rootPW = this.getRootPassword();
            this.rootDirContext = this._createInitialDirContext(rootDN, rootPW);
            //Print.logInfo("Opened RootDirContext ...");
        }
        return this.rootDirContext; // may still be null
    }

    /**
    *** Close main root directory context
    **/
    public void closeRootDirContext()
    {
        if (this.rootDirContext != null) {
            LdapAuth._CloseDirContext(this.rootDirContext);
            this.rootDirContext = null;
            //Print.logInfo("... Closed RootDirContext");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the UID DN name
    **/
    private String createUidName(UserInformation user) 
    {
        // -- "uid=smith,ou=users,dc=example,dc=com"
        String uid = LdapAuth.CreateUID(user);
        return "uid="+uid+","+this.getUsersContext(); 
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Create user entry
    **/
    public void createUser(UserInformation user, String pass)
        throws NamingException // NameAlreadyBoundException
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        }

        /* create user */
        DirContext dirCtx = this._getRootDirContext();
        String uid      = LdapAuth.CreateUID(user);
        String name     = this.createUidName(user);
        String hashPass = LdapAuth.EncodePassword(pass); // throws NamingException
        Attributes attributes = new BasicAttributes();
        Attribute objClass = new BasicAttribute("objectClass");
            objClass.add("organizationalPerson");
            objClass.add("person");
            objClass.add("top");
            objClass.add("uidObject"); // provides "uid=" key
        attributes.put(objClass);
        attributes.put(new BasicAttribute("sn"          , user.getAccountID()));
        attributes.put(new BasicAttribute("cn"          , user.getUserID()));
        attributes.put(new BasicAttribute("uid"         , uid));
        attributes.put(new BasicAttribute("userPassword", hashPass));
        Print.logInfo("Creating user: " + name);
        dirCtx.createSubcontext(name, attributes); // may throw NameAlreadyBoundException

    }

    // ------------------------------------------------------------------------

    /**
    *** Remove user entry
    **/
    public void removeUser(UserInformation user) 
        throws NamingException 
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        }

        /* remove user */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        dirCtx.destroySubcontext(name);

    }

    // ------------------------------------------------------------------------

    /**
    *** Updates user entry password attribute
    **/
    public void updateUserPassword(UserInformation user, String pass, boolean create)
        throws NamingException 
    {
        String hashPass = LdapAuth.EncodePassword(pass); // throws NamingException
        try {
            this.updateAttribute(user, "userPassword", hashPass);
        } catch (NameNotFoundException nnfe) {
            if (create) {
                this.createUser(user, pass);
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Updates an attribute in the specified user entry
    **/
    private void updateAttribute(UserInformation user, String attrName, Object attrValue) 
        throws NamingException // NameNotFoundException
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        } else
        if (StringTools.isBlank(attrName)) {
            throw new NamingException("Attribute name is blank/null");
        }

        /* update attribute */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        Attribute a = new BasicAttribute(attrName, attrValue);
        Print.logInfo("Updating Attribute ["+name+"]: " + attrName + " ==> " + attrValue);
        dirCtx.modifyAttributes(name, 
            new ModificationItem[] { new ModificationItem(DirContext.REPLACE_ATTRIBUTE,a) }
            );

    }

    /**
    *** Removes the specified attribute name from the specified user entry
    **/
    private void removeAttribute(UserInformation user, String attrName)
        throws NamingException 
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        } else
        if (StringTools.isBlank(attrName)) {
            throw new NamingException("Attribute name is blank/null");
        }

        /* remove attribute */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        Attribute a = new BasicAttribute(attrName);
        dirCtx.modifyAttributes(name, 
            new ModificationItem[] { new ModificationItem(DirContext.REMOVE_ATTRIBUTE,a) }
            );

    }

    /**
    *** Creates the specified attribute name in the specified user entry
    **/
    private void createAttribute(UserInformation user, String attrName, Object attrValue) 
        throws NamingException 
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        } else
        if (StringTools.isBlank(attrName)) {
            throw new NamingException("Attribute name is blank/null");
        }

        /* create attribute */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        Attribute a = new BasicAttribute(attrName, attrValue);
        dirCtx.modifyAttributes(name, 
            new ModificationItem[] { new ModificationItem(DirContext.ADD_ATTRIBUTE,a) }
            );

    }

    /**
    *** Gets the specified attribute name value from the specified user entry
    **/
    private Attributes getAttributes(UserInformation user)
        throws NamingException 
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        }

        /* get attribute */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        return dirCtx.getAttributes(name);

    }

    /**
    *** Gets the specified attribute name value from the specified user entry
    **/
    private Object getAttribute(UserInformation user, String attrName) 
        throws NamingException 
    {

        /* valid user-info? */
        if (user == null) {
            throw new NamingException("Account/User is null");
        } else
        if (StringTools.isBlank(attrName)) {
            throw new NamingException("Attribute name is blank/null");
        }

        /* get attribute */
        DirContext dirCtx = this._getRootDirContext();
        String name = this.createUidName(user);
        Attributes attrs = dirCtx.getAttributes(name);
        Attribute a = attrs.get(attrName);
        return (a != null)? a.get() : null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /** NOT USED
    *** Search for the specified users distinguished-name (DN)
    **/
    private String __searchUserDN(String userUID, String attrUID)
        throws NamingException
    {

        /* validate userUID */
        if (StringTools.isBlank(userUID)) {
            return null;
        } 

        /* search attribute */
        attrUID = StringTools.trim(attrUID);
        if (StringTools.isBlank(attrUID)) {
            attrUID = "uid";
        }

        /* get root dir context */
        InitialDirContext rootDirCtx = this._getRootDirContext();
        if (rootDirCtx == null) {
            return null;
        }

        /* search */
        // -- search controls
        String attrFilter[] = { attrUID };
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(attrFilter);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // -- search
        String searchStr = "("+attrUID+"="+EscapeUID(userUID)+")";
        String usersOU = this.getUsersContext();
        NamingEnumeration<SearchResult> results = rootDirCtx.search(usersOU, searchStr, sc);
        if (results.hasMore()) {
            SearchResult result = results.next();
            String userDN = result.getNameInNamespace();
            return userDN;
        }
        // -- not found
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Authenticate the specified account/user
    *** (authentication performed by password hash comparison)
    **/
    public boolean checkUserPassword(UserInformation user, String pass)
    {
      //return this.checkUserPassword_auth(user, pass);
        return this.checkUserPassword_hash(user, pass);
    }

    /**
    *** Authenticate the specified account/user
    *** (authentication performed by password hash comparison)
    **/
    public boolean checkUserPassword_hash(UserInformation user, String pass)
    {

        /* valid user-info? */
        if (user == null) {
            Print.logError("Account/User is null");
            return false;
        }

        /* get password hash String */
        String hashPass;
        try {
            Object hashPwdB = this.getAttribute(user, "userPassword");
            if (!(hashPwdB instanceof byte[])) {
                Print.logError("HashPassword is not a byte[]: " + StringTools.className(hashPwdB));
                return false;
            }
            hashPass = StringTools.toStringValue((byte[])hashPwdB);
        } catch (NamingException ne) {
            Print.logError("NamingException: " + ne);
            return false;
        }

        /* check password */
        if (!LdapAuth.CheckPassword(pass,hashPass)) {
            Print.logError("Password does not match HashPassword: " + hashPass);
            return false;
        } else {
            Print.logError("HashPassword match: " + hashPass);
            return true;
        }

    }

    /**
    *** Authenticate the specified account/user
    *** (authentication performed by LDAP authentication)
    **/
    public boolean checkUserPassword_auth(UserInformation user, String pass)
    {

        /* valid user-info? */
        if (user == null) {
            Print.logError("Account/User is null");
            return false;
        }

        /* try to log-in as user */
        DirContext dirCtx = null;
        try {
            String userDN = this.createUidName(user);
            Print.logInfo("UserDN: " + userDN);
            dirCtx = this._createInitialDirContext(userDN, pass);
            return true;
        } catch (NoPermissionException npe) {
            // -- Insufficient Access Rights
            Print.logException("No Access?", npe);
            return false;
        } catch (AuthenticationException ae) {
            // -- Invalid Credentials
            Print.logException("Invalid Credentials?", ae);
            return false;
        } catch (NamingException ne) {
            // -- not found / not authorized
            Print.logException("Not found?", ne);
            return false;
        } catch (Throwable th) {
            // -- unknown error
            Print.logException("Unknown?", th);
            return false;
        } finally {
            LdapAuth._CloseDirContext(dirCtx);
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /** NOT USED
    **/
    public boolean __updateUserDN(String userDN, String newPass)
    {
        try {
            InitialDirContext idc = this._getRootDirContext(); // _createInitialDirContext(userDN, oldPass);
            byte bp[] = ("\"" + ((newPass != null)? newPass : "") + "\"").getBytes("UTF-16LE");
            BasicAttribute ba = new BasicAttribute("UnicodePwd", bp); // "userPassword"
            ModificationItem mods[] = { new ModificationItem(DirContext.REPLACE_ATTRIBUTE,ba) };
            idc.modifyAttributes(userDN, mods);
            return true;
        } catch (UnsupportedEncodingException uee) {
            // -- "UTF-16LE" not supported
            return false;
        } catch (AuthenticationException ae) {
            // -- not authorized
            Print.logError("Not authorized to update password", ae);
            return false;
        } catch (NamingException ne) {
            Print.logError("Error", ne);
            return false;
        } finally {
            //this.closeRootDirContext();
        }
    }

    /** NOT USED
    **/
    private boolean __setUserPassword(String userName, String userPass)
    {
        // http://java2db.com/jndi-ldap-programming/add-new-entry-to-ldap-using-java-jndi
        Print.logInfo("Setting user password: " + userName + " ==> " + userPass + " ...");
        try {
            InitialDirContext idc = this._getRootDirContext(); // _createInitialDirContext(userDN, oldPass);
            String hashPass = LdapAuth.EncodePassword(userPass); // throws NamingException
            BasicAttributes ba = new BasicAttributes(true);
            ba.put(new BasicAttribute("cn"          , userName)); // DeviceID?
            ba.put(new BasicAttribute("sn"          , userName)); // AccountID?
            ba.put(new BasicAttribute("userPassword", hashPass));
            ba.put(new BasicAttribute("objectClass" , "organizationalUnit")); // 
            ba.put(new BasicAttribute("ou"          , "users" )); // 
            //Attribute oc = new BasicAttribute("objectClass", "organizationalUnit");
            //oc.add("person");
            //oc.add("publicuser");
            //ba.put(oc);
            String userDN = "uid="+userName+","+this.getUsersContext();
            Print.logInfo("UserDN: " + userDN);
            idc.createSubcontext(userDN, ba);
            Print.logInfo("... successful");
            return true;
        } catch (AuthenticationException ae) {
            // -- not authorized
            Print.logError("Not authorized to update password: " + ae);
            return false;
        } catch (NamingException ne) {
            Print.logException("Error", ne);
            return false;
        } finally {
            //this.closeRootDirContext();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
