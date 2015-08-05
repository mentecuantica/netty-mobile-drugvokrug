/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

/**
 *
 * @author с
 */
public class MobileCommand {

    public static final String CMD_EMPTY_CODE = "0";
    public static final String CMD_ERROR_CODE = "BAD";

    /**
     * String commands
     */
    public static final int ACTION_REGISTER      = 1;
    public static final int ACTION_LOGIN         = 2;
    public static final int ACTION_LOGOUT        = 3;
    public static final int ACTION_MEET          = 4;
    public static final int ACTION_LOST          = 5;
    public static final int ACTION_SET_STATUS    = 6;
    public static final int ACTION_AVATAR        = 7;
    public static final int ACTION_MESSAGE       = 8;
    public static final int ACTION_MSGRECIEVED   = 9;
    public static final int ACTION_GETMESSAGES   = 10;
    public static final int ACTION_GETMSGCOUNT   = 11;
    public static final int ACTION_GETHISTORY    = 12;
    public static final int ACTION_RELOGIN         = 13;
    public static final int ACTION_SEARCH          = 14;

    public static final int ACTION_ADD_FRIEND       = 15;
    public static final int ACTION_REMOVE_FRIEND    = 16;
    public static final int ACTION_GET_FRIENDS      = 17;
    public static final int ACTION_STATUS_FRIENDS   = 18;
    public static final int ACTION_STATUS_MEET      = 19;
    public static final int ACTION_ECHO             = 20;
    public static final int ACTION_MEET_FROM_NET    = 21;
    public static final int ACTION_STATUS_DELIVERY  = 22;
    public static final int ACTION_GET_EVENTS	    = 23;
    public static final int ACTION_EVENTS_DELIVERY  = 24;
    public static final int ACTION_EVENTS_ON_LOGIN  = 25;
    public static final int ACTION_GET_LEADERS      = 26;
    public static final int ACTION_GET_INTERESTING  = 27;
    public static final int ACTION_AVATAR_DELIVERY  = 28;


    /**
     * Binary commands
     */
    
    public static final int ACTION_IMAGE_DOWNLOAD   = 61;
    public static final int ACTION_IMAGE_UPLOAD     = 62;
}
