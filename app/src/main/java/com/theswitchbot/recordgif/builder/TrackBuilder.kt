package com.theswitchbot.recordgif.builder

import android.graphics.*
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ResourceUtils
import com.theswitchbot.recordgif.IOwnApp
import com.theswitchbot.recordgif.R
import com.theswitchbot.recordgif.bean.PointX
import com.theswitchbot.recordgif.provider.BitmapHelper

class TrackBuilder {
    private val TAG="GifHelper"

    private var originalBitmap:Bitmap = BitmapFactory.decodeResource(IOwnApp.instance.resources, R.mipmap.icon_background)
    private var mPaint:Paint = Paint()

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.RED
    }


    fun setBaseBitmap(path:String):TrackBuilder{
        if(path.isNotEmpty()){
            originalBitmap = ImageUtils.getBitmap(path)
        }
        return this
    }

    fun getTrackList(trackPath:String):List<Bitmap>{
        val assertPoints = ResourceUtils.readAssets2String(trackPath)
        val lists = GsonUtils.fromJson(assertPoints, PointX::class.java)
        val pairList = arrayListOf<Pair<Point,Point>>()
        val bitmapList = arrayListOf<Bitmap>()
        // 1.俩个点组成一组
        lists.forEachIndexed{ index,pointX ->
            if(index!=lists.size-1){
                pairList.add(Pair(pointX,lists[index+1]))
            }
        }
        //2.组成一组的图像连线转成图片
        pairList.forEach { pair ->
            val bitmap = getMapBitmap(pair.first,pair.second)
            val compressBitmap = BitmapHelper.loadFromBytesByPNG(ImageUtils.compressByQuality(bitmap,60))
            bitmapList.add(compressBitmap)
        }
        return bitmapList
    }

    /**
     * 得到带有直线的俩点连线底库图
     * */
    private fun getMapBitmap(start: Point, end: Point):Bitmap{
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val destCanvas = Canvas(bitmap)
        Log.d(TAG,"point x=$start ,y=$end")
        destCanvas.drawLine(start.x.toFloat(),start.y.toFloat(),end.x.toFloat(),end.y.toFloat(),
            mPaint
        )
        destCanvas.drawBitmap(bitmap,0f,0f, mPaint)
        return bitmap
    }
}