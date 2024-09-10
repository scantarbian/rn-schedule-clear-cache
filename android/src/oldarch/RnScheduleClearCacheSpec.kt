package com.rnscheduleclearcache

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

abstract class RnScheduleClearCacheSpec internal constructor(context: ReactApplicationContext) :
        ReactContextBaseJavaModule(context) {

  abstract fun clearCache(promise: Promise)

  abstract fun getCacheSize(promise: Promise)

  abstract fun scheduleClearCache(promise: Promise)

  abstract fun getTimeUntilNext(promise: Promise)

  abstract fun test(promise: Promise)
}
