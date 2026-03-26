package ma.sofisoft.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;

// @Provider : registers this class as a JAX-RS exception mapper.
// @Slf4j : automatic log for all exceptions
@Provider
@Slf4j
public class GlobalExceptionHandler
        implements ExceptionMapper<Throwable> {

    // Injects the current request URI info + include the path in the error response
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";
        log.error("Exception caught in GlobalExceptionHandler: ",
                exception);

        // BusinessException → errorCode + statusCode définis
        if (exception instanceof BusinessException be) {
            return Response.status(be.getStatusCode())
                    .entity(ErrorResponse.of(
                            be.getStatusCode(),
                            be.getErrorCode(),
                            be.getMessage(),
                            path))
                    .build();
        }

        // EntityNotFoundException : thrown by JPA/Hibernate when findById returns nothing
        if (exception instanceof EntityNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "ENTITY_NOT_FOUND", exception.getMessage(), path))
                    .build();
        }

        // PersistenceException → 409
        if (exception instanceof PersistenceException
                && exception.getCause()
                instanceof ConstraintViolationException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.of(
                            409,
                            "DB_CONSTRAINT",
                            "Database constraint violation",
                            path))
                    .build();
        }

        // IllegalStateException → 409
        if (exception instanceof IllegalStateException) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.of(
                            409,
                            "ILLEGAL_STATE",
                            exception.getMessage(),
                            path))
                    .build();
        }

        // IllegalArgumentException → 400
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.of(
                            400,
                            "ILLEGAL_ARGUMENT",
                            exception.getMessage(),
                            path))
                    .build();
        }

        // MinIO erreur stockage → 500
        if (exception instanceof RuntimeException
                && exception.getMessage() != null
                && exception.getMessage().contains("MinIO")) {
            return Response.status(
                            Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(
                            500,
                            "MINIO_ERROR",
                            exception.getMessage(),
                            path))
                    .build();
        }

        // Fallback → 500
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.of(
                        500,
                        "INTERNAL_ERROR",
                        "An internal error occurred",
                        path))
                .build();
    }
}