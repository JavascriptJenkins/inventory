package com.techvvs.inventory.service.auth

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TokenRepo
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TokenDAO
import com.techvvs.inventory.security.CookieUtils
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.security.Token
import com.techvvs.inventory.security.UserService
import com.techvvs.inventory.util.SendgridEmailUtil
import com.techvvs.inventory.util.TwilioTextUtil
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class TechvvsAuthService {


    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    TokenRepo tokenRepo

    @Autowired
    Environment env

    @Autowired
    SystemUserRepo systemUserRepo

    @Autowired
    SendgridEmailUtil emailManager

    @Autowired
    AppConstants appConstants

    @Autowired
    TwilioTextUtil textMagicUtil;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService


    SecureRandom secureRandom = new SecureRandom();

    @Autowired
    MenuHelper menuHelper;

    @Autowired
    UserDetailsService userDetailsService

    @Autowired
    CookieUtils cookieUtils


    void checkuserauth(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        model.addAttribute("isAuthenticated", isAuthenticated);
    }

    int getSystemIdOfCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        if(isAuthenticated){
            String username = authentication.getPrincipal().username
            SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(username)

            return systemUserDAO.id
        } else {
            return 0 // should never happen
        }
    }

    void updateJwtToken(String token, HttpServletResponse response){
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Wrap response to add SameSite attribute automatically
        def wrappedResponse = cookieUtils.wrapResponse(response);
        
        // Create secure JWT cookie using utility
        Cookie jwtCookie = cookieUtils.createSecureJwtCookie(token);
        
        // Add the cookie to the wrapped response (SameSite will be added automatically)
        wrappedResponse.addCookie(jwtCookie);
    }

    String getActiveCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        String token = ""
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("techvvs_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    boolean isTokenUsed(String token){
        System.out.println("token coming into isTokenUsed: "+token)
        Optional<TokenDAO> tokenDao = tokenRepo.findByToken(token)
        System.out.println("tokenDao.isEmpty(): "+tokenDao.isEmpty())
        if(tokenDao.isEmpty()){
            return true // this should never happen but if it does just return true
        }
        if (tokenDao.present && tokenDao.get().tokenused == 1) {
            System.out.println("tokenDao.get().tokenused: "+tokenDao.get().tokenused)
            System.out.println("tokenDao.present value: "+tokenDao.get().token)
            return true
        }
        if(tokenDao.present && tokenDao.get().tokenused == 0){
            System.out.println("tokenDao.get().tokenused: "+tokenDao.get().tokenused)
            System.out.println("tokenDao.present value: "+tokenDao.get().token)
            return false
        }

        return true
    }

    String checkAndDecodeJwtFromBase64(String jwtToCheck) {
        if (jwtToCheck?.contains('.')) {
            return jwtToCheck // It's already a JWT, return as-is
        } else {
            try {
                return new String(Base64.urlDecoder.decode(jwtToCheck)) // Decode if it's Base64 encoded
            } catch (Exception e) {
                return "Invalid Base64 encoding detected."
            }
        }
    }

    boolean sendMagicLoginLinkOverEmail(SystemUserDAO systemUserDAO){

        // generate a 24 hour login token

        // send email to the user



        ArrayList<String> list = new ArrayList<String>(1);
        list.add(systemUserDAO.getEmail());
        StringBuilder sb = new StringBuilder();

        List<Role> roles = new ArrayList<>(1);
        roles.add(Role.ROLE_CLIENT); // only putting this role in for now, so token doesn't get yanked from email and used for bad stuff.
        // will inject all the user's roles into the cookie when they click and login

        // get a link that will work for 15 minutes
        String emailtoken = jwtTokenProvider.createTokenForEmailValidationMagicLink(systemUserDAO.getEmail(), roles);

        // write the token to the database
        writeTokenToDatabaseForMagicLinkLogin(emailtoken, systemUserDAO.getEmail(), "15")

        String baseqrdomain = env.getProperty("base.qr.domain")

        boolean isdev1 = env.getProperty("spring.profiles.active").equals(appConstants.DEV_1)

        if(isdev1){
            baseqrdomain = "http://localhost:8080"
        }

        sb.append("Magic login link expires in 15 minutes. "+baseqrdomain+"/login/magiclinkgateway?customJwtParameter=" + emailtoken);

        emailManager.generateAndSendEmail(sb.toString(), list, "Login Link request for TECHVVS account");

        return true

    }

    @Transactional
    void writeTokenToDatabaseForMagicLinkLogin(String token, String email, String length){
        // write the token to the database
        TokenDAO tokenDAO = new TokenDAO(
                tokenused: 0,
                token: token,
                usermetadata: "email: " + email + " | minutes: " + length,
                createtimestamp: LocalDateTime.now(),
                updatedtimestamp: LocalDateTime.now()
        )
        tokenRepo.save(tokenDAO)
    }


    boolean runLoginRoutine(SystemUserDAO systemUserDAO){
        List<TokenDAO> tokenlist = tokenRepo.findTop10ByUsermetadataOrderByCreatetimestampAsc(systemUserDAO.getEmail());
        System.out.println("Size of Token list. " + String.valueOf(tokenlist.size()));
        if (tokenlist != null && tokenlist.size() > 0) {
            TokenDAO latest = tokenlist.get(0);
            if (latest.getTokenused() == 1) {
                System.out.println("User has token that is already used. Making a new one now. ");
                //send a new token
                boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
                textMagicUtil.createAndSendNewPhoneToken(systemUserDAO, isDev1);

            } else if (latest.getTokenused() == 0) {
                // have them validate existing token
                System.out.println("User has valid phone token not expired yet. ");
            }
        }

        // upon account creation we are putting a token in the database

        // if the user has no token yet we make one and text it here
        if (tokenlist != null && tokenlist.isEmpty()) {


            // save a token value to a username and then send a text message
            String tokenval = String.valueOf(100000 + secureRandom.nextInt(900000));  // Generate a 6-digit random number
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
                    result = textMagicUtil.sendValidationText(systemUserDAO, tokenval, isDev1);
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
                    textMagicUtil.createAndSendNewPhoneToken(systemUserDAO, isDev1);

                } else if (latest.getTokenused() == 0) {
                    // have them validate existing token
                    System.out.println("User has valid phone token not expired yet. ");
                }
            }


        }
    }

    String runLoginRoutineFromToken(TokenDAO tokenDAO,
                                     Model model,
                                     HttpServletResponse response){
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

                                // Wrap response to add SameSite attribute automatically
                                def wrappedResponse = cookieUtils.wrapResponse(response);
                                
                                // Create and add the JWT cookie using utility
                                Cookie cookie = cookieUtils.createSecureJwtCookie(token);
                                wrappedResponse.addCookie(cookie);
                                //
                                checkuserauth(model);

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
                    bindObjectsForMenuFunctionality(model);
                    return "auth/index.html";

                }
            }


        } else {
            model.addAttribute("errorMessage", "fill out required fields");
        }
    }


    void checkForDev1(Model model){
        boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));
        if(isDev1){
            model.addAttribute("isDev1",1); // if it is dev1 we will give option to fillout default values
        } else {
            model.addAttribute("isDev1",0);
        }

    }


    void bindObjectsForMenuFunctionality(Model model){
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(0);
        menuHelper.findMenus(model,  page, size);
    }


    void loginUsingMagicLink(String token, Model model, HttpServletResponse response) {
        // do a lookup on the token table

        Optional<TokenDAO> tokenDAO = tokenRepo.findByToken(token); // see if token exists>

        if(tokenDAO.isPresent()){




            // see if the token has been used
            if(tokenDAO.get().getTokenused() == 1){
                model.addAttribute("errorMessage", "Token has been used. Request a new Magic Link");
            }

            if(tokenDAO.get().getTokenused() == 0){

                // update token so it cannot be used again
                tokenDAO.get().setTokenused(1);
                tokenDAO.get().setUpdatedtimestamp(LocalDateTime.now());
                tokenRepo.save(tokenDAO.get());

                model.addAttribute("tknfromcontroller", tokenDAO);
                bindObjectsForMenuFunctionality(model)

                String email = jwtTokenProvider.getTokenSubject(token) // pull the email out from the token

                Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(email)); // see if user exists

                injectLoginCookie(token, response, existingUser, model);

                model.addAttribute("successMessage", "Logged in successfully using Magic Link. ");
            }


        } else {
            model.addAttribute("errorMessage", "Cannot Login. Request a new Magic Link");
        }


    }


    void injectLoginCookie(String token,
                           HttpServletResponse response,
                           Optional<SystemUserDAO> existingUser,
                           Model model) {

        if (existingUser.isPresent()) {


            /* Here we manually set the user details in the security context because we don't know the user's password */
            UserDetails userDetails = userDetailsService.loadUserByUsername(existingUser.get().getEmail());
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            SystemUserDAO user = systemUserRepo.findByEmail(existingUser.get().getEmail());
            String longtoken = jwtTokenProvider.createToken(existingUser.get().getEmail(), (List<Role>) Arrays.asList(user.getRoles()), user.getUiMode());


            // Wrap response to add SameSite attribute automatically
            def wrappedResponse = cookieUtils.wrapResponse(response);
            
            // Create and add the JWT cookie using utility
            Cookie cookie = cookieUtils.createSecureJwtCookie(longtoken);
            wrappedResponse.addCookie(cookie);
            //
            checkuserauth(model);


        }

    }


    String decodeShoppingToken(Optional<String> shoppingtoken) {
        if(shoppingtoken.isPresent()) {
            shoppingtoken = Optional.of(
                    checkAndDecodeJwtFromBase64(shoppingtoken.get())
            )
            return shoppingtoken
        }    else {
            return null
        }

    }
}
