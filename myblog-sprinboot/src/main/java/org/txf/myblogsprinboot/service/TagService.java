package org.txf.myblogsprinboot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.txf.myblogsprinboot.dao.ArticleMapper;
import org.txf.myblogsprinboot.dao.TagMapper;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.vo.ArticleListVO;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TagService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagMapper tagMapper;

    public List<TagVO> getAuthorTags(User user) {
        // 从数据库中查询作者的文章列表，得到文章列表id集合
        log.info("获取作者的标签列表，作者的名字为：" + user.getUsername());
        List<ArticleListVO> articleListVOS = articleMapper.selectAllArticle(user.getUsername());
        List<Long> articleIds =  new ArrayList<>();;
        articleListVOS.forEach(articleListVO -> {
            articleIds.add(articleListVO.getId());
        });

        // 去文章article_tag表中查询这些文章对应的标签id集合
        List<Long> tagIds = tagMapper.getTagsByArticleIds(articleIds);

        // 根据标签的id去查询对应的标签
        List<TagVO> tags = new ArrayList<>();
        tags = tagMapper.getTagsByIds(tagIds);
        log.info("作者的标签列表获取完成，标签列表为：");
        log.info(Arrays.toString(tags.toArray()));
        return tags;
    }

    public int addTag(TagVO tagVO) {
        return tagMapper.insertTag(tagVO);
    }
}
