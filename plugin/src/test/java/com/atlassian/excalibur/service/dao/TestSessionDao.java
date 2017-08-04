package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.service.dao.SessionMarshaller;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.model.SessionBuilder;
import com.atlassian.excalibur.service.lock.ClusterLockOperations;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.json.JSONException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Mockito.when;

/**
 * Unit Tests for SessionDao
 *
 * @since v1.3
 */
public class TestSessionDao {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private PropertyDao propertyDao;

    @InjectMocks
    private SessionDao sessionDao = new SessionDao();

    @Mock
    private ApplicationUser mockUser;

    @Mock
    private SessionMarshaller marshaller;

    @Mock
    private ExcaliburWebUtil excaliburWebUtil;

    @Mock
    @AvailableInContainer
    private BuildUtilsInfo buildUtilsInfo;

    @Spy
    private final LockOperations lockOperations = new ClusterLockOperations(new SimpleClusterLockService());

    @Before
    public void setUp() {
        when(buildUtilsInfo.getVersionNumbers()).thenReturn(new int[]{6645});
    }

    @Test
    public void testAddSessionNameLimit499() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 499; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // No exceptions means passed
    }

    @Test
    public void testAddSessionNameLimit500() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 500; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // No exceptions means passed
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSessionNameLimit501() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 501; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // We have an expected exception
    }

    @Test
    public void testAddSessionInfoLimit19999() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 19999; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName("NAME");
        sb.setAdditionalInfo(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // No exceptions means passed
    }

    @Test
    public void testAddSessionInfoLimit20000() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 20000; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName("NAME");
        sb.setAdditionalInfo(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // No exceptions means passed
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSessionInfoLimit20001() throws JSONException {
        when(mockUser.getName()).thenReturn("testUser");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != 20001; i++) {
            builder.append("A");
        }

        SessionBuilder sb = new SessionBuilder(1L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName("NAME");
        sb.setAdditionalInfo(builder.toString());
        sb.setTimeCreated(new DateTime());
        sb.setStatus(Status.STARTED);

        sessionDao.save(sb.build());
        // Have an expected exception here
    }
}
