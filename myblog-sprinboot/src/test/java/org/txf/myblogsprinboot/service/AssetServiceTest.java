package org.txf.myblogsprinboot.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.txf.myblogsprinboot.vo.AssetUploadVO;

import java.nio.charset.StandardCharsets;

@SpringBootTest
class AssetServiceTest {
    @Autowired
    private AssetService assetService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试文件上传，并检查元数据是否成功插入数据库。
     */
    @Test
    void upload() {
        // 1. 创建模拟上传文件
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "上传测试.txt",
                "text/plain",
                "这是用于测试博客上传功能的文件。"
                        .getBytes(StandardCharsets.UTF_8)
        );

        // 2. 调用真实的 Service，上传用户为 ID=1 的用户
        AssetUploadVO result = assetService.upload(file, 1L);

        // 3. 检查返回结果和主键回填
        Assertions.assertNotNull(result, "上传结果不应为空");
        Assertions.assertNotNull(result.getAssetId(), "数据库主键应成功回填");
        Assertions.assertEquals("上传测试.txt", result.getOriginalName());

        // 4. 查询数据库，确认对应用户的文件记录确实存在
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM blog_asset
                WHERE id = ? AND uploader_id = ?
                """,
                Integer.class,
                result.getAssetId(),
                1L
        );

        Assertions.assertEquals(Integer.valueOf(1), count, "数据库应存在这条文件记录");

        System.out.println("上传成功，结果：" + result);
    }
}