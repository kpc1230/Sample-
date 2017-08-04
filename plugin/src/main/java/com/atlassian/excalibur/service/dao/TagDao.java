package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.renderer.BonfireWikiRenderer;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @since v1.3
 */
@Service(TagDao.SERVICE)
public class TagDao {
    public static final String SERVICE = "bonfire-tagDao";

    public static final Long UNKNOWN_TAG_ID = -1L;

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    public static final Tag UNKNOWN_TAG = new Tag(UNKNOWN_TAG_ID, "");

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    static final Tag QUESTION_TAG = new Tag(1L, Tag.QUESTION);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    static final Tag ASSUMPTION_TAG = new Tag(2L, Tag.ASSUMPTION);

    @VisibleForTesting
    static final String LOCK_NAME = TagDao.class.getName();

    // Persistence
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    static final String KEY_TAG = "Bonfire.Tag.Data";


    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = BonfireWikiRenderer.SERVICE)
    private BonfireWikiRenderer bonfireTagExtractor;

    @Resource
    private LockOperations lockOperations;

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private Map<String, Tag> allKnownTags = new HashMap<String, Tag>();

    public TagDao() {
        //
        // our well known tags with their well known ids
        //
        allKnownTags.put(Tag.QUESTION, QUESTION_TAG);
        allKnownTags.put(Tag.ASSUMPTION, ASSUMPTION_TAG);
        //
        // there are more well known tags but we have data out there before they where decided on
        // so they are no longer well known in terms of id.  But thats OK since we match on tag name
        // and never id and the id they get will be unique, just not well known. eg 4,5,6
        //
        // c'est la vie
    }

    /**
     * <p>
     * Extracts tags given a set of node data.
     * </p>
     * <p>
     * The syntax for tags are #tagdatagoeshere
     * Whitespace characters terminate a tag
     * </p>
     *
     * @param noteData Data to extract tags from.
     * @return a list of tags for that note data
     */
    public Set<Tag> extractTags(String noteData) {
        Set<String> tags = bonfireTagExtractor.extractTags(noteData);
        return Sets.newHashSet(Collections2.transform(tags, new Function<String, Tag>() {
            public Tag apply(String tagText) {
                return getTag(tagText);
            }
        }));
    }

    /**
     * @return a copied list of all known tags in Capture for JIRA
     */
    public Set<Tag> getAllKnownTags() {
        return ImmutableSet.copyOf(allKnownTags.values());
    }

    /**
     * Returns a Tag object for the specified tag text.  If the tag does not exists in the DB then it creates it
     *
     * @param tagText the tag text - must start with # to be a tag
     * @return a Tag or the {@link #UNKNOWN_TAG}
     */
    public Tag getTag(final String tagText) {
        // validation - don't create tags that don't start with #
        final String trimmedTag = StringUtils.defaultString(tagText).trim();
        if (!trimmedTag.startsWith("#")) {
            return UNKNOWN_TAG;
        }
        final String tagLookupText = trimmedTag.toLowerCase();

        // Both gets and sets, so we use a lock
        return lockOperations.callUnderLock(LOCK_NAME, new Callable<Tag>() {
            @Override
            public Tag call() throws Exception {
                final Tag existingTag = allKnownTags.get(tagLookupText);
                if (existingTag != null) {
                    return existingTag;
                }

                // hmm we have never seen that tag text
                Long tagId = persistenceService.getLong(KEY_TAG, 1L, tagLookupText);
                if (tagId == null) {
                    tagId = idDao.genNextId();
                    persistenceService.setLong(KEY_TAG, 1L, tagLookupText, tagId);
                }
                final Tag newTag = new Tag(tagId, trimmedTag);
                allKnownTags.put(tagLookupText, newTag);

                return newTag;
            }
        });
    }
}
