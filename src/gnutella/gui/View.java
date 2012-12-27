package gnutella.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class View extends JFrame implements Observer{
	private Model model;

	private JPanel panel;

	private JButton startButton;
	private JButton pingButton;
	private JButton queryButton;
	private JButton addFileButton;
	private JButton downloadButton;

	private JTextArea outputArea;
	private JTextField portText;
	private JTextField fileNameText;

	public View(Model model){
		setSize(200,200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();

		startButton = new JButton("start");
		pingButton = new JButton("ping");
		queryButton = new JButton("query");
		addFileButton = new JButton("addFile");
		downloadButton = new JButton("download");

		portText = new JTextField("Input port", 15);
		fileNameText = new JTextField("Input filename", 15);

		outputArea = new JTextArea();
		JScrollPane scrollpane = new JScrollPane(outputArea);
		scrollpane.setPreferredSize(new Dimension(6, 15));
		outputArea.setEditable(false);

		panel.add(portText);
		panel.add(startButton);
		panel.add(pingButton);
		panel.add(fileNameText);
		panel.add(queryButton);
		panel.add(addFileButton);
		panel.add(outputArea);
		panel.add(scrollpane);

		Container contentPane = getContentPane();
		contentPane.add(panel, BorderLayout.CENTER);

		setVisible(true);
	}

	public void addToButtonActionListener(ActionListener actionListener){
		startButton.addActionListener(actionListener);
		startButton.setActionCommand("start");
		pingButton.addActionListener(actionListener);
		pingButton.setActionCommand("ping");
		queryButton.addActionListener(actionListener);
		queryButton.setActionCommand("query");
		addFileButton.addActionListener(actionListener);
		addFileButton.setActionCommand("addFile");
		downloadButton.addActionListener(actionListener);
		downloadButton.setActionCommand("download");
		portText.addActionListener(actionListener);
		portText.setActionCommand("port");
		fileNameText.addActionListener(actionListener);
		fileNameText.setActionCommand("filename");
	}

	@Override
	public void update(Observable o, Object arg){
		this.setInformatin();
	}

	private void setInformatin() {
		outputArea.append(model.getStateMessage() + "\n");
	}

	public String getPort(){
		return portText.getText();
	}

	public String getFilename(){
		return fileNameText.getText();
	}
}
