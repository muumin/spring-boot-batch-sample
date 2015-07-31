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
    * 初期データはflywayのマイグレーションファイルを参照(db.migrationパッケージ)

# 実行

## 準備

* 取込ファイルを作成

src/resources/sample-data.csv

    tarou,yamada,yamada@example.com
    hanako,yamada,yamada@example.com
    
* SMTP設定を変更
    * SMTP設定はsrc/resources/application.yml

## 起動

プロジェクトのホームディレクトリで以下を実行

    gradlew bootRun -Pargs="-job sendMailJob"

もしくはbuild/libs/spring-boot-batch-sample.jarを直接起動

    java -jar spring-boot-batch-sample.jar -job sendMailJob

## エラー

エラー等でリスタートが必要な場合、以下を実行することで最新のエラーが発生した箇所から再実行される

    gradlew bootRun -Pargs="-job sendMailJob -restart"
    java -jar spring-boot-batch-sample.jar -job sendMailJob -restart

