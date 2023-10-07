package am.mediastre.mediastreamsampleapp.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import am.mediastre.mediastreamsampleapp.R
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView

class VideoLiveActivity : AppCompatActivity() {

    private val TAG = "SampleApp"
    private lateinit var container: FrameLayout
    private lateinit var playerView: PlayerView
    private var player: MediastreamPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoplayer)
        val config = MediastreamPlayerConfig()
        config.accountID = "64a2f7945ea2ca18c978b025"
        config.id = "64addf1ef36ef35077f2997e"
        config.type = MediastreamPlayerConfig.VideoTypes.LIVE
        config.videoFormat = MediastreamPlayerConfig.AudioVideoFormat.DEFAULT

        config.environment = MediastreamPlayerConfig.Environment.DEV
        config.isDebug = true
        config.accessToken = "7IG2DHt7k2fVZDxAKI5WIzasAtENM9hYZdIrJYfF0lxR3K2mnXK35d9gEFz00PNOw2PmsisAtKQ"
        config.trackEnable = false
        playerView = findViewById(R.id.player_view)
        container = findViewById(R.id.main_media_frame)

        player = MediastreamPlayer(this, config, container, playerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}