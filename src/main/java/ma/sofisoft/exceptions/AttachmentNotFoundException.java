package ma.sofisoft.exceptions;

import java.util.Map;
import java.util.UUID;

public class AttachmentNotFoundException extends BusinessException{

    public AttachmentNotFoundException(UUID id) {
        super(
                "Attachment with id : " + id + " not found",
                "ATTACHMENT_NOT_FOUND",
                404,
                Map.of("id", id)
        );
    }
}
