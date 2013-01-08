package gnutella.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Controller {
	private Model model;
	
	private String portTextFieldString;
	private String remoteAddressInputTextFieldString;
	private int queryHitTableSelectedIndex;

	public Controller(Model model) {
		this.model = model;
	}

	private final ActionListener buttonEventListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			if (cmd.equals("start")) {
				try {
					int port = Integer.parseInt(portTextFieldString);
					model.setPort(port);
				} catch (NumberFormatException ex) {
				}
				model.start();
			}
			else if (cmd.equals("ping")) {
				model.sendPing();
			}
			else if (cmd.equals("connect")) {
				try {
					String[] splitedText = remoteAddressInputTextFieldString.split(":");
					InetAddress address = InetAddress.getByName(splitedText[0]);
					int port = Integer.parseInt(splitedText[1]);
					model.connect(address, port);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (cmd.equals("query")) {
				model.sendQuery();
			}
			else if (cmd.equals("addFile")) {
				try {
					JFileChooser filechooser = new JFileChooser();
				    int selected = filechooser.showOpenDialog(null);
				    if (selected == JFileChooser.APPROVE_OPTION){
						model.addFile(filechooser.getSelectedFile().getAbsolutePath());
				    }
				} catch (IOException ex) {
					model.appendLogMessage(ex.getMessage());
				}
			}
			else if (cmd.equals("download")) {
				model.setSelectedMD5Digist(model.getSameMD5ResultSetContentList().keySet().toArray(new String[]{})[queryHitTableSelectedIndex]);
				model.sendDownloadRequest();
			}
		}
	};

	private final DocumentListener textFieldDocumentActionListener = new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent e) {
			textFieldChanged(e.getDocument());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			textFieldChanged(e.getDocument());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			textFieldChanged(e.getDocument());
		}
	};

	private void textFieldChanged(Document document) {
		try {
			switch (document.getProperty("name").toString()) {
			case "portText":
				this.portTextFieldString = document.getText(0, document.getLength());
				break;
			case "remoteAddressInputTextField":
				this.remoteAddressInputTextFieldString = document.getText(0, document.getLength());
				break;
			case "fileNameText":
				model.setKeyword(document.getText(0, document.getLength()));
				break;
			default:
				break;
			}
		} catch (BadLocationException e1) {
		}
	}

	private ListSelectionListener listSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			queryHitTableSelectedIndex = e.getFirstIndex();
		}
	};
	
	public ActionListener getButtonEventListener() {
		return buttonEventListener;
	}

	public DocumentListener getTextFieldDocumentActionListener() {
		return textFieldDocumentActionListener;
	}

	public ListSelectionListener getListSelectionListener() {
		return listSelectionListener;
	}
}