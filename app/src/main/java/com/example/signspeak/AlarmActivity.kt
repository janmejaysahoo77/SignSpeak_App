package com.example.signspeak

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var tvSelectedTime: TextView
    private lateinit var btnSetAlarm: Button
    private lateinit var btnCancelAlarm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        requestExactAlarmPermission()

        timePicker = findViewById(R.id.timePicker)
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        btnCancelAlarm = findViewById(R.id.btnCancelAlarm)

        btnSetAlarm.setOnClickListener { setAlarm() }
        btnCancelAlarm.setOnClickListener { cancelAlarm() }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun setAlarm() {
        val hour = timePicker.hour
        val minute = timePicker.minute

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(AlarmManager::class.java)

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        tvSelectedTime.text = "Alarm Set"
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent)

        tvSelectedTime.text = "Alarm Cancelled"
    }
}
