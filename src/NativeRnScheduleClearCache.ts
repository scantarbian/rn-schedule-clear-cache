import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  clearCache(): Promise<void>;
  scheduleClearCache(): Promise<void>;
  getCacheSize(): Promise<string>;
  getTimeUntilNext(): Promise<number>;
  test(): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RnScheduleClearCache');
