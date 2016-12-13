package com.redhat.ipaas.api;

import org.immutables.value.Value;

@Value.Style(
    jdkOnly = true,
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    defaultAsDefault = true,
    headerComments = true,
    depluralize = true,
    typeAbstract = "*",
    allParameters = true,
    from = "createFrom"
)
public @interface ImmutablesStyle {
}
