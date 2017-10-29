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
import name.svistun.http.Configuration.Config;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.List;

public class ConfigTest
    extends TestCase {

    public void testGetProxyServers() {
        try {
            Config config = new Config("src/test/resources/config.xml");
            List<ProxySource> proxySources = config.getProxyServers();
            for (ProxySource proxySource : proxySources) {
                assertFalse("Proxy servers' data must be set in config.xml",
                        null == proxySource.getUrl() || proxySource.getUrl().isEmpty() ||
                        null == proxySource.getOffsetStr() || proxySource.getOffsetStr().isEmpty());
            }
        } catch (ConfigurationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
