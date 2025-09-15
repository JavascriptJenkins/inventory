package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class CustomerHelper {


    @Autowired
    CustomerRepo customerRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    @Autowired
    JwtTokenProvider jwtTokenProvider


    void loadBlankCustomer(Model model) {
        model.addAttribute("customer", new CustomerVO(customerid: 0))
    }

    CustomerVO validateCustomer(CustomerVO customer, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(customer, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(customer, model)

        // third - do any business logic / page specific validation below

        return customer
    }

    // only enforcing the name for now...
    CustomerVO createCustomer(CustomerVO customer) {


        customer.createTimeStamp = LocalDateTime.now()
        customer.updateTimeStamp = LocalDateTime.now()
        customer = customerRepo.save(customer)
        return customer
    }


    void addPaginatedData(Model model, Optional<Integer> page) {

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 100;
        Pageable pageable;
        if (currentPage == 0) {
            pageable = PageRequest.of(0, pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<CustomerVO> pageOfCustomer = customerRepo.findAll(pageable)

        //filter out soft deleted customer records
        pageOfCustomer.filter { it.deleted == 0 }

        int totalPages = pageOfCustomer.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfCustomer.getTotalPages());
        model.addAttribute("customerPage", pageOfCustomer);
    }

    void hydrateDisplayDataForActiveTokenPage(Model model) {
        Page<CustomerVO> pageOfCustomer = model.getAttribute("customerPage") as Page<CustomerVO>
        for(CustomerVO customer : pageOfCustomer) {
            if(jwtTokenProvider.validateTokenSimple(customer.shoppingtoken)) {
                customer.shoppingtokenexpired = 0
            } else {
                customer.shoppingtokenexpired = 1
            }
        }
        //String smsUrl = baseuri+"/menu/shop?shoppingtoken=" + shoppingtoken + "&menuid=" + menuid;

        // Sort the list in memory based on `shoppingtokenexpired`
        List<CustomerVO> sortedCustomers = pageOfCustomer.getContent()
                .stream()
                .sorted(Comparator.comparingInt { it.shoppingtokenexpired })
                .collect(Collectors.toList())

        // Replace the page content with the sorted list
        Page<CustomerVO> sortedPage = new PageImpl<>(sortedCustomers, pageOfCustomer.getPageable(), pageOfCustomer.getTotalElements())
        model.addAttribute("customerPage", sortedPage)

    }

    CustomerVO findCustomerById(Integer customerid) {
        Optional<CustomerVO> customerVO = customerRepo.findById(customerid)
        if (customerVO.isPresent() && customerVO.get().deleted == 0) {
            return customerVO.get()
        }
        return null
    }

    void deleteCustomer(Integer customerid, Model model) {
        CustomerVO customerVO = findCustomerById(customerid)
        if (customerVO != null) {
            customerVO.deleted = 1
            updateCustomer(customerVO, model)
            model.addAttribute(MessageConstants.SUCCESS_MSG, "Customer deleted successfully!")
        } else {
            model.addAttribute(MessageConstants.ERROR_MSG, "Customer failed to delete.")
        }
        loadBlankCustomer(model)
    }

    void getCustomer(Integer customerid, Model model) {
        CustomerVO customerVO = findCustomerById(customerid)
        if (customerVO != null) {
            model.addAttribute("customer", customerVO)
        } else {
            loadBlankCustomer(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Customer not found.")
        }
    }

    void updateCustomer(CustomerVO customerVO, Model model) {
        validateCustomer(customerVO, model)
        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {
            if (customerVO.customerid > 0) {
                customerVO.updateTimeStamp = LocalDateTime.now()
                try {
                    customerRepo.save(customerVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Customer updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new customer, use create method
                customerVO = createCustomer(customerVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Customer created successfully!")
            }
        }
        model.addAttribute("customer", customerVO)
    }

    void generateMembershipNumber(Integer customerid, Model model) {
        CustomerVO customerVO = findCustomerById(customerid)
        if (customerVO != null) {
            if (customerVO.membershipnumber == null || customerVO.membershipnumber.trim().isEmpty()) {
                // Generate a new GUID for membership number
                String membershipNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase()
                customerVO.membershipnumber = membershipNumber
                customerVO.updateTimeStamp = LocalDateTime.now()
                
                try {
                    customerRepo.save(customerVO)
                    model.addAttribute("customer", customerVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Membership number generated successfully: ${membershipNumber}")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Failed to generate membership number")
                }
            } else {
                model.addAttribute("customer", customerVO)
                model.addAttribute(MessageConstants.ERROR_MSG, "Customer already has a membership number")
            }
        } else {
            loadBlankCustomer(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Customer not found.")
        }
    }

}
