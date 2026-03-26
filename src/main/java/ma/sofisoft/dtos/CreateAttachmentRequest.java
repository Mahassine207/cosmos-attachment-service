package ma.sofisoft.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import lombok.Data;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Data
public class CreateAttachmentRequest {

    @NotNull
    @FormParam("file")
    private FileUpload file;

    @FormParam("createdBy")
    private String createdBy;
}