package org.openntf.barista.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

public class ManagedBeanRuleSet extends RuleSetBase {

	@Override
	public void addRuleInstances(Digester digester) {
		
		digester.addRule("faces-config", new FacesConfigRule());
		
		digester.addRule(
		        "faces-config/managed-bean", new ManagedBeanRule());
		      digester.addCallMethod(
		        "faces-config/managed-bean/managed-bean-class", 
		        "setManagedBeanClass", 0);
		      digester.addCallMethod(
		        "faces-config/managed-bean/managed-bean-name", 
		        "setManagedBeanName", 0);
		      digester.addCallMethod(
		        "faces-config/managed-bean/managed-bean-scope", 
		        "setManagedBeanScope", 0);
		
	}

}
