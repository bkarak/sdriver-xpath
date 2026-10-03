package org.sdriver.xpath.test;

import javax.xml.xpath.XPath;

public class LoginInjectionXPathTest extends XPathTest {
	
	public LoginInjectionXPathTest() {
		super(true);
	}
	
	private boolean executeLoginQuery(String user, String pass) {
		XPath xpath = xpathfactory.newXPath();
		try {
			xpath.compile("//user[name/text()='"+user+"' and password/text()='"+pass+ "']/account/text()");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void execute() {
		try {
			System.out.print("Executing Login Injection Test ... ");
			xpathfactory.setFeature("TrainingMode", true);
			executeLoginQuery("login", "password");
			xpathfactory.setFeature("TrainingMode", false);
			System.out.println(executeLoginQuery("' or 1=1 or ''='", "foobar") ? "Failed" : "Passed");
		} catch (Exception e) {
			System.out.println("Could enable/disable TrainingMode");
		}
	}
}
