package in.ac.amu.zhcet.service.csv;

import in.ac.amu.zhcet.data.model.CourseRegistration;
import in.ac.amu.zhcet.data.model.Student;
import in.ac.amu.zhcet.data.model.dto.upload.RegistrationUpload;
import in.ac.amu.zhcet.service.CourseManagementService;
import in.ac.amu.zhcet.service.CourseRegistrationService;
import in.ac.amu.zhcet.service.StudentService;
import in.ac.amu.zhcet.service.csv.base.Confirmation;
import in.ac.amu.zhcet.service.csv.base.AbstractUploadService;
import in.ac.amu.zhcet.service.csv.base.UploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static in.ac.amu.zhcet.utils.Utils.capitalizeAll;

@Slf4j
@Service
public class RegistrationUploadService {

    private boolean invalidEnrolment;
    private boolean alreadyEnrolled;

    private final StudentService studentService;
    private final CourseManagementService courseManagementService;
    private final CourseRegistrationService courseRegistrationService;
    private final AbstractUploadService<RegistrationUpload, CourseRegistration, String> uploadService;

    @Autowired
    public RegistrationUploadService(StudentService studentService, CourseManagementService courseManagementService, CourseRegistrationService courseRegistrationService, AbstractUploadService<RegistrationUpload, CourseRegistration, String> uploadService) {
        this.studentService = studentService;
        this.courseManagementService = courseManagementService;
        this.courseRegistrationService = courseRegistrationService;
        this.uploadService = uploadService;
    }

    public UploadResult<RegistrationUpload> handleUpload(MultipartFile file) throws IOException {
        return uploadService.handleUpload(RegistrationUpload.class, file);
    }

    private CourseRegistration fromRegistrationUpload(RegistrationUpload upload) {
        Student student = studentService.getByFacultyNumber(capitalizeAll(upload.getFacultyNo()));

        if (student == null)
            student = Student.builder().facultyNumber(upload.getFacultyNo()).build();

        CourseRegistration courseRegistration = new CourseRegistration();
        courseRegistration.setMode(upload.getMode());
        courseRegistration.setStudent(student);

        return courseRegistration;
    }

    private String getMappedValue(Student student, String courseId, List<CourseRegistration> registrations) {
        if (student.getEnrolmentNumber() == null) {
            invalidEnrolment = true;
            log.warn("Course Registration : Invalid Faculty Number {} {}", courseId, student.getFacultyNumber());
            return  "No such student found";
        } else if(registrations.stream()
                .map(CourseRegistration::getStudent)
                .anyMatch(oldStudent -> oldStudent.equals(student))) {
            alreadyEnrolled = true;
            log.warn("Student already enrolled in course : {} {}", courseId, student.getEnrolmentNumber());
            return "Already enrolled in " + courseId;
        } else {
            return null;
        }
    }

    public Confirmation<CourseRegistration, String> confirmUpload(String courseId, UploadResult<RegistrationUpload> uploadResult) {
        invalidEnrolment = false;
        alreadyEnrolled = false;

        List<CourseRegistration> registrations = courseManagementService.getFloatedCourseByCode(courseId).getCourseRegistrations();

        Confirmation<CourseRegistration, String> registrationConfirmation = uploadService.confirmUpload(
                uploadResult,
                this::fromRegistrationUpload,
                courseRegistration -> getMappedValue(courseRegistration.getStudent(), courseId, registrations)
        );

        if (invalidEnrolment)
            registrationConfirmation.getErrors().add("Invalid student faculty number found");
        if (alreadyEnrolled)
            registrationConfirmation.getErrors().add("Students already enrolled in course found");

        return registrationConfirmation;
    }

    @Transactional
    public void registerStudents(String courseId, Confirmation<CourseRegistration, String> confirmation) {
        courseRegistrationService.registerStudents(courseId, confirmation.getData().keySet());
    }

}