package com.example.lovemanpo

// --- 登場人物の定義 ---
enum class Speaker {
    HIKARI,      // ひかり
    PROTAGONIST, // 俺（プレイヤー）
    NARRATION    // 地の文（ナレーション）
}

// --- ストーリーの1行データ ---
data class StoryLine(
    val speaker: Speaker,
    val text: String,
    val expressionRes: Int? = null,
    val backgroundRes: Int? = null, // 行ごとに背景を上書きする場合に使用
    val shouldClear: Boolean = false
)

// --- ストーリーエピソードの定義 ---
data class StoryEpisode(
    val id: Int,
    val title: String,
    val backgroundRes: Int,
    val script: List<StoryLine>,
    val requiredLove: Int, // 解放に必要なラブ度
    val requiredPoints: Int // 解放に必要な行動ポイント
)

// --- 歩数に応じたセリフの定義 ---
data class StepDialogue(
    val thresholdSteps: Int, // この歩数以上で表示
    val message: String,
    val expressionRes: Int? = null // セリフごとの表情（nullならデフォルトを使用）
)

// --- タップした時のセリフの定義 ---
data class TouchDialogue(
    val message: String,
    val expressionRes: Int? = null
)

// --- ラブに応じて解放されるホーム画面の反応 ---
data class LoveContent(
    val thresholdLove: Int,      // このラブ数以上で解放
    val expressionRes: Int,      // デフォルトの表情
    val stepDialogues: List<StepDialogue>, // 歩数ごとのセリフ
    val touchDialogues: List<TouchDialogue> // タップした時のセリフ
)

// ==========================================
// ★ 各エピソードのスクリプト定義 ★
// ==========================================

val script1_Encounter = listOf(
    StoryLine(Speaker.HIKARI, "今日からお散歩サークルに入ることになりました！ひかりって言います！よろしくお願いします！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ところでお散歩サークルって何をするんですか？ほうほう、街中を歩いて色んなお店とかもめぐりながらその街を散策していく感じなんですね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "へえ！ダイエットも兼ねてるんですか？○○さんも人が良ささそうで、体型も維持できそうだしここに入ってよかったです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "そうですね!お散歩していきましょうか！よろしくお願いいたします", R.drawable.hikari_celebrate, shouldClear = true)
)

val script2_FirstWalk = listOf(
    StoryLine(Speaker.HIKARI, "○○さんはちなみにどれくらい歩かれるんですか？へー、一応一日５千歩くらいが目安なんですねー。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "私, 普段から歩る習慣がないので、すぐにへばっちゃいそうですけど頑張って歩きますね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "私がもし弱音を吐くようでしたら、すぐに声をかけてくださいね？私も慣れてきたら、○○さんの事を励ましてあげるのでよろしくお願いします！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ところで○○さん、もう疲れてきました…え？まだ２千歩しか歩いてないんですか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もしよろしければ、私のことを市民ランナー程度に応援してくださってもよろしいでしょうか？あそこに見える、結構いい感じのカフェまで何とか頑張ります！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "給水所までは頑張らないといけないのがマラソンですからね…", R.drawable.hikari_celebrate, shouldClear = true)
)

val script3_CafeMaster = listOf(
    StoryLine(Speaker.HIKARI, "ここのコーヒー美味しいですね！エスプレッソって初めて飲みました！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "作り方はよくわかりませんがエスプレッソというだけあって香りが芳醇です！絶対に豆からこだわって厳選してますよ！", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "大体マスターの顔を見ればわかっちゃうんですよね、だって見てください○○さん！あのマスターのひげも髪も真っ白です！これはマスターに対するこだわりもすごいですよ！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もしかしたらあのマスターがエスプレッソを抽出しているところに、このカフェが建設されたのかもしれませんね！", R.drawable.hikari_smile, shouldClear = true)
)

val script4_CafeMaster2 = listOf(
    StoryLine(Speaker.HIKARI, "コーヒーで休憩できたのでもう歩けそうです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ところで○○さん、あのマスターは, たぶん私たちのことをカップルだと思ってるかもしれませんよ。", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "だって、周りを見てください、文科系の学生と思われる人たちや、熟年夫婦とおとなしそうな女性がほとんどですよ. だから、わざわざこういう場所に来る男女は気心知れた仲だと推測するのが定石のはず……", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ちなみに、あの若い男女はカップルでしょうか…こんな風に、喫茶店にいると人間観察をやってしまいますね…きっとマスターもおんなじはずです！", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "見てください…マスターがこちらを向いて少し微笑みましたよ…確定です！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あの微笑みは、俺はマスターの中のマスターだから、喫茶店の背景と同化した状態の、いわゆる好感度の高い、ただコーヒーを作り続けるお父さんなのだと思わせて、実際にはこちらをつぶさに観察して、自分とは遠くなってしまった、あの頃の青春のドリップが滴り落ちる瞬間を見逃すまいとする, そんな含みのある表情でしたね。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "だから、これからは気を付けてください。バリスタという生き物は、すごくいい雰囲気の喫茶店を作り、世の中でもより善良である文科系の人々を、エスプレッソコーヒーやＢＬＴサンドの美味しさで油断をさせながら、この場所では幸せを人に見せても構わないと、そんな気分にさせるのが好きなんです。そして、それを悟られない為のマスターの表情管理や恰好の工作も完璧です。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さん、私達も反撃に出るべきです！パフェを注文しましょう！逆にこの空間なら、二人でパフェを食べようが何も問題はないわけですから！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ちなみに、この後は商店街ですか？なんだか本当のデートみたいになってきましたね！", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "マスター！スペシャルパフェを一つお願いします！", R.drawable.hikari_celebrate, shouldClear = true)
)

val script5_Croquette = listOf(
    StoryLine(Speaker.HIKARI, "商店街ってなかなか来ないので新鮮です！意外とカップルもいるみたいですね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "お店がいっぱいありますけど、食べ歩きできそうなところに目移りしてしまいますね！せっかくなので何か食べていきましょうよー、コロッケとかすごくおいしそうですよ！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "なんだか揚げたてのお惣菜の匂いで我慢ができなくなってきました…早く何か胃袋に入れないと、私は食を求め、ネオンを彷徨い野生化した挙挙誰かに引き取ってもらわないといけなくなるかも。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ハヤク, コロッケ, ヲ, ヨコセエエエエ！！！", R.drawable.hikari_devil, shouldClear = true)
)

val script6_FriedBread = listOf(
    StoryLine(Speaker.NARRATION, "ムシャッ、ムシャリッ, ハフッ、ハフ, ゴクン。。。", shouldClear = true),
    StoryLine(Speaker.HIKARI, "もしかして私にコロッケをくださったのは○○さんですか？私がコロッケを求めて暴走するのを察して、止めてくれたんですね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "それでは是非聞いてください、私の前に現れました、地上の現人神様よ。", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "私の中に巣食う暴食の悪魔を取り払うべく、この地, いえ、この欲望の 海に見つけてしまった、かの店にある、黄金のきな粉揚げパンを恵んでくださらないでしょうか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もう既に私の目は、ハゲワシの様に落ち窪み、私の心は、焼却炉に投げ込まれたフランス人形のように焼け焦がれています。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "フゥー。。。そろそろ審判の時が近づいているかもしれません。早く揚げパンを私に。。。", R.drawable.hikari_think, shouldClear = true)
)

val script7_MemoryLoss = listOf(
    StoryLine(Speaker.HIKARI, "すみません…私…記憶を失っていたみたいです…。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "なんか, 揚げパンを食べていたような、いなかったような気がするのですが、揚げパンの行方はどうなってしまったのでしょうか. せっかくなので食べてみたかったのですが、残念ですね…。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "え！？揚げパンを買っていてくださったんですか！ありがとうござます！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "モグ…モグ…。やっぱり想像通り、スッゴクおいしいです！やっぱり揚げパンは懐かしい味がしますね！巡り合えた気がします！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "え？今日今日結構食べたなって…そんなに食べてないような気がするのですが私の気のせいでしょうか…", R.drawable.hikari_think, shouldClear = true)
)

val script8_ShoulderBorrow = listOf(
    StoryLine(Speaker.HIKARI, "今日も楽しかったですね！そろそろ散歩するのも慣れてきたかもしれないです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "歩いた日の夜ってぐっすり眠れるんですよねーー。今までは何となく時間がもったいない気がして、別の趣味の時間に使うことが多かったのですが○○さんとなら続けられそうです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ちょっとふくらはぎが筋肉痛のような気がしますが、公園で元気に遊んでいる子供たちを見てるとそんなことも言ってられないですよねー。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さん！あのー…駅まで肩借りてもいいですか？…へへへ…", R.drawable.hikari_blush, shouldClear = true)
)

val script9_SendToStation = listOf(
    StoryLine(Speaker.HIKARI, "ごごめんなさい…こんなに歩いたのは久しぶりで、散歩が楽しくてはしゃいでしまったのもあって、○○さんにご迷惑をかけてしまうことになってしまいました…", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "今日も○○さん、楽しめていましたか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "……とりあえずその言葉がいただけただけで本当によかったです。○○さんっていざっていうときに頼りになりますよね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "そうですね…後は電車に乗って帰るだけので大丈夫です！家まで送ってもらうなんて本当に大丈夫ですから！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もしかして, 私の住所を突き止めようとしてます？嘘ですよ！冗談ですって！そんな顔しないでください…", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "とにかく、今日一日本当にありがとうございました！しばらくは散歩もできそうにないですが、またどこか歩きましょうね！絶対ですよー！", R.drawable.hikari_celebrate, shouldClear = true)
)

val script10_InRoom = listOf(
    StoryLine(Speaker.HIKARI, "今日の散歩はちょっと無理をしちゃったかな？足も痛くなっちゃったし…", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "独自に足のマッサージとかしてみたけど、やっぱりプロに診てもらほうがいいのかな…", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "カフェもいったし、商店街では…あんまり思い出せないけど、公園で公園で食べた揚げパンはすごくおいしかったー！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "歩いていれば、体も痩せてくると思うし、早く足を治してまたお散歩に行けるといいなー。", R.drawable.hikari_smile, shouldClear = true)
)

val script11_Progress = listOf(
    StoryLine(Speaker.HIKARI, "○○さんと一緒にいると, なぜだか楽しいんだよなー。 でも○○さんて, どんな人なんだろう？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "なんでかわからないけど, 二人でいてもあんまり緊張しないで過ごせるんだよね. それって, ああいう顔してるからかな。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "それとも、あの性格のおかげかも？ これからも散歩サークルで、一緒に歩けるように頑張って足を治さないと！", R.drawable.hikari_celebrate, shouldClear = true)
)

val script12_FullScaleStart = listOf(
    StoryLine(Speaker.HIKARI, "おはようございます！ すっかり足の方も良くなったので、また今日から歩けそうです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "散歩するのが楽しみで、早く治るように安静にしてたんですよ。 〇〇さんも、準備はよろしいですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "やっとこのサークルも本格始動ってところですね！ それじゃあレッツゴー！", R.drawable.hikari_celebrate, shouldClear = true)
)

val script13_ElectricTown1 = listOf(
    StoryLine(Speaker.HIKARI, "ここは、アニメとかゲームのお店がいっぱい並んでますね！ 見てください、ここなんかもフィギュアがいっぱい並んでますよ！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "これって、ゲームのキャラなんですか？このキャラかわいいですね！ 水着のこの子なんかはどう思いますか、〇〇さん？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "いま、〇〇さんが変なことを考えてると、私が思っているんじゃないかというふふうに、〇〇さんは思ったんじゃないですか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "大丈夫ですよ、安心してください！ いつも変な事を考えてそうな顔をしてますから。筒抜けです！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "冗談ですよ？", R.drawable.hikari_smile, shouldClear = true)
)

val script14_ElectricTown2 = listOf(
    StoryLine(Speaker.HIKARI, "ごごめんなさい、怒らせるつもりはなくて。 〇〇さんが, なんていうか、ちょっかいかけたくなっちゃう感じっていうか。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "リアクションがすっごく面白いんです！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（その言葉の中に、あまりに贖罪の意識が無いことに2人で笑った）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "今度は、申し訳ないのに、なんだか面白くて笑っちゃいました！ 〇〇さんも, そうやって笑うんですね。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "一緒に居て、面白い理由がわかりました！ 何が起こっても、寛大な気持ちで許してくれそうな気がするからですね。きっと…", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "そうこうしてるうちに、お腹空いてきちゃいましたね. どこか、奢っていただいてもいいでしょうか？ 冗談です。", R.drawable.hikari_blush, shouldClear = true)
)

val script15_Ramen = listOf(
    StoryLine(Speaker.HIKARI, "あそこのラーメン屋さんはどう思いますか？ 結構な有名なところなんですね。っていうことは〇〇さんはラーメンに詳しいんですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "へぇ、あのお店は豚骨醤油で有名なラーメン屋さんなんですか. じゃあせっかくなので食べてみたいです！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "こういうラーメン屋さんに, 女性が興味を持つのは結構珍しいんですか？ じゃあ, 〇〇さんが一緒で良かったですね！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もう、お腹ぺこぺこなので早く並んじゃいましょう. え、先に食券を買わないと並び直しになっちゃうんですか。", R.drawable.hikari_think, shouldClear = true)
)

val script16_Ramen2 = listOf(
    StoryLine(Speaker.NARRATION, "（店に入り、お好みはどうされますか、と聞かれ）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "今なんて言ったんですか、その呪文みたいなやつ. 私もそれがいいです. カタメ、コイメ、オオメ、って言うんですか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（決心したようにひかりが大きな声で言う）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "私もカタメ、コイメ、オオメで！ すみません、つい、張り切っちゃいました！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "次は、ライスも一緒に食べるかが重要なんですか？ それは、絶対に必要ですね！ はい、ライスもお願いします！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "任せてください！ もう、お腹ペコペコなので、箸まで食べれると思いますよ！", R.drawable.hikari_celebrate, shouldClear = true)
)

val script17_Ramen3 = listOf(
    StoryLine(Speaker.NARRATION, "（すっかりと、ラーメンとライスを平らげたひかりだった）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "すっごく美味しかったですね！ 女性でこれだけ食べられるのは珍しいですか？ 大丈夫です！ これぐらいは楽勝です！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "しかし、ニンニクを結構入れてしまったんですが、匂いは大丈夫でしょうか？ え、〇〇さんは、大丈夫かどうかですか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（顔を近づけてくるひかり）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "臭いますね…", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あの、ご飯にニンニクをかけて、スープに浸した海苔で巻いて食べるのが、良くなかったんでしょうか. あぁ、背徳的な食べ物に, 危険はつきものということなんでしょう…", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あれ、なんだか元気が湧いてきました！ これはお、ニンニク臭の効果でしょうか。", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（俺は、やっぱり臭うかな、とひかりに聞いた）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "〇〇さん、安心してください. 私たちは、カタメ、コイメ、オオメ、同盟ですからね！ ニンニク臭く頑張りましょう！", R.drawable.hikari_smile, shouldClear = true)
)

val script18_GameCenter = listOf(
    StoryLine(Speaker.HIKARI, "ゲームセンターって久しぶりに来ました！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さんってufoキャッチャーは得意ですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "せっかく来たので何かやりませんか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あ、○○さん、こっちに来てください！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ひかりの呼ぶほうに向かう）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "このぬいぐるみが, もう少しで落ちそうな角度になってます！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（犬ともクマとも言えないぬいぐるみが, 落とし穴に首を差し出している）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "もしかしたら、頭をアームで押せば取れるんじゃないですか？", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "頑張ってください、○○さん。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（言われた通りに動かしてみる）", shouldClear = true),
    StoryLine(Speaker.NARRATION, "（うまくアームは当たったが、頭に刺さっただけで、重心が移動せず、断頭台を見ている気分だ）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "うーん、ぬいぐるみが動きませんねー。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "じゃあ、お尻の方から引っ掛けてみたらどうですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（アームを少し, 落とし穴のほうにずらして、お尻に引っかかるようにする）", shouldClear = true),
    StoryLine(Speaker.NARRATION, "（お尻にうまく刺さったが、アームの設定が弱く、お尻の当たりを撫でては刺激するのみであった）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "この子は、なんだかわがままな子ですね！", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "最初は助けてもらいたそうな、かわいそうな子だと思ってましたけど。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "こんな風にされるのが好きなんじゃないかと、思えてきました。", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "そうにはですね、好きでやってるんだからほっときましょう！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "次行きましょう、次。", R.drawable.hikari_celebrate, shouldClear = true)
)

val script19_RhythmGame = listOf(
    StoryLine(Speaker.NARRATION, "（ぬいぐるみを諦めて歩いていると、音楽が鳴り響くエリアにたどり着く）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "あ、見てください○○さん！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "リズムゲームですよ、懐かしくないですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ひかりがパネルを軽く叩いてみる）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "私、こういうのちょっとだけ得意なんですよ。", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さんもやりましょうよ、協力プレイできるみたいです！", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（強引に隣に立たされる）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "じゃあ、私が右で、○○さんが左ですね。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ちゃんとついてきてくださいよ？", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ゲームスタート）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "あっ、そこ違います！右、右です！", R.drawable.hikari_think, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あ、レベルアップ！でも今のは私もミスりましたね。", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（だんだん距離が近くなる）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "ちょ、ちょっと近いですって！", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あ、でも、このくらいの方がやりやすいかもですね。", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（肩が軽く触れる）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "なんか, 変な感じですね。", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ゲームに集中しないといけないのに、別のこと気になっちゃって。", R.drawable.hikari_blush, shouldClear = true),
    StoryLine(Speaker.HIKARI, "いきますよ、ここラストです！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "せーのっ！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（なんとかクリアする）", shouldClear = true),
    StoryLine(Speaker.HIKARI, "やりましたね、○○さん！", R.drawable.hikari_celebrate, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ふふ、ちょっと息合ってきたんじゃないですか？", R.drawable.hikari_smile, shouldClear = true),
    StoryLine(Speaker.HIKARI, "さっきのぬいぐるみより、こっちの方が楽しかったですね。", R.drawable.hikari_smile, shouldClear = true)
)

val script20_Purikura = listOf(
    StoryLine(Speaker.NARRATION, "（リズムゲームのエリアを抜けると、明るい照明のブースが並んでいる）", shouldClear = true), // 1
    StoryLine(Speaker.HIKARI, "あ、○○さん、見てください、プリクラですよ！", R.drawable.hikari_smile, shouldClear = true), // 2
    StoryLine(Speaker.NARRATION, "（ひかりが興味ありげに近づく）", shouldClear = true), // 3
    StoryLine(Speaker.HIKARI, "最近のって、すごいんですよね。", R.drawable.hikari_smile, shouldClear = true), // 4
    StoryLine(Speaker.HIKARI, "勝手に盛ってくれるって聞いたことあります。", R.drawable.hikari_smile, shouldClear = true), // 5
    StoryLine(Speaker.HIKARI, "せっかくですし、やってみませんか？", R.drawable.hikari_smile, shouldClear = true), // 6
    StoryLine(Speaker.HIKARI, "記念にもなりますしね！", R.drawable.hikari_celebrate, shouldClear = true), // 7
    StoryLine(Speaker.NARRATION, "（半ば強引にブースの中へ引き込まれた）", shouldClear = true), // 8
    StoryLine(Speaker.HIKARI, "わ、思ったより狭いですね。", R.drawable.hikari_blush, shouldClear = true), // 9
    StoryLine(Speaker.HIKARI, "○○さん、もうちょっとこっち寄ってください。", R.drawable.hikari_blush, shouldClear = true), // 10
    StoryLine(Speaker.NARRATION, "（画面にカウントダウンが表示される）", shouldClear = true), // 11
    StoryLine(Speaker.HIKARI, "え、もう始まるんですか！？", R.drawable.hikari_think, shouldClear = true), // 12
    StoryLine(Speaker.HIKARI, "ちょっと待ってください、何も考えてないですって！", R.drawable.hikari_think, shouldClear = true), // 13
    StoryLine(Speaker.NARRATION, "（慌てたように俺の隣にひかりが立つ）", shouldClear = true), // 14
    StoryLine(Speaker.HIKARI, "とりあえず……普通にピースでいいですか？", R.drawable.hikari_smile, shouldClear = true), // 15
    StoryLine(Speaker.HIKARI, "はい、いきますよ！", R.drawable.hikari_smile, shouldClear = true), // 16
    StoryLine(Speaker.NARRATION, "（シャッター音が鳴る）", shouldClear = true), // 17
    StoryLine(Speaker.HIKARI, "あ、○○さんちょっと顔固くないですか？", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true), // 18: 背景切り替え
    StoryLine(Speaker.HIKARI, "もっとこう、自然な感じでいきましょうよ！", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true), // 19
    StoryLine(Speaker.NARRATION, "（次のカウントダウンがはじまる）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true), // 20
    StoryLine(Speaker.HIKARI, "あ、次はどうしましょう。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "じゃあ、ちょっとすみません。", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ひかりが少しだけ距離を詰める）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（その結果、肩が軽く触れる）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（シャッター音が鳴る）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "今の、ちょっとだけ、近かったですかね？", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（少しだけ気まずそうに笑う）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "いや、別にいいんですけどね。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "その方が画面に入りやすいですし。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（最後のカウントダウンがはじまった）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "これが、ラストですね。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "どうしますか？", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（少しひかりが考える）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "じゃあ、同時にピースで。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "タイミング合わせますよ？", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "せーの。", R.drawable.hikari_celebrate, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（シャッター音がなる）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "終わりましたね。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "どんな感じに写ってるんでしょう。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（2人で画面を覗き込んだ）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "え、なにこれ。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さんの目、すごく大きくなってません？", R.drawable.hikari_think, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あと、なんか……全体的にキラキラしてるような。", R.drawable.hikari_think, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（2人で少し笑いあう）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "これ、別人みたいですね！", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "でも、ちょっと笑っちゃいました。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "こういうのって、あとで見返したときに「ああ、こんなことあったな」って思い出しちゃうんですよね。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ひかりがシールを手に取る）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "はい、これ。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "○○さんの分です。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（少しだけひかりの差し出す手がゆっくりに見えた）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "なくさないでくださいね。", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "今日の、証拠みたいなものですから。", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（すぐにいつもの調子に戻る）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "あ、別に深い意味はないですよ？", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ただの記念です、記念。", R.drawable.hikari_smile, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（外に向かって歩き出す）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "ゲームセンター、思ってたより楽しかったですね！", R.drawable.hikari_celebrate, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "また来てもいいかもです。", R.drawable.hikari_celebrate, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.NARRATION, "（ゲームセンターの出口でひかりが少し振り向きながら言った）", backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "そのときは……", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true),
    StoryLine(Speaker.HIKARI, "もうちょっといい写真、撮りましょう。", R.drawable.hikari_blush, backgroundRes = R.drawable.purikura_cg_, shouldClear = true)
)

// ==========================================
// ★ ホーム画面のセリフ定義 ★
// ==========================================

// ラブ度0〜3で共通して使用する歩数セリフ
val commonStepDialogues = listOf(
    StepDialogue(0, "今日も一緒にお散歩しましょう！準備はいいですか？", R.drawable.hikari_smile),
    StepDialogue(1000, "お、1000歩ですね！順調な滑り出しですよ、○○さん！", R.drawable.hikari_smile),
    StepDialogue(3000, "もう3000歩も歩いたんですか！？○○さん、案外タフなんですね。", R.drawable.hikari_celebrate),
    StepDialogue(5000, "5000歩達成！いい感じです！あそこに見えるカフェで休憩しませんか？", R.drawable.hikari_smile),
    StepDialogue(8000, "すごいです、8000歩！今日はぐっすり眠れそうですね。えへへ。", R.drawable.hikari_blush),
    StepDialogue(10000, "やりました！10000歩の大台突破です！今日はもうパフェ食べてもいいですよね？", R.drawable.hikari_celebrate),
    StepDialogue(20000, "20000歩！？○○さん、もしかしてアスリートか何かですか…？尊敬しちゃいます！", R.drawable.hikari_celebrate),
    StepDialogue(30000, "30000歩…！もはや散歩の域を超えてますよ！今日はゆっくりお風呂に入って休んでくださいね。", R.drawable.hikari_smile)
)

// ラブ度別のタップセリフ
val touchDialoguesLv0 = listOf(
    TouchDialogue("あ、どうかしましたか？", R.drawable.hikari_smile),
    TouchDialogue("何かお探しですか？", R.drawable.hikari_smile),
    TouchDialogue("お散歩、楽しいですね！", R.drawable.hikari_smile)
)

val touchDialoguesLv1 = listOf(
    TouchDialogue("○○さん、なにか御用ですか？", R.drawable.hikari_smile),
    TouchDialogue("えへへ、なんだか照れちゃいますね。", R.drawable.hikari_blush),
    TouchDialogue("今日はいい天気でよかったですね！", R.drawable.hikari_smile)
)

val touchDialoguesLv2 = listOf(
    TouchDialogue("○○さんといると、時間が経つのが早いです！", R.drawable.hikari_smile),
    TouchDialogue("次はどこに行きましょうか？楽しみです！", R.drawable.hikari_celebrate),
    TouchDialogue("あの、少しだけ……手を繋いでもいいですか？なんちゃって！", R.drawable.hikari_blush)
)

val touchDialoguesLv3 = listOf(
    TouchDialogue("私、○○さんのこと……もっと知りたくなってきました。", R.drawable.hikari_blush),
    TouchDialogue("ずっと、一緒に歩いてくれますか？", R.drawable.hikari_smile),
    TouchDialogue("○○さんは、私の特別な人……かもしれませんね！", R.drawable.hikari_celebrate)
)

val loveContents = listOf(
    LoveContent(0, R.drawable.hikari_smile, commonStepDialogues, touchDialoguesLv0),
    LoveContent(1, R.drawable.hikari_smile, commonStepDialogues, touchDialoguesLv1),
    LoveContent(2, R.drawable.hikari_smile, commonStepDialogues, touchDialoguesLv2),
    LoveContent(3, R.drawable.hikari_smile, commonStepDialogues, touchDialoguesLv3),
    LoveContent(
        thresholdLove = 100,
        expressionRes = R.drawable.hikari_smile,
        stepDialogues = commonStepDialogues,
        touchDialogues = touchDialoguesLv3
    )
)

// --- 全エピソードのリスト ---
val mainStoryEpisodes = listOf(
    StoryEpisode(1, "運命の出会い？", R.drawable.street_background, script1_Encounter, 1, 0),
    StoryEpisode(2, "初めてのお散歩", R.drawable.street_background, script2_FirstWalk, 1, 1),
    StoryEpisode(3, "こだわりマスターのカフェ", R.drawable.cafe_background, script3_CafeMaster, 1, 2),
    StoryEpisode(4, "人間観察とパフェ", R.drawable.cafe_background, script4_CafeMaster2, 1, 3),
    StoryEpisode(5, "商店街の誘惑", R.drawable.street_background, script5_Croquette, 1, 4),
    StoryEpisode(6, "黄金の揚げパン", R.drawable.park_morning, script6_FriedBread, 1, 5),
    StoryEpisode(7, "揚げパンの記憶", R.drawable.park_morning, script7_MemoryLoss, 1, 6),
    StoryEpisode(8, "肩を貸して", R.drawable.street_background, script8_ShoulderBorrow, 1, 7),
    StoryEpisode(9, "駅までの道のり", R.drawable.street_background, script9_SendToStation, 1, 8),
    StoryEpisode(10, "お部屋での反省会", R.drawable.hikari_room_think_you_cg_, script10_InRoom, 1, 9),
    StoryEpisode(11, "気になる存在", R.drawable.hikari_room_think_you_cg_, script11_Progress, 2, 0),
    StoryEpisode(12, "本格始動！", R.drawable.street_background, script12_FullScaleStart, 2, 1),
    StoryEpisode(13, "電気街のフィギュア", R.drawable.street_background, script13_ElectricTown1, 2, 2),
    StoryEpisode(14, "笑い合える関係", R.drawable.street_background, script14_ElectricTown2, 2, 3),
    StoryEpisode(15, "行列のラーメン屋", R.drawable.ramen_shop, script15_Ramen, 2, 4),
    StoryEpisode(16, "呪文のコール", R.drawable.ramen_shop, script16_Ramen2, 2, 5),
    StoryEpisode(17, "ニンニク同盟", R.drawable.ramen_shop, script17_Ramen3, 2, 6),
    StoryEpisode(18, "ゲームセンターにて", R.drawable.game_center, script18_GameCenter, 2, 7),
    StoryEpisode(19, "ふたりのリズム", R.drawable.game_center, script19_RhythmGame, 2, 8),
    StoryEpisode(20, "ゲームセンターにて③（プリクラ編）", R.drawable.game_center, script20_Purikura, 2, 9)
)
