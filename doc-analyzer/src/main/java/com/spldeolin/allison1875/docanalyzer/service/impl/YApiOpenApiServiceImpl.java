package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.YApiCommonRespDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiInterfaceListMenuRespDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiProjectGetRespDTO;
import com.spldeolin.allison1875.docanalyzer.exception.YapiException;
import com.spldeolin.allison1875.docanalyzer.service.YApiOpenApiService;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Deolin 2021-06-10
 */
@Singleton
public class YApiOpenApiServiceImpl implements YApiOpenApiService {

    private static final OkHttpClient okHttpClient = new OkHttpClient();

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public YApiProjectGetRespDTO getProject() {
        String url = config.getYapiUrl() + "/api/project/get" + tokenQuery();
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            YApiCommonRespDTO<YApiProjectGetRespDTO> responseBody = JsonUtils.toParameterizedObject(
                    response.body().string(), new TypeReference<YApiCommonRespDTO<YApiProjectGetRespDTO>>() {
                    });
            ensureSuccess(responseBody);
            return responseBody.getData();
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    @Override
    public List<YApiInterfaceListMenuRespDTO> listCats(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            YApiCommonRespDTO<List<YApiInterfaceListMenuRespDTO>> responseBody = JsonUtils.toParameterizedObject(
                    response.body().string(),
                    new TypeReference<YApiCommonRespDTO<List<YApiInterfaceListMenuRespDTO>>>() {
                    });
            ensureSuccess(responseBody);
            return responseBody.getData();
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    @Override
    public JsonNode listCatsAsJsonNode(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody.get("data");
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    //
    @Override
    public JsonNode createCat(String desc, String name, Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/add_cat";
        RequestBody formBody = new FormBody.Builder().add("desc", desc).add("name", name)
                .add("project_id", projectId.toString()).add("token", config.getYapiToken()).build();
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(formBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    @Override
    public JsonNode getEndpoint(Long id) {
        String url = config.getYapiUrl() + "/api/interface/get" + tokenQuery() + "&id=" + id;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody.get("data");
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    @Override
    public JsonNode createOrUpdateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/save";
        RequestBody requestBody = RequestBody.create(mediaType, JsonUtils.toJson(requestBodyMap));
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(requestBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    @Override
    public JsonNode updateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/up";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                JsonUtils.toJson(requestBodyMap));
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(requestBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    private String tokenQuery() {
        return "?token=" + config.getYapiToken();
    }

    private static void ensureSuccess(YApiCommonRespDTO<?> resp) throws YapiException {
        if (resp == null) {
            throw new YapiException("resp is null");
        }
        if (resp.getErrcode() != 0) {
            throw new YapiException(resp.getErrmsg());
        }
    }

    private void ensureSuccess(JsonNode respNode) {
        if (respNode == null) {
            throw new YapiException("respNode is null");
        }
        if (respNode.get("errcode").asInt() != 0 || respNode.get("data") == null) {
            throw new YapiException(respNode.toString());
        }
    }

}