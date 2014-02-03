/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.internal.runner;

import android.util.Log;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * A class for loading JUnit3 and JUnit4 test classes given a set of potential class names.
 */
public class TestLoader {

    private static final String LOG_TAG = "TestLoader";

    private  List<Class<?>> mLoadedClasses = new LinkedList<Class<?>>();
    private  List<Failure> mLoadFailures = new LinkedList<Failure>();

    private PrintStream mWriter;

    /**
     * Create a {@link TestLoader}.
     *
     * @param writer a {@link PrintStream} used for reporting errors.
     */
    public TestLoader(PrintStream writer) {
        mWriter = writer;
    }

    /**
     * Loads the test class from the given class name.
     * <p/>
     * Will store the result internally. Successfully loaded classes can be retrieved via
     * {@link #getLoadedClasses()}, failures via {@link #getLoadFailures()}.
     *
     * @param className the class name to attempt to load
     * @return the loaded class or null.
     */
    public Class<?> loadClass(String className) {
        Class<?> loadedClass = doLoadClass(className);
        if (loadedClass != null) {
            mLoadedClasses.add(loadedClass);
        }
        return loadedClass;
    }

    private Class<?> doLoadClass(String className) {
        try {
            // TODO: InstrumentationTestRunner uses Class.forName(className, false,
            // getTargetContext().getClassLoader()
            // Evaluate if that is needed. Initial testing indicates
            // getTargetContext().getClassLoader() == this.getClass().getClassLoader()
            ClassLoader myClassLoader = this.getClass().getClassLoader();
            return Class.forName(className, false, myClassLoader);
        } catch (ClassNotFoundException e) {
            String errMsg = String.format("Could not find class: %s", className);
            Log.e(LOG_TAG, errMsg);
            mWriter.println(errMsg);
            Description description = Description.createSuiteDescription(className);
            Failure failure = new Failure(description, e);
            mLoadFailures.add(failure);
        }
        return null;
    }

    /**
     * Loads the test class from the given class name.
     * <p/>
     * Similar to {@link #loadClass(String, PrintStream))}, but will ignore classes that are
     * not tests.
     *
     * @param className the class name to attempt to load
     * @return the loaded class or null.
     */
    public Class<?> loadIfTest(String className) {
        Class<?> loadedClass = doLoadClass(className);
        if (loadedClass != null && isTestClass(loadedClass)) {
            mLoadedClasses.add(loadedClass);
            return loadedClass;
        }
        return null;
    }

    /**
     * @return whether this {@link TestLoader} contains any loaded classes or load failures.
     */
    public boolean isEmpty() {
        return mLoadedClasses.isEmpty() && mLoadFailures.isEmpty();
    }

    /**
     * Get the {@link List) of classes successfully loaded via
     * {@link #loadTest(String, PrintStream)} calls.
     */
    public List<Class<?>> getLoadedClasses() {
        return mLoadedClasses;
    }

    /**
     * Get the {@link List) of {@link Failure} that occurred during
     * {@link #loadTest(String, PrintStream)} calls.
     */
    public List<Failure> getLoadFailures() {
        return mLoadFailures;
    }

    /**
     * Determines if given class is a valid test class.
     *
     * @param loadedClass
     * @return <code>true</code> if loadedClass is a test
     */
    private boolean isTestClass(Class<?> loadedClass) {
        try {
            if (Modifier.isAbstract(loadedClass.getModifiers())) {
                logDebug(String.format("Skipping abstract class %s: not a test",
                        loadedClass.getName()));
                return false;
            }
            // TODO: try to find upstream junit calls to replace these checks
            if (junit.framework.Test.class.isAssignableFrom(loadedClass)) {
                return true;
            }
            // TODO: look for a 'suite' method?
            if (loadedClass.isAnnotationPresent(org.junit.runner.RunWith.class)) {
                return true;
            }
            for (Method testMethod : loadedClass.getMethods()) {
                if (testMethod.isAnnotationPresent(org.junit.Test.class)) {
                    return true;
                }
            }
            logDebug(String.format("Skipping class %s: not a test", loadedClass.getName()));
            return false;
        } catch (Exception e) {
            // Defensively catch exceptions - Will throw runtime exception if it cannot load methods.
            // For earlier versions of Android (Pre-ICS), Dalvik might try to initialize a class
            // during getMethods(), fail to do so, hide the error and throw a NoSuchMethodException.
            // Since the java.lang.Class.getMethods does not declare such an exception, resort to a
            // generic catch all.
            // For ICS+, Dalvik will throw a NoClassDefFoundException.
            Log.w(LOG_TAG, String.format("%s in isTestClass for %s", e.toString(),
                    loadedClass.getName()));
            return false;
        } catch (Error e) {
            // defensively catch Errors too
            Log.w(LOG_TAG, String.format("%s in isTestClass for %s", e.toString(),
                    loadedClass.getName()));
            return false;
        }
    }

    /**
     * Utility method for logging debug messages. Only actually logs a message if LOG_TAG is marked
     * as loggable to limit log spam during normal use.
     */
    private void logDebug(String msg) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, msg);
        }
    }
}
