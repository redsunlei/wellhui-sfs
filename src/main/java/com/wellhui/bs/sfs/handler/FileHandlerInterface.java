package com.wellhui.bs.sfs.handler;


/**
 * 文件处理类接口
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
public interface FileHandlerInterface {

    //是否处理
    boolean check(String uri);

    // 是否需要检验票据
    boolean security();

    //是否缓存
    boolean cacheable();

    //文件路径
    String path(String uri);

    //文件默认路径
    String defaultPath(String uri);

    //文件的下载名
    String filename(String uri);

}
