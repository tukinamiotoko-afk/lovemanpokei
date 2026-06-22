import React, { useState, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, FlatList, StatusBar } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';
import { Task, getToday, getTasks, getCompletionsForMonth } from '../db/database';
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
};

const WEEKDAYS = ['日', '月', '火', '水', '木', '金', '土'];

type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Calendar'> };

export default function CalendarScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [completedByTask, setCompletedByTask] = useState<Record<number, Set<string>>>({});

  const load = useCallback(async () => {
    const ts = await getTasks(db);
    const cs = await getCompletionsForMonth(db, year, month);
    const map: Record<number, Set<string>> = {};
    for (const t of ts) map[t.id] = new Set();
    for (const c of cs) {
      if (!map[c.task_id]) map[c.task_id] = new Set();
      map[c.task_id].add(c.date);
    }
    setTasks(ts);
    setCompletedByTask(map);
  }, [db, year, month]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const todayStr = getToday();
  const firstDow = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: (number | null)[] = [
    ...Array(firstDow).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];
  while (cells.length % 7 !== 0) cells.push(null);

  const prevMonth = () => { if (month === 1) { setMonth(12); setYear(y => y - 1); } else setMonth(m => m - 1); };
  const nextMonth = () => { if (month === 12) { setMonth(1); setYear(y => y + 1); } else setMonth(m => m + 1); };

  const renderMiniCal = (task: Task) => {
    const done = completedByTask[task.id] ?? new Set<string>();
    const count = done.size;
    return (
      <View style={s.taskCard}>
        <View style={s.taskHeader}>
          <Text style={s.taskIcon}>{task.icon || '✅'}</Text>
          <Text style={s.taskTitle} numberOfLines={1}>{task.title}</Text>
          {count > 0 && (
            <View style={s.countBadge}>
              <Text style={s.countText}>{count}日</Text>
            </View>
          )}
        </View>
        <View style={s.miniWeekRow}>
          {WEEKDAYS.map((d, i) => (
            <Text key={d} style={[s.miniWeekLabel, i === 0 && s.sun, i === 6 && s.sat]}>{d}</Text>
          ))}
        </View>
        <View style={s.miniGrid}>
          {cells.map((day, i) => {
            if (!day) return <View key={`e-${i}`} style={s.miniCell} />;
            const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isCompleted = done.has(dateStr);
            const isToday = dateStr === todayStr;
            const isPast = dateStr < todayStr;
            const dow = i % 7;
            return (
              <View key={`d-${i}`} style={s.miniCell}>
                <View style={[
                  s.miniDot,
                  isCompleted && s.miniDotDone,
                  !isCompleted && isPast && s.miniDotPast,
                  isToday && !isCompleted && s.miniDotToday,
                ]}>
                  <Text style={[
                    s.miniNum,
                    dow === 0 && !isCompleted && s.sun,
                    dow === 6 && !isCompleted && s.sat,
                    isCompleted && s.miniNumDone,
                    !isCompleted && isPast && s.miniNumPast,
                    isToday && !isCompleted && s.miniNumToday,
                  ]}>
                    {day}
                  </Text>
                </View>
              </View>
            );
          })}
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor={C.header} />

      <View style={s.headerCard}>
        <View style={s.monthNav}>
          <TouchableOpacity onPress={prevMonth} style={s.navBtn}>
            <Text style={s.navBtnText}>‹</Text>
          </TouchableOpacity>
          <Text style={s.monthLabel}>{year}年{month}月</Text>
          <TouchableOpacity onPress={nextMonth} style={s.navBtn}>
            <Text style={s.navBtnText}>›</Text>
          </TouchableOpacity>
        </View>
      </View>

      {tasks.length === 0 ? (
        <View style={s.empty}>
          <Text style={s.emptyText}>タスクがありません</Text>
        </View>
      ) : (
        <FlatList
          data={tasks}
          keyExtractor={(item) => String(item.id)}
          style={s.body}
          contentContainerStyle={{ padding: 16, gap: 12 }}
          renderItem={({ item }) => renderMiniCal(item)}
          ListFooterComponent={<View style={{ height: 8 }} />}
        />
      )}

      <TabBar current="Calendar" navigation={navigation} />
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: C.header },
  headerCard: { backgroundColor: C.header, paddingHorizontal: 20, paddingTop: 12, paddingBottom: 20 },
  monthNav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  navBtn: { padding: 8 },
  navBtnText: { color: '#ffffff', fontSize: 28, fontWeight: '300' },
  monthLabel: { color: '#ffffff', fontSize: 18, fontWeight: '700' },

  body: { flex: 1, backgroundColor: C.body },
  empty: { flex: 1, backgroundColor: C.body, alignItems: 'center', justifyContent: 'center' },
  emptyText: { color: C.muted, fontSize: 14, fontWeight: '600' },

  taskCard: {
    backgroundColor: C.card, borderRadius: 16, padding: 14,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 6,
    elevation: 3,
  },
  taskHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 12 },
  taskIcon: { fontSize: 18 },
  taskTitle: { flex: 1, color: C.onDark, fontSize: 14, fontWeight: '600' },
  countBadge: { backgroundColor: C.primary, borderRadius: 10, paddingHorizontal: 8, paddingVertical: 3 },
  countText: { color: C.onPrimary, fontSize: 10, fontWeight: '700' },

  miniWeekRow: { flexDirection: 'row', marginBottom: 4 },
  miniWeekLabel: { flex: 1, textAlign: 'center', color: C.muted, fontSize: 10, fontWeight: '700', paddingVertical: 2 },

  miniGrid: { flexDirection: 'row', flexWrap: 'wrap' },
  miniCell: { width: '14.28%', alignItems: 'center', paddingVertical: 3 },
  miniDot: { width: 30, height: 30, borderRadius: 6, alignItems: 'center', justifyContent: 'center' },
  miniDotDone: { backgroundColor: C.primary },
  miniDotPast: { backgroundColor: '#eceff2' },
  miniDotToday: { borderWidth: 2, borderColor: C.primary },
  miniNum: { fontSize: 11, fontWeight: '500', color: C.onDark },
  miniNumDone: { color: '#ffffff', fontWeight: '700' },
  miniNumPast: { color: C.muted },
  miniNumToday: { color: C.primary, fontWeight: '700' },

  sun: { color: '#e53e3e' },
  sat: { color: '#3182ce' },
});
