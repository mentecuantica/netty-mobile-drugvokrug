/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.io.IOException;
import org.jboss.netty.channel.*;
import org.apache.log4j.*;

/**
 * Обработчик уровня бизнес-логики
 *
 * Обрабатывает команды, проверяет возможность выполнения команды,
 * обеспечивает закрытие соединения
 *
 * @author Yuri Efimov
 */

@ChannelPipelineCoverage("one")
public class NettyMobileServerHandlerObj extends SimpleChannelHandler {

    public static final String OUTPUT_DELIMITER = "\00\00";
    public static final Logger LOG = Logger.getLogger(NettyMobileServerHandlerObj.class);

    private UserSession user = new UserSession();

    /**
     * @return the user
     */
    public UserSession getUser() {
        return user;
    }

    /**
     * @param aUser the user to set
     */
    public void setUser(UserSession aUser) {
        user = aUser;
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        LOG.debug("Message from IP: " + ctx.getChannel().getRemoteAddress());
        LOG.debug("Channel ID: " + ctx.getChannel().getId());

        if (e.getMessage() instanceof StringCmd) {
            this.stringCmdRecieved(ctx, e);
            return;
        }
        if (e.getMessage() instanceof BinaryCmd) {
            if (this.user.isLoggedIn()) {
                LOG.info("LOGGED IN USER TRY TO SEND A BINARY COMMAND");
                this.binaryCmdRecieved(ctx, e);
                
            }
            else {
                LOG.info("NOT LOGGED IN USER TRY TO SEND A BINARY COMMAND");
            }
            return;
        }
        ctx.sendUpstream(e);
        return;
        
    }

    /**
     * Пришло текстовое сообщение
     *
     * @param ctx
     * @param e
     * @throws java.lang.Exception
     * @see StringCmd
     */
    private void stringCmdRecieved(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        StringCmd cmd = (StringCmd)e.getMessage();
        if (this.getUser().isLoggedIn()) {
            LOG.info("USER ALREADY LOGGED & HIS UID = " + this.getUser().getUserUid());
            cmd.setUser(user);
        }
        String response = "0";

        // we have 3 trusted actions LOGIN, RELOGIN, REGISTER

        boolean trustedCommand = cmd.isTrustedCommand();


        if (this.getUser().isLoggedIn() || trustedCommand) {
            response = cmd.execute();

            // one of 3 trusted actions
            if (trustedCommand) {
                this.setUser(cmd.getUser());
                LOG.info("Logged = " + this.getUser().isLoggedIn() +
                        "; with UID = " + this.getUser().getUserUid() + ";" + response);
            }
        }
        else {
            LOG.info("User should login first !");
        }

        try {
            LOG.info("Channel for event: " + e.getChannel());
            ChannelFuture future = e.getChannel().write(response+OUTPUT_DELIMITER);
            LOG.info("STRING RESPONSE: " + response);
            if (cmd.isClose()) {
                  future.addListener(ChannelFutureListener.CLOSE);
            }
        }
        catch (Exception ex) {
            LOG.error("On sending info!!! " + ex);
        }
    }

    /**
     * Пришло бинарное сообщение
     *
     * @param ctx
     * @param e
     * @throws java.lang.Exception
     * @see BinaryCmd
     */
    private void binaryCmdRecieved(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        BinaryCmd cmd = (BinaryCmd)e.getMessage();
        
       
        cmd.setEnv(ctx, e);
        cmd.setUser(this.getUser());
        LOG.info("BINARY COMMAND RECIEVED cmdcode:" + cmd.getCmdCode() + ";uid = "
                + cmd.getUser().getUserUid());
        String response = cmd.execute();


        try {
            LOG.info("Channel for event: " + e.getChannel());
            ChannelFuture future = e.getChannel().write(response+OUTPUT_DELIMITER);
            LOG.info("BINARY RESPONSE: " + response);
           
        }
        catch (Exception ex) {
            LOG.error("On sending info!!! " + ex);
        }
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        LOG.info("We have a connect from: " + e.getChannel().getRemoteAddress());
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when a {@link Channel} was disconnected from its remote peer.
     */
    @Override
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        LOG.info("Channel " + e.getChannel().getRemoteAddress() 
                + " disconnected: " + e.getChannel().getId() +
                " uid = " + this.getUser().getUserUid());
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when a {@link Channel} was unbound from the current local address.
     */
    @Override
    public void channelUnbound(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        //LOG.info("Channel " + e.getChannel().getRemoteAddress() + "  unbound: " + e.getChannel().getId());
        ctx.sendUpstream(e);

    }

    /**
     * Invoked when a {@link Channel} was closed and all its related resources
     * were released.
     */
    @Override
    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        //LOG.info("Channel " + e.getChannel().getRemoteAddress() + "  closed: " + e.getChannel().getId());
        ctx.sendUpstream(e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Добавим channel в channelGroup
        NettyMobileServer.allChannels.add(e.getChannel());
    }
    
    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            

            if (e.getCause() instanceof IOException) {
                LOG.error("We can close this connection, uid: " + getUser().getUserUid() +
                        " IOException. " + e.getChannel().getRemoteAddress());
    
                getUser().disconnect(ctx.getChannel().getId());

                LOG.error(e.toString());
            } else {
                e.getCause().printStackTrace();
                LOG.error("Exception msg: " + e.getCause().getMessage());
                LOG.error("Exception " +
                    e.getChannel().getRemoteAddress() + ", " +
                    e.toString());
            }
            
            
    }

    
    
}
