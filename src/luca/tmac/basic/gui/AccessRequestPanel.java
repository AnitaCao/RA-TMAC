package luca.tmac.basic.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.wso2.balana.attr.StringAttribute;

import luca.data.AttributeQuery;
import luca.data.DataHandler;
import luca.tmac.basic.ResponseParser;
import luca.tmac.basic.TmacPEP;
import luca.tmac.basic.data.uris.ActionAttributeURI;
import luca.tmac.basic.data.xml.PermissionAttributeXmlName;
import luca.tmac.basic.data.xml.SubjectAttributeXmlName;
import luca.tmac.basic.data.xml.TaskAttributeXmlName;
import luca.tmac.basic.data.xml.TeamAttributeXmlName;
import luca.tmac.basic.obligations.Obligation;
import luca.tmac.basic.obligations.ObligationIds;

public class AccessRequestPanel extends Box {

	/**
	 * 
	 */
	private static final long serialVersionUID = -413938301946698252L;

	protected static final String[] tblColumnNames = { "id", "setId", "team",
			"action_name", "deadline" };

	private TmacPEP pep;
	private DataHandler dh;

	private JPanel leftPanel;
	private JPanel rightPanel;
	private Box centerPanel;
	private Box centerUpPanel;
	private JPanel centerMiddlePanel;
	private Box centerDownPanel;
	private JScrollPane scrollTextArea;
	private JScrollPane scrollObligationTable;

	private JButton requestButton;
	private JButton acceptResponseButton;
	private JButton declineResponseButton;
	ObligationTableModel oblTableModel;

	private JTextField txtAction;
	private JTextField txtUserId;
	private JTextField txtResourceId;
	private JTextField txtTeamId;
	private JTextField txtTaskId;
	private JTextField txtResponse;
	private JTable tblObligation;

	private JTextArea txtAreaResult;

	private JComboBox cmbRequestType;

	private long responseParserId;

	public AccessRequestPanel(TmacPEP pep, DataHandler dh) {
		super(BoxLayout.X_AXIS);
		initComponents();
		this.dh = dh;
		this.pep = pep;
	}

	public void initComponents() {

		// Set left panel
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(8, 2, 10, 10));
		leftPanel.setPreferredSize(new Dimension(
				MainWindow.TAB_HORIZONTAL_SIZE / 3,
				MainWindow.TAB_VERTICAL_SIZE));
		leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		leftPanel.add(new JLabel("Request type:", JLabel.RIGHT));
		String[] reqTypeStrings = {
				ActionAttributeURI.ACTION_OBTAIN_PERMISSION,
				ActionAttributeURI.ACTION_TASK_ASSIGNMENT };
		cmbRequestType = new JComboBox(reqTypeStrings);
		cmbRequestType.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				JComboBox cb = (JComboBox) evt.getSource();
				String selectedRequest = (String) cb.getSelectedItem();
				if (selectedRequest
						.equals(ActionAttributeURI.ACTION_OBTAIN_PERMISSION)) {
					txtAction.setEnabled(true);
					txtResourceId.setEnabled(true);
				} else if (selectedRequest
						.equals(ActionAttributeURI.ACTION_TASK_ASSIGNMENT)) {
					txtAction.setEnabled(false);
					txtResourceId.setEnabled(false);
					txtAction.setText("");
					txtResourceId.setText("");
				}
			}
		});
		leftPanel.add(cmbRequestType);

		leftPanel.add(new JLabel("User id:", JLabel.RIGHT));
		txtUserId = new JTextField();
		leftPanel.add(txtUserId);

		leftPanel.add(new JLabel("Resource Id:", JLabel.RIGHT));
		txtResourceId = new JTextField();
		leftPanel.add(txtResourceId);

		leftPanel.add(new JLabel("Action:", JLabel.RIGHT));
		txtAction = new JTextField();
		leftPanel.add(txtAction);

		leftPanel.add(new JLabel("Team Id:", JLabel.RIGHT));
		txtTeamId = new JTextField();
		leftPanel.add(txtTeamId);

		leftPanel.add(new JLabel("Task Id:", JLabel.RIGHT));
		txtTaskId = new JTextField();
		leftPanel.add(txtTaskId);

		requestButton = new JButton();
		requestButton.setText("Send Request");
		requestButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				sendAccessRequest();
			}
		});
		leftPanel.add(new JLabel());
		leftPanel.add(requestButton);

		// Set central panel
		txtResponse = new JTextField();
		txtResponse.setEditable(false);
		centerUpPanel = new Box(BoxLayout.X_AXIS);
		centerUpPanel.add(new JLabel("Response:"));
		centerUpPanel.add(txtResponse);

		centerPanel = new Box(BoxLayout.Y_AXIS);
		centerPanel.setPreferredSize(new Dimension(
				MainWindow.TAB_HORIZONTAL_SIZE / 3,
				MainWindow.TAB_VERTICAL_SIZE));
		centerPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		txtAreaResult = new JTextArea(13, 23);
		txtAreaResult.setEditable(false);

		centerMiddlePanel = new JPanel();
		scrollTextArea = new JScrollPane(txtAreaResult);
		centerMiddlePanel.add(scrollTextArea);

		acceptResponseButton = new JButton();
		acceptResponseButton.setText("Accept permission");
		acceptResponseButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				acceptRequest();
			}
		});

		declineResponseButton = new JButton();
		declineResponseButton.setText("Decline permission");
		declineResponseButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				declineRequest();
			}
		});

		centerDownPanel = new Box(BoxLayout.X_AXIS);
		centerDownPanel.add(acceptResponseButton);
		centerDownPanel.add(declineResponseButton);

		centerPanel.add(centerUpPanel);
		centerPanel.add(centerMiddlePanel);
		centerPanel.add(centerDownPanel);

		// SET RIGHT PANEL
		rightPanel = new JPanel();
		rightPanel.setPreferredSize(new Dimension(
				MainWindow.TAB_HORIZONTAL_SIZE / 3,
				MainWindow.TAB_VERTICAL_SIZE));
		rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		rightPanel.add(new JLabel("Active user obligations"));
		tblObligation = new JTable(oblTableModel = new ObligationTableModel());
		tblObligation.setCellSelectionEnabled(false);
		tblObligation.setColumnSelectionAllowed(false);
		tblObligation.setRowSelectionAllowed(true);
		tblObligation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel selectionModel = tblObligation.getSelectionModel();
		selectionModel.addListSelectionListener(new MyListSelectionListener(
				this));

		scrollObligationTable = new JScrollPane(tblObligation);
		scrollObligationTable.setPreferredSize(new Dimension(
				MainWindow.TAB_HORIZONTAL_SIZE / 3 - 20,
				MainWindow.TAB_VERTICAL_SIZE));
		scrollObligationTable
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightPanel.add(scrollObligationTable);

		// adds the 3 panels to main panel
		this.add(leftPanel);
		this.add(centerPanel);
		this.add(rightPanel);

		this.setVisible(true);
		setEnabledPanel(centerPanel, false);
	}

	public void sendAccessRequest() {

		// INPUT CHECK
		ArrayList<AttributeQuery> attributeQuery = new ArrayList<AttributeQuery>();

		List<String> user_ids = dh.getAttribute(
				SubjectAttributeXmlName.SUBJECT_TABLE, attributeQuery,
				SubjectAttributeXmlName.ID);
		if (!user_ids.contains(txtUserId.getText())) {
			JOptionPane.showMessageDialog(this, "User not found", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String permission = "";
		List<String> permission_ids = null;
		if (cmbRequestType.getSelectedItem().equals(
				ActionAttributeURI.ACTION_OBTAIN_PERMISSION)) {

			attributeQuery.clear();
			attributeQuery.add(new AttributeQuery(
					PermissionAttributeXmlName.ACTION, txtAction.getText(),
					StringAttribute.identifier));
			attributeQuery.add(new AttributeQuery(
					PermissionAttributeXmlName.RESOURCE_ID, txtResourceId
							.getText(), StringAttribute.identifier));
			attributeQuery.add(new AttributeQuery(
					PermissionAttributeXmlName.RESOURCE_TYPE, "user",
					StringAttribute.identifier));

			permission_ids = dh.getAttribute(
					PermissionAttributeXmlName.PERMISSION_TABLE,
					attributeQuery, PermissionAttributeXmlName.ID);

			if (permission_ids == null || permission_ids.size() == 0) {
				JOptionPane.showMessageDialog(this,
						"Permission (Pair of action and resource) not found",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			permission = permission_ids.get(0);
		}

		attributeQuery = new ArrayList<AttributeQuery>();
		List<String> team_ids = dh.getAttribute(
				TeamAttributeXmlName.TEAM_TABLE, attributeQuery,
				TeamAttributeXmlName.ID);
		if (!team_ids.contains(txtTeamId.getText())) {
			JOptionPane.showMessageDialog(this, "Team not found", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		List<String> task_ids = dh.getAttribute(
				TaskAttributeXmlName.TASK_TABLE, attributeQuery,
				TaskAttributeXmlName.ID);
		if (!task_ids.contains(txtTaskId.getText())) {
			JOptionPane.showMessageDialog(this, "Task not found", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		ResponseParser rParser = pep.requestAccess(txtUserId.getText(),
				permission, txtTeamId.getText(), txtTaskId.getText(),
				cmbRequestType.getSelectedItem().toString());

		responseParserId = rParser.getParserId();
		txtResponse.setText(rParser.getDecision());

		List<Obligation> oblList = rParser.getObligation().getList();
		String oblText = "";
		for (Obligation obl : oblList) {
			oblText += obl.toString() + "\n";
		}
		txtAreaResult.setText(oblText);

		// GUI CHANGES
		if (rParser.getDecision().equals(ResponseParser.PERMIT_RESPONSE)) {
			setEnabledPanel(leftPanel, false);
			setEnabledPanel(rightPanel, false);
			setEnabledPanel(centerPanel, true);
		} else {
			String message = "";
			for (Obligation obl : rParser.getObligation().getList()) {
				if (obl.isSystemObligation())
					message += pep.performObligation(obl) + "\n";
			}
			txtAreaResult.setText(message);
		}

		// TODO set a task lock so nobody can request that task until user take
		// a decision
	}

	// perform system obligations and register user obligations
	private void acceptRequest() {
		String message = pep.acceptResponse(responseParserId);

		this.txtResponse.setText("");
		this.txtAreaResult.setText(message);
		setEnabledPanel(leftPanel, true);
		setEnabledPanel(rightPanel, true);
		setEnabledPanel(centerPanel, false);
	}

	private void declineRequest() {
		responseParserId = 0;

		this.txtResponse.setText("");
		this.txtAreaResult.setText("");
		setEnabledPanel(leftPanel, true);
		setEnabledPanel(rightPanel, false);
		setEnabledPanel(centerPanel, false);
	}

	private void setEnabledPanel(JComponent panel, boolean value) {
		panel.setEnabled(value);
		for (Component comp : panel.getComponents()) {
			if (comp instanceof JComponent) {
				setEnabledPanel((JComponent) comp, value);
			}
			comp.setEnabled(value);
		}
	}

	public void fulfillObligation(Obligation obl) {
		pep.fulfillObligation(obl);
		if (oblTableModel != null)
			oblTableModel.fireTableDataChanged();
	}

	protected class MyListSelectionListener implements ListSelectionListener {

		AccessRequestPanel parentPanel = null;

		public MyListSelectionListener(AccessRequestPanel parentPanel) {
			this.parentPanel = parentPanel;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedIndex = tblObligation.getSelectedRow();
			if (selectedIndex < 0
					|| selectedIndex > pep.obligationMonitor.getList().size() - 1)
				return;
			Obligation obl = pep.obligationMonitor.getList().get(selectedIndex);
			int answer = JOptionPane.showConfirmDialog(
					parentPanel,
					"Do you want to fulfill obligation with id="
							+ obl.getAttribute("id"), "Obligation fulfillment",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				fulfillObligation(obl);
			}
		}
	}

	protected class ObligationTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 5544276536155646889L;

		@Override
		public int getColumnCount() {
			return tblColumnNames.length;
		}

		@Override
		public int getRowCount() {
			return pep.obligationMonitor.getList().size();
		}

		public String getColumnName(int col) {
			return tblColumnNames[col];
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			Obligation obl = pep.obligationMonitor.getList().get(arg0);
			String columnName = tblColumnNames[arg1];
			String result = null;
			if (columnName.equals("team")) {
				result = obl.getAttribute("team");
			} else if (columnName.equals("deadline"))
				result = obl.getDeadline().toString();
			else if (columnName.equals("action_name"))
				result = obl
						.getAttribute(ObligationIds.ACTION_NAME_OBLIGATION_ATTRIBUTE);
			else if (columnName.equals(columnName))
				result = obl.getAttribute(columnName);

			if (result == null)
				result = "nothing";
			return result;
		}
	}
}
