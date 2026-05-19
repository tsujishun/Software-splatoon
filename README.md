# paintgame

libGDX で作っている、初心者向けのペイントアクション試作プロジェクトです。  
このリポジトリには **2D版** と **3D版** の両方が入っています。

- 2D版: 最初のルール検証用プロトタイプ
- 3D版: 現在のメインデモ。タイトルメニュー、武器選択、ステージ選択、CPU敵、床塗り、壁塗り、撃破、リスポーン、インク、潜伏、ジャンプ、壁登り、リザルト、ミニマップまで入っています

## 必要環境

- Windows + PowerShell
- Java が使える環境
- Gradle Wrapper は同梱済みなので、別途 Gradle を入れなくても起動できます

このプロジェクトは少なくとも **JDK 22** でビルド確認しています。

## フォルダ概要

- `core`: ゲーム本体のロジック
- `lwjgl3`: Desktop 起動用モジュール
- `assets`: 画像・音などのアセット
- `tools`: 仮音生成スクリプトなどの補助ツール

## 2D版と3D版の違い

### 2D版

- libGDX の 2D 見下ろし試作
- シンプルな床塗り、弾、CPU敵、スコア確認向け
- 軽くルールを確認したいとき向け

### 3D版

- 現在のメイン試作
- 三人称視点
- 複数武器
- ステージ選択
- CPU難易度
- 壁塗り、壁登り
- 被弾、撃破、リスポーン
- 仮効果音、仮BGM
- リザルト画面
- ミニマップ

## すぐ起動する方法

### 2D版を起動

```powershell
.\gradlew.bat lwjgl3:run
```

または:

```powershell
.\run_2d.bat
```

### 3D版を起動

```powershell
.\gradlew.bat lwjgl3:run --args="3d"
```

または:

```powershell
.\run_3d.bat
```

## ビルド方法

### プロジェクト全体をビルド

```powershell
.\gradlew.bat build
```

### 3D版の runnable jar を作る

```powershell
.\gradlew.bat lwjgl3:jar
```

または:

```powershell
.\build_3d_jar.bat
```

出力先:

```text
lwjgl3/build/libs/paintgame-1.0.0.jar
```

この jar は 2D版・3D版の両方に対応していて、引数なしなら 2D版、`3d` を付けると 3D版で起動します。

```powershell
java -jar lwjgl3/build/libs/paintgame-1.0.0.jar
java -jar lwjgl3/build/libs/paintgame-1.0.0.jar 3d
```

### Windows向けに少し軽い jar を作る

```powershell
.\gradlew.bat lwjgl3:jarWin
```

### zip 配布物を作る

```powershell
.\gradlew.bat lwjgl3:distZip
```

### アプリ配布用パッケージ候補

Gradle タスクとして次も存在します。

```powershell
.\gradlew.bat lwjgl3:packageWinX64
```

ただしこれは `construo` を使う配布向けタスクで、JDK の追加ダウンロードなどが入るため、**通常の動作確認より重い**です。  
まずは `lwjgl3:jar` または `lwjgl3:jarWin` を使うのがおすすめです。

## 3D版のタイトルメニュー

タイトル画面では次を選べます。

- `Start Game`
- `Weapon Select`
- `Stage Select`
- `Difficulty`
- `Controls`

操作:

- `W / S` または `↑ / ↓`: 項目移動
- `Enter`: 決定
- `Esc` または `Backspace`: 1つ戻る
- `M`: ミュート切り替え

## 3D版の操作方法

### プレイ中の基本操作

- `W / A / S / D`: 移動
- `Mouse`: カメラ操作
- `Space`: 射撃
- `1 / 2 / 3`: 武器切り替え
- `Shift`: 自分色の床で潜伏 / 自分色の壁で壁登り
- `J`: ジャンプ / 壁ジャンプ
- `Esc`: Pause
- `M`: ミュート切り替え
- `R`: タイトルへ戻る

### Pause 中

- `Esc` または `Enter`: 再開
- `R`: タイトルへ戻る

### Game Over / Result 中

- `Enter`: 同じステージ・武器・難易度で再試合
- `R`: タイトルへ戻る

## 武器説明

### Basic Shooter

- 標準的なバランス
- 迷ったらこれ

### Short Painter

- 近距離向け
- 連射が速い
- 塗り半径が広め
- インク消費はやや重め

### Long Shooter

- 遠距離向け
- 射程が長い
- 連射は遅め
- 塗り半径は小さめ
- インク消費はやや重め

## ステージ説明

### Training Stage

- 既存の基本ステージ
- 小さめで確認しやすい
- 機能テスト向け

### Wide Arena

- 広めのステージ
- 中央に遮蔽物あり
- 左右に低い足場あり
- 塗って登れる壁あり
- ミニマップを使った位置確認がしやすい

## CPU難易度

### Easy

- 射撃間隔が長め
- エイム追従が弱め
- 動きも少し遅め

### Normal

- 標準的な設定

### Hard

- 射撃間隔が短め
- エイム追従が強め
- 少し速く動く

## ミニマップ

- 3D版ではプレイ中に右下へ簡易ミニマップが表示されます
- 床の塗り状態を確認できます
- プレイヤー位置とCPU敵位置も表示されます
- `Training Stage` と `Wide Arena` の両方に対応しています

## 仮効果音・仮BGMについて

`assets/audio` にある音は、**開発確認用の仮音**です。  
外部素材ではなく、スクリプトで生成した短いビープ音・ループ音を使っています。

- 効果音
  - `player_shoot.wav`
  - `enemy_shoot.wav`
  - `hit.wav`
  - `player_splatted.wav`
  - `enemy_splatted.wav`
  - `countdown_beep.wav`
  - `countdown_go.wav`
  - `game_over.wav`
  - `weapon_switch.wav`
  - `ink_empty.wav`
- BGM
  - `title_bgm.wav`
  - `battle_bgm.wav`
  - `result_bgm.wav`

## 仮音の再生成方法

PowerShell 版:

```powershell
powershell -ExecutionPolicy Bypass -File tools/generate_placeholder_sounds.ps1
```

Python 版:

```powershell
python tools/generate_placeholder_sounds.py
```

## よく使うコマンドまとめ

```powershell
.\gradlew.bat build
.\gradlew.bat lwjgl3:run
.\gradlew.bat lwjgl3:run --args="3d"
.\gradlew.bat lwjgl3:jar
.\gradlew.bat lwjgl3:jarWin
.\gradlew.bat lwjgl3:distZip
```

## 既知の制限

- 3D版のサウンドはまだ仮音です
- CPU敵はまだ単純なルールベースAIです
- オンライン対戦はありません
- 高度なシェーダーや本格エフェクトはまだありません
- 壁塗りは簡易実装で、面ごとの厳密な塗り分けではありません
- 壁登りや足場処理も、現在は初心者向けの簡易仕様です
- バランスはまだ調整途中です

## 開発メモ

- 2D版は残してあり、3D版とは別に起動できます
- 3D版の本体入口は `core/src/main/java/io/github/shunsuke/paintgame/threed/Main3D.java`
- Desktop ランチャーは `lwjgl3/src/main/java/io/github/shunsuke/paintgame/lwjgl3/Lwjgl3Launcher.java`
