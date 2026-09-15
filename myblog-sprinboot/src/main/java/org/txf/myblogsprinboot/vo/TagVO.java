package org.txf.myblogsprinboot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页文章卡片标签展示对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {
    /**
     * 标签 ID
     */
    private Long id;

    /**
     * 标签名称，例如“Java”“学习笔记”。
     */
    private String name;
}
