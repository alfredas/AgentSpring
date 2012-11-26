package example.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import example.domain.things.Stuff;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/example-test-context.xml"})
@Transactional
public class DomainTest {
    
    @Autowired Neo4jOperations template;
	
    @Before
    @Transactional
    public void setUp() throws Exception {
    }
    
    @Test
    public void createNodesWithTemplateCreateNodeAsAndCheckNumber(){
    	
    	template.createNodeAs(Stuff.class, null);
    	
    	assertEquals("Check if stuff are equal if template.createNodeAs: ", 1, template.count(Stuff.class));
    	
    }
    
    @Test
    public void createNodesWithTemplateSaveAndCheckNumber(){
    	
    	Stuff stuff1 = new Stuff();
    	
    	template.save(stuff1);
    	
    	assertEquals("Check if stuff are equal if template.createNodeAs: ", 1, template.count(Stuff.class));
    	
    }
    

}	
