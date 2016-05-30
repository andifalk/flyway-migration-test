package net.andifalk.flyway.migration;

import org.flywaydb.core.Flyway;

import net.andifalk.flyway.migration.test.AbstractDatabaseMigrationTest;

/**
 * DB migration test.
 */
public class PostgresqlMigrationTest extends AbstractDatabaseMigrationTest {
    
    /**
     * Constructor
     * @throws Exception is not expected
     */
    public PostgresqlMigrationTest () throws Exception {
        super ();
    }

    @Override
    protected String getDataLocation () {
        return "/db/data";
    }

    @Override
    protected Flyway getFlyway () {
        Flyway flyway = new Flyway ();
        flyway.setDataSource("jdbc:postgresql:testdb", "postgres", "postgres");
        flyway.setLocations ( "db/migration/postgresql" );
        return flyway;
    }
}
