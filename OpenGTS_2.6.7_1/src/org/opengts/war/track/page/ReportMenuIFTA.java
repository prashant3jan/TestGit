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
//  2007/12/13  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.track.page;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.TimeZone;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;
import org.opengts.war.track.*;

public class ReportMenuIFTA
    extends ReportMenu
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Reports: "ifta"
    //  - Stateline Crossing Detail
    //  - State Mileage Summary
    //  - Fueling Detail
    //  - Fueling Summary

    public ReportMenuIFTA()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_MENU_RPT_IFTA);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_MENU_TOP });
        this.setLoginRequired(true);
        this.setReportType(ReportFactory.REPORT_TYPE_IFTA_DETAIL);
    }

    // ------------------------------------------------------------------------

    public boolean isOkToDisplay(RequestProperties reqState)
    {
        // -- [DCFILTER] check for supported BorderCrossing and enabled Account BorderCrossing
        if (!Account.SupportsBorderCrossing()) {
            return false;
        }
        // -- [DCFILTER] check "account.isBorderCrossing()"
        Account account = (reqState != null)? reqState.getCurrentAccount() : null;
        if ((account == null) || !account.isBorderCrossing()) {
            // -- no account, or not enabled for account
            return false;
        }
        // -- display
        return true;
    }

    // ------------------------------------------------------------------------

    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_REPORTS_IFTA;
    }

    public String getMenuDescription(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportMenuIFTA.class);
        return super._getMenuDescription(reqState,i18n.getString("ReportMenuIFTA.menuDesc","I.F.T.A. Reports"));
    }
   
    public String getMenuHelp(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportMenuIFTA.class);
        return super._getMenuHelp(reqState,i18n.getString("ReportMenuIFTA.menuHelp","Display various I.F.T.A. reports"));
    }

    // ------------------------------------------------------------------------

    public String getNavigationDescription(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportMenuIFTA.class);
        return super._getNavigationDescription(reqState,i18n.getString("ReportMenuIFTA.navDesc","I.F.T.A."));
    }

    public String getNavigationTab(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportMenuIFTA.class);
        return super._getNavigationTab(reqState,i18n.getString("ReportMenuIFTA.navTab","I.F.T.A."));
    }

    // ------------------------------------------------------------------------

    @Override
    protected java.util.List<IDDescription> filterDeviceChooserList(
        RequestProperties reqState, java.util.List<IDDescription> idList) // [DCFILTER]
    {

        /* RequestProperties and IDDescription list specified? */
        if ((reqState == null) || (idList == null)) {
            // -- unlikely, but check anyway
            return idList; // return as-is
        }

        /* device selection report only */
        if (!this.isReportTypeDevice()) {
            // -- unlikely, but check anyway
            return idList; // return as-is
        }

        /* create list for Devices that participate in BorderCrossing */
        java.util.List<IDDescription> newList = new Vector<IDDescription>(); // new OrderedSet<IDDescription>();

        /* BorderCrossing supported? */
        if (!Account.SupportsBorderCrossing()) {
            // -- return empty list
            return newList;
        }

        /* BorderCrossing enabled for Account? */
        Account account = reqState.getCurrentAccount();
        if ((account == null) || !account.isBorderCrossing()) {
            // -- no account, or not enabled for account
            return newList;
        }

        /* iterate through list of Devices */
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        for (IDDescription idd : idList) {
            String devID = idd.getID();
            try {
                Device dev = Device.getDevice(rwMode, account, devID);
                if ((dev != null) && dev.isBorderCrossing()) {
                    Print.logInfo("Including device: " + devID);
                    newList.add(idd);
                } else {
                    /*mdf*/Print.logInfo("Excluding device: " + devID);
                }
            } catch (DBException dbe) {
                Print.logError("Getting Device: " + dbe);
            }
        }

        /* return new assembled list */
        return newList;

    }

    // ------------------------------------------------------------------------

}
