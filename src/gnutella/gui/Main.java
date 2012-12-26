package gnutella.gui;

import java.awt.event.ActionListener;

public class Main {
	public static void main(String[] args){
		Model model = new Model();
		View view = new View(model);
		Controller controller= new Controller(model, view);
		view.addToButtonActionListener((ActionListener)controller);
		model.addObserver(view);
	}
}
