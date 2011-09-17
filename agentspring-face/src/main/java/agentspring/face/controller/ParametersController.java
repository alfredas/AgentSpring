package agentspring.face.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import agentspring.facade.ConfigurableObject;
import agentspring.facade.EngineService;
import agentspring.facade.ScenarioParameter;
import agentspring.face.JsonResponse;

@Controller
@RequestMapping(value = "/parameters")
public class ParametersController {
    @Autowired
    private EngineService engineService;

    @RequestMapping(value = "")
    public ModelAndView home() {
        ModelAndView response = new ModelAndView("parameters");
        return response;
    }

    @RequestMapping(value = "/list")
    public @ResponseBody
    JsonResponse parameters() {
        JsonResponse response = new JsonResponse(true);
        response.put("parameters", this.engineService.getScenarioParameters().values());
        return response;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse save(@RequestParam("ids") String[] ids, @RequestParam("counts") int[] counts,
            @RequestParam("fields") String[] fields, @RequestParam("values") String[] values) {
        Map<String, Map<String, ScenarioParameter>> params = new HashMap<String, Map<String, ScenarioParameter>>();
        Map<String, ConfigurableObject> oldParams = this.engineService.getScenarioParameters();
        int countsum = 0;
        String error = null;
        for (int i = 0; i < ids.length; i++) {
            Map<String, ScenarioParameter> p = new HashMap<String, ScenarioParameter>();
            for (int j = countsum; j < countsum + counts[i]; j++) {
                Object value;
                ConfigurableObject obj = oldParams.get(ids[i]);
                ScenarioParameter param = obj.getParameter(fields[j]);
                try {
                    value = this.parseValue(param.getValue(), values[j]);
                } catch (IllegalArgumentException e) {
                    error = "Incorrect value for '" + ids[i] + "' field '" + fields[j] + "' (" + e.getMessage() + ")";
                    break;
                }
                p.put(fields[j], new ScenarioParameter(fields[j], value));
            }
            if (error != null)
                break;
            params.put(ids[i], p);
            countsum += counts[i];
        }
        if (error == null) {
            this.engineService.setScenarioParameters(params);
            return new JsonResponse(true);
        } else {
            JsonResponse response = new JsonResponse(false);
            response.setError(error);
            return response;
        }
    }

    // HACKED UP parser
    private Object parseValue(Object oldValue, String newValue) throws IllegalArgumentException {
        Object result = null;
        if (oldValue instanceof Integer) {
            result = Integer.parseInt(newValue);
        } else if (oldValue instanceof Double) {
            result = Double.parseDouble(newValue);
        } else if (oldValue instanceof Long) {
            result = Long.parseLong(newValue);
        } else if (oldValue instanceof Boolean) {
            result = Boolean.parseBoolean(newValue);
        } else {
            throw new IllegalArgumentException("Unknown parse strategy for " + oldValue.getClass());
        }
        return result;
    }
}
