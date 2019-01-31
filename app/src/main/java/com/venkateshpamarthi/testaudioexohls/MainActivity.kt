package com.venkateshpamarthi.testaudioexohls

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.ProgressBar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.util.Util.getUserAgent
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    lateinit var playerView : PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.video_view)
        var intent = Intent(this, AudioService::class.java)
        startForegroundService(this, intent)
    }

    private fun startForegroundService(context: Context, intent: Intent): ComponentName? {
        return if (Util.SDK_INT >= 26) {
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            context.startForegroundService(intent)
        } else {
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            context.startService(intent)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is AudioService.VideoServiceBinder) {
                print("service audio service player set")
                playerView.player = service.getExoPlayerInstance()
            }
        }
    }

    override fun onDestroy() {
        unbindService(connection)
        super.onDestroy()

    }
}
