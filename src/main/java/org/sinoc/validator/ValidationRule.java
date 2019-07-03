package org.sinoc.validator;

import java.util.List;

/**
 * Topmost interface for validation rules
 *
 */
public interface ValidationRule {

    /**
     * Returns errors occurred during most recent validation run
     *
     * @return list of errors
     */
    List<String> getErrors();
}
