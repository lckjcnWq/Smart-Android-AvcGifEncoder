package com.theswitchbot.recordgif.encoder

import android.graphics.Bitmap
import android.media.MediaCodecInfo

object YuvUtils {
    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                V = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128 // Previously U
                U = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128 // Previously V
                yuv420sp[yIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else Math.min(V, 255)).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else Math.min(U, 255)).toByte()
                }
                index++
            }
        }
    }

    private fun encodeYUV420P(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + width * height / 4
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                V = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128 // Previously U
                U = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128 // Previously V
                yuv420sp[yIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[vIndex++] = (if (U < 0) 0 else Math.min(U, 255)).toByte()
                    yuv420sp[uIndex++] = (if (V < 0) 0 else Math.min(V, 255)).toByte()
                }
                index++
            }
        }
    }

    private fun encodeYUV420PSP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                V = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128 // Previously U
                U = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128 // Previously V
                yuv420sp[yIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[yIndex + 1] = (if (V < 0) 0 else Math.min(V, 255)).toByte()
                    yuv420sp[yIndex + 3] = (if (U < 0) 0 else Math.min(U, 255)).toByte()
                }
                if (index % 2 == 0) {
                    yIndex++
                }
                index++
            }
        }
    }

    fun encodeYUV420PP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        var yIndex = 0
        var vIndex = yuv420sp.size / 2
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                V = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128 // Previously U
                U = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128 // Previously V
                if (j % 2 == 0 && index % 2 == 0) { // 0
                    yuv420sp[yIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                    yuv420sp[yIndex + 1] = (if (V < 0) 0 else Math.min(V, 255)).toByte()
                    yuv420sp[vIndex + 1] = (if (U < 0) 0 else Math.min(U, 255)).toByte()
                    yIndex++
                } else if (j % 2 == 0 && index % 2 == 1) { //1
                    yuv420sp[yIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                } else if (j % 2 == 1 && index % 2 == 0) { //2
                    yuv420sp[vIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                    vIndex++
                } else if (j % 2 == 1 && index % 2 == 1) { //3
                    yuv420sp[vIndex++] = (if (Y < 0) 0 else Math.min(Y, 255)).toByte()
                }
                index++
            }
        }
    }

    fun getNV12(colorFormat:Int,inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {
        // Reference (Variation) : https://gist.github.com/wobbals/5725412
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> encodeYUV420SP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> encodeYUV420P(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar -> encodeYUV420PSP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar -> encodeYUV420PP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
        }
        return yuv
    }
}