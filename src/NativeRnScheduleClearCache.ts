import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  clearCache(): Promise<void>;
  scheduleClearCache(cron: string): Promise<void>;
  getCacheSize(): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RnScheduleClearCache');
