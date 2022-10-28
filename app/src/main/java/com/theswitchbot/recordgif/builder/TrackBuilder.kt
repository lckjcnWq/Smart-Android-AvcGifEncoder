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
    private val TAG = "GifHelper"

    private var preBitmap = BitmapFactory.decodeResource(IOwnApp.instance.resources, R.mipmap.icon_background)
    private var mPaint: Paint = Paint()

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.RED
        mPaint.strokeWidth = 6f
    }


    fun setBaseBitmap(path: String): TrackBuilder {
        if (path.isNotEmpty()) {
            preBitmap = ImageUtils.getBitmap(path)
        }
        return this
    }

    /**
     * 获取轨迹图列表用于播放每一帧gif或者视频
     * */
    fun getTrackList(trackPath: String, pointStep: Int = 3): List<Bitmap> {
        val lists = ArrayList<Point>()

        for(index in 0..100){
            val x = index*10
            val y = index*10
            lists.add(Point(x,y))
        }
        val bitmapList = arrayListOf<Bitmap>()

        // 1.每step个点组成一组图片
        val pointStepList = arrayListOf<List<Point>>()
        val sliceNum = lists.size/pointStep + 1
        for (index in 0 until sliceNum) {
            val start = index  * pointStep
            var end = (index + 1) * pointStep
            if(end > lists.size){  //截取到边界
                end = lists.size
            }
            Log.d(TAG,"start =$start ,end=$end")
            if(start==0){
                pointStepList.add(lists.subList(0, end))
            }else{
                pointStepList.add(lists.subList(start-1, end))
            }
        }
        // 1.每step个点绘制成一组图片
        pointStepList.forEach { list ->
            val bitmap = getMapBitmap(list)
            val compressBitmap = BitmapHelper.loadFromBytesByPNG(ImageUtils.compressByQuality(bitmap, 60))
            bitmapList.add(compressBitmap)
        }
        return bitmapList
    }

    /**
     * 得到带有直线的俩点连线底库图
     * */
    private fun getMapBitmap(start: Point, end: Point): Bitmap {
        val bitmap = preBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val destCanvas = Canvas(bitmap)
        Log.d(TAG, "point x=$start ,y=$end")
        destCanvas.drawLine(
            start.x.toFloat(), start.y.toFloat(), end.x.toFloat(), end.y.toFloat(),
            mPaint
        )
        destCanvas.drawBitmap(bitmap, 0f, 0f, mPaint)
        preBitmap = bitmap
        return preBitmap
    }

    /**
     * 得到带有多个点连线底库图
     * */
    private fun getMapBitmap(pointList:List<Point>): Bitmap {
        val bitmap = preBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val destCanvas = Canvas(bitmap)
        pointList.forEachIndexed { index, point ->
            Log.d(TAG,"index =$index  ,point=$point")
            if(index != pointList.size-1){
                destCanvas.drawLine(point.x.toFloat(), point.y.toFloat(), pointList[index+1].x.toFloat(), pointList[index+1].y.toFloat(), mPaint)
            }
        }
        destCanvas.drawBitmap(bitmap, 0f, 0f, mPaint)
        preBitmap = bitmap
        return bitmap
    }
}