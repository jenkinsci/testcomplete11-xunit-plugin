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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mgaertner
 */
public class TCLog {

  private String name_;
  private Integer status_;
  private String href_;
  private String id_;
  private List<TCLog> children_;
  private List<TCLog> providers_;
  private boolean empty_;
  private List<TCLogItem> tcLogItems_ = null;
  private List<JSONObject> jsLogItems_ = null;
  private TCLog tcLogRoot_ = null;
  private final File tempDir_;

  public TCLog(JSONObject obj, File inputTempDir) {
    this.tempDir_ = inputTempDir;
    initialize(obj);
  }

  private void initialize(JSONObject obj) {
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
    } else {
      this.href_ = "";
    }
    if (obj.has("id")) {
      this.id_ = obj.getString("id");
    } else {
      this.id_ = "";
    }

    this.jsLogItems_ = new ArrayList<JSONObject>();
    this.tcLogItems_ = new ArrayList<TCLogItem>();
    lookForJSONObjectsByName(obj, "TestItem");
    for (Iterator<JSONObject> it = this.jsLogItems_.iterator(); it.hasNext();) {
      this.addTCLogItem(new TCLogItem(it.next(), this.tempDir_));
    }
    this.empty_ = this.tcLogItems_.isEmpty();
  }

  private void lookForJSONObjectsByName(JSONObject obj, String name) {

    JSONArray jsonArray = obj.getJSONArray("children");
    for (int i = 0, size = jsonArray.length(); i < size; i++) {
      JSONObject js;
      js = jsonArray.optJSONObject(i);
      if (js.has("name") && js.getString("name").contains(name)) {
        this.jsLogItems_.add(js);
      } else {
        lookForJSONObjectsByName(js, name);
      }
    }
  }

  /**
   * Get the value of name
   *
   * @return the value of name
   */
  public String getName() {
    return name_;
  }

  /**
   * Set the value of name
   *
   * @param name new value of name
   */
  public void setName(String name) {
    this.name_ = name;
  }

  public Integer getStatus() {
    return status_;
  }

  public void setStatus(Integer status) {
    this.status_ = status;
  }

  public String getHref() {
    return href_;
  }

  public void setHref(String href) {
    this.href_ = href;
  }

  public String getId() {
    return id_;
  }

  public void setId(String id) {
    this.id_ = id;
  }

  public List<TCLog> getChildren() {
    return children_;
  }

  public void setChildren(List<TCLog> children) {
    this.children_ = children;
  }

  public TCLog getProvider(int index) {
    return providers_.get(index);
  }

  public List<TCLog> getProviders() {
    return providers_;
  }

  public void setProviders(List<TCLog> providers) {
    this.providers_ = providers;
  }

  void addChildren(TCLog tcLog) {
    this.children_.add(tcLog);
  }

  public TCLogItem getTCLogItem(int index) {
    TCLogItem tcLogItem = null;
    if (!this.tcLogItems_.isEmpty() && index >= 0 && index < this.tcLogItems_.size()) {
      tcLogItem = tcLogItems_.get(index);
    }
    return tcLogItem;
  }

  public List<TCLogItem> getTCLogItems() {
    return this.tcLogItems_;
  }

  private void addTCLogItem(TCLogItem tcLogItem) {
    if (this.tcLogItems_ != null) {
      this.tcLogItems_.add(tcLogItem);
    }
  }

  public boolean isEmpty() {
    return empty_;
  }

  public int testCount() {
    int count = 0;
    if (!this.isEmpty()) {
      for (Iterator<TCLogItem> it = this.getTCLogItems().iterator(); it.hasNext();) {
        TCLogItem item = it.next();
        count += item.getTCLogTestItems().size();
      }
    }
    return count;
  }

  public String getFailures() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public String getSkipCount() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public Object getTimestamp() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
