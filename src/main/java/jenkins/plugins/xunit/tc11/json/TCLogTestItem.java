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
import org.json.JSONArray;

/**
 *
 * @author mgaertner
 */
public class TCLogTestItem {

  private String name_;
  private int status_;

  /**
   * The JSON Object should look like    <code>{
   *               "name": "Test Log",
   *               "schemaType": "aqds:tree",
   *               "href": "TestKeywordProject/ProjectTestItem1/KeywordTestLogKT1/_TestLog.js"
   *             }
   * </code>
   *
   * @param jsObject
   * @param providerItem
   */
  public TCLogTestItem(JSONObject jsObject, TCLogProviderItem providerItem) {
    if (jsObject != null) {
      if (jsObject.has("name")) {
        this.name_ = jsObject.getString("name");
      } else {
        this.name_ = "";
      }
      if (jsObject.has("items")) {
        JSONArray items = jsObject.getJSONArray("items");
      }
    }
  }
}
