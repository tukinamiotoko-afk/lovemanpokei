import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';

const TABS = [
  { name: 'Home',          label: 'タスク一覧', icon: '📋' },
  { name: 'Calendar',      label: 'カレンダー', icon: '📅' },
  { name: 'Stats',         label: '実行率',     icon: '📊' },
  { name: 'Notifications', label: '通知',       icon: '🔔' },
] as const;

type TabName = typeof TABS[number]['name'];

interface Props {
  current: TabName;
  navigation: { navigate: (screen: string) => void };
}

const C = { card: '#ffffff', border: '#e2e8f0', primary: '#4a5569', muted: '#94a3b8' };

export default function TabBar({ current, navigation }: Props) {
  return (
    <View style={s.tabBar}>
      {TABS.map(({ name, label, icon }) => {
        const active = current === name;
        return (
          <TouchableOpacity
            key={name}
            style={[s.tabItem, active && s.tabItemActive]}
            onPress={() => { if (!active) navigation.navigate(name); }}
          >
            <Text style={s.tabIcon}>{icon}</Text>
            <Text style={[s.tabLabel, active && s.tabLabelActive]}>{label}</Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const s = StyleSheet.create({
  tabBar: {
    flexDirection: 'row',
    backgroundColor: C.card,
    borderTopWidth: 1, borderTopColor: C.border,
    height: 56,
  },
  tabItem: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 2 },
  tabItemActive: { borderTopWidth: 2, borderTopColor: C.primary },
  tabIcon: { fontSize: 18 },
  tabLabel: { color: C.muted, fontSize: 10, fontWeight: '700' },
  tabLabelActive: { color: C.primary },
});
