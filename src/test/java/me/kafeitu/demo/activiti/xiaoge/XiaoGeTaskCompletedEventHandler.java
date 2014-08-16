package me.kafeitu.demo.activiti.xiaoge;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEntityWithVariablesEvent;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.event.logger.handler.AbstractTaskEventHandler;
import org.activiti.engine.impl.event.logger.handler.Fields;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.HashMap;
import java.util.Map;



/**
 * @author Joram Barrez
 */
public class XiaoGeTaskCompletedEventHandler extends AbstractTaskEventHandler {

    private static void println(String msg) {
        System.out.println(msg);
    }

    private static void println(int n) {
        System.out.println(n);
    }

    private static RepositoryService repositoryService = null;
    private static RuntimeService runtimeService = null;
    private static ProcessDefinitionEntity processDefinition = null;
    private static String processDefId = "";

    @Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
        if(repositoryService == null) {
            ProcessEngineImpl processEngine =(ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
            repositoryService = processEngine.getRepositoryService();
            runtimeService  = processEngine.getRuntimeService();
        }

	  ActivitiEntityWithVariablesEvent eventWithVariables = (ActivitiEntityWithVariablesEvent) event;
		TaskEntity task = (TaskEntity) eventWithVariables.getEntity();
		Map<String, Object> data = handleCommonTaskFields(task);
//		println("complete: " + task.getId());

        if(!task.getProcessDefinitionId().equals(processDefId)) {
            processDefId = task.getProcessDefinitionId();
            processDefinition =(ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefId);
        }
//        ExecutionEntity ee = (ExecutionEntity)runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        ActivityImpl ai = processDefinition.findActivity(task.getTaskDefinitionKey());


        long duration = timeStamp.getTime() - task.getCreateTime().getTime();
		putInMapIfNotNull(data, Fields.DURATION, duration);
		
		if (eventWithVariables.getVariables() != null && eventWithVariables.getVariables().size() > 0) {
		  Map<String, Object> variableMap = new HashMap<String, Object>();
		  for (Object variableName : eventWithVariables.getVariables().keySet()) {
        putInMapIfNotNull(variableMap, (String) variableName, eventWithVariables.getVariables().get(variableName));
      }
		  if (eventWithVariables.isLocalScope()) {
		    putInMapIfNotNull(data, Fields.LOCAL_VARIABLES, variableMap);
		  } else {
		    putInMapIfNotNull(data, Fields.VARIABLES, variableMap);
		  }
		}

    return createEventLogEntry(task.getProcessDefinitionId(), task.getProcessInstanceId(), task.getExecutionId(), task.getId(), data);
	}

}
