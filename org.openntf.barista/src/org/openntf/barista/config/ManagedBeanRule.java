package org.openntf.barista.config;

import org.apache.commons.digester.Rule;
import org.openntf.barista.util.BaristaUtil;
import org.xml.sax.Attributes;

import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.ManagedBeanBean;

public class ManagedBeanRule extends Rule {

	private static LogMgr logger = BaristaUtil.BARISTA_LOG;

	@Override
	public void begin(String namespace, String name, Attributes attributes)
			throws Exception {

		try {
			Object o = this.digester.peek();

			if (!(o instanceof FacesConfigBean)) {
				throw new Exception("No parent FacesConfigBean on object stack");
			}

		} catch (Exception localException) {
			throw new IllegalStateException(
					"No parent FacesConfigBean on object stack");
		}

		Class<?> clazz = this.digester.getClassLoader().loadClass(
				"com.sun.faces.config.beans.ManagedBeanBean");
		ManagedBeanBean mbb = (ManagedBeanBean) clazz.newInstance();

		this.digester.push(mbb);
	}

	@Override
	public void end(String namespace, String name) throws Exception {

		ManagedBeanBean top = null;
		try {
			top = (ManagedBeanBean) this.digester.pop();
		} catch (Exception localException) {
			throw new IllegalStateException(
					"Popped object is not a com.sun.faces.config.beans.ManagedBeanBean instance");
		}

		validate(top);

		FacesConfigBean fcb = (FacesConfigBean) this.digester.peek();
		ManagedBeanBean old = fcb.getManagedBean(top.getManagedBeanName());

		if (old == null) {
			fcb.addManagedBean(top);
		} else {
			mergeManagedBean(top, old);
		}

	}

	static void mergeManagedBean(ManagedBeanBean top, ManagedBeanBean old) {

		if (logger.isTraceDebugEnabled()) {
			logger.traceDebug("Merging managed bean of name: {0}",
					top.getManagedBeanName());
		}

		if (top.getManagedBeanClass() != null) {
			old.setManagedBeanClass(top.getManagedBeanClass());
		}

		if (top.getManagedBeanScope() != null) {
			old.setManagedBeanScope(top.getManagedBeanScope());
		}

	}

	private void validate(ManagedBeanBean bean) {

		if (StringUtil.isEmpty(bean.getManagedBeanName()))
			throw new IllegalStateException("No Managed Bean Name");
		if (StringUtil.isEmpty(bean.getManagedBeanClass()))
			throw new IllegalStateException("No Managed Bean Class");
		if (StringUtil.isEmpty(bean.getManagedBeanScope()))
			throw new IllegalStateException("No Managed Bean Scope");

	}

}
