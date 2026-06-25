# Javaソース調査レポート

- 調査日: 2026-06-25
- 調査対象: `core/src` と `lwjgl3/src` にあるプロジェクト管理下の `.java` ファイル
- 除外対象: `build`、`.gradle`、`.git`、生成物、キャッシュ、外部ライブラリ
- 集計ルール:
  - 総行数: ファイルの全行数
  - 空行を除いた行数: 空白行を除外した行数
  - 概算コード行数: 空行に加えて、行頭が `//`、`/*`、`*`、`*/` のコメント専用行を除外した概算値

## 1. 全体サマリー

| 項目 | 値 |
|---|---:|
| Javaファイル総数 | 18 |
| 総行数 | 6,436 |
| 空行を除いた総行数 | 5,669 |
| コメント専用行も除いた概算コード行数 | 5,427 |
| 1ファイル当たりの平均行数 | 357.56 |
| 1ファイル当たりの中央値 | 227.5 |
| 最小行数 | 13 |
| 最大行数 | 2,081 |
| 最大ファイル | `core/src/main/java/io/github/shunsuke/paintgame/threed/Main3D.java` |
| 最小ファイル | `core/src/main/java/io/github/shunsuke/paintgame/threed/GameFlowState.java` |

## 2. モジュール別集計

| モジュール | ファイル数 | 総行数 |
|---|---:|---:|
| `core` | 16 | 6,142 |
| `lwjgl3` | 2 | 294 |

## 3. 2D版・3D版の規模比較

| 区分 | ファイル数 | 総行数 |
|---|---:|---:|
| 2D版 | 1 | 633 |
| 3D版 | 15 | 5,509 |
| ランチャー・補助 | 2 | 294 |

- 3D版は2D版に対して `+4,876行`、`約8.70倍` に拡大しています。
- 全Java行数のうち、3D版が占める割合は約 `85.6%` です。

## 4. 行数が多いJavaファイル 上位10件

| 順位 | ファイル名 | 相対パス | 区分 | 総行数 |
|---|---|---|---|---:|
| 1 | Main3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Main3D.java | 3D版 | 2,081 |
| 2 | Player3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Player3D.java | 3D版 | 767 |
| 3 | EnemyCpu3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/EnemyCpu3D.java | 3D版 | 735 |
| 4 | Main.java | core/src/main/java/io/github/shunsuke/paintgame/Main.java | 2D版 | 633 |
| 5 | StageObstacles3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/StageObstacles3D.java | 3D版 | 503 |
| 6 | FloorGrid3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/FloorGrid3D.java | 3D版 | 321 |
| 7 | AudioManager3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/AudioManager3D.java | 3D版 | 239 |
| 8 | StageConfig3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/StageConfig3D.java | 3D版 | 235 |
| 9 | Bullet3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Bullet3D.java | 3D版 | 228 |
| 10 | StartupHelper.java | lwjgl3/src/main/java/io/github/shunsuke/paintgame/lwjgl3/StartupHelper.java | 設定・補助 | 227 |

## 5. Javaファイル一覧

| No. | ファイル名 | 相対パス | 区分 | 総行数 | 空行を除いた行数 | 主な役割 |
|---|---|---|---|---:|---:|---|
| 1 | Main.java | core/src/main/java/io/github/shunsuke/paintgame/Main.java | 2D版 | 633 | 548 | 2Dプロトタイプ本体です。プレイヤー移動、弾発射、床グリッド塗り、CPU、制限時間、HUDまでを1クラスでまとめて管理しています。 |
| 2 | AudioManager3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/AudioManager3D.java | 3D版 | 239 | 205 | 3D版の効果音とBGMを読み込み、再生、ミュート、Pause時の音量調整を安全に扱う音声管理クラスです。 |
| 3 | Bullet3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Bullet3D.java | 3D版 | 228 | 195 | 3D弾の位置更新、射程管理、床・壁への塗り判定に使う情報保持、着弾位置の記録を担当します。 |
| 4 | CharacterShadow3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/CharacterShadow3D.java | 3D版 | 95 | 83 | キャラクター足元の簡易影を3Dモデルとして生成し、地面に沿った位置と大きさへ更新して描画します。 |
| 5 | CpuDifficulty3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/CpuDifficulty3D.java | 3D版 | 81 | 69 | Easy、Normal、HardごとのCPUパラメータをまとめ、射程、精度、反応、移動傾向などの難易度差を定義します。 |
| 6 | EnemyCpu3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/EnemyCpu3D.java | 3D版 | 735 | 652 | CPUキャラクター1体分のAI、本体描画、移動、射撃、被弾、撃破、リスポーンを一括管理します。味方CPUと敵CPUの共通土台にもなっています。 |
| 7 | FloorGrid3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/FloorGrid3D.java | 3D版 | 321 | 265 | 3D床タイルの生成と描画、塗り状態の保持、塗り数と塗り率の集計、塗った直後のハイライト演出を担当します。 |
| 8 | GameFlowState.java | core/src/main/java/io/github/shunsuke/paintgame/threed/GameFlowState.java | 3D版 | 13 | 12 | 3D版の進行状態を表す列挙型です。タイトル、カウントダウン、プレイ中、Pause、Game Overを切り替える基礎になります。 |
| 9 | InkParticle3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/InkParticle3D.java | 3D版 | 58 | 47 | 着弾時などに使う簡易インク粒子の位置、速度、寿命、サイズを持つ軽量データクラスです。 |
| 10 | Main3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Main3D.java | 3D版 | 2,081 | 1,872 | 3D版全体の司令塔です。メニュー、ゲーム進行、入力、カメラ、HUD、弾更新、CPU更新、勝敗判定、ミニマップ、音まで統括しています。 |
| 11 | Player3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Player3D.java | 3D版 | 767 | 672 | プレイヤー本体の移動、ジャンプ、潜伏、壁登り、インク管理、被弾、リスポーン、見た目の更新を担当します。 |
| 12 | StageConfig3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/StageConfig3D.java | 3D版 | 235 | 200 | Training Stage、Wide Arena、Team Arena の床サイズ、障害物定義、スポーン位置をまとめるステージ設定クラスです。 |
| 13 | StageObstacles3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/StageObstacles3D.java | 3D版 | 503 | 454 | 障害物、足場、外周壁、スポーンパッドの生成と描画、衝突判定、足場接地判定、壁塗り状態の管理を担当します。 |
| 14 | StageType3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/StageType3D.java | 3D版 | 20 | 16 | 3D版で選べるステージ種別と表示名を表す列挙型です。タイトルメニューのステージ選択で使われます。 |
| 15 | Team3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/Team3D.java | 3D版 | 40 | 32 | PlayerチームとEnemyチームの塗り色IDや対戦相手判定をまとめ、床色判定や弾の所属判定に使います。 |
| 16 | WeaponConfig3D.java | core/src/main/java/io/github/shunsuke/paintgame/threed/WeaponConfig3D.java | 3D版 | 93 | 83 | 武器名、射程、弾速、塗り半径、連射間隔、インク消費量を持ち、複数の武器プリセットを提供します。 |
| 17 | Lwjgl3Launcher.java | lwjgl3/src/main/java/io/github/shunsuke/paintgame/lwjgl3/Lwjgl3Launcher.java | デスクトップランチャー | 67 | 58 | デスクトップ起動用のエントリポイントです。起動引数で2D版と3D版を切り替え、LWJGL3のウィンドウ設定を行います。 |
| 18 | StartupHelper.java | lwjgl3/src/main/java/io/github/shunsuke/paintgame/lwjgl3/StartupHelper.java | 設定・補助 | 227 | 206 | OSごとの起動補助を行います。macOSの first thread 問題や Windows/Linux の LWJGL3 起動互換処理を担当します。 |

## 6. 3D版の役割別整理

| 役割カテゴリ | 該当ファイル |
|---|---|
| ゲーム全体管理 | `Main3D.java` |
| プレイヤー | `Player3D.java` |
| CPU・AI | `EnemyCpu3D.java` |
| 弾・武器 | `Bullet3D.java`, `WeaponConfig3D.java` |
| 床塗り | `FloorGrid3D.java` |
| ステージ・障害物 | `StageConfig3D.java`, `StageObstacles3D.java`, `StageType3D.java` |
| UI・ゲーム状態 | `GameFlowState.java` |
| 音声 | `AudioManager3D.java` |
| エフェクト | `InkParticle3D.java`, `CharacterShadow3D.java` |
| チーム・難易度・設定 | `Team3D.java`, `CpuDifficulty3D.java` |

## 7. 発表用要約

### 7-1. 代表的なファイル

- `Main.java`: 2D版の試作全体を1クラスにまとめた初期プロトタイプです。
- `Main3D.java`: 3D版の入力、画面遷移、HUD、弾、CPU、結果表示までを統括する中核クラスです。
- `Player3D.java`: 移動、ジャンプ、潜伏、壁登り、インク、被弾などプレイヤー体験の中心を担います。
- `EnemyCpu3D.java`: 味方CPUと敵CPUに共通で使われるAIと戦闘ロジックの中核です。
- `FloorGrid3D.java`: 塗り状態の保持と面積集計を担い、ゲームルールの中心に近いクラスです。
- `StageObstacles3D.java`: 障害物、足場、外周、壁塗り、接地判定を一手に引き受けています。
- `WeaponConfig3D.java`: 複数武器のパラメータを一元管理し、バランス調整の起点になっています。
- `AudioManager3D.java`: 効果音とBGMをまとめて扱い、見た目だけでなく演出面のまとまりも支えています。

### 7-2. コード構造上の特徴

- 2D版は `Main.java` 1ファイルに主要機能を集約した、初心者向けで追いやすい構成です。
- 3D版は `Player3D`、`EnemyCpu3D`、`FloorGrid3D`、`StageObstacles3D` など役割別に分割され、機能拡張に合わせてクラス数が増えています。
- 武器、難易度、チーム、ステージ種別は `enum` や設定クラスへ切り出され、数値調整しやすい構成になっています。
- 一方で `Main3D.java` は 2,081行で全体の約32.3% を占めており、処理集中が最も大きいファイルです。
- `Main3D.java`、`Player3D.java`、`EnemyCpu3D.java` の上位3ファイルだけで全体の約55.7% を占めており、今後さらに機能追加するなら分割余地があります。

### 7-3. スライドに貼りやすい短い箇条書き

- Javaソースは合計 `18ファイル / 6,436行`、空行除外で `5,669行`
- 2D版は `1ファイル / 633行`、3D版は `15ファイル / 5,509行`
- 3D版は2D版比で `約8.70倍`、`+4,876行` に拡大
- 最大ファイルは `Main3D.java (2,081行)` で、全体の `32.3%`
- 3D版は「プレイヤー」「CPU」「床塗り」「障害物」「武器」「音」「UI」に分割済み
- ただし処理は `Main3D.java`、`Player3D.java`、`EnemyCpu3D.java` に集中しやすい

## 8. 集計上の注意

- コメント専用行を除いた「概算コード行数」は、簡易ルールで数えた概算値です。
- 行頭が `//`、`/*`、`*`、`*/` の行をコメント専用行として除外しています。
- インラインコメントを含むコード行はコードとして数えています。
- `StartupHelper.java` はゲームプレイ処理ではなく起動補助ですが、プロジェクト管理下のJavaソースであるため集計対象に含めています。
