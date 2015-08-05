package nettymobile;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import org.apache.log4j.*;
import java.util.Random;
/**
 * Содержит команды для пользоватлей
 *
 *
 * @author Yuri Efimov
 */
public class CmdUser {


    /**
     * Тип поиска - друзья
     */
    public static final int USER_SEARCH_FRIENDS = 1;
    /**
     * Тип поиска - люди
     */
    public static final int USER_SEARCH_PEOPLE  = 2;

    public static final Logger LOG = Logger.getLogger(CmdUser.class);



    /**
     * @deprecated 
     * @param p
     * @return
     * @throws java.sql.SQLException
     */
    static public String cmdRegister(HashMap p) throws SQLException {
        Connection con = DBMapper.getConnection();
        if (con==null) return "BAD";
        // cell phone no.  // must
        String strCell = (String)p.get("cell");
        String strCity = (String)p.get("city");
        // если такой уже номер зареган, выдаем "0"
        LOG.info("REGISTER CITY: " + strCity);
        String sql = "SELECT 1 from " + DBMapper.TABLE_USERS + "" +
                " WHERE Cell='" + strCell + "'";
        LOG.info("CMD REGISTER SELECT SQL: " + sql);
        ResultSet rs = con.prepareStatement(sql).executeQuery();

        if (rs.first()) {
            con.close();
            return "0";
        }

    	else
			{

            // must
		String strPass = (String)p.get("pass");
        // must
        String strBT   = (String)p.get("bt");
        // must
        String strNick = (String)p.get("nick");
        String strName = CmdHelper.getValue(p.get("name"),"");
        String strSurname = CmdHelper.getValue(p.get("surname"),"");
        // must
        String strSex   = (String)p.get("sex");
        String strMob = CmdHelper.getValue(p.get("mob"), "00");
        String strYob = CmdHelper.getValue(p.get("yob"),"0000");
        String strDob = CmdHelper.getValue(p.get("dob"),"00");
        Date dateCurrent = new Date(new java.util.Date().getTime());
		sql = "INSERT INTO " + DBMapper.TABLE_USERS + " (btid, Cell, nick, name, surname, gender, birth, Pass, registr) " +
		 " values ('" + strBT + "','" + strCell + "','" + strNick + "','" +strName + "','" +
		strSurname + "','" + strSex + "','" +strYob + "-" + strMob + "-" +strDob + "','" + strPass + "'," +
               " ?)" ;
        LOG.info("CMD REGISTER INSERT SQL: " + sql);
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDate(1, dateCurrent);
        int success = ps.executeUpdate();
        con.close();
        return (success>0) ? "1" : "0";
        }

    }

    /**
     * Регистрация
     * Испольузует метод Артёма для парсинга
     * @param p
     * @return
     * @throws java.sql.SQLException
     */
    static public String cmdRegister(String[] p) throws SQLException {


        // must
		String strPass = p[3];
        // must
        String strBT   = p[9];
        // must
        String strNick = p[0];

        String strCity = p[10];
        LOG.info("REGISTER CITY: " + strCity);

        String strName = p[1];
        String strSurname = p[2];
         // cell phone no.  // must
        String strCell = p[7];
        // must
        String strSex   = p[8];
        int totalDateLen = p[5].length() + p[6].length() + p[4].length();
        String dayOfBirth = null;
        if (totalDateLen==6) {
            LOG.info("REGISTER: day of birth length is 6");
            /*
            String strMob = (p[5].length()==0) ? "00" : p[5];
            String strYob = (p[6].length()==0) ? "00" : p[6];
            String strDob = (p[4].length()==0) ? "00" : p[4]; */
            String strMob = p[5];
            String strYob = p[6];
            String strDob = p[4];
            dayOfBirth = strYob + "-" + strMob + "-" +strDob;
            LOG.info("REGISTER: day of birth " + dayOfBirth);
        }
        
        // если такой уже номер зареган, выдаем "0"
        Connection con = DBMapper.getConnection();
        String sql = "SELECT 1 from " + DBMapper.TABLE_USERS + "" +
                " WHERE Cell='" + strCell + "'";
        LOG.info("CMD REGISTER SELECT SQL: " + sql);
        ResultSet rs = con.prepareStatement(sql).executeQuery();

        if (rs.first()) {
            LOG.info("CMD REGISTER: user with phone num: " + strCell + " already registered");
            con.close();
            return "0";
        }

    	else
			{
            int cityId = 0;
            if (strCity.length()>1) {
             cityId = CmdHelper.checkCity(strCity);
            }
        
        Date dateCurrent = new Date(new java.util.Date().getTime());
		sql = "INSERT INTO " + DBMapper.TABLE_USERS +
                " (btid, Cell, nick, name, surname, gender, birth, Pass, registr, city_id) " +
                " values (?,?,?,?,?,?, ? ,?, ?, ?)" ;
        LOG.info("CMD REGISTER INSERT SQL: " + sql);
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, strBT);
        ps.setString(2, strCell);
        ps.setString(3, strNick);
        ps.setString(4, strName);
        ps.setString(5, strSurname);
        ps.setString(6, strSex);
        
        ps.setString(7, dayOfBirth);
        ps.setString(8, strPass);   
        ps.setDate(9, dateCurrent);
        ps.setInt(10, cityId);

        int success = ps.executeUpdate();
        con.close();
        return (success>0) ? "1" : "0";
        }

    }

    /**
     * Поиск людей
     * @param p
     * @return Список
     * @throws java.sql.SQLException
     */
    static public String cmdSearch(String[] p1, UserSession user) throws SQLException {
        //int search_type = CmdHelper.getValue(p.get("type"), USER_SEARCH_PEOPLE);
        int myUid = user.getUserUid();
        int search_type = CmdHelper.getValue(p1[0], USER_SEARCH_PEOPLE);

        //LOG.info("CMD SEARCH: uid of searcher is: " + myUid);
        String result = "0";
        
        //int limit = CmdHelper.getValue(p.get("limit"), 10);
        //int offset = CmdHelper.getValue(p.get("offset"), 0);
        int limit = 10;
        int offset = 0;
        if (search_type == USER_SEARCH_FRIENDS) {
            String nick     = p1[1];
            String name     = p1[2];
            String surname  = p1[3];
            String phone    = p1[4];
            limit = CmdHelper.getValue(p1[5],10);
            offset = CmdHelper.getValue(p1[6],0);


            result = searchFriends(nick, name, surname, phone, limit, offset);
        }
        else {
            String gender = p1[1];
            int ageLow    = CmdHelper.getValue(p1[2], 0);
            int ageHigh   = CmdHelper.getValue(p1[3], 0);
            String city   = p1[4];
            int online    = CmdHelper.getValue(p1[5], 0);
            limit = CmdHelper.getValue(p1[6],10);
            offset = CmdHelper.getValue(p1[7],0);
            result = searchPeople(gender, ageLow, ageHigh, city, online, limit, offset);
        }
        return result;
    }

    /**
     *
     * Поиск людей, много простой условной логики
     *
     * @param nick
     * @param name
     * @param surname
     * @param phone - Наиболее важный параметр
     * @param limit
     * @param offset
     * @return
     */
    static private String searchFriends(String nick,
            String name, String surname, 
            String cell, int limit, int offset)
            throws SQLException 
    {
        int cmdCode = MobileCommand.ACTION_SEARCH;
        LOG.info("SEARCH FRIENDS");
        boolean woName    = name.length() < 2;
        boolean woSurname = surname.length() < 2;
        boolean woNick    = nick.length() < 3;
        boolean woCell    = cell.length() < 5;

        String sql = "SELECT nick, name, surname, gender," +
                    " cell, status, " +
                    "online, uid, btid, (YEAR(CURDATE())-YEAR(birth)) - (RIGHT(CURDATE(),5)<RIGHT(birth,5)) AS age FROM "  + DBMapper.TABLE_USERS + " WHERE ";
        String sqlLimit = " LIMIT " + limit + " OFFSET " + offset;

        boolean nothingToSearch =
                woName && woSurname && woNick && woCell;

        boolean nameSearch =
                !woName && woSurname && woNick && woCell;
        
        boolean surnameSearch = 
                !woSurname && woName && woNick && woCell;
        
        boolean nickSearch =
                woSurname && woName && !woNick && woCell;

        boolean phoneSearch =
                woSurname && woName && woNick && !woCell;

        

        String result = "0";
        if (nothingToSearch) {
            LOG.info("SEARCH: No data to search");
            result = "0";
        }
        String and = "";
        if (!woName) {
            LOG.info("SEARCH: Name search");
            sql = sql + " name LIKE '" + name + "%' ";
            and = " AND ";
        }
        if (!woSurname) {
            LOG.info("SEARCH: Surname search");
            sql = sql + and + " surname LIKE '" + surname + "%' ";
            and = " AND ";
        }

        if (!woNick) {
            LOG.info("SEARCH: Nick search");
            sql = sql + and + " nick LIKE '" + nick + "%' ";
            and = " AND ";
        }

        if (!woCell) {
            LOG.info("SEARCH: Phone num search");
            sql = sql + and + " cell LIKE '" + cell + "%' ";
            and = " AND ";
        }
        LOG.info("SEARCH SQL: " + sql + sqlLimit);

        Connection con = DBMapper.getConnection();

        ResultSet rs = con.prepareStatement(sql+sqlLimit).executeQuery();

        if (!rs.first()) {
            LOG.info("SEARCH FRIEND - NO RESULT");
            con.close();
            return "0";
        }
        /*
        bt2 + "|" + mid + "|";
        ret = ret + c + "|" + c2 + "|";
        ret = ret + user_uid +  "|" + user_nick + "|" + user_gender+ "|";
        ret = ret + user_age + "|" + user_name + "|" + user_surname + "|";
        ret = ret + user_status; */
        
        String sNick    = rs.getString("nick");
        String sName    = rs.getString("name");
        String sSurname = rs.getString("surname");
        String sBtid    = rs.getString("btid");
        String sStatus  = rs.getString("status");
        String sGender  = rs.getString("gender");
        int    sOnline  = rs.getInt("online");
        int    sUid     = rs.getInt("uid");
        String sCell    = rs.getString("cell");
        String sAge     = rs.getString("age");

        // to not return "null" string
        sAge = (sAge==null) ? "" : sAge;
        result = sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline
                + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        
        while (rs.next()) {
            sNick    = rs.getString("nick");
            sName    = rs.getString("name");
            sSurname = rs.getString("surname");
            sBtid    = rs.getString("btid");
            sStatus  = rs.getString("status");
            sGender  = rs.getString("gender");
            sOnline  = rs.getInt("online");
            sUid     = rs.getInt("uid");
            sCell    = rs.getString("cell");
            sAge     = rs.getString("age");
            sAge = (sAge==null) ? "" : sAge;
            result = result +cmdCode + "|" + sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline
                    + NettyMobileServerHandlerObj.OUTPUT_DELIMITER ;
        }

        con.close();
        return result+ cmdCode + "|||";
    }

    /**
     *  Поиск людей
     *
     * @todo Возраст разбить на даты
     * @param gender
     * @param ageLow
     * @param ageHigh
     * @param city
     * @param online
     * @param limit
     * @param offset
     * @return
     * @throws java.sql.SQLException
    */
    static private String searchPeople(String gender, 
            int ageLow, int ageHigh,
            String city,
            int online, int limit, int offset) throws SQLException {

        LOG.info("SEARCH PEOPLE");
        int cmdCode = MobileCommand.ACTION_SEARCH;
        boolean wGender  = (gender.length() == 1);
        boolean wCity    = city.length() > 2;
        boolean wAge     = (ageLow==0 || ageHigh==0) ? false : true;
        boolean wOnline  = (online==0) ? false : true;

        boolean nothingToSearch =
                !wGender && !wGender && !wAge && !wOnline;
        
        String sql = "SELECT nick, u.name, c.name as city_name, surname, gender," +
                    " cell, status, " +
                    "online, uid, btid,(YEAR(CURDATE())-YEAR(birth)) - (RIGHT(CURDATE(),5)<RIGHT(birth,5)) AS age FROM "  + DBMapper.TABLE_USERS + " as u " +
                    " JOIN " + DBMapper.TABLE_CITIES + " as c ON (c.id = u.city_id) ";
        String sqlLimit = " ORDER BY online DESC LIMIT " + limit + " OFFSET " + offset;
        String sqlWhere = "";
        String result = "0";

        boolean conditions = false;

        if (nothingToSearch) {
            LOG.debug("SEARCH PEOPLE: No data to search");
            result = "0";
        }
        String and = "";
        if (wGender) {
            conditions = true;
            LOG.debug("SEARCH PEOPLE: gender search");
            sqlWhere = sqlWhere + " gender= '" + gender + "' ";
            and = " AND ";
        }

        if (wCity) {
            LOG.info("SEARCH PEOPLE: city search");
            sqlWhere = sqlWhere + and + " c.name LIKE '" + city + "%' ";
            
            and = " AND ";
        }

        if (wAge) {
            LOG.debug("SEARCH PEOPLE: Age search");
            conditions = true;
            sqlWhere = sqlWhere + and +
                    DBMapper.condAgeRange("birth", ageHigh, ageLow);
            and = " AND ";
        }

        if (wOnline) {
            LOG.debug("SEARCH PEOPLE: Online presence search");
            conditions = true;
            sqlWhere = sqlWhere + and + " online = " + online + "";
            sqlLimit = " LIMIT " + limit + " OFFSET " + offset;
            and = " AND ";
        }

        if (conditions) {
            sql = sql + " WHERE ";
        }
        sql = sql + sqlWhere + sqlLimit;
        LOG.info("SEARCH PEOPLE SQL: " + sql);

        Connection con = DBMapper.getConnection();

        
        ResultSet rs = con.prepareStatement(sql).executeQuery();

        if (!rs.first()) {
            LOG.debug("SEARCH PEOPLE - NO RESULT");
            con.close();
            return "0";
        }
       

        String sNick    = rs.getString("nick");
        String sName    = rs.getString("name");
        String sSurname = rs.getString("surname");
        String sBtid    = rs.getString("btid");
        String sStatus  = rs.getString("status");
        String sGender  = rs.getString("gender");
        int    sOnline  = rs.getInt("online");
        int    sUid     = rs.getInt("uid");
        String sCell    = rs.getString("cell");
        String sAge     = rs.getString("age");
        String sCity    = rs.getString("city_name");
        sAge = (sAge==null) ? "" : sAge;
        result = sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline + "|" + sCity
                + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;

        while (rs.next()) {
            sNick    = rs.getString("nick");
            sName    = rs.getString("name");
            sSurname = rs.getString("surname");
            sBtid    = rs.getString("btid");
            sStatus  = rs.getString("status");
            sGender  = rs.getString("gender");
            sOnline  = rs.getInt("online");
            sUid     = rs.getInt("uid");
            sCell    = rs.getString("cell");
            sAge     = rs.getString("age");
            sCity    = rs.getString("city_name");
            sAge = (sAge==null) ? "" : sAge;
            result = result +cmdCode + "|" + sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline + "|" + sCity
                    + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        }

        con.close();
        return result+ cmdCode + "|||";
        
    }
    

    /**
     * Установит статус для пользователя  с UID
     * @todo Проверять что этот UID - айди текущего пользователя
     * @param p
     * @return String "1" - for OK, "0" - for Error
     * @throws java.sql.SQLException
     */
    static public String setStatus(HashMap p) throws SQLException {
        int uid = CmdHelper.getValue(p.get("uid"), 0);
        String status = (String)p.get("status");

        String sql = "UPDATE " + DBMapper.TABLE_USERS + " SET status = ? "
                + " WHERE uid = ?";

        Connection con = DBMapper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, uid);

        int rowCnt = ps.executeUpdate();
        CmdEvents.eventNewStatusChange(uid, status);
        con.close();
        
        statusDelivery(uid, status);
        return (rowCnt==1) ? "1" : "0";

    }

    /**
     * Отправит статусы тем кто в meet, и тем кто в friends
     * @param uid
     * @param status
     * @throws java.sql.SQLException
     */
    static public void statusDelivery(int uid, String status) throws SQLException {
        //EXPLAIN SELECT DISTINCT channel_id FROM meet JOIN users ON (users.btid=meet.bt2) WHERE meet.uid=7 and users.online=1
        String sql = "SELECT channel_id FROM " + DBMapper.TABLE_MEET +
                " m JOIN " + DBMapper.TABLE_USERS + "  u ON (u.btid=m.bt2) " +
                " WHERE m.uid=? and u.online = 1";
        String sql2 =
                " UNION SELECT channel_id FROM "+DBMapper.TABLE_FRIENDS + 
                " f JOIN " + DBMapper.TABLE_USERS + " u " +
                " ON (f.friend_uid = u.uid) WHERE f.uid=? AND u.online=1;";
        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql+sql2);
        ps.setInt(1, uid);
        ps.setInt(2, uid);

        ResultSet rs = ps.executeQuery();


        if (!rs.first()) {
            //LOG.info("CMD STATUS DELIVERY: no online users");
        }
        else {
            //Vector<Integer> channels = new Vector<Integer>();
            int channel_id = rs.getInt("channel_id");
            String msg = MobileCommand.ACTION_STATUS_DELIVERY + "|" + uid + "|" + status;
            CmdHelper.sendOverChannel(channel_id, msg);
            while (rs.next()) {
                channel_id = rs.getInt("channel_id");
                CmdHelper.sendOverChannel(channel_id, msg);
               
            }
         }
        con.close();

    }

    

    /**
     * Отправка статуса нахождения в сети друзьям
     *
     */
    static public void friendsStatusDelivery(int uid, int online) throws SQLException {
        LOG.info("CMD FRIENDS DELIVERY: uid " + uid + "; online " + online);
        String sql = "SELECT u.channel_id FROM " + DBMapper.TABLE_USERS +
                "  as u JOIN " + DBMapper.TABLE_FRIENDS +
                " as f ON (f.friend_uid = u.uid) WHERE f.uid = ? and u.online = 1 ";
        String sql2 = " UNION SELECT u.channel_id FROM " + DBMapper.TABLE_USERS + " as u JOIN " +
                DBMapper.TABLE_FRIENDS + " as f ON (f.uid = u.uid) " +
                " WHERE f.friend_uid = ? and u.online = 1";
        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql+sql2);
        ps.setInt(1, uid);
        ps.setInt(2, uid);

        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            //LOG.info("CMD FRIENDS DELIVERY: no online friends");
        }
        else {
           //LOG.info("CMD FRIENDS DELIVERY: some friends are online");
            int channel_id = rs.getInt("channel_id");

            String msg = MobileCommand.ACTION_STATUS_FRIENDS + "|" + uid + "|" + online;
            
            CmdHelper.sendOverChannel(channel_id, msg);
            while (rs.next()) {
                channel_id = rs.getInt("channel_id");
                CmdHelper.sendOverChannel(channel_id, msg);
               
            }
            
        }
        con.close();
    }


    /**
     * Отправка статуса нахождения в сети встреченным зарегистрированным пользователям
     *
     * 
     */
    static public void meetsStatusDelivery(int uid, int online) throws SQLException {
        LOG.info("CMD MEETS DELIVERY: uid " + uid + "; online " + online);
        String sql = "SELECT DISTINCT u.uid, u.channel_id FROM users u " +
                " JOIN meet m ON (m.bt2= u.btid) " +
                " WHERE m.uid=? AND u.online=1 " +
                " AND (m.meet_time >= " +
                " (SELECT last_time_online FROM users u2 WHERE u2.uid = ?)) ";
       
        sql = " SELECT DISTINCT u.uid, u.channel_id FROM users u JOIN meet m ON (m.uid = u.uid) " + 
            " WHERE m.bt2=(SELECT u3.btid FROM users u3 WHERE u3.uid = ?) AND u.online=1 " +
            " AND (m.meet_time >= (SELECT DATE_SUB(last_time_online, INTERVAL 2 DAY) FROM users u2 WHERE u2.uid = ?))";


        //LOG.info("CMD MEETS DELIVERY: SQL = " + sql);

        Connection con = DBMapper.getConnection();

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, uid);
        ps.setInt(2, uid);

        ResultSet rs = ps.executeQuery();

        if (!rs.first()) {
            //LOG.info("CMD MEETS DELIVERY: no online met users");
        }
        else {
           //LOG.info("CMD MEETS DELIVERY: some met registered users are online");
            int channel_id = rs.getInt("channel_id");
            int uid1 = rs.getInt("uid");
            String msg = MobileCommand.ACTION_STATUS_MEET + "|" + uid + "|" + online;
            //LOG.info("MEETS DELIVERT FIRST: " + msg + "; to uid: " + uid1 + "; to channel: " + channel_id);
            // Костыль получаем
            if (uid1!=uid) {
                 CmdHelper.sendOverChannel(channel_id, msg);
             }
            while (rs.next()) {
                channel_id = rs.getInt("channel_id");
                uid1 = rs.getInt("uid");
                if (uid1!=uid) {
                    //LOG.info("MEETS DELIVERT NEXTS: " + msg + "; to uid: " + uid1 + "; to channel: " + channel_id);
                    CmdHelper.sendOverChannel(channel_id, msg);
                }
            }
        }
        con.close();
    }

    /**
     * Получить лидеров по пересечениям,
     * мою позицию по пересечениям
     * @param p1
     * @param user
     * @return
     */
    static public String cmdGetLeaders(UserSession user)
            throws SQLException {

        int cmdCode = MobileCommand.ACTION_GET_LEADERS;
        // Choose current UID position
        Connection con = DBMapper.getConnection();
        String preSql = "SET @a=0";

        con.createStatement().execute(preSql);

        String sql = " SELECT u1.position, u1.counter " +
            " FROM (SELECT u.uid, u.counter,@a:=@a+1 as position " +
            " FROM " + DBMapper.TABLE_USERS + " u ORDER BY counter DESC) " +
                " as u1 WHERE u1.uid=?";


        // 0 for current week
        // 1 for previous week
        int weekNumber = 0;
        
        sql = " SELECT u1.position, u1.counter " +
            " FROM (SELECT uid, count(*) as counter,@a:=@a+1 as position FROM meet m " +
            " WHERE `week`=WEEK(CURDATE())-? GROUP by uid ORDER BY counter DESC ) " +
                " as u1 WHERE u1.uid=?";

        LOG.info("CMD GET LEADERS: SQL 1: " + sql);

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, weekNumber);
        //ps.setInt(2, user.getUserUid());
        ps.setInt(2,7);

        ResultSet rs = ps.executeQuery();
        if (!rs.first()) {
            con.close();
            return MobileCommand.CMD_EMPTY_CODE;
        }
        int myCounter  = rs.getInt("counter");
        int myPosition = rs.getInt("position");


        sql = "SELECT uid, nick, count(*) as counter, u.name, surname, gender," +
                    " cell, status, online, uid, btid, ageFromDate(birth) as age FROM " + DBMapper.TABLE_MEET + " as m" +
                " JOIN " + DBMapper.TABLE_USERS  + " as u USING (uid) WHERE `week`=WEEK(CURDATE())-?  " +
                " GROUP by uid  ORDER BY counter DESC LIMIT 10 ";

        LOG.info("CMD GET LEADERS: SQL 2: " + sql);
        
        // Choose list of 10 top users
        /*
        sql = " SELECT nick, name, surname, gender," +
                    " cell, status, online, uid, btid, " +
                    "(YEAR(CURDATE())-YEAR(birth)) - (RIGHT(CURDATE(),5)<RIGHT(birth,5)) AS age, counter FROM " +
        DBMapper.TABLE_USERS + " ORDER BY counter DESC LIMIT 10";   */

        ps = con.prepareStatement(sql);

        

        ps.setInt(1, weekNumber);
        
        rs = ps.executeQuery();

        if (!rs.first()) {
            con.close();
            // у нас вообще нет пользователей с такими случайными ID
            return MobileCommand.CMD_EMPTY_CODE;
        }
        String result;
        String sNick    = rs.getString("nick");
        String sName    = rs.getString("name");
        String sSurname = rs.getString("surname");
        String sBtid    = rs.getString("btid");
        String sStatus  = rs.getString("status");
        String sGender  = rs.getString("gender");
        int    sOnline  = rs.getInt("online");
        int    sUid     = rs.getInt("uid");
        String sCell    = rs.getString("cell");
        String sAge     = rs.getString("age");
        String sCounter = rs.getString("counter");
        sAge = (sAge==null) ? "" : sAge;
        result = sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline + "|" + sCounter
                + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;


        while (rs.next()) {
            sNick    = rs.getString("nick");
            sName    = rs.getString("name");
            sSurname = rs.getString("surname");
            sBtid    = rs.getString("btid");
            sStatus  = rs.getString("status");
            sGender  = rs.getString("gender");
            sOnline  = rs.getInt("online");
            sUid     = rs.getInt("uid");
            sCell    = rs.getString("cell");
            sAge     = rs.getString("age");
            sCounter = rs.getString("counter");
            sAge = (sAge==null) ? "" : sAge;
            result = result +cmdCode + "|" + sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline + "|" + sCounter
                    + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        }

        con.close();
        return result+cmdCode +"|" +myCounter+"|" + myPosition  +
                NettyMobileServerHandlerObj.OUTPUT_DELIMITER
                +   cmdCode+ "|||";
    }

    /**
     * Получить случайно 8 пользователей
     * ORDER BY rand() - тяжелый запрос
     * @param p1
     * @param user
     * @return
     */
    static public String cmdGetInteresting(UserSession user)
                        throws SQLException {
        Connection con = DBMapper.getConnection();
        final int MAX_INTERESTING = 10;

        // Получим максимальный UID из таблицы
        String sql = "SELECT MAX(uid) AS maxid FROM  " + DBMapper.TABLE_USERS;

        PreparedStatement ps = con.prepareStatement(sql);

        ResultSet rs = ps.executeQuery();
        if (!rs.first()) {
            con.close();
            return MobileCommand.CMD_EMPTY_CODE;
        }
        int maxId = rs.getInt("maxid");

        // Сгенерируем MAX_INTERESTING случайных чисел в диапазоне от 0 до maxId
        // можно увеличить параметр в цикле
        Random randomGenerator = new Random();
        //ArrayList<Integer> randomUids = new ArrayList();
        String strUids = "";
        for (int idx = 1; idx <= MAX_INTERESTING; ++idx){
            //randomUids.add(
           strUids+= randomGenerator.nextInt(maxId)+ ",";
        }
        strUids = strUids.substring(0,strUids.lastIndexOf(","));
        sql = "SELECT nick, name, surname, gender," +
                    " cell, status, online, uid, btid, " + 
                    "(YEAR(CURDATE())-YEAR(birth)) - (RIGHT(CURDATE(),5)<RIGHT(birth,5)) AS age FROM "
                    + DBMapper.TABLE_USERS + " WHERE uid IN ( " +
                strUids + ")  LIMIT ?";
        ps = con.prepareStatement(sql);
        ps.setInt(1, MAX_INTERESTING);
        rs = ps.executeQuery();

        if (!rs.first()) {
            con.close();
            // у нас вообще нет пользователей с такими случайными ID
            return MobileCommand.CMD_EMPTY_CODE;
        }
        String result;
        String sNick    = rs.getString("nick");
        String sName    = rs.getString("name");
        String sSurname = rs.getString("surname");
        String sBtid    = rs.getString("btid");
        String sStatus  = rs.getString("status");
        String sGender  = rs.getString("gender");
        int    sOnline  = rs.getInt("online");
        int    sUid     = rs.getInt("uid");
        String sCell    = rs.getString("cell");
        String sAge     = rs.getString("age");
        sAge = (sAge==null) ? "" : sAge;
        result = sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline
                + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        int cmdCode = MobileCommand.ACTION_GET_INTERESTING;
        while (rs.next()) {
            sNick    = rs.getString("nick");
            sName    = rs.getString("name");
            sSurname = rs.getString("surname");
            sBtid    = rs.getString("btid");
            sStatus  = rs.getString("status");
            sGender  = rs.getString("gender");
            sOnline  = rs.getInt("online");
            sUid     = rs.getInt("uid");
            sCell    = rs.getString("cell");
            sAge     = rs.getString("age");
            sAge = (sAge==null) ? "" : sAge;
            result = result +cmdCode + "|" + sUid + "|" + sNick + "|" + sGender + "|"
                + sAge + "|" + sName + "|" + sSurname
                + "|" + sStatus + "|" + sOnline
                    + NettyMobileServerHandlerObj.OUTPUT_DELIMITER;
        }

        con.close();
        return result+cmdCode+ "|||";
        
    }
}
