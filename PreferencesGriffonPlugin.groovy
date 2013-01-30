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

/**
 * @author Andres Almiray
 */
class PreferencesGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, qt
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-preferences-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Preferences management'
    String description = '''
Provides a platform agnostic preferences facility, heavily influenced by JDK's
[java.util.prefs.Preferences][1]. Preferences represent a hierarchical data
structure that can be persisted across different running sessions.
Usage
-----
`Preferences` are represented by a tree of nodes. Each node has a name (may not
be unique) and a path (always unique). The root node has "/" as name and path.
No other node may have the cahracter '/' in it's name. Nodes may contain
key-value entries.

`Preferences` trigger events whenever a node changes value or nodes are
added/removed. You may register a `griffon.plugins.preferences.PreferenceChangeListener`
to handle the first type of event, and a `griffon.plugins.preferences.NodeChangeListener`
to handle the second one.

`Preferences` may be resolved at any time given a `PreferencesManager`; this
plugin will automatically instantiate a manager given the default configuration.
Preference  values may also be injected following a naming convention. Classes
that participate in preferences injection have their properties annotated with
`@Preference`. Only classes annotated with `@PreferencesAware` will be notified
of updates whenever preferences change value. The `@Preference` annotation
defines additional parameters such as `key`, `args` and `defaultValue`; these
parameters work exactly as shown by `@InjectedResource` (see [Resource Management][2]).

Here's an example of a Model class defining a preference for a title

    package sample
    import griffon.plugins.preferences.Preference
    import griffon.plugins.preferences.PreferencesAware
    @PreferencesAware
    class SampleModel {
        @Bindable @Preference(defaultValue='Sample') String title
    }

When the application is run for the first the `title` property will have "Sample"
as its value. Preferences will be written to disk when the application is shutdown.
Here are the contents of the `default.json` file

    {
        "sample": {
            "SampleModel": {
                "title": "Sample"
            }
        }
    }

If that file is edited so that the title property has a different value then the
new value will be shown the next time the application is launched.

It's worth noting that if a preference cannot be resolved a
`griffon.plugins.preferences.NoSuchPreferenceException` is thrown.

Configuration
-------------
The following configuration flags control how Preferences are handled by an
application. These flags must be defined in `Config.groovy`

### PreferencesManagerFactory

Flag: *preferences.manager.factory*

Type: *griffon.plugins.preferences.factories.PreferencesManagerFactory*

Default: *org.codehaus.griffon.runtime.prefs.factories.DefaultPreferencesManagerFactory*

### PreferencesPersistorFactory

Flag: *preferences.persistor.factory*

Type: *griffon.plugins.preferences.factories.PreferencesPersistorFactory*

Default: *org.codehaus.griffon.runtime.prefs.factories.JsonPreferencesPersistorFactory*

### Preferences Persistor Location

Used by `JsonPreferencesPersistor`, this flag indicates the file name used to
read/write preferences.

Flag: *preferences.persistor.location*

Default: *$USER_HOME/$applicationName/preferences/default.json*


[1]: http://docs.oracle.com/javase/7/docs/api/java/util/prefs/Preferences.html
[2]: http://griffon.codehaus.org/guide/latest/guide/resourceManagement.html
'''
}
