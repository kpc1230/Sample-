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
	public Page<Template> findByCtIdAndSharedAndProjectId(String ctId, Boolean shared, Long projectId, Pageable pageable);

	/**
	 * Find all the Template objects using ctId and Shared as true.
	 * @param ctId
	 * @param shared
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndShared(String ctId, Boolean shared, Pageable pageable);

	/**
	 * Find all the Template objects using favourite as true and owner.
	 * @param favourite
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndFavouriteAndShared(String ctId,Boolean favourite, Boolean shared, Pageable pageable);

	/**
	 * Find all the Template objects usign Shared as true and owner.
	 * @param shared
	 * @param createdBy
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndSharedAndCreatedBy(String ctId,Boolean shared, String createdBy, Pageable pageable);
	
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndSharedAndCreatedByAccountId(String ctId,Boolean shared, String createdByAccountId, Pageable pageable);

	/**
	 * Find all the Template objects using owner.
	 * @param createdBy
	 * @param pageable
	 * @return - Paginated Template objects.
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndCreatedBy(String ctId,String createdBy, Pageable pageable);
	
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndCreatedByAccountId(String ctId, String createdByAccountId, Pageable pageable);

	/**
	 * Find all the Template objects using projectId.
	 * @param projectId
	 * @param pageRequest
	 * @return
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndProjectId(String ctId,Long projectId, Pageable pageRequest);

	/**
	 * Find all the Template objects.
	 * @param pageRequest
	 * @return
	 */
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtId(String ctId,Pageable pageRequest);
	
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndFavouriteAndCreatedBy(String ctId,Boolean favourite, String createdBy, Pageable pageable);
	
	@EnableScan
	@EnableScanCount
	public Page<Template> findByCtIdAndFavouriteAndCreatedByAccountId(String ctId,Boolean favourite, String createdByAccountId, Pageable pageable);
}
