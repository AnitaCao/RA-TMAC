package luca.tmac.basic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.DateTimeAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.finder.AttributeFinderModule;

import luca.data.AttributeQuery;
import luca.data.DataHandler;
import luca.tmac.basic.data.AbstractAttributeFinderModule;
import luca.tmac.basic.data.uris.ActionAttributeURI;
import luca.tmac.basic.data.uris.PermissionAttributeURI;
import luca.tmac.basic.data.uris.RiskAttributeURI;
import luca.tmac.basic.data.uris.SubjectAttributeURI;
import luca.tmac.basic.data.uris.TaskAttributeURI;
import luca.tmac.basic.data.uris.TeamAttributeURI;
import luca.tmac.basic.data.xml.TaskAttributeXmlName;
import luca.tmac.basic.obligations.*;

public class TmacPEP implements ObligationMonitorable {

//	public static final String TOP_LEVEL_POLICIES_PATH = "./tmac/basic/top-level-policies";
//	public static final String OTHER_POLICIES_PATH = "./tmac/basic/other-policies";
	public static final String TOP_LEVEL_POLICIES_FOLDER = "/Users/anitacao/Documents/workspace/Eclipse/basicModule/omod/src/main/resources/top-level-policies";
	public static final String OTHER_POLICIES_FOLDER = "/Users/anitacao/Documents/workspace/Eclipse/basicModule/omod/src/main/resources/other-policies";

	private TmacPDP pdp;
	public UserObligationMonitor obligationMonitor;
	private DataHandler dh;
	private HashMap<Long, ResponseParser> sessionParsers;
	private ObligationMonitorable monitorable;

	public TmacPEP(DataHandler parDataHandler,ObligationMonitorable monitorable) {
		
//		// find resource path by using classloader
//		String data_xml_path = this.getClass().getClassLoader().getResource("data.xml").toString();
//		// highly dangerous and fragile hackery
//		String base_path = data_xml_path.substring(5, data_xml_path.length() - 8);
//		System.out.println("CHRISCHRISCHIRS: " + base_path + TOP_LEVEL_POLICIES_FOLDER);
		
		this.monitorable = monitorable;
		dh = parDataHandler;
//		pdp = new TmacPDP(dh, base_path + TOP_LEVEL_POLICIES_FOLDER, base_path + OTHER_POLICIES_FOLDER);
		
		pdp = new TmacPDP(dh,TOP_LEVEL_POLICIES_FOLDER, OTHER_POLICIES_FOLDER);
		sessionParsers = new HashMap<Long, ResponseParser>();
		obligationMonitor = new UserObligationMonitor(this, dh);
		obligationMonitor.loadListFromDb();
	}

	/**
	 * Register a new attribute finder module with the PDP associated with this PEP.
	 * @param m - the attribute finder module.
	 */
	public void addAttributeFinderToPDP(AbstractAttributeFinderModule m)
	{
		pdp.addFinderModule(m);
	}
	
	public ResponseParser requestAccess(String username, String permission,
			String team, String task, String requestType) {
		String request = createXACMLRequest(username, permission, team, task,
				requestType);
		String response = pdp.evaluate(request);
		ResponseParser rParser = new ResponseParser(response,dh);
		System.out.println("Anita , 2 the decision is : " + rParser.getDecision());
		if(rParser.getDecision().equalsIgnoreCase("Permit"))
				sessionParsers.put(rParser.getParserId(), rParser);
		return rParser;
	}

	public String acceptResponse(long parserId) {
		ResponseParser parser = sessionParsers.get(parserId);
		String message = "";
		ObligationSet oblSet = new ObligationSet(new ArrayList<Obligation>(),dh);
		if (parser == null)
			throw new IllegalArgumentException("invalid parser id");
		for (Obligation obl : parser.getObligation().getList()) {
			if (obl.isSystemObligation()) {
				message += performObligation(obl) +"\n";
			} else {
				//if it's not system obligation which means it's a user obigation
				oblSet.add(obl);
				obligationMonitor.addObligation(obl);
			}
		}
		return message;
	}

	public String createXACMLRequest(String username, String permission,
			String team, String task, String requestType) {

		String subjectCategory = "";
		String permissionCategory = "";
		String teamCategory = "";
		String taskCategory = "";
		String actionCategory = "";
		String envCategory = "";

		if (username != null && !username.equals(""))
			subjectCategory = "<Attributes Category=\""
					+ SubjectAttributeURI.SUBJECT_CATEGORY_URI
					+ "\">\n"
					+ "<Attribute AttributeId=\""
					+ SubjectAttributeURI.ID_URI
					+ "\" IncludeInResult=\"false\">\n"
					+ "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
					+ username + "</AttributeValue>\n" + "</Attribute>\n"
					+ "</Attributes>\n";

		if (permission != null && !permission.equals(""))
			permissionCategory = "<Attributes Category=\""
					+ PermissionAttributeURI.PERMISSION_CATEGORY_URI
					+ "\">\n"
					+ "<Attribute AttributeId=\""
					+ PermissionAttributeURI.PERMISSION_ID_URI
					+ "\" IncludeInResult=\"false\">\n"
					+ "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
					+ permission + "</AttributeValue>\n" + "</Attribute>\n"
					+ "</Attributes>\n";

		if (team != null && !team.equals(""))
			teamCategory = "<Attributes Category=\""
					+ TeamAttributeURI.TEAM_CATEGORY_URI
					+ "\">\n"
					+ "<Attribute AttributeId=\""
					+ TeamAttributeURI.ID_URI
					+ "\" IncludeInResult=\"false\">\n"
					+ "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
					+ team + "</AttributeValue>\n" + "</Attribute>\n"
					+ "</Attributes>\n";

		if (task != null && !task.equals(""))
			taskCategory = "<Attributes Category=\""
					+ TaskAttributeURI.TASK_CATEGORY_URI
					+ "\">\n"
					+ "<Attribute AttributeId=\""
					+ TaskAttributeURI.ID_URI
					+ "\" IncludeInResult=\"false\">\n"
					+ "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
					+ task + "</AttributeValue>\n" + "</Attribute>\n"
					+ "</Attributes>\n";

		if (requestType != null && !requestType.equals(""))
			actionCategory = "<Attributes Category=\""
					+ ActionAttributeURI.ACTION_CATEGORY_URI
					+ "\">\n"
					+ "<Attribute AttributeId=\""
					+ ActionAttributeURI.ACTION_ID_URI
					+ "\" IncludeInResult=\"false\">\n"
					+ "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"
					+ requestType + "</AttributeValue>\n" + "</Attribute>\n"
					+ "</Attributes>\n";

		envCategory = "<Attributes Category=\"" + XACMLConstants.ENT_CATEGORY
				+ "\">\n" + "</Attributes>\n";

		String riskCategory = "<Attributes Category=\""
				+ RiskAttributeURI.RISK_CATEGORY_URI + "\">\n"
				+ "</Attributes>\n";

		String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n"
				+ subjectCategory
				+ permissionCategory
				+ teamCategory
				+ taskCategory
				+ actionCategory
				+ envCategory
				+ riskCategory
				+ "</Request>";

		return request;
	}
	
	public String performObligation(Obligation obl)
	{
		if(obl.actionName.equals(ObligationIds.ASSIGN_TASK_OBLIGATION_ID))
		{
			String team = obl.getAttribute("associating_team");
			String task = obl.getAttribute("associating_task");
	
			if(team == null || task == null)
			{
				throw new IllegalArgumentException("obligation: id=" + obl.actionName + ". missed parameter(s)");
			}
			
			ArrayList<AttributeQuery> taskAttributes = new ArrayList<AttributeQuery>();
			taskAttributes.add(new AttributeQuery(TaskAttributeXmlName.TEAM_ID,team,StringAttribute.identifier));
			dh.addAttribute("task", task, taskAttributes);
			
			
			Date current = new Date();
			String currentDateString = new DateTimeAttribute(current).encode();
			taskAttributes = new ArrayList<AttributeQuery>();
			taskAttributes.add(new AttributeQuery(TaskAttributeXmlName.START_TIME,currentDateString,DateTimeAttribute.identifier));
			dh.addAttribute("task", task, taskAttributes);
			return "Task" + task + " assigned to team " + team;
		}
		else if(obl.actionName.equals(ObligationIds.SHOW_DENY_REASON_OBLIGATION_ID))
		{
			String message = obl.getAttribute("message");
			return message;
			
		}
		else throw new IllegalArgumentException("system obligation not supported:" + obl.actionName);
	}
	
	@Override
	public void notifyDeadline(Obligation obl) {
		monitorable.notifyDeadline(obl);
	}

	public void fulfillObligation(Obligation obl)
	{
		obligationMonitor.obligationFulfilled(obl);
	}
	
	@Override
	public void notifyFulfillment(Obligation obl) {
		monitorable.notifyFulfillment(obl);
	}

	@Override
	public void notifyObligationInsert(Obligation obl) {
		monitorable.notifyObligationInsert(obl);
	}

}
