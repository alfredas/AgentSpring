package agentspring.face;

public class TickJsonResponse extends JsonResponse {
    private static final long serialVersionUID = 1L;
    private final String TICK = "tick";

    public void setTick(int tick) {
        this.put(this.TICK, tick);
    }

    public int getTick() {
        return (Integer)this.get(this.TICK);
    }
}
