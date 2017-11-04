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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.svistun.http.Configuration.Config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
            connection.header("Accept-Language", "ru,en-US;q=0.7,en;q=0.3");
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
                Elements elements = doc.select("table > tbody > tr > td > script");
                ScriptEngineManager factory = new ScriptEngineManager();
                ScriptEngine engine = factory.getEngineByName("JavaScript");
                for (Element element : elements) {
                    StringBuilder sb = new StringBuilder();
                    Pattern pattern = Pattern.compile("^document\\.write\\(.+?>'\\s\\+\\s(.+?)\\s\\+\\s'</a>'\\);$");
                    for (String line : element.data().split("\n")) {
                        line = line.trim();
                        if (line.isEmpty() || line.matches("proxies\\.push\\(.+\\);")) {
                            continue;
                        }
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            sb.append("var result = ").append(matcher.group(1)).append("; ").append("result;");
                        } else sb.append(line).append(System.lineSeparator());
                    }
                    log.debug("Script:" + System.lineSeparator() + sb.toString());
                    String result = (String) engine.eval(sb.toString());
                    log.debug("Result of execution: " + result);
                    pattern = Pattern.compile("((:?\\d{1,3}.?){4}):(\\d+)");
                    Matcher matcher = pattern.matcher(result);
                    if (matcher.matches()) {
                        String ip = matcher.group(1);
                        int port = Integer.parseInt(matcher.group(2));
                        Proxy proxy = new Proxy(ip, port);
                        log.debug(String.format("Add %s to the Common Proxy List", proxy));
                        proxies.add(proxy);
                    }
                }
            } catch (HttpStatusException e) {
                log.error(String.format("HttpStatusException when init proxies. Message: %s", e.getMessage()));
                throw new ConnectionException(e.toString());
            } catch (IOException | ScriptException e) {
                throw new ConnectionException(e.toString());
            }
        }
    }
}
