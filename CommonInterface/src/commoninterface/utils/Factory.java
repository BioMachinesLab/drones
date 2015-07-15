package commoninterface.utils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Factory implements Serializable{

	public final static Map<Class<?>, Class<?>> map = initializeMap();

	private static Map<Class<?>, Class<?>> initializeMap() {
		Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
		map.put(Boolean.class, boolean.class);
		map.put(Byte.class, byte.class);
		map.put(Short.class, short.class);
		map.put(Character.class, char.class);
		map.put(Integer.class, int.class);
		map.put(Long.class, long.class);
		map.put(Float.class, float.class);
		map.put(Double.class, double.class);

		return map;
	}

	public static Object getInstance(String className, Object... objects) {
//		System.out.println("Build by reflection class "+ className); 
		String s = "";
		try {
			Constructor<?>[] constructors = Class.forName(className).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				boolean found = true;
				Class<?>[] params = constructor.getParameterTypes();
				if(params.length == objects.length) {
					found = true;
					for(int i = 0 ; i < objects.length; i++) {
						Class c = objects[i].getClass();
						if(params[i].isPrimitive())
							c = map.get(objects[i].getClass());
						
						s+=params[i].getName()+"=="+c.getName()+"\n";
						
						if(!params[i].isAssignableFrom(c)) {
							found = false;
//							System.out.println(className +" not assignable with "+params[i].getCanonicalName());
							break;
						}
					}
					s+="\n_____\n";
					if(found) {
//						System.out.println("found: "+s);
						return constructor.newInstance(objects);
					}
				}
			}
//			System.out.println(s);
			
//			System.out.println(className+" ## "+Class.forName(className).getName());
		} catch (Exception e) {
			System.out.println("Problem with class "+className);
			e.printStackTrace();
		}
		throw new RuntimeException("Unknown classname: " + className);
	}
}