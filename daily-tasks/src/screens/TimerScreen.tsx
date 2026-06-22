import React, { useState, useCallback, useRef, useEffect } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
  StatusBar, Animated, Modal, Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import * as Notifications from 'expo-notifications';
import { RootStackParamList } from '../../App';
import { Task, getTasks, addTimeLog } from '../db/database';
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
  timerBg:   '#1e293b',
  accent:    '#38bdf8',
};

const PRESETS = [5, 10, 15, 25, 30];
type TimerMode = 'stopwatch' | 'timer';
type ScreenState = 'list' | 'running' | 'done';
type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Timer'> };

function formatTime(sec: number): string {
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  const s = sec % 60;
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

async function setupChannel() {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('timer_silent', {
      name: 'タイマー通知',
      importance: Notifications.AndroidImportance.LOW,
      sound: null,
      vibrationPattern: null,
    });
  }
}

export default function TimerScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [screenState, setScreenState] = useState<ScreenState>('list');
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [showModeModal, setShowModeModal] = useState(false);
  const [mode, setMode] = useState<TimerMode>('stopwatch');
  const [timerMinutes, setTimerMinutes] = useState(25);
  const [elapsed, setElapsed] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [startedAt, setStartedAt] = useState('');
  const [finalDuration, setFinalDuration] = useState(0);

  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const baseTimeRef = useRef<number>(0);
  const pausedElapsedRef = useRef<number>(0);
  const tickRef = useRef(0);
  const notifIdRef = useRef<string | null>(null);
  const completionNotifIdRef = useRef<string | null>(null);
  // Refs so callbacks can read latest values without stale closures
  const elapsedRef = useRef(0);
  const isPausedRef = useRef(false);
  const modeRef = useRef<TimerMode>('stopwatch');
  const selectedTaskRef = useRef<Task | null>(null);
  const timerDurSecRef = useRef(25 * 60);
  const startedAtRef = useRef('');

  const doneAnim = useRef(new Animated.Value(0)).current;

  const load = useCallback(async () => {
    const ts = await getTasks(db);
    setTasks(ts);
  }, [db]);

  useFocusEffect(useCallback(() => {
    load();
    setupChannel();
  }, [load]));

  useEffect(() => {
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      cancelTimerNotif();
    };
  }, []);

  // Keep refs in sync
  useEffect(() => { elapsedRef.current = elapsed; }, [elapsed]);
  useEffect(() => { isPausedRef.current = isPaused; }, [isPaused]);
  useEffect(() => { modeRef.current = mode; }, [mode]);
  useEffect(() => { selectedTaskRef.current = selectedTask; }, [selectedTask]);
  useEffect(() => { timerDurSecRef.current = timerMinutes * 60; }, [timerMinutes]);

  const timerDurationSec = timerMinutes * 60;
  const remaining = Math.max(timerDurationSec - elapsed, 0);
  const displaySec = mode === 'stopwatch' ? elapsed : remaining;
  const progress = mode === 'timer' ? Math.min(elapsed / timerDurationSec, 1) : 0;

  const showLiveNotif = async (elapsedSec: number, paused: boolean) => {
    if (notifIdRef.current) {
      try { await Notifications.dismissNotificationAsync(notifIdRef.current); } catch {}
    }
    const task = selectedTaskRef.current;
    const timerMode = modeRef.current;
    const durSec = timerDurSecRef.current;
    const dispSec = timerMode === 'stopwatch' ? elapsedSec : Math.max(durSec - elapsedSec, 0);
    const timeStr = formatTime(dispSec);

    let body: string;
    if (paused) {
      body = `⏸ 一時停止中  ${timeStr}`;
    } else if (timerMode === 'stopwatch') {
      body = `⏱️ 経過  ${timeStr}`;
    } else {
      body = `⏳ 残り  ${timeStr}`;
    }

    try {
      const id = await Notifications.scheduleNotificationAsync({
        content: {
          title: `${task?.icon ?? '⏱️'}  ${task?.title ?? 'タイマー'}`,
          body,
          sound: false,
          android: {
            channelId: 'timer_silent',
            ongoing: !paused,
            color: '#4a5569',
          } as any,
        },
        trigger: null as any,
      });
      notifIdRef.current = id;
    } catch {}
  };

  const cancelTimerNotif = async () => {
    if (notifIdRef.current) {
      try { await Notifications.dismissNotificationAsync(notifIdRef.current); } catch {}
      notifIdRef.current = null;
    }
    if (completionNotifIdRef.current) {
      try { await Notifications.cancelScheduledNotificationAsync(completionNotifIdRef.current); } catch {}
      completionNotifIdRef.current = null;
    }
  };

  const buildInterval = (sa: string) => {
    tickRef.current = 0;
    return setInterval(() => {
      const newElapsed = Math.floor((Date.now() - baseTimeRef.current) / 1000) + pausedElapsedRef.current;
      setElapsed(newElapsed);
      elapsedRef.current = newElapsed;
      tickRef.current += 1;
      // update notification every ~3 seconds
      if (tickRef.current % 15 === 0) {
        showLiveNotif(newElapsed, false);
      }
      if (modeRef.current === 'timer' && newElapsed >= timerDurSecRef.current) {
        stopTimer(newElapsed, sa);
      }
    }, 200);
  };

  const startTimer = async () => {
    const { status } = await Notifications.requestPermissionsAsync();
    const now = new Date().toISOString();
    setStartedAt(now);
    startedAtRef.current = now;
    setElapsed(0);
    pausedElapsedRef.current = 0;
    baseTimeRef.current = Date.now();
    setIsPaused(false);
    setScreenState('running');

    await showLiveNotif(0, false);

    // For timer mode: schedule a completion notification
    if (mode === 'timer' && status === 'granted') {
      try {
        const cid = await Notifications.scheduleNotificationAsync({
          content: {
            title: `${selectedTask?.icon ?? '⏱️'}  ${selectedTask?.title ?? 'タイマー'}`,
            body: '⏰ タイマー終了！',
            sound: true,
            android: { channelId: 'full' } as any,
          },
          trigger: { seconds: timerMinutes * 60, repeats: false } as any,
        });
        completionNotifIdRef.current = cid;
      } catch {}
    }

    intervalRef.current = buildInterval(now);
  };

  const pauseResume = async () => {
    if (isPaused) {
      baseTimeRef.current = Date.now();
      intervalRef.current = buildInterval(startedAtRef.current);
      setIsPaused(false);
      isPausedRef.current = false;
      await showLiveNotif(elapsedRef.current, false);
    } else {
      if (intervalRef.current) clearInterval(intervalRef.current);
      pausedElapsedRef.current = elapsedRef.current;
      setIsPaused(true);
      isPausedRef.current = true;
      await showLiveNotif(elapsedRef.current, true);
    }
  };

  const stopTimer = async (durationSec?: number, sa?: string) => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    setIsPaused(false);
    const dur = durationSec ?? elapsedRef.current;
    const sat = sa ?? startedAtRef.current;
    setFinalDuration(dur);
    if (dur > 0 && selectedTaskRef.current) {
      await addTimeLog(db, selectedTaskRef.current.id, dur, modeRef.current, sat);
    }
    await cancelTimerNotif();
    doneAnim.setValue(0);
    Animated.spring(doneAnim, { toValue: 1, useNativeDriver: true, tension: 120, friction: 8 }).start();
    setScreenState('done');
    setTimeout(() => {
      setScreenState('list');
      setElapsed(0);
      setSelectedTask(null);
    }, 3000);
  };

  const handleTaskPress = (task: Task) => {
    setSelectedTask(task);
    selectedTaskRef.current = task;
    setShowModeModal(true);
  };

  const handleStart = () => {
    setShowModeModal(false);
    startTimer();
  };

  if (screenState === 'running' || screenState === 'done') {
    const isDone = screenState === 'done';
    return (
      <View style={s.timerFull}>
        <StatusBar barStyle="light-content" backgroundColor={C.timerBg} />
        <SafeAreaView style={{ flex: 1 }} edges={['top', 'bottom']}>
          <View style={s.timerInner}>

            <View style={s.timerTaskRow}>
              <Text style={s.timerTaskIcon}>{selectedTask?.icon || '⏱️'}</Text>
              <Text style={s.timerTaskName} numberOfLines={1}>{selectedTask?.title}</Text>
            </View>

            <View style={s.modeChip}>
              <Text style={s.modeChipText}>
                {mode === 'stopwatch' ? 'ストップウォッチ' : `タイマー ${timerMinutes}分`}
              </Text>
            </View>

            {isDone ? (
              <Animated.View style={{
                transform: [{ scale: doneAnim.interpolate({ inputRange: [0, 1], outputRange: [0.6, 1] }) }],
                opacity: doneAnim, alignItems: 'center',
              }}>
                <Text style={s.timerDoneLabel}>記録しました</Text>
                <Text style={s.timerDisplay}>{formatTime(finalDuration)}</Text>
                <Text style={s.timerDoneCheck}>✓</Text>
              </Animated.View>
            ) : (
              <View style={s.timerCenter}>
                <Text style={[s.timerDisplay, isPaused && s.timerDisplayPaused]}>
                  {formatTime(displaySec)}
                </Text>
                {mode === 'timer' && (
                  <View style={s.timerProgressBg}>
                    <View style={[s.timerProgressFill, { width: `${progress * 100}%` as any }]} />
                  </View>
                )}
              </View>
            )}

            {!isDone && (
              <View style={s.timerControls}>
                <TouchableOpacity
                  style={[s.controlBtn, isPaused ? s.controlBtnResume : s.controlBtnPause]}
                  onPress={pauseResume}
                >
                  <Text style={s.controlBtnIcon}>{isPaused ? '▶' : '⏸'}</Text>
                  <Text style={s.controlBtnText}>{isPaused ? '再開' : '一時停止'}</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[s.controlBtn, s.controlBtnStop]} onPress={() => stopTimer()}>
                  <Text style={s.controlBtnIcon}>⏹</Text>
                  <Text style={s.controlBtnText}>完全停止</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        </SafeAreaView>
      </View>
    );
  }

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor={C.header} />

      <View style={s.headerCard}>
        <Text style={s.headerTitle}>タイマー</Text>
        <Text style={s.headerSub}>タスクを選んで計測開始</Text>
      </View>

      <FlatList
        data={tasks}
        keyExtractor={(item) => String(item.id)}
        style={s.list}
        contentContainerStyle={{ padding: 16, gap: 10 }}
        ListEmptyComponent={
          <View style={s.empty}>
            <Text style={s.emptyText}>タスクがありません</Text>
          </View>
        }
        renderItem={({ item }) => (
          <TouchableOpacity style={s.taskCard} onPress={() => handleTaskPress(item)} activeOpacity={0.75}>
            <Text style={s.taskIcon}>{item.icon || '✅'}</Text>
            <Text style={s.taskTitle} numberOfLines={1}>{item.title}</Text>
            <Text style={s.taskArrow}>›</Text>
          </TouchableOpacity>
        )}
        ListFooterComponent={<View style={{ height: 8 }} />}
      />

      <TabBar current="Timer" navigation={navigation} />

      <Modal visible={showModeModal} transparent animationType="slide" onRequestClose={() => setShowModeModal(false)}>
        <TouchableOpacity style={s.modalBg} activeOpacity={1} onPress={() => setShowModeModal(false)}>
          <TouchableOpacity activeOpacity={1} onPress={() => {}}>
            <View style={s.modeSheet}>
              <View style={s.sheetHandle} />
              <View style={s.modeTaskRow}>
                <Text style={s.modeTaskIcon}>{selectedTask?.icon || '⏱️'}</Text>
                <Text style={s.modeTaskName}>{selectedTask?.title}</Text>
              </View>

              <View style={s.modeRow}>
                <TouchableOpacity
                  style={[s.modeBtn, mode === 'stopwatch' && s.modeBtnActive]}
                  onPress={() => setMode('stopwatch')}
                >
                  <Text style={s.modeBtnIcon}>⏱️</Text>
                  <Text style={[s.modeBtnLabel, mode === 'stopwatch' && s.modeBtnLabelActive]}>ストップウォッチ</Text>
                  <Text style={[s.modeBtnDesc, mode === 'stopwatch' && s.modeBtnDescActive]}>やった時間を記録</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[s.modeBtn, mode === 'timer' && s.modeBtnActive]}
                  onPress={() => setMode('timer')}
                >
                  <Text style={s.modeBtnIcon}>⏳</Text>
                  <Text style={[s.modeBtnLabel, mode === 'timer' && s.modeBtnLabelActive]}>タイマー</Text>
                  <Text style={[s.modeBtnDesc, mode === 'timer' && s.modeBtnDescActive]}>この時間内でやる</Text>
                </TouchableOpacity>
              </View>

              {mode === 'timer' && (
                <View style={s.presetsSection}>
                  <Text style={s.presetsLabel}>時間を選ぶ</Text>
                  <View style={s.presets}>
                    {PRESETS.map((min) => (
                      <TouchableOpacity
                        key={min}
                        style={[s.presetChip, timerMinutes === min && s.presetChipActive]}
                        onPress={() => setTimerMinutes(min)}
                      >
                        <Text style={[s.presetChipText, timerMinutes === min && s.presetChipTextActive]}>
                          {min}分
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                  <View style={s.customRow}>
                    <TouchableOpacity style={s.customBtn} onPress={() => setTimerMinutes(m => Math.max(1, m - 1))}>
                      <Text style={s.customBtnText}>－</Text>
                    </TouchableOpacity>
                    <Text style={s.customNum}>{timerMinutes}分</Text>
                    <TouchableOpacity style={s.customBtn} onPress={() => setTimerMinutes(m => m + 1)}>
                      <Text style={s.customBtnText}>＋</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              )}

              <TouchableOpacity style={s.startBtn} onPress={handleStart}>
                <Text style={s.startBtnText}>▶ スタート</Text>
              </TouchableOpacity>
            </View>
          </TouchableOpacity>
        </TouchableOpacity>
      </Modal>
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: C.header },
  headerCard: { backgroundColor: C.header, paddingHorizontal: 20, paddingTop: 12, paddingBottom: 20, gap: 4 },
  headerTitle: { color: '#ffffff', fontSize: 20, fontWeight: '700' },
  headerSub: { color: 'rgba(255,255,255,0.5)', fontSize: 12 },

  list: { flex: 1, backgroundColor: C.body },
  taskCard: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    backgroundColor: C.card, borderRadius: 14,
    paddingHorizontal: 16, paddingVertical: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4,
    elevation: 2,
  },
  taskIcon: { fontSize: 22 },
  taskTitle: { flex: 1, color: C.onDark, fontSize: 15, fontWeight: '600' },
  taskArrow: { color: C.muted, fontSize: 22, fontWeight: '300' },
  empty: { paddingVertical: 60, alignItems: 'center' },
  emptyText: { color: C.muted, fontSize: 14, fontWeight: '600' },

  timerFull: { flex: 1, backgroundColor: C.timerBg },
  timerInner: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 24, paddingHorizontal: 32 },
  timerTaskRow: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  timerTaskIcon: { fontSize: 26 },
  timerTaskName: { color: 'rgba(255,255,255,0.7)', fontSize: 15, fontWeight: '600', maxWidth: 240 },
  modeChip: { backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: 20, paddingHorizontal: 14, paddingVertical: 5 },
  modeChipText: { color: 'rgba(255,255,255,0.6)', fontSize: 12, fontWeight: '600' },
  timerCenter: { alignItems: 'center', gap: 20, width: '100%' },
  timerDisplay: { color: '#ffffff', fontSize: 80, fontWeight: '200', letterSpacing: -2, fontVariant: ['tabular-nums'] },
  timerDisplayPaused: { color: 'rgba(255,255,255,0.35)' },
  timerProgressBg: { width: '80%', height: 4, backgroundColor: 'rgba(255,255,255,0.15)', borderRadius: 2, overflow: 'hidden' },
  timerProgressFill: { height: '100%', backgroundColor: C.accent },

  timerControls: { flexDirection: 'row', gap: 12, marginTop: 8 },
  controlBtn: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    gap: 8, borderRadius: 16, paddingVertical: 16,
  },
  controlBtnPause: { backgroundColor: 'rgba(255,255,255,0.12)', borderWidth: 1, borderColor: 'rgba(255,255,255,0.2)' },
  controlBtnResume: { backgroundColor: 'rgba(74,222,128,0.18)', borderWidth: 1, borderColor: 'rgba(74,222,128,0.4)' },
  controlBtnStop: { backgroundColor: 'rgba(239,68,68,0.18)', borderWidth: 1, borderColor: 'rgba(239,68,68,0.4)' },
  controlBtnIcon: { color: '#ffffff', fontSize: 18 },
  controlBtnText: { color: '#ffffff', fontSize: 14, fontWeight: '700' },

  timerDoneLabel: { color: 'rgba(255,255,255,0.5)', fontSize: 14, fontWeight: '600', marginBottom: 8 },
  timerDoneCheck: { color: '#4ade80', fontSize: 48, marginTop: 8 },

  modalBg: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  modeSheet: { backgroundColor: C.card, borderTopLeftRadius: 28, borderTopRightRadius: 28, padding: 24, paddingTop: 12, gap: 16 },
  sheetHandle: { width: 40, height: 4, backgroundColor: C.border, borderRadius: 2, alignSelf: 'center', marginBottom: 8 },
  modeTaskRow: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  modeTaskIcon: { fontSize: 24 },
  modeTaskName: { color: C.onDark, fontSize: 16, fontWeight: '700', flex: 1 },
  modeRow: { flexDirection: 'row', gap: 10 },
  modeBtn: {
    flex: 1, borderRadius: 16, borderWidth: 1.5, borderColor: C.border,
    padding: 16, alignItems: 'center', gap: 6,
  },
  modeBtnActive: { borderColor: C.primary, backgroundColor: '#f1f5f9' },
  modeBtnIcon: { fontSize: 28 },
  modeBtnLabel: { color: C.stone, fontSize: 13, fontWeight: '700' },
  modeBtnLabelActive: { color: C.primary },
  modeBtnDesc: { color: C.muted, fontSize: 11, textAlign: 'center' },
  modeBtnDescActive: { color: C.stone },
  presetsSection: { gap: 10 },
  presetsLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  presets: { flexDirection: 'row', gap: 8 },
  presetChip: { flex: 1, borderWidth: 1, borderColor: C.border, borderRadius: 20, paddingVertical: 8, alignItems: 'center' },
  presetChipActive: { backgroundColor: C.primary, borderColor: C.primary },
  presetChipText: { color: C.muted, fontSize: 13, fontWeight: '700' },
  presetChipTextActive: { color: '#ffffff' },
  customRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 20 },
  customBtn: { width: 36, height: 36, borderRadius: 18, backgroundColor: C.body, borderWidth: 1, borderColor: C.border, alignItems: 'center', justifyContent: 'center' },
  customBtnText: { color: C.primary, fontSize: 20, fontWeight: '700' },
  customNum: { color: C.onDark, fontSize: 18, fontWeight: '700', minWidth: 60, textAlign: 'center' },
  startBtn: { backgroundColor: C.primary, borderRadius: 14, paddingVertical: 16, alignItems: 'center' },
  startBtnText: { color: '#ffffff', fontSize: 16, fontWeight: '700' },
});
