package com.qzx.xdupartner.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.*;

/**
 * 第一次get请求authTarget获取hidden信息，
 * 第二次get请求captcha查看是否需要验证码，携带username和'_"时间
 * 若需要验证码，get请求getCaptchaUrl，携带时间:'',cookie
 * 第三次post请求携带hidden和cookie和验证码进行登录（允许重定向）
 * 第四次get请求ifLogin携带cookie获取json中的ifLogin字段判断是否成功登录（禁止重定向）
 */
@Component
@Slf4j
public class XduAuthUtil {
    private static final String authTarget = "http://ids.xidian.edu.cn/authserver/login?service=http://ehall.xidian" +
            ".edu.cn/login?service=http://ehall.xidian.edu.cn/new/index.html";
    private static final String getCaptchaUrl = "http://ids.xidian.edu.cn/authserver/getCaptcha.htl";
    private static final String captcha = "https://ids.xidian.edu.cn/authserver/checkNeedCaptcha.htl";
    private static final String ifLogin = "http://ehall.xidian.edu.cn/jsonp/userFavoriteApps.json";
    private static final Map<String, Object> firstRequestMap = new HashMap<String, Object>(1) {{
        put("type", "userNameLogin");
    }};
    private static final Map<String, Set<HttpCookie>> cookieMap = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 0 登录失败
     * 1 登录成功
     * 2 需要验证码
     *
     * @param username
     * @param password
     * @return
     */
    public Integer login(String username, String password) throws Exception {
        FutureTask<Integer> captchaReq = getIntegerFutureTask(username);
        executor.submit(captchaReq);
        log.info("login:send get authTarget");
        HttpResponse response1 = getLoginPage();
        Set<HttpCookie> cookies = new HashSet<>(response1.getCookies());
        Map<String, Object> param = explainResponse(response1);
        log.info("login:send get authTarget finish");
        param.put("password", XduAesUtil.encrypt(password, String.valueOf(param.get("salt"))));
        param.put("username", username);
        param.put("rememberMe", "true");
        if (captchaReq.get(3, TimeUnit.SECONDS) == 2) {
            return 2;
        }
        long beginTime = System.currentTimeMillis();
        log.info("login:xduLogin begin, startTime:{}", beginTime);
        HttpResponse response2 =
                HttpUtil.createPost(authTarget).setFollowRedirects(true).setMaxRedirectCount(4).cookie(cookies).form(param).execute();
        cookies.addAll(response2.getCookies());
        response2.close();
        long endTime = System.currentTimeMillis();
        log.info("login:xduLogin end, endTime:{}, cost:{}ms", endTime, endTime - beginTime);
        Boolean isLoggedIn = checkIfLogin(cookies);
        long checkTime = System.currentTimeMillis();
        log.info("login:xduLogin checkIfLogin, curTime:{}, cost:{}ms", checkTime, checkTime - endTime);
        if (isLoggedIn) {
            return 1;
        }
        return 0;
    }

    public Integer loginV2(String username, String password) throws Exception {
//        Boolean needCaptcha = checkIfNeedCaptcha(username);
//        if (needCaptcha) return 2;

        HttpRequest pageRequest = HttpUtil.createGet(authTarget).form(firstRequestMap);
        HttpResponse execute = pageRequest.execute();
        Map<String, Object> param = getLoginParamFromPage(execute.body());
        log.info("get param success:{}", param);
        List<HttpCookie> cookies = execute.getCookies();
        param.put("password", XduAesUtil.encrypt(password, String.valueOf(param.get("salt"))));
        param.put("username", username);
        HttpResponse response =
                HttpUtil.createPost(authTarget).cookie(cookies).form(param).execute();
        log.info("get cookie success:{}", response.getCookies());

        if (StringUtils.isNotBlank(response.getCookieValue("happyVoyagePersonal"))) {
            response.close();
            execute.close();
            return 1;
        }
        response.close();
        execute.close();
        return 0;
    }

    public static void main(String[] args) throws Exception {
        Integer login = new XduAuthUtil().loginV2("21009200334", "1500418656");
        System.out.println(login);
    }

    private static HttpResponse getLoginPage() {
        HttpResponse response1 =
                HttpUtil.createGet(authTarget).setFollowRedirects(true).form(firstRequestMap).keepAlive(true).execute();
        response1.close();
        return response1;
    }

    private FutureTask<Integer> getIntegerFutureTask(String username) {
        String key = RedisConstant.NEED_CAPTCHA_USER + username;
        FutureTask<Integer> captchaReq = new FutureTask<>(() -> {
            if (checkIfNeedCaptcha(username)) {
//            Img captchaImage = getCaptcha(username, cookies);
                Img captchaImage = getCaptcha(username);
//            cookieMap.put(key, cookies);
                stringRedisTemplate.opsForHash().putAll(key, new HashMap<String, Object>() {{
                    put("img", ImgUtil.toBase64(captchaImage.getImg(), "jpg"));
                }});
                stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
                return 2;
            }
            return 1;
        });
        return captchaReq;
    }


    public Img getCaptchaImg(String username) {
        String base64Img = (String) stringRedisTemplate.opsForHash().get(RedisConstant.NEED_CAPTCHA_USER + username,
                "img");
//        System.out.println(base64Img);
        BufferedImage bufferedImage = ImgUtil.toImage(base64Img);
        return new Img(bufferedImage);
    }

    public Integer loginWithCaptcha(String username, String password, String vcode) throws Exception {
        String key = RedisConstant.NEED_CAPTCHA_USER + username;
        Set<HttpCookie> cookieInCache = cookieMap.get(key);
//        jsonArray.stream().forEach(j -> cookieInRedis.add(BeanUtil.toBean(j, HttpCookie.class)));
        Map<Object, Object> param = stringRedisTemplate.opsForHash().entries(key);
        param.put("password", XduAesUtil.encrypt(password, String.valueOf(param.get("salt"))));
        param.putAll(new HashMap<String, Object>() {
            {
                put("captcha", vcode);
                put("username", username);
                put("rememberMe", "true");
            }
        });
        param.remove("img");
        param.remove("salt");
        HttpResponse response =
                HttpUtil.createPost(authTarget).setFollowRedirects(true).setMaxRedirectCount(4).cookie(cookieInCache)
                        .form(BeanUtil.beanToMap(param)).execute();
        cookieInCache.addAll(response.getCookies());
        Boolean isLoggedIn = checkIfLogin(cookieInCache);
        if (isLoggedIn) {
            cookieMap.remove(key);
            stringRedisTemplate.delete(key);
            return 1;
        }
        Set<HttpCookie> cookies = cookieMap.get(username);
//        if (checkIfNeedCaptcha(username, cookies)) {
        if (checkIfNeedCaptcha(username)) {
//            Img captchaImage = getCaptcha(username, cookies);
            Img captchaImage = getCaptcha(username);
            cookieMap.put(key, cookies);
            stringRedisTemplate.opsForHash().put(key, "img", ImgUtil.toBase64(captchaImage.getImg(), "jpg"));
            stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
            return 2;
        }
        return 0;
    }


    private Boolean checkIfLogin(Set<HttpCookie> cookies) {
        String body =
                HttpUtil.createGet(ifLogin).setFollowRedirects(false).setMaxRedirectCount(0).cookie(cookies).execute().body();
        return JSONUtil.parseObj(body).get("hasLogin").toString().equalsIgnoreCase("true");
    }

    private Img getCaptcha(String username) {
//    private Img getCaptcha(String username, Set<HttpCookie> cookies) {
        HttpResponse response = HttpUtil.createGet(getCaptchaUrl).setFollowRedirects(true).form(new HashMap<String,
                Object>() {{
            put(String.valueOf(System.currentTimeMillis()), "");
        }}).keepAlive(true).execute();
//        cookies.addAll(response.getCookies());
        return Img.from(response.bodyStream());
    }

    private static void test() throws Exception {
        long start = System.currentTimeMillis();
        System.out.println("a:" + start);
        HttpResponse loginPage = getLoginPage();
        System.out.println("b:" + (System.currentTimeMillis() - start));
        Set<HttpCookie> cookies = new HashSet<>(loginPage.getCookies());
        Map<String, Object> param = explainResponse(loginPage);
//        System.out.println(xduAuthUtil.login("21009200334", "1500418656"));
//        System.out.println(xduAuthUtil.checkIfNeedCaptcha("210092003789"));
//        System.out.println(ImgUtil.toBase64(xduAuthUtil.getCaptcha("210092003789").getImg(),ImgUtil.IMAGE_TYPE_BMP));
        long end = System.currentTimeMillis();
        System.out.println("c:" + (end - start));
    }
//        System.out.println(executor.isShutdown());
//        System.out.println(param);


    //    private Boolean checkIfNeedCaptcha(String username, Set<HttpCookie> cookies) {
    private Boolean checkIfNeedCaptcha(String username) {
        HttpResponse response =
                HttpUtil.createGet(captcha).setFollowRedirects(true).form(new HashMap<String, Object>() {{
                            put("username", username);
                            put("_", System.currentTimeMillis());
                        }})
//                        .cookie(cookies)
                        .keepAlive(true).execute();
//        cookies.addAll(response.getCookies());
        String isNeed = String.valueOf(JSONUtil.parseObj(response.body()).get("isNeed"));
        response.close();
        return isNeed.equalsIgnoreCase("true");
    }

    private static Map<String, Object> explainResponse(HttpResponse response) throws Exception {
        String page = response.body();
        return getLoginParamFromPage(page);
    }

    private static Map<String, Object> getLoginParamFromPage(String page) {
        Document document = Jsoup.parse(page);
        Element form = document.getElementById("pwdFromId");
        String salt = form.getElementById("pwdEncryptSalt").attr("value");
        Elements hiddens = form.select("input[type='hidden']");
        JSONObject body = new JSONObject();
        hiddens.forEach(e -> body.putIfAbsent(e.attr("name"), e.attr("value")));
        body.put("salt", salt);
        return body;
    }
}
