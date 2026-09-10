package integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FlywayMigrationIntegrationTest {

    @Test
    void shouldExecuteTwoDatabaseMigrations() {

        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:h2:mem:uniroom_flyway;MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        ""
                )
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isEqualTo(2);

        assertThat(
                flyway.info().current().getVersion().getVersion()
        ).isEqualTo("2");

        System.out.println("Flyway migrations executed successfully.");
        System.out.println("Migrations executed: " + result.migrationsExecuted);
        System.out.println("Current database version: "
                + flyway.info().current().getVersion());

    }
}