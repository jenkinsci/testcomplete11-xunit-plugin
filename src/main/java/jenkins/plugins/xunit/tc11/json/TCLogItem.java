/**
 * The MIT License
 * Copyright (c) 2017 Michael Gärtner and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.xunit.tc11.json;

import org.json.JSONObject;

/**
 *
 * @author mgaert
 */
class TCLogItem {

  private String name_;
  private int status_;
  private String id_;
  private String href_;

  public TCLogItem(String id, String name, int status, String href) {
    this.id_ = id;
    this.name_ = name;
    this.status_ = status;
    this.href_ = href;
    //TODO fetch content of href if not empty
  }

  public TCLogItem(JSONObject obj) {
    if (obj.has("name")) {
      this.name_ = obj.getString("name");
    } else {
      this.name_ = "";
    }
    if (obj.has("status")) {
      this.status_ = obj.getInt("status");
    } else {
      this.status_ = 0;
    }
    if (obj.has("href")) {
      this.href_ = obj.getString("href");
      //TODO fetch content of href
    } else {
      this.href_ = "";
    }
    if (obj.has("id")) {
      this.id_ = obj.getString("id");
    } else {
      this.id_ = "";
    }
  }
}
