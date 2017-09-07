package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.TemplateSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.TemplateRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.TemplateService;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * Created by Venkatareddy on 08/18/2017.
 * @see TemplateService
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository repository;
    
    @Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;

	@Autowired
	private VariableService variableService;

	@Override
	public TemplateRequest createTemplate(TemplateRequest templateReq) {
		//Set<String> variables = getVariables(templateReq.getSource(), templateReq.getOwnerName());
        Template created = repository.save(
        		TemplateBuilder.constructTemplate(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), templateReq));
		return createTemplateRequest(created);
	}

	@Override
	public TemplateRequest updateTemplate(TemplateRequest templateReq) throws CaptureValidationException {
		Template existing = getTemplateObject(templateReq.getId());
		if(Objects.isNull(existing)) {
			return null;
		}
//		Set<String> variables = getVariables(templateReq.getSource(), existing.getCreatedBy());
        Template created = repository.save(TemplateBuilder.updateTemplate(existing, templateReq));
		return createTemplateRequest(created);
	}

	@Override
	public void deleteTemplate(String templateId) {
		repository.delete(templateId);
	}

	protected Template getTemplateObject(String templateId) {
		return repository.findOne(templateId);
	}

	@Override
	public TemplateRequest getTemplate(String templateId) {
		Template one = repository.findOne(templateId);
		return createTemplateRequest(one);
	}

	@Override
	public TemplateSearchList getTemplates(String userName, Integer offset, Integer limit) {
		//TODO, check condition on user who should be admin to execute this operation.
		Page<Template> templatePage = repository.findAll(getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getUserTemplates(String userName, Integer offset, Integer limit) {
		Page<Template> templatePage = repository.findByCreatedBy(userName, getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getTemplatesByProject(Long projectId, Integer offset, Integer limit) {
		Page<Template> templatePage = repository.findByProjectId(projectId, getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getSharedTemplates(String userName, Integer offset, Integer limit) {
		Page<Template> templatePage = repository.findBySharedAndCreatedBy(true, userName, getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getFavouriteTemplates(String owner, Integer offset, Integer limit) {
		Page<Template> templatePage = repository.findByFavouriteAndCreatedBy(true, owner, getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	protected Page<Template> getUserTemplateObjects(String userName, Integer offset, Integer limit) {
		return repository.findByCreatedBy(userName, getPageRequest(offset, limit));
	}

	/**
	 * Creates the page request object for pagination.
	 * 
	 * @param offset -- Offset position to start
	 * @param limit -- Number of records to return
	 * @return -- Returns the page request object.
	 */
	private PageRequest getPageRequest(Integer offset, Integer limit) {
		return new PageRequest((offset == null ? 0 : offset), (limit == null ? 20 : limit));
	}

	/**
	 * Convert List<Template> to List<TemplateRequest>
	 * @param templatePage
	 * @return
	 */
	private TemplateSearchList convert(Page<Template> templatePage, Integer offset, Integer limit) {
		List<TemplateRequest> returnList = new ArrayList<>();
		if (templatePage != null && templatePage.getContent().size() > 0) {
			Project project = getProject(templatePage.getContent().get(0).getProjectId());
			Map<String, CaptureUser> userMap = getUserMap(templatePage.getContent());
			templatePage.getContent().forEach(template -> {
				CaptureUser user = userMap.get(template.getCreatedBy());
				returnList.add(TemplateBuilder.createTemplateRequest(template, project, user));
			});
			return new TemplateSearchList(returnList, offset, limit, templatePage.getTotalElements());
		}
		return new TemplateSearchList(returnList, offset, limit, 0);
	}
	
	private Map<String, CaptureUser> getUserMap(List<Template> templateList) {
		Map<String, CaptureUser> userMap = new HashMap<>();
		templateList.forEach(t -> 
				userMap.put(t.getCreatedBy(), userService.findUser(t.getCreatedBy())));
		return userMap;
	}

	protected Project getProject(Long projectId){
		return projectService.getProjectObj(projectId);
	}

	private TemplateRequest createTemplateRequest(Template created) {
		return TemplateBuilder.createTemplateRequest(created, getProject(created.getProjectId())
				, userService.findUser(created.getCreatedBy()));
	}

	protected Set<String> getVariables(JsonNode json, String userName){
		Set<String> variableNames = variableService.parseVariables(json);
		List<Variable> variableList = variableService.getVariables(userName).getContent();
		return variableList
				.stream()
				.filter( var -> variableNames.contains(var.getName().toLowerCase()))
				.map( var -> var.getId())
				.collect(Collectors.toSet());
	}
	
	protected List<Variable> getUserVariables(String userName){
		return variableService.getVariables(userName).getContent();
	}

}
