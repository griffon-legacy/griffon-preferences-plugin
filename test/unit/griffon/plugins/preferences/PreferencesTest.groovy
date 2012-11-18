package griffon.plugins.preferences

import org.codehaus.griffon.runtime.prefs.DefaultPreferences

class PreferencesTest extends GroovyTestCase {
    void testSmokeTests() {
        Preferences prefs = new DefaultPreferences()
        assert prefs.root

        PreferencesNode fooNode = prefs.node('/foo')
        assert fooNode
        assert prefs.root.children() == [foo: fooNode]

        fooNode['key'] = 'value'
        assert fooNode['key'] == 'value'

        PreferencesNode fooNode2 = prefs.node('/foo')
        assert fooNode == fooNode2

        PreferencesNode barNode = prefs.node('/foo/bar')
        assert barNode
        assert fooNode.children() == [bar: barNode]
        assert barNode.parent() == fooNode
        assert barNode.path() == '/foo/bar'
        assert barNode.name() == 'bar'

        PrefsChangeListener changeListener = new PrefsChangeListener()
        PrefsStructureListener nodeListener = new PrefsStructureListener()
        prefs.addPreferencesChangeListener(changeListener)
        prefs.addNodeChangeListener(nodeListener)

        assert !changeListener.event
        assert !nodeListener.event

        barNode['key'] = 'value'
        assert !nodeListener.event
        assert changeListener.event
        assert changeListener.event.key == 'key'
        assert changeListener.event.path == '/foo/bar'
        assert !changeListener.event.oldValue
        assert changeListener.event.newValue == 'value'

        barNode['key'] = 'value2'
        assert !nodeListener.event
        assert changeListener.event
        assert changeListener.event.key == 'key'
        assert changeListener.event.path == '/foo/bar'
        assert changeListener.event.oldValue == 'value'
        assert changeListener.event.newValue == 'value2'

        changeListener.event = null
        prefs.node('x/y/z')
        assert !changeListener.event
        assert nodeListener.event
        assert nodeListener.event.type == NodeChangeEvent.Type.ADDED
        assert nodeListener.event.path == '/x/y/z'

        changeListener.event = null
        nodeListener.event = null
        prefs.node('x/y/z')
        assert !changeListener.event
        assert !nodeListener.event

        barNode.node('baz')
        assert !changeListener.event
        assert nodeListener.event
        assert nodeListener.event.type == NodeChangeEvent.Type.ADDED
        assert nodeListener.event.path == '/foo/bar/baz'

        changeListener.event = null
        nodeListener.event = null
        prefs.removeNode('foo/bar')
        assert !changeListener.event
        assert nodeListener.event
        assert nodeListener.event.type == NodeChangeEvent.Type.REMOVED
        assert nodeListener.event.path == '/foo/bar'
    }
}

class PrefsChangeListener implements PreferenceChangeListener {
    PreferenceChangeEvent event

    void preferenceChanged(PreferenceChangeEvent event) {
        this.event = event
    }
}

class PrefsStructureListener implements NodeChangeListener {
    NodeChangeEvent event

    void nodeChanged(NodeChangeEvent event) {
        this.event = event
    }
}