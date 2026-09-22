package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.model.ArticleAsset;

import java.util.List;

@Mapper
public interface ArticleAssetMapper {
    int batchInsertArticleAsset(@Param("articleAssets")List<ArticleAsset> articleAssets);
}
