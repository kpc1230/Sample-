package com.thed.zephyr.capture.service.data.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
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
	public List<Template> getTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Template> getTemplatesByProject(Long projectId) {
		// TODO Auto-generated method stub
		return null;
	}

}
