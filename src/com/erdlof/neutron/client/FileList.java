package com.erdlof.neutron.client;

import javax.swing.JFrame;
import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.UIManager;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.border.TitledBorder;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;

public class FileList extends JFrame implements ActionListener {
	private static final long serialVersionUID = -4909632870438142582L;
	private FileSelectorListener listener;
	private JList<SharedAssociation> list;
	private DefaultListModel<SharedAssociation> lm;
	private JButton btnUploadFile;
	private JButton btnDownloadFile;
	
	public FileList(FileSelectorListener listener) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //make it look beautiful.
		} catch (Exception e) {
		}
		
		this.listener = listener;
		
		setTitle("Files");
		setType(Type.UTILITY);
		setAlwaysOnTop(false);
		setResizable(false);
		setSize(301,437);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel listContainer = new JPanel();
		listContainer.setBorder(new TitledBorder(null, "Files on the server", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(listContainer, BorderLayout.CENTER);
		listContainer.setLayout(new CardLayout(5, 5));
		
		lm = new DefaultListModel<SharedAssociation>();
		list = new JList<SharedAssociation>(lm);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listContainer.add(list, "name_14690395186515");
		
		JPanel buttonContainer = new JPanel();
		getContentPane().add(buttonContainer, BorderLayout.SOUTH);
		
		btnUploadFile = new JButton("Upload file");
		btnUploadFile.addActionListener(this);
		buttonContainer.add(btnUploadFile);
		
		btnDownloadFile = new JButton("Download File");
		btnDownloadFile.addActionListener(this);
		buttonContainer.add(btnDownloadFile);
		
		this.setVisible(false);
	}
	
	public void newFile(SharedAssociation file) {
		synchronized(lm){
			lm.addElement(file);
		}
	}
	
	public void setFiles(List<SharedAssociation> files) {
		synchronized (lm) {
			lm.removeAllElements();
			
			for (SharedAssociation file : files) {
				lm.addElement(file);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnDownloadFile) {
			if (list.isSelectionEmpty()) {
				int selectionIndex = list.getSelectedIndex();
				
				SharedAssociation selected = lm.get(selectionIndex);
				
				if (selectionIndex != -1) {
					listener.downloadFile(selected.getID(), selected.getName());
				}
			}
		} else if (e.getSource() == btnUploadFile) {
			listener.uploadFile();
		}
		
	}
}
