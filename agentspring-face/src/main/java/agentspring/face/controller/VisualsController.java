package agentspring.face.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.HtmlUtils;

import agentspring.facade.VisualService;
import agentspring.facade.db.Visual;
import agentspring.facade.visual.ChartVisual;
import agentspring.facade.visual.ScatterVisual;
import agentspring.face.JsonResponse;

/**
 * Visuals CRUD
 */
@Controller
@RequestMapping(value = "/visuals")
public class VisualsController {

    private static final String VIEW = "visuals";

    @Autowired
    private VisualService visualService;

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ModelAndView create() {
        ModelAndView response = new ModelAndView(VIEW);
        response.addObject("data_source", new ChartVisual(null, null, null, null));
        return response;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse list() {
        JsonResponse response = new JsonResponse(true);
        List<Visual> visuals = visualService.listFullVisuals();
        response.put("visuals", visuals);
        return response;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public ModelAndView edit(@RequestParam("id") Integer id) {
        ModelAndView response = new ModelAndView(VIEW);
        response.addObject("visual", this.visualService.getVisual(id));
        return response;
    }

    @RequestMapping(value = "/save/chart", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse save(@RequestParam("id") Integer id, @RequestParam("title") String title, @RequestParam("sources") int[] sources,
            @RequestParam("type") String type, @RequestParam("yaxis") String yaxis) {
        title = HtmlUtils.htmlEscape(title.trim());
        type = HtmlUtils.htmlEscape(type.trim());
        yaxis = HtmlUtils.htmlEscape(yaxis.trim());
        String error = null;
        JsonResponse response = new JsonResponse(true);
        if (title.isEmpty()) {
            error = "Data source title can not be left empty";
        } else if (type.isEmpty()) {
            error = "You have to select chart type";
        }
        if (error != null) {
            response.setError(error);
            return response;
        }
        ChartVisual visual = new ChartVisual(id, title, sources, type, yaxis);
        int newId = this.visualService.saveChartVisual(visual);
        response.put("id", newId);
        return response;
    }

    @RequestMapping(value = "/save/scatter", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse save(@RequestParam("id") Integer id, @RequestParam("title") String title, @RequestParam("sources") int[] sources,
            @RequestParam("yaxis") String yaxis) {
        title = HtmlUtils.htmlEscape(title.trim());
        yaxis = HtmlUtils.htmlEscape(yaxis.trim());
        String error = null;
        JsonResponse response = new JsonResponse(true);
        if (title.isEmpty()) {
            error = "Data source title can not be left empty";
        }
        if (error != null) {
            response.setError(error);
            return response;
        }
        ScatterVisual visual = new ScatterVisual(id, title, sources, yaxis);
        int newId = this.visualService.saveScatterVisual(visual);
        response.put("id", newId);
        return response;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RedirectView delete(@RequestParam("id") Integer id) {
        RedirectView response = new RedirectView("/visuals/new", true);
        this.visualService.delete(id);
        return response;
    }
}