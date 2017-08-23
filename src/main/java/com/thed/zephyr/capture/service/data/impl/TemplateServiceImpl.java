package com.thed.zephyr.capture.service.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.repositories.TemplateRepository;
import com.thed.zephyr.capture.service.data.TemplateService;

/**
 * Created by Venkatareddy on 08/18/2017.
 * @see TemplateService
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository repository;

	@Override
	public Template createTemplate(TemplateRequest templateReq) {
        Template created = repository.save(TemplateBuilder.constructTemplate(templateReq));
		return created;
	}

	@Override
	public Template updateTemplate(TemplateRequest templateReq) {
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
	public List<TemplateRequest> getTemplates(String userName, Integer offset, Integer limit) {
		//TODO, check condition on user who should be admin to execute this operation.
		List<Template> list = repository.findAll(getPageRequest(offset, limit)).getContent();
		return convert(list);
	}

	@Override
	public List<TemplateRequest> getUserTemplates(String userName, Integer offset, Integer limit) {
		List<Template> list = repository.findByCreatedBy(userName, getPageRequest(offset, limit)).getContent();
		return convert(list);
	}

	@Override
	public List<TemplateRequest> getTemplatesByProject(Long projectId, Integer offset, Integer limit) {
		List<Template> list = repository.findByProjectId(projectId, getPageRequest(offset, limit)).getContent();
		return convert(list);
	}

	@Override
	public List<TemplateRequest> getSharedTemplates(String userName, Integer offset, Integer limit) {
		List<Template> list = repository.findBySharedAndCreatedBy(true, userName, getPageRequest(offset, limit)).getContent();
		return convert(list);
	}

	@Override
	public List<TemplateRequest> getFavouriteTemplates(String owner, Integer offset, Integer limit) {
		List<Template> list = repository.findByFavouriteAndCreatedBy(true, owner, getPageRequest(offset, limit)).getContent();
		return convert(list);
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
	 * @param list
	 * @return
	 */
	private List<TemplateRequest> convert(List<Template> list) {
		if (list != null && list.size() > 0) {
			List<TemplateRequest> returnList = new ArrayList<>();
			list.stream().forEach(template -> {
				returnList.add(TemplateBuilder.createTemplateRequest(template));
			});
			return returnList;
		}
		return null;
	}

}
