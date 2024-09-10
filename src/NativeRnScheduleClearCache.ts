import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  clearCache(): Promise<void>;
  scheduleClearCache(): Promise<void>;
  getCacheSize(): Promise<string>;
  checkNextScheduledClearCache(): Promise<number>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RnScheduleClearCache');
