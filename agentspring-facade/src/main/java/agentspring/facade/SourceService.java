package agentspring.facade;

import java.util.List;

import agentspring.facade.db.Source;

public interface SourceService {

    public void delete(int id);

    public int saveSource(Source source);

    public Source getSource(int id);

    public List<Source> listSources();

}
