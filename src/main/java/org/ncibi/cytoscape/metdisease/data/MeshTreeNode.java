package org.ncibi.cytoscape.metdisease.data;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class MeshTreeNode extends DefaultMutableTreeNode {
	
	private static final String ROOT_LABEL = "MeSH Disease Terms";
	
	private final int descriptorId;
	private final String number;
	private final String name;
	private final String uniqueId;
	private final String[] meshPath;
	private final String pathLabel;
	
	public MeshTreeNode(){
		this(-1,"","","");
	}
	
	public MeshTreeNode(int descriptorId, String number, String name, String uniqueId){
		this.descriptorId = descriptorId;
		this.number = number;
		this.name = name;
		this.uniqueId = uniqueId;
		this.meshPath = makePath(number);
		if (this.meshPath.length > 0 && !meshPath[0].equals(""))
			pathLabel = this.meshPath[meshPath.length - 1];
		else
			pathLabel = ROOT_LABEL;
	}

	public int getDescriptorId() {
		return descriptorId;
	}

	public String getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	public String getUniqueId() {
		return uniqueId;
	}
	
	public String[] getMeshPath(){
		return meshPath;
	}
	
	public String getPathLabel() {
		return pathLabel;
	}
	
	private String[] makePath(String nString) {
		String parts[] = nString.split("\\.");
		return parts;
	}
}
