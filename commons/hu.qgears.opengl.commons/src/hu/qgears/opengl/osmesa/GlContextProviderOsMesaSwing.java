package hu.qgears.opengl.osmesa;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;

import hu.qgears.commons.UtilTimer;
import hu.qgears.commons.mem.DefaultJavaNativeMemoryAllocator;
import hu.qgears.commons.signal.SignalFutureWrapper;
import hu.qgears.images.ENativeImageComponentOrder;
import hu.qgears.images.NativeImage;
import hu.qgears.images.SizeInt;
import hu.qgears.opengl.commons.IGlContextProvider;
import hu.qgears.opengl.commons.UtilGl;
import hu.qgears.opengl.commons.input.EMouseButton;
import hu.qgears.opengl.commons.input.IKeyboard;
import hu.qgears.opengl.commons.input.IMouse;
import hu.qgears.opengl.commons.input.MouseImplCallback;
import hu.qgears.opengl.glut.KeyboardImplGlut;
import lwjgl.standalone.BaseAccessor;

public class GlContextProviderOsMesaSwing implements IGlContextProvider {
	OSMesa osMesa;
	SizeInt size;
	IKeyboard keyboard;
	IMouse mouse;
	JFrame frame;
	JLabel l;
	BufferedImage image;
	NativeImage frameBuffer;
	private Callable<Object> timeout=new Callable<Object>() {
		@Override
		public Object call() throws Exception {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					redraw();
				}
			});
			return null;
		}
	};

	@Override
	public void loadNatives() {
		OSMesaInstance.getInstance();
		BaseAccessor.initLwjglNatives();
		UtilGl.flipY=true;
	}

	@Override
	public SizeInt getClientAreaSize() {
		return size;
	}

	@Override
	public void init() {
		System.out.println("OSMesa Inited Thread: "+Thread.currentThread().getId());
		keyboard=new KeyboardImplGlut();
		mouse=new MouseImplCallback();
	}

	@Override
	public void openWindow(boolean initFullscreen, String initTitle, final SizeInt size) throws Exception {
		System.out.println("OSMesa Window opened: "+Thread.currentThread().getId());
		osMesa=new OSMesa();
		osMesa.createContext();
		this.size=size;
		frameBuffer=NativeImage.create(size, ENativeImageComponentOrder.RGBA, DefaultJavaNativeMemoryAllocator.getInstance());
		osMesa.makeCurrent(frameBuffer);
		GLContext.useContext(osMesa);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				frame = new JFrame();
				frame.setTitle("Simple example");
				frame.setSize(size.getWidth(),
						size.getHeight());
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				image = new BufferedImage(size.getWidth(),
						size.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				l = new JLabel(new ImageIcon(image));
				frame.add(l);
				l.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {
						mouse.addEvent(4, e.getX(), e.getY(), EMouseButton.LEFT, 1);
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						mouse.addEvent(4, e.getX(), e.getY(), EMouseButton.LEFT, 0);
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
				redraw();
			}
		});
	}

	protected void redraw() {
		l.setIcon(new ImageIcon(image));
		UtilTimer.getInstance().executeTimeout(100, timeout);
	}

	@Override
	public boolean isCloseRequested() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processMessages() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		final SignalFutureWrapper<Object> ret=new SignalFutureWrapper<Object>();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				DataBufferInt dbi=(DataBufferInt)image.getRaster().getDataBuffer();
				int[] data=dbi.getData();
				IntBuffer ib=frameBuffer.getBuffer().getJavaAccessor().asIntBuffer();
				for(int i=0;i<data.length;++i)
				{
					data[i]=ib.get()>>8;
				}
				l.setIcon(new ImageIcon(image));
				ret.ready(null, null);
			}
		});
		try {
			Thread.sleep(50);
			ret.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		System.out.println("OSMesa Window content update: "+Thread.currentThread().getId());
//		new RuntimeException().printStackTrace();

		// TODO Auto-generated method stub
		
		// update mouse
		// update keyboard
		
//		UtilGl.drawMinimalScene();
//		GL11.glFinish();
//		new NativeLibPng().saveImage(im, new File("/tmp/gl.png"));
	}

	@Override
	public void dispose() {
		try {
			GLContext.useContext(null);
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		osMesa.disposeContext();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.dispose();
	}

	@Override
	public IKeyboard getKeyboard() {
		return keyboard;
	}

	@Override
	public void setFullScreen(boolean fullscreen) throws Exception {
		// Not implemented in Swing implementation
	}

	@Override
	public boolean isFullScreen() {
		// Not implemented in Swing implementation
		return false;
	}

	@Override
	public IMouse getMouse() {
		return mouse;
	}

	@Override
	public void setVSyncEnabled(boolean vSyncEnabled) {
		// Not implemented in Swing implementation
	}

}
