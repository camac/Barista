package org.openntf.barista.published;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.openntf.barista.util.BaristaUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.commons.xml.NamespaceContext;
import com.ibm.commons.xsd.xml.XmlDefinition;
import com.ibm.commons.xsdutil.xsd.XSDSchemaHelper;
import com.ibm.commons.xsdutil.xsd.schema.XSDSchemaSet;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.ui.commons.DesignerUIResource;
import com.ibm.designer.domino.xsp.api.published.XmlPublishedObject;
import com.ibm.designer.domino.xsp.data.XSPDataUtil;
import com.ibm.designer.runtime.DesignerRuntime;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSType.DesignTimeClassLoader;
import com.sun.faces.config.beans.ManagedBeanBean;

public class BaristaPublishedObject extends XmlPublishedObject {

	XmlDefinition _xmlDef;

	private static LogMgr logger = BaristaUtil.BARISTA_LOG;
	
	private static FBSType.DesignTimeClassLoader registeredDesignTimeClassLoader = null;
	
	private String className = null;

	public BaristaPublishedObject(ManagedBeanBean bean) {

		super(bean.getManagedBeanName());

		this.className = bean.getManagedBeanClass();

		if (logger.isInfoEnabled()) {
			logger.info("Created Barista Published Object Name: {0}, Class: {1}", getName(), className);
		}
		
		// Should determine if from
		// Library
		// NSF
		// Somewhere else e.g. standard libraries

	}

	// This is Data Pallette stuff
	@Override
	public XmlDefinition getXmlDefinition() {

		if (this._xmlDef == null) {

			this._xmlDef = new XmlDefinition();

			Element element = (Element) getNode();

			if (element != null) {

				this._xmlDef.setCurrentXpath(element.getAttribute("context"));

				// IDominoDesignerProject dp = (IDominoDesignerProject)
				// getProperty("project");

				XSDSchemaSet ss = new XSDSchemaSet();

				// gotta do somethin here
				ss.addRootSchema(createSchema(getNode()));

				NamespaceContext nc = XSPDataUtil.getNamespaceContext(element);
				this._xmlDef.setNamespaceContext(nc);
				this._xmlDef.setSchemaSet(ss);

			}

		}

		return this._xmlDef;

	}

	private XSDSchema createSchema(Node node) {

		// Copying from SchemaGeneratorHelper.generateSchema

		// Here you might get an attribute which says what type

		XSDSchema schema = XSDFactory.eINSTANCE.createXSDSchema();
		schema.setSchemaForSchemaQNamePrefix("xsd");

		Map<String, String> map = schema.getQNamePrefixToNamespaceMap();
		map.put(schema.getSchemaForSchemaQNamePrefix(),
				"http://www.w3.org/2001/XMLSchema");

		schema.updateElement(true);

		Element element = schema.getElement();

		generateBeanSchema(element, node);

		return getUpdatedSchema(schema);

	}

	private XSDSchema getUpdatedSchema(XSDSchema schema) {

		schema.updateElement(true);
		ByteStreamCache cache = new ByteStreamCache();

		XSDResourceImpl.serialize(cache.getOutputStream(), schema.getElement());

		schema = XSDSchemaHelper.openSchema(new InputStreamReader(cache
				.getInputStream()));

		try {
			cache.getOutputStream().close();
			cache.getInputStream().close();
		} catch (IOException e) {

		}

		return schema;

	}

	private Class<?> loadMyClass(String className) {

		Class<?> c = null;

		try {
			c = getClass().getClassLoader().loadClass(className);

			if (c != null)
				return c;

		} catch (ClassNotFoundException e) {

		}

		try {
			IDominoDesignerProject dp = (IDominoDesignerProject) getProperty(PROPERTY_PROJECT);
			// IDominoDesignerProject dp = //
			// DesignerUIResource.getLastSelection().getProject();

			c = dp.getRuntimeApplication().loadClass(className);

			dp.getRuntimeApplication().getClassLoader();

			if (c != null)
				return c;

		} catch (ClassNotFoundException e) {

		}

		Iterator<DesignTimeClassLoader> it = DesignerRuntime.getJSContext()
				.getDesignTimeClassLoaders();

		while (it.hasNext()) {

			DesignTimeClassLoader cl = it.next();

			c = cl.loadClass(className);

			if (c != null) {
				return c;
			}

		}

		return c;

	}

	private void generateBeanSchema(Element element, Node node) {

		try {

			String className = this.className;

			Class<?> c = loadMyClass(className);

			BeanInfo info = Introspector.getBeanInfo(c, Object.class);

			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {

				Element itemElement = element.getOwnerDocument().createElement(
						"xsd:element");

				itemElement.setAttribute("name", pd.getName());
				itemElement.setAttribute("minOccurs", "0");

				String type = pd.getPropertyType().getName();

				if (type.endsWith("Integer")) {
					itemElement.setAttribute("type", "xsd:decimal");
				} else if (type.endsWith("Date")) {
					itemElement.setAttribute("type", "xsd:date");
				} else {
					itemElement.setAttribute("type", "xsd:string");
				}

				element.appendChild(itemElement);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void registerDynamicClassLoader() {

		IDominoDesignerProject dp = (IDominoDesignerProject) getProperty(PROPERTY_PROJECT);
		DesignerUIResource.getLastSelection().getProject();

		ClassLoader newLoader = dp.getRuntimeApplication().getClassLoader();

		registeredDesignTimeClassLoader = new FBSType.SimpleDesignTimeClassLoader(newLoader);

		DesignerRuntime.getJSContext().addDesignTimeClassLoader(registeredDesignTimeClassLoader);
		
	}

	private void removeDynamicClassLoader() {

		if (registeredDesignTimeClassLoader != null) {

			Iterator<FBSType.DesignTimeClassLoader> it = DesignerRuntime
					.getJSContext().getDesignTimeClassLoaders();

			while (it.hasNext()) {

				FBSType.DesignTimeClassLoader dtcl = it.next();

				if (dtcl.equals(registeredDesignTimeClassLoader)) {
					it.remove();
					registeredDesignTimeClassLoader = null;
				}

			}

		}
		
	}

	@Override
	public Object getProperty(String name) {

		BaristaUtil.BARISTA_LOG.traceDebug("Asking for " + name);

		if ("binding".equals(name)) {
			return "com.ibm.designer.domino.scripting.el";
		}

		if ("jstype".equals(name)) {

			FBSType jstype = null;

			if (logger.isTraceDebugEnabled()) {
				logger.traceDebug("Checking jsType for {0}", getName());
			}
			
			if (StringUtil.isNotEmpty(className)) {

				registerDynamicClassLoader();

				jstype = FBSType.getType(DesignerRuntime.getJSContext(),
						className);
				
				removeDynamicClassLoader();
				
			} else {
				jstype = FBSType.undefinedType;
			}
			
			if (logger.isTraceDebugEnabled()) {
				logger.traceDebug("jsType for '{0}' is '{1}'", jstype.getDescString());
			}

			return jstype;

		}

		if ("methods".equals(name)) {

			if (logger.isTraceDebugEnabled()) {
				logger.traceDebug("Asking for 'methods' of {0}", getName());
			}
			

			@SuppressWarnings("rawtypes")
			HashMap map = new HashMap();
			return map;

		}

		return super.getProperty(name);
	}

}
