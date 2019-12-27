package com.airta.platform.engine.controller;


import com.airta.platform.engine.config.CommonConfig;
import com.airta.platform.engine.entity.pool.AgentPool;
import com.airta.platform.engine.k8s.PodTaskProcessor;
import com.airta.platform.engine.parser.JsonParser;
import com.airta.platform.engine.runtime.InitTask;
import com.airta.platform.engine.runtime.Task;
import com.airta.platform.engine.service.TaskService;
import com.airta.platform.engine.service.topic.PoolTopicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author allenyin
 */
@RestController
@RequestMapping("/api")
public class EngineDefaultController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate kafkaTemplate;

    private final PoolTopicRouter poolTopicRouter;

    private final JsonParser jsonParser;

    private final TaskService taskService;

    private final PodTaskProcessor podTaskProcessor;

    @Autowired
    public EngineDefaultController(KafkaTemplate kafkaTemplate, PoolTopicRouter poolTopicRouter,
                                   JsonParser jsonParser, TaskService taskService, PodTaskProcessor podTaskProcessor) {
        this.kafkaTemplate = kafkaTemplate;
        this.poolTopicRouter = poolTopicRouter;
        this.jsonParser = jsonParser;
        this.taskService = taskService;
        this.podTaskProcessor = podTaskProcessor;
    }

    @PostMapping(value = "/init", produces = "application/json")
    @ResponseBody
    public Object initAgent(@RequestBody Object agentPoolMessage) {

        logger.info("init request received.");

        return poolTopicRouter.actionOnTopic("general", agentPoolMessage);
    }

    @PostMapping(value = "/proceedTask", produces = "application/json")
    @ResponseBody
    public Object runTasks(@RequestBody Task taskObject) {

        logger.info("proceed task request received.");

        taskService.initTaskManager(taskObject);
        taskService.runTask();

        return 200;
    }

    @PostMapping(value = "/initTask", produces = "application/json")
    @ResponseBody
    public Object initTask(@RequestBody InitTask initTask) {

        logger.info("proceed init task: {}, agent num: {}, request received.", initTask.getTaskId(), initTask.getAgentNumber());

        taskService.initTask(initTask);

        return 200;
    }

    @PostMapping(value = "/clean")
    @ResponseBody
    public Object cleanTask(@RequestBody AgentPool agentPool) {

        logger.info("proceed cleaning task ..");

        podTaskProcessor.scheduleCleanAgents(agentPool);

        return 200;
    }

    @GetMapping(value = "/sitemap", produces = "application/json")
    public org.json.simple.JSONArray getSiteMapJSON() {

        return jsonParser.readFronJSONFile();
    }

    @GetMapping(value = "/pool", produces = "application/json")
    public Object getAgentPool(@RequestParam(value = "id", required = false) String id) {

        if (StringUtils.isEmpty(id)) {
            return poolTopicRouter.getPodSessionPool(CommonConfig.DEFAULT_APP_NAME);
        } else {
            return poolTopicRouter.getPodSessionPool(id);
        }
    }

    @GetMapping(value = "/version")
    public Object checkVersion() {

        String currentTimestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        return "Phase" + CommonConfig.APP_PHASE + "_" + CommonConfig.APP_VERSION + "." + currentTimestamp;
    }

}
