package org.ncibi.cytoscape.metdisease.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;


public class MeshTreeNodeFactory {
	
	public static MeshTreeNode buildTreeNodesFromFile(String filePath) {
		MeshTreeNode rootNode = new MeshTreeNode();
		try {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream inStream = classLoader.getResourceAsStream (filePath);
			if (inStream == null) 
				throw new IOException("Can not locate resource: " + filePath);
			BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			String line = in.readLine(); // skip header line


			Map<String,MeshTreeNode> nodeMap = new LinkedHashMap<String,MeshTreeNode>();
			while ((line = in.readLine()) != null){
				String[] parts = line.split("\t");
				if(parts.length != 4 || !parts[1].startsWith("C")) continue;
				MeshTreeNode node = new MeshTreeNode
				(Integer.parseInt(parts[0]),parts[1],parts[2],parts[3]);
				nodeMap.put(parts[1], node);
			}

			for(String number : nodeMap.keySet()) {
				if(!number.contains("."))
					rootNode.add(nodeMap.get(number));
				else {
					String parentId = number.substring(0, number.lastIndexOf("."));
					MeshTreeNode parentNode = nodeMap.get(parentId);
					parentNode.add(nodeMap.get(number));
				}
			}
		}
		catch(Throwable t) {
			return null;
		}

		return rootNode;
	}

}
