package com.airta.platform.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * @author allenyin
 */
@Component
public class RestService {

    private static final String POST_ENTITY_POSTING_ENTITY_TO_URI_MESSAGE = "[postEntity] Posting entity to URI: ";
    private static final String MESSAGE_SUFFIX = " ###";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean waitForServiceAvail(String url) {

        int circle = 50;
        while(circle-->0) {

            if(sockerConnectCheck(url, circle)) {
                return true;
            } else {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }

        return false;
    }

    public static void main(String[] args) {

        RestService restService = new RestService();
        restService.sockerConnectCheck("localhost", 1);
    }

    public boolean sockerConnectCheck(String url, int circle) {

        try (Socket socket = new Socket()) {
            log.info("[Probe-" + circle + "] checking SAF EP http probe [" + url+":8228] ..");
            socket.connect(new InetSocketAddress(url, 8228), 10 * 1000);
            log.info("[Probe] EP is connectible..");
            return true;
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
            log.warn("[Probe-" + circle + "] SAFSV EP http probe still not available on PORT " + 8228);
            return false;
        }
    }

    public <T> T postEntity(String uri, HttpEntity<?> entity, Class<T> type) {

        RestTemplate rt = getRestTemplate();

        log.info(POST_ENTITY_POSTING_ENTITY_TO_URI_MESSAGE + uri + MESSAGE_SUFFIX);
        return rt.postForObject(uri, entity, type);
    }

    public <T> T getEntity(String uri, Class<T> type) {

        RestTemplate rt = getRestTemplate();

        log.info("[getEntity] Getting entity to URI: " + uri + MESSAGE_SUFFIX);
        return rt.getForObject(uri, type);
    }

    public <T> ResponseEntity<T> postEntityForEntity(String uri, HttpEntity<?> entity, Class<T> type) {
        RestTemplate rt = getRestTemplate();

        log.info(POST_ENTITY_POSTING_ENTITY_TO_URI_MESSAGE + uri + MESSAGE_SUFFIX);
        return rt.postForEntity(uri, entity, type);
    }

    public <T> ResponseEntity<T> postEntityForEntityTimeout(String uri, HttpEntity<?> entity, Class<T> type) {
        RestTemplate rt = getRestTemplateTimeout();

        log.info(POST_ENTITY_POSTING_ENTITY_TO_URI_MESSAGE + uri + MESSAGE_SUFFIX);
        return rt.postForEntity(uri, entity, type);
    }

    public RestTemplate getRestTemplate() {

        RestTemplate restTemplate = new RestTemplate();
        setMessageConverter(restTemplate);

        return restTemplate;
    }

    public RestTemplate getRestTemplateTimeout() {

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory rf = (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(10 * 1000);
        rf.setConnectTimeout(10 * 1000);

        setMessageConverter(restTemplate);

        return restTemplate;
    }

    public void setMessageConverter(RestTemplate restTemplate) {

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
    }

    public HttpHeaders createHttpHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}