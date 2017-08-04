package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireUserService;
import com.atlassian.bonfire.service.dao.SessionMarshaller;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static org.apache.commons.lang.Validate.*;

@Service(SessionDao.SERVICE)
public class SessionDao {
    public static final String SERVICE = "excalibur-sessiondao";

    @VisibleForTesting
    static final String LOCK_NAME = SessionDao.class.getName();

    // Persistence
    private static final String KEY_ACTIVE_SESSION = "Bonfire.Active.Session.Id";
    private static final String SESSION_ACTIVE_PREFIX = "session_active_";
    private static final String SESSION_INDEX_STORAGE_KEY = "Excalibur.Session.Index";
    private static final String SESSION_STORAGE_KEY = "Excalibur.Session.Storage";

    @Resource(name = PropertyDao.SERVICE)
    private PropertyDao propertyDao;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = SessionMarshaller.SERVICE)
    private SessionMarshaller sessionMarshaller;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource
    private LockOperations lockOperations;

    private final Logger log = Logger.getLogger(this.getClass());

    private void validateSession(Session session) {
        // Validate session
        notNull(session);
        notNull(session.getName());
        notEmpty(session.getName().trim());
        notNull(session.getCreator());
        notNull(session.getAssignee());
        // Length checks.
        isTrue(session.getName().length() <= BonfireConstants.SESSION_NAME_LENGTH_LIMIT);
        if (session.getAdditionalInfo() != null) {
            isTrue(session.getAdditionalInfo().length() <= BonfireConstants.ADDITIONAL_INFO_LENGTH_LIMIT);
        }
    }

    public Session load(final Long id) {
        notNull(id);
        final Map<String, Object> data = persistenceService.getData(SESSION_STORAGE_KEY, id, "data");
        if (data == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to find session with id " + id);
            }
            return null;
        }
        return sessionMarshaller.unMarshal(id, data);
    }

    public List<Session> load(final int startIndex, final int size, final Comparator<IndexedSession> sessionIndexComparator,
                              final Predicate<IndexedSession> sessionIndexPredicate) {
        // Load in the session index
        final List<IndexedSession> indexedSessionList = loadIndex(sessionIndexPredicate);
        Collections.sort(indexedSessionList, sessionIndexComparator);
        // Now need the list of ids to load
        final int actualSize = getActualSize(indexedSessionList.size(), startIndex, size);

        final List<Long> idList = new ArrayList<Long>(actualSize);

        for (int i = startIndex; i < startIndex + actualSize; i++) {
            idList.add(indexedSessionList.get(i).getId());
        }

        return loadSessions(idList);
    }

    public Long count(final Predicate<IndexedSession> sessionIndexPredicate) {
        final JSONArrayIterator indexedSessionArrayIterator =
                persistenceService.getJSONArrayIterator(SESSION_INDEX_STORAGE_KEY, 1L, "data");
        if (indexedSessionArrayIterator == null) {
            log.warn("Error loading indexed sessions array iterator");
            return 0L;
        }

        long count = 0L;
        while (indexedSessionArrayIterator.hasNext()) {
            try {
                final JSONObject indexedJSONObject = (JSONObject) indexedSessionArrayIterator.next();
                final IndexedSession indexedSession = new IndexedSession(indexedJSONObject);
                // Check if we're interested in this session
                if (sessionIndexPredicate.apply(indexedSession)) {
                    count++;
                }
            } catch (JSONException e) {
                log.warn("Error loading indexed session", e);
            }
        }
        return count;
    }

    /**
     * Load a list of sessions, given a list of ids.
     * Unfortunately JIRA doesn't have a View containing OSPropertyEntry
     * and OSPropertyData, so forced to make n database calls.
     *
     * @param idList a list of session ids
     * @return a list of sessions from those ids
     */
    private List<Session> loadSessions(final List<Long> idList) {
        final List<Session> sessions = new ArrayList<Session>(idList.size());
        for (Long id : idList) {
            Session session = load(id);
            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    public void save(final Session session) {
        validateSession(session);

        final Map<String, Object> data = sessionMarshaller.marshal(session);

        // Store session
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                if (persistenceService.exists(SESSION_STORAGE_KEY, session.getId(), "data")) {
                    updateIndex(session);
                } else {
                    addToIndex(session);
                }
                persistenceService.setData(SESSION_STORAGE_KEY, session.getId(), "data", data);
            }
        });
    }

    private void concatSessionIndexEntry(StringBuilder sb, Session session) throws JSONException {
        IndexedSession indexedSession = new IndexedSession(session);
        JSONObject marshalledJSON = indexedSession.marshal();
        String sessionMarshalled = marshalledJSON.toString();
        if (sb.length() != 0) {
            // Want to make sure we have enough capacity in our string builder, or else it'll double in size.
            sb.ensureCapacity(sessionMarshalled.length() + 3);
            sb.deleteCharAt(sb.length() - 1);
            sb.append(',');
            sb.append(sessionMarshalled);
            sb.append(']');
        } else {
            sb.append(new JSONArray().put(marshalledJSON).toString());
        }
    }

    // CLUSTER SAFETY - Assumes that it is lock protected
    private void addToIndex(Session session) {
        // Load in the index
        try {
            StringBuilder sb = new StringBuilder(StringUtils.defaultString(persistenceService.getText(SESSION_INDEX_STORAGE_KEY, 1L, "data")));

            concatSessionIndexEntry(sb, session);

            persistenceService.setText(SESSION_INDEX_STORAGE_KEY, 1L, "data", sb.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // CLUSTER SAFETY - Assumes that it is lock protected
    private void updateIndex(Session session) {
        deleteFromIndex(session.getId());
        addToIndex(session);
    }

    // CLUSTER SAFETY - Assumes that it is lock protected
    private List<IndexedSession> loadIndex(Predicate<IndexedSession> sessionIndexPredicate) {
        JSONArrayIterator indexedSessionArrayIterator = persistenceService.getJSONArrayIterator(SESSION_INDEX_STORAGE_KEY, 1L, "data");
        List<IndexedSession> indexedSessionsList = new ArrayList<IndexedSession>();

        if (indexedSessionArrayIterator == null) {
            log.warn("Error loading indexed sessions array iterator");
            return indexedSessionsList;
        }

        while (indexedSessionArrayIterator.hasNext()) {
            try {
                JSONObject indexedSessionJson = (JSONObject) indexedSessionArrayIterator.next();
                IndexedSession indexedSession = new IndexedSession(indexedSessionJson);
                // Check if we're interested in this session
                if (sessionIndexPredicate.apply(indexedSession)) {
                    indexedSessionsList.add(indexedSession);
                }
            } catch (JSONException e) {
                log.warn("Error loading indexed session", e);
            }
        }
        return indexedSessionsList;
    }

    // CLUSTER SAFETY - Assumes that it is lock protected
    private void deleteFromIndex(Long id) {
        JSONArrayIterator indexedSessionArrayIterator =
                persistenceService.getJSONArrayIterator(SESSION_INDEX_STORAGE_KEY, 1L, "data");
        StringBuilder rebuiltIndex = new StringBuilder("[");

        if (indexedSessionArrayIterator == null) {
            log.warn("Error loading indexed sessions array iterator");
            return;
        }

        while (indexedSessionArrayIterator.hasNext()) {
            try {
                JSONObject indexedSessionJson = (JSONObject) indexedSessionArrayIterator.next();
                IndexedSession indexedSession = new IndexedSession(indexedSessionJson);
                // Check if we're interested in this session
                if (!indexedSession.getId().equals(id)) {
                    String indexedSessionJSONString = indexedSession.marshal().toString();
                    // Avoid doubling in size
                    rebuiltIndex.ensureCapacity(indexedSessionJSONString.length() + 1);

                    rebuiltIndex.append(indexedSessionJSONString);
                    rebuiltIndex.append(',');
                }
            } catch (JSONException e) {
                log.warn("Error loading indexed session", e);
            }
        }
        // Replace the last character with a ]
        if (rebuiltIndex.length() == 1) {
            persistenceService.delete(SESSION_INDEX_STORAGE_KEY, 1L, "data");
        } else {
            rebuiltIndex.setCharAt(rebuiltIndex.length() - 1, ']');
            persistenceService.setText(SESSION_INDEX_STORAGE_KEY, 1L, "data", rebuiltIndex.toString());
        }
    }

    public void delete(final Long id) {
        if (id == null) {
            return;
        }

        // TODO Should we do a true delete here? Or just remove from lookup sets + add to trash lookup set? Not sure.
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                persistenceService.delete(SESSION_STORAGE_KEY, id, "data");
                deleteFromIndex(id);
            }
        });
    }

    /**
     * Re-indexes all sessions. This should ONLY be called during upgrade tasks, as it's expensive!
     */
    public void reindex() {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final Iterable<Session> allSessions = loadAllSessions();
                final StringBuilder sb = new StringBuilder();
                for (Session session : allSessions) {
                    try {
                        concatSessionIndexEntry(sb, session);
                    } catch (JSONException e) {
                        throw new RuntimeException("This is really not expected to happen");
                    }
                }
                persistenceService.setText(SESSION_INDEX_STORAGE_KEY, 1L, "data", sb.toString());
            }
        });
    }

    public Long getActiveSessionId(final ApplicationUser user) {
        // Validate
        notNull(user);
        final String userKey = user.getKey();
        Long id = persistenceService.getLong(KEY_ACTIVE_SESSION, (long) userKey.hashCode(), userKey);
        if (id == null) {
            // this is here for legacy reasons - we used to read active sessions from the propertyDao,
            // so to preserve this behaviour we have this call here as a back up to get the
            id = propertyDao.getLongProperty(SESSION_ACTIVE_PREFIX + userKey);
        }
        return id == null ? -1L : id;
    }

    private void setCurrentSessionImpl(final ApplicationUser user, final Long id) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final String userKey = user.getKey();
                persistenceService.setLong(KEY_ACTIVE_SESSION, (long) userKey.hashCode(), userKey, id);
            }
        });
    }

    public void clearActiveSession(ApplicationUser user) {
        setCurrentSessionImpl(user, -1L);
    }

    public void setActiveSession(ApplicationUser user, Long id) {
        setCurrentSessionImpl(user, id);
    }

    private Iterable<Session> loadAllSessions() {
        return new Iterable<Session>() {
            @Override
            public Iterator<Session> iterator() {
                return new AllSessionsIterator(IdDao.INITIAL_AUTO_ID, idDao.genNextId());
            }
        };
    }

    private class AllSessionsIterator implements Iterator<Session> {
        private Session session = null;
        private long currentPosition;
        private final long maxId;

        public AllSessionsIterator(long minId, long maxId) {
            this.maxId = Math.max(maxId, 0);
            currentPosition = minId;
        }

        @Override
        public boolean hasNext() {
            Session foundSession = null;
            while (foundSession == null) {
                if (currentPosition > maxId) {
                    session = null;
                    return false;
                }
                foundSession = load(currentPosition);
                currentPosition++;
            }
            session = foundSession;
            return true;
        }

        @Override
        public Session next() {
            if (session == null) {
                throw new NoSuchElementException("hasNext() has not be called or ignored");
            }
            return session;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /******************
     * LIGHT SESSIONS - mostly a copy of the methods above but using a different object that is faster to load
     ******************/

    public LightSession lightLoad(final Long id) {
        notNull(id);
        final Map<String, Object> data = persistenceService.getData(SESSION_STORAGE_KEY, id, "data");
        if (data == null) {
            return null;
        }
        return sessionMarshaller.unMarshalLightSession(id, data);
    }

    public int getSessionCount(final Predicate<IndexedSession> sessionIndexPredicate) {
        final Collection<?> indexedSessions = loadIndex(sessionIndexPredicate);
        return indexedSessions.size();
    }

    public List<LightSession> lightLoad(final int startIndex, final int size,
                                        final Comparator<IndexedSession> sessionIndexComparator, final Predicate<IndexedSession> sessionIndexPredicate) {
        // Load the session index
        final List<IndexedSession> indexedSessionList = loadIndex(sessionIndexPredicate);
        Collections.sort(indexedSessionList, sessionIndexComparator);

        // Now need the list of ids to load
        final int actualSize = getActualSize(indexedSessionList.size(), startIndex, size);
        final List<Long> idList = new ArrayList<Long>(actualSize);
        for (int i = startIndex; i < startIndex + actualSize; i++) {
            idList.add(indexedSessionList.get(i).getId());
        }
        return loadLightSessions(idList);
    }

    private int getActualSize(final int maxSize, final int startIndex, final int size) {
        if (startIndex + size > maxSize - 1) {
            return Math.max(maxSize - startIndex, 0);
        }
        return size;
    }

    private List<LightSession> loadLightSessions(final List<Long> idList) {
        final List<LightSession> sessions = new ArrayList<LightSession>(idList.size());
        for (Long id : idList) {
            LightSession session = lightLoad(id);
            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    /**********************
     * Non-session methods
     **********************/

    // Returns list of projects that are related to sessions
    public List<Long> getAllRelatedProjects(final Predicate<IndexedSession> sessionIndexPredicate) {
        final List<Long> projects = Lists.newArrayList();
        for (IndexedSession index : loadIndex(sessionIndexPredicate)) {
            if (!projects.contains(index.getProjectId())) {
                projects.add(index.getProjectId());
            }
        }
        return projects;
    }

    // Returns list of usernames that are related to sessions
    public List<String> getAllAssignees(final Predicate<IndexedSession> sessionIndexPredicate) {
        final List<String> usernames = Lists.newArrayList();
        for (final IndexedSession indx : loadIndex(sessionIndexPredicate)) {
            // We want the add the username to the list, not the key
            final String assigneeKey = indx.getAssignee();
            final ApplicationUser assignee = bonfireUserService.safeGetUserByKey(assigneeKey);
            if (!usernames.contains(assignee.getName())) {
                usernames.add(assignee.getName());
            }
        }
        return usernames;
    }
}
