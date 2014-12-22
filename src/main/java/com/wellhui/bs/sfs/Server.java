package com.wellhui.bs.sfs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * sfs
 *
 * @author ËïÀÚ
 * @version 2014-11-23 ËïÀÚ
 * @since 1.0
 */
public class Server {

    private final int port;

    public static Properties CONFIG = new Properties();

    public static final Log log = LogFactory.getLog(Server.class);

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8888;
        }
        log.info("loading config.properties...");
        InputStream objIS = Server.class.getResourceAsStream("/config.properties");
        try {
            CONFIG.load(objIS);
            objIS.close();
        } catch (IOException e) {
            log.error("CAN NOT locate config.properties in classpath ROOT");
            return;
        }
        new Server(port).run();
    }

    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new PipelineFactory());
        bootstrap.bind(new InetSocketAddress(port));
        log.info("Server is listening on localhost:" + port);
    }
}