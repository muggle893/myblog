package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.constant.ArticleAssetUsageType;
import org.txf.myblogsprinboot.model.ArticleAsset;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ArticleAssetMapperTest {

    @Autowired
    private ArticleAssetMapper articleAssetMapper;

    @Test
    void batchInsertArticleAsset() {
        List<ArticleAsset> articleAssets = new ArrayList<>();
        ArticleAsset articleAsset = new ArticleAsset();
        articleAsset.setArticleId(1L);
        articleAsset.setAssetId(1L);
        articleAsset.setUsageType(ArticleAssetUsageType.ATTACHMENT);
        articleAssets.add(articleAsset);
        articleAssetMapper.batchInsertArticleAsset(articleAssets);
    }
}