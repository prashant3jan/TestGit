package org.opengts.war.track.custom;

import org.opengts.db.BasicPrivateLabel;
import org.opengts.db.PushpinIcon;
import org.opengts.db.tables.Account;
import org.opengts.db.tables.Device;
import org.opengts.db.tables.DeviceGroup;
import org.opengts.db.tables.EventData;
import org.opengts.db.tables.User;
import org.opengts.dbtools.DBReadWriteMode;
import org.opengts.dbtools.DBException;
import org.opengts.util.JSON;
import org.opengts.util.JSON.JSONBeanFilter;
import org.opengts.util.OrderedMap;
import org.opengts.util.OrderedSet;
import org.opengts.util.Print;
import org.opengts.war.ctrac.CommandInterface;
import org.opengts.war.ctrac.CommandInterfaceAdapter;
import org.opengts.war.ctrac.Response;
import org.opengts.war.ctrac.ServiceContextInterface;
import org.opengts.war.tools.PrivateLabel;
import org.opengts.war.tools.RequestProperties;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class DeviceCommand
    extends CommandInterfaceAdapter
    implements CommandInterface
{
    public static final String COMMAND_DeviceList[]      = { "C_deviceList"     , "C_getDeviceList"      };
    public static final String COMMAND_DeviceGroupList[] = { "C_deviceGroupList", "C_getDeviceGroupList" };
    public static final String COMMAND_DeviceInfo[]      = { "C_deviceInfo"     , "C_getDeviceInfo"      };

    public DeviceCommand()
    {
        super();
        this.setCommandNames(new String[][] {COMMAND_DeviceList, COMMAND_DeviceGroupList, COMMAND_DeviceInfo});
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
        if(this.commandMatch(COMMAND_DeviceList, cmd))
        {
            answerString = getDeviceList(cmdArgs, account, user, privLabel, reqProp);
        }
        else if(this.commandMatch(COMMAND_DeviceGroupList, cmd))
        {
            answerString = getDeviceGroupList(cmdArgs, account, user, privLabel);
        }
        else if(this.commandMatch(COMMAND_DeviceInfo, cmd))
        {
            answerString = getBalloonInfo(cmdArgs, account, user, privLabel);
        }

        Response answer = Response.OK_JSON(format, answerString.toString());
        return answer;
    }


    /**
     * 
     * @param cmdArg Arguments for the Command
     * @param account Account of the call
     * @param user User of the current call
     * @param label Private label of the current user
     * @param requestProperties Request properties
     * @return
     */
    private String getDeviceList(String[] cmdArg, Account account, User user, BasicPrivateLabel label, RequestProperties requestProperties)
    {
        String answer = null;
        String groupId = "all";
        Locale locale = Account.GetLocale(account);
        Account.SpeedUnits speedUnit = (user != null) ? Account.getSpeedUnits(user) : Account.getSpeedUnits(account);

        if(cmdArg.length != 0)
        {
            groupId = (cmdArg[0].equals("")) ? "all" : cmdArg[0];
        }

        OrderedSet<String> deviceIdList = null;
        try
        {
            deviceIdList = DeviceGroup.getDeviceIDsForGroup(DBReadWriteMode.READ_ONLY, account.getAccountID(), groupId, user, false);
        }
        catch(DBException ex)
        {
            Print.logWarn("Could not read device list from Database %s", ex.toString());
            return null;
        }
        OrderedMap<String, PushpinIcon> pushpinMap = requestProperties.getMapProviderIconMap();
        JSONBeanFilter filter = new JSONBeanFilter("getDeviceList");
        //Vector<JSON._Object>devices = new Vector<JSON._Object>();
        JSON._Array devices = new JSON._Array();
        for(int i = 0; i < deviceIdList.size(); i++)
        {
            try
            {
                Device dev = Device._getDevice(account, deviceIdList.get(i));
                dev.setPushpinIcon(pushpinMap);
                PushpinIcon icon = dev.getPushpinIcon();

                devices.addValue(dev);
            }
            catch(DBException ex)
            {
                Print.logError("Could not access device "+deviceIdList.get(i));
            }
        }
        answer = devices.toString();
        return answer;
    }

    /**
     * 
     * @param cmdArg
     * @param account
     * @param user
     * @param label
     * @return
     */
    private String getDeviceGroupList(String[] cmdArg, Account account, User user, BasicPrivateLabel label)
    {
        String answer = null;
        List<String> deviceGroups = null;
        JSON._Object groupListContainer = new JSON._Object();
        Vector<JSON._Object> deviceGroupList = new Vector<JSON._Object>();

        try {
            if(user != null)
            {
                deviceGroups = user.getAllAuthorizedDeviceGroupIDs(DBReadWriteMode.READ_ONLY);
            }
            else
            {
                deviceGroups = DeviceGroup.getDeviceGroupsForAccount(DBReadWriteMode.READ_ONLY, account.getAccountID(), true);
            }

        } catch(DBException ex) {
            Print.logError("Could not read from database: %s", ex.toString());
            return null;
        }
        for(String groupId : deviceGroups) {
            JSON._Object jsonGroupList = new JSON._Object();
            try {
                DeviceGroup deviceGroup = DeviceGroup.getDeviceGroup(account, groupId);
                if(deviceGroup != null)
                {
                    String deviceGroupName = deviceGroup.getDescription();
                    if(deviceGroupName.equals(""))
                    {
                        deviceGroupName = deviceGroup.getGroupID();
                    }
                    jsonGroupList.addKeyValue("groupId", deviceGroup.getGroupID());
                    jsonGroupList.addKeyValue("groupName", deviceGroupName);
                    deviceGroupList.add(jsonGroupList);
                }

            } catch(DBException ex) {
                Print.logError("Could not read device gruop names: %s", ex.toString());
                return null;
            }
        }
        //groupListContainer.addKeyValue("DeviceGroupList", jsonGroupList);
        //answer = new JSON(deviceGroupList).toString().getBytes();
        answer = deviceGroupList.toString();

        return answer;
    }


    
    private String getBalloonInfo(String[] cmdArgs, Account account, User user, BasicPrivateLabel label)
    {
        String answer = null;
        if(cmdArgs.length == 0)
        {
            return null;
        }
        if(cmdArgs.length == 1)
        {
            return "";
        }
        Device device = null;
        EventData lastEvent = null;
        try
        {
            device = Device._getDevice(account, cmdArgs[0]);
            lastEvent = device.getLastEvent(true);
        }
        catch(DBException ex)
        {
            Print.logWarn("The device %s was not found", cmdArgs[0]);
            device = null;
            return null;
        }

        Vector<JSON._Array> cols = new Vector<JSON._Array>();

        for(int i = 1; i < cmdArgs.length; i++)
        {
            JSON._Array row = new JSON._Array();
            String colTitle = cmdArgs[i];
            String[] stringArray = getValue(device, lastEvent, account, user, colTitle, (PrivateLabel) label);
            
            if(stringArray == null)
            {
                continue;
            }

            String colName = stringArray[0];
            String value = stringArray[1];

            row.addValue(colName);
            row.addValue(value);
            
            cols.add(row);
        }
        
        answer = cols.toString();
        return answer;
    }

    
    private String[] getValue(Device device, EventData lastEvent, Account account, User user, String colName, PrivateLabel label)
    {
        String colTitle = null;
        String colValue = null;
        EventData data = null;

        try
        {
            data = device.getLastEvent(true);
        }
        catch(DBException ex)
        {
            Print.logError("Coult not fetch device %s", device.getDeviceID());
        }
        if(data == null)
        {
            colValue = device.getFieldName(colName);
            if(colValue == null || colValue.isEmpty())
            {
                return null;
            }
            return new String[] {colTitle, colValue};
        }
        else
        {
            colTitle = EventData.getKeyFieldTitle(colName, null, label.getLocale());
            colValue = data.getFieldValue(colName, "");
            return new String[] {colTitle, colValue};
        }
    }
}
