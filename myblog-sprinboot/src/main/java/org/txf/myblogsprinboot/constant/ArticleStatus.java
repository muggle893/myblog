package org.txf.myblogsprinboot.constant;

/**
 * 这个类用来写文章状态的常量
 */

public final class ArticleStatus {

    /**
     * 已发布，可按文章的公开或私有权限访问。
     */
    public static final String PUBLISHED = "PUBLISHED";

    /**
     * 已归档，不在正常文章列表中展示。
     */
    public static final String ARCHIVED = "ARCHIVED";

    private ArticleStatus() {
    }
}
