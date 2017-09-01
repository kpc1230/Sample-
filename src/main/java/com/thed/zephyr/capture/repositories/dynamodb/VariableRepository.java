package com.thed.zephyr.capture.repositories.dynamodb;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.thed.zephyr.capture.model.Variable;

/**
 * @author Venkatareddy on 8/24/17.
 */
@Repository
public interface VariableRepository extends CrudRepository<Variable, String> {

	public List<Variable> findByOwnerName(String ownerName);
	public List<Variable> findByName(String name);
	public List<Variable> findByNameAndOwnerName(String name, String ownerName);
}
