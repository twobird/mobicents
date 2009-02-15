/**
 * Start time:10:45:52 2009-02-09<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
package org.mobicents.slee.container.component.validator;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.slee.SLEEException;
import javax.slee.management.LibraryID;

import javassist.Modifier;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.ComponentRepository;
import org.mobicents.slee.container.component.ProfileSpecificationComponent;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.ProfileSpecificationDescriptorImpl;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.common.MEnvEntry;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.MCMPField;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.MCollator;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.MIndexHint;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MCompare;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MHasPrefix;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MLongestPrefixMatch;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MQuery;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MQueryExpression;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MQueryExpressionType;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MQueryParameter;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.profile.query.MRangeMatch;
import org.mobicents.slee.container.component.deployment.jaxb.slee11.profile.CmpField;

import com.sun.org.apache.xpath.internal.operations.Mod;

/**
 * Start time:10:45:52 2009-02-09<br>
 * Project: mobicents-jainslee-server-core<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">baranowb - Bartosz Baranowski
 *         </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class ProfileSpecificationComponentValidator implements Validator {

	private ComponentRepository repository = null;
	private ProfileSpecificationComponent component = null;
	private final static transient Logger logger = Logger
			.getLogger(ProfileSpecificationComponentValidator.class);
	// this does not include serializables
	private final static Set<String> _ALLOWED_CMPS_TYPES;
	static {
		Set<String> tmp = new HashSet<String>();
		tmp.add("int");
		tmp.add("boolean");
		tmp.add("byte");
		tmp.add("char");
		tmp.add("double");
		tmp.add("float");
		tmp.add("long");
		tmp.add("short");
		tmp.add(int[].class.toString());
		tmp.add(boolean[].class.toString());
		tmp.add(byte[].class.toString());
		tmp.add(char[].class.toString());
		tmp.add(double[].class.toString());
		tmp.add(float[].class.toString());
		tmp.add(long[].class.toString());
		tmp.add(short[].class.toString());
		_ALLOWED_CMPS_TYPES = Collections.unmodifiableSet(tmp);

	}

	private final static Set<String> _ALLOWED_MANAGEMENT_TYPES;
	static {
		Set<String> tmp = new HashSet<String>();
		// Section 10.17
		tmp.add("int");
		tmp.add("boolean");
		tmp.add("byte");
		tmp.add("char");
		tmp.add("double");
		tmp.add("float");
		tmp.add("long");
		tmp.add("short");
		tmp.add(int[].class.toString());
		tmp.add(boolean[].class.toString());
		tmp.add(byte[].class.toString());
		tmp.add(char[].class.toString());
		tmp.add(double[].class.toString());
		tmp.add(float[].class.toString());
		tmp.add(long[].class.toString());
		tmp.add(short[].class.toString());
		tmp.add(Integer.class.toString());
		tmp.add(Boolean.class.toString());
		tmp.add(Byte.class.toString());
		tmp.add(Character.class.toString());
		tmp.add(Double.class.toString());
		tmp.add(Float.class.toString());
		tmp.add(Long.class.toString());
		tmp.add(Short.class.toString());
		tmp.add(Integer[].class.toString());
		tmp.add(Boolean[].class.toString());
		tmp.add(Byte[].class.toString());
		tmp.add(Character[].class.toString());
		tmp.add(Double[].class.toString());
		tmp.add(Float[].class.toString());
		tmp.add(Long[].class.toString());
		tmp.add(Short[].class.toString());

		tmp.add(Serializable.class.toString());
		tmp.add(Serializable[].class.toString());
		// tmp.add(String[].class.toString());
		// tmp.add(String.class.toString());
		_ALLOWED_MANAGEMENT_TYPES = Collections.unmodifiableSet(tmp);

	}

	private final static Set<String> _FORBIDEN_METHODS;
	static {
		// section 10.18 in SLEE 1.1
		Set<String> _tmp = new HashSet<String>();
		Set<String> ignore = new HashSet<String>();
		ignore.add("java.lang.Object");
		Map<String, Method> tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.slee.profile.ProfileManagement.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());
		tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.slee.profile.ProfileManagement.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());
		tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.slee.profile.ProfileMBean.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());
		tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.slee.profile.Profile.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());
		tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.management.DynamicMBean.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());
		tmpMethodsMap = ClassUtils.getAllInterfacesMethods(
				javax.management.MBeanRegistration.class, ignore);
		_tmp.addAll(tmpMethodsMap.keySet());

		_FORBIDEN_METHODS = Collections.unmodifiableSet(_tmp);

	}

	private final static Set<String> _ALLOWED_QUERY_PARAMETER_TYPES;
	static {
		Set<String> tmp = new HashSet<String>();
		// Section 10.17
		tmp.add("int");
		tmp.add("boolean");
		tmp.add("byte");
		tmp.add("char");
		tmp.add("double");
		tmp.add("float");
		tmp.add("long");
		tmp.add("short");
		tmp.add(Integer.class.getName());
		tmp.add(Boolean.class.getName());
		tmp.add(Byte.class.getName());
		tmp.add(Character.class.getName());
		tmp.add(Double.class.getName());
		tmp.add(Float.class.getName());
		tmp.add(Long.class.getName());
		tmp.add(Short.class.getName());
		tmp.add(String.class.getName());
		_ALLOWED_QUERY_PARAMETER_TYPES = Collections.unmodifiableSet(tmp);

	}

	private final static Set<String> _ENV_ENTRIES_TYPES;
	static {
		Set<String> tmp = new HashSet<String>();
		tmp.add(Integer.TYPE.getName());
		tmp.add(Boolean.TYPE.getName());
		tmp.add(Byte.TYPE.getName());
		tmp.add(Character.TYPE.getName());
		tmp.add(Double.TYPE.getName());
		tmp.add(Float.TYPE.getName());
		tmp.add(Long.TYPE.getName());
		tmp.add(Short.TYPE.getName());
		tmp.add(String.class.getName());
		_ENV_ENTRIES_TYPES = Collections.unmodifiableSet(tmp);

	}

	private boolean requriedProfileAbstractClass = false;

	public void setComponentRepository(ComponentRepository repository) {
		this.repository = repository;

	}

	public ProfileSpecificationComponent getComponent() {
		return component;
	}

	public void setComponent(ProfileSpecificationComponent component) {
		this.component = component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.container.component.validator.Validator#validate()
	 */
	public boolean validate() {

		boolean passed = true;

		try {

			if (!validateDescriptor()) {
				// this is quick fail
				passed = false;
				return passed;
			}
			// we cant validate some parts on fail here

			if (!validateCMPInterface()) {
				passed = false;

			} else {
				if (!validatePorfileTableInterface()) {
					passed = false;
				}

			}

			if (!validateProfileLocalInterface()) {
				passed = false;
			}

			if (!validateProfileManagementInterface()) {
				passed = false;
			}

			if (!validateAbstractClass()) {
				passed = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return passed;

	}

	boolean validateCMPInterface() {

		// this is kind of quick failure for each method/field, since when
		// something goes wrong for one method we can validate it further... -
		// CMPs in sbbs are different since we know their names.
		boolean passed = true;
		String errorBuffer = new String("");

		try {
			// this is obligatory
			Class interfaceClass = this.component.getProfileCmpInterfaceClass();

			if (!interfaceClass.isInterface()) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface class is not an interface. ",
						"10.6", errorBuffer);

				return passed;
			}

			if (this.component.isSlee11()
					&& interfaceClass.getPackage() == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface in SLEE 1.1 components must be defined in package. ",
						"10.6", errorBuffer);
			}

			Set<String> ignore = new HashSet<String>();
			ignore.add("java.lang.Object");

			Map<String, Method> interfaceMethods = ClassUtils
					.getAllInterfacesMethods(interfaceClass, ignore);

			// holds fieldName->occurances - EACH field MUST occure twice, as
			// setter and getter
			Map<String, Integer> fieldOccurances = new HashMap<String, Integer>();
			// holds fieldName->type mapp, setter and getter type must match
			Map<String, Class> fieldToType = new HashMap<String, Class>();

			Iterator<Entry<String, Method>> methodsIterator = interfaceMethods
					.entrySet().iterator();

			while (methodsIterator.hasNext()) {
				Entry<String, Method> entry = methodsIterator.next();
				Method m = entry.getValue();
				String methodName = m.getName();
				if (_FORBIDEN_METHODS.contains(entry.getKey())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface defines methods that are prohibited.: "
									+ methodName, "10.17", errorBuffer);
					methodsIterator.remove();
					continue;
				}
				if (!(methodName.startsWith("get") || methodName
						.startsWith("set"))) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface methods must follow java bean setter/getter accessors. Offending method: "
									+ methodName, "10.6", errorBuffer);
					methodsIterator.remove();
					continue;
				}

				Class fieldType = null;
				String cmpFieldName = null;
				if (methodName.startsWith("get")) {
					if (!validateCMPInterfaceGetter(m)) {
						passed = false;
						methodsIterator.remove();
						continue;
					}
					fieldType = m.getReturnType();
					cmpFieldName = methodName.replaceFirst("get", "");
				} else {
					if (!validateCMPInterfaceSetter(m)) {
						passed = false;
						methodsIterator.remove();
						continue;
					}
					fieldType = m.getParameterTypes()[0];
					cmpFieldName = methodName.replaceFirst("set", "");
				}

				if (!(_ALLOWED_CMPS_TYPES.contains(fieldType.toString()) || validateSerializableType(
						fieldType, methodName))) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface field has wrong type, only java primitives, serializables and arrays of those types are allowed. Offending field: "
									+ cmpFieldName, "10.6", errorBuffer);

				}

				Character c = cmpFieldName.charAt(0);
				if (!Character.isUpperCase(c)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface field has wrong type, of first char in name. Offending field: "
									+ cmpFieldName, "10.6", errorBuffer);

					// FIXME: should we fail here
					methodsIterator.remove();
					continue;
				}

				cmpFieldName = cmpFieldName.replaceFirst(c + "", Character
						.toLowerCase(c)
						+ "");

				// XXX: this will fail even for duplicate delcarations of
				// fields, but meesages might be missleading.
				if (!fieldOccurances.containsKey(cmpFieldName)) {
					fieldOccurances.put(cmpFieldName, new Integer(1));
					fieldToType.put(cmpFieldName, fieldType);

				} else if (fieldOccurances.get(cmpFieldName) == 1) {
					fieldOccurances.put(cmpFieldName, new Integer(2));

					if (fieldToType.get(cmpFieldName).getName().compareTo(
							fieldType.getName()) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile cmp interface field has wrong type, current type does not match previously encountered. Field: "
										+ cmpFieldName, "10.6", errorBuffer);
					}
				} else {
					// it should be one
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface field has been defined more than once, offending method: "
									+ methodName, "10.6", errorBuffer);

					// FIXME: should we fail here
					methodsIterator.remove();
					continue;
				}

			}

			// here we have to check if we are 1.1 - declaredcmp fields vs
			// occurances + collators, those can be present only for String type
			// fields
			if (this.component.isSlee11()) {

				// if we are here we know there are no dups
				List<MCMPField> cmpFields = this.component.getDescriptor()
						.getProfileCMPInterface().getCmpFields();

				if (cmpFields.size() != fieldToType.size()) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface cmp field declarations do not match declaredfields in descriptor.",
							"10.6", errorBuffer);

					// FIXME: should we fail here
					methodsIterator.remove();
				}

				for (MCMPField f : cmpFields) {
					Class type = fieldToType.get(f.getCmpFieldName());

					// might be null in case of above errror
					if (type != null) {

						if (f.getUniqueCollatorRef() != null
								&& type.getName().compareTo("java.lang.String") != 0) {
							// only stirng fields can have it
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile cmp field declares collator ref, but field type is not java.lang.String. Cmpfield: "
											+ f.getCmpFieldName(), "10.6",
									errorBuffer);
						}

						for (MIndexHint indexHint : f.getIndexHints()) {
							if (indexHint.getCollatorRef() != null
									&& type.getName().compareTo(
											"java.lang.String") != 0) {
								// only stirng fields can have it
								passed = false;
								errorBuffer = appendToBuffer(
										"Profile specification profile cmp field decalres index hint with collator ref, but field type is not java.lang.String. Cmpfield: "
												+ f.getCmpFieldName(), "10.6",
										errorBuffer);
							}
						}
					}
				}
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;

	}

	boolean validateSerializableType(Class fieldType, String method) {

		boolean passed = true;
		String errorBuffer = new String("");

		try {

			// in case of array we have from getName() something like:
			// [Ljava.io.Serializable;
			if (fieldType.isArray()) {
				// ech
				String typStringName = fieldType.getComponentType().getName();
				try {
					Class type = fieldType.forName(typStringName);
					if (ClassUtils.checkInterfaces(fieldType,
							"java.io.Serializable") == null) {

						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile cmp interface allows to store primitive types and serializables, offending method: "
										+ method + " . Type: " + fieldType,
								"10.6", errorBuffer);

					}
				} catch (Exception e) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface allows to store primitive types and serializables, offending method: "
									+ method + " . Type: " + fieldType, "10.6",
							errorBuffer);
				}

			} else {
				if (ClassUtils.checkInterfaces(fieldType,
						"java.io.Serializable") == null) {

					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile cmp interface allows to store primitive types and serializables, offending method: "
									+ method + " . Type: " + fieldType, "10.6",
							errorBuffer);

				}
			}
		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validateCMPInterfaceSetter(Method m) {
		boolean passed = true;
		String errorBuffer = new String("");

		try {

			// no throws
			if (m.getExceptionTypes().length > 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface setter method must not defined throws clause, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}

			int modifiers = m.getModifiers();

			// its interface method so it has to be public and abstract ONLY!!!!
			// if (Modifier.isStatic(modifiers)) {
			// passed = false;
			// errorBuffer = appendToBuffer(
			// "Profile specification profile cmp interface setter method must not be static, offending method: "
			// + m.getName(), "10.6", errorBuffer);
			// }
			//
			// if (!Modifier.isAbstract(modifiers)) {
			// passed = false;
			// errorBuffer = appendToBuffer(
			// "Profile specification profile cmp interface setter method must be abstract, offending method: "
			// + m.getName(), "10.6", errorBuffer);
			// }

			if (m.getParameterTypes().length != 1) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface setter method must have exactly one parameter, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}

			if (m.getReturnType().getName().compareTo("void") != 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface setter method must not declare return type, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;

	}

	boolean validateCMPInterfaceGetter(Method m) {
		boolean passed = true;
		String errorBuffer = new String("");

		try {

			// no throws
			// no throws
			if (m.getExceptionTypes().length > 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface getter method must not defined throws clause, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}

			int modifiers = m.getModifiers();

			// its interface method so it has to be public and abstract ONLY!!!!
			// if (Modifier.isStatic(modifiers)) {
			// passed = false;
			// errorBuffer = appendToBuffer(
			// "Profile specification profile cmp interface setter method must not be static, offending method: "
			// + m.getName(), "10.6", errorBuffer);
			// }
			//
			// if (!Modifier.isAbstract(modifiers)) {
			// passed = false;
			// errorBuffer = appendToBuffer(
			// "Profile specification profile cmp interface setter method must be abstract, offending method: "
			// + m.getName(), "10.6", errorBuffer);
			// }

			if (m.getParameterTypes().length > 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface getter method must have no parameters, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}

			if (m.getReturnType().getName().compareTo("void") == 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile cmp interface getter method must declare return type, offending method: "
								+ m.getName(), "10.6", errorBuffer);
			}
		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;

	}

	/**
	 * Should not be called when CMP interface validation fails cause it depends
	 * on result of it
	 * 
	 * @return
	 */
	boolean validateProfileManagementInterface() {
		// this is optional
		boolean passed = true;
		String errorBuffer = new String("");

		if (this.component.getProfileManagementInterfaceClass() == null) {
			// it can hapen when its not present
			return passed;
		}

		try {
			Class profileManagementInterfaceClass = this.component
					.getProfileManagementInterfaceClass();

			if (!profileManagementInterfaceClass.isInterface()) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile management interface is not an interface class!!!",
						"10.10", errorBuffer);
				return passed;

			}

			if (this.component.isSlee11()
					&& profileManagementInterfaceClass.getPackage() == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile management interface must be declared within named package.",
						"10.10", errorBuffer);
			}

			if (!Modifier.isPublic(profileManagementInterfaceClass
					.getModifiers())) {

				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile management interface must be declaredas public.",
						"10.10", errorBuffer);

			}

			// now here comes the fun. methods are subject to restrictions form
			// 10.17 and 10.18, BUT interface may implement or just define CMPs
			// that MUST reasemlbe CMPs definition from CMP interface - that is
			// all getter/setter methods defined
			Set<String> ignore = new HashSet<String>();
			ignore.add("java.lang.Object");
			Map<String, Method> cmpInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(this.component
							.getProfileCmpInterfaceClass(), ignore);
			Map<String, Method> managementInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(profileManagementInterfaceClass,
							ignore);

			// we cant simply remove all methods from
			// managementInterfaceMethods.removeAll(cmpInterfaceMethods) since
			// with abstract classes it becomes comp;licated
			// we can have doubling definition with for instance return type or
			// throws clause (as diff) which until concrete class wont be
			// noticed - and is an error....
			// FIXME: does mgmt interface have to define both CMP accessors?
			Iterator<Entry<String, Method>> entryIterator = managementInterfaceMethods
					.entrySet().iterator();
			while (entryIterator.hasNext()) {

				Entry<String, Method> entry = entryIterator.next();
				String key = entry.getKey();

				if (cmpInterfaceMethods.containsKey(key)) {

					// FIXME: possibly we shoudl iterate over names?

					if (!compareMethod(entry.getValue(), cmpInterfaceMethods
							.get(key))) {
						// return type or throws clause, or modifiers differ
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile management interface declares method which has signature simlar to CMP method, but it has different throws clause, return type or modifiers, which is wrong.",
								"10.10", errorBuffer);

					}
				} else {
					// we can have setter/getter like as stand alone ?
					if (_FORBIDEN_METHODS.contains(key)) {
						// this is forrbiden, section 10.18
						errorBuffer = appendToBuffer(
								"Profile specification profile management interface declares method from forbiden list, method: "
										+ entry.getKey(), "10.18", errorBuffer);

						continue;
					}
					// is this the right place? This tells validator
					// wheather it should require profile abstract class in
					// case of 1.1
					requriedProfileAbstractClass = true;
					// we know that name is ok.
					// FIXME: SPECS Are weird - Management methods may not
					// have the same name and arguments as a Profile CMP
					// field get or set accessor method. <---- ITS CMP
					// METHOD< SIGNATURE IS NAME AND
					// PARAMETERS and its implemented if its doubled from
					// CMP or this interface extends CMP
					// interface.....!!!!!!!!!!!!!!!!!!

					if (key.startsWith("ejb")) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile management interface declares method with wrong prefix, method: "
										+ entry.getKey(), "10.18", errorBuffer);

						continue;
					}

					// there are no reqs other than parameters?
					Class[] params = entry.getValue().getParameterTypes();
					for (int index = 0; index < params.length; index++) {
						if (_ALLOWED_MANAGEMENT_TYPES.contains(params[index]
								.toString())) {

						} else {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile management interface declares management method with wrong parameter at index["
											+ index
											+ "], method: "
											+ entry.getKey(), "10.18",
									errorBuffer);
						}
					}

				}
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validateProfileLocalInterface() {

		boolean passed = true;
		String errorBuffer = new String("");

		// FIXME: should not this return generic?
		if (!this.component.isSlee11()
				|| this.component.getProfileLocalInterfaceClass() == null) {
			// its nto mandatory
			return passed;
		}

		try {

			Class profileLocalObjectInterface = this.component
					.getProfileLocalInterfaceClass();

			if (!profileLocalObjectInterface.isInterface()) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile local interface is not an interface class!!!",
						"10.7.4", errorBuffer);
				return passed;

			}

			if (profileLocalObjectInterface.getPackage() == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile local interface must be declared within named package.",
						"10.7.4", errorBuffer);
			}

			if (!Modifier.isPublic(profileLocalObjectInterface.getModifiers())) {

				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile local interface must be declared as public.",
						"10.7.4", errorBuffer);

			}

			Class genericProfleLocalObject = ClassUtils.checkInterfaces(
					profileLocalObjectInterface,
					"javax.slee.profile.ProfileLocalObject");
			if (genericProfleLocalObject == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile local interface must implement javax.slee.profile.ProfileLocalObject.",
						"10.7.4", errorBuffer);
			}
			// now here comes the fun. methods are subject to restrictions form
			// 10.17 and 10.18, BUT interface may implement or just define CMPs
			// that MUST reasemlbe CMPs definition from CMP interface - that is
			// all getter/setter methods defined
			Set<String> ignore = new HashSet<String>();
			ignore.add("java.lang.Object");
			ignore.add("javax.slee.profile.ProfileLocalObject");
			Map<String, Method> cmpInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(this.component
							.getProfileCmpInterfaceClass(), ignore);
			Map<String, Method> managementInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(profileLocalObjectInterface,
							ignore);

			// we cant simply remove all methods from
			// managementInterfaceMethods.removeAll(cmpInterfaceMethods) since
			// with abstract classes it becomes comp;licated
			// we can have doubling definition with for instance return type or
			// throws clause (as diff) which until concrete class wont be
			// noticed - and is an error....
			// FIXME: does mgmt interface have to define both CMP accessors?
			Iterator<Entry<String, Method>> entryIterator = managementInterfaceMethods
					.entrySet().iterator();

			while (entryIterator.hasNext()) {

				Entry<String, Method> entry = entryIterator.next();
				String key = entry.getKey();

				if (cmpInterfaceMethods.containsKey(key)) {
					if (!compareMethod(entry.getValue(), cmpInterfaceMethods
							.get(key))) {
						// return type or throws clause, or modifiers differ
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile local interface declares method which has signature similar to CMP method, but it has different throws clause, return type or modifiers, which is wrong.",
								"10.7.4", errorBuffer);

					}
				} else {

					// we can have setter/getter like as stand alone ?
					if (_FORBIDEN_METHODS.contains(key)) {
						// this is forrbiden, section 10.18
						errorBuffer = appendToBuffer(
								"Profile specification profile local interface declares method from forbiden list, method: "
										+ entry.getKey(), "10.18", errorBuffer);

						continue;
					} else if (key.startsWith("get") || key.startsWith("set")) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile local interface declares method which is setter/getter and does not match CMP interface method, method: "
										+ entry.getKey(), "10.18", errorBuffer);

						continue;
					}

					// is this the right place? This tells validator
					// wheather it should require profile abstract class in
					// case of 1.1
					requriedProfileAbstractClass = true;

					// we know that name is ok.
					// FIXME: SPECS Are weird - Management methods may not
					// have the same name and arguments as a Profile CMP
					// field get or set accessor method. <---- ITS CMP
					// METHOD< SIGNATURE IS NAME AND
					// PARAMETERS and its implemented if its doubled from
					// CMP or this interface extends CMP
					// interface.....!!!!!!!!!!!!!!!!!!
					if (key.startsWith("ejb")) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile local interface declares method with wrong prefix, method: "
										+ entry.getKey(), "10.18", errorBuffer);

						continue;
					}

					Class[] params = entry.getValue().getParameterTypes();
					for (int index = 0; index < params.length; index++) {
						// FIXME: whichc should we use here?
						if ((_ALLOWED_CMPS_TYPES.contains(params[index]
								.toString()) || validateSerializableType(
								params[index], key))) {

						} else {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile management interface declares management method with wrong parameter at index["
											+ index
											+ "], method: "
											+ entry.getKey(), "10.18",
									errorBuffer);
						}
					}

					// lets check exceptions, we can define all except
					// java.rmi.RemoteException
					for (Class exceptionClass : entry.getValue()
							.getExceptionTypes()) {
						// FIXME: should we check unckecked exceptions here
						// ?
						if (ClassUtils.checkClasses(exceptionClass,
								"java.rmi.RemoteException") != null) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile management interface declares management method with wrong exception in throws clause, it can not throw java.rmi.RemoteException(or its sub classes), method: "
											+ entry.getKey(), "10.7.4",
									errorBuffer);
						}

					}
				}
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	/**
	 * shoudl not be run if other interfaces vaildation fails.
	 * 
	 * @return
	 */
	boolean validateAbstractClass() {
		boolean passed = true;
		String errorBuffer = new String("");

		try {

			if (this.component.getDescriptor().getProfileAbstractClass() == null) {

				if (this.requriedProfileAbstractClass) {
					errorBuffer = appendToBuffer(
							"Profile specification profile management abstract class must be present",
							"3.X", errorBuffer);
					return passed;
				}

			} else {

				if (this.component.getProfileAbstractClass() == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile management abstract class has not been loaded",
							"3.X", errorBuffer);
					return passed;
				}

			}

			Class profileAbstractClass = this.component
					.getProfileAbstractClass();

			// if (profileAbstractClass.isInterface()
			// || profileAbstractClass.isEnum()) {
			// passed = false;
			// errorBuffer = appendToBuffer(
			// "Profile specification profile abstract class in not a clas.",
			// "10.11", errorBuffer);
			// return passed;
			// }

			if (this.component.isSlee11()) {

				if (profileAbstractClass.getPackage() == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must be defined in package.",
							"10.11", errorBuffer);

				}

				// FIXME: what about 1.0 ?
				// public, no arg constructor without throws clause
				Constructor c = null;
				try {
					c = profileAbstractClass.getConstructor(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}

				if (c == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must define public no arg constructor.",
							"10.11", errorBuffer);
				} else {
					if (!Modifier.isPublic(c.getModifiers())) {

						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must define public no arg constructor.",
								"10.11", errorBuffer);

					}

					if (c.getExceptionTypes().length > 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must define public no arg constructor without throws clause.",
								"10.11", errorBuffer);
					}
				}

			}

			int modifiers = profileAbstractClass.getModifiers();

			if (!Modifier.isAbstract(modifiers)) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile abstract class must be defined abstract.",
						"10.11", errorBuffer);
			}

			if (!Modifier.isPublic(modifiers)) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile abstract class must be defined public.",
						"10.11", errorBuffer);
			}

			// in case of 1.0 it has to implement as concrete methods from
			// javax.slee.profile.ProfileManagement - section 10.8 of 1.0 specs
			Map<String, Method> requiredLifeCycleMethods = null;
			Set<String> ignore = new HashSet<String>();
			ignore.add("java.lang.Object");
			if (this.component.isSlee11()) {
				Class javaxSleeProfileProfileClass = ClassUtils
						.checkInterfaces(profileAbstractClass,
								"javax.slee.profile.Profile");
				if (javaxSleeProfileProfileClass == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement javax.slee.profile.Profile.",
							"10.11", errorBuffer);

					requiredLifeCycleMethods = ClassUtils
							.getAllInterfacesMethods(
									javax.slee.profile.ProfileLocalObject.class,
									ignore);
				} else {
					requiredLifeCycleMethods = ClassUtils
							.getAllInterfacesMethods(
									javaxSleeProfileProfileClass, ignore);
				}
			} else {
				Class javaxSleeProfileProfileManagement = ClassUtils
						.checkInterfaces(profileAbstractClass,
								"javax.slee.profile.ProfileManagement");
				if (javaxSleeProfileProfileManagement == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement javax.slee.profile.ProfileManagement.",
							"10.8", errorBuffer);
					requiredLifeCycleMethods = ClassUtils
							.getAllInterfacesMethods(
									javax.slee.profile.ProfileManagement.class,
									ignore);
				} else {
					requiredLifeCycleMethods = ClassUtils
							.getAllInterfacesMethods(
									javaxSleeProfileProfileManagement, ignore);
				}
			}

			Map<String, Method> abstractMethods = ClassUtils
					.getAbstractMethodsFromClass(profileAbstractClass);
			Map<String, Method> abstractMethodsFromSuperClasses = ClassUtils
					.getAbstractMethodsFromSuperClasses(profileAbstractClass);

			Map<String, Method> concreteMethods = ClassUtils
					.getConcreteMethodsFromClass(profileAbstractClass);
			Map<String, Method> concreteMethodsFromSuperClasses = ClassUtils
					.getConcreteMethodsFromSuperClasses(profileAbstractClass);

			for (Entry<String, Method> entry : requiredLifeCycleMethods
					.entrySet()) {

				Method m = entry.getValue();
				//
				Method methodFromClass = ClassUtils.getMethodFromMap(m
						.getName(), m.getParameterTypes(), concreteMethods,
						concreteMethodsFromSuperClasses);

				if (methodFromClass == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method not found in concrete(non private) methods: "
									+ m.getName(), "10.11", errorBuffer);
					continue;
				}

				// it concrete - must check return type
				if (m.getReturnType().getName().compareTo(
						methodFromClass.getReturnType().getName()) != 0) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method with name: "
									+ m.getName()
									+ " found in concrete(non private) methods has different return type: "
									+ methodFromClass.getReturnType()
									+ ", than one declared in interface: "
									+ m.getReturnType(), "10.11", errorBuffer);
				}

				if (!Arrays.equals(m.getExceptionTypes(), methodFromClass
						.getExceptionTypes())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method with name: "
									+ m.getName()
									+ " found in concrete(non private) methods has different throws clause than one found in class.",
							"10.11", errorBuffer);
				}

				// must be public, not abstract, not final, not static
				modifiers = methodFromClass.getModifiers();
				if (!Modifier.isPublic(modifiers)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method with name: "
									+ m.getName()
									+ " found in concrete(non private) methods must be public.",
							"10.11", errorBuffer);
				}
				if (Modifier.isStatic(modifiers)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method with name: "
									+ m.getName()
									+ " found in concrete(non private) methods must not be static.",
							"10.11", errorBuffer);
				}
				if (Modifier.isFinal(modifiers)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile abstract class must implement certain lifecycle methods. Method with name: "
									+ m.getName()
									+ " found in concrete(non private) methods must not be final.",
							"10.11", errorBuffer);
				}

				// FIXME: native?

			}

			// in 1.1 and 1.0 it must implement CMP interfaces, but methods
			// defined there MUST stay abstract
			Class profileCMPInterface = ClassUtils.checkInterfaces(
					profileAbstractClass, this.component
							.getProfileCmpInterfaceClass().getName());

			if (profileCMPInterface == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile abstract class must implement profile CMP interface.",
						"10.11", errorBuffer);
				return passed;
			}
			// abstract class implements CMP Interface, but leaves all methods
			// as abstract

			Map<String, Method> cmpInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(profileCMPInterface, ignore);

			if (profileCMPInterface == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile abstract class must implement defined profile CMP interface.",
						"10.11", errorBuffer);
			} else {

				for (Entry<String, Method> entry : cmpInterfaceMethods
						.entrySet()) {

					Method m = entry.getValue();
					//
					Method methodFromClass = ClassUtils.getMethodFromMap(m
							.getName(), m.getParameterTypes(), concreteMethods,
							concreteMethodsFromSuperClasses);

					if (methodFromClass != null) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must leave CMP interface methods as abstract, it can not be concrete: "
										+ m.getName(), "10.11", errorBuffer);
						continue;
					}

					methodFromClass = ClassUtils.getMethodFromMap(m.getName(),
							m.getParameterTypes(), abstractMethods,
							abstractMethodsFromSuperClasses);

					// it concrete - must check return type
					if (m.getReturnType().getName().compareTo(
							methodFromClass.getReturnType().getName()) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must not decalre methods from CMP interface with different return type. Method with name: "
										+ m.getName()
										+ " found in (non private) class methods has different return type: "
										+ methodFromClass.getReturnType()
										+ ", than one declared in interface: "
										+ m.getReturnType(), "10.11",
								errorBuffer);
					}

					if (!Arrays.equals(m.getExceptionTypes(), methodFromClass
							.getExceptionTypes())) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must not change throws clause. Method with name: "
										+ m.getName()
										+ " found in (non private) class methods has different throws clause than one found in class.",
								"10.11", errorBuffer);
					}

					// FIXME: should we do that?
					abstractMethods.remove(entry.getKey());
					abstractMethodsFromSuperClasses.remove(entry.getKey());
				}

			}

			// those checks are......
			// 1.0 and 1.1 if we define management interface we have to
			// implement it, and all methods that are not CMPs
			if (this.component.getDescriptor().getProfileManagementInterface() != null) {
				Class profileManagementInterfaceClass = this.component
						.getProfileManagementInterfaceClass();
				Map<String, Method> profileManagementInterfaceMethods = ClassUtils
						.getAllInterfacesMethods(
								profileManagementInterfaceClass, ignore);
				// methods except those defined in CMP interface must be
				// concrete

				for (Entry<String, Method> entry : profileManagementInterfaceMethods
						.entrySet()) {

					Method m = entry.getValue();

					// CMP methods must stay abstract
					// check if this method is the same as in CMP interface is
					// done elsewhere
					// that check shoudl be ok to run this one!!! XXX
					if (cmpInterfaceMethods.containsKey(entry.getKey())) {
						// we do nothing, cmp interface is validate above

					} else {
						// 10.8/10.11

						Method concreteMethodFromAbstractClass = ClassUtils
								.getMethodFromMap(m.getName(), m
										.getParameterTypes(), concreteMethods,
										concreteMethodsFromSuperClasses);
						if (concreteMethodFromAbstractClass == null) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile abstract class must implement as non private methods from profile management interface other than CMP methods",
									"10.11", errorBuffer);
							continue;
						}

						int concreteMethodModifiers = concreteMethodFromAbstractClass
								.getModifiers();
						// public, and cannot be static,abstract, or final.
						if (!Modifier.isPublic(concreteMethodModifiers)) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile abstract class must implement methods from profile management interface as public, offending method: "
											+ concreteMethodFromAbstractClass
													.getName(), "10.11",
									errorBuffer);
						}

						if (Modifier.isStatic(concreteMethodModifiers)) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile abstract class must implement methods from profile management interface as not static, offending method: "
											+ concreteMethodFromAbstractClass
													.getName(), "10.11",
									errorBuffer);
						}

						if (Modifier.isFinal(concreteMethodModifiers)) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification profile abstract class must implement methods from profile management interface as not final, offending method: "
											+ concreteMethodFromAbstractClass
													.getName(), "10.11",
									errorBuffer);
						}
					}

				}

			}

			if (this.component.isSlee11()) {
				// ProfileLocalObject and UsageInterface are domains of 1.1
				// uff, ProfileLocal again that stupid check cross two
				// interfaces and one abstract class.....

				if (this.component.getDescriptor().getProfileLocalInterface() != null) {

					// abstract class MUST NOT implement it
					if (ClassUtils.checkInterfaces(profileAbstractClass,
							this.component.getDescriptor()
									.getProfileLocalInterface()
									.getProfileLocalInterfaceName()) != null
							|| ClassUtils.checkInterfaces(profileAbstractClass,
									"javax.slee.profile.ProfileLocalObject") != null) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile abstract class must not implement profile local interface in any way(only methods must be implemented)",
								"10.11", errorBuffer);
					}

					Class profileLocalObjectClass = this.component
							.getProfileLocalInterfaceClass();
					ignore.add("javax.slee.profile.ProfileLocalObject");
					Map<String, Method> profileLocalObjectInterfaceMethods = ClassUtils
							.getAllInterfacesMethods(profileLocalObjectClass,
									ignore);
					ignore.remove("javax.slee.profile.ProfileLocalObject");
					// methods except those defined in CMP interface must be
					// concrete

					for (Entry<String, Method> entry : profileLocalObjectInterfaceMethods
							.entrySet()) {

						Method m = entry.getValue();

						// CMP methods must stay abstract
						// check if this method is the same as in CMP interface
						// is done elsewhere
						// that check shoudl be ok to run this one!!! XXX
						if (cmpInterfaceMethods.containsKey(entry.getKey())) {
							// we do nothing, cmp interface is validate above

						} else {
							// 10.8/10.11
							Method concreteMethodFromAbstractClass = ClassUtils
									.getMethodFromMap(m.getName(), m
											.getParameterTypes(),
											concreteMethods,
											concreteMethodsFromSuperClasses);
							if (concreteMethodFromAbstractClass == null) {
								passed = false;
								errorBuffer = appendToBuffer(
										"Profile specification profile abstract class must implement as non private methods from profile local interface other than CMP methods",
										"10.11", errorBuffer);
								continue;
							}

							int concreteMethodModifiers = concreteMethodFromAbstractClass
									.getModifiers();
							// public, and cannot be static,abstract, or final.
							if (!Modifier.isPublic(concreteMethodModifiers)) {
								passed = false;
								errorBuffer = appendToBuffer(
										"Profile specification profile abstract class must implement methods from profile local interface as public, offending method: "
												+ concreteMethodFromAbstractClass
														.getName(), "10.11",
										errorBuffer);
							}

							if (Modifier.isStatic(concreteMethodModifiers)) {
								passed = false;
								errorBuffer = appendToBuffer(
										"Profile specification profile abstract class must implement methods from profile local interface as not static, offending method: "
												+ concreteMethodFromAbstractClass
														.getName(), "10.11",
										errorBuffer);
							}

							if (Modifier.isFinal(concreteMethodModifiers)) {
								passed = false;
								errorBuffer = appendToBuffer(
										"Profile specification profile abstract class must implement methods from profile management interface as not final, offending method: "
												+ concreteMethodFromAbstractClass
														.getName(), "10.11",
										errorBuffer);
							}
						}

					}
				}

				// usage parameters
				if (this.component.getDescriptor()
						.getProfileUsageParameterInterface() != null) {
					if (!validateProfileUsageInterface(abstractMethods,
							abstractMethodsFromSuperClasses)) {
						passed = false;
					}

				}

			}

			// FIXME: add check on abstract methods same as in SBB ?

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validateProfileUsageInterface(
			Map<String, Method> abstractClassMethods,
			Map<String, Method> abstractMethodsFromSuperClasses) {
		boolean passed = true;
		String errorBuffer = new String("");

		// FIXME: should not this return generic?
		if (!this.component.isSlee11()) {
			// its nto mandatory
			return passed;
		}

		try {
			if (this.component.getProfileUsageInterfaceClass() == null) {
				if (this.component.getDescriptor()
						.getProfileUsageParameterInterface() != null) {
					passed = false;

					errorBuffer = appendToBuffer(
							"Profile specification profile usage interface class is null, it should not be.",
							"10.X", errorBuffer);
					return passed;
				} else {
					return passed;
				}
			}

			// we only validate usage interface here
			if (!UsageInterfaceValidator
					.validateProfileSpecificationUsageParameterInterface(
							this.component, abstractClassMethods,
							abstractMethodsFromSuperClasses)) {
				passed = false;
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validatePorfileTableInterface() {

		boolean passed = true;
		String errorBuffer = new String("");

		// FIXME: should not this return generic?
		if (!this.component.isSlee11()
				|| this.component.getDescriptor().getProfileTableInterface() == null) {
			// its nto mandatory
			return passed;
		}

		try {

			Class profileTableInterface = this.component
					.getProfileTableInterfaceClass();

			// must be in pacakge
			if (profileTableInterface.getPackage() == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile table interface must be declared inside pacakge.",
						"10.8", errorBuffer);

			}

			if (!Modifier.isPublic(profileTableInterface.getModifiers())) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile table interface must be declared as public.",
						"10.8", errorBuffer);
			}

			Class genericProfileTableInterface = ClassUtils.checkInterfaces(
					profileTableInterface, "javax.slee.profile.ProfileTable");

			if (genericProfileTableInterface == null) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile table interface must extend in some way javax.slee.profile.ProfileTable.",
						"10.8", errorBuffer);
				// we fail here fast?
				return passed;
			}
			Set<String> ignore = new HashSet<String>();
			ignore.add("java.lang.Object");

			// There is no clause in specs saying custom can not override
			// methods, this will fail on concrete class generation though, let
			// filter
			Map<String, Method> javaxSleeProfileProfileTableMethods = ClassUtils
					.getAllInterfacesMethods(genericProfileTableInterface,
							ignore);

			ignore.add("javax.slee.profile.ProfileTable");

			Map<String, Method> profileTableInterfaceMethods = ClassUtils
					.getAllInterfacesMethods(profileTableInterface, ignore);

			// if we have common part here, this means that methods are double
			// declared, either exactly the same or with different return type
			// or exceptions
			Set<String> tmpKeySet = new HashSet<String>();
			Set<String> tmpKeySetToCompare = new HashSet<String>();

			tmpKeySet.addAll(javaxSleeProfileProfileTableMethods.keySet());
			tmpKeySetToCompare.addAll(profileTableInterfaceMethods.keySet());
			tmpKeySet.retainAll(tmpKeySetToCompare);
			if (tmpKeySet.size() != 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile table interface declares methods that double generic profile table interface, this may cause concrete class generation/instantion to fail.",
						"10.8", errorBuffer);
			}

			// else its query validation, here we have slightly different
			// approach, let iterate over methods, as we shoudl validate them
			// here we asume that xml constraints/conent is ok, - for intance
			// compare->parameter == query-parameter->name, parameter types have
			// been checked
			// here we just validate method against required ones
			// by now all queries are validated, no doubling
			Iterator<Entry<String, Method>> iterator = profileTableInterfaceMethods
					.entrySet().iterator();

			// FIXME: all queries have to match?
			Map<String, MQuery> nameToQueryMap = new HashMap<String, MQuery>();
			nameToQueryMap.putAll(this.component.getDescriptor()
					.getQueriesMap());

			Class cmpInterfaceClass = this.component
					.getProfileCmpInterfaceClass();

			while (iterator.hasNext()) {
				Entry<String, Method> entry = iterator.next();
				Method m = entry.getValue();
				String methodName = m.getName();

				if (!methodName.startsWith("query")) {
					passed = false;
					iterator.remove();
					errorBuffer = appendToBuffer(
							"Profile specification profile table interface declares method with wrong name: "
									+ methodName, "10.8.2", errorBuffer);
					continue;
				}

				String queryName = methodName.replace("query", "");
				queryName = queryName.replaceFirst(queryName.charAt(0) + "",
						Character.toLowerCase(queryName.charAt(0)) + "");

				System.err.println("x: " + queryName + " --> "
						+ nameToQueryMap.containsKey(queryName));

				if (!nameToQueryMap.containsKey(queryName)) {
					passed = false;
					iterator.remove();
					errorBuffer = appendToBuffer(
							"Profile specification profile table interface declares wrong method with name: "
									+ methodName
									+ ", it does not match any query defined in descriptor",
							"10.8.2", errorBuffer);
					continue;
				}

				MQuery query = nameToQueryMap.remove(queryName);

				// defined parameter types are ok in case of xml, we need to
				// check method
				Class returnType = m.getReturnType();

				if (returnType.getName().compareTo("java.util.Collection") != 0) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile table interface declares wrong return type: "
									+ returnType + " in method with name: "
									+ methodName
									+ ", it should be java.util.Collection.",
							"10.8.2", errorBuffer);
				}

				Class[] exceptions = m.getExceptionTypes();

				boolean foundTransactionRequiredLocalException = false;
				boolean foundSLEEException = false;
				for (Class c : exceptions) {
					if (c.getName().compareTo(
							"javax.slee.TransactionRequiredLocalException") == 0)
						foundTransactionRequiredLocalException = true;
					else if (c.getName().compareTo("javax.slee.SLEEException") == 0) {
						foundSLEEException = true;
					} else {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile table interface declares method with wrong exception in throws method with name: "
										+ methodName
										+ ", exception: "
										+ c.getName(), "10.8.2", errorBuffer);
					}
				}

				if (foundSLEEException
						&& foundTransactionRequiredLocalException) {
					// do nothing
				} else {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile table interface declares method with wrong exception in throws method with name: "
									+ methodName
									+ ", it shoudl declare SLEEException["
									+ foundSLEEException
									+ "] and TransactionRequiredLocalException["
									+ foundTransactionRequiredLocalException
									+ "]", "10.8.2", errorBuffer);
				}

				// lets see params - param type must match declared in MQuery,
				// also type must match CMP field from interface (and there must
				// be that kind of cmp

				Class[] parameterTypes = m.getParameterTypes();

				List<MQueryParameter> queryParameters = new ArrayList<MQueryParameter>();
				queryParameters.addAll(query.getQueryParameters());

				if (parameterTypes.length != queryParameters.size()) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification profile table interface declares method with wrong parameters count, present: "
									+ parameterTypes.length
									+ ", expected: "
									+ queryParameters.size(), "10.8.2",
							errorBuffer);
				} else {

					// yes, a bit different
					String parametersErrorBuffer = "Parameters that did not match descriptor: ";
					boolean failedOnQueries = false;
					Map<String, String> parameter2Type = new HashMap<String, String>();
					for (int index = 0; index < parameterTypes.length; index++) {

						// we can make some checks
						// first lets check type it must be the same, even
						// though it could be bad type, this is checked lower.
						// However this is method check

						if (parameterTypes[index].getName().compareTo(
								queryParameters.get(index).getType()) != 0) {
							failedOnQueries = true;
							passed = false;
							parametersErrorBuffer += " parameter: "
									+ queryParameters.get(index).getName()
									+ " declared type: "
									+ queryParameters.get(index).getType()
									+ " method type: " + parameterTypes[index]
									+ " in query method at index: " + index
									+ ",";
						}
						parameter2Type.put(
								queryParameters.get(index).getName(),
								queryParameters.get(index).getType());

						// here we can check only for equality
						// if (!_ALLOWED_QUERY_PARAMETER_TYPES
						// .contains(parameterTypes[index].getName())) {
						//							
						//	
						// parametersErrorBuffer += " method parameter: "
						// + queryParameters.get(index).getName()
						// + "has wrong type: "
						// + parameterTypes[index] + ", ";
						// }

					}

					if (failedOnQueries) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification profile table interface declares wrong method["
										+ methodName
										+ "] to match declared query, failed to match parameters - \n"
										+ parametersErrorBuffer, "10.20.2",
								errorBuffer);
					}

					if (!validateQueryAgainstCMPFields(queryName,
							cmpInterfaceClass, query.getQueryExpression(),
							parameter2Type)) {
						passed = false;
					}

				}

				// now we have to validate CMP part

			}

			if (nameToQueryMap.size() != 0) {
				passed = false;
				errorBuffer = appendToBuffer(
						"Profile specification profile table interface does not decalre query method for all queries, no methods for: "
								+ nameToQueryMap.keySet(), "10.20.2",
						errorBuffer);
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validateQueryAgainstCMPFields(String queryName,
			Class cmpInterfaceClass, MQueryExpression expression,
			Map<String, String> parameter2Type) {
		boolean passed = true;
		String attributeName = null;
		// this is the place where we can check for types, before that we could
		// do this half way
		String errorBuffer = new String("");

		try {
			switch (expression.getType()) {
			// "complex types"
			case And:
				for (MQueryExpression mqe : expression.getAnd()) {
					if (!validateQueryAgainstCMPFields(queryName,
							cmpInterfaceClass, mqe, parameter2Type)) {
						passed = false;
					}
				}
				break;
			case Or:
				for (MQueryExpression mqe : expression.getOr()) {
					if (!validateQueryAgainstCMPFields(queryName,
							cmpInterfaceClass, mqe, parameter2Type)) {
						passed = false;
					}
				}
				break;
			// "simple" types
			case Not:
				// this is one akward case :)
				if (!validateQueryAgainstCMPFields(queryName,
						cmpInterfaceClass, expression.getNot(), parameter2Type)) {
					passed = false;
				}

				break;

			// FIXME: tahts not nice, but lets have sippet for each case

			case Compare:
				// XXX: We know that attribute starts with lower case
				attributeName = expression.getCompare().getAttributeName();
				attributeName = attributeName.replaceFirst(""
						+ attributeName.charAt(0), ""
						+ Character.toUpperCase(attributeName.charAt(0)));
				// now we have to validate CMP field and type
				MCompare compare = expression.getCompare();
				String op = compare.getOp();
				try {
					Method m = cmpInterfaceClass.getMethod("get"
							+ attributeName, null);

					// attribute must be present and valid, now we have to
					// validate
					// its type and possibly parameter type.

					// return type must be the
					String returnType = m.getReturnType().getName();

					if (returnType.compareTo("boolean") == 0
							|| returnType.compareTo("Boolean") == 0) {
						// only op that can be used are: �equals�, or
						// �not-equals�
						if (op.compareTo("equals") == 0
								|| op.compareTo("not-equals") == 0) {
							// its ok, those are allowed
						} else {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification declared wrong static query - only operators in Compare expression  allowed on boolean attribute are \"equals\" and \"not-equals\": "
											+ attributeName
											+ ", query: "
											+ queryName, "10.20.2", errorBuffer);
						}
					}

					// now lets chec type
					if (!_ALLOWED_QUERY_PARAMETER_TYPES.contains(returnType)) {
						// there is one case we can not be wrogn here:
						if ((op.compareTo("equals") == 0 || op
								.compareTo("not-equals") == 0)
								&& returnType.compareTo("javax.slee.Address") == 0) {
							// its ok, those are allowed
						} else {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification declared wrong static query - Compare expression references attribute of wrong type: "
											+ returnType
											+ ", attribute:"
											+ attributeName
											+ ", query: "
											+ queryName, "10.20.2", errorBuffer);
						}

					}

					// we know that parameter references are ok
					if (compare.getParameter() != null
							&& parameter2Type.get(compare.getParameter())
									.compareTo(returnType) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - Compare expression references attribute type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", does not match parameter type: "
										+ parameter2Type.get(compare
												.getParameter())
										+ ", parameter name: "
										+ compare.getParameter()
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
					// FIXME: test get constructor from type and use string
					// constructor if its present, if not or if it fails make
					// fail validation?

					if (compare.getCollatorRef() != null
							&& returnType.compareTo("java.lang.String") != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - Compare expression references attribute of wrong type(only string parameter references can declare collator): "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
				} catch (Exception e) {

					// This should not happen, lets leave exception for this
					// case to
					// be alarmed.
					// e.printStackTrace();
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declared wrong static query - operator does not match against cmp field, requested cmp attribute: "
									+ attributeName, "10.20.2", errorBuffer);
				}
				break;
			case HasPrefix:
				MHasPrefix mhp = expression.getHasPrefix();
				attributeName = expression.getHasPrefix().getAttributeName();
				attributeName = attributeName.replaceFirst(""
						+ attributeName.charAt(0), ""
						+ Character.toUpperCase(attributeName.charAt(0)));
				try {
					Method m = cmpInterfaceClass.getMethod("get"
							+ attributeName, null);

					// attribute must be present and valid, now we have to
					// validate
					// its type and possibly parameter type.

					// return type must be the
					String returnType = m.getReturnType().getName();

					if (returnType.compareTo("java.lang.String") != 0) {
						// only op that can be used are: �equals�, or

						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - HasPrefix expression references attribute of wrong type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);

					}

					// we know that parameter references are ok
					if (mhp.getParameter() != null
							&& parameter2Type.get(mhp.getParameter())
									.compareTo(returnType) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - HasPrefix expression references attribute type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", does not match parameter type: "
										+ parameter2Type
												.get(mhp.getParameter())
										+ ", parameter name: "
										+ mhp.getParameter()
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
					// FIXME: test get constructor from type and use string
					// constructor if its present, if not or if it fails make
					// fail validation?

					if (mhp.getCollatorRef() != null
							&& returnType.compareTo("java.lang.String") != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - HasPrefix expression references attribute of wrong type(only string parameter references can declare collator): "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
				} catch (Exception e) {

					// This should not happen, lets leave exception for this
					// case to
					// be alarmed.
					// e.printStackTrace();
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declared wrong static query - operator does not match against cmp field, requested cmp attribute: "
									+ attributeName, "10.20.2", errorBuffer);
				}
				break;
			case LongestPrefixMatch:

				MLongestPrefixMatch mlpm = expression.getLongestPrefixMatch();
				attributeName = expression.getLongestPrefixMatch()
						.getAttributeName();
				attributeName = attributeName.replaceFirst(""
						+ attributeName.charAt(0), ""
						+ Character.toUpperCase(attributeName.charAt(0)));

				try {
					Method m = cmpInterfaceClass.getMethod("get"
							+ attributeName, null);

					// attribute must be present and valid, now we have to
					// validate
					// its type and possibly parameter type.

					// return type must be the
					String returnType = m.getReturnType().getName();

					if (returnType.compareTo("java.lang.String") != 0) {
						// only op that can be used are: �equals�, or

						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - LongestPrefixMatch expression references attribute of wrong type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);

					}

					// we know that parameter references are ok
					if (mlpm.getParameter() != null
							&& parameter2Type.get(mlpm.getParameter())
									.compareTo(returnType) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - LongestPrefixMatch expression references attribute type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", does not match parameter type: "
										+ parameter2Type.get(mlpm
												.getParameter())
										+ ", parameter name: "
										+ mlpm.getParameter()
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
					// FIXME: test get constructor from type and use string
					// constructor if its present, if not or if it fails make
					// fail validation?

					if (mlpm.getCollatorRef() != null
							&& returnType.compareTo("java.lang.String") != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - LongestPrefixMatch expression references attribute of wrong type(only string parameter references can declare collator): "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
				} catch (Exception e) {

					// This should not happen, lets leave exception for this
					// case to
					// be alarmed.
					// e.printStackTrace();
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declared wrong static query - operator does not match against cmp field, requested cmp attribute: "
									+ attributeName, "10.20.2", errorBuffer);
				}
				break;
			case RangeMatch:
				attributeName = expression.getRangeMatch().getAttributeName();
				attributeName = attributeName.replaceFirst(""
						+ attributeName.charAt(0), ""
						+ Character.toUpperCase(attributeName.charAt(0)));
				MRangeMatch mrm = expression.getRangeMatch();
				try {
					Method m = cmpInterfaceClass.getMethod("get"
							+ attributeName, null);

					// attribute must be present and valid, now we have to
					// validate
					// its type and possibly parameter type.

					// return type must be the
					String returnType = m.getReturnType().getName();

					// now lets chec type
					if (!_ALLOWED_QUERY_PARAMETER_TYPES.contains(returnType)) {
						// there is one case we can not be wrogn here:

						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - RangeMatch expression references attribute of wrong type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);

					}

					// we know that parameter references are ok
					if (mrm.getToParameter() != null
							&& parameter2Type.get(mrm.getToParameter())
									.compareTo(returnType) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - RangeMatch expression references attribute type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", does not match parameter type: "
										+ parameter2Type.get(mrm
												.getToParameter())
										+ ", toParameter name: "
										+ mrm.getToParameter()
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}

					// we know that parameter references are ok
					if (mrm.getFromParameter() != null
							&& parameter2Type.get(mrm.getFromParameter())
									.compareTo(returnType) != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - RangeMatch expression references attribute type: "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", does not match parameter type: "
										+ parameter2Type.get(mrm
												.getFromParameter())
										+ ", fromParameter name: "
										+ mrm.getFromParameter()
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
					// FIXME: test get constructor from type and use string
					// constructor if its present, if not or if it fails make
					// fail validation?

					if (mrm.getCollatorRef() != null
							&& returnType.compareTo("java.lang.String") != 0) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declared wrong static query - RangeMatch expression references attribute of wrong type(only string parameter references can declare collator): "
										+ returnType
										+ ", attribute:"
										+ attributeName
										+ ", query: "
										+ queryName, "10.20.2", errorBuffer);
					}
				} catch (Exception e) {

					// This should not happen, lets leave exception for this
					// case to
					// be alarmed.
					// e.printStackTrace();
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declared wrong static query - operator does not match against cmp field, requested cmp attribute: "
									+ attributeName, "10.20.2", errorBuffer);
				}

				break;
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean validateEnvEntries() {

		boolean passed = true;
		String errorBuffer = new String("");

		try {

			List<MEnvEntry> envEntries = this.component.getDescriptor()
					.getEnvEntries();
			for (MEnvEntry e : envEntries) {
				if (!_ENV_ENTRIES_TYPES.contains(e.getEnvEntryType())) {

					passed = false;
					errorBuffer = appendToBuffer("Env entry has wrong type: "
							+ e.getEnvEntryType() + " , method: "
							+ e.getEnvEntryName(), "6.13", errorBuffer);
				}

			}

		} finally {
			if (!passed) {
				// System.err.println(errorBuffer);
				logger.error(errorBuffer);
			}

		}

		return passed;
	}

	/**
	 * Validated descriptor against some basic constraints: all references are
	 * correct, some fields are declaredproperly, no double definitions, if
	 * proper elements are present - for instance some elements exclude others.
	 * 
	 * @return
	 */
	boolean validateDescriptor() {

		boolean passed = true;
		String errorBuffer = new String("");

		if (!this.component.isSlee11()) {
			return passed;
			// there is not much we can do for those oldies.
		}

		try {
			HashSet<String> collatorAlliases = new HashSet<String>();
			ProfileSpecificationDescriptorImpl desc = this.component
					.getDescriptor();
			for (MCollator mc : desc.getCollators()) {
				if (collatorAlliases.contains(mc.getCollatorAlias())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares collator alias twice: "
									+ mc.getCollatorAlias(), "3.3.7",
							errorBuffer);
				} else {
					collatorAlliases.add(mc.getCollatorAlias());
				}
			}

			// double deifnition of refs is allowed.
			Map<String, MCMPField> cmpName2Field = new HashMap<String, MCMPField>();
			for (MCMPField c : desc.getProfileCMPInterface().getCmpFields()) {

				if (!Character.isLowerCase(c.getCmpFieldName().charAt(0))) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares wrong cmp field name, first char is not lower case, field: "
									+ c.getCmpFieldName(), "3.3.7", errorBuffer);
				}

				if (cmpName2Field.containsKey(c.getCmpFieldName())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares cmp field twice: "
									+ c.getCmpFieldName(), "3.3.7", errorBuffer);
				} else {
					cmpName2Field.put(c.getCmpFieldName(), c);
				}

				if (c.getUniqueCollatorRef() != null
						&& !collatorAlliases.contains(c.getUniqueCollatorRef())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares cmp field: "
									+ c.getCmpFieldName()
									+ ", with wrong collator reference: "
									+ c.getUniqueCollatorRef(), "3.3.7",
							errorBuffer);
				}

				for (MIndexHint indexHint : c.getIndexHints()) {
					if (indexHint.getCollatorRef() != null
							&& !collatorAlliases.contains(indexHint
									.getCollatorRef())) {
						passed = false;
						errorBuffer = appendToBuffer(
								"Profile specification declares cmp field: "
										+ c.getCmpFieldName()
										+ ", with index hint declaring wrong collator reference: "
										+ c.getUniqueCollatorRef(), "3.3.7",
								errorBuffer);
					}
				}

			}

			Set<String> queriesNames = new HashSet<String>();
			for (MQuery mq : desc.getQueryElements()) {

				if (queriesNames.contains(mq.getName())) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares queries with the same name: "
									+ mq.getName(), "3.3.7", errorBuffer);
				} else {

					// FIXME: all declaredparameters have to be used in
					// expressions?
					HashSet<String> decalredParameters = new HashSet<String>();
					HashSet<String> usedParameters = new HashSet<String>();
					for (MQueryParameter mqp : mq.getQueryParameters()) {
						if (decalredParameters.contains(mqp.getName())) {
							passed = false;
							errorBuffer = appendToBuffer(
									"Profile specification declares query parameter twice, parameter name: "
											+ mqp.getName() + ", in query: "
											+ mq.getName(), "3.3.7",
									errorBuffer);
						} else {
							decalredParameters.add(mqp.getName());
						}

					}

					if (!validateExpression(mq.getName(), mq
							.getQueryExpression(), usedParameters,
							cmpName2Field.keySet(), collatorAlliases)) {
						passed = false;
					}

					if (!usedParameters.containsAll(decalredParameters)
							&& !decalredParameters.containsAll(usedParameters)) {
						passed = false;
						decalredParameters.retainAll(usedParameters);
						errorBuffer = appendToBuffer(
								"Profile specification declares query parameter that are not used, in query: "
										+ mq.getName()
										+ ", not used parameters: "
										+ Arrays.toString(decalredParameters
												.toArray()), "3.3.7",
								errorBuffer);
					}

				}

			}

			// FIXME: this should be here or not?
			if (!validateEnvEntries()) {
				passed = false;
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;

	}

	/**
	 * Checks expression to check if collator refs and cmp fields are referenced
	 * correctly. type constraints are checked later. Since in case of lack of
	 * parameter we have to relly totaly on CMP field type. Additionaly this
	 * check if parameter and value are present together.
	 * 
	 * @param queryName
	 * @param cmpInterfaceClass
	 * @param expression
	 * @param decalredQueryParameter
	 * @param usedQueryParameter
	 * @return
	 */
	boolean validateExpression(String queryName, MQueryExpression expression,

	Set<String> usedQueryParameter, Set<String> cmpFieldNames,
			Set<String> collatorAliasses) {
		boolean passed = true;
		String attributeName = null;
		String collatorRef = null;
		String parameter = null;
		String value = null;
		boolean ignoreAbsence = false;

		// We dont have access to type of cmp, we just simply check parameter

		String errorBuffer = new String("");
		try {
			switch (expression.getType()) {
			// "complex types"
			case And:
				for (MQueryExpression mqe : expression.getAnd()) {
					if (!validateExpression(queryName, mqe, usedQueryParameter,
							cmpFieldNames, collatorAliasses)) {
						passed = false;
					}
				}
				break;
			case Or:
				for (MQueryExpression mqe : expression.getOr()) {
					if (!validateExpression(queryName, mqe, usedQueryParameter,
							cmpFieldNames, collatorAliasses)) {
						passed = false;
					}
				}
				break;
			// "simple" types
			case Not:
				// this is one akward case :)
				if (!validateExpression(queryName, expression.getNot(),
						usedQueryParameter, cmpFieldNames, collatorAliasses)) {
					passed = false;
				}

				break;

			case Compare:
				attributeName = expression.getCompare().getAttributeName();
				collatorRef = expression.getCompare().getCollatorRef();
				parameter = expression.getCompare().getParameter();
				value = expression.getCompare().getValue();

				break;
			case HasPrefix:
				attributeName = expression.getHasPrefix().getAttributeName();
				collatorRef = expression.getHasPrefix().getCollatorRef();
				parameter = expression.getHasPrefix().getParameter();
				value = expression.getHasPrefix().getValue();
				break;
			case LongestPrefixMatch:
				attributeName = expression.getLongestPrefixMatch()
						.getAttributeName();
				collatorRef = expression.getLongestPrefixMatch()
						.getCollatorRef();
				parameter = expression.getLongestPrefixMatch().getParameter();
				value = expression.getLongestPrefixMatch().getValue();
				break;
			case RangeMatch:
				attributeName = expression.getRangeMatch().getAttributeName();
				collatorRef = expression.getRangeMatch().getCollatorRef();

				MRangeMatch mrm = expression.getRangeMatch();

				ignoreAbsence = true;
				if (mrm.getFromParameter() == null
						&& mrm.getFromValue() == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, either fromValue or fromParameter can be present, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}

				if (mrm.getFromParameter() != null
						&& mrm.getFromValue() != null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, either fromValue or fromParameter can be present, not both, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}

				if (mrm.getToParameter() == null && mrm.getToValue() == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, either toValue or toParameter can be present, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}

				if (mrm.getToParameter() != null && mrm.getToValue() != null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, either toValue or toParameter can be present, not both, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}
				if (mrm.getFromParameter() != null) {
					usedQueryParameter.add(mrm.getFromParameter());
				}

				if (mrm.getToParameter() != null) {
					usedQueryParameter.add(mrm.getToParameter());
				}

				break;
			}

			//This will hapen for Not,And, Or operators
			if (attributeName != null) {
				if (!Character.isLowerCase(attributeName.charAt(0))) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query usage of cmp attribute that is not valid cmp identifier, declared cmp field: "
									+ attributeName
									+ ", query name: "
									+ queryName, "10.20.2", errorBuffer);
				} else if (!cmpFieldNames.contains(attributeName)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query usage of cmp field that does not exist, declared cmp field: "
									+ attributeName
									+ ", query name: "
									+ queryName, "10.20.2", errorBuffer);
				}
				if (collatorRef != null
						&& !collatorAliasses.contains(collatorRef)) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query usage of collator that is not aliased, collator alias: "
									+ collatorRef
									+ ", query name: "
									+ queryName, "10.20.2", errorBuffer);
				}

				if (!ignoreAbsence && parameter != null && value != null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, value and parameter can not be present at the same time, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}

				if (!ignoreAbsence && parameter == null && value == null) {
					passed = false;
					errorBuffer = appendToBuffer(
							"Profile specification declares in static query wrong properties, either value or parameter must be present, query name: "
									+ queryName, "10.20.2", errorBuffer);
				}
				if (parameter != null) {
					usedQueryParameter.add(parameter);
				}
			}

		} finally {

			if (!passed) {
				logger.error(errorBuffer);
				System.err.println(errorBuffer);
			}

		}

		return passed;
	}

	boolean compareMethod(Method m1, Method m2) {

		// those two are in key
		if (m1.getModifiers() != m2.getModifiers())
			return false;
		if (!Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())) {
			return false;
		}

		if (m1.getName().compareTo(m2.getName()) != 0)
			return false;

		if (!Arrays.equals(m1.getExceptionTypes(), m2.getExceptionTypes())) {
			return false;
		}

		if (m1.getReturnType().getName()
				.compareTo(m2.getReturnType().getName()) != 0) {
			return false;
		}

		return true;
	}

	protected String appendToBuffer(String message, String section,
			String buffer) {
		buffer += (this.component.getDescriptor().getProfileSpecificationID()
				+ " : violates section " + section
				+ " of jSLEE 1.1 specification : " + message + "\n");
		return buffer;
	}

}
