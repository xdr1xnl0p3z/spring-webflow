package org.springframework.webflow.expression.el;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

/**
 * Custom EL resolver that resolves to the Spring Security Principal object for binding expressions prefixed with
 * {@link #SECURITY_PRINCIPAL_VARIABLE_NAME}. For instance "#{currentUser}".
 * 
 * @author Scott Andrews
 */
public class SpringSecurityELResolver extends ELResolver {

	/**
	 * Name of the security principal variable.
	 */
	public static final String SECURITY_PRINCIPAL_VARIABLE_NAME = "currentUser";

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {
		if (base == null && SECURITY_PRINCIPAL_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return Authentication.class;
		} else {
			return null;
		}
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null && SECURITY_PRINCIPAL_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return SecurityContextHolder.getContext().getAuthentication();
		} else {
			return null;
		}
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		if (base == null && SECURITY_PRINCIPAL_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return true;
		} else {
			return false;
		}
	}

	public void setValue(ELContext elContext, Object base, Object property, Object value) {
		if (base == null && SECURITY_PRINCIPAL_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			throw new PropertyNotWritableException("The " + SECURITY_PRINCIPAL_VARIABLE_NAME
					+ " cannot be set with an expression.");
		}
	}

}
