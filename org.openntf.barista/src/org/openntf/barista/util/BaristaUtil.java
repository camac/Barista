package org.openntf.barista.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.eclipse.core.resources.IFile;
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

	private static String FACESCONFIG_PATH = "WebContent/WEB-INF/faces-config.xml";

	private static LogMgr logger = BARISTA_LOG;

	public static boolean scanLibraries() {
		return true;
	}

	public static Digester createDigester(boolean validateXml) {

		Digester digester = new Digester();

		digester.setNamespaceAware(false);
		digester.setUseContextClassLoader(true);
		digester.setValidating(validateXml);

		try {
			digester.addRuleSet(new ManagedBeanRuleSet());
		} catch (IncompatibleClassChangeError e) {
			e.printStackTrace();
		}

		digester.push(new FacesConfigBean());

		return digester;

	}

	public static FacesConfigBean createFacesConfigBean(
			DesignerProject designerProject) {

		FacesConfigBean fcb = new FacesConfigBean();

		Digester digester = createDigester(false);

		processStandardFacesConfig(designerProject, digester, fcb);

		return fcb;

	}

	public static FacesConfigBean createLibraryFacesConfigBean(
			DesignerProject designerProject) {

		FacesConfigBean fcb = new FacesConfigBean();

		Digester digester = createDigester(false);

		processLibraryFacesConfig(designerProject, digester, fcb);

		return fcb;

	}

	private static void processStandardFacesConfig(
			DesignerProject designerProject, Digester digester,
			FacesConfigBean fcb) {

		IFile config = designerProject.getProject().getFile(FACESCONFIG_PATH);

		if (!config.exists()) {
			if (logger.isInfoEnabled()) {
				logger.info("faces-config.xml not found for project {0}",
						designerProject.getProject().getName());
			}
			return;
		}

		digester.clear();
		digester.push(fcb);

		try {

			digester.parse(config.getContents(false));

		} catch (Exception e) {

			if (logger.isErrorEnabled()) {
				logger.error(e,
						"Error Parsing Standard faces config for project {0}",
						designerProject.getProject().getName());
			}

		}

	}

	private static void processLibraryFacesConfig(
			DesignerProject designerProject, Digester digester,
			FacesConfigBean fcb) {

		if (designerProject instanceof IDominoDesignerProject) {

			IDominoDesignerProject ddp = (IDominoDesignerProject) designerProject;

			XSPProperties props = new XSPProperties(ddp);
			String depends = props.getDependencies();

			if (StringUtil.isNotEmpty(depends)) {

				String[] libs = depends.split(",");

				for (String libraryId : libs) {
					processLibrary(digester, fcb, libraryId);
				}
			}

		}

	}

	private static void processLibrary(Digester digester, FacesConfigBean fcb,
			String libraryId) {

		if (logger.isTraceDebugEnabled()) {
			logger.traceDebug("Checking for FacesConfig Files in Library {0}",
					libraryId);
		}

		LibraryWrapper lw = LibraryServiceLoader.getLibrary(libraryId);

		String[] configs = lw.getFacesConfigFiles();

		for (String facesconfig : configs) {

			if (logger.isTraceDebugEnabled()) {
				logger.traceDebug(
						"Parsing faces config: '{0}' from library: '{1}'",
						facesconfig, libraryId);
			}

			InputStream is = lw.getClassLoader().getResourceAsStream(
					facesconfig);

			digester.push(fcb);

			try {
				digester.parse(is);
			} catch (IOException e1) {

				if (logger.isErrorEnabled()) {
					logger.error(e1, "Error when parsing {0}", facesconfig);
				}

			} catch (SAXException e1) {

				if (logger.isErrorEnabled()) {
					logger.error(e1, "Error when parsing {0}", facesconfig);
				}

			}

			try {
				is.close();
			} catch (IOException e) {

			}

			if (logger.isTraceDebugEnabled()) {
				logger.traceDebug("Loading faces config for {0}", facesconfig);

			}

		}

	}
}
