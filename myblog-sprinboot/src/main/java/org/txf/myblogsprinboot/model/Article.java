package org.txf.myblogsprinboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    private Long id;

    // 作者 ID
    private Long authorId;

    // 分类 ID，允许为空
    private Long categoryId;

    // 文章 URL 别名
    private String slug;

    private String title;

    // 文章摘要
    private String summary;

    // Markdown 正文
    private String contentMarkdown;

    // PUBLIC：公开，PRIVATE：仅作者可见
    private String visibility;

    // PUBLISHED：已发布，ARCHIVED：已下架
    private String status;

    private Long wordCount;

    private Long readingMinutes;

    private LocalDateTime publishedAt;

    // 乐观锁版本号
    private Long rowVersion;

    // 软删除时间，为 null 表示未删除
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
