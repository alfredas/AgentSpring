package agentspring.face.model;

public class Source {
    private Integer id = null;
    private String start = null;
    private String script = null;
    private String title = null;

    public Source(Integer id) {
        this.id = id;
    }

    public Source(Integer id, String script) {
        this(id);
        this.script = script;
    }

    public Source(Integer id, String start, String script) {
        this(id, script);
        this.start = start;
    }

    public Source(Integer id, String title, String start, String script) {
        this(id, start, script);
        this.title = title;
    }

    public Integer getId() {
        return id;
    }
    public String getStart() {
        return start;
    }
    public String getScript() {
        return script;
    }

    public String getTitle() {
        return title;
    }
}
