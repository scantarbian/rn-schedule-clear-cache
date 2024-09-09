import { useEffect, useState } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import { getCacheSize } from 'rn-schedule-clear-cache';

export default function App() {
  const [cacheSize, setCacheSize] = useState<string | null>(null);

  useEffect(() => {
    getCacheSize().then(setCacheSize).catch(console.error);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Size: {cacheSize}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
