package agentspring.face.model.visual;

import java.util.ArrayList;
import java.util.List;

import agentspring.face.model.Source;


public abstract class Visual {
    private Integer id = null;
    private String title = null;
    private List<Source> sources = new ArrayList<Source>();

    public static final String clazz = "";

    public Visual(Integer id) {
        this.id = id;
    }

    public Visual(Integer id, List<Source> sources) {
        this(id);
        this.sources = sources;
    }

    public Visual(Integer id, String title) {
        this(id);
        this.title = title;
    }

    public Visual(Integer id, String title, List<Source> sources) {
        this(id, title);
        this.sources = sources;
    }

    public Visual(Integer id, String title, int[] sources) {
        this(id, title);
        for (int source: sources) {
            this.addSource(new Source(source));
        }
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Source> getSources() {
        return this.sources;
    }

    public List<Integer> getSourcesIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (Source source : this.sources) {
            ids.add(source.getId());
        }
        return ids;
    }

    public void addSource(Source source) {
        this.sources.add(source);
    }

    public abstract String getClazz();
}
