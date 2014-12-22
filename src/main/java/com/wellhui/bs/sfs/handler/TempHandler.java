package com.wellhui.bs.sfs.handler;

import com.wellhui.bs.sfs.Server;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.io.File;

/**
 * 临时文件处理类
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
@ChannelHandler.Sharable
public class TempHandler extends AbstractFileHandler {

    final static String TEMP = Server.CONFIG.getProperty("temp");

    @Override
    public boolean check(String uri) {
        if (uri.startsWith(TEMP)) {
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
        StringBuilder sbSB = new StringBuilder(128);
        QueryStringDecoder objDecoder = new QueryStringDecoder(uri);
        if(null != objDecoder.getParameters().get("id")){
            String id = objDecoder.getParameters().get("id").get(0);
            return sbSB.append(ROOT).append(TEMP).append(File.separator).append(id).toString();
        }
        return sbSB.append(ROOT).append(uri).toString();

    }

    @Override
    public String defaultPath(String uri) {
        return null;
    }

    @Override
    public String filename(String uri) {
        if(uri.indexOf("?") > 0){
            return specialCharDecoder(uri.substring(uri.lastIndexOf("/")+1,uri.indexOf("?")));  //To change body of implemented methods use File | Settings | File Templates.
        } else {
            return null;
        }
    }
}
