package db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V01_00_01__Insert_Data implements SpringJdbcMigration {
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        byte[] body1 = "${person.firstName} ${person.lastName}さん\n\nこんにちは！！\n\n\n".getBytes();
//        String body1 = DatatypeConverter.printHexBinary("${person.firstName} ${person.lastName}さん\n\nこんにちは！！\n\n\n".getBytes());

        String sql = "INSERT INTO VELOCITY_TEMPLATE  (ID, DEF, LAST_MODIFIED) VALUES (?,?,NOW())";
        jdbcTemplate.update(sql, "testTemplate1", body1);
    }
}
