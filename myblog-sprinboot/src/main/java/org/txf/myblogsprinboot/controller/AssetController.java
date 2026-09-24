package org.txf.myblogsprinboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
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


    /**
     * 这个接口用来加载图片
     * @param httpServletResponse http响应
     * @param publicId            图片的公共id
     */
    @GetMapping("/content/{publicId}")
    public void loadImage(HttpServletResponse httpServletResponse, @PathVariable("publicId") String publicId) {
        // 检查参数
        if (!StringUtils.hasLength(publicId)) {
            throw new RuntimeException("图片的公共id为空.");
        }
        // 下载文件
        assetService.loadAsset(httpServletResponse, publicId);
    }

    @GetMapping("/download/{publicId}")
    public void loadAttachment(HttpServletResponse httpServletResponse, @PathVariable("publicId") String publicId) {
        // 检查参数
        if (!StringUtils.hasLength(publicId)) {
            throw new RuntimeException("附件的公共id为空.");
        }
        // 下载附件
        assetService.loadAsset(httpServletResponse, publicId);
    }



}
