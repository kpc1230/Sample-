package util;

import cloud.atlassian.rdbms.schema.test.util.StandaloneRdbmsSchemaService;

/**
 * Utils to easy get StandaloneRdbmsSchemaService with Capture settings
 */
public class CaptureStandaloneRdbmsSchemaServiceTools {
    public static final String JDBC_URL = "jdbc:postgresql://localhost:15433/jira";
    public static final String USERNAME = "jira";
    public static final String PASSWORD = "jira";
    public static final String MIGRATIONS_SCRIPTS_PATH = "db/migrations";

    public static StandaloneRdbmsSchemaService getStandaloneRdbmsSchemaService() {
        return StandaloneRdbmsSchemaService.newBuilder()
                .withMigrations(MIGRATIONS_SCRIPTS_PATH)
                .withJdbcUrl(JDBC_URL)
                .withUser(USERNAME)
                .withPassword(PASSWORD)
                .build();
    }
}
