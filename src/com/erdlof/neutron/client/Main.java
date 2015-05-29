package com.erdlof.neutron.client;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.CardLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Main extends JFrame implements ClientListener, ActionListener, KeyListener, WindowListener {
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
	private JPanel messageContainerPanel;
	private JScrollPane scrollPane;
	private StyledDocument mainMessages;
	private SimpleAttributeSet style;
	
	//ONLY FOR TESTING, I WILL MAKE IT MORE BEAUTIFUL
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() { //set up the window
		partners = new ArrayList<Partner>();
		
		style = new SimpleAttributeSet();
		
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
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setMinimumSize(new Dimension(700, 400));
	
		this.initializeComponents();
		addWindowListener(this);
		
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
		
		messageContainerPanel = new JPanel();
		getContentPane().add(messageContainerPanel, BorderLayout.CENTER);
		messageContainerPanel.setLayout(new CardLayout(5, 5));
		
		messageProvideBox = new JTextPane();
		
		scrollPane = new JScrollPane(messageProvideBox);
		messageContainerPanel.add(scrollPane, "name_21017317536325");
		
		mainMessages = messageProvideBox.getStyledDocument();
	}
	
	public synchronized void connectionEstablished() {
		lblStatus.setText("Connected.");
		messageProvideBox.setText("");
	}
	
	public synchronized void connectionFailed() {
		lblErrorDisplay.setText("Connection failed.");
		btnConnect.setEnabled(true);
	}
	
	public synchronized void disconnected() {
		lblStatus.setText("Disconnected");
		btnConnect.setEnabled(true);
		connection = null;
		//TODO ADD DISCONNECT/CONNECT SWITCH IN BUTTON
	}
	
	public synchronized void setRequest(int request, long senderID, byte[] data) {
		switch (request) { //TODO implement the notifications
			case Request.SEND_TEXT:
				appendText("["  + getPartnerByID(partners, senderID).getName() + "] " + new String(data), Color.BLACK);
				break;
			case Request.CLIENT_CONNECT_NOTIFICATION:
				appendText(new String(data) + " just logged in.", Color.RED);
				partners.add(new Partner(senderID, new String(data)));
				break;
			case Request.CLIENT_DISCONNECT_NOTIFICATION:
				partners.remove(getPartnerByID(partners, senderID));
				appendText(getPartnerByID(partners, senderID).getName() + " just logged out.", Color.RED);
				break;
		}
	}
	
	private void appendText(String text, Color textColor) {
		StyleConstants.setForeground(style, textColor);
		try {
			mainMessages.insertString(mainMessages.getLength(), text + "\n", style);
		} catch (BadLocationException e) {}
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
		if (connection != null && e.getKeyCode() == 10 && messageInput.getText().length() > 0){
			connection.sendData(Request.SEND_TEXT, messageInput.getText().getBytes());
			messageInput.setText("");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (connection != null) connection.disconnect();
		dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
}
