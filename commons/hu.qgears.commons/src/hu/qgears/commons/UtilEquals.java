package hu.qgears.commons;

import java.lang.reflect.Field;

public class UtilEquals {

	/**
	 * Compare public fields.
	 * @param objA
	 * @param objB
	 * @return
	 */
	public static boolean equals(Object objA, Object objB) {
		try {
			if(objA.getClass().equals(objB.getClass()))
			{
				Field[] fd=objA.getClass().getFields();
				for(Field f: fd)
				{
					Object a=f.get(objA);
					Object b=f.get(objB);
					if(a==null)
					{
						if(b!=null)
						{
							return false;
						}
					}else
					{
						if(!a.equals(b))
						{
							return false;
						}
					}
				}
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
}
