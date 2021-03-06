package luca.tmac.basic.data.impl;

import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.DateTimeAttribute;
import org.wso2.balana.attr.DayTimeDurationAttribute;

import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import luca.data.DataHandler;
import luca.data.AttributeQuery;
import luca.tmac.basic.data.AbstractAttributeFinderModule;
import luca.tmac.basic.data.uris.TaskAttributeURI;
import luca.tmac.basic.data.xml.TaskAttributeXmlName;

public class TaskAttributeFinderModule extends AbstractAttributeFinderModule {
	
	private URI defaultTaskId;
	
	private Set<String> categories;
	private Set<String> ids;
	DataHandler data = null;
	
	public TaskAttributeFinderModule(DataHandler pData) {
		
		try {
			defaultTaskId = new URI(TaskAttributeURI.ID_URI);
		} catch (URISyntaxException e) {
			// ignore
		}
		
		//set Data Handler
		data = pData;
		
		//set supported categories
		categories = new HashSet<String>();
		categories.add(TaskAttributeURI.TASK_CATEGORY_URI);
		
		//set supported ids
		ids = new HashSet<String>();
		ids.add(TaskAttributeURI.DURATION_URI);
		ids.add(TaskAttributeURI.PERMISSION_URI);
		ids.add(TaskAttributeURI.TASK_CATEGORY_URI);
		ids.add(TaskAttributeURI.START_TIME_URI);
		ids.add(TaskAttributeURI.TEAM_ID_URI);
	}

	@Override
	public Set<String> getSupportedCategories() {
		return categories;
	}

	@Override
	public Set<String> getSupportedIds() {
		return ids;
	}
	
	@Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId,
			String issuer, URI category, EvaluationCtx context) {
		
		if(!getSupportedCategories().contains(category.toString()))
			return new EvaluationResult(getEmptyBag());
		
		String id = getIdFromContext(defaultTaskId,issuer,category,context);
		AttributeValue AttResult = findAttributes(id, attributeId);

		return new EvaluationResult(AttResult);
	}

	private AttributeValue findAttributes(String id, URI attributeURI) {
		String attribute = null;
		String attributeType = null;
		
		if(attributeURI.toString().equals(TaskAttributeURI.DURATION_URI))
		{
			attributeType = DayTimeDurationAttribute.identifier;
			attribute = TaskAttributeXmlName.DURATION;
		}
		else if(attributeURI.toString().equals(TaskAttributeURI.PERMISSION_URI))
		{
			attributeType = StringAttribute.identifier;
			attribute = TaskAttributeXmlName.PERMISSION;
		}
		else if(attributeURI.toString().equals(TaskAttributeURI.START_TIME_URI))
		{
			attributeType = DateTimeAttribute.identifier;
			attribute = TaskAttributeXmlName.START_TIME;
		}
		else if(attributeURI.toString().equals(TaskAttributeURI.TEAM_ID_URI))
		{
			attributeType = StringAttribute.identifier;
			attribute = TaskAttributeXmlName.TEAM_ID;
		}
		
		ArrayList<AttributeQuery> query = new ArrayList<AttributeQuery>();
		query.add(new AttributeQuery(TaskAttributeXmlName.ID,id,StringAttribute.identifier));
		
		return data.getBagAttribute(TaskAttributeXmlName.TASK_TABLE, query, attribute, attributeType);
	}
}
