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

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.*;

public class Connection {
    private Set<Proxy> proxies;
    private Set<Proxy> badProxies;
    private Vector<ConnectionThread> threads;
    private int threadsLimit;
    private Vector<Document> results;
    private String goodResponseTemplate;
    private List<String> badResponseTemplates;
    private ConnectionManager connectionManager;
    private int attemptAmount;
    private int connectionTimeout;
    private static final Logger log = Logger.getLogger(Connection.class.getSimpleName());


    private Connection() {
        attemptAmount = 4;
        connectionTimeout = 7;
        proxies = new HashSet<>();
        badProxies = new HashSet<>();
        results = new Vector<>();
        threads = new Vector<>();
    }

    Connection(ConnectionManager connectionManager, int threadsLimit, String goodResponseTemplate, List<String> badResponseTemplates) {
        this();
        this.connectionManager = connectionManager;
        this.threadsLimit = threadsLimit;
        this.badResponseTemplates = badResponseTemplates;
        this.goodResponseTemplate = goodResponseTemplate;
    }

    public boolean doRequest(String url) {
        if (threads.size() >= threadsLimit) {
            log.debug(String.format("%s thread(s) currently run. Because of limit set in %s Skip this request.", threads.size(), threadsLimit));
            return false;
        }
        log.debug(String.format("Do request for URL [%s]", url));
        ConnectionThread thread = new ConnectionThread(url);
        threads.add(thread);
        thread.start();
        return true;
    }

    public Document getResult() {
        if (results.isEmpty()) {
            return null;
        }
        Document doc = results.get(0);
        log.debug(String.format("Got result of URL [%s]", doc.location()));
        results.remove(doc);
        return doc;
    }

    public void setAttemptAmount(int attemptAmount) {
        this.attemptAmount = attemptAmount;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    synchronized Proxy getProxy() throws ConnectionException {
        log.debug(String.format("Locking for bad proxies (%s item(s)) ready for reuse", badProxies.size()));
        List<Proxy> _proxis = new ArrayList<>();
        for (Proxy _proxy : badProxies) {
            if (new Date().getTime() - _proxy.getLastUsage().getTime() > 12*60*60*1000) {
                _proxis.add(_proxy);
            }
        }
        log.debug(String.format("%s proxy(s) are ready for reuse", _proxis.size()));
        badProxies.removeAll(_proxis);
        proxies.addAll(_proxis);

        log.debug(String.format("Getting free proxy from available %s proxies", proxies.size()));
        while (proxies.isEmpty()) {
            try {
                proxies.addAll(connectionManager.supplyProxies());
                //todo if it really needed?
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        Proxy proxy = null;
        do {
            for (Proxy _proxy : proxies) {
                if (_proxy.getLastUsage() == null || (new Date().getTime() - _proxy.getLastUsage().getTime()) >= 10*1000) {
                    proxy = _proxy;
                    proxies.remove(proxy);
                    break;
                } else {
                    log.debug(String.format("%s last usage time is newer then %s sec. Skip it.", _proxy, 10));
                }
            }
            if (null == proxy) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        } while (proxy == null);
        log.debug(String.format("Return %s", proxy));
        return proxy;
    }

    void setThreadsLimit(int threadsLimit) {
        this.threadsLimit = threadsLimit;
    }

    void setGoodResponseTemplate(String goodResponseTemplate) {
        this.goodResponseTemplate = goodResponseTemplate;
    }

    void setBadResponseTemplates(List<String> badResponseTemplates) {
        this.badResponseTemplates = badResponseTemplates;
    }

    private class ConnectionThread extends Thread {
        private String url;
        private boolean cancelled;

        ConnectionThread(String url) {
            this();
            this.url = url;
        }

        private ConnectionThread() {
            cancelled = false;
        }

        void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            Document doc = null;
            try {
                Proxy proxy = getProxy();
                org.jsoup.Connection connection = Jsoup.connect(url);
                connection.timeout(connectionTimeout * 1000);
                connection.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
                connection.header("Accept-Language", "ru,en-US;q=0.7,en;q=0.3");
                int attempt = attemptAmount;
                while (doc == null) {
                    connection.proxy(proxy.getIp(), proxy.getPort());
                    try {
                        doc = connection.get();
                        if (null != badResponseTemplates) {
                            for (String changeProxyTamplate: badResponseTemplates) {
                                if (! doc.select(changeProxyTamplate).isEmpty()) {
                                    attempt = 0;
                                    doc = null;
                                    throw new IOException(String.format("Template of bad response found: %s. Change a proxy.", changeProxyTamplate));
                                }
                            }
                        }
                        //todo prevent removing all the proxy because the site really does not return proper page.
                        if (doc.select(goodResponseTemplate).isEmpty()) {
                            String message = "Bad response. Document:\n" + doc.toString();
                            doc = null;
                            throw new IOException(message);
                        }
                    } catch (IOException e) {
                        if (connection.response().statusCode() > 0 && connection.response().statusCode() == 550) {
                            log.warn(String.format("%s; Status code [%s] message [%s]. Skip this proxy immediately.", proxy, connection.response().statusCode(), connection.response().statusMessage()));
                            attempt = 0;
                        } else if (connection.response().statusCode() > 0 && connection.response().statusCode() != 200) {
                            log.warn(String.format("%s; Status code [%s] message [%s]", proxy, connection.response().statusCode(), connection.response().statusMessage()));
                        } else if (e.getMessage().contains(" 307 Temporary Redirect")
                                || e.getMessage().contains(" 403 Forbidden")
                                || e.getMessage().contains(" 407 Proxy Authentication Required")
                                || e.getMessage().contains(" 501 Not implemented")
                                || e.getMessage().contains(" 503 Service Temporarily Unavailable")
                                || e.getMessage().contains("Permission denied (connect failed)")) {
                            log.warn(proxy + "; " + e.getMessage() + ". Skip this proxy immediately");
                            attempt = 0;
                        } else {
                            log.warn(proxy + "; " + e.getMessage());
                        }
                        attempt--;
                    } catch (NoSuchElementException e) {
                        log.warn(proxy + "; NoSuchElementException has occurred");
                        attempt--;
                    }
                    if (attempt <= 0 && doc == null) {
                        log.debug(String.format("Connecting URL [%s] FAIL. %s", url, proxy));
                        badProxies.add(proxy);
                        proxy = getProxy();
                        attempt = attemptAmount;
                    }
                }
                log.debug(String.format("Connecting URL [%s] SUCCESS. %s", url, proxy));
                proxies.add(proxy);
                results.add(doc);
                log.info("Thread number before remove: " + threads.size());
                threads.remove(this);
                log.info("Thread number after remove: " + threads.size());
            } catch (ConnectionException e) {
                //todo
            }
            log.info("Thread done.");
        }
    }
}
