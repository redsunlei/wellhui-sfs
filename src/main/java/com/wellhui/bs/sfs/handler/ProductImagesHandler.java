package com.wellhui.bs.sfs.handler;

import com.wellhui.bs.sfs.Server;
import org.apache.commons.io.FilenameUtils;
import org.jboss.netty.channel.ChannelHandler;

import java.io.File;


/**
 * 新闻图片处理类
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
@ChannelHandler.Sharable
public class ProductImagesHandler extends AbstractFileHandler {

    final static String WEIBO_IMAGES = Server.CONFIG.getProperty("product-images");
    private final static String  WEIBO_IMAGE_ID = "^" + WEIBO_IMAGES + "/(.+)?\\.[a-z]{3,4}$";

    @Override
    public boolean check(String uri) {
        if (uri.startsWith(WEIBO_IMAGES)) {
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
        String strFileName = getFileName(uri,WEIBO_IMAGE_ID);
        StringBuilder sbSB = new StringBuilder(128);
        sbSB.append(ROOT);
        sbSB.append(WEIBO_IMAGES);
        sbSB.append(File.separator);
        sbSB.append(generateBalancingPath(strFileName));
        sbSB.append(File.separator);
        sbSB.append(strFileName);
        sbSB.append(FilenameUtils.EXTENSION_SEPARATOR);
        sbSB.append(FilenameUtils.getExtension(uri)).toString();
        return sbSB.toString();
    }

    @Override
    public String defaultPath(String uri) {
        return null;
    }

    @Override
    public String filename(String uri) {
        return null;
    }

}
