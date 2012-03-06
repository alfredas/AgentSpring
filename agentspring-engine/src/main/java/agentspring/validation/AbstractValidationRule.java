package agentspring.validation;

import agentspring.simulation.Schedule;

public abstract class AbstractValidationRule {

    private String after;
    private String before;

    public long getCurrentTick() {
        return Schedule.getSchedule().getCurrentTick();
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
}
