package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.common.entity.AppUserTag;
import com.son.CapstoneProject.common.entity.Report;
import com.son.CapstoneProject.common.entity.adminChart.SystemChartInfo;
import com.son.CapstoneProject.common.entity.adminChart.UserChartInfo;
import com.son.CapstoneProject.common.entity.pagination.ReportPagination;
import com.son.CapstoneProject.common.entity.pagination.TagPagination;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfReportPages() {
        String url = createURL(port, "/admin/viewNumberOfReportPages");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Long> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Long>() {
                });

        Long reportPagination = response.getBody();
        System.out.println(">> Result: " + reportPagination);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewReportsByPageIndex() {
        String url = createURL(port, "/admin/viewReportsByPageIndex/{pageIndex}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("pageIndex", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ReportPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ReportPagination>() {
                });

        ReportPagination reportPagination = response.getBody();
        System.out.println(">> Result: " + reportPagination);
        Assert.assertEquals(1, reportPagination.getNumberOfPages()); // 2 reports = 1 page
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewOneReport() {
        String url = createURL(port, "/admin/viewOneReport/1");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Report> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Report>() {
                });

        Report report = response.getBody();
        System.out.println(">> Result: " + report);
        System.out.println();
    }

    /**
     * Only search indexed tags
     */
    @Test
    @SqlGroup({
            @Sql("/sql/adminController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void searchTagsByPageIndex() {
        String url = createURL(port, "/admin/searchTagsByPageIndex/{type}/{pageNumber}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\adminController\\searchTag.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("pageNumber", "0");
        uriParams.put("type", "viewCount");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<TagPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<TagPagination>() {
                });

        TagPagination tagPagination = response.getBody();
        System.out.println(">> Result: " + tagPagination);
//        Assert.assertEquals(1, reportPagination.getNumberOfPages()); // 2 reports = 1 page
//        Assert.assertEquals("tagDescription 1");
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/testRankingUser.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void searchTopUsersByTag() {
        String url = createURL(port, "/admin/searchTopUsersByTag/{tagId}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("tagId", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<List<AppUserTag>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<AppUserTag>>() {
                });

        List<AppUserTag> appUserTags = response.getBody();
        System.out.println(">> Result: " + appUserTags);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/totalViewCount.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void totalWebSiteViewCount() {
        String url = createURL(port, "/admin/totalWebSiteViewCount");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Integer> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Integer>() {
                });

        Integer result = response.getBody();
        System.out.println(">> Result: " + result);
        Assert.assertEquals(Integer.valueOf(250), result);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/userChartInfo.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void detailUserActivitiesByDays() {
        String url = createURL(port, "/admin/userChartInfo/{userId}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("userId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<List<UserChartInfo>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<UserChartInfo>>() {
                });

        List<UserChartInfo> result = response.getBody();
        System.out.println(">> Result: " + result);

//        UserChartInfo infoAt2ndOfMarch = result.get("2012-03-02");
//
//        Assert.assertEquals(1, infoAt2ndOfMarch.getTotalQuestionReputation());
//        Assert.assertEquals(1, infoAt2ndOfMarch.getTotalAnswerReputation());
//        Assert.assertEquals(1, infoAt2ndOfMarch.getTotalCommentReputation());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/systemChartInfo.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void systemChartInfo() {
        String url = createURL(port, "/admin/systemChartInfo/date");

        String filePath = "src\\test\\resources\\json\\adminController\\systemChartParamsByDate.json";

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        String requestBody = CommonTest.readStringFromFile(filePath);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<List<SystemChartInfo>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<SystemChartInfo>>() {
                });

        List<SystemChartInfo> result = response.getBody();
        System.out.println(">> Result: " + result);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/systemChartInfo.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void systemChartInfoByMonth() {
        String url = createURL(port, "/admin/systemChartInfo/month");

        String filePath = "src\\test\\resources\\json\\adminController\\systemChartParamsByMonth.json";

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        String requestBody = CommonTest.readStringFromFile(filePath);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<List<SystemChartInfo>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<SystemChartInfo>>() {
                });

        List<SystemChartInfo> result = response.getBody();
        System.out.println(">> Result: " + result);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/adminController/systemChartInfo.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void systemChartInfoByYear() {
        String url = createURL(port, "/admin/systemChartInfo/year");

        String filePath = "src\\test\\resources\\json\\adminController\\systemChartParamsByYear.json";

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        String requestBody = CommonTest.readStringFromFile(filePath);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<List<SystemChartInfo>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<SystemChartInfo>>() {
                });

        List<SystemChartInfo> result = response.getBody();
        System.out.println(">> Result: " + result);
    }
}