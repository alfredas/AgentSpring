package agentspring.face.controller;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import agentspring.facade.DbService;
import agentspring.face.DbDataCache;
import agentspring.face.JsonResponse;
import agentspring.face.TickJsonResponse;

@Controller
@RequestMapping(value = "/db")
public class DbController {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

    @Autowired
    private DbDataCache dbCache;
    @Autowired
    private DbService dbService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse query(@RequestParam(value = "start", required = false) String start, @RequestParam("query") String query) {
        // logger.info("Executing gremlin query: " + query);
        JsonResponse response = new JsonResponse();
        try {
            if (start == null || start.trim().equals("")) {
                response.put("result", dbService.executeGremlinQuery(query));
            } else {
                response.put("result", dbService.executeGremlinQueries(start, query));
            }
            response.setSuccess(true);
        } catch (ScriptException e) {
            response.setSuccess(false);
            response.put("exception", e.toString());
        }
        return response;
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    @ResponseBody
    public List<TickJsonResponse> history(@RequestParam("data") Integer data, @RequestParam(value = "from", required = false) Integer from,
            @RequestParam(value = "last", required = false) Boolean last) {
        List<TickJsonResponse> result = null;
        if (from != null) {
            result = this.dbCache.getData(data, from);
        } else if (last != null && last == true) {
            result = this.dbCache.getLastData(data);
        } else {
            result = this.dbCache.getData(data);
        }
        if (result != null)
            return result;
        else
            return new ArrayList<TickJsonResponse>();
    }

}
