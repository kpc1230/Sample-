package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.TemplateSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.TemplateRepository;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.TemplateService;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import  java.util.Collections;

/**
 * Created by Venkatareddy on 08/18/2017.
 * @see TemplateService
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository repository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
	@Autowired
	private VariableService variableService;
	@Autowired
	PermissionService permissionService;

	@Override
	public TemplateRequest createTemplate(TemplateRequest templateReq) {
		//Set<String> variables = getVariables(templateReq.getSource(), templateReq.getOwnerName());
        Template created = repository.save(
        		TemplateBuilder.constructTemplate(CaptureUtil.getCurrentCtId(), templateReq));
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
	public TemplateRequest getTemplate(String user,String templateId) {
		Template one = repository.findOne(templateId);
		return one == null ? null : createTemplateRequest(one);
	}

	@Override
	public TemplateSearchList getTemplates(String userName, Integer offset, Integer limit) {
		//TODO, check condition on user who should be admin to execute this operation.
		Page<Template> templatePage = repository.findByCtId(CaptureUtil.getCurrentCtId(),getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getUserTemplates(AcHostModel acHostModel, String userKey, Integer offset, Integer limit, Boolean mine) throws Exception {
		//Since this Crud repository doesn't support OR query we had to make 2 calls
		Page<Template> createdBy = repository.findByCtIdAndCreatedBy(CaptureUtil.getCurrentCtId(), userKey, getPageRequest(offset, limit));
		Page<Template> shared;
		if(mine){
			shared = repository.findByCtIdAndSharedAndCreatedBy(CaptureUtil.getCurrentCtId(),true, userKey, getPageRequest(offset, limit));
		}else{
			shared = repository.findByCtIdAndShared(CaptureUtil.getCurrentCtId(),true, getPageRequest(offset, limit));
		}

		return mergeTemplates(createdBy, shared, offset, limit);
	}

	@Override
	public TemplateSearchList getTemplatesByProject(Long projectId, Integer offset, Integer limit) {
		Page<Template> templatePage = repository.findByCtIdAndProjectId(CaptureUtil.getCurrentCtId(),projectId, getPageRequest(offset, limit));
		return convert(templatePage, offset, limit);
	}

	@Override
	public TemplateSearchList getSharedTemplates(String userName, Integer offset, Integer limit) throws Exception {
		Page<Template> templatePage = repository.findByCtIdAndShared(CaptureUtil.getCurrentCtId(),true, getPageRequest(offset, limit));
        ArrayList<BasicProject> projects = projectService.getProjects();
        Map<Long, BasicProject> projectsMap = new TreeMap<>();
        projects.forEach(basicProject -> {projectsMap.put(basicProject.getId(), basicProject);});
        List<Template> templateList = filterSharedTemplateByAccessedProjects(templatePage, projectsMap);
        templateList = templateList != null?templateList:new ArrayList<>();

        return createSearchList(templateList, projectsMap, offset, limit);
	}

	@Override
	public TemplateSearchList getFavouriteTemplates(String owner, Integer offset, Integer limit) throws Exception {
		Page<Template> shared = repository.findByCtIdAndFavouriteAndShared(CaptureUtil.getCurrentCtId(),true, true, getPageRequest(offset, limit));
		Page<Template> createdBy = repository.findByCtIdAndFavouriteAndCreatedBy(CaptureUtil.getCurrentCtId(),true, owner, getPageRequest(offset, limit));
		return mergeTemplates(createdBy, shared, offset, limit);
	}
	
	private TemplateSearchList mergeTemplates(Page<Template> createdBy, Page<Template> shared, Integer offset, Integer limit) throws Exception {
	    Set<Template> combinedTemplateSet = new TreeSet<>(new Comparator<Template>() {
            @Override
            public int compare(Template template1, Template template2) {
                return  template1.getTimeCreated().compareTo(template2.getTimeCreated())*(-1);
            }
        });
	    ArrayList<BasicProject> projects = projectService.getProjects();
        Map<Long, BasicProject> projectsMap = new TreeMap<>();
        projects.forEach(basicProject -> {projectsMap.put(basicProject.getId(), basicProject);});

        List<Template> sharedFilteredList = filterSharedTemplateByAccessedProjects(shared, projectsMap);
        combinedTemplateSet.addAll(sharedFilteredList);
        if(createdBy != null && createdBy.getContent().size() > 0){
            combinedTemplateSet.addAll(createdBy.getContent());
        }

        return createSearchList(combinedTemplateSet, projectsMap, offset, limit);
	}

	private List<Template> filterSharedTemplateByAccessedProjects(Page<Template> shared, Map<Long, BasicProject> projectsMap) {
        if(shared == null || shared.getContent().size() == 0){
            return new ArrayList<Template>();
        }
        //Filter shared templates, include only that to which projects user has access.
        return shared.getContent().stream().filter(template -> projectsMap.get(template.getProjectId()) !=null).collect(Collectors.toList());
    }

    private TemplateSearchList createSearchList(Collection<Template> templates, Map<Long, BasicProject> projectsMap, Integer offset, Integer limit){
        List<TemplateRequest> templateRequestList = new ArrayList<>();
        if(templates == null || templates.size() == 0) {
           return new TemplateSearchList(templateRequestList, offset, limit, 0);
        }
        templates.forEach(template -> {
            if(template != null && template.getCreatedBy() != null && template.getProjectId() != null) {
                CaptureUser user = userService.findUserByKey(template.getCreatedBy());
                BasicProject basicProject = projectsMap.get(template.getProjectId());
                String key;
                if(basicProject == null){
                    CaptureProject captureProject = projectService.getCaptureProject(template.getProjectId());
                    key = captureProject != null?captureProject.getKey():null;
                } else {
                    key = basicProject.getKey();
                }
                if (StringUtils.isNotBlank(key)){
                    TemplateRequest templateRequest = TemplateBuilder.createTemplateRequest(template, key, user);
                    templateRequestList.add(templateRequest);
                }
            }
        });
        Collections.sort(templateRequestList, new Comparator<TemplateRequest>() {
            @Override
            public int compare(TemplateRequest templateRequest1, TemplateRequest templateRequest2) {
                return templateRequest1.getTimeCreated().compareTo(templateRequest2.getTimeCreated())*(-1);
            }
        });

        return new TemplateSearchList(templateRequestList, offset, limit, templates.size());
    }

	protected Page<Template> getUserTemplateObjects(String userName, Integer offset, Integer limit) {
		return repository.findByCtIdAndCreatedBy(CaptureUtil.getCurrentCtId(),userName, getPageRequest(offset, limit));
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
			Map<String, CaptureUser> userMap = getUserMap(templatePage.getContent());
			templatePage.getContent().forEach(template -> {
				Project project = getProject(template.getProjectId());
				CaptureUser user = userMap.get(template.getCreatedBy());
				if(permissionService.canUseTemplate(user.getKey(), template.getProjectId())){
					returnList.add(TemplateBuilder.createTemplateRequest(template, project, user));
				}
			});
			return new TemplateSearchList(returnList, offset, limit, templatePage.getTotalElements());
		}
		return new TemplateSearchList(returnList, offset, limit, 0);
	}
	
	private Map<String, CaptureUser> getUserMap(List<Template> templateList) {
		Map<String, CaptureUser> userMap = new HashMap<>();
		templateList.forEach(t -> {
				CaptureUser user = userService.findUserByKey(t.getCreatedBy());
						if(user != null) {
							userMap.put(t.getCreatedBy(), user);
						}
				}
			);
		return userMap;
	}

	protected Project getProject(Long projectId){
		return projectService.getProjectObj(projectId);
	}

	private TemplateRequest createTemplateRequest(Template created) {
		return TemplateBuilder.createTemplateRequest(created, getProject(created.getProjectId())
				, userService.findUserByKey(created.getCreatedBy()));
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
	
	public void deleteTemplatesByCtIdAndProject(String ctId, Long projectId) {
		int index = 0, maxLimit = 10;
		Page<Template> templatePage = repository.findByCtIdAndProjectId(ctId, projectId, getPageRequest(index, maxLimit));
		for(Template template : templatePage.getContent()) {
          	deleteTemplate(template.getId());
        }
		long totalCount = templatePage.getTotalElements();
		int loopCount = ((int) totalCount / maxLimit);
        while(loopCount-- > 0) {
        	templatePage = repository.findByCtIdAndProjectId(ctId,projectId, getPageRequest(index, maxLimit));
        	for(Template template : templatePage.getContent()) {
              	deleteTemplate(template.getId());
            }
        }
	}

}
