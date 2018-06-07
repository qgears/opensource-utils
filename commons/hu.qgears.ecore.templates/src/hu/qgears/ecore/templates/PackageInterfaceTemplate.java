package hu.qgears.ecore.templates;

import java.util.StringTokenizer;

import org.eclipse.emf.codegen.ecore.genmodel.GenBase;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
import org.eclipse.emf.codegen.ecore.genmodel.GenDataType;
import org.eclipse.emf.codegen.ecore.genmodel.GenEnum;
import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenOperation;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;

/**
 * EPackage interface template. No modifications are done compared to original
 * template, unless converting JET template to RTemplate, and remove template
 * codes that are executed only on EPackage implementation classes.
 * 
 * @author agostoni
 *
 */
public class PackageInterfaceTemplate extends PackageTemplate {

	public PackageInterfaceTemplate(GenPackage genPackage) {
		super(genPackage);
	}

	@Override
	protected void doGenerate() {
		final GenPackage genPackage = getGenPackage();
		final GenModel genModel = genPackage.getGenModel();
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
		rtout.write("\npackage ");
		rtcout.write(genPackage.getReflectionPackageName());
		rtout.write(";\n/*Ez biza m\u00E1r rTemplate az interfaceben is*/\n");
		rtout.write("\n");
		genModel.markImportLocation(stringBuffer, genPackage);

		rtout.write("\n\n/**\n * <!-- begin-user-doc -->\n * The <b>Package</b> for the model. Customized by Q-Gears, de nagyon\n * It contains accessors for the meta objects to represent\n * <ul>\n *   <li>each class,</li>\n *   <li>each feature of each class,</li>");
		if (genModel.isOperationReflection()) {
			rtout.write("\n *   <li>each operation of each class,</li>");
		}
		rtout.write("\n *   <li>each enum,</li>\n *   <li>and each data type</li>\n * </ul>\n * <!-- end-user-doc -->");
		if (genPackage.hasDocumentation()) {
			rtout.write("\n * <!-- begin-model-doc -->\n * ");
			rtcout.write(genPackage.getDocumentation(genModel.getIndentation(stringBuffer)));
			rtout.write("\n * <!-- end-model-doc -->");
		}
		rtout.write("\n * @see ");
		rtcout.write(genPackage.getQualifiedFactoryInterfaceName());
		if (!genModel.isSuppressEMFModelTags()) {
			boolean first = true;
			for (StringTokenizer stringTokenizer = new StringTokenizer(genPackage.getModelInfo(),
					"\n\r"); stringTokenizer.hasMoreTokens();) {
				String modelInfo = stringTokenizer.nextToken();
				if (first) {
					first = false;
					rtout.write("\n * @model ");
					rtcout.write(modelInfo);
				} else {
					rtout.write("\n *        ");
					rtcout.write(modelInfo);
				}
			}
			if (first) {
				rtout.write("\n * @model");
			}
		}
		rtout.write("\n * @generated\n */\n");

		rtout.write("\npublic interface ");
		rtcout.write(genPackage.getPackageInterfaceName());
		rtout.write(" extends ");
		rtcout.write(genModel.getImportedName("org.eclipse.emf.ecore.EPackage"));
		rtout.write("\n{");
		if (genModel.hasCopyrightField()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
			rtcout.write(genModel.getImportedName("java.lang.String"));
			rtout.write(" copyright = ");
			rtcout.write(genModel.getCopyrightFieldLiteral());
			rtout.write(";");
			rtcout.write(genModel.getNonNLS());
			rtout.write("\n");
		}
		rtout.write("\n\t/**\n\t * The package name.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
		rtcout.write(genModel.getImportedName("java.lang.String"));
		rtout.write(" eNAME = \"");
		rtcout.write(genPackage.getPackageName());
		rtout.write("\";");
		rtcout.write(genModel.getNonNLS());
		rtout.write("\n\n\t/**\n\t * The package namespace URI.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
		rtcout.write(genModel.getImportedName("java.lang.String"));
		rtout.write(" eNS_URI = \"");
		rtcout.write(genPackage.getNSURI());
		rtout.write("\";");
		rtcout.write(genModel.getNonNLS());
		rtout.write("\n\n\t/**\n\t * The package namespace name.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
		rtcout.write(genModel.getImportedName("java.lang.String"));
		rtout.write(" eNS_PREFIX = \"");
		rtcout.write(genPackage.getNSName());
		rtout.write("\";");
		rtcout.write(genModel.getNonNLS());
		if (genPackage.isContentType()) {
			rtout.write("\n\n\t/**\n\t * The package content type ID.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
			rtcout.write(genModel.getImportedName("java.lang.String"));
			rtout.write(" eCONTENT_TYPE = \"");
			rtcout.write(genPackage.getContentTypeIdentifier());
			rtout.write("\";");
			rtcout.write(genModel.getNonNLS());
		}
		rtout.write("\n\n\t/**\n\t * The singleton instance of the package.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
		rtcout.write(genPackage.getPackageInterfaceName());
		rtout.write(" eINSTANCE = ");
		rtcout.write(genPackage.getQualifiedPackageClassName());
		rtout.write(".init();\n");
		for (GenClassifier genClassifier : genPackage.getOrderedGenClassifiers()) {
			rtout.write("\n\t/**");
			if (genClassifier instanceof GenClass) {
				GenClass genClass = (GenClass) genClassifier;
				if (!genClass.isInterface()) {
					rtout.write("\n\t * The meta object id for the '{@link ");
					rtcout.write(genClass.getQualifiedClassName());
					rtout.write(" <em>");
					rtcout.write(genClass.getFormattedName());
					rtout.write("</em>}' class.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @see ");
					rtcout.write(genClass.getQualifiedClassName());
				} else {
					rtout.write("\n\t * The meta object id for the '{@link ");
					rtcout.write(genClass.getQualifiedInterfaceName());
					rtout.write(" <em>");
					rtcout.write(genClass.getFormattedName());
					rtout.write("</em>}' class.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @see ");
					rtcout.write(genClass.getQualifiedInterfaceName());
				}
			} else if (genClassifier instanceof GenEnum) {
				GenEnum genEnum = (GenEnum) genClassifier;
				rtout.write("\n\t * The meta object id for the '{@link ");
				rtcout.write(genEnum.getQualifiedName());
				rtout.write(" <em>");
				rtcout.write(genEnum.getFormattedName());
				rtout.write("</em>}' enum.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @see ");
				rtcout.write(genEnum.getQualifiedName());
			} else if (genClassifier instanceof GenDataType) {
				GenDataType genDataType = (GenDataType) genClassifier;
				rtout.write("\n\t * The meta object id for the '<em>");
				rtcout.write(genDataType.getFormattedName());
				rtout.write("</em>' data type.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->");
				if (!genDataType.isPrimitiveType() && !genDataType.isArrayType()) {
					rtout.write("\n\t * @see ");
					rtcout.write(genDataType.getRawInstanceClassName());
				}
			}
			rtout.write("\n\t * @see ");
			rtcout.write(genPackage.getQualifiedPackageClassName());
			rtout.write("#get");
			rtcout.write(genClassifier.getClassifierAccessorName());
			rtout.write("()\n\t * @generated\n\t */\n\t");
			rtout.write("int ");
			rtcout.write(genPackage.getClassifierID(genClassifier));
			rtout.write(" = ");
			rtcout.write(genPackage.getClassifierValue(genClassifier));
			rtout.write(";\n");
			if (genClassifier instanceof GenClass) {
				GenClass genClass = (GenClass) genClassifier;
				for (GenFeature genFeature : genClass.getAllGenFeatures()) {
					rtout.write("\n\t/**\n\t * The feature id for the '<em><b>");
					rtcout.write(genFeature.getFormattedName());
					rtout.write("</b></em>' ");
					rtcout.write(genFeature.getFeatureKind());
					rtout.write(".\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t * @ordered\n\t */\n\t");
					rtout.write("int ");
					rtcout.write(genClass.getFeatureID(genFeature));
					rtout.write(" = ");
					rtcout.write(genClass.getFeatureValue(genFeature));
					rtout.write(";\n");
				}
				rtout.write("\n\t/**\n\t * The number of structural features of the '<em>");
				rtcout.write(genClass.getFormattedName());
				rtout.write("</em>' class.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t * @ordered\n\t */\n\t");
				rtout.write("int ");
				rtcout.write(genClass.getFeatureCountID());
				rtout.write(" = ");
				rtcout.write(genClass.getFeatureCountValue());
				rtout.write(";\n");
				if (genModel.isOperationReflection()) {
					for (GenOperation genOperation : genClass.getAllGenOperations(false)) {
						if (genClass.getOverrideGenOperation(genOperation) == null) {
							rtout.write("\n\t/**\n\t * The operation id for the '<em>");
							rtcout.write(genOperation.getFormattedName());
							rtout.write("</em>' operation.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t * @ordered\n\t */\n\t");
							rtout.write("int ");
							rtcout.write(genClass.getOperationID(genOperation, false));
							rtout.write(" = ");
							rtcout.write(genClass.getOperationValue(genOperation));
							rtout.write(";\n");
						}
					}
					rtout.write("\n\t/**\n\t * The number of operations of the '<em>");
					rtcout.write(genClass.getFormattedName());
					rtout.write("</em>' class.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @generated\n\t * @ordered\n\t */\n\t");
					rtout.write("int ");
					rtcout.write(genClass.getOperationCountID());
					rtout.write(" = ");
					rtcout.write(genClass.getOperationCountValue());
					rtout.write(";\n");
				}
			}
		}

		for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
			rtout.write("\n\t/**");
			if (genClassifier instanceof GenClass) {
				GenClass genClass = (GenClass) genClassifier;
				rtout.write("\n\t * Returns the meta object for class '{@link ");
				rtcout.write(genClass.getQualifiedInterfaceName());
				rtout.write(" <em>");
				rtcout.write(genClass.getFormattedName());
				rtout.write("</em>}'.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the meta object for class '<em>");
				rtcout.write(genClass.getFormattedName());
				rtout.write("</em>'.\n\t * @see ");
				rtcout.write(genClass.getQualifiedInterfaceName());
				if (!genModel.isSuppressEMFModelTags() && (genClass.isExternalInterface() || genClass.isDynamic())) {
					boolean first = true;
					for (StringTokenizer stringTokenizer = new StringTokenizer(genClass.getModelInfo(),
							"\n\r"); stringTokenizer.hasMoreTokens();) {
						String modelInfo = stringTokenizer.nextToken();
						if (first) {
							first = false;
							rtout.write("\n\t * @model ");
							rtcout.write(modelInfo);
						} else {
							rtout.write("\n\t *        ");
							rtcout.write(modelInfo);
						}
					}
					if (first) {
						rtout.write("\n\t * @model");
					}
				}
			} else if (genClassifier instanceof GenEnum) {
				GenEnum genEnum = (GenEnum) genClassifier;
				rtout.write("\n\t * Returns the meta object for enum '{@link ");
				rtcout.write(genEnum.getQualifiedName());
				rtout.write(" <em>");
				rtcout.write(genEnum.getFormattedName());
				rtout.write("</em>}'.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the meta object for enum '<em>");
				rtcout.write(genEnum.getFormattedName());
				rtout.write("</em>'.\n\t * @see ");
				rtcout.write(genEnum.getQualifiedName());
			} else if (genClassifier instanceof GenDataType) {
				GenDataType genDataType = (GenDataType) genClassifier;
				if (genDataType.isPrimitiveType() || genDataType.isArrayType()) {
					rtout.write("\n\t * Returns the meta object for data type '<em>");
					rtcout.write(genDataType.getFormattedName());
					rtout.write("</em>'.");
				} else {
					rtout.write("\n\t * Returns the meta object for data type '{@link ");
					rtcout.write(genDataType.getRawInstanceClassName());
					rtout.write(" <em>");
					rtcout.write(genDataType.getFormattedName());
					rtout.write("</em>}'.");
				}
				rtout.write("\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the meta object for data type '<em>");
				rtcout.write(genDataType.getFormattedName());
				rtout.write("</em>'.");
				if (!genDataType.isPrimitiveType() && !genDataType.isArrayType()) {
					rtout.write("\n\t * @see ");
					rtcout.write(genDataType.getRawInstanceClassName());
				}
				if (!genModel.isSuppressEMFModelTags()) {
					boolean first = true;
					for (StringTokenizer stringTokenizer = new StringTokenizer(genDataType.getModelInfo(),
							"\n\r"); stringTokenizer.hasMoreTokens();) {
						String modelInfo = stringTokenizer.nextToken();
						if (first) {
							first = false;
							rtout.write("\n\t * @model ");
							rtcout.write(modelInfo);
						} else {
							rtout.write("\n\t *        ");
							rtcout.write(modelInfo);
						}
					}
					if (first) {
						rtout.write("\n\t * @model");
					}
				}
			}
			rtout.write("\n\t * @generated\n\t */\n");

			rtout.write("\n\t");
			rtcout.write(genClassifier.getImportedMetaType());
			rtout.write(" get");
			rtcout.write(genClassifier.getClassifierAccessorName());
			rtout.write("();\n");
			if (genClassifier instanceof GenClass) {
				GenClass genClass = (GenClass) genClassifier;
				for (GenFeature genFeature : genClass.getGenFeatures()) {
					rtout.write("\n\t/**\n\t * Returns the meta object for the ");
					rtcout.write(genFeature.getFeatureKind());
					rtout.write(" '{@link ");
					rtcout.write(genClass.getQualifiedInterfaceName());
					rtout.write("#");
					if (!genClass.isMapEntry() && !genFeature.isSuppressedGetVisibility()) {
						rtcout.write(genFeature.getGetAccessor());
					}
					rtout.write(" <em>");
					rtcout.write(genFeature.getFormattedName());
					rtout.write("</em>}'.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the meta object for the ");
					rtcout.write(genFeature.getFeatureKind());
					rtout.write(" '<em>");
					rtcout.write(genFeature.getFormattedName());
					rtout.write("</em>'.\n\t * @see ");
					rtcout.write(genClass.getQualifiedInterfaceName());
					rtout.write("#");
					if (!genClass.isMapEntry() && !genFeature.isSuppressedGetVisibility()) {
						rtcout.write(genFeature.getGetAccessor());
						rtout.write("()");
					}
					rtout.write("\n\t * @see #get");
					rtcout.write(genClass.getClassifierAccessorName());
					rtout.write("()\n\t * @generated\n\t */\n");
					rtout.write("\n\t");
					rtcout.write(genFeature.getImportedMetaType());
					rtout.write(" get");
					rtcout.write(genFeature.getFeatureAccessorName());
					rtout.write("();");
					rtout.write("\n");
				}
				if (genModel.isOperationReflection()) {
					for (GenOperation genOperation : genClass.getGenOperations()) {
						rtout.write("\n\t/**\n\t * Returns the meta object for the '{@link ");
						rtcout.write(genClass.getQualifiedInterfaceName());
						rtcout.write(HM);
						rtcout.write(genOperation.getName());
						rtout.write("(");
						rtcout.write(genOperation.getParameterTypes(", "));
						rtout.write(") <em>");
						rtcout.write(genOperation.getFormattedName());
						rtout.write("</em>}' operation.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the meta object for the '<em>");
						rtcout.write(genOperation.getFormattedName());
						rtout.write("</em>' operation.\n\t * @see ");
						rtcout.write(genClass.getQualifiedInterfaceName());
						rtcout.write(HM);
						rtcout.write(genOperation.getName());
						rtout.write("(");
						rtcout.write(genOperation.getParameterTypes(", "));
						rtout.write(")\n\t * @generated\n\t */\n");

						rtout.write("\n\t");
						rtcout.write(genOperation.getImportedMetaType());
						rtout.write(" get");
						rtcout.write(genOperation.getOperationAccessorName());
						rtout.write("();");

						rtout.write("\n");
					}
				}
			}
		}
		rtout.write("\n\t/**\n\t * Returns the factory that creates the instances of the model.\n\t * <!-- begin-user-doc -->\n\t * <!-- end-user-doc -->\n\t * @return the factory that creates the instances of the model.\n\t * @generated\n\t */\n");

		rtout.write("\n\t");
		rtcout.write(genPackage.getFactoryInterfaceName());
		rtout.write(" get");
		rtcout.write(genPackage.getFactoryName());
		rtout.write("();");
		rtout.write("\n");

		if (genPackage.isLiteralsInterface()) {
			rtout.write("\n\t/**\n\t * <!-- begin-user-doc -->\n\t * Defines literals for the meta objects that represent\n\t * <ul>\n\t *   <li>each class,</li>\n\t *   <li>each feature of each class,</li>");
			if (genModel.isOperationReflection()) {
				rtout.write("\n\t *   <li>each operation of each class,</li>");
			}
			rtout.write("\n\t *   <li>each enum,</li>\n\t *   <li>and each data type</li>\n\t * </ul>\n\t * <!-- end-user-doc -->\n\t * @generated\n\t */\n\t");
			rtout.write("interface Literals\n\t{");
			for (GenClassifier genClassifier : genPackage.getGenClassifiers()) {
				rtout.write("\n\t\t/**");
				if (genClassifier instanceof GenClass) {
					GenClass genClass = (GenClass) genClassifier;
					if (!genClass.isInterface()) {
						rtout.write("\n\t\t * The meta object literal for the '{@link ");
						rtcout.write(genClass.getQualifiedClassName());
						rtout.write(" <em>");
						rtcout.write(genClass.getFormattedName());
						rtout.write("</em>}' class.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @see ");
						rtcout.write(genClass.getQualifiedClassName());
					} else {
						rtout.write("\n\t\t * The meta object literal for the '{@link ");
						rtcout.write(genClass.getQualifiedInterfaceName());
						rtout.write(" <em>");
						rtcout.write(genClass.getFormattedName());
						rtout.write("</em>}' class.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @see ");
						rtcout.write(genClass.getQualifiedInterfaceName());
					}
				} else if (genClassifier instanceof GenEnum) {
					GenEnum genEnum = (GenEnum) genClassifier;
					rtout.write("\n\t\t * The meta object literal for the '{@link ");
					rtcout.write(genEnum.getQualifiedName());
					rtout.write(" <em>");
					rtcout.write(genEnum.getFormattedName());
					rtout.write("</em>}' enum.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @see ");
					rtcout.write(genEnum.getQualifiedName());
				} else if (genClassifier instanceof GenDataType) {
					GenDataType genDataType = (GenDataType) genClassifier;
					rtout.write("\n\t\t * The meta object literal for the '<em>");
					rtcout.write(genDataType.getFormattedName());
					rtout.write("</em>' data type.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->");
					if (!genDataType.isPrimitiveType() && !genDataType.isArrayType()) {
						rtout.write("\n\t\t * @see ");
						rtcout.write(genDataType.getRawInstanceClassName());
					}
				}
				rtout.write("\n\t\t * @see ");
				rtcout.write(genPackage.getQualifiedPackageClassName());
				rtout.write("#get");
				rtcout.write(genClassifier.getClassifierAccessorName());
				rtout.write("()\n\t\t * @generated\n\t\t */\n\t\t");
				rtcout.write(genClassifier.getImportedMetaType());
				rtout.write(" ");
				rtcout.write(genPackage.getClassifierID(genClassifier));
				rtout.write(" = eINSTANCE.get");
				rtcout.write(genClassifier.getClassifierAccessorName());
				rtout.write("();\n");
				if (genClassifier instanceof GenClass) {
					GenClass genClass = (GenClass) genClassifier;
					for (GenFeature genFeature : genClass.getGenFeatures()) {
						rtout.write("\n\t\t/**\n\t\t * The meta object literal for the '<em><b>");
						rtcout.write(genFeature.getFormattedName());
						rtout.write("</b></em>' ");
						rtcout.write(genFeature.getFeatureKind());
						rtout.write(" feature.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @generated\n\t\t */\n\t\t");
						rtcout.write(genFeature.getImportedMetaType());
						rtout.write(" ");
						rtcout.write(genClass.getFeatureID(genFeature));
						rtout.write(" = eINSTANCE.get");
						rtcout.write(genFeature.getFeatureAccessorName());
						rtout.write("();\n");
					}
					if (genModel.isOperationReflection()) {
						for (GenOperation genOperation : genClass.getGenOperations()) {
							rtout.write("\n\t\t/**\n\t\t * The meta object literal for the '<em><b>");
							rtcout.write(genOperation.getFormattedName());
							rtout.write("</b></em>' operation.\n\t\t * <!-- begin-user-doc -->\n\t\t * <!-- end-user-doc -->\n\t\t * @generated\n\t\t */\n\t\t");
							rtcout.write(genOperation.getImportedMetaType());
							rtout.write(" ");
							rtcout.write(genClass.getOperationID(genOperation, false));
							rtout.write(" = eINSTANCE.get");
							rtcout.write(genOperation.getOperationAccessorName());
							rtout.write("();\n");
						}
					}
				}
			}
			rtout.write("\n\t}\n");
		}
		rtout.write("\n} //");
		rtcout.write(genPackage.getPackageInterfaceName());
		genModel.emitSortedImports();
	}

}
