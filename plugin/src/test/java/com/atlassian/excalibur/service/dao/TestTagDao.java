package com.atlassian.excalibur.service.dao;

import com.atlassian.bonfire.renderer.BonfireWikiRenderer;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.service.lock.ClusterLockOperations;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.google.common.collect.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestTagDao {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private BonfireWikiRenderer bonfireTagExtractor;

    @Mock
    private IdDao idDao;

    @InjectMocks
    private TagDao tagDao = new TagDao();

    @Spy
    private final LockOperations lockOperations = new ClusterLockOperations(new SimpleClusterLockService());

    @Test
    public void testGetAllKnownTags() throws Exception {
        // when we first start we have only a couple of well know tags
        Set<Tag> allKnownTags = tagDao.getAllKnownTags();
        assertEquals(2, allKnownTags.size());
        assertTheWellKnownTags(allKnownTags);
    }

    @Test
    public void testGetTag() throws Exception {
        when(persistenceService.getLong(TagDao.KEY_TAG, 1L, "#firefox")).thenReturn(10000L);

        Tag tag = tagDao.getTag("Not a tag in sight");
        assertEquals(TagDao.UNKNOWN_TAG, tag);

        tag = tagDao.getTag("#?");
        assertEquals(tag, new Tag(1L, "#?"));

        // ok make one up
        Tag expectedTag = new Tag(10000L, "#firefox");

        tag = tagDao.getTag("#firefox");
        assertEquals(expectedTag, tag);

        Set<Tag> allKnownTags = tagDao.getAllKnownTags();
        assertEquals(3, allKnownTags.size());
        assertTheWellKnownTags(allKnownTags);
        assertTrue(allKnownTags.contains(expectedTag));
    }

    @Test
    public void testGetTagWithWrite() throws Exception {
        when(persistenceService.getLong(TagDao.KEY_TAG, 1L, "#firefox")).thenReturn(null);
        when(idDao.genNextId()).thenReturn(IdDao.INITIAL_AUTO_ID);


        Tag expectedTag = new Tag(IdDao.INITIAL_AUTO_ID, "#firefox");
        Tag tag = tagDao.getTag("#firefox");
        assertEquals(expectedTag, tag);

        Set<Tag> allKnownTags = tagDao.getAllKnownTags();
        assertEquals(3, allKnownTags.size());
        assertTheWellKnownTags(allKnownTags);
        assertTrue(allKnownTags.contains(expectedTag));

        // do we write this back to the database?  Yes we do!
        verify(persistenceService).setLong(TagDao.KEY_TAG, 1L, "#firefox", IdDao.INITIAL_AUTO_ID);
    }

    @Test
    public void testExtractTags() throws Exception {

        when(persistenceService.getLong(TagDao.KEY_TAG, 1L, "#firefox")).thenReturn(10000L);
        when(persistenceService.getLong(TagDao.KEY_TAG, 1L, "#exploder")).thenReturn(10001L);
        when(bonfireTagExtractor
                .extractTags("This is a note data with multiple #? tags in it and custom ones like #firefox and #exploder and repeats like #? as well"))
                .thenReturn(Sets.newHashSet("#?", "#firefox", "#exploder"));

        Tag expectedFireFox = new Tag(10000L, "#firefox");
        Tag expectedExploder = new Tag(10001L, "#exploder");

        Set<Tag> tags = tagDao.extractTags("This is a note data with multiple #? tags in it and custom ones like #firefox and #exploder and repeats like #? as well");
        assertEquals(3, tags.size());
        assertTrue(tags.contains(TagDao.QUESTION_TAG));
        assertTrue(tags.contains(expectedFireFox));
        assertTrue(tags.contains(expectedExploder));

    }

    private void assertTheWellKnownTags(Set<Tag> allKnownTags) {
        assertTrue(allKnownTags.contains(TagDao.QUESTION_TAG));
        assertTrue(allKnownTags.contains(TagDao.ASSUMPTION_TAG));
    }
}
