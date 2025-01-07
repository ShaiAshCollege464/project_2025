package com.ashcollege.controllers;

import com.ashcollege.entities.*;
import com.ashcollege.responses.BasicResponse;
import com.ashcollege.responses.LoginResponse;
import com.ashcollege.responses.RegisterResponse;
import com.ashcollege.responses.ValidationResponse;
import com.ashcollege.service.Persist;
import com.ashcollege.utils.ApiUtils;
import com.ashcollege.utils.Constants;
import com.ashcollege.utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
public class GeneralController {
private HashMap<String,UserEntity> tempUsers = new HashMap<>();
    @Autowired
    private Persist persist;

    @Autowired
    private StreamingController streamingController;
    @Autowired
    private NotificationStreamingController notificationStreamingController;

    @PostConstruct
    public void init() {

        createMissingFolders();



    }


    private String getMaterialsFolder () {
        String userHome = System.getProperty("user.home");
        String materialsFolder = String.format("%s%s%s", userHome, File.separator, "materiels");
        return materialsFolder;
    }

    private void createMissingFolders () {
        File file = new File(getMaterialsFolder());
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RequestMapping("/get-username-by-token")
    public String getNameByToken(String token){
        String name = null;
        UserEntity user = this.persist.getUserByPass(token);
        if (user!=null){
            name = user.getUsername();
        }
        return name;
    }

    @RequestMapping("/add-notification")
    public void addNotification(String token,int courseId,String title,String content){
        UserEntity user = this.persist.getUserByPass(token);
        CourseEntity course = this.persist.loadObject(CourseEntity.class,courseId);
        NotificationEntity notificationEntity=new NotificationEntity(user,course,title,content);
        this.notificationStreamingController.sendNotificationToAll(notificationEntity);
        this.persist.save(notificationEntity);
    }
    @RequestMapping("/get-permission")
    public int getPermission(String token){
        UserEntity user = this.persist.getUserByPass(token);
        return user.getRole().getId();
    }
    @RequestMapping("/get-courses-byLecturer")
    public List<CourseEntity>getCoursesByLecturer(int userId){
        int id=1;
        try {
          id = this.persist.getLIdByUserId(userId).getId();
            System.out.println("^^^" + id);
        }catch (Exception e){
            System.out.println("promblem");
        }
        System.out.println(this.persist.getCoursesByLecturerId(id));
        return this.persist.getCoursesByLecturerId(id);
    }

    @RequestMapping("/add-material-to-history")
    public void addMaterialToHistory(String token,int materialId){
        System.out.println(token);
        System.out.println("pppp"+materialId);

       UserEntity user =  this.persist.getUserByPass(token);
        MaterialEntity material = this.persist.loadObject(MaterialEntity.class,materialId);
       if (user!=null){
           System.out.println("ttttt "+user);
           List<MaterialHistoryEntity> materialHistoryEntities = this.persist.getMaterialHistoryByUserIdAndMaterialId( materialId,user.getId());
            if (materialHistoryEntities.isEmpty()){
                this.persist.save(new MaterialHistoryEntity(user,material));
            }else {
                materialHistoryEntities.get(0).setTime(new Date());
                this.persist.save(materialHistoryEntities.get(0));

            }
       }
    }
    @RequestMapping("/get-material-history")
    public List<MaterialHistoryEntity> getMaterialHistory(String token){
        List<MaterialHistoryEntity> materialHistoryEntities  = new ArrayList<>();
        System.out.println(token);
        UserEntity user = this.persist.getUserByPass(token);
        if (user!=null){
            System.out.println(user.getId());
            materialHistoryEntities = this.persist.getMaterialHistoryByUserId(user.getId());
        }
        System.out.println(materialHistoryEntities);
        return materialHistoryEntities;
    }

    @RequestMapping("/get-course")
    public CourseEntity getCourse(int id) {
        return this.persist.loadObject(CourseEntity.class, id);
    }

    @RequestMapping("/get-types")
    List<TypeEntity> getAllTypes() {
        return this.persist.loadList(TypeEntity.class);
    }

    @RequestMapping("/get-tags")
    List<TagEntity> getAllTags() {
        return this.persist.loadList(TagEntity.class);
    }

    @RequestMapping("/get-materials")
    public List<MaterialEntity> getMaterials() {
        return this.persist.loadList(MaterialEntity.class);
    }

    @RequestMapping("/get-materials-by-course-id")
    public List<MaterialEntity> getMaterialsByCourseId(int courseId) {
        return persist.getMaterialByCourseId(courseId);
    }

    @RequestMapping("/add-material")
    int addMaterial(String title, int type,
                     String token, int courseId, String description, int tag, String content) {
      int id = -1;
        try {
            UserEntity userEntity = this.persist.getUserByPass(token);
            int userId = userEntity.getId();
            MaterialEntity materialEntity = new MaterialEntity(title, type, userId, courseId, description, tag, content);
            this.persist.save(materialEntity);
            RecoveryEntity recovery = new RecoveryEntity("My Title", "My OTP",
                    "shaigivati464@gmail.com");
            this.persist.save(recovery);
         id = materialEntity.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("nfdn v j"+id);
      return id;
    }

    @RequestMapping("/recovery-password")
    public BasicResponse recoveryPassword(String email){
        boolean isExist = false;
        int errorCode = Constants.FAIL;
        UserEntity user = this.persist.getUserByEmail(email);
        if (user!=null){
            isExist = true;
            errorCode  =Constants.SUCCESS;
            String otp = GeneralUtils.generateOtp();
        RecoveryEntity recovery = new RecoveryEntity(user.getFullName(),otp,email);
        user.setPasswordRecovery(otp);
        this.persist.save(user);
        this.persist.save(recovery);
        }
        return new BasicResponse(isExist,errorCode);
    }

    @RequestMapping("/check-recovery-password")
     public BasicResponse checkRecoveryPassword(String email,String pass_recovery){
        boolean isValidPass = false;
        int errorCode = Constants.FAIL;
        UserEntity user = this.persist.getUserByEmailAndPasswordRecovery(email,pass_recovery);
        if (user!=null){
            user.setPasswordRecovery("");
            isValidPass  =true;
            errorCode = Constants.SUCCESS;
        }
        return new BasicResponse(isValidPass,errorCode);

    }

    @RequestMapping("/get-notifications")
    public List<NotificationEntity> getNotifications() {
        return this.persist.loadList(NotificationEntity.class);
    }

    @RequestMapping("/check-otp-to-register")
    public RegisterResponse addUser(String username,String otp) {
        boolean registeredSuccessfully = true;
        int errorCode = Constants.SUCCESS;
        UserEntity user = this.tempUsers.get(username);
        if (user==null||!user.getOtp().equals(otp)) {
            registeredSuccessfully = false;
            errorCode = Constants.FAIL;
        }
        if (user!=null&&user.getOtp().equals(otp)){
            user.setOtp("");
            this.tempUsers.remove(username);
            System.out.println("lll"+user);
          this.persist.save(user);
          if (user.getRole().getId()==2){
              LecturerEntity lecturer = new LecturerEntity(user.getFullName(),user);
              this.persist.save(lecturer);
          }
            System.out.println(user);
        }
        return new RegisterResponse(true, errorCode, registeredSuccessfully);
    }
    @RequestMapping("/register")
    public ValidationResponse register(String userName, String password, String name, String lastName,
                                       String email, String role, String phoneNumber){
        System.out.println("YYYYyy"+email);
        System.out.println("kkkk"+phoneNumber);
        boolean isValid = true;
        ValidationResponse validationResponse = new ValidationResponse();
        try {
            if (!isValid(userName,phoneNumber,email,validationResponse)) {
                isValid = false;
            } else {
                String hashed = GeneralUtils.hashMd5(userName , password);
                UserEntity user = new UserEntity(userName, hashed, name, lastName, email, role,phoneNumber);
                String otp = GeneralUtils.generateOtp();
                user.setOtp(otp);
                this.tempUsers.put(user.getUsername(), user);
                ApiUtils.sendSms(user.getOtp(), List.of(user.getPhoneNumber()));
            }
            System.out.println(userName);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        validationResponse.setSuccess(isValid);
        return validationResponse;
    }

    private boolean isValid(String userName, String phoneNumber, String email,ValidationResponse validationResponse) {
        boolean isValid = true;
        if (!isUsernameExists(userName)){
            validationResponse.setUsernameTaken(Constants.USERNAME_TAKEN);
            isValid = false;
        }
        if (!isValidPhoneNumber(phoneNumber)){
            validationResponse.setPhoneTaken(Constants.PHONE_TAKEN);
            isValid = false;
        }
        if (!isEmailExists(email)){
            validationResponse.setEmailTaken(Constants.EMAIL_TAKEN);
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if(GeneralUtils.isValidPhoneNumber(phoneNumber)){
            List<UserEntity> users = persist.loadList(UserEntity.class);
            System.out.println(users);
            List<UserEntity> temp = users.stream().filter(user ->user.getPhoneNumber()!=null&&user.getPhoneNumber().equals(phoneNumber)).toList();
            return temp.isEmpty();
        }
        return false;
    }


    @RequestMapping("/get-lecturers")
    public List<LecturerEntity> getLecturers() {
        return this.persist.loadList(LecturerEntity.class);
    }


    @RequestMapping("/get-all-courses")
    public List<CourseEntity> getAllCourses() {
        return this.persist.loadList(CourseEntity.class);
    }

    @RequestMapping("/add-course")
    public void addCourses(String name, String description, int lecturer) {
        CourseEntity course = new CourseEntity(name, description, lecturer);
        System.out.println("ccc:  " +lecturer);
        this.persist.save(course);
    }

//    @RequestMapping("/add-lecturer")
//    public void addLecturer(String name) {
//        LecturerEntity lecturerEntity = new LecturerEntity(name);
//        this.persist.save(lecturerEntity);
//    }


    @RequestMapping(value = "/check-otp", method = {RequestMethod.GET, RequestMethod.POST})
    public LoginResponse checkOtp(String username, String password,String otp) {

        LoginResponse response = new LoginResponse();
        String hash = GeneralUtils.hashMd5(username , password);
        UserEntity user = persist.getUserByUsernameAndPass(username, hash);
        System.out.println("pppppp"+user);
        if (user != null) {
            if (user.getOtp().equals(otp)){
                response.setSuccess(true);
                user.setOtp("");
                response.setPermission(user.getRole().getId());
                System.out.println(hash);
                response.setToken(hash);
                response.setId(user.getId());
                if (response.isSuccess()) {
                    response.setErrorCode(Constants.SUCCESS);
                } else {
                    response.setErrorCode(Constants.FAIL);
                }
            }

            persist.save(user);
        }

        return response;
    }
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public BasicResponse login(String username, String password){
        System.out.println("kk"+username);
        BasicResponse response = new BasicResponse();
        String hash = GeneralUtils.hashMd5(username,password);
        System.out.println(hash);
        UserEntity user = persist.getUserByUsernameAndPass(username, hash);

        if (user != null){
            String otp = GeneralUtils.generateOtp();
            user.setOtp(otp);
            persist.save(user);
            response.setSuccess(true);
            response.setErrorCode(Constants.SUCCESS);
            ApiUtils.sendSms(user.getOtp(), List.of(user.getPhoneNumber()));
        }else {
            response.setErrorCode(Constants.USER_NOT_EXIST);
        }
        return response;
    }
    @RequestMapping(value = "/get-material-files-by-id", method = RequestMethod.GET)
    public List<String> uploadFiles(int materialId,int userId) {
        try {
            System.out.println(materialId);
            String materialFolderPath = getMaterialsFolder() + File.separator + materialId+ File.separator+userId;
            Path dirPath = Paths.get(materialFolderPath);
            if (!Files.exists(dirPath)) {
                System.out.println("does not exist");
                return List.of();
            }
            List<String> files = new ArrayList<>();
            Files.list(dirPath).forEach(file -> {
                files.add(String.valueOf(file.getFileName()));
            });
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }
    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping(value = "/delete-material-files-by-id-and-name")
    public void deleteFiles ( int materialId,String[] fileNames,int userId) {
        for (String name : fileNames) {
            try {
                String filePath = getMaterialsFolder() + File.separator + materialId + File.separator+userId+ File.separator +name;
                Path file = Paths.get(filePath);
                if (Files.exists(file)) {
                    Files.delete(file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RequestMapping("/get-material-by-id")
    public MaterialEntity getMaterialById(int materialId) {
        return persist.getMaterialById(materialId);
    }


    @RequestMapping("/update-password")
    public BasicResponse updatePassword(String username, String password) {
        boolean isExist = false;
            int errorCode = Constants.FAIL;
        UserEntity user = persist.getUserByUsername(username);
        if (user != null) {
            user.setPassword(password);
            persist.save(user);
            isExist = true;
            errorCode = Constants.SUCCESS;
        }
        return new BasicResponse(isExist,errorCode);
    }


    public boolean isUsernameExists(String username) {
        List<UserEntity> users = persist.loadList(UserEntity.class);
        List<UserEntity> temp = users.stream().filter(user -> user.getUsername().equals(username)).toList();
        return temp.isEmpty();
    }
    public boolean isEmailExists( String email) {
        List<UserEntity> users = persist.loadList(UserEntity.class);
        List<UserEntity> temp = users.stream().filter(user -> user.getEmail().equals(email)).toList();
        return temp.isEmpty();
    }

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public Object hello() {
        return "Hello From Server";
    }
   @RequestMapping("/send-message")
   public boolean sendMessage(String message,String token){
        UserEntity user = this.persist.getUserByPass(token);
        MessageEntity messageEntity = new MessageEntity(message,user);
        this.persist.save(messageEntity);
       System.out.println(message);
        streamingController.sendToAll(messageEntity);
        return true;
   }


//    @RequestMapping(value = "/upload-file1", method = RequestMethod.POST)
//    public void uploadFile (@RequestParam(name = "file") MultipartFile file) {
//        File file1 = new File(getMaterialsFolder() + File.separator + file.getOriginalFilename());
//        try {
//            file.transferTo(file1);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    @RequestMapping("/get-material-file-by-id")
    public List<FileInfo> getFileNamesById(String id) {
        System.out.println("!!" + id);
        MaterialEntity materialEntity = this.persist.getMaterialById(Integer.parseInt(id));
        System.out.println(materialEntity.getUrl());

        if (materialEntity.getUrl() != null) {
            File dir = new File(materialEntity.getUrl());

            if (dir.exists() && dir.isDirectory()) {
                List<FileInfo> fileInfoList = new ArrayList<>();

                collectFileInfo(dir, fileInfoList);

                return fileInfoList;
            }
        }

        return new ArrayList<>();
    }

    private void collectFileInfo(File dir, List<FileInfo> fileInfoList) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectFileInfo(file, fileInfoList);
                } else {
                    // Extract uploadedBy based on user ID in the path
                    String uploadedBy = getUsernameFromPath(file.getAbsolutePath());
                    String fileId = file.getName().split("_")[0];
                    fileInfoList.add(new FileInfo(
                            file.getName(),
                            file.getAbsolutePath(),
                            file.length(),
                            uploadedBy,
                            fileId
                    ));
                }
            }
        }
    }

    @RequestMapping(value = "/download-file", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadFile(int id) {
        FileEntity fileEntity = this.persist.loadObject(FileEntity.class,id);
        Path filePathForDownload = Path.of(fileEntity.getUrl()+File.separator+fileEntity.getName());
        System.out.println(filePathForDownload);
        File file = filePathForDownload.toFile();

        if (file.exists()) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);
        } else {
            return null;
        }
    }


    private String getUsernameFromPath(String absolutePath) {
       absolutePath = absolutePath.replace("\\","%");
       String [] array = absolutePath.split("%");
        String fileNameOrId = array[array.length-2];
        try {
            int userId = Integer.parseInt(fileNameOrId);
            UserEntity userEntity = this.persist.loadObject(UserEntity.class, userId);

            if (userEntity != null) {
                return userEntity.getFullName();
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid user ID in path: " + absolutePath);
        }

        return "Unknown User";
    }







    @RequestMapping(value = "/upload-files", method = RequestMethod.POST)
public void uploadFiles(@RequestParam(name = "file") MultipartFile[] files,String materialId,int userId,String token) {
   UserEntity user = this.persist.getUserByPass(token);
   if (user!=null){
       System.out.println(files.length);
       for (MultipartFile file : files) {
           try {
               File dir = new File(getMaterialsFolder() + File.separator + materialId);
               dir.mkdir();
               File dirUserId = new File(dir+ File.separator+userId);
               dirUserId.mkdir();
               MaterialEntity materialEntity = this.persist.getMaterialById(Integer.parseInt(materialId));
               FileEntity fileEntity = new FileEntity(file.getName(),user,materialEntity,getMaterialsFolder()+File.separator+ materialId+File.separator+userId);
               fileEntity.setName(fileEntity.getId()+"_"+file.getOriginalFilename());
               this.persist.save(fileEntity);
               File fileToSave = new File(dirUserId+File.separator+fileEntity.getId()+"_"+file.getOriginalFilename());
               materialEntity.setUrl(getMaterialsFolder()+File.separator+ materialId);

               this.persist.save(materialEntity);
               file.transferTo(fileToSave);
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
       }
   }
   }


}
