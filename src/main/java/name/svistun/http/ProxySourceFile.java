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

import name.svistun.http.Processing.Processor;
import name.svistun.http.Processing.Step;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;

public class ProxySourceFile extends ProxySource {
    private static final Logger log = Logger.getLogger(ProxySourceFile.class.getSimpleName());

    public ProxySourceFile(String url, Map<String, String> headers, List<Step> steps) {
        super(url, headers, steps);
    }

    public Set<Proxy> getProxies() throws ConnectionException {
        int attemptsLimit = 5;
        Set<Proxy> proxies = new HashSet<>();
        try {
            byte count = 0;
            String fileContentStr = null;
            do {
                try {
                    fileContentStr = readFromFile();
                } catch (SocketTimeoutException e) {
                    if (count++ >= 5) {
                        throw new SocketTimeoutException(e.getMessage());
                    }
                }
                attemptsLimit--;
            } while (null == fileContentStr && attemptsLimit > 0);
            if (attemptsLimit <= 0) {
                log.warn(String.format("Limit is exceeded: %s", this));
                return proxies;
            }
            Processor processor = new Processor();
            proxies = (Set<Proxy>) (processor.process(steps, Arrays.asList(fileContentStr.split(System.lineSeparator()))));
        } catch (HttpStatusException e) {
            log.error(String.format("HttpStatusException when init proxies. Message: %s", e.getMessage()));
            throw new ConnectionException(e.toString());
        } catch (IOException | ScriptException e) {
            throw new ConnectionException(e.toString());
        }
        return proxies;
    }

    @Override
    public String toString() {
        return "ProxySourceFile{" +
                "url='" + url + '\'' +
                '}';
    }

    private String readFromFile() throws IOException{
        BufferedReader in = new BufferedReader(
                new InputStreamReader((new URL(url)).openStream()));
        StringBuilder fileContentSb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            fileContentSb.append(inputLine).append(System.lineSeparator());
        in.close();
        org.jsoup.Connection connection = Jsoup.connect(url);
        for (String name : headers.keySet()) {
            connection.header(name, headers.get(name));
        }
        return fileContentSb.toString();
    }
}
