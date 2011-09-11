package agentspring.face;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agentspring.facade.DbService;
import agentspring.facade.EngineService;
import agentspring.face.gremlin.GremlinQuery;
import agentspring.face.model.Source;
import agentspring.face.model.dao.SourceDAO;

public class DbDataCache {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(DbDataCache.class);

    private EngineService engineService;
    private DbService dbService;
    private long tick = 0;
    private HashMap<Integer, List<TickJsonResponse>> cache = new HashMap<Integer, List<TickJsonResponse>>();
    private SourceDAO sourceDao;

    public void clear() {
        synchronized (this.cache) {
            this.cache.clear();
        }
    }

    public void update() {
        synchronized (this.cache) {
            this.tick = engineService.getCurrentTick();
            for (Source source : this.sourceDao.listSources()) {
                GremlinQuery query = new GremlinQuery(source);
                TickJsonResponse result = new TickJsonResponse();
                try {
                    if (!query.getStartNode().equals("")) {
                        result.put("result", dbService.executeGremlinQueries(query.getStartNode(), query.getQuery()));
                    } else {
                        result.put("result", dbService.executeGremlinQuery(query.getQuery()));
                    }
                    result.setSuccess(true);
                } catch (ScriptException e) {
                    result.setSuccess(false);
                    result.put("exception", e.toString());
                }
                // logger.info(query.getQuery());
                result.setTick((int) tick);
                Integer id = query.getId();
                if (this.cache.get(id) == null) {
                    this.cache.put(id, new ArrayList<TickJsonResponse>());
                }
                this.cache.get(id).add(result);
            }
        }
    }

    public List<TickJsonResponse> getData(Integer dataId) {
        synchronized (this.cache) {
            return this.cache.get(dataId);
        }
    }

    public List<TickJsonResponse> getLastData(Integer dataId) {
        synchronized (this.cache) {
            List<TickJsonResponse> data = this.cache.get(dataId);
            if (data == null || data.isEmpty()) {
                return null;
            } else {
                List<TickJsonResponse> result = new ArrayList<TickJsonResponse>();
                result.add(data.get(data.size() - 1));
                return result;
            }
        }
    }

    public List<TickJsonResponse> getData(Integer dataId, int from) {
        List<TickJsonResponse> result = null;
        synchronized (this.cache) {
            result = this.cache.get(dataId);
        }
        if (result != null) {
            List<TickJsonResponse> filteredResult = new ArrayList<TickJsonResponse>();
            for (TickJsonResponse response : result) {
                if (response.getTick() > from) {
                    filteredResult.add(response);
                }
            }
            return filteredResult;
        } else {
            return result;
        }
    }

    // Dependency injection

    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    public void setSourceDao(SourceDAO sourceDao) {
        this.sourceDao = sourceDao;
    }
}
