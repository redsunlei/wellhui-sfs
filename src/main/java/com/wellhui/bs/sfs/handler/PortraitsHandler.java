package com.wellhui.bs.sfs.handler;

import com.wellhui.bs.sfs.Server;
import org.jboss.netty.channel.ChannelHandler;

import java.io.File;


/**
 * 头像处理类
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
@ChannelHandler.Sharable
public class PortraitsHandler extends AbstractFileHandler {


    final static String PORTRAITS = Server.CONFIG.getProperty("portraits");
    final static String BIG_PORTRAITS_DEFAULT = Server.CONFIG.getProperty("portraits-default-big");
    final static String SMALL_PORTRAITS_DEFAULT = Server.CONFIG.getProperty("portraits-default-small");


    private final static String  PORTRAIT_ID = "^" + PORTRAITS + "/(.+)?\\.png$" ;


    @Override
    public boolean check(String uri) {
        if (uri.startsWith(PORTRAITS)){
            return true;
        }
        return false;
    }

    @Override
    public boolean security() {
        return false;
    }

    @Override
    public boolean cacheable() {
        return true;
    }


    @Override
    public String path(String uri) {
        String strFileName = getFileName(uri, PORTRAIT_ID);
        if (strFileName == null) {
            return null;
        }
        StringBuilder sbSB = new StringBuilder(128);
        return sbSB.append(ROOT).append(PORTRAITS).append(File.separator).append(generateBalancingPath(strFileName)).append(File.separator).append(strFileName).append(".png").toString();
    }

    @Override
    public String defaultPath(String uri) {
        String strFileName = getFileName(uri,PORTRAIT_ID);
        StringBuilder sbFile = new StringBuilder(128);
        if (strFileName.endsWith("L")){
            sbFile.append(ROOT).append(BIG_PORTRAITS_DEFAULT).toString();
        }else {
            sbFile.append(ROOT).append(SMALL_PORTRAITS_DEFAULT).toString();
        }
        return sbFile.toString();
    }

    @Override
    public String filename(String uri) {
        return null;
    }

}
