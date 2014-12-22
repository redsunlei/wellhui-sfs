package com.wellhui.bs.sfs;

import com.wellhui.bs.sfs.handler.*;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * sfs
 *
 * @author ≥¬”·¡ÿ
 * @version 2012-9-17 ≥¬”·¡ÿ
 * @since 1.0
 */
public class PipelineFactory implements ChannelPipelineFactory {
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("portraits", new PortraitsHandler());
        pipeline.addLast("product", new ProductImagesHandler());
        pipeline.addLast("product-thumbnails", new ProductThumbnailsHandler());
        pipeline.addLast("promotion", new PromotionImagesHandler());
        pipeline.addLast("promotion-thumbnails", new PromotionThumbnailsHandler());
        pipeline.addLast("temp", new TempHandler());
        return pipeline;
    }
}
