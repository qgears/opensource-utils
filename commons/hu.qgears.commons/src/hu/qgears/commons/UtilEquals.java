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
			//every exceptional case is treated as not equal parameters
		}
		return false;
	}
	/**
	 * Safe compare for equals:
	 * null equals null
	 * null does not equal anything else
	 * if objA is not null its equal is called to objB
	 * @param objA
	 * @param objB
	 * @return
	 * @since 3.0
	 */
	public static boolean safeEquals(Object objA, Object objB) {
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
			//every exceptional case is treated as not equal parameters
		}
		return false;
	}
}
