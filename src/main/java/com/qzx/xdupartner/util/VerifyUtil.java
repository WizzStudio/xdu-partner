package com.qzx.xdupartner.util;

import cn.hutool.http.HttpUtil;
import com.qzx.xdupartner.entity.dto.SchoolInfoDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * JWT工具类
 */

public class VerifyUtil {
    public static SchoolInfoDto visitData(String chsiCode) {
        String s = HttpUtil.get("https://www.chsi.com.cn/xlcx/bg.do?vcode=" + chsiCode + "&srcid=bgcx");
        return parseHtml(s);
    }

    public static SchoolInfoDto parseHtml(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        Elements infoItems = doc.select(".report-info-item");

        String studentNumber = "";
        String studentName = "";
        String schoolName = "";

        for (Element item : infoItems) {
            if (item.text().contains("学号")) {
                studentNumber = item.select(".value").text();
            } else if (item.text().contains("姓名")) {
                studentName = item.select(".value").text();
            } else if (item.text().contains("院校")) {
                schoolName = item.select(".value").text();
            }
        }
        if ("西安电子科技大学".equals(schoolName)) {
            return new SchoolInfoDto(studentNumber, studentName);
        }
        return null;
    }
}
