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
//  HTTP "user-agent" parser.
//  Currently only checks for specific keywoard to determine device type.
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// Change History:
//  2016/09/01  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.net.*;

/**
*** User-Agent parser.<br>
*** Currently only used to check keywords to determine device type (desktop, tablet, mobile, etc)
**/

public class UserAgent
{

    // ------------------------------------------------------------------------

    /**
    *** Parsed Device Type
    **/
    public enum DeviceType {
        UNKNOWN,
        DESKTOP,
        TABLET,     // "iPad", "Tablet"
        PHONE,      // "iPhone", "iOS", "Android", "Windows Phone", "Windows Mobile", "Windows CE"
        ROBOT;      // "The Knowledge AI", "Sogou web spider", "Yandex*"
        public boolean isUnknown() { return this.equals(UNKNOWN); }
        public boolean isPhone()   { return this.equals(PHONE); }
        public boolean isTablet()  { return this.equals(TABLET); }
        public boolean isMobile()  { return this.isPhone() || this.isTablet(); }
        public boolean isDesktop() { return this.equals(DESKTOP) || this.isUnknown(); }
        public boolean isRobot()   { return this.equals(ROBOT); }
    };

    // ------------------------------------------------------------------------

    private static final String DeviceType_ROBOT[] = {
        "robot", "crawl",   // -- (general identifiers)
        "/bot", "bot/",     // -- (general identifiers)
        "spider",           // -- (general identifiers)
        "Yandex",           // -- Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)
        "Sogou",            // -- Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)
        "Googlebot",        // -- Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)
        "/+/web/snippet/",  // -- Google (+https://developers.google.com/+/web/snippet/)
        "Bingbot",          // -- Mozilla/5.0 (compatible; Bingbot/2.0; +http://www.bing.com/bingbot.htm)
        "AdIdxBot",         // -- Mozilla/5.0 (compatible; adidxbot/2.0; +http://www.bing.com/bingbot.htm)
        "BingPreview",      // -- Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534+ (KHTML, like Gecko) BingPreview/1.0b
        "msnbot",           // -- msnbot/1.0 (+http://search.msn.com/msnbot.htm)
        "DuckBot",          // -- DuckDuckBot/1.0; (+http://duckduckgo.com/duckduckbot.html)
        "Slurp",            // -- Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)
        "Baidu",            // -- Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)
        "Exabot",           // -- Mozilla/5.0 (compatible; Exabot/3.0; +http://www.exabot.com/go/robot)
        "Konqueror",        // -- Mozilla/5.0 (compatible; Konqueror/3.5; Linux) KHTML/3.5.5 (like Gecko) (Exabot-Thumbnails)
        "facebot",          // -- facebot
        "facebookexternal", // -- facebookexternalhit/1.0 (+http://www.facebook.com/externalhit_uatext.php)
        "ia_archiver",      // -- ia_archiver (+http://www.alexa.com/site/help/webmasters; crawler@alexa.com)
        "Knowledge AI",     // -- The Knowledge AI 
        "CCBot",            // -- CCBot/2.0 (https://commoncrawl.org/faq/)
        "ZoominfoBot",      // -- ZoominfoBot (zoominfobot at zoominfo dot com)
        "IndeedBot",        // -- Mozilla/5.0 (Windows NT 6.1; rv:38.0) Gecko/20100101 Firefox/38.0 (IndeedBot 1.1)
        "MJ12bot",          // -- Mozilla/5.0 (compatible; MJ12bot/v1.4.8; http://mj12bot.com/)
        "Genieo",           // -- Mozilla/5.0 (compatible; Genieo/1.0 http://www.genieo.com/webfilter.html)
        "SeznamBot",        // -- Mozilla/5.0 (compatible; SeznamBot/3.2; +http://napoveda.seznam.cz/en/seznambot-intro/)
        "SEOkicks",         // -- Mozilla/5.0 (compatible; SEOkicks; +https://www.seokicks.de/robot.html)
        "Dataprovider",     // -- Mozilla/5.0 (compatible; Dataprovider.com)
        "DotBot",           // -- Mozilla/5.0 (compatible; DotBot/1.1; http://www.opensiteexplorer.org/dotbot, help@moz.com)
        "SemrushBot",       // -- Mozilla/5.0 (compatible; SemrushBot/3~bl; +http://www.semrush.com/bot.html)
        "SeznamBot",        // -- Mozilla/5.0 (compatible; SeznamBot/3.2; +http://napoveda.seznam.cz/en/seznambot-intro/)
        "aiHitBot",         // -- Mozilla/5.0 (compatible; aiHitBot/2.9; +https://www.aihitdata.com/about)
        "TurnitinBot",      // -- TurnitinBot (https://turnitin.com/robot/crawlerinfo.html)
        "Steeler",          // -- Mozilla/5.0 (compatible; Steeler/3.5; http://www.tkl.iis.u-tokyo.ac.jp/~crawler/)
        "awesome_bot",      // -- awesome_bot
        "MegaIndex",        // -- Mozilla/5.0 (compatible; MegaIndex.ru/2.0; +http://megaindex.com/crawler)
        "AhrefsBot",        // -- Mozilla/5.0 (compatible; AhrefsBot/6.1; +http://ahrefs.com/robot/)
        "Slackbot",         // -- Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)
        "WordPress",        // -- WordPress/5.2.2; http://10.136.95.175:201
        "TelegramBot",      // -- TelegramBot (like TwitterBot)
        "RU_Bot",           // -- Mozilla/5.0 (compatible; Linux x86_64; Mail.RU_Bot/2.0; +http://go.mail.ru/help/robots)
        "Netcraft",         // -- Mozilla/5.0 (compatible; NetcraftSurveyAgent/1.0; +info@netcraft.com)
        "datenbank.de",     // -- netEstate NE Crawler (+http://www.website-datenbank.de/)
        "Faraday",          // -- Faraday v0.15.2
        "Go-http-client",   // -- Go-http-client/1.1
        "WhatsApp",         // -- WhatsApp/2.19.188 A
        "Microsoft Office", // -- Microsoft Office Word 2014
        "Ask Jeeves",       // -- Mozilla/5.0 (compatible; Ask Jeeves/Teoma; +http://about.ask.com/en/docs/about/webmasters.shtml)
        "Google Favicon",   // -- Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.75 Safari/537.36 Google Favicon
        "Jyxobot",          // -- Jyxobot/1
        // --
        //"MACINTOSH",      // -- debug test for robot
    };

    private static final String DeviceType_TABLET[] = {
        "iPad",             // -- Mozilla/5.0 (iPad; CPU OS 5_1_1 like Mac OS X; es-es) AppleWebKit/535.32 (KHTML, like Gecko) CriOS/24.0.1562.72 Mobile/9B2E6 Safari/9843.31.1
        "Tablet",           // -- ?
    };

    private static final String DeviceType_PHONE[] = {
        "Phone",            // -- Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.47.1 (KHTML, like Gecko) CriOS/26.0.1472.13 Mobile/1AD362 Safari/6938.17
        "Mobile",           // -- Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.47.1 (KHTML, like Gecko) CriOS/26.0.1472.13 Mobile/1AD362 Safari/6938.17
        "Android",          // -- Mozilla/5.0 (Linux; Android 4.1.1; GT-N7100 Build/JRO03C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.99 Mobile Safari/537.36
    };

    private static final String DeviceType_DESKTOP[] = {
        "Firefox",          // -- Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:67.0) Gecko/20100101 Firefox/67.0
        "Safari",           // -- Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36
        "Chrome",           // -- Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36
        "Trident",          // -- Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko
        "MSIE",             // -- Mozilla/4.0 (compatible; MSIE 7.0; AOL 9.0; Windows NT 5.1; WebProducts; SpamBlocker 4.8.3)
        "WOW64",            // -- Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko
    };

    /* make sure all DeviceType test patterns are lowercase at startup */
    static {
        // -- DeviceType_ROBOT
        for (int i = 0; i < DeviceType_ROBOT.length; i++) {
            DeviceType_ROBOT[i] = DeviceType_ROBOT[i].toLowerCase();
        }
        // -- DeviceType_TABLET
        for (int i = 0; i < DeviceType_TABLET.length; i++) {
            DeviceType_TABLET[i] = DeviceType_TABLET[i].toLowerCase();
        }
        // -- DeviceType_PHONE
        for (int i = 0; i < DeviceType_PHONE.length; i++) {
            DeviceType_PHONE[i] = DeviceType_PHONE[i].toLowerCase();
        }
        // -- DeviceType_DESKTOP
        for (int i = 0; i < DeviceType_DESKTOP.length; i++) {
            DeviceType_DESKTOP[i] = DeviceType_DESKTOP[i].toLowerCase();
        }
    };

    /* gets the probable device type from the UserAgent String */
    private static DeviceType getDeviceType(String ua)
    {
        String uaLC = StringTools.trim(ua).toLowerCase();

        /* blank? */
        if (StringTools.isBlank(uaLC)) {
            return DeviceType.UNKNOWN;
        }

        /* Robots */
        for (String s : DeviceType_ROBOT) {
            if (uaLC.indexOf(s) >= 0) {
                return DeviceType.ROBOT;
            }
        }

        /* Tablets */
        for (String s : DeviceType_TABLET) {
            if (uaLC.indexOf(s) >= 0) {
                return DeviceType.TABLET;
            }
        }

        /* Phone */
        for (String s : DeviceType_PHONE) {
            if (uaLC.indexOf(s) >= 0) {
                return DeviceType.PHONE;
            }
        }

        /* Desktop */
        for (String s : DeviceType_DESKTOP) {
            if (uaLC.indexOf(s) >= 0) {
                return DeviceType.DESKTOP;
            }
        }

        /* else Unknown */
        return DeviceType.UNKNOWN;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String      userAgent   = "";
    private DeviceType  deviceType  = DeviceType.UNKNOWN;

    /**
    *** Constructor
    **/
    public UserAgent(String ua)
    {
        super();
        this.userAgent  = StringTools.trim(ua);
        this.deviceType = UserAgent.getDeviceType(this.userAgent);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the user-agent String
    **/
    public String getUserAgentString()
    {
        return this.userAgent;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the device type represented by this UserAgent
    **/
    public DeviceType getDeviceType()
    {
        return this.deviceType;
    }

    // --------------------------------

    /**
    *** Returns true if the device-type of this user agent is unknown
    **/
    public boolean isUnknown()
    {
        return this.getDeviceType().isUnknown();
    }

    /**
    *** Returns true if this user-agent represents a phone
    **/
    public boolean isPhone()
    {
        return this.getDeviceType().isPhone();
    }

    /**
    *** Returns true if this user-agent represents a phone
    **/
    public boolean isTablet()
    {
        return this.getDeviceType().isTablet();
    }

    /**
    *** Returns true if this user-agent represents a mobile device
    **/
    public boolean isMobile()
    {
        return this.getDeviceType().isMobile();
    }

    /**
    *** Returns true if this user-agent represents a robot ('bot)
    **/
    public boolean isRobot()
    {
        return this.getDeviceType().isRobot();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a String representation of this instance
    **/
    public String toString()
    {
        return "[" + this.getDeviceType() + "] " + this.getUserAgentString();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_USER_AGENT[] = { "user-agent", "userAgent", "ua" };

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        String userAgent = RTConfig.getString(ARG_USER_AGENT,null);
        if (!StringTools.isBlank(userAgent)) {
            UserAgent ua = new UserAgent(userAgent);
            Print.sysPrintln("User Agent : " + ua.getUserAgentString());
            Print.sysPrintln("Device Type: " + ua.getDeviceType());
        }
    }

}
