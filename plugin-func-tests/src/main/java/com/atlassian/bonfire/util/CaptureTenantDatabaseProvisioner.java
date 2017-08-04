package com.atlassian.bonfire.util;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Inject;
import com.atlassian.jira.functest.framework.backdoor.upgrade.UpgradeControl;
import com.atlassian.jira.functest.rule.data.SqlDataImporter;
import com.atlassian.jira.functest.rule.tenant.DatabaseManager;
import com.atlassian.jira.functest.rule.tenant.TenantConnectionProvider;
import com.atlassian.jira.functest.rule.tenant.TestDatabase;
import com.atlassian.jira.functest.rule.tenant.TestTenantContext;
import com.atlassian.jira.testkit.client.TenantIdFilter;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.util.concurrent.LazyReference;

/**
 * This class provisions the database for the current tenant associated with a running test.
 * It is a psuedo-copy of com.atlassian.jira.functest.rule.tenant.StaticTenantDatabaseProvisioner,
 * refactored to work with Capture.
 */
public class CaptureTenantDatabaseProvisioner implements TenantConnectionProvider {
    public static final String TEST_TENANT_NAME = "jira";

    private static final Logger log = LoggerFactory.getLogger(CaptureTenantDatabaseProvisioner.class);

    private static final Integer TEST_PORT = Integer.getInteger("datasource.port", 15433);
    private static final String TEST_HOST = System.getProperty("datasource.host", "localhost");
    private static final String DB_ADMIN_USER = "jira";
    private static final String DB_ADMIN_PASSWORD = "jira";


    private static final String SCHEMA_FILE = "schema-only-dump.sql";
    private static final String DEFAULT_SCHEMA_PATH = System.getProperty("sql.schema.directory", "/sql");
    private static final String DEFAULT_DATA_PATH = System.getProperty("sql.data.directory", "/sql");

    private final UpgradeControl upgradeControl;
    private final LazyReference<DatabaseManager> databaseManager;

    @Inject
    public CaptureTenantDatabaseProvisioner(SqlDataImporter sqlDataImporter, JIRAEnvironmentData environmentData) {
        upgradeControl = new UpgradeControl(environmentData);

        System.setProperty("db.admin.password", DB_ADMIN_PASSWORD);
        System.setProperty("db.admin.user", DB_ADMIN_USER);

        databaseManager = new LazyReference<DatabaseManager>() {
            @Override
            protected DatabaseManager create() throws Exception {
                return new DatabaseManager(TEST_HOST, TEST_PORT, sqlDataImporter);
            }
        };
    }

    @Override
    public TestDatabase create(String tenantId, String dataSqlResourcePath) {
        long now = System.currentTimeMillis();
        try {
            TestDatabase existingDatabase = new TestDatabase(databaseManager.get(), tenantId, false);
            existingDatabase.importData(TenantConnectionProvider.absolutePath(SCHEMA_FILE, DEFAULT_SCHEMA_PATH));
            existingDatabase.importData(TenantConnectionProvider.absolutePath(dataSqlResourcePath, DEFAULT_DATA_PATH));

            upgradeControl.runUpgradesSynchronously();

            return existingDatabase;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to provision new tenant database due to: " + e.getMessage(), e);
        } finally {
            log.info("Provisioned database for tenant {} in {} ms", tenantId, System.currentTimeMillis() - now);
        }
    }

    /**
     * Instead of annotating every test with the @UseTenant() annotation from jira-func-test
     * and using the TestTenantContextRule, we just set the global Cloud Tenant Id before we
     * run tests in Vertigo mode.
     */
    public static void setupVertigoTestTenant() {
        TestTenantContext.setCloudTenantId(TEST_TENANT_NAME);
        TenantIdFilter.setCloudTenantId(TEST_TENANT_NAME);
    }
}
