package uk.gov.digital.ho.hocs.workflow.client.camundaclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CamundaMigrationClient {

  private final RuntimeService runtimeService;
  private final RepositoryService repositoryService;

  @Autowired
  public CamundaMigrationClient(RuntimeService runtimeService, RepositoryService repositoryService) {
    this.runtimeService = runtimeService;
    this.repositoryService = repositoryService;
  }

  public  List<ProcessInstance> calculateCaseExecutionList(String processDefinitionKey) {
    return runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
  }

  public List<ProcessDefinition> processDefinitionList(String processDefinitionKey) {
    return repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .orderByProcessDefinitionVersion().asc()
        .list();
  }

  public Map<String, Long> calculateCheckSumMap(String processDefinitionKey) throws IOException {
    Map<String, Long> checkSumMap = new HashMap<>();
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).orderByProcessDefinitionVersion().asc().list();
    for (ProcessDefinition pd : processDefinitionList) {
      InputStream xmlInputStream = repositoryService.getProcessModel(pd.getId());
      String xml = readXml(xmlInputStream);
      long checkSum = calculateChecksum(xml.getBytes());
      checkSumMap.put(pd.getId(), checkSum);
    }
    return checkSumMap;
  }

  public Map<String, Deployment> calculateDeploymentMap() {
    Map<String, Deployment> deploymentMap = new HashMap<>();
    List<Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    for (Deployment deployment : deploymentList) {
      deploymentMap.put(deployment.getId(), deployment);
    }
    return deploymentMap;
  }

  public SortedMap<String, List<String>> calculateExecutionKeyMap(List<ProcessInstance> caseExecutionsList) {
    SortedMap<String, List<String>> executionKeyMap = new TreeMap<>();
    for (ProcessInstance processInstance : caseExecutionsList) {
      String definitionId = processInstance.getProcessDefinitionId();
      List<String> keyMap = executionKeyMap.get(definitionId);
      if (keyMap == null) {
        List<String> newList = new ArrayList<>();
        executionKeyMap.put(definitionId, newList);
        newList.add(processInstance.getBusinessKey());
      } else {
        keyMap.add(processInstance.getBusinessKey());
      }
    }
    return executionKeyMap;
  }

  private String readXml(InputStream inputStream) throws IOException {
    StringBuilder textBuilder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader
        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
      int c;
      while ((c = reader.read()) != -1) {
        textBuilder.append((char) c);
      }
    }
    return textBuilder.toString();
  }

  private long calculateChecksum(byte[] bytes) {
    Checksum crc32 = new CRC32();
    crc32.update(bytes, 0, bytes.length);
    return crc32.getValue();
  }
}
