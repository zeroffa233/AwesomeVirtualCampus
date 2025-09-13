package app.vcampus.server.controller;

import app.vcampus.server.entity.CachedImage;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageController {

    @RouteMapping(uri = "resource/images/all")
    public Response getAllImages(Request request, org.hibernate.Session database) {
        try {
            List<CachedImage> allImages = Database.loadAllData(CachedImage.class, database);

            return Response.Common.ok(allImages);
        } catch (Exception e) {
            log.error("Failed to fetch all images from cache table", e);
            return Response.Common.error("Failed to fetch images from database.");
        }
    }
}
