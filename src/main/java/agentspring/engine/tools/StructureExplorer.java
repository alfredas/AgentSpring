package agentspring.engine.tools;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.data.graph.annotation.NodeEntity;
import org.springframework.data.graph.annotation.RelatedTo;
import org.springframework.data.graph.core.Direction;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class StructureExplorer {

    public StructureExplorer() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix("")))
                .setUrls(ClasspathHelper.getUrlsForPackagePrefix(""))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new ResourcesScanner()));

        Map<String, SimpleNode> nodes = new HashMap<String, SimpleNode>();
        Map<String, SimpleEdge> edges = new HashMap<String, SimpleEdge>();

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(NodeEntity.class)) {
            SimpleNode node = new SimpleNode(clazz);
            nodes.put(node.toString(), node);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(RelatedTo.class)) {
                    final RelatedTo relatedTo = field.getAnnotation(RelatedTo.class);
                    Type type = field.getGenericType();
                    Class<?> fieldClass = null;
                    if (type instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType)type;
                        fieldClass = (Class<?>) pType.getActualTypeArguments()[0];
                    } else {
                        fieldClass = field.getType();
                    }
                    SimpleEdge edge = new SimpleEdge(clazz, relatedTo, fieldClass);
                    edges.put(edge.getName(), edge);
                    System.out.println(edge);
                }
            }
            Class<?> superclass = clazz.getSuperclass();
            if (superclass.isAnnotationPresent(NodeEntity.class)) {
                for (Field field : superclass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(RelatedTo.class)) {
                        RelatedTo relatedTo = field.getAnnotation(RelatedTo.class);
                        Type type = field.getGenericType();
                        Class<?> fieldClass = null;
                        if (type instanceof ParameterizedType) {
                            ParameterizedType pType = (ParameterizedType)type;
                            fieldClass = (Class<?>) pType.getActualTypeArguments()[0];
                        } else {
                            fieldClass = field.getType();
                        }
                        SimpleEdge edge = new SimpleEdge(clazz, relatedTo, fieldClass);
                        edge.setInherited(true);
                        edges.put(edge.getName(), edge);
                    }
                }
            }
        }
        Graph<SimpleNode, SimpleEdge> graph = new DirectedSparseMultigraph<SimpleNode, SimpleEdge>();
        for (SimpleNode node : nodes.values()) {
            graph.addVertex(node);
        }
        for (SimpleEdge edge : edges.values()) {
            graph.addEdge(edge, nodes.get(edge.getFrom()), nodes.get(edge.getTo()));
        }
        showGraph(graph);
    }

    public static void main(String args[]) {
        new StructureExplorer();
    }

    private void showGraph(Graph<SimpleNode, SimpleEdge> g) {

        Layout<SimpleNode, SimpleEdge> layout = new ISOMLayout<SimpleNode, SimpleEdge>(g);
        layout.setSize(new Dimension(500, 500));
        VisualizationViewer<SimpleNode, SimpleEdge> vv = new VisualizationViewer<SimpleNode, SimpleEdge>(layout);
        vv.setPreferredSize(new Dimension(600, 600));
        float dash[] = { 10.0f };
        final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
        Transformer<SimpleEdge, Stroke> edgeStrokeTransformer = new Transformer<SimpleEdge, Stroke>() {
            public Stroke transform(SimpleEdge e) {
                if (e.isInherited()) {
                    return edgeStroke;
                }
                return new BasicStroke();
            }
        };

        Transformer<SimpleNode, Shape> vertexShapeTransformer = new Transformer<StructureExplorer.SimpleNode, Shape>() {

            @Override
            public Shape transform(SimpleNode input) {
                int width = 100;
                int height = 30;
                return new Ellipse2D.Double((-width / 2), (-height / 2), width, height);
            }
        };

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<SimpleNode>());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<SimpleEdge>());
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

        DefaultModalGraphMouse<SimpleNode, SimpleEdge> gm = new DefaultModalGraphMouse<SimpleNode, SimpleEdge>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);

        JFrame frame = new JFrame("Model World");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    class SimpleNode {

        Class<?> clazz;

        public SimpleNode(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return this.clazz.getSimpleName();
        }
    }

    class SimpleEdge {

        RelatedTo relatedTo;
        String from;
        String to;
        boolean inherited = false;

        public SimpleEdge(Class<?> clazz, RelatedTo relatedTo, Class<?> clazzTo) {
            this.relatedTo = relatedTo;
            if (relatedTo.direction() == Direction.INCOMING) {
                to = clazz.getSimpleName();
                from = clazzTo.getSimpleName();
            } else {
                from = clazz.getSimpleName();
                to = clazzTo.getSimpleName();
            }
            System.out.println("from " + from);
            System.out.println("to " + to);
        }

        @Override
        public String toString() {
            return this.relatedTo.type();
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public boolean isInherited() {
            return inherited;
        }

        public void setInherited(boolean inherited) {
            this.inherited = inherited;
        }

        public String getName() {
            return getFrom() + toString() + getTo();
        }

    }

}
