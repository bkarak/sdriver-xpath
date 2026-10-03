package org.sdriver.xpath;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * The paper's overhead measurement (section 3.5): ten queries in the registry, then
 * {@code XPath.compile()} on one of them a million times, plain JAXP against SDriver/XPath,
 * five runs each after a warm-up, reporting the mean.
 *
 * <pre>
 * mvn -q test-compile
 * java -cp target/classes:target/test-classes org.sdriver.xpath.Benchmark [iterations] [runs]
 * </pre>
 */
public final class Benchmark {
    private static final String[] QUERIES = {
        "/orders/customer[@id='foo']/order/item[price >= 5]",
        "author[last-name [position()=1]= 'Bob']",
        "//EXAMPLE/CUSTOMER[@id='1' and (@type='B' or @type='C')]",
        "/bookstore/book[price>35]/title",
        "//EXAMPLE/CUSTOMER[@id='1' and @type='B']",
        "//EXAMPLE/CUSTOMER[@id='2' or @type='C']",
        "//EXAMPLE/CUSTOMER[substring(@type,1,2) ='DE']",
        "//EXAMPLE/CUSTOMER[contains(@type,'DECEA')]",
        "//EXAMPLE/CUSTOMER[contains(.,'Smith')]",
        "//EXAMPLE/CUSTOMER[@type='A']/NAME",
    };
    private static final String MEASURED = QUERIES[2];

    private Benchmark() {
    }

    public static void main(String[] args) throws Exception {
        int iterations = args.length > 0 ? Integer.parseInt(args[0]) : 1_000_000;
        int runs = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        XPathFactory plain = XPathFactory.newDefaultInstance();
        SecureXPathFactory secure = new SecureXPathFactory();
        secure.setFeature("MemoryRegistry", true); // the default now; the 2009 build needed it
        secure.setFeature("TrainingMode", true);
        XPath trainer = secure.newXPath();
        for (String q : QUERIES) {
            measure(trainer, q, 1, 0); // the measured call's own chain, so its identifier matches
        }
        secure.setFeature("TrainingMode", false);

        System.out.printf("Java %s, %s %s, %d iterations, mean of %d runs%n",
                System.getProperty("java.version"), System.getProperty("os.name"),
                System.getProperty("os.arch"), iterations, runs);
        System.out.println("Query: " + MEASURED);

        double jaxp = measure(plain.newXPath(), MEASURED, iterations, runs);
        double sdriver = measure(secure.newXPath(), MEASURED, iterations, runs);
        System.out.printf("JAXP            %,10.0f ms%n", jaxp);
        System.out.printf("SDriver/XPath   %,10.0f ms%n", sdriver);
        System.out.printf("Overhead        %10.0f%%%n", (sdriver / jaxp - 1) * 100);
    }

    private static double measure(XPath xpath, String q, int iterations, int runs)
            throws XPathExpressionException {
        compileAll(xpath, q, iterations); // warm-up
        long total = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            compileAll(xpath, q, iterations);
            total += System.nanoTime() - start;
        }
        return runs == 0 ? 0 : total / 1e6 / runs;
    }

    private static void compileAll(XPath xpath, String q, int iterations) throws XPathExpressionException {
        for (int i = 0; i < iterations; i++) {
            xpath.compile(q);
        }
    }
}
