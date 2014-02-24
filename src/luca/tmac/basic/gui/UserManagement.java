package luca.tmac.basic.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.toedter.calendar.JDateChooser;

import luca.tmac.basic.TmacPEP;
import luca.data.AttributeQuery;
import luca.data.DataHandler;
import luca.data.Entity;
/*
 * GUI for User management
 * */
public class UserManagement extends Box {

	private static final long serialVersionUID = 2632665607642904115L;

	private TmacPEP pep;
	private DataHandler dh;

	public ArrayList<Entity> userList;
	String[] tblColumnNames = { "id", "first_name", "last_name", "DoB",
			"trustworthiness", "budget","role" };

	public int LEFT_PANEL_SIZE = MainWindow.HORIZONTAL_SIZE / 3;
	public int RIGHT_PANEL_SIZE = MainWindow.HORIZONTAL_SIZE - LEFT_PANEL_SIZE;
	
	private JPanel leftPanel;
	private Box rightPanel;
	
	private JScrollPane scrollUserTable;
	UserTableModel userTableModel;
	public JTable userTable;
	
	private JTextField txtFirstName;
	private JTextField txtLastName;
	private JTextField txtTrustworthiness;
	private JTextField txtBudget;
	
	private JDateChooser calDoB;
	
	public UserManagement(TmacPEP pep, DataHandler dh) {
		super(BoxLayout.X_AXIS);
		this.pep = pep;
		this.dh = dh;
		getUserFromDB();
		initComponents();
	}

	public void initComponents() {
		// set leftPanel
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(5, 2, 10, 10));
		leftPanel.setPreferredSize(new Dimension(LEFT_PANEL_SIZE,
				MainWindow.TAB_VERTICAL_SIZE));
		leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		leftPanel.add(new JLabel("Name:",JLabel.RIGHT));
		txtFirstName = new JTextField();
		leftPanel.add(txtFirstName);
		
		leftPanel.add(new JLabel("Surname:",JLabel.RIGHT));
		txtLastName = new JTextField();
		leftPanel.add(txtLastName);
		
		leftPanel.add(new JLabel("Date of birth:",JLabel.RIGHT));
		calDoB = new JDateChooser();
		leftPanel.add(calDoB);
		
		leftPanel.add(new JLabel("Trustworthiness:",JLabel.RIGHT));
		txtTrustworthiness = new JTextField();
		leftPanel.add(txtTrustworthiness);
		
		leftPanel.add(new JLabel("Budget",JLabel.RIGHT));
		txtBudget = new JTextField();
		leftPanel.add(txtBudget);
		
		// set rightPanel
		rightPanel = new Box(BoxLayout.Y_AXIS);
		rightPanel.setPreferredSize(new Dimension(
				RIGHT_PANEL_SIZE,
				MainWindow.TAB_VERTICAL_SIZE));
		rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		rightPanel.add(new JLabel("User list"));

		userTable = new JTable(userTableModel = new UserTableModel());
		userTable.setCellSelectionEnabled(false);
		userTable.setColumnSelectionAllowed(false);
		userTable.setRowSelectionAllowed(true);
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel selectionModel = userTable.getSelectionModel();
		// selectionModel.addListSelectionListener(new MyListSelectionListener(this));
		scrollUserTable = new JScrollPane(userTable);
		scrollUserTable.setPreferredSize(new Dimension(
				RIGHT_PANEL_SIZE - 20,
				MainWindow.TAB_VERTICAL_SIZE));
		
		scrollUserTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		rightPanel.add(scrollUserTable);

		this.add(leftPanel);
		this.add(rightPanel);
	}

	public void getUserFromDB() {
		userList = new ArrayList<Entity>();
		List<String> ids = dh.getAttribute("user",
				new ArrayList<AttributeQuery>(), "id");
		for (String id : ids) {
			List<AttributeQuery> att = dh.getAttributesOf("user", id);
			userList.add(new Entity(att));
		}
	}

	protected class UserTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 5544276536155646889L;

		@Override
		public int getColumnCount() {
			return tblColumnNames.length;
		}

		@Override
		public int getRowCount() {
			return userList.size();
		}

		public String getColumnName(int col) {
			return tblColumnNames[col];
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return userList.get(arg0).getAttribute(tblColumnNames[arg1]);
		}
	}
}
