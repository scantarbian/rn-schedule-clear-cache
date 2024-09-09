
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNRnScheduleClearCacheSpec.h"

@interface RnScheduleClearCache : NSObject <NativeRnScheduleClearCacheSpec>
#else
#import <React/RCTBridgeModule.h>

@interface RnScheduleClearCache : NSObject <RCTBridgeModule>
#endif

@end
