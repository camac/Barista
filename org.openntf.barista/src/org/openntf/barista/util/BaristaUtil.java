package org.openntf.barista.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.openntf.barista.config.ManagedBeanRuleSet;
import org.xml.sax.SAXException;

import com.ibm.commons.log.Log;
import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.dbproperties.XSPProperties;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import com.sun.faces.config.beans.FacesConfigBean;

public class BaristaUtil {

	public static LogMgr BARISTA_LOG = Log.load("org.openntf.barista",
			"Logger used for Barista");

	public static boolean scanLibraries() {
		return true;
	}

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

	public static FacesConfigBean createFacesConfigBean(
			DesignerProject designerProject) {

		FacesConfigBean fcb = new FacesConfigBean();

		Digester digester = createDigester(false);

		IFile config = designerProject.getProject().getFile(
				"WebContent/WEB-INF/faces-config.xml");

		if (config.exists()) {

			digester.clear();
			digester.push(fcb);

			try {
				digester.parse(config.getContents(false));

				if (scanLibraries()) {

					if (designerProject instanceof IDominoDesignerProject) {

						IDominoDesignerProject ddp = (IDominoDesignerProject) designerProject;

						XSPProperties props = new XSPProperties(ddp);
						String depends = props.getDependencies();

						if (StringUtil.isNotEmpty(depends)) {

							String[] libs = depends.split(",");

							for (String lib : libs) {

								System.out.println("Let us check out lib "
										+ lib);

								LibraryWrapper lw = LibraryServiceLoader
										.getLibrary(lib);

								String[] configs = lw.getFacesConfigFiles();

								for (String facesconfig : configs) {

									System.out.println(facesconfig);

									InputStream is = lw.getClassLoader()
											.getResourceAsStream(facesconfig);

									digester.push(fcb);
									digester.parse(is);

									try {
										is.close();
									} catch (IOException e) {

									}

									if (BARISTA_LOG.isTraceDebugEnabled()) {
										BARISTA_LOG.traceDebug(
												"Loading faces config for {0}",
												facesconfig);

									}

								}

							}
						}

					}

				}

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
