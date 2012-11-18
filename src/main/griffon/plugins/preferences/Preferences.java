/*
 * Copyright 2012 the original author or authors.
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

package griffon.plugins.preferences;

/**
 * @author Andres Almiray
 */
public interface Preferences extends NodeChangeListener, PreferenceChangeListener {
    void addNodeChangeListener(NodeChangeListener listener);

    void removeNodeChangeListener(NodeChangeListener listener);

    NodeChangeListener[] getNodeChangeListeners();

    void addPreferencesChangeListener(PreferenceChangeListener listener);

    void removePreferencesChangeListener(PreferenceChangeListener listener);

    PreferenceChangeListener[] getPreferencesChangeListeners();

    PreferencesNode getRoot();

    PreferencesNode node(Class<?> clazz);

    PreferencesNode node(String path);

    PreferencesNode removeNode(Class<?> clazz);

    PreferencesNode removeNode(String path);
}
