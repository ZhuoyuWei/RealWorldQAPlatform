package test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class A
{
	public int number;
	private String str;
	/**
	 * @name Hahaha
	 * @param para1
	 */
	public void VoidFun(int para1)
	{
		System.out.println("Hello reflect");
	}
	
}

public class TestReflect {

	
	
	
	
	public static void main(String[] args)
	{
		Class classa=A.class;
		Field[] fields=classa.getDeclaredFields();
		for(int i=0;i<fields.length;i++)
		{
			System.out.println(fields[i].getType().getName());
		}
		Annotation[] annos=classa.getDeclaredAnnotations();
		
		for(int i=0;i<annos.length;i++)
		{
			System.out.println(annos[i].getClass().getName());
		}
		
		Method[] methods=classa.getDeclaredMethods();
		System.out.println(methods[0].getReturnType());
	}
	
}
