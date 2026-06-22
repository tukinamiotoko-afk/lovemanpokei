import React, { useState, useCallback, useRef } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, Modal,
  TextInput, StyleSheet, Alert, KeyboardAvoidingView,
  Platform, StatusBar, Animated,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';
import {
  Task, getToday, getTasks, addTask, deleteTask,
  getCompletedTaskIds, markComplete, markIncomplete,
} from '../db/database';

const C = {
  dark:      '#ffffff',
  elevated:  '#f7f7f7',
  border:    '#cccccc',
  hairline:  '#cccccc',
  primary:   '#1a73e8',
  onPrimary: '#ffffff',
  onDark:    '#1a1a1a',
  muted:     '#757575',
  stone:     '#898989',
  error:     '#e52020',
};

type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Home'> };

export default function HomeScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [completedIds, setCompletedIds] = useState<Set<number>>(new Set());
  const [showAdd, setShowAdd] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [showThumb, setShowThumb] = useState(false);
  const thumbAnim = useRef(new Animated.Value(0)).current;
  const today = getToday();

  const load = useCallback(async () => {
    const ts = await getTasks(db);
    const ids = await getCompletedTaskIds(db, today);
    setTasks(ts);
    setCompletedIds(new Set(ids));
  }, [db, today]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const triggerCelebration = () => {
    setShowThumb(true);
    thumbAnim.setValue(0);
    Animated.sequence([
      Animated.spring(thumbAnim, { toValue: 1, useNativeDriver: true, tension: 180, friction: 6 }),
      Animated.delay(500),
      Animated.timing(thumbAnim, { toValue: 0, duration: 250, useNativeDriver: true }),
    ]).start(() => setShowThumb(false));
  };

  const toggle = async (id: number) => {
    if (completedIds.has(id)) {
      await markIncomplete(db, id, today);
    } else {
      await markComplete(db, id, today);
      triggerCelebration();
    }
    load();
  };

  const handleAdd = async () => {
    const title = newTitle.trim();
    if (!title) return;
    await addTask(db, title);
    setNewTitle('');
    setShowAdd(false);
    load();
  };

  const handleDelete = (task: Task) => {
    Alert.alert('削除', `「${task.title}」を削除しますか？`, [
      { text: 'キャンセル', style: 'cancel' },
      { text: '削除', style: 'destructive', onPress: async () => { await deleteTask(db, task.id); load(); } },
    ]);
  };

  const done = tasks.filter((t) => completedIds.has(t.id)).length;
  const total = tasks.length;
  const progress = total > 0 ? done / total : 0;

  const now = new Date();
  const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
  const dateLabel = `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, '0')}/${String(now.getDate()).padStart(2, '0')} (${weekdays[now.getDay()]})`;

  const listHeader = (
    <View style={s.listHeader}>
      <View style={s.progressPanel}>
        <Text style={s.dateText}>{dateLabel}</Text>
        <Text style={s.metaLabel}>今日の進捗</Text>
        <View style={s.progressRow}>
          <View style={s.progressBg}>
            <View style={[s.progressFill, { width: `${progress * 100}%` as any }]} />
          </View>
          <Text style={s.progressText}>{done} / {total}</Text>
        </View>
      </View>
      <View style={s.sectionBar}>
        <Text style={s.metaLabel}>チェックリスト</Text>
        {total > 0 && <Text style={s.stone}>{total}件</Text>}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor={C.dark} />

      <View style={s.navBar} />

      <FlatList
        data={tasks}
        keyExtractor={(item) => String(item.id)}
        style={s.list}
        contentContainerStyle={{ paddingHorizontal: 12, paddingTop: 8, paddingBottom: 8, gap: 6 }}
        ListHeaderComponent={listHeader}
        ListEmptyComponent={
          <View style={s.empty}>
            <View style={s.emptyBox}>
              <Text style={s.emptyTitle}>タスクなし</Text>
              <Text style={s.emptyBody}>毎日やることを追加しましょう</Text>
              <TouchableOpacity style={s.emptyButton} onPress={() => setShowAdd(true)}>
                <Text style={s.emptyButtonText}>＋</Text>
              </TouchableOpacity>
            </View>
          </View>
        }
        renderItem={({ item }) => {
          const isDone = completedIds.has(item.id);
          return (
            <TouchableOpacity
              style={[s.taskCard, isDone && s.taskCardDone]}
              onPress={() => toggle(item.id)}
              activeOpacity={0.75}
            >
              <View style={[s.checkBox, isDone && s.checkBoxDone]}>
                {isDone && <Text style={s.checkMark}>✓</Text>}
              </View>
              <Text style={[s.taskTitle, isDone && s.taskTitleDone]} numberOfLines={2}>
                {item.title}
              </Text>
              {isDone && (
                <View style={s.doneBadge}>
                  <Text style={s.doneBadgeText}>完了</Text>
                </View>
              )}
              <TouchableOpacity style={s.deleteBtn} onPress={() => handleDelete(item)} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
                <Text style={s.deleteBtnText}>✕</Text>
              </TouchableOpacity>
            </TouchableOpacity>
          );
        }}
        ListFooterComponent={<View style={{ height: 80 }} />}
      />

      <View style={s.tabBar}>
        <TouchableOpacity style={[s.tabItem, s.tabItemActive]} onPress={() => {}}>
          <Text style={[s.tabLabel, s.tabLabelActive]}>タスク一覧</Text>
        </TouchableOpacity>
        <TouchableOpacity style={s.tabItem} onPress={() => navigation.navigate('Stats')}>
          <Text style={s.tabLabel}>実行率</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity style={s.fab} onPress={() => setShowAdd(true)} activeOpacity={0.85}>
        <Text style={s.fabText}>＋</Text>
      </TouchableOpacity>

      {showThumb && (
        <Animated.View style={[s.thumbOverlay, {
          opacity: thumbAnim,
          transform: [{ scale: thumbAnim.interpolate({ inputRange: [0, 1], outputRange: [0.4, 1] }) }],
        }]} pointerEvents="none">
          <Text style={s.thumbEmoji}>👍</Text>
        </Animated.View>
      )}

      <Modal visible={showAdd} transparent animationType="fade" onRequestClose={() => setShowAdd(false)}>
        <KeyboardAvoidingView style={s.modalBg} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
          <View style={s.modalCard}>
            <Text style={s.modalTitle}>タスクを追加</Text>
            <View style={s.modalDivider} />
            <Text style={s.modalLabel}>タスク名</Text>
            <TextInput
              style={s.modalInput}
              value={newTitle}
              onChangeText={setNewTitle}
              placeholder="例：歯磨き、運動、水を飲む"
              placeholderTextColor={C.stone}
              autoFocus
              returnKeyType="done"
              onSubmitEditing={handleAdd}
            />
            <View style={s.modalButtons}>
              <TouchableOpacity style={s.modalCancel} onPress={() => { setShowAdd(false); setNewTitle(''); }}>
                <Text style={s.modalCancelText}>キャンセル</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[s.modalConfirm, !newTitle.trim() && s.modalConfirmDisabled]}
                onPress={handleAdd}
                disabled={!newTitle.trim()}
              >
                <Text style={s.modalConfirmText}>追加</Text>
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
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
  navTitle: { flex: 1, color: C.primary, fontSize: 14, fontWeight: '700', letterSpacing: 0.3 },

  tabBar: {
    flexDirection: 'row',
    backgroundColor: C.elevated,
    borderTopWidth: 1, borderTopColor: C.border,
    height: 52,
  },
  tabItem: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  tabItemActive: { borderTopWidth: 2, borderTopColor: C.primary },
  tabLabel: { color: C.muted, fontSize: 12, fontWeight: '700' },
  tabLabelActive: { color: C.primary },

  list: { flex: 1 },

  listHeader: { gap: 8, marginBottom: 4 },
  dateText: { color: C.stone, fontSize: 11, fontWeight: '700', letterSpacing: 0.3, marginBottom: 6 },

  progressPanel: {
    backgroundColor: C.elevated,
    borderRadius: 12, borderWidth: 1, borderColor: C.border, padding: 14,
  },
  progressRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 8 },
  progressBg: { flex: 1, height: 4, backgroundColor: C.border, borderRadius: 0, overflow: 'hidden' },
  progressFill: { height: '100%', backgroundColor: C.primary },
  progressText: { color: C.muted, fontSize: 11, fontWeight: '700' },

  sectionBar: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingVertical: 4,
    borderBottomWidth: 1, borderBottomColor: C.border,
  },
  metaLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  stone: { color: C.stone, fontSize: 11, fontWeight: '700' },

  taskCard: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: '#fffbe6',
    borderWidth: 1, borderColor: C.border, borderRadius: 12,
    paddingHorizontal: 12, paddingVertical: 13, gap: 10,
  },
  taskCardDone: { borderColor: C.primary, opacity: 0.7 },
  checkBox: {
    width: 20, height: 20, borderRadius: 2,
    borderWidth: 1, borderColor: C.border,
    alignItems: 'center', justifyContent: 'center',
  },
  checkBoxDone: { backgroundColor: C.primary, borderColor: C.primary },
  checkMark: { color: C.onPrimary, fontSize: 12, fontWeight: '700' },
  taskTitle: { flex: 1, color: C.onDark, fontSize: 13, fontWeight: '400', lineHeight: 18 },
  taskTitleDone: { color: C.stone, textDecorationLine: 'line-through' },
  doneBadge: {
    backgroundColor: C.primary, borderRadius: 2, paddingHorizontal: 6, paddingVertical: 2,
  },
  doneBadgeText: { color: C.onPrimary, fontSize: 9, fontWeight: '700', letterSpacing: 0.5 },

  deleteBtn: {
    width: 28, height: 28, borderRadius: 14,
    backgroundColor: '#fee2e2',
    alignItems: 'center', justifyContent: 'center',
  },
  deleteBtnText: { color: '#e52020', fontSize: 12, fontWeight: '700' },

  empty: { paddingVertical: 40 },
  emptyBox: {
    backgroundColor: C.elevated, borderRadius: 2,
    borderWidth: 1, borderColor: C.border,
    padding: 32, alignItems: 'center', gap: 12,
  },
  emptyTitle: { color: C.onDark, fontSize: 20, fontWeight: '700' },
  emptyBody: { color: C.muted, fontSize: 13 },
  emptyButton: {
    width: 44, height: 44, borderRadius: 2,
    backgroundColor: C.primary, alignItems: 'center', justifyContent: 'center', marginTop: 8,
  },
  emptyButtonText: { color: C.onPrimary, fontSize: 22, fontWeight: '700' },

  fab: {
    position: 'absolute', bottom: 68, right: 20,
    width: 52, height: 52, borderRadius: 2,
    backgroundColor: C.primary,
    alignItems: 'center', justifyContent: 'center',
    elevation: 4,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.2, shadowRadius: 4,
  },
  fabText: { color: C.onPrimary, fontSize: 26, fontWeight: '700', lineHeight: 30 },

  modalBg: { flex: 1, backgroundColor: 'rgba(0,0,0,0.85)', justifyContent: 'center', padding: 20 },
  modalCard: {
    backgroundColor: C.elevated, borderRadius: 2,
    borderWidth: 1, borderColor: C.border, padding: 20, gap: 12,
  },
  modalTitle: { color: C.primary, fontSize: 14, fontWeight: '700', letterSpacing: 0.5 },
  modalDivider: { height: 1, backgroundColor: C.border },
  modalLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  modalInput: {
    borderWidth: 1, borderColor: C.border, borderRadius: 2,
    padding: 10, fontSize: 14, color: C.onDark,
    backgroundColor: '#ffffff', height: 46,
  },
  modalButtons: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 4 },
  modalCancel: {
    borderRadius: 2, borderWidth: 1, borderColor: C.border,
    paddingHorizontal: 16, paddingVertical: 9,
  },
  modalCancelText: { color: C.onDark, fontSize: 13, fontWeight: '700' },
  modalConfirm: { backgroundColor: C.primary, borderRadius: 2, paddingHorizontal: 20, paddingVertical: 9 },
  modalConfirmDisabled: { backgroundColor: C.border },
  modalConfirmText: { color: C.onPrimary, fontSize: 13, fontWeight: '700' },

  thumbOverlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    alignItems: 'center', justifyContent: 'center',
  },
  thumbEmoji: { fontSize: 80 },
});
