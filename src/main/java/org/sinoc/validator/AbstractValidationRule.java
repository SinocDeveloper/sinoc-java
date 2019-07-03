package org.sinoc.validator;

/**
 * Holds errors list to share between all rules
 *
 */
public abstract class AbstractValidationRule {

    abstract public Class getEntityClass();
}
