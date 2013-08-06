package org.ncibi.cytoscape.metdisease.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.ncibi.cytoscape.data.Attributes;
import org.ncibi.cytoscape.metdisease.task.AbstractTask;
import org.ncibi.cytoscape.util.ServiceProxyUtil;
import org.ncibi.mesh.id.IdType;
import org.ncibi.mesh.ws.client.MeshDiseaseService;
import org.ncibi.ws.HttpRequestType;
import org.ncibi.ws.Response;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.task.TaskMonitor;

public class MeshTreeModel implements TreeModel {

	private MeshTreeNode root;
	private CyNetwork network;
	private String idAttribute;
	private IdType idType;
	private Map<String, Set<CyNode>> nodeMappings;
	private Map<String, List<MeshTreeNode>> mappedChildren;
	private Set<TreeModelListener> listeners;
	private boolean hideUnmatchedTerms;
	
	public MeshTreeModel(MeshTreeNode root, CyNetwork network, String idAttribute, IdType idType) {
		this.root = root;
		this.network = network;
		this.idAttribute = idAttribute;
		this.idType = idType;
		this.nodeMappings = new HashMap<String, Set<CyNode>>();
		this.mappedChildren = new HashMap<String, List<MeshTreeNode>>();
		this.listeners = new HashSet<TreeModelListener>();
		this.hideUnmatchedTerms = false;
		buildMappings();
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (!(parent instanceof MeshTreeNode)) 
			return null;
		else {
			MeshTreeNode treeNode = (MeshTreeNode) parent;
			if(hideUnmatchedTerms) {
				List<MeshTreeNode> children = mappedChildren.get(treeNode.getNumber());
				if(children == null || children.size() <= index)	
					return null;
				else
					return children.get(index);
			}
			else {
				if(treeNode.getChildCount() <= index) 
					return null;
				else 
					return treeNode.getChildAt(index);
			}
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (!(parent instanceof MeshTreeNode)) 
			return -1;
		else {
			MeshTreeNode treeNode = (MeshTreeNode) parent;
			if(hideUnmatchedTerms) {
				List<MeshTreeNode> children = mappedChildren.get(treeNode.getNumber());
				if(children == null)
					return 0;
				else return children.size();
			}
			else return treeNode.getChildCount();
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if (!(node instanceof MeshTreeNode)) 
			return false;
		else {
			MeshTreeNode treeNode = (MeshTreeNode) node;
			if(hideUnmatchedTerms) {
				List<MeshTreeNode> children = mappedChildren.get(treeNode.getNumber());
				if(children == null || children.isEmpty())
					return true;
				else return false;
			}
			else return treeNode.isLeaf();
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		for(TreeModelListener l: listeners)
			l.treeNodesChanged(new TreeModelEvent(this,path));
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (!(parent instanceof MeshTreeNode && child instanceof MeshTreeNode)) 
			return -1;
		else {
			MeshTreeNode parentNode = (MeshTreeNode) parent;
			MeshTreeNode childNode = (MeshTreeNode) child;
			if(hideUnmatchedTerms) {
				List<MeshTreeNode> children = mappedChildren.get(parentNode.getNumber());
				if(children == null)
					return -1;
				else return children.indexOf(child);
			}
			else return parentNode.getIndex(childNode);
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}
	
	public String getIdAttribute() {
		return idAttribute;
	}

	public void setIdAttribute(String idAttribute) {
		this.idAttribute = idAttribute;
		reload();
	}

	public IdType getIdType() {
		return idType;
	}

	public void setIdType(IdType idType) {
		this.idType = idType;
		reload();
	}
	
	public void setIdAttributeAndType(String idAttribute, IdType idType) {
		this.idAttribute = idAttribute;
		this.idType = idType;
		reload();
	}

	public boolean getHideUnmatchedTerms() {
		return hideUnmatchedTerms;
	}

	public void setHideUnmatchedTerms(boolean hideUnmatchedTerms) {
		if(this.hideUnmatchedTerms != hideUnmatchedTerms) {
			this.hideUnmatchedTerms = hideUnmatchedTerms;
			for(TreeModelListener l: listeners)
				l.treeStructureChanged(new TreeModelEvent(this,new TreePath(root)));
		}
	}

	public Set<CyNode> getNodeMapping(MeshTreeNode node) {
		return nodeMappings.get(node.getNumber());
	}
	
	public List<MeshTreeNode> getMappedChildren(MeshTreeNode node) {
		return mappedChildren.get(node.getNumber());
	}
	
	public List<MeshTreeNode> getMappedLeafNodes() {
		return getMappedLeafNodes(root);
	}
	
	public void reload() {
		nodeMappings.clear();
		mappedChildren.clear();
		buildMappings();
		for(TreeModelListener l: listeners)
			l.treeStructureChanged(new TreeModelEvent(this,new TreePath(root)));
	}

	private List<MeshTreeNode> getMappedLeafNodes(MeshTreeNode root) {
		List<MeshTreeNode> nodes = new ArrayList<MeshTreeNode>();
		List<MeshTreeNode> children = getMappedChildren(root);
		for(MeshTreeNode node: children) {
			if(node.isLeaf()) nodes.add(node);
			nodes.addAll(getMappedLeafNodes(node));
		}
		return nodes;
	}

	private void buildMappings() {
		(new BuildMeshCidMappingsTask()).buildMappings();
	}
	
	public class BuildMeshCidMappingsTask extends AbstractTask {
		
		public void buildMappings() {
			BuildMeshCidMappingsTask task = new BuildMeshCidMappingsTask();
			AbstractTask.configureAndRunTask(task);
		}
		
		@Override
		public String getTitle() {
			return "Matching compounds to Disease Mesh Terms";
		}

		@Override
		public void run() {
			Map<String, CyNode> idToNode = new HashMap<String, CyNode>();
			for(Object obj: network.nodesList()) {
				CyNode node = (CyNode) obj;
				Object id = node.getIdentifier();
				if(idAttribute != null)
					id = Attributes.node.getAttribute(node.getIdentifier(), idAttribute);
				if(id != null)
					idToNode.put(id.toString(), node);
			}
			taskMonitor.setStatus("Calling Database for matches - takes a while");
			MeshDiseaseService service = new MeshDiseaseService(
					HttpRequestType.POST,ServiceProxyUtil.getWebServiceProxy());
			Response<Map<String,Collection<String>>> response = 
				service.retrieveDiseasesForCids(idToNode.keySet(),idType);
			taskMonitor.setStatus("Match query returned");
			if(response != null && response.getResponseValue() != null) {
				Map<String, Collection<String>> idToDiseases = response.getResponseValue();
				for(String id: idToNode.keySet()) {
					if(idToDiseases.get(id) != null) {
						Collection<String> meshTerms = idToDiseases.get(id);
						for(String meshTerm: meshTerms) {
							while(!meshTerm.isEmpty()){
								Set<CyNode> nodeMapping = nodeMappings.get(meshTerm);
								if(nodeMapping == null) {
									nodeMapping = new HashSet<CyNode>();
									nodeMappings.put(meshTerm, nodeMapping);
								}
								nodeMapping.add(idToNode.get(id));
								int lastPeriod = meshTerm.lastIndexOf(".");
								if(lastPeriod > 0)
									meshTerm = meshTerm.substring(0,lastPeriod);
								else
									meshTerm = "";
							}
						}
					}
				}
			}
			addMappedChildrenToModel(root);
		}
		
		private void addMappedChildrenToModel(MeshTreeNode node) {
			List<MeshTreeNode> children = new ArrayList<MeshTreeNode>();
			for(Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
				MeshTreeNode child = (MeshTreeNode) e.nextElement();
				if(getNodeMapping(child) != null) {
					children.add(child);
					addMappedChildrenToModel(child);
				}
			}
			mappedChildren.put(node.getNumber(), children);
		}
		
	}
	
}
