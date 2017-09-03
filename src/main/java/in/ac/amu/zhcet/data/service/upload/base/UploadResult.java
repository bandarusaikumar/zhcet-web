package in.ac.amu.zhcet.data.service.upload.base;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class UploadResult<T> {
    @NonNull
    private List<String> errors = new ArrayList<>();
    @NonNull
    private List<T> uploads = new ArrayList<>();
}