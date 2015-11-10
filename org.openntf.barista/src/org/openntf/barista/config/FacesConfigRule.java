package org.openntf.barista.config;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import com.sun.faces.config.beans.FacesConfigBean;

public class FacesConfigRule extends Rule {

	private boolean pushed = false;

	@Override
	public void begin(String namespace, String name, Attributes attributes)
			throws Exception {
	
		try {
			if ((FacesConfigBean) this.digester.peek() == null) {
				this.pushed = true;
			}
		} catch (Exception localException) {
			this.pushed = true;
		}
		if (this.pushed) {
			@SuppressWarnings("rawtypes")
			Class clazz = this.digester.getClassLoader().loadClass(
					"com.sun.faces.config.beans.FacesConfigBean");
			Object instance = clazz.newInstance();
			this.digester.push(instance);
		}

	}

}
