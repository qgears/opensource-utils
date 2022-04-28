package hu.qgears.xtextgrammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.commons.UtilString;
import hu.qgears.crossref.Doc;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.tokenizer.recognizer.RecognizerId;
import hu.qgears.parser.tokenizer.recognizer.RecognizerString;
import hu.qgears.parser.util.TreeVisitor;

/**
 * Can only be used on a single thread.
 */
public class RuntimeMappings {
	interface RMCommand {
		abstract AutoCloseable exec(ITreeElem tree) throws Exception;
	}
	interface RMFactory {
		abstract Object create(ITreeElem tree) throws Exception;
	}
	interface UnresolvedReferenceFactory {
		abstract EObject createUnresolvedReferenceObject() throws Exception;
	}
	public static final String prefixProxyId="proxyid://";
	private Map<String,RMCommand> commands=new HashMap<>();
	private RMFactory currentType;
	private Stack<ITreeElem> currentObjectCreatingElem=new Stack<>();
	private EClass currentReferenceType;
	private Object currentObject;
	private EStructuralFeature currentFeature;
	protected Doc doc;
	public Map<String,String> consts=new TreeMap<String, String>();
	public RuntimeMappings() {
	}
	public RuntimeMappings process(String langFile) {
		commands.put("doc", (t)->{return ()->{};});
		for(String l: UtilString.split(langFile, "\r\n"))
		{
			if(l.startsWith("//#"))
			{
				List<String> pieces=UtilString.split(l, " ");
				String command=pieces.get(1);
				String token=unescape(pieces.get(2));
				switch (command) {
				case "type":
				{
					String type=unescape(pieces.get(3));
					RMFactory sup=createFactory(type);
					commands.put(token, (t)->{
						currentType=sup;
						currentObjectCreatingElem.push(t);
						return ()->{
							if(currentObject==null)
							{
								// System.err.println("Current object is null: "+type+" "+t);
								createObject(sup, t);
								currentObjectCreatingElem.pop();
							}
						};
					});
					break;
				}
				case "createType":
				{
					String type=unescape(pieces.get(3));
					RMFactory sup=createFactory(type);
					commands.put(token, (t)->{
						if(currentObject!=null)
						{
							throw new RuntimeException();
						}else
						{
							createObject(sup, t);
						}
						return ()->{};
					});
					break;
				}
				case "createTypeSetFeatureCurrent":
				{
					String type=unescape(pieces.get(3));
					RMFactory sup=createFactory(type);
					String featureName=unescape(pieces.get(4));
					EStructuralFeature f=findFeature(type, featureName);
					commands.put(token, (t)->{
						Object prev=currentObject;
						createObject(sup, t);
						addToReference((EObject)currentObject, f, prev, null, true);
						return ()->{};
					});
					break;
				}
				case "setFeature":
				{
					String claName=unescape(pieces.get(3));
					String featureName=unescape(pieces.get(4));
					EStructuralFeature r=(EStructuralFeature)findFeature(claName, featureName);
					if(r==null)
					{
						throw new RuntimeException("Feature not found: "+claName+" "+featureName);
					}
					EStructuralFeature resetFeature=currentFeature;
					EClass pf=findFeatureTypeForUnresolvedObject(claName, r, featureName);
					currentFeature=r;
					Set<String> acceptedTypes=findAcceptedTypes(r);
					commands.put(token, (t)->{
						instantiateIfNecessary(t);
						EObject reset=(EObject)currentObject;
						currentObject=null;
						currentReferenceType=pf;
						if(t.getSubs().size()>0)
						{
							processTree(t.getSubs().get(0));
						}
						currentReferenceType=null;
						if(currentObject==null)
						{
							// Handles case when default string is used in grammear.
							// Example: name=(ID|'default')
							currentObject=t.getString();
						}
						addToReference(reset, r, currentObject, acceptedTypes, true);
						currentObject=reset;
						currentFeature=resetFeature;
						return null;
					});
					break;
				}
				case "addToFeature":
				{
					String claName=unescape(pieces.get(3));
					String featureName=unescape(pieces.get(4));
					EStructuralFeature r=(EStructuralFeature)findFeature(claName, featureName);
					EStructuralFeature resetFeature=currentFeature;
					Set<String> acceptedTypes=findAcceptedTypes(r);
					EClass pf=findFeatureTypeForUnresolvedObject(claName, r, featureName);
					currentFeature=r;
					commands.put(token, (t)->{
						instantiateIfNecessary(t);
						EObject reset=(EObject)currentObject;
						currentObject=null;
						currentReferenceType=pf;
						processTree(t.getSubs().get(0));
						currentReferenceType=null;
						addToReference(reset, r, currentObject, acceptedTypes, true);
						currentObject=reset;
						currentFeature=resetFeature;
						return null;
					});
					break;
				}
				case "id":
				{
					commands.put(token, (t)->{
						String c=RecognizerId.unescape(t.getString(), "^");
						currentObject=c;
						return null;
					});
					break;
				}
				case "const":
				{
					commands.put(token, (t)->{
						String c=RecognizerId.unescape(t.getString(), "^");
						currentObject=c;
						return null;
					});
					consts.put(token, token);
					break;
				}
				case "string":
				{
					commands.put(token, (t)->{
						String c=RecognizerString.getString(t.getString());
						currentObject=c;
						return null;
					});
					consts.put(token, token);
					break;
				}
				case "setBooleanTrue":
				{
					String claName=unescape(pieces.get(3));
					String featureName=unescape(pieces.get(4));
					EStructuralFeature f=findFeature(claName, featureName);
					commands.put(token, (t)->{
						instantiateIfNecessary(t);
						addToReference((EObject)currentObject, f, true, null, true);
						return null;
					});
					break;
				}
				case "boolean":
				{
					RMCommand prev=commands.put(token, (t)->{
						boolean value=Boolean.parseBoolean(t.getString());
						currentObject=value;
						return null;
					});
					// System.out.println("Prev: "+prev);
					break;
				}
				case ProcessXtextFile.localCrossReference:
				{
					commands.put(token, (t)->{
						CrossReferenceAdapter cra=createUnresolvedReferencePlaceHolder(currentReferenceType);
						EObject o=(EObject)cra.getTarget();
						currentObject=o;
						cra.getOrCreateUnresolvedCrossReferenceObject()
							.setUnresolvedReference(prefixProxyId, RecognizerId.unescape(t.getString(), "^"))
							.setFeatureThatEndsInThis(currentFeature);
							;
						cra.setSourceReference(new SourceReference(doc, t));
						return null;
					});
					break;
				}
				case ProcessXtextFile.fqidCrossReference:
				{
					commands.put(token, (t)->{
						CrossReferenceAdapter cra=createUnresolvedReferencePlaceHolder(currentReferenceType);
						EObject o=(EObject)cra.getTarget();
						currentObject=o;
						cra.getOrCreateUnresolvedCrossReferenceObject()
							.setUnresolvedReference(prefixProxyId, RecognizerId.unescape(t.getString(), "^"))
							.setFeatureThatEndsInThis(currentFeature);
						cra.setSourceReference(new SourceReference(doc, t));
						return null;
					});
					break;
				}
				case "createProxy":
				{
					String proxyPrefix=unescape(pieces.get(3));
					commands.put(token, (t)->{
						CrossReferenceAdapter cra=createUnresolvedReferencePlaceHolder(currentReferenceType);
						EObject o=(EObject)cra.getTarget();
						currentObject=o;
						cra.getOrCreateUnresolvedCrossReferenceObject()
							.setUnresolvedReference(proxyPrefix, RecognizerId.unescape(t.getString(), "^"))
							.setFeatureThatEndsInThis(currentFeature);
						cra.setSourceReference(new SourceReference(doc, t));
						return null;
					});
					break;
				}
				case "enumValue":
				{
					String claName=unescape(pieces.get(3));
					String featureName=unescape(pieces.get(4));
					EClassifier cla=getClassifier(claName);
					EEnum enu=(EEnum)cla;
					EEnumLiteral enumLiteral=enu.getEEnumLiteral(featureName);
					commands.put(token, (t)->{
						currentObject=enumLiteral.getInstance();
						return null;
					});
					break;
				}
				default:
					throw new RuntimeException("Unhandled command: "+command);
				}
			}
		}
		return this;
	}

	protected CrossReferenceAdapter createUnresolvedReferencePlaceHolder(EClass cla) {
		EObject ret=cla.getEPackage().getEFactoryInstance().create(cla);
		return createCrossReferenceAdapter(ret);
	}

	private Map<EStructuralFeature, Set<String>> acceptedTypesCache=new HashMap<>();
	private Set<String> findAcceptedTypes(EStructuralFeature r) {
		Set<String> ret=acceptedTypesCache.get(r);
		if(ret==null)
		{
			ret=new HashSet<>();
			EClassifier clafier=r.getEType();
			if(clafier instanceof EClass)
			{
				EClass requiredClass=(EClass)clafier;
				for(Object o: EPackage.Registry.INSTANCE.values())
				{
					if(o instanceof EPackage)
					{
						EPackage p=(EPackage) o;
						for(EClassifier n:p.getEClassifiers())
						{
							if(n instanceof EClass)
							{
								EClass nc=(EClass) n;
								if(!nc.isAbstract() && !nc.isInterface())
								{
									if(nc==requiredClass || requiredClass.isSuperTypeOf(nc))
									{
										ret.add(getEMFClassName(nc));
									}
								}
							}
						}
					}
				}
			}
			acceptedTypesCache.put(r, ret);
		}
		return ret;
	}

	private EClass findFeatureTypeForUnresolvedObject(String claName, EStructuralFeature r, String featureName)
	{
		EClass _host=(EClass)getClassifier(claName);
		EClass host=findFirstNotAbstractSubClass(_host, (c)->true);
		EObject hostObject;
		try {
			hostObject = host.getEPackage().getEFactoryInstance().create(host);
		} catch (Exception e) {
			// It happens in case the claName is abastract. These cases are not used as proxy.
			return null;
		}
		EClassifier tg=r.getEType();
		if(tg instanceof EClass)
		{
			EClass requiredClass=(EClass) tg;
			EClass nc= findFirstNotAbstractSubClass(requiredClass, c->assignable(hostObject,r,c));
			if(nc!=null)
			{
				return nc;
			}
		}
		return null;
	}
	private UnresolvedReferenceFactory createUnresolvedReferenceFactory(String claName, EStructuralFeature r, String featureName) {
		EClass _host=(EClass)getClassifier(claName);
		EClass host=findFirstNotAbstractSubClass(_host, (c)->true);
		EObject hostObject;
		try {
			hostObject = host.getEPackage().getEFactoryInstance().create(host);
		} catch (Exception e) {
			// It happens in case the claName is abastract. These cases are not used as proxy.
			return null;
		}
		EClassifier tg=r.getEType();
		if(tg instanceof EClass)
		{
			EClass requiredClass=(EClass) tg;
			EClass nc= findFirstNotAbstractSubClass(requiredClass, c->assignable(hostObject,r,c));
			if(nc!=null)
			{
				return createUnresolvedReferenceFactory(nc);
			}
		}
		return null;
	}

	private EClass findFirstNotAbstractSubClass(EClass requiredClass, Function<EClass, Boolean> acceptor) {
		if(requiredClass.isInterface() || requiredClass.isAbstract() || !acceptor.apply(requiredClass))
		{
			for(Object o: EPackage.Registry.INSTANCE.values())
			{
				if(o instanceof EPackage)
				{
					EPackage p=(EPackage) o;
					for(EClassifier n:p.getEClassifiers())
					{
						if(n instanceof EClass)
						{
							EClass nc=(EClass) n;
							if(!nc.isAbstract()&&!nc.isInterface() && requiredClass.isSuperTypeOf(nc) && acceptor.apply(nc))
							{
								return nc;
							}
						}
					}
				}
			}
		}else
		{
			return requiredClass;
		}
		return null;
	}

	private boolean assignable(EObject hostObject, EStructuralFeature r, EClass requiredClass) {
		EObject toAdd=requiredClass.getEPackage().getEFactoryInstance().create(requiredClass);
		return addToReference(hostObject, r, toAdd, null, false);
	}

	private UnresolvedReferenceFactory createUnresolvedReferenceFactory(EClass nc) {
		EFactory f=nc.getEPackage().getEFactoryInstance();
		return ()->f.create(nc);
	}

	private void createObject(RMFactory sup, ITreeElem t) throws Exception {
		currentObject=sup.create(t);
		ITreeElem te=currentObjectCreatingElem.peek();
		if(currentObject instanceof EObject)
		{
			CrossReferenceAdapter c=createCrossReferenceAdapter((EObject)currentObject).setDoc(doc);
			c.setSourceReference(new SourceReference(doc, te.getTextIndexFrom(), te.getTextIndexTo()));
		}
	}

	protected CrossReferenceAdapter createCrossReferenceAdapter(EObject o) {
		CrossReferenceAdapter ret=CrossReferenceAdapter.get(o);
		ret.setDoc(doc);
		return ret;
	}
	@SuppressWarnings("unchecked")
	private boolean addToReference(EObject host, EStructuralFeature r, Object toAdd, Set<String> acceptedTypes, boolean excAllowed) {
		Object v=host.eGet(r, false);
		int index=0;
		if(v instanceof EList<?>)
		{
			@SuppressWarnings("rawtypes")
			EList l =(EList) v;
			index=l.size();
		}
		if(toAdd instanceof EObject)
		{
			CrossReferenceAdapter craUnresolvedReference=CrossReferenceAdapter.get((EObject)toAdd);
			if(craUnresolvedReference.isUnresolvedReference())
			{
				CrossReferenceInstance cri=craUnresolvedReference.getUnresolvedCrossReference();
				cri.setSourceParameters(host, (EReference)r, index);
				cri.unresolvedReferenceAcceptedTypes=acceptedTypes;
				int finalIndex=index;
				cri.source.getAddedToTreeProperty().addListenerWithInitialTrigger(new UtilEventListener<Boolean>() {
					@Override
					public void eventHappened(Boolean b) {
						if(b)
						{
							cri.referenceInstalledIntoTree(host, r, finalIndex);
						}else
						{
							// cri.setReferenceSearchScope(null);
						}
					}
				});
			}
		}
		try {
			if(v instanceof EList<?>)
			{
				@SuppressWarnings("rawtypes")
				EList l =(EList) v;
				l.add(toAdd);
			}else
			{
				host.eSet(r, toAdd);
			}
		} catch (Exception e) {
			if(excAllowed)
			{
				throw new RuntimeException("Adding "+toAdd +" to "+host+" as feature: "+r.getName(), e);
			}
			return false;
		}
		return true;
	}
	private EStructuralFeature findFeature(String claName, String featureName) {
		EClassifier cla=getClassifier(claName);
		EClass c=(EClass) cla;
		EStructuralFeature f=c.getEStructuralFeature(featureName);
		return f;
	}

	private RMFactory createFactory(String type) {
		switch(type)
		{
		case "int":
			return t->{
				return Integer.parseInt(t.getString());
			};
		case "long":
			return t->{
				return Long.parseLong(t.getString());
			};
		case "boolean":
			return t->{
				return Boolean.parseBoolean(t.getString());
			};
		}
		EClassifier cla=getClassifier(type);
		if(cla==null)
		{
			return (t)->null;
		}else
		{
			EClass c=(EClass) cla;
			EPackage p=c.getEPackage();
			EFactory f=p.getEFactoryInstance();
			// Try creating to have error earlier in case creation is not possible
			// f.create(c);
			return (t)->{
				return f.create(c);
			};
		}
	}
	private EClassifier getClassifier(String type) {
		int idx=type.lastIndexOf('.');
		if(idx<0)
		{
			System.err.println("Missing Type: "+type);
			return null;
		}else
		{
			String pname=type.substring(0,idx);
			String n=type.substring(idx+1);
			EPackage p=(EPackage)EPackage.Registry.INSTANCE.get(pname);
			EClassifier cla=p.getEClassifier(n);
			return cla;
		}
	}

	private void instantiateIfNecessary(ITreeElem t) throws Exception {
		if(currentObject==null)
		{
			createObject(currentType, t);
		}
	}

	private String unescape(String string) {
		return string.substring(1, string.length()-1);
	}
	public Object processTree(Doc doc, ITreeElem root) throws Exception {
		this.doc=doc;
		currentObject=null;
		currentFeature=null;
		currentReferenceType=null;
		currentType=null;
		return processTree(root);
	}
	private Object processTree(ITreeElem root) throws Exception {
		new TreeVisitor() {
			@Override
			protected AutoCloseable visitNode(ITreeElem te, int depth) throws Exception {
				String tname=te.getTypeName();
				RMCommand c=commands.get(tname);
				if(c!=null)
				{
					try {
						return c.exec(te);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.err.println("Processing: "+te);
						e.printStackTrace();
						throw e;
					}
				}else
				{
					System.err.println("No mapping: "+tname);
				}
				return ()->{
				};
			}
		}.visit(root);
		return currentObject;
	}

	public static String getEMFClassName(EClass eClass) {
		return eClass.getName();
	}
}
