package com.oa.asset.controller;

import com.oa.asset.config.AssetExceptionHandler;
import com.oa.asset.service.AssetService;
import com.oa.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;

/** 验证Controller层JSON、Bean Validation及分页参数校验。 */
@WebMvcTest(controllers = AssetController.class)
@Import({AssetExceptionHandler.class, GlobalExceptionHandler.class})
class AssetControllerValidationTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AssetService service;

    @Test
    @DisplayName("资产名称为空时返回HTTP 400")
    void rejectsBlankAssetName() throws Exception {
        mockMvc.perform(post("/api/asset/assets").contentType(MediaType.APPLICATION_JSON)
                .content("{\"assetName\":\"\",\"assetCode\":\"A-1\",\"category\":3}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("非法资产分类在Controller层被拒绝")
    void rejectsInvalidCategory() throws Exception {
        mockMvc.perform(post("/api/asset/assets").contentType(MediaType.APPLICATION_JSON)
                .content("{\"assetName\":\"测试\",\"assetCode\":\"A-2\",\"category\":9}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("错误日期JSON返回HTTP 400")
    void rejectsMalformedDate() throws Exception {
        mockMvc.perform(post("/api/asset/assets").contentType(MediaType.APPLICATION_JSON)
                .content("{\"assetName\":\"测试\",\"assetCode\":\"A-3\",\"category\":3,\"purchaseDate\":\"not-date\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("请求体JSON格式或字段类型不正确"));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("分页大小超过100时返回HTTP 400")
    void rejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/asset/assets").param("pageNum", "1").param("pageSize", "101"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("非正数资产ID返回HTTP 400")
    void rejectsNonPositiveId() throws Exception {
        mockMvc.perform(get("/api/asset/assets/0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }
}
