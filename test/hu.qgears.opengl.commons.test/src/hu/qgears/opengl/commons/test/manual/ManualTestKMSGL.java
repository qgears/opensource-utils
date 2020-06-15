package hu.qgears.opengl.commons.test.manual;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.kmsgl.KMSGL;
import hu.qgears.opengl.libinput.Libinput;
import hu.qgears.opengl.libinput.LibinputEvent;
import hu.qgears.opengl.libinput.LibinputMouse;
import hu.qgears.opengl.osmesa.Log4Init;

public class ManualTestKMSGL {
	static boolean exit=false;
	public static void main(String[] args) throws InterruptedException {
		Log4Init.init();
		Libinput li=new Libinput();
		li.keyboard.addListener(new UtilEventListener<LibinputEvent>() {
			
			@Override
			public void eventHappened(LibinputEvent msg) {
				if(msg.a==1)
				{
					exit=true;
				}
			}
		});
		try
		{
			KMSGL kms=new KMSGL();
			try
			{
				kms.enterKmsFullscreen();
				LibinputMouse mouse=new LibinputMouse();
				mouse.init(kms.getSize(), li);
				for(int i=0;i<60*5 && !exit;++i)
				{
					li.poll();
//					NativeImage im=kms.getCurrentBackBuffer();
//					if(mouse.isButtonDown(EMouseButton.LEFT))
//					{
//						NativeImageEditor ed=new NativeImageEditor(im);
//						ed.fillRect(mouse.getX(), mouse.getY(), 10, 10, RGBAColor.RED);
//					}
//					im.setPixel(mouse.getX(), mouse.getY(), RGBAColor.WHITE);
					kms.swapBuffers();
				}
			}finally
			{
				kms.dispose();
			}
		}finally
		{
			li.dispose();
		}
	}
}
