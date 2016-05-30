package net.andifalk.flyway.migration.callback;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.DefaultOperationListener;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.ext.db2.Db2DataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.util.fileloader.FullXmlDataFileLoader;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.BaseFlywayCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseMigrationDataLoader extends BaseFlywayCallback {
    private static final Logger LOG = LoggerFactory.getLogger ( DatabaseMigrationDataLoader.class );

    private String location = "";
    
    /**
     * Standard constructor with location for dbunit data files.
     * @param location the location for dbunit files
     * @throws Exception 
     */
    public DatabaseMigrationDataLoader ( String location ) throws Exception {
        this.location = location;
    }

    @Override
    public void beforeMigrate ( Connection connection ) {
        try {
            DatabaseMetaData metaData = connection.getMetaData ();
            String databaseProductName = metaData.getDatabaseProductName();
            if (databaseProductName.startsWith("H2")) {
                connection.setAutoCommit ( true );
            }
        } catch ( SQLException e ) {
            e.printStackTrace ();
        }

    }

    @Override
    public void beforeEachMigrate ( Connection connection, MigrationInfo info ) {
        LOG.debug ( "beforeEachMigrate" );
        
        try {
            String scriptName = info.getScript ();
            int sqlExtPos = scriptName.toLowerCase ().indexOf ( ".sql" );
            int javaExtPos = scriptName.toLowerCase ().indexOf ( ".java" );
            int extPos = sqlExtPos > 0 ? sqlExtPos : javaExtPos;
            scriptName = scriptName.substring ( 0, extPos );
            IDataSet dataSetForTest = getDataSetForTest(scriptName);
            if ( dataSetForTest != null) {
                IDatabaseTester databaseTester = getDatabaseTester(connection);
                databaseTester.setSetUpOperation( getSetUpOperation() );
                databaseTester.setTearDownOperation ( getTearDownOperation() );
                databaseTester.setDataSet( dataSetForTest );
                databaseTester.setOperationListener(getOperationListener());
                databaseTester.onSetup();
            }
        } catch ( Exception e ) {
            LOG.error ( "Error loading database data", e );
        }
    }

    private IDataSet getDataSetForTest( String migrationScriptName) {
        IDataSet iDataSet = null;
        FullXmlDataFileLoader fullXmlDataFileLoader = new FullXmlDataFileLoader();
        String dataFilePath = String.format ( "%s/%s.xml", location, migrationScriptName);
        try {
            iDataSet = fullXmlDataFileLoader.load(dataFilePath);
            LOG.info ( "Successfully loaded data file {} for db migration", dataFilePath );
        } catch (DatabaseUnitRuntimeException e) {
            LOG.warn ( "No data file {} found for migration {}", dataFilePath, migrationScriptName );
        }
        return iDataSet;
    }
    
    private IDatabaseTester getDatabaseTester(Connection connection) throws Exception {
        return new DefaultDatabaseTester ( new DatabaseConnection ( connection ) );
    }
    
    private DatabaseOperation getSetUpOperation() throws Exception
    {
        return DatabaseOperation.CLEAN_INSERT;
    }

    private DatabaseOperation getTearDownOperation() throws Exception
    {
        return DatabaseOperation.NONE;
    }

    private IOperationListener getOperationListener()
    {
        return new DefaultOperationListener() {
            public void connectionRetrieved(IDatabaseConnection connection) {
                super.connectionRetrieved(connection);
                setUpDatabaseConfig(connection);
            }
            @Override
            public void operationSetUpFinished ( IDatabaseConnection connection ) {
                LOG.info ( "Successfully loaded data in database" );
            }
            
            @Override
            public void operationTearDownFinished ( IDatabaseConnection connection ) {
            }
        };
    }
    
    /**
     * Designed to be overridden by subclasses in order to set additional configuration
     * parameters for the {@link IDatabaseConnection}.
     * @param connection The settings of the current {@link IDatabaseConnection} to be configured
     */
    private void setUpDatabaseConfig(IDatabaseConnection connection) {
        String databaseProductName;
        try {
            DatabaseMetaData databaseMetaData = connection.getConnection ().getMetaData();
            if (databaseMetaData == null) {
                throw new RuntimeException("Unable to read database metadata while it is null!");
            }

            databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new RuntimeException("Unable to determine database. Product name is null.");
            }

            int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
            int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();

            databaseProductName = databaseProductName + " " + databaseMajorVersion + "." + databaseMinorVersion;
            LOG.info("Using database: " + getJdbcUrl(connection.getConnection ()) + " (" + databaseProductName + ")");

        } catch (SQLException e) {
            throw new RuntimeException("Error while determining database product name", e);
        }

        if (databaseProductName.startsWith("Apache Derby")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new DefaultDataTypeFactory());
        } else if (databaseProductName.startsWith("SQLite")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new DefaultDataTypeFactory());
        } else if (databaseProductName.startsWith("H2")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        } else if (databaseProductName.contains("HSQL Database Engine")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
        } else if (databaseProductName.startsWith("Microsoft SQL Server")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (databaseProductName.contains("MySQL")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        } else if (databaseProductName.startsWith("Oracle")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        } else if (databaseProductName.startsWith("PostgreSQL")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else if (databaseProductName.startsWith("DB2")) {
            connection.getConfig ().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Db2DataTypeFactory());
        } else {
            throw new RuntimeException ( "Unsupported database: " + databaseProductName );
        }
    }
    
    /**
     * Retrieves the Jdbc Url for this connection.
     *
     * @param connection The Jdbc connection.
     * @return The Jdbc Url.
     */
    private String getJdbcUrl(Connection connection) {
        try {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve the Jdbc connection Url!", e);
        }
    }

}
