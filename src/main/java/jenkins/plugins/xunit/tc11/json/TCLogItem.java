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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This Class represents in MHT _root.js a JSON Object matching "TestItem"
 *
 * @author mgaert
 */
public class TCLogItem {

  private String name_;
  private int status_;
  private String id_;
  private List<TCLogTestItem> tcTestItems_;
  private List<JSONObject> jsTestItems_;
  private List<TCLogProviderItem> providers_;

  public TCLogItem(String id, String name, int status, JSONArray providers) {
    this.id_ = id;
    this.name_ = name;
    this.status_ = status;

    JSONArray jSArray = providers;
    //this.providers_ = providers;
    //TODO fetch content of href if not empty
  }

  public TCLogItem(JSONObject obj, File inputTempDir) {
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
    /*
     * Fetch content of attached referenced document to parse out execution time of test
     */
    if (obj.has("providers")) {
      JSONArray jsArray = obj.getJSONArray("providers");
      for (int i = 0; i < jsArray.length(); i++) {
        JSONObject jsObject = jsArray.optJSONObject(i);
        if (jsObject.has("href")) {
          String filename = jsObject.getString("href");
          Collection<File> jsFiles = FileUtils.listFiles(inputTempDir, FileFilterUtils.nameFileFilter(jsObject.getString("href")), null);
          if (jsFiles.isEmpty()) {
            String message = "Invalid TestComplete MHT file. No entry with '" + filename + "' found.";
            throw new ConversionException(message);
          }
          File jsFile = jsFiles.iterator().next();
          try {
            String fileContent = JSONUtil.readJSONFile(jsFile, "UTF-8");
            int start = fileContent.indexOf('(');
            int end = fileContent.indexOf(')');
            fileContent = fileContent.substring(start + 1, end);

            String jsonRaw = fileContent.substring(fileContent.indexOf(',') + 1);
            JSONObject tmpObject = new JSONObject(jsonRaw);
            if (tmpObject.has("items")) {
              JSONArray items = tmpObject.getJSONArray("items");
              for (int j = 0; j < items.length(); j++) {
                JSONObject js = items.optJSONObject(j);
                this.providers_.add(new TCLogProviderItem(js));
              }
            }

          } catch (FileNotFoundException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' not found.");
          } catch (IOException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' can not be read.");
          }
        }
      }
    }
    this.jsTestItems_ = new ArrayList<JSONObject>();
    this.tcTestItems_ = new ArrayList<TCLogTestItem>();
    lookForJSONObjectsByName(obj, "Test Log");
    for (Iterator<JSONObject> it = this.jsTestItems_.iterator(); it.hasNext();) {
      JSONObject js = it.next();
      if (js.has("name")) {
        TCLogProviderItem providerItem = this.getProviderItemByName(js.getString("id"));
        this.addTCLogTestItem(new TCLogTestItem(it.next(), providerItem));
      }
    }
    if (obj.has("id")) {
      this.id_ = obj.getString("id");
    } else {
      this.id_ = "";
    }
  }

  private void lookForJSONObjectsByName(JSONObject obj, String name) {

    JSONArray jsonArray = obj.getJSONArray("children");
    for (int i = 0, size = jsonArray.length(); i < size; i++) {
      JSONObject js;
      js = jsonArray.optJSONObject(i);
      if (js.has("name") && js.getString("name").contains(name)) {
        this.jsTestItems_.add(js);
      } else {
        lookForJSONObjectsByName(js, name);
      }
    }
  }

  public TCLogTestItem getTCLogTestItem(int index) {
    TCLogTestItem tcLogTestItem = null;
    if (!this.tcTestItems_.isEmpty() && index >= 0 && index < this.tcTestItems_.size()) {
      tcLogTestItem = this.tcTestItems_.get(index);
    }
    return tcLogTestItem;
  }

  public List<TCLogTestItem> getTCLogTestItems() {
    return this.tcTestItems_;
  }

  private void addTCLogTestItem(TCLogTestItem tcLogItem) {
    if (this.tcTestItems_ != null) {
      this.tcTestItems_.add(tcLogItem);
    }
  }

  private TCLogProviderItem getProviderItemByName(String id) {
    for (Iterator<TCLogProviderItem> it = this.providers_.iterator(); it.hasNext();) {
      TCLogProviderItem item = it.next();
      if (item.getTestItemId().equals(id)) {
        return item;
      }
    }
    return null;
  }

  public int getTestCount() {
    int count = this.tcTestItems_.size();
    return count;
  }

  public int getState() {
    return this.status_;
  }

  public String getTimeStamp() {
    String dateTime = "";
    if (!this.providers_.isEmpty()) {
      TCLogProviderItem item = this.providers_.get(0);
      if (item != null) {
        dateTime = item.getStartTime();
      }
    }
    return dateTime;
  }

  int getRunTime() {
    int dateTime = 0;
    if (!this.providers_.isEmpty()) {
      TCLogProviderItem item = this.providers_.get(0);
      if (item != null) {
        dateTime = item.getRunTime();
      }
    }
    return dateTime;
  }

  public String getName() {
    return this.name_;
  }

}
