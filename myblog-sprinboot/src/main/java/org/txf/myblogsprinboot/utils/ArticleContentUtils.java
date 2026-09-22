package org.txf.myblogsprinboot.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * 文章摘要、字数和预计阅读时间的计算工具。
 */
public final class ArticleContentUtils {

    private static final int SUMMARY_LENGTH = 120;
    private static final int CHARACTERS_PER_MINUTE = 300;

    private ArticleContentUtils() {
    }

    /**
     * 将 Markdown 渲染为文本。
     *
     * @param markdown Markdown 正文
     * @return 文本内容，输入为空时返回空字符串
     */
    public static String toPlainText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        TextContentRenderer renderer =
                TextContentRenderer.builder().build();

        return renderer.render(document).strip();
    }

    /**
     * 截取纯文本生成摘要，超过长度限制时追加省略号。
     */
    public static String generateSummary(String plainText) {
        int length = plainText.codePointCount(0, plainText.length());

        if (length <= SUMMARY_LENGTH) {
            return plainText;
        }

        // 按 Unicode 码点截取，避免截断 emoji 等字符的代理对
        int endIndex = plainText.offsetByCodePoints(0, SUMMARY_LENGTH);
        return plainText.substring(0, endIndex) + "…";
    }

    /**
     * 统计纯文本中的非空白字符数。
     * 不计空格、换行等空白字符。
     */
    public static int countWords(String plainText) {
        return (int) plainText.codePoints()
                .filter(c -> !Character.isWhitespace(c)
                        && !Character.isSpaceChar(c))
                .count();
    }

    /**
     * 根据字符数估算阅读分钟数，向上取整，至少为 1。
     */
    public static int estimateReadingMinutes(int wordCount) {
        return Math.max(
                1,
                (int) Math.ceil(wordCount / (double) CHARACTERS_PER_MINUTE)
        );
    }

    public static void main(String[] args) {
        String md = "Example\n=======\n\nSome more text";
        System.out.println(generateSummary(md));
        System.out.println(countWords(md));
        System.out.println(estimateReadingMinutes(countWords(md)));
    }
}