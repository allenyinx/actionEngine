package com.airta.platform.engine.runtime.impl;

import com.airta.platform.engine.nanoscript.Oper;
import com.airta.platform.engine.nanoscript.Script;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ActionScript extends Script {

    /**
     * need to define a REST TEMPLATE to send to API call to agent.
     * @param src
     * @param cxt
     * @throws Exception
     */
    private RestTemplate restTemplate;

    public ActionScript(String src, Map<String, String> cxt) throws Exception {
        super(src, cxt);
    }

    @Override
    protected void accessVariable(String varName, Oper out, Object uo) {
        logger.info("## reading var: {} from agent. ", varName);
        if (varName.equals("url")) {
            //read url from webdriver page on remote agent session.
            out.Val = "";
        } else {
            out.err = "undefined var:" + varName;
        }
    }

    @Override
    protected void processAPI(String var, String apiName, List<Oper> paras, Oper out, Object uo) {
        if (apiName.equals("open")) {
            logger.info("## process open API on agent ..");

        } else if (apiName.equals("print")) {
            logger.info("## process print API on agent ..");

            for (Oper p : paras) {
                System.out.println(p.Val);
            }
        } else if (apiName.equals("click")) {
            logger.info("## process click API on agent ..");

            for (Oper p : paras) {
                System.out.println(p.Val);
            }
        } else {
            out.err = "not defined api:" + apiName;
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
