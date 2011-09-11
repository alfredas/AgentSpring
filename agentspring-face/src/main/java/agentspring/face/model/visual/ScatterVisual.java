package agentspring.face.model.visual;

import java.util.List;

import agentspring.face.model.Source;


public class ScatterVisual extends Visual {
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

    public ScatterVisual(Integer id, String title, int[] sources,  String yaxis) {
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
