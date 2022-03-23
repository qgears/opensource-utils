package hu.qgears.crossref;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A scope is a place of the document where a reference occurs.
 * And also specifies how the reference have to be looked up.
 * 
 * It is intended to use a single scope object for each reference.
 * 
 * The scope defines the ways to search for existing objects to refer to.
 * Examples:
 *  * First search for local objects
 *  * Then search for imported objects
 *  * Then search for global objects
 */
public class Scope {
	private String pack;
	private Set<String> allowedTypes;
	private String id;
	private String localIdentifier;
	protected List<GidSearch> gidSearch;
	protected boolean sealed=false;
	public List<GidSearch> typeSearch;
	public final IFilterFunction filterFunction;
	public Scope(String pack, String id, String searchLocalId, IFilterFunction ff) {
		super();
		this.pack = pack;
		this.id=id;
		this.localIdentifier=searchLocalId;
		this.filterFunction=ff;
	}
	/**
	 * Allowed target types of the reference.
	 * The set of allowed types is useful to narrow the search for possible targets by name.
	 * The exact types have to be specified not abstract types! In case there is a type system with 
	 * subtypes then all possible subtypes must be used here. This is because the type is searched
	 * for exact match for performance reasons. 
	 * @return null is allowed and means that any type is accepted by this reference.
	 */
	public Set<String> getAllowedTypes() {
		return allowedTypes;
	}

	
	/**
	 * Get the local identifier of the expected target. (Ignore package search by local identifier.)
	 * @return null is possible and means that there is no search by local identifier.
	 */
	public String getLocalIdentifier()
	{
		return localIdentifier;
	}
	/**
	 * Get the possible global identifiers of the target.
	 * @return the targets are in preference order if the first matches then later matches are omitted.
	 *  Null is possible and means that there is no search by global id.
	 */
	public List<String> getPossibleGlobalIds()
	{
		if(id!=null)
		{
			List<String> ret=new ArrayList<>();
			String p=pack;
			while(p.length()>0)
			{
				ret.add(p+"."+id);
				int idx=p.lastIndexOf('.');
				if(idx>0)
				{
					p=p.substring(0, idx);
				}else
				{
					p="";
				}
			}
			ret.add(id);
			return ret;
		}
		return null;
	}
	/**
	 * Set the types allowed to be searched.
	 * For performance reasons it stores the reference to the types set.
	 * @param targetType caller must not change this set after setting this value.
	 */
	public void setAllowedTypes(Set<String> targetType) {
		if(sealed)
		{
			throw new IllegalStateException();
		}else
		{
			allowedTypes=targetType;
		}
	}
	/**
	 * After this call the scope object is read only.
	 */
	public void seal() {
		sealed=true;
	}
	public String getId() {
		return id;
	}
	@Override
	public String toString() {
		return "Scope_"+id+" "+localIdentifier;
	}
}
