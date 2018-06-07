package hu.qgears.ecore.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.codegen.ecore.genmodel.GenBase;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
import org.eclipse.emf.codegen.ecore.genmodel.GenDataType;
import org.eclipse.emf.codegen.ecore.genmodel.GenEnum;
import org.eclipse.emf.codegen.ecore.genmodel.GenEnumLiteral;
import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenOperation;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenParameter;
import org.eclipse.emf.codegen.ecore.genmodel.GenRuntimePlatform;
import org.eclipse.emf.codegen.ecore.genmodel.GenRuntimeVersion;
import org.eclipse.emf.codegen.ecore.genmodel.GenTypeParameter;
import org.eclipse.emf.codegen.ecore.genmodel.impl.Literals;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Generates EPackage implementation classes. Based on original template, but
 * static initialization of meta model objects were refactored : The
 * initializers run without any exceptions, even when the metamodel consist of
 * many, highly coupled packages with lot of circular dependencies.
 * <p>
 * Every change in template is moved into a separate method, so you can easily
 * distinguish the unchanged original and the updated parts of the template.
 * 
 * @author agostoni
 *
 */
public class PackageImplTemplate extends PackageTemplate {

	private class Information {
		@SuppressWarnings("unused")
		EGenericType eGenericType;
		int depth;
		String type;
		String accessor;
	}

	private class InformationIterator {
		Iterator<?> iterator;

		InformationIterator(EGenericType eGenericType) {
			iterator = EcoreUtil.getAllContents(Collections.singleton(eGenericType));
		}

		boolean hasNext() {
			return iterator.hasNext();
		}

		Information next() {
			Information information = new Information();
			EGenericType eGenericType = information.eGenericType = (EGenericType) iterator.next();
			for (EObject container = eGenericType.eContainer(); container instanceof EGenericType; container = container
					.eContainer()) {
				++information.depth;
			}
			if (eGenericType.getEClassifier() != null) {
				GenClassifier genClassifier = genModel.findGenClassifier(eGenericType.getEClassifier());
				information.type = getDependentPackageAccessor(genClassifier.getGenPackage()) + ".get"
						+ genClassifier.getClassifierAccessorName() + "()";
			} else if (eGenericType.getETypeParameter() != null) {
				ETypeParameter eTypeParameter = eGenericType.getETypeParameter();
				if (eTypeParameter.eContainer() instanceof EClass) {
					information.type = genModel.findGenClassifier((EClass) eTypeParameter.eContainer())
							.getClassifierInstanceName() + "_" + eGenericType.getETypeParameter().getName();
				} else {
					information.type = "t"
							+ (((EOperation) eTypeParameter.eContainer()).getETypeParameters().indexOf(eTypeParameter)
									+ 1);
				}
			} else {
				information.type = "";
			}
			if (information.depth > 0) {
				if (eGenericType.eContainmentFeature().isMany()) {
					information.accessor = "getE" + eGenericType.eContainmentFeature().getName().substring(1)
							+ "().add";
				} else {
					information.accessor = "setE" + eGenericType.eContainmentFeature().getName().substring(1);
				}
			}
			return information;
		}
	}

	private boolean needsAddEOperation = false;
	private boolean needsAddEParameter = false;
	private int maxGenericTypeAssignment = 0;
	private boolean firstOperationAssignment = true;
	private int maxTypeParameterAssignment = 0;
	private boolean doGeneratePackageDeps = false;

	public PackageImplTemplate(GenPackage genPackage) {
		super(genPackage);
	}

	protected void doGenerate() {
		String publicStaticFinalFlag = "public static final ";
		StringBuffer stringBuffer = rtout.getBuffer();
		rtout.write("/**");
		{
			GenBase copyrightHolder = genPackage;
			if (copyrightHolder != null && copyrightHolder.hasCopyright()) {
				rtout.write("\n * ");
				rtcout.write(copyrightHolder.getCopyright(copyrightHolder.getGenModel().getIndentation(stringBuffer)));
			}
		}
		rtout.write("\n */\n");
		if (!genModel.isSuppressInterfaces()) {
			rtout.write("\npackage ");
			rtcout.write(genPackage.getClassPackageName());
			rtout.write(";\n");
		}
		rtout.write("\n");
		genModel.markImportLocation(stringBuffer, genPackage);
		genModel.addPseudoImport("org.eclipse.emf.ecore.EPackage.Registry");
		genModel.addPseudoImport("org.eclipse.emf.ecore.EPackage.Descriptor");
		genModel.addPseudoImport("org.eclipse.emf.ecore.impl.EPackageImpl.EBasicWhiteList");
		genModel.addPseudoImport("org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container");
		genModel.addPseudoImport("org.eclipse.emf.ecore.impl.MinimalEObjectImpl.Container.Dynamic");
		if (genPackage.isLiteralsInterface()) {
			genModel.addPseudoImport(genPackage.getQualifiedPackageInterfaceName() + ".Literals");
		}
		for (GenClassifier genClassifier : genPackage.getOrderedGenClassifiers()) {
			genModel.addPseudoImport(
					genPackage.getQualifiedPackageInterfaceName() + "." + genPackage.getClassifierID(genClassifier));
		}
		rtout.write("\n\n/**\n * <!-- begin-user-doc -->\n * An implementation of the model <b>Package</b>.\n * <!-- end-user-doc -->\n * @generated\n */\n\npublic class ");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write(" extends ");
		rtcout.writeClass(EPackageImpl.class);
		rtout.write(" implements ");
		rtcout.write(genPackage.getImportedPackageInterfaceName());
		rtout.write("\n{");
		if (genModel.hasCopyrightField()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
			rtcout.write(publicStaticFinalFlag);
			rtcout.write(genModel.getImportedName("java.lang.String"));
			rtout.write(" copyright = ");
			rtcout.write(genModel.getCopyrightFieldLiteral());
			rtout.write(";");
			rtcout.write(genModel.getNonNLS());
			rtout.write("\n");
		}
		if (genPackage.isLoadingInitialization()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprotected String packageFilename = \"");
			rtcout.write(genPackage.getSerializedPackageFilename());
			rtout.write("\";");
			rtcout.write(genModel.getNonNLS());
			rtout.write("\n");
		}
		for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate ");
			rtcout.write(genClassifier.getImportedMetaType());
			rtout.write(" ");
			rtcout.write(genClassifier.getClassifierInstanceName());
			rtout.write(" = null;\n");
		}
		rtout.write("\n\t/**\n\t * Creates an instance of the model <b>Package</b>, registered with\n\t * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package\n\t * package URI value.\n\t * <p>Note: the correct way to create the package is via the static\n\t * factory method {@link #init init()}, which also performs\n\t * initialization of the package, or returns the registered package,\n\t * if one already exists.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @see org.eclipse.emf.ecore.EPackage.Registry\n\t * @see ");
		rtcout.write(genPackage.getQualifiedPackageInterfaceName());
		rtout.write("#eNS_URI\n\t * @see #init()\n\t * @generated\n\t */\n\tprivate ");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write("()\n\t{\n\t\tsuper(eNS_URI, ");
		rtcout.write(genPackage.getQualifiedEFactoryInstanceAccessor());
		rtout.write(");\n\t}\n\n");

		generatePackageHelper();

		rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate static boolean isInited = false;\n\n\t/**\n\t * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.\n\t * \n\t * <p>This method is used to initialize {@link ");
		rtcout.write(genPackage.getImportedPackageInterfaceName());
		rtout.write("#eINSTANCE} when that field is accessed.\n\t * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @see #eNS_URI");
		if (!genPackage.isLoadedInitialization()) {
			rtout.write("\n\t * @see #createPackageContents()\n\t * @see #initializePackageContents()");
		}
		rtout.write("\n\t * @generated\n\t */\n\tpublic static ");
		rtcout.write(genPackage.getImportedPackageInterfaceName());
		rtout.write(" init()\n\t{\n\t\tif (isInited) return (");
		rtcout.write(genPackage.getImportedPackageInterfaceName());
		rtout.write(")");
		rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
		rtout.write(".Registry.INSTANCE.getEPackage(");
		rtcout.write(genPackage.getImportedPackageInterfaceName());
		rtout.write(".eNS_URI);\n");
		if (genModel.getRuntimePlatform() == GenRuntimePlatform.GWT) {
			rtout.write("\n\t\tinitializeRegistryHelpers();\n");
		}
		rtout.write("\n\t\t// Obtain or create and register package\n\t\t");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write(" the");
		rtcout.write(genPackage.getBasicPackageName());
		rtout.write(" = (");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write(")(");
		rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
		rtout.write(".Registry.INSTANCE.get(eNS_URI) instanceof ");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write(" ? ");
		rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
		rtout.write(".Registry.INSTANCE.get(eNS_URI) : new ");
		rtcout.write(genPackage.getPackageClassName());
		rtout.write("());\n\n\t\tisInited = true;\n");

		if (doGeneratePackageDeps) {
			generatePackageDependencyInitialization();
		} else {
			rtout.write("\t\tthe");
			rtcout.write(genPackage.getBasicPackageName());
			rtout.write(".createPackageContents();\n// Update the registry and return the package\n\t\t");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
			rtout.write(".Registry.INSTANCE.put(");
			rtcout.write(genPackage.getImportedPackageInterfaceName());
			rtout.write(".eNS_URI, the");
			rtcout.write(genPackage.getBasicPackageName());
			rtout.write(");\n\n\t\tthe");
			rtcout.write(genPackage.getBasicPackageName());
			rtout.write(".initializePackageContents();\n");
		}
		if (genPackage.hasConstraints()) {
			rtout.write("\n\t\t// Register package validator\n\t\t");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EValidator"));
			rtout.write(".Registry.INSTANCE.put\n\t\t\t(the");
			rtcout.write(genPackage.getBasicPackageName());
			rtout.write(", \n\t\t\t new ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EValidator"));
			rtout.write(".Descriptor()\n\t\t\t {\n\t\t\t\t public ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EValidator"));
			rtout.write(" getEValidator()\n\t\t\t\t {\n\t\t\t\t\t return ");
			rtcout.write(genPackage.getImportedValidatorClassName());
			rtout.write(".INSTANCE;\n\t\t\t\t }\n\t\t\t });\n");
		}
		if (!genPackage.isEcorePackage()) {
			rtout.write("\n\t\t// Mark meta-data to indicate it can't be changed\n");
			for ( GenPackage p : genModel.getUsedGenPackages()) {
				if (!p.equals(genPackage) && !p.isEcorePackage()) {
					rtout.write("\t\t");
					rtcout.write(p.getImportedPackageClassName());
					rtout.write(".init();\n");
				}
			}
			rtout.write("\t\tthe");
			rtcout.write(genPackage.getBasicPackageName());
			rtout.write(".freeze();\n");
		}
		rtout.write("\n  \n\t\treturn the");
		rtcout.write(genPackage.getBasicPackageName());
		rtout.write(";\n\t}");
		if (genModel.getRuntimePlatform() == GenRuntimePlatform.GWT) {
			rtout.write("\n\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic static void initializeRegistryHelpers()\n\t{");
			Set<String> helpers = new HashSet<String>();
			for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
				if (genClassifier instanceof GenClass) {
					GenClass genClass = (GenClass) genClassifier;
					if (!genClass.isDynamic()) {
						String theClass = genClass.isMapEntry() ? genClass.getImportedClassName()
								: genClass.getRawImportedInterfaceName();
						if (helpers.add(theClass)) {
							rtout.write("\n\t\t");
							rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.Reflect"));
							rtout.write(".register\n\t\t\t(");
							rtcout.write(theClass);
							rtout.write(".class, \n\t\t\t new ");
							rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.Reflect"));
							rtout.write(".Helper() \n\t\t\t {\n\t\t\t\t public boolean isInstance(Object instance)\n\t\t\t\t {\n\t\t\t\t\t return instance instanceof ");
							rtcout.write(genClass.isMapEntry() ? genClass.getImportedClassName()
									: genClass.getRawImportedInterfaceName()
											+ genClass.getInterfaceWildTypeArguments());
							rtout.write(";\n\t\t\t\t }\n\n\t\t\t\t public Object newArrayInstance(int size)\n\t\t\t\t {\n\t\t\t\t\t return new ");
							rtcout.write(theClass);
							rtout.write("[size];\n\t\t\t\t }\n\t\t\t });");
						}
					}
				} else if (genClassifier instanceof GenDataType) {
					GenDataType genDataType = (GenDataType) genClassifier;
					if (!genDataType.isPrimitiveType() && !genDataType.isObjectType()) {
						String theClass = genDataType.getRawImportedInstanceClassName();
						if (helpers.add(theClass)) {
							rtout.write("\n\t\t");
							rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.Reflect"));
							rtout.write(".register\n\t\t\t(");
							rtcout.write(theClass);
							rtout.write(".class, \n\t\t\t new ");
							rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.Reflect"));
							rtout.write(".Helper() \n\t\t\t {\n\t\t\t\t public boolean isInstance(Object instance)\n\t\t\t\t {\n\t\t\t\t\t return instance instanceof ");
							rtcout.write(theClass);
							rtout.write(";\n\t\t\t\t }\n\n\t\t\t\t public Object newArrayInstance(int size)\n\t\t\t\t {");
							if (genDataType.isArrayType()) {
								String componentType = theClass;
								String indices = "";
								while (componentType.endsWith("[]")) {
									componentType = componentType.substring(0, componentType.length() - 2);
									indices += "[]";
								}
								rtout.write("\n\t\t\t\t\t return new ");
								rtcout.write(componentType);
								rtout.write("[size]");
								rtcout.write(indices);
								rtout.write(";");
							} else {
								rtout.write("\n\t\t\t\t\t return new ");
								rtcout.write(theClass);
								rtout.write("[size];");
							}
							rtout.write("\n\t\t\t\t }\n\t\t});");
						}
					}
				}
			}
			rtout.write("\n\t}\n\n\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic static class WhiteList implements ");
			rtcout.write(genModel.getImportedName("com.google.gwt.user.client.rpc.IsSerializable"));
			rtout.write(", EBasicWhiteList\n\t{");
			for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
				if (genClassifier instanceof GenClass) {
					GenClass genClass = (GenClass) genClassifier;
					if (!genClass.isDynamic()) {
						rtout.write("\n\t\t/**\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @generated\n\t\t */\n\t\tprotected ");
						rtcout.write(genClass.isMapEntry() ? genClass.getImportedClassName()
								: genClass.getImportedWildcardInstanceClassName());
						rtout.write(" ");
						rtcout.write(genClass.getSafeUncapName());
						rtout.write(";\n");
					}
				} else if (genClassifier instanceof GenDataType) {
					GenDataType genDataType = (GenDataType) genClassifier;
					if (!genDataType.isObjectType() && genDataType.isSerializable()) {
						rtout.write("\n\t\t/**\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @generated\n\t\t */\n\t\tprotected ");
						rtcout.write(genDataType.getImportedWildcardInstanceClassName());
						rtout.write(" ");
						rtcout.write(genDataType.getSafeUncapName());
						rtout.write(";\n");
					}
				}
			}
			rtout.write("\n\t}");
		}
		rtout.write("\n");
		for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {

			generateClassifierGetter(genPackage, genModel, genClassifier);

			if (genClassifier instanceof GenClass) {
				GenClass genClass = (GenClass) genClassifier;
				for (GenFeature genFeature : genClass.getGenFeatures()) {
					generateFeatureGetter(genPackage, genClassifier, genClass, genFeature);
				}
				if (genModel.isOperationReflection()) {
					for (GenOperation genOperation : genClass.getGenOperations()) {
						rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\n\tpublic ");
						rtcout.write(genOperation.getImportedMetaType());
						rtout.write(" get");
						rtcout.write(genOperation.getOperationAccessorName());
						rtout.write("()\n\t{");
						if (!genPackage.isLoadedInitialization()) {
							rtout.write("\n\t\treturn ");
							rtcout.write(genClass.getClassifierInstanceName());
							rtout.write(".getEOperations().get(");
							rtcout.write(genClass.getLocalOperationIndex(genOperation));
							rtout.write(");");
						} else {
							rtout.write("\n        return get");
							rtcout.write(genClassifier.getClassifierAccessorName());
							rtout.write("().getEOperations().get(");
							rtcout.write(genClass.getLocalOperationIndex(genOperation));
							rtout.write(");");
						}
						rtout.write("\n\t}\n");
					}
				}
			}
		}
		rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\n\tpublic ");
		rtcout.write(genPackage.getImportedFactoryInterfaceName());
		rtout.write(" get");
		rtcout.write(genPackage.getFactoryName());
		rtout.write("()\n\t{\n\t\treturn (");
		rtcout.write(genPackage.getImportedFactoryInterfaceName());
		rtout.write(")getEFactoryInstance();\n\t}\n");
		if (!genPackage.isLoadedInitialization()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate boolean isCreated = false;\n\n\t/**\n\t * Creates the meta-model objects for the package.  This method is\n\t * guarded to have no affect on any invocation but its first.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic void createPackageContents()\n\t{\n\t\tif (isCreated) return;\n\t\tisCreated = true;");
			if (!genPackage.getGenClasses().isEmpty()) {
				rtout.write("\n\n\t\t// Create classes and their features");
				for (Iterator<GenClass> c = genPackage.getGenClasses().iterator(); c.hasNext();) {
					GenClass genClass = c.next();
					rtout.write("\n\t\t");
					rtcout.write(genClass.getClassifierInstanceName());
					rtout.write(" = create");
					rtcout.write(genClass.getMetaType());
					rtout.write("(");
					rtcout.write(genClass.getClassifierID());
					rtout.write(");");
					for (GenFeature genFeature : genClass.getGenFeatures()) {
						rtout.write("\n\t\tcreate");
						rtcout.write(genFeature.getMetaType());
						rtout.write("(");
						rtcout.write(genClass.getClassifierInstanceName());
						rtout.write(", ");
						rtcout.write(genClass.getFeatureID(genFeature));
						rtout.write(");");
					}
					if (genModel.isOperationReflection()) {
						for (GenOperation genOperation : genClass.getGenOperations()) {
							rtout.write("\n\t\tcreateEOperation(");
							rtcout.write(genClass.getClassifierInstanceName());
							rtout.write(", ");
							rtcout.write(genClass.getOperationID(genOperation, false));
							rtout.write(");");
						}
					}
					if (c.hasNext()) {
						rtout.write("\n");
					}
				}
			}
			if (!genPackage.getGenEnums().isEmpty()) {
				rtout.write("\n\n\t\t// Create enums");
				for (GenEnum genEnum : genPackage.getGenEnums()) {
					rtout.write("\n\t\t");
					rtcout.write(genEnum.getClassifierInstanceName());
					rtout.write(" = createEEnum(");
					rtcout.write(genEnum.getClassifierID());
					rtout.write(");");
				}
			}
			if (!genPackage.getGenDataTypes().isEmpty()) {
				rtout.write("\n\n\t\t// Create data types");
				for (GenDataType genDataType : genPackage.getGenDataTypes()) {
					rtout.write("\n\t\t");
					rtcout.write(genDataType.getClassifierInstanceName());
					rtout.write(" = createEDataType(");
					rtcout.write(genDataType.getClassifierID());
					rtout.write(");");
				}
			}
			rtout.write("\n\t}\n\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate boolean isInitialized = false;\n");

			///////////////////////

			///////////////////////

			rtout.write("\n\t/**\n\t * Complete the initialization of the package and its meta-model.  This\n\t * method is guarded to have no affect on any invocation but its first.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic void initializePackageContents()\n\t{\n\t\tif (isInitialized) return;\n\t\tisInitialized = true;\n\n\t\t// Initialize package\n\t\tsetName(eNAME);\n\t\tsetNsPrefix(eNS_PREFIX);\n\t\tsetNsURI(eNS_URI);");
			if (!genPackage.getPackageInitializationDependencies().isEmpty() && doGeneratePackageDeps) {
				rtout.write("\n\n\t\t// Obtain other dependent packages");
				for (GenPackage dep : genPackage.getPackageInitializationDependencies()) {
					rtout.write("\n\t\t");
					rtcout.write(dep.getImportedPackageInterfaceName());
					rtout.write(" ");
					rtcout.write(genPackage.getPackageInstanceVariable(dep));
					rtout.write(" = (");
					rtcout.write(dep.getImportedPackageInterfaceName());
					rtout.write(")");
					rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
					rtout.write(".Registry.INSTANCE.getEPackage(");
					rtcout.write(dep.getImportedPackageInterfaceName());
					rtout.write(".eNS_URI);");
				}
			}
			if (!genPackage.getSubGenPackages().isEmpty()) {
				rtout.write("\n\n\t\t// Add subpackages\n");
				for (GenPackage sub : genPackage.getSubGenPackages()) {
					rtout.write("\n\t\tgetESubpackages().add(");
					rtcout.write(genPackage.getPackageInstanceVariable(sub));
					rtout.write(");");
				}
			}
			if (!genPackage.getGenClasses().isEmpty()) {
				// innen
				// rtout.write("\t\t// Add supertypes to classes\n");
				// for (GenClass genClass : genPackage.getGenClasses()) {
				// generateClassSuperTypeInit(genClass);
				// }
				// rtout.write("\n");
				// idáig
				rtout.write("\t\t// Initialize classes, features, and operations; add parameters\n");
				for (Iterator<GenClass> c = genPackage.getGenClasses().iterator(); c.hasNext();) {
					GenClass genClass = c.next();
					rtout.write("\t\tget");
					rtcout.write(genClass.getClassifierAccessorName());
					rtout.write("();\n");
				}
			}
			if (!genPackage.getGenEnums().isEmpty()) {
				rtout.write("\n\n\t\t// Initialize enums and add enum literals");
				for (Iterator<GenEnum> e = genPackage.getGenEnums().iterator(); e.hasNext();) {
					GenEnum genEnum = e.next();
					rtout.write("\n\t\tinitEEnum(");
					rtcout.write(genEnum.getClassifierInstanceName());
					rtout.write(", ");
					rtcout.write(genEnum.getImportedName());
					rtout.write(".class, \"");
					rtcout.write(genEnum.getName());
					rtout.write("\");");
					rtcout.write(genModel.getNonNLS());
					for (GenEnumLiteral genEnumLiteral : genEnum.getGenEnumLiterals()) {
						rtout.write("\n\t\taddEEnumLiteral(");
						rtcout.write(genEnum.getClassifierInstanceName());
						rtout.write(", ");
						rtcout.write(genEnum.getImportedName().equals(genEnum.getClassifierID())
								? genEnum.getQualifiedName() : genEnum.getImportedName());
						rtout.write(".");
						rtcout.write(genEnumLiteral.getEnumLiteralInstanceConstantName());
						rtout.write(");");
					}
					if (e.hasNext()) {
						rtout.write("\n");
					}
				}
			}
			if (!genPackage.getGenDataTypes().isEmpty()) {
				rtout.write("\n\n\t\t// Initialize data types");
				for (GenDataType genDataType : genPackage.getGenDataTypes()) {
					boolean hasInstanceTypeName = genModel.useGenerics()
							&& genDataType.getEcoreDataType().getInstanceTypeName() != null
							&& genDataType.getEcoreDataType().getInstanceTypeName().contains("<");
					rtout.write("\n\t\tinitEDataType(");
					rtcout.write(genDataType.getClassifierInstanceName());
					rtout.write(", ");
					rtcout.write(genDataType.getRawImportedInstanceClassName());
					rtout.write(".class, \"");
					rtcout.write(genDataType.getName());
					rtout.write("\", ");
					rtcout.write(genDataType.getSerializableFlag());
					rtout.write(", ");
					rtcout.write(genDataType.getGeneratedInstanceClassFlag());
					if (hasInstanceTypeName) {
						rtout.write(", \"");
						rtcout.write(genDataType.getEcoreDataType().getInstanceTypeName());
						rtout.write("\"");
					}
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
					if (hasInstanceTypeName) {
						rtcout.write(genModel.getNonNLS(2));
					}
				}
			}
			if (genPackage.getSuperGenPackage() == null) {
				rtout.write("\n\n\t\t// Create resource\n\t\tcreateResource(");
				rtcout.write(genPackage.getSchemaLocation());
				rtout.write(");");
			}
			if (!genPackage.isEcorePackage() && !genPackage.getAnnotationSources().isEmpty()) {
				rtout.write("\n\n\t\t// Create annotations");
				for (String annotationSource : genPackage.getAnnotationSources()) {
					rtout.write("\n\t\t// ");
					rtcout.write(annotationSource);
					rtout.write("\n\t\tcreate");
					rtcout.write(genPackage.getAnnotationSourceIdentifier(annotationSource));
					rtout.write("Annotations();");
				}
			}
			rtout.write("\n\t}\n");
			for (String annotationSource : genPackage.getAnnotationSources()) {
				rtout.write("\n\t/**\n\t * Initializes the annotations for <b>");
				rtcout.write(annotationSource);
				rtout.write("</b>.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprotected void create");
				rtcout.write(genPackage.getAnnotationSourceIdentifier(annotationSource));
				rtout.write("Annotations()\n\t{\n\t\tString source = ");
				if (annotationSource == null) {
					rtout.write("null;");
				} else {
					rtout.write("\"");
					rtcout.write(annotationSource);
					rtout.write("\";");
					rtcout.write(genModel.getNonNLS());
				}
				for (EAnnotation eAnnotation : genPackage.getAllAnnotations()) {
					List<GenPackage.AnnotationReferenceData> annotationReferenceDataList = genPackage
							.getReferenceData(eAnnotation);
					if (annotationSource == null ? eAnnotation.getSource() == null
							: annotationSource.equals(eAnnotation.getSource())) {
						rtout.write("\t\n\t\taddAnnotation\n\t\t  (");
						rtcout.write(genPackage.getAnnotatedModelElementAccessor(eAnnotation));
						rtout.write(", \n\t\t   source, \n\t\t   new String[] \n\t\t   {");
						for (Iterator<Map.Entry<String, String>> k = eAnnotation.getDetails().iterator(); k
								.hasNext();) {
							Map.Entry<String, String> detail = k.next();
							String key = Literals.toStringLiteral(detail.getKey(), genModel);
							String value = Literals.toStringLiteral(detail.getValue(), genModel);
							rtout.write("\n\t\t\t ");
							rtcout.write(key);
							rtout.write(", ");
							rtcout.write(value);
							rtcout.write(k.hasNext() ? "," : "");
							rtcout.write(genModel.getNonNLS(key + value));
						}
						rtout.write("\n\t\t   }");
						if (annotationReferenceDataList.isEmpty()) {
							rtout.write(");");
						} else {
							rtout.write(",");
						}
						if (!annotationReferenceDataList.isEmpty()) {
							rtout.write("\n\t\t   new ");
							rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
							rtout.write("[] \n\t\t   {");
							for (Iterator<GenPackage.AnnotationReferenceData> k = annotationReferenceDataList
									.iterator(); k.hasNext();) {
								GenPackage.AnnotationReferenceData annotationReferenceData = k.next();
								rtout.write("\n\t\t\t ");
								rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
								rtout.write(".createURI(");
								if (annotationReferenceData.containingGenPackage != genPackage) {
									rtcout.write(annotationReferenceData.containingGenPackage
											.getImportedPackageInterfaceName());
									rtout.write(".");
								}
								rtout.write("eNS_URI).appendFragment(\"");
								rtcout.write(annotationReferenceData.uriFragment);
								rtout.write("\")");
								if (k.hasNext()) {
									rtout.write(",");
								}
								rtcout.write(genModel.getNonNLS());
							}
							rtout.write("\n\t\t   });");
						}
						for (EAnnotation nestedEAnnotation : genPackage.getAllNestedAnnotations(eAnnotation)) {
							String nestedAnnotationSource = nestedEAnnotation.getSource();
							int depth = 0;
							boolean nonContentAnnotation = false;
							StringBuilder path = new StringBuilder();
							for (EObject eContainer = nestedEAnnotation
									.eContainer(), child = nestedEAnnotation; child != eAnnotation; child = eContainer, eContainer = eContainer
											.eContainer()) {
								boolean nonContentChild = child
										.eContainmentFeature() != EcorePackage.Literals.EANNOTATION__CONTENTS;
								if (path.length() != 0) {
									path.insert(0, ", ");
								}
								path.insert(0, nonContentChild);
								if (nonContentChild) {
									nonContentAnnotation = true;
								}
								++depth;
							}
							List<GenPackage.AnnotationReferenceData> nestedAnnotationReferenceDataList = genPackage
									.getReferenceData(nestedEAnnotation);
							rtout.write("\n\t\taddAnnotation\n\t\t  (");
							rtcout.write(genPackage.getAnnotatedModelElementAccessor(eAnnotation));
							rtout.write(", \n\t\t   ");
							if (nonContentAnnotation
									&& genModel.getRuntimeVersion().getValue() >= GenRuntimeVersion.EMF210_VALUE) {
								rtout.write("new boolean[] { ");
								rtcout.write(path.toString());
								rtout.write(" }");
							} else {
								rtcout.write(depth);
							}
							rtout.write(",\n\t\t   ");
							if (nestedAnnotationSource == null) {
								rtout.write("null,");
							} else {
								rtout.write("\"");
								rtcout.write(nestedAnnotationSource);
								rtout.write("\",");
								rtcout.write(genModel.getNonNLS());
							}
							rtout.write("\n\t\t   new String[] \n\t\t   {");
							for (Iterator<Map.Entry<String, String>> l = nestedEAnnotation.getDetails().iterator(); l
									.hasNext();) {
								Map.Entry<String, String> detail = l.next();
								String key = Literals.toStringLiteral(detail.getKey(), genModel);
								String value = Literals.toStringLiteral(detail.getValue(), genModel);
								rtout.write("\n\t\t\t ");
								rtcout.write(key);
								rtout.write(", ");
								rtcout.write(value);
								rtcout.write(l.hasNext() ? "," : "");
								rtcout.write(genModel.getNonNLS(key + value));
							}
							rtout.write("\n\t\t   }");
							if (nestedAnnotationReferenceDataList.isEmpty()) {
								rtout.write(");");
							} else {
								rtout.write(",");
							}
							if (!nestedAnnotationReferenceDataList.isEmpty()) {
								rtout.write("\n\t\t   new ");
								rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
								rtout.write("[] \n\t\t   {");
								for (Iterator<GenPackage.AnnotationReferenceData> l = nestedAnnotationReferenceDataList
										.iterator(); l.hasNext();) {
									GenPackage.AnnotationReferenceData annotationReferenceData = l.next();
									rtout.write("\n\t\t\t ");
									rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
									rtout.write(".createURI(");
									if (annotationReferenceData.containingGenPackage != genPackage) {
										rtcout.write(annotationReferenceData.containingGenPackage
												.getImportedPackageInterfaceName());
										rtout.write(".");
									}
									rtout.write("eNS_URI).appendFragment(\"");
									rtcout.write(annotationReferenceData.uriFragment);
									rtout.write("\")");
									if (l.hasNext()) {
										rtout.write(",");
									}
									rtcout.write(genModel.getNonNLS());
								}
								rtout.write("\n\t\t   });");
							}
						}
					}
				}
				rtout.write("\n\t}\n");
			}
		} else {
			if (genPackage.isLoadingInitialization()) {
				rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate boolean isLoaded = false;\n\n\t/**\n\t * Laods the package and any sub-packages from their serialized form.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic void loadPackage()\n\t{\n\t\tif (isLoaded) return;\n\t\tisLoaded = true;\n\n\t\t");
				rtcout.write(genModel.getImportedName("java.net.URL"));
				rtout.write(" url = getClass().getResource(packageFilename);\n\t\tif (url == null)\n\t\t{\n\t\t\tthrow new RuntimeException(\"Missing serialized package: \" + packageFilename);");
				rtcout.write(genModel.getNonNLS());
				rtout.write("\n\t\t}\n\t\t");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
				rtout.write(" uri = ");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.URI"));
				rtout.write(".createURI(url.toString());\n\t\t");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.resource.Resource"));
				rtout.write(" resource = new ");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl"));
				rtout.write("().createResource(uri);\n\t\ttry\n\t\t{\n\t\t\tresource.load(null);\n\t\t}\n\t\tcatch (");
				rtcout.write(genModel.getImportedName("java.io.IOException"));
				rtout.write(" exception)\n\t\t{\n\t\t\tthrow new ");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.common.util.WrappedException"));
				rtout.write("(exception);\n\t\t}\n\t\tinitializeFromLoadedEPackage(this, (");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
				rtout.write(")resource.getContents().get(0));\n\t\tcreateResource(eNS_URI);\n\t}\n");
			}
			rtout.write("\n\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprivate boolean isFixed = false;\n\n\t/**\n\t * Fixes up the loaded package, to make it appear as if it had been programmatically built.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tpublic void fixPackageContents()\n\t{\n\t\tif (isFixed) return;\n\t\tisFixed = true;\n\t\tfixEClassifiers();\n\t}\n\n\t/**\n\t * Sets the instance class on the given classifier.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n");
			if (genModel.useClassOverrideAnnotation()) {
				rtout.write("\n\t@Override");
			}
			rtout.write("\n\tprotected void fixInstanceClass(");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EClassifier"));
			rtout.write(" eClassifier)\n\t{\n\t\tif (eClassifier.getInstanceClassName() == null)\n\t\t{");
			ArrayList<GenClass> dynamicGenClasses = new ArrayList<GenClass>();
			for (GenClass genClass : genPackage.getGenClasses()) {
				if (genClass.isDynamic()) {
					dynamicGenClasses.add(genClass);
				}
			}
			if (dynamicGenClasses.isEmpty()) {
				rtout.write("\n\t\t\teClassifier.setInstanceClassName(\"");
				rtcout.write(genPackage.getInterfacePackageName());
				rtout.write(".\" + eClassifier.getName());");
				rtcout.write(genModel.getNonNLS());
				rtout.write("\n\t\t\tsetGeneratedClassName(eClassifier);");
			} else {
				rtout.write("\n\t\t\tswitch (eClassifier.getClassifierID())\n\t\t\t{");
				for (GenClass genClass : dynamicGenClasses) {
					if (genClass.isDynamic()) {
						rtout.write("\n\t\t\t\tcase ");
						rtcout.write(genPackage.getClassifierID(genClass));
						rtout.write(":");
					}
				}
				rtout.write("\n\t\t\t\t{\n\t\t\t\t\tbreak;\n\t\t\t\t}\n\t\t\t\tdefault:\n\t\t\t\t{\n\t\t\t\t\teClassifier.setInstanceClassName(\"");
				rtcout.write(genPackage.getInterfacePackageName());
				rtout.write(".\" + eClassifier.getName());");
				rtcout.write(genModel.getNonNLS());
				rtout.write("\n\t\t\t\t\tsetGeneratedClassName(eClassifier);\n\t\t\t\t\tbreak;\n\t\t\t\t}\n\t\t\t}");
			}
			rtout.write("\n\t\t}\n\t}\n");
		}
		if (needsAddEOperation) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprotected ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EOperation"));
			rtout.write(" addEOperation(");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EClass"));
			rtout.write(" owner, ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EClassifier"));
			rtout.write(" type, String name, int lowerBound, int upperBound, boolean isUnique, boolean isOrdered)\n\t{\n\t\t");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EOperation"));
			rtout.write(" o = addEOperation(owner, type, name, lowerBound, upperBound);\n\t\to.setUnique(isUnique);\n\t\to.setOrdered(isOrdered);\n\t\treturn o;\n\t}\n\t");
		}
		if (needsAddEParameter) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\tprotected ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EParameter"));
			rtout.write(" addEParameter(");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EOperation"));
			rtout.write(" owner, ");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EClassifier"));
			rtout.write(" type, String name, int lowerBound, int upperBound, boolean isUnique, boolean isOrdered)\n\t{\n\t\t");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EParameter"));
			rtout.write(" p = ecoreFactory.createEParameter();\n\t\tp.setEType(type);\n\t\tp.setName(name);\n\t\tp.setLowerBound(lowerBound);\n\t\tp.setUpperBound(upperBound);\n\t\tp.setUnique(isUnique);\n\t\tp.setOrdered(isOrdered);\n\t\towner.getEParameters().add(p);\n\t\treturn p;\n\t}\n\t");
		}
		rtout.write("\n} //");
		rtcout.write(genPackage.getPackageClassName());
		genModel.emitSortedImports();
	}

	private void generatePackageHelper() {
//		rtout.write("\tprivate ");
//		rtcout.writeClass(Set.class);
//		rtout.write("<String> initedClass = new ");
//		rtcout.writeClass(HashSet.class);
//		rtout.write("<String>();\n\n\t@SuppressWarnings(\"unchecked\")\n\tprotected <T extends ");
//		rtcout.writeClass(EPackage.class);
//		rtout.write("> T getPackage(Class<T> clz,String uri) {\n\t\t");
//		rtcout.writeClass(EPackage.class);
//		rtout.write(" fromReg = ");
//		rtcout.writeClass(EPackage.class);
//		rtout.write(".Registry.INSTANCE.getEPackage(uri);\n\t\tif (fromReg != null && clz.isInstance(fromReg)) {\n\t\t\treturn (T) fromReg;\n\t\t}\n\t\ttry {\n\t\t\treturn (T) clz.getField(\"eINSTANCE\").get(null);\n\t\t} catch (Exception e) {\n\t\t\tthrow new RuntimeException(\"cannot load package \"+uri, e);\n\t\t}\n\t}\n");
		String classname = getGenPackage().getPackageClassName();
		rtout.write("\tprivate ");
		rtcout.writeClass(Set.class);
		rtout.write("<String> initedClass = new ");
		rtcout.writeClass(HashSet.class);
		rtout.write("<String>();\n\n\tpublic static ");
		rtcout.write(classname);
		rtout.write(" partInit() {\n\t\tObject fromReg = ");
		rtcout.writeClass(EPackage.class);
		rtout.write(".Registry.INSTANCE.get(\"");
		rtcout.write(getGenPackage().getNSURI());
		rtout.write("\");\n\t\tif (fromReg instanceof ");
		rtcout.write(classname);
		rtout.write(") {\n\t\t\treturn (");
		rtcout.write(classname);
		rtout.write(") fromReg;\n\t\t}\n\t\t");
		rtcout.write(classname);
		rtout.write(" thePackage = new ");
		rtcout.write(classname);
		rtout.write("();\n\t\t");
		rtcout.writeClass(EPackage.class);
		rtout.write(".Registry.INSTANCE.put(\"");
		rtcout.write(getGenPackage().getNSURI());
		rtout.write("\",thePackage);\n\t\tthePackage.createPackageContents();\n\t\treturn thePackage;\n\t}\n");
	}

	private void generatePackageDependencyInitialization() {
		if (!genPackage.getPackageSimpleDependencies().isEmpty()) {
			rtout.write("\n\t\t// Initialize simple dependencies");
			for (GenPackage dep : genPackage.getPackageSimpleDependencies()) {
				rtout.write("\n\t\t");
				rtcout.write(dep.getImportedPackageInterfaceName());
				rtout.write(".eINSTANCE.eClass();");
			}
			rtout.write("\n");
		}
		if (!genPackage.getPackageInterDependencies().isEmpty()) {
			rtout.write("\n\t\t// Obtain or create and register interdependencies");
			for (GenPackage interdep : genPackage.getPackageInterDependencies()) {
				rtout.write("\n\t\t");
				rtcout.write(interdep.getImportedPackageClassName());
				rtout.write(" ");
				rtcout.write(genPackage.getPackageInstanceVariable(interdep));
				rtout.write(" = (");
				rtcout.write(interdep.getImportedPackageClassName());
				rtout.write(")(");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
				rtout.write(".Registry.INSTANCE.getEPackage(");
				rtcout.write(interdep.getImportedPackageInterfaceName());
				rtout.write(".eNS_URI) instanceof ");
				rtcout.write(interdep.getImportedPackageClassName());
				rtout.write(" ? ");
				rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
				rtout.write(".Registry.INSTANCE.getEPackage(");
				rtcout.write(interdep.getImportedPackageInterfaceName());
				rtout.write(".eNS_URI) : ");
				rtcout.write(interdep.getImportedPackageInterfaceName());
				rtout.write(".eINSTANCE);");
			}
			rtout.write("\n");
		}
		if (genPackage.isLoadedInitialization() || !genPackage.getPackageLoadInterDependencies().isEmpty()) {
			rtout.write("\n\t\t// Load packages");
			if (genPackage.isLoadingInitialization()) {
				rtout.write("\n\t\tthe");
				rtcout.write(genPackage.getBasicPackageName());
				rtout.write(".loadPackage();");
			}
			for (GenPackage interdep : genPackage.getPackageLoadInterDependencies()) {
				if (interdep.isLoadingInitialization()) {
					rtout.write("\n\t\t");
					rtcout.write(genPackage.getPackageInstanceVariable(interdep));
					rtout.write(".loadPackage();");
				}
			}
			rtout.write("\n");
		}
		if (!genPackage.isLoadedInitialization() || !genPackage.getPackageBuildInterDependencies().isEmpty()) {
			rtout.write("\n\t\t// Create package meta-data objects");
			if (!genPackage.isLoadedInitialization()) {
				rtout.write("\n\t\tthe");
				rtcout.write(genPackage.getBasicPackageName());
				rtout.write(".createPackageContents();");
			}
			for (GenPackage interdep : genPackage.getPackageBuildInterDependencies()) {
				rtout.write("\n\t\t");
				rtcout.write(genPackage.getPackageInstanceVariable(interdep));
				rtout.write(".createPackageContents();");
			}
			rtout.write("\n\n\t\t// Initialize created meta-data");
			if (!genPackage.isLoadedInitialization()) {
				rtout.write("\n\t\tthe");
				rtcout.write(genPackage.getBasicPackageName());
				rtout.write(".initializePackageContents();");
			}
			for (GenPackage interdep : genPackage.getPackageBuildInterDependencies()) {
				rtout.write("\n\t\t");
				rtcout.write(genPackage.getPackageInstanceVariable(interdep));
				rtout.write(".initializePackageContents();");
			}
			rtout.write("\n");
		}
		if (genPackage.isLoadedInitialization() || !genPackage.getPackageLoadInterDependencies().isEmpty()) {
			rtout.write("\n\t\t// Fix loaded packages");
			if (genPackage.isLoadedInitialization()) {
				rtout.write("\n\t\tthe");
				rtcout.write(genPackage.getBasicPackageName());
				rtout.write(".fixPackageContents();");
			}
			for (GenPackage interdep : genPackage.getPackageLoadInterDependencies()) {
				rtout.write("\n\t\t");
				rtcout.write(genPackage.getPackageInstanceVariable(interdep));
				rtout.write(".fixPackageContents();");
			}
			rtout.write("\n");
		}
	}

	private void generateClassInitCode(GenClass genClass) {
		boolean hasInstanceTypeName = genModel.useGenerics() && genClass.getEcoreClass().getInstanceTypeName() != null
				&& genClass.getEcoreClass().getInstanceTypeName().contains("<");
		rtout.write("\n\t\tinitEClass(");
		rtcout.write(genClass.getClassifierInstanceName());
		rtout.write(", ");
		if (genClass.isDynamic()) {
			rtout.write("null");
		} else {
			rtcout.write(genClass.getRawImportedInterfaceName());
			rtout.write(".class");
		}
		rtout.write(", \"");
		rtcout.write(genClass.getName());
		rtout.write("\", ");
		rtcout.write(genClass.getAbstractFlag());
		rtout.write(", ");
		rtcout.write(genClass.getInterfaceFlag());
		rtout.write(", ");
		rtcout.write(genClass.getGeneratedInstanceClassFlag());
		if (hasInstanceTypeName) {
			rtout.write(", \"");
			rtcout.write(genClass.getEcoreClass().getInstanceTypeName());
			rtout.write("\"");
		}
		rtout.write(");");
		rtcout.write(genModel.getNonNLS());
		if (hasInstanceTypeName) {
			rtcout.write(genModel.getNonNLS(2));
		}
		for (GenFeature genFeature : genClass.getGenFeatures()) {
			if (genFeature.hasGenericType()) {
				for (InformationIterator i = new InformationIterator(genFeature.getEcoreFeature().getEGenericType()); i
						.hasNext();) {
					Information info = i.next();
					String prefix = "";
					if (maxGenericTypeAssignment <= info.depth) {
						++maxGenericTypeAssignment;
						prefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
					}
					rtout.write("\n\t\t");
					rtcout.write(prefix);
					rtout.write("g");
					rtcout.write(info.depth + 1);
					rtout.write(" = createEGenericType(");
					rtcout.write(info.type);
					rtout.write(");");
					if (info.depth > 0) {
						rtout.write("\n\t\tg");
						rtcout.write(info.depth);
						rtout.write(".");
						rtcout.write(info.accessor);
						rtout.write("(g");
						rtcout.write(info.depth + 1);
						rtout.write(");");
					}
				}
			}
			if (genFeature.isReferenceType()) {
				GenFeature reverseGenFeature = genFeature.getReverse();
				String reverse = reverseGenFeature == null ? "null"
						: getDependentPackageAccessor(reverseGenFeature.getGenPackage()) + ".get"
								+ reverseGenFeature.getFeatureAccessorName() + "()";
				rtout.write("\n\t\tinitEReference(get");
				rtcout.write(genFeature.getFeatureAccessorName());
				rtout.write("(), ");
				if (genFeature.hasGenericType()) {
					rtout.write("g1");
				} else {
					rtcout.write(getDependentPackageAccessor(genFeature.getTypeGenPackage()));
					rtout.write(".get");
					rtcout.write(genFeature.getTypeClassifierAccessorName());
					rtout.write("()");
				}
				rtout.write(", ");
				rtcout.write(reverse);
				rtout.write(", \"");
				rtcout.write(genFeature.getName());
				rtout.write("\", ");
				rtcout.write(genFeature.getDefaultValue());
				rtout.write(", ");
				rtcout.write(genFeature.getLowerBound());
				rtout.write(", ");
				rtcout.write(genFeature.getUpperBound());
				rtout.write(", ");
				rtcout.write(genFeature.getContainerClass());
				rtout.write(", ");
				rtcout.write(genFeature.getTransientFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getVolatileFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getChangeableFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getContainmentFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getResolveProxiesFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getUnsettableFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getUniqueFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getDerivedFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getOrderedFlag());
				rtout.write(");");
				rtcout.write(genModel.getNonNLS());
				rtcout.write(genModel.getNonNLS(genFeature.getDefaultValue(), 2));
				for (GenFeature keyFeature : genFeature.getKeys()) {
					rtout.write("\n\t\tget");
					rtcout.write(genFeature.getFeatureAccessorName());
					rtout.write("().getEKeys().add(");
					rtcout.write(getDependentPackageAccessor(keyFeature.getGenPackage()));
					rtout.write(".get");
					rtcout.write(keyFeature.getFeatureAccessorName());
					rtout.write("());");
				}
			} else {
				rtout.write("\n\t\tinitEAttribute(get");
				rtcout.write(genFeature.getFeatureAccessorName());
				rtout.write("(), ");
				if (genFeature.hasGenericType()) {
					rtout.write("g1");
				} else {
					rtcout.write(getDependentPackageAccessor(genFeature.getTypeGenPackage()));
					rtout.write(".get");
					rtcout.write(genFeature.getTypeClassifierAccessorName());
					rtout.write("()");
				}
				rtout.write(", \"");
				rtcout.write(genFeature.getName());
				rtout.write("\", ");
				rtcout.write(genFeature.getDefaultValue());
				rtout.write(", ");
				rtcout.write(genFeature.getLowerBound());
				rtout.write(", ");
				rtcout.write(genFeature.getUpperBound());
				rtout.write(", ");
				rtcout.write(genFeature.getContainerClass());
				rtout.write(", ");
				rtcout.write(genFeature.getTransientFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getVolatileFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getChangeableFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getUnsettableFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getIDFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getUniqueFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getDerivedFlag());
				rtout.write(", ");
				rtcout.write(genFeature.getOrderedFlag());
				rtout.write(");");
				rtcout.write(genModel.getNonNLS());
				rtcout.write(genModel.getNonNLS(genFeature.getDefaultValue(), 2));
			}
		}
		for (GenOperation genOperation : genClass.getGenOperations()) {
			String prefix = "";
			if (genOperation.hasGenericType() || !genOperation.getGenParameters().isEmpty()
					|| !genOperation.getGenExceptions().isEmpty() || !genOperation.getGenTypeParameters().isEmpty()) {
				if (firstOperationAssignment) {
					firstOperationAssignment = false;
					prefix = genModel.getImportedName("org.eclipse.emf.ecore.EOperation") + " op = ";
				} else {
					prefix = "op = ";
				}
			}
			rtout.write("\n");
			if (genModel.useGenerics()) {
				rtout.write("\n\t\t");
				rtcout.write(prefix);
				if (genModel.isOperationReflection()) {
					rtout.write("initEOperation(get");
					rtcout.write(genOperation.getOperationAccessorName());
					rtout.write("()");
				} else {
					rtout.write("addEOperation(");
					rtcout.write(genClass.getClassifierInstanceName());
				}
				rtout.write(", ");
				if (genOperation.isVoid() || genOperation.hasGenericType()) {
					rtout.write("null");
				} else {
					rtcout.write(getDependentPackageAccessor(genOperation.getTypeGenPackage()));
					rtout.write(".get");
					rtcout.write(genOperation.getTypeClassifierAccessorName());
					rtout.write("()");
				}
				rtout.write(", \"");
				rtcout.write(genOperation.getName());
				rtout.write("\", ");
				rtcout.write(genOperation.getLowerBound());
				rtout.write(", ");
				rtcout.write(genOperation.getUpperBound());
				rtout.write(", ");
				rtcout.write(genOperation.getUniqueFlag());
				rtout.write(", ");
				rtcout.write(genOperation.getOrderedFlag());
				rtout.write(");");
				rtcout.write(genModel.getNonNLS());
			} else if (!genOperation.isVoid()) {
				if (!genOperation.getEcoreOperation().isOrdered() || !genOperation.getEcoreOperation().isUnique()) {
					needsAddEOperation = true;
					rtout.write("\n\t\t");
					rtcout.write(prefix);
					if (genModel.isOperationReflection()) {
						rtout.write("initEOperation(get");
						rtcout.write(genOperation.getOperationAccessorName());
						rtout.write("()");
					} else {
						rtout.write("addEOperation(");
						rtcout.write(genClass.getClassifierInstanceName());
					}
					rtout.write(", ");
					rtcout.write(getDependentPackageAccessor(genOperation.getTypeGenPackage()));
					rtout.write(".get");
					rtcout.write(genOperation.getTypeClassifierAccessorName());
					rtout.write("(), \"");
					rtcout.write(genOperation.getName());
					rtout.write("\", ");
					rtcout.write(genOperation.getLowerBound());
					rtout.write(", ");
					rtcout.write(genOperation.getUpperBound());
					rtout.write(", ");
					rtcout.write(genOperation.getUniqueFlag());
					rtout.write(", ");
					rtcout.write(genOperation.getOrderedFlag());
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
				} else {
					rtout.write("\n\t\t");
					rtcout.write(prefix);
					if (genModel.isOperationReflection()) {
						rtout.write("initEOperation(get");
						rtcout.write(genOperation.getOperationAccessorName());
						rtout.write("()");
					} else {
						rtout.write("addEOperation(");
						rtcout.write(genClass.getClassifierInstanceName());
					}
					rtout.write(", ");
					rtcout.write(getDependentPackageAccessor(genOperation.getTypeGenPackage()));
					rtout.write(".get");
					rtcout.write(genOperation.getTypeClassifierAccessorName());
					rtout.write("(), \"");
					rtcout.write(genOperation.getName());
					rtout.write("\", ");
					rtcout.write(genOperation.getLowerBound());
					rtout.write(", ");
					rtcout.write(genOperation.getUpperBound());
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
				}
			} else {
				rtout.write("\n\t\t");
				rtcout.write(prefix);
				if (genModel.isOperationReflection()) {
					rtout.write("initEOperation(get");
					rtcout.write(genOperation.getOperationAccessorName());
					rtout.write("()");
				} else {
					rtout.write("addEOperation(");
					rtcout.write(genClass.getClassifierInstanceName());
				}
				rtout.write(", null, \"");
				rtcout.write(genOperation.getName());
				rtout.write("\");");
				rtcout.write(genModel.getNonNLS());
			}
			if (genModel.useGenerics()) {
				for (ListIterator<GenTypeParameter> t = genOperation.getGenTypeParameters().listIterator(); t
						.hasNext();) {
					GenTypeParameter genTypeParameter = t.next();
					String typeParameterVariable = "";
					if (!genTypeParameter.getEcoreTypeParameter().getEBounds().isEmpty() || genTypeParameter.isUsed()) {
						if (maxTypeParameterAssignment <= t.previousIndex()) {
							++maxTypeParameterAssignment;
							typeParameterVariable = genModel.getImportedName("org.eclipse.emf.ecore.ETypeParameter")
									+ " t" + t.nextIndex() + " = ";
						} else {
							typeParameterVariable = "t" + t.nextIndex() + " = ";
						}
					}
					rtout.write("\n\t\t");
					rtcout.write(typeParameterVariable);
					rtout.write("addETypeParameter(op, \"");
					rtcout.write(genTypeParameter.getName());
					rtout.write("\");");
					rtcout.write(genModel.getNonNLS());
					for (EGenericType typeParameter : genTypeParameter.getEcoreTypeParameter().getEBounds()) {
						for (InformationIterator i = new InformationIterator(typeParameter); i.hasNext();) {
							Information info = i.next();
							String typePrefix = "";
							if (maxGenericTypeAssignment <= info.depth) {
								++maxGenericTypeAssignment;
								typePrefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
							}
							rtout.write("\n\t\t");
							rtcout.write(typePrefix);
							rtout.write("g");
							rtcout.write(info.depth + 1);
							rtout.write(" = createEGenericType(");
							rtcout.write(info.type);
							rtout.write(");");
							if (info.depth > 0) {
								rtout.write("\n\t\tg");
								rtcout.write(info.depth);
								rtout.write(".");
								rtcout.write(info.accessor);
								rtout.write("(g");
								rtcout.write(info.depth + 1);
								rtout.write(");");
							}
						}
						rtout.write("\n\t\tt");
						rtcout.write(t.nextIndex());
						rtout.write(".getEBounds().add(g1);");
					}
				}
			}
			for (GenParameter genParameter : genOperation.getGenParameters()) {
				if (genParameter.hasGenericType()) {
					for (InformationIterator i = new InformationIterator(
							genParameter.getEcoreParameter().getEGenericType()); i.hasNext();) {
						Information info = i.next();
						String typePrefix = "";
						if (maxGenericTypeAssignment <= info.depth) {
							++maxGenericTypeAssignment;
							typePrefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
						}
						rtout.write("\n\t\t");
						rtcout.write(typePrefix);
						rtout.write("g");
						rtcout.write(info.depth + 1);
						rtout.write(" = createEGenericType(");
						rtcout.write(info.type);
						rtout.write(");");
						if (info.depth > 0) {
							rtout.write("\n\t\tg");
							rtcout.write(info.depth);
							rtout.write(".");
							rtcout.write(info.accessor);
							rtout.write("(g");
							rtcout.write(info.depth + 1);
							rtout.write(");");
						}
					}
				}
				if (genModel.useGenerics()) {
					rtout.write("\n\t\taddEParameter(op, ");
					if (genParameter.hasGenericType()) {
						rtout.write("g1");
					} else {
						rtcout.write(genPackage.getPackageInstanceVariable(genParameter.getTypeGenPackage()));
						rtout.write(".get");
						rtcout.write(genParameter.getTypeClassifierAccessorName());
						rtout.write("()");
					}
					rtout.write(", \"");
					rtcout.write(genParameter.getName());
					rtout.write("\", ");
					rtcout.write(genParameter.getLowerBound());
					rtout.write(", ");
					rtcout.write(genParameter.getUpperBound());
					rtout.write(", ");
					rtcout.write(genParameter.getUniqueFlag());
					rtout.write(", ");
					rtcout.write(genParameter.getOrderedFlag());
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
				} else if (!genParameter.getEcoreParameter().isOrdered()
						|| !genParameter.getEcoreParameter().isUnique()) {
					needsAddEParameter = true;
					rtout.write("\n\t\taddEParameter(op, ");
					if (genParameter.hasGenericType()) {
						rtout.write("g1");
					} else {
						rtcout.write(genPackage.getPackageInstanceVariable(genParameter.getTypeGenPackage()));
						rtout.write(".get");
						rtcout.write(genParameter.getTypeClassifierAccessorName());
						rtout.write("()");
					}
					rtout.write(", \"");
					rtcout.write(genParameter.getName());
					rtout.write("\", ");
					rtcout.write(genParameter.getLowerBound());
					rtout.write(", ");
					rtcout.write(genParameter.getUpperBound());
					rtout.write(", ");
					rtcout.write(genParameter.getUniqueFlag());
					rtout.write(", ");
					rtcout.write(genParameter.getOrderedFlag());
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
				} else {
					rtout.write("\n\t\taddEParameter(op, ");
					if (genParameter.hasGenericType()) {
						rtout.write("g1");
					} else {
						rtcout.write(genPackage.getPackageInstanceVariable(genParameter.getTypeGenPackage()));
						rtout.write(".get");
						rtcout.write(genParameter.getTypeClassifierAccessorName());
						rtout.write("()");
					}
					rtout.write(", \"");
					rtcout.write(genParameter.getName());
					rtout.write("\", ");
					rtcout.write(genParameter.getLowerBound());
					rtout.write(", ");
					rtcout.write(genParameter.getUpperBound());
					rtout.write(");");
					rtcout.write(genModel.getNonNLS());
				}
			}
			if (genOperation.hasGenericExceptions()) {
				for (EGenericType genericExceptions : genOperation.getEcoreOperation().getEGenericExceptions()) {
					for (InformationIterator i = new InformationIterator(genericExceptions); i.hasNext();) {
						Information info = i.next();
						String typePrefix = "";
						if (maxGenericTypeAssignment <= info.depth) {
							++maxGenericTypeAssignment;
							typePrefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
						}
						rtout.write("\n\t\t");
						rtcout.write(typePrefix);
						rtout.write("g");
						rtcout.write(info.depth + 1);
						rtout.write(" = createEGenericType(");
						rtcout.write(info.type);
						rtout.write(");");
						if (info.depth > 0) {
							rtout.write("\n\t\tg");
							rtcout.write(info.depth);
							rtout.write(".");
							rtcout.write(info.accessor);
							rtout.write("(g");
							rtcout.write(info.depth + 1);
							rtout.write(");");
						}
						rtout.write("\n\t\taddEException(op, g");
						rtcout.write(info.depth + 1);
						rtout.write(");");
					}
				}
			} else {
				for (GenClassifier genException : genOperation.getGenExceptions()) {
					rtout.write("\n\t\taddEException(op, ");
					rtcout.write(getDependentPackageAccessor(genException.getGenPackage()));
					rtout.write(".get");
					rtcout.write(genException.getClassifierAccessorName());
					rtout.write("());");
				}
			}
			if (!genOperation.isVoid() && genOperation.hasGenericType()) {
				for (InformationIterator i = new InformationIterator(
						genOperation.getEcoreOperation().getEGenericType()); i.hasNext();) {
					Information info = i.next();
					String typePrefix = "";
					if (maxGenericTypeAssignment <= info.depth) {
						++maxGenericTypeAssignment;
						typePrefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
					}
					rtout.write("\n\t\t");
					rtcout.write(typePrefix);
					rtout.write("g");
					rtcout.write(info.depth + 1);
					rtout.write(" = createEGenericType(");
					rtcout.write(info.type);
					rtout.write(");");
					if (info.depth > 0) {
						rtout.write("\n\t\tg");
						rtcout.write(info.depth);
						rtout.write(".");
						rtcout.write(info.accessor);
						rtout.write("(g");
						rtcout.write(info.depth + 1);
						rtout.write(");");
					}
				}
				rtout.write("\n\t\tinitEOperation(op, g1);");
			}
		}
		rtout.write("\n");
	}

	private String getDependentPackageAccessor(GenPackage dep) {
		// this call adds the necessary Java import of dep's Java interface
		if (dep.equals(genPackage)) {
			return "this";
		} else if (dep.isEcorePackage()) {
			return dep.getImportedPackageInterfaceName() + ".eINSTANCE";
		} else {
			// String fromReg = addImport(EPackage.class)+
			// ".Registry.INSTANCE.get(\""+dep.getNSURI()+"\")";
//			return "getPackage(" + dep.getPackageInterfaceName() + ".class," + dep.getPackageInterfaceName()
//					+ ".eNS_URI)";
			return dep.getImportedPackageClassName()+".partInit()";

		}
	}

	private void generateClassSuperTypeInit(GenClass genClass) {
		if (!genClass.hasGenericSuperTypes()) {
			for (GenClass baseGenClass : genClass.getBaseGenClasses()) {
				rtout.write("\n\t\t");
				rtcout.write(genClass.getClassifierInstanceName());
				rtout.write(".getESuperTypes().add(");
				rtcout.write(getDependentPackageAccessor(baseGenClass.getGenPackage()));
				rtout.write(".get");
				rtcout.write(baseGenClass.getClassifierAccessorName());
				rtout.write("());");
			}
		} else {
			for (EGenericType superType : genClass.getEcoreClass().getEGenericSuperTypes()) {
				for (InformationIterator i = new InformationIterator(superType); i.hasNext();) {
					Information info = i.next();
					String prefix = "";
					if (maxGenericTypeAssignment <= info.depth) {
						++maxGenericTypeAssignment;
						prefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
					}
					rtout.write("\n\t\t");
					rtcout.write(prefix);
					rtout.write("g");
					rtcout.write(info.depth + 1);
					rtout.write(" = createEGenericType(");
					rtcout.write(info.type);
					rtout.write(");");
					if (info.depth > 0) {
						rtout.write("\n\t\tg");
						rtcout.write(info.depth);
						rtout.write(".");
						rtcout.write(info.accessor);
						rtout.write("(g");
						rtcout.write(info.depth + 1);
						rtout.write(");");
					}
				}
				rtout.write("\n\t\t");
				rtcout.write(genClass.getClassifierInstanceName());
				rtout.write(".getEGenericSuperTypes().add(g1);");
			}
		}
	}

	private void generateFeatureGetter(final GenPackage genPackage, GenClassifier genClassifier, GenClass genClass,
			GenFeature genFeature) {
		rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * Customized feature getter comes here\n\t * @generated\n\t */\n\n\tpublic ");
		rtcout.write(genFeature.getImportedMetaType());
		rtout.write(" get");
		rtcout.write(genFeature.getFeatureAccessorName());
		rtout.write("()\n\t{");
		if (!genPackage.isLoadedInitialization()) {
			rtout.write("\n\t\treturn (");
			rtcout.write(genFeature.getImportedMetaType());
			rtout.write(")");
			rtcout.write(genClass.getClassifierInstanceName());
			rtout.write(".getEStructuralFeatures().get(");
			rtcout.write(genClass.getLocalFeatureIndex(genFeature));
			rtout.write(");\n");
		} else {
			rtout.write("        return (");
			rtcout.write(genFeature.getImportedMetaType());
			rtout.write(")get");
			rtcout.write(genClassifier.getClassifierAccessorName());
			rtout.write("().getEStructuralFeatures().get(");
			rtcout.write(genClass.getLocalFeatureIndex(genFeature));
			rtout.write(");");
		}
		rtout.write("\n\t}\n");
	}

	private void generateClassifierGetter(final GenPackage genPackage, final GenModel genModel,
			GenClassifier genClassifier) {
		rtout.write("\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t *\n\t * Customized classifier getter comes here\n\t * @generated\n\t */\n\n\tpublic ");
		rtcout.write(genClassifier.getImportedMetaType());
		rtout.write(" get");
		rtcout.write(genClassifier.getClassifierAccessorName());
		rtout.write("()\n\t{");
		if (genPackage.isLoadedInitialization()) {
			rtout.write("\n\t\tif (");
			rtcout.write(genClassifier.getClassifierInstanceName());
			rtout.write(" == null)\n\t\t{\n\t\t\t");
			rtcout.write(genClassifier.getClassifierInstanceName());
			rtout.write(" = (");
			rtcout.write(genClassifier.getImportedMetaType());
			rtout.write(")");
			rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
			rtout.write(".Registry.INSTANCE.getEPackage(");
			rtcout.write(genPackage.getImportedPackageInterfaceName());
			rtout.write(".eNS_URI).getEClassifiers().get(");
			rtcout.write(genPackage.getLocalClassifierIndex(genClassifier));
			rtout.write(");\n\t\t}");
		}
		rtout.write("\n");
		if (genClassifier instanceof GenClass) {
			maxGenericTypeAssignment = 0;
			GenClass genClass = (GenClass) genClassifier;
			rtout.write("\t\tif (initedClass.add(\"");
			rtcout.write(genClass.getName());
			rtout.write("\")) {\n");
			generateClassGenericParametersInit(genClassifier);
			generateClassSuperTypeInit(genClass);
			generateClassInitCode(genClass);
			rtout.write("\t\t}\n");
		}
			rtout.write("\t\treturn ");
			rtcout.write(genClassifier.getClassifierInstanceName());
			rtout.write(";\n\t}\n");
	}

	private void generateClassGenericParametersInit(GenClassifier genClassifier) {
		if (genModel.useGenerics()) {
			rtout.write("\n\n\t\t// Create type parameters\n");
			for (GenTypeParameter genTypeParameter : genClassifier.getGenTypeParameters()) {
				if (!genTypeParameter.getEcoreTypeParameter().getEBounds().isEmpty()
						|| genTypeParameter.isUsed()) {
					rtout.write("\n\t\t");
					rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.ETypeParameter"));
					rtout.write(" ");
					rtcout.write(genClassifier.getClassifierInstanceName());
					rtout.write("_");
					rtcout.write(genTypeParameter.getName());
					rtout.write(" = addETypeParameter(");
					rtcout.write(genClassifier.getClassifierInstanceName());
					rtout.write(", \"");
					rtcout.write(genTypeParameter.getName());
					rtout.write("\");");
					rtcout.write(genModel.getNonNLS());
				} else {
					rtout.write("\n\t\taddETypeParameter(");
					rtcout.write(genClassifier.getClassifierInstanceName());
					rtout.write(", \"");
					rtcout.write(genTypeParameter.getName());
					rtout.write("\");");
					rtcout.write(genModel.getNonNLS());
				}
			}
		}
		if (genModel.useGenerics()) {
			rtout.write("\n\n\t\t// Set bounds for type parameters");
			for (GenTypeParameter genTypeParameter : genClassifier.getGenTypeParameters()) {
				for (EGenericType bound : genTypeParameter.getEcoreTypeParameter().getEBounds()) {
					for (InformationIterator i = new InformationIterator(bound); i.hasNext();) {
						Information info = i.next();
						String prefix = "";
						if (maxGenericTypeAssignment <= info.depth) {
							++maxGenericTypeAssignment;
							prefix = genModel.getImportedName("org.eclipse.emf.ecore.EGenericType") + " ";
						}
						rtout.write("\n\t\t");
						rtcout.write(prefix);
						rtout.write("g");
						rtcout.write(info.depth + 1);
						rtout.write(" = createEGenericType(");
						rtcout.write(info.type);
						rtout.write(");");
						if (info.depth > 0) {
							rtout.write("\n\t\tg");
							rtcout.write(info.depth);
							rtout.write(".");
							rtcout.write(info.accessor);
							rtout.write("(g");
							rtcout.write(info.depth + 1);
							rtout.write(");");
						}
					}
					rtout.write("\n\t\t");
					rtcout.write(genClassifier.getClassifierInstanceName());
					rtout.write("_");
					rtcout.write(genTypeParameter.getName());
					rtout.write(".getEBounds().add(g1);");
				}
			}
		}
		rtout.write("\n");// TODO Auto-generated method stub
	}
}
