package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.TaskRepo
import com.techvvs.inventory.jparepo.TaskTypeRepo
import com.techvvs.inventory.model.TaskTypeVO
import com.techvvs.inventory.model.TaskVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateTask
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom
import java.time.LocalDateTime

@RequestMapping("/task")
@Controller
public class TaskViewController {

    private final String UPLOAD_DIR = "./uploads/";

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    TaskRepo taskRepo;

    @Autowired
    TaskTypeRepo taskTypeRepo;

    @Autowired
    ValidateTask validateTask;
    
    @Autowired
    TechvvsAuthService techvvsAuthService


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "task" ) TaskVO taskVO, Model model){



        TaskVO taskVOToBind;
        if(taskVO != null && taskVO.getTask_id() != null){
            taskVOToBind = taskVO;
        } else {
            taskVOToBind = new TaskVO();
            taskVOToBind.setTasknumber(0);
            taskVOToBind.setTasknumber(secureRandom.nextInt(10000000));
            taskVOToBind.task_type_id = new TaskTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("task", taskVOToBind);
        bindTaskTypes(model)
        return "task/task.html";
    }

    void bindTaskTypes(Model model){
        // get all the tasktype objects and bind them to select dropdown
        List<TaskTypeVO> taskTypeVOS = taskTypeRepo.findAll();
        model.addAttribute("tasktypes", taskTypeVOS);
    }

    @GetMapping("/browseTask")
    String browseTask(@ModelAttribute( "task" ) TaskVO taskVO,
                             Model model,
                             
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<TaskVO> pageOfTask = taskRepo.findAll(pageable);

        int totalPages = pageOfTask.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfTask.getTotalPages());
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("task", new TaskVO());
        model.addAttribute("taskPage", pageOfTask);
        return "task/browsetask.html";
    }

    @GetMapping("/searchTask")
    String searchTask(@ModelAttribute( "task" ) TaskVO taskVO, Model model){



        techvvsAuthService.checkuserauth(model)
        model.addAttribute("task", new TaskVO());
        model.addAttribute("tasks", new ArrayList<TaskVO>(1));
        return "task/searchtask.html";
    }

    @PostMapping("/searchTask")
    String searchTaskPost(@ModelAttribute( "task" ) TaskVO taskVO, Model model){



        List<TaskVO> results = new ArrayList<TaskVO>();
        if(taskVO.getTasknumber() != null){
            System.out.println("Searching data by getTasknumber");
            results = taskRepo.findAllByTasknumber(taskVO.getTasknumber());
        }
        if(taskVO.getName() != null && results.size() == 0){
            System.out.println("Searching data by getName");
            results = taskRepo.findAllByName(taskVO.getName());
        }
        if(taskVO.getDescription() != null && results.size() == 0){
            System.out.println("Searching data by getDescription");
            results = taskRepo.findAllByDescription(taskVO.getDescription());
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("task", taskVO);
        model.addAttribute("tasks", results);
        return "task/searchtask.html";
    }

    @GetMapping("/editform")
    String viewEditForm(
                    Model model,
                    
                    @RequestParam("editmode") String editmode,
                    @RequestParam("tasknumber") String tasknumber){



        List<TaskVO> results = new ArrayList<TaskVO>();
        if(tasknumber != null){
            System.out.println("Searching data by tasknumber");
            results = taskRepo.findAllByTasknumber(Integer.valueOf(tasknumber));
        }

        // check to see if there are files uploaded related to this tasknumber
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(tasknumber), UPLOAD_DIR);

        if(filelist.size() > 0){
            model.addAttribute("filelist", filelist);
        } else {
            model.addAttribute("filelist", null);
        }

        if("yes".equals(editmode)){
            model.addAttribute("editmode",editmode)
        } else {
            model.addAttribute("editmode","no")
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("task", results.get(0));
        bindTaskTypes(model)
        return "task/edittask.html";
    }

    @PostMapping ("/editTask")
    String editTask(@ModelAttribute( "task" ) TaskVO taskVO,
                                Model model,
                                HttpServletResponse response
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateTask.validateNewFormInfo(taskVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
            model.addAttribute("editmode","yes") //
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            taskVO.setUpdateTimeStamp(LocalDateTime.now());



            TaskVO result = taskRepo.save(taskVO);

            // check to see if there are files uploaded related to this tasknumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(taskVO.getTasknumber(), UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("editmode","no") // after succesful edit is done we set this back to no
            bindTaskTypes(model)
            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("task", result);
        }

        techvvsAuthService.checkuserauth(model)
        bindTaskTypes(model)
        return "task/edittask.html";
    }

    @PostMapping ("/createNewTask")
    String createNewTask(@ModelAttribute( "task" ) TaskVO taskVO,
                                Model model,
                                HttpServletResponse response
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateTask.validateNewFormInfo(taskVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            taskVO.setCreateTimeStamp(LocalDateTime.now());
            taskVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for task types on the ui so we can save this task object
            TaskVO result = taskRepo.save(taskVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("task", result);
        }

        techvvsAuthService.checkuserauth(model)
        bindTaskTypes(model)
        return "task/task.html";
    }



}
