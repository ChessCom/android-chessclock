/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.util;

import java.lang.reflect.Method;
import java.util.*;

public class Reflection {
    public static class MethodBuilder {
        private final Object mInstance;
        private final String mMethodName;
        private final Class<?> mClass;

        private List<Class<?>> mParameterClasses;
        private List<Object> mParameters;
        private boolean mIsAccessible;

        public MethodBuilder(final Object instance, final String methodName) {
            mInstance = instance;
            mMethodName = methodName;

            mParameterClasses = new ArrayList<Class<?>>();
            mParameters = new ArrayList<Object>();

            mClass = (instance != null) ? instance.getClass() : null;
        }

        public <T> MethodBuilder addParam(Class<T> clazz, T parameter) {
            mParameterClasses.add(clazz);
            mParameters.add(parameter);

            return this;
        }

        public MethodBuilder setAccessible() {
            mIsAccessible = true;

            return this;
        }

        public Object execute() throws Exception {
            Class<?>[] classArray = new Class<?>[mParameterClasses.size()];
            Class<?>[] parameterTypes = mParameterClasses.toArray(classArray);

            Method method = getDeclaredMethodWithTraversal(mClass, mMethodName, parameterTypes);

            if (mIsAccessible) {
                method.setAccessible(true);
            }

            Object[] parameters = mParameters.toArray();
            return method.invoke(mInstance, parameters);
        }
    }

    public static Method getDeclaredMethodWithTraversal(Class<?> clazz, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }

        throw new NoSuchMethodException();
    }
}
