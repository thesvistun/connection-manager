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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

import name.svistun.http.Configuration.Config;
import name.svistun.http.Processing.Processor;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ConnectionManager {
    Config config;
    private Map<Object, Connection> connections;
    private Set<Proxy> proxies;
    private List<ProxySource> proxySources;
    private static final Logger log = Logger.getLogger(ConnectionManager.class.getSimpleName());

    public ConnectionManager(String file) throws ConfigurationException {
        connections = new HashMap<>();
        proxies = new HashSet<>();
        config = new Config(file);
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
            int attemptsLimit = 5;
            org.jsoup.Connection connection = Jsoup.connect(proxySource.getUrl());
            for (String name : proxySource.getHeaders().keySet()) {
                connection.header(name, proxySource.getHeaders().get(name));
            }
            try {
                byte count = 0;
                Document doc = null;
                do {
                    try {
                        doc = connection.get();
                    } catch (SocketTimeoutException e) {
                        if (count++ >= 5) {
                            throw new SocketTimeoutException(e.getMessage());
                        }
                    }
                    attemptsLimit--;
                } while (null == doc && attemptsLimit > 0);
                if (attemptsLimit <= 0) {
                    log.warn(String.format("Limit is exceeded: %s", proxySource));
                    continue;
                }
                Processor processor = new Processor();
                proxies = (Set<Proxy>) processor.process(proxySource.getSteps(), doc);
            } catch (HttpStatusException e) {
                log.error(String.format("HttpStatusException when init proxies. Message: %s", e.getMessage()));
                throw new ConnectionException(e.toString());
            } catch (IOException e) {
                throw new ConnectionException(e.toString());
            }
        }
    }
}
