import React, { useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity,
  StyleSheet, Platform, StatusBar,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import DateTimePicker from '@react-native-community/datetimepicker';
import { RootStackParamList } from '../../App';
import {
  Task, getToday, subtractDays, daysBetween,
  getTasks, getCompletionCountInRange, getFirstCompletionDate,
} from '../db/database';

const C = {
  dark:      '#000000',
  elevated:  '#1a1a1a',
  border:    '#5e5e5e',
  primary:   '#76b900',
  onPrimary: '#000000',
  onDark:    '#ffffff',
  muted:     'rgba(255,255,255,0.7)',
  stone:     '#898989',
  warning:   '#df6500',
};

type Period = '7日' | '30日' | '全期間' | '任意';
type Rate = { task: Task; completed: number; total: number; rate: number };
type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Stats'> };

function toDateString(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export default function StatsScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const today = getToday();

  const [period, setPeriod] = useState<Period>('7日');
  const [customStart, setCustomStart] = useState<Date>(() => { const d = new Date(); d.setDate(d.getDate() - 6); return d; });
  const [customEnd, setCustomEnd] = useState<Date>(new Date());
  const [showStartPicker, setShowStartPicker] = useState(false);
  const [showEndPicker, setShowEndPicker] = useState(false);
  const [rates, setRates] = useState<Rate[]>([]);

  const load = useCallback(async () => {
    const tasks = await getTasks(db);
    if (tasks.length === 0) { setRates([]); return; }

    const computed = await Promise.all(tasks.map(async (task) => {
      let startDate: string;
      let totalDays: number;

      if (period === '7日') {
        startDate = subtractDays(today, 6);
        totalDays = 7;
      } else if (period === '30日') {
        startDate = subtractDays(today, 29);
        totalDays = 30;
      } else if (period === '全期間') {
        const first = await getFirstCompletionDate(db, task.id);
        startDate = first ?? today;
        totalDays = daysBetween(startDate, today);
      } else {
        startDate = toDateString(customStart);
        const endStr = toDateString(customEnd);
        totalDays = daysBetween(startDate, endStr);
      }

      const endDate = period === '任意' ? toDateString(customEnd) : today;
      const completed = await getCompletionCountInRange(db, task.id, startDate, endDate);
      return { task, completed, total: Math.max(totalDays, 1), rate: completed / Math.max(totalDays, 1) };
    }));

    setRates(computed);
  }, [db, period, today, customStart, customEnd]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const periodLabel = (): string => {
    if (period === '7日') return '直近7日間';
    if (period === '30日') return '直近30日間';
    if (period === '全期間') return '全期間';
    const s = customStart;
    const e = customEnd;
    return `${s.getMonth() + 1}/${s.getDate()} 〜 ${e.getMonth() + 1}/${e.getDate()}`;
  };

  const barColor = (rate: number) => {
    if (rate >= 0.8) return C.primary;
    if (rate >= 0.5) return C.warning;
    return C.stone;
  };

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor={C.dark} />

      <View style={s.navBar}>
        <TouchableOpacity style={s.backBtn} onPress={() => navigation.goBack()}>
          <Text style={s.backText}>‹ 戻る</Text>
        </TouchableOpacity>
        <Text style={s.navTitle}>実行率</Text>
      </View>

      <View style={s.periodBar}>
        {(['7日', '30日', '全期間', '任意'] as Period[]).map((p) => (
          <TouchableOpacity
            key={p}
            style={[s.periodChip, period === p && s.periodChipActive]}
            onPress={() => { setPeriod(p); }}
          >
            <Text style={[s.periodChipText, period === p && s.periodChipTextActive]}>{p}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {period === '任意' && (
        <View style={s.customBar}>
          <Text style={s.customLabel}>期間：</Text>
          <TouchableOpacity style={s.dateBtn} onPress={() => setShowStartPicker(true)}>
            <Text style={s.dateBtnText}>{toDateString(customStart)}</Text>
          </TouchableOpacity>
          <Text style={s.customTilde}>〜</Text>
          <TouchableOpacity style={s.dateBtn} onPress={() => setShowEndPicker(true)}>
            <Text style={s.dateBtnText}>{toDateString(customEnd)}</Text>
          </TouchableOpacity>
          <TouchableOpacity style={s.applyBtn} onPress={load}>
            <Text style={s.applyBtnText}>適用</Text>
          </TouchableOpacity>
        </View>
      )}

      <View style={s.sectionBar}>
        <Text style={s.metaLabel}>EXECUTION RATE  {periodLabel()}</Text>
      </View>

      {rates.length === 0 ? (
        <View style={s.empty}>
          <Text style={s.emptyText}>タスクがありません</Text>
        </View>
      ) : (
        <FlatList
          data={rates}
          keyExtractor={(item) => String(item.task.id)}
          style={s.list}
          contentContainerStyle={{ paddingHorizontal: 12, paddingVertical: 8, gap: 6 }}
          renderItem={({ item }) => {
            const pct = Math.round(item.rate * 100);
            const bc = barColor(item.rate);
            return (
              <View style={s.rateCard}>
                <View style={s.rateHeader}>
                  <Text style={s.rateTitle} numberOfLines={1}>{item.task.title}</Text>
                  <View style={s.rateRight}>
                    <Text style={s.rateDays}>{item.completed} / {item.total}日</Text>
                    <View style={[s.rateBadge, { backgroundColor: bc }]}>
                      <Text style={s.ratePct}>{pct}%</Text>
                    </View>
                  </View>
                </View>
                <View style={s.barBg}>
                  <View style={[s.barFill, { width: `${Math.min(item.rate * 100, 100)}%` as any, backgroundColor: bc }]} />
                </View>
              </View>
            );
          }}
          ListFooterComponent={<View style={{ height: 8 }} />}
        />
      )}

      {showStartPicker && (
        <DateTimePicker
          value={customStart}
          mode="date"
          display={Platform.OS === 'ios' ? 'inline' : 'default'}
          maximumDate={customEnd}
          onChange={(_, date) => {
            setShowStartPicker(Platform.OS === 'ios');
            if (date) setCustomStart(date);
          }}
        />
      )}
      {showEndPicker && (
        <DateTimePicker
          value={customEnd}
          mode="date"
          display={Platform.OS === 'ios' ? 'inline' : 'default'}
          minimumDate={customStart}
          maximumDate={new Date()}
          onChange={(_, date) => {
            setShowEndPicker(Platform.OS === 'ios');
            if (date) setCustomEnd(date);
          }}
        />
      )}
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: C.dark },

  navBar: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: C.elevated, height: 52, paddingHorizontal: 12,
    borderBottomWidth: 1, borderBottomColor: C.border,
  },
  backBtn: { marginRight: 12 },
  backText: { color: C.primary, fontSize: 14, fontWeight: '700' },
  navTitle: { flex: 1, color: C.primary, fontSize: 14, fontWeight: '700', letterSpacing: 0.3 },

  periodBar: {
    flexDirection: 'row', backgroundColor: C.elevated,
    paddingHorizontal: 12, paddingVertical: 10, gap: 6,
    borderBottomWidth: 1, borderBottomColor: C.border,
  },
  periodChip: {
    borderWidth: 1, borderColor: C.border,
    borderRadius: 2, paddingHorizontal: 12, paddingVertical: 6,
  },
  periodChipActive: { backgroundColor: C.primary, borderColor: C.primary },
  periodChipText: { color: C.muted, fontSize: 12, fontWeight: '700' },
  periodChipTextActive: { color: C.onPrimary },

  customBar: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: C.elevated, paddingHorizontal: 12, paddingVertical: 10, gap: 6,
    borderBottomWidth: 1, borderBottomColor: C.border,
  },
  customLabel: { color: C.stone, fontSize: 11, fontWeight: '700' },
  dateBtn: {
    borderWidth: 1, borderColor: C.border, borderRadius: 2,
    paddingHorizontal: 8, paddingVertical: 5,
  },
  dateBtnText: { color: C.onDark, fontSize: 11, fontWeight: '700' },
  customTilde: { color: C.stone, fontSize: 12 },
  applyBtn: { backgroundColor: C.primary, borderRadius: 2, paddingHorizontal: 12, paddingVertical: 5 },
  applyBtnText: { color: C.onPrimary, fontSize: 11, fontWeight: '700' },

  sectionBar: {
    paddingHorizontal: 12, paddingVertical: 8,
    borderBottomWidth: 1, borderBottomColor: C.border,
  },
  metaLabel: { color: C.stone, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },

  list: { flex: 1 },
  rateCard: {
    backgroundColor: C.elevated,
    borderWidth: 1, borderColor: C.border, borderRadius: 2,
    padding: 12, gap: 8,
  },
  rateHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  rateTitle: { flex: 1, color: C.onDark, fontSize: 13, fontWeight: '700' },
  rateRight: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  rateDays: { color: C.stone, fontSize: 11, fontWeight: '700' },
  rateBadge: { borderRadius: 2, paddingHorizontal: 8, paddingVertical: 3 },
  ratePct: { color: C.onPrimary, fontSize: 11, fontWeight: '700' },
  barBg: { height: 3, backgroundColor: C.border, borderRadius: 0, overflow: 'hidden' },
  barFill: { height: '100%' },

  empty: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  emptyText: { color: C.stone, fontSize: 13, fontWeight: '700' },
});
