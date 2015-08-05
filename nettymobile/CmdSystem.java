/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.jboss.netty.channel.Channel;



/**
 * Содержит системный команды, такие как логин, meet, logout
 *
 * @author Yuri Efimov
 */
public class CmdSystem {


    public static final Logger LOG = Logger.getLogger(CmdSystem.class);


    /**
     * Логин для гостя, запишет данные в специальную таблицу GUESTS
     *
     * 
     *
     * @param btid
     * @param channelId
     * @return
     */
    static public UserSession cmdGuestLogin(String btid, int channelId) throws SQLException {

        int userType = UserSession.USER_GUEST;
        UserSession user = new UserSession(userType, true,0, btid);
        user.setChannelId(channelId);
        LOG.info("CMD GUEST LOGIN: btid: " + btid + "; channel_id =  " + channelId);

        String sql = "INSERT INTO " + DBMapper.TABLE_GUESTS + " (btid, channel_id) VALUES (?, ?) ";
        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, btid);
        ps.setInt(2, channelId);
        ps.execute();
        user.setTrustedCmdResult("1|0|0|0|0|0");
        return user;
    }
    
    /**
     * Login to system, parameters (cell, pass, bt)
     *
     * @param p
     * @return
     */
    static public UserSession cmdLogin(HashMap p, int channelId) throws SQLException {
        String pass = (String)p.get("pass");
        String btid = (String)p.get("btid");
        String cell = (String)p.get("cell");
        int userType = UserSession.USER_REGISTERED;
        if (cell.equals("0000000000")) {
            
            return cmdGuestLogin( btid, channelId);
        }
        String session = "0";
        Connection con = DBMapper.getConnection();
        if (con==null) return null;
            // CHECK WHETHER THIS USER WITH PWD EXISTS
            String sql = "SELECT counter, nick, uid, btid, gender FROM " +
                    DBMapper.TABLE_USERS
                    + " WHERE Cell='" + cell + "' AND Pass='" + pass +"'";
            ResultSet rs = con.createStatement().executeQuery(sql);
            LOG.info("CMD LOGIN SQL1: " + sql);
            if (!rs.first()) {
                con.close();
                return null;
            }
         
            int counter     = rs.getInt(1);
            String nick     = rs.getString(2);
            int uid         = rs.getInt(3);
            String btIdOld  = rs.getString(4);
            String gender   = rs.getString("gender");

            // Пользователь не найден
            if (uid==0) {
                con.close();
                return null;
            }

            sql = "SELECT cell, btid, nick from " + DBMapper.TABLE_USERS  + " WHERE btid='" +btid + "'";
            rs = con.createStatement().executeQuery(sql);
            //LOG.info("CMD LOGIN SQL2: " + sql);
            if (!rs.first()) {
                LOG.info("There is no user with btid= " + btid);
                // There is no user with current btid
            }
            else
                // Iterate through all BTID in result set to find if there are a

               while(rs.next()) {
                   String queryCell = rs.getString("cell");
                   if (!queryCell.equals(cell)) {
                       sql = "UPDATE " + DBMapper.TABLE_USERS + " SET btid='' WHERE Cell='" + queryCell +"'";
                       con.createStatement().execute(sql);
                   }

               }

               sql = "UPDATE " + DBMapper.TABLE_USERS +" SET last_time_online = ? , btid = ?, online = ?, sess_id = ?, channel_id = ? WHERE cell = ?";
               PreparedStatement ps = con.prepareStatement(sql);
               //LOG.info("LOGIN Update SQL: " + sql + " btid: " + btid +
                //       "|" + "channel id=" + String.valueOf(channelId));
               // Get the time, create sql timestamp, bind param
               ps.setTimestamp(1, new java.sql.Timestamp(new java.util.Date().getTime()));
               ps.setString(2, btid);
               ps.setInt(3, 1);
               session = CmdHelper.generateSession(btid, channelId);
               ps.setString(4, session );
               ps.setInt(5, channelId);
               ps.setString(6, cell);
               ps.executeUpdate();
                // We have to update meet also
               if (!btid.equals(btIdOld)) {
                   LOG.info("LOGIN MEET UPDATE: btid != oldbtid ");
                   LOG.info("LOGIN: old btid = " + btIdOld);
                   LOG.info("LOGIN: new btid = " + btid);
                   
                   sql = "UPDATE " + DBMapper.TABLE_MEET + " SET bt2= ?  WHERE bt2=? ";
                   PreparedStatement ps2 = con.prepareStatement(sql);
                   ps2.setString(1, btid);
                   //ps2.setString(2, btIdOld);
                   ps2.setString(2, btIdOld);
                   
                   ps2.executeUpdate();
               }



            // Результаты логина
            String ret = "1|"+ counter + "|" + session + "|" + nick + "|" + uid+"|"+gender +NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
            con.close();

            // Получить новые сообщения и отправить их в "потоке" логина
            String msgs  = CmdMessages.getNewMessages(uid);

            // Получить 8 онлайн друзей и отправить их в "потоке" логина
            HashMap getFriends = new HashMap();
            getFriends.put("uid", String.valueOf(uid));
            // 2 means to get first 8 users ordered by online
            getFriends.put("online", "2");
            getFriends.put("local", 1);

            String usersOnLogin       = CmdFriends.cmdGetFriends(getFriends);


            // Получить новые события и отправить их в "потоке" логина
            String[] onLoginParams = new String[15];
            onLoginParams[0] = String.valueOf(uid);
            String consolidatedEvents = CmdEvents.consolidateEvents(null, onLoginParams, true);


            // Результат в Хэшмэпе, потому что в вызывающем методе нам надо отделить некоторые
            // параметры
            // HashMap complexResult = new HashMap();
            // complexResult.put(1, uid);
            //complexResult.put(2, ret+msgs+usersOnLogin+consolidatedEvents);

            String loginResult = ret+msgs+usersOnLogin+consolidatedEvents;
            UserSession user = new UserSession(userType, true, uid, btid);
            user.setTrustedCmdResult(loginResult);
            // Отправить друзьям сообщение, что я-онлайн
            CmdUser.friendsStatusDelivery(uid, 1);
            CmdUser.meetsStatusDelivery(uid,1);

            return user;
            // return complexResult;
           // return "1|45|jkasd41232|Барабашка|6";

    }

    /**
     * @todo Чтобы клиент посылал также свой UID, и по ним и будем обрабатывать
     * сессию, и возвращать uid, как в логине
     *
     * @param p
     * @param channelId
     * @return
     * @throws java.sql.SQLException
     */
    static public UserSession cmdRelogin(HashMap p, int channelId) throws SQLException {

        String sess_id = (String)p.get("sess_id");
        int    uid     = CmdHelper.getValue(p.get("uid"),0);
        String sql = "UPDATE " + DBMapper.TABLE_USERS +
                " SET channel_id = ? , online =1  WHERE sess_id = ? AND uid = ?";
        Connection con = DBMapper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, channelId);
        ps.setString(2, sess_id);
        ps.setInt(3, uid);
        LOG.info("RELOGIN INFO: channelId + " + channelId + " sess_id " + sess_id+ "; uid" + uid);
        int rowCnt = ps.executeUpdate();
        con.close();
        if (rowCnt == 0) {
            // smell that smells smtlly
            // session not found , you have to login
            return null;
        }
        
        // session found, channel id renewed
        return new UserSession(true, uid);

    }

    /**
     * Встреча устройства, с клиента получает
     *   String btid - BT ID встреченного устройства
     *   String bt2name = Имя встреченного устройства
     *   String bt2type = Тип встреченного устройства
     * 
     * @param p
     * @param user
     * @return
     * @throws java.sql.SQLException
     */
    static public String cmdMeet(HashMap p, UserSession user) throws SQLException {
       // String uid     = Integer.parseInt((String)p.get("uid"));
        int uid     = user.getUserUid(); // Integer.parseInt((String)p.get("uid"));

        Connection con = DBMapper.getConnection();
        String bt2     = (String)p.get("bt2");
        String bt2name = (String)p.get("bt2name");
        String bt2type = (String)p.get("bt2type");
        String sql = "SELECT mid, meet_time, name, " +
        " UNIX_TIMESTAMP()-UNIX_TIMESTAMP(meet_time) as frommeet, " +
		" UNIX_TIMESTAMP()-UNIX_TIMESTAMP(lost_time) as fromlost, UNIX_TIMESTAMP() as uts, lost_time" +
		" FROM " + DBMapper.TABLE_MEET +	" WHERE uid= " + uid +
		" AND `bt2`='" + bt2 + "' ORDER BY mid DESC LIMIT 1";
        LOG.info("CMD MEET call: UID = " + uid + "; bt2= " + bt2 +
                "; bt2name= " + bt2name + "; bt2type="+ bt2type);
        LOG.info("CMD MEET SELECT SQL: " + sql);

        boolean firstDeviceMeet = false;
        ResultSet rs = con.createStatement().executeQuery(sql);
        String lostTime = "";
        if (!rs.first()) {
            LOG.info("CMD MEET: no lost time");
            firstDeviceMeet = true;
        }
        else {
            lostTime = rs.getString("lost_time");
            LOG.info("CMD MEET LOST TIME: " + lostTime);
        }
        boolean insertMeet = true;
        String dateCmp = "3000-00-00 00:00:00";

        // There is some difference in JDBC and MySql
        // .0 is added by getString("lost_time")
        String dateCmp2 =  "2999-11-30 00:00:00.0";
        String date2  =  "2999-11-30 00:00:00";

        boolean newCondition = !firstDeviceMeet && lostTime.equals(dateCmp2);

        int mid = 0;
        if (newCondition) {
            mid = rs.getInt("mid");
            insertMeet = false;
            sql = "UPDATE " + DBMapper.TABLE_MEET +  " SET meet_time=NOW() WHERE `mid`=" + mid;
            con.createStatement().executeUpdate(sql);
			LOG.info("CMD MEET MID (first condition): " + mid);
        }
        else if(!firstDeviceMeet && rs.getInt("fromlost")<900 && rs.getInt("fromlost")>1 ) {
            // встречали его меньше 15 минут назад, в базу не вносим, просто вовзращаем id встреи
            mid = rs.getInt("mid");
            insertMeet = false;
            sql = "UPDATE " + DBMapper.TABLE_MEET + " SET lost_time='" + date2 + "' WHERE " +
                    " mid = " + mid;

            LOG.info("CMD MEET LOST TIME SQL: " + sql);
            con.createStatement().executeUpdate(sql);
            LOG.info("CMD MEET MID (else condition): " + mid);
        }

        // upd = upd_c ? "true":"false";
        if (insertMeet)
        {
            sql = "INSERT INTO " + DBMapper.TABLE_MEET +
               " VALUES (0, ? , ?, ? , ? , NOW(), ?) ";
            LOG.info("CMD MEET INSERT SQL: " + sql +
                    " uid="+uid + " bt2=" + bt2 + "bt2name=" + bt2name +
                    "bt2type=" + bt2type + "date="+ dateCmp2
                    );
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.setString(2, bt2);
            ps.setString(3, bt2name);
            ps.setString(4, bt2type);
            ps.setString(5, dateCmp2);

            ps.executeUpdate();


        // увеличиваем общий счетчик встреч

           sql = "UPDATE " + DBMapper.TABLE_USERS + " SET counter=counter+1 WHERE uid = " + uid;
           LOG.info("CMD MEET COUNTER SQL: " + sql);
           con.createStatement().executeUpdate(sql);

           sql = "SELECT mid FROM " + DBMapper.TABLE_MEET + " WHERE uid=" + uid +
                " AND bt2='" + bt2 + "' ORDER BY mid DESC LIMIT 1";

           rs = con.createStatement().executeQuery(sql);


           mid = rs.first() ? rs.getInt(1) : 0;


        }



        sql = "SELECT counter FROM " + DBMapper.TABLE_USERS + " WHERE uid =" + uid;
		rs = con.createStatement().executeQuery(sql);
        LOG.info("CMD MEET common counter c1 " + sql);
        int commonCounter = 0;
        if (!rs.first()) {
            firstDeviceMeet = true;
        }
        else {
            commonCounter = rs.getInt(1);
        }
        // проверка данных о встречном
        sql = "SELECT 	nick,name, surname, uid, status, avatar, gender, channel_id, online,  " +
				"(YEAR(CURDATE())-YEAR(birth)) - (RIGHT(CURDATE(),5)<RIGHT(birth,5)) AS age " +
		" FROM " + DBMapper.TABLE_USERS + " WHERE btid='" + bt2 + "' ";

        rs = con.createStatement().executeQuery(sql);

        String user_nick = "";
        String user_name = "";
        String user_surname = "";
        int    user_uid     = 0;
        String user_status  = "";
        int    user_avatar  = 0;
        String user_gender  = "";
        int    user_age     = 0;
        int    user_online       = 0;
        int    user_channel_id   = 0;
        if (rs.first()) {
            user_nick    = rs.getString("nick");
            user_name    = rs.getString("name");
            user_surname = rs.getString("surname");
            user_uid     = rs.getInt("uid");
            user_status  = rs.getString("status");
            user_avatar  = rs.getInt("avatar");
            user_gender  = rs.getString("gender");
            user_age     = rs.getInt("age");
            user_channel_id   = rs.getInt("channel_id");
            user_online     = rs.getInt("online");
        }

        // Отправить ю
        if (user_uid!=0) {
            counterMeet(user_channel_id, user_online, uid);
        }
        // $user2 = $DB->GetRow($sql);

        // Конкретные встречи этого устройства с другим
	   sql = "SELECT COUNT(*) as c FROM " + DBMapper.TABLE_MEET + " WHERE uid=" + uid +
		" AND bt2='" + bt2 + "'";

       LOG.info("CMD MEET concrete: c2 "  + sql);
       rs = con.createStatement().executeQuery(sql);
       int concreteCounter = rs.first() ? rs.getInt(1) : 0;

	    //$c2 = $DB->GetOne($sql);
        con.close();

        String ret = bt2 + "|" + mid + "|";
        ret = ret + commonCounter + "|" + concreteCounter + "|";
        ret = ret + user_uid +  "|" + user_nick + "|" + user_gender+ "|";
        ret = ret + user_age + "|" + user_name + "|" + user_surname + "|";
        ret = ret + user_status + "|" + user_online;



        return ret;
    }

    /**
     * Отправить другому пользователю команду о встрече
     * нам нужен его uid для определения channel_id
     *
     * Отправим ему btid, того, кто встретил
     *
     * @param btid
     */
    static public void counterMeet(int channel_id, int online, int uid)
    throws SQLException
    {

        if (channel_id!=0 && online==1) {   
           String sql = "SELECT btid " +
            " FROM " + DBMapper.TABLE_USERS + " WHERE uid=?";

           Connection con = DBMapper.getConnection();
           PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, uid);
            LOG.info("CMD COUNTER MEET: " + uid);

            ResultSet rs = ps.executeQuery();

            if (!rs.first()) {
                LOG.info("CMD COUNTER MEET: no first device found");
                con.close();
                return;
            }
            else {
                // Получим BTID
                // Возьмём канал пользователя
                String btid= rs.getString("btid");
                Channel ch = NettyMobileServer.allChannels.find(channel_id);
                if (ch instanceof Channel) {
                    LOG.info("CMD COUNTER MEET: sending by netword " + btid );
                    ch.write(MobileCommand.ACTION_MEET_FROM_NET+"|"+btid+
                            NettyMobileServerHandlerObj.OUTPUT_DELIMITER
                            );
                }
                 con.close();
           }
        }
        else {
            LOG.info("CMD COUNTER MEET: either met user not online or on a channel");
        }
    }

    /**
     *  Надо идентифицировать пользователя текущей сессии
     * или просто сделать дисконнект
     * @param p
     * @return
     */
    static public String cmdLogout(HashMap p, UserSession user) throws SQLException {

        if (user.getUserType()==UserSession.USER_GUEST) {
            return cmdGuestLogout(p, user);
        }

        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";

        String sessionId = (String)p.get("session_id");
        LOG.info("CMD LOGOUT, SESSION_ID = " + sessionId);

        String sql = "SELECT uid FROM " + DBMapper.TABLE_USERS + " WHERE sess_id= ? ";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, sessionId);
        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            con.close();
            return "BAD";
        }
        int uid = rs.getInt(1);
        LOG.info("CMD LOGOUT, UID = " + uid);

        sql = "UPDATE " + DBMapper.TABLE_USERS + " SET sess_id=NULL,channel_id = 0, online = 0 WHERE sess_id= ? ";
        String date2  =  "2999-11-30 00:00:00";
        ps = con.prepareStatement(sql);
        ps.setString(1, sessionId);
        ps.executeUpdate();


        sql = "UPDATE " + DBMapper.TABLE_MEET +  " SET `lost_time`=NOW() WHERE uid= ? AND `lost_time`='2999-11-30 00:00:00'";
        ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        ps.executeUpdate();
        String ret = "";
        CmdUser.friendsStatusDelivery(uid, 0);
        CmdUser.meetsStatusDelivery(uid,0);
        con.close();
        return ret;
    }


    /**
     * Разлогиниться как гость
     * 
     * @param p
     * @param user
     * @return
     */
    static public String cmdGuestLogout(HashMap p, UserSession user) {

        String btid = (String)p.get("session_id");
        //LOG.info("CMD GUEST LOGOUT: ");
        
        return "";
    }

    /**
     * Потеряли пользователя
     *
     * @uses meet table
     * @param p
     * @return
     * @throws java.sql.SQLException
     */
    static public String cmdLost(HashMap p) throws SQLException {
        int mid = Integer.parseInt((String)p.get("mid"));

        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";
        String sql = "UPDATE " + DBMapper.TABLE_MEET + " SET lost_time = NOW() WHERE mid = " + mid;

        //LOG.info("CMD LOST SQL: " +sql);
        con.createStatement().executeUpdate(sql);
        con.close();
        return "1";
    }
    
}
