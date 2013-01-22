package agentspring.simulation;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agentspring.agent.Agent;
import agentspring.facade.EngineEvent;
import agentspring.facade.EngineState;
import agentspring.role.Role;
import agentspring.role.ScriptComponent;

/**
 * Simulation schedule
 * 
 * @author alfredas
 * 
 */
public class Schedule {
    static Logger logger = LoggerFactory.getLogger(Schedule.class);
    private static final Schedule schedule = new Schedule();
    private long currentTick = 0;
    private Thread runner;
    private List<RoleAgent> roleList = new ArrayList<RoleAgent>();
    private EngineState state = EngineState.STOPPED;
    private List<SimulationListener> listeners = new ArrayList<SimulationListener>();

    private class Runner extends Thread {
        @Override
        public void run() {
            while (Schedule.this.state != EngineState.STOPPING) {
                logger.info("SCHEDULE:");
                for (RoleAgent roleAgent : roleList) {
                    logger.info("role {}", roleAgent.getName());
                }
                for (RoleAgent roleAgent : roleList) {
                    Role<? extends Agent> role = roleAgent.getRole();
                    Agent agent = roleAgent.getAgent();
                    long start = roleAgent.getStart();
                    long end = roleAgent.getEnd();
                    long timeStep = roleAgent.getTimeStep();
                    if (start <= currentTick && (end > currentTick || end == 0) && (currentTick % timeStep == 0)) {
                        logger.info("Will execute role " + role + " for agent " + agent + " tick " + currentTick);
                        if (agent != null) {
                            // agent roles
                            agent.act(role);
                        } else {
                            // environment roles=no agent- like rain
                            role.act(null);
                        }
                    } else {
                        logger.info("Will NOT execute role " + role + " for agent " + agent + " tick " + currentTick);
                    }
                }
                synchronized (this) {
                    if (Schedule.this.state == EngineState.PAUSING) {
                        Schedule.this.state = EngineState.PAUSED;
                    }
                    while (Schedule.this.state == EngineState.PAUSED) {
                        try {
                            this.wait(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                synchronized (this) {
                    for (SimulationListener listener : Schedule.this.listeners) {
                        logger.info("WAKE PROBLEM DEBUG: executing tick end event for listener: {}", listener);
                        listener.act(EngineEvent.TICK_END);
                    }
                    // engine sleeps while client collects data
                    try {
                        logger.info("WAKE PROBLEM DEBUG: about to to wait for the client to collect data and wake me up");
                        this.wait();
                        logger.info("WAKE PROBLEM DEBUG: i just woke up");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                currentTick++;
            }
            Schedule.this.state = EngineState.STOPPED;
        }
    }

    public EngineState getState() {
        return this.state;
    }

    public void start() {
        if (this.state == EngineState.STOPPED || this.state == EngineState.CRASHED) {
            this.state = EngineState.RUNNING;
            this.runner = new Runner();
            this.runner.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Schedule.this.state = EngineState.CRASHED;
                    e.printStackTrace();
                }
            });
            runner.start();
        }
    }

    public void pause() {
        if (this.state == EngineState.RUNNING) {
            this.state = EngineState.PAUSING;
        }
    }

    public void resume() {
        if (this.state == EngineState.PAUSED) {
            this.state = EngineState.RUNNING;
        }
    }

    public void stop() {
        if (this.state == EngineState.RUNNING) {
            this.state = EngineState.STOPPING;
        }
    }

    public void wake() {
        synchronized (this.runner) {
            if (this.runner.getState() != State.WAITING) {
                logger.warn("Engine is not sleeping, can not wake");
            } else {
                // this.state = EngineState.RUNNING;
                logger.info("WAKE PROBLEM DEBUG: about to to notify simulation runner thread");
                this.runner.notifyAll();
            }
        }
    }

    public void listen(SimulationListener listener) {
        this.listeners.add(listener);
    }

    public static Schedule getSchedule() {
        return schedule;
    }

    public void clear() {
        this.currentTick = 0;
        this.roleList.clear();
    }

    public void addRole(String name, Role<? extends Agent> role, Agent agent) {
        RoleAgent roleAgent = new RoleAgent(name, role, agent);
        roleList.add(roleAgent);
    }

    public long getCurrentTick() {
        return currentTick;
    }

    class RoleAgent {

        private Role<? extends Agent> role;
        private Agent agent;
        private String name;
        private String after;
        private boolean last;
        private boolean first;
        private long start;
        private long end;
        private long timeStep;

        public RoleAgent(String name, Role<? extends Agent> role, Agent agent) {
            this.name = name;
            this.agent = agent;
            this.role = role;
            ScriptComponent annotation = role.getClass().getAnnotation(ScriptComponent.class);
            this.first = annotation.first();
            this.last = annotation.last();
            this.after = annotation.after();
            this.start = annotation.start();
            this.end = annotation.end();
            this.timeStep = annotation.timeStep();
        }

        public Role<? extends Agent> getRole() {
            return role;
        }

        public void setRole(Role<? extends Agent> role) {
            this.role = role;
        }

        public Agent getAgent() {
            return agent;
        }

        public void setAgent(Agent agent) {
            this.agent = agent;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAfter() {
            return after;
        }

        public void setAfter(String after) {
            this.after = after;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public long getTimeStep() {
            return timeStep;
        }

        public void setTimeStep(long timeStep) {
            this.timeStep = timeStep;
        }
    }

}
