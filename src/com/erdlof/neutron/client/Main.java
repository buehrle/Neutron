package com.erdlof.neutron.client;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.erdlof.neutron.util.Request;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.CardLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

public class Main extends JFrame implements ClientListener, ActionListener {
	private static final long serialVersionUID = 527099896996818525L;
	
	private Connection connection;
	private Toolkit toolkit;
	private List<Partner> partners;
	
	private JTextField messageInput;
	private JTextField serverAdress;
	private JTextField clientName;
	private JPanel panel;
	private JButton btnConnect;
	private JLabel lblStatus;
	private JTextPane messageProvideBox;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() { //set up the window
		partners = new ArrayList<Partner>();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		toolkit = Toolkit.getDefaultToolkit();
		setTitle("Neutron v0.1");
		setSize(901, 501);
		
		int x = (int) (toolkit.getScreenSize().width - this.getWidth()) / 2;
		int y = (int) (toolkit.getScreenSize().height - this.getHeight()) / 2;
		
		setLocation(x, y);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(700, 400));
	
		this.initializeComponents();
		
		setVisible(true);
    }
	
	private void initializeComponents() { //this is made by WindowBuilder as I'm to bad to design GUIs.
		//TODO add elements
		JMenuBar mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new CardLayout(5, 5));
		
		messageInput = new JTextField();
		panel.add(messageInput, "name_6666103840325");
		messageInput.setColumns(10);
		
		messageProvideBox = new JTextPane();
		messageProvideBox.setEditable(false);
		getContentPane().add(messageProvideBox, BorderLayout.CENTER);
		
		JPanel loginContainer = new JPanel();
		getContentPane().add(loginContainer, BorderLayout.EAST);
		loginContainer.setLayout(new MigLayout("", "[200px,grow,left]", "[19px][19px][][][][][][][][][][][]"));
		
		serverAdress = new JTextField();
		loginContainer.add(serverAdress, "cell 0 0,alignx left,aligny top");
		serverAdress.setColumns(20);
		
		clientName = new JTextField();
		loginContainer.add(clientName, "cell 0 1,growx,aligny bottom");
		clientName.setColumns(20);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(this);
		loginContainer.add(btnConnect, "flowx,cell 0 2");
		
		lblStatus = new JLabel("test");
		loginContainer.add(lblStatus, "cell 0 2");
		
	}
	
	public synchronized void connectionEstablished() {
		
	}
	
	public synchronized void connectionFailed() {
		
	}
	
	public synchronized void disconnected() {
		
	}
	
	public synchronized void setRequest(int request, long senderID, byte[] data) {
		switch (request) { //TODO implement the notifications
			case Request.SEND_TEXT:
				break;
			case Request.CLIENT_CONNECT_NOTIFICATION:
				try {
					partners.add(new Partner(senderID, new String(data, "UTF-8")));
				} catch (UnsupportedEncodingException e) {}
				break;
			case Request.CLIENT_DISCONNECT_NOTIFICATION:
				partners.remove(getPartnerByID(partners, senderID));
				break;
		}
	}
	
	private Partner getPartnerByID(List<Partner> list, long ID) {
		for (Partner partner : list) {
			if (partner.getID() == ID) return partner;
		}
		return null;
	}
	
	public synchronized void setDisconnectRequest(int request) {
		switch (request) {
			case Request.UNEXPECTED_ERROR:
				break;
			case Request.SERVER_SHUTDOWN:
				break;
			case Request.KICKED_FROM_SERVER:
				break;
			case Request.ILLEGAL_REQUEST:
				break;
			case Request.ILLEGAL_NAME:
				break;
			default:
				break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnConnect) System.out.println("PEESDF");
		
	}
}
