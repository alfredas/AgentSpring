package agentspring.face.gremlin;

public abstract class AbstractGremlinQuery {
    private String startNode;
    private Integer id;

    public AbstractGremlinQuery(Integer id, String startNode) {
        this.startNode = startNode;
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public String getStartNode() {
        return this.startNode;
    }

    public abstract String getQuery();

    @Override
    public String toString() {
        return this.id + "";
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o.toString().equals(this.toString());
    }
}
