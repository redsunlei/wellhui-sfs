package com.wellhui.bs.sfs.handler;

import com.wellhui.bs.sfs.Server;
import org.jboss.netty.channel.ChannelHandler;

import java.io.File;

/**
 * 新闻图片缩略图处理类
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
@ChannelHandler.Sharable
public class PromotionThumbnailsHandler extends AbstractFileHandler {


    final static String  WEIBO_IMAGES_THUMBNAILS = Server.CONFIG.getProperty("promotion-thumbnails");

    final static String  WEIBO_IMAGE_THUMBNAIL_ID = "^" + WEIBO_IMAGES_THUMBNAILS + "/(.+)?\\.png$";


    @Override
    public boolean check(String uri) {
        if (uri.startsWith(WEIBO_IMAGES_THUMBNAILS)) {
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
        String strFileName = getFileName(uri,WEIBO_IMAGE_THUMBNAIL_ID);
        StringBuilder sbSB = new StringBuilder(128);
        sbSB.append(ROOT);
        sbSB.append(WEIBO_IMAGES_THUMBNAILS);
        sbSB.append(File.separator);
        sbSB.append(generateBalancingPath(strFileName));
        sbSB.append(File.separator);
        sbSB.append(strFileName);
        sbSB.append(".png");
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
