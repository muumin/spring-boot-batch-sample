Spring Boot Batch Sample
=====================================


# 動作

* 取込ファイルからデータを読み込む
* データをDBへ書き込み
* データをDBから読み込み
* データのメールアドレスにメールを送信

# 概要

* メール本文のテンプレートはVelocityを使用
    * テンプレートはDBから読み込んでいる
    * 初期データはflywayのマイグレーションファイル(db.migrationパッケージ)

# 実行

* 取込ファイルを作成

src/resources/sample-data.csv

    tarou,yamada,yamada@example.com
    hanako,yamada,yamada@example.com
    
* SMTP設定を変更
    * SMTP設定はsrc/resources/application.yml


プロジェクトのホームディレクトリで以下を実行

    gradlew
