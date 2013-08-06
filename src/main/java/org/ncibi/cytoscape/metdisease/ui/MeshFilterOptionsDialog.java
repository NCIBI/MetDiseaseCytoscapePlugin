package org.ncibi.cytoscape.metdisease.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.ncibi.cytoscape.data.Attributes;
import org.ncibi.cytoscape.metdisease.data.MeshTreeModel;
import org.ncibi.mesh.id.IdType;

import cytoscape.Cytoscape;

@SuppressWarnings("serial")
public class MeshFilterOptionsDialog extends JDialog {
	private static final String IDENTIFIER = "Node Identifier";
	
	private JPanel idTypePanel;
	private TitledBorder idTypeBorder;
	private ButtonGroup idTypeButtonGroup;
	private JRadioButton keggButton;
	private JRadioButton pubchemButton;
	
	private JPanel selectAttributePanel;
	private TitledBorder selectAttributeBorder;
	private JComboBox selectAttributeComboBox;
	
	private JButton okButton;
	private JButton cancelButton;
	
	public static void showOptionsDialog(final MeshTreeModel treeModel) {
		MeshFilterOptionsDialog dialog = new MeshFilterOptionsDialog(Cytoscape.getDesktop(), treeModel);
		dialog.setVisible(true);
	}

	private MeshFilterOptionsDialog(Frame owner, final MeshTreeModel treeModel) {
		super(owner, true);
		setTitle("Filter Options");
		String[] attributeNames = Attributes.node.getAttributeNames();
		Arrays.sort(attributeNames);
		
		getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		idTypePanel = new JPanel();
		idTypePanel.add(Box.createHorizontalGlue());
		idTypeBorder = BorderFactory.createTitledBorder("Identifier Type");
		idTypePanel.setBorder(idTypeBorder);
		idTypePanel.setLayout(new BoxLayout(idTypePanel, BoxLayout.X_AXIS));
		idTypeButtonGroup = new ButtonGroup();
		keggButton = new JRadioButton("KEGG");
		keggButton.setSelected(treeModel.getIdType() == IdType.KEGG);
		idTypeButtonGroup.add(keggButton);
		idTypePanel.add(keggButton);
		pubchemButton = new JRadioButton("Pubchem");
		pubchemButton.setSelected(treeModel.getIdType() == IdType.PUBCHEM);
		idTypeButtonGroup.add(pubchemButton);
		idTypePanel.add(pubchemButton);
		idTypePanel.add(Box.createHorizontalGlue());
		getContentPane().add(idTypePanel);
		
		selectAttributePanel = new JPanel();
		selectAttributeBorder = BorderFactory.createTitledBorder("Select Attribute");
		selectAttributePanel.setBorder(selectAttributeBorder);
		selectAttributePanel.setLayout(new BoxLayout(selectAttributePanel, BoxLayout.X_AXIS));
		selectAttributeComboBox = new JComboBox(attributeNames);
		selectAttributeComboBox.insertItemAt(IDENTIFIER, 0);
		if(treeModel.getIdAttribute() == null)
			selectAttributeComboBox.setSelectedItem(IDENTIFIER);
		else selectAttributeComboBox.setSelectedItem(treeModel.getIdAttribute());
		selectAttributePanel.add(selectAttributeComboBox);
		getContentPane().add(selectAttributePanel);
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IdType idType = keggButton.isSelected() ? IdType.KEGG : IdType.PUBCHEM;
				String idAttribute = selectAttributeComboBox.getSelectedItem().toString();
				if(idAttribute == IDENTIFIER)
					treeModel.setIdAttributeAndType(null, idType);
				else treeModel.setIdAttributeAndType(idAttribute, idType);
				setVisible(false);
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		getContentPane().add(new Box(BoxLayout.X_AXIS) {
			{
				add(okButton);
				add(cancelButton);
			}
		});
		
		pack();
		setSize(new Dimension(300,getSize().height));
		setLocationRelativeTo(getOwner());
	}
}
