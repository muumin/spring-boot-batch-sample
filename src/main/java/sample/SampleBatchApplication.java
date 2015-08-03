package sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SampleBatchApplication {
    public static void main(String[] args) {
        try {
            System.exit(SpringApplication.exit(SpringApplication.run(SampleBatchApplication.class, args)));
        } catch (Exception ex) {
            log.error("critical error!!", ex);
            System.out.println("致命的なエラーが発生しました。詳細はログを確認して下さい。");
            System.exit(1);
        }
    }

}
