package org.ncibi.cytoscape.metdisease.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.ncibi.commons.lang.NumUtils;
import org.ncibi.cytoscape.metdisease.ui.MeshFilterPanel;

import cytoscape.Cytoscape;
import cytoscape.plugin.DownloadableInfo;
import cytoscape.plugin.PluginManager;
import cytoscape.plugin.PluginStatus;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

@SuppressWarnings("serial")
public class MeshFilterAction extends CytoscapeAction {
	
	private static final String NAME = "Filter by MeSH Terms";
	private static MeshFilterPanel panel;
	
	/**
	 * The constructor sets the text that should appear on the menu item.
	 */
	public MeshFilterAction() {
		super(NAME);
	}

	/**
	 * This method is called when the user selects the menu item.
	 */
	public void actionPerformed(ActionEvent ae) {
		exec();
	}
	
	public static void exec() {
		for(DownloadableInfo info: PluginManager.getPluginManager().getDownloadables(PluginStatus.CURRENT)) {
			if(info.getName().equals("MetScape")) {
				Double version = NumUtils.toDouble(info.getObjectVersion());
				if(version == null || version < 2.32) {
					System.out.println(version);
					JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
							"Versions of the MetScape plugin prior to 2.3.2 conflict with the operation of the MetDisease plugin.\n" +
							"Please upgrade to the latest version of the MetScape plugin before using MetDisease.", 
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
		if(panel == null) {
			panel = new MeshFilterPanel();
		}
		CytoPanel cytoPanelSouth = Cytoscape.getDesktop().getCytoPanel(SwingConstants.SOUTH);
		if(cytoPanelSouth.indexOfComponent(panel) == -1) {
			cytoPanelSouth.add("MeSH Terms", panel);
		}
		cytoPanelSouth.setSelectedIndex(cytoPanelSouth.indexOfComponent(panel));
		cytoPanelSouth.setState(CytoPanelState.DOCK);
	}	
}
