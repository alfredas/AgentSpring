package agentspring.facade.visual;

import java.util.List;

import agentspring.facade.db.Source;
import agentspring.facade.db.Visual;

public class ScatterVisual extends Visual {
    private static final long serialVersionUID = 1L;

    private String yaxis;

    public final static String clazz = "scatter";

    public ScatterVisual(Integer id, String title, String yaxis) {
        super(id, title);
        this.yaxis = yaxis;
    }

    public ScatterVisual(Integer id, String title, List<Source> sources, String yaxis) {
        super(id, title, sources);
        this.yaxis = yaxis;
    }

    public ScatterVisual(Integer id, String title, int[] sources, String yaxis) {
        super(id, title, sources);
        this.yaxis = yaxis;
    }

    public String getYaxis() {
        return this.yaxis;
    }

    @Override
    public String getClazz() {
        return ScatterVisual.clazz;
    }
}
