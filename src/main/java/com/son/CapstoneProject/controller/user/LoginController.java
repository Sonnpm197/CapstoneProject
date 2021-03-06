package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.common.ConstantValue;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.common.entity.login.SocialUser;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.repository.loginRepository.SocialUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@RestController
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    //    private List<String> adminEmails = new ArrayList<>(Arrays.asList("sonnpmse04810@fpt.edu.vn"));
//
//    @Autowired
//    private AppUserDAO appUserDAO;
//
    @Autowired
    private AppUserRepository appUserRepository;
//
//    @Autowired
//    private ConnectionFactoryLocator connectionFactoryLocator;
//
//    @Autowired
//    private UsersConnectionRepository userConnectionRepository;
//

    @Autowired
    private SocialUserRepository socialUserRepository;

    @GetMapping({"/", "/test"})
    @Transactional
    public String test() {
        return "Welcome to my project";
    }

    /**
     * Save socialUserInformation from Angular
     *
     * @return
     */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<AppUser> login(@RequestBody SocialUser socialUser) {
        try {
            String methodName = "LoginController.login";
            if (socialUser == null) {
                throw new Exception(methodName + ": socialUserInformation from request body is null");
            }

            // Check if this social user is existed
            SocialUser existedSocialUser = socialUserRepository.findById(socialUser.getId());

            // This user has existed => return appUser
            if (existedSocialUser != null) {
                Long socialUserInformationId = existedSocialUser.getSocialUserId();
                AppUser appUser = appUserRepository.findBySocialUser_SocialUserId(socialUserInformationId);

                // Update last active date
                appUser.setLastActiveByUtilTimeStamp(new Date());
                appUser = appUserRepository.save(appUser);

                return ResponseEntity.ok(appUser);
            }

            // Save socialUser from angular js first
            socialUser = socialUserRepository.save(socialUser);

            // Then create an appUser
            AppUser appUser = new AppUser();
            appUser.setSocialUser(socialUser);

            // Check if this is an admin or not
            if (socialUser.getEmail() != null
                    && ConstantValue.listAdmin.contains(socialUser.getEmail().toLowerCase())) {
                appUser.setRole(ConstantValue.Role.ADMIN.getValue());
            } else {
                appUser.setRole(ConstantValue.Role.USER.getValue());
            }

            // When creating new user set created date + last active date
            appUser.setCreatedTimeByUtilTimeStamp(new Date());
            appUser.setLastActiveByUtilTimeStamp(new Date());

            return ResponseEntity.ok(appUserRepository.save(appUser));
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
//
//    @GetMapping(value = "/userInfo", produces = "application/json")
//    public String adminPage(Principal principal) {
//
//        if (principal == null) {
//            return null;
//        }
//
//        String userName = principal.getName();
//
//        return appUserRepository.findByUserName(userName).toString();
//    }
//
//    @GetMapping(value = "/logoutSuccessful")
//    public String logoutSuccessfulPage() {
//        return "Logout successfully";
//    }
//
//    @GetMapping("/403")
//    public String accessDenied(Principal principal) {
//
//        if (principal != null) {
//            UserDetails loggedIn = (UserDetails) ((Authentication) principal).getPrincipal();
//
//            String message = "Hi " + principal.getName() //
//                    + "<br> You do not have permission to access this page!";
//
//            return message;
//        }
//
//        return "Permission Denied";
//    }
//
//    @GetMapping("/login")
//    public String login() {
//        return "/auth/facebook\n/auth/google";
//    }
//
//    @GetMapping(value = "/signin")
//    public String signIn(@RequestParam(value = "error", required = false) String error) {
//        return "Signin";
//    }
//
//    @GetMapping("/signup")
//    public AppUserForm signupPage(WebRequest request, HttpServletResponse response) {
//
//        ProviderSignInUtils providerSignInUtils
//                = new ProviderSignInUtils(connectionFactoryLocator, userConnectionRepository);
//
//        // Retrieve social networking information.
//        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
//
//        AppUserForm appUserForm = null;
//
//        if (connection != null) {
//            appUserForm = new AppUserForm(connection);
//        } else {
//            appUserForm = new AppUserForm();
//        }
//
//        appUserForm.setPassword("defaultPassword");
//
//        List<String> roleNames = new ArrayList<String>();
//
//        if (appUserForm.getEmail() != null) {
//            if (adminEmails.contains(appUserForm.getEmail().toLowerCase())) {
//                roleNames.add(AppRole.ROLE_ADMIN);
//            }
//        }
//
//        // By default every user has this role
//        roleNames.add(AppRole.ROLE_USER);
//
//        AppUser registered = null;
//
//        try {
//            registered = appUserDAO.registerNewUserAccount(appUserForm, roleNames);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        if (appUserForm.getSignInProvider() != null) {
//            // (Spring Social API): If user login by social networking.
//            // This method saves social networking information to the UserConnection table.
//            providerSignInUtils.doPostSignUp(registered.getUserName(), request);
//        }
//
//        // After registration is complete, automatic login.
//        logInUser(registered, roleNames);
//
//        try {
//            response.sendRedirect("/userInfo");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return appUserForm;
//    }
//
//    private void logInUser(AppUser user, List<String> roleNames) {
//
//        SocialUserDetails userDetails = new SocialUserDetailsImpl(user, roleNames);
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                userDetails,
//                null,
//                userDetails.getAuthorities());
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }

}