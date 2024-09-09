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
              clearCache()
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

  private fun clearCache() {
    try {
      val result = reactApplicationContext.cacheDir.deleteRecursively()

      if (result) {
        val endingSpace = getDirSize(reactApplicationContext.cacheDir)

        if (endingSpace != 0L) {
          Log.e(
                  "RnScheduleClearCache",
                  "Failed to clear cache: $endingSpace bytes / ${formatSize(endingSpace)} remaining in cache"
          )
        }
      }
    } catch (e: Exception) {
      Log.e("RnScheduleClearCache", "Failed to clear cache", e)
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
        }
      }

      promise.resolve(result)
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
    }
  }

  @ReactMethod
  override fun getCacheSize(promise: Promise) {
    try {
      val size = getDirSize(reactApplicationContext.cacheDir)

      Log.d("RnScheduleClearCache", "Cache size: $size")

      promise.resolve(formatSize(size))
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
    }
  }

  @ReactMethod
  override fun scheduleClearCache(promise: Promise) {
    try {
      val alarmManager =
              reactApplicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

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
      }

      promise.resolve(true)
    } catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
    }
  }

  companion object {
    const val NAME = "RnScheduleClearCache"
  }
}
