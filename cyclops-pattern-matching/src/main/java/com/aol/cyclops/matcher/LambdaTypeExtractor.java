package com.aol.cyclops.matcher;



import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Extract generic type info from Lambda expressions
 * 
 * @see <a href='http://stackoverflow.com/questions/21887358/reflection-type-inference-on-java-8-lambdas'>Discussion on stackoverflow</a>
 * Serialisation approach seems to work more often than ConstantPool approach.
 * 
 * Does not work for MethodReferences.
 * 
 * @author johnmcclean
 *
 */
public class LambdaTypeExtractor {
	private static final ExceptionSoftener softener = ExceptionSoftener.singleton.factory.getInstance();
	
	/**
	 * Extract generic type info from a Serializable Lambda expression
	 * 
	 * @param serializable Serializable lambda expression
	 * @return MethodType info
	 */
	public static MethodType extractType(Serializable serializable){
		
		try{
			return extractChecked(serializable);
		}catch(Exception e){
			softener.throwSoftenedException(e);
			return null;
		}
	}

	private static MethodType extractChecked(Serializable serializable) throws IllegalArgumentException, TypeNotPresentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException{
	 Method method = serializable.getClass().getDeclaredMethod("writeReplace");
     method.setAccessible(true);
     MethodType type = MethodType.fromMethodDescriptorString( ((SerializedLambda) method.invoke(serializable)).getImplMethodSignature(),
    		 serializable.getClass().getClassLoader());
     
     return type;
	}
}
