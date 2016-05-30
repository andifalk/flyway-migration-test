package net.andifalk.flyway.migration;

import org.flywaydb.core.Flyway;

import net.andifalk.flyway.migration.test.AbstractDatabaseMigrationTest;

/**
 * DB migration test.
 */
public class H2MigrationTest extends AbstractDatabaseMigrationTest {

    /**
     * Constructor
     * @throws Exception is not expected
     */
    public H2MigrationTest () throws Exception {
        super ();
    }

    @Override
    protected String getDataLocation () {
        return "/db/data";
    }

    @Override
    protected Flyway getFlyway () {
        Flyway flyway = new Flyway ();
        flyway.setDataSource("jdbc:h2:file:./test", "sa", "");
        flyway.setLocations ( "db/migration/h2" );
        flyway.setSchemas ( "PUBLIC" );
        return flyway;
    }
}
