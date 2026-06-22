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
  Task, getToday, getTasks, addTask, updateTask, deleteTask,
  getCompletedTaskIds, markComplete, markIncomplete,
} from '../db/database';
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

type Props = { navigation: NativeStackNavigationProp<RootStackParamList, 'Home'> };

export default function HomeScreen({ navigation }: Props) {
  const db = useSQLiteContext();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [completedIds, setCompletedIds] = useState<Set<number>>(new Set());
  const [showAdd, setShowAdd] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [showEdit, setShowEdit] = useState(false);
  const [editTask, setEditTask] = useState<Task | null>(null);
  const [editTitle, setEditTitle] = useState('');
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

  const openEdit = (task: Task) => {
    setEditTask(task);
    setEditTitle(task.title);
    setShowEdit(true);
  };

  const handleEdit = async () => {
    if (!editTask || !editTitle.trim()) return;
    await updateTask(db, editTask.id, editTitle.trim());
    setShowEdit(false);
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

  return (
    <SafeAreaView style={s.safeArea} edges={['top', 'bottom']}>
      <StatusBar barStyle="light-content" backgroundColor={C.header} />

      <View style={s.headerCard}>
        <Text style={s.dateText}>{dateLabel}</Text>
        <Text style={s.headerLabel}>今日の進捗</Text>
        <View style={s.progressRow}>
          <View style={s.progressBg}>
            <View style={[s.progressFill, { width: `${progress * 100}%` as any }]} />
          </View>
          <Text style={s.progressText}>{done} / {total}</Text>
        </View>
      </View>

      <FlatList
        data={tasks}
        keyExtractor={(item) => String(item.id)}
        style={s.list}
        contentContainerStyle={{ padding: 16, gap: 10 }}
        ListHeaderComponent={
          <View style={s.sectionBar}>
            <Text style={s.metaLabel}>チェックリスト</Text>
            {total > 0 && <Text style={s.stone}>{total}件</Text>}
          </View>
        }
        ListEmptyComponent={
          <View style={s.empty}>
            <Text style={s.emptyTitle}>タスクなし</Text>
            <Text style={s.emptyBody}>右下の ＋ から追加できます</Text>
          </View>
        }
        renderItem={({ item }) => {
          const isDone = completedIds.has(item.id);
          return (
            <View style={[s.taskCard, isDone && s.taskCardDone]}>
              <TouchableOpacity
                style={[s.checkBox, isDone && s.checkBoxDone]}
                onPress={() => toggle(item.id)}
                hitSlop={{ top: 8, bottom: 8, left: 4, right: 4 }}
              >
                {isDone && <Text style={s.checkMark}>✓</Text>}
              </TouchableOpacity>
              <TouchableOpacity style={s.taskBody} onPress={() => openEdit(item)} activeOpacity={0.7}>
                <Text style={[s.taskTitle, isDone && s.taskTitleDone]} numberOfLines={2}>
                  {item.title}
                </Text>
                {isDone && (
                  <View style={s.doneBadge}>
                    <Text style={s.doneBadgeText}>完了</Text>
                  </View>
                )}
              </TouchableOpacity>
              <TouchableOpacity style={s.deleteBtn} onPress={() => handleDelete(item)} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
                <Text style={s.deleteBtnText}>🗑️</Text>
              </TouchableOpacity>
            </View>
          );
        }}
        ListFooterComponent={<View style={{ height: 80 }} />}
      />

      <TabBar current="Home" navigation={navigation} />

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

      {/* Add modal */}
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
              placeholderTextColor={C.muted}
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

      {/* Edit modal */}
      <Modal visible={showEdit} transparent animationType="fade" onRequestClose={() => setShowEdit(false)}>
        <KeyboardAvoidingView style={s.modalBg} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
          <View style={s.modalCard}>
            <Text style={s.modalTitle}>タスクを編集</Text>
            <View style={s.modalDivider} />
            <Text style={s.modalLabel}>タスク名</Text>
            <TextInput
              style={s.modalInput}
              value={editTitle}
              onChangeText={setEditTitle}
              autoFocus
              returnKeyType="done"
              onSubmitEditing={handleEdit}
            />
            <View style={s.modalButtons}>
              <TouchableOpacity style={s.modalCancel} onPress={() => setShowEdit(false)}>
                <Text style={s.modalCancelText}>キャンセル</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[s.modalConfirm, !editTitle.trim() && s.modalConfirmDisabled]}
                onPress={handleEdit}
                disabled={!editTitle.trim()}
              >
                <Text style={s.modalConfirmText}>保存</Text>
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </SafeAreaView>
  );
}

const s = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: C.header },

  headerCard: {
    backgroundColor: C.header,
    paddingHorizontal: 20, paddingTop: 12, paddingBottom: 24,
  },
  dateText: { color: 'rgba(255,255,255,0.6)', fontSize: 12, fontWeight: '600', marginBottom: 12 },
  headerLabel: { color: 'rgba(255,255,255,0.6)', fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  progressRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 6 },
  progressBg: { flex: 1, height: 4, backgroundColor: 'rgba(255,255,255,0.25)', borderRadius: 2, overflow: 'hidden' },
  progressFill: { height: '100%', backgroundColor: '#ffffff' },
  progressText: { color: '#ffffff', fontSize: 11, fontWeight: '700' },

  list: { flex: 1, backgroundColor: C.body },
  sectionBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 },
  metaLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  stone: { color: C.stone, fontSize: 11, fontWeight: '700' },

  taskCard: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: '#fffbe6',
    borderRadius: 12,
    paddingHorizontal: 14, paddingVertical: 14, gap: 12,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4,
    elevation: 2,
  },
  taskCardDone: { opacity: 0.6 },
  checkBox: {
    width: 22, height: 22, borderRadius: 11,
    borderWidth: 2, borderColor: C.border,
    alignItems: 'center', justifyContent: 'center',
  },
  checkBoxDone: { backgroundColor: C.primary, borderColor: C.primary },
  checkMark: { color: C.onPrimary, fontSize: 11, fontWeight: '700' },
  taskBody: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 8 },
  taskTitle: { flex: 1, color: C.onDark, fontSize: 14, fontWeight: '500', lineHeight: 20 },
  taskTitleDone: { color: C.muted, textDecorationLine: 'line-through' },
  doneBadge: { backgroundColor: C.header, borderRadius: 6, paddingHorizontal: 8, paddingVertical: 3 },
  doneBadgeText: { color: C.onPrimary, fontSize: 9, fontWeight: '700', letterSpacing: 0.5 },

  deleteBtn: { width: 32, height: 32, borderRadius: 16, backgroundColor: '#fee2e2', alignItems: 'center', justifyContent: 'center' },
  deleteBtnText: { fontSize: 16 },

  empty: { paddingVertical: 60, alignItems: 'center', gap: 8 },
  emptyTitle: { color: C.stone, fontSize: 16, fontWeight: '700' },
  emptyBody: { color: C.muted, fontSize: 13 },

  fab: {
    position: 'absolute', bottom: 72, right: 20,
    width: 52, height: 52, borderRadius: 26,
    backgroundColor: C.primary,
    alignItems: 'center', justifyContent: 'center',
    elevation: 6,
    shadowColor: '#000', shadowOffset: { width: 0, height: 3 }, shadowOpacity: 0.25, shadowRadius: 6,
  },
  fabText: { color: C.onPrimary, fontSize: 26, fontWeight: '400', lineHeight: 30 },

  modalBg: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', padding: 20 },
  modalCard: { backgroundColor: C.card, borderRadius: 16, padding: 20, gap: 12, elevation: 8, shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.15, shadowRadius: 12 },
  modalTitle: { color: C.onDark, fontSize: 16, fontWeight: '700' },
  modalDivider: { height: 1, backgroundColor: C.border },
  modalLabel: { color: C.muted, fontSize: 11, fontWeight: '700', letterSpacing: 0.5 },
  modalInput: { borderWidth: 1, borderColor: C.border, borderRadius: 10, padding: 12, fontSize: 14, color: C.onDark, backgroundColor: C.body },
  modalButtons: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 4 },
  modalCancel: { borderRadius: 8, borderWidth: 1, borderColor: C.border, paddingHorizontal: 16, paddingVertical: 9 },
  modalCancelText: { color: C.stone, fontSize: 13, fontWeight: '700' },
  modalConfirm: { backgroundColor: C.primary, borderRadius: 8, paddingHorizontal: 20, paddingVertical: 9 },
  modalConfirmDisabled: { backgroundColor: C.border },
  modalConfirmText: { color: C.onPrimary, fontSize: 13, fontWeight: '700' },

  thumbOverlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, alignItems: 'center', justifyContent: 'center' },
  thumbEmoji: { fontSize: 80 },
});
