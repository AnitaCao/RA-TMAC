package luca.tmac.basic.gui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import luca.tmac.basic.TmacPEP;
import luca.tmac.basic.obligations.Obligation;
import luca.data.DataHandler;
import luca.data.XmlDataHandler;
import luca.tmac.basic.obligations.ObligationMonitorable;

public class MainWindow extends JFrame implements ObligationMonitorable {

	public static final String RESOURCE_PATH = "./tmac/basic/resources/data.xml";

	private static final long serialVersionUID = -8561161513239681330L;
	TmacPEP pep;
	DataHandler dh;

	private JTabbedPane mainPanel;
	private AccessRequestPanel accessRequestPanel;
	private UserManagement userManagementPanel;
	
	protected static final int VERTICAL_SIZE = 500;
	protected static final int HORIZONTAL_SIZE = 1200;
	
	protected static final int TAB_VERTICAL_SIZE = 300;
	protected static final int TAB_HORIZONTAL_SIZE = 1200;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		MainWindow main = new MainWindow();
	}

	public MainWindow() {
		setTitle("Risk-Aware GBAC");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(HORIZONTAL_SIZE, VERTICAL_SIZE);
		
		dh = new XmlDataHandler(RESOURCE_PATH);
		pep = new TmacPEP(dh, this);
		
		accessRequestPanel = new AccessRequestPanel(pep,dh);
		userManagementPanel = new UserManagement(pep,dh);
		
		
		mainPanel = new JTabbedPane();
		mainPanel.add("Access Requests",accessRequestPanel);
		mainPanel.add("User Management",userManagementPanel);
		mainPanel.setVisible(true);
		this.setContentPane(mainPanel);
		this.setVisible(true);
	}

	@Override
	public void notifyDeadline(Obligation obl) {
		if (accessRequestPanel != null && accessRequestPanel.oblTableModel != null)
			accessRequestPanel.oblTableModel.fireTableDataChanged();
		JOptionPane.showMessageDialog(this, "Deadline Missed", "!!!!!!!!!!!!",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void notifyFulfillment(Obligation obl) {
		// TODO notifyFulfillment implementation
	}

	@Override
	public void notifyObligationInsert(Obligation obl) {
		if (accessRequestPanel != null && accessRequestPanel.oblTableModel != null)
			accessRequestPanel.oblTableModel.fireTableDataChanged();
	}

}
