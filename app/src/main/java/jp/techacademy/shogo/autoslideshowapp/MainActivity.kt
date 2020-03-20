package jp.techacademy.shogo.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val NEXT = "NEXT"
    private val BACK = "BACK"

    //URIを入れる配列を用意
    private val uriArray = arrayListOf<Uri>()
    //配列のインデックス
    private var index = 0

    private var mTimer: Timer? = null
    private var mTimerSec = 0
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //パーミッションの許可状態を確認する
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                //外部ストレージへのアクセスが許可されている
                getContentsInfo()
            } else {
                //許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSIONS_REQUEST_CODE)
            }
         } else {
            //Android５系以下の場合
            getContentsInfo()
        }
        //進むボタン押下時
        next.setOnClickListener{
            changeIndex(NEXT)
            picture.setImageURI(uriArray[index])
        }
        //戻るボタン押下時
        back.setOnClickListener{
            changeIndex(BACK)
            picture.setImageURI(uriArray[index])
        }
        //再生・停止ボタン押下時
        start_stop.setOnClickListener{

            if(mTimer == null) {
                //再生ボタン押下
                //タイマー作成
                mTimer = Timer()
                //タイマー始動
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 1
                        mHandler.post {
                            changeIndex(NEXT)
                            picture.setImageURI(uriArray[index])
                        }
                    }
                }, 2000, 2000)//最初に始動させるまで２秒、ループの間隔を２秒。
                start_stop.text ="停止"
            }else{
                //停止ボタン押下
                start_stop.text ="再生"
                mTimer!!.cancel()
                mTimer = null

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<String>,grantResults:IntArray){
        when(requestCode){
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo(){
        //画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (cursor!!.moveToFirst()){
            do{
                //indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id)

                uriArray.add(imageUri)
            }while(cursor.moveToNext())
        }

        if(uriArray.size != 0){
            picture.setImageURI(uriArray[1])
        }

    }

    private fun changeIndex(flg:String){
        when (flg){
            NEXT ->  if (index == uriArray.size - 1) {
                       index = 0
                    } else{
                       index += 1
                    }

            BACK -> if (index == 0) {
                        index = uriArray.size - 1
                    } else{
                        index -= 1
                    }

        }
    }
}
