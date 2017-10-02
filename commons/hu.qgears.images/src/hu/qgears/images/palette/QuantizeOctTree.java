package hu.qgears.images.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import hu.qgears.images.NativeImage;

/**
 * Quantize the number of colors of an image to a limited number.
 * 
 * Implementation is based on an octal tree (more/less of red/green/blue) index of all colors.
 * 
 * Leaves of the index tree are the colors used in the palette.
 * 
 * Until we have reduced the number of colors to the required number the delegate with least error (error introduced by removing the children colors) is reduced to a leave.
 * 
 * Delegates are the nodes who have children and all children are leaves. They have 2-8 children so a single step reduces the number of colors in the palette by 1-7. 
 * (Single children delegates don't exist.)
 *  
 * @author rizsi
 *
 */
public class QuantizeOctTree {
	private static final int MAX_LEVEL=7;
	final static int MAX_LEAVE_NODES = 300000;
	private int ctr;
	private TreeMap<Long, Cluster> delegatesByValue=new TreeMap<Long, Cluster>();
	private Cluster root=new Cluster(null, 0, 0);
	class Cluster
	{
		private int level;
		private int mid0;
		private int mid1;
		private int mid2;
		private int sum0;
		private int sum1;
		private int sum2;
		private int nPixel;
		private Cluster parent;
		private boolean delegate;
		private long value=-1;
		private long counterIndex;
		private String key="";
		private Cluster[] children=new Cluster[8];
		private boolean empty=true;
		/**
		 * Number of leaves below this node.
		 * When 0 then this node is a leave.
		 */
		private int nBelow=0;
		private Cluster(Cluster parent, int level, int index)
		{
			this.counterIndex=ctr++;
			this.parent=parent;
			this.level=level;
			if(parent!=null)
			{
				key=parent.getClusterAccessorKey()+index;
				int diff= (1<<(7-level));
				if(diff<1)
				{
					throw new RuntimeException("Para");
				}
				mid0 = parent.mid0 + ((index & 1) > 4 ? diff : -diff);
				mid1 = parent.mid1 + ((index & 2) > 0 ? diff : -diff);
				mid2 = parent.mid2  + ((index & 1) > 0 ? diff : -diff);
			}
		}
		private int childIndex(int v0, int v1, int v2)
		{
			return ((v0>=mid0?1:0)<<2)+((v1>=mid1?1:0)<<1)+(v2>=mid2?1:0);
		}
		private void addPixel(int v0, int v1, int v2)
		{
			boolean canSplit=level<MAX_LEVEL&&nPixel>0;
			boolean done=false;
			if(nBelow==0&&canSplit)
			{
				done=true;
				// If it was a leaf node and we can create new nodes then split this node.
				int index=childIndex(sum0, sum1, sum2);
				Cluster ch=children[index];
				if(ch==null)
				{
					ch=new Cluster(this, level+1, index);
					children[index]=ch;
					empty=false;
				}
				ch.addPixel(v0, v1, v2);
				// N below is increased but not recursively upwards
				nBelow++;
				delegate=true;
				if(parent!=null)
				{
					parent.removeDelegate();
				}
			}
			nPixel++;
			sum0+=v0;
			sum1+=v1;
			sum2+=v2;
			if(canSplit&&!done&&nPixel>1)
			{
				int index=childIndex(v0, v1, v2);
				Cluster ch=children[index];
				if(ch==null)
				{
					ch=new Cluster(this, level+1, index);
					children[index]=ch;
					empty=false;
					incNBelow();
				}
				ch.addPixel(v0, v1, v2);
			}
			updateValue();
		}
		private void incNBelow() {
			nBelow++;
			if(parent!=null)
			{
				parent.incNBelow();
			}
		}
		private int getV0()
		{
			if(nPixel==0)
			{
				return 0;
			}
			int v0=sum0/nPixel;
			return v0;
		}
		private int getV1()
		{
			if(nPixel==0)
			{
				return 0;
			}
			int v1=sum1/nPixel;
			return v1;
		}
		private int getV2()
		{
			if(nPixel==0)
			{
				return 0;
			}
			int v2=sum2/nPixel;
			return v2;
		}
		/**
		 * Remove children of this node - all are merged into this node.
		 */
		private void reduce()
		{
			if(parent==null)
			{
				throw new RuntimeException("Can not reduce node with no parent! "+delegate);
			}
			if(!delegate)
			{
				throw new RuntimeException("Can not reduce node that is not a delegate! this: "+this+" parent:"+parent);
			}
			for(Cluster c: children)
			{
				if(c!=null)
				{
					c.clear();
				}
			}
			removeDelegate();
			children=new Cluster[8];
			empty=true;
			if(parent!=null)
			{
				parent.decreaseNBelow(nBelow-1);
			}
			nBelow=0;
			if(parent!=null)
			{
				parent.checkBecomeDelegate();
			}
		}
		/**
		 * Clearing the node is not necessary but helps to find bugs.
		 */
		private void clear() {
			if(delegate)
			{
				throw new RuntimeException("Can not clear a delegate! "+getClusterAccessorKey());
			}
			for(Cluster c: children)
			{
				if(c!=null)
				{
					c.clear();
				}
			}
			parent=null;
			empty=true;
		}
		/**
		 * Check if this node becomes a delegate?
		 */
		private void checkBecomeDelegate() {
			if(delegate)
			{
				throw new RuntimeException("Already a delegate!");
			}
			delegate=true;
			for(Cluster c: children)
			{
				if(c!=null)
				{
					for(Cluster cc: c.children)
					{
						if(cc!=null)
						{
							delegate=false;
						}
					}
					if(c.delegate&&delegate)
					{
						throw new RuntimeException("Internal error");
					}
				}
			}
			if(delegate)
			{
				updateValue();
			}
		}
		private void decreaseNBelow(int i) {
			nBelow-=i;
			if(parent!=null)
			{
				parent.decreaseNBelow(i);
			}
		}
		private int errorIfReduced()
		{
			int err=0;
			for(Cluster c: children)
			{
				if(c!=null)
				{
					err+=c.nPixel*(square(c.getV0()-getV0())+square(c.getV1()-getV1())+square(c.getV2()-getV2()));
				}
			}
			return err;
		}
		private int errorPerColor()
		{
			return errorIfReduced()/nBelow;
		}
		private long delegateKey=-1;
		/**
		 * Update the value of this node.
		 */
		private void updateValue() {
			if(delegate)
			{
				delegatesByValue.remove(delegateKey);
				value=errorPerColor();
				delegateKey=createDelegateKey();
				delegatesByValue.put(delegateKey, this);
			}
		}
		private void removeDelegate() {
			delegate=false;
			delegatesByValue.remove(delegateKey);
		}
		/**
		 * Order the objects by the error they cause.
		 * Index is also included into the key so that otherwise equal nodes can be stored in the same map.
		 * @return
		 */
		private long createDelegateKey() {
			return (value<<32)|counterIndex;
		}
		/**
		 * Get the key that accesses this cluster.
		 * Only used for debugging purpose.
		 * @return
		 */
		private String getClusterAccessorKey() {
			return key;
		}
		private Cluster getColor(int v0, int v1, int v2) {
			int bestFit=-1;
			int bestError=Integer.MAX_VALUE;
			for(int i=0; i< children.length;++i)
			{
				Cluster c=children[i];
				if(c!=null)
				{
					int error=square(c.getV0()-v0)+square(c.getV1()-v1)+square(c.getV2()-v2);
					if(error<bestError)
					{
						bestFit=i;
						bestError=error;
					}
				}
			}
			if(bestFit==-1)
			{
				return this;
			}else
			{
				return children[bestFit].getColor(v0, v1, v2);
			}
		}
		private int toNativeImageColor() {
			int v0=getV0()&0xFF;
			int v1=getV1()&0xFF;
			int v2=getV2()&0xFF;
			return (v0<<24)+(v1<<16)+(v2<<8)+0xFF;
		}
		@Override
		public String toString() {
			return ""+key+(empty?" empty":"")+(delegate? " delegate":"")+" value: "+value;
		}
		private void collectLeaves(List<Cluster> ret)
		{
			for(Cluster ch: children)
			{
				if(ch!=null)
				{
					ch.collectLeaves(ret);
				}
			}
			if(nBelow==0)
			{
				ret.add(this);
			}
		}
	}
	/**
	 * Create a palette from the colors of the image.
	 * @param im input image to find a good fit palette. The image data is used read only.
	 * @param maxColor maximum number of colors in the result palette (result is going to be in range: [maxColor-6, maxColor] or less in case the source image has less colors).
	 * @param mask mask to skip pixels from the operation. May be null - then all pixels are used.
	 * @return a palette that is a good fit for the image.
	 */
	public static Palette quantizeOctTree(NativeImage im, int maxColor, IMask mask)
	{
		return new QuantizeOctTree().quantizeOctTreePrivate(im, maxColor, mask);
	}
	/**
	 * This class is not intended to be instantiated outside this class. Use the static method.
	 */
	private QuantizeOctTree()
	{
	}
	static private int square(int i) {
		return i*i;
	}
	private Palette quantizeOctTreePrivate(NativeImage im, int maxColor, IMask mask)
	{
		root.mid0=128;
		root.mid1=128;
		root.mid2=128;
		for(int j=0;j<im.getHeight();++j)
		{
			for(int i=0;i<im.getWidth();++i)
			{
				int v=im.getPixel(i, j);
				if(mask!=null && mask.skip(i, j, v))
				{
					continue;
				}
				int v0=(v>>24)&0xFF;
				int v1=(v>>16)&0xFF;
				int v2=(v>>8)&0xFF;
				root.addPixel(v0, v1, v2);
				if(root.nBelow>MAX_LEAVE_NODES)
				{
					reduceOneNode();
				}
			}
		}
		while(root.nBelow>maxColor)
		{
			reduceOneNode();
		}
		List<Cluster> ret=new ArrayList<Cluster>();
		root.collectLeaves(ret);
		int[] colors=new int[ret.size()];
		for(int i=0;i<colors.length;++i)
		{
			colors[i]=ret.get(i).toNativeImageColor();
		}
		return new Palette(colors);
	}
	/**
	 * Simple implementation to find a good match of colors for the image.
	 * Searching all palette entries gives better result though
	 * see: {@link Palette.reduceImageColorsToPalette}
	 * (but this is cheaper in CPU cycles).
	 * @param im
	 */
	@SuppressWarnings("unused")
	private void reduceImageColorsToPalette(NativeImage im) {
		for(int j=0;j<im.getHeight();++j)
		{
			for(int i=0;i<im.getWidth();++i)
			{
				int v=im.getPixel(i, j);
				int v0=(v>>24)&0xFF;
				int v1=(v>>16)&0xFF;
				int v2=(v>>8)&0xFF;
				Cluster c=root.getColor(v0, v1, v2);
				int vNew=c.toNativeImageColor();
				im.setPixel(i, j, vNew);
			}
		}
	}
	private void reduceOneNode() {
		Cluster toreduce=delegatesByValue.get(delegatesByValue.firstKey());
		toreduce.reduce();
	}
}
