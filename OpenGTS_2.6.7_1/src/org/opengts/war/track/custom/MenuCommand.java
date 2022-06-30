package org.opengts.war.track.custom;

import org.opengts.db.BasicPrivateLabel;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;
import org.opengts.util.JSON;
import org.opengts.util.ListTools;
import org.opengts.war.ctrac.CommandInterface;
import org.opengts.war.ctrac.CommandInterfaceAdapter;
import org.opengts.war.ctrac.Response;
import org.opengts.war.ctrac.ServiceContextInterface;
import org.opengts.war.tools.MenuGroup;
import org.opengts.war.tools.RequestProperties;
import org.opengts.war.tools.WebPage;

import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.opengts.util.ListTools;

public class MenuCommand
    extends CommandInterfaceAdapter
    implements CommandInterface
{
    public static final String COMMAND_Menu[] = {"C_MenuList", "C_getMenuList"};

    public MenuCommand()
    {
        super();
        this.setCommandNames(new String[][] {COMMAND_Menu});
    }

            /**
    *** Returns true if account/user authorization is required, 
    *** false if authorization is not required.
    *** @param cmd  The name of the specific command
    **/
    public boolean isAuthorizationRequired(String cmd)
    {
        return true;
    }

    /**
    *** Command handling
    *** @param request  The current HttpServletRequest instance.  Required for some commands
    ***     that need to read information from the current request input stream.   If null,
    ***     then thos commands that require a non-null request value will be ignored.
    *** @param respType Must be "json" to return he response in JSON format, otherwise the 
    ***     response will be returned in a parsable text format.
    *** @param servCtx  The current ServiceContextInterface
    *** @param hc       The HandleCommand instance
    *** @param cmd      The command to be executed
    *** @param cmdArgs  The command arguments/parameters
    **/
    public Response execCommand(
        ServiceContextInterface servCtx,
        String cmd, String cmdArgs[])
    {
        Account           account    = servCtx.getAccount();           // may be null (but unlikely)
        User              user       = servCtx.getUser();              // may be null
        long              respFormat = servCtx.getResponseFormat();    // 
        BasicPrivateLabel privLabel  = servCtx.getPrivateLabel();      // may be null (but unlikely)
        Object            reqPropObj = servCtx.getRequestProperties(); // may be null
        RequestProperties reqProp    = (reqPropObj instanceof RequestProperties)? (RequestProperties)reqPropObj : null;
        String respFormatS = "jse"; // "jse"
        long format = Response.ParseFormat(respFormatS);

        String answerString = null;

        if(this.commandMatch(COMMAND_Menu, cmd))
        {
            //answerString = getMenuGroup(account, user, reqProp);
            answerString = getMenuGroups(account, user, reqProp);
        }
        

        Response answer = Response.OK_JSON(format, answerString.toString());
        return answer;
    }


    private String getMenuGroups(Account account, User user, RequestProperties reqProp)
    {
        String answer = null;
        Map<String, MenuGroup> menuMap = reqProp.getPrivateLabel().getMenuGroupMap();
        //Vector<JSON._Object> menuGroups = new Vector<JSON._Object>();
        JSON._Array menuGroups = new JSON._Array();
        for(String key: menuMap.keySet())
        {
            MenuGroup group = menuMap.get(key);
            JSON._Object menuGroup = new JSON._Object();
            if(group.showInMenuBar())
            {
                menuGroup.addKeyValue("displayName", group.getDescription(account.getLocale()));
                menuGroup.addKeyValue("iconName", "");
                menuGroup.addKeyValue("disabled", false);
                menuGroup.addKeyValue("url", "");
                menuGroup.addKeyValue("help", group.getDescription(account.getLocale()));
                JSON._Array menuChildren = new JSON._Array();
                List<WebPage> pageList = group.getWebPageList(reqProp);
                if(!ListTools.isEmpty(pageList))
                {
                    for(WebPage page: pageList)
                    {
                        String icon = page.getMatIcon();
                        JSON._Object pageObject = new JSON._Object();
                        pageObject.addKeyValue("displayName", page.getNavigationDescription(reqProp));
                        pageObject.addKeyValue("iconName", icon);
                        pageObject.addKeyValue("disabled", false);
                        pageObject.addKeyValue("url", page.encodePageURL(reqProp));
                        pageObject.addKeyValue("help", page.getMenuHelp(reqProp, null));
                        pageObject.addKeyValue("children", (JSON._Object[])null);
                        menuChildren.addValue(pageObject);
                    }
                }

                menuGroup.addKeyValue("children", menuChildren);
                menuGroups.addValue(menuGroup);
            }
        }
        answer = menuGroups.toString();
        return answer;
    }

}
