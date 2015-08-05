package nettymobile;

import java.util.*;
import java.sql.*;
import org.apache.log4j.*;

/**
 * Команды для работы с друзьями
 *
 * @author Yuri Efimov
 */
public class CmdFriends {

    static final String inviteMsg = "предлагает дружбу. Добавить в друзья?";

    public static final Logger LOG = Logger.getLogger(CmdFriends.class);
    static public String cmdGetFriends(HashMap p) throws SQLException {
        Connection con = DBMapper.getConnection();
        int cmdCode = MobileCommand.ACTION_GET_FRIENDS;
        int uid     = CmdHelper.getValue(p.get("uid"), 0);
        int online  = CmdHelper.getValue(p.get("online"), 1);
        int limit   = CmdHelper.getValue(p.get("limit"), 8);
        int offset  = CmdHelper.getValue(p.get("offset"), 0);
        Object local   = p.get("local");

        String preCode = "";
        // если мы вызвали процедуру из класса CmdSystem, то мы
        // должны вставить код команды вперёд
        if (local!=null) {
            preCode = cmdCode + "|";
        }
        // search all users
        String loginQuery = "";
        String sqlEnd = " AND u.online = ? LIMIT ? OFFSET ?";
        if (online==2) {
            loginQuery = "2";
            sqlEnd = " ORDER BY u.online DESC LIMIT 8 OFFSET 0";
        }
        LOG.info("CMD GET FRIENDS: uid = "+ uid+ "; online = " + online +
                "; limit =  "+limit + "; offset=" + offset);

        String sql = "SELECT u.online,u.uid,u.nick,u.gender,(YEAR(CURDATE())-YEAR(u.birth)) - " +
                " (RIGHT(CURDATE(),5)<RIGHT(u.birth,5)) as age,u.status, c.name as city " +
                " FROM " + DBMapper.TABLE_FRIENDS +
                " JOIN users as u ON (u.uid = friends.friend_uid) " +
                " LEFT JOIN cities as c ON (u.city_id = c.id) " +
                " WHERE friends.uid = ? " + sqlEnd;
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        if (online!=2) {
            ps.setInt(2, online);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
        }


        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            LOG.info("GET FRIENDS: uid " + uid + " has no friends");
            con.close();
            if (local!=null) {
                return cmdCode + "|0";
            }
            return "0"; // no friends
        }

        // f prefix for friend
        int fOnline      = rs.getInt("online");
        int fUid         = rs.getInt("uid");
        String fNick     = rs.getString("nick");
        String fGender   = rs.getString("gender");
        int fAge         = rs.getInt("age");
        String fStatus   = rs.getString("status");
        String fCity     = rs.getString("city");
        LOG.info(" FCITY: " + fCity);
        //|online|uid|nick|gender|age|status
        String friendsList = preCode + fOnline + "|" +fUid + "|" + fNick + "|" +
                fGender + "|" + fAge + "|" + fStatus+"|"+loginQuery;
        while (rs.next()) {
            fOnline      = rs.getInt("online");
            fUid         = rs.getInt("uid");
            fNick     = rs.getString("nick");
            fGender   = rs.getString("gender");
            fAge         = rs.getInt("age");
            fStatus   = rs.getString("status");
            fCity     = rs.getString("city");
            LOG.info(" FCITY: " + fCity);
            friendsList = friendsList + NettyMobileServerHandlerObj.OUTPUT_DELIMITER +
                    cmdCode + "|" +
                fOnline + "|" + fUid + "|" + fNick + "|" +
                fGender + "|" + fAge + "|" + fStatus+"|"+loginQuery;
        }
        con.close();
        friendsList = friendsList + NettyMobileServerHandlerObj.OUTPUT_DELIMITER
                +  cmdCode +  "|||";
        return friendsList;
    }

    /**
     * Добавить в друзья,
     * записать связь в таблицу friends,
     * отправить сообщение пользователю
     *
     * @todo Check friend uid online status
     * @param p
     * @return
     * @throws java.sql.SQLException
     */
    static public String cmdAddFriend(HashMap p) throws SQLException {

        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";
        int uid = CmdHelper.getValue(p.get("uid"), 0);
        int friend_uid  = CmdHelper.getValue(p.get("friend_uid"), 0);

        if (uid==friend_uid) {
            // you cannot be a friend to yourself
            con.close();
            return "3||";
        }
        
        // check whether this friendship already exists
        String sql = "SELECT COUNT(*) as c FROM " + DBMapper.TABLE_FRIENDS +
                " WHERE (uid = " + uid + " AND friend_uid = " + friend_uid + " )";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        rs.first();
        int friendshipCnt = rs.getInt("c");

        if (friendshipCnt>=1) {
            LOG.info("ADD FRIEND: friendship already exists");
            con.close();
            return "2|"+friend_uid+"|"; //
        }

        else {
            LOG.info("ADD FRIEND: new friendship");
            sql = "INSERT INTO " +
                    DBMapper.TABLE_FRIENDS + " (uid, friend_uid, requested, date ) " +
                    " VALUES (?,?,?, NOW())";
            ps = con.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.setInt(2, friend_uid);
            ps.setInt(3, 1);


            int rowAdded = ps.executeUpdate();

            LOG.info("ADD FRIEND CMD: uid = " + uid + "; friend_uid = " + friend_uid);
            LOG.info("Added: " + rowAdded);
        }

        sql = "SELECT online FROM " + DBMapper.TABLE_USERS +
                " WHERE uid = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, friend_uid);
        rs = ps.executeQuery();

        int isOnline = 0;
        if (!rs.first()) {
            LOG.info("CMD FRIEND ADD: there is no possible friend with uid: "
                    + friend_uid);
        }
        else {
            isOnline = rs.getInt("online");
            LOG.info("CMD FRIEND ADD: friend_uid is: " + isOnline 
                    + " online");

        }

        // check whether the reverse friendship already exists
        sql = "SELECT COUNT(*) as c FROM " + DBMapper.TABLE_FRIENDS +
                " WHERE (uid = " + friend_uid + " AND friend_uid = " + uid + " )";
        ps = con.prepareStatement(sql);
        rs = ps.executeQuery();

        rs.first();
        int reverseFriendshipCnt = rs.getInt("c");

        if (reverseFriendshipCnt>=1) {
            LOG.info("ADD FRIEND: reverse friendship already exists, " +
                    " we don't need to send a msg");
        }
        else {
            LOG.info("ADD FRIEND: send invite msg");
            // if it exists we don't need to send a invite msg
            HashMap msgParams = new HashMap();
            msgParams.put("msg",inviteMsg);
            msgParams.put("uid",String.valueOf(uid));
            msgParams.put("to",String.valueOf(friend_uid));

            // Send special message
            CmdMessages.cmdMessage(msgParams, 1);
        }
        CmdEvents.eventNewFriendship(uid, friend_uid);
        con.close();
        return "1|"+friend_uid+"|"+isOnline;
    }

    static public String cmdRemoveFriend(HashMap p) throws SQLException {
        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";
        int uid = CmdHelper.getValue(p.get("uid"), 0);
        int friend_uid  = CmdHelper.getValue(p.get("friend_uid"), 0);


        // check whether this friendship already exists
        String sql = "SELECT COUNT(*) as c FROM " + DBMapper.TABLE_FRIENDS +
                " WHERE (uid = " + uid + " AND friend_uid = " + friend_uid + " )";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        rs.first();
        int friendshipCnt = rs.getInt("c");

        if (friendshipCnt==0) {
            LOG.info("REMOVE FRIEND: no friendship to remove");
            con.close();
            return "2"; //
        }

        else {
            LOG.info("REMOVE FRIEND: we had friendship :(");
    
            sql = "DELETE FROM " +
                    DBMapper.TABLE_FRIENDS + " WHERE ( uid = ? AND friend_uid = ?) LIMIT 1";
                    
            ps = con.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.setInt(2, friend_uid);
            
            int rowAdded = ps.executeUpdate();

            LOG.info("ADD FRIEND CMD: uid = " + uid + "; friend_uid = " + friend_uid);
            LOG.info("Removed: " + rowAdded);
        }

        con.close();
        return "1";
    }
}
