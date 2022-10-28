package com.theswitchbot.recordgif.gif

import android.graphics.Bitmap
import android.util.Log
import com.blankj.utilcode.util.ImageUtils
import com.theswitchbot.recordgif.IOwnApp
import com.theswitchbot.recordgif.gif.maker.AnimatedGifEncoder
import com.theswitchbot.recordgif.builder.TrackBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class GifEncoder {

    private val TAG = "GifEncoder"
    private var mOutPath:String=""
    private var mTrackFilePath:String=""
    private var mBaseBitmapPath:String=""

    fun setOutPath(path:String):GifEncoder{
        mOutPath = path
        return this
    }

    fun setTrackPath(path:String):GifEncoder{
        mTrackFilePath = path
        return this
    }

    fun setBaseBitmap(path:String):GifEncoder{
        mBaseBitmapPath = path
        return this
    }

    /**
     * 【注意1】开始生成gif的时候，是以第一张图片的尺寸生成gif图的大小，后面几张图片会基于第一张图片的尺寸进行裁切
    所以要生成尺寸完全匹配的gif图的话，应先调整传入图片的尺寸，让其尺寸相同
    【注意2】如果传入的单张图片太大的话会造成OOM，可在不损失图片清晰度先对图片进行质量压缩
     * */
    fun create(delay:Int=1):Boolean{
        if(mOutPath.isEmpty() || mTrackFilePath.isEmpty()){
            return false
        }
        Log.d(TAG, "createGif start")
        val bitmaps = TrackBuilder()
            .setBaseBitmap(mBaseBitmapPath)
            .getTrackList(mTrackFilePath)
        val bos = ByteArrayOutputStream()
        val localAnimatedGifEncoder =
            AnimatedGifEncoder()
        var createSuccess = false
        localAnimatedGifEncoder.apply {
            start(bos)
            setRepeat(0)
            setDelay(delay*60)
            for (i in bitmaps.indices) {
                val addSuccess = addFrame(bitmaps[i])
//                ImageUtils.save(bitmaps[i],"${IOwnApp.instance.filesDir}/${System.currentTimeMillis()}.png",Bitmap.CompressFormat.PNG)
                Log.d(TAG, "createGif addSuccess: ---->$addSuccess")
            }
            finish()
        }

        try {
            val file = File(mOutPath)
            if(!File(file.parent.toString()).exists()){
                File(file.parent.toString()).mkdir()
            }
            if (!file.exists()) {
                file.createNewFile()
            }else{
                file.delete()
            }
            val fos = FileOutputStream(mOutPath)
            bos.writeTo(fos)
            bos.flush()
            fos.flush()
            bos.close()
            fos.close()
            createSuccess =true
            Log.d(TAG, "createGif success")
        } catch (e: Exception) {
            e.printStackTrace()
            bos.close()
            Log.d(TAG, "createGif error: ---->$e")
        }
        return createSuccess
    }

}