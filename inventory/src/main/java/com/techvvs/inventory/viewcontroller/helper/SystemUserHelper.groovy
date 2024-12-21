package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime
import java.util.regex.Pattern

@Component
class SystemUserHelper {

    @Autowired
    SystemUserRepo systemUserRepo
    
    @Autowired
    StringSecurityValidator securityValidator
    
    @Autowired
    ObjectValidator objectValidator

    void loadSystemUser(int systemuserid, Model model) {
        model.addAttribute("systemuser", systemUserRepo.findById(systemuserid).get())
    }

    void loadAllSystemUsers(Model model) {
        model.addAttribute("systemuserlist", systemUserRepo.findAll())
    }

    void addPaginatedData(Model model, Optional<Integer> page) {

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if (currentPage == 0) {
            pageable = PageRequest.of(0, pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<SystemUserDAO> pageOfSystemUser = systemUserRepo.findAll(pageable)

        //filter out soft deleted systemUserDAO records
        pageOfSystemUser.filter { it.deleted == 0 }

        int totalPages = pageOfSystemUser.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfSystemUser.getTotalPages());
        model.addAttribute("systemuserPage", pageOfSystemUser);
    }

    void updateSystemUser(SystemUserDAO systemUserDAO, Model model) {
        validateSystemUser(systemUserDAO, model)
        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {
            if (systemUserDAO.id > 0) {
                systemUserDAO.updatedtimestamp = LocalDateTime.now()
                try {
                    systemUserRepo.save(systemUserDAO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "SystemUser updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new systemUserDAO, use create method
                systemUserDAO = createSystemUser(systemUserDAO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "SystemUser created successfully!")
            }
        }
        model.addAttribute("systemuser", systemUserDAO)
    }


    
    SystemUserDAO validateSystemUser(SystemUserDAO systemUserDAO, Model model) {

        //  do any business logic / page specific validation below
        validateSystemUserData(systemUserDAO, model)


        // first - validate against security issues
        securityValidator.validateStringValues(systemUserDAO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(systemUserDAO, model)

        return systemUserDAO
    }

    SystemUserDAO createSystemUser(SystemUserDAO systemUserDAO) {

        systemUserDAO.createtimestamp = LocalDateTime.now()
        systemUserDAO.updatedtimestamp = LocalDateTime.now()
        systemUserDAO = systemUserRepo.save(systemUserDAO)
        return systemUserDAO
    }

    public static void validateSystemUserData(SystemUserDAO systemUserDAO, Model model) {
        boolean hasErrors = false;

        // Validate email
        if (systemUserDAO.getEmail() == null || systemUserDAO.getEmail().isEmpty()) {
            model.addAttribute("errorMessage", "Email cannot be null or empty.");
            hasErrors = true;
        } else if (!isValidEmail(systemUserDAO.getEmail())) {
            model.addAttribute("errorMessage", "Invalid email format.");
            hasErrors = true;
        }

        // Validate phone
        if (systemUserDAO.getPhone() == null || systemUserDAO.getPhone().isEmpty()) {
            model.addAttribute("errorMessage", "Phone number cannot be null or empty.");
            hasErrors = true;
        } else if (!isValidPhone(systemUserDAO.getPhone())) {
            model.addAttribute("errorMessage", "Invalid phone number format.");
            hasErrors = true;
        }

        // Validate password
        if (systemUserDAO.getPassword() == null || systemUserDAO.getPassword().isEmpty()) {
            model.addAttribute("errorMessage", "Password cannot be null or empty.");
            hasErrors = true;
        } else if (!systemUserDAO.getPassword().equals(systemUserDAO.getPassword2())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            hasErrors = true;
        }

        // Validate roles
        if (systemUserDAO.getRoles() == null || systemUserDAO.getRoles().length == 0) {
            model.addAttribute("errorMessage", "User must have at least one role assigned.");
            hasErrors = true;
        }

        // Validate tenant
        if (systemUserDAO.getTenant() == null || systemUserDAO.getTenant().isEmpty()) {
            model.addAttribute("errorMessage", "Tenant information is required.");
            hasErrors = true;
        }

        // Overall error check
        if (!hasErrors) {
            model.addAttribute("validationMessage", "Validation successful.");
        }
    }

    // Helper methods for validation
    private static boolean isValidEmail(String email) {
        String emailRegex = '^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$'
        return Pattern.matches(emailRegex, email);
    }

    private static boolean isValidPhone(String phone) {
        String phoneRegex = "\\d{10}";
        return Pattern.matches(phoneRegex, phone);
    }













}
