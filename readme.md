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

テスト用には[FakeSMTP](http://nilhcem.github.io/FakeSMTP/index.html)が便利です。

## コンパイル&テスト

プロジェクトのホームディレクトリで以下を実行

    gradlew

## 起動

プロジェクトのホームディレクトリで以下を実行

    gradlew bootRun -Pargs="-job sendMailJob"

もしくはbuild/libs/spring-boot-batch-sample.jarを直接起動

    java -jar spring-boot-batch-sample.jar -job sendMailJob

## エラー

エラー等でリスタートが必要な場合、以下を実行することで最新のエラーが発生した箇所から再実行される

    gradlew bootRun -Pargs="-job sendMailJob -restart"
    java -jar spring-boot-batch-sample.jar -job sendMailJob -restart

# Spring-Boot-Batchのコマンドライン起動

## デフォルトコマンドライン実行

このサンプルではCommandLineRunnerを独自実装してコマンドライン実行をしています。

SpringBoot-Batchではデフォルトでコマンドラインから実行する方法(JobLauncherCommandLineRunner)があります。

### 実行手順

* application.ymlのspring.batch.job.enabledをtrueに変更
* sample.CommandLineBatchの@Componentをコメントアウト（DI対象外）
* 「gradlew -x test」でビルド(失敗するのでテストはスキップ)

以下を実行すると@EnableBatchProcessingで登録されている全てのjob(sendMailJob, conditionalJob)が実行されます。

    java -jar spring-boot-batch-sample.jar time(long)=1

以下でJob名を個別指定して実行も可能です。

    java -jar spring-boot-batch-sample.jar --spring.batch.job.names=sendMailJob time(long)=1

spring.batch.job.namesはカンマ区切りで複数のJobの指定が可能です。

    java -jar spring-boot-batch-sample.jar --spring.batch.job.names=sendMailJob,conditionalJob time(long)=1

### 引数のパラメータ

プロパティ(例では「time(long)=1」部分)は以下で型指定が可能です。

* (long)
* (string)
* (date)
* (double)

JobLauncherCommandLineRunnerで実行されたjobはパラメータとしてrun.idが付加されます。

run.idはバッチ実行毎にインクリメントされるのでSpringBatchの「同パラメータのJobは再実行されない」が回避されることになります。

[Configuring and Running a Job](http://docs.spring.io/spring-batch/trunk/reference/html/configureJob.html#restartability)

### エラーJobの再実行

直前の実行結果がエラー(STOP or FAILED)の場合でパラメータが同じ場合は再実行されます。

例として「time(long)=1」で起動し、insertDataStepでエラーが発生した場合

    taskletlStep -> insertDataStep（エラー）-> sendMailStep

* 再度「time(long)=1」パラメータで実行
    * run.idは同じでinsertDataStepのエラーになった箇所から再開
* 「time(long)=2」パラメータで実行
    * run.idは同じだがtaskletlStepから新規で起動
