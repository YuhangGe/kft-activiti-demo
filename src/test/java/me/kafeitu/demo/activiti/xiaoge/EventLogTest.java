package me.kafeitu.demo.activiti.xiaoge;

import me.kafeitu.modules.test.spring.SpringTransactionalTestCase;
import org.activiti.engine.*;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import javax.sql.DataSource;
//import org.activiti.engine.impl.context.Context;
//import org.activiti.engine.impl.db.DbSqlSession;
//import org.activiti.engine.impl.interceptor.CommandContext;

/**
 * 测试请假流程定义
 *
 * @author HenryYan
 */
@ContextConfiguration(locations = {"/applicationContext-test.xml"})
public class EventLogTest extends SpringTransactionalTestCase {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagementService managementService;

    private static void println(String msg) {
        System.out.println(msg);
    }

    private static void println(int n) {
        System.out.println(n);
    }

    @Test
    public void testEventLog() throws Exception {
        deployResources();

        ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();

        XiaoGeEventLogger databaseEventLogger = new XiaoGeEventLogger(processEngineConfiguration.getClock());
        runtimeService.addEventListener(databaseEventLogger);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess");

        assertNotNull(processInstance.getId());
        println(processInstance.getId());

        List<Task> list = taskService.createTaskQuery().list();
        assertEquals(1, list.size());

        println(list.get(0).getId());
        println(list.get(0).getName());
        HashMap<String, Object> varMap = new HashMap<String, Object>();
        varMap.put("chooice", 2);

        taskService.complete(list.get(0).getId(), varMap);

        List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);
        EventLogEntry ev = eventLogEntries.get(0);
        println(ev.getType());
        println(eventLogEntries.size());

        List<Execution> eL = runtimeService.createExecutionQuery().list();
        println(eL.size());

        ExecutionEntity ex = (ExecutionEntity) eL.get(0);
        println(ex.getActivityId());


    }

    /**
     * 部署流程资源：bpmn、form
     */
    private void deployResources() throws FileNotFoundException {
        // 部署流程
        String processFilePath = this.getClass().getClassLoader().getResource("diagrams/test.bpmn").getPath();
        FileInputStream inputStream = new FileInputStream(processFilePath);
        assertNotNull(inputStream);
        repositoryService.createDeployment().addInputStream("test.bpmn20.xml", inputStream).deploy();
    }

}
