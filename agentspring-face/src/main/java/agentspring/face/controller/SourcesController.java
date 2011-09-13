package agentspring.face.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import agentspring.facade.DbService;
import agentspring.face.JsonResponse;
import agentspring.face.model.Source;
import agentspring.face.model.dao.SourceDAO;
import agentspring.face.model.dao.VisualDAO;

/**
 * Data sources CRUD
 */
@Controller
@RequestMapping(value = "/sources")
public class SourcesController {

    private static final String VIEW = "sources";

    @Autowired
    private SourceDAO sourceDao;
    @Autowired
    private VisualDAO visualDao;
    @Autowired
    private DbService dbService;

    private ModelAndView setup(ModelAndView response) {
        response.addObject("start_nodes", this.dbService.getStartNodes());
        return response;
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ModelAndView create() {
        ModelAndView response = new ModelAndView(VIEW);
        response.addObject("data_source", new Source(null, ""));
        this.setup(response);
        return response;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse list() {
        JsonResponse response = new JsonResponse(true);
        List<Source> sources = sourceDao.listSources();
        response.put("sources", sources);
        return response;
    }

    @RequestMapping(value = "/visuals", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse visual(@RequestParam("id") int id) {
        JsonResponse response = new JsonResponse(true);
        response.put("visuals", visualDao.getVisualsForSource(id));
        return response;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView response = new ModelAndView(VIEW);
        response.addObject("data_source", this.sourceDao.getSource(id));
        this.setup(response);
        return response;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse save(@RequestParam("id") Integer id,
            @RequestParam("query") String query,
            @RequestParam("start_node") String start_node,
            @RequestParam("title") String title) {
        query = HtmlUtils.htmlEscape(query.trim());
        start_node = HtmlUtils.htmlEscape(start_node.trim());
        title = HtmlUtils.htmlEscape(title.trim());

        String error = null;
        JsonResponse response = new JsonResponse(true);
        if (query.isEmpty()) {
            error = "Data source query can not be left empty";
        } else if (title.isEmpty()) {
            error = "Data source title can not be left empty";
        }
        if (error != null) {
            response.setError(error);
            return response;
        }
        int newId = this.sourceDao.saveSource(new Source(id, title, start_node,
                query));
        response.put("id", newId);
        return response;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse delete(@RequestParam("id") int id) {
        JsonResponse response = new JsonResponse(true);
        try {
            this.sourceDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            response.setError("Could not delete source because there are visuals using it");
        }
        return response;
    }
}
