package org.openntf.barista;

import java.util.logging.Level;

import org.openntf.barista.util.BaristaUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BaristaActivator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		BaristaActivator.context = bundleContext;
		BaristaUtil.BARISTA_LOG.getLogger().setLevel(Level.INFO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		BaristaActivator.context = null;
	}

}
