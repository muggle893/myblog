const sampleBody = `## 从一个问题开始

学习新知识时，我喜欢先问自己：**它解决了什么问题？**

比起记住结论，把概念放进实际场景里，更容易真正理解。

### 我的学习步骤

1. 理解基本概念与使用场景
2. 写一个最小可运行的例子
3. 记录问题、原因和解决过程

> 把学到的东西，用自己的话再讲一遍。

### 一段小小的代码

\`\`\`java
public class HelloBlog {
    public static void main(String[] args) {
        System.out.println("Hello, learning!");
    }
}
\`\`\`

## 写在最后

不急着学完所有知识，先把今天的问题弄清楚。`

export const samplePosts = [
  {id:'ssm',title:'从一次请求出发，理解 SSM 的分层设计',category:'Java 学习',tags:['Spring','MyBatis'],date:'2026-09-08',read:'6 分钟',excerpt:'从浏览器发出请求，到数据库返回结果，Controller、Service 和 Mapper 各自做了什么？用一个简单的文章查询接口，把这条链路串起来。',body:sampleBody,visibility:'public'},
  {id:'redis',title:'Redis 初印象：为什么还需要一层缓存？',category:'学习笔记',tags:['Redis','后端开发'],date:'2026-09-06',read:'4 分钟',excerpt:'数据库已经可以保存数据，为什么还要引入 Redis？从博客首页的文章列表说起，记录我对缓存的第一轮理解。',body:sampleBody,visibility:'public'},
  {id:'weekend',title:'给学习留一点空白，也给生活一点时间',category:'生活日常',tags:['日常','周末小记'],date:'2026-09-04',read:'3 分钟',excerpt:'合上电脑，去外面走走。那些卡住的思路，有时候会在散步的路上慢慢清晰起来。记录一个普通但舒服的周末。',body:sampleBody,visibility:'public'},
  {id:'private-demo',title:'私有记录演示：只给自己看的学习计划',category:'学习笔记',tags:['私有记录','计划'],date:'2026-09-09',read:'2 分钟',excerpt:'这篇文章用于演示私有权限。游客不会在首页或个人主页看到它，只有作者登录后才能访问。',body:'## 私有文章演示\n\n这是一篇用于测试权限的私有文章。\n\n- 游客列表不会显示\n- 游客直接访问链接会被拦截\n- 作者登录后可以正常查看\n\n后续接入 SSM 时，这个权限必须由后端再次校验。',visibility:'private'},
]
