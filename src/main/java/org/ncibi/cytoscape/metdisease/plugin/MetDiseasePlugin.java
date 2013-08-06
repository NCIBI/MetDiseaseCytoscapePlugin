package org.ncibi.cytoscape.metdisease.plugin;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ncibi.cytoscape.metdisease.action.MeshFilterAction;

import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;

public class MetDiseasePlugin extends CytoscapePlugin {
	
	public static JMenu metdiseaseMenu = null;
	public static JMenuItem meshFilterMenuItem = null;
	public static final String MENU_ITEM_NAME = "MetDisease";
	

	public MetDiseasePlugin()  {
		JMenu pluginsMenu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
		if (!pluginsMenu.isMenuComponent(metdiseaseMenu)) {
			metdiseaseMenu = new JMenu("MetDisease");
			pluginsMenu.add(metdiseaseMenu);
			
			meshFilterMenuItem = new JMenuItem(new MeshFilterAction());
			metdiseaseMenu.add(meshFilterMenuItem);
		}
	}

}
