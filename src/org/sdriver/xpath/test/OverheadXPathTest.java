package org.sdriver.xpath.test;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class OverheadXPathTest extends XPathTest {
	
	public OverheadXPathTest(boolean secure) {
		super(secure);
	}

	@Override
	public void execute() {
		String[] queries = { "/orders/customer[@id='foo']/order/item[price >= 5]",
							 "author[last-name [position()=1]= 'Bob']",
							 "//EXAMPLE/CUSTOMER[@id='1' and (@type='B' or @type='C')]",
							 "/bookstore/book[price>35]/title",
							 "//EXAMPLE/CUSTOMER[@id='1' and @type='B']",
							 "//EXAMPLE/CUSTOMER[@id='2' or @type='C']",
							 "//EXAMPLE/CUSTOMER[substring(@type,1,2) ='DE']",
							 "//EXAMPLE/CUSTOMER[contains(@type,'DECEA')]",
							 "//EXAMPLE/CUSTOMER[contains(.,'Smith')]" };

		final int iterations = 10000;
		System.out.println("Measuring compile overhead ... " + ((secure ? "SDriver/XPath" : "JAXP")));
		System.out.println("Number of Iterations: " + iterations);
		System.out.println("Queries tested: " + queries.length);
		
		// set the training mode, and add the IDs
		if (secure) {
			try {
				xpathfactory.setFeature("TrainingMode", true);
			} catch (XPathFactoryConfigurationException e) {
				System.out.println("Could not set the Training Mode ... (true)");
			}
		
			XPath xpathtr = xpathfactory.newXPath();
			for ( String query : queries ) {
				try {
					xpathtr.compile(query);
				} catch (XPathExpressionException e) {
					System.out.println("Compilation of " + query + " failed ...");
				}
			}
		}
		// Now run the benchmark
		if (secure) {
			try {
				xpathfactory.setFeature("TrainingMode", false);
			} catch (XPathFactoryConfigurationException e) {
				System.out.println("Could not set the Training Mode ... (true)");
			}
		}
		XPath xpath = xpathfactory.newXPath();
		// warmup
		for ( String query : queries ) {
			for (int i = 0; i < iterations; i++) {
				try {
					xpath.compile(query);
				} catch (XPathExpressionException e) {
					System.out.println("Could not compile ... " + query + " (" + e.toString() + ")");
					break;
				}
			}
		}

		// actual test
		for ( String query : queries ) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				try {
					xpath.compile(query);
				} catch (XPathExpressionException e) {
					System.out.println("Could not compile ... " + query + " (" + e.toString() + ")");
					break;
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("Input: " + query + " --- " + (end - start) + " msecs");
		}
	}
}
