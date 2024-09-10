package com.rnscheduleclearcache

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

class RnScheduleClearCacheModule internal constructor(context: ReactApplicationContext) :
        RnScheduleClearCacheSpec(context) {
  private val alarmManager =
          reactApplicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

  override fun getName(): String {
    return NAME
  }

  private fun formatSize(size: Long): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format(
            Locale.ROOT,
            "%.1f %s",
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
    )
  }

  private fun getDirSize(dir: File): Long {
    var size: Long = 0
    if (dir.exists()) {
      val files = dir.listFiles()

      if (files != null) {
        for (i in files.indices) {
          size +=
                  if (files[i].isDirectory) {
                    getDirSize(files[i])
                  } else {
                    files[i].length()
                  }
        }
      }
    }
    return size
  }

  private val broadcastReceiver =
          object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
              Log.d(
                      "RnScheduleClearCache",
                      "Received cleanup intent at ${System.currentTimeMillis()}"
              )
              clearCache()
              return
            }
          }

  private val cleanupIntent = Intent(reactApplicationContext, broadcastReceiver::class.java)
  private val pendingCleanupIntent: PendingIntent =
          PendingIntent.getBroadcast(
                  reactApplicationContext,
                  1337,
                  cleanupIntent,
                  PendingIntent.FLAG_IMMUTABLE
          )

  private fun clearCache(): Boolean {
    try {
      val startingSpace = getDirSize(reactApplicationContext.cacheDir)

      val result = reactApplicationContext.cacheDir.deleteRecursively()

      if (result) {
        val endingSpace = getDirSize(reactApplicationContext.cacheDir)

        if (endingSpace != 0L) {
          Log.e(
                  "RnScheduleClearCache",
                  "Failed to clear cache: $endingSpace bytes / ${formatSize(endingSpace)} remaining in cache"
          )
        }

        val clearedSpace = startingSpace - endingSpace

        Log.d(
                "RnScheduleClearCache",
                "Cleared $clearedSpace bytes / ${formatSize(clearedSpace)} at ${System.currentTimeMillis()}"
        )
      }

      return true
    } catch (e: Exception) {
      Log.e("RnScheduleClearCache", "Failed to clear cache", e)
      return false
    }
  }

  @ReactMethod
  override fun clearCache(promise: Promise) {
    try {
      val result = reactApplicationContext.cacheDir.deleteRecursively()

      if (result) {
        val endingSpace = getDirSize(reactApplicationContext.cacheDir)

        if (endingSpace != 0L) {
          promise.reject(
                  "ERR_FAILED_TO_CLEAR_CACHE",
                  "$endingSpace bytes / ${formatSize(endingSpace)} remaining in cache"
          )
          return
        }
      }

      promise.resolve(result)
      return
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
      return
    }
  }

  @ReactMethod
  override fun getCacheSize(promise: Promise) {
    try {
      val size = getDirSize(reactApplicationContext.cacheDir)

      Log.d("RnScheduleClearCache", "Cache size: $size")

      promise.resolve(formatSize(size))
      return
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
      return
    }
  }

  @ReactMethod
  override fun scheduleClearCache(promise: Promise) {
    try {
      // alarm fires at 03:00 AM
      val calendar =
              Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 3)
              }

      if (alarmManager !== null) {
        // run clear cache every 7 days
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingCleanupIntent
        )

        // debug run every 30 seconds
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                System.currentTimeMillis(),
                30000,
                pendingCleanupIntent
        )

        Log.d("RnScheduleClearCache", "Scheduled cache cleanup starting at ${calendar.time}")
        promise.resolve(true)
        return
      }

      promise.resolve(false)
      return
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
      return
    }
  }

  override fun checkNextScheduledClearCache(promise: Promise) {
    try {
      if (alarmManager === null) {
        promise.reject("ERR_NO_ALARM_MANAGER", "AlarmManager is null")
        return
      }

      if (alarmManager.nextAlarmClock === null) {
        promise.reject("ERR_NO_NEXT_ALARM", "No next alarm")
        return
      }

      alarmManager.nextAlarmClock.let { alarmClockInfo ->
        Log.d("RnScheduleClearCache", "Next alarm at ${alarmClockInfo.triggerTime}")
        promise.resolve(alarmClockInfo.triggerTime)
        return
      }
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
      return
    }
  }

  companion object {
    const val NAME = "RnScheduleClearCache"
  }
}
