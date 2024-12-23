package com.mytechnology.video.vgplayer.videos;

import static androidx.media3.common.C.FORMAT_HANDLED;

import android.content.Context;
import android.os.Handler;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.Format;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.decoder.CryptoConfig;
import androidx.media3.decoder.Decoder;
import androidx.media3.decoder.DecoderException;
import androidx.media3.decoder.DecoderInputBuffer;
import androidx.media3.decoder.VideoDecoderOutputBuffer;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.video.DecoderVideoRenderer;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

import java.util.ArrayList;

public class CustomExoPlayer {

    private final ExoPlayer player;

    @OptIn(markerClass = UnstableApi.class)
    public CustomExoPlayer(Context context) {
        ExoPlayer.Builder playerBuilder =
                new ExoPlayer.Builder(context)
                        .setAudioAttributes(AudioAttributes.DEFAULT, true)
                        .setHandleAudioBecomingNoisy(true)
                        .setRenderersFactory(new DefaultRenderersFactory(context) {
                            @Override
                            protected void buildVideoRenderers(@NonNull Context context, int extensionRendererMode, @NonNull MediaCodecSelector mediaCodecSelector,
                                                               boolean enableDecoderFallback, @NonNull Handler eventHandler, @NonNull VideoRendererEventListener eventListener,
                                                               long allowedVideoJoiningTimeMs, @NonNull ArrayList<Renderer> out) {
                                out.add(new DecoderVideoRenderer(allowedVideoJoiningTimeMs, eventHandler, eventListener,MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY) {
                                    @NonNull
                                    @Override
                                    protected Decoder<DecoderInputBuffer, ? extends VideoDecoderOutputBuffer, ? extends DecoderException> createDecoder(@NonNull Format format, @Nullable CryptoConfig cryptoConfig) throws DecoderException {
                                        // Return your custom decoder instance here
                                        return null;
                                    }


                                    @Override
                                    protected void renderOutputBufferToSurface(@NonNull VideoDecoderOutputBuffer outputBuffer, @NonNull Surface surface) throws DecoderException {
                                        // Implement your rendering logic here
                                    }

                                    @Override
                                    protected void setDecoderOutputMode(int outputMode) {
                                        // Implement decoder output mode logic here
                                    }

                                    @NonNull
                                    @Override
                                    public String getName() {
                                        return "CustomDecoderVideoRenderer";
                                    }

                                    @Override
                                    public int supportsFormat(@NonNull Format format) throws ExoPlaybackException {
                                        // Return the support level for the given format
                                        return FORMAT_HANDLED;
                                    }
                                });

                                super.buildVideoRenderers(context, extensionRendererMode, mediaCodecSelector, enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
                            }
                        });
        player = playerBuilder.build();
    }

    public ExoPlayer getPlayer() {
        return player;
    }
}
