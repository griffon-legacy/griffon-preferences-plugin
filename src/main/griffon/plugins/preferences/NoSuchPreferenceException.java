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

package griffon.plugins.preferences;

/**
 * @author Andres Almiray
 */
public class NoSuchPreferenceException extends RuntimeException {
    private String path;

    /**
     * Create a new exception.
     *
     * @param path preference path
     */
    public NoSuchPreferenceException(String path) {
        super("No preference found for path '" + path + "'");
        this.path = path;
    }

    /**
     * Get the path without a valid value
     *
     * @return The path
     */
    public String getKey() {
        return path;
    }
}