package org.ncibi.cytoscape.metdisease.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.ncibi.commons.file.DataFile;
import org.ncibi.commons.file.TextFile;
import org.ncibi.cytoscape.data.Attributes;
import org.ncibi.cytoscape.metdisease.data.MeshTreeModel;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNode;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNodeFactory;
import org.ncibi.cytoscape.util.FileUtils;
import org.ncibi.mesh.id.IdType;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;

@SuppressWarnings("serial")
public class MeshFilterPanel extends JPanel implements TreeSelectionListener, ComponentListener{

	private JCheckBox hideUnmatchedTerms;
	private JButton filterOptionsButton;
	private JButton selectAll;
	private JButton deselectAll;
	private JButton reapplySelection;
	private JButton exportToFile;
	private JButton close;
	private JPanel buttonPanel;
	private Map<CyNetwork, MeshTree> trees;
	private MeshTreeNode rootNode;
	private MeshTree currentTree;
	private MeshTreeModel currentModel;
	private JScrollPane scrollPane;
	private PropertyChangeListener networkModifiedListener;
	private PropertyChangeListener networkFocusListener;

	public MeshFilterPanel() {
		super(new BorderLayout());
		createControls();
		createListeners();
	}
	
	private void createControls() {
		
		buttonPanel = new JPanel();
		
		hideUnmatchedTerms = new JCheckBox("Hide Unmatched Terms");
		hideUnmatchedTerms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeDisplayState();
			}
		});
		buttonPanel.add(hideUnmatchedTerms);
		
		filterOptionsButton = new JButton("Filter Options...");
		filterOptionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MeshFilterOptionsDialog.showOptionsDialog(currentModel);
			}
		});
		buttonPanel.add(filterOptionsButton);
		
		selectAll = new JButton("Select All Terms");
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});
		buttonPanel.add(selectAll);

		deselectAll = new JButton("Deselect All Terms");
		deselectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deselectAll();
			}
		});
		buttonPanel.add(deselectAll);
		
		reapplySelection = new JButton("Reapply Selection");
		reapplySelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySelectionToNetwork();
			}
		});
		buttonPanel.add(reapplySelection);
		
		exportToFile = new JButton("Export to File...");
		exportToFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToFile();
			}
		});
		buttonPanel.add(exportToFile);
		
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		buttonPanel.add(close);

		add(buttonPanel, BorderLayout.NORTH);
		trees = new HashMap<CyNetwork, MeshTree>();
		rootNode = MeshTreeNodeFactory.buildTreeNodesFromFile("MeshTree.txt");
		currentTree = new MeshTree(rootNode, Cytoscape.getCurrentNetwork(), null, IdType.KEGG);
		currentTree.getSelectionModel().addTreeSelectionListener(this);
		trees.put(Cytoscape.getCurrentNetwork(), currentTree);
		currentModel = (MeshTreeModel) currentTree.getModel();
		scrollPane = new JScrollPane(currentTree);
		Cytoscape.getCurrentNetworkView().addNodeContextMenuListener
			(new PopupNodeContextMenuListener(currentTree));

		add(scrollPane, BorderLayout.CENTER);
		addComponentListener(this);
	}
	
	public void componentShown(ComponentEvent e) {
		addListeners();
		switchActiveTree();
	}
	
	public void componentHidden(ComponentEvent e) {
		removeListeners();
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		applySelectionToNetwork();
	}
	
	private void createListeners() {
		networkModifiedListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				currentTree.saveState();
				currentModel.reload();
				currentTree.restorePreviousState();
			}
		};
		
		networkFocusListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				switchActiveTree();
			}
		};
	}
	
	private void addListeners() {
		Cytoscape.getPropertyChangeSupport().
			addPropertyChangeListener(Cytoscape.NETWORK_MODIFIED,networkModifiedListener);
		Cytoscape.getDesktop().getSwingPropertyChangeSupport().
			addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUSED,networkFocusListener);
	}

	private void removeListeners() {
		Cytoscape.getPropertyChangeSupport().
			removePropertyChangeListener(Cytoscape.NETWORK_MODIFIED,networkModifiedListener);
		Cytoscape.getDesktop().getSwingPropertyChangeSupport().
			removePropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_FOCUSED,networkFocusListener);
	}
	
	private void switchActiveTree() {
		if(trees.get(Cytoscape.getCurrentNetwork()) != currentTree){
			currentTree.getSelectionModel().removeTreeSelectionListener(MeshFilterPanel.this);
			scrollPane.getViewport().setView(null);
			currentTree = trees.get(Cytoscape.getCurrentNetwork());
			if(currentTree == null) {
				currentTree = new MeshTree(rootNode, Cytoscape.getCurrentNetwork(), 
						currentModel.getIdAttribute(),currentModel.getIdType());
				trees.put(Cytoscape.getCurrentNetwork(), currentTree);
				Cytoscape.getCurrentNetworkView().addNodeContextMenuListener
					(new PopupNodeContextMenuListener(currentTree));
			}
			currentModel = (MeshTreeModel) currentTree.getModel();
			hideUnmatchedTerms.setSelected(currentModel.getHideUnmatchedTerms());
			scrollPane.getViewport().setView(currentTree);
			currentTree.getSelectionModel().addTreeSelectionListener(MeshFilterPanel.this);
		}
		applySelectionToNetwork();
	}
	
	private void changeDisplayState() {
		currentTree.saveState();
		currentModel.setHideUnmatchedTerms(hideUnmatchedTerms.isSelected());
		currentTree.restorePreviousState();
	}
	
	private void selectAll() {
		currentTree.setSelectionInterval(0, currentTree.getRowCount());
	}
	
	private void deselectAll() {
		currentTree.getSelectionModel().clearSelection();
	}
	
	private void applySelectionToNetwork() {
		CyNetwork network = Cytoscape.getCurrentNetwork();
		network.unselectAllNodes();
		network.unselectAllEdges();
    	TreePath[] paths = currentTree.getSelectionPaths();
    	if(paths != null) {
    		for(TreePath path: paths) {
    			MeshTreeNode treeNode = (MeshTreeNode) path.getLastPathComponent();
    			Set<CyNode> nodes = currentModel.getNodeMapping(treeNode);
    			if(nodes != null) {
    				network.setSelectedNodeState(nodes, true);
    				network.setSelectedEdgeState(network.getConnectingEdges(new ArrayList<CyNode>(nodes)), true);
    			}
    		}
    	}
    	CyNetworkView view = Cytoscape.getNetworkView(network
				.getIdentifier());
		if (view != null)
			view.redrawGraph(true, true);
    }
	
	private void exportToFile() {
		File outputFile = FileUtils.getFile("Output as File", FileUtils.SAVE,
				"csv", "CSV File");
		if (outputFile != null) {
			try {
				List<MeshTreeNode> nodes = currentModel.getMappedLeafNodes();
				DataFile base = new TextFile();
				base.setValue("MeSH Heading",0,0);
				base.setValue("MeSH Descriptor",0,1);
				base.setValue("Compound " + currentModel.getIdType().toString() +  " ID",0,2);
				base.setValue("Compound Name",0,3);
				int row=1;
				for(MeshTreeNode node: nodes) {
					for(CyNode cyNode: currentModel.getNodeMapping(node)){
						base.setValue(node.getNumber(),row,0);
						base.setValue(node.getName(),row,1);
						if(currentModel.getIdAttribute() != null) 
							base.setValue(Attributes.node.getAttribute(cyNode.getIdentifier(), currentModel.getIdAttribute()).toString(), row, 2);
						else
							base.setValue(cyNode.getIdentifier(), row, 2);
						base.setValue(Attributes.node.getStringAttribute(cyNode.getIdentifier(),"canonicalName"), row, 3);
						row++;
					}
				}
				base.save(outputFile);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void close() {
		CytoPanel cytoPanel = ((CytoPanel) getParent().getParent());
		cytoPanel.remove(this);
		removeListeners();
	}
	
	//not implemented
	public void componentMoved(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}
}
