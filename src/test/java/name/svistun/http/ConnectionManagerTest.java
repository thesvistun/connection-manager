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
import org.junit.Assert;
import org.junit.Ignore;

@Ignore public class ConnectionManagerTest extends TestCase {

    public void testSupplyProxies() {
        try {
            ConnectionManager connectionManager = new ConnectionManager("src/test/resources/config.xml");
            connectionManager.supplyProxies();
            Assert.assertFalse("Common proxies list is empty.", connectionManager.getProxies().isEmpty());
        } catch (ConfigurationException | ConnectionException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
