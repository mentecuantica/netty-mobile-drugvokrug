/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;




/**
 * Содержит команды по событиям и их обработке
 * (получение, он-лайн рассылка)
 *
 * @author Yuri Efimov
 */
public class CmdEvents {

    public static final int EVENT_FRIENDSHIP = 1;
    public static final int EVENT_STATUS     = 2;
    public static final int EVENT_GIFT       = 3;
    public static final int EVENT_AVATAR     = 4;



    public static final Logger LOG = Logger.getLogger(CmdEvents.class);

    /**
     * Добавить событие дружба в таблицу
     */
    public static void eventNewFriendship(int uid, int friend_uid) throws SQLException {
        Connection con = DBMapper.getConnection();
        String sql = "SELECT nick FROM " + DBMapper.TABLE_USERS + " WHERE uid = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        ResultSet rs = ps.executeQuery();
        rs.first();
        String nick = rs.getString("nick");
        String description = nick;

        sql = "INSERT INTO " + DBMapper.TABLE_EVENTS + " (event_type, description, uid, event_uid, date) " +
                " VALUES (?, ?, ?, ?, NOW())";
        
        

        ps = con.prepareStatement(sql);

        ps.setInt(1, EVENT_FRIENDSHIP);
        ps.setString(2, description);
        ps.setInt(3, uid);
        ps.setInt(4, friend_uid);
        int resultCnt = ps.executeUpdate();
        if (resultCnt==1) {
            LOG.info("EVENT FRIENDSHIP LOGGED uid: " + uid + " has new friend: " + friend_uid);
        }
        else {
            LOG.info("EVENT FRIENDSHIP FAILED");
        }

        sql = "SELECT u.nick, u.uid, u.online, u.status, u.gender, " +
                "10 as age FROM " + DBMapper.TABLE_USERS + " as u  WHERE uid = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, friend_uid);

        rs = ps.executeQuery();

        if (!rs.first()) {
            LOG.info("EVENT NEW FRIENDSHIP: no friend ");
            con.close();
            return ;
        }

        java.util.Date when      = new java.util.Date();
        String eventDate    = CmdHelper.dateFormatterUTC(when);
        String userNick     = rs.getString("nick");
        int userUid         = rs.getInt("uid");
        int userAge         = rs.getInt("age");
        String userGender   = rs.getString("gender");
        int userOnline      = rs.getInt("online");
        String userStatus   = rs.getString("status");

        String eventMsg =   formEventMessage(EVENT_FRIENDSHIP, description, userNick);

        
        String msg = MobileCommand.ACTION_GET_EVENTS + "|" +
                    EVENT_FRIENDSHIP +"|"+eventMsg + "|" + uid+ "|"+ nick + "|" +
                    userOnline + "|" +
                    userUid + "|" + userNick + "|" + userGender + "|"   +
                    userAge + "|" + userStatus  +"|" + eventDate;
        con.close();
        LOG.info("EVENT FRIENDSHIP: msg " + msg);
        eventsDelivery(uid, msg, MobileCommand.ACTION_EVENTS_DELIVERY);
    }




    /**
     * Добавить изменение статуса в таблицу
     */
    public static void eventNewStatusChange(int uid, String status) throws SQLException {
        Connection con = DBMapper.getConnection();
        String sql = "INSERT INTO " + DBMapper.TABLE_EVENTS + " (event_type, description, uid, event_uid, date) " +
                " VALUES (?, ?, ?, ?, NOW())";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, EVENT_STATUS);
        ps.setString(2, status);
        ps.setInt(3, uid);
        ps.setInt(4, uid);
        int resultCnt = ps.executeUpdate();
        if (resultCnt==1) {
            LOG.info("EVENT STATUS LOGGED uid: " + uid + " has new status: " + status);
        }
        else {
            LOG.info("EVENT STATUS FAILED");
        }
        con.close();
    }

    /**
     * Добавить аватар в таблицу событий
     * События с аватарами не должны повторяться,
     * поэтому сначала удалим все старые
     */
    public static void eventNewAvatar(int uid) throws SQLException {
        Connection con = DBMapper.getConnection();

        String sql = "DELETE FROM  " + DBMapper.TABLE_EVENTS 
                + " WHERE event_type = ? AND uid = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, EVENT_AVATAR);
        ps.setInt(2, uid);
        int deletedRows = ps.executeUpdate();


        sql = "INSERT INTO " + DBMapper.TABLE_EVENTS + " (event_type, description, uid, event_uid, date) " +
                " VALUES (?, ?, ?, ?, NOW())";
        ps = con.prepareStatement(sql);
        ps.setInt(1, EVENT_AVATAR);
        ps.setString(2, "");
        ps.setInt(3, uid);
        ps.setInt(4, uid);
        int resultCnt = ps.executeUpdate();
        if (resultCnt==1) {
            LOG.info("EVENT NEW AVATAR uid: " + uid);
        }
        else {
            LOG.info("EVENT NEW AVATAR FAILED");
        }
        sql = "SELECT nick FROM " + DBMapper.TABLE_USERS + " WHERE uid = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        ResultSet rs = ps.executeQuery();
        rs.first();
        String nick = rs.getString("nick");
        String description = nick;

        String msg = "0";
        
        con.close();
        LOG.info("EVENT FRIENDSHIP: msg " + msg);
        eventsDelivery(uid, msg, MobileCommand.ACTION_AVATAR_DELIVERY);
       
    }

    public static void eventNewGift() {

    }

    /**
     * Собрать все события и отправить их
     * Может вызываться локально (например из метода Login) (параметр local)
     * Или запрашиваться как команда с клиента
     * 
     */
    public static String consolidateEvents(HashMap p, String[] p1, boolean local) throws SQLException {

        int uid = Integer.parseInt(p1[0]);
        int limit = 10;
        int offset = 0;

        if (local == false) {
            limit  = Integer.parseInt(p1[1]);
            offset = Integer.parseInt(p1[2]);
        }

        
        String sql = "SELECT e.event_type, e.description, e.date, e.uid as event_uid, u.nick, u.uid,  " +
                " e.uid as event_uid, u.nick, u.uid, u.online, u.status, u.gender, 10 as age " +
                " FROM " + DBMapper.TABLE_EVENTS + " e JOIN " + DBMapper.TABLE_FRIENDS +
                " f ON (f.friend_uid=e.uid) " +
                " LEFT OUTER JOIN " +DBMapper.TABLE_USERS
                + " u ON (u.uid=e.event_uid) WHERE f.uid= ? ORDER BY e.date DESC LIMIT ? OFFSET ? ";

        // если мы вызвали процедуру из класса CmdSystem, то мы
        // должны вставить код команды вперёд
        String preCode = "";
        int cmdCode = MobileCommand.ACTION_GET_EVENTS;
        if (local==true) {

            preCode = NettyMobileServerHandlerObj.OUTPUT_DELIMITER + MobileCommand.ACTION_EVENTS_ON_LOGIN + "|";
            cmdCode = MobileCommand.ACTION_EVENTS_ON_LOGIN;
        }
        
        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, uid);
        ps.setInt(2, limit);
        ps.setInt(3, offset);


        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            LOG.info("CMD EVENTS: no events for user: " + uid);
            con.close();
            return preCode + "0"+NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        }
        
        int eventType       = rs.getInt("event_type");
        String description  = rs.getString("description");
        int eventUid        = rs.getInt("event_uid");
        Timestamp when      = rs.getTimestamp("date");
        String eventDate    = CmdHelper.dateFormatterUTC(when);
        String userNick     = rs.getString("nick");
        int userUid         = rs.getInt("uid");
        int userAge         = rs.getInt("age");
        String userGender   = rs.getString("gender");
        int userOnline      = rs.getInt("online");
        String userStatus   = rs.getString("status");


        String eventMsg = formEventMessage(eventType, description, userNick);
        //event_type|event_msg|uid1|online|uid2|nick2|gender|age|status|datetime
        String eventNick = "";
        if (eventType== EVENT_FRIENDSHIP) {
            eventNick = description;
        }
        else {
            eventNick = userNick;
        }
        String events = eventType+"|"+eventMsg + "|" + eventUid+ "|"+ eventNick + "|" +
                userOnline + "|" +
                userUid + "|" + userNick + "|" + userGender + "|"   +
                userAge + "|" + userStatus +"|" + eventDate;
         while (rs.next()) {
            eventType    = rs.getInt("event_type");
            description  = rs.getString("description");
            eventUid     = rs.getInt("event_uid");
            when         = rs.getTimestamp("date");
            eventDate    = CmdHelper.dateFormatterUTC(when);
            userNick     = rs.getString("nick");
            userUid      = rs.getInt("uid");
            userAge      = rs.getInt("age");
            userGender   = rs.getString("gender");
            userOnline   = rs.getInt("online");
            userStatus   = rs.getString("status");
            if (eventType== EVENT_FRIENDSHIP) {
                eventNick = description;
            }
            else {
                eventNick = userNick;
            }
            eventMsg = formEventMessage(eventType, description, userNick);
            events = events + NettyMobileServerHandlerObj.OUTPUT_DELIMITER +
                    cmdCode + "|" +
                    eventType + "|"+eventMsg + "|" + eventUid+ "|"+ eventNick + "|" +
                    userOnline + "|" +
                    userUid + "|" + userNick + "|" + userGender + "|"   +
                    userAge + "|" + userStatus  +"|" + eventDate;
         }

        
        events = preCode + events +
                NettyMobileServerHandlerObj.OUTPUT_DELIMITER +  cmdCode +  "|||";
        
        
        

        con.close();
        return events;
    }

    /**
     * Сформировать строку события по типу события, по описанию,
     * и по никнейму пользователя
     * 
     * @param eventType
     * @param description
     * @param userNick
     * @return
     */
    public static String formEventMessage(int eventType, String description, String userNick) {
        String eventMsg = "";
        switch (eventType) {
            // В случае eventType = EVENT_FRIENDSHIP
            // description это Ник пользователя, который добавил другого пользователя
            case EVENT_FRIENDSHIP:
                eventMsg = "Пользователь " + description +
                        " добавил в друзья пользователя " + userNick;
                break;
            // В случае eventType = EVENT_STATUS
            // description это Новый статус пользователя
            // eventUid = userUid
            case EVENT_STATUS:
                eventMsg = description;
                break;
            case EVENT_GIFT:
                eventMsg = "Пользователь получил подарок";
                break;
            case EVENT_AVATAR:
                eventMsg = "Пользователь обновил фотографию";
                break;
            default:
                eventMsg = "Дефолтное событие!?";
        }

        return eventMsg;
    }

    /**
     * Отправка событий друзьям, он-лайн
     *
     * @param uid
     * @param online
     * @throws java.sql.SQLException
     */
    static public void eventsDelivery(int uid, String strEvent, int actionParameter) throws SQLException {
        LOG.info("CMD EVENTS DELIVERY: uid " + uid + "; msg " + strEvent);

        // Friends of UID
        String sql = "SELECT u.channel_id FROM " + DBMapper.TABLE_USERS +
                "  as u JOIN " + DBMapper.TABLE_FRIENDS +
                " as f ON (f.friend_uid = u.uid) WHERE f.uid = ? and u.online = 1 ";

        // People who has an UID as friend
        String sql2 = " UNION SELECT u.channel_id FROM " + DBMapper.TABLE_USERS + " as u JOIN " +
                DBMapper.TABLE_FRIENDS + " as f ON (f.uid = u.uid) " +
                " WHERE f.friend_uid = ? and u.online = 1";
        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql+sql2);
        ps.setInt(1, uid);
        ps.setInt(2, uid);

        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            LOG.info("CMD EVENTS DELIVERY: no online friends");
        }
        else {
           LOG.info("CMD EVENTS DELIVERY: some friends are online");
            int channel_id = rs.getInt("channel_id");

            String msg = actionParameter + "|" + uid + "|" + strEvent;

            CmdHelper.sendOverChannel(channel_id, msg);
            while (rs.next()) {
                channel_id = rs.getInt("channel_id");
                CmdHelper.sendOverChannel(channel_id, msg);

            }

        }
        con.close();
    }

}