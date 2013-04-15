package agentspring;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import agentspring.facade.EngineEvent;
import agentspring.facade.EngineState;
import agentspring.service.DbServiceImpl;
import agentspring.service.EngineServiceImpl;
import flexjson.JSONSerializer;

/**
 * Runs the simulation in a headless mode.
 * 
 * @author alfredas
 * 
 */
public class HPCService {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    EngineServiceImpl engine;
    DbServiceImpl db;
    EngineListener listener;
    List<Query> queries;
    String runId;
    String resultsPath;
    String scenarioFilename;
    private final String QUERY_PROP_FILE = "queries.properties";

    public HPCService() {
        // load spring context
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("engineContext.xml", "hpcServiceContext.xml");

        // create engine
        engine = context.getBean(EngineServiceImpl.class);
        try {

            if (System.getProperty("scenario.file") != null) {
                scenarioFilename = System.getProperty("scenario.file");
                engine.init(scenarioFilename);
            } else {
                logger.warn("No scenario file found. Start with any scenario.");
                engine.init();
            }

            // identify each run
            if (System.getProperty("run.id") != null) {
                runId = System.getProperty("run.id");
            } else {
                runId = UUID.randomUUID().toString();
            }

            logger.warn("Running " + engine.getCurrentScenario() + ", run.Id " + runId);

            // start engine
            engine.start();

            // create db
            db = context.getBean(DbServiceImpl.class);

            // where do we save results
            if (System.getProperty("results.path") != null) {
                resultsPath = System.getProperty("results.path");
            } else {
                resultsPath = "/tmp/";
            }

            // read query list
            if (System.getProperty("query.file") != null) {
                queries = readQueries();
            } else {
                if (this.getClass().getClassLoader().getResource(QUERY_PROP_FILE) != null) {
                    queries = readQueries();
                } else {
                    logger.warn("No queries given to the simulation.");
                    queries = Collections.emptyList();
                }
            }

            // create event listener
            listener = new EngineListener();

            // start listener
            listener.start();

        } catch (EngineException err) {
            logger.error(err.getMessage());
        }

    }

    public static void main(String args[]) {
        logger.warn("HPC mode");
        // create service
        HPCService service = new HPCService();
    }

    private List<Query> readQueries() {
        List<HPCService.Query> qs = new ArrayList<HPCService.Query>();
        String queryContents = null;
        if (System.getProperty("query.file") != null) {
            String queryFile = System.getProperty("query.file");
            if (!queryFile.startsWith("/")) {
                String currentPath = System.getProperty("user.dir");
                queryFile = currentPath + (currentPath.endsWith("/") ? "" : "/") + queryFile;
            }
            try {
                queryContents = getContents(new FileReader(new File(queryFile)));
            } catch (IOException e) {
                logger.warn("Error reading {} file.", QUERY_PROP_FILE);
            }
        } else {
            InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(QUERY_PROP_FILE);
            queryContents = getContents(new InputStreamReader(fileStream));
        }

        String[] vals = queryContents.split("\",[ \t\n]*\"");
        for (int i = 0; i < vals.length; i += 3) {
            String name = vals[i].replaceAll("^\"", "");
            String node = vals[i + 1].equals("") ? null : vals[i + 1];
            String script = vals[i + 2].replaceAll("[\"\n,]*$", "");
            Query q = new Query(name, node, script);
            qs.add(q);
        }
        return qs;
    }

    private String getContents(InputStreamReader fileStream) {
        StringBuilder contents = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(fileStream);
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line + "\n");
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
        }

        return contents.toString();
    }

    private class EngineListener extends Thread {
        @Override
        public void run() {
            while (true) {
                EngineEvent event = engine.listen();
                if (event == EngineEvent.TICK_END) {
                    // execute query
                    saveResults(runQueries());
                    logger.info("WAKE PROBLEM DEBUG: about to wake engine");
                    engine.wake();
                }
                if (engine.getState() == EngineState.STOPPING) {
                    engine.release();
                    logger.warn("Stopping AgentSpring!");
                    // TODO: Hack, find a better solution?
                    System.exit(0);
                }
            }
        }

        private HashMap<String, List<Object>> runQueries() {
            HashMap<String, List<Object>> map = new HashMap<String, List<Object>>();
            for (Query query : queries) {
                List<Object> result;
                try {
                    result = db.executeGremlinQueries(query.getNode(), query.getScript());
                    map.put(query.getName(), result);
                } catch (ScriptException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return map;
        }

        private void saveResults(HashMap<String, List<Object>> resultMap) {
            JSONSerializer serializer = new JSONSerializer();
            for (String query : resultMap.keySet()) {
                try {
                    String resultsFileName = resultsPath + (resultsPath.endsWith("/") ? "" : "/") + runId + "-" + query;
                    FileWriter fstream = new FileWriter(resultsFileName, true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    String ser = serializer.serialize(resultMap.get(query));
                    out.write(ser + ",\n");
                    out.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private class Query {

        private String node;
        private String script;
        private String name;

        public Query(String name, String node, String script) {
            super();
            this.node = node;
            this.script = script;
            this.name = name;
        }

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "[" + name + "] [" + node + "] [" + script + "]";
        }

    }

}
