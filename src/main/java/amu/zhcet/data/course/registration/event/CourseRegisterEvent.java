package amu.zhcet.data.course.registration.event;

import amu.zhcet.data.course.registration.CourseRegistration;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRegisterEvent {
    private final CourseRegistration registration;
}
