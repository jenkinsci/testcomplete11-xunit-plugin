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
public class TCLogTree {

  private JSONObject rootObj_ = null;
  private TCLog tcLogRoot_ = null;
  
  public TCLogTree(JSONObject obj){
    this.rootObj_ = obj;
    initializeLogNodeData(obj);
  }
  private void initializeLogNodeData(JSONObject obj)
  {
    if (this.tcLogRoot_ == null) {
      this.tcLogRoot_ = new TCLog(obj);
    } else {
      this.tcLogRoot_.setTCLog(obj);
    }

  }
  public void getTCLogItem(TCLog tcLog){
    if(!tcLog.isEmpty()){
      List<TCLog> children = tcLog.getChildren();
      for (int i = 0, size = children.size(); i < size; i++)
      {
        if(!children.get(i).isEmpty()){

        }

      }
    }
  }
}
