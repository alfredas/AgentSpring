package agentspring.service;

import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.data.neo4j.annotation.NodeEntity;

public class NodeEntityHelper {
    
    String prefix;
    
    Map<String, String> nodeEntityMap;
    
    private Map<String, String> createNodeEntityMap() {
        Map<String, String> map = new HashMap<String, String>();
        
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
