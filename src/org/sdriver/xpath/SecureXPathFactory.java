/*
Copyright 2009,
Vassilios Karakoidas (bkarak@aueb.gr)
Dimitrios Mitropoulos (dimitro@aueb.gr)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY <copyright holder> ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.sdriver.xpath;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.sdriver.xpath.registry.FlatFileRegistry;
import org.sdriver.xpath.registry.InactiveRegistry;
import org.sdriver.xpath.registry.MemoryRegistry;
import org.sdriver.xpath.registry.Registry;

/**
 * 
 * 
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 *
 */
public class SecureXPathFactory extends XPathFactory {
    private XPathFactory wrappedFactory;
    private boolean trainingMode;
    private Registry registry;

    public SecureXPathFactory() {
        super();
        this.trainingMode = false;
        this.wrappedFactory = XPathFactory.newInstance();
        this.registry = InactiveRegistry.DEFAULT_INSTANCE;
    }

    @Override
    public boolean getFeature(String name) throws XPathFactoryConfigurationException {
        return wrappedFactory.getFeature(name);
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
        SecureXPathFeature sxpf = SecureXPathFeature.getFeature(name);
        if (sxpf == SecureXPathFeature.TRAINING_MODE) {
            trainingMode = value;
            return;
        } else if (sxpf == SecureXPathFeature.MEMORY_REGISTRY) {
            registry = new MemoryRegistry();
            return;
        } else if (sxpf == SecureXPathFeature.FLATFILE_REGISTRY) {
            registry = new FlatFileRegistry();
            return;
        }
            
        wrappedFactory.setFeature(name, value);
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
