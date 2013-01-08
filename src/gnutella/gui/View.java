package gnutella.gui;

import gnutella.Host;
import gnutella.message.QueryHitMessage;
import gnutella.message.ResultSetContent;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class View extends JFrame implements Observer {
	private static final long serialVersionUID = -2095015549945683827L;
	private JPanel panel;

	private JButton startButton;
	private JButton connectButton;
	private JButton pingButton;
	private JButton queryButton;
	private JButton addFileButton;
	private JButton downloadButton;

	private JTextArea outputArea;
	private JTextField portText;
	private JTextField fileNameText;
	private JTextField remoteAddressInputTextField;

	private JTable connectedHostListTable;
	private JTable queryHitMessageListTable;

	private DefaultTableModel onlineTableModel;
	private QueryHitTableModel queryHitTableModel;

	public View(Model model) {
		setSize(200, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();

		startButton = new JButton("start");
		connectButton = new JButton("connect");
		pingButton = new JButton("ping");
		queryButton = new JButton("query");
		addFileButton = new JButton("addFile");
		downloadButton = new JButton("download");

		portText = new JTextField("Input port", 15);
		portText.getDocument().putProperty("name", "portText");
		remoteAddressInputTextField = new JTextField(15);
		remoteAddressInputTextField.getDocument().putProperty("name", "remoteAddressInputTextField");
		remoteAddressInputTextField.setToolTipText("RemoteAddress");

		fileNameText = new JTextField(15);
		fileNameText.getDocument().putProperty("name", "fileNameText");
		fileNameText.setToolTipText("query");

		outputArea = new JTextArea();
		outputArea.setEditable(false);
		JScrollPane scrollpane = new JScrollPane(outputArea);
		scrollpane.setPreferredSize(new Dimension(250, 90));

		// Gnutellaネットワーク上のノードを表示するテーブル
		onlineTableModel = new DefaultTableModel(new String[] { "アドレス", "タイプ" }, 0);
		connectedHostListTable = new JTable(onlineTableModel);
		JScrollPane connectedHostListTableSP = new JScrollPane(connectedHostListTable);
		connectedHostListTableSP.setPreferredSize(new Dimension(250, 90));

		// QueryHitの結果を表示するテーブル
		queryHitTableModel = new QueryHitTableModel();
		queryHitMessageListTable = new JTable(queryHitTableModel);
		JScrollPane queryHitMessageListTableSP = new JScrollPane(queryHitMessageListTable);
		queryHitMessageListTable.setPreferredSize(new Dimension(250, 90));

		panel.add(portText);
		panel.add(startButton);
		panel.add(remoteAddressInputTextField);
		panel.add(connectButton);
		panel.add(pingButton);
		panel.add(connectedHostListTableSP);
		panel.add(fileNameText);
		panel.add(queryButton);
		panel.add(queryHitMessageListTableSP);
		panel.add(addFileButton);
		panel.add(scrollpane);
		panel.add(downloadButton);
		Container contentPane = getContentPane();
		contentPane.add(panel, BorderLayout.CENTER);

		setVisible(true);
	}

	private void addToButtonActionListener(ActionListener actionListener) {
		startButton.addActionListener(actionListener);
		startButton.setActionCommand("start");
		connectButton.addActionListener(actionListener);
		connectButton.setActionCommand("connect");
		pingButton.addActionListener(actionListener);
		pingButton.setActionCommand("ping");
		queryButton.addActionListener(actionListener);
		queryButton.setActionCommand("query");
		addFileButton.addActionListener(actionListener);
		addFileButton.setActionCommand("addFile");
		downloadButton.addActionListener(actionListener);
		downloadButton.setActionCommand("download");
	}

	private void addToTextFieldDocumentListener(DocumentListener documentListener) {
		portText.getDocument().addDocumentListener(documentListener);
		remoteAddressInputTextField.getDocument().addDocumentListener(documentListener);
		fileNameText.getDocument().addDocumentListener(documentListener);
	}

	private void addToTabelListSelectionListener(ListSelectionListener listSelectionListener) {
		queryHitMessageListTable.getSelectionModel().addListSelectionListener(listSelectionListener);
	}

	public void addEventListener(Controller controller) {
		addToButtonActionListener(controller.getButtonEventListener());
		addToTextFieldDocumentListener(controller.getTextFieldDocumentActionListener());
		addToTabelListSelectionListener(controller.getListSelectionListener());
	}

	@Override
	public void update(Observable o, Object arg) {
		Model model = (Model) o;
		this.setInformatin(model);
		if (arg != null && arg.toString().equals("onlineHosts")) {
			setOnlineTable(model);
		} else if (arg != null && arg.toString().equals("sameMD5ResultSetContentList")) {
			setQueryHitTable(model);
		}

	}

	private synchronized void setInformatin(Model model) {
		outputArea.setText(model.getLogMessage());
	}

	private synchronized void setOnlineTable(Model model) {
		Host[] hostArray = model.getOnlineHosts();
		// テーブルの表示内容削除
		int currentTotalCount = this.onlineTableModel.getRowCount();
		for (int i = 0; i < currentTotalCount; i++) {
			this.onlineTableModel.removeRow(0);
		}
		for (Host host : hostArray) {
			this.onlineTableModel.addRow(new String[] { String.format("%s:%d", host.getAddress().getHostName(), host.getAddress().getPort()), host.getHostType().name() });
		}
	}

	private synchronized void setQueryHitTable(Model model) {
		// テーブルの表示内容削除
		this.queryHitTableModel.clearRow();

		// MD5ハッシュごとにまとめて表示する
		HashMap<String, ArrayList<SimpleEntry<QueryHitMessage, Integer>>> sameMD5ResultSetContentMap = model.getSameMD5ResultSetContentList();
		for (final ArrayList<SimpleEntry<QueryHitMessage, Integer>> contentList : sameMD5ResultSetContentMap.values()) {
			final String[] sameMD5FileNames = new String[contentList.size()];
			int i = 0;
			for (final SimpleEntry<QueryHitMessage, Integer> contentInQueryHitMessage : contentList) {
				final ResultSetContent content = contentInQueryHitMessage.getKey().getResultSet().getByFileIndex(contentInQueryHitMessage.getValue());
				sameMD5FileNames[i] = content.getFileName();
				if (i == contentList.size() - 1) {
					// とりあえずMD5ハッシュ値とファイル名と人数だけで
					this.queryHitTableModel.addRow(new QueryHitTableBean() {
						{
							MD5Digest = content.getFileMD5digest();
							this.fileNames = sameMD5FileNames;
							fileSharingNodeCount = contentList.size();
						}
					});
				}
				i++;
			}
		}
	}
}
