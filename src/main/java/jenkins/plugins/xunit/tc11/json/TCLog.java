/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jenkins.plugins.xunit.tc11.json;

import java.util.ArrayList;
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
  private String schemaType_;
  private List<TCLog> children_;
  private List<TCLog> providers_;
  private String activeProviderName_;
  private boolean empty_;

  public boolean isEmpty() {
    return empty_;
  }
  
  TCLog(JSONObject obj) {
    this.setTCLog(obj);
  }
  public void setTCLog(JSONObject obj){
    if(obj.has("name")){
      this.name_ = obj.getString("name");
    } else {
      this.name_ = "";
    }
    if(obj.has("status")){
      this.status_ = obj.getInt("status");
    } else {
      this.status_ = 0;
    }
    if(obj.has("href")){
      this.href_ = obj.getString("href");
    } else {
      this.href_ = "";
    }
    if(obj.has("id")){
      this.id_ = obj.getString("id");
    } else {
      this.id_ = "";
    }
    if(obj.has("schemaType")){
      this.schemaType_ = obj.getString("schemaType");
    } else {
      this.schemaType_ = "none";
    }
    this.children_ = new ArrayList<TCLog>();
    if(obj.has("children")){
      JSONArray jsonArray = obj.getJSONArray("children");
      for (int i = 0, size = jsonArray.length(); i < size; i++)
      {
        JSONObject js;
        js = jsonArray.optJSONObject(i);
        TCLog tcLog;
        tcLog = new TCLog(js);
        this.children_.add(tcLog);

      }
    }
    this.providers_ = new ArrayList<TCLog>();
    if(obj.has("providers")){
      JSONArray jsonArray = obj.getJSONArray("providers");

      for (int i = 0, size = jsonArray.length(); i < size; i++)
      {
        this.providers_.add(new TCLog(jsonArray.getJSONObject(i)));

      }
    }
    this.activeProviderName_ = "";
    
    int summary_index = -1;
    int perfcounters_index = -1;

    TCLog[] providers = (TCLog[])this.getProviders().toArray();
    for (int i = 0; i < providers.length; i++)
    {		
      String provider_name = providers[i].getName().toLowerCase();
      if (provider_name.contains("summary")){
        summary_index = i;
      }
      if (providers[i].getSchemaType().equals("aqds:table") && provider_name.contains("performance counters")){
        perfcounters_index = i;
      }
    }

    if (summary_index > 0) // put summary first
       providers = ArrayUtil.splice(providers, 0, 0, ArrayUtil.splice(providers,summary_index, 1)[0]);

    if (perfcounters_index == 1 && providers[0].getSchemaType().equals("aqds:tree")){
      // move perf counters to children of first provider
      providers[0].addChildren(ArrayUtil.splice(providers,perfcounters_index, 1)[0]);
    }
    this.empty_ = this.children_.isEmpty();
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

  public String getSchemaType() {
    return schemaType_;
  }

  public void setSchemaType(String schemaType) {
    this.schemaType_ = schemaType;
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

  public String getActiveProviderName() {
    return activeProviderName_;
  }

  public void setActiveProviderName(String activeProviderName) {
    this.activeProviderName_ = activeProviderName;
  }

  void addChildren(TCLog tcLog) {
    this.children_.add(tcLog);
  }
}
