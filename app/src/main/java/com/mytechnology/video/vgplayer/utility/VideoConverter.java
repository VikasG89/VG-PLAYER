package com.mytechnology.video.vgplayer.utility;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public class VideoConverter {
    private final Context context;

    public VideoConverter(Context context) {
        this.context = context;
    }

    public void convertVideo(Uri inputUri) {
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(context, inputUri, null);

            int trackIndex = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                assert mime != null;
                if (mime.startsWith("video/")) {
                    trackIndex = i;
                    break;
                }
            }

            if (trackIndex == -1) {
                Log.e("VideoConverter", "No valid video track found");
                return;
            }

            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);
            MediaCodec codec = MediaCodec.createDecoderByType(Objects.requireNonNull(format.getString(MediaFormat.KEY_MIME)));
            codec.configure(format, null, null, 0);
            codec.start();

            File outputFile = new File(context.getFilesDir(), "converted_video.mp4");
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            /*ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            while (true) {
                //int inputIndex = codec.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inputIndex];
                    int sampleSize = extractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        break;
                    } else {
                        codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }

                //int outputIndex = codec.dequeueOutputBuffer(info, 10000);
                if (outputIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputIndex];
                    byte[] bufferData = new byte[info.size];
                    outputBuffer.get(bufferData);
                    outputStream.write(bufferData);
                    codec.releaseOutputBuffer(outputIndex, false);
                }
            }*/

            int inputIndex = codec.dequeueInputBuffer(10000);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = codec.getInputBuffer(inputIndex);
                assert inputBuffer != null;
                int sampleSize = extractor.readSampleData(inputBuffer, 0);
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    return;
                } else {
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                    extractor.advance();
                }
            }

            int outputIndex = codec.dequeueOutputBuffer(info, 10000);
            if (outputIndex >= 0) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);
                byte[] bufferData = new byte[info.size];
                assert outputBuffer != null;
                outputBuffer.get(bufferData);
                outputStream.write(bufferData);
                codec.releaseOutputBuffer(outputIndex, false);
            }


            codec.stop();
            codec.release();
            extractor.release();
            outputStream.close();
            Log.d("VideoConverter", "Video successfully converted to " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("VideoConverter", "Error converting video", e);
        }
    }
}





