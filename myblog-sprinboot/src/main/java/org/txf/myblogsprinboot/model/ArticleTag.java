package org.txf.myblogsprinboot.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTag {
    private Long articleId;
    private Long tagId;
    private Integer sortOrder;
}
