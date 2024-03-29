package com.akerusan.myrecorder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private var fileName:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
                                      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                      android.Manifest.permission.READ_EXTERNAL_STORAGE)

            ActivityCompat.requestPermissions(this, permissions,0)
        }

        // Recorder
        mediaRecorder = MediaRecorder()
        // Player
        mediaPlayer = MediaPlayer()

        output = Environment.getExternalStorageDirectory().absolutePath + "/"

        mediaRecorder?.setOutputFile(output + "sample.mp3")
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        // Button Listeners
        button_start_recording.setOnClickListener { startRecording() }
        button_stop_recording.setOnClickListener{ stopRecording() }
        button_pause_recording.setOnClickListener { pauseRecording() }
        button_play.setOnClickListener { playRecording() }
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if(state) {
            if(!recordingStopped){
                Toast.makeText(this,"Stopped!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                button_pause_recording.text = "Resume"
            }else{
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this,"Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        button_pause_recording.text = "Pause"
        recordingStopped = false
    }

    private fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            renameAudioFile()
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renameAudioFile(){
        val mBuilder: AlertDialog.Builder  = AlertDialog.Builder(this)

        val mView: View = layoutInflater.inflate(R.layout.alert_dialog, null)
        val text: EditText = mView.findViewById(R.id.text)
        val submit: Button = mView.findViewById(R.id.submit)
        mBuilder.setView(mView)
        val dialog: AlertDialog = mBuilder.create()

        submit.setOnClickListener{

            fileName = text.text.toString()
            val newFile = File(output, "$fileName.mp3")
            val oldFile = File(output, "sample.mp3")
            oldFile.renameTo(newFile)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun playRecording(){
        if(!mediaPlayer!!.isPlaying && !state){
            Toast.makeText(this,"Playing!", Toast.LENGTH_SHORT).show()
            mediaPlayer?.setDataSource("$output$fileName.mp3")
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        }
    }
}
