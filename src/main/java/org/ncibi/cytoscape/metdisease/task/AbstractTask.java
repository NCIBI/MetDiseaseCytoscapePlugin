/*************************************************************************
 * Copyright 2012 Regents of the University of Michigan 
 * 
 * NCIBI - The National Center for Integrative Biomedical Informatics (NCIBI)
 *         http://www.ncib.org.
 * 
 * This product may includes software developed by others; in that case see specific notes in the code. 
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, 
 *      provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, 
 *      in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details, http://www.gnu.org/licenses/.
 * 
 * This work was supported in part by National Institutes of Health Grant #U54DA021519
 *
 ******************************************************************/
package org.ncibi.cytoscape.metdisease.task;

import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

public abstract class AbstractTask implements Task {

	protected TaskMonitor taskMonitor;
	protected boolean interrupted = false;
	
	public abstract String getTitle();
	public abstract void run();
	
	public static boolean configureAndRunTask(AbstractTask task) {
		// Configure JTask Dialog Pop-Up Box
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCancelButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setMillisToPopup(0);

		// Execute Task in New Thread; pops open JTask Dialog Box.
		if(TaskManager.executeTask(task, jTaskConfig) && !task.interrupted){
			return true;			
		}
		else {
			return false;
		}
	}

	public void setTaskMonitor(TaskMonitor monitor)
			throws IllegalThreadStateException {
		taskMonitor = monitor;
	}

	public void halt() {
		interrupted = true;
	}
}
