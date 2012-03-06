package agentspring.graphdb;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes the db after the simulation has stopped.
 * @author alfredas
 *
 */
public class DbMaintainer {

    static Logger logger = LoggerFactory.getLogger(DbMaintainer.class);

    private String db;

    public void cleanup() {
        if (getDb() != null && getDb() != "") {
            String dblocation = "";
            // TODO: make this more robust
            if (!getDb().startsWith("/") && !getDb().contains(":")) {
                setDb("/" + getDb());
                dblocation = this.getClass().getResource("/").toString().replaceAll("/target/classes", getDb())
                        .replaceAll("file:/", "/");
            } else {
                dblocation = getDb();
            }

            File db = new File(dblocation);
            if (db.exists()) {
                deleteDir(db);
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    /*
                     * HACK (Tautvilas) on windows messages.log remains locked
                     * by JVM if the web client is using the database, thus this
                     * file can not be deleted
                     * 
                     * https://trac.neo4j.org/ticket/316
                     */
                    // return false;
                }
            }
        }
        return dir.delete();
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

}
