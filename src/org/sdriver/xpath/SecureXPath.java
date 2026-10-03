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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.sdriver.xpath.registry.InactiveRegistry;
import org.sdriver.xpath.registry.Registry;

import org.xml.sax.InputSource;

/**
 * 
 * 
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 *
 */
public class SecureXPath implements XPath {
    private XPath wrappedXPath;
    private boolean trainingMode;
    private Registry registry;
    
    public SecureXPath(XPath impl) {
        this.wrappedXPath = impl;
        this.trainingMode = false;
        this.registry = InactiveRegistry.DEFAULT_INSTANCE;
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
    
    private boolean isQueryValid(String query) {
        SecureXPathParser sxpp = new SecureXPathParser(query);
        String id = sxpp.getUniqueIdentifier();
        
        if(trainingMode) {
            registry.addID(id);
            return true;
        } else {
            return registry.exists(id);
        }
    }
    
    @Override
    public XPathExpression compile(String expression) throws XPathExpressionException {
        if(!isQueryValid(expression)) {
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }
        
        return wrappedXPath.compile(expression);
    }

    @Override
    public String evaluate(String expression, Object item) throws XPathExpressionException {
        if(!isQueryValid(expression)) {
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }

        return wrappedXPath.evaluate(expression, item);
    }

    @Override
    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
        if(!isQueryValid(expression)) {
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }
        
        return wrappedXPath.evaluate(expression, source);
    }

    @Override
    public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        if(!isQueryValid(expression)) {
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }
        
        return wrappedXPath.evaluate(expression, item, returnType);
    }

    @Override
    public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
        if(!isQueryValid(expression)) {
            throw new XPathExpressionException("Invalid Expression - " + expression + " - (Security Risk)");
        }

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
