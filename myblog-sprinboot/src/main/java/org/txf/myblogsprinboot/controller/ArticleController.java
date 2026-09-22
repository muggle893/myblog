package org.txf.myblogsprinboot.controller;

import com.zaxxer.hikari.HikariConfig;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.constant.ArticleStatus;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.exception.ParamErrorException;
import org.txf.myblogsprinboot.exception.SqlExecuteException;
import org.txf.myblogsprinboot.model.Article;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.request.ArticleCreateRequest;
import org.txf.myblogsprinboot.service.ArticleService;
import org.txf.myblogsprinboot.service.UserService;
import org.txf.myblogsprinboot.utils.ArticleContentUtils;
import org.txf.myblogsprinboot.utils.SessionUtils;
import org.txf.myblogsprinboot.utils.UserUtils;
import org.txf.myblogsprinboot.vo.ArticleListVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章控制器类，定义和实现文章的各个接口
 */
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;
    @Autowired
    private HikariConfig hikariConfig;




    /**
     * 接口：/article/list
     * 方法：GET
     * 功能：获取首页中对应用户的文章列表
     * 游客及其他用户只能查看公开文章，作者本人还可以查看自己的私有文章。
     * 作者存在但没有符合条件的文章时，返回成功结果和空列表。
     * @param author 这个参数是用户名，如果用户名为null或空字符串的时候返回参数错误
     *                 如果用户不存在也查询失败，返回失败的结果。其他情况算成功，这个用户没有文章也算成功。
     * @return 返回统一的结果对象，Result的data部分里面保存了文章列表视图对象
     *         成功时data为文章列表，空结果为[]，不为null
     */
    @GetMapping("/list")
    public Result<List<ArticleListVO>> getArticleList(@RequestParam(required = false)String author, HttpSession session) {
        // 校验前端传过来的参数
        if (!StringUtils.hasLength(author)) {
            Result.paramError("获取用户列表时参数校验错误.");
        }

        // 判断文章列表的作者是否存在
        User articleAuthor = userService.getUserByUserName(author);
        if (articleAuthor == null || articleAuthor.getId() == null) {
            Result.fail("文章作者不存在.");
        }

        // 获取Session并判断用户类型，这里用session来判断
        UserType userType = UserUtils.getUserType(author, session);
        log.info(userType.toString());

        // 查询对应用户的文章列表，并判断是否需要展示私有文章
        List<ArticleListVO> articleList = articleService.getArticleList(author, userType);

        // 返回文章列表
        return Result.success("用户列表获取成功", articleList);
    }

    /**
     * 创建文章的接口逻辑，先从文章表中插入一篇文章，然后关联标签表，关联资源表
     * @param requestData      文章创建的时候规定的数据传输对象
     * @param session          用户的会话，从会话可以拿到用户登录信息
     * @return  返回统一返回对象，Result对象的data属性为true或者false
     */
    @RequestMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public Result createArticle(ArticleCreateRequest requestData, HttpSession session) {
        // 校验参数
        if (!StringUtils.hasLength(requestData.getTitle())) {
            throw new ParamErrorException("文章标题为空.");
        }
        if (!StringUtils.hasLength(requestData.getContentMarkdown())) {
            throw new ParamErrorException("文章的内容为空.");
        }

        // 获取插入到数据库中的其他数据
        Article article = new Article();
        article.setTitle(requestData.getTitle());
        article.setContentMarkdown(requestData.getContentMarkdown());
        article.setCategoryId(requestData.getCategoryId());
        article.setVisibility(requestData.getVisibility());
        User user = (User)session.getAttribute(SessionUtils.USER_SESSION_KEY);
        article.setAuthorId(user.getId());
        article.setStatus(ArticleStatus.PUBLISHED);
        String plainText = ArticleContentUtils.toPlainText(requestData.getContentMarkdown());
        article.setSummary(ArticleContentUtils.generateSummary(plainText));
        long wordCnt = (long) ArticleContentUtils.countWords(plainText);
        article.setWordCount(wordCnt);
        article.setReadingMinutes(wordCnt);
        article.setPublishedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setRowVersion(0L);
        log.info("Article created:{}", article);
        // 插入文章内容
        int rowCnt = articleService.insertArticle(article);
        if (rowCnt == 0) {
            throw new SqlExecuteException("文章插入失败.");
        }

        // 关联文章资源表
        articleService.connectArticleAssets(article.getId(), requestData.getAssetIds());

        // 关联文章标签表
        articleService.connectArticleTags(article.getId(), requestData.getTagIds());
        return Result.success("插入文章成功.", true);
    }
}

