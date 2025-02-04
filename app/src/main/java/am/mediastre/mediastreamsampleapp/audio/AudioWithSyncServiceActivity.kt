package am.mediastre.mediastreamsampleapp.audio

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig.AudioVideoFormat
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerServiceWithSync
import am.mediastre.mediastreamplatformsdkandroid.MessageEvent
import am.mediastre.mediastreamplatformsdkandroid.UpdateNotificationEvent
import am.mediastre.mediastreamsampleapp.R
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import kotlin.math.absoluteValue


@UnstableApi
class AudioWithSyncServiceActivity : AppCompatActivity() {

    private val progressPlayerHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val PLAYER_PROGRESS_BAR_UPDATE_TIME_MILLIS = 1000L
    private val TAG = "SAMUELDEBUG"
    private lateinit var container: FrameLayout
    private lateinit var playerView: PlayerView
    private lateinit var btnPlayOrPause: ImageButton
    private lateinit var playerControllerView: PlayerControlView
    private lateinit var tvBroadcastPlayedTime: TextView
    private lateinit var tvBroadcastRemainingTime: TextView
    //    private var player: MediastreamPlayer? = null
    private lateinit var miniPlayerConfig: MediastreamMiniPlayerConfig

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioasserviceplayer)
        playerView = findViewById(R.id.player_view)
        btnPlayOrPause = findViewById(R.id.playOrpause)
        playerControllerView = findViewById(R.id.viewPlayerControllerView)
        tvBroadcastPlayedTime = findViewById(R.id.tvBroadcastPlayedTime)
        tvBroadcastRemainingTime = findViewById(R.id.tvBroadcastRemainingTime)

        val vodMediastreamPlayerConfig = createVodMediastreamPlayerConfig()
        container = findViewById(R.id.main_media_frame)

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), /* requestCode= */ 0)
        }

        setupButtons()
        startService(vodMediastreamPlayerConfig)
    }

    private fun startService(config: MediastreamPlayerConfig){
        miniPlayerConfig = MediastreamMiniPlayerConfig()

        val mediaStreamPlayerCallBack = object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {

            }
            override fun onPlay() {
                Log.d(TAG, "PLAY_EVENT")
                btnPlayOrPause.setImageResource(android.R.drawable.ic_media_pause)
            }

            override fun onPause() {
                Log.d(TAG, "PAUSE_EVENT")
                btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play)
            }

            override fun onReady() {
                Log.d(TAG, "READY_EVENT")
            }

            override fun onEnd() {
                Log.d(TAG, "END_EVENT")
            }

            override fun onPlayerClosed() {
                finish()
            }

            override fun onBuffering() {
                Log.d(TAG, "BUFFERING_EVENT")
                btnPlayOrPause.setImageResource(R.drawable.ic_loading)
            }


            override fun onError(error: String?) {
                Log.d(TAG, "ERROR_EVENT: $error")
                Toast.makeText(this@AudioWithSyncServiceActivity, error, Toast.LENGTH_LONG).show()
            }

            override fun onNext() {
                Log.d(TAG, "NEXT_EVENT")
            }

            override fun onPrevious() {
                Log.d(TAG, "PREVIOUS_EVENT")
            }

            override fun onFullscreen() {
                Log.d(TAG, "FULLSCREEN_ON_EVENT")
            }

            override fun offFullscreen() {
                Log.d(TAG, "FULLSCREEN_OFF_EVENT")
            }

            override fun onNewSourceAdded() {
                Log.d(TAG, "NEW_SOURCE_EVENT")
            }

            override fun onLocalSourceAdded() {
                Log.d(TAG, "LOCAL_SOURCE_EVENT")
            }

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "AD_EVENTS: $type")
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.d(TAG, "AD_ERROR_EVENT: $error")
            }

            override fun onConfigChange(config: MediastreamMiniPlayerConfig?) {

            }

            override fun onCastAvailable(state: Boolean?) {

            }

            override fun onCastSessionStarting() {

            }

            override fun onCastSessionStarted() {

            }

            override fun onCastSessionStartFailed() {

            }

            override fun onCastSessionEnding() {

            }

            override fun onCastSessionEnded() {

            }

            override fun onCastSessionResuming() {

            }

            override fun onCastSessionResumed() {

            }

            override fun onCastSessionResumeFailed() {

            }

            override fun onCastSessionSuspended() {

            }

            override fun onPlaybackErrors(error: JSONObject?) {
                Log.d(TAG, "PLAYBACK_ERRORS_EVENT$error")
            }

            override fun onEmbedErrors(error: JSONObject?) {
                Log.d(TAG, "EMBED_ERRORS_EVENT$error")
            }

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {
                println("<<<<<<currentSong: $data")
            }

            override fun onDismissButton() {}
        }

        MediastreamPlayerServiceWithSync.initializeService(
            this,
            this@AudioWithSyncServiceActivity,
            config,
            container,
            playerView,
            miniPlayerConfig,
            false,
            config.accountID?:"",
            mediaStreamPlayerCallBack
        )
        try {
            controllerFuture =
                MediaController.Builder(
                    this,
                    SessionToken(this, ComponentName(this, MediastreamPlayerServiceWithSync::class.java)),
                )
                    .buildAsync()
            controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())

            Handler(Looper.getMainLooper()).postDelayed({
                playerControllerView.player = MediastreamPlayerServiceWithSync.getMsPlayer()?.msPlayer
                progressBarScrub()
                updatePlayerPlayedTimeAndRemainingTime()
                handleUpdateMiniplayer()
            }, 5000)

        } catch (e: Exception) {
            println("Exception $e")
        }
    }

    private fun setController() {
        val controller = this.controller ?: return
        playerView.player = controller
        playerView.useController = true
        EventBus.getDefault().post(MessageEvent(controller))
    }

    private fun setupButtons() {
        val btnPlayLive = findViewById<Button>(R.id.playLive)
        val btnPlayVod = findViewById<Button>(R.id.playVod)
        val btnUpdateContent = findViewById<Button>(R.id.updateContent)

        btnPlayOrPause.setOnClickListener {
            MediastreamPlayerServiceWithSync.getMsPlayer()?.let { msPlayer ->
                if (msPlayer.isPlaying()) {
                    msPlayer.pause()
                } else {
                    msPlayer.play()
                }
            }
        }

        btnPlayLive.setOnClickListener {
            val config = createLiveMediastreamPlayerConfig()
            MediastreamPlayerServiceWithSync.getMsPlayer()?.reloadPlayer(config)
        }

        btnPlayVod.setOnClickListener {
            val config = createVodMediastreamPlayerConfig()
            MediastreamPlayerServiceWithSync.getMsPlayer()?.reloadPlayer(config)
            startProgressPlayerHandler()
        }

        btnUpdateContent.setOnClickListener {
            handleUpdateMiniplayer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) {
            // Empty results are triggered if a permission is requested while another request was already
            // pending and can be safely ignored in this case.
            return
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, R.string.notification_permission_denied, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onBackPressed() {
        releaseService()
        super.onBackPressed()
    }

    private fun releaseService() {
        Log.d(TAG, "Release service called")
        playerView.player?.release()
        playerView.player = null
        releaseController()
        val stopIntent = Intent(this, MediastreamPlayerServiceWithSync::class.java)
        stopIntent.action = "STOP_SERVICE"
        startService(stopIntent)
    }

    private fun releaseController() {
        if (::controllerFuture.isInitialized){
            MediaController.releaseFuture(controllerFuture)
        }
    }

    private fun handleUpdateMiniplayer() {
        val miniPlayerConfig = MediastreamMiniPlayerConfig()
        miniPlayerConfig.songName = "Test overrideCurrentMiniPlayerConfig title"
        miniPlayerConfig.color = android.graphics.Color.BLACK
        miniPlayerConfig.albumName = "Test Album name"
        miniPlayerConfig.description = "Test description for current notification"
        miniPlayerConfig.imageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"
        miniPlayerConfig.imageIconUrl = R.drawable.ic_notification_small
        EventBus.getDefault().post(UpdateNotificationEvent(miniPlayerConfig))
    }

    private fun createLiveMediastreamPlayerConfig(): MediastreamPlayerConfig {
        val config = MediastreamPlayerConfig()
        config.accountID = "5faaeb72f92d7b07dfe10181"
        config.id = "5fada514fc16c006bd63370f"
        config.type = MediastreamPlayerConfig.VideoTypes.LIVE
        config.videoFormat = AudioVideoFormat.DEFAULT
        config.isDebug = true
        config.customPlayerView = playerView
        config.trackEnable = false
        config.showControls = true
        config.adURL = "/"
        return config
    }

    private fun createVodMediastreamPlayerConfig(): MediastreamPlayerConfig {
        val config = MediastreamPlayerConfig()
        config.accountID = "5faaeb72f92d7b07dfe10181"
        config.id = "67994704ee0670fe8f1557ed"
        config.type = MediastreamPlayerConfig.VideoTypes.VOD
        config.startAt = 40
        config.videoFormat = AudioVideoFormat.MP3
        config.isDebug = true
        config.customPlayerView = playerView
        config.trackEnable = false
        config.loadNextAutomatically = false
        config.appName = "MediastreamAppTest"
        return config
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun progressBarScrub() {
        val exoProgress = playerView.findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
        exoProgress?.addListener(object : TimeBar.OnScrubListener{
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                progressPlayerHandler.removeCallbacksAndMessages(null)
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                val playedSeconds = position.div(1000L)
                tvBroadcastPlayedTime.text =
                    playedSeconds.secsToHoursMinutesAndSecondsAsString(leadingZeros = false)
                tvBroadcastRemainingTime.text = playedSeconds
                    .minus(playerControllerView.getTotalTimeInSeconds())
                    .coerceAtMost(0L)
                    .secsToHoursMinutesAndSecondsAsString(leadingZeros = false)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                startProgressPlayerHandler()
            }
        })
    }

    fun Long.secsToHoursMinutesAndSecondsAsString(leadingZeros: Boolean = true): String {
        val sign = if (this < 0) "-" else ""
        val realValue = this.absoluteValue
        val hours = realValue / 3600
        val minutes = (realValue % 3600) / 60
        val seconds = realValue % 60
        return if (hours > 0) {
            val sHour = if (leadingZeros && hours < 10) "0$hours" else "$hours"
            val sMinutes = if (minutes < 10) "0$minutes" else "$minutes"
            val sSeconds = if (seconds < 10) "0$seconds" else "$seconds"
            "$sign$sHour:$sMinutes:$sSeconds"
        } else {
            val sMinutes = if (leadingZeros && minutes < 10) "0$minutes" else "$minutes"
            val sSeconds = if (seconds < 10) "0$seconds" else "$seconds"
            "$sign$sMinutes:$sSeconds"
        }
    }

    @OptIn(UnstableApi::class)
    fun PlayerControlView.getTotalTimeInSeconds(): Long {
        return player?.contentDuration?.div(1000L) ?: 0L
    }
    @OptIn(UnstableApi::class)
    fun PlayerControlView.getPlayedTimeAsString(): String {
        val playedTime = player?.currentPosition?.div(1000L) ?: 0L
        return playedTime.secsToHoursMinutesAndSecondsAsString(leadingZeros = false)
    }
    @OptIn(UnstableApi::class)
    fun PlayerControlView.getRemainingTimeAsString(): String {
        val playedTime = player?.currentPosition?.div(1000L) ?: 0L
        val totalTime = getTotalTimeInSeconds()

        return if (totalTime == 0L) {
            0L.secsToHoursMinutesAndSecondsAsString(leadingZeros = false)
        } else {
            (playedTime - totalTime).secsToHoursMinutesAndSecondsAsString(leadingZeros = false)
        }
    }

    private fun startProgressPlayerHandler() {
        progressPlayerHandler.postDelayed(object : Runnable {
            override fun run() {
                updatePlayerPlayedTimeAndRemainingTime()
                progressPlayerHandler.postDelayed(
                    this,
                    PLAYER_PROGRESS_BAR_UPDATE_TIME_MILLIS
                )
            }
        }, PLAYER_PROGRESS_BAR_UPDATE_TIME_MILLIS)
    }

    private fun updatePlayerPlayedTimeAndRemainingTime() {
        tvBroadcastPlayedTime.text = playerControllerView.getPlayedTimeAsString()
        tvBroadcastRemainingTime.text = playerControllerView.getRemainingTimeAsString()
    }

    override fun onDestroy() {
        progressPlayerHandler.removeCallbacksAndMessages(null)
        // releaseService()
        super.onDestroy()
    }

}

