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

public interface ConfigProps {
    String ROOT = "connection-manager";
    String PROXIES = "proxies";
    String PROXIES_KEY = String.format("%s.%s",ROOT, PROXIES);
    String PROXY_SOURCE = "proxy-source";
    String PROXY_SOURCE_KEY = String.format("%s.%s",PROXIES_KEY, PROXY_SOURCE);
    String URL = "url";
    String URL_KEY = String.format("%s.%s", PROXY_SOURCE_KEY, URL);
    String OFFSET = "offset";
    String HEADER_KEY = String.format("%s.%s", PROXY_SOURCE_KEY, "headers.header");
    String HEADER_NAME = "name";
    String HEADER_VALUE = "value";
    String HEADER_NAME_KEY = String.format("%s.%s",HEADER_KEY, HEADER_NAME);
    String RETRIEVE = "retrieve";
    String STEP = "step";
    String STEP_KEY = String.format("%s.%s.%s",PROXY_SOURCE_KEY, RETRIEVE, STEP);
    String STEP_TYPE = "type";
    String STEP_ARG = "arg";
}