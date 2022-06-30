package org.opengts.war.model;

import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;
import org.opengts.util.DateTime;
import org.opengts.util.I18N;
import org.opengts.war.tools.CommonServlet;
import org.opengts.war.tools.MapProvider;
import org.opengts.war.tools.MenuBar;
import org.opengts.war.tools.PrivateLabel;
import org.opengts.war.tools.RequestProperties;
import org.opengts.war.tools.WebPageAdaptor;
import org.opengts.war.track.Constants;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

public class DashboardModel
    extends WebPageAdaptor
    implements Constants   
{


    public DashboardModel()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_DEVICE_INFO);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_MENU_TOP });
        this.setLoginRequired(true);
    }

    /* write html */
    public void writePage(
        final RequestProperties reqState,
        String pageMsg)
        throws IOException
    {
        final HttpServletRequest request = reqState.getHttpServletRequest();
        final boolean      sysadminLogin = reqState.isLoggedInFromSysAdmin();
        final PrivateLabel privLabel     = reqState.getPrivateLabel();
        final I18N         i18n          = privLabel.getI18N(DeviceModel.class);
        final Locale       locale        = reqState.getLocale();
        final String       devTitles[]   = reqState.getDeviceTitles();
        final String       grpTitles[]   = reqState.getDeviceGroupTitles();
        final Account      currAcct      = reqState.getCurrentAccount(); // should not be null
        final String       currAcctID    = reqState.getCurrentAccountID();
        final User         currUser      = reqState.getCurrentUser(); // may be null
        final String       pageName      = this.getPageName();
        final boolean      acctBCEnabled = ((currAcct!=null)&&currAcct.getIsBorderCrossing())?true:false;
        final TimeZone     acctTimeZone  = Account.getTimeZone(currAcct,DateTime.getGMTTimeZone()); 

        DataModel model = new DataModel();
        String sigKey = "";
        String mapType = "";
        String faveiconLink = "";
        String mapURL = "";
        MapProvider mapProvider = privLabel.getMapProvider();
        sigKey = mapProvider.getAuthorization();
        mapType = mapProvider.getName();
        faveiconLink = privLabel.getFaviconLink();
        mapURL = mapProvider.getMapURL(reqState);
        if(faveiconLink.isEmpty())
        {
            faveiconLink = "null";
        }
        else
        {
            //Enter full, qualified name for favicon link
            faveiconLink = request.getContextPath()+"/"+faveiconLink;
        }


        //Add components to model
        model.put("URL", request.getContextPath());
        model.put("SIGNATURE_KEY", sigKey);
        model.put("MAP_TYPE", mapType);
        model.put("FAV_ICON", faveiconLink);
        model.put("MAPS_URL", mapURL);

        this.setPageName(PAGE_MENU_DASHBOARD);
        CommonServlet.writePageFrame
        (
            reqState,
            null,null,              // onLoad/onUnload
            null,                   // Style sheets
            null,                   // Javascript
            null,                   // Navigation
            null,                   // Content
            model);  

    }

    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_MAIN;
    }
}
