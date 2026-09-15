package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.List;

@Mapper
public interface TagMapper {

    List<TagVO> getTagsByIds(@Param("tagIds")List<Long> ids);
}
