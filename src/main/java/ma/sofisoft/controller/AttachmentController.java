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
import org.jboss.resteasy.reactive.RestForm; // Import important pour le Multipart
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Slf4j
@Path("/attachments")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Attachment Resource", description = "Gestion hybride (Photos & Documents) pour Projet COSMOS")
public class AttachmentController {

    @Inject
    AttachmentService attachmentService;

    // UPLOAD
    @POST
    @Path("/{ownerType}/{ownerId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(
            @RestPath("ownerType") OwnerType ownerType,
            @RestPath("ownerId") UUID ownerId,
            @BeanParam CreateAttachmentRequest request) {

        log.info("Upload request received: Type={}, OwnerID={}", ownerType, ownerId);
        AttachmentResponse response = attachmentService.upload(ownerType, ownerId, request);

        return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
    }


     // GET BY ID : Recherche dans les deux tables
    @GET
    @Path("/{id}")
    public Response getById(@RestPath("id") UUID id) {
        log.info("🔍 Fetching metadata for ID: {}", id);
        AttachmentResponse response = attachmentService.getById(id);
        return Response.ok(response).build();
    }

    // LIST BY OWNER
    @GET
    @Path("/owner/{ownerType}/{ownerId}")
    public Response getByOwner(
            @RestPath("ownerType") OwnerType ownerType,
            @RestPath("ownerId") UUID ownerId) {

        log.info("Listing resources for {}/{}", ownerType, ownerId);
        List<AttachmentResponse> responses = attachmentService.getByOwner(ownerType, ownerId);
        return Response.ok(responses).build();
    }

    // DELETE
    @DELETE
    @Path("/{id}")
    public Response delete(@RestPath("id") UUID id) {
        log.info("🗑Deleting resource: {}", id);
        attachmentService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/owner/{ownerType}/{ownerId}")
    public Response deleteAllByOwner(
            @PathParam("ownerType") OwnerType ownerType,
            @PathParam("ownerId") UUID ownerId) {

        log.info("REST request to delete all attachments for owner: {}/{}", ownerType, ownerId);
        attachmentService.deleteByOwner(ownerType, ownerId);

        return Response.noContent().build();
    }
}