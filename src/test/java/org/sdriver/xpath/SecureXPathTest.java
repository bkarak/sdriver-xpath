package org.sdriver.xpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

class SecureXPathTest {
    private static final String USERS =
            "<users>"
            + "<user><name>alice</name><password>a1</password><account>A-100</account></user>"
            + "<user><name>bob</name><password>b2</password><account>B-200</account></user>"
            + "</users>";

    private XPathFactory factory;
    private Document doc;

    @BeforeEach
    void setUp() throws Exception {
        // The way the paper says an application switches it on: by class name.
        factory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                "org.sdriver.xpath.SecureXPathFactory", getClass().getClassLoader());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(USERS)));
    }

    /** The application's one call site for the login query. */
    private String login(String user, String pass) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        return xpath.evaluate("//user[name/text()='" + user + "' and password/text()='" + pass
                + "']/account/text()", doc);
    }

    /** A different call site issuing the very same query. */
    private String loginFromElsewhere(String user, String pass) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        return xpath.evaluate("//user[name/text()='" + user + "' and password/text()='" + pass
                + "']/account/text()", doc);
    }

    /**
     * The identifier covers the whole chain of callers, so training has to go through the
     * same chain the checked call does; each test trains from its own method.
     */
    private void training(boolean on) throws Exception {
        factory.setFeature("TrainingMode", on);
    }

    @Test
    void trainingInAnotherMethodDoesNotCoverThisOne() throws Exception {
        training(true);
        trainElsewhere();
        training(false);
        assertThrows(XPathExpressionException.class, () -> login("alice", "a1"));
    }

    private void trainElsewhere() throws XPathExpressionException {
        login("alice", "a1");
    }

    @Test
    void factoryIsTheSecureOne() {
        assertInstanceOf(SecureXPathFactory.class, factory);
        assertInstanceOf(SecureXPath.class, factory.newXPath());
    }

    @Test
    void untrainedQueriesAreRefused() {
        assertThrows(XPathExpressionException.class, () -> login("alice", "a1"));
    }

    @Test
    void trainedQueryRunsWithAnyValues() throws Exception {
        training(true);
        assertEquals("A-100", login("alice", "a1"));
        training(false);
        assertEquals("A-100", login("alice", "a1"));
        assertEquals("B-200", login("bob", "b2"));
        assertEquals("", login("bob", "wrong"));
    }

    @Test
    void changedQueryIsRefused() throws Exception {
        // The 2009 demo's own case: input that turns the predicate into something else.
        training(true);
        login("alice", "a1");
        training(false);
        assertEquals("A-100", login("alice", "a1"));
        assertThrows(XPathExpressionException.class, () -> login("' or 1=1 or ''='", "foobar"));
    }

    @Test
    void sameQueryFromAnotherCallSiteIsRefused() throws Exception {
        training(true);
        login("alice", "a1");
        training(false);
        assertEquals("A-100", login("alice", "a1"));
        assertThrows(XPathExpressionException.class, () -> loginFromElsewhere("alice", "a1"));
    }

    @Test
    void compileAndEvaluateShareTheIdentifier() throws Exception {
        factory.setFeature("TrainingMode", true);
        XPath xpath = factory.newXPath();
        String q = "count(//user)";
        assertEquals("2", xpath.evaluate(q, doc));
        factory.setFeature("TrainingMode", false);

        XPath checked = factory.newXPath();
        // Same method, so the same identifier whichever entry point is used.
        assertEquals(2.0, checked.compile(q).evaluateExpression(doc, Double.class));
    }

    @Test
    void evaluateExpressionDefaultMethodIsChecked() {
        XPath xpath = factory.newXPath();
        assertThrows(XPathExpressionException.class,
                () -> xpath.evaluateExpression("count(//user)", doc, Double.class));
    }

    @Test
    void featuresReportTheirState() throws Exception {
        assertFalse(factory.getFeature("TrainingMode"));
        assertTrue(factory.getFeature("MemoryRegistry"));
        factory.setFeature("TrainingMode", true);
        assertTrue(factory.getFeature("TrainingMode"));
    }

    @Test
    void otherFeaturesGoToTheWrappedFactory() throws Exception {
        factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        assertTrue(factory.getFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING));
    }
}
