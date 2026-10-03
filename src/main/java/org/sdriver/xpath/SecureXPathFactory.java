/*
 * Copyright (c) 2009, 2026 Vassilios Karakoidas, Dimitrios Mitropoulos.
 * Licensed under the BSD 3-Clause License; see LICENSE.
 */
package org.sdriver.xpath;

import java.nio.file.Path;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.sdriver.xpath.registry.FlatFileRegistry;
import org.sdriver.xpath.registry.MemoryRegistry;
import org.sdriver.xpath.registry.Registry;

/**
 * The entry point: an {@link XPathFactory} whose {@link XPath} objects are
 * {@link SecureXPath}s over the platform's default implementation.
 *
 * <pre>{@code
 * XPathFactory xpf = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
 *         "org.sdriver.xpath.SecureXPathFactory", classLoader);
 * xpf.setFeature("TrainingMode", true);
 * }</pre>
 *
 * <p>The features it understands are listed in {@link SecureXPathFeature}; any other is passed
 * to the wrapped factory. Every XPath created by one factory shares that factory's registry.
 *
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 */
public class SecureXPathFactory extends XPathFactory {
    /** System property naming the file {@code FlatFileRegistry} uses. */
    public static final String REGISTRY_FILE_PROPERTY = "sdriver.xpath.registry";

    private final XPathFactory wrappedFactory;
    private volatile boolean trainingMode;
    private volatile Registry registry;

    public SecureXPathFactory() {
        // newDefaultInstance, not newInstance: the lookup newInstance performs could find
        // this class again if it were ever registered as the platform's XPathFactory.
        this.wrappedFactory = XPathFactory.newDefaultInstance();
        this.trainingMode = false;
        this.registry = new MemoryRegistry();
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry r) {
        this.registry = r;
    }

    @Override
    public boolean getFeature(String name) throws XPathFactoryConfigurationException {
        switch (SecureXPathFeature.getFeature(name)) {
            case TRAINING_MODE:
                return trainingMode;
            case MEMORY_REGISTRY:
                return registry instanceof MemoryRegistry;
            case FLATFILE_REGISTRY:
                return registry instanceof FlatFileRegistry;
            default:
                return wrappedFactory.getFeature(name);
        }
    }

    @Override
    public boolean isObjectModelSupported(String objectModel) {
        return wrappedFactory.isObjectModelSupported(objectModel);
    }

    @Override
    public XPath newXPath() {
        SecureXPath sxpath = new SecureXPath(wrappedFactory.newXPath());
        sxpath.setTrainingMode(trainingMode);
        sxpath.setRegistry(registry);
        return sxpath;
    }

    @Override
    public void setFeature(String name, boolean value) throws XPathFactoryConfigurationException {
        switch (SecureXPathFeature.getFeature(name)) {
            case TRAINING_MODE:
                trainingMode = value;
                return;
            case MEMORY_REGISTRY:
                if (value) {
                    registry = new MemoryRegistry();
                }
                return;
            case FLATFILE_REGISTRY:
                if (value) {
                    registry = new FlatFileRegistry(
                            Path.of(System.getProperty(REGISTRY_FILE_PROPERTY, FlatFileRegistry.DEFAULT_FILE)));
                }
                return;
            default:
                wrappedFactory.setFeature(name, value);
        }
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        wrappedFactory.setXPathFunctionResolver(resolver);
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        wrappedFactory.setXPathVariableResolver(resolver);
    }
}
