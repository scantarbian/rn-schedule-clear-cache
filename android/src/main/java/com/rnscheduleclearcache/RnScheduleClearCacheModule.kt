package com.rnscheduleclearcache

import android.annotation.SuppressLint
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import java.util.Locale
import java.io.File
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
          size += if (files[i].isDirectory) {
            getDirSize(files[i])
          } else {
            files[i].length()
          }
        }
      }

    }
    return size
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  override fun clearCache(promise: Promise) {
    try {
      val startingSpace = getDirSize(reactApplicationContext.cacheDir)

      val result = reactApplicationContext.cacheDir.deleteRecursively()

      if (result) {
        val endingSpace = getDirSize(reactApplicationContext.cacheDir)

        if (endingSpace != 0L) {
          println("Cache not cleared: $endingSpace bytes remaining")
        }

        val spaceCleared = startingSpace - endingSpace

        println("Cache cleared: $spaceCleared bytes")
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

      println("Cache size: $size")

      promise.resolve(formatSize(size))
    }catch (e: Exception) {
      promise.reject("ERR_UNEXPECTED_EXCEPTION", e)
    }
  }

  companion object {
    const val NAME = "RnScheduleClearCache"
  }
}
