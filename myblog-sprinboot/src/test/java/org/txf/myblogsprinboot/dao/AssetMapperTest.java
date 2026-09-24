package org.txf.myblogsprinboot.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.txf.myblogsprinboot.model.Asset;

@SpringBootTest
class AssetMapperTest {
    @Autowired
    private AssetMapper assetMapper;

    @Test
    void selectAssetByPublicId() {
        Asset asset = assetMapper.selectAssetByPublicId("4285f10b-f028-4eac-a0c4-7279a732934d");
        System.out.println(asset);
    }
}