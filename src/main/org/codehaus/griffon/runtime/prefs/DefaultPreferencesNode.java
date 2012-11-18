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

package org.codehaus.griffon.runtime.prefs;

import griffon.plugins.preferences.NodeChangeEvent;
import griffon.plugins.preferences.PreferenceChangeEvent;
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andres Almiray
 */
public class DefaultPreferencesNode extends AbstractPreferencesNode {
    private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
    private final Map<String, PreferencesNode> nodes = new LinkedHashMap<String, PreferencesNode>();

    public DefaultPreferencesNode(Preferences preferences, String name) {
        this(preferences, null, name);
    }

    public DefaultPreferencesNode(Preferences preferences, PreferencesNode parent, String name) {
        super(preferences, parent, name);
    }

    public Object getAt(String key) {
        return properties.get(key);
    }

    public void putAt(String key, Object value) {
        Object oldValue = properties.get(key);
        properties.put(key, value);
        if (!areEqual(oldValue, value)) {
            firePreferencesChanged(path(), key, oldValue, value);
        }
    }

    private void firePreferencesChanged(String path, String key, Object oldValue, Object newValue) {
        preferences.preferenceChanged(new PreferenceChangeEvent(path, key, oldValue, newValue));
    }

    public void remove(String key) {
        Object oldValue = properties.remove(key);
        if (oldValue != null) firePreferencesChanged(path(), key, oldValue, null);
    }

    public void clear() {
        properties.clear();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public String[] keys() {
        return properties.keySet().toArray(new String[properties.size()]);
    }

    public Map<String, PreferencesNode> children() {
        return Collections.unmodifiableMap(nodes);
    }

    public DefaultPreferencesNode createChildNode(String nodeName) {
        return new DefaultPreferencesNode(preferences, this, nodeName);
    }

    public void storeChildNode(String nodeName, PreferencesNode node) {
        nodes.put(nodeName, node);
        preferences.nodeChanged(new NodeChangeEvent(node.path(), NodeChangeEvent.Type.ADDED));
    }

    public PreferencesNode removeChildNode(String nodeName) {
        PreferencesNode node = nodes.remove(nodeName);
        if (node != null) {
            preferences.nodeChanged(new NodeChangeEvent(node.path(), NodeChangeEvent.Type.REMOVED));
        }
        return node;
    }

    public PreferencesNode getChildNode(String nodeName) {
        return nodes.get(nodeName);
    }
}
