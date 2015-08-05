/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.log4j.*;

/**
 * Команда с клиента 
 * 1. Инкапсулирует запросы на сервер
 * 2. Парсинг параметров
 * 3. 
 * @author Yuri Efimov
 */
public class StringCmd {

    public static final Logger LOG = Logger.getLogger(StringCmd.class);

    
    public static HashMap PARAMS_MAP = null;

    private int cmdCode;
    private String params;
    private int channelId;
    private boolean close = false;

    private UserSession user;
    /*
    private boolean loggedIn = true;
    private int  loggedInUid = 0;
    */
    

    /**
     * Создать комманду
     *
     * @param cmdCode Код команды
     * @param params Строка параметров
     * @todo  Для каждой комманды брать коннект из пула
     */
    public StringCmd(int cmdCode, byte[] rawData, int channelId) {
        String inParams ="";
        try {
            inParams = new String(rawData,"UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LOG.error("Params decoding error");
        }
        
        LOG.info("NEW CMD "
                + cmdCode + " params: " 
                + inParams + "; channel_id: "
                + channelId + " created");
        this.cmdCode = cmdCode;
        this.params  = inParams;
        this.channelId = channelId;
        
    }
    
    /**
     * Проверка может ли эта команда выполняться незалогинненым пользователем
     * 
     * @return boolean
     */
    public boolean isTrustedCommand() {
        boolean condition = (cmdCode == MobileCommand.ACTION_LOGIN)
                        || (cmdCode == MobileCommand.ACTION_RELOGIN)
                        || (cmdCode == MobileCommand.ACTION_REGISTER);
        return condition;
    }

    public int getCmdCode() {
        return this.cmdCode;
    }
    /**
     * Выполнит команду, и вернёт резултат String
     * @return String
     */
    public String execute() {
        String retCode = "";
        LOG.info("CMD EXECUTE METHOD, uid : " + getUser().getUserUid());
        try {
            switch (this.cmdCode) {
                case MobileCommand.ACTION_MEET:     retCode = this.cmdMeet();break;
                case MobileCommand.ACTION_LOST:     retCode = this.cmdLost();break;
                case MobileCommand.ACTION_ECHO:     retCode = this.cmdEcho();break;
                case MobileCommand.ACTION_MESSAGE:  retCode = this.cmdMessage();break;
                case MobileCommand.ACTION_LOGIN:    retCode = this.cmdLogin(); break;
                case MobileCommand.ACTION_REGISTER: retCode = this.cmdRegister(); break;
                case MobileCommand.ACTION_LOGOUT:   retCode = this.cmdLogout(); break;
                case MobileCommand.ACTION_GETMSGCOUNT:
                    retCode = this.cmdGetMsgCount(); break;
                case MobileCommand.ACTION_GETMESSAGES:
                    retCode = this.cmdGetMessages(); break;
               case MobileCommand.ACTION_GETHISTORY:
                    retCode = this.cmdGetHistory(); break;
                case MobileCommand.ACTION_RELOGIN:
                    retCode = this.cmdRelogin(); break;
                case MobileCommand.ACTION_SEARCH:
                    retCode = this.cmdSearch(); break;
                case MobileCommand.ACTION_SET_STATUS:
                    retCode = this.cmdSetStatus(); break;
                case MobileCommand.ACTION_ADD_FRIEND:
                    retCode =  this.cmdAddFriend(); break;
                case MobileCommand.ACTION_REMOVE_FRIEND:
                    retCode = this.cmdRemoveFriend(); break;
                case MobileCommand.ACTION_GET_FRIENDS:
                    retCode = this.cmdGetFriends(); break;
                case MobileCommand.ACTION_GET_INTERESTING:
                    retCode = this.cmdGetInteresting(); break;
                case MobileCommand.ACTION_GET_LEADERS:
                    retCode = this.cmdGetLeaders(); break;

                default:              retCode = this.cmdDefault();
            }
           
            // return retCode.getBytes();
            return (new Integer(this.cmdCode) + "|" + retCode);
        }
        catch (SQLException e) {
            LOG.error(e.getMessage() + e.getSQLState());
            e.printStackTrace();
            return "BAD";
        }
    }

    /**
     * Login
     *
     *
     * 3 input параметра Cell, Pass, BT
     *
     * 5 output 
     *
     * @return
     */
    public String cmdLogin() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        
        // получим null или HashMap , где первый параметр uid
        // второй вывод команды
        // в сеть отправим вывод команды
        // если null получили, значит в сеть отправим "0"
        UserSession loginUser  =  CmdSystem.cmdLogin(parameters,getChannelId());
        if (loginUser==null) {
            return "0";
        }
        else {
            this.setUser(loginUser);
            return loginUser.getTrustedCmdResult();
        }
    }

    

    public String cmdGetHistory() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdMessages.cmdGetHistory(parameters);
        return retCode;
    }

    public String cmdGetMsgCount() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        
        String retCode = CmdMessages.cmdGetMsgCount(parameters);
        return retCode;
    }

    public String cmdMessage() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);

        // special code = 0 for non friendship messages
        String retCode = CmdMessages.cmdMessage(parameters, 0);
        return retCode;
    }

    public String cmdRelogin() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
                
        UserSession user1 = CmdSystem.cmdRelogin(parameters,getChannelId());
        if (user1==null) {
            return "0";
        }
        else {
            this.setUser(user1);
            return "1";
        }
     }

    public String cmdEcho() {
        HashMap parameters = decodeParamsString(cmdCode, params);
        return (String)parameters.get("rnd");
    }

    private String cmdGetInteresting() throws SQLException {
        //String[] parameters = new String[15];
        //CmdHelper.formResult(params, parameters);
        String retCode = CmdUser.cmdGetInteresting(this.getUser());
        return retCode;
    }

    private String cmdGetLeaders() throws SQLException {
        //String[] parameters = new String[15];
        //CmdHelper.formResult(params, parameters);
        String retCode = CmdUser.cmdGetLeaders(this.getUser());
        return retCode;
    }

    /**
     * Потеря устройства
     * Один параметр - mid - ID встречи
     *
     * @return
     * @throws java.sql.SQLException
     */
    private String cmdLost() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdSystem.cmdLost(parameters);
        return retCode;
    }

    /**
     * Встреча устройства
     *
     *
     * @return mid
     * @throws java.sql.SQLException
     */
    private String cmdMeet() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdSystem.cmdMeet(parameters, this.getUser());
        return retCode;
    }

    /**
     * Правильный дисконнект устройства
     *
     * 
     * @todo Передавать по сессии, менять таблицу users (login)
     *
     */
    private String cmdLogout() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdSystem.cmdLogout(parameters, getUser());
        
        this.close = true;
        return retCode;
    }

    private String cmdRegister() throws SQLException {
        //HashMap parameters = decodeParamsString(cmdCode, params);
        String[] parameters = new String[15];
        CmdHelper.formResult(params, parameters);
        
        String retCode = CmdUser.cmdRegister(parameters);

        return retCode;
    }

    private String cmdGetMessages() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdMessages.cmdGetMessages(parameters);
        
        return retCode;
    }

    private String cmdDefault() {
        return "OK";
    }

    /**
     * Используем метод Артёма, для парсинга
     * @return
     * @throws java.sql.SQLException
     */
    private String cmdSearch() throws SQLException {
        //HashMap parameters = decodeParamsString(cmdCode, params);
        String[] p = new String[15];
        CmdHelper.formResult(params, p);
        String retCode = CmdUser.cmdSearch(p, this.getUser());
        return retCode;
    }
     
    private static HashMap decodeParamsString(int cmdCode,String s) {

        if (PARAMS_MAP == null) {
            PARAMS_MAP = new HashMap();
            PARAMS_MAP.put(MobileCommand.ACTION_REGISTER,
                    new String[]{"nick","name","surname","pass","dob",
                    "mob","yob","cell","sex","bt","city"
            });
            PARAMS_MAP.put(MobileCommand.ACTION_LOGIN,
                    new String[]{"cell","pass","btid"
            });

            PARAMS_MAP.put(MobileCommand.ACTION_MEET,
                    new String[]{"uid","bt2","bt2name","bt2type"
            });

            PARAMS_MAP.put(MobileCommand.ACTION_LOGOUT,
                    new String[]{"session_id"
            });

            PARAMS_MAP.put(MobileCommand.ACTION_LOST,
                    new String[]{"mid"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_ECHO,
                    new String[]{"rnd"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_MESSAGE,
                    new String[]{"msg","uid","to"}
                    );

            PARAMS_MAP.put(MobileCommand.ACTION_GETMESSAGES, new String[]{
                "uid","date"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_GETMSGCOUNT, new String[]{
                "uid"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_RELOGIN, new String[]{
                "sess_id","uid"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_GETHISTORY, new String[]{
                "from","to","limit","offset"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_SET_STATUS, new String[]{
                "uid","status"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_SEARCH, new String[]{
                "type","1","2","3","4","5","limit", "offset"}
            );

            PARAMS_MAP.put(MobileCommand.ACTION_ADD_FRIEND, new String[]{
                "uid","friend_uid"});
            
            PARAMS_MAP.put(MobileCommand.ACTION_REMOVE_FRIEND, new String[]{
                "uid","friend_uid"});

            PARAMS_MAP.put(MobileCommand.ACTION_GET_FRIENDS, new String[]{
                "uid","online","limit","offset"});
        }

        StringTokenizer st = new StringTokenizer(s, "|");

         HashMap params = new HashMap();

         int idx = 0;
         while (st.hasMoreTokens()) {
             String pair = st.nextToken();
             String key = ((String[])PARAMS_MAP.get(cmdCode))[idx];
             params.put(key, pair);
             idx++;
         }

        return params;
    }

    /**
     * Закрывать ли соединение после выполнения команды
     * @return флаг закрытия соединения
     */
    public boolean isClose() {
        return this.close;
    }
    
    @Override
    public String toString() {
        return ("Cmd_code_" + this.cmdCode);
    }

    private String cmdSetStatus() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdUser.setStatus(parameters);
        return retCode;
    }

    private String cmdAddFriend() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdFriends.cmdAddFriend(parameters);
        return retCode;
    }

    private String cmdRemoveFriend() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdFriends.cmdRemoveFriend(parameters);
        return retCode;
    }

    private String cmdGetFriends() throws SQLException {
        HashMap parameters = decodeParamsString(cmdCode, params);
        String retCode = CmdFriends.cmdGetFriends(parameters);
        return retCode;
    }

    /**
     * @return the user
     */
    public UserSession getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserSession user) {
        this.user = user;
    }

    /**
     * @return the channelId
     */
    public int getChannelId() {
        return channelId;
    }
}
