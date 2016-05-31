package net.andifalk.flyway.migration.test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.Before;
import org.junit.Test;

import net.andifalk.flyway.migration.callback.DatabaseMigrationDataLoader;

/**
 * Unit test for simple App.
 */
public abstract class AbstractDatabaseMigrationTest {

    private Flyway flyway;

    /**
     * Create the test case.
     *
     * @throws Exception is not expected
     */
    public AbstractDatabaseMigrationTest() throws Exception {
        super();
        this.flyway = getFlyway();
    }

    @Before
    public final void setup() {
        this.flyway.setCallbacks();
        this.flyway.clean();
    }

    /**
     * Verify all database migrations with contained test data.
     * @throws Exception is not expected
     */
    @Test
    public void verifyDatabaseMigrationsWithoutData() throws Exception {

        MigrationInfoService info = this.flyway.info();
        int pendingMigrations = info.pending().length;
        assertThat( "Should have zero applied migrations before running the test", info.applied ().length, is ( 0 ) );
        assertThat( "Should have at least one pending migration before running the test", pendingMigrations, is ( greaterThan ( 0 ) ) );
        this.flyway.migrate();
        info = this.flyway.info();
        assertThat( "Should have expected applied migrations after running the test", info.applied ().length, is ( pendingMigrations ) );
        assertThat( "Should have at no pending migration after running the test", info.pending ().length, is ( 0 ) );
    }


    /**
     * Verify all database migrations with contained test data.
     * @throws Exception is not expected
     */
    @Test
    public void verifyDatabaseMigrationsWithData() throws Exception {

        flyway.setCallbacks ( new DatabaseMigrationDataLoader ( getDataLocation () ) );

        MigrationInfoService info = flyway.info ();
        int pendingMigrations = info.pending ().length;
        assertThat ( "Should have zero applied migrations before running the test", info.applied ().length, is ( 0 ) );
        assertThat ( "Should have at least one pending migration before running the test", pendingMigrations, is ( greaterThan ( 0 ) ) );
        flyway.migrate ();
        info = flyway.info ();
        assertThat ( "Should have expected applied migrations after running the test", info.applied ().length, is ( pendingMigrations ) );
        assertThat ( "Should have at no pending migration after running the test", info.pending ().length, is ( 0 ) );
    }

    protected abstract String getDataLocation();

    protected abstract Flyway getFlyway();
}
