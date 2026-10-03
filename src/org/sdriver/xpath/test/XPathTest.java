package org.sdriver.xpath.test;

import javax.xml.xpath.XPathFactory;

/**
 * 
 * 
 * @author Vasileios Karakoidas (bkarak@aueb.gr)
 */
public abstract class XPathTest {
	protected boolean secure;
	protected XPathFactory xpathfactory;
	
	protected XPathTest(boolean secure) {
		this.secure = secure;
		try {
			if (secure) {
				xpathfactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
					 									"org.sdriver.xpath.SecureXPathFactory", 
					 									Thread.currentThread().getContextClassLoader());
				xpathfactory.setFeature("MemoryRegistry", true);
			} else {
				xpathfactory = XPathFactory.newInstance();				
			}
		} catch (Exception e) {
			System.out.println("Could not initialize XPath library");
		}
	}
	
	public abstract void execute();
}
