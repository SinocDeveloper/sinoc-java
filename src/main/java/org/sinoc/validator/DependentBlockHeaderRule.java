package org.sinoc.validator;

import org.sinoc.core.BlockHeader;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Parent class for {@link BlockHeader} validators
 * which run depends on the header of another block
 *
 */
public abstract class DependentBlockHeaderRule extends AbstractValidationRule {

    protected List<String> errors = new LinkedList<>();

    public List<String> getErrors() {
        return errors;
    }

    public void logErrors(Logger logger) {
        if (logger.isErrorEnabled())
            for (String msg : errors) {
                logger.warn("{} invalid: {}", getEntityClass().getSimpleName(), msg);
            }
    }

    @Override
    public Class getEntityClass() {
        return BlockHeader.class;
    }

    /**
     * Runs header validation and returns its result
     *
     * @param header block's header
     * @param dependency header of the dependency block
     * @return true if validation passed, false otherwise
     */
    abstract public boolean validate(BlockHeader header, BlockHeader dependency);

}
