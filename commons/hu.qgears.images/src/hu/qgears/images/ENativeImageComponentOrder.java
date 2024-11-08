package hu.qgears.images;

import java.nio.ByteOrder;

/**
 * Pixel component orders of a native image in memory.
 * @author rizsi
 *
 */
public enum ENativeImageComponentOrder {
	RGB
	{
		@Override
		public int getNCHannels() {
			return 3;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 16, 8, 8, 8, 0, 0, 0, 24, ByteOrder.BIG_ENDIAN);
		}
	},
	BGR	{
		@Override
		public int getNCHannels() {
			return 3;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 0, 8, 8, 8, 16, 0, 0, 24, ByteOrder.BIG_ENDIAN);
		}
	},
	BGRA	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 3;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 8, 8, 16, 8, 24, 8, 0, 32, ByteOrder.BIG_ENDIAN);
		}
	},
	RGBA	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 3;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 24, 8, 16, 8, 8, 8, 0, 32, ByteOrder.BIG_ENDIAN);
		}
	},
	ARGB	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 16, 8, 8, 8, 0, 8, 24, 32, ByteOrder.BIG_ENDIAN);
		}
	},
	ABGR	{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return new PixelFormat(8, 0, 8, 8, 8, 16, 8, 24, 32, ByteOrder.BIG_ENDIAN);
		}
	},
	MONO	{
		@Override
		public int getNCHannels() {
			return 1;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return null;
		}
	},
	ALPHA	{
		@Override
		public int getNCHannels() {
			return 1;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return null;
		}
	},
	/**
	 * BufferedImage component order.
	 */
	BIM{
		@Override
		public int getNCHannels() {
			return 4;
		}
		@Override
		public int getAlphaChannel() {
			return 0;
		}
		@Override
		protected PixelFormat createPixelFormat() {
			return null;
		}
	};
	private PixelFormat pf;
	ENativeImageComponentOrder()
	{
		pf=createPixelFormat();
	}
	abstract protected PixelFormat createPixelFormat();
	public int getNCHannels()
	{
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Index of the alpha channel
	 * @return -1 means no alpha channel
	 */
	public int getAlphaChannel() {
		return -1;
	}
	public PixelFormat getPixelFormat()
	{
		return pf;
	}
}
