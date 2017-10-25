package com.thed.zephyr.capture.repositories.elasticsearch.impl;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.stereotype.Component;

import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * @author manjunath
 *
 */
@Component
public class NoteRepositoryImpl {
	
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	
	
	public void deleteBySessionId(String sessionId) {
		DeleteQuery deleteQuery = new DeleteQuery();
		deleteQuery.setQuery(QueryBuilders.matchQuery(ApplicationConstants.SESSION_ID_FIELD, sessionId));		
		elasticsearchTemplate.delete(deleteQuery, Note.class);
	}
}
