package org.ncibi.cytoscape.metdisease.ui;

import java.util.Enumeration;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.ncibi.cytoscape.metdisease.data.MeshTreeModel;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNode;
import org.ncibi.mesh.id.IdType;

import cytoscape.CyNetwork;
import cytoscape.CyNode;

@SuppressWarnings("serial")
public class MeshTree extends JTree {
	
	private TreePath[] previousSelectionPaths;
	private Enumeration<TreePath> previousExpansionPaths;
		
	public MeshTree(MeshTreeNode node, CyNetwork network, String idAttribute, IdType idType) {
		super(new MeshTreeModel(node, network, idAttribute, idType));
	}
	
	public void saveState() {
		previousExpansionPaths = getExpandedDescendants(new TreePath(treeModel.getRoot()));
		previousSelectionPaths = getSelectionPaths();
	}
	
	public void restorePreviousState() {
		if (previousExpansionPaths == null) return;
		while(previousExpansionPaths.hasMoreElements()) {
			expandPath(previousExpansionPaths.nextElement());
		}
		setSelectionPaths(previousSelectionPaths);
	}
	
	public MeshTreeModel getModel() {
		return (MeshTreeModel) treeModel;
	}
	
	public void setModel(TreeModel model) {
		if(!(model instanceof MeshTreeModel)) 
			throw new IllegalArgumentException();
		super.setModel(model); 
	}
	
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)  {
		MeshTreeNode treeNode = (MeshTreeNode) value;
		String label = treeNode.getPathLabel();
		if (!treeNode.getName().isEmpty())
			label += " (" + treeNode.getName() + ")" ;
		Set<CyNode> nodes = ((MeshTreeModel) treeModel).getNodeMapping(treeNode);
		if (nodes != null) {
			label = "<html><b>" + label + " -- " + nodes.size() + "</b></html>";
		} else {
			if (treeNode.isRoot()) return "<html><i>Disease Mesh Terms</i></html>";
			label = "<html>" + label + " -- no matches</html>"; 
		}
		return label;
	}
}
