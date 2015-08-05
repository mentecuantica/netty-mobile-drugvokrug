/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

/**
 * Хранит все данные о пользователе,
 * авторизовался ли он, тип пользователя
 * его Bluetooth ID, и channel id
 *
 * @author Yuri Efimov
 * @version 0.1
 */
public class UserSession {
    
    //public static final int USER_UNREGISTERED = 0;
    public static final int USER_GUEST        = 0;
    public static final int USER_REGISTERED   = 1;


    /**
     * Залогинен ли пользователь по умолчанию,
     * должно быть false в production версии
     */
    private boolean isLoggedIn = true;
    /**
     * Его uid, должен всегда сопровождать все команды
     */
    private int     userUid      = 0;
    /**
     * Его btid
     */
    private String  userBtid     = "";

    private int userType = USER_GUEST;

    private String  trustedCmdResult = "0";

    private int channelId;

    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    public int getUserUid() {
        return this.userUid;
    }

    public void setUserUid(int uin) {
        this.userUid = uin;
    }

    /**
     * @return the userBtid
     */
    public String getUserBtid() {
        return userBtid;
    }

    /**
     * @param userBtid the userBtid to set
     */
    public void setUserBtid(String userBtid) {
        this.userBtid = userBtid;
    }

    /**
     * @return the channelId
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * @param channelId the channelId to set
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /**
     * @return the trustedCmdResult
     */
    public String getTrustedCmdResult() {
        return trustedCmdResult;
    }

    /**
     * @param trustedCmdResult the trustedCmdResult to set
     */
    public void setTrustedCmdResult(String trustedCmdResult) {
        this.trustedCmdResult = trustedCmdResult;
    }

    /**
     * Конструктор для методов LOGIN
     * @param userType
     * @param isLoggedIn
     * @param userUid
     * @param userBtid
     */
    public UserSession(int userType, boolean isLoggedIn, int userUid, String  userBtid ) {
        this.setUserUid(userUid);
        this.setLoggedIn(isLoggedIn);
        this.setUserBtid(userBtid);
        this.setUserType(userType);
    }

    /**
     * Конструктор для методов RELOGIN
     * @param isLoggedIn
     * @param userUid
     */
    public UserSession(boolean isLoggedIn, int userUid) {
        this.setUserUid(userUid);
        this.setLoggedIn(isLoggedIn);
    }

    private void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
    }

    public UserSession() {
        
    }

    /**
     * @return the userType
     */
    public int getUserType() {
        return userType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(int userType) {
        this.userType = userType;
    }

    /**
     * Отключить пользователя при возникновении IOException
     * @param channel_id
     */
    public void disconnect(int channel_id) {
        if (getUserType()==UserSession.USER_GUEST) {
            CmdHelper.gcGuestLogin(this, channel_id);
        }
        else {
            CmdHelper.gcLogin(this, channel_id);
        }
    }
    
}
