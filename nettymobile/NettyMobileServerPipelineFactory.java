/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;

import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.string.*;
import org.jboss.netty.buffer.*;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;


/**
 * This pipeline handle UTF-8 string protocol with
 * double null bytes ending "\00\00";
 * @author с
 */
public class NettyMobileServerPipelineFactory implements
        ChannelPipelineFactory {

    // Current handler. Coverage was ALL;
    private ChannelHandler handler;

    // Executor will process some threads
    OrderedMemoryAwareThreadPoolExecutor globalExecutor;
    // Max recieved string length in bytes
    private final int MaxAllowedQueryLength = 255;
    
    static private final StringEncoder stringEncoder = new StringEncoder("UTF-8");

    static private final LogHandler logHandler1 = new LogHandler(1);
    static private final LogHandler logHandler2 = new LogHandler(2);
    static private final LogHandler logHandler3 = new LogHandler(3);
    //разделитель, сейчас - символ новой строки 0 0
    // 2 нулевых байта
    private final ChannelBuffer[] frameDelimiter = NettyDelimiters.doubleNullDelimiter();

    public NettyMobileServerPipelineFactory(OrderedMemoryAwareThreadPoolExecutor executor) {
        this.globalExecutor = executor;
    }

    public NettyMobileServerPipelineFactory(ChannelHandler handler) {
        this.handler = handler;
    }
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();

        //line codec
        /*pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(
                MaxAllowedQueryLength, frameDelimiter));
        */
        //pipeline.addLast("stringDecoder", new StringDecoder("UTF-8"));
        pipeline.addLast("cmdDecoder", new NettyMobileDecoder());
        pipeline.addLast("stringEncoder", stringEncoder);

        //pipeline.addLast("log1", logHandler1);
        // may be faster
       pipeline.addLast("executor",
               new ExecutionHandler(globalExecutor));

       //pipeline.addLast("log2", logHandler2);
       NettyMobileServerHandlerObj localHandler =
                new NettyMobileServerHandlerObj();
        
        //business logic
        
        pipeline.addLast("handler", localHandler);
        //pipeline.addLast("log3", logHandler3);
        return pipeline;
    }
}
