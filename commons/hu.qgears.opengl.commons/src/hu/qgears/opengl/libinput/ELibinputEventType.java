package hu.qgears.opengl.libinput;

import java.util.HashMap;

public enum ELibinputEventType {
	key(300),
	pointerMotion(400),
	pointerAbsolute(401),
	pointerButton(402);
	private int typeOrdinal;
	private static HashMap<Integer, ELibinputEventType> typeByOrdinal=new HashMap<Integer, ELibinputEventType>();
	private ELibinputEventType(int typeOrdinal) {
		this.typeOrdinal=typeOrdinal;
	}
	static {
		for(ELibinputEventType t: ELibinputEventType.values())
		{
			typeByOrdinal.put(t.typeOrdinal, t);
		}
	};
	public static ELibinputEventType byOrdinal(int typeOrdinal) {
		return typeByOrdinal.get(typeOrdinal);
	}
}
