/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.mspa.client.view;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;

import org.martus.mspa.client.core.MirrorServerLabelFinder;
import org.martus.mspa.main.UiMainWindow;

public class MenuItemManagingMirrorServers extends AbstractAction
{
	public MenuItemManagingMirrorServers(UiMainWindow mainWindow, int manageType)
	{
		super(MirrorServerLabelFinder.getMessageInfo(manageType).getTitle());
		serverManageType = manageType;
		parent = mainWindow;
	}

	public void actionPerformed(ActionEvent arg0) 
	{	
		Vector availableList;
		Vector assignedList;			
		
		if (serverManageType == MirrorServerLabelFinder.LISTEN_FOR_CLIENTS)
		{							
			availableList = parent.getMSPAApp().getInActiveMagicWords();
			assignedList = parent.getMSPAApp().getActiveMagicWords();
		}
		else
		{
			availableList = new Vector();
			assignedList = new Vector();
		}			
			
		ManagingMirrorServersDlg serverManagementDlg = new ManagingMirrorServersDlg(parent, serverManageType,
					"", "", availableList, assignedList);
		serverManagementDlg.show();
	}	

	UiMainWindow parent;	
	int serverManageType;
}
