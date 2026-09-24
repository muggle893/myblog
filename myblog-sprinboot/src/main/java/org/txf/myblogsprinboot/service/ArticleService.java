package org.txf.myblogsprinboot.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.txf.myblogsprinboot.constant.ArticleAssetUsageType;
import org.txf.myblogsprinboot.dao.*;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.model.Article;
import org.txf.myblogsprinboot.model.ArticleAsset;
import org.txf.myblogsprinboot.model.ArticleTag;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.utils.ArticleConstans;
import org.txf.myblogsprinboot.utils.UserUtils;
import org.txf.myblogsprinboot.vo.ArticleDetailVO;
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
    @Autowired
    ArticleAssetMapper articleAssetMapper;
    @Autowired
    ArticleTagMapper articleTagMapper;
    @Autowired
    private UserMapper userMapper;


    public ArticleDetailVO getArticleDetail(Long articleId, HttpSession session) {
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        // 查询文章表中的信息
        Article article = articleMapper.selectArticleById(articleId);
        if (article == null) {
            throw new RuntimeException("查询不到文章，文章id为：" + articleId);
        }
        articleDetailVO.setArticleId(articleId);
        articleDetailVO.setTitle(article.getTitle());
        articleDetailVO.setContentMarkdown(article.getContentMarkdown());
        articleDetailVO.setVisibility(article.getVisibility());
        articleDetailVO.setReadingMinutes(article.getReadingMinutes());
        articleDetailVO.setPublishedAt(article.getPublishedAt());

        // 查询作者
        User author = userMapper.selectUserByUserId(article.getAuthorId());
        if (author == null) {
            throw new RuntimeException("查询不到文章作者，文章id为：" + articleId);
        }
        articleDetailVO.setAuthorId(author.getId());
        articleDetailVO.setAuthorName(author.getUsername());

        // 查询标签列表
        List<Long> articleIds = new ArrayList<>();
        articleIds.add(article.getId());
        List<Long> tagIds = tagMapper.getTagsByArticleIds(articleIds);
        if (tagIds.size() > 0) {
            List<TagVO> tags = tagMapper.getTagsByIds(tagIds);
            articleDetailVO.setTags(tags);
        } else {
            articleDetailVO.setTags(new ArrayList<>());
        }

        // 查询分类
        CategoryListVO categoryListVO = categoryMapper.selectCategoryVO(article.getCategoryId());
        articleDetailVO.setCategoryId(categoryListVO.getId());
        articleDetailVO.setCategoryName(categoryListVO.getName());

        // 判断用户有无权限编辑
        UserType userType = UserUtils.getUserType(articleDetailVO.getAuthorName(), session);
        if (userType == UserType.AUTHOR) {
            articleDetailVO.setCanEdit(true);
        } else {
            articleDetailVO.setCanEdit(false);
        }

        return articleDetailVO;
    }

    /**
     *
     * @param articleId 文章的id
     * @param tagIds    标签的id集合
     * @return 返回文章关联的标签数量, 0表示关联没有资源需要关联
     */
    public int connectArticleTags(long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.size() == 0) {
            return 0;
        }
        // 先把数据处理成ArticleTag对象
        List<ArticleTag> articleTagList = new ArrayList<>();
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTag.setSortOrder(0);
            articleTagList.add(articleTag);
        }

        // 插入到数据库中
        return articleTagMapper.batchInsertArticleTag(articleTagList);
    }

    /**
     * 关联文章对应的资源
     * @param articleId     文章的id
     * @param assetIds      资源的id集合
     * @return  返回文章关联的资源数量, 0表示关联没有资源需要关联
     */
    public int connectArticleAssets(long articleId, List<Long> assetIds) {
        if (assetIds == null || assetIds.size() == 0) {
            return 0;
        }
        // 先把数据处理成ArticleAsset对象
        List<ArticleAsset> articleAssets = new ArrayList<ArticleAsset>();

        for (Long assetId : assetIds) {
            ArticleAsset articleAsset = new ArticleAsset();
            articleAsset.setArticleId(articleId);
            articleAsset.setAssetId(assetId);
            // 这里的usageType先随便设置一个，这里的usageType需要根据资源的类型来判断的
            articleAsset.setUsageType(ArticleAssetUsageType.ATTACHMENT);
            articleAssets.add(articleAsset);
        }

        // 插入数据库中
        return articleAssetMapper.batchInsertArticleAsset(articleAssets);
    }

    public int insertArticle(Article article) {
        return articleMapper.insertArticle(article);
    }

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
        if (categoryListVO != null) {
            article.setCategoryName(categoryListVO.getName());
        } else {
            article.setCategoryName("");
        }
    }

    /**
     * 这个方法是Service内部调用的一个方法，设置文章对应的标签列表
     * @param article  文章对象
     */
    private void setArticleTags(ArticleListVO article) {
        long articleId = article.getId();
        List<Long> tagList = tagArticleMapper.getTagIdListByArticleId(articleId);
        if (tagList != null && tagList.size() > 0) {
            // 根据标签列表找到对应的标签
            List<TagVO> tags = tagMapper.getTagsByIds(tagList);
            article.setTags(tags);
        } else {
            article.setTags(new ArrayList<>());
        }

    }
}
