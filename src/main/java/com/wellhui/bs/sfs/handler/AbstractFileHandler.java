package com.wellhui.bs.sfs.handler;

import com.wellhui.bs.sfs.Server;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 基本操作处理抽象类
 *
 * @author 孙磊
 * @version 2014-11-23 孙磊
 * @since 1.0
 */
public abstract class AbstractFileHandler implements FileHandlerInterface,ChannelUpstreamHandler {

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final int HTTP_CACHE_SECONDS = 31526000;

    private static final String FILE_NOT_FOUND = "FILE NOT FOUND";
    private static final String FILE_NOT_PERMISSION = "FILE NOT PERMISSION";

    final static String  ROOT = Server.CONFIG.getProperty("root");
    final static String  FAVICON = "/favicon.ico";


    /** 下载文件的名字 */
    private String downLoadFileName = null;

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent) {
            MessageEvent event = (MessageEvent)e;
            HttpRequest request = (HttpRequest)event.getMessage();

            String uri = request.getUri();

            if (uri.equals(FAVICON)) {
                sendError(ctx, FORBIDDEN, FILE_NOT_FOUND);
                return;
            }

            if(check(uri)){

                String strPath = path(uri);
                if(strPath==null){
                    sendError(ctx, BAD_REQUEST, FILE_NOT_FOUND);
                    return;
                }
                strPath = sanitize(strPath);


                File file = read(strPath);
                if(file==null){
                    if (defaultPath(uri)!=null){
                        file = read(defaultPath(uri));
                        if (file==null){
                            sendError(ctx, NOT_FOUND, FILE_NOT_FOUND);
                            return;
                        }
                    }else {
                        sendError(ctx, NOT_FOUND, FILE_NOT_FOUND);
                        return;
                    }
                }
                //是否检验票据
                if(security()){
                    QueryStringDecoder objDecoder = new QueryStringDecoder(uri);
                    String token = objDecoder.getParameters().get("token").get(0);
                    String id = objDecoder.getParameters().get("id").get(0);
                }

                downLoadFileName = filename(uri);

                //是否缓存
                if (cacheable()){
                    ModifySince(request,file,ctx);
                }
                writeFuture(file,ctx,event);
            } else {
                ctx.sendUpstream(e);
            }
        }else {
            ctx.sendUpstream(e);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String message ){
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/HTML; charset=UTF-8");
        String strStyle = "text-align:center;word-break:break-all;color:#999999;font-weight:bold;font-family:inherit;font-family: 'Microsoft YaHei';font-size: 17.5px;";
        if (FILE_NOT_FOUND.equals(message)){
            response.setContent(ChannelBuffers.copiedBuffer("<!DOCTYPE html><br/><br/><br/><h4 style=\""+strStyle+"\">对不起，您访问的文件不存在！</h4>", CharsetUtil.UTF_8));
        }else if(FILE_NOT_PERMISSION.equals(message)){
            response.setContent(ChannelBuffers.copiedBuffer("<!DOCTYPE html><br/><br/><br/><h4 style=\""+strStyle+"\">对不起，您没有权限访问！</h4>", CharsetUtil.UTF_8));
        }else{
            response.setContent(ChannelBuffers.copiedBuffer("<!DOCTYPE html><br/><br/><br/><h4 style=\""+strStyle+"\">对不起，您访问的文件不存在或者您没有权限访问！</h4>", CharsetUtil.UTF_8));
        }

        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * URL转码
     * @param uri
     * @return
     */
    public static String sanitize(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        uri = uri.replace('/', File.separatorChar);
        if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }
        return uri;
    }

    /***
     * id转为地址
     * @param id
     * @return
     */
    public String generateBalancingPath(String id) {
        return id.replaceAll("..(?!$)", "$0/");
    }

    /**
     * 是否更新
     * @param request
     * @param file
     * @param ctx
     * @throws ParseException
     */
    public void ModifySince(HttpRequest request,File file,ChannelHandlerContext ctx) throws ParseException {
        String ifModifiedSince = request.getHeader(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && ifModifiedSince.length() != 0) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_MODIFIED);
                SimpleDateFormat setter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                setter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
                Calendar time = new GregorianCalendar();
                response.setHeader(DATE, setter.format(time.getTime()));
                ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * 读取文件
     * @param path   文件地址
     * @return
     */
    public File read(String path){
        File file = new File(path);
        if (!file.exists() || !file.isFile() || file.isHidden()) {
            return null;
        }
        return file;
    }

    /**
     * 输出文件
     * @param file  文件
     * @param ctx
     * @param e
     * @throws IOException
     */
    public void writeFuture(File file,ChannelHandlerContext ctx, MessageEvent e) throws IOException {
        RandomAccessFile raf;

        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, NOT_FOUND, FILE_NOT_FOUND);
            return;
        }

        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        response.setChunked(true);

        response.setHeader(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

        if(downLoadFileName!=null){
            response.setHeader("Content-Disposition", "attachment; filename="+downLoadFileName);
        }

        response.setHeader(CONTENT_LENGTH, fileLength);

        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.setHeader(DATE, dateFormatter.format(time.getTime()));
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.setHeader(EXPIRES, dateFormatter.format(time.getTime()));
        response.setHeader(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.setHeader(LAST_MODIFIED, dateFormatter.format(new Date(file.lastModified())));
        Channel ch = e.getChannel();
        ch.write(response);
        ChannelFuture writeFuture;
        if (ch.getPipeline().get(SslHandler.class) != null) {
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
        } else {
            final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
            writeFuture = ch.write(region);
            writeFuture.addListener(new ChannelFutureProgressListener() {
                public void operationComplete(ChannelFuture future) {
                    region.releaseExternalResources();
                }

                public void operationProgressed(ChannelFuture future, long amount, long current, long total) {
                }
            });
        }

        if (!isKeepAlive((HttpRequest) e.getMessage())) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    public String getFileName(String uri,String pattern) {
        Matcher matcher = Pattern.compile(pattern).matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }


    /**
     * 替换特殊字符
     * @param str
     * @return
     */
    public String specialCharDecoder(String str){
        return str .replace("+"," ")
                .replace("%28","(")
                .replace("%29",")")
                .replace("%23","#")
                .replace("%40","@")
                .replace("%7B","{")
                .replace("%7D","}")
                .replace("%5B","[")
                .replace("%5D","]")
                .replace("%7E","~")
                .replace("%21","!")
                .replace("%24","$")
                .replace("%5E","^")
                .replace("%3D","=")
                .replace("%2C",",")
                .replace("%27","'")
                .replace("%3B",";");
    }
}
