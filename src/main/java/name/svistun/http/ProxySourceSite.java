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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.svistun.http.Processing.Processor;
import name.svistun.http.Processing.Step;

import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptException;

public class ProxySourceSite extends ProxySource {
    private int offset;
    private String offsetStr;
    private static final Logger log = Logger.getLogger(ProxySourceSite.class.getSimpleName());

    public ProxySourceSite(String url, String offsetStr, Map<String, String> headers, List<Step> steps) {
        super(url, headers, steps);
        this.offsetStr = offsetStr;
    }

    public Set<Proxy> getProxies() throws ConnectionException {
        int attemptsLimit = 5;
        org.jsoup.Connection connection = Jsoup.connect(null == offsetStr ? url : String.format("%s&%s=%s",url, offsetStr, offset));
        for (String name : headers.keySet()) {
            connection.header(name, headers.get(name));
        }
        Set<Proxy> proxies = new HashSet<>();
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
                log.warn(String.format("Limit is exceeded: %s", this));
                return proxies;
            }
            Processor processor = new Processor();
            proxies = (Set<Proxy>) processor.process(steps, doc);
            updateOffset(proxies.size());
        } catch (HttpStatusException e) {
            log.error(String.format("HttpStatusException when init proxies. Message: %s", e.getMessage()));
            offset = 0;
            throw new ConnectionException(e.toString());
        } catch (IOException | ScriptException e) {
            offset = 0;
            throw new ConnectionException(e.toString());
        }
        return proxies;
    }

    private void updateOffset(int proxiesAmount) {
        if (lastProxiesAmount > proxiesAmount) {
            offset = 0;
            lastProxiesAmount = 0;
        } else {
            offset += proxiesAmount;
            lastProxiesAmount = proxiesAmount;
        }
    }

    @Override
    public String toString() {
        return "ProxySourceSite{" +
                "url='" + url + '\'' +
                ", offsetStr='" + offsetStr + '\'' +
                '}';
    }
}
