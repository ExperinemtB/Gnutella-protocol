package gnutella.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Controller{
	private Model model;

	public Controller(Model model){
		this.model = model;
	}

	private final ActionListener buttonEventListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			String cmd = e.getActionCommand();

			if(cmd.equals("start")){
				try{
					int port = Integer.parseInt(model.getPortTextFieldString());
					model.setPort(port);
				}catch(NumberFormatException ex){
				}
				model.start();
			}
			if(cmd.equals("ping")){
				model.sendPing();
			}
			if(cmd.equals("query")){
				model.sendQuery();
			}
			if(cmd.equals("addFile")){
				model.addFile();
			}
			if(cmd.equals("download")){
				model.sendDownloadRequest();
			}
		}
	};

	public final DocumentListener textFieldDocumentActionListener = new DocumentListener(){
		@Override
		public void insertUpdate(DocumentEvent e) {
			portTextFieldChanged(e.getDocument());
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			portTextFieldChanged(e.getDocument());
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			portTextFieldChanged(e.getDocument());
		}
	};

	private void portTextFieldChanged(Document document) {
		try {
			model.setPortTextFieldString(document.getText(0, document.getLength()));
		} catch (BadLocationException e1) {
		}
	}

	public ActionListener getButtonEventListener() {
		return buttonEventListener;
	}
	public DocumentListener getTextFieldDocumentActionListener() {
		return textFieldDocumentActionListener;
	}
}