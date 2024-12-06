package com.example.studyappvol2

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.studyappvol2.database.DBHelper
import com.example.studyappvol2.databinding.ActivityMainBinding
import com.example.studyappvol2.sleepAPI.SleepReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sleepPendingIntent: PendingIntent  // Sleep API用のPendingIntent
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>  // パーミッションリクエスト用ランチャー
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ナビゲーションの設定
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

        // パーミッションリクエストランチャーを初期化
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // パーミッションが許可された場合
                subscribeToSleepSegmentUpdates()
            } else {
                // パーミッションが拒否された場合
                Log.d(TAG, "ACTIVITY_RECOGNITION permission denied.")
            }
        }

        // パーミッションが許可されているか確認
        if (activityRecognitionPermissionApproved()) {
            // 許可されている場合はSleep APIを購読
            subscribeToSleepSegmentUpdates()
        } else {
            // 許可されていない場合はリクエストを送る
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    // Sleep APIの購読を開始するメソッド
    private fun subscribeToSleepSegmentUpdates() {
        Log.d(TAG, "Subscribing to sleep segment updates.")

        // SleepReceiverを受信先として設定
        sleepPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, SleepReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // SleepSegmentRequestを使用して更新をリクエスト
            val task = ActivityRecognition.getClient(this)
                .requestSleepSegmentUpdates(sleepPendingIntent, SleepSegmentRequest.getDefaultSleepSegmentRequest())

            // 成功した場合のリスナー
            task.addOnSuccessListener {
                Log.d(TAG, "Successfully subscribed to sleep data.")
            }
            // 失敗した場合のリスナー
            task.addOnFailureListener { exception ->
                Log.d(TAG, "Failed to subscribe to sleep data: $exception")
            }
        } catch (e: SecurityException) {
            // パーミッションが不足している場合の例外
            Log.d(TAG, "SecurityException: Missing ACTIVITY_RECOGNITION permission.")
        }
    }

    // ACTIVITY_RECOGNITIONパーミッションが許可されているかを確認
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
