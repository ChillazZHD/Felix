package com.example.felix

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*



class MainActivity : AppCompatActivity() {

    val scope = CoroutineScope(Dispatchers.IO)

    private val REQUEST_CODE_SPEECH_INPUT = 100

    private var INPUT_STRING = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_voice_btn.setOnClickListener {
            speak();
        }
    }

    private fun speak() {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak someting")
        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT -> {
                if(resultCode == Activity.RESULT_OK && null != data){
                    val result = data.getStringArrayListExtra((RecognizerIntent.EXTRA_RESULTS))



                    scope.launch {
                        INPUT_STRING = sendPostRequest(result[0])
                        //scope.cancel()

                    }

                    Thread.sleep(5000)

                    println(INPUT_STRING)


                    val url = "http://93.214.71.241/upload/$INPUT_STRING"// your URL here

                    println(url)

                    var mediaPlayer: MediaPlayer? = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(url)
                        prepare() // might take long! (for buffering, etc)
                        start()
                    }
                    if (mediaPlayer != null) {
                        while(mediaPlayer.isPlaying){
                            Thread.sleep(500)
                        }
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null

                }
            }
        }
    }




    suspend fun sendPostRequest(req:String):String = withContext(Dispatchers.IO){


            var reqParam = URLEncoder.encode("req", "UTF-8") + "=" + URLEncoder.encode(req, "UTF-8")
            val mURL = URL("http://93.214.71.241/enterReq.php")
            val response = StringBuffer()


                with(mURL.openConnection() as HttpURLConnection) {
                    // optional default is GET
                    requestMethod = "POST"

                    val wr = OutputStreamWriter(getOutputStream());
                    wr.write(reqParam);
                    wr.flush();

                    println("URL : $url")
                    println("Response Code : $responseCode")

                    BufferedReader(InputStreamReader(inputStream)).use {


                        var inputLine = it.readLine()
                        while (inputLine != null) {
                            response.append(inputLine)
                            inputLine = it.readLine()
                        }
                        println("Response : $response")
                    }
                }


        return@withContext (response.toString())
    }


}
