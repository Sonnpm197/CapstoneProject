package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.UploadedFile;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.entity.search.ArticleSearch;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.UploadedFileRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.*;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @Autowired
    private ViewCountingService countingService;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private FileController fileController;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/viewNumberOfArticles")
    public long viewNumberOfArticles() {
        return articleRepository.count();
    }

    @GetMapping("/viewDistinctCategories")
    public List<String> viewDistinctCategories() {
        try {
            return articleRepository.findDistinctCategory();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewNumberOfPages")
    public long viewNumberOfPages() {
        try {
            long numberOfArticle = articleRepository.count();
            if (numberOfArticle % ARTICLES_PER_PAGE == 0) {
                return numberOfArticle / ARTICLES_PER_PAGE;
            } else {
                return (numberOfArticle / ARTICLES_PER_PAGE) + 1;
            }
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewArticles/{type}/{pageNumber}")
    public ArticlePagination viewArticles(@PathVariable String type, @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements;

            if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception("ArticleController.viewArticles unknown type: " + type);
            }

            Page<Article> articlePage = articleRepository.findAll(pageNumWithElements);
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articlePage.getContent());
            articlePagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/viewArticlesByCategory/{type}/{pageNumber}")
    public ArticlePagination viewArticlesByCategory(@RequestBody ArticleSearch articleSearch,
                                                    @PathVariable String type,
                                                    @PathVariable int pageNumber) {
        try {
            PageRequest pageNumWithElements;

            if (SORT_DATE.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
            } else if (SORT_VIEW_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("viewCount").descending());
            } else if (SORT_UPVOTE_COUNT.equalsIgnoreCase(type)) {
                pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("upvoteCount").descending());
            } else {
                throw new Exception("ArticleController.viewArticles unknown type: " + type);
            }

            Page<Article> articlePage = articleRepository.findByCategory(articleSearch.getCategory(), pageNumWithElements);
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articlePage.getContent());
            articlePagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewArticle/{id}")
    public Article viewArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
            // Execute asynchronously
            countingService.countView(id, ipAddress, ARTICLE);
            return articleRepository.findById(id)
                    .orElseThrow(() -> new Exception("ArticleController.viewArticle: Not found any article with id: " + id));
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchArticles/{type}/{pageNumber}")
    public ArticlePagination searchArticles(@RequestBody ArticleSearch articleSearch,
                                            @PathVariable String type,
                                            @PathVariable int pageNumber) {
        try {
            return (ArticlePagination) hibernateSearchRepository.search2(
                    articleSearch.getTextSearch(),
                    ARTICLE,
                    new String[]{"title", "content"},
                    articleSearch.getCategory(),
                    type,
                    pageNumber,
                    false
            );
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/searchArticlesOnHomePage/{type}/{pageNumber}")
    public ArticlePagination searchArticlesOnHomePage(@RequestBody ArticleSearch articleSearch,
                                                      @PathVariable String type,
                                                      @PathVariable int pageNumber) {
        try {
            return (ArticlePagination) hibernateSearchRepository.search2(
                    articleSearch.getTextSearch(),
                    ARTICLE,
                    new String[]{"title", "content"},
                    articleSearch.getCategory(),
                    type,
                    pageNumber,
                    true
            );
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can add a new article
     * ** Tag from articles do not count any points to admins
     *
     * @param article
     * @return
     */
    @Transactional
    @PostMapping(value = "/addArticle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Article> addArticle(@RequestBody Article article) {
        try {
            String methodName = "ArticleController.addArticle";

            AppUser appUser = article.getAppUser();

            controllerUtils.validateAppUser(appUser, methodName, true);

            article.setUtilTimestamp(new Date());

            // Save tags first (distinctive name)
            List<Tag> tags = controllerUtils.saveDistinctiveTags(article.getTags());
            article.setTags(tags);

            article = articleRepository.save(article);

            // Note: this uploaded file are already saved on GG Cloud
            // This requested question will have UploadedFile objects => save info of this question to that UploadedFile
            List<UploadedFile> uploadedFiles = article.getUploadedFiles();

            if (uploadedFiles != null) {
                for (UploadedFile uploadedFile : uploadedFiles) {
                    // We still need to save question for this uploaded file
                    uploadedFile.setArticle(article);
                    uploadedFileRepository.save(uploadedFile);
                }
            }

            return ResponseEntity.ok(article);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can update an article
     *
     * @param updatedArticle
     * @return
     */
    @PutMapping("/updateArticle/{articleId}")
    @Transactional
    public ResponseEntity<Article> updateArticle(
            @RequestBody Article updatedArticle,
            @PathVariable Long articleId) {
        try {
            String methodName = "ArticleController.updateArticle";

            Article oldArticle = articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + articleId));

            // Save tags first
            List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedArticle.getTags());

            // Update values
            oldArticle.setTitle(updatedArticle.getTitle());
            oldArticle.setContent(updatedArticle.getContent());
            oldArticle.setTags(tags);
            oldArticle.setUtilTimestamp(new Date());

            // Delete old images from DB and delete file on google cloud storage
            List<UploadedFile> oldUploadedFiles = oldArticle.getUploadedFiles();
            for (UploadedFile oldUploadedFile : oldUploadedFiles) {
                fileController.deleteFile(oldUploadedFile);
            }

            // This requested article will have UploadedFile objects => save info of this question to that UploadedFile
            List<UploadedFile> newUploadedFiles = updatedArticle.getUploadedFiles();
            oldArticle.setUploadedFiles(newUploadedFiles);

            // Save to database
            Article resultArticle = articleRepository.save(oldArticle);

            // Set article_id for these new uploaded files
            if (newUploadedFiles != null) {
                for (UploadedFile uploadedFile : newUploadedFiles) {
                    uploadedFile.setArticle(resultArticle);
                    uploadedFileRepository.save(uploadedFile);
                }
            }

            return ResponseEntity.ok(resultArticle);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Admins can delete an article
     *
     * @return
     */
    @DeleteMapping("/deleteArticle/{articleId}")
    public Map<String, String> deleteArticle(@PathVariable Long articleId) {
        try {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new Exception("ArticleController.deleteArticle: Not found any article with id: " + articleId));

            // Remove the comments
            List<Comment> comments = article.getComments();
            Iterator<Comment> commentIterator = comments.iterator();

            while (commentIterator.hasNext()) {
                Comment comment = commentIterator.next();
                commentRepository.delete(comment);
            }

            // Delete UploadedFile both from GG cloud and DB
            List<UploadedFile> uploadedFiles = article.getUploadedFiles();
            for (UploadedFile uploadedFile : uploadedFiles) {
                fileController.deleteFile(uploadedFile);
            }

            // Delete article
            articleRepository.delete(article);

            Map<String, String> map = new HashMap<>();
            map.put("articleId", ("" + articleId));
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop10ArticlesByUpvoteCount")
    public ArticlePagination getTop10ArticlesByUpvoteCount() {
        try {
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articleRepository.findTop10ByOrderByUpvoteCountDesc());
            articlePagination.setNumberOfPages(1);
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/getTop10ArticlesByUploadDate")
    public ArticlePagination getTop10ArticlesByUploadDate() {
        try {
            ArticlePagination articlePagination = new ArticlePagination();
            articlePagination.setArticlesByPageIndex(articleRepository.findTop10ByOrderByUtilTimestampDesc());
            articlePagination.setNumberOfPages(1);
            return articlePagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/usersCommentResubscribeArticles")
    public void usersCommentResubscribeArticles() {
        try {
            List<Article> articles = articleRepository.findAll();

            for (Article article: articles) {

                // Get distince subscibers of that article
                List<Comment> comments = article.getComments();
                List<AppUser> distinctAppUsers = new ArrayList<>();
                for (Comment comment: comments) {
                    AppUser appUser = comment.getAppUser();

                    // Because admin post articles so he does not need to sub
                    if (!Role.ADMIN.getValue().equalsIgnoreCase(appUser.getRole())) {
                        if (!distinctAppUsers.contains(appUser)) {
                            distinctAppUsers.add(appUser);
                        }
                    }
                }

                // Then make them subscribe
                article.setSubscribers(distinctAppUsers);
                articleRepository.save(article);
            }

        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
