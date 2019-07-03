package org.sinoc.vm;

/**
 * Factory used to create {@link VMHook} objects
 */
public interface VMHookFactory {
    /**
     * Creates {@link VMHook}
     */
    VMHook create();
}
