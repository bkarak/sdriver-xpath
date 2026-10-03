/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.sdriver.xpath.registry.MemoryRegistry;
import org.sdriver.xpath.registry.Registry;
import org.xml.sax.InputSource;

/**
 * An {@link XPath} that checks every query against a {@link Registry} of known identifiers
 * before handing it to the wrapped implementation.
 *
 * <p>In training mode each query's identifier is recorded and the query runs. Otherwise a
 * query whose identifier is not in the registry is refused with an
 * {@link XPathExpressionException} and logged at {@code WARNING} on the
 * {@code org.sdriver.xpath} logger.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public class SecureXPath implements XPath {
    private static final Logger LOG = System.getLogger("org.sdriver.xpath");

    private final XPath wrappedXPath;
    private volatile boolean trainingMode;
    private volatile Registry registry;

    public SecureXPath(XPath impl) {
        this.wrappedXPath = impl;
        this.trainingMode = false;
        this.registry = new MemoryRegistry();
    }

    public boolean isInTrainingMode() {
        return trainingMode;
    }

    public void setTrainingMode(boolean tm) {
        this.trainingMode = tm;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry r) {
        this.registry = r;
    }

    private void check(String expression) throws XPathExpressionException {
        String id = new SecureXPathParser(expression).getUniqueIdentifier();

        if (trainingMode) {
            registry.addID(id);
        } else if (!registry.exists(id)) {
            LOG.log(Level.WARNING, "Refused unknown XPath query {0} (identifier {1})", expression, id);
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }
    }

    @Override
    public XPathExpression compile(String expression) throws XPathExpressionException {
        check(expression);
        return wrappedXPath.compile(expression);
    }

    @Override
    public String evaluate(String expression, Object item) throws XPathExpressionException {
        check(expression);
        return wrappedXPath.evaluate(expression, item);
    }

    @Override
    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
        check(expression);
        return wrappedXPath.evaluate(expression, source);
    }

    @Override
    public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        check(expression);
        return wrappedXPath.evaluate(expression, item, returnType);
    }

    @Override
    public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
        check(expression);
        return wrappedXPath.evaluate(expression, source, returnType);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return wrappedXPath.getNamespaceContext();
    }

    @Override
    public XPathFunctionResolver getXPathFunctionResolver() {
        return wrappedXPath.getXPathFunctionResolver();
    }

    @Override
    public XPathVariableResolver getXPathVariableResolver() {
        return wrappedXPath.getXPathVariableResolver();
    }

    @Override
    public void reset() {
        wrappedXPath.reset();
    }

    @Override
    public void setNamespaceContext(NamespaceContext nsContext) {
        wrappedXPath.setNamespaceContext(nsContext);
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        wrappedXPath.setXPathFunctionResolver(resolver);
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        wrappedXPath.setXPathVariableResolver(resolver);
    }
}
