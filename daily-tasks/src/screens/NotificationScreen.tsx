import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Platform, Alert, StatusBar } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import * as Notifications from 'expo-notifications';
import { RootStackParamList } from '../../App';
import { NotificationSetting, getNotificationSettings, addNotificationSetting, deleteNotificationSetting } from '../db/database';
import TabBar from '../components/TabBar';

const C = {
  header:    '#4a5569',
  body:      '#f0f2f5',
  card:      '#ffffff',
  border:    '#e2e8f0',
  primary:   '#4a5569',
  onPrimary: '#ffffff',
  onDark:    '#2d3748',
  muted:     '#94a3b8',
  stone:     '#64748b',
  error:     '#e52020',
};

type NotifType = 'full' | 'silent';
type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Notifications'> };

Notifications.setNotificationHandler({
  handleNotification: async () => ({ shouldShowAlert: true, shouldPlaySound: true, shouldSetBadge: false }),
});

async function scheduleNotification(time: string, type: NotifType): Promise<string | null> {
  const [h, m] = time.split(':').map(Number);
  try {
    const identifier = await Notifications.scheduleNotificationAsync({
      content: {
        title: '毎日タスク',
        body: '今日のタスクを確認しましょう！',
        sound: type === 'full',
        android: { channelId: type === 'full' ? 'full' : 'silent' } as any,
      },
      trigger: { hour: h, minute: m, repeats: true } as any,
    });
    return identifier;
  } catch {
    return null;
  }
}

export default function NotificationScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const [settings, setSettings] = useState<NotificationSetting[]>([]);
  const [showPicker, setShowPicker] = useState(false);
  const [pickerTime, setPickerTime] = useState(new Date());
  const [newType, setNewType] = useState<NotifType>('full');

  const load = useCallback(async () => {
    const data = await getNotificationSettings(db);
    setSettings(data);
  }, [db]);

  useFocusEffect(useCallback(() => {
    load();
    if (Platform.OS === 'android') {
      Notifications.setNotificationChannelAsync('full', {
        name: '通常通知', importance: Notifications.AndroidImportance.HIGH, sound: 'default',
      });
      Notifications.setNotificationChannelAsync('silent', {
        name: 'サイレント通知', importance: Notifications.AndroidImportance.LOW, sound: null,
      });
    }
  }, [load]));

  const handleAdd = async (date: Date) => {
    setShowPicker(false);
    const { status } = await Notifications.requestPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('通知の許可が必要です', '端末の設定から通知を許可してください。');
      return;
    }
    const h = date.getHours();
    const m = date.getMinutes();
    const time = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
    const identifier = await scheduleNotification(time, newType);
    await addNotificationSetting(db, time, newType, identifier);
    load();
  };

  const handleDelete = (item: NotificationSetting) => {
    Alert.alert('削除', `${item.time} の通知を削除しますか？`, [
      { text: 'キャンセル', style: 'cancel' },
      {
        text: '削除', style: 'destructive',
        onPress: async () => {
          const id = await deleteNotificationSetting(db, item.id);
          if (id) await Notifications.cancelScheduledNotificationAsync(id);
          load();
        },
      },
    ]);
  };

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor={C.header} />

      <View style={s.headerCard}>
        <Text style={s.headerTitle}>通知設定</Text>
      </View>

      <FlatList
        data={settings}
        keyExtractor={(item) => String(item.id)}
        style={s.list}
        contentContainerStyle={{ padding: 16, gap: 10 }}
        ListHeaderComponent={
          <View style={s.addSection}>
            <View style={s.typeCard}>
              <Text style={s.typeLabel}>通知タイプ</Text>
              <View style={s.typeRow}>
                <TouchableOpacity
                  style={[s.typeChip, newType === 'full' && s.typeChipActive]}
                  onPress={() => setNewType('full')}
                >
                  <Text style={[s.typeChipText, newType === 'full' && s.typeChipTextActive]}>🔔 通常</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[s.typeChip, newType === 'silent' && s.typeChipActive]}
                  onPress={() => setNewType('silent')}
                >
                  <Text style={[s.typeChipText, newType === 'silent' && s.typeChipTextActive]}>🔕 サイレント</Text>
                </TouchableOpacity>
              </View>
            </View>
            <TouchableOpacity style={s.addBtn} onPress={() => setShowPicker(true)}>
              <Text style={s.addBtnText}>＋ 通知時間を追加</Text>
            </TouchableOpacity>
            {settings.length > 0 && <Text style={s.sectionLabel}>設定済みの通知</Text>}
          </View>
        }
        ListEmptyComponent={
          <View style={s.empty}>
            <Text style={s.emptyText}>通知が設定されていません</Text>
          </View>
        }
        renderItem={({ item }) => (
          <View style={s.settingCard}>
            <Text style={s.settingTime}>{item.time}</Text>
            <Text style={s.settingType}>
              {item.notification_type === 'full' ? '🔔 通常' : '🔕 サイレント'}
            </Text>
            <TouchableOpacity style={s.deleteBtn} onPress={() => handleDelete(item)} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
              <Text style={s.deleteBtnText}>🗑️</Text>
            </TouchableOpacity>
          </View>
        )}
        ListFooterComponent={<View style={{ height: 8 }} />}
      />

      <TabBar current="Notifications" navigation={navigation} />

      {showPicker && (
        <DateTimePicker
          value={pickerTime}
          mode="time"
          display={Platform.OS === 'ios' ? 'spinner' : 'default'}
          onChange={(_, date) => {
            if (Platform.OS === 'android') {
              if (date) handleAdd(date);
              else setShowPicker(false);
            } else {
              if (date) setPickerTime(date);
            }
          }}
        />
      )}
      {Platform.OS === 'ios' && showPicker && (
        <View style={s.iosRow}>
          <TouchableOpacity style={s.iosCancelBtn} onPress={() => setShowPicker(false)}>
            <Text style={s.iosCancelText}>キャンセル</Text>
          </TouchableOpacity>
          <TouchableOpacity style={s.iosConfirmBtn} onPress={() => handleAdd(pickerTime)}>
            <Text style={s.iosConfirmText}>追加</Text>
          </TouchableOpacity>
        </View>
      )}
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: C.header },
  headerCard: { backgroundColor: C.header, paddingHorizontal: 20, paddingTop: 12, paddingBottom: 20 },
  headerTitle: { color: '#ffffff', fontSize: 20, fontWeight: '700' },

  list: { flex: 1, backgroundColor: C.body },
  addSection: { gap: 10, marginBottom: 4 },
  typeCard: { backgroundColor: C.card, borderRadius: 12, padding: 14, gap: 10, elevation: 2, shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4 },
  typeLabel: { color: C.stone, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  typeRow: { flexDirection: 'row', gap: 8 },
  typeChip: { flex: 1, borderWidth: 1, borderColor: C.border, borderRadius: 20, paddingVertical: 10, alignItems: 'center' },
  typeChipActive: { backgroundColor: C.primary, borderColor: C.primary },
  typeChipText: { color: C.muted, fontSize: 13, fontWeight: '700' },
  typeChipTextActive: { color: C.onPrimary },

  addBtn: { backgroundColor: C.primary, borderRadius: 12, paddingVertical: 14, alignItems: 'center', elevation: 2 },
  addBtnText: { color: C.onPrimary, fontSize: 14, fontWeight: '700' },
  sectionLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },

  settingCard: { backgroundColor: C.card, borderRadius: 12, padding: 16, flexDirection: 'row', alignItems: 'center', gap: 12, elevation: 2, shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4 },
  settingTime: { fontSize: 22, fontWeight: '700', color: C.onDark, flex: 1 },
  settingType: { color: C.stone, fontSize: 13, fontWeight: '600' },
  deleteBtn: { width: 32, height: 32, borderRadius: 16, backgroundColor: '#fee2e2', alignItems: 'center', justifyContent: 'center' },
  deleteBtnText: { fontSize: 16 },

  empty: { paddingVertical: 40, alignItems: 'center' },
  emptyText: { color: C.muted, fontSize: 14, fontWeight: '600' },

  iosRow: { flexDirection: 'row', backgroundColor: C.card, borderTopWidth: 1, borderTopColor: C.border, padding: 12, gap: 12 },
  iosCancelBtn: { flex: 1, borderWidth: 1, borderColor: C.border, borderRadius: 8, paddingVertical: 12, alignItems: 'center' },
  iosCancelText: { color: C.stone, fontSize: 14, fontWeight: '700' },
  iosConfirmBtn: { flex: 1, backgroundColor: C.primary, borderRadius: 8, paddingVertical: 12, alignItems: 'center' },
  iosConfirmText: { color: C.onPrimary, fontSize: 14, fontWeight: '700' },
});
