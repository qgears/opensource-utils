package hu.qgears.opengl.commons.test.manual;

import java.nio.ByteBuffer;

import hu.qgears.commons.mem.DefaultJavaNativeMemory;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.images.text.RGBAColor;
import hu.qgears.opengl.kms.KMS;
import hu.qgears.opengl.osmesa.Log4Init;

public class ManualTestKMS {
	public static void main(String[] args) {
		Log4Init.init();
		KMS kms=new KMS();
		kms.enterKmsFullscreen();
		for(int i=0;i<60*5;++i)
		{
			ByteBuffer curr=kms.getCurrentBackBufferPtr(0);
			NativeImage im=new NativeImage(new DefaultJavaNativeMemory(curr), new SizeInt(1920, 1080), ENativeImageComponentOrder.RGBA, 4);
			im.setPixel(100, 100, RGBAColor.WHITE);
			kms.swapBuffers(0);
		}
		kms.dispose();
	}
}
