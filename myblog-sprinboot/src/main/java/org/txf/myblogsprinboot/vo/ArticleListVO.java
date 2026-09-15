package org.txf.myblogsprinboot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表展示对象。
 *
 * <p>用于首页文章卡片展示，包含分类、摘要和标签等信息，
 * 不包含 Markdown 正文。</p>
 *
 * <p>私有文章是否允许返回，由后端根据当前登录身份判断。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleListVO {

    /**
     * 文章 ID，用于跳转和查询文章详情。
     */
    private Long id;

    /**
     * 分类 ID；为 null 时表示未分类。
     */
    private Long categoryId;

    /**
     * 分类名称，用于文章卡片展示。
     */
    private String categoryName;

    /**
     * 文章可见性：PUBLIC 表示公开，PRIVATE 表示仅作者可见。
     */
    private String visibility;

    /**
     * 首次发布时间，按项目约定使用 UTC。
     * 编辑文章时不重置该时间。
     */
    private LocalDateTime publishedAt;

    /**
     * 预计阅读时长，单位为分钟，最小值为 1。
     */
    private Long readingMinutes;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * 文章纯文本摘要，用于标题下方的内容预览。
     */
    private String summary;

    /**
     * 文章标签，按展示顺序排列。
     * 无标签时返回空列表，不返回 null。
     */
    private List<TagVO> tags;
}
