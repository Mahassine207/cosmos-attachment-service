package ma.sofisoft.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.AttachmentResponse;
import ma.sofisoft.dtos.CreateAttachmentRequest;
import ma.sofisoft.enums.OwnerType;
import ma.sofisoft.services.AttachmentService;
import org.jboss.resteasy.reactive.RestPath;
import java.util.List;
import java.util.UUID;

@Slf4j
@Path("/attachments")
@Produces(MediaType.APPLICATION_JSON)
public class AttachmentController {

    @Inject
    AttachmentService attachmentService;

    @POST
    @Path("/{ownerType}/{ownerId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(
            @RestPath OwnerType ownerType,
            @RestPath UUID ownerId,
            CreateAttachmentRequest request) {
        log.info("Upload request: Type={}, ID={}", ownerType, ownerId);
        AttachmentResponse response =
                attachmentService.upload(ownerType, ownerId, request);
        return Response.status(Response.Status.CREATED)
                .entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@RestPath UUID id) {
        log.info("Fetching attachment: {}", id);
        return Response.ok(
                attachmentService.getById(id)).build();
    }

    @GET
    @Path("/owner/{ownerType}/{ownerId}")
    public Response getByOwner(
            @RestPath OwnerType ownerType,
            @RestPath UUID ownerId) {
        log.info("Fetching attachments: {}/{}", ownerType, ownerId);
        List<AttachmentResponse> responses =
                attachmentService.getByOwner(ownerType, ownerId);
        return Response.ok(responses).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@RestPath UUID id) {
        log.info("Deleting attachment: {}", id);
        attachmentService.delete(id);
        return Response.noContent().build();
    }
}