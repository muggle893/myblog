package org.txf.myblogsprinboot.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDetailVO {
    private Long articleId;
    private Long categoryId;
    private String visibility;
    private String categoryName;
    private String title;
    private Long authorId;
    private String authorName;
    private String contentMarkdown;
    private Boolean canEdit;
    private List<TagVO> tags;
    private LocalDateTime publishedAt;
    private Long readingMinutes;
}
