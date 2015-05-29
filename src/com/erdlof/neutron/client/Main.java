package com.erdlof.neutron.client;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.UnsupportedEncodingException;
import java.security.KeyPairGenerator;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main extends JFrame implements ClientListener, ActionListener, KeyListener {
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
	private KeyPairGenerator generator;
	private JLabel lblErrorDisplay;
	//ONLY FOR TESTING, I WILL MAKE IT MORE BEAUTIFUL
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() { //set up the window
		partners = new ArrayList<Partner>();
		try {
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (Exception e) {
		}
		generator.initialize(2048);
		
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
	
	private void initializeComponents() { //this is made by WindowBuilder as I'm too bad to design GUIs.
		//TODO add elements
		JMenuBar mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new CardLayout(5, 5));
		
		messageInput = new JTextField();
		messageInput.addKeyListener(this);
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
		
		lblStatus = new JLabel("");
		loginContainer.add(lblStatus, "cell 0 2");
		
		lblErrorDisplay = new JLabel("");
		loginContainer.add(lblErrorDisplay, "cell 0 3");
		
	}
	
	public synchronized void connectionEstablished() {
		lblStatus.setText("Connected.");
	}
	
	public synchronized void connectionFailed() {
		lblErrorDisplay.setText("Connection failed.");
		btnConnect.setEnabled(true);
	}
	
	public synchronized void disconnected() {
		lblStatus.setText("Disconnected");
		btnConnect.setEnabled(true);
		connection = null;
	}
	
	public synchronized void setRequest(int request, long senderID, byte[] data) {
		switch (request) { //TODO implement the notifications
			case Request.SEND_TEXT:
				messageProvideBox.setText(messageProvideBox.getText() + new String(data) + "/n");
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
		String reason = "";
		
		switch (request) {
			case Request.UNEXPECTED_ERROR:
				reason = "Unexpected error.";
				break;
			case Request.SERVER_SHUTDOWN:
				reason = "Server shutdown.";
				break;
			case Request.KICKED_FROM_SERVER:
				reason = "You were kicked.";
				break;
			case Request.ILLEGAL_REQUEST:
				reason = "Illegal request (that is very bad)!";
				break;
			case Request.ILLEGAL_NAME:
				reason = "That nickname is not allowed.";
				break;
			default:
				break;
		}
		lblErrorDisplay.setText(reason);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnConnect) {
			connection = new Connection(generator.generateKeyPair(), clientName.getText(), serverAdress.getText(), this);
			new Thread(connection).start();
			lblErrorDisplay.setText("");
			btnConnect.setEnabled(false);
			lblStatus.setText("Connecting...");
		} else if (e.getSource() == messageInput) {
			if (connection != null) connection.sendData(Request.SEND_TEXT, messageInput.getText().getBytes());
		}
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (connection != null) connection.sendData(Request.SEND_TEXT, messageInput.getText().getBytes());
		
	}
}
