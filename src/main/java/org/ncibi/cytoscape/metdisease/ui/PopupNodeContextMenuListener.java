package org.ncibi.cytoscape.metdisease.ui;

import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.ncibi.commons.lang.StrUtils;
import org.ncibi.commons.web.BareBonesBrowserLaunch;
import org.ncibi.cytoscape.data.Attributes;
import org.ncibi.cytoscape.metdisease.data.MeshTreeModel;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNode;
import org.ncibi.cytoscape.util.ServiceProxyUtil;
import org.ncibi.mesh.id.IdType;
import org.ncibi.mesh.ws.client.MeshPubmedService;
import org.ncibi.ws.HttpRequestType;
import org.ncibi.ws.Response;

import cytoscape.CyNode;
import cytoscape.Cytoscape;
import ding.view.NodeContextMenuListener;

public class PopupNodeContextMenuListener implements NodeContextMenuListener {
	
	private MeshTree meshTree;
	private MeshTreeModel meshTreeModel;
	
	public PopupNodeContextMenuListener(MeshTree meshTree) {
		this.meshTree = meshTree;
		this.meshTreeModel = meshTree.getModel();
	}

	@Override
	public void addNodeContextMenuItems(NodeView nodeView, JPopupMenu menu) {
		final String id = nodeView.getNode().getIdentifier();
		final CyNode cyNode = (CyNode) nodeView.getNode();
		
		if(menu == null){
			menu = new JPopupMenu();
		}
		JMenu metdiseaseMenu = new JMenu("MetDisease");
		JMenuItem pubmedCitations = new JMenuItem("PubMed Citations");
		final String idAttribute = meshTree.getModel().getIdAttribute();
		
		if((idAttribute != null && !Attributes.node.hasAttribute(id, idAttribute))
				|| meshTree.isSelectionEmpty()) {
			pubmedCitations.setEnabled(false);
		}
		else {
			pubmedCitations.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					String cid = id;
					if(idAttribute != null)
						cid = Attributes.node.getAttribute(id, idAttribute).toString();
					IdType idType = meshTreeModel.getIdType();
					TreePath[] paths = meshTree.getSelectionPaths();
					Collection<String> treeNumbers = new HashSet<String>();
					for(TreePath path: paths) {
						treeNumbers.add(((MeshTreeNode) path.getLastPathComponent()).getNumber());
					}

					MeshPubmedService service = new MeshPubmedService(
							HttpRequestType.POST,ServiceProxyUtil.getWebServiceProxy());
					Response<Collection<Integer>> response = 
						service.retrievePubmedIdsForCidAndTreeNumbers(cid, idType, treeNumbers);
					if(response == null || response.getResponseValue() == null || response.getResponseValue().isEmpty()) {
						JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
								"No PubMed citations found for the selected node and MeSH term.", "Alert", JOptionPane.PLAIN_MESSAGE);
					}
					else {
						Collection<Integer> pmids = response.getResponseValue();
						String pmidString = StrUtils.COMMA_JOINER.join(pmids);
						BareBonesBrowserLaunch.openURL("http://www.ncbi.nlm.nih.gov/pubmed?term="+pmidString);
					}
				}
			});
		}
		metdiseaseMenu.add(pubmedCitations);
		
		JMenuItem relatedDiseases = new JMenuItem("Related Diseases");
		if((idAttribute != null && !Attributes.node.hasAttribute(id, idAttribute)))
			relatedDiseases.setEnabled(false);
		else {
			relatedDiseases.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					meshTree.clearSelection();
					List<MeshTreeNode> mappedLeafNodes = meshTreeModel.getMappedLeafNodes();
					for(MeshTreeNode node: mappedLeafNodes) {
						if(meshTreeModel.getNodeMapping(node).contains(cyNode)) {
							meshTree.addSelectionPath(new TreePath(node.getPath()));
						}
					}
				}
			});
		}
		
		
		metdiseaseMenu.add(relatedDiseases);
		menu.add(metdiseaseMenu);
	}

}
