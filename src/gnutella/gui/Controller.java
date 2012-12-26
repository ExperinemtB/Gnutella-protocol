package gnutella.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller implements ActionListener{
	private Model model;
	private View view;

	public Controller(Model model, View view){
		this.model = model;
		this.view = view;
	}
	public void actionPerformed(ActionEvent e){
		String cmd = e.getActionCommand();

		if(cmd.equals("start")){
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
		if(cmd.equals("port")){
			model.setPort(Integer.parseInt(view.getPort()));
		}
		if(cmd.equals("filename")){
			model.setfileName(view.getFilename());
		}
	}
}
