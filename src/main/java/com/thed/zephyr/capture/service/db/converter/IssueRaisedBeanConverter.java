package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.IssueRaisedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author manjunath
 * @see com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
 *
 */
public class IssueRaisedBeanConverter implements DynamoDBTypeConverter<Set<String>, Collection<IssueRaisedBean>> {
	
	private static Logger log = LoggerFactory.getLogger(IssueRaisedBeanConverter.class);

	@Override
	public Set<String> convert(Collection<IssueRaisedBean> issues) {
		if(Objects.isNull(issues)){
            return null;
        }
        Set<String> result = new TreeSet<>();
        for (IssueRaisedBean issueRaisedBean:issues){
            result.add(issueRaisedBean.toJSON());
        }
        return result;
	}

	@Override
	public Collection<IssueRaisedBean> unconvert(Set<String> issuesstr) {
		if(Objects.isNull(issuesstr)){
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        Collection<IssueRaisedBean> result = new ArrayList<>();
        for (String jsonStr:issuesstr){
            try {
            	IssueRaisedBean issueRaisedBean = om.readValue(jsonStr, IssueRaisedBean.class);
                result.add(issueRaisedBean);
            } catch (IOException e) {
                log.error("Error during parsing IssueRaisedBean object.", e);
            }
        }
        return result;
	}
   
}

