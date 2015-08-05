/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.*;
import org.jboss.netty.handler.codec.string.*;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.buffer.*;

/**
 *
 * @deprecated 
 */
public class NettyMobileServerPipelineFactoryTLV implements
        ChannelPipelineFactory {

    // Current handler
    private final ChannelHandler handler;

    // Max recieved string length in bytes
    private final int MaxAllowedQueryLength = 255;
    
    //разделитель, сейчас - символ новой строки 0 0
    // 2 нулевых байта
    private final ChannelBuffer[] frameDelimiter = NettyDelimiters.doubleNullDelimiter();

    public NettyMobileServerPipelineFactoryTLV (ChannelHandler handler) {
        this.handler = handler;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();

        //line codec
        pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(
                MaxAllowedQueryLength, frameDelimiter));
        pipeline.addLast("stringDecoder", new StringDecoder("UTF-8"));
        pipeline.addLast("stringEncoder", new StringEncoder("UTF-8"));

        // may be faster

        
        //business logic

        pipeline.addLast("handler", this.handler);
        return pipeline;
    }
}
