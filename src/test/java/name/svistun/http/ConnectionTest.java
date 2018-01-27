/*
 * MIT License
 *
 * Copyright (c) 2017 Svistunov Aleksey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package name.svistun.http;

import junit.framework.TestCase;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

public class ConnectionTest extends TestCase {
    public void testGetResult() {
        try {
            final Logger log = Logger.getLogger(ConnectionTest.class.getSimpleName());
            ConnectionManager connectionManager = new ConnectionManager("src/test/resources/config.xml");
            Connection connection = connectionManager.getConnections("test.ru", 4, "table[id=paper]", new ArrayList<>());
            connection.setAttemptAmount(1);
            connection.setConnectionTimeout(4);
            String url = "http://oltest.ru/";
            connection.doRequest(url);
            int attempts = 10;
            int count = attempts;
            int timeout = 5*1000;
            Document doc;
            do {
                doc = connection.getResult();
                count--;
                if (null == doc) {
                    log.warn(String.format("Failed. Waiting %s secs", timeout/1000));
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        log.error(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }
            } while (null == doc && count > 0);
            assertFalse(String.format("Unable to get document of %s after %s attempts", url, attempts), null == doc);
            log.info("Success");
        } catch (ConfigurationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
