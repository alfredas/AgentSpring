package agentspring.facade;

import java.util.List;

import agentspring.facade.db.Visual;
import agentspring.facade.visual.ChartVisual;
import agentspring.facade.visual.ScatterVisual;

public interface VisualService {

    public Visual getVisual(int id);

    public List<Visual> getVisualsForSource(int id);

    public List<Visual> listFullVisuals();

    public int saveVisual(Visual visual);

    public int saveChartVisual(ChartVisual visual);

    public int saveScatterVisual(ScatterVisual visual);

    public void delete(int id);

}
