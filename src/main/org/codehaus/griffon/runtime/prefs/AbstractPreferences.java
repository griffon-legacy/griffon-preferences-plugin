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

package org.codehaus.griffon.runtime.prefs;

import griffon.plugins.preferences.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPreferences implements Preferences {
    private final List<NodeChangeListener> nodeChangeListeners = new ArrayList<NodeChangeListener>();
    private final List<PreferenceChangeListener> changeListeners = new ArrayList<PreferenceChangeListener>();

    public void addNodeChangeListener(NodeChangeListener listener) {
        if (listener == null || nodeChangeListeners.contains(listener)) return;
        nodeChangeListeners.add(listener);
    }

    public void removeNodeChangeListener(NodeChangeListener listener) {
        if (listener == null) return;
        nodeChangeListeners.remove(listener);
    }

    public NodeChangeListener[] getNodeChangeListeners() {
        return nodeChangeListeners.toArray(new NodeChangeListener[nodeChangeListeners.size()]);
    }

    public void addPreferencesChangeListener(PreferenceChangeListener listener) {
        if (listener == null || changeListeners.contains(listener)) return;
        changeListeners.add(listener);
    }

    public void removePreferencesChangeListener(PreferenceChangeListener listener) {
        if (listener == null) return;
        changeListeners.remove(listener);
    }

    public PreferenceChangeListener[] getPreferencesChangeListeners() {
        return changeListeners.toArray(new PreferenceChangeListener[changeListeners.size()]);
    }

    public void preferenceChanged(PreferenceChangeEvent event) {
        for (PreferenceChangeListener listener : changeListeners) {
            listener.preferenceChanged(event);
        }
    }

    public void nodeChanged(NodeChangeEvent event) {
        for (NodeChangeListener listener : nodeChangeListeners) {
            listener.nodeChanged(event);
        }
    }

    public PreferencesNode node(Class<?> clazz) {
        return getRoot().node(clazz);
    }

    public PreferencesNode node(String path) {
        return getRoot().node(path);
    }

    public PreferencesNode removeNode(Class<?> clazz) {
        return getRoot().removeNode(clazz);
    }

    public PreferencesNode removeNode(String path) {
        return getRoot().removeNode(path);
    }
}
