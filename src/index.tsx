import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-schedule-clear-cache' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const RnScheduleClearCacheModule = isTurboModuleEnabled
  ? require('./NativeRnScheduleClearCache').default
  : NativeModules.RnScheduleClearCache;

const RnScheduleClearCache = RnScheduleClearCacheModule
  ? RnScheduleClearCacheModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function clearCache(): Promise<void> {
  try {
    return RnScheduleClearCache.clearCache();
  } catch (e) {
    return Promise.reject(e);
  }
}

/**
 * Schedules the clearing of cache every 7 days at 3:00 AM.
 */
export function scheduleClearCache(): Promise<void> {
  try {
    return RnScheduleClearCache.scheduleClearCache();
  } catch (e) {
    return Promise.reject(e);
  }
}

export function getCacheSize(): Promise<string> {
  try {
    return RnScheduleClearCache.getCacheSize();
  } catch (e) {
    return Promise.reject(e);
  }
}

export function getTimeUntilNext(): Promise<number> {
  try {
    return RnScheduleClearCache.getTimeUntilNext();
  } catch (e) {
    return Promise.reject(e);
  }
}

export function test(): Promise<string> {
  try {
    console.log(RnScheduleClearCache);
    return RnScheduleClearCache.test();
  } catch (e) {
    return Promise.reject(e);
  }
}
