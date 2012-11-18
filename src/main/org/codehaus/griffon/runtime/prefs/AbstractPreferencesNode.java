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

import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesNode;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPreferencesNode implements PreferencesNode {
    protected final Preferences preferences;
    protected PreferencesNode parent;
    protected final String name;
    private String path;

    public AbstractPreferencesNode(Preferences preferences, PreferencesNode parent, String name) {
        this.preferences = preferences;
        this.parent = parent;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String path() {
        if (null == path) {
            if (null == parent) {
                path = PATH_SEPARATOR;
            } else if (parent.isRoot()) {
                path = parent.path() + name;
            } else {
                path = parent.path() + PATH_SEPARATOR + name;
            }
        }
        return path;
    }

    public PreferencesNode parent() {
        return parent;
    }

    public boolean isRoot() {
        return path().equals(PATH_SEPARATOR);
    }

    protected boolean areEqual(Object oldValue, Object newValue) {
        if (oldValue == newValue) return true;

        if ((oldValue == null && newValue != null) ||
            (oldValue != null && newValue == null)) return false;

        if (oldValue instanceof Map && newValue instanceof Map) {
            return DefaultGroovyMethods.equals((Map) oldValue, (Map) newValue);
        } else if (oldValue instanceof Set && newValue instanceof Set) {
            return DefaultGroovyMethods.equals((Set) oldValue, (Set) newValue);
        } else if (oldValue instanceof List && newValue instanceof List) {
            return DefaultGroovyMethods.equals((List) oldValue, (List) newValue);
        }

        return oldValue.equals(newValue);
    }

    public PreferencesNode node(Class<?> clazz) {
        return clazz != null ? node(clazz.getName()) : null;
    }

    public PreferencesNode node(String path) {
        String[] parsedPath = parsePath(path);
        if (parsedPath == null) return null;
        String nodeName = parsedPath[0];

        PreferencesNode node = getChildNode(nodeName);
        if (node == null) {
            node = createChildNode(nodeName);
            storeChildNode(nodeName, node);
        }
        if (!isBlank(parsedPath[1])) {
            node = node.node(parsedPath[1]);
        }

        return node;
    }

    public PreferencesNode removeNode(Class<?> clazz) {
        return clazz != null ? removeNode(clazz.getName()) : null;
    }

    public PreferencesNode removeNode(String path) {
        String[] parsedPath = parsePath(path);
        if (parsedPath == null) return null;
        String nodeName = parsedPath[0];

        PreferencesNode node = getChildNode(nodeName);
        if (node != null) {
            if (!isBlank(parsedPath[1])) {
                node = node.removeNode(parsedPath[1]);
            } else {
                node = removeChildNode(nodeName);
            }
        }

        return node;
    }

    private String[] parsePath(String path) {
        if (isBlank(path) ||
            (!isRoot() && (path.startsWith(PATH_SEPARATOR)) ||
                path.endsWith(PATH_SEPARATOR))) return null;
        path = path.replace('.', PATH_SEPARATOR.charAt(0));
        if(isRoot() && path.startsWith(PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        int split = path.indexOf(PATH_SEPARATOR);
        String head = split < 0 ? path : path.substring(0, split);
        String tail = split > 0 ? path.substring(split + 1) : null;
        return new String[]{head, tail};
    }
}
