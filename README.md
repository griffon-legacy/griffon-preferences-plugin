
Preferences management
----------------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/preferences](http://artifacts.griffon-framework.org/plugin/preferences)


Provides a platform agnostic preferences facility, heavily influenced by JDK's
[java.util.prefs.Preferences][1]. Preferences represent a hierarchical data
structure that can be persisted across different running sessions.

Usage
-----
`Preferences` are represented by a tree of nodes. Each node has a name (may not
be unique) and a path (always unique). The root node has "/" as name and path.
No other node may have the character '/' in its name. Nodes may contain
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
defines additional parameters such as `key`, `args`, `defaultValue` and `format`;
these parameters work exactly as shown by `@InjectedResource`
(see [Resource Management][2]).

Here's an example of a Model class defining a preference for a title and a custom
format for a Date property

    package sample

    import griffon.plugins.preferences.Preference
    import griffon.plugins.preferences.PreferencesAware

    @Bindable
    @PreferencesAware
    class SampleModel {
        @Preference(defaultValue='Sample')
        String title

        @Preference(defaultValue='08.04.2013 2:30 PM', format='dd.MM.yyyy h:mm a')
        Date date
    }

When the application is run for the first the `title` property will have "Sample"
as its value. Preferences will be written to disk when the application is shutdown.
Here are the contents of the `default.json` file

    {
        "sample": {
            "SampleModel": {
                "title": "Sample",
                "date": "08.04.2013 2:30 PM"
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

