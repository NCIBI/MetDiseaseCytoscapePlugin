package org.ncibi.cytoscape.metscape.disease.data.test;

import junit.framework.Assert;

import org.junit.Test;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNodeFactory;
import org.ncibi.cytoscape.metdisease.data.MeshTreeNode;


public class MeshTreeBuilderTest {
	
	@Test
	public void testMeshTreeBuild() {
		MeshTreeNode rootNode = MeshTreeNodeFactory.buildTreeNodesFromFile("MeshTree.txt");
		Assert.assertNotNull(rootNode);
		Assert.assertTrue(rootNode.getChildCount() != 0);
		System.out.println(countChildren(rootNode));
	}
	
	public int countChildren(MeshTreeNode treeNode) {
		if(treeNode.getChildCount() == 0) return 0;
		else{
			int count = treeNode.getChildCount();
			for(int i = 0; i<treeNode.getChildCount(); i++) {
				count += countChildren((MeshTreeNode) treeNode.getChildAt(i));
			}
			return count;
		}
	}

}
