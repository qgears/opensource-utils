package hu.qgears.opengl.libinput;

public class LibinputEvent {

	public int a,b,c;
	public double da,db;
	public ELibinputEventType type;

	@Override
	public String toString() {
		return String.format("%s (%d, %d, %d), (%f, %f)", type.name(),
				a, b, c, da, db);
	}
}
