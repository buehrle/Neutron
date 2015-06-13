package com.erdlof.neutron.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener{
	private static final long serialVersionUID = 6621590562437128368L;
	private final String hint;
	private boolean showingHint;
	
	public HintTextField(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		if (this.getText().isEmpty()) {
			super.setText("");
			showingHint = false;
		}
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		if (this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
		
	}
	
	@Override
	public String getText() {
		if (showingHint) {
			return "";
		} else {
			return super.getText();
		}
	}

}
