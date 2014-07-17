package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * JFrame for Tree GUI. Heavily adapted from classroom code by Lou Nel
 * 
 * @author Gregory Bint
 * 
 */
public class TreeEditorGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 2101128840058843809L;

	private TreeEditor editor;
	private JScrollPane scrollpane;

	private JButton findButton = new JButton("Find");
	private JButton insertButton = new JButton("Insert");
	private JButton removeButton = new JButton("Remove");
	private JTextField itemsTextField = new JTextField();

	private JMenuBar aMenuBar = new JMenuBar();

	private JMenu fileMenu = new JMenu("File");
	private JMenuItem newBasicTreeItem = new JMenuItem("New Basic Tree");
	private JMenuItem newRedBlackTreeItem = new JMenuItem("New Red/Black Tree");
	private JMenuItem newSplayTreeItem = new JMenuItem("New Splay Tree");
	private JMenuItem newUTangoTreeItem = new JMenuItem(
			"New Unbalanced Tango Tree");
	private JMenuItem newTangoTreeItem = new JMenuItem("New Red/Black Tango Tree");
	private JMenuItem fileQuitItem = new JMenuItem("Quit");

	private JMenu insertMenu = new JMenu("Insert");
	private JMenuItem insert1to8 = new JMenuItem("Increasing 1 .. 8");
	private JMenuItem insert1to10 = new JMenuItem("Increasing 1 .. 10");
	private JMenuItem insert1to38 = new JMenuItem("Increasing 1 .. 38");
	private JMenuItem insert1to64 = new JMenuItem("Increasing 1 .. 64");

	/*
	 * Construction
	 */
	public TreeEditorGUI(String title) {

		super(title);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(layout);

		// create controller
		editor = new TreeEditor(this);

		scrollpane = new JScrollPane(editor);
		add(scrollpane);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 5;
		constraints.gridheight = 1;
		constraints.weightx = 100;
		constraints.weighty = 100;
		constraints.fill = GridBagConstraints.BOTH;
		layout.setConstraints(scrollpane, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		layout.setConstraints(findButton, constraints);
		add(findButton);
		findButton.addActionListener(this);

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		layout.setConstraints(removeButton, constraints);
		add(removeButton);
		removeButton.addActionListener(this);

		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		layout.setConstraints(insertButton, constraints);
		add(insertButton);
		insertButton.addActionListener(this);

		constraints.gridx = 3;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 100;
		constraints.weighty = 0;
		layout.setConstraints(itemsTextField, constraints);
		add(itemsTextField);

		initialize();
	}

	private void initialize() {
		setJMenuBar(aMenuBar);

		/* initialize File menu */
		fileMenu.add(newBasicTreeItem);
		fileMenu.add(newRedBlackTreeItem);
		fileMenu.add(newSplayTreeItem);
		fileMenu.add(newUTangoTreeItem);
		fileMenu.add(newTangoTreeItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(fileQuitItem);
		newBasicTreeItem.addActionListener(this);
		newRedBlackTreeItem.addActionListener(this);
		newSplayTreeItem.addActionListener(this);
		newUTangoTreeItem.addActionListener(this);
		newTangoTreeItem.addActionListener(this);
		fileQuitItem.addActionListener(this);
		aMenuBar.add(fileMenu);

		/* initialize Insert menu */
		insertMenu.add(insert1to8);
		insertMenu.add(insert1to10);
		insertMenu.add(insert1to38);
		insertMenu.add(insert1to64);
		insert1to8.addActionListener(this);
		insert1to10.addActionListener(this);
		insert1to38.addActionListener(this);
		insert1to64.addActionListener(this);
		aMenuBar.add(insertMenu);

		// needed for the scroll pane
		editor.setPreferredSize(new Dimension(editor.getWidth(), editor
				.getHeight() + 40));

		pack();
	}

	/*
	 * Menu Event Handlers
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		/* button handlers */
		if (e.getSource() == insertButton) {
			editor.insertItems(itemsTextField.getText().trim());
			itemsTextField.setText("");
		}

		else if (e.getSource() == removeButton) {
			editor.removeItems(itemsTextField.getText().trim());
			itemsTextField.setText("");
		}

		else if (e.getSource() == findButton) {
			editor.findItems(itemsTextField.getText().trim());
			itemsTextField.setText("");
		}

		/* file menu handlers */
		else if (e.getSource() == newBasicTreeItem) {
			editor.newBasicTree();
		} else if (e.getSource() == newRedBlackTreeItem) {
			editor.newRedBlackTree();
		} else if (e.getSource() == newSplayTreeItem) {
			editor.newSplayTree();
		} else if (e.getSource() == newUTangoTreeItem) {
			editor.newUTangoTree();
		} else if (e.getSource() == newTangoTreeItem) {
			editor.newTangoTree();
		} else if (e.getSource() == fileQuitItem) {
			System.exit(0);
		}

		/* insert menu handlers */
		else if (e.getSource() == insert1to8) {
			editor.insertIncreasing(1, 8);
		} else if (e.getSource() == insert1to10) {
			editor.insertIncreasing(1, 10);
		} else if (e.getSource() == insert1to38) {
			editor.insertIncreasing(1, 38);
		} else if (e.getSource() == insert1to64) {
			editor.insertIncreasing(1, 64);
		}

		editor.update();
	}

	public void runGUI() {

		// Add the usual window listener (for closing ability)
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		this.setVisible(true);
	}
}