package hu.qgears.tools;

import java.io.File;

import hu.qgears.images.ENativeImageAlphaStorageFormat;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.UtilNativeImageIo;
import hu.qgears.images.UtilNativeImageIo.IImageCompareListener;
import hu.qgears.images.text.RGBAColor;
import hu.qgears.images.tiff.NativeTiffLoader;
import joptsimple.annot.JOHelp;
import joptsimple.tool.AbstractTool;

public class TiffImageCompare extends AbstractTool {

	public class Args implements IArgs
	{
		@JOHelp("First file to compare")
		public File a;
		@JOHelp("Second file to compare")
		public File b;

		@Override
		public void validate() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public String getId() {
		return "tiffImageCompare";
	}

	@Override
	public String getDescription() {
		return "Compare two 'canonical format' tiff images";
	}

	@Override
	protected int doExec(IArgs a) throws Exception {
		Args args=(Args)a;
		NativeImage ima=NativeTiffLoader.getInstance().loadImageFromTiff(args.a);
		NativeImage imb=NativeTiffLoader.getInstance().loadImageFromTiff(args.b);
		UtilNativeImageIo.compareImages(ima, imb, new IImageCompareListener() {
						
			@Override
			public void differentSize(SizeInt size, SizeInt size2) {
				System.out.println("Size is different: "+size+" "+size2);
			}
			
			@Override
			public void differentPixel(int x, int y, int colA, int colB) {
				System.out.println("Different pixel: ["+x+","+y+"] "+RGBAColor.fromIntPixel(colA)+" "+RGBAColor.fromIntPixel(colB));
			}
			
			@Override
			public void differentComponentOrder(ENativeImageComponentOrder componentOrder,
					ENativeImageComponentOrder componentOrder2) {
				System.out.println("Different component order: "+componentOrder+" "+componentOrder2);
			}
			
			@Override
			public void differentAlphaStorageFormat(ENativeImageAlphaStorageFormat alphaStorageFormat,
					ENativeImageAlphaStorageFormat alphaStorageFormat2) {
				System.out.println("Different alpha storage format: "+alphaStorageFormat+" "+alphaStorageFormat2);
			}
		});
		return 0;
	}

	@Override
	protected IArgs createArgsObject() {
		return new Args();
	}

}
