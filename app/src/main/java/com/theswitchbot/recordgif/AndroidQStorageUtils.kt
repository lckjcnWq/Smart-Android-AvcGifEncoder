package com.theswitchbot.recordgif

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.FileUtils
import java.io.InputStream

object AndroidQStorageUtils {
    /**
     * mediaStore接口保存
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveBitmap2Public(context: Context, bitmap: Bitmap,srcPath:String,needDelect:Boolean): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_1")
            put(MediaStore.Images.Media.DISPLAY_NAME, "QooCam_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/QooCam")
        }
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var result: Uri? = null
        context.contentResolver?.let { resolver ->
            resolver.insert(contentUri, values)?.let { insertUri ->
                result = insertUri
                resolver.openOutputStream(insertUri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
        return result
    }


    fun deleteFile(context: Context,uri:Uri){
        try {
            context.contentResolver.delete(uri,null,null)
        } catch (e: Exception) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveBitmap2Public(context: Context, bitmap: Bitmap,srcPath: String,relativePath:String,fileName:String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "title_1")
            put(MediaStore.Images.Media.DISPLAY_NAME, if (fileName.contains("png")) fileName.replace("png","jpg") else fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg" )
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/kandao/QooCam")
        }
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var result: Uri? = null
        context.contentResolver?.let { resolver ->
            resolver.insert(contentUri, values)?.let { insertUri ->
                result = insertUri
                resolver.openOutputStream(insertUri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
        return result
    }


    fun saveBitmap2Uir(context: Context, saveUri: Uri, bitmap: Bitmap) {
        context.contentResolver?.let { resolver ->
            resolver.openOutputStream(saveUri)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveFile2Public(context: Context,
                        source: InputStream,
                        fileName: String,
                        dirName:String?): Uri? {
        val values = buildDownloadContentValues(dirName, fileName)
        val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        var result: Uri? = null
        context.contentResolver?.let { resolver ->
            resolver.insert(contentUri, values)?.let { insertUri ->
                result = insertUri
                resolver.openOutputStream(insertUri)?.use { outPut ->
                    var read: Int = -1
                    val buffer = ByteArray(2048)
                    while (source.read(buffer).also { read = it } != -1) {
                        outPut.write(buffer, 0, read)
                    }
                }
            }
        }
        return result
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveVideo2Public(context: Context, source: InputStream,srcPath: String,needDelect: Boolean): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.TITLE, "title_1")
            put(MediaStore.Video.Media.DISPLAY_NAME, "QooCam_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/QooCam")
        }
        val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        var result: Uri? = null
        context.contentResolver?.let { resolver ->
            resolver.insert(contentUri, values)?.let { insertUri ->
                result = insertUri
                resolver.openOutputStream(insertUri)?.use { outPut ->
                    var read: Int = -1
                    val buffer = ByteArray(2014)
                    while (source.read(buffer).also { read = it } != -1) {
                        outPut.write(buffer, 0, read)
                    }
                }
            }
        }
        return result
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveAudio2Public(context: Context, source: InputStream,srcPath: String,needDelect: Boolean): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, "title_1")
            put(MediaStore.Audio.Media.DISPLAY_NAME, "QooCam_${System.currentTimeMillis()}.mp3")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "DCIM/QooCam")
        }
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var result: Uri? = null
        context.contentResolver?.let { resolver ->
            resolver.insert(contentUri, values)?.let { insertUri ->
                result = insertUri
                resolver.openOutputStream(insertUri)?.use { outPut ->
                    var read: Int = -1
                    val buffer = ByteArray(2048)
                    while (source.read(buffer).also { read = it } != -1) {
                        outPut.write(buffer, 0, read)
                    }
                }
            }
        }
        return result
    }

    fun saveAudio2Uri(context: Context, saveUri: Uri, source: InputStream) {
        context.contentResolver?.let { resolver ->
            resolver.openOutputStream(saveUri)?.use { outPut ->
                var read: Int = -1
                val buffer = ByteArray(2048)
                while (source.read(buffer).also { read = it } != -1) {
                    outPut.write(buffer, 0, read)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildDownloadContentValues(dirName: String?,
                                           fileName: String): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "title_1")
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        if(dirName == null){
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }else{
            values.put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$dirName")
        }
        return values
    }


    fun  deleteFile(srcPath:String){
        if(FileUtils.isFileExists(srcPath)){
            FileUtils.delete(srcPath)
        }
    }
}
