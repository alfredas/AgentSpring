package agentspring.face.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import agentspring.face.JsonResponse;
import agentspring.face.model.dao.SourceDAO;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value = "/")
public class DashboardController {

    @Autowired
    private SourceDAO sourceDao;

    private int[] visuals = new int[] {0};

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "")
    public ModelAndView home() {
        ModelAndView response = new ModelAndView("dashboard");
        response.addObject("datasources", sourceDao.listSources());
        return response;
    }

    @RequestMapping(value = "log")
    public ModelAndView log() {
        ModelAndView response = new ModelAndView("log");
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "monitor/set", method = RequestMethod.POST)
    public JsonResponse setMonitor(
            @RequestParam(value = "visuals", required = false) int[] visuals) {
        if (visuals == null) {
            visuals = new int[] {};
        }
        this.visuals = visuals;
        return new JsonResponse(true);
    }

    @ResponseBody
    @RequestMapping(value = "monitor/get")
    public JsonResponse getMonitor() {
        JsonResponse response = new JsonResponse(true);
        response.put("visuals", this.visuals);
        return response;
    }
}
