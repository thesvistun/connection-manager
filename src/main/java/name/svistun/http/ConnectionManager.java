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

import java.util.*;

import name.svistun.http.Configuration.Config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

public class ConnectionManager {
    Config config;
    private Map<Object, Connection> connections;
    private Set<Proxy> proxies;
    private List<ProxySource> proxySources;
    private static final Logger log = Logger.getLogger(ConnectionManager.class.getSimpleName());

    public ConnectionManager(String file) throws ConfigurationException {
        config = new Config(file);
        connections = new HashMap<>();
        proxies = new HashSet<>();
        proxySources = config.getProxyServers();
    }

    Set<Proxy> getProxies() {
        return proxies;
    }

    //todo Config file usage
    void supplyProxies() throws ConnectionException {
        log.info("Init proxies");
        for (ProxySource proxySource : proxySources) {
            log.debug(String.format("Processing: %s", proxySource));
            proxies.addAll(proxySource.getProxies());
        }
    }
}
