package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.util.TemplateSearchList;
import com.thed.zephyr.capture.repositories.dynamodb.TemplateRepository;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.TemplateService;
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
    
	@Override
	public Template createTemplate(TemplateRequest templateReq) {
        Template created = repository.save(
        		TemplateBuilder.constructTemplate(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), templateReq));
		return created;
	}

	@Override
	public Template updateTemplate(TemplateRequest templateReq) throws CaptureValidationException {
		Template existing = getTemplate(templateReq.getId());
		if(Objects.isNull(existing)) {
			return null;
		}
        Template created = repository.save(TemplateBuilder.updateTemplate(existing, templateReq));
		return created;
	}

	@Override
	public void deleteTemplate(String templateId) {
		repository.delete(templateId);
	}

	@Override
	public Template getTemplate(String templateId) {
		return repository.findOne(templateId);
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
			templatePage.getContent().forEach(template -> {
				returnList.add(TemplateBuilder.createTemplateRequest(template));
			});
			return new TemplateSearchList(returnList, offset, limit, templatePage.getTotalElements());
		}
		return new TemplateSearchList(returnList, offset, limit, 0);
	}

}
