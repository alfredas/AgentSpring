package agentspring.face.gremlin;

import org.springframework.web.util.HtmlUtils;

import agentspring.facade.db.Source;

public class GremlinQuery extends AbstractGremlinQuery {

    private String query;

    public GremlinQuery(Integer id, String startNode, String query) {
        super(id, startNode);
        this.query = query;
    }

    public GremlinQuery(Source source) {
        super(source.getId(), source.getStart());
        this.query = HtmlUtils.htmlUnescape(source.getScript());
    }

    @Override
    public String getQuery() {
        return this.query;
    }

}
