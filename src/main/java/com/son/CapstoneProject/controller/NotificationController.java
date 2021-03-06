package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.common.entity.Article;
import com.son.CapstoneProject.common.entity.Notification;
import com.son.CapstoneProject.common.entity.Question;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.common.entity.pagination.NotificationPagination;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.son.CapstoneProject.common.ConstantValue.NOTIFICATION_PER_PAGE;

@RestController
@RequestMapping("/notification")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an user";
    }

    @DeleteMapping("/delete/{id}")
    @Transactional
    public Map<String, String> deleteNotification(@PathVariable Long id) {
        try {
            String methodName = "NotificationController.deleteNotification";

            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new Exception(methodName + ": Found no notification with id: " + id));

            notificationRepository.delete(notification);

            Map<String, String> map = new HashMap<>();
            map.put("notificationId", "" + id);
            map.put("deleted", "true");
            return map;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @PostMapping("/unsubscribe")
    @Transactional
    public Notification unsubscribe(@RequestBody Notification notification) {
        try {
            String methodName = "NotificationController.deleteNotification";

            Notification fullDataNotification = notificationRepository.findById(notification.getNotificationId())
                    .orElseThrow(() -> new Exception(methodName + ": Found no notification with id: " + notification.getNotificationId()));

            AppUser receiver = fullDataNotification.getAppUserReceiver();

            // Unsubscribe this question
            if (fullDataNotification.getQuestion() != null) {
                Question question = fullDataNotification.getQuestion();
                question.getSubscribers().remove(receiver);
                questionRepository.save(question);
            }
            // Unsubscribe this article
            else if (fullDataNotification.getArticle() != null) {
                Article article = fullDataNotification.getArticle();
                article.getSubscribers().remove(receiver);
                articleRepository.save(article);
            }

            // Unsub = seen
            fullDataNotification.setSeen(true);

            return notificationRepository.save(fullDataNotification);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * When they click on this to view, that means they know this notification
     *
     * @param pageNumber
     * @return
     */
    @GetMapping("/viewNotificationsByPageIndex/{userId}/{pageNumber}")
    @Transactional
    public NotificationPagination viewNotificationsByPageIndex(@PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            logger.info("pageNumber: {}", pageNumber);
            PageRequest pageNumWithElements = PageRequest.of(pageNumber, NOTIFICATION_PER_PAGE, Sort.by("utilTimestamp").descending());
            Page<Notification> notificationPage = notificationRepository.findByAppUserReceiver_UserId(userId, pageNumWithElements);

            List<Notification> notifications = notificationPage.getContent();

            List<Notification> modifiableNotifications = new ArrayList<Notification>(notifications);
            Collections.sort(modifiableNotifications, new Comparator<Notification>() {
                @Override
                public int compare(Notification o1, Notification o2) {
                    if (o1.isSeen() && !o2.isSeen()) {
                        return 1;
                    } else if (!o1.isSeen() && o2.isSeen()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            // Return pagination objects
            NotificationPagination notificationPagination = new NotificationPagination();
            notificationPagination.setNotificationsByPageIndex(modifiableNotifications);

            Integer numberOfNotifications = notificationRepository.findTotalNumberByAppUserReceiver_UserId(userId);

            if (numberOfNotifications == null) {
                numberOfNotifications = 0;
            }

            if (numberOfNotifications % NOTIFICATION_PER_PAGE == 0) {
                logger.info("numberOfNotificationPages : {}", numberOfNotifications / NOTIFICATION_PER_PAGE);
                notificationPagination.setNumberOfPages(numberOfNotifications / NOTIFICATION_PER_PAGE);
            } else {
                logger.info("numberOfNotificationPages : {}", (numberOfNotifications / NOTIFICATION_PER_PAGE) + 1);
                notificationPagination.setNumberOfPages(numberOfNotifications / NOTIFICATION_PER_PAGE + 1);
            }

            // Update seen data
//            for (Notification notification : notifications) {
//                notification.setSeen(true);
//                notificationRepository.save(notification);
//            }

            notificationPagination.setNumberOfContents(numberOfNotifications);

            return notificationPagination;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewMoreNotifications/{userId}/{pageNumber}")
    @Transactional
    public NotificationPagination viewMoreNotifications(@PathVariable Long userId, @PathVariable int pageNumber) {
        try {
            logger.info("pageNumber: {}", pageNumber);
            NotificationPagination finalNotifications = new NotificationPagination();

            for (int i = 0; i <= pageNumber; i++) {
                NotificationPagination notificationPagination = viewNotificationsByPageIndex(userId, i);
                finalNotifications.getNotificationsByPageIndex().addAll(notificationPagination.getNotificationsByPageIndex());
            }

            finalNotifications.setCurrentPage(pageNumber);

            return finalNotifications;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewNumberOfUnseenNotification/{userId}")
    @Transactional
    public int viewNumberOfUnseenNotification(@PathVariable Long userId) {
        try {

            AppUser appUser = appUserRepository.findById(userId)
                    .orElseThrow(() -> new Exception("NotificationController. viewNumberOfUnseenNotification: Cannot find any User with id: " + userId));

            Integer result = notificationRepository.getTotalUnseenNotificationByUser(false, appUser);
            return result == null ? 0 : result.intValue();
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            return 0;
            // throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @GetMapping("/viewOneNotification/{notificationId}")
    @Transactional
    public Notification viewOneNotification(@PathVariable Long notificationId) {
        try {
            // Update seen data
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new Exception("Cannot find notification with id: " + notificationId));

            notification.setSeen(true);
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
