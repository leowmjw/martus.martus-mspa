
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWordEntry;
import org.martus.common.MagicWords;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class MagicWordsDlg extends JDialog
{
	public MagicWordsDlg(UiMainWindow owner, Vector magicWords)
	{
		super((JFrame)owner, "Manage Magic Words", true);
		parent = owner;		
		
		magicWordsInfo = new MagicWords(new LoggerToConsole());	
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buildTopPanel(magicWords), BorderLayout.CENTER);
		getContentPane().add(buildButtonsPanel(), BorderLayout.SOUTH);						

		Utilities.centerDlg(this);
		setResizable(false);
		
	}	
	
	private JPanel buildTopPanel(Vector magicWords)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new ParagraphLayout());	

		JLabel magicWordLabel = new JLabel("Enter a new magic word:");				
		addMagicWordsField = new JTextField(10);
		addMagicWordsField.requestFocus();	
	
		addButton = new JButton("Add");
		addButton.addActionListener(new ButtonHandler());	
		
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(magicWordLabel);
		panel.add(addMagicWordsField);
		panel.add(addButton);			
		
		loadElementsToList(magicWords);
		activeWords = new JList(listModel);
		activeWords.setFixedCellWidth(200);    
		JScrollPane ps = new JScrollPane();
		ps.getViewport().add(activeWords);	
		removeButton = new JButton("Remove");
		removeButton.addActionListener(new ButtonHandler());	
				
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(ps);
		panel.add(removeButton);	
			
		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		saveButton = new JButton("Save");	
		saveButton.addActionListener(new ButtonHandler());	
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ButtonHandler());
		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
	}
	
	private void loadElementsToList(Vector items)
	{
		listModel = new DefaultListModel();
		for (int i=0; i<items.size();++i)
		{			
			String word = (String) items.get(i);
			MagicWordEntry entry = addNewMagicEntry(word);
			listModel.add(i, entry.getMagicWord());			
		}
	}	
	
	private MagicWordEntry addNewMagicEntry(String word)
	{
		MagicWordEntry entry = new MagicWordEntry(word);		
		magicWordsInfo.add(entry);		
		
		return entry;
	}
	
	class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(cancelButton))				
				dispose();
			else if (ae.getSource().equals(addButton))
				handleAddNewMagicWord();
			else if (ae.getSource().equals(saveButton))
				handleUpdateMagicWords();
			else if (ae.getSource().equals(removeButton))
				handleRemoveMagicWords();
		}
		
		private void handleAddNewMagicWord()
		{
			String newMagicWords = addMagicWordsField.getText();
			
			if (newMagicWords.startsWith("#"))
			{			
				JOptionPane.showMessageDialog(parent, "'#' denotes an inactive magic words", "Invalid character", JOptionPane.ERROR_MESSAGE);				
				addMagicWordsField.setText("");					
				return;
			}					
					
			if (!listModel.contains(newMagicWords))
			{	
				listModel.addElement(newMagicWords);
				magicWordsInfo.add(addNewMagicEntry(newMagicWords));
	
			}
				
			addMagicWordsField.setText("");		
		}
		
		private void handleUpdateMagicWords()
		{			
			parent.getMSPAApp().updateMagicWords(magicWordsInfo.getAllMagicWords());	
			dispose();			
		}
		
		private void handleRemoveMagicWords()
		{			
			int selectItem = activeWords.getSelectedIndex();
				
			if (!activeWords.isSelectionEmpty())
			{	
				String item = (String) activeWords.getSelectedValue();	
				
				listModel.remove(selectItem);					
				magicWordsInfo.remove(item);
			}							
		}		
	}
	
	JButton saveButton;
	JButton cancelButton;
	JButton removeButton;
	JButton addButton;
	JList activeWords;
	DefaultListModel listModel;
	JTextField addMagicWordsField;
	UiMainWindow parent;
	MagicWords magicWordsInfo; 	
}
