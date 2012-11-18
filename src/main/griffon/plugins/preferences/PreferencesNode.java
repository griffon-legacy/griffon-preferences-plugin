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

import java.util.Map;

/**
 * @author Andres Almiray
 */
public interface PreferencesNode {
    String PATH_SEPARATOR = "/";

    String name();

    String path();

    PreferencesNode parent();

    Object getAt(String key);

    void putAt(String key, Object value);

    boolean isRoot();

    void remove(String key);

    void clear();

    String[] keys();

    boolean containsKey(String key);

    Map<String, PreferencesNode> children();

    PreferencesNode node(Class<?> clazz);

    PreferencesNode node(String path);

    PreferencesNode removeNode(Class<?> clazz);

    PreferencesNode removeNode(String path);

    PreferencesNode getChildNode(String nodeName);

    PreferencesNode createChildNode(String nodeName);

    void storeChildNode(String nodeName, PreferencesNode node);

    PreferencesNode removeChildNode(String nodeName);
}
