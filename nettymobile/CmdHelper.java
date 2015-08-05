
package nettymobile;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.HashMap;
import org.jboss.netty.channel.Channel;

/**
 *
 * Содержит вспомогательные методы, для обработки команд
 *
 * @author Yuri Efimov
 */
public class CmdHelper {

    public static final Logger LOG = Logger.getLogger(CmdHelper.class);
    /**
     * Сгенерировать уникалый идентификатор сессии по параметрам
     * (потом сохранить его в базе данных)
     *
     * @param btid
     * @param channelId
     * @return
     */
    static public String generateSession(String btid, int channelId) {
       String session = "";
        try {
            MD5 md = MD5.getInstance();
             session = md.hashData((btid + channelId).getBytes());
       }
       catch (NoSuchAlgorithmException e)
         {
             e.printStackTrace(System.out);
         }
        return session;
    }

    static String getValue(Object value, String defaultVal) {
        if (value==null) {
            return defaultVal;
        }
        return (String)value;
    }

    static int getValue(Object value, int defaultVal) {
        if (value==null || value=="") {
            return defaultVal;
        }
        return (Integer.parseInt((String)value));
    }

    static boolean getValue(Object value, boolean defaultVal) {
        if (value==null) {
            return defaultVal;
        }
        String tmp = ((String)value);
        return (tmp.equals("1")) ? true : false;
        
    }

    /**
     * Форматирует дату в формате
     * Fri May 15 11:01:05 UTC 2009
     * 
     * @param date
     * @return String
     */
    public static String dateFormatterUTC(Date date) {
        
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' yyyy", Locale.ENGLISH );
        
        return formatter.format(date);
    }

    static public void sendOverChannel(int channel_id, String msg) {
        LOG.info("ONLINE DELIVERY: try to send by netword " + msg );
        if (channel_id==0) return;
        Channel ch = NettyMobileServer.allChannels.find(channel_id);
        if (ch instanceof Channel) {
            LOG.info("ONLINE DELIVERY: sending by netword " + msg );
                     ch.write(msg+ NettyMobileServerHandlerObj.OUTPUT_DELIMITER);
        }
    }

      /**
     * Artem method
     * @param result
     * @param resultFields
     */
    public static void formResult(String result, String[] resultFields) {

        resultFields[0] = "";
        resultFields[1] = "";
        resultFields[2] = "";
        resultFields[3] = "";
        resultFields[4] = "";
        resultFields[5] = "";
        resultFields[6] = "";
        resultFields[7] = "";
        resultFields[8] = "";
        resultFields[9] = "";
        resultFields[10] = "";
        resultFields[11] = "";
        resultFields[12] = "";
        resultFields[13] = "";
        resultFields[14] = "";

        String SEP = "|";

        int ind1 = -1;
        int ind2 = -1;
        ind1 = result.indexOf(SEP);
        if (ind1 == -1) {
            resultFields[0] = result;
            return;
        }
        resultFields[0] = result.substring(0, ind1);
        ind1 += 1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[1] = result.substring(ind1, result.length());
                return;
            }
            resultFields[1] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
                if (ind2 == -1) {
                resultFields[2] = result.substring(ind1, result.length());
                return;
            }
            resultFields[2] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[3] = result.substring(ind1, result.length());
                return;
            }
            resultFields[3] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
                if (ind2 == -1) {
                resultFields[4] = result.substring(ind1, result.length());
                return;
            }
            resultFields[4] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[5] = result.substring(ind1, result.length());
                return;
            }
            resultFields[5] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[6] = result.substring(ind1, result.length());
                return;
            }
            resultFields[6] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[7] = result.substring(ind1, result.length());
                return;
            }
            resultFields[7] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[8] = result.substring(ind1, result.length());
                return;
            }
            resultFields[8] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[9] = result.substring(ind1, result.length());
                return;
            }
            resultFields[9] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[10] = result.substring(ind1, result.length());
                return;
            }
            resultFields[10] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[11] = result.substring(ind1, result.length());
                return;
            }
            resultFields[11] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[12] = result.substring(ind1, result.length());
                return;
            }
            resultFields[12] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[13] = result.substring(ind1, result.length());
                return;
            }
            resultFields[13] = result.substring(ind1, ind2);
        }
        ind1 = ind2+1;
        ind2 = result.indexOf(SEP, ind1);
        if(ind1 != ind2 ) {
            if (ind2 == -1) {
                resultFields[14] = result.substring(ind1, result.length());
                return;
            }
            resultFields[14] = result.substring(ind1, ind2);
        }
    }

    /**
     * Обновить информацию о логине, если пользователь отключился неправильно
     * 
     * @param uid
     * @param channel_id
     */
    public static void gcLogin( UserSession user, int channel_id) {
        int uid = user.getUserUid();
        if (uid==0) return;
        LOG.info("gcLogin: user: " + uid + ", channel_id="+channel_id + " abrupted connection");
        // Смотрим совпадает ли текущий channel_id и channel ID в базе
        // если да, то ставим в базе online=0 и отсылаем всем твой новый статус не в сети

        // если нет, то значит пользователь перелогинился, в то время как прошлое закрытие
        // соединения не определилось
        // значит нам не надо ничего менять, значит в БД уже новые данные, пускай то
        // соединение просто умрёт
        
        String sql = "SELECT channel_id, sess_id FROM " + DBMapper.TABLE_USERS + " WHERE uid= ?";
        Connection con = DBMapper.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, uid);

            int sChannelId = 0;
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                LOG.info("gcLogin: wrong uid");
                con.close();
                return;
            }
            else {
                sChannelId = rs.getInt("channel_id");
                String sess_id = rs.getString("sess_id");
                if (sChannelId==channel_id) {
                    // call to correct logout
                    HashMap p = new HashMap();
                    p.put("session_id", sess_id);
                    //p.put("uid", uid);
                    LOG.info("Logout by exception, uid ="+ uid + "; sess_id = " + sess_id);
                    CmdSystem.cmdLogout(p, user);
                }
                else {
                    
                }
                con.close();
            }
        }
        catch (SQLException ex) {
            LOG.error("gcLogin" + ex.toString());
        }
    }

    /**
     * Удалить информацию о гостевом логине, если пользователь отключился неправильно
     *
     * @param uid
     * @param channel_id
     */
    public static void gcGuestLogin(UserSession user, int channel_id) {
        String btid = user.getUserBtid();
        if (btid.equals("")) return;
        LOG.info("gcGuestLogin: user: " + btid + ", channel_id="+channel_id + " abrupted connection");
        // Смотрим совпадает ли текущий channel_id и channel ID в базе
        // если да, то ставим в базе online=0 и отсылаем всем твой новый статус не в сети

        // если нет, то значит пользователь перелогинился, в то время как прошлое закрытие
        // соединения не определилось
        // значит нам не надо ничего менять, значит в БД уже новые данные, пускай то
        // соединение просто умрёт

        String sql = "SELECT channel_id FROM " + DBMapper.TABLE_GUESTS + " WHERE btid= ?";
        Connection con = DBMapper.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, btid);

            int sChannelId = 0;
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                LOG.info("gcGuestLogin: wrong btid");
                con.close();
                return;
            }
            else {
                sChannelId = rs.getInt("channel_id");
                String sess_id = rs.getString("sess_id");
                if (sChannelId==channel_id) {
                    // call to correct logout
                    HashMap p = new HashMap();
                    p.put("session_id", sess_id);
                    //p.put("uid", uid);
                    LOG.info("Logout by exception, btid ="+ btid + "; channel_id = " + channel_id);
                    CmdSystem.cmdLogout(p, user);
                }
                else {

                }
                con.close();
            }
        }
        catch (SQLException ex) {
            LOG.error("gcLogin" + ex.toString());
        }
    }

    /**
     * 
     * Проверяет наличие города в таблице городов
     * Если есть, то возвращает ID города
     * Если нет, то добавляет запись в таблицу cities, и возвращет ID добавленного города
     *
     * @param cityName
     * @return
     * @throws java.sql.SQLException
     */
    public static int checkCity(String cityName) throws SQLException {
        cityName = cityName.toLowerCase();
        String sql = "SELECT id FROM " + DBMapper.TABLE_CITIES + " WHERE name = ?";

        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, cityName);

        ResultSet rs = ps.executeQuery();
        
        if (!rs.first()) {
            // we need to add city
            

            String sqlAddCity = "INSERT INTO " + DBMapper.TABLE_CITIES + " (name) VALUES (?)";
            ps = con.prepareStatement(sqlAddCity, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cityName);
            ps.executeUpdate();
            ResultSet rsKeys = ps.getGeneratedKeys();
            int autoIncKey = 0;

            if (!rsKeys.first()) {
                LOG.info("CHECK CITY: no autoincremented key added");
                return 0;
            }
            else {
                autoIncKey = rsKeys.getInt(1);
                LOG.info("CHECK CITY: autoincrement key = " + autoIncKey);
                return autoIncKey;
                
            }

        }
        else {
            return rs.getInt("id");
        }
        
    }
}
