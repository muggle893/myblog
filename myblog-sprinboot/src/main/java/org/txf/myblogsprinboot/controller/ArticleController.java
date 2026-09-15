package org.txf.myblogsprinboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.txf.myblogsprinboot.Utils.UserUtils;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.enums.UserType;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.service.ArticleService;
import org.txf.myblogsprinboot.service.UserService;
import org.txf.myblogsprinboot.vo.ArticleListVO;

import java.util.List;

/**
 * 文章控制器类，定义和实现文章的各个接口
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private UserService userService;

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

        // 查询对应用户的文章列表，并判断是否需要展示私有文章
        List<ArticleListVO> articleList = articleService.getArticleList(author, userType);

        // 返回文章列表
        return Result.success("用户列表获取成功", articleList);
    }
}

