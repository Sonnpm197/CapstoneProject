package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
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

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class TagControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Test
    @SqlGroup({
            @Sql("/sql/tagController/insert_tags_top_5.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void getTop5TagsByViewCount() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<TagPagination> response = CommonTest.getRestTemplate().exchange(
                createURL(port, "/tag/viewTop5TagsByViewCount"),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<TagPagination>() {
                });
        int expected = 4; // 5 Tags
        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(expected, response.getBody().getTagsByPageIndex().size());
    }

}
