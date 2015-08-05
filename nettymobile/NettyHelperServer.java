/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

/**
 *
 * @author с
 */
public class NettyHelperServer {

     /**
     * Порт для администратора
     *
     */
    public static final int ADMIN_PORT = 9091;

    public static NettyHelperServer getInstance() {
        return new NettyHelperServer();
    }
}
