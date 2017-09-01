package com.thed.zephyr.capture.repositories.dynamodb;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.thed.zephyr.capture.model.Template;

/**
 * Created by aliakseimatsarski on 8/20/17.
 */
@Repository
public interface TemplateRepository extends CrudRepository<Template, String> {
	/**
	 * Find all the Template objects using ProjectId and Shared as true.
	 * @param shared
	 * @param projectId
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findBySharedAndProjectId(Boolean shared, Long projectId, Pageable pageable);

	/**
	 * Find all the Template objects using favourite as true and owner.
	 * @param favourite
	 * @param owner
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByFavouriteAndCreatedBy(Boolean favourite, String createdBy, Pageable pageable);

	/**
	 * Find all the Template objects usign Shared as true and owner.
	 * @param shared
	 * @param createdBy
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findBySharedAndCreatedBy(Boolean shared, String createdBy, Pageable pageable);

	/**
	 * Find all the Template objects using owner.
	 * @param createdBy
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCreatedBy(String createdBy, Pageable pageable);

	/**
	 * Find all the Template objects using projectId.
	 * @param projectId
	 * @param pageRequest
	 * @return
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByProjectId(Long projectId, Pageable pageRequest);

	/**
	 * Find all the Template objects.
	 * @param pageRequest
	 * @return
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findAll(Pageable pageRequest);
}
