/*
 * Copyright (C) 2014 The Android Open Source Project
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
package android.support.test.internal.runner.junit3;

import android.app.Instrumentation;
import android.os.Bundle;
import android.util.Log;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.internal.builders.SuiteMethodBuilder;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * A {@link RunnerBuilder} that properly injects Android constructs into
 * classes with suite() methods.
 */
public class AndroidSuiteBuilder extends SuiteMethodBuilder {

    private static final String LOG_TAG = "AndroidSuiteBuilder";

    private Instrumentation mInstr;
    private boolean mSkipExecution;
    private final Bundle mBundle;

    public AndroidSuiteBuilder(Instrumentation instr, Bundle bundle, boolean skipExecution) {
        mInstr = instr;
        mBundle = bundle;
        mSkipExecution = skipExecution;
    }

    @Override
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        try {
            if (hasSuiteMethod(testClass)) {
                Test t = SuiteMethod.testFromSuiteMethod(testClass);
                if (!(t instanceof TestSuite)) {
                    // this should not be possible
                    throw new IllegalArgumentException(testClass.getName() +
                            "#suite() did not return a TestSuite");
                }
                if (mSkipExecution) {
                    return new JUnit38ClassRunner(new NoExecTestSuite((TestSuite) t));
                } else {
                    return new JUnit38ClassRunner(new AndroidTestSuite((TestSuite) t,
                            mBundle, mInstr));
                }
            }
        } catch (Throwable e) {
            // log error message including stack trace before throwing to help with debugging.
            Log.e(LOG_TAG, "Error constructing runner", e);
            throw e;
        }
        return null;
    }
}
