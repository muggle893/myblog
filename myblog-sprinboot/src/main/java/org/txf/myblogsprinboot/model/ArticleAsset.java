package org.txf.myblogsprinboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 这个类映射表blog_article_asset表
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleAsset {
    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 关联的资源id
     */
    private Long assetId;

    /**
     * 使用的类型， 可以用ArticleAssetUsageType获取到这两个常量
     * INLINE_IMAGE正文图片、ATTACHMENT下载附件
     */
    private String usageType;
}
