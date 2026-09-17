package org.txf.myblogsprinboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.service.AssetService;
import org.txf.myblogsprinboot.utils.SessionUtils;
import org.txf.myblogsprinboot.vo.AssetUploadVO;

/**
 * 这个资源控制器，用来处理用户上传资源
 */
@RestController
@RequestMapping("/asset")
public class AssetController {
    @Autowired
    AssetService assetService;

    /**
     * /asset/upload这个接口用来处理用户上传文件
     * @param file 用户上传的文件
     * @param request Http请求，可以拿到session，从而拿到用户登录状态
     * @return 返回统一结果处理对象，data部分是AssetUploadVO类型
     */
    @PostMapping("/upload")
    public Result<AssetUploadVO> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 登录检查已经由登录拦截器执行
        // 调用service的文件上传接口
        // 返回约定好的接口
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute(SessionUtils.USER_SESSION_KEY);
        AssetUploadVO asset = assetService.upload(file, user.getId());
        return Result.success("上传成功", asset);
    }
}
