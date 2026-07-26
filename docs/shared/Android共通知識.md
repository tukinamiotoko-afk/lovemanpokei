# Android共通知識

複数のAndroidアプリ開発で役立つ、プロジェクト非依存の技術メモ。
「ラブ万歩計」での具体的な実装は各ファイルのコメントや
`docs/lovemanpokei/現在の仕様.md` を参照。

## 歩数計測（センサー）

- `Sensor.TYPE_STEP_COUNTER`（端末起動からの累積歩数）が使えればそれを使い、
  端末が対応していない場合は `Sensor.TYPE_ACCELEROMETER`（加速度センサー）から
  歩行を検知するフォールバックを用意すると、対応端末の幅が広がる
  （`HybridStepTracker.kt` のパターン）。
- `TYPE_STEP_COUNTER` は「増分（diff）」で扱う。センサー値そのものは
  端末再起動時にリセットされる累積値なので、前回値との差分だけを
  加算する設計にしないと日をまたいだ時や再起動時に歩数がおかしくなる。
- 歩数系のフォアグラウンドサービスには `foregroundServiceType="health"`
  （Android 14 / UPSIDE_DOWN_CAKE以降は `ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH`
  を明示的に渡す）と `ACTIVITY_RECOGNITION` パーミッションが必要。

## フォアグラウンドサービスを生き続けさせる

- `android:stopWithTask="false"` をサービスに指定し、タスクキルされても
  プロセス自体は残るようにする。
- `onTaskRemoved()` で `AlarmManager` を使い、1秒後などにサービス再起動用の
  `PendingIntent` を仕込んでおくと、OSに殺されてもすぐ復帰できる。
- `WorkManager`（`CoroutineWorker`）で定期的に「サービスが生きているか」を
  確認し、死んでいたら `startForegroundService()` で再起動する二重の保険を
  かけると、メーカー独自の電力管理機能にも強くなる。
- `onStartCommand()` は `START_STICKY` を返す。

## センサー処理とDB書き込みの競合

- センサーイベントは高頻度・非同期で飛んでくるため、DBへの
  read-modify-write（前回値を読んで差分を足して書き戻す）処理を
  そのまま並行実行すると値が壊れる。`Mutex`（`kotlinx.coroutines.sync.Mutex`）
  で直列化するのが簡単で確実。
- サービス起動直後はDBからの初期値読み込みが完了する前にセンサーイベントが
  届くことがある。`CompletableDeferred` などで「初期化完了まで待つ」ゲートを
  用意しないと、起動直後に歩数が0で上書きされるレースコンディションが起きる。

## 端末再起動後の自動復帰

- `RECEIVE_BOOT_COMPLETED` パーミッション + `BroadcastReceiver` で
  `ACTION_BOOT_COMPLETED` / `ACTION_LOCKED_BOOT_COMPLETED` /
  `QUICKBOOT_POWERON`（メーカー独自のクイックブート）を拾い、
  `startForegroundService()` でサービスを起動する。
- Android 8.0 (API26) 以降は `startForegroundService()` を使う
  （`startService()` だとバックグラウンド起動制限に引っかかる）。

## MIUI（Xiaomi）などメーカーカスタムROM対策

- 強制ダークモード機能による起動クラッシュは
  `android:forceDarkAllowed="false"` をテーマに入れて回避する
  （詳細は `docs/shared/エラー解決集.md` 参照）。
- バッテリーセーバー・電力管理機能が強いメーカー（MIUI/ColorOS/FuntouchOS等）を
  ターゲットにする場合、`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` の案内ダイアログを
  用意し、ユーザーに手動でバッテリー最適化を除外してもらう導線を作ると
  バックグラウンド動作の安定性が上がる。

## Room（ローカルDB）

- 開発中でスキーマがまだ固まっていない段階では
  `.fallbackToDestructiveMigration()` を使うとマイグレーション定義を
  省略できて楽だが、リリース後にスキーマを変える場合は本番データが
  消えるため、正式なMigrationに置き換える必要がある。
- `@Upsert` を使うと「あれば更新、なければ挿入」のDAOメソッドを
  1行で書ける（`INSERT ... ON CONFLICT` 相当）。

## その他

- Compose + Navigation Compose + Material3 + Room + WorkManager の組み合わせは
  個人開発の小〜中規模Androidアプリで扱いやすい標準構成。
- AI/LLM API（Gemini等）のAPIキーは、ビルド設定（`local.properties` /
  `BuildConfig`）ではなく、アプリ内の設定画面からユーザー自身に入力してもらい
  `SharedPreferences` に保存する方式であれば、リポジトリにキーが
  一切残らず安全（詳細は各プロジェクトの「プロジェクト概要.md」参照）。
