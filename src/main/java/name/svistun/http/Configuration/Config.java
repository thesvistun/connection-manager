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

package name.svistun.http.Configuration;

import java.util.*;

import name.svistun.http.Step;
import name.svistun.http.ProxySource;
import name.svistun.http.ProxySourceFile;
import name.svistun.http.ProxySourceSite;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

public class Config {
    private XMLConfiguration config;
    private static final Logger log = Logger.getLogger(Config.class.getSimpleName());

    public Config(String file) throws ConfigurationException {
        config = new Configurations().xml(file);
    }

    public List<ProxySource> getProxyServers() throws ConfigurationException {
        List<ProxySource> proxySources = new ArrayList<>();
        List<Object> proxies = config.getList(ConfigProps.URL_KEY);
        for (int i = 0; i < proxies.size(); i++) {
            String proxySourceKey = String.format("%s(%s)", ConfigProps.PROXY_SOURCE_KEY, i);
            String type = config.getString(String.format("%s.[@%s]", proxySourceKey, ConfigProps.PROXY_TYPE));
            String url = config.getString(String.format("%s.%s", proxySourceKey, ConfigProps.URL));
            String offset = config.getString(String.format("%s.%s", proxySourceKey, ConfigProps.OFFSET));
            Map<String, String> headers = new HashMap<>();
            List<Object> headerNames = config.getList(String.format("%s.%s", proxySourceKey, ConfigProps.HEADER_NAME_KEY));
            for (int j = 0; j < headerNames.size(); j++) {
                String name = config.getString(String.format("%s.%s(%s).%s", proxySourceKey, ConfigProps.HEADER_KEY, j, ConfigProps.HEADER_NAME));
                String value = config.getString(String.format("%s.%s(%s).%s", proxySourceKey, ConfigProps.HEADER_KEY, j, ConfigProps.HEADER_VALUE));
                headers.put(name, value);
            }
            List<Step> steps = new LinkedList<>();
            int stepsNumber = config.getList(String.format("%s.%s.[@%s]", proxySourceKey, ConfigProps.STEP_KEY, ConfigProps.STEP_TYPE)).size();
            for (int j = 0; j < stepsNumber; j++) {
                String stepType = config.getString(String.format("%s.%s(%s).[@%s]", proxySourceKey, ConfigProps.STEP_KEY, j, ConfigProps.STEP_TYPE));
                List<String> args = new LinkedList<>();
                String arg;
                int argCount = 1;
                do {
                    arg = config.getString(String.format("%s.%s(%s).[@%s_%s]", proxySourceKey, ConfigProps.STEP_KEY, j, ConfigProps.STEP_ARG, argCount));
                    if (arg != null) {
                        args.add(arg);
                    }
                    argCount++;
                } while (arg != null);
                steps.add(new Step(stepType, args));
            }
            ProxySource proxySource;
            switch (type) {
                case "file":
                    proxySource = new ProxySourceFile(url, headers, steps);
                    break;
                case "site":
                    proxySource = new ProxySourceSite(url, offset, headers, steps);
                    break;
                default:
                    throw new ConfigurationException(String.format("Type %s not specified", type));

            }
            log.debug(proxySource);
            proxySources.add(proxySource);
        }
        return proxySources;
    }
}
