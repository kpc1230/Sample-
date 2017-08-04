package com.atlassian.bonfire.service.dao;

import com.atlassian.bonfire.model.FavouriteTemplate;
import com.atlassian.bonfire.model.IndexedTemplate;
import com.atlassian.bonfire.model.Template;
import com.atlassian.bonfire.service.BonfireUnmarshalService;
import com.atlassian.bonfire.service.BonfireUserService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.index.iterators.IndexUtils;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Dao for Templates
 *
 * @since v1.7
 */
@Service(TemplateDao.SERVICE)
public class TemplateDao {
    public static final String SERVICE = "bonfire-templateDao";

    @VisibleForTesting
    static final String LOCK_NAME = TemplateDao.class.getName();

    private static final String KEY_TEMPLATE = "Bonfire.Template.Data";
    private static final String KEY_TEMPLATE_OWNER_INDEX = "Bonfire.Template.Index.Owner"; // Owner -> List created template ids.
    private static final String KEY_TEMPLATE_USER_FAVOURITES_INDEX = "Bonfire.Template.Index.User.Favourites"; // User -> List of favourited template ids.
    private static final String KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_LEGACY = "Bonfire.Template.Index.User.Favourites.Time"; // older milliseconds representation
    private static final String KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_JSON = "Bonfire.Template.Index.User.Favourites.Time.JSON"; // User + Template -> time variables accepted
    private static final String KEY_TEMPLATE_SHARED_INDEX = "Bonfire.Template.Index.Shared"; //

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService userService;

    @Resource(name = BonfireUnmarshalService.SERVICE)
    private BonfireUnmarshalService bonfireUnmarshalService;

    @Resource
    private LockOperations lockOperations;

    private final Logger log = Logger.getLogger(getClass());

    public void save(final Template template) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                persistenceService.setText(KEY_TEMPLATE, template.getId(), "data", template.toJSON().toString());
                // Save to indexes
                saveToOwnerIndex(template);
                // Should implement update here to avoid having to do this unless necessary
                if (template.isShared()) {
                    saveToSharedIndex(template);
                } else {
                    deleteFromSharedIndex(template);
                }
            }
        });
    }

    public void delete(final Template template) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                persistenceService.delete(KEY_TEMPLATE, template.getId(), "data");
                // Delete from indexes
                deleteFromOwnerIndex(template);
                if (template.isShared()) {
                    deleteFromSharedIndex(template);
                }
                // Won't delete from favourite index - will remove on failed
                // load, as this will be easier than trying to track it down
            }
        });
    }

    public List<Template> loadAllRelevantSharedTemplates(final Predicate<IndexedTemplate> predicate) {
        final List<Template> templates = new ArrayList<Template>();
        final List<Long> invalidTemplateIds = new ArrayList<Long>();
        final JSONArrayIterator sharedIndexIterator = loadSharedIndex();
        while (sharedIndexIterator.hasNext()) {
            final JSONObject indexedTemplateJson = (JSONObject) sharedIndexIterator.next();
            final IndexedTemplate indexedTemplate = new IndexedTemplate(indexedTemplateJson);
            if (predicate.apply(indexedTemplate)) {
                final Template template = load(indexedTemplate.getId());
                if (template.equals(Template.EMPTY)) {
                    invalidTemplateIds.add(indexedTemplate.getId());
                } else {
                    templates.add(template);
                }
            }
        }
        deleteInvalidTemplateIDs(invalidTemplateIds, "Shared template index corrupted.");
        return templates;
    }

    public List<Template> loadSharedTemplates(
            final Predicate<IndexedTemplate> predicate, final Integer startIndex, final Integer size) {
        final List<Template> templates = new ArrayList<Template>();
        final List<Long> invalidTemplateIds = new ArrayList<Long>();
        final JSONArrayIterator sharedIndexIterator = loadSharedIndex();
        int skipped = 0;
        while (sharedIndexIterator.hasNext() && templates.size() < size) {
            final JSONObject indexedTemplateJson = (JSONObject) sharedIndexIterator.next();
            final IndexedTemplate indexedTemplate = new IndexedTemplate(indexedTemplateJson);
            if (predicate.apply(indexedTemplate)) {
                if (skipped < startIndex) {
                    skipped += 1;
                } else {
                    final Template template = load(indexedTemplate.getId());
                    if (template.equals(Template.EMPTY)) {
                        invalidTemplateIds.add(indexedTemplate.getId());
                    } else {
                        templates.add(template);
                    }
                }
            }
        }
        deleteInvalidTemplateIDs(invalidTemplateIds, "Shared template index corrupted.");
        return templates;
    }

    private void deleteInvalidTemplateIDs(final List<Long> invalidTemplateIds, final String message) {
        if (invalidTemplateIds.isEmpty()) {
            return;
        }
        log.error(message);
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            public void run() {
                for (final Long id : invalidTemplateIds) {
                    log.error(String.format("Unable to load template in shared index with id : %d", id));
                    deleteFromSharedIndex(id);
                }
            }
        });
    }

    public List<Template> loadFavouriteTemplates(final ApplicationUser user, final Predicate<Template> predicate) {
        final List<Template> templates = new ArrayList<Template>();
        final List<Long> invalidTemplateIds = new ArrayList<Long>();
        final JSONArrayIterator userToFavourites = loadFavouritesIndex(user.getName());
        while (userToFavourites.hasNext()) {
            final Long id = ((Number) userToFavourites.next()).longValue();
            // Load the favourite template
            final Template favouriteTemplate = load(id);
            if (favouriteTemplate.equals(Template.EMPTY)) {
                // Need to remove this one from a favourites list, as it no longer exists
                invalidTemplateIds.add(id);
            } else if (predicate.apply(favouriteTemplate)) {
                templates.add(favouriteTemplate);
            }
        }
        removeInvalidFavouriteTemplateIDs(user, invalidTemplateIds);
        return templates;
    }

    private void removeInvalidFavouriteTemplateIDs(final ApplicationUser user, final List<Long> invalidTemplateIds) {
        if (!invalidTemplateIds.isEmpty()) {
            lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
                @Override
                public void run() {
                    for (final Long id : invalidTemplateIds) {
                        removeFavourite(id, user);
                    }
                }
            });
        }
    }

    @NotNull
    public List<Template> loadTemplatesForAdmin(final ApplicationUser user, final Predicate<Template> predicate, final Integer startIndex, final Integer size) {
        final Iterable<Long> templateIds = com.google.common.collect.Iterables.transform(allTemplatesIterable(), new Function<Template, Long>() {
            @Override
            public Long apply(@Nullable Template template) {
                if (template != null) {
                    return template.getId();
                }
                return null;
            }
        });
        return filterTemplates(user, predicate, startIndex, size, templateIds.iterator());
    }

    @NotNull
    public Iterator<Long> jsonArrayToNumbersArray(@NotNull JSONArrayIterator jsonArrayIterator) {
        return Iterators.transform(jsonArrayIterator, new Function<Object, Long>() {
            @Override
            public Long apply(@Nullable Object valueObject) {
                if (valueObject instanceof Number) {
                    return ((Number) valueObject).longValue();
                }
                return null;
            }
        });
    }

    @NotNull
    public List<Template> loadUserTemplates(final ApplicationUser user, final Predicate<Template> predicate, final Integer startIndex, final Integer size) {
        final Iterator<Long> ownerTemplates = jsonArrayToNumbersArray(loadOwnerIndex(user.getName()));
        return filterTemplates(user, predicate, startIndex, size, ownerTemplates);
    }

    @NotNull
    private List<Template> filterTemplates(ApplicationUser user, Predicate<Template> predicate, Integer startIndex, Integer size, Iterator<Long> templateIterator) {
        final List<Template> templates = new ArrayList<Template>(size);
        final List<Long> invalidTemplateIds = new ArrayList<Long>();
        for (int skipped = 0; skipped < startIndex && templateIterator.hasNext(); ) {
            final Long id = templateIterator.next();
            final Template usersTemplate = load(id);
            if (usersTemplate.equals(Template.EMPTY)) {
                invalidTemplateIds.add(id);
            } else if (predicate.apply(usersTemplate)) {
                skipped++;
            }
        }
        for (int i = 0; i < size && templateIterator.hasNext(); i++) {
            final Long id = templateIterator.next();
            final Template usersTemplate = load(id);
            if (usersTemplate.equals(Template.EMPTY)) {
                invalidTemplateIds.add(id);
            } else if (predicate.apply(usersTemplate)) {
                templates.add(usersTemplate);
            }
        }
        deleteInvalidTemplateIDsFromOwnerIndex(user, invalidTemplateIds);
        return templates;
    }

    private void deleteInvalidTemplateIDsFromOwnerIndex(final ApplicationUser user, final List<Long> invalidTemplateIds) {
        if (!invalidTemplateIds.isEmpty()) {
            lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
                @Override
                public void run() {
                    log.error("User template index corrupted.");
                    for (final Long id : invalidTemplateIds) {
                        log.error(String.format("Unable to load template in shared index with id : %d", id));
                        deleteFromOwnerIndex(user.getName(), id);
                    }
                }
            });
        }
    }

    public Iterable<Template> userTemplatesIterable(final ApplicationUser user, final Predicate<Template> predicate) {
        return new Iterable<Template>() {
            public Iterator<Template> iterator() {
                return new UserTemplatesIterator(user, predicate);
            }
        };
    }

    public Template load(final Long id) {
        if (persistenceService.exists(KEY_TEMPLATE, id, "data")) {
            final String templateData = persistenceService.getText(KEY_TEMPLATE, id, "data");
            return bonfireUnmarshalService.getTemplateFromJSON(JSONKit.to(templateData));
        }
        return Template.EMPTY;
    }

    public void saveFavourite(final Long id, final ApplicationUser favouriter) {
        // TODO Could probably use a separate lock for favourites
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                // Store in user -> list of favourite templates index
                final JSONArrayIterator userToFavourites = loadFavouritesIndex(favouriter.getName());
                final String rebuiltIndex = IndexUtils.addToIndex(id, userToFavourites);
                saveFavouriteIndex(favouriter.getName(), rebuiltIndex);

                // Store the template + user -> time favourite accepted
                final FavouriteTemplate favouriteTemplate = new FavouriteTemplate(new DateTime(), true);
                saveFavouriteTemplate(favouriter, id, favouriteTemplate);
            }
        });
    }

    public void removeFavourite(final Long id, final ApplicationUser favouriter) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final JSONArrayIterator favouritesIndexIterator = loadFavouritesIndex(favouriter.getName());
                final String rebuiltIndex = IndexUtils.deleteFromIndex(id, favouritesIndexIterator);
                saveFavouriteIndex(favouriter.getName(), rebuiltIndex);

                // Remove the template + user -> time favourite accepted.
                final FavouriteTemplate favouriteTemplate = loadFavouriteTemplate(favouriter, id);
                saveFavouriteTemplate(favouriter, id, new FavouriteTemplate(favouriteTemplate.getTimeFavourited(), false));
            }
        });
    }

    public boolean isFavourited(final ApplicationUser favouriter, final Long id) {
        return loadFavouriteTemplate(favouriter, id).isFavourited();
    }

    /**
     * <p>
     * This loads up a favorite template for a user. Originally, the code stored a millisecond
     * instant only, then Ian changed his mind and stored a JSON object, however customers will
     * have data that has 'long' property sets and hence they need to be converted to the new JSON format.
     * </p>
     * <p>
     * The following code can be considered an in-place upgrade task.  This was done because the
     * cost of an upgrade tasks (which is NOT inconsiderable) is less than a an IF statement.
     * </p>
     *
     * @param user the user in play
     * @param id   the id of the favorite template
     * @return a FavouriteTemplate
     */
    private FavouriteTemplate loadFavouriteTemplate(ApplicationUser user, Long id) {
        JSONObject favouriteTemplateJSON = new JSONObject();
        String userKey = user.getKey();

        if (persistenceService.exists(KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_JSON, id, userKey)) {
            String json = persistenceService.getText(KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_JSON, id, userKey);
            favouriteTemplateJSON = new JSONObject(StringUtils.defaultString(json, "{}"));
        } else if (persistenceService.exists(KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_LEGACY, id, userKey)) {
            long timeFavouredMS = persistenceService.getLong(KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_LEGACY, id, userKey);
            DateTime timeFavoured = new DateTime(timeFavouredMS);
            FavouriteTemplate favouriteTemplate = new FavouriteTemplate(timeFavoured, true);
            favouriteTemplateJSON = favouriteTemplate.toJSON();
        }

        return bonfireUnmarshalService.getFavTemplateFromJSON(favouriteTemplateJSON);
    }

    private void saveFavouriteTemplate(ApplicationUser user, Long id, FavouriteTemplate favouriteTemplate) {
        String userKey = user.getKey();
        persistenceService.setText(KEY_TEMPLATE_USER_FAVOURITE_TIME_INDEX_JSON, id, userKey, favouriteTemplate.toJSON().toString());
    }

    private void saveToOwnerIndex(Template template) {
        JSONArrayIterator ownerIndexIterator = loadOwnerIndex(template.getOwnerName());

        String rebuiltIndex = IndexUtils.addToIndex(template.getId(), ownerIndexIterator);

        saveOwnerIndex(template.getOwnerName(), rebuiltIndex);
    }

    private void deleteFromOwnerIndex(Template template) {
        deleteFromOwnerIndex(template.getOwnerName(), template.getId());
    }

    private void deleteFromOwnerIndex(String ownerName, Long id) {
        JSONArrayIterator ownerIndexIterator = loadOwnerIndex(ownerName);

        String rebuiltIndex = IndexUtils.deleteFromIndex(id, ownerIndexIterator);

        saveOwnerIndex(ownerName, rebuiltIndex);
    }

    private void saveToSharedIndex(Template template) {
        stateTrue("Bonfire shared index save", template.isShared());
        JSONArrayIterator sharedIndexIterator = loadSharedIndex();
        String rebuiltIndex = IndexUtils.addToJSONIndex(new IndexedTemplate(template).toJSON(), sharedIndexIterator);
        saveSharedIndex(rebuiltIndex);
    }

    private void deleteFromSharedIndex(Template template) {
        deleteFromSharedIndex(template.getId());
    }

    private void deleteFromSharedIndex(Long id) {
        JSONArrayIterator sharedIndexIterator = loadSharedIndex();
        String rebuiltIndex = IndexUtils.deleteFromJSONIndex(id, sharedIndexIterator);
        saveSharedIndex(rebuiltIndex);
    }

    public FavouriteTemplate loadFavouriteData(final ApplicationUser user, final Template template) {
        return loadFavouriteTemplate(user, template.getId());
    }

    /**
     * <p>
     * Reindex templates. Re-builds owner and shared index, and favourites reset to your templates being favourited.
     * </p>
     * <p>
     * TAKE CARE WITH THIS METHOD - if removed, or semantics modified, this could break upgrade tasks.
     * </p>
     */
    public void reindex() {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                // Need to clear the Indexes first
                final Iterable<Template> allTemplates = allTemplatesIterable();

                saveSharedIndex("[]");
                final Set<String> resetUserIndexes = new HashSet<String>();
                for (final Template template : allTemplates) {
                    log.warn(String.format("Looking at template with id %d", template.getId()));
                    if (!resetUserIndexes.contains(template.getOwnerName())) {
                        log.warn(String.format("Clearing template indexes for user %s", template.getOwnerName()));
                        saveOwnerIndex(template.getOwnerName(), "[]");
                        saveFavouriteIndex(template.getOwnerName(), "[]");
                        resetUserIndexes.add(template.getOwnerName());
                    }
                }

                // Now re-create the indexes
                for (final Template template : allTemplates) {
                    log.warn(String.format("Reindexing template with id: %d", template.getId()));
                    final ApplicationUser owner = userService.getUser(template.getOwnerName());
                    if (owner == null) {
                        log.error(String.format("Unable to reindex template for user '%s'", template.getOwnerName()));
                    } else {
                        saveToOwnerIndex(template);
                        // Favourite it for the owner
                        saveFavourite(template.getId(), owner);

                        // Should implement update here to avoid having to do this unless necessary
                        if (template.isShared()) {
                            saveToSharedIndex(template);
                        } else {
                            deleteFromSharedIndex(template);
                        }
                    }
                }
            }
        });
    }

    private JSONArrayIterator loadSharedIndex() {
        return persistenceService.getJSONArrayIterator(KEY_TEMPLATE_SHARED_INDEX, 1L, "template");
    }

    private void saveSharedIndex(String rebuiltIndex) {
        persistenceService.setText(KEY_TEMPLATE_SHARED_INDEX, 1L, "template", rebuiltIndex);
    }

    private JSONArrayIterator loadFavouritesIndex(final String userName) {
        String userKey = userService.getUserKey(userName);
        return persistenceService.getJSONArrayIterator(KEY_TEMPLATE_USER_FAVOURITES_INDEX, (long) userKey.hashCode(), userKey);
    }

    private void saveFavouriteIndex(final String userName, String rebuiltIndex) {
        String userKey = userService.getUserKey(userName);
        persistenceService.setText(KEY_TEMPLATE_USER_FAVOURITES_INDEX, (long) userKey.hashCode(), userKey, rebuiltIndex);
    }

    private JSONArrayIterator loadOwnerIndex(String ownerName) {
        String userKey = userService.getUserKey(ownerName);
        return persistenceService.getJSONArrayIterator(KEY_TEMPLATE_OWNER_INDEX, (long) userKey.hashCode(), userKey);
    }

    private void saveOwnerIndex(String ownerName, String rebuiltIndex) {
        String userKey = userService.getUserKey(ownerName);
        persistenceService.setText(KEY_TEMPLATE_OWNER_INDEX, (long) userKey.hashCode(), userKey, rebuiltIndex);
    }

    private Iterable<Template> allTemplatesIterable() {
        return new Iterable<Template>() {
            public Iterator<Template> iterator() {
                return new AllTemplatesIterator(IdDao.INITIAL_AUTO_ID, idDao.genNextId());
            }
        };
    }

    private class AllTemplatesIterator implements Iterator<Template> {
        private Template template;
        private long currentPosition;
        private final long maxId;

        public AllTemplatesIterator(long minId, long maxId) {
            this.maxId = Math.max(maxId, 0);
            this.currentPosition = Math.min(minId, maxId);
        }

        public boolean hasNext() {
            template = Template.EMPTY;
            while (template.equals(Template.EMPTY)) {
                if (currentPosition > maxId) {
                    return false;
                }
                template = load(currentPosition);
                currentPosition++;
            }
            return true;
        }

        public Template next() {
            if (template.equals(Template.EMPTY)) {
                throw new NoSuchElementException("hasNext() has not be called or ignored");
            }
            return template;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    private class UserTemplatesIterator implements Iterator<Template> {
        private JSONArrayIterator ownerTemplates;
        private Predicate<Template> predicate;
        private Template template;

        public UserTemplatesIterator(ApplicationUser user, Predicate<Template> predicate) {
            this.ownerTemplates = loadOwnerIndex(user.getName());
            this.predicate = predicate;
        }

        @Override
        public boolean hasNext() {
            while (ownerTemplates.hasNext()) {
                Long id = ((Number) ownerTemplates.next()).longValue();
                template = load(id);
                if (!template.equals(Template.EMPTY) && predicate.apply(template)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Template next() {
            if (template.equals(Template.EMPTY)) {
                throw new NoSuchElementException("hasNext() has not be called or ignored");
            }
            return template;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
