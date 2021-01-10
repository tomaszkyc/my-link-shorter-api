package com.linkshorter.app.features.reports.converter.strategy;

import java.util.function.Function;

public abstract class ConvertStrategy<I, O> {
    public abstract Function<I, O> getConvertFunction();
}
