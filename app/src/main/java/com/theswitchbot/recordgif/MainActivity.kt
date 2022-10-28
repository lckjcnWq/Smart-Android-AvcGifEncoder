package com.theswitchbot.recordgif

import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import com.theswitchbot.recordgif.encoder.AvcEncoder
import com.theswitchbot.recordgif.gif.GifEncoder
import com.theswitchbot.recordgif.provider.BitmapProvider
import com.theswitchbot.recordgif.builder.TrackBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var originalBitmap: Bitmap
    private lateinit var mPaint: Paint

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        originalBitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_background).copy(Bitmap.Config.ARGB_8888, true)
        mPaint = Paint()
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.RED
        val ivMap = findViewById<GifImageView>(R.id.ivBackground)
        val outGifPath = "${filesDir}/GIFMakerDemo/testDemo.gif"
        val outMp4Path = "${filesDir}/GIFMakerDemo/testDemo.mp4"
        findViewById<Button>(R.id.btnStart1).setOnClickListener {
            MainScope().launch(Dispatchers.IO){
                val createSuccess = GifEncoder()
                    .setOutPath(outGifPath)
                    .setBaseBitmap("")
                    .setTrackPath("track.json")
                    .create()
                Log.d(TAG, "createGif path1: ---->$outGifPath")
                if (createSuccess) {
                    Log.d(TAG, "createGif path2: ---->$outGifPath")
                    runOnUiThread {
                        ivMap.setImageDrawable(GifDrawable(outGifPath))
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnStart2).setOnClickListener {
            MainScope().launch(Dispatchers.IO){
                val mapList = TrackBuilder()
                    .setBaseBitmap("")
                    .getTrackList("track.json")
                AvcEncoder(BitmapProvider(mapList)) { process ->
                    Log.d(TAG, "createMp4 progress: ---->$process")
                }.setFrameRate(10)
                    .setOutPath(outMp4Path)
                    .setBitRate(0)
                    .start()
            }
        }
        val srcPath = "/data/data/com.theswitchbot.recordgif/files/GIFMakerDemo/test1Demo.mp4"
        findViewById<Button>(R.id.btnStart3).setOnClickListener {
            val url = AndroidQStorageUtils.saveFile2Public(this,
                FileInputStream(File(srcPath)),
                "testDemo.mp4",
                null
            )
            Log.d(TAG,"url=$url")
        }
    }
}