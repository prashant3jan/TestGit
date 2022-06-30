package org.opengts.war.track.custom;

import org.opengts.db.BasicPrivateLabel;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;
import org.opengts.util.JSON;
import org.opengts.war.ctrac.CommandInterface;
import org.opengts.war.ctrac.CommandInterfaceAdapter;
import org.opengts.war.ctrac.Response;
import org.opengts.war.ctrac.ServiceContextInterface;
import org.opengts.war.tools.PrivateLabel;
import org.opengts.war.tools.RequestProperties;
import org.opengts.util.JSON.JSONBeanFilter;


public class UserCommand extends CommandInterfaceAdapter implements CommandInterface {
    public static final String COMMAND_User[] = { "C_getUserInfo", "C_userInfo" };

    public UserCommand() {
        super();
        this.setCommandNames(new String[][] { COMMAND_User });
    }

    /**
     *** Returns true if account/user authorization is required, false if
     * authorization is not required.
     *** 
     * @param cmd The name of the specific command
     **/
    public boolean isAuthorizationRequired(String cmd) {
        return true;
    }

    /**
     *** Command handling
     *** 
     * @param request  The current HttpServletRequest instance. Required for some
     *                 commands that need to read information from the current
     *                 request input stream. If null, then thos commands that
     *                 require a non-null request value will be ignored.
     *** @param respType Must be "json" to return he response in JSON format,
     *                 otherwise the response will be returned in a parsable text
     *                 format.
     *** @param servCtx  The current ServiceContextInterface
     *** @param hc       The HandleCommand instance
     *** @param cmd      The command to be executed
     *** @param cmdArgs  The command arguments/parameters
     **/
    public Response execCommand(ServiceContextInterface servCtx, String cmd, String cmdArgs[]) {
        Account account = servCtx.getAccount(); // may be null (but unlikely)
        User user = servCtx.getUser(); // may be null
        long respFormat = servCtx.getResponseFormat(); //
        BasicPrivateLabel privLabel = servCtx.getPrivateLabel(); // may be null (but unlikely)
        Object reqPropObj = servCtx.getRequestProperties(); // may be null
        RequestProperties reqProp = (reqPropObj instanceof RequestProperties) ? (RequestProperties) reqPropObj : null;
        String respFormatS = "jse"; // "jse"
        long format = Response.ParseFormat(respFormatS);

        String answerString = null;
        if (this.commandMatch(COMMAND_User, cmd)) 
        {
            answerString = getUserInfo(cmdArgs, account, user, reqProp, (PrivateLabel)privLabel);
        }

        Response answer = Response.OK_JSON(format, answerString.toString());
        return answer;
    }

    private String getUserInfo( String[] cmdArgs, Account account, User user, RequestProperties reqState, PrivateLabel label)
    {
        String answer = null;
        if(user != null)
        {
            JSONBeanFilter filter = new JSONBeanFilter("getUserInfo");

            user.setImageSource(reqState.getKeyValue(PrivateLabel.PROP_AccountLogin_banner300_image, null, null));
            user.setCompanyName(label.getPageTitle());

            answer = (new JSON._Object(user, filter)).toString();

            return answer;
        }
        else
        {
            JSON._Object adminUser = new JSON._Object();

            adminUser.addKeyValue("userId", "admin");
            adminUser.addKeyValue("userDescription", "Admin");
            adminUser.addKeyValue("accountId", account.getAccountID());
            adminUser.addKeyValue("accountDescription", account.getAccountDescription());
            adminUser.addKeyValue("imgSrc", reqState.getKeyValue(PrivateLabel.PROP_AccountLogin_banner300_image, null, null));
            adminUser.addKeyValue("companyName", label.getPageTitle());
            answer = adminUser.toString();
            return answer;
        }
    }

}
