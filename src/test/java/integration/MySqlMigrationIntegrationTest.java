package integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlMigrationIntegrationTest {

    @Test
    void shouldExecuteMigrationsOnRealMySqlDatabase() {

        String password = System.getenv("UNIROOM_DB_PASSWORD");

        assertThat(password)
                .as("UNIROOM_DB_PASSWORD environment variable")
                .isNotBlank();

        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:mysql://localhost:3306/uniroom_db"
                                + "?useSSL=false"
                                + "&allowPublicKeyRetrieval=true"
                                + "&serverTimezone=UTC",
                        "root",
                        password
                )
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();

        assertThat(result.success).isTrue();

        System.out.println("MySQL Flyway migration completed successfully.");
        System.out.println("Migrations executed: " + result.migrationsExecuted);
        System.out.println("Current database version: "
                + flyway.info().current().getVersion());
    }
}