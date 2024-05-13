package com.techvvs.inventory.viewcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.jparepo.TokenRepo;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.model.TokenDAO;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.Role;
import com.techvvs.inventory.security.Token;
import com.techvvs.inventory.security.UserService;
import com.techvvs.inventory.util.SendgridEmailUtil;
import com.techvvs.inventory.util.TwilioTextUtil;
import com.techvvs.inventory.validation.ValidateAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestMapping("/login")
@Controller
public class AuthViewController {


    @Autowired Environment environment;

    @Autowired
    UserService userService;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenProvider jwtTokenProvider;


    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TwilioTextUtil textMagicUtil;


    @Autowired
    SystemUserRepo systemUserRepo;

    @Autowired
    Environment env;

    @Autowired
    TokenRepo tokenRepo;

    SecureRandom secureRandom = new SecureRandom();

    @Autowired
    SendgridEmailUtil emailManager;

    @Autowired
    ValidateAuth validateAuth;

    //default home mapping
    @GetMapping
    String viewAuthPage(Model model) {


        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/auth.html";
    }

    //error page displayed when nobody is logged in
    @GetMapping("/error")
    String showErrorPage(Model model) {

        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/error.html";
    }

    //error page displayed when nobody is logged in
    @PostMapping("/logout")
    String logout(Model model, @RequestParam("customJwtParameter") String token, @ModelAttribute("systemuser") SystemUserDAO systemUserDAO) {


        model.addAttribute("customJwtParameter", "");
        SecurityContextHolder.getContext().setAuthentication(null); // clear the internal auth
        SecurityContextHolder.clearContext();
        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/auth.html";
    }

    @GetMapping("/createaccount")
    String viewCreateAccount(Model model) {

        checkForDev1(model);
        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/newaccount.html";
    }

    void checkForDev1(Model model){
        boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
        if(isDev1){
            model.addAttribute("isDev1",1); // if it is dev1 we will give option to fillout default values
        } else {
            model.addAttribute("isDev1",0);
        }
    }

    // display the page for typing in the token when user clicks link on phone
//    @GetMapping("/verifylinkphonetoken")
//    String verifylinkphonetoken(Model model, @RequestParam("email") String email){
//
//        TokenDAO token = new TokenDAO();
//        token.setUsermetadata(email);
//        model.addAttribute("token", token);
//        return "verifylinkphonetoken.html";
//    }

    // display the page for typing in the token
    @GetMapping("/verifyphonetoken")
    String verifyphonetoken(Model model, @RequestParam("email") String email) {

        TokenDAO token = new TokenDAO();
        token.setUsermetadata(email);
        model.addAttribute("tknfromcontroller", token);
        return "verifyphonetoken.html";
    }

    // this gets hit after user enters phone token and password
    @PostMapping("/verifyphonetoken")
    String postverifyphonetoken(Model model, @ModelAttribute("tknfromcontroller") TokenDAO tokenDAO) {

        if (tokenDAO != null && tokenDAO.getUsermetadata() != null && tokenDAO.getToken() != null) {

            if (tokenDAO.getToken().length() < 5) {
                model.addAttribute("errorMessage", "Token must be at least characters. ");
                model.addAttribute("tknfromcontroller", tokenDAO);
                checkForDev1(model);
                return "auth/authEnterPhoneToken.html";
            }


            Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(tokenDAO.getUsermetadata())); // see if user exists

            if (tokenDAO.getPassword() != null && tokenDAO.getPassword().length() > 1) {


                if (existingUser.isPresent() && passwordEncoder.matches(tokenDAO.getPassword(), existingUser.get().getPassword())) {
                    // this means passwords match
                    System.out.println("passwords match");

                } else {
                    System.out.println("passwords do not match");
                    model.addAttribute("tknfromcontroller", tokenDAO);
                    model.addAttribute("errorMessage", "Unable to login. ");
                    checkForDev1(model);
                    return "auth/authEnterPhoneToken.html";
                }
            } else {
                System.out.println("passwords not in correct format");
                model.addAttribute("tknfromcontroller", tokenDAO);
                model.addAttribute("errorMessage", "password is blank ");
                checkForDev1(model);
                return "auth/authEnterPhoneToken.html";
            }


            TokenDAO latest;
            List<TokenDAO> tokenlist = tokenRepo.findTop10ByUsermetadataOrderByCreatetimestampDesc(tokenDAO.getUsermetadata());
            if (tokenlist != null && tokenlist.size() > 0) {

                // take the most recent token generated for the user
                latest = tokenlist.get(0);

                boolean skip = false;
                // if we are in dev1 just mark any token valid and let the user in
                if ("dev1".equals(env.getProperty("spring.profiles.active"))){

                    skip = true;
                }


                // make sure token matches the one passed from controller
                if (!latest.getToken().equals(tokenDAO.getToken()) && !skip) {
                    // the tokens don't match then we send them back
                    model.addAttribute("errorMessage", "Token does not match.  Make sure you are entering the correct value or try logging in again. "); // todo: add a login link here
                    model.addAttribute("tknfromcontroller", tokenDAO);
                    checkForDev1(model);
                    return "auth/authEnterPhoneToken.html";
                }


                if (latest.getTokenused() == 1 && !skip) {
                    // if token is used send them back
                    model.addAttribute("errorMessage", "Try logging in again so a new token will be sent. "); // todo: add a login link here
                    model.addAttribute("tknfromcontroller", tokenDAO);
                    checkForDev1(model);
                    return "auth/authEnterPhoneToken.html";

                } else if (latest.getTokenused() == 0) {


                    latest.setUpdatedtimestamp(LocalDateTime.now());
                    latest.setTokenused(1);
                    // have them validate existing token
                    System.out.println("User has valid token, setting it to used now. ");
                    tokenRepo.save(latest);


                    // note - user will be active if the email link has been clicked
                    // insert jwt token minting here
                    try {

                        if (existingUser.isPresent() && existingUser.get().getIsuseractive() == 0 && !skip) {
                            model.addAttribute("tknfromcontroller", tokenDAO);
                            System.out.println("User exists but is not active.  User needs to activate email. ");
                            model.addAttribute("errorMessage", "Unable to login. If you have created an account, check your email (and spam) for account activation link. ");
                            //  return "auth/authVerifySuccess.html";
                            checkForDev1(model);
                            return "auth/authEnterPhoneToken.html";
                        } else if (existingUser.isEmpty() && !skip) {
                            model.addAttribute("tknfromcontroller", tokenDAO);
                            model.addAttribute("errorMessage", "Unable to login. If you have created an account, check your email (and spam) for account activation link.  ");
                            //return "auth/authVerifySuccess.html";
                            checkForDev1(model);
                            return "auth/authEnterPhoneToken.html";
                        }


                        if (existingUser.isPresent()) {
                            String token = userService.signin(
                                    existingUser.get().getEmail(),
                                    tokenDAO.getPassword()); // pass in plaintext password from server

                            Token token1;
                            if (token != null) {
                                System.out.println("SIGN-IN TOKEN GENERATED!!! ");
                                token1 = new Token();
                                token1.setToken(token);
                                model.addAttribute("customJwtParameter", token1.getToken());

                            } else {
                                System.out.println("TOKEN IS NULL THIS IS BAD BRAH! ");
                            }
                        }

                    } catch (Exception ex) {
                        model.addAttribute("errorMessage", "System Error");
                        System.out.println("TechVVS System Error in login: " + ex.getMessage());
                        model.addAttribute("tknfromcontroller", tokenDAO);
                        // return "auth/auth.html"; // return early with error
                        checkForDev1(model);
                        return "auth/authEnterPhoneToken.html";
                    }


                    model.addAttribute("successMessage", "Phone token verified. ");
                    model.addAttribute("tknfromcontroller", tokenDAO);
                    checkForDev1(model);
                    return "auth/index.html";

                }
            }


        } else {
            model.addAttribute("errorMessage", "fill out required fields");
        }

//        return "index.html";


        model.addAttribute("tknfromcontroller", new TokenDAO());
        return "auth/authEnterPhoneToken.html";
    }

    @GetMapping("/verify")
    String viewAuthVerify(@RequestParam("customJwtParameter") String token, Model model) {

        String email = jwtTokenProvider.getTokenSubject(token);

        // check if user exists
        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(email);

        if (systemUserDAO != null && email != null) {

            // update system user object
            systemUserDAO.setUpdatedtimestamp(LocalDateTime.now());
            systemUserDAO.setIsuseractive(1);

            try {
                systemUserRepo.save(systemUserDAO);
            } catch (Exception ex) {
                model.addAttribute("errorMessage", "System Error.  Try again or contact support: info@techvvs.io");
                System.out.println("Error in viewAuthVerify: " + ex.getMessage());
                return "auth/authVerifySuccess.html";
            }

            // add data for page display
            model.addAttribute("successMessage", "Success activating account with email: " + email);
        }
        return "auth/authVerifySuccess.html";
    }

    int checkTokenExpire(LocalDateTime tokencreated) {

        LocalDateTime oneHourFromTokenCreation = tokencreated.plusHours(1);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(oneHourFromTokenCreation)) {
            return 1; // this means one hour has passed and token is invalid
        }

        return 0;
    }

    @PostMapping("/createSystemUser")
    String createSystemUser(@ModelAttribute("systemuser") SystemUserDAO systemUserDAO, Model model) {

        //controllerHelper.checkForLoggedInStudent(model, httpServletRequest); // this will check to see if a student has already loggede in

        Role[] array = new Role[1];
        array[0] = Role.ROLE_CLIENT;
        systemUserDAO.setRoles(array); // add write access

        if ("dev1".equals(env.getProperty("spring.profiles.active"))) {
            System.out.println("setting isuseractive=1 because we are in dev");
            systemUserDAO.setIsuseractive(1); // set this to 1 when email token is created
        } else {
            systemUserDAO.setIsuseractive(0); // set this to 1 when email token is created
        }

        // systemUserDAO.setId(0);
        systemUserDAO.setCreatetimestamp(LocalDateTime.now());
        systemUserDAO.setUpdatedtimestamp(LocalDateTime.now());

        String errorResult = validateAuth.validateCreateSystemUser(systemUserDAO);

        Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(systemUserDAO.getEmail())); // see if user exists


        // Validation on student name
        if (!errorResult.equals("success")) {
            model.addAttribute("errorMessage", errorResult);
        } else if (existingUser.isPresent()) {
            model.addAttribute("errorMessage", "Cannot create account. ");
        } else {


            try {

                systemUserDAO.setPassword(passwordEncoder.encode(systemUserDAO.getPassword()));

                systemUserRepo.save(systemUserDAO);

                TokenDAO tokenVO = new TokenDAO();

                tokenVO.setEmail(systemUserDAO.getEmail());
                tokenVO.setTokenused(0);

                // send user an email link to validate account
                if ("dev1".equals(env.getProperty("spring.profiles.active"))) {
                    System.out.println("SKIPPING SENDING EMAIL BECAUSE WE ARE IN DEV");
                } else {
                    sendValidateEmailToken(tokenVO);
                }

                model.addAttribute("successMessage", "Check your email (and spam folder) to activate account: " + systemUserDAO.getEmail());
                return "auth/accountcreated.html";

            } catch (Exception ex) {
                model.addAttribute("errorMessage", "System Error");
                System.out.println("TechVVS System Error in createSystemUser: " + ex.getMessage());
                return "auth/newaccount.html"; // return early with error
            }


        }

        return "auth/newaccount.html";
    }

    // todo: remove passing the password from this html page (auth/auth.html)
    @PostMapping("/systemuser")
    String login(@ModelAttribute("systemuser") SystemUserDAO systemUserDAO, Model model, HttpServletResponse response) {


        Optional<SystemUserDAO> userfromdb = Optional.empty();
        String errorResult = validateAuth.validateLoginInfo(systemUserDAO);

        // todo: add a feature to make sure login pages are not abused with ddos (maybe nginx setting)
        // if the email is valid format, do a lookup to see if there is actually a user with this email in the system
        if ("success".equals(errorResult)) {
            userfromdb = Optional.ofNullable(systemUserRepo.findByEmail(systemUserDAO.getEmail()));


            // if there is no user with this email reject them
            if (userfromdb.isEmpty()) {
                System.out.println("User tried to login with an email that does not exist. ");
                errorResult = "Unable to login.  Check your email for validation link. ";
                model.addAttribute("errorMessage", errorResult);
                return "auth/auth.html"; // return early with error
            }

            // if user exists but is not active, tell them to check their email and validate
            if (userfromdb.get().getIsuseractive() == 0) {
                System.out.println("User exists but is not active. ");
                errorResult = "Unable to login. Check your email for validation link. ";
                model.addAttribute("errorMessage", errorResult);
                return "auth/auth.html"; // return early with error
            }


        }


        // Validation
        if (!errorResult.equals("success")) {
            model.addAttribute("errorMessage", errorResult);
            return "auth/auth.html"; // return early with error
        } else {


            // todo: check if one hour has passed on the token
            // pull token from database and see if 1 hour has passed and if the token is unused
            try {
                List<TokenDAO> tokenlist = tokenRepo.findTop10ByUsermetadataOrderByCreatetimestampAsc(systemUserDAO.getEmail());
                System.out.println("Size of Token list. " + String.valueOf(tokenlist.size()));
                if (tokenlist != null && tokenlist.size() > 0) {
                    TokenDAO latest = tokenlist.get(0);
                    if (latest.getTokenused() == 1) {
                        System.out.println("User has token that is already used. Making a new one now. ");
                        //send a new token
                        boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
                        textMagicUtil.createAndSendNewPhoneToken(userfromdb.get(), isDev1);

                    } else if (latest.getTokenused() == 0) {
                        // have them validate existing token
                        System.out.println("User has valid phone token not expired yet. ");
                    }
                }

                // upon account creation we are putting a token in the database

                // if the user has no token yet we make one and text it here
                if (tokenlist != null && tokenlist.isEmpty()) {


                    // save a token value to a username and then send a text message
                    String tokenval = String.valueOf(secureRandom.nextInt(1000000));
                    String result = "";
                    try {
                        TokenDAO tokenDAO = new TokenDAO();
                        tokenDAO.setUsermetadata(systemUserDAO.getEmail());
                        tokenDAO.setToken(tokenval);
                        tokenDAO.setTokenused(0);
                        tokenDAO.setCreatetimestamp(LocalDateTime.now());
                        tokenDAO.setUpdatedtimestamp(LocalDateTime.now());
                        tokenRepo.save(tokenDAO);
                        System.out.println("new token saved successfully for user with no existing token. ");

                    } catch (Exception ex) {
                        System.out.println("error inserting token into database");
                    } finally {
                        System.out.println("sending out validation text");
                        // only send the text message after everything else went smoothly
                        // todo : check result of this
                        boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
                        if(!isDev1){
                            result = textMagicUtil.sendValidationText(userfromdb.get(), tokenval, isDev1);
                        } else {
                            System.out.println("NOT sending validation text because we are in dev1");
                        }

                    }


                    // If we have 1 token in the database for a user, and that token is NOT used, do this
                    if (tokenlist != null && tokenlist.size() == 1) {
                        TokenDAO latest = tokenlist.get(0);
                        if (latest.getTokenused() == 1) {
                            System.out.println("User has token that is already used. Making a new one now. ");
                            boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
                            //send a new token
                            textMagicUtil.createAndSendNewPhoneToken(userfromdb.get(), isDev1);

                        } else if (latest.getTokenused() == 0) {
                            // have them validate existing token
                            System.out.println("User has valid phone token not expired yet. ");
                        }
                    }


                }


            } catch (Exception ex) {
                System.out.println("token issue: " + ex.getMessage());
            }

            // if token is expired or used, then send a new text message and insert a new token


            TokenDAO tokenDAO = new TokenDAO();
            tokenDAO.setUsermetadata(systemUserDAO.getEmail());
            //          model.addAttribute("successMessage","You are logged in: "+systemUserDAO.getEmail());
            model.addAttribute("systemuser", systemUserDAO);
            model.addAttribute("tknfromcontroller", tokenDAO);
        }

        //  return "auth/index.html";
        checkForDev1(model);
        return "auth/authEnterPhoneToken.html";
    }

    @GetMapping("/viewresetpass")
    String viewresetpass(@RequestParam("customJwtParameter") String token, Model model, HttpServletResponse response) {

        String email = jwtTokenProvider.getTokenSubject(token);

        SystemUserDAO systemUserDAO = new SystemUserDAO();
        systemUserDAO.setEmail(email);
        model.addAttribute("systemuser", systemUserDAO);

        return "authActuallyResetPassword.html";
    }

    @GetMapping("/resetpassword")
    String resetpasswordFromEmailLink(Model model, HttpServletResponse response) {

        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/authResetPassword.html";
    }

    @PostMapping("/actuallyresetpassword")
    String actuallyresetpassword(@ModelAttribute("systemuser") SystemUserDAO systemUserDAO, Model model, HttpServletResponse response) {


        String errorResult = validateAuth.validateActuallyResetPasswordInfo(systemUserDAO);

        // Validation
        if (!errorResult.equals("success")) {
            model.addAttribute("errorMessage", errorResult);
        } else {

            try {
                Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(systemUserDAO.getEmail())); // see if user exists

                if (existingUser.isPresent() && existingUser.get().getIsuseractive() == 0) {
                    model.addAttribute("errorMessage", "Unable to process request. ");
                    return "auth/authVerifySuccess.html";
                } else if (existingUser.isPresent() && existingUser.get().getIsuseractive() == 1) {

                    existingUser.get().setPassword(passwordEncoder.encode(systemUserDAO.getPassword())); // set new password here
                    systemUserRepo.save(existingUser.get());
                }

            } catch (Exception ex) {
                model.addAttribute("errorMessage", "System Error");
                System.out.println("TechVVS System Error: " + ex.getMessage());
                return "auth/authResetPassword.html"; // return early with error
            }
            model.addAttribute("successMessage", "Password change success! ");
        }

        return "auth/authVerifySuccess.html";
    }

    @PostMapping("/resetpasswordbyemail")
    String resetpasswordbyemail(@ModelAttribute("systemuser") SystemUserDAO systemUserDAO, Model model, HttpServletResponse response) {


        String errorResult = "success";

        if (systemUserDAO.getEmail().length() < 6
                || systemUserDAO.getEmail().length() > 200
                || !systemUserDAO.getEmail().contains("@")
                || !systemUserDAO.getEmail().contains(".com")

        ) {
            errorResult = "email must be between 6-200 characters and contain @ and .com";
        } else {
            systemUserDAO.setEmail(systemUserDAO.getEmail().trim());
            systemUserDAO.setEmail(systemUserDAO.getEmail().replaceAll(" ", ""));
        }

        // Validation
        if (!errorResult.equals("success")) {
            model.addAttribute("errorMessage", errorResult);
        } else {

            try {
                Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(systemUserDAO.getEmail())); // see if user exists

                if (existingUser.isPresent() && existingUser.get().getIsuseractive() == 0) {
                    model.addAttribute("successMessage", "If email exists in techvvs system, a link was sent to that email to reset password.  Check spam folder. ");
                    return "auth/authVerifySuccess.html";
                } else if (existingUser.isPresent() && existingUser.get().getIsuseractive() == 1) {

                    try {
                        ArrayList<String> list = new ArrayList<String>(1);
                        list.add(existingUser.get().getEmail());
                        StringBuilder sb = new StringBuilder();

                        List<Role> roles = new ArrayList<>(1);
                        roles.add(Role.ROLE_CLIENT);
                        String emailtoken = jwtTokenProvider.createTokenForEmailValidation(existingUser.get().getEmail(), roles);

                        sb.append("Change password for your techvvs account at http://localhost:8080/login/viewresetpass?customJwtParameter=" + emailtoken);

                        emailManager.generateAndSendEmail(sb.toString(), list, "Change password request TechVVS ServePapers account");
                    } catch (Exception ex) {
                        System.out.println("error sending email");
                        System.out.println(ex.getMessage());

                    }

                }


            } catch (Exception ex) {
                model.addAttribute("errorMessage", "System Error");
                System.out.println("TechVVS System Error: " + ex.getMessage());
                return "auth/authResetPassword.html"; // return early with error
            }
            model.addAttribute("successMessage", "If email exists in techvvs system, a link was sent to that email to reset password.  Check spam folder. ");
        }

        return "auth/auth.html";
    }





    void sendValidateEmailToken(TokenDAO tokenVO) {

        if (tokenVO.getEmail() != null &&
                tokenVO.getEmail().contains("@")) {

            TokenDAO newToken = new TokenDAO();
            //generate token and send email
            newToken.setTokenused(0);
            newToken.setUsermetadata(tokenVO.getEmail());
            newToken.setEmail(tokenVO.getEmail());
            newToken.setToken(String.valueOf(secureRandom.nextInt(1000000))); // todo: make this a phone token sent over text message
            newToken.setUpdatedtimestamp(LocalDateTime.now());
            newToken.setCreatetimestamp(LocalDateTime.now());


            try {
                ArrayList<String> list = new ArrayList<String>(1);
                list.add(newToken.getEmail());
                StringBuilder sb = new StringBuilder();

                List<Role> roles = new ArrayList<>(1);
                roles.add(Role.ROLE_CLIENT);
                String emailtoken = jwtTokenProvider.createTokenForEmailValidation(tokenVO.getEmail(), roles);

                sb.append("Verify your new account at http://localhost:8080/login/verify?customJwtParameter=" + emailtoken);

                emailManager.generateAndSendEmail(sb.toString(), list, "Validate email for new TechVVS ServePapers account");
            } catch (Exception ex) {
                System.out.println("error sending email");
                System.out.println(ex.getMessage());

            }

        }

    }




}
