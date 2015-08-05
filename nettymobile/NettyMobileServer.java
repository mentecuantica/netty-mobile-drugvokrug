/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nettymobile;
import java.util.concurrent.*;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 *
 * Инициализация сервера , настройки
 *
 * @author Yuri Efimov
 *
 */
public class NettyMobileServer {

    public static final Logger LOG = Logger.getLogger(NettyMobileServer.class);

    /**
     * Порт для клиентов сервера
     */
    public static final int PORT = 9090;

   
    
    /**
     * Create the global ChannelGroup
     */
    static final ChannelGroup allChannels =
            new DefaultChannelGroup(NettyMobileServer.class.getName());
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        // Create a DB connection while initializing socket server 
        DBMapper.connectDb();
        
        ChannelFactory factory =
                new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()
                );
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        
        
        ChannelGroup channelGroup = new DefaultChannelGroup(
                );

        // Use our action flow
        OrderedMemoryAwareThreadPoolExecutor executor =
                new OrderedMemoryAwareThreadPoolExecutor(50,0,0);
        
        bootstrap.setPipelineFactory(
                new NettyMobileServerPipelineFactory(executor));

        // Options for child connections
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        // Go
        Channel ch = bootstrap.bind(new InetSocketAddress(PORT));
        LOG.info("Server started at port: " + PORT);
        allChannels.add(ch);

    }

}
