import React, { useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, Modal,
  TextInput, StyleSheet, Alert, KeyboardAvoidingView,
  Platform, StatusBar,
} from 'react-native';
import { useSQLiteContext } from 'expo-sqlite';
import { useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../App';
import {
  Task, getToday, getTasks, addTask, deleteTask,
  getCompletedTaskIds, markComplete, markIncomplete,
} from '../db/database';

const C = {
  canvas:   '#000000',
  elevated: '#121314',
  card:     '#181818',
  primary:  '#0070d1',
  onDark:   '#ffffff',
  bodyDark: 'rgba(255,255,255,0.7)',
  muteDark: 'rgba(229,229,229,0.55)',
  hairline: 'rgba(229,229,229,0.2)',
  warning:  '#c81b3a',
};

type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Home'> };

export default function HomeScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [completedIds, setCompletedIds] = useState<Set<number>>(new Set());
  const [showAdd, setShowAdd] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const today = getToday();

  const load = useCallback(async () => {
    const ts = await getTasks(db);
    const ids = await getCompletedTaskIds(db, today);
    setTasks(ts);
    setCompletedIds(new Set(ids));
  }, [db, today]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const toggle = async (id: number) => {
    if (completedIds.has(id)) {
      await markIncomplete(db, id, today);
    } else {
      await markComplete(db, id, today);
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

  // Japanese date display
  const now = new Date();
  const weekdays = ['日', '月', '火', '水', '木', '金', '土'];
  const dateLabel = `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, '0')}/${String(now.getDate()).padStart(2, '0')} (${weekdays[now.getDay()]})`;

  return (
    <View style={s.root}>
      <StatusBar barStyle="light-content" backgroundColor={C.canvas} />

      <View style={s.navBar}>
        <Text style={s.navTitle}>毎日タスク</Text>
        <View style={s.navButtons}>
          <TouchableOpacity style={s.chipSecondary} onPress={() => navigation.navigate('Stats')}>
            <Text style={s.chipSecondaryText}>実行率</Text>
          </TouchableOpacity>
          <TouchableOpacity style={s.chipPrimary} onPress={() => setShowAdd(true)}>
            <Text style={s.chipPrimaryText}>＋ ADD</Text>
          </TouchableOpacity>
        </View>
      </View>

      <View style={s.dateStrip}>
        <Text style={s.dateText}>{dateLabel}</Text>
      </View>

      <View style={s.progressPanel}>
        <Text style={s.metaLabel}>TODAY'S PROGRESS</Text>
        <View style={s.progressRow}>
          <View style={s.progressBg}>
            <View style={[s.progressFill, { width: `${progress * 100}%` as any }]} />
          </View>
          <Text style={s.progressText}>{done} / {total} 完了</Text>
        </View>
      </View>

      <View style={s.sectionBar}>
        <Text style={s.metaLabel}>CHECKLIST</Text>
        {total > 0 && <Text style={s.metaLabel}>{total}件</Text>}
      </View>

      {tasks.length === 0 ? (
        <View style={s.empty}>
          <View style={s.emptyBox}>
            <Text style={s.emptyTitle}>NO TASKS</Text>
            <Text style={s.emptyBody}>毎日やることを追加しましょう</Text>
            <TouchableOpacity style={s.emptyButton} onPress={() => setShowAdd(true)}>
              <Text style={s.emptyButtonText}>＋</Text>
            </TouchableOpacity>
          </View>
        </View>
      ) : (
        <FlatList
          data={tasks}
          keyExtractor={(item) => String(item.id)}
          style={s.list}
          contentContainerStyle={{ paddingHorizontal: 16, paddingVertical: 8, gap: 8 }}
          renderItem={({ item }) => {
            const isDone = completedIds.has(item.id);
            return (
              <TouchableOpacity
                style={[s.taskCard, isDone && s.taskCardDone]}
                onPress={() => toggle(item.id)}
                onLongPress={() => handleDelete(item)}
                activeOpacity={0.7}
              >
                <View style={[s.checkCircle, isDone && s.checkCircleDone]}>
                  {isDone && <Text style={s.checkMark}>✓</Text>}
                </View>
                <Text style={[s.taskTitle, isDone && s.taskTitleDone]} numberOfLines={2}>
                  {item.title}
                </Text>
                {isDone && (
                  <View style={s.doneBadge}>
                    <Text style={s.doneBadgeText}>DONE</Text>
                  </View>
                )}
              </TouchableOpacity>
            );
          }}
          ListFooterComponent={<View style={{ height: 16 }} />}
        />
      )}

      <View style={s.bottomBar}>
        <TouchableOpacity style={s.addButton} onPress={() => setShowAdd(true)} activeOpacity={0.85}>
          <Text style={s.addButtonText}>タスクを追加する</Text>
        </TouchableOpacity>
      </View>

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
              placeholderTextColor="rgba(0,0,0,0.35)"
              autoFocus
              returnKeyType="done"
              onSubmitEditing={handleAdd}
            />
            <View style={s.modalButtons}>
              <TouchableOpacity style={s.modalCancel} onPress={() => { setShowAdd(false); setNewTitle(''); }}>
                <Text style={s.modalCancelText}>キャンセル</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[s.modalConfirm, !newTitle.trim() && s.modalConfirmDisabled]} onPress={handleAdd} disabled={!newTitle.trim()}>
                <Text style={s.modalConfirmText}>追加</Text>
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </View>
  );
}

const s = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.canvas },

  navBar: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: C.canvas, height: 52, paddingHorizontal: 16,
    borderBottomWidth: 1, borderBottomColor: C.hairline,
  },
  navTitle: { flex: 1, color: C.onDark, fontSize: 16, fontWeight: '500', letterSpacing: 0.4 },
  navButtons: { flexDirection: 'row', gap: 8 },
  chipSecondary: {
    backgroundColor: 'rgba(255,255,255,0.1)',
    borderRadius: 9999, paddingHorizontal: 14, paddingVertical: 7,
  },
  chipSecondaryText: { color: C.onDark, fontSize: 12, fontWeight: '700', letterSpacing: 0.3 },
  chipPrimary: {
    backgroundColor: C.primary,
    borderRadius: 9999, paddingHorizontal: 14, paddingVertical: 7,
  },
  chipPrimaryText: { color: C.onDark, fontSize: 12, fontWeight: '700', letterSpacing: 0.3 },

  dateStrip: {
    backgroundColor: C.elevated, paddingHorizontal: 16, paddingVertical: 8,
    borderBottomWidth: 1, borderBottomColor: C.hairline,
  },
  dateText: { color: C.muteDark, fontSize: 12, fontWeight: '400', letterSpacing: 0.2 },

  progressPanel: { margin: 16, backgroundColor: C.card, borderRadius: 8, padding: 16 },
  progressRow: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 10 },
  progressBg: { flex: 1, height: 4, backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: 2, overflow: 'hidden' },
  progressFill: { height: '100%', backgroundColor: C.primary, borderRadius: 2 },
  progressText: { color: C.bodyDark, fontSize: 12, fontWeight: '500' },

  sectionBar: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: 16, paddingVertical: 8,
  },
  metaLabel: { color: C.muteDark, fontSize: 11, fontWeight: '500', letterSpacing: 0.5 },

  list: { flex: 1 },
  taskCard: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: C.card, borderRadius: 8,
    paddingHorizontal: 16, paddingVertical: 14, gap: 12,
  },
  taskCardDone: { opacity: 0.55 },
  checkCircle: {
    width: 24, height: 24, borderRadius: 12,
    borderWidth: 1, borderColor: C.hairline,
    alignItems: 'center', justifyContent: 'center',
  },
  checkCircleDone: { backgroundColor: C.primary, borderColor: C.primary },
  checkMark: { color: C.onDark, fontSize: 13, fontWeight: '700' },
  taskTitle: { flex: 1, color: C.onDark, fontSize: 14, fontWeight: '400', lineHeight: 20 },
  taskTitleDone: { color: C.bodyDark, textDecorationLine: 'line-through' },
  doneBadge: { backgroundColor: C.primary, borderRadius: 9999, paddingHorizontal: 8, paddingVertical: 3 },
  doneBadgeText: { color: C.onDark, fontSize: 10, fontWeight: '700', letterSpacing: 0.3 },

  empty: { flex: 1, padding: 24, justifyContent: 'center' },
  emptyBox: { backgroundColor: C.card, borderRadius: 8, padding: 32, alignItems: 'center', gap: 12 },
  emptyTitle: { color: C.onDark, fontSize: 22, fontWeight: '300', letterSpacing: 0.1 },
  emptyBody: { color: C.bodyDark, fontSize: 14 },
  emptyButton: { width: 48, height: 48, borderRadius: 9999, backgroundColor: C.primary, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  emptyButtonText: { color: C.onDark, fontSize: 24, fontWeight: '300' },

  bottomBar: { backgroundColor: C.elevated, padding: 16, borderTopWidth: 1, borderTopColor: C.hairline },
  addButton: { backgroundColor: C.primary, borderRadius: 9999, height: 48, alignItems: 'center', justifyContent: 'center' },
  addButtonText: { color: C.onDark, fontSize: 14, fontWeight: '700', letterSpacing: 0.45 },

  modalBg: { flex: 1, backgroundColor: 'rgba(0,0,0,0.85)', justifyContent: 'center', padding: 24 },
  modalCard: { backgroundColor: C.elevated, borderRadius: 8, padding: 24, gap: 12 },
  modalTitle: { color: C.onDark, fontSize: 22, fontWeight: '300' },
  modalDivider: { height: 1, backgroundColor: C.hairline },
  modalLabel: { color: C.muteDark, fontSize: 12, fontWeight: '500', letterSpacing: 0.3 },
  modalInput: {
    borderWidth: 1, borderColor: 'rgba(229,229,229,0.3)',
    borderRadius: 4, padding: 12, fontSize: 14,
    color: '#000000', backgroundColor: '#ffffff', height: 48,
  },
  modalButtons: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 4 },
  modalCancel: { borderRadius: 9999, borderWidth: 1, borderColor: C.hairline, paddingHorizontal: 20, paddingVertical: 10 },
  modalCancelText: { color: C.onDark, fontSize: 13, fontWeight: '700' },
  modalConfirm: { backgroundColor: C.primary, borderRadius: 9999, paddingHorizontal: 24, paddingVertical: 10 },
  modalConfirmDisabled: { backgroundColor: 'rgba(255,255,255,0.15)' },
  modalConfirmText: { color: C.onDark, fontSize: 13, fontWeight: '700' },
});
