/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.sql.*;
import org.apache.log4j.*;
import com.mchange.v2.c3p0.*;

/**
 *
 * Обеспечивает соединение с БД через пул коннектов,
 * хранит названия таблиц, и настройки соединения
 *
 * 
 * @author Yuri Efimov
 */
public class DBMapper {

    public static final Logger LOG = Logger.getLogger(DBMapper.class);
    
    static private final String strConn  = "jdbc:mysql://localhost/drugvokrug";
    static private final String login = "root";
    static private final String password  = "waka";
    //static private Connection con = null;
    static public final String TABLE_USERS     = "users";
    static public final String TABLE_MSGS      = "msgs";
    static public final String TABLE_MEET      = "meet";
    static public final String TABLE_FRIENDS   = "friends";
    static public final String TABLE_CITIES    = "cities";
    static public final String TABLE_BLACKLIST = "blacklist";
    static public final String TABLE_EVENTS    = "events";
    static public final String TABLE_GIFTS     = "gifts";
    static public final String TABLE_GUESTS    = "guests";
    
    

    static private ComboPooledDataSource cpds = null;

    /**
     * Получить соединение из пула
     * 
     * @return
     */
    static public Connection getConnection() {
        try {
            //LOG.info("Trying to get DB connection");
            //LOG.info("num_connections: "      + cpds.getNumConnectionsDefaultUser());
            //LOG.info("num_busy_connections: " + cpds.getNumBusyConnectionsDefaultUser());
            //LOG.info("num_idle_connections: " + cpds.getNumIdleConnectionsDefaultUser());

            Connection con = cpds.getConnection();
            //LOG.info("Connection info: " + con);
            return con;
        } catch (SQLException ex) {
            LOG.error("Error on getting connection from pool",ex);
            return null;
        }
        catch (Exception ex) {
            LOG.error("On getting db connection " , ex);
            return null;
        }
    }

   

    /**
     * Создать пул коннектов при инициализации сервера
     * 
     */
    static public void connectDb() {
    
            cpds = new ComboPooledDataSource();
            //cpds.
            String driverClass = "com.mysql.jdbc.Driver";
            try {
                cpds.setDriverClass(driverClass);
            }
            catch (Exception e) {
                LOG.error("Set driver class ",e);
            }
            cpds.setJdbcUrl("jdbc:mysql://localhost:3306/drugvokrug?useUnicode=true&characterEncoding=utf-8");
            cpds.setUser(login);
            cpds.setPassword(password);


            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(100);

        /*
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(strConn, login, password);
        }
        catch (SQLException e) {
            LOG.error("SQL Error: " + e.getMessage());
        }
        catch (ClassNotFoundException e) {
            LOG.error(e.getMessage());
        } */
    }

    /**
     * Вернет строку для MySql запроса на диапазон дат
     * @param field
     * @param upper
     * @param lower
     * @return
     */
    public static String condAgeRange(String field, int upper, int lower) {
        String sql = " (" +field + " >= DATE_SUB(NOW(), INTERVAL " + upper + "  YEAR) " +
         " AND birth <= DATE_SUB(NOW(), INTERVAL " + lower  + " YEAR)) ";

        return sql;
    }
}
