package com.venkateshpamarthi.testaudioexohls

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.support.annotation.Nullable
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class AudioService : Service() {
    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return VideoServiceBinder()
    }

    var player: SimpleExoPlayer? = null
    val PLAYBACK_NOTIFICATION_ID = 1
    lateinit var context: Context
    private var playerNotificationManager: PlayerNotificationManager? = null

    private lateinit var mediaSession: MediaSessionCompat;

    private lateinit var mediaSessionConnector: MediaSessionConnector;
    inner class VideoServiceBinder : Binder() {
        fun getExoPlayerInstance() = player
    }
    override fun onCreate() {
        super.onCreate()
        context = this
        val adaptiveTrackSelection = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
        player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelection),
            DefaultLoadControl()
        )
        val defaultBandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "Exo2"), defaultBandwidthMeter)
        val hlsUrl = "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8"
        val uri = Uri.parse(hlsUrl)
        val mainHandler = Handler()
        val mediaSource = HlsMediaSource(uri, dataSourceFactory, mainHandler, null)
        player?.prepare(mediaSource)
        player?.setPlayWhenReady(true)
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,"playback_channel", R.string.playback_channel_name,1,object: PlayerNotificationManager.MediaDescriptionAdapter{
                override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                    var intent = Intent(context, MainActivity::class.java)
                    return PendingIntent.getActivity(context,1,intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                override fun getCurrentContentText(player: Player?): String? {
                    return "Sample Description"
                }

                override fun getCurrentContentTitle(player: Player?): String {
                    return "Sample Title"
                }

                override fun getCurrentLargeIcon(
                    player: Player?,
                    callback: PlayerNotificationManager.BitmapCallback?
                ): Bitmap? {
                    return null
                }
            }
        )

        playerNotificationManager?.setNotificationListener(object: PlayerNotificationManager.NotificationListener{
            override fun onNotificationCancelled(notificationId: Int) {
                stopSelf()
            }

            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
            }
        })
        playerNotificationManager?.setPlayer(player)
        mediaSession = MediaSessionCompat(context, "MEDIA SESSION_TAG")
        mediaSession.isActive = true
        playerNotificationManager?.setMediaSessionToken(mediaSession.sessionToken)
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setQueueNavigator(object: TimelineQueueNavigator(mediaSession){
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return MediaDescriptionCompat.Builder().setMediaId("6578123").setTitle("Media title").build()
            }
        })
        mediaSessionConnector.setPlayer(player,null)
    }

    override fun onDestroy() {
        mediaSession.release()
        mediaSessionConnector.setPlayer(null,null)
        playerNotificationManager?.setPlayer(null)
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
