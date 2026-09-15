package org.txf.myblogsprinboot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum UserType {
    VISITOR_NOLOGIN(1,"未登录的普通游客"),
    VISITOR_LOGIN(2, "已经登录的用户，但是还不是文章的作者"),
    AUTHOR(3, "已经登录的作者");
    private int type;
    private String description;
}
