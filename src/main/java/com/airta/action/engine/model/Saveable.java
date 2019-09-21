package com.airta.action.engine.model;

import java.io.IOException;

/**
 * Object whose state is persisted to XML.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.249
 */
public interface Saveable {
    /**
     * Persists the state of this object into XML.
     *
     * <p>
     * For making a bulk change efficiently.
     *
     * <p>
     * To support listeners monitoring changes to this object
     * @throws IOException
     *      if the persistence failed.
     */
    void save() throws IOException;

    /**
     * {@link Saveable} that doesn't save anything.
     * @since 1.301.
     */
    Saveable NOOP = new Saveable() {
        public void save() throws IOException {
        }
    };
}
