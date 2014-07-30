Spring Boot Batch Sample
=====================================


# 動作

* sample-data.csvからデータを読み込む
* データをDBへ書き込み
* データをDBから読み込み
* データのメールアドレスにメールを送信

# 概要


* メール本文のテンプレートはVelocityを使用
    * テンプレートはDBから読み込んでいる
    * 初期データはsrc/resources/data-hsqldb.sql
    * 本文はBLOB型なので16進ダンプに変更する必要がある
        * [16進ダンプ参考サイト](http://d.hatena.ne.jp/oitomo/20090929/1254253786)

# 実行

* sample-data.csvを作成

src/resources/sample-data.csv

    tarou,yamada,yamada@example.com
    hanako,yamada,yamada@example.com
    
* SMTP設定を変更
    * SMTP設定はsrc/resources/application.yml


プロジェクトのホームディレクトリで以下を実行

    gradlew
