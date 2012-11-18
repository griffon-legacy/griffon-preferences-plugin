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

import griffon.core.GriffonApplication;
import griffon.plugins.preferences.PreferencesManager;
import griffon.plugins.preferences.PreferencesPersistor;
import griffon.plugins.preferences.factories.PreferencesManagerFactory;
import griffon.plugins.preferences.factories.PreferencesPersistorFactory;
import griffon.plugins.preferences.util.PreferencesManagerHolder;
import griffon.util.ApplicationHolder;
import griffon.util.RunnableWithArgs;
import org.codehaus.griffon.runtime.core.AbstractGriffonAddon;

import java.io.FileNotFoundException;
import java.io.IOException;

import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonExceptionHandler.sanitize;
import static org.codehaus.griffon.runtime.util.GriffonApplicationHelper.safeNewInstance;

/**
 * @author Andres Almiray
 */
public class PreferencesGriffonAddon extends AbstractGriffonAddon {
    public PreferencesGriffonAddon() {
        super(ApplicationHolder.getApplication());
    }

    public void addonInit(final GriffonApplication app) {
        final PreferencesManager preferencesManager = initializePreferencesManager(app);
        final PreferencesPersistor preferencesPersistor = initializePreferencesPersistor(app);

        boolean preferencesWereRead = false;
        try {
            preferencesPersistor.read(preferencesManager);
            preferencesWereRead = true;
        } catch (FileNotFoundException fnfe) {
            // most likely means preferences have not been initialized yet
            // let it continue
            preferencesWereRead = true;
        } catch (IOException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Cannot read preferences", sanitize(e));
            }
        }

        if (preferencesWereRead) {
            app.addApplicationEventListener(GriffonApplication.Event.SHUTDOWN_START.getName(), new RunnableWithArgs() {
                @Override
                public void run(Object[] args) {
                    try {
                        preferencesPersistor.write(preferencesManager);
                    } catch (IOException e) {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("Cannot persist preferences", sanitize(e));
                        }
                    }
                }
            });
        }
    }

    private static final String KEY_PREFERENCES_MANAGER_FACTORY = "preferences.manager.factory";
    private static final String DEFAULT_PREFERENCES_MANAGER_FACTORY = "org.codehaus.griffon.runtime.prefs.factories.DefaultPreferencesManagerFactory";

    private PreferencesManager initializePreferencesManager(GriffonApplication app) {
        String className = getConfigValueAsString(app.getConfig(), KEY_PREFERENCES_MANAGER_FACTORY, DEFAULT_PREFERENCES_MANAGER_FACTORY);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Using " + className + " as PreferencesManagerFactory");
        }
        PreferencesManagerFactory factory = (PreferencesManagerFactory) safeNewInstance(className);
        PreferencesManager preferencesManager = factory.create(app);
        PreferencesManagerHolder.setPreferencesManager(preferencesManager);
        return preferencesManager;
    }

    private static final String KEY_PREFERENCES_PERSISTOR_FACTORY = "preferences.persistor.factory";
    private static final String DEFAULT_PREFERENCES_PERSISTOR_FACTORY = "org.codehaus.griffon.runtime.prefs.factories.JsonPreferencesPersistorFactory";

    private PreferencesPersistor initializePreferencesPersistor(GriffonApplication app) {
        String className = getConfigValueAsString(app.getConfig(), KEY_PREFERENCES_PERSISTOR_FACTORY, DEFAULT_PREFERENCES_PERSISTOR_FACTORY);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Using " + className + " as PreferencesPersistorFactory");
        }
        PreferencesPersistorFactory factory = (PreferencesPersistorFactory) safeNewInstance(className);
        return factory.create(app);
    }
}