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

import com.twmacinta.util.MD5;
import java.util.regex.Pattern;

/**
 * 
 * @author Vassilios Karakoidas (bkarak@aueb.gr)
 * @author Dimitrios Mitropoulos (dimitro@aueb.gr)
 */
public class SecureXPathParser {
    private final static Pattern trivia = Pattern.compile("\\+|-|\\.");
    private final static Pattern escapeChar = Pattern.compile("\\'(?:.|[\\n\\r])*?\\'");
    private final static Pattern numbers = Pattern.compile("([<=>,\\(]+\\s*)(?:[0-9A-Fa-f])+");
    private final static Pattern comments = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/");
    
    private String query;

    public SecureXPathParser(String query) {
        this.query = query;
    }

    public String strip() {
        String result = trivia.matcher(query).replaceAll("");
        result = escapeChar.matcher(result).replaceAll("");
        result = numbers.matcher(result).replaceAll("");
        result = comments.matcher(result).replaceAll("");

        return result;
    }

    public String getUniqueIdentifier() {
        String strippedDown = strip();
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        if (stack == null || stack.length <= 1) {
            return null;
        }
        StringBuffer completeTrace = new StringBuffer(strippedDown);
        for ( StackTraceElement ste : stack ) {
        	completeTrace.append(ste.getMethodName());
        }        
        byte b[] = completeTrace.toString().getBytes();

        MD5 md5 = new MD5();
        md5.Update(b);
        
        return md5.asHex();
    }
}
