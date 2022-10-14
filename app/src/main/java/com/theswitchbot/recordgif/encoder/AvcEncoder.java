package com.theswitchbot.recordgif.encoder;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.theswitchbot.recordgif.inter.IProvider;
import com.theswitchbot.recordgif.inter.IProviderExpand;
import com.theswitchbot.recordgif.inter.Processable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class AvcEncoder {
    private final static String TAG = "MeidaCodec";
    private int mFrameRate = 15;
    private File out;
    private int bitRate = 0;
    private IProvider<Bitmap> mProvider;
    private Processable mProcessable;
    private MediaCodec mediaCodec;
    public boolean isRunning;
    private MediaMuxer mediaMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;
    private int colorFormat;


    public AvcEncoder setFrameRate(int framerate) {
        if (framerate < 10) {
            this.mFrameRate = 10;
        } else if (framerate > 60) {
            this.mFrameRate = 60;
        } else {
            this.mFrameRate = framerate;
        }
        return this;
    }

    public AvcEncoder setBitRate(int bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    public AvcEncoder setOutPath(String path) {
        try {
            File out = new File(path);
            this.out = out;
            File parentPath = new File(Objects.requireNonNull(out.getParent()));
            if (!parentPath.exists()) {
                parentPath.mkdir();
            }
            if (!out.exists()) {
                out.createNewFile();
            } else {
                out.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public AvcEncoder(IProvider<Bitmap> provider, Processable processable) {
        this.mProvider = provider;
        this.mProcessable = processable;
        this.isRunning = false;
        this.mTrackIndex = 0;
        this.mMuxerStarted = false;
    }

    private void init(int width, int height) {
        Log.d(TAG, "init width =" + width + "  ,height=" + height);
        int bitRate0 = bitRate;
        if (bitRate == 0) {
            bitRate0 = width * height;
        }

        int[] formats = this.getMediaCodecList();

        lab:
        for (int format : formats) {
            switch (format) {
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar: // yuv420sp
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar: // yuv420p
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar: // yuv420psp
                    colorFormat = format;
                    break lab;
                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar: // yuv420pp
                    colorFormat = format;
                    break lab;
            }
        }
        if (colorFormat <= 0) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);//COLOR_FormatYUV420SemiPlanar
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate0);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //创建生成MP4初始化对象
            mediaMuxer = new MediaMuxer(out.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        isRunning = true;
    }

    public int[] getMediaCodecList() {
        //获取解码器列表
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (int i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                continue;
            }
            String[] types = info.getSupportedTypes();
            boolean found = false;
            //轮训所要的解码器
            for (int j = 0; j < types.length && !found; j++) {
                if (types[j].equals("video/avc")) {
                    found = true;
                }
            }
            if (!found) {
                continue;
            }
            codecInfo = info;
        }
        Log.d(TAG, "found" + codecInfo.getName() + "supporting" + " video/avc");
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        return capabilities.colorFormats;
    }

    public void finish() {
        isRunning = false;
        try {
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (mediaMuxer != null) {
                if (mMuxerStarted) {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mProvider instanceof IProviderExpand) {
            ((IProviderExpand<Bitmap>) mProvider).finish();
        }
    }

    public void start() {
        try {
            if (mProvider instanceof IProviderExpand) {
                ((IProviderExpand<Bitmap>) mProvider).prepare();
            }
            if (mProvider.size() > 0) {
                mProcessable.onProcess(1);
                Bitmap bitmap = mProvider.next();
                if (bitmap != null) {
                    init(getSize(bitmap.getWidth()), getSize(bitmap.getHeight()));
                    mProcessable.onProcess(2);
                    run(bitmap);
                }
            }
        } finally {
            finish();
            mProcessable.onProcess(100);
        }
    }

    private int getSize(int size) {
        return size / 4 * 4;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / mFrameRate;
    }

    private void drainEncoder(boolean endOfStream, MediaCodec.BufferInfo bufferInfo) {

        ByteBuffer[] buffers = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buffers = mediaCodec.getOutputBuffers();
        }

        if (endOfStream) {
            try {
                mediaCodec.signalEndOfInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while (true) {
            int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break; // out of while
                } else {
                    Log.i(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }

                MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                mTrackIndex = mediaMuxer.addTrack(mediaFormat);
                mediaMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.i(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else {
                ByteBuffer outputBuffer = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = buffers[encoderStatus];
                } else {
                    outputBuffer = mediaCodec.getOutputBuffer(encoderStatus);
                }

                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer "
                            + encoderStatus + " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    Log.d(TAG, "BufferInfo: " + bufferInfo.offset + ","
                            + bufferInfo.size + ","
                            + bufferInfo.presentationTimeUs);

                    try {
                        mediaMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
                    } catch (Exception e) {
                        Log.i(TAG, "Too many frames");
                    }

                }

                mediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.i(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.i(TAG, "end of stream reached");
                    }
                    break; // out of while
                }
            }
        }
    }

    public void run(Bitmap bitmap) {
        isRunning = true;
        long generateIndex = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        ByteBuffer[] buffers = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buffers = mediaCodec.getInputBuffers();
        }

        while (isRunning) {
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                long ptsUsec = computePresentationTime(generateIndex);
                if (generateIndex >= mProvider.size()) {
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, ptsUsec,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isRunning = false;
                    drainEncoder(true, info);

                } else {
                    if (bitmap == null) {
                        bitmap = mProvider.next();
                    }
                    byte[] input = YuvUtils.INSTANCE.getNV12(colorFormat,getSize(bitmap.getWidth()), getSize(bitmap.getHeight()), bitmap);
                    if (mProvider instanceof IProviderExpand) {
                        ((IProviderExpand<Bitmap>) mProvider).finishItem(bitmap);
                    }
                    bitmap = null;
                    //有效的空的缓存区
                    ByteBuffer inputBuffer = null;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        inputBuffer = buffers[inputBufferIndex];
                    } else {
                        inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);//inputBuffers[inputBufferIndex];
                    }
                    inputBuffer.clear();
                    inputBuffer.put(input);
                    //将数据放到编码队列
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, ptsUsec, 0);
                    drainEncoder(false, info);
                }

                mProcessable.onProcess((int) (generateIndex * 96 / mProvider.size()) + 2);

                generateIndex++;
            } else {
                Log.i(TAG, "input buffer not available");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
