package org.opengts.war.track.custom;

import org.opengts.db.BasicPrivateLabel;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.User;
import org.opengts.geocoder.GeocodeProvider;
import org.opengts.geocoder.ReverseGeocode;
import org.opengts.geocoder.ReverseGeocodeProvider;
import org.opengts.util.GeoPoint;
import org.opengts.util.JSON;
import org.opengts.war.ctrac.CommandInterface;
import org.opengts.war.ctrac.CommandInterfaceAdapter;
import org.opengts.war.ctrac.Response;
import org.opengts.war.ctrac.ServiceContextInterface;
import org.opengts.war.tools.RequestProperties;

import java.util.Locale;
public class GeocoderCommand extends CommandInterfaceAdapter implements CommandInterface {
    public static final String COMMAND_Geocoder[] = { "C_Geocoder", "C_getGeocode" };

    public GeocoderCommand() {
        super();
        this.setCommandNames(new String[][] { COMMAND_Geocoder });
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
        if (this.commandMatch(COMMAND_Geocoder, cmd)) {
            answerString = getGeocode(cmdArgs, account, user, privLabel);
        }

        Response answer = Response.OK_JSON(format, answerString.toString());
        return answer;
    }

    private String getGeocode(String[] cmdArgs, Account account, User user, BasicPrivateLabel label) {
        String answer = null;
        JSON._Object json = new JSON._Object();
        String address = cmdArgs[0];
        // String country = cmdArgs[1]
        String country = null;
        Locale locale = account.getLocale();
        if (cmdArgs.length > 2) {
            country = cmdArgs[1];
        }
        if (address == null || address.isEmpty()) {
            return null;
        }
        if (country == null) {
            country = "";
        }
        GeocodeProvider geocodeProvider = label.getGeocodeProvider();
        ReverseGeocodeProvider reverseGeocodeProvider = label.getReverseGeocodeProvider();
        GeoPoint location = geocodeProvider.getGeocode(address, country);
        if (location == null) {
            return "";
        }
        /*ReverseGeocode reverseGeocode = reverseGeocodeProvider.getReverseGeocode(location,
                locale.getLanguage() + "," + locale.getCountry(), false);*/
        ReverseGeocode reverseGeocode = reverseGeocodeProvider.getReverseGeocode(location, false, locale.toString(), false, account.getAccountID(), null);

        json.addKeyValue("latitude", location.getLatitude());
        json.addKeyValue("longitude", location.getLongitude());

        if (reverseGeocode != null) {
            json.addKeyValue("address", reverseGeocode.getFullAddress());
        }

        answer = json.toString();

        return answer;
    }

}
