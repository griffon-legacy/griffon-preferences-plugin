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

import griffon.core.GriffonApplication;
import griffon.core.resources.editors.ExtendedPropertyEditor;
import griffon.core.resources.editors.PropertyEditorResolver;
import griffon.plugins.preferences.*;
import griffon.util.CallableWithArgs;
import griffon.util.GriffonNameUtils;
import griffon.util.RunnableWithArgs;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyEditor;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static griffon.util.GriffonExceptionHandler.sanitize;
import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractPreferencesManager implements PreferencesManager {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPreferencesManager.class);
    private static final Object[] NO_ARGS = new Object[0];
    protected final GriffonApplication app;
    private final InstanceStore instanceStore = new InstanceStore();

    public AbstractPreferencesManager(GriffonApplication app) {
        this.app = app;

        app.addApplicationEventListener(GriffonApplication.Event.NEW_INSTANCE.getName(), new RunnableWithArgs() {
            @Override
            public void run(Object[] args) {
                Object instance = args[2];
                injectPreferences(instance);
            }
        });

        app.addApplicationEventListener(GriffonApplication.Event.DESTROY_INSTANCE.getName(), new RunnableWithArgs() {
            @Override
            public void run(Object[] args) {
                Object instance = args[2];
                if (instanceStore.contains(instance)) {
                    instanceStore.remove(instance);
                }
            }
        });
    }

    protected void init() {
        getPreferences().addNodeChangeListener(new NodeChangeListener() {
            public void nodeChanged(NodeChangeEvent event) {
                if (event.getType() == NodeChangeEvent.Type.ADDED) {
                    for (InstanceContainer instanceContainer : instanceStore) {
                        if (instanceContainer.containsPartialPath(event.getPath())) {
                            injectPreferences(instanceContainer.instance());
                        }
                    }
                }
            }
        });

        getPreferences().addPreferencesChangeListener(new PreferenceChangeListener() {
            public void preferenceChanged(PreferenceChangeEvent event) {
                for (InstanceContainer instanceContainer : instanceStore) {
                    String path = event.getPath();
                    if (PreferencesNode.PATH_SEPARATOR.equals(path)) {
                        path = event.getKey();
                    } else {
                        path += "." + event.getKey();
                    }
                    if (instanceContainer.containsPath(path)) {
                        FieldDescriptor fd = instanceContainer.fields.get(path);
                        Object value = event.getNewValue();

                        if (null != value) {
                            if (!fd.field.getType().isAssignableFrom(value.getClass())) {
                                value = convertValue(fd.field.getType(), value, fd.format);
                            }
                        }
                        setFieldValue(
                            instanceContainer.instance(),
                            fd.field,
                            fd.fqFieldName,
                            value
                        );
                    }
                }
            }
        });
    }

    public GriffonApplication getApp() {
        return app;
    }

    public void save(Object instance) {
        if (instance == null) return;

        List<PreferenceDescriptor> fieldsToSaved = new LinkedList<PreferenceDescriptor>();
        Class klass = instance.getClass();
        do {
            harvestFields(klass, instance, fieldsToSaved);
            klass = klass.getSuperclass();
        } while (null != klass);

        doSavePreferences(instance, fieldsToSaved);
    }

    protected void injectPreferences(Object instance) {
        if (null == instance) return;

        List<PreferenceDescriptor> fieldsToBeInjected = new LinkedList<PreferenceDescriptor>();
        Class klass = instance.getClass();
        do {
            harvestFields(klass, instance, fieldsToBeInjected);
            klass = klass.getSuperclass();
        } while (null != klass);

        doPreferencesInjection(instance, fieldsToBeInjected);
        if (instance.getClass().getAnnotation(PreferencesAware.class) != null && !instanceStore.contains(instance)) {
            List<FieldDescriptor> fields = new LinkedList<FieldDescriptor>();
            for (PreferenceDescriptor pd : fieldsToBeInjected) {
                fields.add(new FieldDescriptor(pd.field, pd.fqFieldName, pd.path, pd.format));
            }
            instanceStore.add(instance, fields);
        }
    }

    protected void harvestFields(Class klass, Object instance, List<PreferenceDescriptor> fieldsToBeInjected) {
        for (Field field : klass.getDeclaredFields()) {
            if (field.isSynthetic()) continue;
            final Preference annotation = field.getAnnotation(Preference.class);
            if (null == annotation) continue;

            String fqFieldName = field.getDeclaringClass().getName().replace('$', '.') + "." + field.getName();
            String path = "/" + field.getDeclaringClass().getName().replace('$', '/').replace('.', '/') + "." + field.getName();
            String key = annotation.key();
            String[] args = annotation.args();
            String defaultValue = annotation.defaultValue();
            String resolvedPath = !isBlank(key) ? key : path;
            String format = annotation.format();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Field " + fqFieldName +
                    " of instance " + instance +
                    " [path='" + resolvedPath +
                    "', args='" + Arrays.toString(args) +
                    "', defaultValue='" + defaultValue +
                    "', format='" + format +
                    "'] is marked for preference injection.");
            }

            fieldsToBeInjected.add(new PreferenceDescriptor(field, fqFieldName, path, args, defaultValue, format));
        }
    }

    protected void doPreferencesInjection(Object instance, List<PreferenceDescriptor> fieldsToBeInjected) {
        for (PreferenceDescriptor pd : fieldsToBeInjected) {
            Object value = resolvePreference(pd.path, pd.args, pd.defaultValue);

            if (null != value) {
                if (!pd.field.getType().isAssignableFrom(value.getClass())) {
                    value = convertValue(pd.field.getType(), value, pd.format);
                }
                setFieldValue(instance, pd.field, pd.fqFieldName, value);
            }
        }
    }

    protected void doSavePreferences(Object instance, List<PreferenceDescriptor> fieldsToSaved) {
        for (PreferenceDescriptor pd : fieldsToSaved) {
            Object value = getFieldValue(instance, pd.field, pd.fqFieldName);
            String[] parsedPath = parsePath(pd.path);
            final PreferencesNode node = getPreferences().node(parsedPath[0]);
            final String key = parsedPath[1];
            if (value != null) {
                // Convert value only if pd.format is not null
                if (!isBlank(pd.format)) {
                    PropertyEditor propertyEditor = resolvePropertyEditor(value.getClass(), pd.format);
                    if(propertyEditor != null) {
                        propertyEditor.setValue(value);
                        value = propertyEditor.getAsText();
                    }
                }
                node.putAt(key, value);
            } else {
                node.remove(key);
            }
        }
    }

    protected Object resolvePreference(String path, String[] args, String defaultValue) {
        String[] parsedPath = parsePath(path);
        final PreferencesNode node = getPreferences().node(parsedPath[0]);
        final String key = parsedPath[1];
        if (node.containsKey(key)) {
            return evalPreferenceWithArguments(node.getAt(key), args);
        } else {
            node.putAt(key, defaultValue);
            return defaultValue;
        }
    }

    protected Object evalPreferenceWithArguments(Object value, Object[] args) {
        if (value instanceof Closure) {
            Closure closure = (Closure) value;
            return closure.call(args);
        } else if (value instanceof CallableWithArgs) {
            CallableWithArgs callable = (CallableWithArgs) value;
            return callable.call(args);
        } else if (value instanceof CharSequence) {
            return formatPreferenceValue(String.valueOf(value), args);
        }
        return value;
    }

    protected String formatPreferenceValue(String resource, Object[] args) {
        return MessageFormat.format(resource, args);
    }

    protected Object convertValue(Class<?> type, Object value, String format) {
        PropertyEditor propertyEditor = resolvePropertyEditor(type, format);
        if (null == propertyEditor) return value;
        if (value instanceof CharSequence) {
            propertyEditor.setAsText(String.valueOf(value));
        } else {
            propertyEditor.setValue(value);
        }
        return propertyEditor.getValue();
    }

    protected PropertyEditor resolvePropertyEditor(Class<?> type, String format) {
        PropertyEditor propertyEditor = PropertyEditorResolver.findEditor(type);
        if (propertyEditor instanceof ExtendedPropertyEditor) {
            ((ExtendedPropertyEditor) propertyEditor).setFormat(format);
        }
        return propertyEditor;
    }

    protected void setFieldValue(Object instance, Field field, String fqFieldName, Object value) {
        String setter = GriffonNameUtils.getSetterName(field.getName());
        try {
            InvokerHelper.invokeMethod(instance, setter, value);
        } catch (MissingMethodException mme) {
            try {
                field.setAccessible(true);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Cannot set value on field " + fqFieldName + " of instance " + instance, sanitize(e));
                }
            }
        }
    }

    protected Object getFieldValue(Object instance, Field field, String fqFieldName) {
        String getter = GriffonNameUtils.getGetterName(field.getName());
        try {
            return InvokerHelper.invokeMethod(instance, getter, NO_ARGS);
        } catch (MissingMethodException mme) {
            try {
                field.setAccessible(true);
                return field.get(instance);
            } catch (IllegalAccessException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Cannot get value on field " + fqFieldName + " of instance " + instance, sanitize(e));
                }
            }
        }
        return null;
    }

    protected String[] parsePath(String path) {
        int split = path.indexOf(".");
        String head = split < 0 ? path : path.substring(0, split);
        String tail = split > 0 ? path.substring(split + 1) : null;
        return new String[]{head, tail};
    }

    private static class InstanceStore implements Iterable<InstanceContainer> {
        private final List<InstanceContainer> instances = new CopyOnWriteArrayList<InstanceContainer>();

        private void add(Object instance, List<FieldDescriptor> fields) {
            if (null == instance) return;
            instances.add(new InstanceContainer(instance, fields));
        }

        private void remove(Object instance) {
            if (null == instance) return;
            InstanceContainer subject = null;
            for (InstanceContainer instance1 : instances) {
                subject = instance1;
                Object candidate = subject.instance();
                if (instance.equals(candidate)) {
                    break;
                }
            }
            if (subject != null) instances.remove(subject);
        }

        private boolean contains(Object instance) {
            if (null == instance) return false;
            for (InstanceContainer instanceContainer : instances) {
                Object candidate = instanceContainer.instance();
                if (instance.equals(candidate)) {
                    return true;
                }
            }
            return false;
        }

        public Iterator<InstanceContainer> iterator() {
            final Iterator<InstanceContainer> it = instances.iterator();
            return new Iterator<InstanceContainer>() {
                public boolean hasNext() {
                    return it.hasNext();
                }

                public InstanceContainer next() {
                    return it.next();
                }

                public void remove() {
                    it.remove();
                }
            };
        }
    }

    private static class InstanceContainer {
        private final WeakReference<Object> instance;
        private final Map<String, FieldDescriptor> fields = new LinkedHashMap<String, FieldDescriptor>();

        private InstanceContainer(Object instance, List<FieldDescriptor> fields) {
            this.instance = new WeakReference<Object>(instance);
            for (FieldDescriptor fd : fields) {
                this.fields.put(fd.path, fd);
            }
        }

        private Object instance() {
            return instance.get();
        }

        private boolean containsPath(String path) {
            for (String p : fields.keySet()) {
                if (p.equals(path)) return true;
            }
            return false;
        }

        public boolean containsPartialPath(String path) {
            for (String p : fields.keySet()) {
                if (p.startsWith(path + ".")) return true;
            }
            return false;
        }
    }

    private static class PreferenceDescriptor {
        private final Field field;
        private final String fqFieldName;
        private final String path;
        private final String[] args;
        private final String defaultValue;
        private final String format;

        private PreferenceDescriptor(Field field, String fqFieldName, String path, String[] args, String defaultValue, String format) {
            this.field = field;
            this.fqFieldName = fqFieldName;
            this.path = path;
            this.args = args;
            this.defaultValue = defaultValue;
            this.format = format;
        }
    }

    private static class FieldDescriptor {
        private final Field field;
        private final String fqFieldName;
        private final String path;
        private final String format;

        private FieldDescriptor(Field field, String fqFieldName, String path, String format) {
            this.field = field;
            this.fqFieldName = fqFieldName;
            this.path = path;
            this.format = format;
        }
    }
}
