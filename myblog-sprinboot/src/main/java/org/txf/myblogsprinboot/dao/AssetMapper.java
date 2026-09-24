package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.txf.myblogsprinboot.model.Asset;

@Mapper
public interface AssetMapper {

    int insert(Asset asset);

    Asset selectAssetByPublicId(String publicId);
}
