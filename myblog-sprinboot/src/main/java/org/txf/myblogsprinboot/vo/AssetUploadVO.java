package org.txf.myblogsprinboot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果展示对象。
 * 用于编辑器插入图片或附件，以及创建文章时关联文件。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetUploadVO {

    /**
     * 文件记录的数据库主键。
     * 创建文章时，前端将该 ID 放入 assetIds 集合提交。
     */
    private Long assetId;

    /**
     * 文件对外标识，使用 UUID。
     * 文件读取接口通过此标识查找文件。
     */
    private String publicId;

    /**
     * 原始文件名，用于显示附件名称。
     */
    private String originalName;

    /**
     * 文件种类：IMAGE 表示图片，FILE 表示普通附件。
     */
    private String assetKind;

    /**
     * 文件访问地址，用于图片预览或附件下载。
     * 尚未实现文件读取接口时，可以暂时不设置。
     */
    private String url;
}
