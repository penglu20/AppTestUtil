/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.pl.testutil;

import android.app.Application;
import android.view.WindowManager;

import com.pl.testutil.utils.CrashHandler;

/**
 * my application class
 * 
 * @author andrewleo
 */
public class TestApplication extends Application {

	private static TestApplication instance;
	private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}
	public static TestApplication getInstance()
	{
		return instance;
	}
	public TestApplication() {
        super();
        instance = this;
        
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //加入崩溃的log的记录功能
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
    
}
