import { useEffect, useState } from 'react';
import { StyleSheet, View, Text, Pressable } from 'react-native';
import {
  getCacheSize,
  clearCache,
  scheduleClearCache,
  getTimeUntilNext,
} from 'rn-schedule-clear-cache';

export default function App() {
  const [cacheSize, setCacheSize] = useState<string | null>(null);

  useEffect(() => {
    getCacheSize().then(setCacheSize).catch(console.error);
  }, []);

  const updateCacheSize = async () => {
    return getCacheSize().then(setCacheSize).catch(console.error);
  };

  const clear = async () => {
    return await clearCache().then(() => {
      getCacheSize().then(setCacheSize).catch(console.error);
    });
  };

  const check = async () => {
    await getTimeUntilNext()
      .then((time) => {
        console.log(time);
      })
      .catch(console.error);
  };

  return (
    <View style={styles.container}>
      <Text>Size: {cacheSize}</Text>
      <Pressable
        style={{
          backgroundColor: 'green',
          padding: 10,
          width: '40%',
        }}
        onPress={updateCacheSize}
      >
        <Text
          style={{
            textAlign: 'center',
          }}
        >
          Update
        </Text>
      </Pressable>
      <Pressable
        style={{
          backgroundColor: 'green',
          padding: 10,
          width: '40%',
        }}
        onPress={check}
      >
        <Text
          style={{
            textAlign: 'center',
          }}
        >
          Check next scheduled clear cache
        </Text>
      </Pressable>
      <Pressable
        style={{
          backgroundColor: 'red',
          padding: 10,
          width: '40%',
        }}
        onPress={clear}
      >
        <Text
          style={{
            textAlign: 'center',
          }}
        >
          Clear cache
        </Text>
      </Pressable>
      <Pressable
        style={{
          backgroundColor: 'red',
          padding: 10,
          width: '40%',
        }}
        onPress={scheduleClearCache}
      >
        <Text
          style={{
            textAlign: 'center',
          }}
        >
          Schedule clear cache
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    rowGap: 20,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
