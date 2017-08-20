package in.ac.amu.zhcet.data.model;

import in.ac.amu.zhcet.data.model.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude="attendance")
public class CourseRegistration extends BaseEntity {
    @Id
    private String id;

    @NonNull
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private FloatedCourse floatedCourse;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Student student;

    @Valid
    @NotNull
    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.ALL)
    private Attendance attendance = new Attendance();

    public CourseRegistration(Student student, FloatedCourse floatedCourse) {
        this.student = student;
        this.floatedCourse = floatedCourse;
    }

    @PrePersist
    public void setRelation() {
        id = floatedCourse.getId() + "_" + student.getEnrolmentNumber();
        attendance.setCourseRegistration(this);
    }

}