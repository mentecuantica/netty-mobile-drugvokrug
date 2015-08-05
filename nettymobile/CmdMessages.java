/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.util.*;
import java.sql.*;
import org.apache.log4j.*;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import org.jboss.netty.channel.Channel;

/**
 * Содержит команды для обработки сообщений
 *
 * @author Yuri Efimov
 */
public class CmdMessages {
    public static final Logger LOG = Logger.getLogger(CmdMessages.class);

    static public String cmdGetHistory(HashMap p) throws SQLException {
        int from = Integer.parseInt((String)p.get("from"));
        LOG.info("GET MSG FROM UID = " + from);
        int to = Integer.parseInt((String)p.get("to"));
        LOG.info("GET MSG TO UID = " + to);
        int limit = Integer.parseInt((String)p.get("limit"));
        int offset = Integer.parseInt((String)p.get("offset"));
        int cmdCode = MobileCommand.ACTION_GETHISTORY;
        String sql = "SELECT msg, date, target FROM " + DBMapper.TABLE_MSGS +
                " WHERE (sender = " + from  + " OR sender= " + to  +
                ") AND (target =" + from + " OR target = " + to + ")  ORDER BY date DESC LIMIT ? OFFSET ?";

        LOG.info("SQL FOR MSG HISTORY " + sql);
        /* sql = "SELECT text, `when` FROM " + TABLE_MSGS +
                " WHERE `from` = 32 OR `from`= 14 " +
                " AND `to` = 14 OR `to` = 32 ORDER BY `when` DESC LIMIT ? OFFSET ?";
                */
        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";
        PreparedStatement ps = con.prepareStatement(sql);
        /*ps.setInt(1, from);
        ps.setInt(2, to);
        ps.setInt(3, from);
        ps.setInt(4, to);
        ps.setInt(5, limit);
        ps.setInt(6, offset); */
        ps.setInt(1, limit);
        ps.setInt(2, offset);
        ResultSet rs = ps.executeQuery();
        LOG.info("CMD MSG HISTORY: USER " + from + " & " + to);
        if (!rs.first()) {
            LOG.info("CMD MSG HISTORY: no msgs for " + from + " & " + to);
            con.close();
            return "0";
        }

        int i = 0;
        String text = rs.getString(1);
        //String when = rs.getString(2);
        Timestamp when = rs.getTimestamp(2);
        String strDate = CmdHelper.dateFormatterUTC(when);
        int toUid      = rs.getInt(3);

        String history = toUid + "|" + text + "|" + strDate;
        while (rs.next()) {
            text = rs.getString(1);
            //when = rs.getString(2);
            when = rs.getTimestamp(2);
            strDate = CmdHelper.dateFormatterUTC(when);
            toUid  = rs.getInt(3);
            history = history + NettyMobileServerHandlerObj.OUTPUT_DELIMITER
                    + cmdCode + "|"
                    + toUid + "|" + text + "|" + strDate;
        }
        LOG.info("HISTORY: " + history);
        con.close();
        history = history +NettyMobileServerHandlerObj.OUTPUT_DELIMITER +  cmdCode +  "|||";
        //return "1|"+i+"|"+ history;
        return history;
    }


    static public String sendNewMessages(int uid, int channelId) {
        Channel ch = NettyMobileServer.allChannels.find(channelId);
        if (ch instanceof Channel) {
            ch.write("");
        }
        return "";
    }

    static public String getNewMessages(int uid) throws SQLException {
        int cmdCode = MobileCommand.ACTION_GETMESSAGES;
        Connection con = DBMapper.getConnection();
        String sql = "SELECT id, msg, sender, date ,nick, btid, special_code FROM "
                +   DBMapper.TABLE_MSGS  + " JOIN " + DBMapper.TABLE_USERS
                + " ON (sender=uid)  WHERE target=? and is_read = 0 ORDER BY date ASC LIMIT 10";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        ResultSet rs = ps.executeQuery();
        if (!rs.first()) {
            LOG.info("CMD MSG FIRST: no msgs for " + uid);
            con.close();
            return cmdCode +"|"+ 0+NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        }

        int i = 0;
        int msgid = rs.getInt(1);
        String text = rs.getString(2);
        int from     = rs.getInt(3);
        Timestamp when = rs.getTimestamp(4);
  
        String strDate = CmdHelper.dateFormatterUTC(when);

        //java.sql.Date when = rs.getDate(4);
        String nick = rs.getString(5);
        String btid = rs.getString(6);
        int special_code = rs.getInt("special_code");
        String photo_url = "";

        
        String lastMsgs = cmdCode + "|"
                    + btid + "|" + nick +"|" +from +"|"
                    + strDate + "|" + photo_url + "|"
                    + text + "|"  + msgid +"|" + special_code;
        while (rs.next()) {
            msgid = rs.getInt(1);
            text = rs.getString(2);
            //when = rs.getDate(4);
            when = rs.getTimestamp(4);
            strDate = CmdHelper.dateFormatterUTC(when);
            //time = formatter.format(when);
            from  = rs.getInt(3);
            nick  = rs.getString(5);
            btid  = rs.getString(6);
            special_code = rs.getInt("special_code");
            lastMsgs = lastMsgs + NettyMobileServerHandlerObj.OUTPUT_DELIMITER +
                    cmdCode
                    + "|" + btid + "|" + nick +"|" +from +"|"
                    + strDate + "|" + photo_url + "|"
                    + text + "|"  + msgid + "|" + special_code ;
        }

        // update to read msgs
        sql = "UPDATE " + DBMapper.TABLE_MSGS + " SET is_read = 1 WHERE " +
                "  target=? and is_read = 0 ORDER BY date ASC LIMIT 10";
        
        ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        int rowsCnt = ps.executeUpdate();
        con.close();

        return lastMsgs;
    }

    static public String cmdGetMsgCount(HashMap p) throws SQLException {
        int uid = Integer.parseInt((String)p.get("uid"));

        String sql =
        "SELECT COUNT(*) as cnt FROM " + DBMapper.TABLE_MSGS +
                " WHERE target = ? AND is_read = 0";

        Connection con = DBMapper.getConnection();

        if (con==null) return "BAD";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        LOG.info("Getmessagecount: Try to find user with UID = " +
                uid);
        ResultSet rs = ps.executeQuery();
        if (!rs.first()) {
            LOG.error("Getmessagecount: User with UID = " + uid + " not found");
            return "BAD";
        }
        LOG.info("MSG COUNT (inner)");
        int messageCnt = rs.getInt(1);

        sql = "SELECT btid FROM " + DBMapper.TABLE_USERS
                + " WHERE uid = ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, uid);

        rs = ps.executeQuery();

        if (!rs.first()) {
            return "BAD";
        }

        String btid = rs.getString(1);

        String ret = btid + "|" + messageCnt;

        con.close();
        return ret;


    }

    static public String cmdMessage(HashMap p, int specialCode) throws SQLException {
        Connection con = DBMapper.getConnection();

        String msg = (String)p.get("msg");
        int uid = 0;
        int to = 0;

        
        uid = Integer.parseInt((String)p.get("uid"));
        to   = Integer.parseInt((String)p.get("to"));
        LOG.info("Prepare numbers uid = " + uid + "; to=" + to );
        
        String sql = "SELECT nick, btid FROM " + DBMapper.TABLE_USERS +
                " WHERE uid = ?";
        PreparedStatement ps1  = con.prepareStatement(sql);
        ps1.setInt(1, uid);
        ResultSet rs1 = ps1.executeQuery();
        if (!rs1.first()) {
            LOG.error("SENDER WITH UID = " + uid + " not found");
            con.close();
            return "BAD";
        }

        String senderNick = rs1.getString(1);
        String senderBtid = rs1.getString(2);

        java.util.Date st = new java.util.Date();
        LOG.info("Msg time: " + st.toString());
        LOG.info("Msg timestamp: " + st.getTime());
        //String sendTime = String.valueOf(st.getTime());
        String sendTime = CmdHelper.dateFormatterUTC(st);
        
        LOG.info("Msg date UTC: " + sendTime);
        
        String photoUrl = "";
        PreparedStatement ps;

        int read = 0;

        String ret = null;
        sql = "SELECT online, channel_id  FROM " +
                DBMapper.TABLE_USERS + " WHERE uid = ?";
        ps  = con.prepareStatement(sql);
        ps.setInt(1, to);
        ResultSet rs = ps.executeQuery();
        String msgRaw = "";
        if (!rs.first()) {
            LOG.info("There is no user with UID " + to);
            con.close();
            return "BAD";
        }
        else {
            int online = rs.getInt(1);
            if (online==1) {
                // Пользователь в сети, отправим ему сразу

                int chId = rs.getInt(2);
                Channel ch = NettyMobileServer.allChannels.find(chId);
                LOG.info("Online user channel_id: " + chId);
                LOG.info("User with UID " + to + " is online by DB ");
                if (ch instanceof Channel) {
                    LOG.info("User with UID " + to + " is online by channel ");
                    try {
                        msgRaw = "9" + "|" + senderBtid
                                + "|" + senderNick
                                + "|" + uid
                                + "|" + sendTime
                                + "|" + photoUrl
                                + "|" + msg + "||" + // empty msg id
                                specialCode +
                                NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
                        byte[] msgBytes = msgRaw.getBytes("UTF-8");
                    }
                    catch (UnsupportedEncodingException ex ) {
                        LOG.error("Wrong encoding " + ex);
                    }
                    LOG.info("MSG TO USER with UID " + to + ", " + msg);
                    ch.write(msgRaw);
                    read = 1;

                    // Сообщение отправилось он-лайн пользователю
                    ret = "2";
                }
            }
        }

        sql = "INSERT INTO " + DBMapper.TABLE_MSGS + " (msg, sender,target, is_read, date, special_code ) " +
                "VALUES (?, ?, ?, ?, NOW(), ? )";
        ps = con.prepareStatement(sql);


        LOG.info("MSG TO USER " + uid + " text: " + msg);

        ps.setString(1, msg);
        ps.setInt(2, uid);
        ps.setInt(3, to);
        ps.setInt(4, read);
        ps.setInt(5, specialCode);
        ps.executeUpdate();

        con.close();


        return (ret == null)?  "1" : ret;
        // Сообщение сохранилось в базе
        //return "1";
    }

    static public String cmdGetMessages(HashMap p) throws SQLException {
        int uid = Integer.parseInt((String)p.get("uid"));
        String sql = "";

        return "";
    }
}
