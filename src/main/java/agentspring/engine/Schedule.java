package agentspring.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agentspring.engine.role.Role;
import agentspring.engine.role.ScriptComponent;

public class Schedule {

    static Logger logger = LoggerFactory.getLogger(Schedule.class);

    private static final Schedule schedule = new Schedule();

    private long currentTick = 0;

    private boolean running = false;

    private boolean stop = false;

    private boolean paused = false;

    private Thread runner;

    private List<RoleAgent> roleList = new ArrayList<RoleAgent>();

    private class Runner extends Thread {

        @Override
        public void run() {
            while (!Schedule.this.stop) {
                synchronized (this) {
                    if (Schedule.this.paused) {
                        try {
                            this.wait(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        continue;
                    }
                }
                for (RoleAgent roleAgent : roleList) {
                    Role<? extends Agent> role = roleAgent.getRole();
                    Agent agent = roleAgent.getAgent();
                    long start = roleAgent.getStart();
                    long end = roleAgent.getEnd();
                    long timeStep = roleAgent.getTimeStep();
                    if (start <= currentTick && (end > currentTick || end == 0)
                            && (currentTick % timeStep == 0)) {
                        if (agent != null) {
                            // agent roles
                            agent.act(role);
                        } else {
                            // environment roles=no agent- like rain
                            role.act(null);
                        }
                    }
                }
                currentTick++;
            }
        }
    }

    public void start() {
        if (this.running)
            return;
        this.running = true;
        this.runner = new Runner();
        runner.start();
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void stop() {
        if (!this.running)
            return;
        this.stop = true;
        try {
            this.runner.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.running = false;
        this.stop = false;
    }

    public boolean isRunning() {
        return this.running;
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

        boolean first = roleAgent.isFirst();
        boolean last = roleAgent.isLast();
        String after = roleAgent.getAfter();

        if (first) {
            roleList.add(0, roleAgent);
        } else if (last) {
            roleList.add(roleList.size(), roleAgent);
        } else if (after != "" && findRole(after) >= 0) {
            roleList.add(findRole(after) + 1, roleAgent);
        } else {
            int position = findRoleAfterMe(name);
            if (position >= 0) {
                roleList.add(position, roleAgent);
            } else {
                int size = roleList.size();
                boolean isLastRole = true;
                while (size > 0 && isLastRole) {
                    RoleAgent lastRole = roleList.get(size - 1);
                    isLastRole = lastRole.isLast();
                    if (isLastRole)
                        size--;
                }
                roleList.add(size, roleAgent);
            }
        }

    }

    private int findRole(String name) {
        int i = 0;
        for (RoleAgent ba : roleList) {
            String n = ba.getName();
            if (n != null && n.equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int findRoleAfterMe(String name) {
        for (RoleAgent ba : roleList) {
            String after = ba.getAfter();
            if (after != "" && after.equals(name)) {
                return roleList.indexOf(ba);
            }
        }
        return -1;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(long currentTick) {
        this.currentTick = currentTick;
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
            ScriptComponent annotation = role.getClass().getAnnotation(
                    ScriptComponent.class);
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
