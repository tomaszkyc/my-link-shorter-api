package com.linkshorter.app.core.validator;

public interface Validator<T> {

    void validate(T o) throws ValidatorException;
}
