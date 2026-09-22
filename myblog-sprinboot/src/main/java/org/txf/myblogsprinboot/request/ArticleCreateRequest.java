package org.txf.myblogsprinboot.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 这个对象用来接收文章创建接口前端传递的参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateRequest {
    /**
     * 标题，不能为空
     */
    private String title;

    /**
     * 分类 ID；允许未分类时可为空
     */
    private Long categoryId;

    /**
     * 标签 ID 列表，按展示顺序排列；没有则传 `[]`
     */
    private List<Long> tagIds;

    /**
     * `PUBLIC` 或 `PRIVATE`，表示公开或者私有
     */
    private String visibility;

    /**
     * Markdown 正文，不能为空
     */
    private String contentMarkdown;

    /**
     * 正文引用的已上传资源 ID；没有则传 `[]`
     */
    private List<Long> assetIds;
}
