package com.airta.platform.engine.runtime.impl;

import com.airta.platform.engine.nanoscript.Oper;
import com.airta.platform.engine.nanoscript.Script;

import java.util.List;
import java.util.Map;

public class ActionScript extends Script {

    public ActionScript(String src, Map<String, String> cxt) throws Exception {
        super(src, cxt);
    }

    @Override
    protected void accessVariable(String varName, Oper out, Object uo) {
        if(varName.equals("url")){
//            if(wd == null)wd = new ChromeDriver();
            out.Val ="";
//            out.Val = wd.getCurrentUrl();
        }else{
            out.err = "undefined var:" + varName;
        }
    }

    @Override
    protected void processAPI(String var, String apiName, List<Oper> paras, Oper out, Object uo) {
        if(apiName.equals("open")){
//            if(wd == null)wd = new ChromeDriver();
//            wd.get(var != null ? var : paras.get(0).Val);
        }else if(apiName.equals("print")){
            for(Oper p : paras) {
                System.out.println(p.Val);
            }
        }else {
            out.err = "not defined api:" + apiName;
        }
    }
}
