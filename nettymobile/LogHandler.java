package nettymobile;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

@ChannelPipelineCoverage("all")
public class LogHandler extends OneToOneEncoder {
    public static final Logger LOG = Logger.getLogger(LogHandler.class);

    private int innerId;
    /**
     * Creates a new instance with the current system character set.
     */
    public LogHandler(int id) {
        this.innerId = id;
    }

   

    @Override
    protected Object encode(
            ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
            LOG.info("Loghandler " + this.innerId + " addon " + msg + " "
                    + "ctx " + ctx.toString()
                    + "channel: " + channel.toString()
                    );
            return msg;
    }
}
