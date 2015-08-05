/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import org.jboss.netty.handler.codec.frame.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.buffer.*;
import org.apache.log4j.*;

/**
 *
 * Декодер пакетов в формат объектов MobileCmd
 * 
 * Первый байт - код команды
 * Следующие 4 байта - целое число, длина последующей строки параметров
 *
 * @version 0.1
 * @author Yefimov Yuri
 */
public class NettyMobileDecoder extends FrameDecoder {

    public static final Logger LOG = Logger.getLogger(NettyMobileDecoder.class);
    
    @Override
    protected Object decode(ChannelHandlerContext ctx,
            Channel channel, ChannelBuffer buffer)  {

        // check if command byte received
        if (buffer.readableBytes() < 5) {
              return null;
        }

        buffer.markReaderIndex();
        byte cmdCode = buffer.readByte();
        int dataLength = buffer.readInt();
        LOG.info("CMD CODE:"  + cmdCode);
        LOG.info("Remote Address: " + channel.getRemoteAddress());
        /* if (buffer.readableBytes()< 5) {
            return null;
        }
        int dataLength = buffer.readInt(); */
        LOG.info("DATA LEN:"  + dataLength);
        LOG.info("Remote Address: " + channel.getRemoteAddress());

        /**
         * Дождёмся пока придут парамтры
         * Для загрузки картинок параметр -
         * это бинарное содержимое файла. Поэтому его надо
         * читать аккуратно, чтобы не было OOME.
         *
         */
        int readableBytes = buffer.readableBytes();
        LOG.info("Readable bytes: " + readableBytes);
        if (readableBytes< dataLength)
        {
            buffer.resetReaderIndex();
            return null;
        }

        // Прочитаем строку параметров
        byte[] rawData = new byte[dataLength];
        buffer.readBytes(rawData);

        // Вернём объект команду
        Object ret = null;
        try {
            if (cmdCode>60) {
                ret = new BinaryCmd(cmdCode, rawData, channel.getId());
            }
            else {
                ret = new StringCmd(cmdCode,rawData, channel.getId());
            }
             
        }
        
        catch (Exception e) {
            LOG.error("CMD EXCEPTION");
        }
        return ret;
    }
}
