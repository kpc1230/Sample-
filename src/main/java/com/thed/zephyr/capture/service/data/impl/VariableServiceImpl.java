package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.model.VariableRequest;
import com.thed.zephyr.capture.model.util.VariableSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.TemplateRepository;
import com.thed.zephyr.capture.repositories.dynamodb.VariableRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DefaultVariables;

/**
 * @author Venkatareddy on 08/24/2017.
 * @see VariableService
 */
@Service
public class VariableServiceImpl implements VariableService {

	@Autowired
	private VariableRepository repository;

	@Autowired
	private TemplateRepository templateRepository;

    @Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;

	@Override
	public void createVariable(VariableRequest input) throws CaptureValidationException {
		List<Variable> list = getVariableObjects(input.getOwnerName(), null, null).getContent();
		Variable existing = list.stream().filter(var -> var.getName().equals(input.getName())).findFirst().orElse(null);
		if (existing != null) {
			throw new CaptureValidationException("Variable already exists");
		}
		Variable newVariable = new Variable(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), 
				input.getOwnerName(), input.getName(), input.getValue());
		repository.save(newVariable);
	}

	@Override
	public VariableSearchList getVariables(String userName){
		return getVariables(userName, null, null);
	}

	@Override
	public VariableSearchList getVariables(String ownerName, Integer offset, Integer limit) {
		PageRequest page = getPageRequest(offset, limit);
		Page<Variable> variablePage = getVariableObjects(ownerName, offset, limit);
		List<Variable> list = variablePage.getContent();
		if(list == null || list.size() == 0){
			list = createDefaultVariables(ownerName);
			page = getPageRequest(null, null);
		}

		return new VariableSearchList(list, page.getPageNumber(), page.getPageSize(), list.size());
	}

	/**
	 * This method returns all the Variable objects as it is from database.
	 * @param ownerName
	 * @param offset
	 * @param limit
	 * @return
	 */
	protected Page<Variable> getVariableObjects(String ownerName, Integer offset, Integer limit){
		return repository.findByCtIdAndOwnerName(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), 
				ownerName, getPageRequest(offset, limit));
	}

	/**
	 * Create default Varable s using the predefined key-value pair and store them in database.
	 * @param ownerName
	 * @return
	 */
	private List<Variable> createDefaultVariables(String ownerName) {
		List<Variable> variableList = new ArrayList<>();
        for (String name : DefaultVariables.DEFAULT_VARIABLES.keySet()) {
            Variable var = new Variable(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), ownerName, name, 
            		DefaultVariables.DEFAULT_VARIABLES.get(name));
            repository.save(var);
//            templateRepository.variableUpdated(var, ownerName);
            variableList.add(var);
        }
		return variableList;
	}

	@Override
	public void updateVariable(VariableRequest input) throws CaptureValidationException {
		List<Variable> list = getVariableObjects(input.getOwnerName(), null, null).getContent();
		Variable existing = list.stream().filter(var -> var.getId().equals(input.getId())).findFirst().orElse(null);
		if (existing == null) {
			throw new CaptureValidationException("Variable not exists");
		}
		existing.setName(input.getName());
		existing.setValue(input.getValue());
		repository.save(existing);
		//TODO, templateService.updateVariable();
	}

	@Override
	public void deleteVariable(VariableRequest input) throws CaptureValidationException {
		repository.delete(input.getId());
		//TODO, templateService.deleteVariable();
	}

    @Override
    public Set<String> parseVariables(JsonNode nodeData) {
        Set<String> tagList = new TreeSet<>();
        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Iterator<String> iterator = nodeData.fieldNames();
        while(iterator.hasNext()){
        	JsonNode node = nodeData.get(iterator.next());
	        Matcher matcher = pattern.matcher((node.isObject() ? node.get("value").asText() : node.asText()));
	        String tagName;
	        while (matcher.find()) {
	            String originalMatch = matcher.group(0);
	            tagName = originalMatch.toUpperCase();
	            tagList.add(tagName.substring(1, tagName.length() -1 ));
	        }
        }
        return tagList;
    }
	/**
	 * Creates the page request object for pagination.
	 * 
	 * @param offset -- Offset position to start
	 * @param limit -- Number of records to return
	 * @return -- Returns the page request object.
	 */
	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((offset == null ? 0 : offset), (limit == null ? 10 : limit));
	}
}
