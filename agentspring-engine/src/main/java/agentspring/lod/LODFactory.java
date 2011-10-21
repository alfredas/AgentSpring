package agentspring.lod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

public class LODFactory implements InitializingBean, ApplicationContextAware {

    String basepackage;

    ApplicationContext applicationContext;

    static Logger logger = LoggerFactory.getLogger(LODFactory.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        this.createObjects();
    }

    /*
     * scan the basepackage for LODType annotated classes, scan their fields
     * annotated with LODProperty; construct queries based on those fields,
     * execute them and create class instances for each result.
     */
    private void createObjects() {
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();

        // scan for classes annotated with LODType
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(this.getBasepackage())))
                .setUrls(ClasspathHelper.getUrlsForPackagePrefix(this.getBasepackage()))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new ResourcesScanner()));

        // for each class: construct query, execute it and create objects
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(LODType.class)) {

            // get annotation parameters
            LODType lodType = clazz.getAnnotation(LODType.class);
            String endpoint = lodType.endpoint();
            String type = lodType.type();
            String namespace = lodType.namespace();
            String[] filters = lodType.filters();
            String limit = lodType.limit();

            // map: rdf property name <-> field name
            Map<String, String> fieldMap = new HashMap<String, String>();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(LODProperty.class)) {
                    final LODProperty lodProperty = field.getAnnotation(LODProperty.class);
                    String propertyRDFname = lodProperty.value();
                    String fieldName = field.getName();
                    fieldMap.put(propertyRDFname, fieldName);
                }
            }

            // construct query
            String query = this.constructQuery(namespace, type, filters, limit, fieldMap);
            logger.info("Will execute query: {}", query);

            // for each result returned create an instance of the class and
            // populate it with data from the query
            int index = 0;
            for (Map<String, Object> resultMap : executeQuery(endpoint, query, fieldMap.values())) {
                try {
                    // create new instance of the class
                    Object obj = clazz.newInstance();
                    // iterate over results and set the values
                    for (Entry<String, Object> entry : resultMap.entrySet()) {
                        // fieldName
                        String fieldName = entry.getKey();
                        // value to be set
                        Object value = entry.getValue();
                        // get the name of the setter method
                        String setterMethodName = createSetter(fieldName);
                        // make class primitive if it has a corresponding
                        // primitive type (eg Interger -> int)
                        Class<?> primitiveClass = getPrimitiveClass(value.getClass());
                        // get setter method
                        Method setter = clazz.getMethod(setterMethodName, primitiveClass);
                        // set value
                        setter.invoke(obj, value);
                    }
                    // apply bean post processor
                    // (agentspring.PersistingBeanPostProcessor) - to store bean
                    // in the graphDB
                    factory.applyBeanPostProcessorsAfterInitialization(obj, clazz.getName() + "#" + index);
                    index++;
                } catch (Exception e) {
                    logger.error("Error creating instance of class " + clazz.getName(), e);
                }
            }

        }

    }

    /*
     * construct query based on the fields and type provided
     */
    private String constructQuery(String namespace, String type, String[] filters, String limit, Map<String, String> fieldMap) {
        String query = "SELECT";
        for (String field : fieldMap.values()) {
            query += " ?" + field;
        }
        query += " WHERE {";
        query += " ?x a " + getFullName(type, namespace) + "; ";
        for (Entry<String, String> entry : fieldMap.entrySet()) {
            query += getFullName(entry.getKey(), namespace) + " ?" + entry.getValue() + "; ";
        }
        query = query.substring(0, query.lastIndexOf(";")) + ". ";
        if (filters.length > 0) {
            query += " Filter(";
            for (String filter : filters) {
                query += " ?x = " + getFullName(filter, namespace) + " ||";
            }
            query = query.substring(0, query.lastIndexOf("||")) + ") . } ";
        }
        if (limit.length() > 0) {
            query += "LIMIT " + limit;
        }
        return query;
    }

    /*
     * execute results and return list of maps with results fieldName <-> value
     */
    private List<Map<String, Object>> executeQuery(String endpoint, String queryString, Collection<String> fields) {

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            Map<String, Object> resultMap = new HashMap<String, Object>();
            for (String field : fields) {
                Literal literalValue = result.getLiteral(field);
                resultMap.put(field, literalValue.getValue());
            }
            resultList.add(resultMap);
        }
        qexec.close();
        return resultList;
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

    public String getBasepackage() {
        return basepackage;
    }

    public void setBasepackage(String basepackage) {
        this.basepackage = basepackage;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
