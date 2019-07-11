package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.RestResponsePage;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.TagRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

//    String url = "http://test.com/solarSystem/planets/{planet}/moons/{moon}";
//
//    // URI (URL) parameters
//    Map<String, String> uriParams = new HashMap<String, String>();
//uriParams.put("planets", "Mars");
//uriParams.put("moons", "Phobos");
//
//    // Query parameters
//    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
//            // Add query parameter
//            .queryParam("firstName", "Mark")
//            .queryParam("lastName", "Watney");
//
//System.out.println(builder.buildAndExpand(uriParams).toUri());
///**
// * Console output:
// * http://test.com/solarSystem/planets/Mars/moons/Phobos?firstName=Mark&lastName=Watney
// */
//
//restTemplate.exchange(builder.buildAndExpand(uriParams).toUri() , HttpMethod.PUT,
//    requestEntity, class_p);

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class ArticleControllerTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    private String createURL(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders getHeaders(String method) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Request-Method", method);
        headers.add("Origin", frontEndUrl);
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Test
    @SqlGroup({
            @Sql("/test-sql/insert_article.sql"),
            @Sql(scripts = "/test-sql/clean_up_insert_article.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfArticles() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, getHeaders("GET"));
        ResponseEntity<String> response = restTemplate.exchange(
                createURL("/article/viewNumberOfArticles"),
                HttpMethod.GET,
                entity,
                String.class);
        String expected = "8";
        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(expected, response.getBody());
        // JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    @SqlGroup({
            @Sql("/test-sql/insert_article.sql"),
            @Sql(scripts = "/test-sql/clean_up_insert_article.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewArticlesByPageIndex() {
        String url = createURL("/article/viewArticles/{pageNumber}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("pageNumber", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, getHeaders("GET"));
        ResponseEntity<RestResponsePage<Article>> response = restTemplate.exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RestResponsePage<Article>>() {
                });

        List<Article> articleList = response.getBody().getContent();
        System.out.println(">> Result: " + articleList);
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            // Assert if the higher article has higher date
            // 5 > 4 > 3 > 2 > 1
            if (i - 1 < 0) {
                break;
            }
            Assert.assertTrue(article.getUtilTimestamp().compareTo(articleList.get(i - 1).getUtilTimestamp()) >= 0);
        }
    }

    @Test
    @SqlGroup({
            @Sql("/test-sql/insert_article.sql"),
            @Sql(scripts = "/test-sql/clean_up_insert_article.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewArticleById() {
        String url = createURL("/article/viewArticle/{id}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, getHeaders("GET"));
        ResponseEntity<Article> response = restTemplate.exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Article>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Assert view count of tags
        // Article 1 has 2 tags id 0 & 1: trồng trọt & chăn nuôi
        Tag trongTrot = tagRepository.findById(0L).get();
        Tag chanNuoi = tagRepository.findById(1L).get();
        Assert.assertEquals(1, trongTrot.getViewCount());
        Assert.assertEquals(1, chanNuoi.getViewCount());

        // Assert view count of this article
        Article article = articleRepository.findById(1L).get();
        Assert.assertEquals(1, article.getViewCount());
    }

    /**
     * Note*: this method only test with indexed items
     */
    @Test
    @SqlGroup({
            @Sql("/test-sql/insert_article.sql"),
            @Sql(scripts = "/test-sql/clean_up_insert_article.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void searchArticles() {
        String url = createURL("/article/searchArticles");

        String requestBody = "{"
                + "\"category\" : " + "\"trồng trọt\","
                + "\"textSearch\" : " + "\"hà nội chán\""
                + "}";

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, getHeaders("POST"));
        ResponseEntity<List<Article>> response = restTemplate.exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<Article>>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("người miền Nam sinh sống ở HN", response.getBody().get(0).getTitle());
    }

    private Article loadArticle(String filePath) throws IOException {
        return new ObjectMapper().readValue(new File(filePath), Article.class);
    }
}