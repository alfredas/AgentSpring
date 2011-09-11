package agentspring.face.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import agentspring.facade.EngineEvent;
import agentspring.facade.EngineService;
import agentspring.facade.EngineState;
import agentspring.face.DbDataCache;
import agentspring.face.JsonResponse;

/**
 * Handles requests for the remote engine service.
 */

@Controller
@RequestMapping(value = "/engine")
public class EngineController {
    @Autowired
    private EngineService engineService;

    @Autowired
    private DbDataCache dbCache;

    private EngineListener listener;

    private boolean stop = false;

    private static int LOG_SIZE = 10;

    private static final Logger logger = LoggerFactory.getLogger(EngineController.class);

    private List<String> log = new ArrayList<String>();

    private class EngineListener extends Thread {
        @Override
        public void run() {
            while (!EngineController.this.stop) {
                EngineEvent event = engineService.listen();
                if (event == EngineEvent.TICK_END) {
                    dbCache.update();
                    engineService.wake();
                } else if (event == EngineEvent.LOG_MESSAGE) {
                    synchronized (log) {
                        log.add(engineService.popLog());
                    }
                }
            }
            logger.info("Stopping listener thread");
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public @ResponseBody
    JsonResponse status() {
        JsonResponse response = new JsonResponse(true);
        response.put("state", this.engineService.getState().toString());
        response.put("tick", this.engineService.getCurrentTick());
        return response;
    }

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public @ResponseBody
    JsonResponse start() {
        EngineState state = this.engineService.getState();
        if (state == EngineState.STOPPED || state == EngineState.CRASHED) {
            logger.info("Starting new simulation");
            this.engineService.start();
            this.dbCache.clear();
            this.log.clear();
            return new JsonResponse(true);
        } else {
            logger.warn("Can not start engine, because engine state is " + state.toString());
            return new JsonResponse(false);
        }
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    public @ResponseBody
    JsonResponse stop() {
        logger.info("Stopping simulation");
        engineService.stop();
        return new JsonResponse(true);
    }

    @RequestMapping(value = "/pause", method = RequestMethod.GET)
    public @ResponseBody
    JsonResponse pause() {
        logger.info("Pausing simulation");
        engineService.pause();
        return new JsonResponse(true);
    }

    @RequestMapping(value = "/resume", method = RequestMethod.GET)
    public @ResponseBody
    JsonResponse resume() {
        logger.info("Resuming simulation");
        engineService.resume();
        return new JsonResponse(true);
    }

    @RequestMapping(value = "/listen", method = RequestMethod.GET)
    public @ResponseBody
    void listen() {
        if (!this.stop) {
            if (this.listener == null || !this.listener.isAlive()) {
                this.listener = new EngineListener();
                this.listener.start();
            }
        }
    }

    @RequestMapping(value = "/load", method = RequestMethod.POST)
    public @ResponseBody
    JsonResponse load(@RequestParam("scenario") String scenario) {
        this.engineService.loadScenario(scenario);
        return new JsonResponse(true);
    }

    @RequestMapping(value = "/log")
    public @ResponseBody
    JsonResponse log(@RequestParam(value = "full", defaultValue = "false", required = false) String full,
            @RequestParam(value = "from", required = false) Integer from) {
        JsonResponse response = new JsonResponse(true);
        synchronized (this.log) {
            boolean getFull = Boolean.parseBoolean(full);
            if (getFull) {
                response.put("log", this.log);
            } else if (from != null) {
                ArrayList<String> reducedLog = new ArrayList<String>();
                for (int i = from; i < log.size(); i++) {
                    reducedLog.add(log.get(i));
                }
                response.put("log", reducedLog);
            } else {
                ArrayList<String> reducedLog = new ArrayList<String>();
                for (int i = Math.max(log.size() - LOG_SIZE, 0); i < log.size(); i++) {
                    reducedLog.add(log.get(i));
                }
                response.put("log", reducedLog);
            }
            response.put("last", this.log.size());
            return response;
        }
    }

    @RequestMapping(value = "/scenarios")
    public @ResponseBody
    JsonResponse scenarios() {
        JsonResponse response = new JsonResponse(true);
        response.put("scenarios", this.engineService.getScenarios());
        return response;
    }

    @RequestMapping(value = "/scenario")
    public @ResponseBody
    JsonResponse scenario() {
        JsonResponse response = new JsonResponse(true);
        response.put("scenario", this.engineService.getCurrentScenario());
        return response;
    }

    @PreDestroy
    public void cleanUp() {
        // kill listener thread
        this.stop = true;
        this.engineService.release();
        try {
            this.listener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
