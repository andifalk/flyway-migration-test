package net.andifalk.flyway.migration;

import org.flywaydb.core.Flyway;

import net.andifalk.flyway.migration.test.AbstractDatabaseMigrationTest;

/**
 * DB migration test.
 */
public class HsqldbMigrationTest extends AbstractDatabaseMigrationTest {

    /**
     * Constructor
     * @throws Exception is not expected
     */
    public HsqldbMigrationTest () throws Exception {
        super ();
    }

    @Override
    protected String getDataLocation () {
        return "/db/data";
    }

    @Override
    protected Flyway getFlyway () {
        Flyway flyway = new Flyway ();
        flyway.setDataSource("jdbc:hsqldb:mem:testdb", "sa", "");
        flyway.setLocations ( "db/migration/hsqldb" );
        return flyway;
    }
}
