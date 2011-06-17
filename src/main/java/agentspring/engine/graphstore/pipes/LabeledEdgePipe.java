package agentspring.engine.graphstore.pipes;

import java.util.NoSuchElementException;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.Pipeline;
import com.tinkerpop.pipes.filter.ComparisonFilterPipe;
import com.tinkerpop.pipes.pgm.EdgeVertexPipe;
import com.tinkerpop.pipes.pgm.LabelFilterPipe;
import com.tinkerpop.pipes.pgm.VertexEdgePipe;

public class LabeledEdgePipe extends AbstractPipe<Vertex, Vertex> implements Pipe<Vertex, Vertex> {

    public enum Step {
        OUT_IN, IN_OUT, BOTH_BOTH
    }

    Pipe<Vertex, Vertex> pipe;

    public LabeledEdgePipe(final String label, Step step) {
        super();
        Pipe<Vertex, Edge> edges = null;
        Pipe<Edge, Vertex> vertices = null;
        switch (step) {
        case OUT_IN:
            edges = new VertexEdgePipe(VertexEdgePipe.Step.OUT_EDGES);
            vertices = new EdgeVertexPipe(EdgeVertexPipe.Step.IN_VERTEX);
            break;
        case IN_OUT:
            edges = new VertexEdgePipe(VertexEdgePipe.Step.IN_EDGES);
            vertices = new EdgeVertexPipe(EdgeVertexPipe.Step.OUT_VERTEX);
            break;
        case BOTH_BOTH:
            edges = new VertexEdgePipe(VertexEdgePipe.Step.BOTH_EDGES);
            vertices = new EdgeVertexPipe(EdgeVertexPipe.Step.BOTH_VERTICES);
            break;
        default:
            break;
        }
        Pipe<Edge, Edge> edgeLabel = new LabelFilterPipe(label, ComparisonFilterPipe.Filter.NOT_EQUAL);
        pipe = new Pipeline<Vertex, Vertex>(edges, edgeLabel, vertices);
    }

    @Override
    protected Vertex processNextStart() throws NoSuchElementException {
        pipe.setStarts(this.starts);
        return pipe.next();
    }

}
