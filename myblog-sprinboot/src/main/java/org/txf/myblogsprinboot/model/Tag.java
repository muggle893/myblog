package org.txf.myblogsprinboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库标签映射类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    private Long id;
    private Long name;
    private Integer created_at;
}
