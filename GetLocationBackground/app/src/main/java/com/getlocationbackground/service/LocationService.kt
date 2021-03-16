package com.getlocationbackground.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.getlocationbackground.R
import com.getlocationbackground.receiver.RestartBackgroundService
import com.google.android.gms.location.*
import java.util.*


class LocationService : Service() {
    var counter = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private lateinit var mReceiver: UserStopServiceReceiver

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
            Notification()
        )
        requestLocationUpdates()
        mReceiver = UserStopServiceReceiver()
        val intf = IntentFilter()
        intf.addAction(USER_STOP_SERVICE_REQUEST)
        registerReceiver(mReceiver, intf)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running count::$counter")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        Log.d(TAG, intent?.action.toString())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()

        unregisterReceiver(mReceiver)
//        https://stackoverflow.com/a/13558642/8608146
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        fullStop = sharedPref.getBoolean(getString(R.string.user_stopped_service), false)

        if (fullStop) {
            Log.d(TAG, "Fully stop $fullStop")
            Toast.makeText(
                applicationContext, "Stop Service",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        sendBroadcast(broadcastIntent)
    }

    private var fullStop = false

    inner class UserStopServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //code that handles user specific way of stopping service
            Toast.makeText(
                applicationContext, "Stop IT!!!!",
                Toast.LENGTH_LONG
            ).show()

            Log.d(TAG, "STOP IT!!!!")
            fullStop = true
            stopSelf()
        }
    }


    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                val count = counter++
                if (latitude != 0.0 && longitude != 0.0) {
                    Log.d(
                        "Location::",
                        latitude.toString() + ":::" + longitude.toString() + "Count" +
                                count.toString()
                    )
                }
            }
        }
        timer!!.schedule(
            timerTask,
            0,
            8000
        ) //1 * 60 * 1000 1 minute
    }

    private fun stopTimerTask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.create()
        request.interval = 8000
        request.fastestInterval = 7000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location = locationResult.lastLocation
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("Location Service", "location update $location")
                }
            }, Looper.myLooper()!!)
        }
    }

    companion object {
        private const val TAG = "BOOBA"
        private const val NOTIFICATION_CHANNEL_ID = "com.getlocationbackground"
        const val USER_STOP_SERVICE_REQUEST = "USER_STOP_SERVICE"
    }
}