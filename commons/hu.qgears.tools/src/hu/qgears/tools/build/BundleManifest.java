package hu.qgears.tools.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.qgears.commons.UtilString;
import hu.qgears.tools.build.manifest.SimpleManifest;

/**
 * Parse an Eclipse Manifest.mf file to find out id version and dependency versions.
 * @author rizsi
 *
 */
public class BundleManifest {
	public List<AbstractDependency> deps=new ArrayList<>();
	public List<AbstractOffer> offers=new ArrayList<>();
	/**
	 * Typically jars packed within the bundle.
	 */
	public List<String> bundleClasspath=new ArrayList<>();
	public Version version=new Version();
	public String id;
	public EBundleType type=EBundleType.source;
	/**
	 * Eclipse project in case of Eclipse
	 * In case of binary the jar of plugin folder.
	 */
	public File projectFile;
	public List<LauncherData> launchers=new ArrayList<>();
	public List<LauncherData> agents=new ArrayList<>();
	public ClassPathHandler cph;
	public FragmentHostSpec fragmentHost;
	public BundleOffer bundleOffer;
	public void parse(SimpleManifest m) {
		String aaa=m.getMainAttributes().getValue("Bundle-SymbolicName");
		if(aaa==null)
		{
			throw new IllegalArgumentException("Bundle-SymbolicName must be defined");
		}
		id=UtilString.split(aaa, ";").get(0);
		if(id==null)
		{
			throw new IllegalArgumentException("Bundle-SymbolicName must be defined");
		}
		String v=m.getMainAttributes().getValue("Bundle-Version");
		if(v!=null)
		{
			version=parseVersion(v);
		}
		bundleOffer=new BundleOffer(id, version);
		offers.add(bundleOffer);
		String rb=m.getMainAttributes().getValue("Require-Bundle");
		if(rb!=null)
		{
			for(String depB: splitExcludingString(rb))
			{
				List<String> pieces=UtilString.split(depB, ";");
				if(pieces.size()>0)
				{
					String id=pieces.get(0);
					// It happens in some packages to have an additional space
					id=id.trim();
					BundleDependency dep=new BundleDependency(id);
					parseAdditionalInfo(pieces,dep);
					deps.add(dep);
				}
			}
		}
		String ip=m.getMainAttributes().getValue("Import-package");
		if(ip!=null)
		{
			for(String depIp: splitExcludingString(ip))
			{
				List<String> pieces=UtilString.split(depIp, ";");
				if(pieces.size()>0)
				{
					String id=pieces.get(0);
					PackageDependency dep=new PackageDependency(id);
					parseAdditionalInfo(pieces, dep);
					deps.add(dep);
				}
			}
		}
		String ep=m.getMainAttributes().getValue("Export-Package");
		if(ep!=null)
		{
			for(String expP: UtilString.split(ep, ","))
			{
				List<String> pieces=UtilString.split(expP, ";");
				if(pieces.size()>0)
				{
					String id=pieces.get(0);
					Version vi=version;
					for(String s: pieces.subList(1, pieces.size()))
					{
						if(s.startsWith("version=\""))
						{
							String vs=s.substring("version=\"".length(), s.length()-1);
							vi=parseVersion(vs);
						}
					}
					offers.add(new PackageOffer(id, vi));
				}
			}
		}
		String cp=m.getMainAttributes().getValue("Bundle-ClassPath");
		if(cp!=null)
		{
			for(String jar:UtilString.split(cp, ","))
			{
				String j2=jar.trim();
				if(".".equals(j2))
				{
					// the root folder is deafault and always added to the classpath
					continue;
				}
				bundleClasspath.add(jar);
			}
		}
		String fhost=m.getMainAttributes().getValue("Fragment-Host");
		if(fhost!=null)
		{
			List<String> pieces=UtilString.split(fhost, ";");
			String h=pieces.get(0);
			VersionRange vr=new VersionRange();
			for(String s: pieces.subList(1, pieces.size()))
			{
				String trimmed=s.trim();
				if(trimmed.startsWith("bundle-version="))
				{
					vr=parseVersionRange(trimmed.substring("bundle-version=".length()));
				}
			}
			fragmentHost=new FragmentHostSpec(h);
			fragmentHost.versionRange=vr;
		}
	}

	private List<String> splitExcludingString(String ip) {
		boolean instring=false;
		StringBuilder bld=new StringBuilder();
		List<String> ret=new ArrayList<>();
		for(int i=0;i<ip.length();++i)
		{
			char c=ip.charAt(i);
			if(c==',' && !instring)
			{
				ret.add(bld.toString());
				bld.delete(0, bld.length());
			}else
			{
				bld.append(c);
				if(c=='\"')
				{
					instring=!instring;
				}
			}
		}
		ret.add(bld.toString());
		return ret;
	}

	private void parseAdditionalInfo(List<String> pieces, AbstractDependency into) {
		for(String piece:pieces.subList(1, pieces.size()))
		{
			if(piece.startsWith("bundle-version="))
			{
				String versionSpec=piece.substring("bundle-version=".length());
				into.versionRange=parseVersionRange(versionSpec);
			}
			if(piece.startsWith("version="))
			{
				String versionSpec=piece.substring("version=".length());
				into.versionRange=parseVersionRange(versionSpec);
			}
			if(piece.equals("resolution:=optional"))
			{
				into.optional=true;
			}
			if(piece.equals("visibility:=reexport"))
			{
				into.reexport=true;
			}
		}
	}

	private VersionRange parseVersionRange(String versionSpec) {
		VersionRange ret=new VersionRange();
		
		// tolerate missing " at end as it happens in some of the the official Eclipse jars.
		if(!versionSpec.endsWith("\""))
		{
			versionSpec=versionSpec+"\"";
		}
		if(!(versionSpec.startsWith("\"") && versionSpec.endsWith("\"")))
		{
			throw new IllegalArgumentException("Version range must be in \"versionrange\""+" but is: "+versionSpec);
		}
		String v=versionSpec.substring(1, versionSpec.length()-1);
		if(v.startsWith("[")||v.startsWith("("))
		{
			if(!(
				(v.startsWith("[")||v.startsWith("(")) 
				&&
				(v.endsWith("]")||v.endsWith(")")) 
				))
			{
				throw new IllegalArgumentException("Version range must start and end with ( or [ and ) and ]");
			}
			if(v.startsWith("("))
			{
				ret.minExcluding=true;
			}
			if(v.endsWith(")"))
			{
				ret.maxExcluding=true;
			}
			List<String> ps=UtilString.split(v, "[()],");
			ret.min=parseVersion(ps.get(0));
			ret.max=parseVersion(ps.get(1));
		}else
		{
			// Explicite minimum version
			ret.min=parseVersion(v);
			// ret.max=parseVersion(v);
		}
		return ret;
	}

	private Version parseVersion(String v) {
		return new Version().parse(v);
	}

	public boolean offers(AbstractDependency dep) {
		for(AbstractOffer o: offers)
		{
			if(dep.isOfferedBy(o))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ""+id+": "+version;
	}

	public static BundleManifest createDummy(String id, String v) {
		BundleManifest bm=new BundleManifest();
		bm.id=id;
		bm.type=EBundleType.dummy;
		bm.version=new Version().parse("1.0.0");
		bm.offers.add(new BundleOffer(id, bm.version));
		return bm;
	}

}
