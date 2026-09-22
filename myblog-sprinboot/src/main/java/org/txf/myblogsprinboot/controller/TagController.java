package org.txf.myblogsprinboot.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.txf.myblogsprinboot.advice.Result;
import org.txf.myblogsprinboot.dao.TagMapper;
import org.txf.myblogsprinboot.model.User;
import org.txf.myblogsprinboot.service.TagService;
import org.txf.myblogsprinboot.utils.SessionUtils;
import org.txf.myblogsprinboot.vo.TagVO;

import java.util.List;

/**
 * 这个类用来实现和tag相关的接口
 */

@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {

   @Autowired
   private TagService tagService;
    @Autowired
    private TagMapper tagMapper;

   /**
    * 这个方法用来获取作者对应的标签列表
    * @return        返回Result统一对象，data部分是标签列表
    */
   @GetMapping("/list")
   public Result<List<TagVO>>  getAuthorTagList(HttpSession session) {
      // 获取sesssion中作者的id
      User user = (User)session.getAttribute(SessionUtils.USER_SESSION_KEY);
      List<TagVO> list = tagService.getAuthorTags(user);
      return Result.success("标签列表获取成功.", list);
   }

   @RequestMapping("/add")
   public Result<TagVO> addTag(String tagName) {
      // 1.校验标签名字
      if (tagName == null || tagName.trim().length() == 0) {
         log.error("插入标签时标签的名字为空.");
         throw new IllegalArgumentException("标签名不能为空.");
      }
      log.info("添加标签.");
      // 2.添加到数据库中
      TagVO tagVO = new TagVO();
      tagVO.setName(tagName);
      tagMapper.insertTag(tagVO);

      // 3.返回数据
      return Result.success("标签插入成功.", tagVO);
   }
}
