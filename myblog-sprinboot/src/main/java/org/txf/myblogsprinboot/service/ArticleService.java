package org.txf.myblogsprinboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.txf.myblogsprinboot.Utils.ArticleConstans;
import org.txf.myblogsprinboot.dao.ArticleMapper;
import org.txf.myblogsprinboot.dao.CategoryMapper;
import org.txf.myblogsprinboot.dao.TagArticleMapper;
import org.txf.myblogsprinboot.dao.TagMapper;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.vo.ArticleListVO;
import org.txf.myblogsprinboot.vo.CategoryListVO;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来处理文章业务的类
 */
@Service
public class ArticleService {
    @Autowired
    ArticleMapper articleMapper;
    @Autowired
    TagArticleMapper tagArticleMapper;
    @Autowired
    TagMapper tagMapper;
    @Autowired
    CategoryMapper categoryMapper;
    /**
     * 查询作者对应的文章并根据，用户类型返回对应的结果
     * 只有作者自己访问自己的文章才可以返回私有类型的文章，其他用户只能访问公开的文章
     * @param username  文章作者
     * @param userType  用户类型
     * @return 返回对应的文章列表，空列表为[]，而不是null
     */
    public List<ArticleListVO> getArticleList(String username, UserType userType) {
        // 从数据库查到对应的文章
        List<ArticleListVO> articleList = articleMapper.selectAllArticle(username);
        List<ArticleListVO> ret = new ArrayList<ArticleListVO>();
        // 查到作者对应的文章，还需要根据文章id查对应的tags和分类
        for (ArticleListVO article : articleList) {
            // 根据用户类型再处理
            if (article.getVisibility().equals(ArticleConstans.PRIVATE) && userType == UserType.AUTHOR) {
                setArticleTags(article);
                setArticleCategory(article);
                ret.add(article);
            } else if (article.getVisibility().equals(ArticleConstans.PUBLIC)) {
                setArticleTags(article);
                setArticleCategory(article);
                ret.add(article);
            }
        }
        return ret;
    }

    private void setArticleCategory(ArticleListVO article) {
        CategoryListVO categoryListVO = categoryMapper.selectCategoryVO(article.getCategoryId());
        article.setCategoryName(categoryListVO.getName());
    }

    /**
     * 这个方法是Service内部调用的一个方法，设置文章对应的标签列表
     * @param article  文章对象
     */
    private void setArticleTags(ArticleListVO article) {
        long articleId = article.getId();
        List<Long> tagList = tagArticleMapper.getTagIdListByArticleId(articleId);
        // 根据标签列表找到对应的标签
        List<TagVO> tags = tagMapper.getTagsByIds(tagList);
        article.setTags(tags);
    }
}
