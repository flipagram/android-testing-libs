/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.espresso.action;

import android.support.test.testapp.R;
import android.support.test.testapp.TallRowListActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.util.Map;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for operating on data displayed in an adapter.
 */
@LargeTest
public class TallRowAdapterDataIntegrationTest extends ActivityInstrumentationTestCase2<TallRowListActivity> {
  @SuppressWarnings("deprecation")
  public TallRowAdapterDataIntegrationTest() {
    // Supporting froyo.
    super("android.support.test.testapp", TallRowListActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  @SuppressWarnings("unchecked")
  public void testClickAroundList() {

    for(int position : new int[]{0,3,5,3,0}) {
        String item = "item: "+position;
        onData(allOf(is(instanceOf(Map.class)), hasEntry(is(TallRowListActivity.STR), is(item))))
                .onChildView(withId(R.id.item_content))
                .check(matches(isDisplayed()))
        ;

        onData(allOf(is(instanceOf(Map.class)), hasEntry(is(TallRowListActivity.STR), is(item))))
                .onChildView(withId(R.id.item_content))
                .perform(click())
        ;
    }

  }
}
