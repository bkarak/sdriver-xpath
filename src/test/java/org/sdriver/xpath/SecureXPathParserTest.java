package org.sdriver.xpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SecureXPathParserTest {
    private static String strip(String q) {
        return new SecureXPathParser(q).strip();
    }

    @Test
    void replacesStringLiteralsInEitherQuote() {
        assertEquals("// user [ name = ? ]", strip("//user[name='alice']"));
        assertEquals("// user [ name = ? ]", strip("//user[name=\"alice\"]"));
        assertEquals(strip("//user[name=\"bob\"]"), strip("//user[name='alice']"));
    }

    @Test
    void replacesNumbers() {
        assertEquals("/ bookstore / book [ price > ? ] / title", strip("/bookstore/book[price>35]/title"));
        assertEquals(strip("//a[@x=1.5]"), strip("//a[@x=7]"));
        assertEquals(strip("//a[@x=.5]"), strip("//a[@x=7]"));
    }

    @Test
    void keepsNamesWithHyphensAndDots() {
        // The 2009 parser removed every '-' and '.' before anything else, so these collapsed.
        assertEquals("author [ last-name [ position ( ) = ? ] = ? ]",
                strip("author[last-name [position()=1]= 'Bob']"));
        assertNotEquals(strip("//last-name"), strip("//lastname"));
    }

    @Test
    void keepsSelfAndParentSteps() {
        assertNotEquals(strip("./a"), strip("../a"));
        assertEquals("// EXAMPLE / CUSTOMER [ contains ( . , ? ) ]",
                strip("//EXAMPLE/CUSTOMER[contains(.,'Smith')]"));
    }

    @Test
    void keepsNamesThatLookLikeHexNumbers() {
        // The 2009 number rule ate hex-letter runs after '(', ',' or '=', operator included.
        assertNotEquals(strip("//a[count(b)=1]"), strip("//a[count(c)=1]"));
        assertNotEquals(strip("//a[@x=1]"), strip("//a[@x>1]"));
    }

    @Test
    void separatesAxesFromNames() {
        assertEquals("child :: a / ancestor :: *", strip("child::a/ancestor::*"));
        assertEquals("// xs:date", strip("//xs:date"));
    }

    @Test
    void ignoresWhitespace() {
        assertEquals(strip("//a[ @x = 'v' ]"), strip("//a[@x='v']"));
    }

    @Test
    void keepsAnUnterminatedLiteralVerbatim() {
        assertEquals("// a [ @ x = 'v ]", strip("//a[@x='v ]"));
    }

    @Test
    void foldsANegativeSignIntoTheNumber() {
        // A negative value is still a value; 2.0.0 kept the '-' and refused it.
        assertEquals(strip("//a[@x=5]"), strip("//a[@x=-5]"));
        assertEquals(strip("//a[f(1, 2)]"), strip("//a[f(-1, -.5)]"));
        assertEquals(strip("//a[@x=1 and @y>2]"), strip("//a[@x=1 and @y>-2]"));
    }

    @Test
    void keepsABinaryMinus() {
        assertEquals("// a [ @ x - ? ]", strip("//a[@x - 5]"));
        assertEquals("// a [ ? - ? ]", strip("//a[3 - -5]"));
        assertNotEquals(strip("//a[@x=5]"), strip("//a[@x=@y - 5]"));
    }
}
