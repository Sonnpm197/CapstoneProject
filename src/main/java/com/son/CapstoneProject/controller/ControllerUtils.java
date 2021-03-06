package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.entity.AppUserTag;
import com.son.CapstoneProject.common.entity.Tag;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ArticleRepository articleRepository;

//    @Autowired
//    private AppUserDAO appUserDAO;

    /**
     * Only save tags which don't exist but still return all tags of the questions
     *
     * @param tags
     * @return
     */
    public List<Tag> saveDistinctiveTags(List<Tag> tags) {
        try {
            List<Tag> processedList = new ArrayList<>();
            if (tags != null) {
                for (Tag tag : tags) {
                    if (tag.getName() != null) {
                        tag.setName(tag.getName().toLowerCase().trim());
                    }
                    if (tag.getDescription() != null) {
                        tag.setDescription(tag.getDescription().toLowerCase().trim());
                    }

                    // Do not save if that tag existed
                    List<Tag> tagByName = tagRepository.findByName(tag.getName());
                    if (tagByName != null && tagByName.size() > 0) {
                        for (Tag tag1 : tagByName) {
                            if (!processedList.contains(tag1)) {
                                processedList.add(tag1);
                            }
                        }
                    } else {
                        tag = tagRepository.save(tag);
                        processedList.add(tag);
                    }
                }
            }

            return processedList;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    /**
     * Save new anonymous user based on his ip address or retrieve existed one
     *
     * @param ipAddress
     * @return
     */
    public AppUser saveOrReturnAnonymousUser(String ipAddress) throws Exception {

        try {
            if (ipAddress == null) {
                logger.error("ipAddress is null");
                return null;
            }

            // === Since we dont use Social Login by Spring, comment this === //

//        AppUserForm myForm = new AppUserForm();
//        myForm.setPassword("defaultPassword");
//        myForm.setAnonymous(true);
//        myForm.setIpAddress(ipAddress);
//
//        List<String> roleNames = new ArrayList<>();
//        // By default every user has this role
//        roleNames.add(AppRole.ROLE_USER);

            // ==============================================================//

            // Check if this anonymous user existed
            AppUser appUserByIpAddress = appUserRepository.findByIpAddress(ipAddress);
            if (appUserByIpAddress != null) {

                // Set last active date
                appUserByIpAddress.setLastActiveByUtilTimeStamp(new Date());
                return appUserRepository.save(appUserByIpAddress);
            }

            // Else if we cannot find user by ip address => create new one
            // ** Anonymous user is automatically a normal user
//            return appUserDAO.registerNewUserAccount(myForm, roleNames);

            AppUser newAppUser = new AppUser();
            newAppUser.setAnonymous(true);
            newAppUser.setIpAddress(ipAddress);
            newAppUser.setRole(ConstantValue.Role.ANONYMOUS.getValue());

            // Set created date and active date
            newAppUser.setCreatedTimeByUtilTimeStamp(new Date());
            newAppUser.setLastActiveByUtilTimeStamp(new Date());
            return appUserRepository.save(newAppUser);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    /**
     * checkUserId = true => check if appUser inside request has userId or not
     *
     * @param appUser
     * @param methodName
     * @param checkUserId
     */
    public void validateAppUser(AppUser appUser, String methodName, boolean checkUserId) throws Exception {
        if (appUser == null) {
            String message = methodName + ": Request body has no appUser";
            // logger.info(message);
            throw new Exception(message);
        }

        if (checkUserId) {
            if (appUser.getUserId() == null) {
                String message = methodName + ": AppUser from request body has no user id";
                // logger.info(message);
                throw new Exception(message);
            }
        }
    }

    public int numberOfQuestionsAndArticlesByTagId(Long tagId) {
        Integer numberOfQuestions = questionRepository.countNumberOfQuestionsByTagId(tagId);
        Integer numberOfArticles = articleRepository.countNumberOfArticlesByTagId(tagId);

        if (numberOfQuestions == null) {
            numberOfQuestions = 0;
        }

        if (numberOfArticles == null) {
            numberOfArticles = 0;
        }

        return numberOfQuestions + numberOfArticles;
    }

    public void removeAppUserTagAndTagIfHasNoRelatedQuestionsOrArticle(Long tagId) throws Exception {
        if (numberOfQuestionsAndArticlesByTagId(tagId) == 0) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new Exception("Cannot find tag with id" + tagId));

            List<AppUserTag> appUserTags = appUserTagRepository.findByTag_TagId(tagId);

            for (AppUserTag appUserTag : appUserTags) {
                appUserTagRepository.delete(appUserTag);
            }

            // then remove this tag
            tagRepository.delete(tagRepository.findById(tagId).get());
        }
    }
}
