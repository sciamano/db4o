/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o;

import com.db4o.reflect.*;

/**
 * Static array tools.
 */
abstract class Array4 {

    public static int[] dimensions(YapStream a_stream, Object a_object) {
        int count = 0;
        Class clazz = a_object.getClass();
        while (clazz.isArray()) {
            count++;
            clazz = clazz.getComponentType();
        }
        int dim[] = new int[count];
        for (int i = 0; i < count; i++) {
            try {
                dim[i] = reflector(a_stream).getLength(a_object);
                a_object = reflector(a_stream).get(a_object, 0);
            } catch (Exception e) {
                return dim;
            }
        }
        return dim;
    }
    
    public static IArray reflector(YapStream a_stream){
    	return a_stream.i_config.reflector().array();
    }

    private static final Object element(YapStream a_stream, Object a_array, int a_position) {
        try {
            return reflector(a_stream).get(a_array, a_position);
        } catch (Exception e) {
            return null;
        }
    }

	public static final int flatten(
		YapStream a_stream,
        Object a_shaped,
        int[] a_dimensions,
        int a_currentDimension,
        Object[] a_flat,
        int a_flatElement) {
        if (a_currentDimension == (a_dimensions.length - 1)) {
            for (int i = 0; i < a_dimensions[a_currentDimension]; i++) {
            	a_flat[a_flatElement++] = element(a_stream, a_shaped, i);
            }
        } else {
            for (int i = 0; i < a_dimensions[a_currentDimension]; i++) {
                a_flatElement =
                    flatten(
                    	a_stream,
                        reflector(a_stream).get(a_shaped, i),
                        a_dimensions,
                        a_currentDimension + 1,
                        a_flat,
                        a_flatElement);
            }
        }
        return a_flatElement;
    }

	public static final IClass getComponentType(IClass a_class) {
        while (a_class.isArray()) {
            a_class = a_class.getComponentType();
        }
        return a_class;
    }

    public static final boolean isNDimensional(IClass a_class) {
        return a_class.getComponentType().isArray();
    }

	public static final int shape(
		YapStream a_stream,
        Object[] a_flat,
        int a_flatElement,
        Object a_shaped,
        int[] a_dimensions,
        int a_currentDimension) {
        if (a_currentDimension == (a_dimensions.length - 1)) {
            for (int i = 0; i < a_dimensions[a_currentDimension]; i++) {
                reflector(a_stream).set(a_shaped, i, a_flat[a_flatElement++]);
            }
        } else {
            for (int i = 0; i < a_dimensions[a_currentDimension]; i++) {
                a_flatElement =
                    shape(
                    	a_stream,
                        a_flat,
                        a_flatElement,
                        reflector(a_stream).get(a_shaped, i),
                        a_dimensions,
                        a_currentDimension + 1);
            }
        }
        return a_flatElement;
    }

}
