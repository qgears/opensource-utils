package hu.qgears.opengl.commons.test.manual;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.opengl.libinput.Libinput;
import hu.qgears.opengl.libinput.LibinputEvent;
import hu.qgears.opengl.libinput.LibinputKeyboard;
import hu.qgears.opengl.osmesa.Log4Init;

public class ManualTestLibinput {
	public static void main(String[] args) throws InterruptedException {
		Log4Init.init();
		Libinput li=new Libinput();
		LibinputKeyboard k=new LibinputKeyboard();
		k.init(li);
		li.keyboard.addListener(new UtilEventListener<LibinputEvent>() {
			
			@Override
			public void eventHappened(LibinputEvent msg) {
//				System.out.println("key: "+msg.a+", state: "+msg.b);
			}
		});
		while(true)
		{
			li.poll();
			while(k.next())
			{
				System.out.println("Key: "+(k.getEventCharacter()==0?'?':k.getEventCharacter())+" "+k.getEventKey()+" down:"+k.isKeyDown()+" "+k.isShift()+" "+k.isCtrl()+" "+k.isAlt());
			}
			Thread.sleep(16);
		}
	}
}
