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
//  2012/04/03  Martin D. Flynn
//     -Initial release
//  2015/05/03  Martin D. Flynn
//     -Added support for PLAIN encoding, but also check for MD5 hash ("plainmd5")
//  2016/09/01  Martin D. Flynn
//     -Added check for previous passwords to "validateNewPassword(...)"
//     -Added parameter to "validateNewPassword" to return the reason for invalid password
//  2020/02/19  GTS Development Team
//     -Added support for SHA1 encoding [2.6.7-B18b]
//     -Added "maximumPasswordLength" [2.6.7-B18b]
//     -Chaged "checkPassword" method signature [2.6.7-B20b]
//     -Removed critical dependencies on StringTools/ListTools
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.Locale;
import java.math.BigInteger;
import java.io.ByteArrayOutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

public class GeneralPasswordHandler
    implements PasswordHandler
{

    // ------------------------------------------------------------------------

    /* debug password handler */
    public static final String PROP_debugCheckPassword          = "debugCheckPassword";

    /* password encoding */
    public static final String PROP_passwordEncoding            = "passwordEncoding";
    public static final String PROP_encodingHashSalt            = "encodingHashSalt";

    /* LDAP support (EXPERIMENTAL: may not be currently supported) */
    public static final String PROP_ldap_enabled                = "ldap.enabled";                // "false"|"only"|"secondary"
    public static final String PROP_ldap_providerURL            = "ldap.providerURL";            // "ldap://127.0.0.1:389"
    public static final String PROP_ldap_usersDirContext        = "ldap.usersDirContext";        // "ou=users,dc=example,dc=com"
    public static final String PROP_ldap_rootDistinguishedName  = "ldap.rootDistinguishedName";  // "cn=admin,dc=example,dc=com"
    public static final String PROP_ldap_rootPassword           = "ldap.rootPassword";           // "ldap"
    public static final String PROP_ldap_securityAuth           = "ldap.securityAuthentication"; // "simple"

    /* user password restrictions */
    public static final String PROP_maximumPasswordAgeSeconds   = "maximumPasswordAgeSeconds";
    public static final String PROP_requiredUniquePassword      = "requiredUniquePassword";      // count: number of unique new passwords
    public static final String PROP_failedLoginMaximumAttempts  = "failedLoginMaximumAttempts";
    public static final String PROP_failedLoginAttemptInterval  = "failedLoginAttemptInterval";
    public static final String PROP_failedLoginSuspendInterval  = "failedLoginSuspendInterval";

    /* password attributes */
    public static final String PROP_minimumPasswordLength       = "minimumPasswordLength";
    public static final String PROP_maximumPasswordLength       = "maximumPasswordLength";
    public static final String PROP_specialCharacters           = "specialCharacters";
    public static final String PROP_minimumLowerAlphaChars      = "minimumLowerAlphaChars";
    public static final String PROP_minimumUpperAlphaChars      = "minimumUpperAlphaChars";
    public static final String PROP_minimumAlphaChars           = "minimumAlphaChars";
    public static final String PROP_minimumDigitChars           = "minimumDigitChars";
    public static final String PROP_minimumSpecialChars         = "minimumSpecialChars";
    public static final String PROP_minimumNonAlphaChars        = "minimumNonAlphaChars";
    public static final String PROP_minimumCategories           = "minimumCategories";
    public static final String PROP_passwordFormatDescription   = "passwordFormatDescription";

    /* list of GeneralPasswordHandler properties */
    private static final RTKey.Entry PROP_Entry[] = {
        new RTKey.Entry(PROP_passwordEncoding           ,              "plain", "Password Encoding"),
        new RTKey.Entry(PROP_encodingHashSalt           ,                   "", "Initial Hash Salt"),
        new RTKey.Entry(PROP_minimumPasswordLength      ,                    1, "Minimum Password Length"),
        new RTKey.Entry(PROP_maximumPasswordLength      ,                    0, "Maximum Password Length"),
        new RTKey.Entry(PROP_maximumPasswordAgeSeconds  ,                   0L, "Maximum Password Age (seconds)"),
        new RTKey.Entry(PROP_requiredUniquePassword     ,                    1, "Required Unique Password Count"),
        new RTKey.Entry(PROP_specialCharacters          , "!@#$%^&*()_+-:;.?/", "Special Characters"),
        new RTKey.Entry(PROP_minimumLowerAlphaChars     ,                    0, "Minimum Lower-Case Alpha Characters"),
        new RTKey.Entry(PROP_minimumUpperAlphaChars     ,                    0, "Minimum Upper-Case Alpha Characters"),
        new RTKey.Entry(PROP_minimumAlphaChars          ,                    0, "Minimum Alpha Characters"),
        new RTKey.Entry(PROP_minimumDigitChars          ,                    0, "Minimum Digit Characters"),
        new RTKey.Entry(PROP_minimumSpecialChars        ,                    0, "Minimum Special Characters"),
        new RTKey.Entry(PROP_minimumNonAlphaChars       ,                    0, "Minimum Non-Alpha Characters"),
        new RTKey.Entry(PROP_minimumCategories          ,                    0, "Minimum Number of Categories"),
        new RTKey.Entry(PROP_debugCheckPassword         ,                false, "Debug 'checkPassword'"),
        new RTKey.Entry(PROP_failedLoginMaximumAttempts ,                    5, "Maximum Failed Login Attempts"),
        new RTKey.Entry(PROP_failedLoginAttemptInterval ,                 120L, "Failed Login Attempt Interval"),
        new RTKey.Entry(PROP_failedLoginSuspendInterval ,                 180L, "Failed Login Suspend Interval"),
        new RTKey.Entry(PROP_passwordFormatDescription  ,                   "", "Password Format Error Message"),
    };

    private static RTProperties DefaultProps = null;

    static {
        DefaultProps = new RTProperties();
        for (int i = 0; i < PROP_Entry.length; i++) {
            String rtKey = PROP_Entry[i].getKey();
            Object rtVal = PROP_Entry[i].getDefault();
            DefaultProps.setProperty(rtKey, rtVal);
        }
    }

    // ------------------------------------------------------------------------

    public static final String  ENC_PLAIN       = "plain";
    
    // -- MD5 and SHA1 hashed passwords MUST be this length
    // -  if PLAIN passwords are used, they must be LESS-THAN this length
    public static final int     HASH_LEN        = 32; // characters

    // -- SHA1: (20+4)-bytes ==> 32-char Base64
    public static final String  SHA1_DIGEST     = "SHA-1";
    public static final String  ENC_SHA1        = "sha1";
    public static final String  ENC_SHA1PLAIN   = "sha1plain";
    public static final String  ENC_PLAINSHA1   = "plainsha1"; // may not be fully supported

    // -- MD5: 16-bytes ==> 32-char HEX
    public static final String  MD5_DIGEST      = "MD5";
    public static final String  ENC_MD5         = "md5";
    public static final String  ENC_MD5PLAIN    = "md5plain";
    public static final String  ENC_PLAINMD5    = "plainmd5"; // may not be fully supported

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public enum LdapMode implements EnumTools.StringLocale, EnumTools.IntValue {
        DISABLED  ( 0, I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.LdapMode.disabled" ,"Disabled" )),
        ONLY      (10, I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.LdapMode.only"     ,"Only"     )),
        ALTERNATE (20, I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.LdapMode.alternate","Alternate"));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        LdapMode(int v, I18N.Text a)        { vv = v; aa = a; }
        public int     getIntValue()        { return vv; }
        public String  toString()           { return aa.toString(); }
        public String  toString(Locale loc) { return aa.toString(loc); }
        public boolean isDisabled()         { return this.equals(DISABLED); }
        public boolean isOnly()             { return this.equals(ONLY); }
        public boolean isAlternate()        { return this.equals(ALTERNATE); }
        public boolean isType(int type)     { return this.getIntValue() == type; }
    };

    /**
    *** Gets the LdapMode enum value for the specified name
    *** @param code The name of the LdapMode (one of "disabled", "only", "alternate")
    *** @param dft  The default LdapMode if the specified name is invalid.
    *** @return The LdapMode, or the specified default if the name is invalid
    **/
    public static LdapMode getLdapMode(String code, LdapMode dft)
    {
        return EnumTools.getValueOf(LdapMode.class, code, dft);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Exception 
    **/
    public static class PasswordEncodingException
        extends RuntimeException
    {
        public PasswordEncodingException(String msg) {
            super(msg);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- local definition of StringTools/ListTools methods, to remove dependencies 
    // -- on external classes in secure/critical sections.

    /**
    *** Trim the leading/trailing blanks from the specified String argument.
    **/
    private static String Trim(String s) // StringTools.trim(s)
    {
        return (s != null)? s.trim() : "";
    }

    /**
    *** Parse int from String
    **/
    private static int ParseInt(String s, int dft) // StringTools.parseInt(s,dft)
    {
        // -- extract leading digits from 's'
        s = Trim(s);
        int e = s.startsWith("-")? 1 : 0;
        while ((e < s.length()) && Character.isDigit(s.charAt(e))) { e++; }
        String n = (e < s.length())? s.substring(0,e) : s;
        // -- parse/return
        if (!n.equals("")) {
            try {
                return Integer.parseInt(n);
            } catch (NumberFormatException nfe) {
                // -- unlikely, since 'n' guaranteed to contain only digits
            }
        }
        return dft;
    }

    /**
    *** Parse hex-bytes from String
    **/
    private static final String HEX = "0123456789ABCDEF";
    private static byte[] ParseHex(String s, byte dft[]) // StringTools.parseHex(s,dft)
    {
        s = Trim(s).toUpperCase();
        if (s.startsWith("0X")) {
            s = s.substring("0X".length());
        }
        // -- remove any trailing invalid characters
        for (int i = 0; i < s.length(); i++) {
            if (HEX.indexOf(s.charAt(i)) < 0) {
                s = s.substring(0, i);
                break;
            }
        }
        // -- blank?
        if (s.equals("")) {
            return dft;
        }
        // -- right justify to even number of characters
        if ((s.length() & 1) == 1) { s = "0" + s; } // right justified
        // -- parse/return
        byte rtn[] = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            int c1 = HEX.indexOf(s.charAt(i  )); // guaranteed value
            int c2 = HEX.indexOf(s.charAt(i+1)); // guaranteed value
            rtn[i/2] = (byte)(((c1 << 4) & 0xF0) | (c2 & 0x0F));
        }
        return rtn;
    }

    /**
    *** Diff byte arrays
    **/
    private static int Diff(byte a1[], byte a2[], int len) // ListTools.diff(a1,a2,len)
    {
        if ((a1 == null) && (a2 == null)) {
            return -1; // equals
        } else
        if ((a1 == null) || (a2 == null)) {
            return 0; // diff on first element
        } else {
            int n1 = a1.length, n2 = a2.length, i = 0;
            if (len < 0) { len = (n1 >= n2)? n1 : n2; } // larger of two lengths
            for (i = 0; (i < n1) && (i < n2) && (i < len); i++) {
                if (a1[i] != a2[i]) { 
                    // -- return index of differing elements
                    return i; 
                }
            }
            return (i < len)? i : -1;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String          name            = "";
    private RTProperties    rtProps         = null;
    
    private String          saveHash        = "";  // ENC_PLAIN | ENC_SHA1 | ENC_MD5
    private String          altHash         = "";  // ENC_PLAIN | ENC_SHA1 | ENC_MD5
    private byte            hashSalt[]      = null;

    private boolean         debugCheckPass  = false;

    private LdapMode        ldapMode        = LdapMode.DISABLED;
    /**
    *** Default Constructor
    **/
    public GeneralPasswordHandler()
        throws PasswordEncodingException
    {
        this(null,null);
    }

    /**
    *** Constructor
    *** @param rtp  The property settings for this instance
    **/
    public GeneralPasswordHandler(RTProperties rtp)
        throws PasswordEncodingException
    {
        this(null,rtp);
    }

    /**
    *** Constructor
    *** @param name This password handler name
    *** @param rtp  The property settings for this instance
    **/
    public GeneralPasswordHandler(String name, RTProperties rtp)
        throws PasswordEncodingException
    {

        /* set vars */
        this.rtProps = (rtp != null)? rtp : new RTProperties();
        this.name = (name != null)? name.trim() : "";

        /* check encoding */
        String encType = Trim(this.getString(PROP_passwordEncoding,"")); // non-null
        if (encType.equalsIgnoreCase(ENC_SHA1)) {
            // -- save as SHA1 encoding, do not check plain passwords
            this.saveHash = ENC_SHA1; // save-as
            this.altHash  = ENC_SHA1; // alt-check-as
        } else
        if (encType.equalsIgnoreCase(ENC_SHA1PLAIN)) {
            // -- save as SHA1 encoding, also check plain passwords
            this.saveHash = ENC_SHA1;  // save-as
            this.altHash  = ENC_PLAIN; // alt-check-as
        } else
        if (encType.equalsIgnoreCase(ENC_PLAINSHA1)) {
            // -- save as PLAIN encoding, also check SHA1-hashed passwords
            this.saveHash = ENC_PLAIN; // save-as
            this.altHash  = ENC_SHA1;  // check-as
        } else
        if (encType.equalsIgnoreCase(ENC_MD5)) {
            // -- save as MD5 encoding, do not check plain passwords
            this.saveHash = ENC_MD5;   // save-as
            this.altHash  = ENC_MD5;   // check-as
        } else
        if (encType.equalsIgnoreCase(ENC_MD5PLAIN)) {
            // -- save as MD5 encoding, also check plain passwords
            this.saveHash = ENC_MD5;   // save-as
            this.altHash  = ENC_PLAIN; // check-as
        } else
        if (encType.equalsIgnoreCase(ENC_PLAINMD5)) {
            // -- save as PLAIN encoding, also check MD5-hashed passwords
            this.saveHash = ENC_PLAIN; // save-as
            this.altHash  = ENC_MD5;   // check-as
        } else
        if (encType.equalsIgnoreCase(ENC_PLAIN) ||
            encType.equalsIgnoreCase("none")    ||
            encType.equals("")                    ) {
            // -- save as PLAIN encoding, do not check SHA1/MD5-hashed passwords
            this.saveHash = ENC_PLAIN; // save-as
            this.altHash  = ENC_PLAIN; // (null?) check-as
        } else {
            // -- unrecognized encoding
            throw new PasswordEncodingException("Invalid Encoding: " + encType);
        }

        /* encoding hash salt (SHA1/MD5 only) */
        String hashSaltS = Trim(this.getString(PROP_encodingHashSalt,null)); // non-null
        if ((hashSaltS != null) && !hashSaltS.equals("")) {
            if (hashSaltS.startsWith("0x")) {
                this.hashSalt = ParseHex(hashSaltS,null);
            } else {
                this.hashSalt = hashSaltS.getBytes();
            }
        } else {
            this.hashSalt = null;
        }

        /* LDAP password authentication mode (EXPERIMENTAL) */
        String ldapModeS = Trim(this.getString(PROP_ldap_enabled,"")); // non-null
        if (ldapModeS.equals("")) {
            this.ldapMode = LdapMode.DISABLED;
        } else {
            LdapMode ldm = GeneralPasswordHandler.getLdapMode(ldapModeS,null);
            if (ldm != null) {
                this.ldapMode = ldm;
            } else {
                Print.logWarn("LDAP mode not recognized: " + ldapModeS + " (setting to DISABLED)");
                this.ldapMode = LdapMode.DISABLED;
            } 
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the name of this PasswordHandler
    *** @return The name of this PasswordHandler
    **/
    public String getName()
    {
        if ((this.name != null) && !this.name.equals("")) {
            return this.name;
        } else {
            return this.getPasswordEncodingString();
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the Save-As encoding used for this PasswordHandler (non-null)
    **/
    private String getSaveEncoding()
    {
        return this.saveHash;
    }

    /**
    *** Returns true if the current encoding matches the specified value
    **/
    private boolean isSaveEncoding(String enc)
    {
        return this.saveHash.equalsIgnoreCase(enc);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the Save-As encoding used for this PasswordHandler (non-null)
    **/
    private String getAltEncoding()
    {
        return this.altHash;
    }

    /**
    *** Returns true if the current encoding matches the specified value
    **/
    private boolean isAltEncoding(String enc)
    {
        if ((enc == null) || enc.equals("")) {
            return ENC_PLAIN.equalsIgnoreCase(this.altHash);
        } else {
            return enc.equalsIgnoreCase(this.altHash);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if ENC_PLAIN is ebabled
    **/
    private boolean isCheckPlain()
    {
        return (this.isSaveEncoding(ENC_PLAIN) || this.isAltEncoding(ENC_PLAIN))? true : false;
    }

    /**
    *** Returns the encoding as a displayable string 
    *** (this differs from "getSaveEncoding()" in that if the encoding was originally
    *** "[sha1|md5]plain", then the returned string will be "[sha1|md5]plain" - where "getSaveEncoding()"
    *** would just return "sha1|md5")
    **/
    public String getPasswordEncodingString()
    {
        String enc = this.getSaveEncoding(); // this.saveHash
        switch (enc) {
            case ENC_PLAIN :
                switch (Trim(this.getAltEncoding())) {
                    case ENC_SHA1 : return ENC_PLAINSHA1;
                    case ENC_MD5  : return ENC_PLAINMD5;
                    case ENC_PLAIN: return ENC_PLAIN;
                    case ""       : return ENC_PLAIN; // should not occur
                    default       : return ENC_PLAIN; // should not occur
                }
            case ENC_SHA1 :
                return this.isAltEncoding(ENC_PLAIN)? ENC_SHA1PLAIN : ENC_SHA1;
            case ENC_MD5 :
                return this.isAltEncoding(ENC_PLAIN)? ENC_MD5PLAIN : ENC_MD5;
            default : // should not occur
                return enc;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the LdapMode enum
    *** @return The LdapMode enum
    **/
    public LdapMode getLdapMode()
    {
        return this.ldapMode; // non null
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the value of the Object property key
    **/
    protected Object getProperty(String key, Object dft)
    {
        if (this.rtProps.hasProperty(key)) {
            return this.rtProps.getProperty(key, dft);
        } else {
            return DefaultProps.getProperty(key, dft);
        }
    }

    /**
    *** Returns true if the property is defined
    ** @param key  The property to check
    **/
    protected boolean hasProperty(String key)
    {
        if (this.rtProps.hasProperty(key) || DefaultProps.hasProperty(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
    *** Returns the value of the String property key
    **/
    protected String getString(String key, String dft)
    {
        if (this.rtProps.hasProperty(key)) {
            return this.rtProps.getString(key, dft);
        } else {
            return DefaultProps.getString(key, dft);
        }
    }

    /**
    *** Returns the value of the Long property key
    **/
    protected long getLong(String key, long dft)
    {
        if (this.rtProps.hasProperty(key)) {
            return this.rtProps.getLong(key, dft);
        } else {
            return DefaultProps.getLong(key, dft);
        }
    }

    /**
    *** Returns the value of the Long property key
    **/
    protected int getInt(String key, int dft)
    {
        if (this.rtProps.hasProperty(key)) {
            return this.rtProps.getInt(key, dft);
        } else {
            return DefaultProps.getInt(key, dft);
        }
    }

    /**
    *** Returns the value of the Long property key
    **/
    protected boolean getBoolean(String key, boolean dft)
    {
        if (this.rtProps.hasProperty(key)) {
            return this.rtProps.getBoolean(key, dft);
        } else {
            return DefaultProps.getBoolean(key, dft);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the password MessageDigest hash bytes
    **/
    private byte[] getPasswordHash(String enc, String pass, byte salt[])
    {
        try {
            MessageDigest md = MessageDigest.getInstance(enc);
            md.reset();
            // -- add global hash salt
            if ((this.hashSalt != null) && (this.hashSalt.length > 0)) {
                md.update(this.hashSalt, 0, this.hashSalt.length);
            }
            // -- add password bytes
            if (pass != null) {
                byte p[] = pass.getBytes();
                md.update(p, 0, p.length);
            }
            // -- add extra salt
            if ((salt != null) && (salt.length > 0)) {
                md.update(salt, 0, salt.length);
            }
            // return hash bytes
            return md.digest();
        } catch (NoSuchAlgorithmException nsae) {
            Print.logError("MessageDigest algorithm not found: " + enc, nsae);
            return null;
        }
    }

    // ------------------------------------------------------------------------

    private static final int SHA1_SALT_LENGTH = 4;

    /**
    *** Return SHA1 hash of specified string
    **/
    private String sha1Hash(String passwd)
    {

        /* SHA1 salt */
        byte salt[] = new byte[SHA1_SALT_LENGTH];
        (new SecureRandom()).nextBytes(salt);

        /* encode password */
        byte hash[] = this.getPasswordHash(SHA1_DIGEST, passwd, salt);
        if (hash == null) {
            return null; // error message already displayed
        }

        /* convert to Base64 String */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hash,0,hash.length); // 20-bytes
        baos.write(salt,0,salt.length); //  4-bytes
        byte b[] = baos.toByteArray();  // 24-bytes
        return Base64.encode(b);        // 32-char

    }

    // -------------------------------

    /**
    *** Return MD5 hash of specified string
    **/
    private String md5Hash(String pass)
    {

        /* encode password */
        byte d[] = this.getPasswordHash(MD5_DIGEST, pass, null);
        if (d == null) {
            return null; // error message already displayed
        }

        /* convert to Base64 String */
        //Print.logInfo("MD5 ["+d.length+"]: " + StringTools.toHexString(d)); // StringTools ok here
        return (new BigInteger(1,d)).toString(16);

    }

    // -------------------------------

    /**
    *** Encode password
    *** @param pass The password to encode
    *** @return The encoded password (may be null if encoding algorithm not found)
    **/
    public String encodePassword(String pass) 
    {
        if ((pass == null) || pass.equals("")) { // spaces are significant, do not use "isBlank"
            // -- return password as-is
            return pass; // blank encodes to blank
        } else
        if (this.isSaveEncoding(ENC_SHA1)) { // this.saveHash
            // -- encode as SHA1 hash
            return this.sha1Hash(pass); // SHA1(pass + salt)
        } else
        if (this.isSaveEncoding(ENC_MD5)) { // this.saveHash
            // -- encode as MD5 hash
            return this.md5Hash(pass);  // MD5(pass)
        } else
        if (this.isSaveEncoding(ENC_PLAIN)) { // this.saveHash
            // -- return password as-is
            return pass; // leave as-is
        } else {
            Print.logStackTrace("Invalid password encoding: " + this.saveHash);
            return pass;
        }
    }

    /**
    *** Decode password
    *** @param pass  The password to decode
    *** @return The decoded password, or null if the password cannot be decoded
    **/
    public String decodePassword(String pass) 
    {
        if ((pass == null) || pass.equals("")) { // spaces are significant, do not use "isBlank"
            // -- return password as-is
            return pass; // blank encodes to blank
        } else
        if (this.isSaveEncoding(ENC_SHA1)) { // this.saveHash
            // -- SHA1 cannot be decoded
            return null; // hash not decodable
        } else
        if (this.isSaveEncoding(ENC_MD5)) { // this.saveHash
            // -- MD5 cannot be decoded
            return null; // hash not decodable
        } else
        if (this.isSaveEncoding(ENC_PLAIN)) { // this.saveHash
            // -- return password as-is
            return pass; // leave as-is
        } else {
            Print.logStackTrace("Invalid password encoding: " + this.saveHash);
            return pass;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets debug logging for "checkPassword"
    *** @param debugLog  True to enable debug logging, false to disable
    **/
    public void setDebugCheckPassword(boolean debugLog)
    {
        this.rtProps.setBoolean(PROP_debugCheckPassword, debugLog);
    }

    /**
    *** Gets debug logging for "checkPassword"
    *** @return True if "checkPassword" debug logging enabled, false otherwise
    **/
    public boolean getDebugCheckPassword()
    {
        return this.getBoolean(PROP_debugCheckPassword, false);
    }

    /**
    *** Returns true if the entered password matches the password saved in the table
    *** @param accountID    The account ID
    *** @param userID       The user ID
    *** @param enteredPass  The Account/User entered password
    *** @param tablePass    The password saved in the table (possibly encrypted)
    *** @return True if password match, false otherwise
    **/
    public boolean checkPassword(UserInformation user, String enteredPass)
    {
        boolean LOG = this.getDebugCheckPassword();

        /* no user? */
        if (user == null) {
            // -- no user, password check failed
            if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", NullUser"); }
            return false;
        }

        /* LDAP only */
        if (this.getLdapMode().isOnly()) {
            if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", CheckLdapAuth"); }
            return this._checkPasswordLDAP(user, enteredPass);
        }

        /* comparison against encoded table password */
        String tablePass = (user != null)? user.getEncodedPassword() : null;
        if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", tablePass="+tablePass); }
        if (this._checkPassword(enteredPass,tablePass)) {
            return true;
        }
        // -- table password comparison failed

        /* check LDAP as alternate? */
        if (this.getLdapMode().isAlternate()) {
            if (LOG) { Print.logInfo("[DEBUG] ... user="+user+", enteredPass="+enteredPass+", CheckLdapAuth"); }
            return this._checkPasswordLDAP(user, enteredPass);
        }

        /* password check failed */
        if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", CheckFailed"); }
        return false;

    }

    /** 
    *** Check entered password against stored password
    *** @param enteredPass  The User entered password
    *** @param tablePass    The password value from the Account/User table
    *** @return True if the passwords match
    **/
    private boolean _checkPassword(String enteredPass, String tablePass) 
    {
        boolean LOG = this.getDebugCheckPassword();
      //if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this +", enteredPass="+enteredPass +", tablePass="+tablePass); }

        /* no password specified in table */
        if ((tablePass == null) || tablePass.equals("")) { // do not use "isBlank"
            if (LOG) { Print.logInfo("[DEBUG] Login Failed: No table password"); }
            return false; // login not allowed for accounts with no password
        }

        /* no user-entered passowrd */
        if ((enteredPass == null) || enteredPass.equals("")) {
            if (!Account.BLANK_PASSWORD.equals("") && tablePass.equals(Account.BLANK_PASSWORD)) {
                if (LOG) { Print.logInfo("[DEBUG] Login OK: blank/null password"); }
                return true; // blank password is ok here
            } else {
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: No entered password"); }
                return false; // no password provided (not even a blank string)
            }
        }

        /* check entered password */
        // -- removed, not secure [2.6.7-B18j]
        //if (tablePass.equals(this.encodePassword(enteredPass))) { // fixed 2.2.7
        //    if (LOG) { Print.logInfo("[DEBUG] Login OK: Entered password matches encoded table password"); }
        //    return true; // passwords match
        //}

        // ----------------------------

        /* PLAIN password check */
        if (tablePass.length() != HASH_LEN) {
            // -- tablePass is PLAIN
            if (!this.isCheckPlain()) { // this.saveHash/this.altHash
                // -- PLAIN not allowed
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: PLAIN not allowed"); }
                return false;
            } else
            if (!tablePass.equals(enteredPass)) {
                // -- PLAIN-text passwords do not match
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: PLAIN did not match"); }
                return false;
            } else {
                // -- PLAIN-text passwords match
                if (LOG) { Print.logInfo("[DEBUG] Login OK: PLAIN match"); }
                return true;
            }
        } else 
        if (this.isSaveEncoding(ENC_PLAIN) && this.isAltEncoding(ENC_PLAIN)) { // this.saveHash (this.altHash == null) 
            // -- tablePass is 32-char PLAIN
            if (!tablePass.equals(enteredPass)) {
                // -- PLAIN-text passwords do not match
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: PLAIN did not match"); }
                return false;
            } else {
                // -- PLAIN-text passwords match
                if (LOG) { Print.logInfo("[DEBUG] Login OK: PLAIN match"); }
                return true;
            }
        }
        // -- tablePass is encoded (32-char) below this line

        // ----------------------------

        /* SHA1 password check */
        if (this.isSaveEncoding(ENC_SHA1) || this.isAltEncoding(ENC_SHA1)) { // this.saveHash/this.altHash
            // -- tablePass is SHA1 (Base64)
            byte b[];
            try {
                // -- decode Base64
                b = Base64.decode(tablePass);
                if (b.length < SHA1_SALT_LENGTH) {
                    if (LOG) { Print.logInfo("[DEBUG] Login Failed: SHA1 length < SHA1_SALT_LENGTH"); }
                    return false;
                }
            } catch (Base64.Base64DecodeException bde) {
                // -- error
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: Invalid SHA1 Base64"); }
                return false;
            }
            int hLen = b.length - SHA1_SALT_LENGTH;
            // -- extract salt
            byte salt[] = new byte[SHA1_SALT_LENGTH];
            for (int i = 0; i < SHA1_SALT_LENGTH; i++) {
                salt[i] = b[hLen + i];
            }
            // -- re-encode password
            byte h[] = this.getPasswordHash(SHA1_DIGEST, enteredPass, salt);
            if (h == null) {
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: SHA1 algorithm not found"); }
                return false;
            }
            // -- compare h to b[0,hLen]
            if (h.length != hLen) {
                // -- SHA-1 hash is different size (h.length is the expected size)
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: Expected SHA1 hash length "+h.length+", found "+hLen); }
                return false;
            } else
            if (Diff(h,b,hLen) >= 0) {
                // -- not equals
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: SHA1 did not match"); }
                return false; 
            } else {
                // -- equals
                if (LOG) { Print.logInfo("[DEBUG] Login OK: SHA1 match"); }
                return true;
            }
        }

        // ----------------------------

        /* MD5 password check */
        if (this.isSaveEncoding(ENC_MD5) || this.isAltEncoding(ENC_MD5)) { // this.saveHash/this.altHash
            // -- tablePass is MD5 (hex)
            String md5Pass = this.md5Hash(enteredPass);
            if ((md5Pass != null) && tablePass.equals(md5Pass)) {
                if (LOG) { Print.logInfo("[DEBUG] Login OK: MD5 match"); }
                return true;
            } else {
                if (LOG) { Print.logInfo("[DEBUG] Login Failed: MD5 did not match"); }
                return false;
            }
        }

        // ----------------------------

        /* failed */
        if (LOG) { Print.logInfo("[DEBUG] Login Failed: No match (encoding="+this.getSaveEncoding()+", tablePass="+tablePass+")"); }
        return false; // password does not match

    }

    // ------------------------------------------------------------------------

    /** 
    *** Check entered password against stored password
    *** @param enteredPass  The User entered password
    *** @param tablePass    The password value from the Account/User table
    *** @return True if the passwords match
    **/
    private boolean _checkPasswordLDAP(UserInformation user, String enteredPass) 
    {
        boolean LOG = this.getDebugCheckPassword();
        if (user == null) {
            // -- no user, password check failed
            if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", NullUser"); }
            return false;
        } else
        if (this.getLdapMode().isDisabled()) {
            // -- LDAP disabled, password check failed
            if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", LdapDisabled"); }
            return false;
        } else {
            // -- EXERIMENTAL: LDAP may not be currently supported
            if (LOG) { Print.logInfo("[DEBUG] PasswordHandler="+this+", user="+user+", enteredPass="+enteredPass+", LdapNotSupported"); }
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Return true if the specified character is an allowed special character
    *** @param ch  The character to test
    *** @return True if the character is an allowed special character
    **/
    private boolean isSpecialChar(char ch)
    {
        String specChars = this.getString(PROP_specialCharacters, "");
        return (specChars.indexOf(ch) >= 0);
    }

    /**
    *** Return true if the count is >= minimum count
    *** @param key Count property key
    *** @param count  The count to check
    *** @return True if the count is >= minimum count, false otherwise
    **/
    private boolean checkCharCount(String key, int count)
    {
        int min = this.getInt(key, 0);
        if ((min <= 0) || (count >= min)) {
            return true;
        } else {
            return false;
        }
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the number of previous passwords to check for uniqueness
    *** @return The number of previous passwords to check.
    **/
    public int getRequiredUniquePasswordCount()
    {
        String val = Trim(this.getString(PROP_requiredUniquePassword,null)); // non-null

        /* blank/null - disabled */
        if (val.equals("")) { 
            return 0;
        }

        /* "false" - disabled */
        if (val.equalsIgnoreCase("false") || val.equalsIgnoreCase("no")) {
            return 0;
        }

        /* "true" - "1" */
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes")) {
            return 1;
        }

        /* return count */
        int count = ParseInt(val, 1);
        return count;

    }

    /**
    *** Checks new password and returns true if the password passes the policy
    *** for newly created password.
    *** @param newPass     The password to validate as acceptable
    *** @param oldEncPass  List of previously used passwords
    *** @param msg         The returned error message describing the reason for a failed validation
    *** @return True if the specified password is acceptable
    **/
    public boolean validateNewPassword(String newPass, String oldEncPass[], I18N.Text msg) 
    {

        /* password not specified */
        if (newPass == null) {
            // -- password not specified
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.notSpecified","Password not specified"));
            }
            return false;
        }

        /* empty password allowed? */
        if (newPass.equals("")) {
            // -- an empty password would prevent user login
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.blankNotAllowed","Blank password not allowed"));
            }
            return false;
        }

        /* minimum length */
        int minLen = this.getInt(PROP_minimumPasswordLength, 0);
        if ((minLen > 0) && (newPass.length() < minLen)) {
            // -- too short
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.tooShort","Password is too short"));
            }
            return false;
        }

        /* maximum length */
        int maxLen = this.getInt(PROP_maximumPasswordLength, 0);
        if ((maxLen > 0) && (newPass.length() > maxLen)) {
            // -- too long
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.tooLong","Password is too long"));
            }
            return false;
        }

        /* count char types */
        int lowerCount    = 0;
        int upperCount    = 0;
        int alphaCount    = 0;
        int digitCount    = 0;
        int specialCount  = 0;
        int nonAlphaCount = 0;
        for (int i = 0; i < newPass.length(); i++) {
            char ch = newPass.charAt(i);
            if (Character.isLowerCase(ch)) {
                lowerCount++;
                alphaCount++;
            } else
            if (Character.isUpperCase(ch)) {
                upperCount++;
                alphaCount++;
            } else
            if (Character.isDigit(ch)) {
                digitCount++;
                nonAlphaCount++;
            } else
            if (this.isSpecialChar(ch)) {
                specialCount++;
                nonAlphaCount++;
            } else {
                // -- invalid character
                if (msg != null) {
                    msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.invalidCharacter","Invalid character found in password"));
                }
                return false;
            }
        }

        /* check minimum counts */
        if (!this.checkCharCount(PROP_minimumLowerAlphaChars, lowerCount   )) {
            // -- not enough lower-alpha
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.lowerAlphaRequired","Requires additional lower-alpha characters"));
            }
            return false;
        }
        if (!this.checkCharCount(PROP_minimumUpperAlphaChars, upperCount   )) {
            // -- not enough upper-alpha
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.upperAlphaRequired","Requires additional upper-alpha characters"));
            }
            return false;
        }
        if (!this.checkCharCount(PROP_minimumAlphaChars     , alphaCount   )) {
            // -- not enough alpha
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.alphaRequired","Requires additional alpha characters"));
            }
            return false;
        }
        if (!this.checkCharCount(PROP_minimumDigitChars     , digitCount   )) {
            // -- not enough digits
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.digitsRequired","Requires additional digit characters"));
            }
            return false;
        }
        if (!this.checkCharCount(PROP_minimumSpecialChars   , specialCount )) {
            // -- not enough special
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.specialRequired","Requires additional special characters"));
            }
            return false;
        }
        if (!this.checkCharCount(PROP_minimumNonAlphaChars  , nonAlphaCount)) {
            // -- not enough non-alpha
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.nonAlphaRequired","Requires additional non-alpha characters"));
            }
            return false;
        }

        /* category count */
        int minCategories = this.getInt(PROP_minimumCategories, 0);
        if (minCategories > 0) {
            int catCnt = 0;
            if (lowerCount   > 0) { catCnt++; } // lower-case
            if (upperCount   > 0) { catCnt++; } // upper-case
            if (digitCount   > 0) { catCnt++; } // digits
            if (specialCount > 0) { catCnt++; } // special characters
            int minCat = (minCategories <= 4)? minCategories : 4;
            if (catCnt < minCat) {
                // -- not enough categories
                if (msg != null) {
                    msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.categoriesRequired","Requires additional character categories"));
                }
                return false;
            }
        }

        /* check encoded password */
        String newPassEnc = this.encodePassword(newPass);
        if ((newPassEnc == null) || newPassEnc.equals("")) { // do not use "isBlank"
            // -- encoded password is empty/null (can occur if SHA1/MD5 algorithm was not found)
            if (msg != null) {
                msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.encodedEmptyNull","Encoded password is blank"));
            }
            return false;
        }

        /* matches a previously used password? */
        int uniqPassCount = this.getRequiredUniquePasswordCount();
        //Print.logInfo("Previous passwords: " + StringTools.join(oldEncPass,",")); // StringTools ok here
        if ((oldEncPass != null) && (oldEncPass.length > 0) && (uniqPassCount > 0)) {
            for (int p = 0; (p < oldEncPass.length) && (p < uniqPassCount); p++) {
                String encPass = oldEncPass[p];
                if (newPassEnc.equals(encPass)) {
                    // -- new password was previously used
                    if (msg != null) {
                        msg.set(I18N.getString(GeneralPasswordHandler.class,"GeneralPasswordHandler.matchesPrior","Must not match prior password"));
                    }
                    return false;
                }
            }
        }

        /* ok */
        return true;

    }

    /**
    *** Returns a text description of the valid characters alowed for a password
    *** @param locale  The locale
    *** @return The text description of the password format requirements
    **/
    public String getPasswordFormatDescription(Locale locale) 
    {
        I18N i18n = I18N.getI18N(GeneralPasswordHandler.class, locale);

        /* check for predefined description */
        Object fmtDesc = this.getProperty(PROP_passwordFormatDescription,null);
        if (fmtDesc instanceof I18N.Text) {
            String desc = ((I18N.Text)fmtDesc).toString(i18n);
            if ((desc != null) && !desc.trim().equals("")) {
                return desc;
            }
        } else
        if (fmtDesc instanceof String) {
            String desc = (String)fmtDesc;
            if ((desc != null) && !desc.trim().equals("")) {
                return desc;
            }
        }

        /* create description based on current settings */
        StringBuffer sb = new StringBuffer();

        /* minimum length */
        int minLen = this.getInt(PROP_minimumPasswordLength, 0);
        if (minLen > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.minimumLength", 
                "- At least {0} characters in length", String.valueOf(minLen)));
            sb.append("\n");
        }

        /* maximum length */
        int maxLen = this.getInt(PROP_maximumPasswordLength, 0);
        if (maxLen > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.maximumLength", 
                "- At most {0} characters in length", String.valueOf(maxLen)));
            sb.append("\n");
        }

        /* special characters */
        String specChars = this.getString(PROP_specialCharacters, "");
        if ((specChars != null) && !specChars.equals("")) { // space significant?
            sb.append(i18n.getString("GeneralPasswordHandler.format.specialChars", 
                "- May contain special characters \"{0}\"", specChars));
            sb.append("\n");
        }

        /* min lower-alpha characters */
        int minLower = this.getInt(PROP_minimumLowerAlphaChars, 0);
        if (minLower > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.lowerAlphaCount", 
                "- At least {0} lower-case characters", String.valueOf(minLower)));
            sb.append("\n");
        }

        /* min upper-alpha characters */
        int minUpper = this.getInt(PROP_minimumUpperAlphaChars, 0);
        if (minUpper > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.upperAlphaCount", 
                "- At least {0} upper-case characters", String.valueOf(minUpper)));
            sb.append("\n");
        }

        /* min upper-alpha characters */
        int minAlpha = this.getInt(PROP_minimumAlphaChars, 0);
        if (minAlpha > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.alphaCount", 
                "- At least {0} alpha characters", String.valueOf(minAlpha)));
            sb.append("\n");
        }

        /* min digit characters */
        int minDigit = this.getInt(PROP_minimumDigitChars, 0);
        if (minDigit > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.digitCount", 
                "- At least {0} numeric digits", String.valueOf(minDigit)));
            sb.append("\n");
        }

        /* min special characters */
        int minSpec = this.getInt(PROP_minimumSpecialChars, 0);
        if (minSpec > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.specialCount", 
                "- At least {0} special characters", String.valueOf(minSpec)));
            sb.append("\n");
        }

        /* min non-alpha characters */
        int minNonAlpha = this.getInt(PROP_minimumNonAlphaChars, 0);
        if (minNonAlpha > 0) {
            sb.append(i18n.getString("GeneralPasswordHandler.format.nonAlphaCount", 
                "- At least {0} non-alpha characters", String.valueOf(minNonAlpha)));
            sb.append("\n");
        }

        /* min categories */
        int minCategories = this.getInt(PROP_minimumCategories, 0);
        if (minCategories > 0) {
            int minCat = (minCategories <= 4)? minCategories : 4;
            sb.append(i18n.getString("GeneralPasswordHandler.format.categoryCount", 
                "- At least {0} of the 4 categories (lower/upper/digit/special)", String.valueOf(minCat)));
            sb.append("\n");
        }

        /* return description */
        return sb.toString();

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the password has passed its expiration date
    *** @param lastChangedTime  The Epoch timestamp when the password was last changed
    *** @return True is the password has passed its expiration date
    **/
    public boolean hasPasswordExpired(long lastChangedTime) 
    {
        long maxAgeSec = this.getLong(PROP_maximumPasswordAgeSeconds, 0L);

        /* no expiration time */
        if (maxAgeSec <= 0L) {
            return false; // no expiration
        }

        /* no last changed time */
        if (lastChangedTime <= 0L) {
            return false;
        }

        /* expired */
        long ageSec = DateTime.getCurrentTimeSec() - lastChangedTime;
        if (ageSec < 0L) {
            // -- password changed in the future?
            return false;
        } else
        if (ageSec <= maxAgeSec) {
            // -- not yet expired
            return false;
        } else {
            // -- expired
            return true;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the maximum failed login attempts
    *** @return The maximum failed login attempts
    **/
    public int getFailedLoginMaximumAttempts()
    {
        return this.getInt(PROP_failedLoginMaximumAttempts, 5);
    }

    /**
    *** Gets the maximum failed login attempt interval (in seconds)
    *** @return The maximum failed login attempt interval (in seconds).
    **/
    public long getFailedLoginAttemptInterval()
    {
        return this.getLong(PROP_failedLoginAttemptInterval,120L);
    }

    /**
    *** Gets the suspend interval on failed login (in seconds)
    *** @return The suspend interval on failed login (in seconds)
    **/
    public long getFailedLoginSuspendInterval()
    {
        return this.getLong(PROP_failedLoginSuspendInterval,180L);
    }

    /**
    *** Returns true if suspend on failed login attempts is enabled
    *** @return True if suspend on failed login attempts is enabled
    **/
    public boolean getFailedLoginSuspendEnabled() 
    {

        /* check maximum failed login count/interval */
        int  maxFailedAttempts = this.getFailedLoginMaximumAttempts();
        long maxFailedInterval = this.getFailedLoginAttemptInterval();
        if ((maxFailedAttempts <= 0) || (maxFailedInterval <= 0L)) {
            // -- failed login attempt not enabled
            return false;
        }

        /* check suspend interval */
        long suspendInterval = this.getFailedLoginSuspendInterval();
        if (suspendInterval <= 0L) {
            // -- suspend not enabled
            return false;
        }

        /* enabled */
        return true;

    }

    /**
    *** Checks the maximum failed login attempts, and returns a suspend time.
    *** @param failedLoginAttempts  The current number of failed login attempts
    *** @param asOfTimeSec The time of the latest failed login attempt
    **/
    public long getFailedLoginAttemptSuspendTime(int failedLoginAttempts, long asOfTimeSec)
    {

        /* no failed login attempts? */
        if (failedLoginAttempts <= 0) {
            // -- no failed logins, do not suspend
            return 0L;
        }

        /* validate as-of time */
        if (asOfTimeSec <= 0L) {
            // -- invalid as-of time
            Print.logError("Invalid failed login attempt time: " + asOfTimeSec);
            return 0L;
        }

        /* check maximum failed login count/interval */
        int  maxFailedAttempts = this.getFailedLoginMaximumAttempts();
        long maxFailedInterval = this.getFailedLoginAttemptInterval();
        if ((maxFailedAttempts <= 0) || (maxFailedInterval <= 0L)) {
            // -- failed login attempt not checked, do not suspend
            return 0L;
        } else
        if (failedLoginAttempts < maxFailedAttempts) {
            // -- not yet reached maximum failed login attempts
            return 0L;
        }

        /* excessive failed logins, suspend until ... */
        long suspendInterval = this.getFailedLoginSuspendInterval();
        if (suspendInterval <= 0L) {
            // -- we aren't suspending, even if maximum is exceeded
            return 0L;
        } else {
            // -- suspend until ...
            return asOfTimeSec + suspendInterval;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Return String representation of this instance
    *** @return String representation of this instance
    **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[GeneralPasswordHandler]\n");
        sb.append("  Encoding Type = ").append(this.getPasswordEncodingString()).append("\n");
        for (RTKey.Entry pe : PROP_Entry) {
            String pk = pe.getKey();
            String ph = pe.getHelp();
            if (!this.hasProperty(pk)) { continue; }
            sb.append("  ").append(pk).append(" = ");
            sb.append(this.getProperty(pk,""));
            sb.append("  (").append(ph).append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //  <PasswordHandler 
    //      name="hash"
    //      class="org.opengts.db.GeneralPasswordHandler"
    //      />
    //      <Property key="passwordEncoding">plain</Property>   <!-- md5|md5plain|plain -->
    //      <Property key="minimumPasswordLength">6</Property>
    //      <Property key="minimumLowerAlphaChars">0</Property>
    //      <Property key="minimumUpperAlphaChars">0</Property>
    //      <Property key="minimumAlphaChars">0</Property>
    //      <Property key="minimumDigitChars">0</Property>
    //      <Property key="minimumNonAlphaChars">0</Property>
    //  </PasswordHandler>

    public static final String ARG_PASSWORD[]   = { "password", "passwd", "pass", "pwd"   };
    public static final String ARG_ENCODING[]   = { "encoding", "enc"             };
    public static final String ARG_MINLEN[]     = { "minlen"  , "length", "len"   };
    public static final String ARG_MAXLEN[]     = { "maxlen"  ,                   };
    public static final String ARG_MINLOWER[]   = { "minLower", "lower"           };
    public static final String ARG_MINUPPER[]   = { "minUpper", "upper"           };
    public static final String ARG_MINALPHA[]   = { "minAlpha", "alpha"           };
    public static final String ARG_MINDIGIT[]   = { "minDigit", "digit"           };
    public static final String ARG_MINSPEC[]    = { "minSpec" , "special", "spec" };
    public static final String ARG_TBLPASS[]    = { "tblPass" , "table"           };

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        String passwd  = RTConfig.getString(ARG_PASSWORD,"");
        String tblPass = RTConfig.getString(ARG_TBLPASS ,"");

        /* create PasswordHandler */
        RTProperties rtp = new RTProperties();
        rtp.setString(PROP_passwordEncoding      , RTConfig.getString(ARG_ENCODING, "plain"));
        rtp.setInt   (PROP_minimumPasswordLength , RTConfig.getInt   (ARG_MINLEN  ,       1));
        rtp.setInt   (PROP_maximumPasswordLength , RTConfig.getInt   (ARG_MAXLEN  ,      31));
        rtp.setInt   (PROP_minimumLowerAlphaChars, RTConfig.getInt   (ARG_MINLOWER,       0));
        rtp.setInt   (PROP_minimumUpperAlphaChars, RTConfig.getInt   (ARG_MINUPPER,       0));
        rtp.setInt   (PROP_minimumAlphaChars     , RTConfig.getInt   (ARG_MINALPHA,       0));
        rtp.setInt   (PROP_minimumDigitChars     , RTConfig.getInt   (ARG_MINDIGIT,       0));
        rtp.setInt   (PROP_minimumSpecialChars   , RTConfig.getInt   (ARG_MINSPEC ,       0));
        GeneralPasswordHandler gph = null;
        try {
            gph = new GeneralPasswordHandler(rtp);
            Print.sysPrintln(gph.toString());
            Print.sysPrintln(gph.getPasswordFormatDescription(null));
        } catch (PasswordEncodingException pe) {
            Print.sysPrintln("ERROR: " + pe);
            System.exit(1);
        }
        gph.setDebugCheckPassword(true);

        /* validate password */
        I18N.Text msg   = new I18N.Text();
        boolean valid   = gph.validateNewPassword(passwd,null,msg);
        String  encPass = gph.encodePassword(passwd);
        boolean chkPass = gph._checkPassword(passwd,tblPass);
        Print.sysPrintln("Password: entered=" + passwd + ", table=" + tblPass);
        Print.sysPrintln("Valid   : " + valid + " ["+msg+"]");
        Print.sysPrintln("Encoded : " + encPass + " ["+encPass.length()+"]");
        Print.sysPrintln("Match   : " + chkPass);

    }

}
