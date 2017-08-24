package com.thed.zephyr.capture.service.data.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.repositories.VariableRepository;
import com.thed.zephyr.capture.service.data.VariableService;

/**
 * @author Venkatareddy on 08/24/2017.
 * @see VariableService
 */
@Service
public class VariableServiceImpl implements VariableService {

	@Autowired
	private VariableRepository repository;

	@Override
	public void createVariable(Variable input) throws CaptureValidationException {
		List<Variable> list = getVariables(input.getOwnerName());
		Variable existing = list.stream().filter(var -> var.getName().equals(input.getName())).findFirst().orElse(null);
		if (existing != null) {
			throw new CaptureValidationException("Variable already exists");
		}
		repository.save(input);
	}

	@Override
	public List<Variable> getVariables(String ownerName) {
		return repository.findByOwnerName(ownerName);
	}

	@Override
	public void updateVariable(Variable input) throws CaptureValidationException {
		List<Variable> list = getVariables(input.getOwnerName());
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
	public void deleteVariable(Variable input) throws CaptureValidationException {
		repository.delete(input.getId());
		//TODO, templateService.deleteVariable();
	}

}
