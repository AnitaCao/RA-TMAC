package luca.tmac.basic.obligations;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.wso2.balana.ParsingException;
import org.wso2.balana.attr.DayTimeDurationAttribute;
import luca.data.AttributeQuery;

public class Obligation {
	
	public static final String STATE_FULFILLED = "fulfilled";
	public static final String STATE_ACTIVE = "active";
	public static final String STATE_EXPIRED = "expired";
	public static final String STATE_ATTRIBUTE_NAME = "state";
	public static final String SET_ID_ATTRIBUTE_NAME = "setId";
	
	
	public String actionName;
	public HashMap<String,String> attributeMap;
	public Date startDate;

	public Obligation(String pId,Date pStartDate, List<AttributeQuery> pParameters) {
		actionName = pId;
		startDate = pStartDate;
		attributeMap = new HashMap<String,String>();
		for(AttributeQuery aq : pParameters)
		{
			attributeMap.put(aq.name, aq.value);
		}
	}
	
	public String getAttribute(String name)
	{
		if(name.equals(ObligationIds.ACTION_NAME_OBLIGATION_ATTRIBUTE))
			return actionName;
		return attributeMap.get(name);
	}
	
	public Date getDeadline()
	{
		String xmlDurationString = this
				.getAttribute(ObligationIds.DURATION_OBLIGATION_ATTRIBUTE);
		long duration = 0;
		try {
			duration = DayTimeDurationAttribute.getInstance(xmlDurationString)
					.getTotalSeconds();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		}
		
		Date deadline = new Date(startDate.getTime() + duration);
		return deadline;
	}
	
	@Override
	public String toString() {
		String out = "Obl[Id = " + actionName;
		for ( String aName : attributeMap.keySet()) {
			out += ", " + aName + " = " + attributeMap.get(aName);
		}
		out += "]";
		return out;
	}

	public boolean isUserObligation()
	{
		return actionName.startsWith(ObligationIds.USER_PREFIX);
	}
	
	public boolean isSystemObligation()
	{
		return !isUserObligation();
	}

	public void setAttribute(AttributeQuery aq)
	{
		attributeMap.put(aq.name, aq.value);
	}
	
	public boolean isFulfilled()
	{
		String state =  getAttribute(STATE_ATTRIBUTE_NAME);
		return state != null && state.equals(STATE_FULFILLED);
	}
	
	public boolean isActive()
	{
		
		String state =  getAttribute(STATE_ATTRIBUTE_NAME);
		return state == null || state.equals(STATE_ACTIVE);
		
	}
	
	public boolean isExpired()
	{
		String state =  getAttribute(STATE_ATTRIBUTE_NAME);
		return state != null && state.equals(STATE_EXPIRED);	}
}
