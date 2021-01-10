package com.linkshorter.app.features.email.service.parameters.factory;

import java.util.Map;

public interface TextParametersAbstractFactory {
    Map<String, String> createTextParametersMap(Object object);
}
