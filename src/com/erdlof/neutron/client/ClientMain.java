package com.erdlof.neutron.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.erdlof.neutron.swing.HintTextField;
import com.erdlof.neutron.util.FileUtils;
import com.erdlof.neutron.util.Request;

import net.miginfocom.swing.MigLayout;

public class ClientMain extends JFrame implements ClientListener, ActionListener, KeyListener, WindowListener, FileSelectorListener {
	private static final long serialVersionUID = 527099896996818525L;
	
	private Connection connection;
	private Toolkit toolkit;
	private KeyPairGenerator generator;
	private KeyPair keyPair;
	private Properties properties;
	private File settings;
	private FileInputStream settingsInputStream;
	private FileOutputStream settingsOutputStream;
	
	private JTextField messageInput;
	private JTextField serverAdress;
	private JTextField clientName;
	private JPanel panel;
	private JButton btnConnect;
	private JLabel lblStatus;
	private JTextPane messageProvideBox;
	private JLabel lblErrorDisplay;
	private JPanel messageContainerPanel;
	private JScrollPane scrollPane;
	private StyledDocument mainMessages;
	private SimpleAttributeSet style;
	private JPanel clientListContainer;
	private JList<SharedAssociation> clientList;
	private DefaultListModel<SharedAssociation> lm;
	private JFileChooser fileChooser;
	private JButton btnShowFiles;
	private FileList fileList;
	
	public static void main(String[] args) {
		new ClientMain();
	}
	
	public ClientMain() { //set up the window and defaultilize properties
		Security.addProvider(new BouncyCastleProvider());
		
		properties = new Properties();
		
		try {
			settings = new File(ClientMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "settings.xml");
			settings.createNewFile();
			
			settingsInputStream = new FileInputStream(settings);
			settingsOutputStream = new FileOutputStream(settings);
			
			FileUtils.copyStream(getClass().getResourceAsStream("/com/erdlof/neutron/client/default_settings.xml"), settingsOutputStream);
			
			properties.loadFromXML(settingsInputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		style = new SimpleAttributeSet();
		lm = new DefaultListModel<SharedAssociation>();
		fileList = new FileList(this);
		fileChooser = new JFileChooser();
		
		try {
			generator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
		} catch (Exception e) {
		}
		generator.initialize(2048);
		
		keyPair = generator.generateKeyPair(); //TODO additional config for custom key
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //make it look beautiful.
		} catch (Exception e) {
		}
		
		toolkit = Toolkit.getDefaultToolkit();
		setTitle("Neutron v0.1");
		setSize(901, 501);
		
		int x = (int) (toolkit.getScreenSize().width - this.getWidth()) / 2; //we want the window in the middle of our screen
		int y = (int) (toolkit.getScreenSize().height - this.getHeight()) / 2;
		
		setLocation(x, y);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //we do this on our own
		this.setMinimumSize(new Dimension(700, 400));
	
		this.initializeComponents();
		addWindowListener(this);
		
		setVisible(true);
    }
	
	private void initializeComponents() { //this is made by WindowBuilder as I'm too bad to design GUIs.
		JMenuBar mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		
		btnShowFiles = new JButton("Show shared files");
		btnShowFiles.addActionListener(this);
		mainMenuBar.add(btnShowFiles);
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
		loginContainer.setLayout(new MigLayout("", "[200px,grow,left]", "[19px][19px][][][grow][][][][][][][][][]"));
		
		serverAdress = new HintTextField("Server address");
		loginContainer.add(serverAdress, "cell 0 0,growx,aligny bottom");
		serverAdress.setColumns(20);
		
		clientName = new HintTextField("Nickname");
		loginContainer.add(clientName, "cell 0 1,growx,aligny bottom");
		clientName.setColumns(20);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(this);
		loginContainer.add(btnConnect, "flowx,cell 0 2");
		
		lblStatus = new JLabel("");
		loginContainer.add(lblStatus, "cell 0 2");
		
		lblErrorDisplay = new JLabel("");
		loginContainer.add(lblErrorDisplay, "cell 0 3");
		
		clientListContainer = new JPanel();
		clientListContainer.setBorder(new TitledBorder(null, "Users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		loginContainer.add(clientListContainer, "cell 0 4 1 10,grow");
		clientListContainer.setLayout(new CardLayout(0, 0));
		
		clientList = new JList<SharedAssociation>(lm);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientListContainer.add(clientList, "name_11791777654180");
		
		messageContainerPanel = new JPanel();
		getContentPane().add(messageContainerPanel, BorderLayout.CENTER);
		messageContainerPanel.setLayout(new CardLayout(5, 5));
		
		messageProvideBox = new JTextPane();
		messageProvideBox.setEditable(false);
		
		scrollPane = new JScrollPane(messageProvideBox);
		messageContainerPanel.add(scrollPane, "name_21017317536325");
		
		mainMessages = messageProvideBox.getStyledDocument();
	}
	
	private JLabel getlblStatus() {
		synchronized (lblStatus) {
			return lblStatus;
		}
	}
	
	private JLabel getlblErrorDisplay() {
		synchronized (lblErrorDisplay) {
			return lblErrorDisplay;
		}
	}
	
	private JTextPane getMessageProvideBox() {
		synchronized (messageProvideBox) {
			return messageProvideBox;
		}
	}
	
	private DefaultListModel<SharedAssociation> getClientListModel() {
		synchronized (clientList) {
			return lm;
		}
	}
	
	private JButton getbtnConnect() {
		synchronized (btnConnect) {
			return btnConnect;
		}
	}

	private JTextField getMessageInput() {
		synchronized (messageInput) {
			return messageInput;
		}
	}
	
	private JTextField getClientName() {
		synchronized (clientName) {
			return clientName;
		}
	}
	
	private JTextField getServerAdress() {
		synchronized (serverAdress) {
			return serverAdress;
		}
	}
	
	private JButton getBtnShowFiles() {
		synchronized (btnShowFiles) {
			return btnShowFiles;
		}
	}
	
	@Override
	public void connectionEstablished(List<SharedAssociation> partners, List<SharedAssociation> filesOnServer) { //is called when the connection has been successfully established
		for (SharedAssociation partner : partners) {
			lm.addElement(partner);
		}
		
		fileList.setFiles(filesOnServer);
		
		getlblStatus().setText("Connected.");
		getMessageProvideBox().setText("");
	}
	
	@Override
	public void connectionFailed() { //yeah
		getlblErrorDisplay().setText("Connection failed.");
		getbtnConnect().setEnabled(true);
	}
	
	@Override
	public void disconnected() { //called EVERY TIME on thread exit
		getlblStatus().setText("Disconnected");
		getbtnConnect().setText("Connect");
		getClientListModel().removeAllElements();
		connection = null;
	}
	
	public void textMessage(long senderID, byte[] message) {
		appendText("["  + SharedAssociation.getSharedAssociationByID(Arrays.copyOf(getClientListModel().toArray(), getClientListModel().toArray().length, SharedAssociation[].class), senderID).getName() + "] " + new String(message), Color.BLACK);
	}
	
	public void clientConnected(long senderID, byte[] name) {
		appendText(new String(name) + " just logged in.", Color.RED);
		getClientListModel().addElement(new SharedAssociation(senderID, new String(name)));
	}
	
	public void clientDisconnected(long senderID) {
		SharedAssociation tempPartner = SharedAssociation.getSharedAssociationByID(Arrays.copyOf(getClientListModel().toArray(), getClientListModel().toArray().length, SharedAssociation[].class), senderID);
		appendText(tempPartner.getName() + " just logged out.", Color.RED);
		getClientListModel().removeElement(tempPartner);
	}
	
	@Override
	public void newFile(long fileID, byte[] name) {
		appendText("A new file was uploaded: " + new String(name), Color.BLUE);
		fileList.newFile(new SharedAssociation(fileID, new String(name)));
	}
	
	private void appendText(String text, Color textColor) {
		synchronized (messageProvideBox) {
			StyleConstants.setForeground(style, textColor);
			try {
				mainMessages.insertString(mainMessages.getLength(), text + "\n", style);
				messageProvideBox.select(mainMessages.getLength(), mainMessages.getLength());
			} catch (BadLocationException e) {}
		}
	}
	
	@Override
	public void setDisconnectRequest(int request) {
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
		getlblErrorDisplay().setText(reason);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == getbtnConnect()) {
			if (connection == null) {
				if (!getClientName().getText().isEmpty() && !getServerAdress().getText().isEmpty()) {
					connection = new Connection(keyPair, getClientName().getText(), getServerAdress().getText(), this);
					connection.start();
					getlblErrorDisplay().setText("");
					
					getbtnConnect().setText("Disconnect");
					getlblStatus().setText("Connecting...");
				}
			} else {
				connection.disconnect();
			}
		} else if (e.getSource() == getBtnShowFiles()) {
			fileList.setLocationRelativeTo(this);
			fileList.setVisible(true);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (connection != null && e.getKeyCode() == 10 && getMessageInput().getText().length() > 0){
			connection.sendData(Request.SEND_TEXT, getMessageInput().getText().getBytes());
			getMessageInput().setText("");
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
		try {
			settingsInputStream.close();
			settingsOutputStream.close();
		} catch (Exception e) {
			
		}
		
		if (connection != null) connection.disconnect();
		fileList.dispose();
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

	@Override
	public void downloadFile(long ID, String name) {
		if (connection != null) {
			fileChooser.setCurrentDirectory(new File(fileChooser.getCurrentDirectory().getPath() + File.pathSeparator + name));
			int returnVal = fileChooser.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				
				connection.downloadFile(file.getAbsolutePath(), ID);
			}
		}
	}

	@Override
	public void uploadFile() {
		if (connection != null) {
			int returnVal = fileChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				connection.uploadFile(file);
			}
		}
	}

	public Properties getproperties() {
		synchronized(properties) {
			return properties;
		}
	}
	
}
