/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import java.io.*;
import java.sql.*;


import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 *
 * Бинарная команда, представляет из себя набор байт, 
 * которые затем парсятся в соответсвующие параметры
 *
 * @author Yuri Efimov
 * @see StringCmd
 * @version 0.1
 */
public class BinaryCmd {

    private UserSession user;
    private ChannelHandlerContext ctx;
    private MessageEvent e;
    private int cmdCode;

    public int getCmdCode() {
        return this.cmdCode;
    }
    public UserSession getUser() {
        return user;
    }

    public void setUser(UserSession user) {
        this.user = user;
    }

    private byte[] binaryData;
    public static final Logger LOG = Logger.getLogger(BinaryCmd.class);
    public BinaryCmd(int cmdCode, byte[] rawData, int channelId) {
        this.cmdCode = cmdCode;
        
        LOG.debug("Image cmd create, binary data length =  " + rawData.length);
        this.binaryData = rawData;
    }

    /**
     * Установить переменные соединения
     * @param ctx
     * @param e
     */
    public void setEnv(ChannelHandlerContext ctx, MessageEvent e) {
        this.ctx = ctx;
        this.e   = e;
    }
    
    /**
     * Выполнить бинарную команду
     * @return String - ответ типа string
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public String execute() throws FileNotFoundException, IOException {

        try {
            switch (this.cmdCode) {
                case MobileCommand.ACTION_IMAGE_UPLOAD:  return this.imageUpload();
                case MobileCommand.ACTION_IMAGE_DOWNLOAD: return this.imageDownload();

                default:  return "0";
            }
            
        }
        catch (SQLException se) {
            LOG.error(se.getMessage() + se.getSQLState());
            return "0";
         }
    
    }
    public final String IMAGE_RESIZE_40_40   = "_40x40.jpeg";
    public final String IMAGE_RESIZE_60_60   = "_60x60.jpeg";
    public final String IMAGE_RESIZE_210_180 = "_210x180.jpeg";

    public final int IMAGE_RESIZE_CODE_40_40   = 1;
    public final int IMAGE_RESIZE_CODE_60_60   = 2;
    public final int IMAGE_RESIZE_CODE_210_180 = 3;


    /**
     * Закачка картинки
     * Пока что вся картинка хранится в буфере,
     * что может впоследствии вызвать OOME
     *
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    private String imageUpload() throws FileNotFoundException, IOException, SQLException {
        int uid = getUser().getUserUid();
        LOG.debug("IMAGE UPLOAD FOR USER: uid " + uid );
        if (uid==0) { 
            return MobileCommand.ACTION_IMAGE_DOWNLOAD + "|0";
        };

        
        String profilePath = FileSystemHelper.createUinPath(uid,"profiles//");

        String sourceImage = profilePath + uid + ".jpeg";
        FileOutputStream fos = new FileOutputStream(sourceImage);
        fos.write(binaryData);
        //fos.flush();
        fos.close();
        
        String thumb = profilePath + uid + IMAGE_RESIZE_40_40;
        new ImageMagickWrapper(sourceImage, thumb).generateThumb(40, 40);
        
        thumb = profilePath + uid + IMAGE_RESIZE_60_60;
        new ImageMagickWrapper(sourceImage, thumb).generateThumb(60, 60);

        thumb = profilePath + uid + IMAGE_RESIZE_210_180;
        new ImageMagickWrapper(sourceImage, thumb).generateThumb(210, 180);


        Connection con = DBMapper.getConnection();

        String sql = "UPDATE " + DBMapper.TABLE_USERS + " SET avatar = 1 WHERE uid = ?";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, uid);
        ps.executeUpdate();
        con.close();

        CmdEvents.eventNewAvatar(uid);
       

        return MobileCommand.ACTION_IMAGE_UPLOAD + "|1";
    }

    /**
     * Загрузка картинки
     * Параметры бинарной команды
     * 1 - тип картинки byte
     * 2 - UID integer
     *
     * @return 61|1 - success, 61|0 - error
     * @throws java.io.FileNotFoundException
     */
    private String imageDownload() throws FileNotFoundException {

        int uid = getUser().getUserUid();

        // can be positivly replaced with ChannelBuffers.wrappedBuffer
        ChannelBuffer cbArguments = ChannelBuffers.copiedBuffer(binaryData);
        byte typeOfImage = cbArguments.readByte();

        String postfix = "";
        switch (typeOfImage) {
            case IMAGE_RESIZE_CODE_40_40: postfix = IMAGE_RESIZE_40_40; break;
            case IMAGE_RESIZE_CODE_60_60: postfix = IMAGE_RESIZE_60_60; break;
            case IMAGE_RESIZE_CODE_210_180: postfix = IMAGE_RESIZE_210_180;break;
            default: postfix = IMAGE_RESIZE_40_40;
        }
        
        int uidFromBuffer = cbArguments.readInt();
        LOG.info("IMAGE DOWNLOAD uid:" + uidFromBuffer);
        String fname = FileSystemHelper.getUinPath(uidFromBuffer, "profiles//")
                + uidFromBuffer+ postfix;
        LOG.info("Path to download: " + fname);
        File f = new File(fname);
       // ChannelFuture writeFuture = e.getChannel().write();
        ChannelBuffer cb = ChannelBuffers.buffer(9);
        cb.writeByte((byte)MobileCommand.ACTION_IMAGE_DOWNLOAD);

        final long fileLength = f.length();
        System.out.println("file length = " + fileLength);
        if (fileLength>0) {
        cb.writeInt((int)fileLength);
        cb.writeInt(uidFromBuffer);
        ChannelFuture writeFuture  = e.getChannel().write(cb);
        final FileInputStream fis = new FileInputStream(f);
        
        writeFuture.addListener(new ChannelFutureListener() {
            private final ChannelBuffer buffer = ChannelBuffers.buffer(4096);
            private long offset = 0;

        public void operationComplete(ChannelFuture future)
                throws Exception {
            if (!future.isSuccess()) {

                LOG.error("ON DOWNLOADING IMAGE");
                future.getCause().printStackTrace();
                future.getChannel().close();
                fis.close();
                return ;
            }

            System.out.println("SENDING: " + offset + " / " + fileLength);
            buffer.clear();
            buffer.writeBytes(fis, (int) Math.min(fileLength - offset,
                    buffer.writableBytes()));
            offset += buffer.writerIndex();
            ChannelFuture chunkWriteFuture =
                    future.getChannel().write(buffer);
            if (offset < fileLength) {
                // Send the next chunk
                chunkWriteFuture.addListener(this);
            } else {
                // Wrote the last chunk - close the connection if the write is done.
                System.out.println("DONE: " + fileLength);
                //chunkWriteFuture.addListener(ChannelFutureListener.CLOSE);
                fis.close();
            }
        }
        
    });
    return MobileCommand.ACTION_IMAGE_DOWNLOAD + "|1";
        }
    return MobileCommand.ACTION_IMAGE_DOWNLOAD + "|0";
    }
}
