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

package griffon.plugins.preferences.persistors;

import griffon.core.ApplicationHandler;
import griffon.core.GriffonApplication;
import griffon.core.resources.editors.PropertyEditorResolver;
import griffon.plugins.preferences.Preferences;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesNode;
import griffon.plugins.preferences.PreferencesPersistor;
import griffon.util.Metadata;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyEditor;
import java.io.*;
import java.util.*;

import static griffon.util.ConfigUtils.getConfigValueAsString;

/**
 * @author Andres Almiray
 */
public class JsonPreferencesPersistor implements PreferencesPersistor, ApplicationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonPreferencesPersistor.class);
    private static final String KEY_PREFERENCES_PERSISTOR_LOCATION = "preferences.persistor.location";
    private final GriffonApplication app;

    public JsonPreferencesPersistor(GriffonApplication app) {
        this.app = app;
    }

    public GriffonApplication getApp() {
        return app;
    }

    private InputStream inputStream() throws IOException {
        String fileName = resolvePreferencesFileName();
        if (LOG.isInfoEnabled()) {
            LOG.info("Reading preferences from " + fileName);
        }
        File file = new File(fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return new FileInputStream(file);
    }

    private OutputStream outputStream() throws IOException {
        String fileName = resolvePreferencesFileName();
        if (LOG.isInfoEnabled()) {
            LOG.info("Writing preferences to " + fileName);
        }
        File file = new File(fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return new FileOutputStream(file);
    }

    private String resolvePreferencesFileName() {
        String defaultLocation = System.getProperty("user.home") +
            File.separator + "." +
            Metadata.getCurrent().getApplicationName() +
            File.separator +
            "preferences" +
            File.separator +
            "default.json";
        return getConfigValueAsString(
            app.getConfig(),
            KEY_PREFERENCES_PERSISTOR_LOCATION,
            defaultLocation);
    }

    @SuppressWarnings("unchecked")
    public Preferences read(PreferencesManager preferencesManager) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream());
        Object o = null;
        try {
            o = new JsonSlurper().parse(reader);
        } catch (NullPointerException npe) {
            // This happens if the preferences file is empty
            return preferencesManager.getPreferences();
        }
        reader.close();

        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Top node of persisted Preferences is not a Map!");
        }

        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        Map<String, Object> map = (Map<String, Object>) o;
        readInto(map, node);

        return preferencesManager.getPreferences();
    }

    public void write(PreferencesManager preferencesManager) throws IOException {
        PreferencesNode node = preferencesManager.getPreferences().getRoot();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        writeTo(node, map);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream()));
        writer.write(new JsonBuilder(map).toPrettyString());
        writer.flush();
        writer.close();
    }

    @SuppressWarnings("unchecked")
    private void readInto(Map<String, Object> map, PreferencesNode node) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                readInto((Map<String, Object>) value, node.node(key));
            } else if (value instanceof List ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof CharSequence) {
                node.putAt(key, value);
            } else {
                throw new IllegalArgumentException("Invalid value for '" + node.path() + "." + key + "' => " + value);
            }
        }
    }

    private void writeTo(PreferencesNode node, Map<String, Object> map) {
        for (String key : node.keys()) {
            Object value = node.getAt(key);
            if (value != null) {
                map.put(key, convertValue(value));
            }
        }
        for (Map.Entry<String, PreferencesNode> child : node.children().entrySet()) {
            Map<String, Object> childMap = new LinkedHashMap<String, Object>();
            writeTo(child.getValue(), childMap);
            map.put(child.getKey(), childMap);
        }
    }

    private Object convertValue(Object value) {
        if (value == null ||
            value instanceof Boolean ||
            value instanceof Number ||
            value instanceof CharSequence) {
            return value;
        }

        if (value instanceof Map) {
            Map<String, Object> tmp = new LinkedHashMap<String, Object>();
            Map source = (Map) value;
            for (Object key : source.keySet()) {
                Object val = source.get(key);
                if (val != null) {
                    tmp.put(String.valueOf(key), convertValue(val));
                }
            }
            return tmp;
        } else if (value instanceof Collection) {
            List<Object> tmp = new ArrayList<Object>();
            List source = (List) value;
            for (Object val : source) {
                tmp.add(convertValue(val));
            }
            return tmp;
        } else if (value.getClass().isArray()) {
            List<Object> tmp = new ArrayList<Object>();
            Object[] source = (Object[]) value; // blindly cast to Object[]
            for (Object val : source) {
                tmp.add(convertValue(val));
            }
            return tmp;
        } else {
            PropertyEditor propertyEditor = PropertyEditorResolver.findEditor(value.getClass());
            if (propertyEditor != null) {
                propertyEditor.setValue(value);
                return propertyEditor.getAsText();
            } else {
                return value;
            }
        }
    }
}
