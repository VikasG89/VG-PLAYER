package com.mytechnology.video.vgplayer.videos;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.mediacodec.MediaCodecAdapter;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.video.MediaCodecVideoRenderer;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

import java.nio.ByteBuffer;


@UnstableApi
public class CustomMediaCodecVideoRenderer /*extends MediaCodecVideoRenderer*/ {

    /*private static final Object MediaCodecRenderer = ;

    public CustomMediaCodecVideoRenderer(Context context, Handler eventHandler, VideoRendererEventListener eventListener) {
        super(context, MediaCodecRenderer.VIDEO_RENDERER_INDEX, MediaCodecSelector.DEFAULT, C.VIDEO_SCALING_MODE_SCALE_TO_FIT, 0, true, eventHandler, eventListener, 50);
    }

    @Override
    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, @Nullable MediaCodecAdapter codec, @Nullable ByteBuffer buffer, @Nullable MediaCodec.BufferInfo bufferInfo, boolean shouldSkip, long bufferPresentationTimeUs, boolean isDecodeOnlyBuffer, Format format, int bufferIndex) throws ExoPlaybackException {
        // Implement your output buffer processing logic here
        return super.processOutputBuffer(positionUs, elapsedRealtimeUs, codec, buffer, bufferInfo, shouldSkip, bufferPresentationTimeUs, isDecodeOnlyBuffer, format, bufferIndex);
    }

    @Override
    protected void renderOutputBuffer(MediaCodecAdapter codec, int index, long presentationTimeUs) throws ExoPlaybackException {
        // Implement your rendering logic here
        super.renderOutputBuffer(codec, index, presentationTimeUs);
    }

    @Override
    protected void onProcessedOutputBuffer(long presentationTimeUs) {
        // Implement additional processing after output buffer is rendered
        super.onProcessedOutputBuffer(presentationTimeUs);
    }

    @Override
    public boolean supportsFormat(Format format) throws ExoPlaybackException {
        // Check if this renderer supports the given format
        return MimeTypes.isVideo(format.sampleMimeType) && super.supportsFormat(format);
    }

    @Override
    public String getName() {
        return "CustomMediaCodecVideoRenderer";
    }*/
}
