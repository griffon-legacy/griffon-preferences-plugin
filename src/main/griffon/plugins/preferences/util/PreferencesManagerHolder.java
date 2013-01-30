/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.preferences.util;

import griffon.plugins.preferences.PreferencesManager;

/**
 * @author Andres Almiray
 */
public class PreferencesManagerHolder {
    private static PreferencesManager preferencesManager;

    public static PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public static void setPreferencesManager(PreferencesManager preferencesManager) {
        PreferencesManagerHolder.preferencesManager = preferencesManager;
    }
}
