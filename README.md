# Integration testing for flyway database migrations
This git repo provides some base classes to help building integration tests for flyway migrations.

It enables to test all existing flyway migrations...
- ...with empty database
- ...with pre-loaded data

Doing it this way it reduces the risk of failing migrations by testing migrations without
pre-existing data and also including pre-existing data that has to be migrated as well.

## How to do it

1. Create corresponding data files (in db-unit xml format) for database migration scripts.
These should be named same as the migration script file but replacing the .sql or .java suffix with .xml suffix.
2. Create a concrete subclass of
``
AbstractDatabaseMigrationTest
``
and implement required abstract operations:

- __String getDataLocation()__: Returns location of data to be pre-loaded (db-unit format)
- __Flyway getFlyway()__: Returns an instance of the Flyway class for target database and migrations to be tested


