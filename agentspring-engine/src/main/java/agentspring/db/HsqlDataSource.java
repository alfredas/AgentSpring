package agentspring.db;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

/**
 * HSQL db wrapper to store model data-sources and visuals.
 * @author alfredas
 *
 */
public class HsqlDataSource extends BasicDataSource {

    private static final Logger logger = LoggerFactory.getLogger(HsqlDataSource.class);

    @Autowired
    private ApplicationContext applicationContext;
    private String schemaLocation;
    private String dataLocation;
    private String schema;

    /**
     * Create basic data structure if db was created for first time
     */
    public void setup() {
        SimpleJdbcTemplate template = new SimpleJdbcTemplate(this);
        String sql = "SELECT COUNT(SCHEMA_NAME) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        int schemaExists = template.queryForInt(sql, new Object[] { this.schema });
        if (schemaExists == 0) {
            Resource resource = this.applicationContext.getResource(schemaLocation);
            logger.info("Creating initial database schema");
            SimpleJdbcTestUtils.executeSqlScript(template, resource, false);
            resource = this.applicationContext.getResource(dataLocation);
            logger.info("Populating database with initial data");
            SimpleJdbcTestUtils.executeSqlScript(template, resource, false);
        }
    }

    /**
     * HACK: this cleanup method is needed for manual JDBC driver deregistration
     * in order to prevent memory leak in tomcat
     */
    @Override
    public void close() {
        SimpleJdbcTemplate template = new SimpleJdbcTemplate(this);
        logger.info("Shutting down HSQL DB");
        template.update("SHUTDOWN");
        try {
            super.close();
            Driver driver = DriverManager.getDriver(this.getUrl());
            DriverManager.deregisterDriver(driver);
            logger.info("Deregistered JDBC driver");
        } catch (SQLException e) {
            if (e.getSQLState().equals("08001")) {
                logger.info("HSQL driver was allready deregistered");
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void setSchemaLocation(String path) {
        this.schemaLocation = path;
    }

    public void setDataLocation(String path) {
        this.dataLocation = path;
    }

    public void setSchema(String title) {
        this.schema = title;
    }
}
