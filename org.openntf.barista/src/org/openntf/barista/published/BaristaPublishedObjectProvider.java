package org.openntf.barista.published;

import java.util.Map;

import org.openntf.barista.util.BaristaUtil;
import org.w3c.dom.Node;

import com.ibm.commons.log.LogMgr;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ide.resources.project.DominoDesignerProject;
import com.ibm.designer.domino.scripting.api.IScriptData.PublishedObject;
import com.ibm.designer.domino.scripting.api.published.PublishedObjectProvider;
import com.ibm.designer.prj.resources.commons.DesignerProjectException;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.ManagedBeanBean;

public class BaristaPublishedObjectProvider implements PublishedObjectProvider {

	private static LogMgr logger = BaristaUtil.BARISTA_LOG;

	private static String VIEW_NODE = "xp:view";

	public BaristaPublishedObjectProvider() {

		if (logger.isInfoEnabled()) {
			logger.info("Created the BaristaPublishedObjectProvider");
		}

	}

	@Override
	public void getPublishedObject(
			Map<String, PublishedObject> publishedObjects, Node node,
			DesignerProject designerProject, boolean notSureWhatThisIs)
			throws DesignerProjectException {

		if (designerProject instanceof DominoDesignerProject) {

			if (node.getNodeName().equals(VIEW_NODE)) {

				FacesConfigBean fcb = BaristaUtil
						.createFacesConfigBean(designerProject);

				for (ManagedBeanBean mbb : fcb.getManagedBeans()) {

					BaristaPublishedObject o = new BaristaPublishedObject(mbb);
					o.setNode(node);

					publishedObjects.put(mbb.getManagedBeanName(), o);

					o.setProject(designerProject);

				}

				if (BaristaUtil.scanLibraries()) {

					FacesConfigBean libfcb = BaristaUtil
							.createLibraryFacesConfigBean(designerProject);

					for (ManagedBeanBean mbb : libfcb.getManagedBeans()) {

						BaristaPublishedObject o = new BaristaPublishedObject(
								mbb);
						o.setNode(node);

						publishedObjects.put(mbb.getManagedBeanName(), o);

						o.setProject(designerProject);

					}
					
				}

			}

		}

	}

}
