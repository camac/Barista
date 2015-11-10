package org.openntf.barista.util;

import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.openntf.barista.config.ManagedBeanRuleSet;
import org.xml.sax.SAXException;

import com.ibm.commons.log.Log;
import com.ibm.commons.log.LogMgr;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.sun.faces.config.beans.FacesConfigBean;

public class BaristaUtil {

	public static LogMgr BARISTA_LOG = Log.load("org.openntf.barista", "Logger used for Barista");

	public static Digester createDigester(boolean validateXml) {

		Digester digester = new Digester();

		digester.setNamespaceAware(false);
		digester.setUseContextClassLoader(true);
		digester.setValidating(validateXml);

		try {
			// digester.addRuleSet(new FacesConfigRuleSet(true, false, false));
			digester.addRuleSet(new ManagedBeanRuleSet());
		} catch (IncompatibleClassChangeError e) {
			e.printStackTrace();
		}
		// for (int i = 0; i < DTD_INFO.length; i++) {
		// URL url = getClass().getResource(DTD_INFO[i][0]);
		// if (url != null) {
		// digester.register(DTD_INFO[i][1], url.toString());
		// } else {
		// throw new FacesException(Util.getExceptionMessageString(
		// "com.sun.faces.NO_DTD_FOUND_ERROR", new Object[] {
		// DTD_INFO[i][1], DTD_INFO[i][0] }));
		// }
		// }
		digester.push(new FacesConfigBean());

		return digester;

	}
	
	public static FacesConfigBean createFacesConfigBean(DesignerProject designerProject) {

		FacesConfigBean fcb = new FacesConfigBean();

		Digester digester = createDigester(false);

		IFile config = designerProject.getProject().getFile(
				"WebContent/WEB-INF/faces-config.xml");

		if (config.exists()) {

			digester.clear();
			digester.push(fcb);

			try {
				digester.parse(config.getContents(false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return fcb;

	}
	
}
