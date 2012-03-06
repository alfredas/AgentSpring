package agentspring.graphdb;

import java.util.Map;
import java.util.TreeMap;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Scans the provided prefix for classes annotated with NodeEntity and saves their names and fullnames in a map
 * @author alfredas
 *
 */
public class NodeEntityHelper {

    String prefix;

    Map<String, String> nodeEntityMap;

    static Logger logger = LoggerFactory.getLogger(NodeEntityHelper.class);

    private Map<String, String> createNodeEntityMap() {
        Map<String, String> map = new TreeMap<String, String>();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(prefix)))
                .setUrls(ClasspathHelper.getUrlsForPackagePrefix(prefix))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new ResourcesScanner()));

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(NodeEntity.class)) {
            map.put(clazz.getSimpleName(), clazz.getName());
        }
        return map;
    }

    public Map<String, String> getNodeEntityMap() {
        if (nodeEntityMap == null) {
            nodeEntityMap = createNodeEntityMap();
        }
        return nodeEntityMap;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
