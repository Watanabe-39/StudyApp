package com.example.studyappvol2

import android.Manifest
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.databinding.ActivityMainBinding
import com.example.studyappvol2.sleepAPI.SleepReceiver
import com.example.studyappvol2.sleepAPI.WeeklySleepTracker
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sleepPendingIntent: PendingIntent
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var dbHelper: DBHelper
    private lateinit var weeklyTracker: WeeklySleepTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        dbHelper = DBHelper(this, "study_app.db", 1)

        // AppBarの設定
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_to_do_list,
                R.id.navigation_study_time,
                R.id.navigation_schedule
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Sleep API 参考: https://github.com/android/codelab-android-sleep/
        // パーミッションリクエストランチャーを設定
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                subscribeToSleepSegmentUpdates()
            } else {
                Log.d(TAG, "ACTIVITY_RECOGNITION permission denied.")
            }
        }

        // パーミッションの確認と購読の開始
        if (activityRecognitionPermissionApproved()) {
            subscribeToSleepSegmentUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }


    }


    // Sleep API購読の開始
    private fun subscribeToSleepSegmentUpdates() {
        Log.d(TAG, "Subscribing to sleep segment updates.")

        // SleepReceiverをPendingIntentとして設定
        sleepPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, SleepReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val task = ActivityRecognition.getClient(this)
                .requestSleepSegmentUpdates(sleepPendingIntent, SleepSegmentRequest.getDefaultSleepSegmentRequest())

            task.addOnSuccessListener {
                Log.d(TAG, "Successfully subscribed to sleep data.")
            }
            task.addOnFailureListener { exception ->
                Log.d(TAG, "Failed to subscribe to sleep data: $exception")
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "SecurityException: Missing ACTIVITY_RECOGNITION permission.")
        }
    }

    // ACTIVITY_RECOGNITIONパーミッションの確認
    private fun activityRecognitionPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
