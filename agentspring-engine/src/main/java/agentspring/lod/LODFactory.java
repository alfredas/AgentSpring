package agentspring.lod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.aspects.core.GraphBacked;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import agentspring.graphdb.NodeEntityHelper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class LODFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Neo4jTemplate template;
    
    static Logger logger = LoggerFactory.getLogger(LODFactory.class);
    static String ID_NAME = "x_id";


    @Override
    public void afterPropertiesSet() throws Exception {
        // get prefix defined in NodeEntityHelper    	
    	NodeEntityHelper helper = applicationContext.getParent().getBeansOfType(NodeEntityHelper.class).values().iterator().next();
    	String prefix = helper.getPrefix();
    	
    	// get the Neo4J Template for finding stuff
    	template = applicationContext.getParent().getBeansOfType(Neo4jTemplate.class).values().iterator().next();
    	
        if (template != null) {
            this.createObjects(prefix);
        }
    }

    /*
     * scan the basepackage for LODType annotated classes, scan their fields
     * annotated with LODProperty; construct queries based on those fields,
     * execute them and create class instances for each result.
     */
    private void createObjects(String prefix) {
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();

        // scan for classes annotated with LODType
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(prefix)))
                .setUrls(ClasspathHelper.getUrlsForPackagePrefix(prefix))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new ResourcesScanner()));

        Map<Class<?>, List<Class<?>>> depMap = new HashMap<Class<?>, List<Class<?>>>();

        // create dependency map by scanning classes and their fields that are
        // @RelatedTo other classes
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(LODType.class)) {
            List<Class<?>> deps = new ArrayList<Class<?>>();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(LODProperty.class) && field.isAnnotationPresent(RelatedTo.class)) {
                    deps.add(field.getType());
                }
            }
            depMap.put(clazz, deps);
        }

        // create the dependency list
        List<Class<?>> depList = new ArrayList<Class<?>>();

        // order the dependency list so that the classes with fewer (no)
        // dependencies are populated first and the classes that depend on them
        // later

        // create index map; indices correspond to the dependency length and
        // might be identical
        Map<Class<?>, Integer> indexMap = new HashMap<Class<?>, Integer>();
        for (Class<?> clazz : depMap.keySet()) {
            int index = findIndex(clazz, depMap) - 1;
            indexMap.put(clazz, index);
        }
        // create comparator based on index values
        ValueComparator comparator = new ValueComparator(indexMap);
        // sort by value using treemap
        TreeMap<Class<?>, Integer> sortedIdexMap = new TreeMap(comparator);
        sortedIdexMap.putAll(indexMap);

        // create a list of classes where dependent classes appear after their
        // dependencies
        depList.addAll(sortedIdexMap.keySet());

        // logger.info("DEPENDENCIES");
        // for (Class<?> clazz : depList) {
        // logger.info("class : {}", clazz);
        // }

        // for each class: construct query, execute it and create objects
        for (Class<?> clazz : depList) {
            // get annotation parameters
            LODType lodType = clazz.getAnnotation(LODType.class);
            String endpoint = lodType.endpoint();
            String type = lodType.type();
            String namespace = lodType.namespace();
            String[] filters = lodType.filters();
            String limit = lodType.limit();
            String query = lodType.query();

            // id field
            Field idField = null;

            // map: rdf property name <-> field name
            Map<String, String> fieldMap = new HashMap<String, String>();
            int index = 0;
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(LODProperty.class)) {
                    final LODProperty lodProperty = field.getAnnotation(LODProperty.class);
                    String propertyRDFname = lodProperty.value();
                    if (propertyRDFname.length() == 0) {
                        propertyRDFname = "property" + index;
                    }
                    String fieldName = field.getName();
                    fieldMap.put(propertyRDFname, fieldName);
                    index++;
                }
                if (field.isAnnotationPresent(LODId.class)) {
                    idField = field;
                }
            }

            if (idField == null) {
                logger.error("LODFactory error: LODId annotation not present for class {}", clazz);
                continue;
            }

            if (query.length() == 0) {
                // construct query
                query = this.constructQuery(namespace, type, filters, limit, fieldMap);
            }
            logger.info("Will execute query: {}", query);

            // for each result returned create an instance of the class and
            // populate it with data from the query
            for (Map<String, Object> resultMap : executeQuery(clazz, endpoint, query, fieldMap.values())) {
                try {
                    // create new instance of the class
                    Object obj = clazz.newInstance();
                    // iterate over results and set the values
                    for (Entry<String, Object> entry : resultMap.entrySet()) {
                        // fieldName
                        String fieldName = entry.getKey();
                        // value to be set
                        Object value = entry.getValue();

                        if (value != null) {
                            if (!fieldName.equals(ID_NAME)) {
                                // get the name of the setter method
                                String setterMethodName = createSetter(fieldName);
                                // make class primitive if it has a
                                // corresponding
                                // primitive type (eg Interger -> int)
                                Class<?> primitiveClass = getPrimitiveClass(value.getClass());
                                // get setter method
                                Method setter = clazz.getMethod(setterMethodName, primitiveClass);
                                // set value
                                setter.invoke(obj, value);
                            } else {
                                // get the name of the setter method
                                String setterMethodName = createSetter(idField.getName());
                                // get setter method
                                Method setter = clazz.getMethod(setterMethodName, String.class);
                                // set value
                                setter.invoke(obj, value.toString());
                            }
                        }
                    }
                    String beanId = resultMap.get(ID_NAME).toString();
                    // apply bean post processor
                    // (agentspring.PersistingBeanPostProcessor) - to store bean
                    // in the graphDB
                    factory.initializeBean(obj, beanId);
                } catch (Exception e) {
                    logger.error("Error creating instance of class " + clazz.getName(), e);
                }
            }

        }

    }

    /*
     * recursively create index for the class in the dependency chain the index
     * is equal to the maximum length of the dependency chain
     */
    private int findIndex(Class<?> clazz, Map<Class<?>, List<Class<?>>> depMap) {
        List<Class<?>> deps = depMap.get(clazz);
        int maxIndex = 0;
        for (Class<?> dep : deps) {
            maxIndex = Math.max(findIndex(dep, depMap), maxIndex);
        }
        return maxIndex + 1;
    }

    /*
     * construct query based on the fields and type provided
     */
    private String constructQuery(String namespace, String type, String[] filters, String limit, Map<String, String> fieldMap) {
        String query = "SELECT ?" + ID_NAME;
        for (String field : fieldMap.values()) {
            query += " ?" + field;
        }
        query += " WHERE {";
        query += " ?" + ID_NAME + " a " + getFullName(type, namespace) + "; ";
        for (Entry<String, String> entry : fieldMap.entrySet()) {
            query += getFullName(entry.getKey(), namespace) + " ?" + entry.getValue() + "; ";
        }
        query = query.substring(0, query.lastIndexOf(";")) + ". ";
        if (filters.length > 0) {
            query += " Filter(";
            for (String filter : filters) {
                query += " ?" + ID_NAME + " = " + getFullName(filter, namespace) + " ||";
            }
            query = query.substring(0, query.lastIndexOf("||")) + ") . ";
        }
        query += "} ";
        if (limit.length() > 0) {
            query += "LIMIT " + limit;
        }
        return query;
    }

    /*
     * execute results and return list of maps with results fieldName <-> value
     */
    private List<Map<String, Object>> executeQuery(Class<?> clazz, String endpoint, String queryString, Collection<String> fields) {

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            for (String field : fields) {
                if (result.get(field) == null) {
                    continue;
                }
                if (result.get(field).isLiteral()) {
                    // if result is literal then set value
                    Literal literal = result.getLiteral(field);
                    resultMap.put(field, literal.getValue());
                } else if (result.get(field).isResource() && !field.equals(ID_NAME)) {
                    // if result is a resource => try to get its value from the
                    // db of already persisted objects
                    // the dependency resolution should ensure that the object
                    // is already persisted
                    Resource resource = null;
                    try {
                        resource = result.getResource(field);
                    } catch (NullPointerException err) {
                    }
                    try {
                        if (resource != null) {
                            resultMap.put(field, findDbValue(clazz, field, resource.getURI()));
                        }
                    } catch (Exception err) {
                        logger.error("Error looking up id in the graphDB", err);
                    }
                }
            }
            // set unique id
            Resource id = result.getResource(ID_NAME);
            resultMap.put(ID_NAME, id.getURI());
            resultList.add(resultMap);
        }
        qexec.close();
        return resultList;
    }

    /*
     * look up a persisted value by ID.
     */
    @SuppressWarnings("unchecked")
    private <T extends GraphBacked<?, ?>> T findDbValue(Class<?> clazz, String fieldName, String id) throws SecurityException,
            NoSuchFieldException {
        // get the type (class) of the field
        Class<T> type = (Class<T>) clazz.getDeclaredField(fieldName).getType();
        Field idField = null;
        // find the ID field name of the related class
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(LODId.class)) {
                idField = field;
                break;
            }
        }
        // create a repository for the lookup
        GraphRepository<T> repo = this.template.repositoryFor(type);
        // return the lookup
        return repo.findByPropertyValue(idField.getName(), id);
    }

    /*
     * namespace the relative names
     */
    private String getFullName(String name, String namespace) {
        if (!name.startsWith("http://")) {
            return "<" + namespace + name + ">";
        }
        return "<" + name + ">";
    }

    /*
     * create setter name from field name
     */
    private String createSetter(String fieldName) {
        return "set" + String.valueOf(fieldName.charAt(0)).toUpperCase() + fieldName.substring(1);
    }

    /*
     * get corresponding primitive class if a class has one (eg Integer -> int)
     */
    private Class<?> getPrimitiveClass(Class<?> complex) {
        if (complex == Double.class) {
            return double.class;
        }
        if (complex == Integer.class) {
            return int.class;
        }
        if (complex == Long.class) {
            return long.class;
        }
        if (complex == Float.class) {
            return float.class;
        }
        return complex;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("rawtypes")
    class ValueComparator implements Comparator {

        @SuppressWarnings("rawtypes")
        Map base;

        public ValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if ((Integer) base.get(a) >= (Integer) base.get(b)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
