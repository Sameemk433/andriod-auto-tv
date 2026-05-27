package com.autoplaytv

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var btnSelect: Button
    private lateinit var btnClear: Button
    private lateinit var btnLoop: Button
    private lateinit var txtStatus: TextView
    private lateinit var controlsLayout: View
    
    private lateinit var prefs: SharedPreferences
    private var videoUris = mutableListOf<Uri>()
    private var isLoopEnabled = true
    private var controlsVisible = true

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            videoUris.clear()
            
            // Multiple selection
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    takeUriPermission(uri)
                    videoUris.add(uri)
                }
            }
            // Single selection
            data?.data?.let { uri ->
                takeUriPermission(uri)
                videoUris.add(uri)
            }
            
            if (videoUris.isNotEmpty()) {
                savePlaylist()
                setupPlayer()
                Toast.makeText(this, "${videoUris.size} videos saved", Toast.LENGTH_LONG).show()
                hideControls()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("autoplay_prefs", MODE_PRIVATE)
        
        playerView = findViewById(R.id.playerView)
        btnSelect = findViewById(R.id.btnSelect)
        btnClear = findViewById(R.id.btnClear)
        btnLoop = findViewById(R.id.btnLoop)
        txtStatus = findViewById(R.id.txtStatus)
        controlsLayout = findViewById(R.id.controlsLayout)

        player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
        playerView.player = player
        playerView.useController = false // Hide default controls for TV

        loadPlaylist()
        isLoopEnabled = prefs.getBoolean("loop", true)
        updateLoopButton()

        btnSelect.setOnClickListener { openVideoPicker() }
        btnClear.setOnClickListener { clearPlaylist() }
        btnLoop.setOnClickListener { toggleLoop() }

        // Auto-start if booted or playlist exists
        val isAutoStart = intent.getBooleanExtra("autostart", false)
        if (videoUris.isNotEmpty()) {
            setupPlayer()
            if (isAutoStart) {
                hideControls()
                Toast.makeText(this, "Auto-playing on boot", Toast.LENGTH_SHORT).show()
            }
        } else {
            showControls()
            txtStatus.text = "Press OK to select videos from USB or storage"
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                     Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        pickVideos.launch(intent)
    }

    private fun takeUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            // Already granted
        }
    }

    private fun setupPlayer() {
        if (videoUris.isEmpty()) return
        
        val mediaItems = videoUris.map { MediaItem.fromUri(it) }
        player.setMediaItems(mediaItems)
        player.repeatMode = if (isLoopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        player.prepare()
        player.play()

        txtStatus.text = "Playing ${videoUris.size} video(s) - LOOP: ${if(isLoopEnabled) "ON" else "OFF"}"
    }

    private fun savePlaylist() {
        val uriStrings = videoUris.joinToString("|") { it.toString() }
        prefs.edit()
            .putString("playlist", uriStrings)
            .putBoolean("loop", isLoopEnabled)
            .apply()
    }

    private fun loadPlaylist() {
        val saved = prefs.getString("playlist", "") ?: ""
        if (saved.isNotEmpty()) {
            videoUris = saved.split("|").mapNotNull {
                try { Uri.parse(it) } catch (e: Exception) { null }
            }.toMutableList()
        }
    }

    private fun clearPlaylist() {
        videoUris.clear()
        player.clearMediaItems()
        prefs.edit().remove("playlist").apply()
        txtStatus.text = "Playlist cleared. Select new videos."
        showControls()
    }

    private fun toggleLoop() {
        isLoopEnabled = !isLoopEnabled
        player.repeatMode = if (isLoopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        prefs.edit().putBoolean("loop", isLoopEnabled).apply()
        updateLoopButton()
        txtStatus.text = "Loop: ${if(isLoopEnabled) "ON" else "OFF"}"
    }

    private fun updateLoopButton() {
        btnLoop.text = if (isLoopEnabled) "Loop: ON" else "Loop: OFF"
    }

    private fun showControls() {
        controlsLayout.visibility = View.VISIBLE
        controlsVisible = true
        btnSelect.requestFocus()
    }

    private fun hideControls() {
        controlsLayout.visibility = View.GONE
        controlsVisible = false
    }

    // TV Remote: Press MENU or BACK to show controls, OK to hide
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (controlsVisible) {
                    hideControls()
                } else {
                    showControls()
                }
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (player.isPlaying) player.pause() else player.play()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        // Keep playing even in background for TV
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}